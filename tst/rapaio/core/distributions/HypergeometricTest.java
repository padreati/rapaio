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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.data.Frame;
import rapaio.io.Csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HypergeometricTest {

    private static final double TOL = 1e-15;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Frame df;
    private Hypergeometric hg1;
    private Hypergeometric hg2;

    @Before
    public void setUp() throws Exception {
        df = Csv.instance().withNAValues("NaN").read(HypergeometricTest.class, "hyper.csv");

        hg1 = Hypergeometric.of(20, 20, 30);
        hg2 = Hypergeometric.of(70, 70, 100);
    }

    @Test
    public void testGeneric() {
        Hypergeometric hg = Hypergeometric.of(10, 10, 6);
        assertEquals("Hypergeometric(m=10,n=10,k=6)", hg.name());
        assertTrue(hg1.discrete());

        assertEquals(0, hg1.cdf(Double.NEGATIVE_INFINITY), TOL);
        assertEquals(1, hg1.cdf(Double.POSITIVE_INFINITY), TOL);
        assertEquals(1, hg1.cdf(32), TOL);

        assertEquals(0, hg1.min(), TOL);
        assertEquals(20, hg1.max(), TOL);
        assertEquals(15, hg1.mean(), TOL);
        assertEquals(15, hg1.mode(), TOL);
        assertEquals(1.9230769230769231, hg1.var(), TOL);
        assertEquals(0.018027756377319945, hg1.skewness(), TOL);
        assertEquals(-0.11891891891891893, hg1.kurtosis(), TOL);
        assertEquals(Double.NaN, hg1.entropy(), TOL); // not implemented
    }

    @Test
    public void testInvalidWhiteBalls() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("m parameter should not be negative.");
        Hypergeometric.of(-1, 10, 10);
    }

    @Test
    public void testInvalidBlackBalls() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("n parameter should not be negative.");
        Hypergeometric.of(10, -1, 10);
    }

    @Test
    public void testInvalidSumTest() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("m + n should be at least 1.");
        Hypergeometric.of(0, 0, 10);
    }

    @Test
    public void testInvalidSampleSize() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Size of sample k should be at most m + n.");
        Hypergeometric.of(10, 10, 30);
    }

    @Test
    public void testInvalidValuePdf() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("x should be an integer since the hypergeometric" +
                " repartition is a discrete repartion.");
        hg1.pdf(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testRPdf() {
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")), df.getDouble(i, "pdf_20_20_30"), hg1.pdf(df.getDouble(i, "x")), TOL);
            assertEquals(String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")), df.getDouble(i, "pdf_70_70_100"), hg2.pdf(df.getDouble(i, "x")), TOL);
        }
    }

    @Test
    public void testRCdf() {
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")), df.getDouble(i, "cdf_20_20_30"), hg1.cdf(df.getDouble(i, "x")), TOL);
            assertEquals(String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")), df.getDouble(i, "cdf_70_70_100"), hg2.cdf(df.getDouble(i, "x")), TOL);
        }
    }

    @Test
    public void testRQuantile() {
        for (int i = 0; i < df.rowCount(); i++) {
            if(df.getDouble(i, "x")>1)
                break;
            assertEquals(String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")), df.getDouble(i, "q_20_20_30"), hg1.quantile(df.getDouble(i, "x")), TOL);
            assertEquals(String.format("error at i: %d, value: %f", i, df.getDouble(i, "x")), df.getDouble(i, "q_70_70_100"), hg2.quantile(df.getDouble(i, "x")), TOL);
        }
    }
}
