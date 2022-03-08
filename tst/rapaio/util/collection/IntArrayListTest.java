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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IntArrayListTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testBuilders() {
        int[] elements = new int[] {1, 2, 3, 4};
        IntArrayList list1 = IntArrayList.wrap(elements);
        assertEquals(4, list1.size());
        assertEquals(1, list1.getInt(0));
        assertEquals(4, list1.getInt(3));

        assertArrayEquals(elements, list1.elements());

        IntArrayList list2 = new IntArrayList(0);
        list2.add(1);
        list2.add(2);
        list2.add(3);
        list2.add(4);

        assertArrayEquals(elements, Arrays.copyOf(list2.elements(), 4));

        IntArrayList list3 = new IntArrayList(new TreeSet<>(Set.of(1, 3, 4, 2)));
        assertArrayEquals(elements, Arrays.copyOf(list3.elements(), 4));

        IntArrayList list4 = new IntArrayList();
        list4.add(1);
        list4.add(2);
        list4.add(3);
        list4.add(4);

        assertArrayEquals(elements, Arrays.copyOf(list4.elements(), 4));

        IntArrayList list5 = new IntArrayList(IntStream.range(1, 5).iterator());
        assertArrayEquals(elements, Arrays.copyOf(list5.elements(), 4));

        IntArrayList list6 = new IntArrayList(list5.iterator());
        assertArrayEquals(elements, Arrays.copyOf(list6.elements(), 4));
    }

    @Test
    void testAddContains() {
        IntArrayList list = new IntArrayList();
        list.add(0, 2);
        list.add(0, 1);
        list.add(2, 3);

        assertTrue(list.containsInt(1));
        assertTrue(list.containsInt(2));
        assertTrue(list.containsInt(3));

        assertEquals(1, list.getInt(0));
        assertEquals(2, list.getInt(1));
        assertEquals(3, list.getInt(2));

        list.add(2);

        assertEquals(1, list.indexOf(2));
        assertEquals(3, list.lastIndexOf(2));
        assertEquals(-1, list.indexOf(10));
        assertEquals(-1, list.lastIndexOf(10));

        list.removeInt(1);
        assertEquals(1, list.getInt(0));
        assertEquals(3, list.getInt(1));
        assertEquals(2, list.getInt(2));

        list.rem(3);
        assertEquals(2, list.size());

        list.set(1, 10);
        assertEquals(1, list.getInt(0));
        assertEquals(10, list.getInt(1));
    }
}
