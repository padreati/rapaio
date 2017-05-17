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
        Var index = IndexVar.empty(1);
        assertTrue(index.getType().isNumeric());
        assertFalse(index.getType().isNominal());

        assertEquals(1, index.getRowCount());

        try {
            index.getLevels();
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }

        try {
            IndexVar.empty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("Index[name:?, rowCount:1]", IndexVar.empty(1).toString());
    }

    @Test
    public void testEmptyIndex() {
        Var index = IndexVar.empty();
        assertEquals(0, index.getRowCount());

        index = IndexVar.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(0, index.getIndex(i));
        }
    }

    @Test
    public void testFillVector() {
        Var index = IndexVar.fill(10, -1);
        assertEquals(10, index.getRowCount());
        for (int i = 0; i < index.getRowCount(); i++) {
            assertEquals(-1, index.getIndex(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Var index = IndexVar.seq(1, 10);
        assertEquals(10, index.getRowCount());
        for (int i = 0; i < index.getRowCount(); i++) {
            assertEquals(i + 1, index.getIndex(i));
        }
    }

    @Test
    public void testSetterGetter() {

        Var index = IndexVar.fill(3, 0);

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
        Var index = IndexVar.seq(1, 10, 1);
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
        Var one = IndexVar.scalar(2);
        assertEquals(1, one.getRowCount());
        assertEquals(2, one.getIndex(0));

        one = IndexVar.scalar(3);
        assertEquals(1, one.getRowCount());
        assertEquals(3, one.getIndex(0));
    }

    @Test
    public void testBuilders() {
        IndexVar x1 = IndexVar.copy(1, 2, 3, 4);
        int[] wrap = new int[]{1, 2, 3, 4};
        IndexVar x2 = IndexVar.wrap(wrap);
        IndexVar x3 = IndexVar.seq(4);
        IndexVar x4 = IndexVar.seq(1, 4);
        IndexVar x5 = IndexVar.seq(1, 4, 2);
        IndexVar x6 = IndexVar.empty();
        x6.addIndex(1);
        x6.addIndex(2);
        x6.addIndex(3);
        x6.addIndex(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.getIndex(i));
            assertEquals(i + 1, x2.getIndex(i));
            assertEquals(i, x3.getIndex(i));
            assertEquals(i + 1, x4.getIndex(i));
            assertEquals(i * 2 + 1, x5.getIndex(i));
            assertEquals(i + 1, x6.getIndex(i));
        }

        wrap[2] = 10;

        assertEquals(10, x2.getIndex(2));
    }

    @Test
    public void testLabel() {
        IndexVar x = IndexVar.copy(1, 2, 3);
        assertEquals("1", x.getLabel(0));
    }

    @Test
    public void testAddLabel() {
        IndexVar x = IndexVar.copy(1, 2, 3);
        x.addLabel("10");
        assertEquals(4, x.getRowCount());
        assertEquals("10", x.getLabel(3));
    }

    @Test
    public void testSetLabel() {
        IndexVar x = IndexVar.copy(1, 2, 3);
        x.setLabel(0, "10");
        assertEquals(3, x.getRowCount());
        assertEquals("10", x.getLabel(0));
    }

    @Test
    public void testSetDictionary() {
        IndexVar x = IndexVar.copy(1, 2, 3);
        expected.expect(IllegalArgumentException.class);
        x.setLevels(new String[]{"x"});
    }

    @Test
    public void testBinary() {
        IndexVar x = IndexVar.empty();
        x.addBinary(true);
        x.addBinary(false);
        x.addMissing();
        x.setBinary(2, true);

        assertEquals(1, x.getIndex(0));
        assertEquals(0, x.getIndex(1));
        assertEquals(1, x.getIndex(2));

        assertEquals(true, x.getBinary(0));
        assertEquals(false, x.getBinary(1));
        assertEquals(true, x.getBinary(2));
    }

    @Test
    public void testStamp() {
        IndexVar x = IndexVar.empty();
        x.addStamp(0);
        x.addMissing();
        x.setStamp(1, 100);

        assertEquals(0, x.getStamp(0));
        assertEquals(100, x.getStamp(1));
    }

    @Test
    public void testRemoveClear() {

        IndexVar x = IndexVar.copy(1, 3, 6, 7, 9);
        x.remove(0);

        assertEquals(4, x.getRowCount());
        assertEquals(3, x.getIndex(0));
        assertEquals(9, x.getIndex(3));

        x.clear();
        assertEquals(0, x.getRowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.remove(-1);
    }

    @Test
    public void testSolidCopy() {

        IndexVar x1 = IndexVar.copy(1, 2, 3, 4, 5);
        Var x2 = MappedVar.byRows(x1, 0, 1, 2);
        Var x3 = x2.solidCopy();
        Var x4 = x3.solidCopy();
        x4.addValue(8);

        assertEquals(4, x4.getRowCount());
        assertEquals(1, x4.getIndex(0));
        assertEquals(3, x4.getIndex(2));
        assertEquals(8, x4.getIndex(3));
    }

}
