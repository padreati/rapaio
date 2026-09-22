/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import rapaio.util.collection.Int2IntOpenHashMap;
import rapaio.util.collection.IntOpenHashSet;

/**
 * Regression tests for the open addressing structures: probing must terminate on any table, the empty-slot
 * marker must not restrict the value domain, and tiny allocations must not break growth.
 */
public class OpenHashRegressionTest {

    @ParameterizedTest
    @EnumSource(IntOpenHashSet.Probing.class)
    void setProbingTerminatesWhenFilledToTheLoadFactor(IntOpenHashSet.Probing probing) {
        // a high load factor keeps the table nearly full, the case where cumulative quadratic probing used to spin
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int seed = 1; seed <= 20; seed++) {
                IntOpenHashSet set = new IntOpenHashSet(seed, 0.95, 3, probing);
                for (int i = 0; i < 5000; i++) {
                    assertTrue(set.add(i * 7919));
                }
                for (int i = 0; i < 5000; i++) {
                    assertTrue(set.contains(i * 7919));
                    assertFalse(set.contains(i * 7919 + 1));
                }
                assertEquals(5000, set.size());
            }
        });
    }

    @ParameterizedTest
    @EnumSource(Int2IntOpenHashMap.Probing.class)
    void mapProbingTerminatesWhenFilledToTheLoadFactor(Int2IntOpenHashMap.Probing probing) {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int seed = 1; seed <= 20; seed++) {
                Int2IntOpenHashMap map = new Int2IntOpenHashMap(seed, 0.95, 3, probing);
                for (int i = 0; i < 5000; i++) {
                    map.put(i * 7919, -i);
                }
                for (int i = 0; i < 5000; i++) {
                    assertTrue(map.containsKey(i * 7919));
                    assertEquals(-i, map.get(i * 7919));
                    assertFalse(map.containsKey(i * 7919 + 1));
                    assertEquals(Int2IntOpenHashMap.MISSING, map.get(i * 7919 + 1));
                }
                assertEquals(5000, map.size());
            }
        });
    }

    @Test
    void setStoresIntegerMinValue() {
        IntOpenHashSet set = new IntOpenHashSet();
        assertFalse(set.contains(Integer.MIN_VALUE));
        assertTrue(set.add(Integer.MIN_VALUE));
        assertFalse(set.add(Integer.MIN_VALUE));
        assertTrue(set.contains(Integer.MIN_VALUE));
        assertEquals(1, set.size());
        set.add(3);
        assertEquals(Set.of(Integer.MIN_VALUE, 3), toSet(set.toArray()));
        set.clear();
        assertFalse(set.contains(Integer.MIN_VALUE));
        assertEquals(0, set.size());
    }

    @Test
    void mapKeepsValuesEqualToTheMissingMarker() {
        Int2IntOpenHashMap map = new Int2IntOpenHashMap();
        map.put(1, Integer.MIN_VALUE);
        map.put(2, 5);
        assertEquals(2, map.size());
        assertEquals(Integer.MIN_VALUE, map.get(1));
        assertEquals(2, map.values().size());
        assertTrue(map.values().contains(Integer.MIN_VALUE));
        assertEquals(Set.of(1, 2), toSet(map.keySet().toArray()));
        assertThrows(IllegalArgumentException.class, () -> map.put(Integer.MIN_VALUE, 1));
        assertThrows(IllegalArgumentException.class, () -> map.get(Integer.MIN_VALUE));
        assertFalse(map.containsKey(Integer.MIN_VALUE));
    }

    @Test
    void tinyAllocationsGrowCorrectly() {
        for (int allocation = 0; allocation <= 3; allocation++) {
            IntOpenHashSet set = new IntOpenHashSet(7, 0.75, allocation, IntOpenHashSet.Probing.QUADRATIC);
            Int2IntOpenHashMap map = new Int2IntOpenHashMap(7, 0.75, allocation, Int2IntOpenHashMap.Probing.QUADRATIC);
            for (int i = 0; i < 100; i++) {
                set.add(i);
                map.put(i, i * i);
            }
            assertEquals(100, set.size());
            assertEquals(100, map.size());
            for (int i = 0; i < 100; i++) {
                assertTrue(set.contains(i));
                assertEquals(i * i, map.get(i));
            }
        }
    }

    @Test
    void invalidConstructorArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new IntOpenHashSet(1, 1.0, 4, IntOpenHashSet.Probing.LINEAR));
        assertThrows(IllegalArgumentException.class, () -> new IntOpenHashSet(1, 0.5, -1, IntOpenHashSet.Probing.LINEAR));
        assertThrows(IllegalArgumentException.class, () -> new Int2IntOpenHashMap(1, 0.0, 4, Int2IntOpenHashMap.Probing.LINEAR));
    }

    @Test
    void randomizedAgainstJavaUtil() {
        Random random = new Random(11);
        IntOpenHashSet set = new IntOpenHashSet(random.nextInt(), 0.6, 8, IntOpenHashSet.Probing.QUADRATIC);
        Int2IntOpenHashMap map = new Int2IntOpenHashMap(random.nextInt(), 0.6, 8, Int2IntOpenHashMap.Probing.LINEAR);
        Set<Integer> refSet = new HashSet<>();
        Map<Integer, Integer> refMap = new HashMap<>();
        for (int i = 0; i < 20_000; i++) {
            int v = random.nextInt(3000) - 1500;
            if (random.nextInt(50) == 0) {
                v = Integer.MIN_VALUE;
            }
            assertEquals(refSet.add(v), set.add(v));
            if (v != Integer.MIN_VALUE) {
                int value = random.nextInt();
                refMap.put(v, value);
                map.put(v, value);
            }
        }
        assertEquals(refSet.size(), set.size());
        assertEquals(refSet, toSet(set.toArray()));
        assertEquals(refMap.size(), map.size());
        for (Map.Entry<Integer, Integer> e : refMap.entrySet()) {
            assertTrue(map.containsKey(e.getKey()));
            assertEquals(e.getValue(), map.get(e.getKey()));
        }
        assertEquals(refMap.keySet(), toSet(map.keySet().toArray()));
        assertEquals(new TreeSet<>(refMap.values()), new TreeSet<>(map.values()));
        for (int probe = -2000; probe < 2000; probe++) {
            assertEquals(refSet.contains(probe), set.contains(probe), "probe " + probe);
            assertEquals(refMap.containsKey(probe), map.containsKey(probe), "probe " + probe);
        }
    }

    private static Set<Integer> toSet(int[] values) {
        Set<Integer> s = new HashSet<>();
        for (int v : values) {
            s.add(v);
        }
        return s;
    }
}
