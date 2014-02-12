package rapaio.ml.boost.gbt;

import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.ml.Regressor;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface BTRegressor extends Regressor {

	void boostFit(Frame x, Vector y, Vector fx, BoostingLossFunction lossFunction);

	@Override
	BTRegressor newInstance();
}
