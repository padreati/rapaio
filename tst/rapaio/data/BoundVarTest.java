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
        NumericVar a = NumericVar.copy(1, 2);
        BinaryVar b = BinaryVar.copy(true, false);

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
        NumericVar a = NumericVar.wrap(1, 2, 3);
        NumericVar b = NumericVar.wrap(4, 5);
        NumericVar c = NumericVar.wrap(6, 7, 8, 9);
        NumericVar d = NumericVar.empty(1);
        NumericVar e = NumericVar.wrap(Math.PI, Math.E);

        Var x = BoundVar.from(a, b);
        Var y = BoundVar.from(c, d);
        x = x.bindRows(y).bindRows(e);

        assertEquals(12, x.getRowCount());
        assertEquals(1, x.getValue(0), 1e-12);
        assertEquals(4, x.getValue(3), 1e-12);
        assertEquals(8, x.getValue(7), 1e-12);
        assertEquals(true, x.isMissing(9));
        assertEquals(Math.E, x.getValue(11), 1e-12);

        try {
            x.getValue(100);
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

        assertEquals(x.getRowCount(), z.getRowCount());
        for (int i = 0; i < x.getRowCount(); i++) {
            if (x.isMissing(i)) {
                assertEquals(x.isMissing(i), z.isMissing(i));
            } else {
                assertEquals(x.getValue(i), z.getValue(i), 1e-12);
            }
        }

        z = x.mapRows(Mapping.copy(0, 7, 9));
        assertEquals(3, z.getRowCount());
        assertEquals(1, z.getValue(0), 1e-12);
        assertEquals(8, z.getValue(1), 1e-12);
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
        Var a = NumericVar.wrap(1, 2);
        Var b = NumericVar.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setValue(0, 100);
        assertEquals(100, x.getValue(0), 1e-12);

        try {
            x.addValue(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }


    @Test
    public void testIndexBound() {
        Var a = IndexVar.wrap(1, 2);
        Var b = IndexVar.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setIndex(0, 100);
        assertEquals(100, x.getIndex(0));

        try {
            x.addIndex(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }


    @Test
    public void testStampBound() {
        Var a = StampVar.wrap(1, 2);
        Var b = StampVar.wrap(3, 4);

        Var x = a.bindRows(b);
        x.setStamp(0, 100);
        assertEquals(100, x.getStamp(0));

        try {
            x.addStamp(100);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testBinaryBound() {
        Var a = BinaryVar.copy(true);
        Var b = BinaryVar.copy(false);

        Var x = a.bindRows(b);
        x.setBinary(0, false);
        assertEquals(false, x.getBinary(0));

        try {
            x.addBinary(false);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testNominalBound() {
        Var a = NominalVar.copy("a", "b", "a");
        Var b = NominalVar.copy("b", "a", "b");

        Var x = a.bindRows(b);
        x.setLabel(0, "b");
        assertEquals("b", x.getLabel(0));

        try {
            x.addLabel("b");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        assertEquals("a", x.getLevels()[1]);
        assertEquals("b", x.getLevels()[2]);
        assertEquals(3, x.getLevels().length);

        try {
            x.setLevels("c", "d");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            NominalVar.copy("x").bindRows(NominalVar.copy("b"));
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testRemove() {
        Var x = NumericVar.copy(1, 2, 3).bindRows(NumericVar.copy(4, 5, 6));

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
