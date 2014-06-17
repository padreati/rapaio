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
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.data.RowComparators.nominalComparator;
import static rapaio.data.RowComparators.numericComparator;
import static rapaio.data.filters.BaseFilters.sort;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortedFrameTest {

    private Frame df;

    @Before
    public void init() throws IOException, URISyntaxException {
        df = new Csv()
                .withQuotas(false)
                .withNumericFields("z")
                .withIndexFields("y")
                .read(SortedFrameTest.class, "sorted-frame.csv");
    }


    @Test
    public void testMultipleStressSortedLayers() {
        RandomSource.setSeed(1);
        Var[] vars = new Var[1_000];
        String[] names = new String[1_000];
        for (int i = 0; i < 1_000; i++) {
            vars[i] = Numeric.newFill(10_000);
            for (int j = 0; j < 10_000; j++) {
                vars[i].setValue(j, RandomSource.nextDouble());
            }
            names[i] = "v" + i;
        }
        Frame sorted = new SolidFrame(10_000, vars, names, null);

        for (int i = 0; i < 100; i++) {
            int col = RandomSource.nextInt(sorted.colCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            sorted = sort(sorted, numericComparator(sorted.col(col), asc));
        }

        sorted = sort(sorted, numericComparator(sorted.col(0), true));
        for (int i = 1; i < sorted.rowCount(); i++) {
            assertTrue(sorted.value(i - 1, 0) <= sorted.value(i, 0));
        }

    }

    @Test
    public void smokeTest() {
        assertEquals(3, df.colCount());
        assertEquals(4, df.rowCount());

        Frame sort = sort(df, nominalComparator(df.col(0), true));
        assertEquals(3, sort.colCount());
        assertEquals(4, sort.rowCount());

        assertEquals(2, sort.rowId(0));
        assertEquals(1, sort.rowId(1));
        assertEquals(0, sort.rowId(2));
        assertEquals(3, sort.rowId(3));

        boolean exceptional = false;
        try {
            sort.col("wrong-getCol-name");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testSortNominal() {
        Frame sort = sort(df, nominalComparator(df.col(0), true));
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.label(i - 1, 0);
            String label2 = sort.label(i, 0);
            assertTrue(label1.compareTo(label2) <= 0);
        }

        sort = sort(df, nominalComparator(df.col(0), false));
        for (int i = 1; i < sort.rowCount(); i++) {
            String label1 = sort.label(i - 1, 0);
            String label2 = sort.label(i, 0);
            assertTrue(label1.compareTo(label2) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        for (int col = 1; col <= 2; col++) {
            Frame sort = sort(df, numericComparator(df.col(col), true));
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.value(i - 1, col) <= sort.value(i, col));
            }

            sort = sort(df, numericComparator(df.col(col), false));
            for (int i = 1; i < sort.rowCount(); i++) {
                assertTrue(sort.value(i - 1, col) >= sort.value(i, col));
            }
        }
    }

    @Test
    public void testCols() {
        Frame sorted = sort(df, nominalComparator(df.col(0), true));

        assertEquals(df.colCount(), sorted.colCount());
        for (int i = 0; i < df.colCount(); i++) {
            assertEquals(df.colNames()[i], sorted.colNames()[i]);
        }
        assertEquals(df.colNames().length, sorted.colNames().length);
        for (int i = 0; i < df.colNames().length; i++) {
            assertEquals(df.colNames()[i], sorted.colNames()[i]);
            assertEquals(df.colIndex(df.colNames()[i]), sorted.colIndex(sorted.colNames()[i]));
            assertEquals(df.colNames()[i], sorted.colNames()[i]);
            assertEquals(df.col(df.colNames()[i]).type().isNominal(), sorted.col(sorted.colNames()[i]).type().isNominal());
        }
    }

    @Test
    public void testMultipleSortedLayers() {
        Frame sorted = df;

        for (int i = 0; i < 10_000; i++) {
            int col = RandomSource.nextInt(sorted.colCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            Comparator<Integer> comp = sorted.col(col).type().isNominal() ?
                    nominalComparator(sorted.col(0), asc) :
                    numericComparator(sorted.col(0), asc);
            sorted = sort(sorted, comp);
        }

        sorted = sort(sorted, nominalComparator(sorted.col("x"), true));

        assertEquals(2, sorted.rowId(0));
        assertEquals(1, sorted.rowId(1));
        assertEquals(0, sorted.rowId(2));
        assertEquals(3, sorted.rowId(3));

    }
}
