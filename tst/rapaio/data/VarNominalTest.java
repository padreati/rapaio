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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarNominalTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testSmoke() {
        Var v = VarNominal.empty(0);
        assertEquals(0, v.rowCount());
        assertEquals(1, v.levels().size());
        assertEquals("?", v.levels().get(0));

        v = VarNominal.empty();
        assertEquals(0, v.rowCount());
        assertEquals(1, v.levels().size());
        assertEquals("?", v.levels().get(0));

        assertTrue(v.type().isNominal());
        assertFalse(v.type().isNumeric());

        v = VarNominal.empty(1, "a");
        assertEquals(1, v.rowCount());
        assertEquals("?", v.getLabel(0));

        assertEquals("Nominal[name:?, rowCount:10]", VarNominal.empty(10).toString());
    }

    @Test
    public void testDictionary() {
        Var v = VarNominal.empty(0, "a", "a", "v", "a");
        assertEquals(3, v.levels().size());
        assertEquals("?", v.levels().get(0));
        assertEquals("a", v.levels().get(1));
        assertEquals("v", v.levels().get(2));

        ArrayList<String> set = new ArrayList<>();
        set.add("a");
        set.add("v");
        set.add("a");

        v = VarNominal.empty(0, set);
        assertEquals(3, v.levels().size());
        assertEquals("?", v.levels().get(0));
        assertEquals("a", v.levels().get(1));
        assertEquals("v", v.levels().get(2));
    }

    @Test
    public void testSetterGetter() {
        Var v = VarNominal.empty(4, "a", "b", "c");
        for (int i = 0; i < 4; i++) {
            assertTrue(v.isMissing(i));
            assertEquals(0, v.getInt(i));
        }

        // w/ index

        v.setInt(0, 1);
        v.setInt(1, 2);
        v.setInt(2, 3);
        v.setInt(3, 0);

        assertEquals("a", v.getLabel(0));
        assertEquals("b", v.getLabel(1));
        assertEquals("c", v.getLabel(2));
        assertEquals("?", v.getLabel(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.getInt(0));
        assertEquals(2, v.getInt(1));
        assertEquals(1, v.getInt(2));
        assertEquals(0, v.getInt(3));

        // w/ value

        v.setDouble(0, 1);
        v.setDouble(1, 2);
        v.setDouble(2, 3);
        v.setDouble(3, 0);

        assertEquals("a", v.getLabel(0));
        assertEquals("b", v.getLabel(1));
        assertEquals("c", v.getLabel(2));
        assertEquals("?", v.getLabel(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.getDouble(0), 1e-10);
        assertEquals(2, v.getDouble(1), 1e-10);
        assertEquals(1, v.getDouble(2), 1e-10);
        assertEquals(0, v.getDouble(3), 1e-10);
    }

    @Test
    public void testLabel() {
        Var v = VarNominal.empty(1, "a", "b", "c");

        boolean exceptional = false;
        try {
            v.setLabel(0, "j");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(!exceptional);

        exceptional = false;
        try {
            v.setLabel(-1, "a");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);

        exceptional = false;
        try {
            v.setLabel(4, "a");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);
    }

    @Test
    public void testMissing() {
        Var v = VarNominal.empty(1, "a", "b");
        assertTrue(v.isMissing(0));

        v.setLabel(0, "a");
        assertFalse(v.isMissing(0));

        v.setMissing(0);
        assertTrue(v.isMissing(0));

        v.setLabel(0, "?");
        assertTrue(v.isMissing(0));
    }

    @Test
    public void testCopy() {
        VarNominal a = VarNominal.empty(0, "x", "y");
        a.addLabel("x");
        a.addLabel("y");

        VarNominal b = a.solidCopy();

        a.addLabel("z");

        assertEquals(2, b.rowCount());
        assertEquals(3, a.rowCount());
    }

    @Test
    public void testFactorBaseAddRemove() {
        VarNominal var = VarNominal.empty(0, "x", "y");

        var.addMissing();
        assertEquals(1, var.rowCount());

        var.addInt(1);
        assertEquals(2, var.rowCount());
        assertEquals("x", var.getLabel(1));

        var.addDouble(2.4);
        assertEquals(3, var.rowCount());
        assertEquals("y", var.getLabel(2));

        var = VarNominal.empty();
        var.addLabel("x");
        var.addLabel("y");
        var.remove(0);

        assertEquals(1, var.rowCount());
        assertEquals("y", var.getLabel(0));

        var.clear();
        assertEquals(0, var.rowCount());
    }

    @Test
    public void testBuilders() {
        String[] src1 = new String[]{"a", "b", "c", "?", "a", "b", "c", "?"};
        List<String> src2 = Arrays.stream(src1).collect(Collectors.toList());

        VarNominal copy1 = VarNominal.copy(src1);
        VarNominal copy2 = VarNominal.copy(src2);
        VarNominal copy3 = VarNominal.from(src1.length, row -> src1[row], "a", "b", "c");
        VarNominal copy4 = src2.stream().collect(VarNominal.collector());
        VarNominal copy5 = src2.stream().parallel().collect(VarNominal.collector());

        assertTrue(copy1.deepEquals(copy2));
        assertTrue(copy1.deepEquals(copy3));
        assertTrue(copy1.deepEquals(copy4));

        VarNominal copy6 = VarNominal.copy("a", "b");
        copy6.addRows(2);
        assertEquals(4, copy6.rowCount());
        assertTrue(copy6.isMissing(2));
        assertTrue(copy6.isMissing(3));
    }

    @Test
    public void testFactorBaseBinaryStamp() {

        try {
            VarNominal.empty(1, "x").getBoolean(0);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            VarNominal.empty().addBoolean(true);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            VarNominal.empty(1, "x").setBoolean(0, true);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            VarNominal.empty(1, "x").getLong(0);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            VarNominal.empty().addLong(1);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            VarNominal.empty(1, "x").setLong(0, 1);
            assertTrue(false);
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void testJoinTermsDictionary() {
        VarNominal x = VarNominal.empty(0, "a", "b", "c");
        x.addLabel("a");
        x.addLabel("b");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");

        x.setLevels("x", "y", "x");

        assertEquals(3, x.levels().size());
        assertEquals("x", x.getLabel(0));
        assertEquals("y", x.getLabel(1));
        assertEquals("x", x.getLabel(2));
        assertEquals("x", x.getLabel(3));
    }

    @Test
    public void testAddTermsDictionary() {
        VarNominal x = VarNominal.empty(0, "a", "b", "c");
        x.addLabel("a");
        x.addLabel("b");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");

        x.setLevels("x", "y", "z", "p");

        assertEquals(5, x.levels().size());
        assertEquals("x", x.getLabel(0));
        assertEquals("y", x.getLabel(1));
        assertEquals("x", x.getLabel(2));
        assertEquals("z", x.getLabel(3));

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("new levels does not contains all old labels");
        VarNominal y = VarNominal.empty(0, "a", "b");
        y.setLevels("x");
    }
}
