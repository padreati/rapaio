/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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
 *
 */

package rapaio.experiment.math.regression;

/**
 * Taken from
 * <p>
 * http://tullo.ch/articles/speeding-up-isotonic-regression/
 * <p>
 * and translated to java
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/5/16.
 */
public class IsotonicRegression {

    public double[] isotonic_regression(double[] y, double[] weight) {
        double[] solution = new double[y.length];

        double numerator, denominator;
        int i, n, k;

        n = y.length;

        // The algorithm proceeds by iteratively updating the solution array.

        for (i = 0; i < n; i++) {
            solution[i] = y[i];
        }

        if (n <= 1)
            return solution;

        n -= 1;
        boolean pooled = true;
        while (pooled) {
            // repeat until there are no more adjacent violators.
            i = 0;
            pooled = false;
            while (i < n) {
                k = i;
                while (k < n && solution[k] >= solution[k + 1])
                    k += 1;
                if (solution[i] != solution[k]) {
                    // solution[i:k + 1] is a decreasing subsequence, so
                    // replace each point in the subsequence with the
                    // weighted average of the subsequence.
                    numerator = 0.0;
                    denominator = 0.0;
                    for (int j = i; j < k + 1; j++) {
                        numerator += solution[j] * weight[j];
                        denominator += weight[j];
                    }
                    for (int j = i; j < k + 1; j++)
                        solution[j] = numerator / denominator;
                    pooled = true;
                }
                i = k + 1;
            }
        }
        return solution;
    }
}
