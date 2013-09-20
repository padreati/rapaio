/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.filters.BaseFilters;
import rapaio.io.CsvPersistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;

import static rapaio.filters.RowFilters.*;
import static rapaio.data.RowComparators.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortedFrameTest {

    private Frame df;

    @Before
    public void init() throws IOException {
        final String csv = new String("" +
                "c, 1, 1.\n" +
                "b, 3, 4.\n" +
                "a, 2, 2.5\n" +
                "d, 2, 4");
        CsvPersistence persistence = new CsvPersistence();
        persistence.setHasHeader(false);
        persistence.setHasQuotas(false);
        df = persistence.read("df", new ByteArrayInputStream(csv.getBytes()));

        Vector[] vectors = new Vector[3];
        vectors[0] = BaseFilters.renameVector(df.getCol(0), "x");
        vectors[1] = BaseFilters.toIndex("y", df.getCol(1));
        vectors[2] = BaseFilters.toNumeric("z", df.getCol(2));

        df = new SolidFrame("df", df.getRowCount(), vectors);
    }

    @Test
    public void smokeTest() {
        assertEquals(3, df.getColCount());
        assertEquals(4, df.getRowCount());

        Frame sort = sort(df, nominalComparator(df.getCol(0), true));
        assertEquals(3, sort.getColCount());
        assertEquals(4, sort.getRowCount());

        assertEquals(2, sort.getRowId(0, 0));
        assertEquals(1, sort.getRowId(1, 0));
        assertEquals(0, sort.getRowId(2, 0));
        assertEquals(3, sort.getRowId(3, 0));

        boolean exceptional = false;
        try {
            sort.getCol("wrong-col-name");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testSortNominal() {
        Frame sort = sort(df, nominalComparator(df.getCol(0), true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) <= 0);
        }

        sort = sort(df, nominalComparator(df.getCol(0), false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            String label1 = sort.getLabel(i - 1, 0);
            String label2 = sort.getLabel(i, 0);
            assertTrue(label1.compareTo(label2) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        for (int col = 1; col <= 2; col++) {
            Frame sort = sort(df, numericComparator(df.getCol(col), true));
            for (int i = 1; i < sort.getRowCount(); i++) {
                assertTrue(sort.getValue(i - 1, col) <= sort.getValue(i, col));
            }

            sort = sort(df, numericComparator(df.getCol(col), false));
            for (int i = 1; i < sort.getRowCount(); i++) {
                assertTrue(sort.getValue(i - 1, col) >= sort.getValue(i, col));
            }
        }
    }

    @Test
    public void testCols() {
        Frame sorted = sort(df, nominalComparator(df.getCol(0), true));

        assertEquals(df.getColCount(), sorted.getColCount());
        for (int i = 0; i < df.getColCount(); i++) {
            assertEquals(df.getCol(i).getName(), sorted.getCol(i).getName());
        }
        assertEquals(df.getColNames().length, sorted.getColNames().length);
        for (int i = 0; i < df.getColNames().length; i++) {
            assertEquals(df.getColNames()[i], sorted.getColNames()[i]);
            assertEquals(df.getColIndex(df.getColNames()[i]), sorted.getColIndex(sorted.getColNames()[i]));
            assertEquals(df.getCol(i).getName(), sorted.getCol(i).getName());
            assertEquals(df.getCol(df.getColNames()[i]).isNominal(), sorted.getCol(sorted.getColNames()[i]).isNominal());
        }
    }

    @Test
    public void testMultipleSortedLayers() {
        Frame sorted = df;

        for (int i = 0; i < 10_000; i++) {
            int col = RandomSource.nextInt(sorted.getColCount());
            boolean asc = RandomSource.nextDouble() >= .5;
            Comparator<Integer> comp = sorted.getCol(col).isNominal() ?
                    nominalComparator(df.getCol(0), asc) :
                    numericComparator(df.getCol(0), asc);
            sorted = sort(sorted, comp);
        }

        sorted = sort(sorted, nominalComparator(sorted.getCol("x"), true));

        assertEquals(2, sorted.getRowId(0, 0));
        assertEquals(1, sorted.getRowId(1, 0));
        assertEquals(0, sorted.getRowId(2, 0));
        assertEquals(3, sorted.getRowId(3, 0));

    }
}
