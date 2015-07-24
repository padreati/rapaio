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

package rapaio.data;

import org.junit.Test;
import rapaio.data.filter.VFRefSort;
import rapaio.data.filter.VFSort;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static rapaio.data.RowComparators.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortVarTest {

    @Test
    public void smokeTest() {
        Var v = Index.newEmpty();
        Var sorted = new VFRefSort(index(v, true)).filter(v);
        assertTrue(sorted.type().isNumeric());
        assertFalse(sorted.type().isNominal());

        v = Numeric.newEmpty();
        sorted = new VFRefSort(numeric(v, true)).filter(v);
        assertTrue(sorted.type().isNumeric());
        assertFalse(sorted.type().isNominal());

        v = Nominal.newEmpty(0);
        sorted = new VFRefSort(nominal(v, true)).filter(v);
        assertFalse(sorted.type().isNumeric());
        assertTrue(sorted.type().isNominal());
    }

    @Test
    public void testSortIndex() {
        Var index = Index.newSeq(10, 10, -1);
        index.setMissing(2);
        index.setMissing(5);
        index.setIndex(0, 1);

        assertEquals(10, index.rowCount());
        Var sort = new VFSort().filter(index);
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.index(i - 1) <= sort.index(i));
        }

        sort = new VFSort(false).filter(index);
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.index(i - 1) >= sort.index(i));
        }

        Var second = new VFSort().filter(index);
        for (int i = 1; i < second.rowCount(); i++) {
            assertTrue(second.index(i - 1) <= second.index(i));
        }
    }

    @Test
    public void testSortNumeric() {
        Var numeric = Numeric.newCopyOf(2., 4., 1.2, 1.3, 1.2, 0., 100.);

        assertEquals(7, numeric.rowCount());
        Var sort = new VFSort(true).filter(numeric);
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.value(i - 1) <= sort.value(i));
        }

        sort = new VFSort(false).filter(numeric);
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.value(i - 1) >= sort.value(i));
        }

        Var second = new VFSort(true).filter(numeric);
        for (int i = 1; i < second.rowCount(); i++) {
            assertTrue(second.index(i - 1) <= second.index(i));
        }
    }

    @Test
    public void testSortNominal() {
        String[] dict = new String[]{"a", "Aa", "b", "c", "Cc"};
        Var nominal = Nominal.newEmpty(10, dict);

        for (int i = 0; i < 10; i++) {
            nominal.setLabel(i, dict[i % dict.length]);
        }
        nominal.setMissing(2);
        nominal.setMissing(3);
        nominal.setMissing(4);
        nominal.setMissing(5);

        Var sort = new VFSort(true).filter(nominal);
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.label(i - 1).compareTo(sort.label(i)) <= 0);
        }

        sort = new VFSort(false).filter(nominal);
        for (int i = 1; i < sort.rowCount(); i++) {
            assertTrue(sort.label(i - 1).compareTo(sort.label(i)) >= 0);
        }

        Var second = new VFSort(true).filter(nominal);
        for (int i = 1; i < second.rowCount(); i++) {
            assertTrue(second.label(i - 1).compareTo(second.label(i)) <= 0);
        }
    }

    @Test
    public void testGetterSetter() throws IOException, URISyntaxException {

        Frame df = new Csv()
                .withQuotes(false)
                .withTypes(VarType.NUMERIC, "z")
                .withTypes(VarType.INDEX, "y")
                .read(SortVarTest.class, "sorted-frame.csv");

        Var nominal = df.var(0);
        Var index = df.var(1);
        Var numeric = df.var(2);

        // nominal

        HashMap<String, String> transform = new HashMap<>();
        transform.put("a", "c");
        transform.put("b", "a");
        transform.put("c", "b");
        transform.put("d", "d");
        Var sort = new VFSort().filter(nominal);
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

        sort = new VFSort().filter(numeric);
        for (int i = 0; i < sort.rowCount(); i++) {
            sort.setValue(i, sort.value(i) + Math.E);
        }
        assertEquals(Math.E + 1., numeric.value(0), 1e-10);
        assertEquals(Math.E + 2.5, numeric.value(2), 1e-10);
        assertEquals(Math.E + 4, numeric.value(1), 1e-10);
        assertEquals(Math.E + 4., numeric.value(3), 1e-10);


        // index

        sort = new VFSort().filter(index);
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
        Var v = Index.newSeq(1, 10);
        v = new VFRefSort(index(v, true)).filter(v);
        for (int i = 0; i < 10; i += 3) {
            v.setMissing(i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i % 3 == 0, v.missing(i));
        }
    }
}
