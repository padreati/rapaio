package rapaio.experiment.math.functions;

import rapaio.math.linear.DV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/25/17.
 */
@FunctionalInterface
public interface RFunction {

    double apply(DV x);
}
