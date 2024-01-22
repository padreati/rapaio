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

package rapaio.math.tensor.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;

public class CholeskyDecompositionTest {

    private static final int TIMES = 100;

    private Random random;
    private static final TensorEngine.OfType<Byte> ofByte = TensorEngine.base().ofByte();
    private static final TensorEngine.OfType<Integer> ofInt = TensorEngine.base().ofInt();
    private static final TensorEngine.OfType<Float> ofFloat = TensorEngine.base().ofFloat();
    private static final TensorEngine.OfType<Double> ofDouble = TensorEngine.base().ofDouble();

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void symmetricPositiveDefiniteTest() {
        testType(DType.DOUBLE, ofDouble, true, 1e-8);
        testType(DType.FLOAT, ofFloat, true, 1e-0);
        testType(DType.DOUBLE, ofDouble, false, 1e-8);
        testType(DType.FLOAT, ofFloat, false, 1e-0);
    }

    private <N extends Number> void testType(DType<N> dType, TensorEngine.OfType<N> ofType, boolean rightFlag, double TOL) {
        for (int i = 0; i < TIMES; i++) {
            var m = ofType.random(Shape.of(10, 10), random, Order.C);

            testSolve(m.t().mm(m), ofType.full(Shape.of(10, 1), dType.castValue(1d)), rightFlag, TOL);
            testSolve(ofType.eye(100, Order.C), ofType.full(Shape.of(100), dType.castValue(1d)), rightFlag, TOL);

            testNonSPD(ofType.random(Shape.of(10, 10), random), ofType.random(Shape.of(10, 3), random));
        }
    }

    private <N extends Number> void testSolve(Tensor<N> A, Tensor<N> b, boolean rightFlag, double TOL) {
        var chol = A.chol(rightFlag);
        assertTrue(chol.isSPD());
        assertEquals(rightFlag, chol.hasRightFlag());

        if (rightFlag) {
            assertNotNull(chol.r());
            assertNull(chol.l());
        } else {
            assertNull(chol.r());
            assertNotNull(chol.l());
        }

        Tensor<N> solution = chol.solve(b);
        assertTrue(A.mv(solution).deepEquals(b.squeeze(), TOL));
        assertTrue(A.deepEquals(A.chol(false).l().mm(A.chol(true).r()), TOL));
    }

    @Test
    void testIntegerTypes() {
        var ti = ofInt.eye(10);
        var ei = assertThrows(IllegalArgumentException.class, ti::chol);
        assertEquals("Cannot compute decomposition for integer types (dtype: INTEGER)", ei.getMessage());

        var tb = ofByte.eye(10);
        var eb = assertThrows(IllegalArgumentException.class, tb::chol);
        assertEquals("Cannot compute decomposition for integer types (dtype: BYTE)", eb.getMessage());
    }

    <N extends Number> void testNonSPD(Tensor<N> a, Tensor<N> b) {
        var chol = a.chol();
        assertFalse(chol.isSPD());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, chol::inv);
        assertEquals("Matrix is not symmetric positive definite.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, b::chol);
        assertEquals("Only square matrices can have Cholesky decomposition.", ex.getMessage());
    }
}
