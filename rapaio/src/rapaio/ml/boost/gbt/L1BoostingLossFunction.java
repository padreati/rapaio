package rapaio.ml.boost.gbt;

import rapaio.ml.Regressor;
import rapaio.ml.simple.L1ConstantRegressor;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L1BoostingLossFunction implements BoostingLossFunction {

	@Override
	public double computeError(double y, double fx) {
		return Math.abs(y - fx);
	}

	@Override
	public double computeInvGradient(double y, double fx) {
		return Math.signum(y - fx);
	}

	@Override
	public Regressor getInitialRegressor() {
		return new L1ConstantRegressor();
	}
}
