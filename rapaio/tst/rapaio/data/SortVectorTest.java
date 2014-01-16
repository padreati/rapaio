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

import org.junit.Test;
import rapaio.core.BaseMath;
import rapaio.io.CsvPersistence;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static rapaio.data.RowComparators.*;
import static rapaio.filters.RowFilters.sort;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortVectorTest {

    @Test
    public void smokeTest() {
        Vector v = Vectors.newIdx(0);
        Vector sorted = sort(v, indexComparator(v, true));
        assertTrue(sorted.type().isNumeric());
        assertFalse(sorted.type().isNominal());

        v = new Numeric(0);
        sorted = sort(v, numericComparator(v, true));
        assertTrue(sorted.type().isNumeric());
        assertFalse(sorted.type().isNominal());

        v = new Nominal(0, new String[]{});
        sorted = sort(v, nominalComparator(v, true));
        assertFalse(sorted.type().isNumeric());
        assertTrue(sorted.type().isNominal());
    }

    @Test
    public void testSortIndex() {
        Vector index = Vectors.newSeq(10, 1, -1);
        index.setMissing(2);
        index.setMissing(5);
        index.setIndex(0, 1);

        assertEquals(10, index.rowCount());
        Vector sort = sort(index, indexComparator(index, true));
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.index(i - 1) <= sort.index(i));
        }

        sort = sort(index, indexComparator(index, false));
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.index(i - 1) >= sort.index(i));
        }

        Vector second = sort(sort, indexComparator(sort, true));
        for (int i = 1; i < second.rowCount(); i++) {
            assertTrue(second.index(i - 1) <= second.index(i));
        }
    }

    @Test
    public void testSortNumeric() {
        Vector numeric = new Numeric(new double[]{2., 4., 1.2, 1.3, 1.2, 0., 100.});

        assertEquals(7, numeric.rowCount());
        Vector sort = sort(numeric, numericComparator(numeric, true));
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.value(i - 1) <= sort.value(i));
        }

        sort = sort(numeric, numericComparator(numeric, false));
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.value(i - 1) >= sort.value(i));
        }

        Vector second = sort(sort, numericComparator(sort, true));
        for (int i = 1; i < second.rowCount(); i++) {
            assertTrue(second.index(i - 1) <= second.index(i));
        }
    }

    @Test
    public void testSortNominal() {
        String[] dict = new String[]{"a", "Aa", "b", "c", "Cc"};
        Vector nominal = new Nominal(10, dict);

        for (int i = 0; i < 10; i++) {
            nominal.setLabel(i, dict[i % dict.length]);
        }
        nominal.setMissing(2);
        nominal.setMissing(3);
        nominal.setMissing(4);
        nominal.setMissing(5);

        Vector sort = sort(nominal, nominalComparator(nominal, true));
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.label(i - 1).compareTo(sort.label(i)) <= 0);
        }

        sort = sort(nominal, nominalComparator(nominal, false));
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.label(i - 1).compareTo(sort.label(i)) >= 0);
        }

        Vector second = sort(sort, nominalComparator(sort, true));
        for (int i = 1; i < second.rowCount(); i++) {
            assertTrue(second.label(i - 1).compareTo(second.label(i)) <= 0);
        }
    }

    @Test
    public void testGetterSetter() throws IOException, URISyntaxException {

        CsvPersistence persistence = new CsvPersistence();
        persistence.setHasHeader(true);
        persistence.setColSeparator(',');
        persistence.setHasQuotas(false);
        persistence.getNumericFieldHints().add("z");
        persistence.getIndexFieldHints().add("y");
        Frame df = persistence.read(SortVectorTest.class, "sorted-frame.csv");

        Vector nominal = df.col(0);
        Vector index = df.col(1);
        Vector numeric = df.col(2);

        // nominal

        HashMap<String, String> transform = new HashMap<>();
        transform.put("a", "c");
        transform.put("b", "a");
        transform.put("c", "b");
        transform.put("d", "d");
        Vector sort = sort(nominal);
        for (int i = 0; i < sort.rowCount(); i++) {
            sort.setLabel(i, transform.get(sort.label(i)));
        }

        assertEquals("b", nominal.label(0));
        assertEquals("a", nominal.label(1));
        assertEquals("c", nominal.label(2));
        assertEquals("d", nominal.label(3));

        for (int i = 0; i < sort.rowCount(); i++) {
            sort.setIndex(i, 2);
            assertEquals(nominal.dictionary()[2], nominal.label(i));
            assertEquals(2, nominal.index(i));
        }

        assertEquals(nominal.dictionary().length, sort.dictionary().length);
        for (int i = 0; i < nominal.dictionary().length; i++) {
            assertEquals(nominal.dictionary()[i], sort.dictionary()[i]);
        }

        // numeric

        sort = sort(numeric, numericComparator(numeric, true));
        for (int i = 0; i < sort.rowCount(); i++) {
            sort.setValue(i, sort.value(i) + BaseMath.E);
        }
        assertEquals(BaseMath.E + 1., numeric.value(0), 1e-10);
        assertEquals(BaseMath.E + 2.5, numeric.value(2), 1e-10);
        assertEquals(BaseMath.E + 4, numeric.value(1), 1e-10);
        assertEquals(BaseMath.E + 4., numeric.value(3), 1e-10);


        // index

        sort = sort(index, indexComparator(index, true));
        for (int i = 0; i < sort.rowCount(); i++) {
            sort.setValue(i, sort.index(i) + 10);
        }
        assertEquals(11, index.index(0));
        assertEquals(12, index.index(2));
        assertEquals(12, index.index(3));
        assertEquals(13, index.index(1));
    }

    @Test
    public void testMissing() {
        Vector v = Vectors.newSeq(1, 10, 1);
        v = sort(v, indexComparator(v, true));
        for (int i = 0; i < 10; i += 3) {
            v.setMissing(i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i % 3 == 0 ? true : false, v.isMissing(i));
        }
    }
}
