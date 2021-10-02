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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.math.linear.DMatrix;

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

            CholeskyDecomposition cholesky = CholeskyDecomposition.from(b);
            DMatrix l = cholesky.getL();

            assertTrue(cholesky.isSPD());
            assertTrue(b.deepEquals(l.dot(l.t()), TOL));
        }
    }

    @Test
    void testNonSPD() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = DMatrix.random(30, 30);
            CholeskyDecomposition cholesky = CholeskyDecomposition.from(a);
            assertFalse(cholesky.isSPD());
        }
    }

    @Test
    void testSystem() {
        DMatrix a1 = DMatrix.wrap(new double[][]{
                {2, -1, 0},
                {-1, 2, -1},
                {0, -1, 2}
        });
        DMatrix b1 = DMatrix.wrap(new double[][]{
                {1},
                {-2},
                {0}
        });
        DMatrix x1 = DMatrix.wrap(new double[][]{
                {-0.25},
                {-1.5},
                {-0.75}
        });
        DMatrix s1 = CholeskyDecomposition.from(a1).solve(b1);
        assertTrue(x1.deepEquals(s1, TOL));

        DMatrix a2 = DMatrix.wrap(new double[][]{
                {2, 3},
                {3, 9},
        });
        DMatrix b2 = DMatrix.wrap(new double[][]{
                {6},
                {15},
        });
        DMatrix x2 = DMatrix.wrap(new double[][]{
                {1},
                {1.33333333333333333333333333333333},
        });
        DMatrix s2 = LUDecomposition.from(a2).solve(b2);
        assertTrue(x2.deepEquals(s2, TOL));
    }

    @Test
    void testSystemNonSymmetric() {
        assertThrows(IllegalArgumentException.class, () -> CholeskyDecomposition.from(DMatrix.random(2, 2)).solve(DMatrix.random(2, 1)));
    }

    @Test
    void testSystemNonCompatible() {
        assertThrows(IllegalArgumentException.class, () -> CholeskyDecomposition.from(DMatrix.random(2, 2)).solve(DMatrix.random(3, 1)));
    }
}
