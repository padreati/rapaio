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

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;

import static org.junit.Assert.assertEquals;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CorrSpearmanTest {

    private final Var iq = VarDouble.copy(106, 86, 100, 101, 99, 103, 97, 113, 112, 110);
    private final Var tvHours = VarDouble.copy(7, 0, 27, 50, 28, 29, 20, 12, 6, 17);

    @Test
    public void testFromWikipedia() {
        CorrSpearman sc = CorrSpearman.of(iq, tvHours);
        // according with wikipedia article rho must be -0.175757575
        assertEquals(-0.175757575, sc.matrix().get(0,1), 1e-8);
    }

    @Test
    public void testSameVector() {
        CorrSpearman same = CorrSpearman.of(iq, iq);
        assertEquals(1., same.matrix().get(0,1), 1e-10);

        same = CorrSpearman.of(tvHours, tvHours);
        assertEquals(1., same.matrix().get(0,1), 1e-10);
    }

    @Test
    public void maxCorrTest() {
        VarDouble x = VarDouble.from(1_000, Math::sqrt).withName("x");
        CorrSpearman cp = CorrSpearman.of(x, x);
        cp.printSummary();
        Assert.assertEquals(1, cp.singleValue(), 1e-12);

        cp = CorrSpearman.of(x);
        cp.printSummary();
        Assert.assertEquals(1, cp.singleValue(), 1e-20);

        VarDouble y = x.stream().mapToDouble().map(v -> -v).boxed().collect(VarDouble.collector()).withName("y");
        cp = CorrSpearman.of(x, y);
        cp.printSummary();
        Assert.assertEquals(-1, cp.singleValue(), 1e-12);
    }

    @Test
    public void randomTest() {
        RandomSource.setSeed(123);
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> norm.sampleNext()).withName("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        cp.printSummary();
        Assert.assertEquals(0.023296211476962116, cp.singleValue(), 1e-20);
    }

    @Test
    public void testNonLinearCorr() {
        RandomSource.setSeed(123);
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        cp.printSummary();
        Assert.assertEquals(0.8789432182134321, cp.singleValue(), 1e-20);
    }

    @Test
    public void testMultipleVarsNonLinear() {

        RandomSource.setSeed(123);
        Normal norm = Normal.of(0, 12);
        VarDouble x = VarDouble.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");
        VarDouble z = VarDouble.from(10_000, row -> Math.pow(row, 2) + norm.sampleNext()).withName("z");


        RM exp = SolidRM.copy(3, 3,
                1, 0.8789432182134321, 0.8789431613694316,
                0.8789432182134321, 1, 0.999999997876,
                0.8789431613694316, 0.999999997876, 1);

        CorrSpearman cp = CorrSpearman.of(x, y, z);
        cp.printSummary();

        DistanceMatrix m = cp.matrix();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertEquals("wrong values for [i,j]=[" + i + "," + j + "]",
                        exp.get(i, j), m.get(i,j), 1e-20);
            }
        }

        cp = CorrSpearman.of(SolidFrame.byVars(x, y, x));
        cp.printSummary();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertEquals("wrong values for [i,j]=[" + i + "," + j + "]",
                        exp.get(i, j), m.get(i,j), 1e-20);
            }
        }
    }

    @Test
    public void testMissingValues() {
        VarDouble x = VarDouble.copy(1, 2, Double.NaN, Double.NaN, 5, 6, 7).withName("x");
        VarDouble y = VarDouble.copy(1, 2, 3, Double.NaN, Double.NaN, 6, 7).withName("y");

        CorrSpearman cp = CorrSpearman.of(x, y);
        cp.printSummary();

        Assert.assertEquals(1, cp.singleValue(), 1e-20);
    }


    @Test
    public void testCollisions() {
        VarDouble x = VarDouble.wrap(1, 2, 3, 3, 3, 3, 4, 5, 6);
        VarDouble y = VarDouble.from(x, value -> value*value);

        CorrSpearman cp = CorrSpearman.of(x, y);
        cp.printSummary();

        Assert.assertEquals(1.0, cp.singleValue(), 1e-20);
    }
}
