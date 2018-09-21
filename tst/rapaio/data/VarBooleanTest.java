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
import rapaio.core.stat.Mean;
import rapaio.data.stream.VSpot;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class VarBooleanTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmpty() {
        VarBoolean b = VarBoolean.empty();
        b.addBoolean(true);
        b.addBoolean(true);
        b.addBoolean(false);
        b.addMissing();
        b.addMissing();
        b.addBoolean(true);

        assertEquals(1, b.stream().complete().filter(s -> !s.getBoolean()).count());
        assertEquals(3, b.stream().complete().filter(VSpot::getBoolean).count());
        assertEquals(2, b.stream().incomplete().count());

        assertEquals(10, VarBoolean.empty(10).stream().incomplete().count());
        assertEquals(0, VarBoolean.empty().stream().incomplete().count());
    }

    @Test
    public void testFill() {
        VarBoolean b = VarBoolean.fill(10, false);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(10, b.stream().complete().filter(s -> !s.getBoolean()).count());
        assertEquals(0, b.stream().complete().filter(VSpot::getBoolean).count());

        b = VarBoolean.fill(10, true);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(0, b.stream().complete().filter(s -> !s.getBoolean()).count());
        assertEquals(10, b.stream().complete().filter(VSpot::getBoolean).count());
    }

    @Test
    public void testNumericStats() {
        VarBoolean b = VarBoolean.copy(1, 1, 0, 0, 1, 0, 1, 1);
        b.printSummary();
        assertEquals(0.625, Mean.from(b).value(), 10e-10);
    }

    @Test
    public void testMissingValues() {
        VarBoolean bin = VarBoolean.copy(1, 0, 1, 0, -1, -1, 1, 0);
        assertEquals(8, bin.rowCount());
        assertTrue(bin.isMissing(4));
        assertFalse(bin.isMissing(7));

        bin = VarBoolean.empty();
        bin.addMissing();
        bin.addBoolean(true);
        bin.setMissing(1);

        assertEquals(2, bin.rowCount());
        assertTrue(bin.isMissing(0));
        assertTrue(bin.isMissing(1));
    }

    @Test
    public void testBuilders() {
        VarBoolean bin = VarBoolean.copy(true, true, false, false);
        assertEquals(4, bin.rowCount());
        assertTrue(bin.getBoolean(0));
        assertFalse(bin.getBoolean(3));

        VarBoolean bin2 = VarBoolean.fromIndex(100, i -> i % 3 == 0 ? -1 : i % 3 == 1 ? 0 : 1);
        for (int i = 0; i < bin2.rowCount(); i++) {
            switch (i % 3) {
                case 0:
                    assertTrue(bin2.isMissing(i));
                    break;
                case 1:
                    assertFalse(bin2.getBoolean(i));
                    break;
                default:
                    assertTrue(bin2.getBoolean(i));
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

        VarBoolean bin4 = VarBoolean.from(100, row -> array[row]);
        assertTrue(bin2.deepEquals(bin4));

        assertTrue(VarBoolean.empty(10).deepEquals(bin4.newInstance(10)));
    }

    @Test
    public void testOther() {
        VarBoolean bin = VarBoolean.empty();
        bin.addDouble(1);
        bin.setDouble(0, 0);
        bin.addInt(1);
        bin.setInt(1, 0);

        assertEquals(0, bin.getDouble(0), 10e-10);
        assertEquals(0, bin.getInt(1));

        VarBoolean copy = bin.solidCopy();
        assertFalse(copy.getBoolean(0));
        assertFalse(copy.getBoolean(1));
        assertEquals(2, copy.rowCount());

        copy.removeRow(0);
        assertEquals(1, copy.rowCount());
        assertFalse(copy.getBoolean(0));

        copy.clearRows();
        assertEquals(0, copy.rowCount());

        copy.removeRow(10);

        VarBoolean bin1 = VarBoolean.fill(10, true);
        bin1.addRows(10);
        assertEquals(20, bin1.rowCount());
        for (int i = 0; i < 10; i++) {
            assertTrue(bin1.getBoolean(i));
            assertTrue(bin1.isMissing(i+10));
        }
    }

    @Test
    public void testAddInvalidDouble() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value 2.000000 is not a valid binary value");
        VarBoolean.empty().addDouble(2);
    }

    @Test
    public void testSetInvalidDouble() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value 2.000000 is not a valid binary value");
        VarBoolean.empty(1).setDouble(0, 2);
    }

    @Test
    public void testDouble() {

        VarBoolean bin = VarBoolean.empty();
        bin.addDouble(1);
        bin.addDouble(0);
        bin.addDouble(-1);

        assertEquals(3, bin.rowCount());
        assertTrue(bin.getBoolean(0));
        assertFalse(bin.getBoolean(1));
        assertTrue(bin.isMissing(2));

        bin.setDouble(0, -1);
        bin.setDouble(1, 0);
        bin.setDouble(2, 1);

        assertTrue(bin.isMissing(0));
        assertFalse(bin.getBoolean(1));
        assertTrue(bin.getBoolean(2));
    }

    @Test
    public void testAddInvalidInt() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value 2 is not a valid binary value");
        VarBoolean.empty().addInt(2);
    }

    @Test
    public void testSetInvalidInt() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Value 2 is not a valid binary value");
        VarBoolean.empty(1).setInt(0, 2);
    }

    @Test
    public void testInt() {

        VarBoolean bin = VarBoolean.empty();
        bin.addInt(1);
        bin.addInt(0);
        bin.addInt(-1);

        assertEquals(3, bin.rowCount());
        assertTrue(bin.getBoolean(0));
        assertFalse(bin.getBoolean(1));
        assertTrue(bin.isMissing(2));
        assertEquals(-1, bin.getInt(2));

        bin.setInt(0, -1);
        bin.setInt(1, 0);
        bin.setInt(2, 1);

        assertTrue(bin.isMissing(0));
        assertFalse(bin.getBoolean(1));
        assertTrue(bin.getBoolean(2));
    }

    @Test
    public void testAddInvalidLong() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("This value 2 is not a valid binary value");
        VarBoolean.empty().addLong(2);
    }

    @Test
    public void testSetInvalidLong() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("This value 2 is not a valid binary value");
        VarBoolean.empty(0).setLong(0, 2);
    }

    @Test
    public void testLong() {

        VarBoolean bin = VarBoolean.empty();
        bin.addLong(1);
        bin.addLong(0);
        bin.addLong(-1);

        assertEquals(3, bin.rowCount());
        assertTrue(bin.getBoolean(0));
        assertFalse(bin.getBoolean(1));
        assertTrue(bin.isMissing(2));

        bin.setLong(0, -1);
        bin.setLong(1, 0);
        bin.setLong(2, 1);

        assertTrue(bin.isMissing(0));
        assertFalse(bin.getBoolean(1));
        assertTrue(bin.getBoolean(2));

        assertEquals(1L, bin.getLong(0));
    }

    @Test
    public void testAddInvalidLabel() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The value x could not be converted to a binary value");
        VarBoolean.empty().addLabel("x");
    }

    @Test
    public void testSetInvalidLabel() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The value x could not be converted to a binary value");
        VarBoolean.empty(0).setLabel(0, "x");
    }

    @Test
    public void testLabel() {

        String[] labels = new String[] {"true","false","true","?"};

        VarBoolean bin = VarBoolean.empty();
        for(String label : labels) {
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

        List<String> levels = VarBoolean.empty().levels();
        assertEquals(3, levels.size());
        assertEquals("?", levels.get(0));
        assertEquals("true", levels.get(1));
        assertEquals("false", levels.get(2));
    }

    @Test
    public void testIllegalSetLevels() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Operation not implemented on binary variables");
        VarBoolean.empty().setLevels("?", "1");
    }
}
