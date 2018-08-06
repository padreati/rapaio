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
        Var index = VarInt.empty(1);
        assertTrue(index.type().isNumeric());
        assertFalse(index.type().isNominal());

        assertEquals(1, index.rowCount());

        try {
            VarInt.empty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("Index[name:?, rowCount:1]", VarInt.empty(1).toString());
    }

    @Test
    public void testEmptyIndex() {
        Var index = VarInt.empty();
        assertEquals(0, index.rowCount());

        index = VarInt.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(0, index.getInt(i));
        }
    }

    @Test
    public void testFillVector() {
        Var index = VarInt.fill(10, -1);
        assertEquals(10, index.rowCount());
        for (int i = 0; i < index.rowCount(); i++) {
            assertEquals(-1, index.getInt(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Var index = VarInt.seq(1, 10);
        assertEquals(10, index.rowCount());
        for (int i = 0; i < index.rowCount(); i++) {
            assertEquals(i + 1, index.getInt(i));
        }
    }

    @Test
    public void testSetterGetter() {

        Var index = VarInt.fill(3, 0);

        assertEquals(0, index.getInt(0));
        index.setInt(0, 1);
        index.setInt(1, 3);

        assertEquals(1, index.getInt(0));
        assertEquals(3, index.getInt(1));

        assertEquals(1., index.getDouble(0), 1e-10);
        assertEquals(3., index.getDouble(1), 1e-10);

        index.setDouble(0, 2.5);
        index.setDouble(1, 7.8);
        index.setDouble(2, 2.51);

        assertEquals(2, index.getInt(0));
        assertEquals(2., index.getDouble(0), 1e-10);
        assertEquals(8, index.getInt(1));
        assertEquals(8., index.getDouble(1), 1e-10);
        assertEquals(3, index.getInt(2));
        assertEquals(3., index.getDouble(2), 1e-10);

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
        Var index = VarInt.seq(1, 10, 1);
        for (int i = 0; i < index.rowCount(); i++) {
            assertTrue(!index.isMissing(i));
        }
        for (int i = 0; i < index.rowCount(); i++) {
            if (i % 2 == 0)
                index.setMissing(i);
        }
        for (int i = 0; i < index.rowCount(); i++) {
            assertEquals(i % 2 == 0, index.isMissing(i));
        }
    }

    @Test
    public void testOneIndex() {
        Var one = VarInt.scalar(2);
        assertEquals(1, one.rowCount());
        assertEquals(2, one.getInt(0));

        one = VarInt.scalar(3);
        assertEquals(1, one.rowCount());
        assertEquals(3, one.getInt(0));
    }

    @Test
    public void testBuilders() {
        VarInt x1 = VarInt.copy(1, 2, 3, 4);
        int[] wrap = new int[]{1, 2, 3, 4};
        VarInt x2 = VarInt.wrap(wrap);
        VarInt x3 = VarInt.seq(4);
        VarInt x4 = VarInt.seq(1, 4);
        VarInt x5 = VarInt.seq(1, 4, 2);
        VarInt x6 = VarInt.empty();
        x6.addInt(1);
        x6.addInt(2);
        x6.addInt(3);
        x6.addInt(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.getInt(i));
            assertEquals(i + 1, x2.getInt(i));
            assertEquals(i, x3.getInt(i));
            assertEquals(i + 1, x4.getInt(i));
            assertEquals(i * 2 + 1, x5.getInt(i));
            assertEquals(i + 1, x6.getInt(i));
        }

        wrap[2] = 10;

        assertEquals(10, x2.getInt(2));
    }

    @Test
    public void testLabel() {
        VarInt x = VarInt.copy(1, 2, 3);
        assertEquals("1", x.getLabel(0));
    }

    @Test
    public void testAddLabel() {
        VarInt x = VarInt.copy(1, 2, 3);
        x.addLabel("10");
        assertEquals(4, x.rowCount());
        assertEquals("10", x.getLabel(3));
    }

    @Test
    public void testSetLabel() {
        VarInt x = VarInt.copy(1, 2, 3);
        x.setLabel(0, "10");
        assertEquals(3, x.rowCount());
        assertEquals("10", x.getLabel(0));
    }

    @Test
    public void testSetDictionary() {
        VarInt x = VarInt.copy(1, 2, 3);
        expected.expect(IllegalArgumentException.class);
        x.setLevels(new String[]{"x"});
    }

    @Test
    public void testBinary() {
        VarInt x = VarInt.empty();
        x.addBoolean(true);
        x.addBoolean(false);
        x.addMissing();
        x.setBoolean(2, true);

        assertEquals(1, x.getInt(0));
        assertEquals(0, x.getInt(1));
        assertEquals(1, x.getInt(2));

        assertEquals(true, x.getBoolean(0));
        assertEquals(false, x.getBoolean(1));
        assertEquals(true, x.getBoolean(2));
    }

    @Test
    public void testStamp() {
        VarInt x = VarInt.empty();
        x.addLong(0);
        x.addMissing();
        x.setLong(1, 100);

        assertEquals(0, x.getLong(0));
        assertEquals(100, x.getLong(1));
    }

    @Test
    public void testRemoveClear() {

        VarInt x = VarInt.copy(1, 3, 6, 7, 9);
        x.remove(0);

        assertEquals(4, x.rowCount());
        assertEquals(3, x.getInt(0));
        assertEquals(9, x.getInt(3));

        x.clear();
        assertEquals(0, x.rowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.remove(-1);
    }

    @Test
    public void testSolidCopy() {

        VarInt x1 = VarInt.copy(1, 2, 3, 4, 5);
        Var x2 = MappedVar.byRows(x1, 0, 1, 2);
        Var x3 = x2.solidCopy();
        Var x4 = x3.solidCopy();
        x4.addDouble(8);

        assertEquals(4, x4.rowCount());
        assertEquals(1, x4.getInt(0));
        assertEquals(3, x4.getInt(2));
        assertEquals(8, x4.getInt(3));
    }

}
