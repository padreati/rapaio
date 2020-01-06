package rapaio.util.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface DoubleDoubleFunction extends Function<Double, Double>, Serializable {

    @Override
    @Deprecated
    default Double apply(Double value) {
        return applyAsDouble(value);
    }

    double applyAsDouble(double value);
}