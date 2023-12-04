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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.util.collection.IntArrays;
import rapaio.util.collection.IntOpenHashSet;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/23/21.
 */
public class IntOpenHashSetTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testEmpty() {
        IntOpenHashSet set = new IntOpenHashSet();
        Assertions.assertEquals(0, set.size());
        assertTrue(set.isEmpty());

        set.add(11);
        set.add(10);

        Assertions.assertEquals(2, set.size());
        assertTrue(set.contains(10));
        assertTrue(set.contains(11));
        set.clear();

        Assertions.assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    @Test
    void containsTest() {
        int[] array = IntArrays.newSeq(0, 1_000_000);
        IntArrays.shuffle(array, random);

        int[] in = IntArrays.copy(array, 0, 500_000);
        int[] out = IntArrays.copy(array, 500_000, 500_000);

        IntOpenHashSet set = new IntOpenHashSet();
        set.addAll(IntStream.of(in).boxed().collect(Collectors.toList()));

        for (int x : in) {
            assertTrue(set.contains(x));
        }
        for (int x : out) {
            Assertions.assertFalse(set.contains(x));
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
        Assertions.assertThrows(ClassCastException.class, () -> set.addAll(set1));
    }
}
