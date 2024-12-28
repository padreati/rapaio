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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DArrayScalarTest {

    private DArrayManager tm;

    @BeforeEach
    void beforeEach() {
        tm = DArrayManager.base();
    }

    @Test
    void binaryScalarTest() {

        DArray<?> v1 = tm.seq(DType.DOUBLE, Shape.of(3)).add(10);
        for (int i = 0; i < 3; i++) {
            assertEquals(10 + i, v1.get(i).doubleValue());
        }

        DArray<?> v2 = tm.seq(DType.INTEGER, Shape.of(3)).add(10.);
        for (int i = 0; i < 3; i++) {
            assertEquals(10 + i, v2.get(i).intValue());
        }

        DArray<?> v3 = tm.seq(DType.BYTE, Shape.of(3)).add(10.);
        for (int i = 0; i < 3; i++) {
            assertEquals(10 + i, v3.get(i).byteValue());
        }
    }
}
