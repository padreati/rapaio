/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.narray.layout;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.narray.Layout;
import rapaio.narray.Order;
import rapaio.narray.Shape;
import rapaio.util.collection.IntArrays;

public class StrideLayoutTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void equalsTest() {
        var layout1 = StrideLayout.of(Shape.of(2, 3, 4), 10, new int[] {1, 2, 6});
        var layout2 = StrideLayout.of(Shape.of(2, 3, 4), 10, new int[] {1, 2, 6});
        assertEquals(layout1, layout2);
    }

    @Test
    void testHashEquals() {
        List<StrideLayout> layouts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int[] shape = new int[random.nextInt(3) + 1];
            for (int j = 0; j < shape.length; j++) {
                shape[j] = random.nextInt(10) + 1;
            }
            Order order = random.nextDouble() > 0.5 ? Order.C : Order.F;
            var layout = StrideLayout.ofDense(Shape.of(shape), random.nextInt(10), order);

            assertEquals(IntArrays.prod(shape, 0, shape.length), layout.size());
            assertEquals(shape.length, layout.rank());
            assertArrayEquals(shape, layout.dims());
            for (int j = 0; j < shape.length; j++) {
                assertEquals(shape[j], layout.dim(j));
            }
            int[] strides = IntArrays.newFill(shape.length, 1);
            if (order == Order.C) {
                for (int j = shape.length - 2; j >= 0; j--) {
                    strides[j] = strides[j + 1] * shape[j + 1];
                }
            } else {
                for (int j = 1; j < shape.length; j++) {
                    strides[j] = strides[j - 1] * shape[j - 1];
                }
            }
            assertArrayEquals(strides, layout.strides());

            if (layout.rank() < 2) {
                assertEquals(Order.defaultOrder(), layout.storageFastOrder());
            } else {
                assertEquals(order, layout.storageFastOrder());
            }

            layouts.add(layout);
        }

        long count = layouts.stream().distinct().count();

        Set<Layout> set = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            Collections.shuffle(layouts);
            set.addAll(layouts);
        }
        assertEquals(count, set.size());

        Map<Layout, Layout> map = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            for (var layout : layouts) {
                map.put(layout, layout);
            }
        }
        assertEquals(count, map.size());
        for (var e : map.entrySet()) {
            assertEquals(e.getKey(), e.getValue());
        }
    }
}
