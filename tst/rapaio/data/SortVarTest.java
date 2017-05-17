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

import org.junit.Test;
import rapaio.data.filter.var.VFRefSort;
import rapaio.data.filter.var.VFSort;
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
        Var v = IndexVar.empty();
        Var sorted = new VFRefSort(index(v, true)).fitApply(v);
        assertTrue(sorted.getType().isNumeric());
        assertFalse(sorted.getType().isNominal());

        v = NumericVar.empty();
        sorted = new VFRefSort(numeric(v, true)).fitApply(v);
        assertTrue(sorted.getType().isNumeric());
        assertFalse(sorted.getType().isNominal());

        v = NominalVar.empty(0);
        sorted = new VFRefSort(nominal(v, true)).fitApply(v);
        assertFalse(sorted.getType().isNumeric());
        assertTrue(sorted.getType().isNominal());
    }

    @Test
    public void testSortIndex() {
        Var index = IndexVar.seq(10, 10, -1);
        index.setMissing(2);
        index.setMissing(5);
        index.setIndex(0, 1);

        assertEquals(10, index.getRowCount());
        Var sort = new VFSort().fitApply(index);
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getIndex(i - 1) <= sort.getIndex(i));
        }

        sort = new VFSort(false).fitApply(index);
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getIndex(i - 1) >= sort.getIndex(i));
        }

        Var second = new VFSort().fitApply(index);
        for (int i = 1; i < second.getRowCount(); i++) {
            assertTrue(second.getIndex(i - 1) <= second.getIndex(i));
        }
    }

    @Test
    public void testSortNumeric() {
        Var numeric = NumericVar.copy(2., 4., 1.2, 1.3, 1.2, 0., 100.);

        assertEquals(7, numeric.getRowCount());
        Var sort = new VFSort(true).fitApply(numeric);
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getValue(i - 1) <= sort.getValue(i));
        }

        sort = new VFSort(false).fitApply(numeric);
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getValue(i - 1) >= sort.getValue(i));
        }

        Var second = new VFSort(true).fitApply(numeric);
        for (int i = 1; i < second.getRowCount(); i++) {
            assertTrue(second.getIndex(i - 1) <= second.getIndex(i));
        }
    }

    @Test
    public void testSortNominal() {
        String[] dict = new String[]{"a", "Aa", "b", "c", "Cc"};
        Var nominal = NominalVar.empty(10, dict);

        for (int i = 0; i < 10; i++) {
            nominal.setLabel(i, dict[i % dict.length]);
        }
        nominal.setMissing(2);
        nominal.setMissing(3);
        nominal.setMissing(4);
        nominal.setMissing(5);

        Var sort = new VFSort(true).fitApply(nominal);
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) <= 0);
        }

        sort = new VFSort(false).fitApply(nominal);
        for (int i = 1; i < sort.getRowCount(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) >= 0);
        }

        Var second = new VFSort(true).fitApply(nominal);
        for (int i = 1; i < second.getRowCount(); i++) {
            assertTrue(second.getLabel(i - 1).compareTo(second.getLabel(i)) <= 0);
        }
    }

    @Test
    public void testGetterSetter() throws IOException, URISyntaxException {

        Frame df = new Csv()
                .withQuotes(false)
                .withTypes(VarType.NUMERIC, "z")
                .withTypes(VarType.INDEX, "y")
                .read(SortVarTest.class, "sorted-frame.csv");

        Var nominal = df.getVar(0);
        Var index = df.getVar(1);
        Var numeric = df.getVar(2);

        // nominal

        HashMap<String, String> transform = new HashMap<>();
        transform.put("a", "c");
        transform.put("b", "a");
        transform.put("c", "b");
        transform.put("d", "d");
        Var sort = new VFSort().fitApply(nominal);
        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setLabel(i, transform.get(sort.getLabel(i)));
        }

        assertEquals("b", nominal.getLabel(0));
        assertEquals("a", nominal.getLabel(1));
        assertEquals("c", nominal.getLabel(2));
        assertEquals("d", nominal.getLabel(3));

        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setIndex(i, 2);
            assertEquals(nominal.getLevels()[2], nominal.getLabel(i));
            assertEquals(2, nominal.getIndex(i));
        }

        assertEquals(nominal.getLevels().length, sort.getLevels().length);
        for (int i = 0; i < nominal.getLevels().length; i++) {
            assertEquals(nominal.getLevels()[i], sort.getLevels()[i]);
        }

        // numeric

        sort = new VFSort().fitApply(numeric);
        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setValue(i, sort.getValue(i) + Math.E);
        }
        assertEquals(Math.E + 1., numeric.getValue(0), 1e-10);
        assertEquals(Math.E + 2.5, numeric.getValue(2), 1e-10);
        assertEquals(Math.E + 4, numeric.getValue(1), 1e-10);
        assertEquals(Math.E + 4., numeric.getValue(3), 1e-10);


        // index

        sort = new VFSort().fitApply(index);
        for (int i = 0; i < sort.getRowCount(); i++) {
            sort.setValue(i, sort.getIndex(i) + 10);
        }
        assertEquals(11, index.getIndex(0));
        assertEquals(12, index.getIndex(2));
        assertEquals(12, index.getIndex(3));
        assertEquals(13, index.getIndex(1));
    }

    @Test
    public void testMissing() {
        Var v = IndexVar.seq(1, 10);
        v = new VFRefSort(index(v, true)).fitApply(v);
        for (int i = 0; i < 10; i += 3) {
            v.setMissing(i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i % 3 == 0, v.isMissing(i));
        }
    }
}
