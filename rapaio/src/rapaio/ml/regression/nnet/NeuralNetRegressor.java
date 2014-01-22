package rapaio.ml.regression.nnet;

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Frames;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NeuralNetRegressor {

	private final double learningRate;
	private final double momentum;
	private final NetNode[][] net;
	private final SigmoidFunction sigmoid = new SigmoidFunction();

	private List<String> inputCols;
	private List<String> targetCols;
	private Frame prediction;

	public NeuralNetRegressor(int[] layerSizes, double learningRate, double momentum) {
		this.learningRate = learningRate;
		this.momentum = momentum;

		if (layerSizes.length < 2) {
			throw new IllegalArgumentException("neural net must hav at least 2 layers (including input layer)");
		}

		// build design

		net = new NetNode[layerSizes.length][];
		for (int i = 0; i < layerSizes.length; i++) {
			int add = i != layerSizes.length - 1 ? 1 : 0;
			net[i] = new NetNode[layerSizes[i] + add];
			for (int j = 0; j < net[i].length; j++) {
				net[i][j] = new NetNode();
			}
		}

		// wire-up nodes
		for (int i = 0; i < net.length; i++) {
			if (i == 0) {
				for (int j = 0; j < net[i].length; j++) {
					net[i][j].setInputs(null);
					net[i][j].setOutputs(net[i + 1]);
					if (j == 0) {
						net[i][j].setValue(1.);
					}
				}
				continue;
			}
			if (i == layerSizes.length - 1) {
				for (int j = 0; j < net[i].length; j++) {
					net[i][j].setInputs(net[i - 1]);
					net[i][j].setOutputs(null);
				}
				continue;
			}

			for (int j = 0; j < net[i].length; j++) {
				if (j == 0) {
					net[i][j].setInputs(null);
					net[i][j].setOutputs(net[i + 1]);
					net[i][j].setValue(1.);
					continue;
				}
				net[i][j].setInputs(net[i - 1]);
				net[i][j].setOutputs(net[i + 1]);
			}
		}
	}

	public void learn(Frame df, String targetColNames, boolean skipSetup, int rounds) {
		if (!skipSetup) {
			ColRange targetColRange = new ColRange(targetColNames);
			List<Integer> targets = targetColRange.parseColumnIndexes(df);
			targetCols = new ArrayList<>();
			for (int i = 0; i < targets.size(); i++) {
				targetCols.add(df.colNames()[targets.get(i)]);
			}
			inputCols = new ArrayList<>();
			for (int i = 0; i < df.colNames().length; i++) {
				if (targetCols.contains(df.colNames()[i])) continue;
				if (df.col(df.colNames()[i]).type().isNominal()) continue;
				inputCols.add(df.colNames()[i]);
			}

			// validate
			if (targetCols.size() != net[net.length - 1].length) {
				throw new IllegalArgumentException("target columns does not fit output nodes");
			}
			if (inputCols.size() != net[0].length) {
				throw new IllegalArgumentException("input columns does not fit input nodes");
			}
		}

		// learn network

		for (int kk = 0; kk < rounds; kk++) {
			int pos = RandomSource.nextInt(df.rowCount());

			// set inputs
			for (int i = 1; i < inputCols.size(); i++) {
				net[0][i].setValue(df.value(pos, inputCols.get(i - 1)));
			}

			// feed forward
			for (int i = 1; i < net.length; i++) {
				for (int j = 0; j < net[i].length; j++) {
					if (net[i][j].getInputs() != null) {
						double t = 0;
						for (int k = 0; k < net[i][j].getInputs().length; k++) {
							t += net[i][j].getInputs()[k].value * net[i][j].getWeights()[k];
						}
						net[i][j].setValue(sigmoid.compute(t));
					}
				}
			}

			// back propagate

			double[] error = new double[targetCols.size()];
			for (int i = 0; i < error.length; i++) {
				double expected = df.value(pos, targetCols.get(i));
				double actual = net[net.length - 1][i].getValue();
				error[i] = learningRate * sigmoid.differential(actual) * (expected - actual);
			}

			for (int i = net.length - 1; i > 0; i--) {
				double[] newerror = new double[net[i - 1].length];
				for (int j = 0; j < net[i].length; j++) {
					if (net[i][j].getInputs() != null) {
						for (int k = 0; k < net[i][j].getInputs().length; k++) {
							net[i][j].getWeights()[k] += error[j] * net[i - 1][k].getValue();
							newerror[k] += learningRate * net[i - 1][k].getValue() * error[j] * sigmoid.differential(net[i - 1][k].getValue());
						}
					}
				}
				error = newerror;
			}
		}
	}

	public void predict(Frame df) {
		prediction = Frames.newMatrixFrame(df.rowCount(), targetCols);

		for (int pos = 0; pos < df.rowCount(); pos++) {
			// set inputs
			for (int i = 1; i < inputCols.size(); i++) {
				net[0][i].setValue(df.value(pos, inputCols.get(i - 1)));
			}

			// feed forward
			for (int i = 1; i < net.length; i++) {
				for (int j = 0; j < net[i].length; j++) {
					if (net[i][j].getInputs() != null) {
						double t = 0;
						for (int k = 0; k < net[i][j].getInputs().length; k++) {
							t += net[i][j].getInputs()[k].value * net[i][j].getWeights()[k];
						}
						net[i][j].setValue(sigmoid.compute(t));
					}
				}
			}
			for (int i = 0; i < targetCols.size(); i++) {
				prediction.setValue(pos, i, net[net.length - 1][i].getValue());
			}
		}
	}

	public Frame getFittedValues() {
		return prediction;
	}
}
