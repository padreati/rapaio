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
        Var stamp = Stamp.empty(1);
        assertFalse(stamp.type().isNumeric());
        assertFalse(stamp.type().isNominal());

        try {
            stamp.levels();
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }

        try {
            Stamp.empty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        stamp.addIndex(1);
        assertEquals(2, stamp.rowCount());
        assertEquals(true, stamp.missing(0));
        assertEquals(1, stamp.stamp(1));

        try {
            Stamp.copy(10).binary(0);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("Stamp[1]", Stamp.empty(1).toString());
    }

    @Test
    public void testEmptyStamp() {
        Var stamp = Stamp.empty();
        assertEquals(0, stamp.rowCount());

        stamp = Stamp.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(Stamp.MISSING_VALUE, stamp.stamp(i));
        }
    }

    @Test
    public void testFillVector() {
        Var stamp = Numeric.newFill(10, -1);
        assertEquals(10, stamp.rowCount());
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertEquals(-1, stamp.stamp(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Var stamp = Stamp.seq(10000000000L, 10);
        assertEquals(10, stamp.rowCount());
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertEquals(i + 10000000000L, stamp.stamp(i));
        }
    }

    @Test
    public void testSetterGetter() {

        Var stamp = Stamp.fill(3, 0);

        assertEquals(0, stamp.stamp(0));
        stamp.setIndex(0, 1);
        stamp.setIndex(1, 3);

        assertEquals(1, stamp.stamp(0));
        assertEquals(3, stamp.stamp(1));

        assertEquals(1., stamp.value(0), 1e-10);
        assertEquals(3., stamp.value(1), 1e-10);

        stamp.setValue(0, 2.5);
        stamp.setValue(1, 7.8);
        stamp.setValue(2, 2.51);

        assertEquals(2, stamp.stamp(0));
        assertEquals(2., stamp.value(0), 1e-10);
        assertEquals(8, stamp.stamp(1));
        assertEquals(8., stamp.value(1), 1e-10);
        assertEquals(3, stamp.stamp(2));
        assertEquals(3., stamp.value(2), 1e-10);

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
        Var stamp = Stamp.seq(1, 10, 1);
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertTrue(!stamp.missing(i));
        }
        for (int i = 0; i < stamp.rowCount(); i++) {
            if (i % 2 == 0)
                stamp.setMissing(i);
        }
        for (int i = 0; i < stamp.rowCount(); i++) {
            assertEquals(i % 2 == 0, stamp.missing(i));
        }
    }

    @Test
    public void testOneStamp() {
        Var one = Stamp.scalar(2);
        assertEquals(1, one.rowCount());
        assertEquals(2, one.stamp(0));

        one = Stamp.scalar(3);
        assertEquals(1, one.rowCount());
        assertEquals(3, one.stamp(0));
    }

    @Test
    public void testBuilders() {
        Stamp x1 = Stamp.copy(1L, 2L, 3L, 4L);
        long[] wrap = new long[]{1, 2, 3, 4};
        Stamp x2 = Stamp.wrap(wrap);
        Stamp x3 = Stamp.seq(4);
        Stamp x4 = Stamp.seq(1, 4);
        Stamp x5 = Stamp.seq(1, 4, 2);
        Stamp x6 = Stamp.empty();
        x6.addStamp(1);
        x6.addStamp(2);
        x6.addStamp(3);
        x6.addStamp(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.stamp(i));
            assertEquals(i + 1, x2.stamp(i));
            assertEquals(i, x3.stamp(i));
            assertEquals(i + 1, x4.stamp(i));
            assertEquals(i * 2 + 1, x5.stamp(i));
            assertEquals(i + 1, x6.stamp(i));
        }

        wrap[2] = 10;

        assertEquals(10, x2.stamp(2));
    }

    @Test
    public void testLabel() {
        Stamp x = Stamp.copy(1, 2, 3);
        assertEquals("1", x.label(0));
    }

    @Test
    public void testAddLabel() {
        Stamp x = Stamp.copy(1, 2, 3);
        x.addLabel("10");
        assertEquals(4, x.rowCount());
        assertEquals("1", x.label(0));
        assertEquals("10", x.label(3));
    }

    @Test
    public void testSetLabel() {
        Stamp x = Stamp.copy(1, 2, 3);
        x.setLabel(0, "10");
        assertEquals(3, x.rowCount());
        assertEquals("10", x.label(0));
    }

    @Test
    public void testSetDictionary() {
        Stamp x = Stamp.copy(1, 2, 3);
        expected.expect(IllegalArgumentException.class);
        x.setLevels(new String[]{"x"});
    }

    @Test
    public void testBinary() {
        Stamp x = Stamp.empty();
        x.addBinary(true);
        x.addBinary(false);
        x.addMissing();
        x.setBinary(2, true);

        assertEquals(1, x.stamp(0));
        assertEquals(0, x.stamp(1));
        assertEquals(1, x.stamp(2));

        assertEquals(true, x.binary(0));
        assertEquals(false, x.binary(1));
        assertEquals(true, x.binary(2));
    }

    @Test
    public void testStamp() {
        Stamp x = Stamp.empty();
        x.addStamp(0);
        x.addMissing();
        x.setStamp(1, 100);

        assertEquals(0, x.stamp(0));
        assertEquals(100, x.stamp(1));
    }

    @Test
    public void testRemoveClear() {

        Stamp x = Stamp.copy(1, 3, 6, 7, 9);
        x.remove(0);

        assertEquals(4, x.rowCount());
        assertEquals(3, x.stamp(0));
        assertEquals(9, x.stamp(3));

        x.clear();
        assertEquals(0, x.rowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.remove(-1);
    }

    @Test
    public void testSolidCopy() {

        Stamp x1 = Stamp.copy(1, 2, 3, 4, 5);
        Var x2 = MappedVar.newByRows(x1, 0, 1, 2);
        Var x3 = x2.solidCopy();
        Var x4 = x3.solidCopy();
        x4.addValue(8);

        assertEquals(4, x4.rowCount());
        assertEquals(1, x4.stamp(0));
        assertEquals(3, x4.stamp(2));
        assertEquals(8, x4.stamp(3));
    }

}
