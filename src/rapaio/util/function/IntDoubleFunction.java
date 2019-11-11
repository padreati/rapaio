package rapaio.util.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface IntDoubleFunction extends Function<Integer, Double>, Serializable {

    @Override
    @Deprecated
    default Double apply(Integer value) {
        return applyDouble(value);
    }

    double applyDouble(int value);
}
