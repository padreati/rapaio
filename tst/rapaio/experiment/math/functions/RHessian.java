package rapaio.experiment.math.functions;

import rapaio.experiment.math.linear.RM;
import rapaio.experiment.math.linear.RV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/25/17.
 */
@FunctionalInterface
public interface RHessian {

    RM apply(RV x);
}
