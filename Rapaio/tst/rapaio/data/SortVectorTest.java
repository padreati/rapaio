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
import org.junit.Test;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortVectorTest {

    @Test
    public void testSortIndex() {
        Vector index = new IndexVector("x", 10, 1, -1);
        index.setMissing(2);
        index.setMissing(5);
        index.setIndex(0, 1);

        assertEquals(10, index.getRowCount());
        Vector sort = new SortedVector(index, index.getComparator(true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getIndex(i - 1) <= sort.getIndex(i));
        }

        sort = new SortedVector(index, index.getComparator(false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getIndex(i - 1) >= sort.getIndex(i));
        }
    }

    @Test
    public void testSortNumeric() {
        Vector numeric = new NumericVector("x", new double[]{2., 4., 1.2, 1.3, 1.2, 0., 100.});

        assertEquals(7, numeric.getRowCount());
        Vector sort = new SortedVector(numeric, numeric.getComparator(true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getValue(i - 1) <= sort.getValue(i));
        }

        sort = new SortedVector(numeric, numeric.getComparator(false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getValue(i - 1) >= sort.getValue(i));
        }

    }

    @Test
    public void testSortNominal() {
        String[] dict = new String[]{"a", "Aa", "b", "c", "Cc"};
        Vector nominal = new NominalVector("c", 10, dict);

        for (int i = 0; i < 10; i++) {
            nominal.setLabel(i, dict[i % dict.length]);
        }
        nominal.setMissing(2);
        nominal.setMissing(3);
        nominal.setMissing(4);
        nominal.setMissing(5);

        Vector sort = new SortedVector(nominal, nominal.getComparator(true));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) <= 0);
        }
        sort = new SortedVector(nominal, nominal.getComparator(false));
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) >= 0);
        }
    }
}
