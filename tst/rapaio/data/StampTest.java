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
        Var stamp = StampVar.empty(1);
        assertFalse(stamp.getType().isNumeric());
        assertFalse(stamp.getType().isNominal());

        try {
            stamp.getLevels();
            assertTrue(false);
        } catch (RuntimeException ex) {
            assertTrue(true);
        }

        try {
            StampVar.empty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        stamp.addIndex(1);
        assertEquals(2, stamp.getRowCount());
        assertEquals(true, stamp.isMissing(0));
        assertEquals(1, stamp.getStamp(1));

        try {
            StampVar.copy(10).getBinary(0);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("Stamp[1]", StampVar.empty(1).toString());
    }

    @Test
    public void testEmptyStamp() {
        Var stamp = StampVar.empty();
        assertEquals(0, stamp.getRowCount());

        stamp = StampVar.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(StampVar.MISSING_VALUE, stamp.getStamp(i));
        }
    }

    @Test
    public void testFillVector() {
        Var stamp = NumericVar.fill(10, -1);
        assertEquals(10, stamp.getRowCount());
        for (int i = 0; i < stamp.getRowCount(); i++) {
            assertEquals(-1, stamp.getStamp(i));
        }
    }

    @Test
    public void testSequenceVector() {
        Var stamp = StampVar.seq(10000000000L, 10);
        assertEquals(10, stamp.getRowCount());
        for (int i = 0; i < stamp.getRowCount(); i++) {
            assertEquals(i + 10000000000L, stamp.getStamp(i));
        }
    }

    @Test
    public void testSetterGetter() {

        Var stamp = StampVar.fill(3, 0);

        assertEquals(0, stamp.getStamp(0));
        stamp.setIndex(0, 1);
        stamp.setIndex(1, 3);

        assertEquals(1, stamp.getStamp(0));
        assertEquals(3, stamp.getStamp(1));

        assertEquals(1., stamp.getValue(0), 1e-10);
        assertEquals(3., stamp.getValue(1), 1e-10);

        stamp.setValue(0, 2.5);
        stamp.setValue(1, 7.8);
        stamp.setValue(2, 2.51);

        assertEquals(2, stamp.getStamp(0));
        assertEquals(2., stamp.getValue(0), 1e-10);
        assertEquals(8, stamp.getStamp(1));
        assertEquals(8., stamp.getValue(1), 1e-10);
        assertEquals(3, stamp.getStamp(2));
        assertEquals(3., stamp.getValue(2), 1e-10);

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
        Var stamp = StampVar.seq(1, 10, 1);
        for (int i = 0; i < stamp.getRowCount(); i++) {
            assertTrue(!stamp.isMissing(i));
        }
        for (int i = 0; i < stamp.getRowCount(); i++) {
            if (i % 2 == 0)
                stamp.setMissing(i);
        }
        for (int i = 0; i < stamp.getRowCount(); i++) {
            assertEquals(i % 2 == 0, stamp.isMissing(i));
        }
    }

    @Test
    public void testOneStamp() {
        Var one = StampVar.scalar(2);
        assertEquals(1, one.getRowCount());
        assertEquals(2, one.getStamp(0));

        one = StampVar.scalar(3);
        assertEquals(1, one.getRowCount());
        assertEquals(3, one.getStamp(0));
    }

    @Test
    public void testBuilders() {
        StampVar x1 = StampVar.copy(1L, 2L, 3L, 4L);
        long[] wrap = new long[]{1, 2, 3, 4};
        StampVar x2 = StampVar.wrap(wrap);
        StampVar x3 = StampVar.seq(4);
        StampVar x4 = StampVar.seq(1, 4);
        StampVar x5 = StampVar.seq(1, 4, 2);
        StampVar x6 = StampVar.empty();
        x6.addStamp(1);
        x6.addStamp(2);
        x6.addStamp(3);
        x6.addStamp(4);

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, x1.getStamp(i));
            assertEquals(i + 1, x2.getStamp(i));
            assertEquals(i, x3.getStamp(i));
            assertEquals(i + 1, x4.getStamp(i));
            assertEquals(i * 2 + 1, x5.getStamp(i));
            assertEquals(i + 1, x6.getStamp(i));
        }

        wrap[2] = 10;

        assertEquals(10, x2.getStamp(2));
    }

    @Test
    public void testLabel() {
        StampVar x = StampVar.copy(1, 2, 3);
        assertEquals("1", x.getLabel(0));
    }

    @Test
    public void testAddLabel() {
        StampVar x = StampVar.copy(1, 2, 3);
        x.addLabel("10");
        assertEquals(4, x.getRowCount());
        assertEquals("1", x.getLabel(0));
        assertEquals("10", x.getLabel(3));
    }

    @Test
    public void testSetLabel() {
        StampVar x = StampVar.copy(1, 2, 3);
        x.setLabel(0, "10");
        assertEquals(3, x.getRowCount());
        assertEquals("10", x.getLabel(0));
    }

    @Test
    public void testSetDictionary() {
        StampVar x = StampVar.copy(1, 2, 3);
        expected.expect(IllegalArgumentException.class);
        x.setLevels(new String[]{"x"});
    }

    @Test
    public void testBinary() {
        StampVar x = StampVar.empty();
        x.addBinary(true);
        x.addBinary(false);
        x.addMissing();
        x.setBinary(2, true);

        assertEquals(1, x.getStamp(0));
        assertEquals(0, x.getStamp(1));
        assertEquals(1, x.getStamp(2));

        assertEquals(true, x.getBinary(0));
        assertEquals(false, x.getBinary(1));
        assertEquals(true, x.getBinary(2));
    }

    @Test
    public void testStamp() {
        StampVar x = StampVar.empty();
        x.addStamp(0);
        x.addMissing();
        x.setStamp(1, 100);

        assertEquals(0, x.getStamp(0));
        assertEquals(100, x.getStamp(1));
    }

    @Test
    public void testRemoveClear() {

        StampVar x = StampVar.copy(1, 3, 6, 7, 9);
        x.remove(0);

        assertEquals(4, x.getRowCount());
        assertEquals(3, x.getStamp(0));
        assertEquals(9, x.getStamp(3));

        x.clear();
        assertEquals(0, x.getRowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.remove(-1);
    }

    @Test
    public void testSolidCopy() {

        StampVar x1 = StampVar.copy(1, 2, 3, 4, 5);
        Var x2 = MappedVar.byRows(x1, 0, 1, 2);
        Var x3 = x2.solidCopy();
        Var x4 = x3.solidCopy();
        x4.addValue(8);

        assertEquals(4, x4.getRowCount());
        assertEquals(1, x4.getStamp(0));
        assertEquals(3, x4.getStamp(2));
        assertEquals(8, x4.getStamp(3));
    }

}
