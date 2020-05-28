package rapaio.ml.eval.split;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import rapaio.data.Frame;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
@Getter
@Builder
public class Split {

    @NonNull
    private final int round;
    @NonNull
    private final int fold;
    @NonNull
    private final Frame trainDf;
    @NonNull
    private final Frame testDf;
    private final Var trainWeights;
    private final Var testWeights;
}
