package rapaio.ml.boost.gbt;

import rapaio.ml.Regressor;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface BoostingLossFunction {

	double computeError(double y, double fx);

	double computeInvGradient(double y, double fx);

	Regressor getInitialRegressor();
}
