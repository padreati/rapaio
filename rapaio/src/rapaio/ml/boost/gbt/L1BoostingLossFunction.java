package rapaio.ml.boost.gbt;

import rapaio.core.stat.Quantiles;
import rapaio.data.Numeric;
import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L1BoostingLossFunction implements BoostingLossFunction {

	@Override
	public double minimize(Vector y, Vector fx) {
		Numeric values = new Numeric();
		for (int i = 0; i < y.getRowCount(); i++) {
			values.addValue(y.getValue(i) - fx.getValue(i));
		}
		return new Quantiles(values, new double[]{0.5}).getValues()[0];
//		return new Mean(values).getValue();
	}

	@Override
	public double computeInvGradient(double y, double fx) {
		return (y - fx < 0) ? -1. : 1.;
	}
}
