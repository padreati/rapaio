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

public class HypergeometricDistributionTest {

    private static final double TOL = 1e-15;

    private Frame df;
    private Hypergeometric hg1;
    private Hypergeometric hg2;

    @Before
    public void setUp() throws Exception {
        df = new Csv()
                .withNAValues("NaN")
                .read(HypergeometricDistributionTest.class, "hyper.csv");

        hg1 = new Hypergeometric(20, 20, 30);
        hg2 = new Hypergeometric(70, 70, 100);
    }

    @Test
    public void rPdfTest() throws IOException {
        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.value(i, "x")), df.value(i, "pdf_20_20_30"), hg1.pdf(df.value(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.value(i, "x")), df.value(i, "pdf_70_70_100"), hg2.pdf(df.value(i, "x")), TOL);
        }
    }

    @Test
    public void rCdfTest() throws IOException {
        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.value(i, "x")), df.value(i, "cdf_20_20_30"), hg1.cdf(df.value(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.value(i, "x")), df.value(i, "cdf_70_70_100"), hg2.cdf(df.value(i, "x")), TOL);
        }
    }

    @Test
    public void rQuantileTest() throws IOException {
        for (int i = 0; i < df.rowCount(); i++) {
            if(df.value(i, "x")>1)
                break;
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.value(i, "x")), df.value(i, "q_20_20_30"), hg1.quantile(df.value(i, "x")), TOL);
            Assert.assertEquals(String.format("error at i: %d, value: %f", i, df.value(i, "x")), df.value(i, "q_70_70_100"), hg2.quantile(df.value(i, "x")), TOL);
        }
    }
}
