package rapaio.ml.distance;

import rapaio.data.Frame;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class EuclideanDistance implements Distance {

    @Override
    public double measure(Frame df, int row1, int row2) {
        return measure(df, row1, df, row2);
    }

    @Override
    public double measure(Frame df1, int row1, Frame df2, int row2) {
        double total = 0;
        for (int i = 0; i < df1.getColCount(); i++) {
            if (df1.getCol(i).isNominal()) {
                if (df1.getIndex(row1, i) == df2.getIndex(row2, i)) {
                    total++;
                }
            } else {
                total += df1.getValue(row1, i) * df2.getValue(row2, i);
            }
        }
        return total;
    }
}
