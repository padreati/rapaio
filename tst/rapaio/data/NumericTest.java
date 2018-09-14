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
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NumericTest {

    @Test
    public void smokeTest() {
        Var v = VarDouble.empty();
        boolean flag = v.type().isNumeric();
        assertEquals(true, flag);
        assertEquals(false, v.type().isNominal());

        assertEquals(0, v.rowCount());

        try {
            VarDouble.empty(-1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        assertEquals("VarDouble[name:?, rowCount:1]", VarDouble.empty(1).toString());
    }

    @Test
    public void testGetterSetter() {
        Var v = VarDouble.empty(10);
        for (int i = 0; i < 10; i++) {
            v.setDouble(i, Math.log(10 + i));
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(Math.log(10 + i), v.getDouble(i), 1e-10);
            assertEquals((int) Math.rint(Math.log(10 + i)), v.getInt(i));
        }

        for (int i = 0; i < 10; i++) {
            v.setInt(i, i * i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i * i, v.getInt(i));
            assertEquals(i * i, v.getDouble(i), 1e-10);
        }

        for (int i = 0; i < v.rowCount(); i++) {
            assertEquals(String.valueOf(v.getDouble(i)), v.getLabel(i));
        }
        try {
            v.setLabel(0, "test");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        try {
            v.addLabel("x");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        try {
            v.levels();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }

        try {
            v.setLevels();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void testOneNumeric() {
        Var one = VarDouble.scalar(Math.PI);

        assertEquals(1, one.rowCount());
        assertEquals(Math.PI, one.getDouble(0), 1e-10);

        one = VarDouble.scalar(Math.E);
        assertEquals(1, one.rowCount());
        assertEquals(Math.E, one.getDouble(0), 1e-10);
    }

    @Test
    public void testWithName() {
        VarDouble x = VarDouble.copy(1, 2, 3, 5).withName("X");
        assertEquals("X", x.name());

        Var y = MappedVar.byRows(x, 1, 2);
        assertEquals("X", y.name());
        y.withName("y");
        assertEquals("y", y.name());

        assertEquals(2.0, y.getDouble(0), 10e-10);
        assertEquals(3.0, y.getDouble(1), 10e-10);
    }

    @Test
    public void testBuilders() {
        List<Integer> intList = new ArrayList<>();
        List<Double> doubleList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            intList.add(i + 1);
            doubleList.add(i + 1.0);
        }

        VarDouble x1 = VarDouble.copy(1, 2, 3);
        VarDouble x2 = VarDouble.copy(1.0, 2.0, 3.0);
        VarDouble x3 = VarDouble.copy(intList);
        VarDouble x4 = VarDouble.copy(doubleList);

        for (int i = 0; i < 3; i++) {
            assertEquals(x1.getDouble(i), x2.getDouble(i), 10e-10);
            assertEquals(x3.getDouble(i), x4.getDouble(i), 10e-10);
            assertEquals(x1.getDouble(i), x3.getDouble(i), 10e-10);
        }

        double[] y1v = new double[3];
        y1v[0] = 10;
        y1v[1] = 20;
        y1v[2] = 30;
        VarDouble y1 = VarDouble.wrap(y1v);
        y1v[1] = Double.NaN;
        y1v[2] = 100;
        for (int i = 0; i < 3; i++) {
            assertEquals(y1v[i], y1.getDouble(i), 10e-10);
        }

        VarDouble y2 = VarDouble.copy(x2);
        for (int i = 0; i < 3; i++) {
            assertEquals(x1.getDouble(i), y2.getDouble(i), 10e-10);
        }

        VarDouble y3 = VarDouble.copy(1, 2, 3, 4, 5);
        Var y4 = MappedVar.byRows(y3, 3, 1, 2);
        Var y5 = VarDouble.copy(y4);

        for (int i = 0; i < 3; i++) {
            assertEquals(y4.getDouble(i), y5.getDouble(i), 10e-10);
        }

        VarDouble z1 = VarDouble.fill(10);
        VarDouble z2 = VarDouble.fill(10, Math.PI);

        for (int i = 0; i < 10; i++) {
            assertEquals(0, z1.getDouble(i), 10e-10);
            assertEquals(Math.PI, z2.getDouble(i), 10e-10);
        }
    }

    @Test
    public void testOtherValues() {
        VarDouble x = VarDouble.copy(1, 2, 3, 4).withName("x");

        x.addInt(10);
        assertEquals(10, x.getDouble(x.rowCount() - 1), 10e-10);

        VarDouble b = VarDouble.empty();
        b.addBoolean(true);
        b.addBoolean(false);

        assertEquals(true, b.getBoolean(0));
        assertEquals(false, b.getBoolean(1));

        assertEquals(1, b.getDouble(0), 10e-10);
        assertEquals(0, b.getDouble(1), 10e-10);

        b.setBoolean(1, true);
        assertEquals(1, b.getDouble(1), 10e-10);
        assertEquals(true, b.getBoolean(1));

        VarDouble s = VarDouble.empty();
        s.addLong(1);
        s.addLong(-100000000000L);
        assertEquals(1L, s.getLong(0));
        assertEquals(-100000000000d, s.getLong(1), 10e-10);

        s.setLong(1, 15);
        assertEquals(15, s.getLong(1));


        VarDouble mis = VarDouble.empty();
        mis.addMissing();
        mis.addDouble(1);
        mis.addMissing();
        mis.addDouble(2);
        mis.setMissing(3);

        assertTrue(mis.isMissing(0));
        assertTrue(mis.isMissing(2));
        assertTrue(mis.isMissing(3));
        assertFalse(mis.isMissing(1));
    }

    @Test
    public void testClearRemove() {
        VarDouble x = VarDouble.copy(1, 2, 3);
        x.remove(1);

        assertEquals(1, x.getInt(0));
        assertEquals(3, x.getInt(1));

        VarDouble y = x.solidCopy();

        x.clear();

        assertEquals(0, x.rowCount());

        assertEquals(2, y.rowCount());
        assertEquals(1, y.getInt(0));
        assertEquals(3, y.getInt(1));
    }
}
