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

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NominalTest {

    @Test
    public void testSmoke() {
        Var v = NominalVar.empty(0);
        assertEquals(0, v.getRowCount());
        assertEquals(1, v.getLevels().length);
        assertEquals("?", v.getLevels()[0]);

        v = NominalVar.empty();
        assertEquals(0, v.getRowCount());
        assertEquals(1, v.getLevels().length);
        assertEquals("?", v.getLevels()[0]);

        assertTrue(v.getType().isNominal());
        assertFalse(v.getType().isNumeric());

        v = NominalVar.empty(1, "a");
        assertEquals(1, v.getRowCount());
        assertEquals("?", v.getLabel(0));

        assertEquals("Nominal[name:?, rowCount:10]", NominalVar.empty(10).toString());
    }

    @Test
    public void testDictionary() {
        Var v = NominalVar.empty(0, "a", "a", "v", "a");
        assertEquals(3, v.getLevels().length);
        assertEquals("?", v.getLevels()[0]);
        assertEquals("a", v.getLevels()[1]);
        assertEquals("v", v.getLevels()[2]);

        ArrayList<String> set = new ArrayList<>();
        set.add("a");
        set.add("v");
        set.add("a");

        v = NominalVar.empty(0, set);
        assertEquals(3, v.getLevels().length);
        assertEquals("?", v.getLevels()[0]);
        assertEquals("a", v.getLevels()[1]);
        assertEquals("v", v.getLevels()[2]);
    }

    @Test
    public void testSetterGetter() {
        Var v = NominalVar.empty(4, "a", "b", "c");
        for (int i = 0; i < 4; i++) {
            assertTrue(v.isMissing(i));
            assertEquals(0, v.getIndex(i));
        }

        // w/ index

        v.setIndex(0, 1);
        v.setIndex(1, 2);
        v.setIndex(2, 3);
        v.setIndex(3, 0);

        assertEquals("a", v.getLabel(0));
        assertEquals("b", v.getLabel(1));
        assertEquals("c", v.getLabel(2));
        assertEquals("?", v.getLabel(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.getIndex(0));
        assertEquals(2, v.getIndex(1));
        assertEquals(1, v.getIndex(2));
        assertEquals(0, v.getIndex(3));

        // w/ value

        v.setValue(0, 1);
        v.setValue(1, 2);
        v.setValue(2, 3);
        v.setValue(3, 0);

        assertEquals("a", v.getLabel(0));
        assertEquals("b", v.getLabel(1));
        assertEquals("c", v.getLabel(2));
        assertEquals("?", v.getLabel(3));

        v.setLabel(0, "c");
        v.setLabel(1, "b");
        v.setLabel(2, "a");
        v.setLabel(3, "?");

        assertEquals(3, v.getValue(0), 1e-10);
        assertEquals(2, v.getValue(1), 1e-10);
        assertEquals(1, v.getValue(2), 1e-10);
        assertEquals(0, v.getValue(3), 1e-10);
    }

    @Test
    public void testLabel() {
        Var v = NominalVar.empty(1, "a", "b", "c");

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
        Var v = NominalVar.empty(1, "a", "b");
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
        NominalVar a = NominalVar.empty(0, "x", "y");
        a.addLabel("x");
        a.addLabel("y");

        NominalVar b = a.solidCopy();

        a.addLabel("z");

        assertEquals(2, b.getRowCount());
        assertEquals(3, a.getRowCount());
    }

    @Test
    public void testFactorBaseAddRemove() {
        NominalVar var = NominalVar.empty(0, "x", "y");

        var.addMissing();
        assertEquals(1, var.getRowCount());

        var.addIndex(1);
        assertEquals(2, var.getRowCount());
        assertEquals("x", var.getLabel(1));

        var.addValue(2.4);
        assertEquals(3, var.getRowCount());
        assertEquals("y", var.getLabel(2));

        var = NominalVar.empty();
        var.addLabel("x");
        var.addLabel("y");
        var.remove(0);

        assertEquals(1, var.getRowCount());
        assertEquals("y", var.getLabel(0));

        var.clear();
        assertEquals(0, var.getRowCount());
    }

    @Test
    public void testFactorBaseBinaryStamp() {

        try {
            NominalVar.empty(1, "x").getBinary(0);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            NominalVar.empty().addBinary(true);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            NominalVar.empty(1, "x").setBinary(0, true);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            NominalVar.empty(1, "x").getStamp(0);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            NominalVar.empty().addStamp(1);
            assertTrue(false);
        } catch (Throwable ignored) {
        }

        try {
            NominalVar.empty(1, "x").setStamp(0, 1);
            assertTrue(false);
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void testJoinTermsDictionary() {
        NominalVar x = NominalVar.empty(0, "a", "b", "c");
        x.addLabel("a");
        x.addLabel("b");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");

        x.setLevels("x", "y", "x");

        assertEquals(3, x.getLevels().length);
        assertEquals("x", x.getLabel(0));
        assertEquals("y", x.getLabel(1));
        assertEquals("x", x.getLabel(2));
        assertEquals("x", x.getLabel(3));
    }

    @Test
    public void testAddTermsDictionary() {
        NominalVar x = NominalVar.empty(0, "a", "b", "c");
        x.addLabel("a");
        x.addLabel("b");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");
        x.addLabel("c");
        x.addLabel("a");

        x.setLevels("x", "y", "z", "p");

        assertEquals(5, x.getLevels().length);
        assertEquals("x", x.getLabel(0));
        assertEquals("y", x.getLabel(1));
        assertEquals("x", x.getLabel(2));
        assertEquals("z", x.getLabel(3));

        try {
            NominalVar y = NominalVar.empty(0, "a", "b");
            y.setLevels("x");
            assertTrue(false);
        } catch (Throwable ignored) {
        }
    }
}
