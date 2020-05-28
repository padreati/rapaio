package rapaio.util.function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface DoubleDoubleFunction extends SFunction<Double, Double> {

    @Override
    @Deprecated
    default Double apply(Double value) {
        return applyAsDouble(value);
    }

    double applyAsDouble(double value);
}