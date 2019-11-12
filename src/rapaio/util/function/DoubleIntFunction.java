package rapaio.util.function;

import java.io.Serializable;
import java.util.function.DoubleToIntFunction;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/12/19.
 */
@FunctionalInterface
public interface DoubleIntFunction extends Function<Double, Integer>, DoubleToIntFunction, Serializable {

    @Override
    @Deprecated
    default Integer apply(Double value) {
        return applyAsInt(value);
    }
}
