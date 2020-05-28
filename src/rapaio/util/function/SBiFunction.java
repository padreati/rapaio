package rapaio.util.function;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/28/20.
 */
@FunctionalInterface
public interface SBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {
}
