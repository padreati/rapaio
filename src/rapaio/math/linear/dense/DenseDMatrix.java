package rapaio.math.linear.dense;

import rapaio.math.linear.DMatrix;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/3/19.
 */
public class DenseDMatrix implements DMatrix {

    private final int rows;
    private final int cols;
    private final double[][] values;

    private DenseDMatrix(int rows, int cols, double[][] values) {
        this.rows = rows;
        this.cols = cols;
        this.values = values;
    }

    @Override
    public int ndim() {
        return 2;
    }

    @Override
    public int[] shape() {
        return new int[]{rows, cols};
    }

    @Override
    public double get(int... index) {
        return values[index[0]][index[1]];
    }
}
