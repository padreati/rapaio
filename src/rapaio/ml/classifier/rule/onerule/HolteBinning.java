package rapaio.ml.classifier.rule.onerule;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.rule.OneRule;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/30/20.
 */
public class HolteBinning implements OneRule.Binning {

    private final int minCount;

    public HolteBinning(int minCount) {
        this.minCount = minCount;
    }

    @Override
    public String name() {
        return "HolteBinning(minCount=" + minCount + ")";
    }

    @Override
    public RuleSet compute(String testVarName, OneRule parent, Frame df, Var weights) {
        RuleSet set = new RuleSet(testVarName);

        int[] rows = IntArrays.newSeq(0, df.rowCount());
        IntArrays.quickSort(rows, 0, rows.length, df.rvar(testVarName).refComparator());

        // find where missing values starts
        int len = 0;
        for (int row : rows) {
            if (df.isMissing(row, testVarName)) {
                break;
            }
            len++;
        }

        int i = 0;
        while (i < len) {

            int startIndex = i;

            // start a new bucket
            DensityVector hist = null;
            int bestIndex = Integer.MIN_VALUE;

            // while class remains the same, keep on filling
            while (i < len) {

                // compute a density for all next observations with the same value
                int j = i;
                DensityVector delta = DensityVector.empty(false, parent.firstTargetLevels());
                while (j < len && df.getDouble(rows[j], testVarName) == df.getDouble(rows[i], testVarName)) {
                    int index = df.getInt(j, parent.firstTargetName());
                    delta.increment(index, weights.getDouble(rows[i]));
                    j++;
                }

                // if it's the first interval than do initialization
                if (bestIndex == Integer.MIN_VALUE) {
                    hist = delta;
                    bestIndex = delta.findBestIndex();
                    i = j;
                    continue;
                }

                // if it predicts the same class as current interval, than incorporate this interval
                if ((hist.get(bestIndex) < minCount) || (hist.findBestIndex() == delta.findBestIndex())) {
                    for (int k = 0; k < hist.rowCount(); k++) {
                        hist.increment(k, delta.get(k));
                    }
                    bestIndex = hist.findBestIndex();
                    i = j;
                    continue;
                }

                // for other cases we skip accumulation
                break;
            }

            // min value of the interval
            double minValue = Double.NEGATIVE_INFINITY;
            if (set.getRules().size() > 0) {
                double a = df.getDouble(rows[startIndex], testVarName);
                double b = df.getDouble(rows[startIndex - 1], testVarName);
                minValue = b + (a - b) / 2.;
            }

            // max value of the interval
            double maxValue = Double.POSITIVE_INFINITY;
            if (i != len) {
                double a = df.getDouble(rows[i - 1], testVarName);
                double b = df.getDouble(rows[i], testVarName);
                maxValue = b + (a - b) / 2;
            }

            // create the interval rule and append it to rule set
            set.getRules().add(new NumericRule(minValue, maxValue, false, bestIndex, hist));
        }

        // now process missing values if there are such instances
        if (len < df.rowCount()) {
            DensityVector hist = DensityVector.empty(true, parent.firstTargetLevels());
            for (int j = len; j < df.rowCount(); j++) {
                hist.increment(df.getInt(rows[i], parent.firstTargetName()), weights.getDouble(rows[i]));
            }
            set.getRules().add(new NumericRule(Double.NaN, Double.NaN, true, hist.findBestIndex(), hist));
        }

        return set;
    }
}
