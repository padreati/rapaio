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

package rapaio.math.linear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.VarInt;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/25/21.
 */
class ShapeTest {

    private List<int[]> arrays;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(42);
        arrays = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            arrays.add(VarInt.from(RandomSource.nextInt(10) + 1, row -> RandomSource.nextInt(100) + 1).elements());
        }
    }

    @Test
    void of() {
        for (int[] array : arrays) {
            Shape shape = Shape.of(array);
            assertNotNull(shape);
            assertEquals(array.length, shape.size());
            for (int i = 0; i < array.length; i++) {
                assertEquals(array[i], shape.get(i));
            }
        }
    }

    @Test
    void testToString() {
        for (int[] array : arrays) {
            Shape shape = Shape.of(array);
            String str = "[" + Arrays.toString(array) + "]";
            assertEquals(str, shape.toString());
            assertEquals(str, shape.toContent());
            assertEquals(str, shape.toFullContent());
            assertEquals(str, shape.toSummary());
        }
    }

    @Test
    void testInvalidShapes() {
        assertEquals("Cannot create shape: dimension array is empty or null.",
                assertThrows(IllegalArgumentException.class, () -> Shape.of(null)).getMessage());
        assertEquals("Cannot create shape: dimension array is empty or null.",
                assertThrows(IllegalArgumentException.class, () -> Shape.of(new int[0])).getMessage());
        assertEquals("Dimension value cannot be zero.",
                assertThrows(IllegalArgumentException.class, () -> Shape.of(new int[10])).getMessage());
    }
}