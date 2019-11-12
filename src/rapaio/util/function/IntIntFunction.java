package rapaio.util.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface IntIntFunction extends Function<Integer, Integer>, Serializable {

    @Override
    @Deprecated
    default Integer apply(Integer value) {
        return applyAsInt(value);
    }

    int applyAsInt(int value);
}
