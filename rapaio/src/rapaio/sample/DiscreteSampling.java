package rapaio.sample;

import rapaio.core.RandomSource;

import static rapaio.core.MathBase.log;
import static rapaio.core.MathBase.pow;
import static rapaio.core.RandomSource.nextDouble;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class DiscreteSampling {

	/**
	 * Discrete sampling with repetition.
	 * Nothing special, just using the uniform discrete sampler offered by the system.
	 */
	public int[] sampleWR(int m, final int populationSize) {
		int[] sample = new int[m];
		for (int i = 0; i < m; i++) {
			sample[i] = RandomSource.nextInt(populationSize);
		}
		return sample;
	}

	/**
	 * Draw an uniform discrete sample without replacement.
	 * <p/>
	 * Implements reservoir sampling.
	 *
	 * @param sampleSize     sample size
	 * @param populationSize population size
	 * @return
	 */
	public int[] sampleWOR(final int sampleSize, final int populationSize) {
		if (sampleSize > populationSize) {
			throw new IllegalArgumentException("Can't draw a sample without replacement bigger than population size.");
		}
		int[] sample = new int[sampleSize];
		if (sampleSize == populationSize) {
			for (int i = 0; i < sampleSize; i++) {
				sample[i] = i;
			}
			return sample;
		}
		for (int i = 0; i < sampleSize; i++) {
			sample[i] = i;
		}
		for (int i = sampleSize; i < populationSize; i++) {
			int j = RandomSource.nextInt(i + 1);
			if (j < sampleSize) {
				sample[j] = i;
			}
		}
		return sample;
	}

	/**
	 * Draw m <= n weighted random samples, weight by probabilities
	 * without replacement.
	 * <p/>
	 * Weighted random sampling without replacement.
	 * Implements Efraimidis-Spirakis method.
	 *
	 * @param sampleSize number of samples
	 * @param prob       vector of probabilities
	 * @return vector with m indices in [0,weights.length-1]
	 * @See: http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf
	 */
	public int[] sampleWeightedWOR(final int sampleSize, final double[] prob) {
		// validation
		validate(prob, sampleSize);

		int[] result = new int[sampleSize];

		if (sampleSize == prob.length) {
			for (int i = 0; i < prob.length; i++) {
				result[i] = i;
			}
			return result;
		}

		int len = 1;
		while (len <= sampleSize) {
			len *= 2;
		}
		len = len * 2;

		int[] heap = new int[len];
		double[] k = new double[sampleSize];

		// fill with invalid ids
		for (int i = 0; i < len; i++) {
			heap[i] = -1;
		}
		// fill heap base
		for (int i = 0; i < sampleSize; i++) {
			heap[i + len / 2] = i;
			k[i] = pow(nextDouble(), 1. / prob[i]);
			result[i] = i;
		}

		// learn heap
		for (int i = len / 2 - 1; i > 0; i--) {
			if (heap[i * 2] == -1) {
				heap[i] = -1;
				continue;
			}
			if (heap[i * 2 + 1] == -1) {
				heap[i] = heap[i * 2];
				continue;
			}
			if (k[heap[i * 2]] < k[heap[i * 2 + 1]]) {
				heap[i] = heap[i * 2];
			} else {
				heap[i] = heap[i * 2 + 1];
			}
		}

		// exhaust the source
		int pos = sampleSize;
		while (pos < prob.length) {
			double r = nextDouble();
			double xw = log(r) / log(k[heap[1]]);

			double acc = 0;
			while (pos < prob.length) {
				if (acc + prob[pos] < xw) {
					acc += prob[pos];
					pos++;
					continue;
				}
				break;
			}
			if (pos == prob.length) break;

			// min replaced with the new selected getValue
			double tw = pow(k[heap[1]], prob[pos]);
			double r2 = nextDouble() * (1. - tw) + tw;
			double ki = pow(r2, 1 / prob[pos]);

			k[heap[1]] = ki;
			result[heap[1]] = pos++;
			int start = heap[1] + len / 2;
			while (start > 1) {
				start /= 2;
				if (heap[start * 2 + 1] == -1) {
					heap[start] = heap[start * 2];
					continue;
				}
				if (k[heap[start * 2]] < k[heap[start * 2 + 1]]) {
					heap[start] = heap[start * 2];
				} else {
					heap[start] = heap[start * 2 + 1];
				}
			}
		}
		return result;
	}

	private void validate(double[] p, int m) {
		if (m > p.length) {
			throw new IllegalArgumentException("required sample size is bigger than population size");
		}
		double total = 0;
		for (int i = 0; i < p.length; i++) {
			if (p[i] <= 0) {
				throw new IllegalArgumentException("weights must be strict positive.");
			}
			total += p[i];
		}
		if (total != 1.) {
			for (int i = 0; i < p.length; i++) {
				p[i] /= total;
			}
		}
	}

}
