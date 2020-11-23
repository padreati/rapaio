package rapaio.data.stream;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.stat.Sum;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/27/18.
 */
public class FSpotsTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testBuilders() {
        Frame x = SolidFrame.byVars(VarDouble.seq(10));
        Frame c1 = x.stream().toMappedFrame();
        assertTrue(x.deepEquals(c1));
    }

    @Test
    void testDouble() {
        Frame x = SolidFrame.byVars(VarDouble.from(100, RandomSource::nextDouble));
        assertEquals(100, x.stream().count());

        assertTrue(x.deepEquals(x.stream().toMappedFrame()));

        assertEquals(100, x.stream().count());
        assertEquals(100, x.stream().distinct().count());

        double[] a1 = x.stream().mapToDouble(s -> s.getDouble(0)).toArray();
        double[] a2 = x.stream().peek(s -> s.setDouble(0, s.getDouble(0) + 1)).peek(s -> s.setDouble(0, s.getDouble(0) - 1))
                .mapToDouble(s -> s.getDouble(0)).toArray();

        assertArrayEquals(a1, a2, TOL);

        int[] a3 = VarInt.seq(10).stream().skip(1).limit(2).mapToInt().toArray();
        assertArrayEquals(new int[]{1, 2}, a3);

        List<Double> l1 = new ArrayList<>();
        VarDouble.from(100, RandomSource::nextDouble).stream().sorted().forEachOrdered(s -> l1.add(s.getDouble()));
        for (int i = 1; i < l1.size(); i++) {
            assertTrue(l1.get(i - 1) <= l1.get(i));
        }

    }

    @Test
    void testFilter() {
        VarDouble x = VarDouble.from(100, () -> RandomSource.nextDouble() - 0.5);
        x.stream().filter(s -> s.getDouble() >= 0).forEach(s -> assertTrue(s.getDouble() >= 0));
        x.stream().unordered().filterValue(v -> v >= 0).forEach(s -> assertTrue(s.getDouble() >= 0.0));
    }

    @Test
    void testMappers() {

        VarDouble x = VarDouble.seq(100);
        assertEquals(5050, x.stream().map(VSpot::getInt).mapToDouble(a -> a).sum(), TOL);
        assertEquals(5050, x.stream().mapToInt().sum());
        assertEquals(5050, x.stream().mapToInt(VSpot::getInt).sum());
        assertEquals(5050, x.stream().mapToLong(VSpot::getLong).sum());

        assertEquals(395, x.stream().mapToString().mapToInt(String::length).sum());
    }

    @Test
    void testFlatMap() {
        VarDouble x = VarDouble.seq(10);
        assertEquals(165, x.stream().flatMap(s -> IntStream.range(0, s.getInt()).boxed()).mapToInt(s -> s).sum());
        assertEquals(165, x.stream().flatMapToInt(s -> IntStream.range(0, s.getInt())).sum());
        assertEquals(165, x.stream().flatMapToLong(s -> LongStream.range(0, s.getInt())).sum());
        assertEquals(165, x.stream().flatMapToDouble(s -> DoubleStream.iterate(0, a -> a + 1).limit(s.getInt())).sum(), TOL);

        assertTrue(x.stream().parallel().isParallel());
        assertFalse(x.stream().sequential().isParallel());
    }

    @Test
    void testCollect() {
        Var x = VarInt.seq(10);
        Object[] a1 = x.stream().toArray();
        Object[] a2 = x.stream().toArray(Object[]::new);

        for (int i = 0; i < 10; i++) {
            assertEquals(a1[i], a2[i]);
        }
    }

    @Test
    void testSpliterator() {
        VarDouble x = VarDouble.from(10_000, RandomSource::nextDouble);
        double sum1 = x.stream().parallel().mapToDouble().sum();
        double sum3 = x.stream().sequential().mapToDouble().sum();
        double sum2 = Sum.of(x).value();
        assertEquals(sum2, sum1, TOL);
        assertEquals(sum3, sum1, TOL);
    }

    @Test
    void testClose() {
        Pair<Integer, Integer> pair = Pair.from(1, 2);
        VarDouble.wrap(1, 2, 3, 4).stream().onClose(() -> pair.v1 = 3).close();
        assertEquals(3, pair.v1.intValue());
    }

    @Test
    void testComplete() {
        Var index = VarInt.from(10, row -> row % 2 == 0 ? VarInt.MISSING_VALUE : row);
        assertEquals(5, index.stream().complete().count());
        assertEquals(5, index.stream().incomplete().count());
    }

    @Test
    void testGettersSetters() {
        Frame df = SolidFrame.byVars(
                VarDouble.wrap(1, 2).name("x"),
                VarNominal.copy("a", "b").name("y"));

        FSpot spot = new FSpot(df, 1);

        assertFalse(spot.isMissing());
        assertFalse(spot.isMissing(0));

        assertEquals(2.0, spot.getDouble(0), TOL);
        spot.setDouble(0, 3);
        assertEquals(3, spot.getDouble(0), TOL);
        spot.setDouble("x", 4);
        assertEquals(4, spot.getDouble(0), TOL);

        spot.setInt(0, 0);
        assertEquals(0, spot.getInt(0));
        spot.setInt("x", 1);
        assertEquals(1, spot.getInt("x"));

        spot.setLabel(0, "0");
        assertEquals("0.0", spot.getLabel(0));
        spot.setLabel("x", "1");
        assertEquals("1.0", spot.getLabel("x"));

        spot.setInt(0, 0);
        assertEquals(0, spot.getInt(0));
        spot.setInt("x", 1);
        assertEquals(1, spot.getInt("x"));

        String[] levels1 = spot.levels(1).toArray(new String[0]);
        String[] levels2 = spot.levels("y").toArray(new String[0]);
        String[] levels3 = new String[]{"?", "a", "b"};

        assertArrayEquals(levels1, levels2);
        assertArrayEquals(levels2, levels3);

        spot.setMissing(1);
        assertTrue(spot.isMissing(1));
        spot.setLabel(1, "a");
        assertEquals("a", spot.getLabel(1));
        spot.setMissing("y");
        assertTrue(spot.isMissing("y"));
    }
}
