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
import rapaio.core.RandomSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarFloatTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(134);
    }

    @Test
    public void testStaticBuilders() {

        VarFloat empty1 = VarFloat.empty();
        assertEquals(0, empty1.rowCount());

        VarFloat empty2 = VarFloat.empty(100);
        assertEquals(100, empty2.rowCount());
        for (int i = 0; i < 100; i++) {
            assertTrue(empty2.isMissing(i));
        }

        int[] sourceIntArray = IntStream.range(0, 100).map(i -> (i % 10 == 0) ? Integer.MIN_VALUE : RandomSource.nextInt(100)).toArray();
        List<Integer> sourceIntList = Arrays.stream(sourceIntArray).boxed().collect(Collectors.toList());

        VarFloat copy = VarFloat.copy(sourceIntArray);
        assertEquals(100, copy.rowCount());
        for (int i = 0; i < 100; i++) {
            assertEquals(sourceIntArray[i], copy.getDouble(i), TOL);
        }
        assertTrue(copy.deepEquals(VarFloat.copy(sourceIntList)));
        assertTrue(copy.deepEquals(VarFloat.copy(copy)));
        assertTrue(copy.deepEquals(VarFloat.copy(VarInt.wrap(sourceIntArray))));

        float[] sourceFloatArray = new float[100];
        for (int i = 0; i < 100; i++) {
            if (i % 10 == 0) {
                sourceFloatArray[i] = Float.NaN;
            } else {
                sourceFloatArray[i] = (float) RandomSource.nextDouble();
            }
        }
        List<Float> sourceFloatList = new ArrayList<>();
        for (float val : sourceFloatArray) {
            sourceFloatList.add(val);
        }

        VarFloat dcopy = VarFloat.copy(sourceFloatArray);
        assertEquals(100, dcopy.rowCount());
        for (int i = 0; i < dcopy.rowCount(); i++) {
            assertEquals(sourceFloatArray[i], dcopy.getDouble(i), TOL);
        }
        assertTrue(dcopy.deepEquals(VarFloat.copy(dcopy)));
        assertTrue(dcopy.deepEquals(VarFloat.wrap(sourceFloatArray)));

        Iterator<Float> it = sourceFloatList.iterator();

        VarFloat fill1 = VarFloat.fill(100);
        assertEquals(100, fill1.rowCount());
        fill1.stream().mapToDouble().forEach(val -> assertEquals(0.0, val, TOL));

        VarFloat fill2 = VarFloat.fill(100, 20);
        assertEquals(100, fill2.rowCount());
        fill2.stream().mapToDouble().forEach(val -> assertEquals(20.0, val, TOL));
        assertTrue(VarFloat.empty().deepEquals(fill2.newInstance(0)));

        VarFloat seq1 = VarFloat.seq(100);
        VarFloat seq2 = VarFloat.seq(0, 100);
        VarFloat seq3 = VarFloat.seq(0, 100, 1);

        assertTrue(seq1.deepEquals(seq2));
        assertTrue(seq1.deepEquals(seq3));
    }

    @Test
    public void smokeTest() {
        Var v = VarFloat.empty();
        boolean flag = v.type().isNumeric();
        assertTrue(flag);
        assertFalse(v.type().isNominal());

        assertEquals(0, v.rowCount());
        assertEquals("VarFloat[name:?, rowCount:1]", VarFloat.empty(1).toString());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Illegal row count: -1");
        VarFloat.empty(-1);
    }

    @Test
    public void testGetterSetter() {
        Var v = VarFloat.empty();
        for (int i = 0; i < 10; i++) {
            v.addDouble((float) Math.log(10 + i));
        }

        for (int i = 0; i < 10; i++) {
            assertEquals((float) Math.log(10 + i), v.getDouble(i), 1e-10);
            assertEquals((int) Math.rint(Math.log(10 + i)), v.getInt(i));
        }

        for (int i = 0; i < 10; i++) {
            v.setInt(i, i * i);
            assertEquals(i * i, v.getInt(i));
            assertEquals(i * i, v.getDouble(i), 1e-10);
            assertEquals(String.valueOf(v.getDouble(i)), v.getLabel(i));
        }
    }

    @Test
    public void testSetUnparsableString() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"test\"");
        VarFloat.scalar(10).setLabel(0, "test");
    }

    @Test
    public void testAddUnparsableLabel() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"x\"");
        VarFloat.scalar(10).addLabel("x");
    }

    @Test
    public void testGetLevels() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for float variables.");
        VarFloat.scalar(10).levels();
    }

    @Test
    public void testSetLeveles() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for float variables.");
        VarFloat.scalar(10).setLevels(new String[]{});
    }

    @Test
    public void testOneNumeric() {
        Var one = VarFloat.scalar((float) Math.PI);

        assertEquals(1, one.rowCount());
        assertEquals((float) Math.PI, one.getDouble(0), 1e-10);

        one = VarFloat.scalar((float) Math.E);
        assertEquals(1, one.rowCount());
        assertEquals((float) Math.E, one.getDouble(0), 1e-10);
    }

    @Test
    public void testWithName() {
        VarFloat x = VarFloat.copy(1, 2, 3, 5).withName("X");
        assertEquals("X", x.name());

        Var y = MappedVar.byRows(x, 1, 2);
        assertEquals("X", y.name());
        y.withName("y");
        assertEquals("y", y.name());

        assertEquals(2.0, y.getDouble(0), 10e-10);
        assertEquals(3.0, y.getDouble(1), 10e-10);
    }

    @Test
    public void testOtherValues() {
        VarFloat x = VarFloat.copy(1, 2, 3, 4).withName("x");

        x.addInt(10);
        assertEquals(10, x.getDouble(x.rowCount() - 1), 10e-10);

        VarFloat b = VarFloat.empty();
        b.addBoolean(true);
        b.addBoolean(false);

        assertTrue(b.getBoolean(0));
        assertFalse(b.getBoolean(1));

        assertEquals(1, b.getDouble(0), 10e-10);
        assertEquals(0, b.getDouble(1), 10e-10);

        b.setBoolean(1, true);
        assertEquals(1, b.getDouble(1), 10e-10);
        assertTrue(b.getBoolean(1));

        VarFloat s = VarFloat.empty();
        s.addLong(1);
        s.addLong(-100000000000L);
        assertEquals(1L, s.getLong(0));
        assertEquals(-100000000000f, s.getLong(1), 10e-10);

        s.setLong(1, 15);
        assertEquals(15, s.getLong(1));


        VarFloat mis = VarFloat.empty();
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
        VarFloat x = VarFloat.copy(1, 2, 3);
        VarFloat x2 = VarFloat.copy(x);
        x.remove(1);

        assertEquals(1, x.getInt(0));
        assertEquals(3, x.getInt(1));

        VarFloat y = x.solidCopy();

        x.clear();

        assertEquals(0, x.rowCount());

        assertEquals(2, y.rowCount());
        assertEquals(1, y.getInt(0));
        assertEquals(3, y.getInt(1));

        x2.addRows(3);

        assertEquals(6, x2.rowCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, x2.getDouble(i), TOL);
            assertTrue(x2.isMissing(i + 3));
        }
    }

    @Test
    public void testLabelOperations() {
        VarFloat var = VarFloat.wrap(1.0f, 1.0f, 1.0f, 1.0f);

        var.setLabel(0, "?");
        var.setLabel(1, "Inf");
        var.setLabel(2, "-Inf");
        var.setLabel(3, "-10.3");

        var.addLabel("?");
        var.addLabel("Inf");
        var.addLabel("-Inf");
        var.addLabel("-10.3");

        float[] expected = new float[]{
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -10.3f,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -10.3f};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], var.getDouble(i), TOL);
        }
    }

    @Test
    public void testCollector() {
        List<Float> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add((float) (RandomSource.nextDouble() * 100));
        }
        VarFloat copy = list.stream().collect(VarFloat.collector());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), copy.getDouble(i), TOL);
        }
    }
}
