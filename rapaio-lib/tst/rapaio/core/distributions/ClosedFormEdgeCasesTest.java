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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * Edge cases of the closed form distributions {@link Uniform}, {@link DUniform} and
 * {@link Exponential}: parameter validation, the quantile contract at and outside the
 * boundaries, and (for {@link Exponential}, previously untested) values against scipy 1.18.
 */
public class ClosedFormEdgeCasesTest {

    private static final double TOL = 1e-12;

    @Test
    void quantileRejectsProbabilitiesOutsideUnitInterval() {
        List<Distribution> all = List.of(Uniform.of(-2, 5), DUniform.of(-3, 3), Exponential.of(2.5));
        for (Distribution d : all) {
            for (double p : new double[] {-0.1, 1.1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
                assertThrows(IllegalArgumentException.class, () -> d.quantile(p), d.name() + " quantile(" + p + ")");
            }
            assertEquals(d.minValue(), d.quantile(0), 0, d.name() + " quantile(0)");
            assertEquals(d.maxValue(), d.quantile(1), 0, d.name() + " quantile(1)");
        }
    }

    @Test
    void dUniformQuantileIsSmallestSupportPointReachingP() {
        DUniform d = DUniform.of(-3, 3);
        // was a - 1 = -4, outside the support
        assertEquals(-3, d.quantile(0), 0);
        assertEquals(-3, d.quantile(1e-9), 0);
        assertEquals(-3, d.quantile(1.0 / 7), 0);
        assertEquals(-2, d.quantile(1.0 / 7 + 1e-12), 0);
        assertEquals(0, d.quantile(0.5), 0);
        assertEquals(3, d.quantile(0.99), 0);
        assertEquals(3, d.quantile(1), 0);
        for (double p = 0.001; p < 1; p += 0.0137) {
            double q = d.quantile(p);
            assertTrue(q >= d.minValue() && q <= d.maxValue(), "in support at p=" + p);
            assertTrue(d.cdf(q) >= p, "cdf(q) >= p at p=" + p);
            assertTrue(d.cdf(q - 1) < p, "cdf(q-1) < p at p=" + p);
        }
        assertEquals(8, DUniform.of(8, 8).quantile(0), 0);
        assertEquals(8, DUniform.of(8, 8).quantile(1), 0);
    }

    @Test
    void dUniformValidatesAndKeepsIntegerParameters() {
        assertThrows(IllegalArgumentException.class, () -> DUniform.of(3, 2));
        DUniform d = DUniform.of(1, 6);
        assertEquals(3.5, d.mean(), 0);
        assertEquals(1, d.a());
        assertEquals(6, d.b());
        assertEquals("DUniform(a=1,b=6)", d.name());

        // the whole int range: n = 2^32 does not fit an int
        DUniform full = DUniform.of(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertEquals(-0.5, full.mean(), 0);
        assertEquals(1.0 / 4294967296.0, full.pdf(0), 0);
        assertEquals(Integer.MIN_VALUE, full.quantile(0), 0);
        assertEquals(Integer.MAX_VALUE, full.quantile(1), 0);
        assertEquals(32 * Math.log(2), full.entropy(), TOL);

        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            assertEquals("DUniform(a=1,b=6)", DUniform.of(1, 6).name());
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    void uniformRejectsDegenerateOrInvalidIntervals() {
        assertThrows(IllegalArgumentException.class, () -> Uniform.of(1, 1));
        assertThrows(IllegalArgumentException.class, () -> Uniform.of(2, 1));
        assertThrows(IllegalArgumentException.class, () -> Uniform.of(Double.NEGATIVE_INFINITY, 0));
        assertThrows(IllegalArgumentException.class, () -> Uniform.of(0, Double.NaN));
        Uniform u = Uniform.of(-2, 5);
        assertEquals(1.0 / 7, u.pdf(-2), 0);
        assertEquals(1.0 / 7, u.pdf(5), 0);
        assertEquals(0, u.cdf(-2), 0);
        assertEquals(1, u.cdf(5), 0);
    }

    @Test
    void exponentialValidatesRate() {
        for (double lambda : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> Exponential.of(lambda), "lambda=" + lambda);
        }
    }

    @Test
    void exponentialMatchesReference() {
        // scipy.stats.expon(scale=1/2.5)
        Exponential e = Exponential.of(2.5);
        assertEquals("Exponential(lambda=2.5)", e.name());
        assertEquals(0, e.pdf(-1), 0);
        assertEquals(0, e.cdf(-1), 0);
        assertEquals(2.5, e.pdf(0), TOL);
        assertEquals(1.1809163818525368, e.pdf(0.3), TOL);
        assertEquals(0.205212496559747, e.pdf(1), TOL);
        assertEquals(0.5276334472589852, e.cdf(0.3), TOL);
        assertEquals(0.9179150013761012, e.cdf(1), TOL);
        assertEquals(0.9999546000702375, e.cdf(4), TOL);
        assertEquals(0.04214420626313053, e.quantile(0.1), TOL);
        assertEquals(0.2772588722239781, e.quantile(0.5), TOL);
        assertEquals(0.9210340371976184, e.quantile(0.9), TOL);
        assertEquals(2.7631021115928545, e.quantile(0.999), TOL);
        assertEquals(0.2772588722239781, e.median(), TOL);
        assertEquals(0, e.quantile(0), 0);
        assertEquals(Double.POSITIVE_INFINITY, e.quantile(1), 0);
        for (double p = 0.01; p < 1; p += 0.07) {
            assertEquals(p, e.cdf(e.quantile(p)), TOL, "cdf(quantile(p)) at p=" + p);
        }

        Random random = new Random(42);
        double sum = 0;
        int n = 100_000;
        for (int i = 0; i < n; i++) {
            double x = e.sampleNext(random);
            assertTrue(x >= 0);
            sum += x;
        }
        assertEquals(e.mean(), sum / n, 5e-3);
    }
}
