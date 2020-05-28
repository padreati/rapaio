package rapaio.util.function;

import java.util.function.IntToDoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface IntDoubleFunction extends SFunction<Integer, Double>, IntToDoubleFunction {

    @Override
    @Deprecated
    default Double apply(Integer value) {
        return applyAsDouble(value);
    }
}
