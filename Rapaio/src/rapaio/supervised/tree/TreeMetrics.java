package rapaio.supervised.tree;

import rapaio.data.Frame;
import rapaio.filters.FilterGroupByNominal;

import static rapaio.core.BaseMath.log;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class TreeMetrics {

    public double entropy(Frame df, int classIndex) {
        int[] hits = new int[df.getCol(classIndex).dictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            hits[df.getCol(classIndex).getIndex(i)]++;
        }
        double entropy = 0.;
        for (int i = 0; i < hits.length; i++) {
            if (hits[i] != 0) {
                double p = hits[i] / (1. * df.getRowCount());
                entropy += -p * log(p) / log(2);
            }
        }
        return entropy;
    }

    public double infoGain(Frame df, int ClassIndex, int splitIndex) {
        Frame[] split = new FilterGroupByNominal().groupByNominal(df, splitIndex);
        double infoGain = entropy(df, ClassIndex);
        for (Frame f : split) {
            if (f == null) {
                continue;
            }
            infoGain -= (f.getRowCount() / (1. * df.getRowCount())) * entropy(f, ClassIndex);
        }
        return infoGain;
    }
}
