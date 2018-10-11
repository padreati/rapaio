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
import rapaio.data.VType;
import rapaio.io.Csv;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OtherNormalTest {

    private static final double TOL = 1e-12;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Frame df;

    @Before
    public void setUp() throws IOException {
        df = new Csv()
                .withHeader(true)
                .withSeparatorChar(',')
                .withDefaultTypes(VType.DOUBLE)
                .withNAValues("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "other_normal.csv");
    }

    @Test
    public void testStandardQuantile() {
        Normal d = Normal.of(10, 2);
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.getDouble(i, "x") > 0 && df.getDouble(i, "x") < 1) {
                assertEquals(df.getDouble(i, "quantile"), d.quantile(df.getDouble(i, "x")), TOL);
            }
        }
    }

    @Test
    public void testStandardPdf() {
        Normal d = Normal.of(10, 2);
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "pdf"), d.pdf(df.getDouble(i, "x")), TOL);
        }
    }

    @Test
    public void testStandardCdf() {
        Normal d = Normal.of(10, 2);
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "cdf"), d.cdf(df.getDouble(i, "x")), TOL);
        }
    }

    @Test
    public void testOtherAspects() {
        Normal normal = Normal.std();
        assertEquals(Double.NEGATIVE_INFINITY, normal.min(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, normal.max(), TOL);
        assertEquals(0, normal.mean(), TOL);
        assertEquals(0, normal.mode(), TOL);
        assertEquals(1, normal.var(), TOL);
        assertEquals(0, normal.skewness(), TOL);
        assertEquals(0, normal.kurtosis(), TOL);
        assertEquals(2.8378770664093453, normal.entropy(), TOL);

        assertEquals(Double.NaN, Normal.std().cdf(Double.NaN), TOL);
        assertEquals(0, Normal.std().cdf(Double.NEGATIVE_INFINITY), TOL);
        assertEquals(0, Normal.std().cdf(Double.POSITIVE_INFINITY), TOL);

        assertEquals(Double.NEGATIVE_INFINITY, normal.quantile(0), TOL);
        assertEquals(Double.POSITIVE_INFINITY, normal.quantile(1), TOL);

        assertEquals(1, normal.sd(), TOL);
        assertEquals(0, normal.median(), TOL);
    }

    @Test
    public void testInvalidQuantileSmallInput() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Inverse of a probability requires a probablity in the range [0,1], not -1.0");
        Normal.std().quantile(-1);
    }

    @Test
    public void testInvalidQuantileBigInput() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Inverse of a probability requires a probablity in the range [0,1], not 2.0");
        Normal.std().quantile(2);
    }

    @Test
    public void testName() {
        assertEquals("Normal(mu=10, sd=20)", Normal.of(10, 20).name());
        assertEquals("Normal(mu=10.3, sd=20.3)", Normal.of(10.3, 20.3).name());
    }

    @Test
    public void testIsDiscrete() {
        assertFalse(Normal.std().discrete());
        assertFalse(Normal.of(0, 23).discrete());
    }
}