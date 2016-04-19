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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundVarTest {

    @Test
    public void testBuildWrong() {
        Numeric a = Numeric.copy(1, 2);
        Binary b = Binary.copy(true, false);

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
        Numeric a = Numeric.wrap(1, 2, 3);
        Numeric b = Numeric.wrap(4, 5);
        Numeric c = Numeric.wrap(6, 7, 8, 9);
        Numeric d = Numeric.empty(1);
        Numeric e = Numeric.wrap(Math.PI, Math.E);

        Var x = BoundVar.from(a, b);
        Var y = BoundVar.from(c, d);
        x = x.bindRows(y).bindRows(e);

        assertEquals(12, x.rowCount());
        assertEquals(1, x.value(0), 1e-12);
        assertEquals(4, x.value(3), 1e-12);
        assertEquals(8, x.value(7), 1e-12);
        assertEquals(true, x.missing(9));
        assertEquals(Math.E, x.value(11), 1e-12);

        try {
            x.value(100);
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
            if (x.missing(i)) {
                assertEquals(x.missing(i), z.missing(i));
            } else {
                assertEquals(x.value(i), z.value(i), 1e-12);
            }
        }

        z = x.mapRows(Mapping.copy(0, 7, 9));
        assertEquals(3, z.rowCount());
        assertEquals(1, z.value(0), 1e-12);
        assertEquals(8, z.value(1), 1e-12);
        assertEquals(true, z.missing(2));

        z.setMissing(1);
        assertEquals(true, z.missing(1));

        try {
            x.addMissing();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testValueBound() {
        Var a = Numeric.wrap(1, 2);
        Var b = Numeric.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setValue(0, 100);
        assertEquals(100, x.value(0), 1e-12);

        try {
            x.addValue(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }


    @Test
    public void testIndexBound() {
        Var a = Index.wrap(1, 2);
        Var b = Index.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setIndex(0, 100);
        assertEquals(100, x.index(0));

        try {
            x.addIndex(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }


    @Test
    public void testStampBound() {
        Var a = Stamp.wrap(1, 2);
        Var b = Stamp.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setStamp(0, 100);
        assertEquals(100, x.stamp(0));

        try {
            x.addStamp(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testBinaryBound() {
        Var a = Binary.copy(true);
        Var b = Binary.copy(false);

        Var x = a.bindRows(b);
        x.setBinary(0, false);
        assertEquals(false, x.binary(0));

        try {
            x.addBinary(false);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testNominalBound() {
        Var a = Nominal.copy("a", "b", "a");
        Var b = Nominal.copy("b", "a", "b");

        Var x = a.bindRows(b);
        x.setLabel(0, "b");
        assertEquals("b", x.label(0));

        try {
            x.addLabel("b");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        assertEquals("a", x.levels()[1]);
        assertEquals("b", x.levels()[2]);
        assertEquals(3, x.levels().length);

        try {
            x.setLevels("c", "d");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            Nominal.copy("x").bindRows(Nominal.copy("b"));
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testRemove() {
        Var x = Numeric.copy(1, 2, 3).bindRows(Numeric.copy(4, 5, 6));

        try {
            x.remove(1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
        try {
            x.clear();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }
}
