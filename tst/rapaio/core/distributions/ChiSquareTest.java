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

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.io.Csv;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ChiSquareTest {

    private static final double ERROR = 1e-9;

    @Test
    public void testWithR() throws IOException {
        Frame df = new Csv()
                .withHeader(true)
                .withSeparatorChar(',')
                .withDefaultTypes(VarType.NUMERIC)
                .withNAValues("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "chisq.csv");

        ChiSquare c1 = new ChiSquare(1);
        ChiSquare c2 = new ChiSquare(2);
        ChiSquare c5 = new ChiSquare(5);
        ChiSquare c10 = new ChiSquare(10);
        ChiSquare c100 = new ChiSquare(100);

        for (int i = 1; i < df.getRowCount(); i++) {

            double x = df.getValue(i, "x");

            assertEquals(df.getValue(i, "pdf_1"), c1.pdf(x), ERROR);
            assertEquals(df.getValue(i, "cdf_1"), c1.cdf(x), ERROR);
            if (x > 0 && x < 1) {
                assertEquals(df.getValue(i, "quantile_1"), c1.quantile(df.getValue(i, "x")), ERROR);
            }
            assertEquals(df.getValue(i, "pdf_2"), c2.pdf(x), ERROR);
            assertEquals(df.getValue(i, "cdf_2"), c2.cdf(x), ERROR);
            if (x > 0 && x < 1) {
                assertEquals(df.getValue(i, "quantile_2"), c2.quantile(df.getValue(i, "x")), ERROR);
            }
            assertEquals(df.getValue(i, "pdf_5"), c5.pdf(x), ERROR);
            assertEquals(df.getValue(i, "cdf_5"), c5.cdf(x), ERROR);
            if (x > 0 && x < 1) {
                assertEquals(df.getValue(i, "quantile_5"), c5.quantile(df.getValue(i, "x")), ERROR);
            }
            assertEquals(df.getValue(i, "pdf_10"), c10.pdf(x), ERROR);
            assertEquals(df.getValue(i, "cdf_10"), c10.cdf(x), ERROR);
            if (x > 0 && x < 1) {
                assertEquals(df.getValue(i, "quantile_10"), c10.quantile(df.getValue(i, "x")), ERROR);
            }
            assertEquals(df.getValue(i, "pdf_100"), c100.pdf(x), ERROR);
            assertEquals(df.getValue(i, "cdf_100"), c100.cdf(x), ERROR);
            if (x > 0 && x < 1) {
                assertEquals(df.getValue(i, "quantile_100"), c100.quantile(df.getValue(i, "x")), ERROR);
            }
        }
    }

    @Test
    public void testOtherChiSq() {
        ChiSquare c = new ChiSquare(1);

        assertEquals("ChiSq(df=1)", c.name());
        assertEquals(false, c.discrete());
        assertEquals(0, c.min(), ERROR);
        assertEquals(Double.POSITIVE_INFINITY, c.max(), ERROR);
        assertEquals(1, c.mean(), ERROR);
        assertEquals(2, c.var(), ERROR);
        assertEquals(0, c.mode(), ERROR);
        assertEquals(2.8284271247461903, c.skewness(), ERROR);
        assertEquals(12, c.kurtosis(), ERROR);

        ChiSquare c2 = new ChiSquare(2);
        Var sample = c2.sample(100);
        long count = sample.stream().mapToDouble().filter(x -> x > 0).count();
        assertEquals(sample.getRowCount(), count);

        sample = c.sample(100);
        count = sample.stream().mapToDouble().filter(x -> x > 0).count();
        assertEquals(sample.getRowCount(), count);
    }
}
