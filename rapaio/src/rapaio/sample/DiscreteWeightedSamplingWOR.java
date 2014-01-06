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
package rapaio.sample;

import static rapaio.core.BaseMath.log;
import static rapaio.core.BaseMath.pow;
import static rapaio.core.RandomSource.nextDouble;

/**
 * Weighted random sampling without replacement.
 * Implements Efraimidis-Spirakis method.
 *
 * @See: http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DiscreteWeightedSamplingWOR {

    private final double[] p;

    /**
     * Builds a samples with p as vector of probabilities
     *
     * @param p vector of probabilities
     */
    public DiscreteWeightedSamplingWOR(final double[] p) {
        this.p = p;
    }

    /**
     * Draw m <= n weighted random samples.
     *
     * @param m number of samples
     * @return vector with m indices in [0,weights.length-1]
     */
    public int[] sample(int m) {
        // validation
        validate(p, m);

        int[] result = new int[m];

        if (m == p.length) {
            for (int i = 0; i < p.length; i++) {
                result[i] = i;
            }
            return result;
        }

        int len = 1;
        while (len <= m) {
            len *= 2;
        }
        len = len * 2;

        int[] heap = new int[len];
        double[] k = new double[m];

        // fill with invalid ids
        for (int i = 0; i < len; i++) {
            heap[i] = -1;
        }
        // fill heap base
        for (int i = 0; i < m; i++) {
            heap[i + len / 2] = i;
            k[i] = pow(nextDouble(), 1. / p[i]);
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
        int pos = m;
        while (pos < p.length) {
            double r = nextDouble();
            double xw = log(r) / log(k[heap[1]]);

            double cumulate = 0;
            while (pos < p.length) {
                if (cumulate + p[pos] < xw) {
                    cumulate += p[pos];
                    pos++;
                    continue;
                }
                break;
            }
            if (pos == p.length) break;

            // min replaced with the new selected value
            double tw = pow(k[heap[1]], p[pos]);
            double r2 = nextDouble() * (1. - tw) + tw;
            double ki = pow(r2, 1 / p[pos]);

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
