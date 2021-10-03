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

public class LUDecompositionTest {

    private static final double TOL = 1e-14;

    @BeforeEach
    void setUpEach() {
        RandomSource.setSeed(14);
    }

    @Test
    void testBasicGaussian() {

        DMatrix a = DMatrix.random(100, 100);
        LUDecomposition lu = LUDecomposition.from(a, LUDecomposition.Method.GAUSSIAN_ELIMINATION);
        DMatrix a1 = a.mapRows(lu.getPivot());
        DMatrix a2 = lu.getL().dot(lu.getU());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testBasicCrout() {
        DMatrix a = DMatrix.random(100, 100);
        LUDecomposition lu = LUDecomposition.from(a, LUDecomposition.Method.CROUT);
        DMatrix a1 = a.mapRows(lu.getPivot());
        DMatrix a2 = lu.getL().dot(lu.getU());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testIsSingular() {
        assertFalse(LUDecomposition.from(DMatrix.empty(10, 10)).isNonSingular());
        assertTrue(LUDecomposition.from(DMatrix.random(10, 10)).isNonSingular());
    }

    @Test
    void solveTest() {

        DMatrix a1 = DMatrix.wrap(3, 3, true,
                3, 2, -1,
                2, -2, 4,
                -1, 0.5, -1
        );
        DMatrix b1 = DMatrix.wrap(3, 1, true,
                1,
                -2,
                0
        );
        DMatrix x1 = DMatrix.wrap(3, 1, true,
                1,
                -2,
                -2
        );
        assertTrue(x1.deepEquals(LUDecomposition.from(a1).solve(b1), TOL));


        DMatrix a2 = DMatrix.wrap(2, 2, true,
                2, 3,
                4, 9
        );
        DMatrix b2 = DMatrix.wrap(2, 1, true,
                6,
                15
        );
        DMatrix x2 = DMatrix.wrap(2, 1, true, 1.5, 1);
        assertTrue(x2.deepEquals(LUDecomposition.from(a2).solve(b2), TOL));
    }

    @Test
    void determinantTest() {
        DMatrix a = DMatrix.wrap(2, 2, true,
                1, 2,
                3, 4
        );
        assertEquals(-2, LUDecomposition.from(a).det(), TOL);
    }

    @Test
    void determinantTestEx() {
        assertThrows(IllegalArgumentException.class, () -> LUDecomposition.from(DMatrix.random(4, 3)).det());
    }

    @Test
    void builderTestEx() {
        assertThrows(IllegalArgumentException.class, () -> LUDecomposition.from(DMatrix.random(2, 3)).det());
    }

    @Test
    void builderTestMethodEx() {
        assertThrows(IllegalArgumentException.class, () -> LUDecomposition.from(DMatrix.random(2, 3), LUDecomposition.Method.GAUSSIAN_ELIMINATION).det());
    }
}
