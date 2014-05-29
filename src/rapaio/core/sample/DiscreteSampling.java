/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.core.sample;

import rapaio.core.RandomSource;

import java.util.Arrays;

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
     * <p>
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
     * <p>
     * Weighted random sampling without replacement.
     * Implements Efraimidis-Spirakis method.
     *
     * @param sampleSize number of samples
     * @param prob       var of probabilities
     * @return var with m indices in [0,weights.length-1]
     * @See: http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf
     */
    public int[] sampleWeightedWOR(final int sampleSize, final double[] prob) {
        // validation
        validateWeighterWOR(prob, sampleSize);

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
            k[i] = Math.pow(nextDouble(), 1. / prob[i]);
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
            double xw = Math.log(r) / Math.log(k[heap[1]]);

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

            // min replaced with the new selected value
            double tw = Math.pow(k[heap[1]], prob[pos]);
            double r2 = nextDouble() * (1. - tw) + tw;
            double ki = Math.pow(r2, 1 / prob[pos]);

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

    private void validateWeighterWOR(double[] p, int m) {
        if (m > p.length) {
            throw new IllegalArgumentException("required sample size is bigger than population size");
        }
        double total = 0;
        for (double aP : p) {
            if (aP <= 0) {
                throw new IllegalArgumentException("weights must be strict positive.");
            }
            total += aP;
        }
        if (total != 1.) {
            for (int i = 0; i < p.length; i++) {
                p[i] /= total;
            }
        }
    }


    /**
     * Generate discrete weighted random samples with replacement (same values might occur),
     * based on previous aliases computed by a previous call
     * to {@link rapaio.core.sample.DiscreteSampling#sampleWeightedWR(int, double[])}.
     * <p>
     * Implementation based on Vose alias-method algorithm.
     */
    public int[] sampleWeightedWR(int m) {
        return sampleWeightedWR(m, null);
    }

    /**
     * Generate discrete weighted random samples with replacement (same values might occur)
     * with building aliases according to the new probabilities.
     * <p>
     * Implementation based on Vose alias-method algorithm
     */
    public int[] sampleWeightedWR(int m, double[] p) {
        double[] prob = Arrays.copyOf(p, p.length);
        for (int i = 0; i < prob.length; i++) {
            prob[i] *= prob.length;
        }
        int[] alias = new int[p.length];

        if (p != null) {
            makeAliasWR(p, prob, alias);
        }
        int[] sample = new int[m];
        for (int i = 0; i < m; i++) {
            int column = RandomSource.nextInt(prob.length);
            sample[i] = RandomSource.nextDouble() < prob[column] ? column : alias[column];
        }
        return sample;
    }

    /**
     * Builds discrete random sampler without replacement
     *
     * @param p     The list of probabilities.
     * @param prob
     * @param alias
     */
    private void makeAliasWR(double[] p, double[] prob, int[] alias) {
        if (p.length == 0)
            throw new IllegalArgumentException("Probability var must be nonempty.");


        int[] dq = new int[p.length];
        int smallPos = -1;
        int largePos = prob.length;

        for (int i = 0; i < prob.length; ++i) {
            if (prob[i] >= 1.) {
                dq[largePos - 1] = i;
                largePos--;
            } else {
                dq[smallPos + 1] = i;
                smallPos++;
            }
        }

        while (smallPos >= 0 && largePos <= p.length - 1) {
            int small = dq[smallPos--];
            int large = dq[largePos++];

            alias[small] = large;
            prob[large] = prob[large] + prob[small] - 1.;

            if (prob[large] >= 1.0) {
                dq[largePos - 1] = large;
                largePos--;
            } else {
                dq[smallPos + 1] = large;
                smallPos++;
            }
        }

        while (smallPos > 0) {
            prob[dq[smallPos - 1]] = 1.0;
            smallPos--;
        }
        while (largePos < dq.length) {
            prob[dq[largePos]] = 1.0;
            largePos++;
        }
    }


}
