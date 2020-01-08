package rapaio.util.function;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public interface IntIntDoubleBiFunction extends BiFunction<Integer, Integer, Double>, Serializable {

    @Override
    @Deprecated
    default Double apply(Integer i1, Integer i2) {
        return applyAsDouble(i1, i2);
    }

    double applyAsDouble(int i1, int i2);
}
