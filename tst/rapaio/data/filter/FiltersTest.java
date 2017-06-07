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

package rapaio.data.filter;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.ChiSquare;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.NominalVar;
import rapaio.data.NumericVar;
import rapaio.data.Var;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static rapaio.core.CoreTools.*;
import static rapaio.data.filter.Filters.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/21/15.
 */
public class FiltersTest {

    RandomSource randomSource = RandomSource.createRandom();
    @Test
    public void testJitterStandard() {
        randomSource.setSeed(1);
        Var a = jitter(NumericVar.fill(100_000, 1));
        Mean mean = mean(a);
        Variance var = variance(a);
        mean.printSummary();
        var.printSummary();

        assertTrue(mean.getValue() > 0.9);
        assertTrue(mean.getValue() < 1.1);
        assertTrue(var.sdValue() > 0.095);
        assertTrue(var.sdValue() < 1.005);
    }

    @Test
    public void testJitterStandardSd() {
        randomSource.setSeed(1);
        Var a = jitter(NumericVar.fill(100_000, 1), 2);
        Mean mean = mean(a);
        Variance var = variance(a);
        mean.printSummary();
        var.printSummary();

        assertTrue(mean.getValue() > 0.9);
        assertTrue(mean.getValue() < 1.1);
        assertTrue(var.sdValue() > 1.995);
        assertTrue(var.sdValue() < 2.005);
    }

    @Test
    public void testJitterDistributed() {
        randomSource.setSeed(1);
        Var a = jitter(NumericVar.fill(100_000, 1), new ChiSquare(5));
        Mean mean = mean(a);
        Variance var = variance(a);
        mean.printSummary();
        var.printSummary();

        assertTrue(mean.getValue() > 5.0);
        assertTrue(mean.getValue() < 7.0);
        assertTrue(var.sdValue() > 3.1);
        assertTrue(var.sdValue() < 3.2);
    }

    @Test
    public void testSortNominal() {
        randomSource.setSeed(1);
        Var x1 = NominalVar.copy("z", "q", "a", "b", "d", "c");
        Var x2 = sort(x1);
        for (int i = 0; i < x2.getRowCount() - 1; i++) {
            assertTrue(x2.getLabel(i).compareTo(x2.getLabel(i + 1)) <= 0);
        }
        Var x3 = sort(x1, false);
        for (int i = 0; i < x3.getRowCount() - 1; i++) {
            assertTrue(x3.getLabel(i).compareTo(x3.getLabel(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        randomSource.setSeed(1);
        Var x1 = NumericVar.copy(7, 5, 1, 2, 5, 4);
        Var x2 = sort(x1);
        for (int i = 0; i < x2.getRowCount() - 1; i++) {
            assertTrue(Double.compare(x2.getValue(i), x2.getValue(i + 1)) <= 0);
        }
        Var x3 = sort(x1, false);
        for (int i = 0; i < x3.getRowCount() - 1; i++) {
            assertTrue(Double.compare(x3.getValue(i), x3.getValue(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortRef() {
        randomSource.setSeed(1);
        Var x1 = NominalVar.copy("z", "q", "a", "b", "d", "c");
        Var x2 = NumericVar.copy(7, 6, 1, 2, 5, 4);
        Var x3 = refSort(x2, x1);
        Var x4 = refSort(x1, x2);
        for (int i = 0; i < x3.getRowCount() - 1; i++) {
            assertTrue(Double.compare(x3.getValue(i), x3.getValue(i + 1)) <= 0);
        }
        for (int i = 0; i < x4.getRowCount() - 1; i++) {
            assertTrue(x4.getLabel(i).compareTo(x4.getLabel(i + 1)) <= 0);
        }
    }

    @Test
    public void testShuffle() {
        randomSource.setSeed(1);
        double N = 1000.0;
        Var x = NumericVar.seq(0, N, 1);
        Var first = NumericVar.empty();
        for (int i = 0; i < 100; i++) {
            Var y = shuffle(x);
            double t = y.stream().mapToDouble().sum();
            assertEquals(N * (N + 1) / 2, t, 1e-30);
            first.addValue(y.getValue(0));
        }
    }

    @Test
    public void powerTransform() {
        randomSource.setSeed(1);

        Var x = distNormal().sample(1000).stream().mapToDouble(s -> Math.pow(s.getValue(), 2)).boxed().collect(NumericVar.collector());
        Var y = transformPower(x.solidCopy(), 0.2);

        variance(x).printSummary();
        assertEquals(1.459663, variance(x).sdValue(), 1e-6);
        variance(y).printSummary();
        assertEquals(0.5788231, variance(y).sdValue(), 1e-6);

        corrPearson(x, y).printSummary();
        assertEquals(0.8001133350403581, corrPearson(x, y).values()[0][1], 1e-6);
        corrSpearman(x, y).printSummary();
        assertEquals(1, corrSpearman(x, y).values()[0][1], 1e-6);
    }
}
