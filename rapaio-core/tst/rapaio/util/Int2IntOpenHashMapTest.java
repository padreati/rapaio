/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.util.collection.Int2IntOpenHashMap;
import rapaio.util.collection.IntArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/21.
 */
public class Int2IntOpenHashMapTest {

    private static final int N = 100_000;
    private int[] x;
    private int[] y;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
        x = IntArrays.newFrom(N, row -> random.nextInt(N) - N / 2);
        y = IntArrays.newFrom(N, row -> random.nextInt(N) - N / 2);
    }

    @Test
    void smokeTest() {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        for (int i = 0; i < N; i++) {
            map.put(x[i], y[i]);
            assertTrue(map.containsKey(x[i]));
            assertEquals(y[i], map.get(x[i]));
        }
    }


    @Test
    void testProbings() {
        Int2IntOpenHashMap map1 = new Int2IntOpenHashMap(random.nextInt(), 0.75, 12, Int2IntOpenHashMap.Probing.LINEAR);
        Int2IntOpenHashMap map2 = new Int2IntOpenHashMap(random.nextInt(), 0.75, 12, Int2IntOpenHashMap.Probing.QUADRATIC);

        for (int i = 0; i < 1_000; i++) {
            int x = random.nextInt(10_000) - 5_000;
            int y = random.nextInt(10_000);

            map1.put(x, y);
            map2.put(x, y);

            assertEquals(map1.size(), map2.size());

            assertTrue(map1.containsKey(x));
            assertTrue(map2.containsKey(x));

            for (int j = 0; j < 10; j++) {
                int next = random.nextInt(100_000);
                assertEquals(map1.containsKey(next), map2.containsKey(next));
                assertEquals(map1.get(next), map2.get(next));
            }

            for (int xs : map1.keySet()) {
                assertTrue(map1.containsKey(xs));
                assertTrue(map2.containsKey(xs));
            }
        }
    }
}
