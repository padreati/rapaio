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

package rapaio.classifier.rule;

import rapaio.classifier.AbstractClassifier;
import rapaio.classifier.tools.DensityVector;
import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends AbstractClassifier {

    private static final Logger log = Logger.getLogger(OneRule.class.getName());

    private double minCount = 6;
    private RuleSet bestRuleSet;

    @Override
    public String name() {
        return "OneRule";
    }

    @Override
    public String fullName() {
        return String.format("OneRule (minCount=%.0f)", minCount);
    }

    @Override
    public OneRule newInstance() {
        return new OneRule().withMinCount(minCount);
    }

    public OneRule withMinCount(double minCount) {
        this.minCount = minCount;
        return this;
    }

    @Override
    public void learn(Frame df, String targetCol) {
        this.dict = df.col(targetCol).getDictionary();
        this.targetCol = targetCol;

        bestRuleSet = null;
        for (String testCol : df.colNames()) {
            if (targetCol.equals(testCol))
                continue;
            RuleSet ruleSet = df.col(testCol).type().isNominal() ?
                    buildNominal(testCol, df) :
                    buildNumeric(testCol, df);
            if (bestRuleSet == null || ruleSet.getAccuracy() > bestRuleSet.getAccuracy()) {
                bestRuleSet = ruleSet;
            }
        }
    }

    @Override
    public void predict(Frame test) {
        pred = new Nominal(test.rowCount(), dict);
        for (int i = 0; i < test.rowCount(); i++) {
            pred.setLabel(i, predict(test, i));
        }
    }

    private String predict(Frame test, int row) {
        if (bestRuleSet == null) {
            log.severe("Best rule not found. Either the classifier was not trained, either something went wrong.");
            return "?";
        }
        String colName = bestRuleSet.colName;

        if (test.col(colName).type().isNominal()) {
            String value = test.getLabel(row, test.colIndex(colName));
            for (Rule oneRule : bestRuleSet.rules) {
                NominalRule nominal = (NominalRule) oneRule;
                if (nominal.getColValue().equals(value)) {
                    return nominal.getPredictedClass();
                }
            }
        }
        if (test.col(colName).type().isNumeric()) {
            boolean missing = test.col(colName).isMissing(row);
            double value = test.getValue(row, test.colIndex(colName));
            for (Rule oneRule : bestRuleSet.rules) {
                NumericRule numeric = (NumericRule) oneRule;
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
    public void buildSummary(StringBuilder sb) {
        sb.append("Classification: OneRule\n");
        sb.append("Parameters:{minCount:").append(minCount).append("}\n");
        sb.append("Best one rule:").append(bestRuleSet.toString()).append("\n");
    }

    private RuleSet buildNominal(String testCol, Frame df) {
        RuleSet set = new RuleSet(testCol);

        int len = df.col(testCol).getDictionary().length;
        DensityVector[] dvs = new DensityVector[len];
        for (int i = 0; i < len; i++) {
            dvs[i] = new DensityVector(dict);
        }
        for (int i = 0; i < df.rowCount(); i++) {
            dvs[df.getIndex(i, testCol)].update(df.getIndex(i, targetCol), df.getWeight(i));
        }
        for (int i = 0; i < len; i++) {
            DensityVector dv = dvs[i];
            dv.normalize(true);
            int j = dv.findBestIndex();
            String[] colValues = df.col(testCol).getDictionary();
            set.rules.add(new NominalRule(colValues[i], dict[j], dv.sum(true), dv.sum(true) - dv.get(j)));
        }
        return set;
    }

    private RuleSet buildNumeric(String testCol, Frame df) {
        RuleSet set = new RuleSet(testCol);
        Vector sort = BaseFilters.sort(Vectors.newSeq(df.getWeights().rowCount()),
                RowComparators.numericComparator(df.col(testCol), true),
                RowComparators.nominalComparator(df.col(targetCol), true));
        int pos = 0;
        while (pos < sort.rowCount()) {
            if (df.isMissing(sort.getIndex(pos), testCol)) {
                pos++;
                continue;
            }
            break;
        }

        // first process missing values
        if (pos > 0) {
            double[] hist = new double[dict.length];
            for (int i = 0; i < pos; i++) {
                hist[df.getIndex(sort.getIndex(i), targetCol)] += df.getWeight(sort.getIndex(i));
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
            set.rules.add(new NumericRule(Double.NaN, Double.NaN, true, dict[next], total, total - max));
        }

        // now learn isNumeric intervals
        List<NumericRule> candidates = new ArrayList<>();

        //splits from same getValue
        int i = pos;
        int index;
        while (i < sort.rowCount()) {
            // start a new bucket
            int startIndex = i;
            double[] hist = new double[dict.length];

            do { // fill it until it has enough of the majority class
                index = df.getIndex(sort.getIndex(i), targetCol);
                hist[index] += df.getWeight(sort.getIndex(i));
                i++;
            } while (hist[index] < minCount && i < sort.rowCount());

            // while class remains the same, keep on filling
            while (i < sort.rowCount()) {
                index = sort.getIndex(i);
                if (df.getIndex(sort.getIndex(i), targetCol) == index) {
                    hist[index] += df.getWeight(sort.getIndex(i));
                    i++;
                    continue;
                }
                break;
            }
            // keep on while attr getValue is the same
            while (i < sort.rowCount()
                    && df.getValue(sort.getIndex(i - 1), testCol)
                    == df.getValue(sort.getIndex(i), testCol)) {
                index = df.getIndex(sort.getIndex(i), targetCol);
                hist[index] += df.getWeight(sort.getIndex(i));
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
                minValue = (df.getValue(sort.getIndex(startIndex), testCol)
                        + df.getValue(sort.getIndex(startIndex - 1), testCol)) / 2.;
            }
            double maxValue = Double.POSITIVE_INFINITY;
            if (i != sort.rowCount()) {
                maxValue = (df.getValue(sort.getIndex(i - 1), testCol) + df.getValue(sort.getIndex(i), testCol)) / 2;
            }

            candidates.add(new NumericRule(minValue, maxValue, false,
                    dict[best.get(next)],
                    total,
                    total - max));
        }

        NumericRule last = null;
        for (NumericRule rule : candidates) {
            if (last == null) {
                last = rule;
                continue;
            }
            if (last.getPredictedClass().equals(rule.getPredictedClass())) {
                last = new NumericRule(
                        last.getMinValue(),
                        rule.getMaxValue(),
                        false,
                        last.getPredictedClass(),
                        last.getTotalCount() + rule.getTotalCount(),
                        last.getErrorCount() + rule.getErrorCount());
            } else {
                set.rules.add(last);
                last = rule;
            }
        }

        set.rules.add(last);
        return set;
    }

}

class RuleSet {

    final String colName;
    final List<Rule> rules = new ArrayList<>();

    public RuleSet(String colName) {
        this.colName = colName;
    }

    public double getAccuracy() {
        double total = 0;
        double err = 0;
        for (Rule rule : rules) {
            total += rule.getTotalCount();
            err += rule.getErrorCount();
        }
        return (total - err) / total;
    }

    @Override
    public String toString() {
        return "RuleSet{" + "colName=" + colName + ", accuracy=" + String.format("%.3f", getAccuracy()) + "}";
    }
}

interface Rule {

    String getPredictedClass();

    double getErrorCount();

    double getTotalCount();
}

class NominalRule implements Rule {

    private final String colValue;
    private final String predictedClass;
    private final double totalCount;
    private final double errorCount;

    public NominalRule(String colValue, String predictedClass, double totalCount, double errorCount) {
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

class NumericRule implements Rule {

    private final double minValue;
    private final double maxValue;
    private final boolean missingValue;
    private final String predictedClass;
    private final double errorCount;
    private final double totalCount;

    public NumericRule(double minValue, double maxValue, boolean missingValue,
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
