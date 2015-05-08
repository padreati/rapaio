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
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.core.stat.Quantiles;
import rapaio.core.stat.Variance;
import rapaio.data.*;
import rapaio.io.Csv;
import rapaio.printer.Printer;
import rapaio.ws.Summary;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.core.CoreStat.*;
import static rapaio.core.distributions.Distributions.normal;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CoreStatTest {

    private final Frame df;

    public CoreStatTest() throws IOException {
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
        Numeric num1 = Numeric.newCopyOf(Double.NaN, Double.NaN, Double.NaN);
        double mean = mean(num1).value();
        assertTrue(Double.isNaN(mean));

        Numeric num2 = Numeric.newWrapOf(1, 2, 3, 4);
        StringBuilder sb = new StringBuilder();
        mean(num2).buildPrintSummary(sb);

        assertEquals("> mean[?]\n" +
                "total rows: 4 (complete: 4, missing: 0)\n" +
                "mean: 2.5\n", sb.toString());
        sb = new StringBuilder();
        var(num2).buildPrintSummary(sb);
        assertEquals("> variance[?]\n" +
                        "total rows: 4 (complete: 4, missing: 0)\n" +
                        "variance: 1.6666667\n" +
                        "sd: 1.2909944\n",
                sb.toString());

        mean(num2).printSummary();
        var(num2).printSummary();
    }

    @Test
    public void testQuantiles() {
        Numeric v = Numeric.newSeq(0, 1, 0.001);
        Quantiles quantiles = quantiles(v, Numeric.newSeq(0, 1, 0.001));
        assertTrue(v.deepEquals(Numeric.newWrapOf(quantiles.values())));
    }

    @Test
    public void testMode() {
        assertEquals("[a, b]", Arrays.deepToString(modes(Nominal.newCopyOf("a", "a", "b", "a", "b", "c", "b")).values()));
        assertEquals("[a]", Arrays.deepToString(modes(Nominal.newCopyOf("a")).values()));
        assertEquals("[a]", Arrays.deepToString(modes(Nominal.newCopyOf("a", "a", "a", "b", "c", "b")).values()));
        assertEquals("[a, c, b]", Arrays.deepToString(modes(Nominal.newCopyOf("a", "c", "b")).values()));
        assertEquals("[]", Arrays.deepToString(modes(Nominal.newCopyOf()).values()));
    }

    @Test
    public void testCovariance() {
        Numeric v1 = Numeric.newSeq(0, 200, 0.1);
        Numeric v2 = Numeric.newWrapOf(1, 201, 0.1);
        assertEquals(cov(v1, v1).value(), var(v1).value(), 1e-12);

        Numeric x = Numeric.newCopyOf(1, 2, 3, 4);
        assertEquals(cov(x, x).value(), var(x).value(), 1e-12);

        Numeric norm = normal().sample(20_000);
        assertEquals(cov(norm, norm).value(), var(norm).value(), 1e-12);

        Var x1 = Numeric.newSeq(0, 200, 1);
        Var x2 = Numeric.newSeq(0, 50, 0.25);
        assertEquals(845.875, cov(x1, x2).value(), 1e-12);
    }
}
