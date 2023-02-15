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

import rapaio.math.tensor.Shape;

public class PointerIteratorTest {

    @Test
    void testScalarIterator() {
        var it = new ScalarPointerIterator(10);
        testScalar(it, 10);
    }

    @Test
    void testDenseIterator() {
        var it = new DensePointerIterator(Shape.of());
        testScalar(it, 0);

        it = new DensePointerIterator(Shape.of(2, 3));
        for (int i = 0; i < 6; i++) {
            assertTrue(it.hasNext());
            assertEquals(i, it.nextInt());
            assertEquals(i, it.position());
        }
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::nextInt);
    }

    @Test
    void testCIterator() {
        var it = new CPointerIterator(Shape.of(), 10, new int[0]);
        testScalar(it, 10);

        it = new CPointerIterator(Shape.of(2, 3), 10, new int[] {4, 19});

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertTrue(it.hasNext());
                assertEquals(10 + i * 4 + j * 19, it.nextInt());
                assertEquals(i * 3 + j, it.position());
            }
        }
        assertFalse(it.hasNext());
    }

    @Test
    void testFIterator() {
        var it = new FPointerIterator(Shape.of(), 10, new int[0]);
        testScalar(it, 10);

        it = new FPointerIterator(Shape.of(2, 3), 10, new int[] {4, 19});

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 2; i++) {
                assertTrue(it.hasNext());
                assertEquals(10 + i * 4 + j * 19, it.nextInt());
                assertEquals(i + j * 2, it.position());
            }
        }
        assertFalse(it.hasNext());
    }

    @Test
    void testSIterator() {
        var it = new SPointerIterator(Shape.of(), 10, new int[0]);
        testScalar(it, 10);

        it = new SPointerIterator(Shape.of(2, 3, 5), 10, new int[] {100, 4, 19});

        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 5; k++) {
                for (int j = 0; j < 3; j++) {
                    assertTrue(it.hasNext());
                    assertEquals(10 + i * 100 + j * 4 + k * 19, it.nextInt());
                    assertEquals(i * 15 + k * 3 + j, it.position());
                }
            }
        }
        assertFalse(it.hasNext());
    }

    void testScalar(PointerIterator it, int pointer) {
        assertTrue(it.hasNext());
        assertEquals(pointer, it.nextInt());
        assertEquals(0, it.position());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::nextInt);
    }
}
