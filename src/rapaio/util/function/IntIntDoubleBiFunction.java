package rapaio.util.function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public interface IntIntDoubleBiFunction extends SBiFunction<Integer, Integer, Double> {

    @Override
    @Deprecated
    default Double apply(Integer i1, Integer i2) {
        return applyAsDouble(i1, i2);
    }

    double applyAsDouble(int i1, int i2);
}
