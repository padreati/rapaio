/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.stat.Sum;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;
import rapaio.util.Pair;

import java.util.Comparator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class VSpotsTest {

    private static final double TOL = 1e-10;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testBuilders() {
        VarDouble x = VarDouble.seq(10);
        Var c1 = x.stream().toMappedVar();
        assertTrue(x.deepEquals(c1));
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, RandomSource::nextDouble);
        assertEquals(100, x.stream().count());

        assertTrue(x.deepEquals(x.stream().toMappedVar()));
        assertTrue(x.deepEquals(VarDouble.wrap(x.stream().mapToDouble().toArray())));

        assertTrue(x.deepEquals(x.stream().findFirst().orElseGet(() -> new VSpot(0, null)).rvar()));

        Var sorted1 = x.stream().sorted().toMappedVar();
        Var sorted2 = x.stream().sorted(Comparator.comparingDouble(VSpot::getDouble)).toMappedVar();
        for (int i = 1; i < sorted1.size(); i++) {
            assertTrue(sorted1.getDouble(i - 1) <= sorted1.getDouble(i));
            assertTrue(sorted2.getDouble(i - 1) <= sorted2.getDouble(i));
            assertTrue(sorted1.getInt(i - 1) <= sorted1.getInt(i));
            assertTrue(sorted1.getLong(i - 1) <= sorted1.getLong(i));
        }

        assertEquals(100, x.stream().count());
        assertEquals(100, x.stream().distinct().count());

        double[] a1 = x.stream().mapToDouble().toArray();
        double[] a2 = x.stream().peek(s -> s.setDouble(s.getDouble() + 1)).peek(s -> s.setDouble(s.getDouble() - 1)).mapToDouble().toArray();

        assertArrayEquals(a1, a2, TOL);

        int[] a3 = SolidFrame.byVars(VarInt.seq(10)).stream().skip(1).limit(2).mapToInt(s -> s.getInt(0)).toArray();
        assertArrayEquals(new int[]{1, 2}, a3);
    }

    @Test
    void testFilter() {
        Frame x = SolidFrame.byVars(VarDouble.from(100, () -> RandomSource.nextDouble() - 0.5));
        x.stream().filter(s -> s.getDouble(0) >= 0).forEach(s -> assertTrue(s.getDouble(0) >= 0));
    }

    @Test
    void testMappers() {

        Frame x = SolidFrame.byVars(VarDouble.seq(100));
        assertEquals(5050, x.stream().map(s -> s.getInt(0)).mapToDouble(a -> a).sum(), TOL);
        assertEquals(5050, x.stream().mapToInt(s -> s.getInt(0)).sum());
    }

    @Test
    void testFlatMap() {
        Frame x = SolidFrame.byVars(VarDouble.seq(10));
        assertEquals(165, x.stream().flatMap(s -> IntStream.range(0, s.getInt(0)).boxed()).mapToInt(s -> s).sum());
        assertEquals(165, x.stream().flatMapToInt(s -> IntStream.range(0, s.getInt(0))).sum());
        assertEquals(165, x.stream().flatMapToLong(s -> LongStream.range(0, s.getInt(0))).sum());
        assertEquals(165, x.stream().flatMapToDouble(s -> DoubleStream.iterate(0, a -> a + 1).limit(s.getInt(0))).sum(), TOL);

        assertTrue(x.stream().parallel().isParallel());
        assertFalse(x.stream().sequential().isParallel());
    }

    @Test
    void testCollect() {
        Frame x = SolidFrame.byVars(VarInt.seq(10));
        Object[] a1 = x.stream().toArray();
        Object[] a2 = x.stream().toArray(Object[]::new);

        for (int i = 0; i < 10; i++) {
            assertEquals(a1[i], a2[i]);
        }
    }

    @Test
    void testSpliterator() {
        Frame x = SolidFrame.byVars(VarDouble.from(10_000, RandomSource::nextDouble));
        double sum1 = x.stream().parallel().mapToDouble(s -> s.getDouble(0)).sum();
        double sum3 = x.stream().sequential().mapToDouble(s -> s.getDouble(0)).sum();
        double sum2 = Sum.of(x.rvar(0)).value();
        assertEquals(sum2, sum1, TOL);
        assertEquals(sum3, sum1, TOL);
    }

    @Test
    void testClose() {
        Pair<Integer, Integer> pair = Pair.from(1, 2);
        SolidFrame.byVars(VarDouble.wrap(1, 2, 3, 4)).stream().onClose(() -> pair.v1 = 3).close();
        assertEquals(3, pair.v1.intValue());
    }

    @Test
    void testComplete() {
        Frame index = SolidFrame.byVars(VarInt.from(10, row -> row % 2 == 0 ? VarInt.MISSING_VALUE : row));
        assertEquals(5, index.stream().complete().count());
        assertEquals(5, index.stream().incomplete().count());
    }

    @Test
    void testSorted() {

        Var s1 = VarDouble.from(100, RandomSource::nextDouble).stream().sorted().toMappedVar();
        for (int i = 1; i < s1.size(); i++) {
            assertTrue(s1.getDouble(i - 1) <= s1.getDouble(i));
        }

        Var s2 = VarInt.from(100, row -> RandomSource.nextInt(100)).stream().sorted().toMappedVar();
        for (int i = 1; i < s1.size(); i++) {
            assertTrue(s1.getInt(i - 1) <= s1.getInt(i));
        }

        Var s3 = VarLong.from(100, row -> (long) (RandomSource.nextInt(100)))
                .stream().sorted().toMappedVar();
        for (int i = 1; i < s1.size(); i++) {
            assertTrue(s1.getLong(i - 1) <= s1.getLong(i));
        }

        Var s4 = VarBinary.from(100, row -> RandomSource.nextDouble() > 0.5)
                .stream().sorted().toMappedVar();
        for (int i = 1; i < s1.size(); i++) {
            assertTrue(s1.getInt(i - 1) <= s1.getInt(i));
        }

        String[] words = new String[]{"ana", "are", "mere", "galbene"};
        Var s5 = VarNominal.from(100, row -> words[RandomSource.nextInt(words.length)])
                .stream().sorted().toMappedVar();

        for (int i = 1; i < s5.size(); i++) {
            assertTrue(s5.getLabel(i - 1).compareTo(s5.getLabel(i)) <= 0);
        }
    }

    @Test
    void testVSpot() {
        Var wrap = VarDouble.wrap(1, 2);
        VSpot spot = new VSpot(1, wrap);

        assertFalse(spot.isMissing());

        assertEquals(2.0, spot.getDouble(), TOL);
        spot.setDouble(3);
        assertEquals(3, spot.getDouble(), TOL);

        spot.setInt(0);
        assertEquals(0, spot.getInt());

        spot.setLabel("0");
        assertEquals("0.0", spot.getLabel());

        spot.setInt(0);
        assertEquals(0, spot.getInt());

        spot.setLong(17);
        assertEquals(17, spot.getLong());

        spot.setLabel("12");
        assertEquals("12.0", spot.getLabel());

        spot.setMissing();
        assertTrue(spot.isMissing());

        VSpot copy = new VSpot(1, wrap);
        assertEquals(copy, spot);
    }
}
