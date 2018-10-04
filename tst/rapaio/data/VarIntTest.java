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
import rapaio.core.stat.Sum;
import rapaio.data.accessor.VarIntDataAccessor;

import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarIntTest {

    private static final double TOL = 1e-20;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void smokeTest() {
        Var index = VarInt.empty(1);
        assertTrue(index.type().isNumeric());
        assertFalse(index.type().isNominal());
        assertEquals(1, index.rowCount());
        assertEquals("VarInt[name:?, rowCount:1]", VarInt.empty(1).toString());
    }

    @Test
    public void invalidRowNumber() {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Illegal row count: -1");
        VarInt.empty(-1);
    }

    @Test
    public void unparsableSetLabel() {
        expected.expect(NumberFormatException.class);
        expected.expectMessage("For input string: \"Test\"");
        VarInt.empty(1).setLabel(0, "Test");
    }

    @Test
    public void unparsableAddLabel() {
        expected.expect(NumberFormatException.class);
        expected.expectMessage("For input string: \"Test\"");
        VarInt.empty(1).addLabel("Test");
    }

    @Test
    public void testNotImplementedLevels() {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("Operation not available for integer variables.");
        VarInt.seq(10).levels();
    }

    @Test
    public void testNotImplementedSetLevels() {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("Operation not available for integer variables.");
        VarInt.seq(10).setLevels(new String[]{"a", "b"});
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

        one.addRows(2);
        one.setInt(2, 10);
        assertEquals(3, one.rowCount());
        assertEquals(10, one.getInt(2));

        one.setLabel(0, "?");
        assertTrue(one.isMissing(0));
    }

    @Test
    public void testBuilders() {
        Var empty1 = VarInt.empty();
        assertEquals(0, empty1.rowCount());
        empty1 = VarInt.empty(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(Integer.MIN_VALUE, empty1.getInt(i));
        }

        Var fill1 = VarInt.fill(10, -1);
        assertEquals(10, fill1.rowCount());
        for (int i = 0; i < fill1.rowCount(); i++) {
            assertEquals(-1, fill1.getInt(i));
        }

        Var seq1 = VarInt.seq(1, 10);
        assertEquals(10, seq1.rowCount());
        for (int i = 0; i < seq1.rowCount(); i++) {
            assertEquals(i + 1, seq1.getInt(i));
        }

        int[] src = new int[]{1, 2, 3, 4};
        VarInt x1 = VarInt.copy(src);
        VarInt x2 = VarInt.wrap(src);
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

        src[2] = 10;
        assertEquals(10, x2.getInt(2));

        VarInt from1 = VarInt.from(x2.rowCount(), x2::getInt);
        assertTrue(from1.deepEquals(x2));

        VarInt collect1 = IntStream.range(0, 100).boxed().collect(VarInt.collector());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, collect1.getInt(i));
        }
        VarInt collect2 = IntStream.range(0, 100).boxed().parallel().collect(VarInt.collector());
        int sum = (int)Sum.of(collect2).value();
        assertEquals(99*100/2, sum);

        VarInt empty3 = collect2.newInstance(10);
        VarInt empty4 = VarInt.empty(10);
        assertTrue(empty3.deepEquals(empty4));
    }

    @Test
    public void testLabels() {
        int[] array = new int[]{1, 2, 3, Integer.MIN_VALUE, 5, 6, Integer.MIN_VALUE};

        VarInt int1 = VarInt.empty();
        for(int val : array) {
            int1.addInt(val);
        }
        for (int i = 0; i < int1.rowCount(); i++) {
            if (array[i] == Integer.MIN_VALUE) {
                assertEquals("?", int1.getLabel(i));
            } else {
                assertEquals(String.valueOf(i + 1), int1.getLabel(i));
            }
        }

        VarInt int2 = VarInt.copy(1, 2, 3);
        int2.addLabel("10");

        assertEquals(4, int2.rowCount());
        assertEquals("1", int2.getLabel(0));
        assertEquals("2", int2.getLabel(1));
        assertEquals("3", int2.getLabel(2));
        assertEquals("10", int2.getLabel(3));

        VarInt x3 = VarInt.copy(1, 2, 3);
        x3.setLabel(0, "10");
        x3.removeRow(1);

        assertEquals(2, x3.rowCount());
        assertEquals("10", x3.getLabel(0));
        assertEquals("3", x3.getLabel(1));

        String[] stringValues = new String[] {"?", "-4", "4", "?"};
        VarInt x4 = VarInt.empty();
        for(String str : stringValues) {
            x4.addLabel(str);
        }

        assertEquals(stringValues.length, x4.rowCount());
        for (int i = 0; i < stringValues.length; i++) {
            assertEquals(stringValues[i], x4.getLabel(i));
        }
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

        assertTrue(x.getBoolean(0));
        assertFalse(x.getBoolean(1));
        assertTrue(x.getBoolean(2));
    }

    @Test
    public void testDouble() {

        double[] values = new double[] {0, 1, Double.NaN, 3, 4, Double.NaN, 6, 7, -8, -100};
        VarInt int1 = VarInt.empty();
        for(double val : values) {
            int1.addDouble(val);
        }

        assertEquals(values.length, int1.rowCount());
        for (int i = 0; i < values.length; i++) {
            if(Double.isNaN(values[i])) {
                assertTrue(int1.isMissing(i));
            } else {
                assertEquals(values[i], int1.getDouble(i), TOL);
            }
        }

        for (int i = 0; i < int1.rowCount(); i++) {
            int1.setDouble(i, values[i]);
        }
        assertEquals(values.length, int1.rowCount());
        for (int i = 0; i < values.length; i++) {
            if(Double.isNaN(values[i])) {
                assertTrue(int1.isMissing(i));
                assertEquals(Double.NaN, int1.getDouble(i), TOL);
            } else {
                assertEquals(values[i], int1.getDouble(i), TOL);
            }
        }
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
        x.removeRow(0);

        assertEquals(4, x.rowCount());
        assertEquals(3, x.getInt(0));
        assertEquals(9, x.getInt(3));

        x.clearRows();
        assertEquals(0, x.rowCount());


        expected.expect(IndexOutOfBoundsException.class);
        x.removeRow(-1);
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

    @Test
    public void testDataAccessor() {
        VarInt int1 = VarInt.seq(0, 100, 2);
        VarIntDataAccessor acc = int1.getDataAccessor();
        assertEquals(Integer.MIN_VALUE, acc.getMissingValue());
        assertEquals(int1.rowCount(), acc.getRowCount());
        for (int i = 0; i < int1.rowCount(); i++) {
            assertEquals(int1.getInt(i), acc.getData()[i]);
        }
        int[] values = new int[] {0, 1, Integer.MIN_VALUE, 3, 4};
        acc.setData(values);
        acc.setRowCount(values.length);

        assertTrue(VarInt.wrap(values).deepEquals(int1));
    }
}
