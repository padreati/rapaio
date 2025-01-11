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

package rapaio.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import rapaio.util.collection.Floats;

public class VarFloatTest {

    private static final float TOL = 1e-20f;
    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10})
    void builderEmptyTest(int len) {
        VarFloat v = VarFloat.empty();
        assertEquals(0, v.size());

        v = VarFloat.empty(len);
        assertEquals(len, v.size());
        for (float value : v) {
            assertTrue(Float.isNaN(value));
        }
    }

    @ParameterizedTest
    @MethodSource("builderCopyParams")
    void builderCopyTest(VarFloat v) {
        assertEquals(3, v.size());
        assertEquals(1.f, v.getFloat(0));
        assertEquals(2.f, v.getFloat(1));
        assertEquals(3.f, v.getFloat(2));
    }

    static Stream<VarFloat> builderCopyParams() {
        return Stream.of(
                VarFloat.copy(List.of(1.f, 2.f, 3.f)),
                VarFloat.copy(1, 2, 3),
                VarFloat.copy(1.f, 2.f, 3.f),
                VarFloat.copy(VarFloat.seq(1, 3)),
                VarFloat.copy(VarInt.seq(1, 3))
        );
    }

    @Test
    void builderWrapTest() {
        float[] array = new float[10];
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextFloat();
        }
        VarFloat v = VarFloat.wrap(array);
        assertEquals(array.length, v.size());
        for (int i = 0; i < v.size(); i++) {
            assertEquals(array[i], v.getFloat(i), TOL);
        }
    }

    @Test
    void builderFillTest() {
        VarFloat f1 = VarFloat.fill(10);
        assertEquals(10, f1.size());
        for (float value : f1) {
            assertEquals(0, value, TOL);
        }

        VarFloat f2 = VarFloat.fill(10, 0.3f);
        assertEquals(10, f2.size());
        for (float value : f2) {
            assertEquals(0.3f, value, TOL);
        }

        VarFloat f3 = VarFloat.scalar(0.2f);
        assertEquals(1, f3.size());
        assertEquals(0.2f, f3.getFloat(0), TOL);
    }

    @Test
    void builderFromTest() {
        VarFloat from1 = VarFloat.from(10, () -> random.nextFloat());
        assertEquals(10, from1.size());
        random.setSeed(42);
        for (float value : from1) {
            assertEquals(random.nextFloat(), value, TOL);
        }

        VarFloat from2 = VarFloat.from(10, i -> 1.f / i);
        assertEquals(10, from2.size());
        assertEquals(Float.POSITIVE_INFINITY, from2.getFloat(0), TOL);
        for (int i = 1; i < 10; i++) {
            assertEquals(1.f / i, from2.getFloat(i), TOL);
        }

        VarFloat from3 = VarFloat.from(VarInt.seq(10), i -> 1.f / i);
        assertEquals(10, from3.size());
        assertEquals(Float.POSITIVE_INFINITY, from3.getFloat(0), TOL);
        for (int i = 1; i < 10; i++) {
            assertEquals(1.f / i, from3.getFloat(i), TOL);
        }
    }

    @Test
    void builderInvalidSize() {
        var ex = assertThrows(IllegalArgumentException.class, () -> VarFloat.fill(-1));
        assertEquals("Illegal row count: -1", ex.getMessage());
    }

    @Test
    void testCollector() {
        int[] values = new int[] {1, 2, 3, 4, 5};
        VarFloat f = Arrays.stream(values).mapToObj(Float::valueOf).collect(VarFloat.collector());
        assertEquals(5, f.size());
        float v = 1.f;
        for (float value : f) {
            assertEquals(v, value, TOL);
            v++;
        }
    }

    @Test
    void testElements() {
        float[] array = Floats.newFill(10, 1);
        VarFloat v = VarFloat.wrap(array);
        float[] elements = v.elements();
        assertArrayEquals(array, elements);
        assertSame(array, elements);
    }
}
