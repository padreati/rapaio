/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.io.Csv;

/**
 * Test for binomial distribution
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/16.
 */
public class BinomialTest {

    private static final double TOL = 1e-12;
    private Frame df;

    @BeforeEach
    public void setUp() throws Exception {
        df = Csv.instance()
                .header.set(true)
                .quotes.set(false)
                .read(BinomialTest.class, "binom.csv");
    }

    @Test
    void nameTest() {
        assertEquals("Binomial(p=0.5,n=10)", Binomial.of(0.5, 10).name());
    }

    @Test
    void bigN() {
        Binomial b = Binomial.of(0.5, 10000000);
        assertEquals(10000000. / 2, b.quantile(0.5), TOL);
    }

    @Test
    void testVariousArtifacts() {

        Binomial b = Binomial.of(0.2, 120);
        assertTrue(b.discrete());
        assertEquals(0, b.minValue(), TOL);
        assertEquals(120, b.maxValue(), TOL);
        assertEquals(24, b.mean(), TOL);
        assertEquals(24, b.mode(), TOL);
        assertEquals(19.2, b.var(), TOL);

        assertEquals(0.13693063937629152, b.skewness(), TOL);
        assertEquals(0.0020833333333333233, b.kurtosis(), TOL);
        assertEquals(4.1786127880975386, b.entropy(), TOL);
    }

    @Test
    void testBinomialPdf() {

        Binomial b1 = Binomial.of(0.1, 10);
        Binomial b2 = Binomial.of(0.1, 100);
        Binomial b3 = Binomial.of(0.9, 100);
        Binomial b4 = Binomial.of(0.9, 2000);

        Var x = df.rvar("x");

        Var pdf1 = df.rvar("pdf_10_0.1");
        Var pdf2 = df.rvar("pdf_100_0.1");
        Var pdf3 = df.rvar("pdf_100_0.9");
        Var pdf4 = df.rvar("pdf_2000_0.9");

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(pdf1.getDouble(i), b1.pdf(x.getDouble(i)), TOL);
            assertEquals(pdf2.getDouble(i), b2.pdf(x.getDouble(i)), TOL);
            assertEquals(pdf3.getDouble(i), b3.pdf(x.getDouble(i)), TOL);
            assertEquals(pdf4.getDouble(i), b4.pdf(x.getDouble(i)), TOL);
        }
    }

    @Test
    void testBinomialCdf() {

        Binomial b1 = Binomial.of(0.1, 10);
        Binomial b2 = Binomial.of(0.1, 100);
        Binomial b3 = Binomial.of(0.9, 100);
        Binomial b4 = Binomial.of(0.9, 2000);

        Var x = df.rvar("x");

        Var cdf1 = df.rvar("cdf_10_0.1");
        Var cdf2 = df.rvar("cdf_100_0.1");
        Var cdf3 = df.rvar("cdf_100_0.9");
        Var cdf4 = df.rvar("cdf_2000_0.9");

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(cdf1.getDouble(i), b1.cdf(x.getDouble(i)), TOL);
            assertEquals(cdf2.getDouble(i), b2.cdf(x.getDouble(i)), TOL);
            assertEquals(cdf3.getDouble(i), b3.cdf(x.getDouble(i)), TOL);
            assertEquals(cdf4.getDouble(i), b4.cdf(x.getDouble(i)), TOL);
        }
    }

    @Test
    void testBinomialQuantile() {

        Binomial b1 = Binomial.of(0.1, 10);
        Binomial b2 = Binomial.of(0.1, 100);
        Binomial b3 = Binomial.of(0.9, 100);
        Binomial b4 = Binomial.of(0.9, 2000);

        Var x = df.rvar("x");

        Var q1 = df.rvar("q_10_0.1");
        Var q2 = df.rvar("q_100_0.1");
        Var q3 = df.rvar("q_100_0.9");
        Var q4 = df.rvar("q_2000_0.9");

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(q1.getDouble(i), b1.quantile(x.getDouble(i)), TOL);
            assertEquals(q2.getDouble(i), b2.quantile(x.getDouble(i)), TOL);
            assertEquals(q3.getDouble(i), b3.quantile(x.getDouble(i)), TOL);
            assertEquals(q4.getDouble(i), b4.quantile(x.getDouble(i)), TOL);
        }
    }
}
