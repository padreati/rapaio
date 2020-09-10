package rapaio.util.function;

import java.util.function.IntUnaryOperator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
@FunctionalInterface
public interface Int2IntFunction extends SFunction<Integer, Integer>, IntUnaryOperator {

    int applyAsInt(int value);

    @Deprecated
    @Override
    default Integer apply(Integer value) {
        return applyAsInt(value);
    }
}
