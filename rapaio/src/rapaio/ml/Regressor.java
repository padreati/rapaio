package rapaio.ml;

import rapaio.data.Frame;
import rapaio.data.Vector;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public interface Regressor {

	Regressor newInstance();

	void learn(Frame df, List<Double> weights, String targetColName);

	void learn(Frame df, String targetColName);

	void predict(Frame df);

	Vector getFitValues();

	Vector getResidualValues();

	Frame getAllFitValues();

	Frame getAllResidualValues();
}
