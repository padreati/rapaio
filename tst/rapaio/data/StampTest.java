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
public class StampTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void smokeTest() {
        Var stamp = VarLong.empty(1);
        assertFalse(stamp.type().isNumeric());
        assertFalse(stamp.type().isNominal());

        try {
            stamp.levels();
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }

        try {
            VarLong.empty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        stamp.addInt(1);
        assertEquals(2, stamp.rowCount());
        assertEquals(true, stamp.isMissing(0));
        assertEquals(1, stamp.getLong(1));

        try {
            VarLong.copy(10).getBoolean(0);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("Stamp[1]", VarLong.empty(1).toString());
    }

    @Test
    public void testEmptyStamp() {
        Var stamp = VarLong.empty();
        assertEquals(0, stamp.rowCount());

        stamp = VarLong.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(VarLong.MISSING_VALUE, stamp.getLong(i));
        }
    }

    @Test
    public void testFillVector() {
        Var stamp = VarDouble.fill(10, -1);
        assertEquals(10, stamp.rowCount());
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertEquals(-1, stamp.getLong(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Var stamp = VarLong.seq(10000000000L, 10);
        assertEquals(10, stamp.rowCount());
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertEquals(i + 10000000000L, stamp.getLong(i));
        }
    }

    @Test
    public void testSetterGetter() {

        Var stamp = VarLong.fill(3, 0);

        assertEquals(0, stamp.getLong(0));
        stamp.setInt(0, 1);
        stamp.setInt(1, 3);

        assertEquals(1, stamp.getLong(0));
        assertEquals(3, stamp.getLong(1));

        assertEquals(1., stamp.getDouble(0), 1e-10);
        assertEquals(3., stamp.getDouble(1), 1e-10);

        stamp.setDouble(0, 2.5);
        stamp.setDouble(1, 7.8);
        stamp.setDouble(2, 2.51);

        assertEquals(2, stamp.getLong(0));
        assertEquals(2., stamp.getDouble(0), 1e-10);
        assertEquals(8, stamp.getLong(1));
        assertEquals(8., stamp.getDouble(1), 1e-10);
        assertEquals(3, stamp.getLong(2));
        assertEquals(3., stamp.getDouble(2), 1e-10);

        boolean exceptional = false;
        try {
            stamp.setLabel(0, "Test");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);
    }

    @Test
    public void testMissing() {
        Var stamp = VarLong.seq(1, 10, 1);
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertTrue(!stamp.isMissing(i));
        }
        for (int i = 0; i < stamp.rowCount(); i++) {
            if (i % 2 == 0)
                stamp.setMissing(i);
        }
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertEquals(i % 2 == 0, stamp.isMissing(i));
        }
    }

    @Test
    public void testOneStamp() {
        Var one = VarLong.scalar(2);
        assertEquals(1, one.rowCount());
        assertEquals(2, one.getLong(0));

        one = VarLong.scalar(3);
        assertEquals(1, one.rowCount());
        assertEquals(3, one.getLong(0));
    }

    @Test
    public void testBuilders() {
        VarLong x1 = VarLong.copy(1L, 2L, 3L, 4L);
        long[] wrap = new long[]{1, 2, 3, 4};
        VarLong x2 = VarLong.wrap(wrap);
        VarLong x3 = VarLong.seq(4);
        VarLong x4 = VarLong.seq(1, 4);
        VarLong x5 = VarLong.seq(1, 4, 2);
        VarLong x6 = VarLong.empty();
        x6.addLong(1);
        x6.addLong(2);
        x6.addLong(3);
        x6.addLong(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.getLong(i));
            assertEquals(i + 1, x2.getLong(i));
            assertEquals(i, x3.getLong(i));
            assertEquals(i + 1, x4.getLong(i));
            assertEquals(i * 2 + 1, x5.getLong(i));
            assertEquals(i + 1, x6.getLong(i));
        }

        wrap[2] = 10;

        assertEquals(10, x2.getLong(2));
    }

    @Test
    public void testLabel() {
        VarLong x = VarLong.copy(1, 2, 3);
        assertEquals("1", x.getLabel(0));
    }

    @Test
    public void testAddLabel() {
        VarLong x = VarLong.copy(1, 2, 3);
        x.addLabel("10");
        assertEquals(4, x.rowCount());
        assertEquals("1", x.getLabel(0));
        assertEquals("10", x.getLabel(3));
    }

    @Test
    public void testSetLabel() {
        VarLong x = VarLong.copy(1, 2, 3);
        x.setLabel(0, "10");
        assertEquals(3, x.rowCount());
        assertEquals("10", x.getLabel(0));
    }

    @Test
    public void testSetDictionary() {
        VarLong x = VarLong.copy(1, 2, 3);
        expected.expect(IllegalArgumentException.class);
        x.setLevels(new String[]{"x"});
    }

    @Test
    public void testBinary() {
        VarLong x = VarLong.empty();
        x.addBoolean(true);
        x.addBoolean(false);
        x.addMissing();
        x.setBoolean(2, true);

        assertEquals(1, x.getLong(0));
        assertEquals(0, x.getLong(1));
        assertEquals(1, x.getLong(2));

        assertEquals(true, x.getBoolean(0));
        assertEquals(false, x.getBoolean(1));
        assertEquals(true, x.getBoolean(2));
    }

    @Test
    public void testStamp() {
        VarLong x = VarLong.empty();
        x.addLong(0);
        x.addMissing();
        x.setLong(1, 100);

        assertEquals(0, x.getLong(0));
        assertEquals(100, x.getLong(1));
    }

    @Test
    public void testRemoveClear() {

        VarLong x = VarLong.copy(1, 3, 6, 7, 9);
        x.remove(0);

        assertEquals(4, x.rowCount());
        assertEquals(3, x.getLong(0));
        assertEquals(9, x.getLong(3));

        x.clear();
        assertEquals(0, x.rowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.remove(-1);
    }

    @Test
    public void testSolidCopy() {

        VarLong x1 = VarLong.copy(1, 2, 3, 4, 5);
        Var x2 = MappedVar.byRows(x1, 0, 1, 2);
        Var x3 = x2.solidCopy();
        Var x4 = x3.solidCopy();
        x4.addDouble(8);

        assertEquals(4, x4.rowCount());
        assertEquals(1, x4.getLong(0));
        assertEquals(3, x4.getLong(2));
        assertEquals(8, x4.getLong(3));
    }

}
