/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.filter.frame.FFRefSort;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.data.RowComparators.nominal;
import static rapaio.data.RowComparators.numeric;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortedFrameTest {

    private Frame df;

    @Before
    public void init() throws IOException, URISyntaxException {
        df = new Csv()
                .withQuotas(false)
                .withTypes(VarType.NUMERIC, "z")
                .withTypes(VarType.INDEX, "y")
                .read(SortedFrameTest.class, "sorted-frame.csv");
    }


    @Test
    public void testMultipleStressSortedLayers() {
        RandomSource.setSeed(1);
        Var[] vars = new Var[1_000];
        for (int i = 0; i < 1_000; i++) {
            vars[i] = Numeric.newFill(1_000).withName("v" + i);
            for (int j = 0; j < 1_000; j++) {
                vars[i].setValue(j, RandomSource.nextDouble());
            }
        }
        Frame sorted = SolidFrame.newWrapOf(1_000, vars);

        for (int i = 0; i < 100; i++) {
            int col = RandomSource.nextInt(sorted.varCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            sorted = new FFRefSort(numeric(sorted.var(col), asc)).fitApply(sorted);
        }

        sorted = new FFRefSort(numeric(sorted.var(0), true)).fitApply(sorted);
        for (int i = 1; i < sorted.rowCount(); i++) {
            assertTrue(sorted.value(i - 1, 0) <= sorted.value(i, 0));
        }

    }

    @Test
    public void smokeTest() {
        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        Frame sort = new FFRefSort(nominal(df.var(0), true)).fitApply(df);
        assertEquals(3, sort.varCount());
        assertEquals(4, sort.rowCount());

        boolean exceptional = false;
        try {
            sort.var("wrong-getCol-name");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testSortNominal() {
        Frame sort = new FFRefSort(nominal(df.var(0), true)).fitApply(df);
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.label(i - 1, 0);
            String label2 = sort.label(i, 0);
            assertTrue(label1.compareTo(label2) <= 0);
        }

        sort = new FFRefSort(nominal(df.var(0), false)).fitApply(df);
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.label(i - 1, 0);
            String label2 = sort.label(i, 0);
            assertTrue(label1.compareTo(label2) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        for (int col = 1; col <= 2; col++) {
            Frame sort = new FFRefSort(numeric(df.var(col), true)).fitApply(df);
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.value(i - 1, col) <= sort.value(i, col));
            }

            sort = new FFRefSort(numeric(df.var(col), false)).fitApply(df);
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.value(i - 1, col) >= sort.value(i, col));
            }
        }
    }

    @Test
    public void testCols() {
        Frame sorted = new FFRefSort(nominal(df.var(0), true)).fitApply(df);

        assertEquals(df.varCount(), sorted.varCount());
        for (int i = 0; i < df.varCount(); i++) {
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
        }
        assertEquals(df.varNames().length, sorted.varNames().length);
        for (int i = 0; i < df.varNames().length; i++) {
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
            assertEquals(df.varIndex(df.varNames()[i]), sorted.varIndex(sorted.varNames()[i]));
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
            assertEquals(df.var(df.varNames()[i]).type().isNominal(), sorted.var(sorted.varNames()[i]).type().isNominal());
        }
    }

    @Test
    public void testMultipleSortedLayers() {
        Frame sorted = df;

        for (int i = 0; i < 10_000; i++) {
            int col = RandomSource.nextInt(sorted.varCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            Comparator<Integer> comp = sorted.var(col).type().isNominal() ?
                    nominal(sorted.var(0), asc) :
                    numeric(sorted.var(0), asc);
            sorted = new FFRefSort(comp).fitApply(sorted);
        }

        sorted = new FFRefSort(nominal(sorted.var("x"), true)).fitApply(sorted);

        for (int i = 0; i < sorted.rowCount() - 1; i++) {
            assertTrue(sorted.label(i, "x").compareTo(sorted.label(i + 1, "x")) <= 0);
        }
    }
}
