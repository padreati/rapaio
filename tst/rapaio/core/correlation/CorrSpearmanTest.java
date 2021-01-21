/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.clustering.DistanceMatrix;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixStripe;
import rapaio.sys.WS;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.printer.Printer.textWidth;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrSpearmanTest {

    private static final double TOL = 1e-20;

    private final Var iq = VarDouble.copy(106, 86, 100, 101, 99, 103, 97, 113, 112, 110);
    private final Var tvHours = VarDouble.copy(7, 0, 27, 50, 28, 29, 20, 12, 6, 17);

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testInvalidSingleVariable() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CorrSpearman.of(VarDouble.seq(10)));
        assertEquals("Correlation can be computed only between two variables.", ex.getMessage());
    }

    @Test
    void testUnequalRowCount() {
        Var x = VarDouble.from(100, Normal.std()::sampleNext);
        Var y = VarDouble.from(10, Normal.std()::sampleNext);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> CorrSpearman.of(x, y));
        assertEquals("Variables does not have the same size.", ex.getMessage());
    }

    @Test
    void testFromWikipedia() {
        CorrSpearman sc = CorrSpearman.of(iq, tvHours);
        // according with wikipedia article rho must be -0.175757575
        assertEquals(-0.175757575, sc.matrix().get(0, 1), 1e-8);
    }

    @Test
    void testSameVector() {
        CorrSpearman same = CorrSpearman.of(iq, iq);
        assertEquals(1., same.matrix().get(0, 1), 1e-10);

        same = CorrSpearman.of(tvHours, tvHours);
        assertEquals(1., same.matrix().get(0, 1), 1e-10);
    }

    @Test
    void maxCorrTest() {
        VarDouble x = VarDouble.from(1_000, Math::sqrt).name("x");
        CorrSpearman cp = CorrSpearman.of(x, x);
        assertEquals(1, cp.singleValue(), 1e-12);

        VarDouble y = x.stream().mapToDouble().map(v -> -v).boxed().collect(VarDouble.collector()).name("y");
        cp = CorrSpearman.of(SolidFrame.byVars(x, y));
        assertEquals(-1, cp.singleValue(), 1e-12);
    }

    @Test
    void randomTest() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> norm.sampleNext()).name("x");
        VarDouble y = VarDouble.from(10_000, row -> norm.sampleNext()).name("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(0.023296211476962116, cp.singleValue(), TOL);
    }

    @Test
    void testNonLinearCorr() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).name("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).name("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(0.8789432182134321, cp.singleValue(), TOL);
    }

    @Test
    void testMultipleVarsNonLinear() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).name("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).name("y");
        VarDouble z = VarDouble.from(10_000, row -> Math.pow(row, 2) + norm.sampleNext()).name("z");

        DMatrix exp = DMatrixStripe.copy(3, 3,
                1, 0.8789432182134321, 0.8789431613694316,
                0.8789432182134321, 1, 0.999999997876,
                0.8789431613694316, 0.999999997876, 1);

        CorrSpearman cp = CorrSpearman.of(x, y, z);
        DistanceMatrix m = cp.matrix();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(exp.get(i, j), m.get(i, j), TOL);
            }
        }
    }

    @Test
    void testMissingValues() {
        VarDouble x = VarDouble.copy(1, 2, Double.NaN, Double.NaN, 5, 6, 7).name("x");
        VarDouble y = VarDouble.copy(1, 2, 3, Double.NaN, Double.NaN, 6, 7).name("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(1, cp.singleValue(), TOL);
    }

    @Test
    void testCollisions() {
        VarDouble x = VarDouble.wrap(1, 2, 3, 3, 3, 3, 4, 5, 6);
        VarDouble y = VarDouble.from(x, value -> value * value);

        CorrSpearman cp = CorrSpearman.of(x, y);
        assertEquals(1.0, cp.singleValue(), TOL);
    }

    @Test
    void testSummary() {

        Var x1 = VarDouble.from(100, Normal.std()::sampleNext);
        Var x2 = VarDouble.from(100, Normal.std()::sampleNext);
        Var x3 = VarDouble.seq(99);

        assertEquals("> spearman[?, ?] - Spearman's rank correlation coefficient\n" +
                "0.2875008\n", CorrSpearman.of(x1, x2).toFullContent());

        assertEquals("> spearman[[?, ?, ?]] - Spearman's rank correlation coefficient\n" +
                "       1.?       2.?       3.?    \n" +
                "1.? 1         0.2875008 0.0649745 \n" +
                "2.? 0.2875008 1         0.2294869 \n" +
                "3.? 0.0649745 0.2294869 1         \n", CorrSpearman.of(x1, x2, x3).toSummary());
    }

    @Test
    void testManyVars() {
        int K = 10;
        Var[] vars = new Var[K];
        for (int i = 0; i < K; i++) {
            vars[i] = VarDouble.from(100, Normal.std()::sampleNext).name("Var_" + (i + 1));
        }

        WS.getPrinter().withOptions(textWidth(100));

        CorrSpearman cs = CorrSpearman.of(vars);
        assertEquals("spearman[Var_1, Var_2, Var_3, Var_4, Var_5, Var_6, Var_7, Var_8, Var_9, Var_10] " +
                "= [[1,0.2875008,-0.1588839,0.0274587,-0.0455086,-0.0454365,-0.0772997," +
                "-0.0059766,0.110303,-0.0820642],[0.2875008,...],...]", cs.toString());
        assertEquals("> spearman[[Var_1, Var_2, Var_3, Var_4, Var_5, Var_6, Var_7, Var_8, Var_9, Var_10]] - Spearman's rank correlation coefficient\n" +
                "           1.Var_1    2.Var_2    3.Var_3    4.Var_4    5.Var_5    6.Var_6    7.Var_7    8.Var_8    9.Var_9   \n" +
                "1.Var_1    1          0.2875008 -0.1588839  0.0274587 -0.0455086 -0.0454365 -0.0772997 -0.0059766  0.110303  \n" +
                "2.Var_2    0.2875008  1          0.0247105  0.0608101 -0.0108731  0.0363396 -0.0076088 -0.0181578  0.1333933 \n" +
                "3.Var_3   -0.1588839  0.0247105  1          0.0814881 -0.0459646 -0.0460366 -0.1936994 -0.0515692  0.0290189 \n" +
                "4.Var_4    0.0274587  0.0608101  0.0814881  1          0.1019262 -0.0236664  0.009997   0.050405   0.1585119 \n" +
                "5.Var_5   -0.0455086 -0.0108731 -0.0459646  0.1019262  1          0.0151215 -0.0793399  0.1506391  0.0516172 \n" +
                "6.Var_6   -0.0454365  0.0363396 -0.0460366 -0.0236664  0.0151215  1          0.0345515  0.0709031  0.1235044 \n" +
                "7.Var_7   -0.0772997 -0.0076088 -0.1936994  0.009997  -0.0793399  0.0345515  1          0.1090069  0.0387279 \n" +
                "8.Var_8   -0.0059766 -0.0181578 -0.0515692  0.050405   0.1506391  0.0709031  0.1090069  1          0.03988   \n" +
                "9.Var_9    0.110303   0.1333933  0.0290189  0.1585119  0.0516172  0.1235044  0.0387279  0.03988    1         \n" +
                "10.Var_10 -0.0820642  0.0953735 -0.0436964 -0.0872727 -0.2266307 -0.0289229  0.0641104 -0.0858206 -0.129961  \n" +
                "\n" +
                "          10.Var_10  \n" +
                "1.Var_1   -0.0820642 \n" +
                "2.Var_2    0.0953735 \n" +
                "3.Var_3   -0.0436964 \n" +
                "4.Var_4   -0.0872727 \n" +
                "5.Var_5   -0.2266307 \n" +
                "6.Var_6   -0.0289229 \n" +
                "7.Var_7    0.0641104 \n" +
                "8.Var_8   -0.0858206 \n" +
                "9.Var_9   -0.129961  \n" +
                "10.Var_10  1         \n" +
                "\n", cs.toContent());
    }
}
