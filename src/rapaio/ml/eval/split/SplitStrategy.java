package rapaio.ml.eval.split;

import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public interface SplitStrategy {

    List<Split> generateSplits(Frame df, Var weights);
}
