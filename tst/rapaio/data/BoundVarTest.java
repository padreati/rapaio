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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundVarTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private VarDouble a;
    private VarDouble b;
    private VarDouble c;

    @Before
    public void setUp() {
        a = VarDouble.seq(0, 10);
        b = VarDouble.empty(1);
        c = VarDouble.seq(20, 40);
    }

    @Test
    public void testInvalidBindTypes() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("It is not allowed to bind variables of different types");
        VarDouble.scalar(1).bindRows(VarInt.seq(10));
    }

    @Test
    public void testInvalidBindEmptyCollections() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("List of vars is empty");
        BoundVar.from(new ArrayList<>(), new ArrayList<>());
    }

    @Test
    public void testInvalidEmptyCountCollection() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("List of counts is empty");
        List<Var> vars = new ArrayList<>();
        vars.add(VarDouble.seq(10));
        BoundVar.from(new ArrayList<>(), vars);
    }

    @Test
    public void testInvalidNonMatchingCollections() {
        List<Var> vars = new ArrayList<>();
        vars.add(VarDouble.seq(10));
        List<Integer> counts = new ArrayList<>();
        counts.add(10);
        counts.add(1);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("List of counts is not equal with list of variables");
        BoundVar.from(counts, vars);
    }

    @Test
    public void testOutsideBounds() {
        VarDouble a = VarDouble.wrap(1, 2, 3);
        VarDouble b = VarDouble.wrap(4, 5);
        Var x = BoundVar.from(a, b);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Row index is not valid: 100");
        x.getDouble(100);
    }

    @Test
    public void testBind() {
        VarDouble a = VarDouble.wrap(1, 2, 3);
        VarDouble b = VarDouble.wrap(4, 5);
        VarDouble c = VarDouble.wrap(6, 7, 8, 9);
        VarDouble d = VarDouble.empty(1);
        VarDouble e = VarDouble.wrap(Math.PI, Math.E);

        Var x = BoundVar.from(a, b);
        Var y = BoundVar.from(c, d);
        x = x.bindRows(y).bindRows(e);

        assertEquals(12, x.rowCount());
        assertEquals(1, x.getDouble(0), 1e-12);
        assertEquals(4, x.getDouble(3), 1e-12);
        assertEquals(8, x.getDouble(7), 1e-12);
        assertTrue(x.isMissing(9));
        assertEquals(Math.E, x.getDouble(11), 1e-12);

        List<Var> vars = new ArrayList<>();
        vars.add(a);
        vars.add(b);
        vars.add(c);
        vars.add(d);
        vars.add(e);
        Var z = BoundVar.from(vars);

        assertEquals(x.rowCount(), z.rowCount());
        for (int i = 0; i < x.rowCount(); i++) {
            if (x.isMissing(i)) {
                assertEquals(x.isMissing(i), z.isMissing(i));
            } else {
                assertEquals(x.getDouble(i), z.getDouble(i), 1e-12);
            }
        }

        z = x.mapRows(Mapping.wrap(0, 7, 9));
        assertEquals(3, z.rowCount());
        assertEquals(1, z.getDouble(0), 1e-12);
        assertEquals(8, z.getDouble(1), 1e-12);
        assertTrue(z.isMissing(2));

        z.setMissing(1);
        assertTrue(z.isMissing(1));

        expectedException.expect(OperationNotAvailableException.class);
        x.addMissing();
    }

    @Test
    public void testInvalidAddDouble() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addDouble(1.0);
    }

    @Test
    public void testInvalidAddInt() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addInt(1);
    }

    @Test
    public void testInvalidAddLong() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addLong(1);
    }

    @Test
    public void testInvalidAddLabel() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addLabel("1");
    }

    @Test
    public void testInvalidAddBoolean() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addInt(1);
    }

    @Test
    public void testInvalidAddMissing() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addMissing();
    }

    @Test
    public void testInvalidAddRows() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).addRows(1);
    }

    @Test
    public void testInvalidRemoveRow() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).removeRow(1);
    }

    @Test
    public void testInvalidAddClear() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).clearRows();
    }

    @Test
    public void testInvalidSetLevelk() {
        expectedException.expect(OperationNotAvailableException.class);
        BoundVar.from(a, b, c).setLevels(new String[]{});
    }

    @Test
    public void testNewInstance() {
        Var var = BoundVar.from(VarDouble.seq(10)).newInstance(10);
        assertTrue(var.deepEquals(VarDouble.empty(10)));
    }

    @Test
    public void testDoubleBound() {
        Var a = VarDouble.wrap(1, 2);
        Var b = VarDouble.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setDouble(0, 100);
        assertEquals(100, x.getDouble(0), 1e-12);
    }


    @Test
    public void testIntBound() {
        Var a = VarInt.wrap(1, 2);
        Var b = VarInt.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setInt(0, 100);
        assertEquals(100, x.getInt(0));
    }


    @Test
    public void testStampBound() {
        Var a = VarLong.wrap(1, 2);
        Var b = VarLong.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setLong(0, 100);
        assertEquals(100, x.getLong(0));
    }

    @Test
    public void testBinaryBound() {
        Var a = VarBinary.copy(1);
        Var b = VarBinary.copy(0);

        Var x = a.bindRows(b);
        x.setInt(0, 0);
        assertEquals(0, x.getInt(0));
    }

    @Test
    public void testNominalBound() {
        Var a = VarNominal.copy("a", "b", "a");
        Var b = VarNominal.copy("b", "a", "b");

        Var x = a.bindRows(b);
        x.setLabel(0, "b");
        assertEquals("b", x.getLabel(0));

        assertEquals("a", x.levels().get(1));
        assertEquals("b", x.levels().get(2));
        assertEquals(3, x.levels().size());
    }
}
