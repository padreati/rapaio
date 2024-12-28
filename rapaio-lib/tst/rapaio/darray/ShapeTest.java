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

package rapaio.darray;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ShapeTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void builderTest() {
        assertEquals("Shape: []", Shape.of().toString());
        assertEquals("Shape: [5]", Shape.of(5).toString());
        assertEquals("Shape: [5,3]", Shape.of(5, 3).toString());
        assertEquals("Shape: [2,4,10,8]", Shape.of(2, 4, 10, 8).toString());
    }

    @Test
    void testCases() {
        checkTensor(Shape.of());
        checkTensor(Shape.of(5), 5);
        checkTensor(Shape.of(5, 3), 5, 3);
        checkTensor(Shape.of(2, 4, 10, 8), 2, 4, 10, 8);
    }

    void checkTensor(Shape shape, int... dims) {
        assertArrayEquals(shape.dims(), dims);
        assertEquals(shape.rank(), dims.length);
        for (int i = 0; i < dims.length; i++) {
            assertEquals(dims[i], shape.dim(i));
        }
    }

    @Test
    void testInvalid() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Shape.of(1, 2, -3, 0));
        assertEquals("Invalid shape dimensions: [1,2,-3,0].", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Shape.of(0, 0));
        assertEquals("Invalid shape dimensions: [0,0].", e.getMessage());
    }

    @Test
    void testPositionAndIndex() {
        for (int i = 0; i < 100; i++) {
            int rank = random.nextInt(6) + 1;
            int[] dims = new int[rank];
            for (int j = 0; j < rank; j++) {
                dims[j] = random.nextInt(4) + 1;
            }
            Shape shape = Shape.of(dims);

            for (int j = 0; j < shape.size(); j++) {
                assertEquals(j, shape.position(Order.C, shape.index(Order.C, j)));
                assertEquals(j, shape.position(Order.F, shape.index(Order.F, j)));
            }
        }
    }

    @Test
    void testInvalidOrderPosition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Shape.of(1,2,3).position(Order.S));
        assertEquals("Indexing order not allowed.", ex.getMessage());
    }

    @Test
    void testInvalidOrderIndex() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Shape.of(1,2,3).index(Order.S, 0));
        assertEquals("Indexing order not allowed.", ex.getMessage());
    }

    @Test
    void testEqualsAndHash() {
        Set<Shape> set = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            int rank = random.nextInt(6)+1;
            int[] dims = new int[rank];
            for (int j = 0; j < rank; j++) {
                dims[j] = random.nextInt(4)+1;
            }
            Shape shape = Shape.of(dims);
            set.add(shape);
        }

        Set<Shape> copy = new HashSet<>();
        copy.addAll(set);
        copy.addAll(set);

        Shape[] shapes1 = set.toArray(Shape[]::new);
        Shape[] shapes2 = copy.toArray(Shape[]::new);

        assertArrayEquals(shapes1, shapes2);
    }
}
