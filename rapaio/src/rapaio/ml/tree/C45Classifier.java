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

package rapaio.ml.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;
import rapaio.ml.tools.DensityTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static rapaio.data.filters.BaseFilters.sort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class C45Classifier extends AbstractClassifier<C45Classifier> {

	/**
	 * Features which need to be implemented:
	 * - use grouping
	 */
	public static final int SELECTION_INFOGAIN = 0;
	public static final int SELECTION_GAINRATIO = 1;
	private double minWeight = 2; // min weight for at least 2 output groups in order for an attribute to be selected
	private int selection = SELECTION_GAINRATIO;
	;
	String[] dict;
	private C45ClassifierNode root;
	private Nominal prediction;
	private Frame distribution;

	public int getSelectionCriterion() {
		return selection;
	}

	public C45Classifier setSelectionCriterion(int selection) {
		this.selection = selection;
		return this;
	}

	public double getMinWeight() {
		return minWeight;
	}

	public C45Classifier setMinWeight(double minWeight) {
		this.minWeight = minWeight;
		return this;
	}

	@Override
	public Classifier newInstance() {
		return new C45Classifier().setMinWeight(minWeight).setSelectionCriterion(selection);
	}

	@Override
	public void learn(Frame df, List<Double> weights, String classColName) {
		dict = df.getCol(classColName).getDictionary();
		List<String> testColNames = new ArrayList<>();
		for (String colName : df.getColNames()) {
			if (colName.equals(classColName))
				continue;
			testColNames.add(classColName);
		}
		root = new C45ClassifierNode(this);
		root.learn(df, weights, testColNames, classColName);
	}

	@Override
	public void predict(Frame df) {
		prediction = new Nominal(df.getRowCount(), dict);
		distribution = Frames.newMatrixFrame(df.getRowCount(), dict);

		for (int i = 0; i < df.getRowCount(); i++) {
			double[] d = root.computeDistribution(df, i);
			for (int j = 0; j < dict.length; j++) {
				distribution.setValue(i, j, d[j]);
			}
			List<Integer> cand = new ArrayList<>();
			double max = 0;
			for (int j = 0; j < dict.length; j++) {
				if (distribution.getValue(i, j) > max) {
					max = distribution.getValue(i, j);
					cand.clear();
					cand.add(j);
					continue;
				}
				if (distribution.getValue(i, j) == max) {
					cand.add(j);
				}
			}
			prediction.setLabel(i, dict[RandomSource.nextInt(cand.size())]);
		}
	}

	@Override
	public Nominal getPrediction() {
		return prediction;
	}

	@Override
	public Frame getDistribution() {
		return distribution;
	}

	@Override
	public void summary() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}

class C45ClassifierNode {

	final C45Classifier parent;
	String testColName;
	double testValue; // used by numeric children to distinguish between left and right
	double totalWeight;
	boolean leaf = false;
	double[] distribution;
	Map<String, C45ClassifierNode> nominalChildren;
	C45ClassifierNode numericLeftChild;
	C45ClassifierNode numericRightChild;

	public C45ClassifierNode(C45Classifier parent) {
		this.parent = parent;
	}

	public void learn(Frame df, List<Double> weights, List<String> testColNames, String classColName) {
		totalWeight = 0;
		for (double weight : weights) {
			totalWeight += weight;
		}

		leaf = true;

		// if totalWeight < 2*minWeight we have a leaf
		if (totalWeight < 2 * parent.getMinWeight()) {
			distribution = new double[parent.dict.length];
			for (int i = 0; i < df.getRowCount(); i++) {
				distribution[df.getIndex(i, classColName)] += weights.get(i);
			}
			for (int i = 0; i < distribution.length; i++) {
				distribution[i] /= totalWeight;
			}
			return;
		}

		// if there is only one getLabel
		for (int i = 1; i < df.getRowCount(); i++) {
			if (df.getIndex(0, classColName) != df.getIndex(i, classColName)) {
				leaf = false;
				break;
			}
		}
		if (leaf) {
			distribution = new double[parent.dict.length];
			distribution[df.getIndex(0, classColName)] += 1.;
			return;
		}

		// try to find a good split
		double max_criteria = 0;
		String selColName = null;
		double selSplitValue = Double.NaN;

		for (String testColName : testColNames) {
			// for nominal columns
			if (df.getCol(testColName).getType().isNominal()) {
				DensityTable id = new DensityTable(df, weights, testColName, classColName);
				int count = id.countWithMinimum(false, parent.getMinWeight());
				if (count < 2) {
					continue;
				}
				double criteria = 0;
				if (parent.SELECTION_GAINRATIO == parent.getSelectionCriterion()) {
					criteria = id.getGainRatio(true);
				}
				if (parent.SELECTION_INFOGAIN == parent.getSelectionCriterion()) {
					criteria = id.getInfoGain(true);
				}
				if (criteria > max_criteria) {
					selColName = testColName;
					max_criteria = criteria;
				}
			}

			// for numeric columns
			if (df.getCol(testColName).getType().isNumeric()) {
				DensityTable id = new DensityTable(DensityTable.NUMERIC_DEFAULT_LABELS, parent.dict);
				Vector sort = Vectors.newSeq(df.getRowCount());
				sort = sort(sort, RowComparators.numericComparator(df.getCol(testColName), true));
				// first fill the density table
				for (int i = 0; i < df.getRowCount(); i++) {
					int pos = sort.getRowId(i);
					if (df.isMissing(pos, testColName)) {
						id.update(0, df.getIndex(pos, classColName), weights.get(pos));
					} else {
						id.update(2, df.getIndex(pos, classColName), weights.get(pos));
					}
				}
				// process the split points
				for (int i = 0; i < df.getRowCount(); i++) {
					int pos = sort.getRowId(i);
					if (df.isMissing(pos, testColName)) continue;
					id.move(2, 1, df.getIndex(pos, classColName), weights.get(pos));

					if (i < df.getRowCount() - 1
							&& df.getValue(pos, testColName)
							== df.getValue(sort.getRowId(i + 1), testColName)) {
						continue;
					}

					if (id.countWithMinimum(false, parent.getMinWeight()) < 2) {
						continue;
					}

					double criteria = 0;
					if (parent.SELECTION_GAINRATIO == parent.getSelectionCriterion()) {
						criteria = id.getGainRatio(true);
					}
					if (parent.SELECTION_INFOGAIN == parent.getSelectionCriterion()) {
						criteria = id.getInfoGain(true);
					}
					if (criteria > max_criteria) {
						selColName = testColName;
						selSplitValue = df.getValue(pos, testColName);
						max_criteria = criteria;
					}
				}
			}
		}

		// we have some split
		if (selColName != null) {
			if (df.getCol(selColName).getType().isNominal()) {
				int childrenCount = df.getCol(selColName).getDictionary().length;

				List<Integer>[] childIds = new List[childrenCount];
				List<Double>[] childWeights = new List[childrenCount];
				double[] childTotals = new double[childrenCount];
				double totalNonMissing = 0;

				for (int i = 0; i < childrenCount; i++) {
					childIds[i] = new ArrayList<>();
					childWeights[i] = new ArrayList<>();
				}

				// distribute non-missing
				for (int i = 0; i < df.getRowCount(); i++) {
					if (df.isMissing(i, selColName)) {
						continue;
					}
					int index = df.getIndex(i, selColName);
					childIds[index].add(i);
					childWeights[index].add(weights.get(i));
					childTotals[index] += weights.get(i);
				}

				// compute non missing totals
				for (int i = 0; i < childrenCount; i++) {
					totalNonMissing += childTotals[i];
				}

				// distribute missing
				for (int i = 0; i < df.getRowCount(); i++) {
					if (df.isMissing(i, selColName)) {
						for (int j = 0; j < childrenCount; j++) {
							if (childTotals[i] == 0) {
								continue;
							}
							childIds[j].add(i);
							childWeights[j].add(weights.get(i) * childTotals[j] / totalNonMissing);
						}
					}
				}

				// build children nodes
				nominalChildren = new HashMap<>();
				for (int i = 0; i < childrenCount; i++) {
					String label = df.getCol(selColName).getDictionary()[i];

					// TODO continue implementation here
				}
			}
		}
	}

	public double[] computeDistribution(Frame df, int row) {
		if (leaf) {
			return distribution;
		}
		// if missing aggregate all child nodes
		if (df.getCol(testColName).isMissing(row)) {
			if (df.getCol(testColName).getType().isNominal()) {
				double[] d = new double[parent.dict.length];
				for (Map.Entry<String, C45ClassifierNode> entry : nominalChildren.entrySet()) {
					double[] dd = entry.getValue().computeDistribution(df, row);
					for (int i = 0; i < dd.length; i++) {
						d[i] += dd[i] * entry.getValue().totalWeight / totalWeight;
					}
				}
				return d;
			}
			if (df.getCol(testColName).getType().isNumeric()) {
				double[] d = new double[parent.dict.length];
				double[] left = numericLeftChild.computeDistribution(df, row);
				double[] right = numericRightChild.computeDistribution(df, row);
				for (int i = 0; i < d.length; i++) {
					d[i] += left[i] * numericLeftChild.totalWeight / totalWeight;
					d[i] += right[i] * numericRightChild.totalWeight / totalWeight;
				}
				return d;
			}
			// should not be here
			return null;
		}

		// we have a getValue
		if (df.getCol(testColName).getType().isNominal()) {
			String label = df.getLabel(row, testColName);
			for (Map.Entry<String, C45ClassifierNode> entry : nominalChildren.entrySet()) {
				if (entry.getKey().equals(label)) {
					return entry.getValue().computeDistribution(df, row);
				}
			}
			// should not be here
			return null;
		}
		if (df.getCol(testColName).getType().isNumeric()) {
			if (df.getValue(row, testColName) <= testValue) {
				return numericLeftChild.computeDistribution(df, row);
			} else {
				return numericRightChild.computeDistribution(df, row);
			}
		}
		// should not be here
		return null;
	}
}
