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

package rapaio.core.stat;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.data.*;
import rapaio.io.Csv;
import rapaio.sys.WS;

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
        mean(df.getVar(0)).printSummary();
        variance(df.getVar(0)).printSummary();
        assertEquals(Double.valueOf("999.98132402093892779"), mean(df.getVar(0)).getValue(), 1e-12);
        assertEquals(Double.valueOf("1.0012615815492349469"), Math.sqrt(Variance.from(df.getVar(0)).getValue()), 1e-12);
        Assert.assertEquals(996.343866540788, Minimum.from(df.getVar(0)).getValue(), 1e-12);
        Assert.assertEquals(1004.24956126934, Maximum.from(df.getVar(0)).getValue(), 1e-12);
    }

    @Test
    public void testEmptyMean() {
        NumericVar num1 = NumericVar.copy(Double.NaN, Double.NaN, Double.NaN);
        double mean = mean(num1).getValue();
        assertTrue(Double.isNaN(mean));

        NumericVar num2 = NumericVar.wrap(1, 2, 3, 4);
        StringBuilder sb = new StringBuilder();
        sb.append(mean(num2).getSummary());

        assertEquals("\n> mean[?]\n" +
                "total rows: 4 (complete: 4, missing: 0)\n" +
                "mean: 2.5\n", sb.toString());
        sb = new StringBuilder();
        sb.append(variance(num2).getSummary());
        assertEquals("\n> variance[?]\n" +
                        "total rows: 4 (complete: 4, missing: 0)\n" +
                        "variance: 1.6666667\n" +
                        "sd: 1.2909944\n",
                sb.toString());

        mean(num2).printSummary();
        variance(num2).printSummary();
    }

    @Test
    public void testQuantiles() {
        NumericVar v = NumericVar.seq(0, 1, 0.001);
        Quantiles q1 = quantiles(v, NumericVar.seq(0, 1, 0.001));
        assertTrue(v.deepEquals(NumericVar.wrap(q1.getValues())));


        NumericVar vEmpty = NumericVar.empty(10);
        NumericVar vOne = vEmpty.solidCopy();
        vOne.setValue(3, 10);

        Quantiles q2 = quantiles(vEmpty, NumericVar.seq(0, 1, 0.1));
        Assert.assertEquals(11, q2.getValues().length);
        for (int i = 0; i < q2.getValues().length; i++) {
            Assert.assertTrue(Double.isNaN(q2.getValues()[i]));
        }

        Quantiles q3 = quantiles(vOne, NumericVar.seq(0, 1, 0.1));
        Assert.assertEquals(11, q3.getValues().length);
        for (int i = 0; i < q3.getValues().length; i++) {
            Assert.assertEquals(10, q3.getValues()[i], 1e-20);
        }

        Quantiles q4 = quantiles(v, Quantiles.Type.R8, NumericVar.seq(0, 1, 0.1));

        Arrays.stream(q4.getValues()).forEach(val -> WS.println(WS.formatLong(val)));
        NumericVar v4 = NumericVar.copy(0,
                0.09946666666666674,
                0.19960000000000017,
                0.2997333333333336,
                0.399866666666667,
                0.5000000000000003,
                0.6001333333333337,
                0.7002666666666671,
                0.8004000000000006,
                0.900533333333334,
                1.0000000000000007);
        q4.printSummary();
        assertTrue(v4.deepEquals(NumericVar.wrap(q4.getValues())));
    }

    @Test
    public void testMode() {
        assertEquals("[a, b]", Arrays.deepToString(modes(NominalVar.copy("a", "a", "b", "a", "b", "c", "b")).getValues()));
        assertEquals("[a]", Arrays.deepToString(modes(NominalVar.copy("a")).getValues()));
        assertEquals("[a]", Arrays.deepToString(modes(NominalVar.copy("a", "a", "a", "b", "c", "b")).getValues()));
        assertEquals("[a, c, b]", Arrays.deepToString(modes(NominalVar.copy("a", "c", "b")).getValues()));
        assertEquals("[]", Arrays.deepToString(modes(NominalVar.copy()).getValues()));
    }

    @Test
    public void testCovariance() {
        NumericVar v1 = NumericVar.seq(0, 200, 0.1);
        NumericVar v2 = NumericVar.wrap(1, 201, 0.1);
        assertEquals(cov(v1, v1).getValue(), variance(v1).getValue(), 1e-12);

        NumericVar x = NumericVar.copy(1, 2, 3, 4);
        assertEquals(cov(x, x).getValue(), variance(x).getValue(), 1e-12);

        NumericVar norm = distNormal().sample(20_000);
        assertEquals(cov(norm, norm).getValue(), variance(norm).getValue(), 1e-12);

        Var x1 = NumericVar.seq(0, 200, 1);
        Var x2 = NumericVar.seq(0, 50, 0.25);
        assertEquals(845.875, cov(x1, x2).getValue(), 1e-12);
    }

    @Test
    public void testGeometricMean() {
        assertEquals(4, GeometricMean.from(NumericVar.copy(2, 8)).getValue(), 1e-20);
        assertEquals(0.5, GeometricMean.from(NumericVar.copy(4, 1, 1 / 32.)).getValue(), 1e-16);
        assertEquals(42.42640687119286, GeometricMean.from(NumericVar.copy(6, 50, 9, 1200)).getValue(), 1e-20);
        GeometricMean.from(NumericVar.copy(6, 50, 9, 1200)).printSummary();

        Assert.assertFalse(Double.NaN == GeometricMean.from(NumericVar.copy(1, -1)).getValue());
        GeometricMean.from(NumericVar.wrap(1, -1)).printSummary();
    }

    @Test
    public void testToolsOnNonNumeric() {
        Var idx1 = IndexVar.wrap(1, 2, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4, 5, 6, Integer.MIN_VALUE, 7);
        Var idx2 = IndexVar.wrap(1, 2, 3, 4, 5, 6, 7);

        Assert.assertEquals(4, CoreTools.mean(idx1).getValue(), 1e-20);
        Assert.assertEquals(CoreTools.variance(idx2).getValue(), CoreTools.variance(idx1).getValue(), 1e-20);

        Assert.assertEquals(7, CoreTools.variance(idx1).completeCount());
        Assert.assertEquals(3, CoreTools.variance(idx1).missingCount());


        Var bin1 = BinaryVar.copy(1, 0, 1, -1, 1, -1, 0);
        Var bin2 = BinaryVar.copy(true, false, true, true, false);
        Assert.assertEquals(0.6, CoreTools.mean(bin1).getValue(), 1e-20);
        Assert.assertEquals(CoreTools.variance(bin2).getValue(), CoreTools.variance(bin1).getValue(), 1e-20);
    }
}
