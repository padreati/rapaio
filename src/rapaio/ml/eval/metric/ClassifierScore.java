package rapaio.ml.eval.metric;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/28/20.
 */
@Builder
@Getter
public final class ClassifierScore {

    @NonNull
    private final Double value;
}
