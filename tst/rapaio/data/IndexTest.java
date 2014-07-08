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

import static junit.framework.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IndexTest {

    @Test
    public void smokeTest() {
        Var index = Index.newEmpty(1);
        assertTrue(index.type().isNumeric());
        assertFalse(index.type().isNominal());

        try {
            index.dictionary();
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyIndex() {
        Var index = Index.newEmpty();
        assertEquals(0, index.rowCount());

        index = Index.newEmpty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(0, index.index(i));
        }
    }

    @Test
    public void testFillVector() {
        Var index = Numeric.newFill(10, -1);
        assertEquals(10, index.rowCount());
        for (int i = 0; i < index.rowCount(); i++) {
            assertEquals(-1, index.index(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Var index = Index.newSeq(1, 10);
        assertEquals(10, index.rowCount());
        for (int i = 0; i < index.rowCount(); i++) {
            assertEquals(i + 1, index.index(i));
        }
    }

    @Test
    public void testSetterGetter() {

        Var index = Index.newFill(3, 0);

        assertEquals(0, index.index(0));
        index.setIndex(0, 1);
        index.setIndex(1, 3);

        assertEquals(1, index.index(0));
        assertEquals(3, index.index(1));

        assertEquals(1., index.value(0), 1e-10);
        assertEquals(3., index.value(1), 1e-10);

        index.setValue(0, 2.5);
        index.setValue(1, 7.8);
        index.setValue(2, 2.51);

        assertEquals(2, index.index(0));
        assertEquals(2., index.value(0), 1e-10);
        assertEquals(8, index.index(1));
        assertEquals(8., index.value(1), 1e-10);
        assertEquals(3, index.index(2));
        assertEquals(3., index.value(2), 1e-10);

        assertEquals("", index.label(0));
        assertEquals("", index.label(1));

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
        Var index = Index.newSeq(1, 10, 1);
        for (int i = 0; i < index.rowCount(); i++) {
            assertTrue(!index.missing(i));
        }
        for (int i = 0; i < index.rowCount(); i++) {
            if (i % 2 == 0)
                index.setMissing(i);
        }
        for (int i = 0; i < index.rowCount(); i++) {
            assertEquals(i % 2 == 0, index.missing(i));
        }
    }

    @Test
    public void testOneIndex() {
        Var one = Index.newScalar(2);
        assertEquals(1, one.rowCount());
        assertEquals(2, one.index(0));

        one = Index.newScalar(3);
        assertEquals(1, one.rowCount());
        assertEquals(3, one.index(0));
    }
}
