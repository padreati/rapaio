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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundVarTest {

    @Test
    public void testBuildWrong() {
        VarDouble a = VarDouble.copy(1, 2);
        VarBoolean b = VarBoolean.copy(true, false);

        try {
            a.bindRows(b);
            assertTrue("This should raise an exception", false);
        } catch (Throwable ignored) {
        }

        try {
            BoundVar.from(new ArrayList<>(), new ArrayList<>());
            assertTrue("This should raise an exception", false);
        } catch (Throwable ignored) {
        }

        try {
            List<Var> vars = new ArrayList<>();
            vars.add(a);
            BoundVar.from(new ArrayList<>(), vars);
            assertTrue("This should raise an exception", false);
        } catch (Throwable ignored) {
        }

        try {
            List<Var> vars = new ArrayList<>();
            vars.add(a);
            List<Integer> counts = new ArrayList<>();
            counts.add(10);
            counts.add(1);
            BoundVar.from(counts, vars);
            assertTrue("This should raise an exception", false);
        } catch (Throwable ignored) {
        }
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
        assertEquals(true, x.isMissing(9));
        assertEquals(Math.E, x.getDouble(11), 1e-12);

        try {
            x.getDouble(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

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
        assertEquals(true, z.isMissing(2));

        z.setMissing(1);
        assertEquals(true, z.isMissing(1));

        try {
            x.addMissing();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testValueBound() {
        Var a = VarDouble.wrap(1, 2);
        Var b = VarDouble.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setDouble(0, 100);
        assertEquals(100, x.getDouble(0), 1e-12);

        try {
            x.addDouble(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }


    @Test
    public void testIndexBound() {
        Var a = VarInt.wrap(1, 2);
        Var b = VarInt.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setInt(0, 100);
        assertEquals(100, x.getInt(0));

        try {
            x.addInt(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }


    @Test
    public void testStampBound() {
        Var a = VarLong.wrap(1, 2);
        Var b = VarLong.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setLong(0, 100);
        assertEquals(100, x.getLong(0));

        try {
            x.addLong(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testBinaryBound() {
        Var a = VarBoolean.copy(true);
        Var b = VarBoolean.copy(false);

        Var x = a.bindRows(b);
        x.setBoolean(0, false);
        assertEquals(false, x.getBoolean(0));

        try {
            x.addBoolean(false);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testNominalBound() {
        Var a = VarNominal.copy("a", "b", "a");
        Var b = VarNominal.copy("b", "a", "b");

        Var x = a.bindRows(b);
        x.setLabel(0, "b");
        assertEquals("b", x.getLabel(0));

        try {
            x.addLabel("b");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        assertEquals("a", x.levels().get(1));
        assertEquals("b", x.levels().get(2));
        assertEquals(3, x.levels().size());

        try {
            x.setLevels("c", "d");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            VarNominal.copy("x").bindRows(VarNominal.copy("b"));
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testRemove() {
        Var x = VarDouble.copy(1, 2, 3).bindRows(VarDouble.copy(4, 5, 6));

        try {
            x.removeRow(1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
        try {
            x.clearRows();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }
}
