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

import static java.lang.Math.sqrt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tests.KSTestOneSample;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.io.Csv;

public class GammaTest {

    private static final double TOL = 1e-13;
    private Frame df;
    private Gamma g_low_low;
    private Gamma g_one_low;
    private Gamma g_high_low;
    private Gamma g_low_one;
    private Gamma g_one_one;
    private Gamma g_high_one;
    private Gamma g_low_high;
    private Gamma g_one_high;
    private Gamma g_high_high;

    @BeforeEach
    void beforeEach() throws IOException {
        df = Csv.instance().naValues.set("NaN").read(HypergeometricTest.class, "gamma.csv").mapRows(Mapping.range(1_000));
        g_low_low = Gamma.of(0.5, 0.5);
        g_one_low = Gamma.of(0.5, 1);
        g_high_low = Gamma.of(0.5, 5);
        g_low_one = Gamma.of(1, 0.5);
        g_one_one = Gamma.of(1, 1);
        g_high_one = Gamma.of(1, 5);
        g_low_high = Gamma.of(5, 0.5);
        g_one_high = Gamma.of(5, 1);
        g_high_high = Gamma.of(5, 5);
    }

    @Test
    void testInvalidParamAlpha() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Gamma.of(-1, 1));
        Assertions.assertEquals("Value parameters alpha (-1) and beta (1) parameters should be strictly positive.", ex.getMessage());
    }

    @Test
    void testInvalidParamBeta() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Gamma.of(1, -1));
        assertEquals("Value parameters alpha (1) and beta (-1) parameters should be strictly positive.", ex.getMessage());
    }

    @Test
    void testNotImplementedEntropy() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Gamma.of(2, 3).entropy());
        assertEquals("Not implemented", ex.getMessage());
    }

    @Test
    void testMiscelaneous() {
        Gamma g = Gamma.of(0.5, 0.5);

        assertFalse(g.discrete());
        assertEquals("Gamma(alpha=0.5, beta=0.5)", g.name());
        assertEquals(0, g.minValue(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, g.maxValue(), TOL);
        assertEquals(Double.NaN, g.pdf(-1), TOL);
        assertEquals(0, g.cdf(-1), TOL);
        assertEquals(Double.NaN, g.mode(), TOL);

        assertEquals(1.5, Gamma.of(3, 2).mean(), TOL);
        assertEquals(sqrt(2), Gamma.of(3, 2).skewness(), TOL);
        assertEquals(3 / 4., Gamma.of(3, 2).var(), TOL);
        assertEquals(2, Gamma.of(3, 2).kurtosis(), TOL);
    }

    @Test
    void testRPdf() {
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "pdf_1"), g_low_low.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_2"), g_low_one.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_3"), g_low_high.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_4"), g_one_low.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_5"), g_one_one.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_6"), g_one_high.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_7"), g_high_low.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_8"), g_high_one.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "pdf_9"), g_high_high.pdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
        }
    }

    @Test
    void testRCdf() {
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "cdf_1"), g_low_low.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_2"), g_low_one.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_3"), g_low_high.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_4"), g_one_low.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_5"), g_one_one.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_6"), g_one_high.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_7"), g_high_low.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_8"), g_high_one.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "cdf_9"), g_high_high.cdf(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
        }
    }

    @Test
    void testRQuantile() {
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.getDouble(i, "x") > 1) {
                break;
            }
            assertEquals(df.getDouble(i, "q_1"), g_low_low.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_2"), g_low_one.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_3"), g_low_high.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_4"), g_one_low.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_5"), g_one_one.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_6"), g_one_high.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_7"), g_high_low.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_8"), g_high_one.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
            assertEquals(df.getDouble(i, "q_9"), g_high_high.quantile(df.getDouble(i, "x")), TOL,
                    String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")));
        }
    }

    @Test
    void testSampling() {
        Gamma g1 = Gamma.of(10, 10);
        Var sample1 = g1.sample(new Random(123), 100);
        assertTrue(KSTestOneSample.from(sample1, g1).pValue() > 0.05);

        Gamma g2 = Gamma.of(0.1, 0.1);
        Var sample2 = g2.sample(new Random(123), 100);
        assertTrue(KSTestOneSample.from(sample2, g2).pValue() > 0.05);
    }
}
