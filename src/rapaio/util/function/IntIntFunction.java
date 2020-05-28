package rapaio.util.function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface IntIntFunction extends SFunction<Integer, Integer> {

    @Override
    @Deprecated
    default Integer apply(Integer value) {
        return applyAsInt(value);
    }

    int applyAsInt(int value);
}
