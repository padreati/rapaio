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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.io.Csv;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import static rapaio.graphics.Plotter.lines;

/**
 * Test for binomial distribution
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/16.
 */
public class BinomialTest {

    private static final double TOL = 1e-12;
    private Frame df;

    @Before
    public void setUp() throws Exception {
        df = new Csv()
                .withHeader(true)
                .withQuotes(false)
                .read(BinomialTest.class, "binom.csv");
    }

    @Test
    public void testVariousArtifacts() {

        Binomial b = new Binomial(0.2, 120);
        Assert.assertTrue(b.discrete());
        Assert.assertEquals(0, b.min(), TOL);
        Assert.assertEquals(120, b.max(), TOL);
        Assert.assertEquals(24, b.mean(), TOL);
        Assert.assertEquals(24, b.mode(), TOL);
        Assert.assertEquals(19.2, b.var(), TOL);

        Assert.assertEquals(0.13693063937629152, b.skewness(), TOL);
        Assert.assertEquals(0.0020833333333333233, b.kurtosis(), TOL);
        Assert.assertEquals(4.1786127880975386, b.entropy(), TOL);
    }

    @Test
    public void testBinomialPdf() {

        Binomial b1 = new Binomial(0.1, 10);
        Binomial b2 = new Binomial(0.1, 100);
        Binomial b3 = new Binomial(0.9, 100);
        Binomial b4 = new Binomial(0.9, 2000);

        Var x = df.var("x");

        Var pdf1 = df.var("pdf_10_0.1");
        Var pdf2 = df.var("pdf_100_0.1");
        Var pdf3 = df.var("pdf_100_0.9");
        Var pdf4 = df.var("pdf_2000_0.9");

        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(pdf1.value(i), b1.pdf(x.value(i)), TOL);
            Assert.assertEquals(pdf2.value(i), b2.pdf(x.value(i)), TOL);
            Assert.assertEquals(pdf3.value(i), b3.pdf(x.value(i)), TOL);
            Assert.assertEquals(pdf4.value(i), b4.pdf(x.value(i)), TOL);
        }
    }

    @Test
    public void testBinomialCdf() {

        Binomial b1 = new Binomial(0.1, 10);
        Binomial b2 = new Binomial(0.1, 100);
        Binomial b3 = new Binomial(0.9, 100);
        Binomial b4 = new Binomial(0.9, 2000);

        Var x = df.var("x");

        Var cdf1 = df.var("cdf_10_0.1");
        Var cdf2 = df.var("cdf_100_0.1");
        Var cdf3 = df.var("cdf_100_0.9");
        Var cdf4 = df.var("cdf_2000_0.9");

        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(cdf1.value(i), b1.cdf(x.value(i)), TOL);
            Assert.assertEquals(cdf2.value(i), b2.cdf(x.value(i)), TOL);
            Assert.assertEquals(cdf3.value(i), b3.cdf(x.value(i)), TOL);
            Assert.assertEquals(cdf4.value(i), b4.cdf(x.value(i)), TOL);
        }
    }

    @Test
    public void testBinomialQuantile() {

        Binomial b1 = new Binomial(0.1, 10);
        Binomial b2 = new Binomial(0.1, 100);
        Binomial b3 = new Binomial(0.9, 100);
        Binomial b4 = new Binomial(0.9, 2000);

        Var x = df.var("x");

        Var q1 = df.var("q_10_0.1");
        Var q2 = df.var("q_100_0.1");
        Var q3 = df.var("q_100_0.9");
        Var q4 = df.var("q_2000_0.9");

        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(q1.value(i), b1.quantile(x.value(i)), TOL);
            Assert.assertEquals(q2.value(i), b2.quantile(x.value(i)), TOL);
            Assert.assertEquals(q3.value(i), b3.quantile(x.value(i)), TOL);
            Assert.assertEquals(q4.value(i), b4.quantile(x.value(i)), TOL);
        }
    }
}
