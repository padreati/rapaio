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
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.NumVar;
import rapaio.data.SolidFrame;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;

/**
 * Tests for pearson correlation
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/29/16.
 */
public class CorrPearsonTest {

    @Test
    public void maxCorrTest() {
        NumVar x = NumVar.from(1_000, Math::sqrt);
        CorrPearson cp = CoreTools.corrPearson(x, x);
        cp.printSummary();
        Assert.assertEquals(1, cp.singleValue(), 1e-20);

        x = NumVar.from(1_000, Math::sqrt).withName("x");
        cp = CoreTools.corrPearson(x, x);
        cp.printSummary();
        Assert.assertEquals(1, cp.singleValue(), 1e-20);

        cp = CoreTools.corrPearson(x);
        cp.printSummary();
        Assert.assertEquals(1, cp.singleValue(), 1e-20);

        NumVar y = x.stream().mapToDouble().map(v -> -v).boxed().collect(NumVar.collector()).withName("y");
        cp = CoreTools.corrPearson(x, y);
        cp.printSummary();
        Assert.assertEquals(-1, cp.singleValue(), 1e-20);
    }

    @Test
    public void randomTest() {
        RandomSource.setSeed(123);
        Normal norm = new Normal(0, 12);
        NumVar x = NumVar.from(10_000, row -> norm.sampleNext()).withName("x");
        NumVar y = NumVar.from(10_000, row -> norm.sampleNext()).withName("y");

        CorrPearson cp = CoreTools.corrPearson(x, y);
        cp.printSummary();
        Assert.assertEquals(0.021769705986371495, cp.singleValue(), 1e-20);
    }

    @Test
    public void testNonLinearCorr() {
        RandomSource.setSeed(123);
        Normal norm = new Normal(0, 12);
        NumVar x = NumVar.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        NumVar y = NumVar.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");

        CorrPearson cp = CoreTools.corrPearson(x, y);
        cp.printSummary();
        Assert.assertEquals(0.8356446312071465, cp.singleValue(), 1e-20);
    }

    @Test
    public void testMultipleVarsNonLinear() {

        RandomSource.setSeed(123);
        Normal norm = new Normal(0, 12);
        NumVar x = NumVar.from(10_000, row -> Math.sqrt(row) + norm.sampleNext()).withName("x");
        NumVar y = NumVar.from(10_000, row -> Math.pow(row, 1.5) + norm.sampleNext()).withName("y");
        NumVar z = NumVar.from(10_000, row -> Math.pow(row, 2) + norm.sampleNext()).withName("z");


        RM exp = SolidRM.copy(3, 3,
                1, 0.8356446312071465, 0.7997143292750094,
                0.8356446312071465, 1, 0.9938073109055177,
                0.7997143292750094, 0.9938073109055177, 1);

        CorrPearson cp = CoreTools.corrPearson(x, y, z);
        cp.printSummary();

        DistanceMatrix m = cp.matrix();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertEquals("wrong values for [i,j]=[" + i + "," + j + "]",
                        exp.get(i, j), m.get(i,j), 1e-20);
            }
        }

        cp = CoreTools.corrPearson(SolidFrame.byVars(x, y, x));
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
        NumVar x = NumVar.copy(1, 2, Double.NaN, Double.NaN, 5, 6, 7);
        NumVar y = NumVar.copy(1, 2, 3, Double.NaN, Double.NaN, 6, 7);

        CorrPearson cp = CoreTools.corrPearson(x, y);
        cp.printSummary();

        Assert.assertEquals(1, cp.singleValue(), 1e-20);
    }
}
