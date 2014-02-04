package rapaio.ml.tree;

import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.ml.AbstractRegressor;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class RandomForestRegressor extends AbstractRegressor {

	int mtrees = 0;

	@Override
	public void learn(Frame df, List<Double> weights, String targetColName) {

	}

	@Override
	public void predict(Frame df) {

	}

	@Override
	public Vector getTestFittedValues() {
		return null;
	}
}
