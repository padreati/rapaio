/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.io.Csv;

import static org.junit.jupiter.api.Assertions.*;

public class PoissonTest {

    private static final double TOL = 1e-13;

    private Frame df;
    private Poisson pois1;
    private Poisson pois5;
    private Poisson pois10;
    private Poisson pois100;

    @BeforeEach
    void setUp() throws Exception {
        df = Csv.instance()
                .withNAValues("NaN", "Inf")
                .read(rapaio.core.distributions.HypergeometricTest.class, "pois.csv");

        pois1 = Poisson.of(1);
        pois5 = Poisson.of(5);
        pois10 = Poisson.of(10);
        pois100 = Poisson.of(100);
    }

    @Test
    void testMiscelaneous() {
        assertEquals("Poisson(lambda=1)", pois1.name());
        assertTrue(pois1.discrete());
        assertEquals(0, pois1.pdf(-1), TOL);
        assertEquals(0, pois1.cdf(-1), TOL);
        assertEquals(Double.POSITIVE_INFINITY, pois1.quantile(1), TOL);

        assertEquals(0, pois1.min(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, pois1.max(), TOL);
        assertEquals(1, pois1.mean(), TOL);
        assertEquals(1, pois1.mode(), TOL);
        assertEquals(1, pois1.var(), TOL);
        assertEquals(1, pois1.skewness(), TOL);
        assertEquals(1, pois1.kurtosis(), TOL);
        assertEquals(0, pois1.entropy(), TOL);
    }

    @Test
    void testInvalidLambda() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Poisson.of(-1));
        assertEquals("lambda parameter value must be a real positive value", ex.getMessage());
    }

    @Test
    void testRPdf() {
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "pdf_1"), pois1.pdf(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "pdf_5"), pois5.pdf(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "pdf_10"), pois10.pdf(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "pdf_100"), pois100.pdf(df.getDouble(i, "x")), TOL);
        }
    }

    @Test
    void testRCdf() {
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "cdf_1"), pois1.cdf(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "cdf_5"), pois5.cdf(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "cdf_10"), pois10.cdf(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "cdf_100"), pois100.cdf(df.getDouble(i, "x")), TOL);
        }
    }

    @Test
    void testRQuantile() {
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.getDouble(i, "x") >= 1)
                break;
            assertEquals(df.getDouble(i, "q_1"), pois1.quantile(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "q_5"), pois5.quantile(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "q_10"), pois10.quantile(df.getDouble(i, "x")), TOL);
            assertEquals(df.getDouble(i, "q_100"), pois100.quantile(df.getDouble(i, "x")), TOL);
        }
    }
}
