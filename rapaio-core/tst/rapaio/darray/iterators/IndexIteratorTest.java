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

package rapaio.darray.iterators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;

public class IndexIteratorTest {

    private Random random;
    private DArrayManager manager;

    @BeforeEach
    void beforeEach() {
        random = new Random();
        manager = DArrayManager.base();
    }

    @ParameterizedTest
    @MethodSource("shapes")
    void test4dIndexIteratorCOrder(Shape shape) {
        DArray<?> t = manager.seq(DType.DOUBLE, shape);
        var indexIt = new IndexIterator(t.shape(), Order.C);
        var valueIt = t.iterator(Order.C);
        while (valueIt.hasNext()) {
            assertTrue(indexIt.hasNext());
            assertEquals(valueIt.next(), t.get(indexIt.next()));
        }

        indexIt = new IndexIterator(t.shape(), Order.F);
        valueIt = t.iterator(Order.F);
        while (valueIt.hasNext()) {
            assertTrue(indexIt.hasNext());
            assertEquals(valueIt.next(), t.get(indexIt.next()));
        }
    }

    @Test
    void testInvalidOrder() {
        var t = manager.seq(DType.DOUBLE, Shape.of(2, 3));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new IndexIterator(t.shape(), Order.A));
        assertEquals("Order must be either Order.C or Order.F", ex.getMessage());
    }

    static Stream<Shape> shapes() {
        return Stream.of(Shape.of(2, 4, 3, 5), Shape.of(1, 3, 2), Shape.of(10), Shape.of(1), Shape.of(10, 1));
    }
}
