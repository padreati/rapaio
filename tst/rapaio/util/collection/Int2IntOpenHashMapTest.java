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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.core.tests.TTestTwoSamples;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/21.
 */
public class Int2IntOpenHashMapTest {

    private static final int N = 100_000;
    private VarInt x;
    private VarInt y;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
        x = VarInt.from(N, row -> RandomSource.nextInt(N) - N / 2);
        y = VarInt.from(N, row -> RandomSource.nextInt(N) - N / 2);
    }

    @Test
    void smokeTest() {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        for (int i = 0; i < N; i++) {
            map.put(x.getInt(i), y.getInt(i));
            assertTrue(map.containsKey(x.getInt(i)));
            assertEquals(y.getInt(i), map.get(x.getInt(i)));
        }
    }

    @Test
    void performanceTest() {

        VarDouble timeHash = VarDouble.empty().name("hash");
        VarDouble timeOpen = VarDouble.empty().name("open");

        final int TIMES = 200;
        final int SKIP = 20;
        final int RANGE = 20_000;
        for (int t = 0; t < TIMES; t++) {

            VarInt x = VarInt.from(RANGE, row -> RandomSource.nextInt(RANGE));
            VarInt y = VarInt.from(RANGE, row -> RandomSource.nextInt(RANGE));

            HashMap<Integer, Integer> hashMap = new HashMap<>();

            long start = System.currentTimeMillis();
            for (int i = 0; i < x.size(); i++) {
                hashMap.put(x.getInt(i), y.getInt(i));
            }
            long stop = System.currentTimeMillis();
            if (t >= SKIP) {
                timeHash.addDouble(stop - start);
            }

            Int2IntOpenHashMap openMap = new Int2IntOpenHashMap();

            start = System.currentTimeMillis();
            for (int i = 0; i < x.size(); i++) {
                openMap.put(x.getInt(i), y.getInt(i));
            }
            stop = System.currentTimeMillis();
            if (t >= SKIP) {
                timeOpen.addDouble(stop - start);
            }

            assertEquals(hashMap.size(), openMap.size());
        }

        var m1 = Mean.of(timeHash);
        var v1 = Variance.of(timeHash);

        var m2 = Mean.of(timeOpen);
        var v2 = Variance.of(timeOpen);

        TTestTwoSamples.test(m1.value(), TIMES - SKIP, m2.value(), TIMES - SKIP, 0, v1.sdValue(), v2.sdValue());

    }

    @Test
    void testProbings() {
        Int2IntOpenHashMap map1 = new Int2IntOpenHashMap(0.75, 12, Int2IntOpenHashMap.Probing.LINEAR);
        Int2IntOpenHashMap map2 = new Int2IntOpenHashMap(0.75, 12, Int2IntOpenHashMap.Probing.QUADRATIC);

        for (int i = 0; i < 1_000; i++) {
            int x = RandomSource.nextInt(10_000) - 5_000;
            int y = RandomSource.nextInt(10_000);

            map1.put(x, y);
            map2.put(x, y);

            assertEquals(map1.size(), map2.size());

            assertTrue(map1.containsKey(x));
            assertTrue(map2.containsKey(x));

            for (int j = 0; j < 10; j++) {
                int next = RandomSource.nextInt(100_000);
                assertEquals(map1.containsKey(next), map2.containsKey(next));
                assertEquals(map1.get(next), map2.get(next));
            }

            for (int xs : map1.keySet()) {
                assertTrue(map1.containsKey(xs));
                assertTrue(map2.containsKey(xs));
            }
        }
    }

    @Test
    void testValues() {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        VarInt x = VarInt.wrap(SamplingTools.sampleWOR(100, 100));
        VarInt y = VarInt.seq(100);

        for (int i = 0; i < 100; i++) {
            map.put(x.getInt(i), y.getInt(i));

            Var values = map.values().stream().collect(VarInt.collector());
            values.dVec().sortValues();
            assertEquals(i + 1, values.size());
            for (int j = 0; j < values.size(); j++) {
                assertEquals(y.getInt(i), values.getInt(i));
            }
        }


    }
}
