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

package rapaio.core.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Regression tests for the two sample Kolmogorov-Smirnov statistic when the same value appears in both samples.
 */
public class KSTestTiesTest {

    /**
     * The statistic by its definition: the largest absolute difference of the two empirical distribution
     * functions, evaluated at every observed value.
     */
    private static double bruteForceD(double[] a, double[] b) {
        double d = 0;
        for (double[] sample : new double[][] {a, b}) {
            for (double point : sample) {
                double fa = 0;
                for (double v : a) {
                    if (v <= point) fa++;
                }
                double fb = 0;
                for (double v : b) {
                    if (v <= point) fb++;
                }
                d = Math.max(d, Math.abs(fa / a.length - fb / b.length));
            }
        }
        return d;
    }

    @Test
    void identicalConstantSamplesAreIndistinguishable() {
        // both empirical functions jump from 0 to 1 at the same point, R: ks.test(c(1,1,1,1), c(1,1))$statistic == 0
        var test = KSTestTwoSamples.from(VarDouble.copy(1, 1, 1, 1), VarDouble.copy(1, 1));
        assertEquals(0.0, test.d(), 0.0);
        assertEquals(1.0, test.pValue(), 1e-12);
    }

    @Test
    void tiesAcrossSamplesAreComparedAfterTheWholeRun() {
        // by hand: at 1 the functions are 4/5 and 2/3, at 5 both are 1, thus D = 4/5 - 2/3 = 2/15
        var test = KSTestTwoSamples.from(VarDouble.copy(1, 1, 1, 1, 5), VarDouble.copy(1, 1, 5));
        assertEquals(2.0 / 15.0, test.d(), 1e-12);
    }

    @Test
    void identicalSamplesWithRepeatedValuesHaveZeroDistance() {
        var test = KSTestTwoSamples.from(VarDouble.copy(1, 1, 2, 2, 2, 3), VarDouble.copy(1, 1, 2, 2, 2, 3));
        assertEquals(0.0, test.d(), 0.0);
    }

    @Test
    void disjointSamplesAreAtMaximumDistance() {
        var test = KSTestTwoSamples.from(VarDouble.copy(1, 2, 3), VarDouble.copy(4, 5, 6));
        assertEquals(1.0, test.d(), 0.0);
    }

    @Test
    void statisticMatchesTheDefinitionOnHeavilyTiedRandomSamples() {
        Random random = new Random(42);
        for (int trial = 0; trial < 500; trial++) {
            int n1 = 1 + random.nextInt(12);
            int n2 = 1 + random.nextInt(12);
            int levels = 1 + random.nextInt(4);
            double[] a = new double[n1];
            double[] b = new double[n2];
            for (int i = 0; i < n1; i++) a[i] = random.nextInt(levels);
            for (int i = 0; i < n2; i++) b[i] = random.nextInt(levels);
            var test = KSTestTwoSamples.from(VarDouble.wrap(a.clone()), VarDouble.wrap(b.clone()));
            assertEquals(bruteForceD(a, b), test.d(), 1e-12, "trial " + trial);
        }
    }
}
