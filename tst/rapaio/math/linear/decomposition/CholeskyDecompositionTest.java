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
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DMatrixDenseR;

public class CholeskyDecompositionTest {

    private static final double TOL = 1e-12;
    private static final int TIMES = 100;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testBasic() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = DMatrix.random(30, 30);
            DMatrix b = a.t().dot(a);

            DCholeskyDecomposition cholesky = b.ops().cholesky();
            DMatrix l = cholesky.l();

            assertTrue(cholesky.isSPD());
            assertTrue(b.deepEquals(l.dot(l.t()), TOL));
        }
    }

    @Test
    void testNonSPD() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = DMatrix.random(30, 30);
            DCholeskyDecomposition cholesky = a.ops().cholesky();
            assertFalse(cholesky.isSPD());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, cholesky::inv);
            assertEquals("Matrix is not symmetric positive definite.", ex.getMessage());
        }

        DMatrix a = DMatrix.random(10, 3);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> a.ops().cholesky());
        assertEquals("Only square matrices can have Cholesky decomposition.", ex.getMessage());
    }

    @Test
    void testSystem() {
        DMatrix a1 = DMatrixDenseR.wrap(3, 3,
                2, -1, 0,
                -1, 2, -1,
                0, -1, 2
        );
        DMatrix b1 = DMatrixDenseR.wrap(3, 1,
                1,
                -2,
                0
        );
        DMatrix x1 = DMatrixDenseR.wrap(3, 1,
                -0.25,
                -1.5,
                -0.75
        );
        DMatrix s1 = a1.ops().cholesky().solve(b1);
        assertTrue(x1.deepEquals(s1, TOL));

        DMatrix a2 = DMatrixDenseR.wrap(2, 2,
                2, 3,
                3, 9
        );
        DMatrix b2 = DMatrixDenseR.wrap(2, 1,
                6,
                15
        );
        DMatrix x2 = DMatrixDenseR.wrap(2, 1,
                1,
                1.33333333333333333333333333333333
        );
        DMatrix s2 = a2.ops().lu().solve(b2);
        assertTrue(x2.deepEquals(s2, TOL));
    }

    @Test
    void testSystemNonSymmetric() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DMatrix.random(2, 2).ops().cholesky().solve(DMatrix.random(2, 1)));
        assertEquals("Matrix is not symmetric positive definite.", ex.getMessage());
    }

    @Test
    void testSystemNonCompatible() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> DMatrix.random(2, 2).ops().cholesky().solve(DMatrix.random(3, 1)));
        assertEquals("Matrix row dimensions must agree.", ex.getMessage());
    }

    @Test
    void testLeftRight() {

        DMatrix a = DMatrixDenseC.random(4, 4);
        DMatrix ata = a.t().dot(a);

        var chl = ata.ops().cholesky();
        var chr = ata.ops().cholesky(true);

        assertFalse(chl.hasRightFlag());
        assertTrue(chr.hasRightFlag());

        assertTrue(chl.l().t().deepEquals(chr.r(), 1e-10));

        DMatrix inv = chl.solve(DMatrix.identity(4));
        DMatrix res = ata.dot(inv).roundValues(12);
        assertTrue(DMatrix.identity(4).deepEquals(res));

        inv = chr.solve(DMatrix.identity(4));
        res = ata.dot(inv).roundValues(12);
        assertTrue(DMatrix.identity(4).deepEquals(res));

        DMatrix inv1 = chl.inv();
        DMatrix inv2 = chr.inv();
        assertTrue(inv1.deepEquals(inv2));


        DVector v = DVector.random(4);
        DVector x1 = chl.solve(v);
        DVector x2 = chr.solve(v);

        assertTrue(x1.deepEquals(x2));
    }
}
