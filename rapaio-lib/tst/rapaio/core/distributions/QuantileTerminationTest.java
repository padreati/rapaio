/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for the search based {@code quantile} implementations of
 * {@link Gamma}, {@link ChiSquare} and {@link Poisson}. The original code
 * doubled an upper bound {@code while (cdf(up) <= p)}, which never stops for
 * {@code p > 1} (the bound reaches {@code +Inf} where the cdf is 1) or, for
 * {@link Poisson}, overflowed its {@code int} bound. {@link ChiSquare} also
 * stopped only on {@code |cdf(mid) - p| < 1e-14}, a condition that adjacent
 * doubles cannot always satisfy for large degrees of freedom.
 * <p>
 * Reference values come from scipy 1.18 ({@code stats.<dist>.ppf}).
 */
public class QuantileTerminationTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final List<Distribution> SEARCH_BASED = List.of(
            Gamma.of(2, 3), Gamma.of(0.5, 1), Gamma.of(1e6, 1),
            ChiSquare.of(1), ChiSquare.of(3), ChiSquare.of(1e6),
            Poisson.of(0.5), Poisson.of(7.5), Poisson.of(3e9)
    );

    private static double timed(DoubleUnaryOperator f, double p) {
        return assertTimeoutPreemptively(TIMEOUT, () -> f.applyAsDouble(p));
    }

    @Test
    void probabilitiesOutsideUnitIntervalAreRejected() {
        for (Distribution d : SEARCH_BASED) {
            for (double p : new double[] {1.5, 1 + 1e-12, -0.1, -1e-12, Double.NaN,
                    Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
                assertTimeoutPreemptively(TIMEOUT,
                        () -> assertThrows(IllegalArgumentException.class, () -> d.quantile(p),
                                d.name() + " quantile(" + p + ")"),
                        d.name() + " quantile(" + p + ") did not terminate");
            }
        }
    }

    @Test
    void boundaryProbabilitiesMapToSupportEnds() {
        for (Distribution d : SEARCH_BASED) {
            assertEquals(Double.POSITIVE_INFINITY, timed(d::quantile, 1.0), d.name() + " quantile(1)");
            assertEquals(0.0, timed(d::quantile, 0.0), d.name() + " quantile(0)");
        }
    }

    @Test
    void gammaQuantileMatchesReference() {
        // 1e-12 relative: the limit of incGamma's accuracy at alpha = 1e6, not of the search
        assertEquals(999999.6666666864, timed(Gamma.of(1e6, 1)::quantile, 0.5), 1e-5);
        assertEquals(50.06526237248832, timed(Gamma.of(2, 3)::quantile, 0.999999), 1e-9);
        assertEquals(7.85398163397448e-21, timed(Gamma.of(0.5, 1)::quantile, 1e-10), 1e-30);
    }

    @Test
    void continuousSearchIsExactInverseOfImplementedCdf() {
        for (Distribution d : List.of(Gamma.of(2, 3), Gamma.of(0.5, 1), Gamma.of(1e6, 1),
                ChiSquare.of(1), ChiSquare.of(3), ChiSquare.of(1e6), ChiSquare.of(1e8))) {
            for (double p : new double[] {1e-12, 0.01, 0.3, 0.5, 0.9, 0.999999}) {
                double q = timed(d::quantile, p);
                assertTrue(d.cdf(q) >= p, d.name() + " cdf(q) >= p at p=" + p);
                assertTrue(d.cdf(Math.nextDown(q)) < p, d.name() + " cdf(nextDown(q)) < p at p=" + p);
            }
        }
    }

    @Test
    void chiSquareQuantileMatchesReferenceForLargeDegreesOfFreedom() {
        assertEquals(999999.3333334123, timed(ChiSquare.of(1e6)::quantile, 0.5), 1e-6);
        assertEquals(99992583.37351976, timed(ChiSquare.of(1e8)::quantile, 0.3), 1e-4);
        assertEquals(6.251388631170325, timed(ChiSquare.of(3)::quantile, 0.9), 1e-12);
        assertEquals(2.417987942718036e-08, timed(ChiSquare.of(3)::quantile, 1e-12), 1e-20);
    }

    @Test
    void poissonQuantileSurvivesLargeRates() {
        Poisson pois = Poisson.of(3e9);
        assertEquals(3_000_000_000.0, timed(pois::quantile, 0.5), 0);
        assertEquals(3_000_107_352.0, timed(pois::quantile, 0.975), 0);
    }

    @Test
    void poissonQuantileIsSmallestIntegerWithCdfAtLeastP() {
        Poisson pois = Poisson.of(7.5);
        // scipy: ppf(0.3) = 6, cdf(5) = 0.2414...
        assertEquals(6.0, timed(pois::quantile, 0.3), 0);
        // exactly at a cdf value the quantile is that integer
        double c5 = pois.cdf(5);
        assertEquals(5.0, timed(pois::quantile, c5), 0);
        for (double p = 0.01; p < 1; p += 0.07) {
            double q = timed(pois::quantile, p);
            assertEquals(q, Math.rint(q), 0, "integer quantile");
            assertTrue(pois.cdf(q) >= p, "cdf(q) >= p at p=" + p);
            assertTrue(q == 0 || pois.cdf(q - 1) < p, "cdf(q-1) < p at p=" + p);
        }
    }
}
