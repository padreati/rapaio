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

package rapaio.math.tensor.iterators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;

public class PointerIteratorTest {

    @Test
    void testDenseIterator() {
        var it = new DensePointerIterator(Shape.of(2, 3), 0, 1);
        for (int i = 0; i < 6; i++) {
            assertTrue(it.hasNext());
            assertEquals(i, it.nextInt());
            assertEquals(i, it.pointer());
        }
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::nextInt);
    }

    @Test
    void testCIterator() {
        var it = new StridePointerIterator(StrideLayout.of(Shape.of(2, 3), 10, new int[] {4, 19}), Order.C);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertTrue(it.hasNext());
                assertEquals(10 + i * 4 + j * 19, it.nextInt());
                assertEquals(i * 3 + j, it.pointer());
            }
        }
        assertFalse(it.hasNext());
    }

    @Test
    void testFIterator() {
        var it = new StridePointerIterator(StrideLayout.of(Shape.of(2, 3), 10, new int[] {4, 19}), Order.F);

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 2; i++) {
                assertTrue(it.hasNext());
                assertEquals(10 + i * 4 + j * 19, it.nextInt());
                assertEquals(i + j * 2, it.pointer());
            }
        }
        assertFalse(it.hasNext());
    }

    @Test
    void testSIterator() {
        var it = new StridePointerIterator(StrideLayout.of(Shape.of(2, 3, 5), 10, new int[] {100, 4, 19}), Order.S);

        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 5; k++) {
                for (int j = 0; j < 3; j++) {
                    assertTrue(it.hasNext());
                    assertEquals(10 + i * 100 + j * 4 + k * 19, it.nextInt());
                    assertEquals(i * 15 + k * 3 + j, it.pointer());
                }
            }
        }
        assertFalse(it.hasNext());
    }
}
