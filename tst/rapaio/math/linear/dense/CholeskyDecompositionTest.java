/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.math.linear.dense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.math.linear.DMatrix;

import static org.junit.jupiter.api.Assertions.*;

public class CholeskyDecompositionTest {

    private static final double TOL = 1e-14;
    private static final int TIMES = 100;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testBasic() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = SolidDMatrix.random(30, 30);
            DMatrix b = a.t().dot(a);

            CholeskyDecomposition cholesky = CholeskyDecomposition.from(b);
            DMatrix l = cholesky.getL();

            assertTrue(cholesky.isSPD());
            assertTrue(b.isEqual(l.dot(l.t()), TOL));
        }
    }

    @Test
    void testNonSPD() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = SolidDMatrix.random(30, 30);
            CholeskyDecomposition cholesky = CholeskyDecomposition.from(a);
            assertFalse(cholesky.isSPD());
        }
    }

    @Test
    void testSystem() {
        DMatrix a1 = SolidDMatrix.wrap(new double[][]{
                {2, -1, 0},
                {-1, 2, -1},
                {0, -1, 2}
        });
        DMatrix b1 = SolidDMatrix.wrap(new double[][]{
                {1},
                {-2},
                {0}
        });
        DMatrix x1 = SolidDMatrix.wrap(new double[][]{
                {-0.25},
                {-1.5},
                {-0.75}
        });
        DMatrix s1 = CholeskyDecomposition.from(a1).solve(b1);
        s1.printSummary();
        assertTrue(x1.isEqual(s1, TOL));


        DMatrix a2 = SolidDMatrix.wrap(new double[][]{
                {2, 3},
                {3, 9},
        });
        DMatrix b2 = SolidDMatrix.wrap(new double[][]{
                {6},
                {15},
        });
        DMatrix x2 = SolidDMatrix.wrap(new double[][]{
                {1},
                {1.33333333333333333333333333333333},
        });
        DMatrix s2 = LUDecomposition.from(a2).solve(b2);
        s2.printSummary();
        assertTrue(x2.isEqual(s2, TOL));
    }

    @Test
    void testSystemNonSymmetric() {
        assertThrows(IllegalArgumentException.class, () -> CholeskyDecomposition.from(SolidDMatrix.random(2, 2)).solve(SolidDMatrix.random(2, 1)));
    }

    @Test
    void testSystemNonCompatible() {
        assertThrows(IllegalArgumentException.class, () -> CholeskyDecomposition.from(SolidDMatrix.random(2, 2)).solve(SolidDMatrix.random(3, 1)));
    }
}
