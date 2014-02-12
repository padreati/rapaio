package rapaio.ml.boost.gbt;

import rapaio.core.stat.Mean;
import rapaio.data.Numeric;
import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L2BoostingLossFunction implements BoostingLossFunction {

	@Override
	public double findMinimum(Vector y, Vector fx) {
		return new Mean(gradient(y, fx)).getValue();
	}

	@Override
	public Numeric gradient(Vector y, Vector fx) {
		Numeric delta = new Numeric();
		for (int i = 0; i < y.getRowCount(); i++) {
			delta.addValue(y.getValue(i) - fx.getValue(i));
		}
		return delta;
	}
}
