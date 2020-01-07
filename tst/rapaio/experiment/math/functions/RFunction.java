package rapaio.experiment.math.functions;

import rapaio.math.linear.DVector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/25/17.
 */
@FunctionalInterface
public interface RFunction {

    double apply(DVector x);
}
