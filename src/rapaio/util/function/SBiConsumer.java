package rapaio.util.function;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/7/20.
 */
@FunctionalInterface
public interface SBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {
}
