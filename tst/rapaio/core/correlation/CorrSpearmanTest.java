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

package rapaio.core.correlation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.*;
import rapaio.core.distributions.*;
import rapaio.data.*;
import rapaio.experiment.ml.clustering.*;
import rapaio.math.linear.*;
import rapaio.math.linear.dense.*;
import rapaio.sys.*;

import static org.junit.Assert.assertEquals;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrSpearmanTest {

    private static final double TOL = 1e-20;

    private final Var iq = VarDouble.copy(106, 86, 100, 101, 99, 103, 97, 113, 112, 110);
    private final Var tvHours = VarDouble.copy(7, 0, 27, 50, 28, 29, 20, 12, 6, 17);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();


    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testInvalidSingleVariable() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Correlation can be computed only between two variables.");
        CorrSpearman.of(VarDouble.seq(10));
    }

    @Test
    public void testUnequalRowCount() {
        Var x = VarDouble.from(100, Normal.std()::sampleNext);
        Var y = VarDouble.from(10, Normal.std()::sampleNext);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Variables does not have the same size.");
        CorrSpearman.of(x,y);
    }

    @Test
    public void testFromWikipedia() {
        CorrSpearman sc = CorrSpearman.of(iq, tvHours);
        // according with wikipedia article rho must be -0.175757575
        assertEquals(-0.175757575, sc.matrix().get(0, 1), 1e-8);
    }

    @Test
    public void testSameVector() {
        CorrSpearman same = CorrSpearman.of(iq, iq);
        assertEquals(1., same.matrix().get(0, 1), 1e-10);

        same = CorrSpearman.of(tvHours, tvHours);
        assertEquals(1., same.matrix().get(0, 1), 1e-10);
    }

    @Test
    public void maxCorrTest() {
        VarDouble x = VarDouble.from(1_000, Math::sqrt).withName("x");
        CorrSpearman cp = CorrSpearman.of(x, x);
        assertEquals(1, cp.singleValue(), 1e-12);

        VarDouble y = x.stream().mapToDouble().map(v -> -v).boxed().collect(VarDouble.collector()).withName("y");
        cp = CorrSpearman.of(SolidFrame.byVars(x, y));
        assertEquals(-1, cp.singleValue(), 1e-12);
    }

    @Test
    public void randomTest() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> norm.sampleNext()).withName("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(0.023296211476962116, cp.singleValue(), TOL);
    }

    @Test
    public void testNonLinearCorr() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(0.8789432182134321, cp.singleValue(), TOL);
    }

    @Test
    public void testMultipleVarsNonLinear() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");
        VarDouble z = VarDouble.from(10_000, row -> Math.pow(row, 2) + norm.sampleNext()).withName("z");

        RM exp = SolidRM.copy(3, 3,
                1, 0.8789432182134321, 0.8789431613694316,
                0.8789432182134321, 1, 0.999999997876,
                0.8789431613694316, 0.999999997876, 1);

        CorrSpearman cp = CorrSpearman.of(x, y, z);
        DistanceMatrix m = cp.matrix();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals("wrong values for [i,j]=[" + i + "," + j + "]",
                        exp.get(i, j), m.get(i, j), TOL);
            }
        }
    }

    @Test
    public void testMissingValues() {
        VarDouble x = VarDouble.copy(1, 2, Double.NaN, Double.NaN, 5, 6, 7).withName("x");
        VarDouble y = VarDouble.copy(1, 2, 3, Double.NaN, Double.NaN, 6, 7).withName("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(1, cp.singleValue(), TOL);
    }

    @Test
    public void testCollisions() {
        VarDouble x = VarDouble.wrap(1, 2, 3, 3, 3, 3, 4, 5, 6);
        VarDouble y = VarDouble.from(x, value -> value * value);

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(1.0, cp.singleValue(), TOL);
    }

    @Test
    public void testSummary() {

        Var x1 = VarDouble.from(100, Normal.std()::sampleNext);
        Var x2 = VarDouble.from(100, Normal.std()::sampleNext);
        Var x3 = VarDouble.seq(99);

        assertEquals("> spearman[?, ?] - Spearman's rank correlation coefficient\n" +
                "0.2875008\n", CorrSpearman.of(x1, x2).fullContent());

        assertEquals("> spearman[[?, ?, ?]] - Spearman's rank correlation coefficient\n" +
                "       1.?       2.?       3.?    \n" +
                "1.? 1         0.2875008 0.0649745 \n" +
                "2.? 0.2875008 1         0.2294869 \n" +
                "3.? 0.0649745 0.2294869 1         \n", CorrSpearman.of(x1, x2, x3).summary());
    }

    @Test
    public void testManyVars() {
        int K = 10;
        Var[] vars = new Var[K];
        for (int i = 0; i < K; i++) {
            vars[i] = VarDouble.from(100, Normal.std()::sampleNext).withName("Var_" + (i + 1));
        }

        WS.getPrinter().withTextWidth(100);

        CorrSpearman cs = CorrSpearman.of(vars);
        assertEquals("spearman[Var_1, Var_2, Var_3, Var_4, Var_5, Var_6, Var_7, Var_8, Var_9, Var_10] " +
                "= [[1,0.2875008,-0.1588839,0.0274587,-0.0455086,-0.0454365,-0.0772997," +
                "-0.0059766,0.110303,-0.0820642],[0.2875008,...],...]", cs.toString());
        assertEquals("> spearman[[Var_1, Var_2, Var_3, Var_4, Var_5, Var_6, Var_7, Var_8, Var_9, Var_10]] - Spearman's rank correlation coefficient\n" +
                "           1.Var_1    2.Var_2   \n" +
                "1.Var_1    1          0.2875008 \n" +
                "2.Var_2    0.2875008  1         \n" +
                "3.Var_3   -0.1588839  0.0247105 \n" +
                "4.Var_4    0.0274587  0.0608101 \n" +
                "5.Var_5   -0.0455086 -0.0108731 \n" +
                "6.Var_6   -0.0454365  0.0363396 \n" +
                "7.Var_7   -0.0772997 -0.0076088 \n" +
                "8.Var_8   -0.0059766 -0.0181578 \n" +
                "9.Var_9    0.110303   0.1333933 \n" +
                "10.Var_10 -0.0820642  0.0953735 \n" +
                "\n" +
                "           3.Var_3    4.Var_4   \n" +
                "1.Var_1   -0.1588839  0.0274587 \n" +
                "2.Var_2    0.0247105  0.0608101 \n" +
                "3.Var_3    1          0.0814881 \n" +
                "4.Var_4    0.0814881  1         \n" +
                "5.Var_5   -0.0459646  0.1019262 \n" +
                "6.Var_6   -0.0460366 -0.0236664 \n" +
                "7.Var_7   -0.1936994  0.009997  \n" +
                "8.Var_8   -0.0515692  0.050405  \n" +
                "9.Var_9    0.0290189  0.1585119 \n" +
                "10.Var_10 -0.0436964 -0.0872727 \n" +
                "\n" +
                "           5.Var_5    6.Var_6   \n" +
                "1.Var_1   -0.0455086 -0.0454365 \n" +
                "2.Var_2   -0.0108731  0.0363396 \n" +
                "3.Var_3   -0.0459646 -0.0460366 \n" +
                "4.Var_4    0.1019262 -0.0236664 \n" +
                "5.Var_5    1          0.0151215 \n" +
                "6.Var_6    0.0151215  1         \n" +
                "7.Var_7   -0.0793399  0.0345515 \n" +
                "8.Var_8    0.1506391  0.0709031 \n" +
                "9.Var_9    0.0516172  0.1235044 \n" +
                "10.Var_10 -0.2266307 -0.0289229 \n" +
                "\n" +
                "           7.Var_7    8.Var_8   \n" +
                "1.Var_1   -0.0772997 -0.0059766 \n" +
                "2.Var_2   -0.0076088 -0.0181578 \n" +
                "3.Var_3   -0.1936994 -0.0515692 \n" +
                "4.Var_4    0.009997   0.050405  \n" +
                "5.Var_5   -0.0793399  0.1506391 \n" +
                "6.Var_6    0.0345515  0.0709031 \n" +
                "7.Var_7    1          0.1090069 \n" +
                "8.Var_8    0.1090069  1         \n" +
                "9.Var_9    0.0387279  0.03988   \n" +
                "10.Var_10  0.0641104 -0.0858206 \n" +
                "\n" +
                "           9.Var_9   10.Var_10  \n" +
                "1.Var_1    0.110303  -0.0820642 \n" +
                "2.Var_2    0.1333933  0.0953735 \n" +
                "3.Var_3    0.0290189 -0.0436964 \n" +
                "4.Var_4    0.1585119 -0.0872727 \n" +
                "5.Var_5    0.0516172 -0.2266307 \n" +
                "6.Var_6    0.1235044 -0.0289229 \n" +
                "7.Var_7    0.0387279  0.0641104 \n" +
                "8.Var_8    0.03988   -0.0858206 \n" +
                "9.Var_9    1         -0.129961  \n" +
                "10.Var_10 -0.129961   1         \n" +
                "\n", cs.content());
    }
}
