package rapaio.util.function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/9/20.
 */
@FunctionalInterface
public interface Int2DoubleFunction extends SFunction<Integer, Double> {

    double applyAsDouble(int value);

    @Deprecated
    default Double apply(Integer value) {
        return applyAsDouble(value);
    }
}
