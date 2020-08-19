package rapaio.math.linear.base;

import rapaio.data.VarDouble;
import rapaio.math.linear.DV;
import rapaio.math.linear.dense.DVDense;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class DVBase extends AbstractDV {

    public static DVBase wrap(double[] values) {
        return new DVBase(values.length, values);
    }

    private static final long serialVersionUID = -6444914455097469657L;

    protected final int size;
    protected final double[] values;

    protected DVBase(int size, double[] values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public Type type() {
        return Type.BASE;
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

    @Override
    public void increment(int i, double value) {
        values[i] += value;
    }

    @Override
    public DV copy(Type type) {
        double[] copy = new double[size];
        System.arraycopy(values, 0, copy, 0, size);
        switch (type) {
            case BASE:
                return DVBase.wrap(copy);
            case DENSE:
                return DVDense.wrap(copy);
            default:
                throw new IllegalArgumentException("DVType." + type.name() + " cannot be used to create a copy.");
        }
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
