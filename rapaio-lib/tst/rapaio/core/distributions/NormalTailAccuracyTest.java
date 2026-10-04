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

package rapaio.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.math.MathTools;

/**
 * The normal distribution in the tails and at infinity. The reference values were computed with 60 digit
 * decimal arithmetic from the continued fraction of the normal tail, independently of the code under test.
 */
public class NormalTailAccuracyTest {

    private static final double INF = Double.POSITIVE_INFINITY;

    private static void assertRelative(double expected, double actual, double tol, String message) {
        assertEquals(expected, actual, Math.abs(expected) * tol, message + ": expected " + expected + " but was " + actual);
    }

    /** z, Phi(z) */
    private static final double[][] CDF = {
            {-37, 5.72557122252457710e-300}, {-30, 4.90671392714818718e-198}, {-20, 2.75362411860623374e-89},
            {-15, 3.67096619931275083e-51}, {-10, 7.61985302416052545e-24}, {-8, 6.22096057427178387e-16},
            {-6, 9.86587645037698091e-10}, {-5, 2.86651571879193912e-07}, {-4, 3.16712418331199243e-05},
            {-3, 1.34989803163009458e-03}, {-2.5, 6.20966532577613486e-03}, {-2, 2.27501319481792086e-02},
            {-1, 1.58655253931457046e-01}, {-0.5, 3.08537538725986882e-01}, {0.5, 6.91462461274013118e-01},
            {1, 8.41344746068542926e-01}, {2, 9.77249868051820791e-01}, {3, 9.98650101968369897e-01},
            {5, 9.99999713348428076e-01}, {8, 9.99999999999999334e-01}};

    /** p, the lower tail quantile of the standard normal */
    private static final double[][] QUANTILE = {
            {1e-300, -3.70470962993612005e+01}, {1e-200, -3.02055941795796414e+01}, {1e-100, -2.12734535609653257e+01},
            {1e-50, -1.49333375347884889e+01}, {1e-30, -1.14640246884436152e+01}, {1e-20, -9.26234008979840695e+00},
            {1e-17, -8.49379322410959858e+00}, {1e-15, -7.94134532617099698e+00}, {1e-12, -7.03448382530113214e+00},
            {1e-10, -6.36134090240405659e+00}, {1e-8, -5.61200124417478907e+00}, {1e-6, -4.75342430882289868e+00},
            {1e-4, -3.71901648545568042e+00}, {0.001, -3.09023230616781364e+00}, {0.01, -2.32634787404084120e+00},
            {0.025, -1.95996398454005427e+00}, {0.1, -1.28155156554460037e+00}, {0.3, -5.24400512708040778e-01}};

    @Test
    void cdfAtInfinity() {
        // the cdf at +Infinity used to be 0, which made every one sided p value at an infinite z score wrong
        assertEquals(1.0, Normal.std().cdf(INF), 0.0);
        assertEquals(0.0, Normal.std().cdf(-INF), 0.0);
        assertEquals(1.0, Normal.of(3, 2).cdf(INF), 0.0);
        assertEquals(0.0, Normal.of(3, 2).cdf(-INF), 0.0);
        assertTrue(Double.isNaN(Normal.std().cdf(Double.NaN)));
    }

    @Test
    void cdfIsAccurateInRelativeTermsInTheTails() {
        // the series lost the tail: cdf(-10) was 5e-16 instead of 7.6e-24
        for (double[] r : CDF) {
            assertRelative(r[1], Normal.std().cdf(r[0]), 1e-12, "cdf(" + r[0] + ")");
        }
    }

    @Test
    void cdfOfAShiftedScaledNormalUsesTheSameAccuracy() {
        assertRelative(7.61985302416052545e-24, Normal.of(100, 5).cdf(50), 1e-12, "cdf of N(100, 5) at 50");
        assertRelative(2.86651571879193912e-07, Normal.of(-4, 0.5).cdf(-6.5), 1e-12, "cdf of N(-4, 0.5) at -6.5");
    }

    @Test
    void cdfIsMonotoneAcrossTheSwitchBetweenTheTwoAlgorithms() {
        double previous = 0;
        for (double z = -8; z <= 8; z += 1.0 / 1024) {
            double c = Normal.std().cdf(z);
            assertTrue(c >= previous, "cdf is not monotone at z=" + z);
            previous = c;
        }
        // and continuous at the switch, where a mismatch between the algorithms would show as a jump
        double below = Normal.std().cdf(-Math.nextUp(2.0));
        double at = Normal.std().cdf(-2.0);
        assertEquals(1.0, below / at, 1e-12);
    }

    @Test
    void cdfIsSymmetric() {
        for (double z = 0.1; z < 9; z += 0.37) {
            assertEquals(1.0, Normal.std().cdf(-z) + Normal.std().cdf(z), 1e-15, "z=" + z);
        }
    }

    @Test
    void quantileIsAccurateInRelativeTermsInTheTails() {
        // quantile(1e-17) was -8.25 instead of -8.49, because the refinement step used the inaccurate tail cdf
        for (double[] r : QUANTILE) {
            assertRelative(r[1], Normal.std().quantile(r[0]), 1e-12, "quantile(" + r[0] + ")");
        }
    }

    @Test
    void quantileAndCdfAreInverseFunctionsInTheLowerTail() {
        for (double e = 1; e <= 300; e += 3) {
            double p = Math.pow(10, -e);
            assertRelative(p, Normal.std().cdf(Normal.std().quantile(p)), 1e-11, "p=1e-" + e);
        }
    }

    @Test
    void quantileAtTheBoundariesIsInfinite() {
        assertEquals(-INF, Normal.std().quantile(0), 0.0);
        assertEquals(INF, Normal.std().quantile(1), 0.0);
    }

    @Test
    void logNormalCdfAtInfinity() {
        assertEquals(1.0, LogNormal.of(0, 1).cdf(INF), 0.0);
        assertEquals(0.0, LogNormal.of(0, 1).cdf(0), 0.0);
        assertEquals(0.5, LogNormal.of(2, 0.7).cdf(Math.exp(2)), 1e-15);
    }

    @Test
    void logNormalQuantileOfTinyProbabilities() {
        // 2p - 1 rounds to -1 for p below 1e-16, which made the quantile 0 for every such probability
        for (double[] r : QUANTILE) {
            assertRelative(Math.exp(r[1]), LogNormal.of(0, 1).quantile(r[0]), 1e-11, "p=" + r[0]);
            assertRelative(Math.exp(2 + 0.5 * r[1]), LogNormal.of(2, 0.5).quantile(r[0]), 1e-11, "p=" + r[0]);
        }
        assertEquals(0.0, LogNormal.of(0, 1).quantile(0), 0.0);
        assertEquals(INF, LogNormal.of(0, 1).quantile(1), 0.0);
    }

    @Test
    void errorFunctionsAtInfinity() {
        // erf(+Infinity) was -1 and erfc(-Infinity) was 0
        assertEquals(1.0, MathTools.erf(INF), 0.0);
        assertEquals(-1.0, MathTools.erf(-INF), 0.0);
        assertEquals(0.0, MathTools.erfc(INF), 0.0);
        assertEquals(2.0, MathTools.erfc(-INF), 0.0);
    }

    @Test
    void complementaryErrorFunctionKeepsItsTail() {
        // erfc(5) = 1.5374597944280349e-12, from the C library reference
        assertRelative(1.5374597944280349e-12, MathTools.erfc(5), 1e-12, "erfc(5)");
    }

    @Test
    void oneSidedZTestWithAnInfiniteZScore() {
        // a zero known standard deviation makes the z score infinite; the one sided p values used to be the
        // opposite of the right ones, since the cdf at +Infinity was 0
        var greater = rapaio.core.tests.ZTestOneSample.test(5.0, 10, 4.0, 0.0, 0.05, rapaio.core.tests.HTest.Alternative.GREATER_THAN);
        var less = rapaio.core.tests.ZTestOneSample.test(5.0, 10, 4.0, 0.0, 0.05, rapaio.core.tests.HTest.Alternative.LESS_THAN);
        var two = rapaio.core.tests.ZTestOneSample.test(5.0, 10, 4.0, 0.0, 0.05, rapaio.core.tests.HTest.Alternative.TWO_TAILS);
        assertEquals(0.0, greater.pValue(), 0.0);
        assertEquals(1.0, less.pValue(), 0.0);
        assertEquals(0.0, two.pValue(), 0.0);

        var greaterBelow = rapaio.core.tests.ZTestOneSample.test(3.0, 10, 4.0, 0.0, 0.05, rapaio.core.tests.HTest.Alternative.GREATER_THAN);
        var lessBelow = rapaio.core.tests.ZTestOneSample.test(3.0, 10, 4.0, 0.0, 0.05, rapaio.core.tests.HTest.Alternative.LESS_THAN);
        assertEquals(1.0, greaterBelow.pValue(), 0.0);
        assertEquals(0.0, lessBelow.pValue(), 0.0);
    }
}
