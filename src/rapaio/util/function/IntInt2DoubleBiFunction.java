package rapaio.util.function;

import java.util.function.ToDoubleBiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/6/20.
 */
@FunctionalInterface
public interface IntInt2DoubleBiFunction extends ToDoubleBiFunction<Integer, Integer> {

    @Override
    @Deprecated
    default double applyAsDouble(Integer integer, Integer integer2) {
        return applyIntIntAsDouble(integer, integer2);
    }

    double applyIntIntAsDouble(int integer, int integer2);
}
