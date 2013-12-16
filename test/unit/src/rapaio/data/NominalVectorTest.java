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

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.HashSet;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NominalVectorTest {

    @Test
    public void testSmoke() {
        Vector v = new NominalVector(0, new String[]{});
        assertEquals(0, v.getRowCount());
        assertEquals(1, v.getDictionary().length);
        assertEquals("?", v.getDictionary()[0]);

        assertTrue(v.isNominal());
        assertFalse(v.isNumeric());

        v = new NominalVector(1, new String[]{"a"});
        assertEquals(1, v.getRowCount());
        assertEquals("?", v.getLabel(0));

        assertEquals(0, v.getRowId(0));
    }

    @Test
    public void testDictionary() {
        Vector v = new NominalVector(0, new String[]{"a", "a", "v", "a"});
        assertEquals(3, v.getDictionary().length);
        assertEquals("?", v.getDictionary()[0]);
        assertEquals("a", v.getDictionary()[1]);
        assertEquals("v", v.getDictionary()[2]);

        HashSet<String> set = new HashSet<>();
        set.add("a");
        set.add("v");
        set.add("a");

        v = new NominalVector(0, set);
        assertEquals(3, v.getDictionary().length);
        assertEquals("?", v.getDictionary()[0]);
        assertEquals("a", v.getDictionary()[1]);
        assertEquals("v", v.getDictionary()[2]);
    }

    @Test
    public void testSetterGetter() {
        Vector v = new NominalVector(4, new String[]{"a", "b", "c"});
        for (int i = 0; i < 4; i++) {
            assertTrue(v.isMissing(i));
            assertEquals(0, v.getIndex(i));
        }

        // w/ index

        v.setIndex(0, 1);
        v.setIndex(1, 2);
        v.setIndex(2, 3);
        v.setIndex(3, 0);

        assertEquals("a", v.getLabel(0));
        assertEquals("b", v.getLabel(1));
        assertEquals("c", v.getLabel(2));
        assertEquals("?", v.getLabel(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.getIndex(0));
        assertEquals(2, v.getIndex(1));
        assertEquals(1, v.getIndex(2));
        assertEquals(0, v.getIndex(3));

        // w/ value

        v.setValue(0, 1);
        v.setValue(1, 2);
        v.setValue(2, 3);
        v.setValue(3, 0);

        assertEquals("a", v.getLabel(0));
        assertEquals("b", v.getLabel(1));
        assertEquals("c", v.getLabel(2));
        assertEquals("?", v.getLabel(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.getValue(0), 1e-10);
        assertEquals(2, v.getValue(1), 1e-10);
        assertEquals(1, v.getValue(2), 1e-10);
        assertEquals(0, v.getValue(3), 1e-10);
    }

    @Test
    public void testLabel() {
        Vector v = new NominalVector(1, new String[]{"a", "b", "c"});

        boolean exceptional = false;
        try {
            v.setLabel(0, "j");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);

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
        Vector v = new NominalVector(1, new String[]{"a", "b"});
        assertTrue(v.isMissing(0));

        v.setLabel(0, "a");
        assertFalse(v.isMissing(0));

        v.setMissing(0);
        assertTrue(v.isMissing(0));

        v.setLabel(0, "?");
        assertTrue(v.isMissing(0));
    }
}
