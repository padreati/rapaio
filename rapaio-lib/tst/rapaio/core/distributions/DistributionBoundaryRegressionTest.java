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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for boundary behaviour of distributions: the entropy of a Poisson with a very large rate,
 * the quantile of unbounded or half bounded distributions at probability 0 and 1, and the quantile of a
 * binomial with a very large number of trials.
 */
public class DistributionBoundaryRegressionTest {

    /**
     * Entropy by direct summation, with the log of the factorial accumulated from logs of integers,
     * which is independent of the saddle point and series code used by the implementation.
     */
    private static double bruteForcePoissonEntropy(double lambda) {
        int to = (int) (lambda + 60 * Math.sqrt(lambda) + 60);
        double lnFact = 0;
        double sum = 0;
        double logLambda = Math.log(lambda);
        for (int k = 0; k <= to; k++) {
            if (k > 0) {
                lnFact += Math.log(k);
            }
            double logPmf = k * logLambda - lambda - lnFact;
            double pmf = Math.exp(logPmf);
            if (pmf > 0) {
                sum -= pmf * logPmf;
            }
        }
        return sum;
    }

    @Test
    void poissonEntropyMatchesBruteForceSummation() {
        for (double lambda : new double[] {0.5, 1, 7.3, 50, 300, 999}) {
            assertEquals(bruteForcePoissonEntropy(lambda), Poisson.of(lambda).entropy(), 1e-9, "lambda=" + lambda);
        }
    }

    @Test
    void poissonEntropyAtTheSwitchToTheAsymptoticSeries() {
        // the summation covers rates below 1000 and the series the rest, both have to agree with the
        // brute force value on both sides of the switch, and with each other at the switch
        for (double lambda : new double[] {1000, 2500, 5000}) {
            assertEquals(bruteForcePoissonEntropy(lambda), Poisson.of(lambda).entropy(), 1e-9, "lambda=" + lambda);
        }
        assertEquals(Poisson.of(Math.nextDown(1000.0)).entropy(), Poisson.of(1000.0).entropy(), 1e-9);
    }

    @Test
    void poissonEntropyOfRatesBeyondTheIntRangeTerminates() {
        // the range of the summation used to be computed in int, which saturates near 2.1e9 and turned the
        // loop into an endless one
        for (double lambda : new double[] {2.15e9, 3e9, 1e12}) {
            double entropy = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> Poisson.of(lambda).entropy(),
                    "entropy did not terminate for lambda=" + lambda);
            assertEquals(0.5 * Math.log(2 * Math.PI * Math.E * lambda), entropy, 1e-6, "lambda=" + lambda);
        }
    }

    @Test
    void poissonEntropyOfHugeRatesGrowsLikeHalfLogLambda() {
        double previous = Poisson.of(1e6).entropy();
        for (double lambda : new double[] {1e8, 2.1e9, 2.15e9, 3e9, 1e12, 1e15}) {
            double entropy = Poisson.of(lambda).entropy();
            assertEquals(0.5 * Math.log(2 * Math.PI * Math.E * lambda), entropy, 1e-6, "lambda=" + lambda);
            assertTrue(entropy > previous, "entropy must grow with the rate, lambda=" + lambda);
            previous = entropy;
        }
    }

    @Test
    void poissonEntropyKeepsItsPrecisionForLargeModerateRates() {
        // 1e6 and 1e7 are summed rates in the old code's range, where its result was off by 7e-3 and 7e-2
        for (double lambda : new double[] {1e6, 1e7}) {
            double expected = 0.5 * Math.log(2 * Math.PI * Math.E * lambda) - 1 / (12 * lambda);
            assertEquals(expected, Poisson.of(lambda).entropy(), 1e-9, "lambda=" + lambda);
        }
    }

    @Test
    void studentQuantileAtTheBoundariesIsInfinite() {
        for (StudentT t : new StudentT[] {StudentT.of(3), StudentT.of(1), StudentT.of(30, 2, 5)}) {
            assertEquals(Double.NEGATIVE_INFINITY, t.quantile(0), t.name());
            assertEquals(Double.POSITIVE_INFINITY, t.quantile(1), t.name());
            // the boundaries continue the interior monotonically
            assertTrue(t.quantile(0) < t.quantile(1e-9));
            assertTrue(t.quantile(1e-9) < t.quantile(0.5));
            assertTrue(t.quantile(0.5) < t.quantile(1 - 1e-9));
            assertTrue(t.quantile(1 - 1e-9) < t.quantile(1));
        }
    }

    @Test
    void fisherQuantileAtTheBoundaries() {
        for (Fisher f : new Fisher[] {Fisher.of(4, 7), Fisher.of(1, 1), Fisher.of(30, 2)}) {
            assertEquals(0.0, f.quantile(0), f.name());
            assertEquals(Double.POSITIVE_INFINITY, f.quantile(1), f.name());
            // the margin is 1e-6 and not smaller: the root search resolves x to an absolute 1e-15, which
            // for extreme tails is coarser than the quantile itself (a separate accuracy limit of the search)
            assertTrue(f.quantile(1e-6) > 0);
            assertTrue(f.quantile(1 - 1e-6) < f.quantile(1));
            // and the cdf is the inverse of the quantile strictly inside
            assertEquals(0.3, f.cdf(f.quantile(0.3)), 1e-9, f.name());
        }
    }

    /**
     * The quantile is the smallest k with cdf(k) >= p.
     */
    private static void assertIsSmallestKWithCdfAtLeast(Binomial b, double p, double k) {
        assertTrue(b.cdf(k) >= p * (1 - 1e-12), "cdf(" + k + ")=" + b.cdf(k) + " < p=" + p);
        if (k > 0) {
            assertTrue(b.cdf(k - 1) < p * (1 + 1e-12), "cdf(" + (k - 1) + ")=" + b.cdf(k - 1) + " >= p=" + p + ", k is not the smallest");
        }
    }

    @Test
    void binomialQuantileNearTheTopOfTheSupportForLargeN() {
        // n - X ~ Bin(1e6, 1e-6) ~ Poisson(1): cdf(999997) = P(Y>=3) ~ 0.0803, cdf(999998) = P(Y>=2) ~ 0.2642,
        // thus the quantile of every probability in (0.0803, 0.2642] is 999998. The coarse right search used
        // to step over the end of the support, keep a stale cdf value and never come back, answering n.
        Binomial b = Binomial.of(0.999999, 1_000_000);
        for (double p : new double[] {0.081, 0.083, 0.085, 0.09, 0.2, 0.26}) {
            assertEquals(999998.0, b.quantile(p), "p=" + p);
        }
        assertEquals(999999.0, b.quantile(0.27));
        assertEquals(999997.0, b.quantile(0.05));
    }

    @Test
    void binomialQuantileOfLargeNFollowsItsDefinition() {
        for (double prob : new double[] {0.999999, 0.99999, 0.9999, 1e-6, 0.3, 0.5}) {
            Binomial b = Binomial.of(prob, 200_000);
            for (double p = 0.01; p < 1; p += 0.0137) {
                assertIsSmallestKWithCdfAtLeast(b, p, b.quantile(p));
            }
        }
    }
}
