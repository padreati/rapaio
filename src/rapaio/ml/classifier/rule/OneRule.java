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

package rapaio.ml.classifier.rule;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.filter.var.VFRefSort;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.tools.DensityVector;

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
    public void learn(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);

        if (targetNames().length < 1) {
            throw new IllegalArgumentException("target variable names must not be empty");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("OneRule algorithm can't handle multiple targets");
        }

        bestRuleSet = null;
        for (String testCol : inputNames()) {
            RuleSet ruleSet = df.var(testCol).type().isNominal() ?
                    buildNominal(testCol, df, weights) :
                    buildNumeric(testCol, df, weights);
            if (bestRuleSet == null || ruleSet.getAccuracy() > bestRuleSet.getAccuracy()) {
                bestRuleSet = ruleSet;
            }
        }
    }

    @Override
    public CResult predict(final Frame test, final boolean withClasses, final boolean withDensities) {
        CResult pred = CResult.newEmpty(this, test, withClasses, withDensities);
        pred.addTarget(firstTargetName(), firstDict());

        for (int i = 0; i < test.rowCount(); i++) {
            String label = "";
            if (withClasses || withDensities) {
                label = predict(test, i);
            }
            if (withClasses) {
                pred.firstClasses().setLabel(i, label);
            }
            if (withDensities) {
                pred.firstDensity().setValue(i, label, 1.0);
            }
        }
        return pred;
    }

    private String predict(Frame test, int row) {
        if (bestRuleSet == null) {
            log.severe("Best rule not found. Either the classifier was not trained, either something went wrong.");
            return "?";
        }
        String colName = bestRuleSet.colName;

        if (test.var(colName).type().isNominal()) {
            String value = test.label(row, test.varIndex(colName));
            for (Rule oneRule : bestRuleSet.rules) {
                NominalRule nominal = (NominalRule) oneRule;
                if (nominal.getColValue().equals(value)) {
                    return nominal.getPredictedClass();
                }
            }
        }
        if (test.var(colName).type().isNumeric()) {
            boolean missing = test.var(colName).missing(row);
            double value = test.value(row, test.varIndex(colName));
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

    private RuleSet buildNominal(String testCol, Frame df, Var weights) {
        RuleSet set = new RuleSet(testCol);

        int len = df.var(testCol).dictionary().length;
        DensityVector[] dvs = new DensityVector[len];
        for (int i = 0; i < len; i++) {
            dvs[i] = new DensityVector(firstDict());
        }
        for (int i = 0; i < df.rowCount(); i++) {
            dvs[df.index(i, testCol)].update(df.index(i, firstTargetName()), weights.value(i));
        }
        for (int i = 0; i < len; i++) {
            DensityVector dv = dvs[i];
            dv.normalize(true);
            int j = dv.findBestIndex();
            String[] colValues = df.var(testCol).dictionary();
            set.rules.add(new NominalRule(colValues[i], firstDict()[j], dv.sum(true), dv.sum(true) - dv.get(j)));
        }
        return set;
    }

    private RuleSet buildNumeric(String testCol, Frame df, Var weights) {
        RuleSet set = new RuleSet(testCol);
        Var sort = new VFRefSort(RowComparators.numeric(df.var(testCol), true),
                RowComparators.nominal(df.var(firstTargetName()), true)).fitApply(Index.newSeq(weights.rowCount()));
        int pos = 0;
        while (pos < sort.rowCount()) {
            if (df.missing(sort.index(pos), testCol)) {
                pos++;
                continue;
            }
            break;
        }

        // first process missing values
        if (pos > 0) {
            double[] hist = new double[firstDict().length];
            for (int i = 0; i < pos; i++) {
                hist[df.index(sort.index(i), firstTargetName())] += weights.value(sort.index(i));
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
            set.rules.add(new NumericRule(Double.NaN, Double.NaN, true, firstDict()[next], total, total - max));
        }

        // now learn isNumeric intervals
        List<NumericRule> candidates = new ArrayList<>();

        //splits from same value
        int i = pos;
        int index;
        while (i < sort.rowCount()) {
            // start a new bucket
            int startIndex = i;
            double[] hist = new double[firstDict().length];

            do { // fill it until it has enough of the majority class
                index = df.index(sort.index(i), firstTargetName());
                hist[index] += weights.value(sort.index(i));
                i++;
            } while (hist[index] < minCount && i < sort.rowCount());

            // while class remains the same, keep on filling
            while (i < sort.rowCount()) {
                index = sort.index(i);
                if (df.index(sort.index(i), firstTargetName()) == index) {
                    hist[index] += weights.value(sort.index(i));
                    i++;
                    continue;
                }
                break;
            }
            // keep on while attr value is the same
            while (i < sort.rowCount()
                    && df.value(sort.index(i - 1), testCol)
                    == df.value(sort.index(i), testCol)) {
                index = df.index(sort.index(i), firstTargetName());
                hist[index] += weights.value(sort.index(i));
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
                minValue = (df.value(sort.index(startIndex), testCol)
                        + df.value(sort.index(startIndex - 1), testCol)) / 2.;
            }
            double maxValue = Double.POSITIVE_INFINITY;
            if (i != sort.rowCount()) {
                maxValue = (df.value(sort.index(i - 1), testCol) + df.value(sort.index(i), testCol)) / 2;
            }

            candidates.add(new NumericRule(minValue, maxValue, false,
                    firstDict()[best.get(next)],
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
                + "value=" + colValue
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
