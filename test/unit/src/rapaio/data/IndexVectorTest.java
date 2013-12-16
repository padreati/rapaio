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

import static junit.framework.Assert.*;
import org.junit.Test;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IndexVectorTest {

    @Test
    public void smokeTest() {
        Vector index = new IndexVector(1);
        assertTrue(index.isNumeric());
        assertFalse(index.isNominal());

        assertEquals(0, index.getRowId(0));

        assertNotNull(index.getDictionary());
        assertEquals(0, index.getDictionary().length);
    }

    @Test
    public void testEmptyIndex() {
        Vector index = new IndexVector(0);
        assertEquals(0, index.getRowCount());

        index = new IndexVector(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(0, index.getIndex(i));
        }
    }

    @Test
    public void testFillVector() {
        Vector index = new IndexVector(10, -1);
        assertEquals(10, index.getRowCount());
        for (int i = 0; i < index.getRowCount(); i++) {
            assertEquals(-1, index.getIndex(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Vector index = new IndexVector(1, 10, 1);
        assertEquals(10, index.getRowCount());
        for (int i = 0; i < index.getRowCount(); i++) {
            assertEquals(i + 1, index.getIndex(i));
        }

        boolean exceptional = false;
        try {
            index = new IndexVector(1, 1, 0);
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            new IndexVector(1, 2, 0);
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

    }

    @Test
    public void testSetterGetter() {

        Vector index = new IndexVector(3, 0);

        assertEquals(0, index.getIndex(0));
        index.setIndex(0, 1);
        index.setIndex(1, 3);

        assertEquals(1, index.getIndex(0));
        assertEquals(3, index.getIndex(1));

        assertEquals(1., index.getValue(0), 1e-10);
        assertEquals(3., index.getValue(1), 1e-10);

        index.setValue(0, 2.5);
        index.setValue(1, 7.8);
        index.setValue(2, 2.51);

        assertEquals(2, index.getIndex(0));
        assertEquals(2., index.getValue(0), 1e-10);
        assertEquals(8, index.getIndex(1));
        assertEquals(8., index.getValue(1), 1e-10);
        assertEquals(3, index.getIndex(2));
        assertEquals(3., index.getValue(2), 1e-10);

        assertEquals("", index.getLabel(0));
        assertEquals("", index.getLabel(1));

        boolean exceptional = false;
        try {
            index.setLabel(0, "Test");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);
    }

    @Test
    public void testMissing() {
        Vector index = new IndexVector(1, 10, 1);
        for (int i = 0; i < index.getRowCount(); i++) {
            assertTrue(!index.isMissing(i));
        }
        for (int i = 0; i < index.getRowCount(); i++) {
            if (i % 2 == 0)
                index.setMissing(i);
        }
        for (int i = 0; i < index.getRowCount(); i++) {
            assertEquals(i % 2 == 0, index.isMissing(i));
        }
    }

    @Test
    public void testOneIndex() {
        Vector one = new OneIndexVector(2);
        assertEquals(1, one.getRowCount());
        assertEquals(2, one.getIndex(0));

        one = new OneIndexVector(3);
        assertEquals(1, one.getRowCount());
        assertEquals(3, one.getIndex(0));
    }
}
