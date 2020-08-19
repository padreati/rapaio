package rapaio.math.linear.base;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import rapaio.math.linear.DM;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Basic implementation of a matrix which uses array of arrays. This implementation
 * uses only API interface for implementations. The purpose of this implementation is to
 * offer default implementation baselines for all specialization matrices.
 * <p>
 * On purpose, this implementation helps implementers in two purposes:
 * <ul>
 *     <li>offers a performance baseline for specific implementations</li>
 *     <li>offers a skeleton implementation to make performance improvement development possible
 *     in an incremental manner</li>
 * </ul>
 * <p>
 * This class is a reference implementation and it is not intended to be used in
 * performance critical operations.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class DMBase extends AbstractDM {

    public static DMBase empty(int rowCount, int colCount) {
        return new DMBase(rowCount, colCount);
    }

    public static DMBase wrap(double[][] values) {
        return new DMBase(values.length, (values.length == 0) ? 0 : values[0].length, values);
    }

    private static final long serialVersionUID = -7586346894985345827L;

    protected final int rowCount;
    protected final int colCount;
    protected final double[][] values;

    protected DMBase(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = new double[rowCount][colCount];
    }

    protected DMBase(int rowCount, int colCount, double[][] values) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = values;
    }

    @Override
    public Type type() {
        return Type.BASE;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int colCount() {
        return colCount;
    }

    @Override
    public double get(int row, int col) {
        return values[row][col];
    }

    @Override
    public void set(int row, int col, double value) {
        values[row][col] = value;
    }

    @Override
    public void increment(int row, int col, double value) {
        values[row][col] += value;
    }

    @Override
    public DM apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                values[i][j] = fun.applyAsDouble(values[i][j]);
            }
        }
        return this;
    }

    @Override
    public DM t() {
        DMBase t = new DMBase(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.set(j, i, get(i, j));
            }
        }
        return t;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).flatMapToDouble(Arrays::stream);
    }

    @Override
    public DMBase copy() {
        DMBase copy = new DMBase(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
        }
        return copy;
    }
}
