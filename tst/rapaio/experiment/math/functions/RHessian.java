package rapaio.experiment.math.functions;

import rapaio.math.linear.DM;
import rapaio.math.linear.DV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/25/17.
 */
@FunctionalInterface
public interface RHessian {

    DM apply(DV x);
}
