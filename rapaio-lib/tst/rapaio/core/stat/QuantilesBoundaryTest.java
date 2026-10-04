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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Regression tests for the bracket of the interpolated quantile estimators.
 */
public class QuantilesBoundaryTest {

    private static VarDouble zeroToNMinusOne(int n) {
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = i;
        }
        return VarDouble.wrap(values);
    }

    /**
     * For the data 0, 1, ..., N-1 the sample quantile of type 8 is exactly {@code h - 1}, with
     * {@code h = (N + 1/3) p + 1/3}, clamped to the range of the data.
     */
    private static double expectedR8(int n, double p) {
        double h = (n + 1. / 3.) * p + 1. / 3.;
        return Math.max(0, Math.min(h - 1, n - 1));
    }

    @Test
    void r8QuantileDoesNotLeaveTheArrayAtTheUpperThreshold() {
        // (N - 1/3) / (N + 1/3) is the probability from which the maximum is answered; the probabilities just
        // below it recompute h with different rounding, which used to reach one position past the array
        int[][] cases = {{9, 0}, {1333, 1}};
        double[] probabilities = {0.9285714285714284, 0.9995};
        for (int i = 0; i < cases.length; i++) {
            int n = cases[i][0];
            double p = probabilities[i];
            double q = Quantiles.of(zeroToNMinusOne(n), Quantiles.Type.R8, p).values()[0];
            assertEquals(expectedR8(n, p), q, 1e-9, "n=" + n + " p=" + p);
            assertTrue(q <= n - 1);
        }
    }

    @Test
    void r8QuantileDoesNotLeaveTheArrayAtTheLowerThreshold() {
        // the same at (2/3) / (N + 1/3), where h used to round below 1 and reach position -1
        int n = 39;
        double p = 0.016949152542372878;
        double q = Quantiles.of(zeroToNMinusOne(n), Quantiles.Type.R8, p).values()[0];
        assertEquals(expectedR8(n, p), q, 1e-9);
        assertTrue(q >= 0, "the quantile must not fall below the smallest observation, but was " + q);
    }

    @Test
    void r8QuantileStaysInsideTheDataAroundBothThresholds() {
        for (int n = 2; n <= 400; n++) {
            var x = zeroToNMinusOne(n);
            double upper = (n - 1.0 / 3) / (n + 1.0 / 3);
            double lower = (2.0 / 3) / (n + 1.0 / 3);
            double pu = upper;
            double pl = lower;
            for (int k = 0; k < 8; k++) {
                pu = Math.nextDown(pu);
                for (double p : new double[] {pu, pl}) {
                    double q = Quantiles.of(x, Quantiles.Type.R8, p).values()[0];
                    assertEquals(expectedR8(n, p), q, 1e-9, "n=" + n + " p=" + p);
                    assertTrue(q >= 0 && q <= n - 1, "n=" + n + " p=" + p + " q=" + q);
                }
                pl = Math.nextUp(pl);
            }
        }
    }

    @Test
    void r7QuantileStaysInsideTheDataAtTheBoundaries() {
        for (int n = 2; n <= 400; n++) {
            var x = zeroToNMinusOne(n);
            for (double p : new double[] {0, Math.nextUp(0.0), Math.nextDown(1.0), 1}) {
                double q = Quantiles.of(x, Quantiles.Type.R7, p).values()[0];
                assertTrue(q >= 0 && q <= n - 1, "n=" + n + " p=" + p + " q=" + q);
            }
        }
    }
}
