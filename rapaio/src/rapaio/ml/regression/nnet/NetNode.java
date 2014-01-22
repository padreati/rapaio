package rapaio.ml.regression.nnet;

import rapaio.core.RandomSource;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NetNode {

	double value = RandomSource.nextDouble() / 10.;
	NetNode[] inputs;
	NetNode[] outputs;
	double[] weights;


	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public NetNode[] getInputs() {
		return inputs;
	}

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

	public NetNode[] getOutputs() {
		return outputs;
	}

	public void setOutputs(NetNode[] outputs) {
		this.outputs = outputs;
	}

	public double[] getWeights() {
		return weights;
	}

	public boolean hasInputs() {
		return inputs.length > 0;
	}

	public boolean hasOutputs() {
		return outputs.length > 0;
	}
}
