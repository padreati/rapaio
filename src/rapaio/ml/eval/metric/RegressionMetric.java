package rapaio.ml.eval.metric;

import rapaio.data.Var;
import rapaio.ml.regression.RegressionResult;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/15/20.
 */
public interface RegressionMetric extends Serializable {

    String getName();

    RegressionScore compute(Var actual, RegressionResult prediction);

    RegressionScore compute(Var actual, Var prediction);
}
