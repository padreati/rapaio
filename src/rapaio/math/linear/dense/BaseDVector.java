package rapaio.math.linear.dense;

import rapaio.data.VarDouble;
import rapaio.math.linear.AbstractDVector;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class BaseDVector extends AbstractDVector {

    public static BaseDVector wrap(double[] values) {
        return new BaseDVector(values.length, values);
    }

    private static final long serialVersionUID = -6444914455097469657L;

    protected final int size;
    protected final double[] values;

    protected BaseDVector(int size, double[] values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double get(int i) {
        return values[i];
    }

    @Override
    public void set(int i, double value) {
        values[i] = value;
    }

    public BaseDVector copy() {
        double[] newValues = new double[size];
        System.arraycopy(values, 0, newValues, 0, size);
        return new BaseDVector(size, newValues);
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).limit(size);
    }

    @Override
    public VarDouble asVarDouble() {
        return VarDouble.wrapArray(size, values);
    }

}
