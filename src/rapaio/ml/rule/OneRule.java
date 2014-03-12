/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.ml.rule;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.ml.AbstractClassifier;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static rapaio.workspace.Workspace.code;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends AbstractClassifier<OneRule> {

	private static final Logger log = Logger.getLogger(OneRule.class.getName());

	private double minCount = 6;
	private String[] classLabels;
	private OneRuleSet bestRuleSet;
	private Nominal predict;

	@Override
	public OneRule newInstance() {
		return new OneRule().setMinCount(getMinCount());
	}

	public double getMinCount() {
		return minCount;
	}

	public OneRule setMinCount(double minCount) {
		this.minCount = minCount;
		return this;
	}

	@Override
	public void learn(Frame df, List<Double> weights, String classColName) {
		classLabels = df.getCol(classColName).getDictionary();

		validate(df, classColName);

		bestRuleSet = null;
		for (String sourceColName : df.getColNames()) {
			if (classColName.equals(sourceColName))
				continue;
			if (df.getCol(sourceColName).getType().isNominal()) {
				OneRuleSet ruleSet = buildNominal(sourceColName, classColName, df, weights);
				if (bestRuleSet == null || ruleSet.getAccuracy() > bestRuleSet.getAccuracy()) {
					bestRuleSet = ruleSet;
				}
			}
			if (df.getCol(sourceColName).getType().isNumeric()) {
				OneRuleSet ruleSet = buildNumeric(sourceColName, classColName, df, weights);
				if (bestRuleSet == null || ruleSet.getAccuracy() > bestRuleSet.getAccuracy()) {
					bestRuleSet = ruleSet;
				}
			}
		}
	}

	@Override
	public void predict(Frame test) {
		predict = new Nominal(test.getRowCount(), classLabels);
		for (int i = 0; i < test.getRowCount(); i++) {
			predict.setLabel(i, predict(test, i));
		}
	}

	@Override
	public Nominal getPrediction() {
		return predict;
	}

	@Override
	public Frame getDistribution() {
		return null;
	}

	private String predict(Frame test, int row) {
		if (bestRuleSet == null) {
			log.severe("Best rule not found. Either the classifier was not trained, either something went wrong.");
			return "?";
		}
		String colName = bestRuleSet.getColName();

		if (test.getCol(colName).getType().isNominal()) {
			String value = test.getLabel(row, test.getColIndex(colName));
			for (GenericOneRule oneRule : bestRuleSet.getRules()) {
				NominalOneRule nominal = (NominalOneRule) oneRule;
				if (nominal.getColValue().equals(value)) {
					return nominal.getPredictedClass();
				}
			}
		}
		if (test.getCol(colName).getType().isNumeric()) {
			boolean missing = test.getCol(colName).isMissing(row);
			double value = test.getValue(row, test.getColIndex(colName));
			for (GenericOneRule oneRule : bestRuleSet.getRules()) {
				NumericOneRule numeric = (NumericOneRule) oneRule;
				if (missing && numeric.isMissingValue()) {
					return numeric.getPredictedClass();
				}
				if (!missing && !numeric.isMissingValue() && value >= numeric.getMinValue() && value <= numeric.getMaxValue()) {
					return numeric.getPredictedClass();
				}
			}
		}
		return "?";
	}

	@Override
	public void summary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Classification: OneRule\n");
		sb.append("Parameters:{minCount:").append(minCount).append("}\n");
		sb.append("Best one rule:").append(bestRuleSet.toString()).append("\n");
		code(sb.toString());
	}

	private void validate(Frame df, String classColName) {
		int classIndex = df.getColIndex(classColName);
		if (classIndex >= df.getColCount()) {
			throw new IllegalArgumentException("classIndex is invalid");
		}
		if (!df.getCol(classIndex).getType().isNominal()) {
			throw new IllegalArgumentException("classIndex does not denote a isNominal vector");
		}
		if (df.getRowCount() == 0) {
			throw new IllegalArgumentException("Cannot learn from an empty frame");
		}
		if (df.getColCount() == 1) {
			throw new IllegalArgumentException("Cannot make rules based only the class vector");
		}
	}

	private OneRuleSet buildNominal(String sourceColName, String classColName, Frame df, List<Double> weights) {
		NominalOneRuleSet set = new NominalOneRuleSet(sourceColName);
		double[][] freq = new double[df.getCol(sourceColName).getDictionary().length][df.getCol(classColName).getDictionary().length];
		for (int i = 0; i < df.getRowCount(); i++) {
			freq[df.getIndex(i, sourceColName)][df.getIndex(i, classColName)] += weights.get(i);
		}
		for (int i = 0; i < freq.length; i++) {
			double[] hist = freq[i];
			double totalSubset = 0;
			double max = -1;
			int count = 0;
			for (double aHist : hist) {
				totalSubset += aHist;
				if (aHist > max) {
					max = aHist;
					count = 1;
					continue;
				}
				if (aHist == max && aHist != 0) {
					count++;
				}
			}
			if (count == 0) {
				continue;
			}
			int next = RandomSource.nextInt(count);
			String[] colValues = df.getCol(sourceColName).getDictionary();
			String[] classValues = df.getCol(classColName).getDictionary();
			for (int j = 0; j < hist.length; j++) {
				if (hist[j] == max && next > 0) {
					next--;
					continue;
				}
				if (hist[j] == max && totalSubset != 0) {
					set.getRules().add(new NominalOneRule(colValues[i], classValues[j], totalSubset, totalSubset - hist[j]));
					break;
				}
			}
		}
		return set;
	}

	private OneRuleSet buildNumeric(String sourceName, String className, Frame df, List<Double> weights) {
		NumericOneRuleSet set = new NumericOneRuleSet(sourceName);
		Vector sort = BaseFilters.sort(Vectors.newSeq(weights.size()),
				RowComparators.numericComparator(df.getCol(sourceName), true),
				RowComparators.nominalComparator(df.getCol(className), true));
		int pos = 0;
		while (pos < sort.getRowCount()) {
			if (df.isMissing(sort.getIndex(pos), sourceName)) {
				pos++;
				continue;
			}
			break;
		}

		// first process missing values
		if (pos > 0) {
			double[] hist = new double[classLabels.length];
			for (int i = 0; i < pos; i++) {
				hist[df.getIndex(sort.getIndex(i), className)] += weights.get(sort.getIndex(i));
			}
			List<Integer> best = new ArrayList<>();
			double max = Double.MIN_VALUE;
			double total = 0;
			for (int i = 0; i < hist.length; i++) {
				total += hist[i];
				if (max < hist[i]) {
					max = hist[i];
					best.clear();
					best.add(i);
					continue;
				}
				if (max == hist[i]) {
					best.add(i);
				}
			}
			int next = RandomSource.nextInt(best.size());
			set.getRules().add(new NumericOneRule(Double.NaN, Double.NaN, true, classLabels[next], total, total - max));
		}

		// now learn isNumeric intervals
		List<NumericOneRule> candidates = new ArrayList<>();

		//splits from same getValue
		int i = pos;
		int index;
		while (i < sort.getRowCount()) {
			// start a new bucket
			int startIndex = i;
			double[] hist = new double[classLabels.length];

			do { // fill it until it has enough of the majority class
				index = df.getIndex(sort.getIndex(i), className);
				hist[index] += weights.get(sort.getIndex(i));
				i++;
			} while (hist[index] < minCount && i < sort.getRowCount());

			// while class remains the same, keep on filling
			while (i < sort.getRowCount()) {
				index = sort.getIndex(i);
				if (df.getIndex(sort.getIndex(i), className) == index) {
					hist[index] += weights.get(sort.getIndex(i));
					i++;
					continue;
				}
				break;
			}
			// keep on while attr getValue is the same
			while (i < sort.getRowCount()
					&& df.getValue(sort.getIndex(i - 1), sourceName)
					== df.getValue(sort.getIndex(i), sourceName)) {
				index = df.getIndex(sort.getIndex(i), className);
				hist[index] += weights.get(sort.getIndex(i));
				i++;
			}

			List<Integer> best = new ArrayList<>();
			double max = Double.MIN_VALUE;
			double total = 0;

			for (int j = 0; j < hist.length; j++) {
				total += hist[j];
				if (max < hist[j]) {
					max = hist[j];
					best.clear();
					best.add(j);
					continue;
				}
				if (max == hist[j]) {
					best.add(j);
				}
			}
			int next = RandomSource.nextInt(best.size());
			double minValue = Double.NEGATIVE_INFINITY;
			if (startIndex != pos) {
				minValue = (df.getValue(sort.getIndex(startIndex), sourceName)
						+ df.getValue(sort.getIndex(startIndex - 1), sourceName)) / 2.;
			}
			double maxValue = Double.POSITIVE_INFINITY;
			if (i != sort.getRowCount()) {
				maxValue = (df.getValue(sort.getIndex(i - 1), sourceName) + df.getValue(sort.getIndex(i), sourceName)) / 2;
			}

			candidates.add(new NumericOneRule(minValue, maxValue, false,
					classLabels[best.get(next)],
					total,
					total - max));
		}

		NumericOneRule last = null;
		for (NumericOneRule rule : candidates) {
			if (last == null) {
				last = rule;
				continue;
			}
			if (last.getPredictedClass().equals(rule.getPredictedClass())) {
				last = new NumericOneRule(
						last.getMinValue(),
						rule.getMaxValue(),
						false,
						last.getPredictedClass(),
						last.getTotalCount() + rule.getTotalCount(),
						last.getErrorCount() + rule.getErrorCount());
			} else {
				set.getRules().add(last);
				last = rule;
			}
		}

		set.getRules().add(last);
		return set;
	}

}

abstract class OneRuleSet {

	private final String colName;

	public OneRuleSet(String colName) {
		this.colName = colName;
	}

	public String getColName() {
		return colName;
	}

	public abstract List<GenericOneRule> getRules();

	public int getErrorCount() {
		int count = 0;
		for (int i = 0; i < getRules().size(); i++) {
			count += getRules().get(i).getErrorCount();
		}
		return count;
	}

	public double getAccuracy() {
		double total = 0;
		double err = 0;
		for (int i = 0; i < getRules().size(); i++) {
			total += getRules().get(i).getTotalCount();
			err += getRules().get(i).getErrorCount();
		}
		return (total - err) / total;
	}

	@Override
	public String toString() {
		return "RuleSet{" + "colName=" + colName + ", accuracy=" + String.format("%.3f", getAccuracy()) + "}";
	}
}

class NominalOneRuleSet extends OneRuleSet {

	private final List<GenericOneRule> rules = new ArrayList<>();

	public NominalOneRuleSet(String colName) {
		super(colName);
	}

	@Override
	public List<GenericOneRule> getRules() {
		return rules;
	}

}

class NumericOneRuleSet extends OneRuleSet {

	private final List<GenericOneRule> rules = new ArrayList<>();

	public NumericOneRuleSet(String colName) {
		super(colName);
	}

	@Override
	public List<GenericOneRule> getRules() {
		return rules;
	}
}

interface GenericOneRule {

	String getPredictedClass();

	double getErrorCount();

	double getTotalCount();
}

class NominalOneRule implements GenericOneRule {

	private final String colValue;
	private final String predictedClass;
	private final double totalCount;
	private final double errorCount;

	public NominalOneRule(String colValue, String predictedClass, double totalCount, double errorCount) {
		this.colValue = colValue;
		this.predictedClass = predictedClass;
		this.totalCount = totalCount;
		this.errorCount = errorCount;
	}

	@Override
	public double getErrorCount() {
		return errorCount;
	}

	@Override
	public double getTotalCount() {
		return totalCount;
	}

	public String getColValue() {
		return colValue;
	}

	@Override
	public String getPredictedClass() {
		return predictedClass;
	}

	@Override
	public String toString() {
		return "Rule{"
				+ "getValue=" + colValue
				+ ", class=" + predictedClass
				+ ", errors=" + errorCount
				+ ", total=" + totalCount
				+ '}';
	}
}

class NumericOneRule implements GenericOneRule {

	private final double minValue;
	private final double maxValue;
	private final boolean missingValue;
	private final String predictedClass;
	private final double errorCount;
	private final double totalCount;

	public NumericOneRule(double minValue, double maxValue, boolean missingValue,
						  String predictedClass, double totalCount, double errorCount) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.missingValue = missingValue;
		this.predictedClass = predictedClass;
		this.errorCount = errorCount;
		this.totalCount = totalCount;
	}

	@Override
	public String getPredictedClass() {
		return predictedClass;
	}

	@Override
	public double getErrorCount() {
		return errorCount;
	}

	@Override
	public double getTotalCount() {
		return totalCount;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public boolean isMissingValue() {
		return missingValue;
	}

	@Override
	public String toString() {
		if (missingValue) {
			return "Rule{"
					+ "missing=" + missingValue
					+ ", class=" + predictedClass
					+ ", errors=" + errorCount
					+ ", total=" + totalCount
					+ '}';
		}
		return "Rule{"
				+ "min=" + minValue
				+ ", max=" + maxValue
				+ ", class=" + predictedClass
				+ ", errors=" + errorCount
				+ ", total=" + totalCount
				+ '}';
	}
}
