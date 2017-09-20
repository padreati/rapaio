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

import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OrdinalTest {

    @Test
    public void testSmoke() {
        Var v = OrdinalVar.empty(0);
        assertEquals(0, v.rowCount());
        assertEquals(1, v.levels().length);
        assertEquals("?", v.levels()[0]);

        v = OrdinalVar.empty();
        assertEquals(0, v.rowCount());
        assertEquals(1, v.levels().length);
        assertEquals("?", v.levels()[0]);

        assertTrue(v.type().isNominal());
        assertFalse(v.type().isNumeric());

        v = OrdinalVar.empty(1, "a");
        assertEquals(1, v.rowCount());
        assertEquals("?", v.label(0));

        assertEquals("Ordinal[name:?, rowCount:10]", OrdinalVar.empty(10).toString());
    }

    @Test
    public void testDictionary() {
        Var v = OrdinalVar.empty(0, "a", "a", "v", "a");
        assertEquals(3, v.levels().length);
        assertEquals("?", v.levels()[0]);
        assertEquals("a", v.levels()[1]);
        assertEquals("v", v.levels()[2]);

        TreeSet<String> set = new TreeSet<>();
        set.add("a");
        set.add("v");
        set.add("a");

        v = OrdinalVar.empty(0, set);
        assertEquals(3, v.levels().length);
        assertEquals("?", v.levels()[0]);
        assertEquals("a", v.levels()[1]);
        assertEquals("v", v.levels()[2]);
    }

    @Test
    public void testSetterGetter() {
        Var v = OrdinalVar.empty(4, "a", "b", "c");
        for (int i = 0; i < 4; i++) {
            assertTrue(v.isMissing(i));
            assertEquals(0, v.index(i));
        }

        // w/ index

        v.setIndex(0, 1);
        v.setIndex(1, 2);
        v.setIndex(2, 3);
        v.setIndex(3, 0);

        assertEquals("a", v.label(0));
        assertEquals("b", v.label(1));
        assertEquals("c", v.label(2));
        assertEquals("?", v.label(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.index(0));
        assertEquals(2, v.index(1));
        assertEquals(1, v.index(2));
        assertEquals(0, v.index(3));

        // w/ value

        v.setValue(0, 1);
        v.setValue(1, 2);
        v.setValue(2, 3);
        v.setValue(3, 0);

        assertEquals("a", v.label(0));
        assertEquals("b", v.label(1));
        assertEquals("c", v.label(2));
        assertEquals("?", v.label(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.value(0), 1e-10);
        assertEquals(2, v.value(1), 1e-10);
        assertEquals(1, v.value(2), 1e-10);
        assertEquals(0, v.value(3), 1e-10);
    }

    @Test
    public void testLabel() {
        Var v = OrdinalVar.empty(1, "a", "b", "c");

        boolean exceptional = false;
        try {
            v.setLabel(0, "j");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(!exceptional);

        exceptional = false;
        try {
            v.setLabel(-1, "a");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);

        exceptional = false;
        try {
            v.setLabel(4, "a");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testMissing() {
        Var v = OrdinalVar.empty(1, "a", "b");
        assertTrue(v.isMissing(0));

        v.setLabel(0, "a");
        assertFalse(v.isMissing(0));

        v.setMissing(0);
        assertTrue(v.isMissing(0));

        v.setLabel(0, "?");
        assertTrue(v.isMissing(0));
    }

    @Test
    public void testCopy() {
        OrdinalVar a = OrdinalVar.empty(0, "x", "y");
        a.addLabel("x");
        a.addLabel("y");

        OrdinalVar b = a.solidCopy();

        a.addLabel("z");

        assertEquals(2, b.rowCount());
        assertEquals(3, a.rowCount());
    }
}
