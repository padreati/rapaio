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
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.ml.clustering.DistanceMatrix;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;

import static org.junit.Assert.assertEquals;

/**
 * Tests for pearson correlation
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/29/16.
 */
public class CorrPearsonTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testInvalidSingleVar() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Correlation can be computed only between two variables.");
        CorrPearson.of(VarDouble.seq(10));
    }

    @Test
    public void maxCorrTest() {
        VarDouble x = VarDouble.from(1_000, Math::sqrt);
        CorrPearson cp = CorrPearson.of(x, x);
        assertEquals(1, cp.singleValue(), 1e-20);

        x = VarDouble.from(1_000, Math::sqrt).withName("x");
        cp = CorrPearson.of(x, x);
        assertEquals(1, cp.singleValue(), 1e-20);

        VarDouble y = x.stream().mapToDouble().map(v -> -v).boxed().collect(VarDouble.collector()).withName("y");
        cp = CorrPearson.of(x, y);
        assertEquals(-1, cp.singleValue(), 1e-20);
    }

    @Test
    public void randomTest() {
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> norm.sampleNext()).withName("y");

        CorrPearson cp = CorrPearson.of(x, y);
        assertEquals(0.021769705986371478, cp.singleValue(), TOL);
        assertEquals("pearson[x, y] = [[1,0.0217697],[0.0217697,1]]", cp.toString());
        assertEquals("> pearson[x, y] - Pearson product-moment correlation coefficient\n" +
                "0.0217697\n", cp.content());
    }

    @Test
    public void testNonLinearCorr() {
        RandomSource.setSeed(123);
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");

        CorrPearson cp = CorrPearson.of(SolidFrame.byVars(x, y));
        assertEquals(0.8356446312071465, cp.singleValue(), TOL);
    }

    @Test
    public void testMultipleVarsNonLinear() {

        RandomSource.setSeed(123);
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");
        VarDouble z = VarDouble.from(10_000, row -> Math.pow(row, 2) + norm.sampleNext()).withName("z");


        RM exp = SolidRM.copy(3, 3,
                1, 0.8356446312071465, 0.7997143292750087,
                0.8356446312071465, 1, 0.9938073109055182,
                0.7997143292750087, 0.9938073109055182, 1);

        CorrPearson cp = CorrPearson.of(x, y, z);
        DistanceMatrix m = cp.matrix();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals("wrong values for [i,j]=[" + i + "," + j + "]",
                        exp.get(i, j), m.get(i,j), TOL);
            }
        }

        assertEquals("pearson[x, y, z] = [[1,0.8356446,0.7997143],[0.8356446,1,0.9938073],[0.7997143,0.9938073,1]]", cp.toString());
        assertEquals("> pearson[[x, y, z]] - Pearson product-moment correlation coefficient\n" +
                "       1.x       2.y       3.z    \n" +
                "1.x 1         0.8356446 0.7997143 \n" +
                "2.y 0.8356446 1         0.9938073 \n" +
                "3.z 0.7997143 0.9938073 1         \n", cp.content());
    }

    @Test
    public void testMissingValues() {
        VarDouble x = VarDouble.copy(1, 2, Double.NaN, Double.NaN, 5, 6, 7);
        VarDouble y = VarDouble.copy(1, 2, 3, Double.NaN, Double.NaN, 6, 7);
        assertEquals(1, CorrPearson.of(x, y).singleValue(), 1e-30);
    }

    @Test
    public void testZeroSd() {
        VarDouble x = VarDouble.fill(100, 10);
        assertEquals(Double.NaN, CorrPearson.of(x, x).singleValue(), TOL);

        assertEquals("pearson[?, ?] = [[1,?],[?,1]]", CorrPearson.of(x, x).toString());
        assertEquals("> pearson[?, ?] - Pearson product-moment correlation coefficient\n" +
                "?\n", CorrPearson.of(x, x).summary());

        assertEquals("> pearson[[?, ?, ?]] - Pearson product-moment correlation coefficient\n" +
                "    1.? 2.? 3.? \n" +
                "1.?  1  NaN NaN \n" +
                "2.? NaN  1  NaN \n" +
                "3.? NaN NaN  1  \n", CorrPearson.of(x, x, x).summary());
    }
}
