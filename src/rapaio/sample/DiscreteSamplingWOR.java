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

/**
 * Discrete uniform sampling without replacement.
 * <p/>
 * Implements reservoir sampling.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DiscreteSamplingWOR {

    private final int populationSize;

    public DiscreteSamplingWOR(int populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * Draw an uniform discrete sample without replacement
     *
     * @param m
     * @return
     */
    public int[] sample(int m) {
        if (m > populationSize) {
            throw new IllegalArgumentException("Can't draw a sample without replacement bigger than population size.");
        }
        int[] sample = new int[m];
        if (m == populationSize) {
            for (int i = 0; i < m; i++) {
                sample[i] = i;
            }
            return sample;
        }
        for (int i = 0; i < m; i++) {
            sample[i] = i;
        }
        for (int i = m; i < populationSize; i++) {
            int j = RandomSource.nextInt(i + 1);
            if (j < m) {
                sample[j] = i;
            }
        }
        return sample;
    }
}
