/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.core.stat;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.*;
import rapaio.io.Csv;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.core.CoreTools.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CoreToolsTest {

    private final Frame df;

    public CoreToolsTest() throws IOException {
        this.df = new Csv().withHeader(true).withDefaultTypes(VarType.NUMERIC).read(getClass(), "core_stat.csv");
    }

    @Test
    public void testRReference() throws IOException {
        mean(df.var(0)).printSummary();
        var(df.var(0)).printSummary();
        assertEquals(Double.valueOf("999.98132402093892779"), mean(df.var(0)).value(), 1e-12);
        assertEquals(Double.valueOf("1.0012615815492349469"), Math.sqrt(new Variance(df.var(0)).value()), 1e-12);
        Assert.assertEquals(996.343866540788, new Minimum(df.var(0)).value(), 1e-12);
        Assert.assertEquals(1004.24956126934, new Maximum(df.var(0)).value(), 1e-12);
    }

    @Test
    public void testEmptyMean() {
        Numeric num1 = Numeric.copy(Double.NaN, Double.NaN, Double.NaN);
        double mean = mean(num1).value();
        assertTrue(Double.isNaN(mean));

        Numeric num2 = Numeric.wrap(1, 2, 3, 4);
        StringBuilder sb = new StringBuilder();
        sb.append(mean(num2).summary());

        assertEquals("\n> mean[?]\n" +
                "total rows: 4 (complete: 4, missing: 0)\n" +
                "mean: 2.5\n", sb.toString());
        sb = new StringBuilder();
        sb.append(var(num2).summary());
        assertEquals("\n> variance[?]\n" +
                        "total rows: 4 (complete: 4, missing: 0)\n" +
                        "variance: 1.6666667\n" +
                        "sd: 1.2909944\n",
                sb.toString());

        mean(num2).printSummary();
        var(num2).printSummary();
    }

    @Test
    public void testQuantiles() {
        Numeric v = Numeric.seq(0, 1, 0.001);
        Quantiles quantiles = quantiles(v, Numeric.seq(0, 1, 0.001));
        assertTrue(v.deepEquals(Numeric.wrap(quantiles.values())));
    }

    @Test
    public void testMode() {
        assertEquals("[a, b]", Arrays.deepToString(modes(Nominal.copy("a", "a", "b", "a", "b", "c", "b")).values()));
        assertEquals("[a]", Arrays.deepToString(modes(Nominal.copy("a")).values()));
        assertEquals("[a]", Arrays.deepToString(modes(Nominal.copy("a", "a", "a", "b", "c", "b")).values()));
        assertEquals("[a, c, b]", Arrays.deepToString(modes(Nominal.copy("a", "c", "b")).values()));
        assertEquals("[]", Arrays.deepToString(modes(Nominal.copy()).values()));
    }

    @Test
    public void testCovariance() {
        Numeric v1 = Numeric.seq(0, 200, 0.1);
        Numeric v2 = Numeric.wrap(1, 201, 0.1);
        assertEquals(cov(v1, v1).value(), var(v1).value(), 1e-12);

        Numeric x = Numeric.copy(1, 2, 3, 4);
        assertEquals(cov(x, x).value(), var(x).value(), 1e-12);

        Numeric norm = distNormal().sample(20_000);
        assertEquals(cov(norm, norm).value(), var(norm).value(), 1e-12);

        Var x1 = Numeric.seq(0, 200, 1);
        Var x2 = Numeric.seq(0, 50, 0.25);
        assertEquals(845.875, cov(x1, x2).value(), 1e-12);
    }

    @Test
    public void testGeometricMean() {
        assertEquals(4, new GeometricMean(Numeric.copy(2, 8)).value(), 1e-20);
        assertEquals(0.5, new GeometricMean(Numeric.copy(4, 1, 1 / 32.)).value(), 1e-16);
        assertEquals(42.42640687119286, new GeometricMean(Numeric.copy(6, 50, 9, 1200)).value(), 1e-20);
        new GeometricMean(Numeric.copy(6, 50, 9, 1200)).printSummary();

        Assert.assertFalse(Double.NaN == new GeometricMean(Numeric.copy(1, -1)).value());
        new GeometricMean(Numeric.wrap(1, -1)).printSummary();
    }
}
