package rapaio.supervised.rule;

import rapaio.data.*;
import rapaio.data.Vector;
import rapaio.supervised.AbstractClassifier;
import rapaio.supervised.ClassifierResult;

import java.io.Serializable;
import java.util.*;

import static rapaio.core.BaseMath.getRandomSource;
import static rapaio.explore.Workspace.code;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends AbstractClassifier {

    private int minCount = 6;
    private final List<OneRuleSet> learnedRules = new ArrayList<>();
    ;
    private String[] classDictionary;

    public OneRule() {
        this(6);
    }

    public OneRule(int minCount) {
        this.minCount = minCount;
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    @Override
    public void learn(Frame df, int classIndex) {
        classDictionary = df.getCol(classIndex).dictionary();

        validate(df, classIndex);
        learnedRules.clear();
        for (int i = 0; i < df.getColCount(); i++) {
            if (i == classIndex) {
                continue;
            }
            if (df.getCol(i).isNominal()) {
                learnedRules.add(buildNominal(df.getColNames()[i], df.getCol(i), df.getCol(classIndex)));
            }
            if (df.getCol(i).isNumeric()) {
                learnedRules.add(buildNumeric(df.getColNames()[i], df.getCol(i), df.getCol(classIndex)));
            }
        }

        Collections.sort(learnedRules, new ComparatorImpl());
    }

    @Override
    public ClassifierResult predict(Frame test) {
        Vector predict = new NominalVector("predict", test.getRowCount(), classDictionary);
        for (int i = 0; i < test.getRowCount(); i++) {
            predict.setLabel(i, predict(test, i));
        }
        return new OneRuleClassifierResult(test, predict);
    }

    private String predict(Frame test, int row) {
        if (learnedRules.isEmpty()) {
            throw new RuntimeException("OneRule must be trained first before prediction.");
        }
        OneRuleSet rule = learnedRules.get(0);
        String colName = rule.getColName();

        if (test.getCol(colName).isNominal()) {
            String value = test.getLabel(row, test.getColIndex(colName));
            for (GenericOneRule oneRule : rule.getRules()) {
                NominalOneRule nominal = (NominalOneRule) oneRule;
                if (nominal.getColValue().equals(value)) {
                    return nominal.getPredictedClass();
                }
            }
        }
        if (test.getCol(colName).isNumeric()) {
            boolean missing = test.getCol(colName).isMissing(row);
            double value = test.getValue(row, test.getColIndex(colName));
            for (GenericOneRule oneRule : rule.getRules()) {
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
    public void printModelSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Classification: OneRule\n");
        sb.append("Parameters:{minCount:").append(minCount).append("}\n");
        sb.append("Learned rules:").append(learnedRules.size()).append("\n");
        for (int i = 0; i < learnedRules.size(); i++) {
            sb.append("").append(i + 1).append(". ").append(learnedRules.get(i).toString()).append("\n");
            for (GenericOneRule rule : learnedRules.get(i).getRules()) {
                sb.append(" - ").append(rule.toString()).append("\n");
            }
        }
        code(sb.toString());
    }

    private void validate(Frame df, int classIndex) {
        if (classIndex >= df.getColCount()) {
            throw new IllegalArgumentException("classIndex is invalid");
        }
        if (!df.getCol(classIndex).isNominal()) {
            throw new IllegalArgumentException("classIndex does not denote a isNominal vector");
        }
        if (df.getRowCount() == 0) {
            throw new IllegalArgumentException("Cannot learn from an empty frame");
        }
        if (df.getColCount() == 1) {
            throw new IllegalArgumentException("Cannot make rules based only the class vector");
        }
    }

    private OneRuleSet buildNominal(String string, Vector sourceCol, Vector classCol) {
        NominalOneRuleSet set = new NominalOneRuleSet(string);
        int[][] freq = new int[sourceCol.dictionary().length][classCol.dictionary().length];
        for (int i = 0; i < sourceCol.getRowCount(); i++) {
            freq[sourceCol.getIndex(i)][classCol.getIndex(i)]++;
        }
        for (int i = 0; i < freq.length; i++) {
            int[] hist = freq[i];
            int totalSubset = 0;
            int max = -1;
            int count = 0;
            for (int j = 0; j < hist.length; j++) {
                totalSubset += hist[j];
                if (hist[j] > max) {
                    max = hist[j];
                    count = 1;
                    continue;
                }
                if (hist[j] == max && hist[j] != 0) {
                    count++;
                }
            }
            if (count == 0) {
                continue;
            }
            int next = getRandomSource().nextInt(count);
            String[] colValues = sourceCol.dictionary();
            String[] classValues = classCol.dictionary();
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

    private OneRuleSet buildNumeric(String string, Vector sourceCol, Vector classCol) {
        NumericOneRuleSet set = new NumericOneRuleSet(string);
        Frame df = new SolidFrame("", sourceCol.getRowCount(), new Vector[]{sourceCol, classCol});
        df = new SortedFrame(df, sourceCol.getComparator(true));
        int pos = -1;
        while (pos < df.getRowCount()) {
            if (df.getCol(0).isMissing(pos + 1)) {
                pos++;
                continue;
            }
            break;
        }

        int[] hist = new int[classCol.dictionary().length];

        // first process missing values
        if (pos != -1) {
            Arrays.fill(hist, 0);
            for (int i = 0; i <= pos; i++) {
                hist[df.getIndex(i, 1)]++;
            }
            int totalSubset = 0;
            int max = -1;
            int count = 0;
            for (int i = 0; i < hist.length; i++) {
                totalSubset += hist[i];
                if (max < hist[i]) {
                    max = hist[i];
                    count = 1;
                    continue;
                }
                if (max == hist[i]) {
                    count++;
                }
            }
            int next = getRandomSource().nextInt(count);
            String[] classLabels = classCol.dictionary();
            for (int j = 0; j < hist.length; j++) {
                if (hist[j] == max && next > 0) {
                    next--;
                    continue;
                }
                if (hist[j] == max) {
                    set.getRules().add(new NumericOneRule(Double.NaN, Double.NaN, true, classLabels[j],
                            totalSubset, totalSubset - hist[j]));
                    break;
                }
            }
        }
        pos++;

        // now build isNumeric intervals
        List<NumericOneRule> candidates = new ArrayList<>();

        Arrays.fill(hist, 0);
        int max = -1;
        int minIndex = pos;
        for (int i = pos; i < df.getRowCount(); i++) {
            hist[df.getIndex(i, 1)]++;
            if (max < hist[df.getIndex(i, 1)]) {
                max = hist[df.getIndex(i, 1)];
            }

            if (i == df.getRowCount() - 1) {
                // last getValue, compute rule anyway
                int count = 0;
                for (int h : hist) {
                    if (h == max) {
                        count++;
                    }
                }
                int next = getRandomSource().nextInt(count);
                for (int j = 0; j < hist.length; j++) {
                    if (hist[j] == max && next > 0) {
                        next--;
                        continue;
                    }
                    if (hist[j] == max) {
                        double minValue = Double.NEGATIVE_INFINITY;
                        if (minIndex != pos) {
                            minValue = (df.getValue(minIndex, 0) + df.getValue(minIndex - 1, 0)) / 2.;
                        }
                        double maxValue = Double.POSITIVE_INFINITY;
                        candidates.add(new NumericOneRule(minValue, maxValue, false,
                                df.getCol(1).dictionary()[j],
                                i - minIndex + 1,
                                i - minIndex + 1 - max));
                    }
                }
                break;
            }

            // next are rules to continue no matter how
            if ((i > pos) && Double.compare(df.getValue(i - 1, 0), df.getValue(i, 0)) == 0) {
                continue;
            }
            if ((i > pos) && Double.compare(df.getIndex(i - 1, 1), df.getValue(i, 1)) == 0) {
                continue;
            }

            if (max >= minCount) {

                // we have enough sample of a dominant category
                int count = 0;
                for (int h : hist) {
                    if (h == max) {
                        count++;
                    }
                }
                int next = getRandomSource().nextInt(count);
                for (int j = 0; j < hist.length; j++) {
                    if (hist[j] == max && next > 0) {
                        next--;
                        continue;
                    }
                    if (hist[j] == max) {
                        double minValue = Double.NEGATIVE_INFINITY;
                        if (minIndex != pos) {
                            minValue = (df.getValue(minIndex, 0) + df.getValue(minIndex - 1, 0)) / 2.;
                        }
                        double maxValue = Double.POSITIVE_INFINITY;
                        if (i != df.getRowCount() - 1) {
                            maxValue = (df.getValue(i, 0) + df.getValue(i + 1, 0)) / 2;
                        }
                        candidates.add(new NumericOneRule(minValue, maxValue, false,
                                df.getCol(1).dictionary()[j],
                                i - minIndex + 1,
                                i - minIndex + 1 - max));
                    }
                }

                Arrays.fill(hist, 0);
                minIndex = i + 1;
                max = 0;
            }
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

    private static class ComparatorImpl implements Serializable, Comparator<OneRuleSet> {

        @Override
        public int compare(OneRuleSet o1, OneRuleSet o2) {
            double err1 = o1.getErrorCount();
            double err2 = o2.getErrorCount();
            if (Double.compare(err1, err2) == 0) {
                int rule1 = o1.getRules().size();
                int rule2 = o2.getRules().size();
                if (rule1 == rule2) {
                    return 0;
                }
                return (rule1 < rule2) ? -1 : 1;
            }
            return (err1 < err2) ? -1 : 1;
        }
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

    int getErrorCount();

    int getTotalCount();
}

class NominalOneRule implements GenericOneRule {

    private final String colValue;
    private final String predictedClass;
    private final int totalCount;
    private final int errorCount;

    public NominalOneRule(String colValue, String predictedClass, int totalCount, int errorCount) {
        this.colValue = colValue;
        this.predictedClass = predictedClass;
        this.totalCount = totalCount;
        this.errorCount = errorCount;
    }

    @Override
    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public int getTotalCount() {
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
    private final int errorCount;
    private final int totalCount;

    public NumericOneRule(double minValue, double maxValue, boolean missingValue,
                          String predictedClass, int totalCount, int errorCount) {
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
    public int getErrorCount() {
        return errorCount;
    }

    @Override
    public int getTotalCount() {
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
