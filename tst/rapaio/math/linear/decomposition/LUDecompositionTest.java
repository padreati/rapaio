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

package rapaio.math.linear.decomposition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixDenseR;

public class LUDecompositionTest {

    private static final double TOL = 1e-14;

    @BeforeEach
    void setUpEach() {
        RandomSource.setSeed(14);
    }

    @Test
    void testBasicGaussian() {

        DMatrix a = DMatrix.random(100, 100);
        DLUDecomposition lu = a.ops().lu(DLUDecomposition.Method.GAUSSIAN_ELIMINATION);
        DMatrix a1 = a.mapRows(lu.getPivot());
        DMatrix a2 = lu.l().dot(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testBasicCrout() {
        DMatrix a = DMatrix.random(100, 100);
        DLUDecomposition lu = a.ops().lu(DLUDecomposition.Method.CROUT);
        DMatrix a1 = a.mapRows(lu.getPivot());
        DMatrix a2 = lu.l().dot(lu.u());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testIsSingular() {
        assertFalse(DMatrix.empty(10, 10).ops().lu().isNonSingular());
        assertTrue(DMatrix.random(10, 10).ops().lu().isNonSingular());
    }

    @Test
    void solveTest() {

        DMatrix a1 = DMatrixDenseR.wrap(3, 3,
                3, 2, -1,
                2, -2, 4,
                -1, 0.5, -1
        );
        DMatrix b1 = DMatrixDenseR.wrap(3, 1,
                1,
                -2,
                0
        );
        DMatrix x1 = DMatrixDenseR.wrap(3, 1,
                1,
                -2,
                -2
        );
        assertTrue(x1.deepEquals(a1.ops().lu().solve(b1), TOL));


        DMatrix a2 = DMatrixDenseR.wrap(2, 2,
                2, 3,
                4, 9
        );
        DMatrix b2 = DMatrixDenseR.wrap(2, 1,
                6,
                15
        );
        DMatrix x2 = DMatrixDenseR.wrap(2, 1, 1.5, 1);
        assertTrue(x2.deepEquals(a2.ops().lu().solve(b2), TOL));
    }

    @Test
    void determinantTest() {
        DMatrix a = DMatrixDenseR.wrap(2, 2,
                1, 2,
                3, 4
        );
        assertEquals(-2, a.ops().lu().det(), TOL);
    }

    @Test
    void testInvalidMatrixForDeterminant() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.random(4, 3).ops().lu().det());
        assertEquals("The determinant can be computed only for squared matrices.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.random(2, 3).ops().lu().det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
                () -> DMatrix.random(2, 3).ops().lu(DLUDecomposition.Method.GAUSSIAN_ELIMINATION).det());
        assertEquals("For LU decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());
    }

    @Test
    void testInvalidSolver() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DMatrix.random(4,3).ops().lu().solve(DMatrix.random(6,6)));
        assertEquals("Matrix row dimensions must agree.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.fill(3, 3, 0).ops().lu().solve(DMatrix.identity(3)));
        assertEquals("Matrix is singular.", ex.getMessage());
    }
}
