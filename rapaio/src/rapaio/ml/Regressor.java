package rapaio.ml;

import rapaio.data.Frame;
import rapaio.data.Vector;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface Regressor {

	void learn(Frame df, List<Double> weights, String targetColName);

	void learn(Frame df, String targetColName);

	void predict(Frame df);

	Vector getTestFittedValues();
}
