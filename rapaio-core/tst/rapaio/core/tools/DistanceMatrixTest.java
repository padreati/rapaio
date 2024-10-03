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

package rapaio.core.tools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class DistanceMatrixTest {

    @Test
    void validationTest() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DistanceMatrix.empty(new String[0]));
        assertEquals("Length must be positive.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> DistanceMatrix.empty(0));
        assertEquals("Length must be positive.", ex.getMessage());
    }

    @Test
    void emptyTest() {
        var dm = DistanceMatrix.empty(new String[] {"a", "c", "b"});
        assertEquals(3, dm.length());
        assertArrayEquals(new String[] {"a", "c", "b"}, dm.names());

        dm = DistanceMatrix.empty(3);
        assertEquals(3, dm.length());
        assertArrayEquals(new String[] {"0", "1", "2"}, dm.names());
        assertEquals("1", dm.name(1));
    }

    @Test
    void manualFillTest() {
        var dm = DistanceMatrix.empty(4);

        assertEquals(0, dm.get(0, 1));
        assertEquals(0, dm.get(1, 0));

        assertEquals(0, dm.get(2, 3));
        assertEquals(0, dm.get(3, 2));

        dm.set(0, 1, 10);
        dm.set(3, 2, 2);

        assertEquals(10, dm.get(0, 1));
        assertEquals(10, dm.get(1, 0));

        assertEquals(2, dm.get(2, 3));
        assertEquals(2, dm.get(3, 2));
    }

    @Test
    void fillTest() {
        var dm = DistanceMatrix.empty(10);
        dm.fill(Integer::sum);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(i + j, dm.get(i, j));
            }
        }
    }

    @Test
    void matrixTest() {
        var dm = DistanceMatrix.empty(10);
        var m = dm.toDMatrix();
        assertEquals(0, m.sum());

        m = dm.fill(Integer::sum).toDMatrix();
        assertEquals(900, m.sum());
    }
}
