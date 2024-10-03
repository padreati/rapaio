/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.data.RowComparators.doubleComparator;
import static rapaio.data.RowComparators.integerComparator;
import static rapaio.data.RowComparators.labelComparator;

import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import rapaio.data.transform.VarRefSort;
import rapaio.data.transform.VarSort;
import rapaio.io.Csv;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortVarTest {

    @Test
    void smokeTest() {
        Var v = VarInt.empty();
        Var sorted = VarRefSort.from(integerComparator(v, true)).fapply(v);
        assertTrue(sorted.type().isNumeric());
        assertFalse(sorted.type().isNominal());

        v = VarDouble.empty();
        sorted = VarRefSort.from(doubleComparator(v, true)).fapply(v);
        assertTrue(sorted.type().isNumeric());
        assertFalse(sorted.type().isNominal());

        v = VarNominal.empty(0);
        sorted = VarRefSort.from(labelComparator(v, true)).fapply(v);
        assertFalse(sorted.type().isNumeric());
        assertTrue(sorted.type().isNominal());
    }

    @Test
    void testSortIndex() {
        Var index = VarInt.seq(10, 10, -1);
        index.setMissing(2);
        index.setMissing(5);
        index.setInt(0, 1);

        assertEquals(10, index.size());
        Var sort = VarSort.ascending().fapply(index);
        for (int i = 1; i < sort.size(); i++) {
            assertTrue(sort.getInt(i - 1) <= sort.getInt(i));
        }

        sort = VarSort.descending().fapply(index);
        for (int i = 1; i < sort.size(); i++) {
            assertTrue(sort.getInt(i - 1) >= sort.getInt(i));
        }

        Var second = VarSort.ascending().fapply(index);
        for (int i = 1; i < second.size(); i++) {
            assertTrue(second.getInt(i - 1) <= second.getInt(i));
        }
    }

    @Test
    void testSortNumeric() {
        Var numeric = VarDouble.copy(2., 4., 1.2, 1.3, 1.2, 0., 100.);

        assertEquals(7, numeric.size());
        Var sort = VarSort.ascending().fapply(numeric);
        for (int i = 1; i < sort.size(); i++) {
            assertTrue(sort.getDouble(i - 1) <= sort.getDouble(i));
        }

        sort = VarSort.descending().fapply(numeric);
        for (int i = 1; i < sort.size(); i++) {
            assertTrue(sort.getDouble(i - 1) >= sort.getDouble(i));
        }

        Var second = VarSort.ascending().fapply(numeric);
        for (int i = 1; i < second.size(); i++) {
            assertTrue(second.getInt(i - 1) <= second.getInt(i));
        }
    }

    @Test
    void testSortNominal() {
        String[] dict = new String[]{"a", "Aa", "b", "c", "Cc"};
        Var nominal = VarNominal.empty(10, dict);

        for (int i = 0; i < 10; i++) {
            nominal.setLabel(i, dict[i % dict.length]);
        }
        nominal.setMissing(2);
        nominal.setMissing(3);
        nominal.setMissing(4);
        nominal.setMissing(5);

        Var sort = VarSort.ascending().fapply(nominal);
        for (int i = 1; i < sort.size(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) <= 0);
        }

        sort = VarSort.descending().fapply(nominal);
        for (int i = 1; i < sort.size(); i++) {
            assertTrue(sort.getLabel(i - 1).compareTo(sort.getLabel(i)) >= 0);
        }

        Var second = VarSort.ascending().fapply(nominal);
        for (int i = 1; i < second.size(); i++) {
            assertTrue(second.getLabel(i - 1).compareTo(second.getLabel(i)) <= 0);
        }
    }

    @Test
    void testGetterSetter() throws IOException {

        Frame df = Csv.instance()
                .quotes.set(false)
                .types.add(VarType.DOUBLE, "z")
                .types.add(VarType.INT, "y")
                .read(SortVarTest.class, "sorted-frame.csv");

        Var nominal = df.rvar(0);
        Var index = df.rvar(1);
        Var numeric = df.rvar(2);

        // nominal

        HashMap<String, String> transform = new HashMap<>();
        transform.put("a", "c");
        transform.put("b", "a");
        transform.put("c", "b");
        transform.put("d", "d");
        Var sort = VarSort.ascending().fapply(nominal);
        for (int i = 0; i < sort.size(); i++) {
            sort.setLabel(i, transform.get(sort.getLabel(i)));
        }

        assertEquals("b", nominal.getLabel(0));
        assertEquals("a", nominal.getLabel(1));
        assertEquals("c", nominal.getLabel(2));
        assertEquals("d", nominal.getLabel(3));

        for (int i = 0; i < sort.size(); i++) {
            sort.setInt(i, 2);
            assertEquals(nominal.levels().get(2), nominal.getLabel(i));
            assertEquals(2, nominal.getInt(i));
        }

        assertEquals(nominal.levels().size(), sort.levels().size());
        for (int i = 0; i < nominal.levels().size(); i++) {
            assertEquals(nominal.levels().get(i), sort.levels().get(i));
        }

        // numeric

        sort = VarSort.ascending().fapply(numeric);
        for (int i = 0; i < sort.size(); i++) {
            sort.setDouble(i, sort.getDouble(i) + Math.E);
        }
        assertEquals(Math.E + 1., numeric.getDouble(0), 1e-10);
        assertEquals(Math.E + 2.5, numeric.getDouble(2), 1e-10);
        assertEquals(Math.E + 4, numeric.getDouble(1), 1e-10);
        assertEquals(Math.E + 4., numeric.getDouble(3), 1e-10);


        // index

        sort = VarSort.ascending().fapply(index);
        for (int i = 0; i < sort.size(); i++) {
            sort.setDouble(i, sort.getInt(i) + 10);
        }
        assertEquals(11, index.getInt(0));
        assertEquals(12, index.getInt(2));
        assertEquals(12, index.getInt(3));
        assertEquals(13, index.getInt(1));
    }

    @Test
    void testMissing() {
        Var v = VarInt.seq(1, 10);
        v = VarRefSort.from(integerComparator(v, true)).fapply(v);
        for (int i = 0; i < 10; i += 3) {
            v.setMissing(i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i % 3 == 0, v.isMissing(i));
        }
    }
}
