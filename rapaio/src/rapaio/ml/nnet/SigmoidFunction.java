package rapaio.ml.nnet;

import rapaio.core.RandomSource;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class SigmoidFunction {

	public double compute(double input) {
		return 1. / (1. + StrictMath.exp(-input));
	}

	public double differential(double value) {
		if (value == 0) return RandomSource.nextDouble() / 100;
		return value * (1. - value);
	}
}
