/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.core.distributions.ChiSquare;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.Var;

import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static rapaio.core.CoreTools.mean;
import static rapaio.core.CoreTools.var;
import static rapaio.data.filter.Filters.*;
import static rapaio.graphics.Plotter.hist;
import static rapaio.sys.WS.draw;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/21/15.
 */
public class FiltersTest {

    @Test
    public void testJitterStandard() {
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
        Var a = jitter(Numeric.newFill(100_000, 1), new ChiSquare(5));
        Mean mean = mean(a);
        Variance var = var(a);
        mean.printSummary();
        var.printSummary();

//        setPrinter(new IdeaPrinter());
//        draw(hist(a, bins(100)));
        assertTrue(mean.value() > 5.0);
        assertTrue(mean.value() < 7.0);
        assertTrue(var.sdValue() > 3.1);
        assertTrue(var.sdValue() < 3.2);
    }

    @Test
    public void testSortNominal() {
        Var x1 = Nominal.newCopyOf("z", "q", "a", "b", "d", "c");
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
        Var x1 = Numeric.newCopyOf(7, 5, 1, 2, 5, 4);
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
        Var x1 = Nominal.newCopyOf("z", "q", "a", "b", "d", "c");
        Var x2 = Numeric.newCopyOf(7, 6, 1, 2, 5, 4);
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
        Var x = IntStream.range(0, 100).mapToDouble(v -> v).boxed().collect(Numeric.collector());
        Var first = Numeric.newEmpty();
        for (int i = 0; i < 100; i++) {
            Var y = shuffle(x);
            double t = y.spotStream().mapToDouble().sum();
            assertEquals(4950.0, t, 1e-30);
            first.addValue(y.value(0));
        }
//        setPrinter(new IdeaPrinter());
//        draw(hist(first, bins(100)));
    }
}
