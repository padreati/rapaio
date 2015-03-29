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
 */

package rapaio.data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IndexTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void smokeTest() {
        Var index = Index.newEmpty(1);
        assertTrue(index.getType().isNumeric());
        assertFalse(index.getType().isNominal());

        assertEquals(1, index.rowCount());

        try {
            index.dictionary();
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }

        try {
            Index.newEmpty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("Index[1]", Index.newEmpty(1).toString());
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
        Var index = Index.newFill(10, -1);
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

    @Test
    public void testBuilders() {
        Index x1 = Index.newCopyOf(1, 2, 3, 4);
        int[] wrap = new int[]{1, 2, 3, 4};
        Index x2 = Index.newWrapOf(wrap);
        Index x3 = Index.newSeq(4);
        Index x4 = Index.newSeq(1, 4);
        Index x5 = Index.newSeq(1, 4, 2);
        Index x6 = Index.newEmpty();
        x6.addIndex(1);
        x6.addIndex(2);
        x6.addIndex(3);
        x6.addIndex(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.index(i));
            assertEquals(i + 1, x2.index(i));
            assertEquals(i, x3.index(i));
            assertEquals(i + 1, x4.index(i));
            assertEquals(i * 2 + 1, x5.index(i));
            assertEquals(i + 1, x6.index(i));
        }

        wrap[2] = 10;

        assertEquals(10, x2.index(2));
    }

    @Test
    public void testLabel() {
        Index x = Index.newCopyOf(1, 2, 3);
        assertEquals("1", x.label(0));
    }

    @Test
    public void testAddLabel() {
        Index x = Index.newCopyOf(1, 2, 3);
        x.addLabel("10");
        assertEquals(4, x.rowCount());
        assertEquals("10", x.label(3));
    }

    @Test
    public void testSetLabel() {
        Index x = Index.newCopyOf(1, 2, 3);
        x.setLabel(0, "10");
        assertEquals(3, x.rowCount());
        assertEquals("10", x.label(0));
    }

    @Test
    public void testSetDictionary() {
        Index x = Index.newCopyOf(1, 2, 3);
        expected.expect(IllegalArgumentException.class);
        x.setDictionary(new String[]{"x"});
    }

    @Test
    public void testBinary() {
        Index x = Index.newEmpty();
        x.addBinary(true);
        x.addBinary(false);
        x.addMissing();
        x.setBinary(2, true);

        assertEquals(1, x.index(0));
        assertEquals(0, x.index(1));
        assertEquals(1, x.index(2));

        assertEquals(true, x.binary(0));
        assertEquals(false, x.binary(1));
        assertEquals(true, x.binary(2));
    }

    @Test
    public void testStamp() {
        Index x = Index.newEmpty();
        x.addStamp(0);
        x.addMissing();
        x.setStamp(1, 100);

        assertEquals(0, x.stamp(0));
        assertEquals(100, x.stamp(1));
    }

    @Test
    public void testRemoveClear() {

        Index x = Index.newCopyOf(1, 3, 6, 7, 9);
        x.remove(0);

        assertEquals(4, x.rowCount());
        assertEquals(3, x.index(0));
        assertEquals(9, x.index(3));

        x.clear();
        assertEquals(0, x.rowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.remove(-1);
    }

    @Test
    public void testSolidCopy() {

        Index x1 = Index.newCopyOf(1, 2, 3, 4, 5);
        Var x2 = MappedVar.newByRows(x1, 0, 1, 2);
        Var x3 = x2.solidCopy();
        Var x4 = x3.solidCopy();
        x4.addValue(8);

        assertEquals(4, x4.rowCount());
        assertEquals(1, x4.index(0));
        assertEquals(3, x4.index(2));
        assertEquals(8, x4.index(3));
    }

}
