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
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.Var;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static rapaio.core.CoreTools.*;
import static rapaio.data.filter.Filters.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/21/15.
 */
public class FiltersTest {

    @Test
    public void testJitterStandard() {
        RandomSource.setSeed(1);
        Var a = jitter(Numeric.newFill(100_000, 1));
        Mean mean = mean(a);
        Variance var = var(a);
        mean.printSummary();
        var.printSummary();

        assertTrue(mean.value() > 0.9);
        assertTrue(mean.value() < 1.1);
        assertTrue(var.sdValue() > 0.095);
        assertTrue(var.sdValue() < 1.005);
    }

    @Test
    public void testJitterStandardSd() {
        RandomSource.setSeed(1);
        Var a = jitter(Numeric.newFill(100_000, 1), 2);
        Mean mean = mean(a);
        Variance var = var(a);
        mean.printSummary();
        var.printSummary();

        assertTrue(mean.value() > 0.9);
        assertTrue(mean.value() < 1.1);
        assertTrue(var.sdValue() > 1.995);
        assertTrue(var.sdValue() < 2.005);
    }

    @Test
    public void testJitterDistributed() {
        RandomSource.setSeed(1);
        Var a = jitter(Numeric.newFill(100_000, 1), new ChiSquare(5));
        Mean mean = mean(a);
        Variance var = var(a);
        mean.printSummary();
        var.printSummary();

        assertTrue(mean.value() > 5.0);
        assertTrue(mean.value() < 7.0);
        assertTrue(var.sdValue() > 3.1);
        assertTrue(var.sdValue() < 3.2);
    }

    @Test
    public void testSortNominal() {
        RandomSource.setSeed(1);
        Var x1 = Nominal.newCopy("z", "q", "a", "b", "d", "c");
        Var x2 = sort(x1);
        for (int i = 0; i < x2.rowCount() - 1; i++) {
            assertTrue(x2.label(i).compareTo(x2.label(i + 1)) <= 0);
        }
        Var x3 = sort(x1, false);
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(x3.label(i).compareTo(x3.label(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        RandomSource.setSeed(1);
        Var x1 = Numeric.newCopy(7, 5, 1, 2, 5, 4);
        Var x2 = sort(x1);
        for (int i = 0; i < x2.rowCount() - 1; i++) {
            assertTrue(Double.compare(x2.value(i), x2.value(i + 1)) <= 0);
        }
        Var x3 = sort(x1, false);
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(Double.compare(x3.value(i), x3.value(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortRef() {
        RandomSource.setSeed(1);
        Var x1 = Nominal.newCopy("z", "q", "a", "b", "d", "c");
        Var x2 = Numeric.newCopy(7, 6, 1, 2, 5, 4);
        Var x3 = refSort(x2, x1);
        Var x4 = refSort(x1, x2);
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(Double.compare(x3.value(i), x3.value(i + 1)) <= 0);
        }
        for (int i = 0; i < x4.rowCount() - 1; i++) {
            assertTrue(x4.label(i).compareTo(x4.label(i + 1)) <= 0);
        }
    }

    @Test
    public void testShuffle() {
        RandomSource.setSeed(1);
        double N = 1000.0;
        Var x = Numeric.newSeq(0, N, 1);
        Var first = Numeric.newEmpty();
        for (int i = 0; i < 100; i++) {
            Var y = shuffle(x);
            double t = y.stream().mapToDouble().sum();
            assertEquals(N * (N + 1) / 2, t, 1e-30);
            first.addValue(y.value(0));
        }
    }

    @Test
    public void powerTransform() {
        RandomSource.setSeed(1);

        Var x = distNormal().sample(1000).stream().mapToDouble(s -> Math.pow(s.value(), 2)).boxed().collect(Numeric.collector());
        Var y = transformPower(x.solidCopy(), 0.2);

        var(x).printSummary();
        assertEquals(1.459663, var(x).sdValue(), 1e-6);
        var(y).printSummary();
        assertEquals(0.5788231, var(y).sdValue(), 1e-6);

        corrPearson(x, y).printSummary();
        assertEquals(0.8001133350403581, corrPearson(x, y).values()[0][1], 1e-6);
        corrSpearman(x, y).printSummary();
        assertEquals(1, corrSpearman(x, y).values()[0][1], 1e-6);
    }
}
