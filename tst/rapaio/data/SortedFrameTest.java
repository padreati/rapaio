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

package rapaio.data;

import it.unimi.dsi.fastutil.ints.IntComparator;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.filter.frame.FFRefSort;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static rapaio.data.RowComparators.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortedFrameTest {

    private Frame df;

    @Before
    public void init() throws IOException, URISyntaxException {
        df = new Csv()
                .withQuotes(false)
                .withTypes(VType.DOUBLE, "z")
                .withTypes(VType.INT, "y")
                .read(SortedFrameTest.class, "sorted-frame.csv");
    }


    @Test
    public void testMultipleStressSortedLayers() {
        RandomSource.setSeed(1);
        Var[] vars = new Var[1_000];
        for (int i = 0; i < 1_000; i++) {
            vars[i] = VarDouble.fill(1_000).withName("v" + i);
            for (int j = 0; j < 1_000; j++) {
                vars[i].setDouble(j, RandomSource.nextDouble());
            }
        }
        Frame sorted = SolidFrame.byVars(1_000, vars);

        for (int i = 0; i < 100; i++) {
            int col = RandomSource.nextInt(sorted.varCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            sorted = new FFRefSort(doubleComparator(sorted.rvar(col), asc)).fapply(sorted);
        }

        sorted = new FFRefSort(doubleComparator(sorted.rvar(0), true)).fapply(sorted);
        for (int i = 1; i < sorted.rowCount(); i++) {
            assertTrue(sorted.getDouble(i - 1, 0) <= sorted.getDouble(i, 0));
        }

    }

    @Test
    public void smokeTest() {
        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        Frame sort = new FFRefSort(labelComparator(df.rvar(0), true)).fapply(df);
        assertEquals(3, sort.varCount());
        assertEquals(4, sort.rowCount());

        boolean exceptional = false;
        try {
            sort.rvar("wrong-getCol-name");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testSortNominal() {
        Frame sort = new FFRefSort(labelComparator(df.rvar(0), true)).fapply(df);
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) <= 0);
        }

        sort = new FFRefSort(labelComparator(df.rvar(0), false)).fapply(df);
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        for (int col = 1; col <= 2; col++) {
            Frame sort = new FFRefSort(doubleComparator(df.rvar(col), true)).fapply(df);
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.getDouble(i - 1, col) <= sort.getDouble(i, col));
            }

            sort = new FFRefSort(doubleComparator(df.rvar(col), false)).fapply(df);
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.getDouble(i - 1, col) >= sort.getDouble(i, col));
            }
        }
    }

    @Test
    public void testCols() {
        Frame sorted = new FFRefSort(labelComparator(df.rvar(0), true)).fapply(df);

        assertEquals(df.varCount(), sorted.varCount());
        for (int i = 0; i < df.varCount(); i++) {
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
        }
        assertEquals(df.varNames().length, sorted.varNames().length);
        for (int i = 0; i < df.varNames().length; i++) {
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
            assertEquals(df.varIndex(df.varNames()[i]), sorted.varIndex(sorted.varNames()[i]));
            assertEquals(df.varNames()[i], sorted.varNames()[i]);
            assertEquals(df.rvar(df.varNames()[i]).type().isNominal(), sorted.rvar(sorted.varNames()[i]).type().isNominal());
        }
    }

    @Test
    public void testMultipleSortedLayers() {
        Frame sorted = df;

        for (int i = 0; i < 10_000; i++) {
            int col = RandomSource.nextInt(sorted.varCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            IntComparator comp = sorted.rvar(col).type().isNominal() ?
                    labelComparator(sorted.rvar(0), asc) :
                    doubleComparator(sorted.rvar(0), asc);
            sorted = new FFRefSort(comp).fapply(sorted);
        }

        sorted = new FFRefSort(labelComparator(sorted.rvar("x"), true)).fapply(sorted);

        for (int i = 0; i < sorted.rowCount() - 1; i++) {
            assertTrue(sorted.getLabel(i, "x").compareTo(sorted.getLabel(i + 1, "x")) <= 0);
        }
    }
}
