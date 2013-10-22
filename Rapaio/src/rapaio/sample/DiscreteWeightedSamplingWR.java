/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.core.RandomSource;

import java.util.Arrays;

/**
 * Generate discrete weighted random samples with replacement (same values might occur).
 * <p/>
 * Implementation based on Vose alias-method algorithm
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DiscreteWeightedSamplingWR {

    /* The probability and alias tables. */
    private final double[] prob;
    private final int[] alias;

    /**
     * Builds discrete random sampler without replacement
     *
     * @param p The list of probabilities.
     */
    public DiscreteWeightedSamplingWR(double[] p) {
        if (p.length == 0)
            throw new IllegalArgumentException("Probability vector must be nonempty.");

        prob = Arrays.copyOf(p, p.length);
        for (int i = 0; i < prob.length; i++) {
            prob[i] *= prob.length;
        }
        alias = new int[p.length];

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

    /**
     * Draw a sample of length m
     */
    public int[] sample(int m) {
        int[] sample = new int[m];
        for (int i = 0; i < m; i++) {
            int column = RandomSource.nextInt(prob.length);
            sample[i] = RandomSource.nextDouble() < prob[column] ? column : alias[column];
        }
        return sample;
    }
}
