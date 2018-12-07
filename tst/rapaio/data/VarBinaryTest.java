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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.*;
import rapaio.core.stat.Mean;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class VarBinaryTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testEmpty() {
        VarBinary b = VarBinary.empty();
        b.addInt(1);
        b.addInt(1);
        b.addInt(0);
        b.addMissing();
        b.addMissing();
        b.addInt(1);

        assertEquals(1, b.stream().complete().filter(s -> s.getInt() != 1).count());
        assertEquals(3, b.stream().complete().filter(s -> s.getInt() == 1).count());
        assertEquals(2, b.stream().incomplete().count());

        assertEquals(10, VarBinary.empty(10).stream().incomplete().count());
        assertEquals(0, VarBinary.empty().stream().incomplete().count());
    }

    @Test
    public void testFill() {
        VarBinary b = VarBinary.fill(10, 0);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(10, b.stream().complete().filter(s -> s.getInt() != 1).count());
        assertEquals(0, b.stream().complete().filter(s -> s.getInt() == 1).count());

        b = VarBinary.fill(10, 1);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(0, b.stream().complete().filter(s -> s.getInt() != 1).count());
        assertEquals(10, b.stream().complete().filter(s -> s.getInt() == 1).count());
    }

    @Test
    public void testNumericStats() {
        VarBinary b = VarBinary.copy(1, 1, 0, 0, 1, 0, 1, 1);
        b.printSummary();
        assertEquals(0.625, Mean.of(b).value(), 10e-10);
    }

    @Test
    public void testMissingValues() {
        VarBinary bin = VarBinary.copy(1, 0, 1, 0, -1, -1, 1, 0);
        assertEquals(8, bin.rowCount());
        assertTrue(bin.isMissing(4));
        assertFalse(bin.isMissing(7));

        bin = VarBinary.empty();
        bin.addMissing();
        bin.addInt(1);
        bin.setMissing(1);

        assertEquals(2, bin.rowCount());
        assertTrue(bin.isMissing(0));
        assertTrue(bin.isMissing(1));
    }

    @Test
    public void testBuilders() {
        VarBinary bin = VarBinary.copy(1, 1, 0, 0);
        assertEquals(4, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(3));

        VarBinary bin2 = VarBinary.fromIndex(100, i -> i % 3 == 0 ? -1 : i % 3 == 1 ? 0 : 1);
        for (int i = 0; i < bin2.rowCount(); i++) {
            switch (i % 3) {
                case 0:
                    assertTrue(bin2.isMissing(i));
                    break;
                case 1:
                    assertEquals(0, bin2.getInt(i));
                    break;
                default:
                    assertEquals(1, bin2.getInt(i));
            }
        }

        Boolean[] array = new Boolean[100];
        for (int i = 0; i < array.length; i++) {
            switch (i % 3) {
                case 0:
                    array[i] = null;
                    break;
                case 1:
                    array[i] = false;
                    break;
                default:
                    array[i] = true;
            }
        }

        VarBinary bin4 = VarBinary.from(100, row -> array[row]);
        assertTrue(bin2.deepEquals(bin4));

        assertTrue(VarBinary.empty(10).deepEquals(bin4.newInstance(10)));
    }

    @Test
    public void testOther() {
        VarBinary bin = VarBinary.empty();
        bin.addDouble(1);
        bin.setDouble(0, 0);
        bin.addInt(1);
        bin.setInt(1, 0);

        assertEquals(0, bin.getDouble(0), 10e-10);
        assertEquals(0, bin.getInt(1));

        VarBinary copy = bin.solidCopy();
        assertEquals(0, copy.getInt(0));
        assertEquals(0, copy.getInt(1));
        assertEquals(2, copy.rowCount());

        copy.removeRow(0);
        assertEquals(1, copy.rowCount());
        assertEquals(0, copy.getInt(0));

        copy.clearRows();
        assertEquals(0, copy.rowCount());

        copy.removeRow(10);

        VarBinary bin1 = VarBinary.fill(10, 1);
        bin1.addRows(10);
        assertEquals(20, bin1.rowCount());
        for (int i = 0; i < 10; i++) {
            assertEquals(1, bin1.getInt(i));
            assertTrue(bin1.isMissing(i + 10));
        }
    }

    @Test
    public void testDouble() {

        VarBinary bin = VarBinary.empty();
        bin.addDouble(1);
        bin.addDouble(0);
        bin.addDouble(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));

        bin.setDouble(0, -1);
        bin.setDouble(1, 0);
        bin.setDouble(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));
    }

    @Test
    public void testInt() {

        VarBinary bin = VarBinary.empty();
        bin.addInt(1);
        bin.addInt(0);
        bin.addInt(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));
        assertEquals(Integer.MIN_VALUE, bin.getInt(2));

        bin.setInt(0, -1);
        bin.setInt(1, 0);
        bin.setInt(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));
    }

    @Test
    public void testLong() {

        VarBinary bin = VarBinary.empty();
        bin.addLong(1);
        bin.addLong(0);
        bin.addLong(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(1, bin.getInt(0));
        assertEquals(0, bin.getInt(1));
        assertTrue(bin.isMissing(2));

        bin.setLong(0, -1);
        bin.setLong(1, 0);
        bin.setLong(2, 1);

        assertTrue(bin.isMissing(0));
        assertEquals(0, bin.getInt(1));
        assertEquals(1, bin.getInt(2));

        assertEquals(Long.MIN_VALUE, bin.getLong(0));
    }

    @Test
    public void testAddInvalidLabel() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The value x could not be converted to a binary value");
        VarBinary.empty().addLabel("x");
    }

    @Test
    public void testSetInvalidLabel() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The value x could not be converted to a binary value");
        VarBinary.empty(0).setLabel(0, "x");
    }

    @Test
    public void testLabel() {

        String[] labels = new String[]{"1", "0", "1", "?"};

        VarBinary bin = VarBinary.empty();
        for (String label : labels) {
            bin.addLabel(label);
        }

        assertEquals(labels.length, bin.rowCount());
        for (int i = 0; i < labels.length; i++) {
            assertEquals(labels[i], bin.getLabel(i));
        }

        for (int i = 0; i < labels.length; i++) {
            bin.setLabel(i, labels[i]);
        }

        for (int i = 0; i < labels.length; i++) {
            assertEquals(labels[i], bin.getLabel(i));
        }

        List<String> levels = VarBinary.empty().levels();
        assertEquals(3, levels.size());
        assertEquals("?", levels.get(0));
        assertEquals("true", levels.get(1));
        assertEquals("false", levels.get(2));
    }

    @Test
    public void testIllegalSetLevels() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Operation not implemented on binary variables");
        VarBinary.empty().setLevels("?", "1");
    }

    @Test
    public void testPrint() {
        VarBinary var = VarBinary.copy(IntStream.range(0, 100).map(x -> RandomSource.nextInt(3) - 1).toArray()).withName("x");
        assertEquals("VarBinary [name: \"x\", rowCount: 100, values: 1, 1, 1, 1, ?, 1, ?, 0, 1, 0, 1, ?, 1, 1, 0, ?, ..., ?, 0]", var.toString());
    }
}
