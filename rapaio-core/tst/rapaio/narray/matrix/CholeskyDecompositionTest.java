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

package rapaio.narray.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.NArrays;
import rapaio.narray.Order;
import rapaio.narray.Shape;

public class CholeskyDecompositionTest {

    private static final int TIMES = 100;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void symmetricPositiveDefiniteTest() {
        testType(NArrays.ofDouble(), true, 1e-8);
        testType(NArrays.ofFloat(), true, 1e-0);
    }

    private <N extends Number> void testType(NArrayManager.OfType<N> tmt, boolean rightFlag, double TOL) {
        for (int i = 0; i < TIMES; i++) {
            var m = tmt.random(Shape.of(10, 10), random, Order.C);

            testSolve(m.t().mm(m), tmt.full(Shape.of(10, 1), tmt.dtype().castValue(1d)), rightFlag, TOL);
            testSolve(tmt.eye(100, Order.C), tmt.full(Shape.of(100), tmt.dtype().castValue(1d)), rightFlag, TOL);

            testNonSPD(tmt.random(Shape.of(10, 10), random), tmt.random(Shape.of(10, 3), random));
        }
    }

    private <N extends Number> void testSolve(NArray<N> A, NArray<N> b, boolean rightFlag, double TOL) {
        var chol = A.cholesky(rightFlag);
        assertTrue(chol.isSPD());
        assertEquals(rightFlag, chol.hasRightFlag());

        if (rightFlag) {
            assertNotNull(chol.r());
            assertNull(chol.l());
        } else {
            assertNull(chol.r());
            assertNotNull(chol.l());
        }

        NArray<N> solution = chol.solve(b);
        assertTrue(A.mv(solution.isMatrix() ? solution.squeeze(1) : solution).deepEquals(b.isMatrix() ? b.squeeze(1) : b, TOL));
        assertTrue(A.deepEquals(A.cholesky(false).l().mm(A.cholesky(true).r()), TOL));
    }

    @Test
    void testIntegerTypes() {
        var ti = NArrays.ofInt().eye(10);
        var ei = assertThrows(IllegalArgumentException.class, ti::cholesky);
        assertEquals("Cannot compute decomposition for integer types (dtype: INTEGER)", ei.getMessage());

        var tb = NArrays.ofByte().eye(10);
        var eb = assertThrows(IllegalArgumentException.class, tb::cholesky);
        assertEquals("Cannot compute decomposition for integer types (dtype: BYTE)", eb.getMessage());
    }

    <N extends Number> void testNonSPD(NArray<N> a, NArray<N> b) {
        var chol = a.cholesky();
        assertFalse(chol.isSPD());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, chol::inv);
        assertEquals("Matrix is not symmetric positive definite.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, b::cholesky);
        assertEquals("Only square matrices can have Cholesky decomposition.", ex.getMessage());
    }
}
