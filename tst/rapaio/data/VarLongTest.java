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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/19/18.
 */
public class VarLongTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(134);
    }

    @Test
    public void testEmptyWithNoRows() {
        VarLong empty = VarLong.empty();
        assertEquals(0, empty.rowCount());
    }

    @Test
    public void testVarEmptyWithRows() {
        VarLong empty = VarLong.empty(100);
        assertEquals(100, empty.rowCount());
        for (int i = 0; i < 100; i++) {
            assertTrue(empty.isMissing(i));
        }
    }

    @Test
    public void testStaticBuilders() {
        int[] sourceIntArray = IntStream.range(0, 100).map(i -> (i % 10 == 0) ? Integer.MIN_VALUE : RandomSource.nextInt(100)).toArray();
        List<Integer> sourceIntList = Arrays.stream(sourceIntArray).boxed().collect(Collectors.toList());

        VarLong copy = VarLong.copy(sourceIntArray);
        assertEquals(100, copy.rowCount());
        for (int i = 0; i < 100; i++) {
            assertEquals((double) sourceIntArray[i], copy.getLong(i), TOL);
        }
        assertTrue(copy.deepEquals(VarLong.copy(sourceIntList)));
        assertTrue(copy.deepEquals(VarLong.copy(copy)));
        assertTrue(copy.deepEquals(VarLong.copy(VarInt.wrap(sourceIntArray))));

        long[] sourceLongArray = IntStream.range(0, 100).mapToLong(i -> (i % 10 == 0) ? Long.MIN_VALUE : RandomSource.nextInt(100)).toArray();
        List<Long> sourceLongList = Arrays.stream(sourceLongArray).boxed().collect(Collectors.toList());

        VarLong dcopy = VarLong.copy(sourceLongArray);
        assertEquals(100, dcopy.rowCount());
        for (int i = 0; i < dcopy.rowCount(); i++) {
            assertEquals(sourceLongArray[i], dcopy.getLong(i));
        }
        assertTrue(dcopy.deepEquals(VarLong.copy(dcopy)));
        assertTrue(dcopy.deepEquals(VarLong.wrap(sourceLongArray)));
        assertTrue(dcopy.deepEquals(VarLong.from(100, dcopy::getLong)));

        Iterator<Long> it = sourceLongList.iterator();
        assertTrue(dcopy.deepEquals(VarLong.from(100, it::next)));
        assertTrue(dcopy.deepEquals(VarLong.from(dcopy, val -> val)));

        VarLong fill1 = VarLong.fill(100);
        assertEquals(100, fill1.rowCount());
        fill1.stream().mapToDouble().forEach(val -> assertEquals(0.0, val, TOL));

        VarLong fill2 = VarLong.fill(100, 20);
        assertEquals(100, fill2.rowCount());
        fill2.stream().mapToDouble().forEach(val -> assertEquals(20.0, val, TOL));
        assertTrue(VarLong.empty().deepEquals(fill2.newInstance(0)));

        VarLong seq1 = VarLong.seq(100);
        VarLong seq2 = VarLong.seq(0, 100);
        VarLong seq3 = VarLong.seq(0, 100, 1);

        assertTrue(seq1.deepEquals(seq2));
        assertTrue(seq1.deepEquals(seq3));
    }

    @Test
    public void smokeTest() {
        Var v = VarLong.empty();
        boolean flag = v.type().isNumeric();
        assertFalse(flag);
        assertFalse(v.type().isNominal());

        assertEquals(0, v.rowCount());
        assertEquals("VarLong[name:?, rowCount:1]", VarLong.empty(1).toString());

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Illegal row count: -1");
        VarLong.empty(-1);
    }

    @Test
    public void testGetterSetter() {
        Var v = VarLong.from(10, i -> (long) Math.log(10 + i));

        for (int i = 0; i < 10; i++) {
            assertEquals((long) Math.log(10 + i), v.getLong(i), 1e-10);
        }

        for (int i = 0; i < 10; i++) {
            v.setInt(i, i * i);
            assertEquals(i * i, v.getInt(i));
            assertEquals(i * i, v.getLong(i), 1e-10);
            assertEquals(String.valueOf(v.getLong(i)), v.getLabel(i));
        }
    }

    @Test
    public void testSetUnparsableString() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"test\"");
        VarLong.scalar(10).setLabel(0, "test");
    }

    @Test
    public void testAddUnparsableLabel() {
        expectedException.expect(NumberFormatException.class);
        expectedException.expectMessage("For input string: \"x\"");
        VarLong.scalar(10).addLabel("x");
    }

    @Test
    public void testGetLevels() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for long variable");
        VarLong.scalar(10).levels();
    }

    @Test
    public void testSetLeveles() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Operation not available for long variable");
        VarLong.scalar(10).setLevels(new String[]{});
    }

    @Test
    public void testOneNumeric() {
        Var one = VarLong.scalar((long) Math.PI);

        assertEquals(1, one.rowCount());
        assertEquals((long) Math.PI, one.getLong(0), 1e-10);

        one = VarLong.scalar((long) Math.E);
        assertEquals(1, one.rowCount());
        assertEquals((long) Math.E, one.getLong(0), 1e-10);

        one.setDouble(0, Math.E);
        assertEquals((long) Math.rint(Math.E), one.getLong(0));
    }

    @Test
    public void testWithName() {
        VarLong x = VarLong.copy(1, 2, 3, 5).withName("X");
        assertEquals("X", x.name());

        Var y = MappedVar.byRows(x, 1, 2);
        assertEquals("X", y.name());
        y.withName("y");
        assertEquals("y", y.name());

        assertEquals(2.0, y.getLong(0), 10e-10);
        assertEquals(3.0, y.getLong(1), 10e-10);
    }

    @Test
    public void testOtherValues() {
        VarLong x = VarLong.copy(1, 2, 3, 4).withName("x");

        x.addInt(10);
        assertEquals(10, x.getLong(x.rowCount() - 1), 10e-10);

        VarLong b = VarLong.empty();
        b.addBoolean(true);
        b.addBoolean(false);

        assertTrue(b.getBoolean(0));
        assertFalse(b.getBoolean(1));

        assertEquals(1, b.getLong(0), 10e-10);
        assertEquals(0, b.getLong(1), 10e-10);

        b.setBoolean(1, true);
        assertEquals(1, b.getLong(1), 10e-10);
        assertTrue(b.getBoolean(1));

        VarLong s = VarLong.empty();
        s.addLong(1);
        s.addLong(-100000000000L);
        assertEquals(1L, s.getLong(0));
        assertEquals(-100000000000d, s.getLong(1), 10e-10);

        s.setLong(1, 15);
        assertEquals(15, s.getLong(1));


        VarLong mis = VarLong.empty();
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
        VarLong x = VarLong.copy(1, 2, 3);
        VarLong x2 = VarLong.copy(x);
        x.removeRow(1);

        assertEquals(1, x.getInt(0));
        assertEquals(3, x.getInt(1));

        VarLong y = x.solidCopy();

        x.clearRows();

        assertEquals(0, x.rowCount());

        assertEquals(2, y.rowCount());
        assertEquals(1, y.getInt(0));
        assertEquals(3, y.getInt(1));

        x2.addRows(3);

        assertEquals(6, x2.rowCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, x2.getLong(i), TOL);
            assertTrue(x2.isMissing(i + 3));
        }
    }

    @Test
    public void testLabelOperations() {
        VarLong var = VarLong.wrap(1, 1, 1, 1);

        var.setLabel(0, "?");
        var.setLabel(1, "0");
        var.setLabel(2, "-10");
        var.setLabel(3, "+10");

        var.addLabel("?");
        var.addLabel("0");
        var.addLabel("-10");
        var.addLabel("+10");

        long[] expected = new long[]{
                Long.MIN_VALUE, 0, -10, 10,
                Long.MIN_VALUE, 0, -10, 10};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], var.getLong(i));
        }
    }

    @Test
    public void testCollector() {
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add((long) RandomSource.nextDouble() * 100);
        }
        VarLong copy = list.stream().parallel().collect(VarLong.collector());
        long sum1 = 0;
        long sum2 = 0;
        for (int i = 0; i < list.size(); i++) {
            sum1 += list.get(i);
            sum2 += copy.getLong(i);
        }
        assertEquals(sum1, sum2);
    }
}
