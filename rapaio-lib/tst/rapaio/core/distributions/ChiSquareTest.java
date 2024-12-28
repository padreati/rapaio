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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.io.Csv;

public class ChiSquareTest {

    private static final double TOL = 1e-9;

    @Test
    void testWithR() throws IOException {
        Frame df = Csv.instance()
                .header.set(true)
                .separatorChar.set(',')
                .defaultTypes.set(VarType.DOUBLE)
                .naValues.set("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "chisq.csv");

        ChiSquare c1 = ChiSquare.of(1);
        ChiSquare c2 = ChiSquare.of(2);
        ChiSquare c5 = ChiSquare.of(5);
        ChiSquare c10 = ChiSquare.of(10);
        ChiSquare c100 = ChiSquare.of(100);

        for (int i = 1; i < df.rowCount(); i++) {

            double x = df.getDouble(i, "x");

            assertEquals(df.getDouble(i, "pdf_1"), c1.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_1"), c1.cdf(x), TOL);
            if (x > 0 && x < 1) {
                assertEquals(df.getDouble(i, "quantile_1"), c1.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_2"), c2.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_2"), c2.cdf(x), TOL);
            if (x > 0 && x < 1) {
                assertEquals(df.getDouble(i, "quantile_2"), c2.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_5"), c5.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_5"), c5.cdf(x), TOL);
            if (x > 0 && x < 1) {
                assertEquals(df.getDouble(i, "quantile_5"), c5.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_10"), c10.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_10"), c10.cdf(x), TOL);
            if (x > 0 && x < 1) {
                assertEquals(df.getDouble(i, "quantile_10"), c10.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_100"), c100.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_100"), c100.cdf(x), TOL);
            if (x > 0 && x < 1) {
                assertEquals(df.getDouble(i, "quantile_100"), c100.quantile(df.getDouble(i, "x")), TOL);
            }
        }
    }

    @Test
    void testOtherChiSq() {
        ChiSquare c = ChiSquare.of(1);

        assertEquals("ChiSq(df=1)", c.name());
        assertFalse(c.discrete());
        assertEquals(0, c.minValue(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, c.maxValue(), TOL);
        assertEquals(1, c.mean(), TOL);
        assertEquals(2, c.var(), TOL);
        assertEquals(0, c.mode(), TOL);
        assertEquals(2.8284271247461903, c.skewness(), TOL);
        assertEquals(12, c.kurtosis(), TOL);

        ChiSquare c1 = ChiSquare.of(1);
        Var sample1 = c1.sample(1000_000);
        assertEquals(sample1.size(), sample1.stream().mapToDouble().filter(x -> x > 0).count());

        ChiSquare c2 = ChiSquare.of(2);
        Var sample = c2.sample(100);
        long count = sample.stream().mapToDouble().filter(x -> x > 0).count();
        assertEquals(sample.size(), count);

        sample = c.sample(100);
        count = sample.stream().mapToDouble().filter(x -> x > 0).count();
        assertEquals(sample.size(), count);
        assertEquals(0, ChiSquare.of(2).pdf(-10), TOL);
        assertEquals(0, ChiSquare.of(3).cdf(-10), TOL);
    }

    @Test
    void testInvalidDf() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ChiSquare.of(0));
        assertEquals("degrees of freedom parameter must have value greater than zero", ex.getMessage());
    }

    @Test
    void testNotImplementedEntropy() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ChiSquare.of(12).entropy());
        assertEquals("Not implemented", ex.getMessage());
    }
}
