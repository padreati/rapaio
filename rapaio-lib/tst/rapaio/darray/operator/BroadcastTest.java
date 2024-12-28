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

package rapaio.darray.operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrays;
import rapaio.darray.Shape;

public class BroadcastTest {

    @Test
    void testElementWiseValidCase() {
        List<DArray<?>> arrays = List.of(
                DArrays.seq(Shape.of(2, 1)),
                DArrays.seq(Shape.of(3, 2, 3)),
                DArrays.seq(Shape.of(3))
        );
        List<Shape> shapes = arrays.stream().map(DArray::shape).toList();

        Broadcast.ElementWise broadcast = Broadcast.elementWise(shapes);
        assertTrue(broadcast.valid());
        assertFalse(broadcast.unchanged());
        for (var t : arrays) {
            assertEquals(broadcast.shape(), broadcast.transform(t).shape());
        }
    }

    @Test
    void testElementWiseEmpty() {
        Broadcast.ElementWise broadcast = Broadcast.elementWise(List.of());
        assertTrue(broadcast.valid());
        assertTrue(broadcast.unchanged());
        assertEquals(Shape.of(), broadcast.shape());
    }

    @Test
    void testSingleEmelent() {
        List<DArray<?>> arrays = List.of(DArrays.seq(Shape.of(2, 1, 4)));
        List<Shape> shapes = arrays.stream().map(DArray::shape).toList();
        Broadcast.ElementWise broadcast = Broadcast.elementWise(shapes);
        assertTrue(broadcast.valid());
        assertTrue(broadcast.unchanged());
        assertEquals(arrays.getFirst().shape(), broadcast.shape());
    }

    @Test
    void testElementWiseInvalidCase() {
        List<DArray<?>> arrays = List.of(
                DArrays.seq(Shape.of(2, 1)),
                DArrays.seq(Shape.of(2, 3, 1))
        );
        List<Shape> shapes = arrays.stream().map(DArray::shape).toList();

        Broadcast.ElementWise broadcast = Broadcast.elementWise(shapes);
        assertFalse(broadcast.valid());
        assertTrue(broadcast.unchanged());
    }
}
