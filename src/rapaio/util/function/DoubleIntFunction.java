package rapaio.util.function;

import java.util.function.DoubleToIntFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/12/19.
 */
@FunctionalInterface
public interface DoubleIntFunction extends SFunction<Double, Integer>, DoubleToIntFunction {

    @Override
    @Deprecated
    default Integer apply(Double value) {
        return applyAsInt(value);
    }
}
