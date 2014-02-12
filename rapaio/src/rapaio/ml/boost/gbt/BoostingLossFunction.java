package rapaio.ml.boost.gbt;

import rapaio.data.Numeric;
import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public interface BoostingLossFunction {

	double findMinimum(Vector y, Vector fx);

	Numeric gradient(Vector y, Vector fx);
}
