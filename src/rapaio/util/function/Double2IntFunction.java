package rapaio.util.function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/10/20.
 */
@FunctionalInterface
public interface Double2IntFunction extends SFunction<Double, Integer> {

    int applyAsInt(double value);

    @Override
    @Deprecated
    default Integer apply(Double value) {
        return applyAsInt(value);
    }
}
