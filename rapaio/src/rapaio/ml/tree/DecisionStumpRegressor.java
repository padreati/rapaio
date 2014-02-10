package rapaio.ml.tree;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.boost.gbt.BTRegressor;

import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class DecisionStumpRegressor extends AbstractRegressor implements BTRegressor {

	@Override
	public BTRegressor newInstance() {
		return null;
	}

	@Override
	public void learn(Frame df, List<Double> weights, String targetColName) {

	}

	@Override
	public void boostFit(Frame x, Vector y, Numeric fx) {

	}

	@Override
	public void predict(Frame df) {

	}

	@Override
	public Vector getFitValues() {
		return null;
	}

	@Override
	public Vector getResidualValues() {
		return null;
	}

	@Override
	public Frame getAllFitValues() {
		return null;
	}

	@Override
	public Frame getAllResidualValues() {
		return null;
	}
}
