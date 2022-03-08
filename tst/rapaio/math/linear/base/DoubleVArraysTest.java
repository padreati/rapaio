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

package rapaio.math.linear.base;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.util.collection.DoubleArrays;

public class DoubleVArraysTest {

    private static final double TOL = 1e-20;

    private final int len = 100_003;

    private double[] x1, x2;
    private double[] y1, y2;

    @BeforeEach
    void beforeEach() {
        x1 = DoubleArrays.newSeq(0, len);
        x2 = Arrays.copyOf(x1, x1.length);
        y1 = DoubleArrays.newFill(len, 10);
        y2 = Arrays.copyOf(y1, y1.length);
    }

    @Test
    void testAddScalar() {
        DoubleArrays.add(x1, 10, 1, 103);
        VectorAlgebra.binaryOpTo(DOperators.ADD, x2, 10, 1, x2, 10, 103);
        assertArrayEquals(x1, x2, TOL);
    }

    @Test
    void testAddVectors() {
        DoubleArrays.add(x1, 10, y1, 20, 10_003);
        VectorAlgebra.binaryOpTo(DOperators.ADD, x2, 10, y2, 20, x2, 10, 10_003);
        assertArrayEquals(x1, x2);
    }
}
