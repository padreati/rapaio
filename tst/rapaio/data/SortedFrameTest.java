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
    RandomSource randomSource = RandomSource.createRandom();

    @Before
    public void init() throws IOException, URISyntaxException {
        df = new Csv()
                .withQuotes(false)
                .withTypes(VarType.NUMERIC, "z")
                .withTypes(VarType.INDEX, "y")
                .read(SortedFrameTest.class, "sorted-frame.csv");
    }


    @Test
    public void testMultipleStressSortedLayers() {
        randomSource.setSeed(1);
        Var[] vars = new Var[1_000];
        for (int i = 0; i < 1_000; i++) {
            vars[i] = NumericVar.fill(1_000).withName("v" + i);
            for (int j = 0; j < 1_000; j++) {
                vars[i].setValue(j, randomSource.nextDouble());
            }
        }
        Frame sorted = SolidFrame.byVars(1_000, vars);

        for (int i = 0; i < 100; i++) {
            int col = randomSource.nextInt(sorted.getVarCount());
            boolean asc = randomSource.nextDouble() >= .5;
            sorted = new FFRefSort(numeric(sorted.getVar(col), asc)).fitApply(sorted);
        }

        sorted = new FFRefSort(numeric(sorted.getVar(0), true)).fitApply(sorted);
        for (int i = 1; i < sorted.getRowCount(); i++) {
            assertTrue(sorted.getValue(i - 1, 0) <= sorted.getValue(i, 0));
        }

    }

    @Test
    public void smokeTest() {
        assertEquals(3, df.getVarCount());
        assertEquals(4, df.getRowCount());

        Frame sort = new FFRefSort(nominal(df.getVar(0), true)).fitApply(df);
        assertEquals(3, sort.getVarCount());
        assertEquals(4, sort.getRowCount());

        boolean exceptional = false;
        try {
            sort.getVar("wrong-getCol-name");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testSortNominal() {
        Frame sort = new FFRefSort(nominal(df.getVar(0), true)).fitApply(df);
        for (int i = 1; i < sort.getRowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) <= 0);
        }

        sort = new FFRefSort(nominal(df.getVar(0), false)).fitApply(df);
        for (int i = 1; i < sort.getRowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        for (int col = 1; col <= 2; col++) {
            Frame sort = new FFRefSort(numeric(df.getVar(col), true)).fitApply(df);
            for (int i = 1; i < sort.getRowCount(); i++) {
                assertTrue(sort.getValue(i - 1, col) <= sort.getValue(i, col));
            }

            sort = new FFRefSort(numeric(df.getVar(col), false)).fitApply(df);
            for (int i = 1; i < sort.getRowCount(); i++) {
                assertTrue(sort.getValue(i - 1, col) >= sort.getValue(i, col));
            }
        }
    }

    @Test
    public void testCols() {
        Frame sorted = new FFRefSort(nominal(df.getVar(0), true)).fitApply(df);

        assertEquals(df.getVarCount(), sorted.getVarCount());
        for (int i = 0; i < df.getVarCount(); i++) {
            assertEquals(df.getVarNames()[i], sorted.getVarNames()[i]);
        }
        assertEquals(df.getVarNames().length, sorted.getVarNames().length);
        for (int i = 0; i < df.getVarNames().length; i++) {
            assertEquals(df.getVarNames()[i], sorted.getVarNames()[i]);
            assertEquals(df.getVarIndex(df.getVarNames()[i]), sorted.getVarIndex(sorted.getVarNames()[i]));
            assertEquals(df.getVarNames()[i], sorted.getVarNames()[i]);
            assertEquals(df.getVar(df.getVarNames()[i]).getType().isNominal(), sorted.getVar(sorted.getVarNames()[i]).getType().isNominal());
        }
    }

    @Test
    public void testMultipleSortedLayers() {
        Frame sorted = df;

        for (int i = 0; i < 10_000; i++) {
            int col = randomSource.nextInt(sorted.getVarCount());
            boolean asc = randomSource.nextDouble() >= .5;
            Comparator<Integer> comp = sorted.getVar(col).getType().isNominal() ?
                    nominal(sorted.getVar(0), asc) :
                    numeric(sorted.getVar(0), asc);
            sorted = new FFRefSort(comp).fitApply(sorted);
        }

        sorted = new FFRefSort(nominal(sorted.getVar("x"), true)).fitApply(sorted);

        for (int i = 0; i < sorted.getRowCount() - 1; i++) {
            assertTrue(sorted.getLabel(i, "x").compareTo(sorted.getLabel(i + 1, "x")) <= 0);
        }
    }
}
