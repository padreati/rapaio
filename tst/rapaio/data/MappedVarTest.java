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

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class MappedVarTest {

    private static final double TOL = 1e-12;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBuilders() {
        Var a = VarDouble.wrap(1, 2, 3, 4, 5, 6).mapRows(0, 1, 2, 3).mapRows(2, 3);
        assertEquals(2, a.rowCount());
        assertEquals(3, a.getDouble(0), TOL);
        assertEquals(4, a.getDouble(1), TOL);

        Var b = a.bindRows(VarDouble.wrap(10, 11));
        assertEquals(4, b.rowCount());
        assertEquals(3, b.getDouble(0), TOL);
        assertEquals(10, b.getDouble(2), TOL);

        Var seq = VarInt.seq(100);
        Var mapSeq = MappedVar.byRows(seq, 1, 2, 3);
        assertEquals(3, mapSeq.rowCount());
        for (int i = 0; i < mapSeq.rowCount(); i++) {
            assertEquals(i+1, mapSeq.getInt(i));
        }
    }

    @Test
    public void testSource() {
        VarDouble x = VarDouble.seq(0, 100);
        MappedVar map = x.mapRows(Mapping.range(10));

        assertNotEquals(x.rowCount(), map.rowCount());
        assertTrue(x.deepEquals(map.getSource()));
    }

    @Test
    public void testInvalidAddRows() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarDouble.scalar(1).mapRows(0).addRows(10);
    }

    @Test
    public void testInvalidAddDouble() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarDouble.scalar(1).mapRows(0).addDouble(0);
    }

    @Test
    public void testInvalidAddInt() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarDouble.scalar(1).mapRows(0).addInt(0);
    }

    @Test
    public void testInvalidAddNominal() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarNominal.copy("1").mapRows(0).addLabel("0");
    }

    @Test
    public void testInvalidAddLong() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarLong.scalar(1).mapRows(0).addLong(0);
    }

    @Test
    public void testInvalidAddBoolean() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarBinary.fill(1, 1).mapRows(0).addInt(1);
    }

    @Test
    public void testInvalidAddMissing() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Operation not available on mapped vectors");
        VarBinary.fill(1, 1).mapRows(0).addMissing();
    }

    @Test
    public void testMappedNominal() {
        Var x = VarNominal.copy("a").mapRows(0);
        x.setLevels("x");
        assertEquals("x", x.getLabel(0));

        x.setLabel(0, "y");
        assertEquals("y", x.getLabel(0));

        List<String> levels = x.levels();
        assertEquals("?", levels.get(0));
        assertEquals("x", levels.get(1));
        assertEquals("y", levels.get(2));
    }

    @Test
    public void testMappedBinary() {
        Var x = VarBinary.copy(1, 0, 1).mapRows(0, 2);
        assertEquals(2, x.rowCount());
        assertEquals(1, x.getInt(0));
        assertEquals(1, x.getInt(1));

        x.setInt(1, 0);
        assertEquals(1, x.getInt(0));
        assertEquals(0, x.getInt(1));
    }

    @Test
    public void testMappedLong() {
        Var x = VarLong.copy(100, 200).mapRows(0, 1);
        assertEquals(2, x.rowCount());
        assertEquals(100, x.getLong(0));
        assertEquals(200, x.getLong(1));

        x.setLong(1, 250);
        assertEquals(250, x.getLong(1));
    }

    @Test
    public void testMappedDouble() {
        VarDouble x = VarDouble.seq(10);
        MappedVar y = x.mapRows(1, 2, 3);
        y.setDouble(0, 10);
        y.setMissing(1);

        assertEquals(10, x.getDouble(1), TOL);
        assertTrue(x.isMissing(2));
        assertEquals(3, x.getDouble(3), TOL);
    }

    @Test
    public void testMappedInt() {
        VarInt x = VarInt.seq(10);
        MappedVar y = x.mapRows(1, 2, 3);
        y.setInt(0, 10);
        y.setMissing(1);

        assertEquals(10, x.getInt(1), TOL);
        assertTrue(x.isMissing(2));
        assertEquals(3, x.getInt(3), TOL);
    }

    @Test
    public void testMissing() {
        VarInt x = VarInt.empty(10);
        Var map = x.mapRows(0, 1, 2);
        assertTrue(map.isMissing(0));
    }

    @Test
    public void testRemoveClearRows() {
        Var x = VarInt.seq(100).mapRows(1, 2, 3, 4);
        assertTrue(x.deepEquals(VarInt.wrap(1, 2, 3, 4)));

        x.removeRow(1);
        assertTrue(x.deepEquals(VarInt.wrap(1, 3, 4)));

        x.clearRows();
        assertTrue(x.deepEquals(VarInt.empty()));
    }

    @Test
    public void testNewInstance() {
        Var x = VarInt.seq(10).mapRows(1, 2, 3);
        assertTrue(x.newInstance(0).deepEquals(VarInt.empty()));
    }

    @Test
    public void testToString() {
        assertEquals("MappedVar[type=int, name:?, rowCount:3]", VarInt.seq(10).mapRows(1, 2, 3).toString());
    }
}
