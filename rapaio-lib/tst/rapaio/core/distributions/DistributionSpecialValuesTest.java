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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Special values of the distributions: infinite arguments, invalid parameters, invalid probabilities,
 * the degenerate normal, and sampling when the generator returns its smallest value.
 */
public class DistributionSpecialValuesTest {

    private static final double INF = Double.POSITIVE_INFINITY;

    private static Random alwaysZero() {
        return new Random() {
            @Override
            public double nextDouble() {
                return 0.0;
            }
        };
    }

    @Test
    void cdfAtInfinityIsOneForTheDistributionsOnTheHalfLine() {
        // the special functions evaluated Inf - Inf, and answered NaN
        for (Distribution d : List.of(Gamma.of(2, 3), Gamma.of(0.5, 1), ChiSquare.of(3), ChiSquare.of(1), Fisher.of(4, 7),
                Poisson.of(5), Poisson.of(0.5), Poisson.of(300), Exponential.of(2), StudentT.of(3), Normal.std(), LogNormal.of(0, 1))) {
            assertEquals(1.0, d.cdf(INF), 0.0, d.name());
            assertTrue(Double.isNaN(d.cdf(Double.NaN)), d.name() + " cdf(NaN)");
        }
        for (Distribution d : List.of(Gamma.of(2, 3), ChiSquare.of(3), Fisher.of(4, 7), Poisson.of(5), Exponential.of(2))) {
            assertEquals(0.0, d.cdf(-INF), 0.0, d.name());
        }
    }

    @Test
    void pdfAtInfinityIsZero() {
        for (Distribution d : List.of(Gamma.of(2, 1), Gamma.of(1, 1), Gamma.of(0.5, 2), ChiSquare.of(2), ChiSquare.of(4),
                ChiSquare.of(1), Fisher.of(4, 7), Fisher.of(1, 3), Fisher.of(2, 2), Poisson.of(5), Exponential.of(2), Normal.std())) {
            assertEquals(0.0, d.pdf(INF), 0.0, d.name());
            assertEquals(0.0, d.pdf(-INF), 0.0, d.name() + " at -Inf");
            assertTrue(Double.isNaN(d.pdf(Double.NaN)) || d.pdf(Double.NaN) == 0.0, d.name() + " pdf(NaN)");
        }
    }

    @Test
    void infinityDoesNotChangeTheOrdinaryValues() {
        // P(2, x/3) = 1 - e^-y (1 + y) with y = 4/3
        assertEquals(1 - Math.exp(-4.0 / 3) * (1 + 4.0 / 3), Gamma.of(2, 3).cdf(4), 1e-13);
        // P(3/2, 1/2) = erf(sqrt(1/2)) - sqrt(2/pi) e^(-1/2) = 0.19874804309879915
        assertEquals(0.19874804309879915, ChiSquare.of(3).cdf(1), 1e-12);
        // P(X <= 0) + P(X = 1) is P(X <= 1) for Poisson(1): e^-1 (1 + 1)
        assertEquals(2 * Math.exp(-1), Poisson.of(1).cdf(1), 1e-13);
    }

    @Test
    void parametersWhichAreNotNumbersAreRejected() {
        // the checks were written as `p < 0 || p > 1`, which a NaN never satisfies
        assertThrows(IllegalArgumentException.class, () -> Bernoulli.of(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Gamma.of(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> Gamma.of(1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Poisson.of(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> ChiSquare.of(Double.NaN));
    }

    @Test
    void existingParameterMessagesAreKept() {
        assertEquals("Probability parameter must be in closed interval [0,1]",
                assertThrows(IllegalArgumentException.class, () -> Bernoulli.of(12)).getMessage());
        assertEquals("degrees of freedom parameter must have value greater than zero",
                assertThrows(IllegalArgumentException.class, () -> ChiSquare.of(0)).getMessage());
    }

    @Test
    void fisherRejectsInvalidDegreesOfFreedom() {
        // the record accepted anything, and failed later with a confusing error from the beta function
        assertThrows(IllegalArgumentException.class, () -> Fisher.of(0, 5));
        assertThrows(IllegalArgumentException.class, () -> Fisher.of(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> Fisher.of(3, 0));
        assertThrows(IllegalArgumentException.class, () -> Fisher.of(Double.NaN, 2));
        assertDoesNotThrow(() -> Fisher.of(0.5, 0.5));
    }

    @Test
    void fisherModeIsTheMaximumOfTheDensity() {
        // d2 (d1 - 2) / (d1 (d2 + 2)): the former formula used (d2 - 2), which is the denominator of the mean
        for (double[] c : new double[][] {{4, 7}, {3, 3}, {5, 10}, {10, 4}, {8, 20}, {12, 50}, {3, 100}}) {
            Fisher f = Fisher.of(c[0], c[1]);
            double mode = f.mode();
            assertEquals(c[1] * (c[0] - 2) / (c[0] * (c[1] + 2)), mode, 1e-15, f.name());
            assertTrue(f.pdf(mode) >= f.pdf(mode * (1 - 1e-4)), f.name() + " left");
            assertTrue(f.pdf(mode) >= f.pdf(mode * (1 + 1e-4)), f.name() + " right");
            assertTrue(f.pdf(mode) > f.pdf(mode * 0.5), f.name() + " far left");
            assertTrue(f.pdf(mode) > f.pdf(mode * 2), f.name() + " far right");
        }
    }

    @Test
    void fisherModeForTwoNumeratorDegreesOfFreedomIsZero() {
        // the density of F(2, d) decreases from its value at 0
        assertEquals(0.0, Fisher.of(2, 7).mode(), 0.0);
        assertTrue(Fisher.of(2, 7).pdf(0) > Fisher.of(2, 7).pdf(0.5));
        // and below 2 it is unbounded at 0, with no finite mode
        assertTrue(Double.isNaN(Fisher.of(1, 7).mode()));
    }

    @Test
    void studentRejectsANegativeScale() {
        assertThrows(IllegalArgumentException.class, () -> StudentT.of(3, 0, -1));
        assertDoesNotThrow(() -> StudentT.of(3, 0, 1));
    }

    @Test
    void normalWithZeroDeviationIsTheDegenerateDistribution() {
        // the general formula gives NaN at every point; R: dnorm(2, 2, 0) == Inf, pnorm(1.9, 2, 0) == 0
        Normal n = Normal.of(2, 0);
        assertEquals(INF, n.pdf(2), 0.0);
        assertEquals(0.0, n.pdf(2.0000001), 0.0);
        assertEquals(0.0, n.pdf(-5), 0.0);
        assertEquals(0.0, n.cdf(1.9), 0.0);
        assertEquals(1.0, n.cdf(2), 0.0);
        assertEquals(1.0, n.cdf(3), 0.0);
        assertEquals(2.0, n.quantile(0.3), 0.0);
        assertEquals(2.0, n.quantile(0.999), 0.0);
        assertTrue(Double.isNaN(n.cdf(Double.NaN)));
        assertTrue(Double.isNaN(n.pdf(Double.NaN)));
    }

    @Test
    void studentWithZeroScaleIsTheDegenerateDistribution() {
        StudentT t = StudentT.of(3, 4, 0);
        assertEquals(INF, t.pdf(4), 0.0);
        assertEquals(0.0, t.pdf(5), 0.0);
        assertEquals(0.0, t.cdf(3.9), 0.0);
        assertEquals(1.0, t.cdf(4), 0.0);
        assertEquals(4.0, t.quantile(0.1), 0.0);
    }

    @Test
    void normalRejectsANegativeDeviationAsBefore() {
        assertThrows(IllegalArgumentException.class, () -> Normal.of(0, -1));
    }

    @Test
    void quantileRejectsNotANumberProbability() {
        // the javadoc of Distribution.quantile promises an IllegalArgumentException; the result used to be a
        // different arbitrary value for each distribution: mu, +Infinity, the upper bound, NaN
        for (Distribution d : List.of(StudentT.of(3, 2, 1), Fisher.of(4, 7), Hypergeometric.of(20, 20, 30), Bernoulli.of(0.9),
                Normal.std(), LogNormal.of(0, 1), Gamma.of(2, 1), Poisson.of(3), ChiSquare.of(3), Exponential.of(1), Uniform.of(0, 1))) {
            assertThrows(IllegalArgumentException.class, () -> d.quantile(Double.NaN), d.name());
        }
        assertThrows(IllegalArgumentException.class, () -> MathToolsAccess.normalQuantile(Double.NaN));
    }

    @Test
    void quantileOfFiniteProbabilitiesOutsideTheIntervalKeepsItsTestedBehaviour() {
        assertEquals(0.0, Bernoulli.of(0.3).quantile(-1), 0.0);
        assertEquals(1.0, Bernoulli.of(0.3).quantile(1.1), 0.0);
        assertThrows(IllegalArgumentException.class, () -> StudentT.of(3).quantile(-0.1));
        assertThrows(IllegalArgumentException.class, () -> Fisher.of(4, 7).quantile(1.1));
    }

    @Test
    void samplingWhenTheGeneratorReturnsZeroIsFinite() {
        // nextDouble() is in [0, 1), so 0 is a legal draw, and the quantile at 0 is the boundary of the support
        Random zero = alwaysZero();
        // LogNormal is left out: it samples with nextGaussian, which rejects a generator that only returns zeros
        for (Distribution d : List.of(Normal.std(), StudentT.of(3), Fisher.of(4, 7), Exponential.of(1),
                Uniform.of(0, 1), Normal.of(5, 2))) {
            double draw = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> d.sampleNext(zero), d.name());
            assertTrue(Double.isFinite(draw), d.name() + " drew " + draw);
        }
        assertTrue(Normal.std().sampleNext(zero) < -8, "the replacement draw must be in the far lower tail");
    }

    @Test
    void samplingDrawsOtherThanZeroAreUnchanged() {
        // the same seed must give the same sequence as the plain quantile transform
        Random a = new Random(5);
        Random b = new Random(5);
        for (int i = 0; i < 1000; i++) {
            assertEquals(Normal.std().quantile(b.nextDouble()), Normal.std().sampleNext(a), 0.0);
        }
    }

    @Test
    void bernoulliWithZeroProbabilityNeverSucceeds() {
        // `<=` compared the draw 0 with the probability 0 and gave a success
        assertEquals(0.0, Bernoulli.of(0).sampleNext(alwaysZero()), 0.0);
        assertEquals(1.0, Bernoulli.of(1).sampleNext(alwaysZero()), 0.0);
        Random random = new Random(1);
        for (int i = 0; i < 1000; i++) {
            assertEquals(1.0, Bernoulli.of(1).sampleNext(random), 0.0);
            assertEquals(0.0, Bernoulli.of(0).sampleNext(random), 0.0);
        }
    }

    @Test
    void exponentialKeepsItsPrecisionForTinyProbabilities() {
        // 1 - p rounds to 1 for p below 1e-16, which made the quantile 0; R: qexp(1e-17) == 1e-17
        Exponential e = Exponential.of(1);
        assertEquals(1e-17, e.quantile(1e-17), 1e-17 * 1e-14);
        assertEquals(1e-10, e.quantile(1e-10), 1e-10 * 1e-9);
        assertEquals(5e-18, Exponential.of(2).quantile(1e-17), 5e-18 * 1e-14);
        // and the cdf, as the inverse
        assertEquals(1e-17, e.cdf(1e-17), 1e-17 * 1e-14);
        assertEquals(1e-10, e.cdf(e.quantile(1e-10)), 1e-10 * 1e-9);
    }

    @Test
    void exponentialQuantileAndCdfAreUnchangedForOrdinaryValues() {
        Exponential e = Exponential.of(2.5);
        for (double p : new double[] {0.01, 0.3, 0.5, 0.9, 0.999}) {
            assertEquals(-Math.log(1 - p) / 2.5, e.quantile(p), 1e-13, "p=" + p);
            assertEquals(p, e.cdf(e.quantile(p)), 1e-13, "p=" + p);
        }
    }

    /** the static method is reached through the public MathTools */
    private static final class MathToolsAccess {
        static double normalQuantile(double p) {
            return rapaio.math.MathTools.normalQuantile(p, 0, 1);
        }
    }
}
