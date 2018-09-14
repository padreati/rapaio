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
import org.junit.Before;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarBoolean;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.io.Csv;
import rapaio.sys.WS;
import rapaio.util.Time;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static rapaio.core.CoreTools.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CoreToolsTest {

    private static final double TOL = 1e-20;
    private Normal normal = new Normal(0, 10);
    private Frame df;

    @Before
    public void setUp() throws IOException {
        df = new Csv().withHeader(true).withDefaultTypes(VarType.DOUBLE).read(getClass(), "core_stat.csv");
        RandomSource.setSeed(123);
    }

    @Test
    public void testRReference() throws IOException {
        mean(df.rvar(0)).printSummary();
        variance(df.rvar(0)).printSummary();
        assertEquals(Double.valueOf("999.98132402093892779"), mean(df.rvar(0)).value(), 1e-12);
        assertEquals(Double.valueOf("1.0012615815492349469"), Math.sqrt(Variance.from(df.rvar(0)).value()), 1e-12);
        Assert.assertEquals(996.343866540788, Minimum.from(df.rvar(0)).value(), 1e-12);
        Assert.assertEquals(1004.24956126934, Maximum.from(df.rvar(0)).value(), 1e-12);
    }

    @Test
    public void testEmptyMean() {
        VarDouble num1 = VarDouble.copy(Double.NaN, Double.NaN, Double.NaN);
        double mean = mean(num1).value();
        assertTrue(Double.isNaN(mean));

        VarDouble num2 = VarDouble.wrap(1, 2, 3, 4);
        StringBuilder sb = new StringBuilder();
        sb.append(mean(num2).summary());

        assertEquals("\n> mean[?]\n" +
                "total rows: 4 (complete: 4, missing: 0)\n" +
                "mean: 2.5\n", sb.toString());
        sb = new StringBuilder();
        sb.append(variance(num2).summary());
        assertEquals("\n> variance[?]\n" +
                        "total rows: 4 (complete: 4, missing: 0)\n" +
                        "variance: 1.6666667\n" +
                        "sd: 1.2909944\n",
                sb.toString());

        mean(num2).printSummary();
        variance(num2).printSummary();
    }

    @Test
    public void testWithMissingValues() {

        VarDouble x1 = VarDouble.wrap(1.0, Double.NaN, 2.0, Double.NaN, 3.0, Double.NaN);
        VarDouble x2 = VarDouble.wrap(1.0, 2.0, 3.0);

        assertFalse(Double.isNaN(Mean.from(x1).value()));
    }

    @Test
    public void testSameMean() {
        RandomSource.setSeed(1234);
        double[] x = new double[10_000];
        Normal normal = new Normal(0, 1);
        for (int i = 0; i < x.length; i++) {
            if(RandomSource.nextDouble()<0.1) {
                x[i] = Double.NaN;
            } else {
                x[i] = normal.sampleNext();
            }
        }
        assertEquals(Mean.from(x).value(), Mean.from(VarDouble.wrap(x)).value(), TOL);
    }

    @Test
    public void testQuantiles() {
        VarDouble v = VarDouble.seq(0, 1, 0.001);
        Quantiles q1 = quantiles(v, VarDouble.seq(0, 1, 0.001));
        assertTrue(v.deepEquals(VarDouble.wrap(q1.values())));


        VarDouble vEmpty = VarDouble.empty(10);
        VarDouble vOne = vEmpty.solidCopy();
        vOne.setDouble(3, 10);

        Quantiles q2 = quantiles(vEmpty, VarDouble.seq(0, 1, 0.1));
        Assert.assertEquals(11, q2.values().length);
        for (int i = 0; i < q2.values().length; i++) {
            Assert.assertTrue(Double.isNaN(q2.values()[i]));
        }

        Quantiles q3 = quantiles(vOne, VarDouble.seq(0, 1, 0.1));
        Assert.assertEquals(11, q3.values().length);
        for (int i = 0; i < q3.values().length; i++) {
            Assert.assertEquals(10, q3.values()[i], 1e-20);
        }

        Quantiles q4 = quantiles(v, Quantiles.Type.R8, VarDouble.seq(0, 1, 0.1));

        Arrays.stream(q4.values()).forEach(val -> WS.println(WS.formatLong(val)));
        VarDouble v4 = VarDouble.copy(0,
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
        assertTrue(v4.deepEquals(VarDouble.wrap(q4.values())));
    }

    @Test
    public void quantilesSpeedTest() {
        RandomSource.setSeed(1234);
        Normal normal = new Normal();

        VarDouble x = VarDouble.from(10_000, normal::sampleNext);

        Var y = x;
        for (int i = 0; i < 100; i++) {
            y = y.bindRows(VarDouble.from(10_000, normal::sampleNext));
        }

        Var yy = y;

        Time.showRun(() -> {
                    double[] q = Quantiles.from(yy, 0.1, 0.25, 0.5, 0.75, 0.9).values();
                    for (int i = 0; i < q.length; i++) {
                        System.out.println("q[" + i + "]=" + q[i] + ", ");
                    }
                }
        );
    }

    @Test
    public void testMode() {
        assertEquals("[a, b]", Arrays.deepToString(modes(VarNominal.copy("a", "a", "b", "a", "b", "c", "b")).values()));
        assertEquals("[a]", Arrays.deepToString(modes(VarNominal.copy("a")).values()));
        assertEquals("[a]", Arrays.deepToString(modes(VarNominal.copy("a", "a", "a", "b", "c", "b")).values()));
        assertEquals("[a, c, b]", Arrays.deepToString(modes(VarNominal.copy("a", "c", "b")).values()));
        assertEquals("[]", Arrays.deepToString(modes(VarNominal.copy()).values()));
    }

    @Test
    public void testCovariance() {
        VarDouble v1 = VarDouble.seq(0, 200, 0.1);
        VarDouble v2 = VarDouble.wrap(1, 201, 0.1);
        assertEquals(cov(v1, v1).value(), variance(v1).value(), 1e-12);

        VarDouble x = VarDouble.copy(1, 2, 3, 4);
        assertEquals(cov(x, x).value(), variance(x).value(), 1e-12);

        VarDouble norm = distNormal().sample(20_000);
        assertEquals(cov(norm, norm).value(), variance(norm).value(), 1e-12);

        Var x1 = VarDouble.seq(0, 200, 1);
        Var x2 = VarDouble.seq(0, 50, 0.25);
        assertEquals(845.875, cov(x1, x2).value(), 1e-12);
    }

    @Test
    public void testCorrelation() {
        Var x = VarDouble.from(100, normal::sampleNext).withName("x");
        Var y = x.solidCopy().withName("y");

        Covariance cov = Covariance.from(x, y);
        Variance var = Variance.from(x);
        assertEquals(var.value(), cov.value(), TOL);

        assertEquals("> cov[x, y]\n" +
                "total rows: 100 (complete: 100, missing: 0 )\n" +
                "covariance: 97.7342133\n", cov.summary());
    }

    @Test
    public void testCovarianceInvalid() {
        Var x = VarDouble.from(1, normal::sampleNext).withName("x");
        Var y = x.solidCopy().withName("y");

        Covariance cov = Covariance.from(x, y);
        cov.printSummary();
    }

    @Test
    public void testGeometricMean() {
        assertEquals(4, GeometricMean.from(VarDouble.copy(2, 8)).value(), 1e-20);
        assertEquals(0.5, GeometricMean.from(VarDouble.copy(4, 1, 1 / 32.)).value(), 1e-16);
        assertEquals(42.42640687119286, GeometricMean.from(VarDouble.copy(6, 50, 9, 1200)).value(), 1e-20);
        GeometricMean.from(VarDouble.copy(6, 50, 9, 1200)).printSummary();

        assertTrue(Double.isNaN(GeometricMean.from(VarDouble.copy(1, -1)).value()));
        GeometricMean.from(VarDouble.wrap(1, -1)).printSummary();
    }

    @Test
    public void testToolsOnNonNumeric() {
        Var idx1 = VarInt.wrap(1, 2, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4, 5, 6, Integer.MIN_VALUE, 7);
        Var idx2 = VarInt.wrap(1, 2, 3, 4, 5, 6, 7);

        Assert.assertEquals(4, CoreTools.mean(idx1).value(), 1e-20);
        Assert.assertEquals(CoreTools.variance(idx2).value(), CoreTools.variance(idx1).value(), 1e-20);

        Assert.assertEquals(7, CoreTools.variance(idx1).completeCount());
        Assert.assertEquals(3, CoreTools.variance(idx1).missingCount());


        Var bin1 = VarBoolean.copy(1, 0, 1, -1, 1, -1, 0);
        Var bin2 = VarBoolean.copy(true, false, true, true, false);
        Assert.assertEquals(0.6, CoreTools.mean(bin1).value(), 1e-20);
        Assert.assertEquals(CoreTools.variance(bin2).value(), CoreTools.variance(bin1).value(), 1e-20);
    }
}
