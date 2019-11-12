package rapaio.util.function;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface IntDoubleFunction extends Function<Integer, Double>, IntToDoubleFunction, Serializable {

    @Override
    @Deprecated
    default Double apply(Integer value) {
        return applyAsDouble(value);
    }
}
