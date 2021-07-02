/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundVarTest {

    private VarDouble a;
    private VarDouble b;
    private VarDouble c;

    @BeforeEach
    void setUp() {
        a = VarDouble.seq(0, 10);
        b = VarDouble.empty(1);
        c = VarDouble.seq(20, 40);
    }

    @Test
    void testInvalidBindTypes() {
        var ex = assertThrows(IllegalArgumentException.class, () -> VarDouble.scalar(1).bindRows(VarInt.seq(10)));
        assertEquals("It is not allowed to bind variables of different types", ex.getMessage());
    }

    @Test
    void testInvalidBindEmptyCollections() {
        var ex = assertThrows(IllegalArgumentException.class, () -> BoundVar.from(new ArrayList<>(), new ArrayList<>()));
        assertEquals("List of vars is empty", ex.getMessage());
    }

    @Test
    void testInvalidEmptyCountCollection() {
        List<Var> vars = new ArrayList<>();
        vars.add(VarDouble.seq(10));
        var ex = assertThrows(IllegalArgumentException.class, () -> BoundVar.from(new ArrayList<>(), vars));
        assertEquals("List of counts is empty", ex.getMessage());
    }

    @Test
    void testInvalidNonMatchingCollections() {
        List<Var> vars = new ArrayList<>();
        vars.add(VarDouble.seq(10));
        List<Integer> counts = new ArrayList<>();
        counts.add(10);
        counts.add(1);

        var ex = assertThrows(IllegalArgumentException.class, () -> BoundVar.from(counts, vars));
        assertEquals("List of counts is not equal with list of variables", ex.getMessage());
    }

    @Test
    public void testOutsideBounds() {
        VarDouble a = VarDouble.wrap(1, 2, 3);
        VarDouble b = VarDouble.wrap(4, 5);
        Var x = BoundVar.from(a, b);

        var ex = assertThrows(IllegalArgumentException.class, () -> x.getDouble(100));
        assertEquals("Row index is not valid: 100", ex.getMessage());
    }

    @Test
    void testBind() {
        VarDouble a = VarDouble.wrap(1, 2, 3);
        VarDouble b = VarDouble.wrap(4, 5);
        VarDouble c = VarDouble.wrap(6, 7, 8, 9);
        VarDouble d = VarDouble.empty(1);
        VarDouble e = VarDouble.wrap(Math.PI, Math.E);

        Var x = BoundVar.from(a, b);
        Var y = BoundVar.from(c, d);
        x = x.bindRows(y).bindRows(e);

        assertEquals(12, x.size());
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

        assertEquals(x.size(), z.size());
        for (int i = 0; i < x.size(); i++) {
            if (x.isMissing(i)) {
                assertEquals(x.isMissing(i), z.isMissing(i));
            } else {
                assertEquals(x.getDouble(i), z.getDouble(i), 1e-12);
            }
        }

        z = x.mapRows(Mapping.wrap(0, 7, 9));
        assertEquals(3, z.size());
        assertEquals(1, z.getDouble(0), 1e-12);
        assertEquals(8, z.getDouble(1), 1e-12);
        assertTrue(z.isMissing(2));

        z.setMissing(1);
        assertTrue(z.isMissing(1));

        Var x1 = x;
        assertThrows(OperationNotAvailableException.class, x1::addMissing);
    }

    @Test
    void testInvalidAddDouble() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addDouble(1.0));
    }

    @Test
    void testInvalidAddInt() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addInt(1));
    }

    @Test
    void testInvalidAddLong() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addLong(1));
    }

    @Test
    void testInvalidAddLabel() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addLabel("1"));
    }

    @Test
    void testInvalidAddBoolean() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addInt(1));
    }

    @Test
    void testInvalidAddMissing() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addMissing());
    }

    @Test
    void testInvalidAddRows() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).addRows(1));
    }

    @Test
    void testInvalidRemoveRow() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).removeRow(1));
    }

    @Test
    void testInvalidAddClear() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).clearRows());
    }

    @Test
    void testInvalidSetLevelk() {
        assertThrows(OperationNotAvailableException.class, () -> BoundVar.from(a, b, c).setLevels(new String[]{}));
    }

    @Test
    void testNewInstance() {
        Var var = BoundVar.from(VarDouble.seq(10)).newInstance(10);
        assertTrue(var.deepEquals(VarDouble.empty(10)));
    }

    @Test
    void testDoubleBound() {
        Var a = VarDouble.wrap(1, 2);
        Var b = VarDouble.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setDouble(0, 100);
        assertEquals(100, x.getDouble(0), 1e-12);
    }


    @Test
    void testIntBound() {
        Var a = VarInt.wrap(1, 2);
        Var b = VarInt.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setInt(0, 100);
        assertEquals(100, x.getInt(0));
    }


    @Test
    void testStampBound() {
        Var a = VarLong.wrap(1, 2);
        Var b = VarLong.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setLong(0, 100);
        assertEquals(100, x.getLong(0));
    }

    @Test
    void testBinaryBound() {
        Var a = VarBinary.copy(1);
        Var b = VarBinary.copy(0);

        Var x = a.bindRows(b);
        x.setInt(0, 0);
        assertEquals(0, x.getInt(0));
    }

    @Test
    void testNominalBound() {
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
