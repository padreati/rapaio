/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 11/4/14.
 */
public class DUniformTest {

    private static final double TOL = 1e-12;

    @Test
    void testOtherFeatures() {
        DUniform du = DUniform.of(1, 6);

        assertTrue(du.discrete());
        assertEquals(1, du.a(), TOL);
        assertEquals(6, du.b(), TOL);
        assertEquals(3.5, du.mean(), TOL);
        assertEquals(1, du.minValue(), TOL);
        assertEquals(6, du.maxValue(), TOL);
        assertEquals(Double.NaN, du.mode(), TOL);
        assertEquals(2.9166666666666665, du.var(), TOL);
        assertEquals(0, du.skewness(), TOL);
        assertEquals(-1.2685714285714285, du.kurtosis(), TOL);
        assertEquals(1.791759469228055, du.entropy(), TOL);

        assertEquals(8, DUniform.of(8, 8).quantile(0.7), TOL);
    }

    @Test
    void testLowQuantile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DUniform.of(1, 6).quantile(-0.1));
        assertEquals("Probability must be interface the range [0,1], not -0.1", ex.getMessage());
    }

    @Test
    void testHighQuantile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DUniform.of(1, 6).quantile(1.1));
        assertEquals("Probability must be interface the range [0,1], not 1.1", ex.getMessage());
    }

    @Test
    void testDUniformPdf() {
        DUniform u = DUniform.of(0, 5);

        assertEquals("DUniform(a=0,b=5)", u.name());

        assertEquals(0.0, u.pdf(-1), TOL);
        assertEquals(0.0, u.pdf(0.5), TOL);
        assertEquals(1 / 6.0, u.pdf(0), TOL);
        assertEquals(1 / 6.0, u.pdf(3), TOL);
        assertEquals(1 / 6.0, u.pdf(5), TOL);
        assertEquals(0.0, u.pdf(6), TOL);
        assertEquals(0.0, u.pdf(0.5), TOL);
        assertEquals(0.0, u.pdf(1.5), TOL);
        assertEquals(0.0, u.pdf(Double.POSITIVE_INFINITY), TOL);
        assertEquals(0.0, u.pdf(Double.NaN), TOL);
    }

    @Test
    void testDUniformCdf() {
        DUniform u = DUniform.of(0, 5);

        assertEquals("DUniform(a=0,b=5)", u.name());

        assertEquals(0.0, u.cdf(-1), TOL);
        assertEquals(1 / 6.0, u.cdf(0.5), TOL);
        assertEquals(1 / 6.0, u.cdf(0), TOL);
        assertEquals(4 / 6.0, u.cdf(3), TOL);
        assertEquals(1.0, u.cdf(5), TOL);
        assertEquals(1.0, u.cdf(6), TOL);
    }

    @Test
    void testDUniformQuantile() {
        DUniform u = DUniform.of(0, 5);

        assertEquals("DUniform(a=0,b=5)", u.name());

        assertEquals(0, u.quantile(1 / 6.0), TOL);
        assertEquals(1, u.quantile(1.2 / 6.0), TOL);
        assertEquals(1, u.quantile(2 / 6.0), TOL);
        assertEquals(2, u.quantile(3 / 6.0), TOL);
        assertEquals(3, u.quantile(4 / 6.0), TOL);
        assertEquals(4, u.quantile(5 / 6.0), TOL);
        assertEquals(5, u.quantile(6 / 6.0), TOL);
    }
}
