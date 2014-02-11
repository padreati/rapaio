package rapaio.ml.boost.gbt;

import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface BoostingLossFunction {

	double minimize(Vector y, Vector fx);

	double computeInvGradient(double y, double fx);
}
