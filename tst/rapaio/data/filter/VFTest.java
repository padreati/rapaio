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
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.Assert.*;
import static rapaio.core.CoreTools.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/21/15.
 */
public class VFTest {

    @Test
    public void testJitterStandard() {
        RandomSource.setSeed(1);
        Var a = VarDouble.fill(100_000, 1).fapply(VF.jitter());
        Mean mean = mean(a);
        Variance var = variance(a);
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
        Var a = VarDouble.fill(100_000, 1).fapply(VF.jitter(2));
        Mean mean = mean(a);
        Variance var = variance(a);
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
        Var a = VarDouble.fill(100_000, 1).fapply(VF.jitter(new ChiSquare(5)));
        Mean mean = mean(a);
        Variance var = variance(a);
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
        Var x1 = VarNominal.copy("z", "q", "a", "b", "d", "c");
        Var x2 = x1.fapply(VF.sort());
        for (int i = 0; i < x2.rowCount() - 1; i++) {
            assertTrue(x2.getLabel(i).compareTo(x2.getLabel(i + 1)) <= 0);
        }
        Var x3 = x1.fapply(VF.sort(false));
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(x3.getLabel(i).compareTo(x3.getLabel(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        RandomSource.setSeed(1);
        Var x1 = VarDouble.copy(7, 5, 1, 2, 5, 4);
        Var x2 = x1.fapply(VF.sort());
        for (int i = 0; i < x2.rowCount() - 1; i++) {
            assertTrue(Double.compare(x2.getDouble(i), x2.getDouble(i + 1)) <= 0);
        }
        Var x3 = x1.fapply(VF.sort(false));
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(Double.compare(x3.getDouble(i), x3.getDouble(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortRef() {
        RandomSource.setSeed(1);
        Var x1 = VarNominal.copy("z", "q", "a", "b", "d", "c");
        Var x2 = VarDouble.copy(7, 6, 1, 2, 5, 4);
        Var x3 = x2.solidCopy().fapply(VF.refSort(x1));
        Var x4 = x1.solidCopy().fapply(VF.refSort(x2));
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(Double.compare(x3.getDouble(i), x3.getDouble(i + 1)) <= 0);
        }
        for (int i = 0; i < x4.rowCount() - 1; i++) {
            assertTrue(x4.getLabel(i).compareTo(x4.getLabel(i + 1)) <= 0);
        }
    }

    @Test
    public void testShuffle() {
        RandomSource.setSeed(1);
        double N = 1000.0;
        Var x = VarDouble.seq(0, N, 1);
        Var first = VarDouble.empty();
        for (int i = 0; i < 100; i++) {
            Var y = x.fapply(VF.shuffle());
            double t = y.stream().mapToDouble().sum();
            assertEquals(N * (N + 1) / 2, t, 1e-30);
            first.addDouble(y.getDouble(0));
        }
    }

    @Test
    public void powerTransform() {
        RandomSource.setSeed(1);

        Var x = distNormal().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.solidCopy().fapply(VF.transformPower(0.2));

        variance(x).printSummary();
        assertEquals(1.459663, variance(x).sdValue(), 1e-6);
        variance(y).printSummary();
        assertEquals(0.5788231, variance(y).sdValue(), 1e-6);

        corrPearson(x, y).printSummary();
        assertEquals(0.8001133350403581, corrPearson(x, y).matrix().get(0,1), 1e-6);
        corrSpearman(x, y).printSummary();
        assertEquals(1, corrSpearman(x, y).matrix().get(0,1), 1e-6);
    }
}
