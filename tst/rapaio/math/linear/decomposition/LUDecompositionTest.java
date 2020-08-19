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

package rapaio.math.linear.decomposition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.math.linear.DM;
import rapaio.math.linear.dense.DMStripe;

import static org.junit.jupiter.api.Assertions.*;

public class LUDecompositionTest {

    private static final double TOL = 1e-14;

    @BeforeEach
    void setUpEach() {
        RandomSource.setSeed(14);
    }

    @Test
    void testBasicGaussian() {

        DM a = DMStripe.random(100, 100);
        LUDecomposition lu = LUDecomposition.from(a, LUDecomposition.Method.GAUSSIAN_ELIMINATION);
        DM a1 = a.mapRows(lu.getPivot());
        DM a2 = lu.getL().dot(lu.getU());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testBasicCrout() {
        DM a = rapaio.math.linear.dense.DMStripe.random(100, 100);
        LUDecomposition lu = LUDecomposition.from(a, LUDecomposition.Method.CROUT);
        DM a1 = a.mapRows(lu.getPivot());
        DM a2 = lu.getL().dot(lu.getU());
        assertTrue(a1.deepEquals(a2, TOL));
    }

    @Test
    void testIsSingular() {
        assertFalse(LUDecomposition.from(rapaio.math.linear.dense.DMStripe.empty(10, 10)).isNonSingular());
        assertTrue(LUDecomposition.from(rapaio.math.linear.dense.DMStripe.random(10, 10)).isNonSingular());
    }

    @Test
    void solveTest() {

        DM a1 = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {3, 2, -1},
                {2, -2, 4},
                {-1, 0.5, -1}
        });
        DM b1 = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {1},
                {-2},
                {0}
        });
        DM x1 = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {1},
                {-2},
                {-2}
        });
        assertTrue(x1.deepEquals(LUDecomposition.from(a1).solve(b1), TOL));


        DM a2 = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {2, 3},
                {4, 9},
        });
        DM b2 = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {6},
                {15},
        });
        DM x2 = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {1.5},
                {1},
        });
        assertTrue(x2.deepEquals(LUDecomposition.from(a2).solve(b2), TOL));
    }

    @Test
    void determinantTest() {
        DM a = rapaio.math.linear.dense.DMStripe.wrap(new double[][]{
                {1, 2},
                {3, 4}
        });
        assertEquals(-2, LUDecomposition.from(a).det(), TOL);
    }

    @Test
    void determinantTestEx() {
        assertThrows(IllegalArgumentException.class, () -> LUDecomposition.from(rapaio.math.linear.dense.DMStripe.random(4, 3)).det());
    }

    @Test
    void builderTestEx() {
        assertThrows(IllegalArgumentException.class, () -> LUDecomposition.from(rapaio.math.linear.dense.DMStripe.random(2, 3)).det());
    }

    @Test
    void builderTestMethodEx() {
        assertThrows(IllegalArgumentException.class, () -> LUDecomposition.from(rapaio.math.linear.dense.DMStripe.random(2, 3), LUDecomposition.Method.GAUSSIAN_ELIMINATION).det());
    }
}
