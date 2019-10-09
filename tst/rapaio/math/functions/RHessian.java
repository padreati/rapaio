package rapaio.math.functions;

import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/25/17.
 */
@FunctionalInterface
public interface RHessian {

    RM apply(RV x);
}
