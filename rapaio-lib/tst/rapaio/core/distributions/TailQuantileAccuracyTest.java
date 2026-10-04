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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.math.MathTools;

/**
 * Quantiles of the Student and Fisher distributions in extreme tails. They are computed from the inverse of
 * the regularized incomplete beta function, which used to stop at an absolute resolution of 1e-15 in the beta
 * variable, far coarser than the tail quantiles themselves.
 * <p>
 * All the reference values are closed forms, available for small degrees of freedom:
 * <ul>
 *     <li>Student with 1 degree of freedom (Cauchy): {@code q = -1 / tan(pi p)}</li>
 *     <li>Student with 2 degrees of freedom: {@code q = (2p - 1) / sqrt(2 p (1 - p))}</li>
 *     <li>Fisher(1,1): {@code q = tan^2(pi p / 2)}</li>
 *     <li>Fisher(2,2): {@code q = p / (1 - p)}</li>
 *     <li>Fisher(2,d): {@code q = d/2 ((1 - p)^(-2/d) - 1)}</li>
 * </ul>
 */
public class TailQuantileAccuracyTest {

    private static final double[] LOWER = {1e-300, 1e-200, 1e-100, 1e-30, 1e-15, 1e-9, 1e-6, 1e-3, 0.1, 0.3};

    private static void assertRelative(double expected, double actual, double tol, String message) {
        assertEquals(expected, actual, Math.abs(expected) * tol, message + ": expected " + expected + " but was " + actual);
    }

    @Test
    void studentQuantileWithOneDegreeOfFreedom() {
        for (double p : LOWER) {
            assertRelative(-1 / Math.tan(Math.PI * p), StudentT.of(1).quantile(p), 1e-11, "p=" + p);
        }
    }

    @Test
    void studentQuantileWithTwoDegreesOfFreedom() {
        for (double p : LOWER) {
            assertRelative((2 * p - 1) / Math.sqrt(2 * p * (1 - p)), StudentT.of(2).quantile(p), 1e-11, "p=" + p);
        }
    }

    @Test
    void studentQuantileOfTheUpperTailMirrorsTheLowerOne() {
        // the probabilities are chosen as 1 - 2^-k, which are exact doubles, so the upper tail is exactly the mirror
        for (int k = 10; k <= 50; k += 8) {
            double q = Math.pow(2, -k);
            double p = 1 - q;
            assertRelative(1 / Math.tan(Math.PI * q), StudentT.of(1).quantile(p), 1e-9, "k=" + k);
            assertRelative(-(2 * q - 1) / Math.sqrt(2 * q * (1 - q)), StudentT.of(2).quantile(p), 1e-9, "k=" + k);
        }
    }

    @Test
    void studentQuantileHonoursLocationAndScale() {
        StudentT t = StudentT.of(3, 10, 4);
        // the probabilities are such that 1 - p is distinguishable from 1
        for (double p : new double[] {1e-9, 1e-4, 0.01, 0.4}) {
            assertEquals(10 + 4 * StudentT.of(3).quantile(p), t.quantile(p), 1e-12 * Math.abs(t.quantile(p)), "p=" + p);
            assertEquals(10 - 4 * StudentT.of(3).quantile(p), t.quantile(1 - p), 1e-6 * Math.abs(t.quantile(1 - p)), "upper p=" + p);
        }
        // the lower tail alone can go as far as the probability can be represented
        assertEquals(10 + 4 * StudentT.of(3).quantile(1e-300), t.quantile(1e-300), 1e-12 * Math.abs(t.quantile(1e-300)));
    }

    @Test
    void studentQuantileIsTheInverseOfTheCdfInTheTail() {
        // over the whole range of probabilities: the cdf no longer overflows for a huge quantile, so the only
        // quantiles left out are those which are themselves beyond the largest double
        for (double df : new double[] {0.5, 1, 1.5, 3, 7, 30, 200, 5000}) {
            StudentT t = StudentT.of(df);
            for (double e = 1; e <= 300; e += 7) {
                double p = Math.pow(10, -e);
                double q = t.quantile(p);
                if (Double.isInfinite(q)) {
                    continue;
                }
                assertRelative(p, t.cdf(q), 1e-8, "df=" + df + " p=1e-" + e);
            }
        }
    }

    @Test
    void studentQuantileIsMonotone() {
        StudentT t = StudentT.of(3);
        double previous = Double.NEGATIVE_INFINITY;
        for (double p = 1e-300; p < 1; p = p < 0.4 ? p * 1.7 : p + 0.01) {
            double q = t.quantile(p);
            assertTrue(q >= previous, "not monotone at p=" + p);
            previous = q;
        }
    }

    @Test
    void studentCdfOfTheLowerTailFollowsTheCauchyClosedFormUpToTheLargestDouble() {
        // df = 1 is the Cauchy distribution, P(T < -t) = atan(1/t) / pi. The cdf used to square t, which
        // overflows past 1e154 and turned every such tail probability into 0
        for (double t : new double[] {1e3, 1e10, 1e50, 1e100, 1e150, 1e153, 1e154, 1e155, 1e200, 1e300, 1.7e308}) {
            assertRelative(Math.atan(1 / t) / Math.PI, StudentT.of(1).cdf(-t), 1e-12, "t=-" + t);
        }
    }

    @Test
    void studentCdfOfTheLowerTailFollowsTheClosedFormForTwoDegreesOfFreedom() {
        // df = 2: P(T < -t) = 1/2 (1 - t / sqrt(2 + t^2)) = 1 / (s (s + t)) with s = sqrt(2 + t^2)
        for (double t : new double[] {1e3, 1e10, 1e50, 1e100, 1e150}) {
            double s = Math.sqrt(2 + t * t);
            double expected = t > 1e100 ? 1 / (2 * t * t) : 1 / (s * (s + t));
            assertRelative(expected, StudentT.of(2).cdf(-t), 1e-12, "t=-" + t);
        }
        // past 1e154 the probability itself is subnormal, and below 1e-324 it is 0
        assertRelative(5e-311, StudentT.of(2).cdf(-1e155), 1e-12, "t=-1e155");
        assertEquals(0.0, StudentT.of(2).cdf(-1e200), 0.0);
    }

    @Test
    void studentCdfHonoursLocationAndScaleForHugeDeviations() {
        assertRelative(Math.atan(1 / 1e200) / Math.PI, StudentT.of(1, 5, 2).cdf(5 - 2e200), 1e-12, "lower");
        assertEquals(1.0, StudentT.of(1, 5, 2).cdf(5 + 2e200), 0.0);
    }

    @Test
    void studentCdfAtTheExtremes() {
        StudentT t = StudentT.of(3);
        assertEquals(1.0, t.cdf(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, t.cdf(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(0.5, t.cdf(0), 0.0);
        assertTrue(Double.isNaN(t.cdf(Double.NaN)));
    }

    @Test
    void studentCdfIsContinuousWhereTheAlgorithmChanges() {
        // the beta variable x = df / (df + z^2) is computed directly down to 1e-250 and from its log odds below
        for (double df : new double[] {0.5, 1, 3, 30}) {
            double z = Math.sqrt(df * 1e250);
            double above = StudentT.of(df).cdf(-Math.nextDown(z));
            double below = StudentT.of(df).cdf(-Math.nextUp(z));
            if (above > 0) {
                assertEquals(1.0, above / below, 1e-11, "df=" + df);
            }
        }
    }

    @Test
    void studentCdfIsMonotoneAcrossTheWholeRange() {
        StudentT t = StudentT.of(1);
        double previous = 0;
        for (double e = 300; e >= 0; e -= 0.5) {
            double c = t.cdf(-Math.pow(10, e));
            assertTrue(c >= previous, "cdf is not monotone at t=-1e" + e);
            previous = c;
        }
    }

    @Test
    void studentQuantileOverflowsToInfinityOnlyWhereTheQuantileIsBeyondTheDoubles() {
        // df = 0.5: the tail is C |t|^-0.5, the quantile of 1e-100 is finite and that of 1e-160 is not
        StudentT t = StudentT.of(0.5);
        assertTrue(Double.isFinite(t.quantile(1e-100)));
        assertRelative(1e-100, t.cdf(t.quantile(1e-100)), 1e-8, "1e-100");
        assertRelative(1e-150, t.cdf(t.quantile(1e-150)), 1e-8, "1e-150");
        assertEquals(Double.NEGATIVE_INFINITY, t.quantile(1e-160), 0.0);
    }

    @Test
    void studentPdfForHugeDeviationsDoesNotOverflow() {
        assertRelative(1 / (Math.PI * 1e200), StudentT.of(1).pdf(1e100), 1e-12, "t=1e100");
        assertEquals(0.0, StudentT.of(1).pdf(1e200), 0.0);
        assertRelative(0.3675525969478613, StudentT.of(3).pdf(0), 1e-13, "t=0");
    }

    @Test
    void fisherQuantileWithOneAndOneDegreesOfFreedom() {
        for (double p : LOWER) {
            assertRelative(Math.pow(Math.tan(Math.PI * p / 2), 2), Fisher.of(1, 1).quantile(p), 1e-11, "p=" + p);
        }
    }

    @Test
    void fisherQuantileWithTwoAndTwoDegreesOfFreedom() {
        for (double p : LOWER) {
            assertRelative(p / (1 - p), Fisher.of(2, 2).quantile(p), 1e-11, "p=" + p);
        }
    }

    @Test
    void fisherQuantileWithTwoNumeratorDegreesOfFreedom() {
        for (double p : LOWER) {
            assertRelative(3.5 * Math.expm1(-2.0 / 7 * Math.log1p(-p)), Fisher.of(2, 7).quantile(p), 1e-11, "p=" + p);
        }
    }

    @Test
    void fisherQuantileOfTheUpperTail() {
        for (int k = 10; k <= 50; k += 8) {
            double q = Math.pow(2, -k);
            double p = 1 - q;
            assertRelative(p / q, Fisher.of(2, 2).quantile(p), 1e-9, "F(2,2) k=" + k);
            assertRelative(1 / Math.pow(Math.tan(Math.PI * q / 2), 2), Fisher.of(1, 1).quantile(p), 1e-9, "F(1,1) k=" + k);
            // by the symmetry F(d1,d2) at p is the inverse of F(d2,d1) at 1-p
            assertRelative(1 / (2.5 * Math.expm1(-2.0 / 5 * Math.log1p(-q))), Fisher.of(5, 2).quantile(p), 1e-9, "F(5,2) k=" + k);
        }
    }

    @Test
    void fisherQuantileIsTheInverseOfTheCdfInTheTail() {
        for (double d1 : new double[] {0.5, 1, 2, 5, 30, 400}) {
            for (double d2 : new double[] {0.5, 1, 2, 5, 30, 400}) {
                Fisher f = Fisher.of(d1, d2);
                for (double e = 1; e <= 120; e += 11) {
                    double p = Math.pow(10, -e);
                    double q = f.quantile(p);
                    if (q == 0 || Double.isInfinite(q)) {
                        continue;
                    }
                    assertRelative(p, f.cdf(q), 1e-8, "F(" + d1 + "," + d2 + ") p=1e-" + e);
                }
            }
        }
    }

    @Test
    void fisherQuantileStillAnswersTheOrdinaryCases() {
        // the distribution tests pin the central probabilities against R; the central value is kept
        assertEquals(0.3, Fisher.of(4, 7).cdf(Fisher.of(4, 7).quantile(0.3)), 1e-13);
        assertEquals(0.99, Fisher.of(10, 20).cdf(Fisher.of(10, 20).quantile(0.99)), 1e-13);
    }

    @Test
    void inverseBetaKeepsItsValue() {
        assertEquals(0.3632574910905676, MathTools.invBetaIncReg(0.3, 2, 2), 1e-12);
        for (double p : new double[] {0.001, 0.2, 0.5, 0.8, 0.999}) {
            assertEquals(p, MathTools.betaIncReg(MathTools.invBetaIncReg(p, 3.5, 1.2), 3.5, 1.2), 1e-12, "p=" + p);
        }
    }

    @Test
    void inverseBetaLogOddsAtTheBoundaries() {
        assertEquals(Double.NEGATIVE_INFINITY, MathTools.invBetaIncRegLogit(0, 2, 3), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, MathTools.invBetaIncRegLogit(1, 2, 3), 0.0);
        assertTrue(Double.isNaN(MathTools.invBetaIncRegLogit(Double.NaN, 2, 3)));
        assertEquals(0.0, MathTools.invBetaIncRegLogit(0.5, 2, 2), 1e-12);
    }

    @Test
    void inverseBetaRejectsInvalidArguments() {
        assertThrows(ArithmeticException.class, () -> MathTools.invBetaIncRegLogit(-0.1, 2, 3));
        assertThrows(ArithmeticException.class, () -> MathTools.invBetaIncRegLogit(1.1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> MathTools.invBetaIncRegLogit(0.5, 0, 3));
        assertThrows(IllegalArgumentException.class, () -> MathTools.invBetaIncRegLogit(0.5, 2, -1));
    }

    @Test
    void inverseBetaLogOddsAreSymmetric() {
        // I_x(a,b) = p  <=>  I_{1-x}(b,a) = 1-p: the log odds change sign
        for (double p : new double[] {1e-9, 0.01, 0.25}) {
            double q = 1 - p;
            assertEquals(MathTools.invBetaIncRegLogit(p, 2.5, 4), -MathTools.invBetaIncRegLogit(q, 4, 2.5),
                    1e-6 * Math.abs(MathTools.invBetaIncRegLogit(p, 2.5, 4)), "p=" + p);
        }
    }
}
