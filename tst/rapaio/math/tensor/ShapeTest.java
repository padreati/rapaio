/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ShapeTest {

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
        assertEquals(shape.size(), dims.length);
        for(int i=0; i<dims.length; i++) {
            assertEquals(dims[i], shape.dim(i));
        }
    }

    @Test
    void testInvalid() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Shape.of(1, 2, -3, 0));
        assertEquals("Invalid shape dimension: [1,2,-3,0]", e.getMessage());

        e = assertThrows(IllegalArgumentException.class, () -> Shape.of(0, 0));
        assertEquals("Invalid shape dimension: [0,0]", e.getMessage());
    }
}
