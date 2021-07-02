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

package rapaio.util.collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.core.tests.TTestTwoSamples;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.sys.WS;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/23/21.
 */
public class IntOpenHashSetTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void performanceTest() {

        VarDouble timeHash = VarDouble.empty().name("hash");
        VarDouble timeOpen = VarDouble.empty().name("open");

        final int TIMES = 100;
        final int SKIP = 10;
        final int RANGE = 100_000;
        for (int i = 0; i < TIMES; i++) {

            VarInt x = VarInt.from(RANGE, row -> RandomSource.nextInt(RANGE));

            HashSet<Integer> hashSet = new HashSet<>();

            long start = System.currentTimeMillis();
            for (int value : x) {
                hashSet.add(value);
            }
            long stop = System.currentTimeMillis();
            if (i >= SKIP) {
                timeHash.addDouble(stop - start);
            }

            IntOpenHashSet openSet = new IntOpenHashSet();

            start = System.currentTimeMillis();
            for (int value : x) {
                openSet.add(value);
            }
            stop = System.currentTimeMillis();
            if (i >= SKIP) {
                timeOpen.addDouble(stop - start);
            }

            assertEquals(hashSet.size(), openSet.size());
        }

        WS.println("HashSet time statistics:");
        var m1 = Mean.of(timeHash);
        var v1 = Variance.of(timeHash);

        WS.println("IntOpenHashSet time statistics:");
        var m2 = Mean.of(timeOpen);
        var v2 = Variance.of(timeOpen);

        TTestTwoSamples.test(m1.value(), TIMES - SKIP, m2.value(), TIMES - SKIP, 0, v1.sdValue(), v2.sdValue())
                .printSummary();

    }

    @Test
    void testEmpty() {
        IntOpenHashSet set = new IntOpenHashSet();
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());

        set.add(11);
        set.add(10);

        assertEquals(2, set.size());
        assertTrue(set.contains(10));
        assertTrue(set.contains(11));
        set.clear();

        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    @Test
    void containsTest() {
        int[] array = IntArrays.newSeq(0, 1_000_000);
        IntArrays.shuffle(array, RandomSource.getRandom());

        int[] in = IntArrays.copy(array, 0, 500_000);
        int[] out = IntArrays.copy(array, 500_000, 500_000);

        IntOpenHashSet set = new IntOpenHashSet();
        set.addAll(IntStream.of(in).boxed().collect(Collectors.toList()));

        for (int x : in) {
            assertTrue(set.contains(x));
        }
        for (int x : out) {
            assertFalse(set.contains(x));
        }

        Set<Integer> inSet = IntStream.of(in).boxed().collect(Collectors.toSet());
        for (int value : set) {
            assertTrue(inSet.contains(value));
        }
        for (int value : set.toArray()) {
            assertTrue(inSet.contains(value));
        }
    }

    @Test
    void testUnsupported() {
        var set = new IntOpenHashSet();
        HashSet<Integer> set1 = new HashSet<>();
        set1.add(null);
        assertThrows(ClassCastException.class, () -> set.addAll(set1));
    }
}
