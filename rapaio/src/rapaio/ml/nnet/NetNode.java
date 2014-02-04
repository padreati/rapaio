package rapaio.ml.nnet;

import rapaio.core.RandomSource;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NetNode {

	double value = RandomSource.nextDouble() / 10.;
	NetNode[] inputs;
	double[] weights;
	double gamma;

	public void setInputs(NetNode[] inputs) {
		this.inputs = inputs;
		if (inputs == null) {
			this.weights = null;
			return;
		}
		this.weights = new double[inputs.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = RandomSource.nextDouble() / 10.;
		}
	}
}
