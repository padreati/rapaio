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
import rapaio.io.Csv;

import java.io.IOException;

public class PoissonTest {

    private static final double TOL = 1e-13;

    private Frame df;
    private Poisson pois1;
    private Poisson pois5;
    private Poisson pois10;
    private Poisson pois100;

    @Before
    public void setUp() throws Exception {
        df = new Csv()
                .withNAValues("NaN","Inf")
                .read(rapaio.core.distributions.HypergeometricTest.class, "pois.csv");

        pois1 = new Poisson(1);
        pois5 = new Poisson(5);
        pois10 = new Poisson(10);
        pois100 = new Poisson(100);
    }

    @Test
    public void rPdfTest() throws IOException {
        for (int i = 0; i < df.getRowCount(); i++) {
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "pdf_1"), pois1.pdf(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "pdf_5"), pois5.pdf(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "pdf_10"), pois10.pdf(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "pdf_100"), pois100.pdf(df.getValue(i, "x")), TOL);
        }
    }

    @Test
    public void rCdfTest() throws IOException {
        for (int i = 0; i < df.getRowCount(); i++) {
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "cdf_1"), pois1.cdf(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "cdf_5"), pois5.cdf(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "cdf_10"), pois10.cdf(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "cdf_100"), pois100.cdf(df.getValue(i, "x")), TOL);
        }
    }

    @Test
    public void rQuantileTest() throws IOException {
        for (int i = 0; i < df.getRowCount(); i++) {
            if (df.getValue(i, "x") >= 1)
                break;
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "q_1"), pois1.quantile(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "q_5"), pois5.quantile(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "q_10"), pois10.quantile(df.getValue(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.getValue(i, "x")), df.getValue(i, "q_100"), pois100.quantile(df.getValue(i, "x")), TOL);
        }
    }
}
