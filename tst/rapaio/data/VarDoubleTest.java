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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
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
public class VarDoubleTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(134);
    }

    @Test
    public void testEmptyWithNoRows() {
        VarDouble empty = VarDouble.empty();
        assertEquals(0, empty.rowCount());
    }

    @Test
    public void testVarEmptyWithRows() {
        VarDouble empty = VarDouble.empty(100);
        assertEquals(100, empty.rowCount());
        for (int i = 0; i < 100; i++) {
            assertTrue(empty.isMissing(i));
        }
    }

    @Test
    public void testStaticBuilders() {
        int[] sourceIntArray = IntStream.range(0, 100).map(i -> (i % 10 == 0) ? Integer.MIN_VALUE : RandomSource.nextInt(100)).toArray();
        List<Integer> sourceIntList = Arrays.stream(sourceIntArray).boxed().collect(Collectors.toList());

        VarDouble copy = VarDouble.copy(sourceIntArray);
        assertEquals(100, copy.rowCount());
        for (int i = 0; i < 100; i++) {
            assertEquals((double) sourceIntArray[i], copy.getDouble(i), TOL);
        }
        assertTrue(copy.deepEquals(VarDouble.copy(sourceIntList)));
        assertTrue(copy.deepEquals(VarDouble.copy(copy)));
        assertTrue(copy.deepEquals(VarDouble.copy(VarInt.wrap(sourceIntArray))));

        double[] sourceDoubleArray = IntStream.range(0, 100).mapToDouble(i -> (i % 10 == 0) ? Double.NaN : RandomSource.nextDouble()).toArray();
        List<Double> sourceDoubleList = Arrays.stream(sourceDoubleArray).boxed().collect(Collectors.toList());

        VarDouble dcopy = VarDouble.copy(sourceDoubleArray);
        assertEquals(100, dcopy.rowCount());
        for (int i = 0; i < dcopy.rowCount(); i++) {
            assertEquals(sourceDoubleArray[i], dcopy.getDouble(i), TOL);
        }
        assertTrue(dcopy.deepEquals(VarDouble.copy(dcopy)));
        assertTrue(dcopy.deepEquals(VarDouble.wrap(sourceDoubleArray)));
        assertTrue(dcopy.deepEquals(VarDouble.wrap(new DoubleArrayList(sourceDoubleArray))));
        assertTrue(dcopy.deepEquals(VarDouble.from(100, dcopy::getDouble)));

        Iterator<Double> it = sourceDoubleList.iterator();
        assertTrue(dcopy.deepEquals(VarDouble.from(100, it::next)));
        assertTrue(dcopy.deepEquals(VarDouble.from(dcopy, val -> val)));

        VarDouble fill1 = VarDouble.fill(100);
        assertEquals(100, fill1.rowCount());
        fill1.stream().mapToDouble().forEach(val -> assertEquals(0.0, val, TOL));

        VarDouble fill2 = VarDouble.fill(100, 20);
        assertEquals(100, fill2.rowCount());
        fill2.stream().mapToDouble().forEach(val -> assertEquals(20.0, val, TOL));
        assertTrue(VarDouble.empty().deepEquals(fill2.newInstance(0)));

        VarDouble seq1 = VarDouble.seq(100);
        VarDouble seq2 = VarDouble.seq(0, 100);
        VarDouble seq3 = VarDouble.seq(0, 100, 1);

        assertTrue(seq1.deepEquals(seq2));
        assertTrue(seq1.deepEquals(seq3));
    }

    @Test
    public void smokeTest() {
        Var v = VarDouble.empty();
        boolean flag = v.type().isNumeric();
        assertTrue(flag);
        assertFalse(v.type().isNominal());

        assertEquals(0, v.rowCount());
        assertEquals("VarDouble[name:?, rowCount:1]", VarDouble.empty(1).toString());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Illegal row count: -1");
        VarDouble.empty(-1);
    }

    @Test
    public void testGetterSetter() {
        Var v = VarDouble.from(10, i -> Math.log(10 + i));

        for (int i = 0; i < 10; i++) {
            assertEquals(Math.log(10 + i), v.getDouble(i), 1e-10);
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
        VarDouble.scalar(10).setLabel(0, "test");
    }

    @Test
    public void testAddUnparsableLabel() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"x\"");
        VarDouble.scalar(10).addLabel("x");
    }

    @Test
    public void testGetLevels() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for double vectors.");
        VarDouble.scalar(10).levels();
    }

    @Test
    public void testSetLeveles() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for double vectors.");
        VarDouble.scalar(10).setLevels(new String[]{});
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
    public void testOtherValues() {
        VarDouble x = VarDouble.copy(1, 2, 3, 4).withName("x");

        x.addInt(10);
        assertEquals(10, x.getDouble(x.rowCount() - 1), 10e-10);

        VarDouble b = VarDouble.empty();
        b.addBoolean(true);
        b.addBoolean(false);

        assertTrue(b.getBoolean(0));
        assertFalse(b.getBoolean(1));

        assertEquals(1, b.getDouble(0), 10e-10);
        assertEquals(0, b.getDouble(1), 10e-10);

        b.setBoolean(1, true);
        assertEquals(1, b.getDouble(1), 10e-10);
        assertTrue(b.getBoolean(1));

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
        VarDouble x2 = VarDouble.copy(x);
        x.removeRow(1);

        assertEquals(1, x.getInt(0));
        assertEquals(3, x.getInt(1));

        VarDouble y = x.solidCopy();

        x.clearRows();

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
        VarDouble var = VarDouble.wrap(1.0, 1.0, 1.0, 1.0);

        var.setLabel(0, "?");
        var.setLabel(1, "Inf");
        var.setLabel(2, "-Inf");
        var.setLabel(3, "-10.3");

        var.addLabel("?");
        var.addLabel("Inf");
        var.addLabel("-Inf");
        var.addLabel("-10.3");

        double[] expected = new double[]{
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -10.3,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -10.3};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], var.getDouble(i), TOL);
        }
    }

    @Test
    public void testCollector() {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(RandomSource.nextDouble()*100);
        }
        VarDouble copy = list.stream().collect(VarDouble.collector());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), copy.getDouble(i), TOL);
        }
    }
}
