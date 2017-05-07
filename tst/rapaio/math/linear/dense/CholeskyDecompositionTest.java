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

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.math.linear.RM;

import static org.junit.Assert.*;

public class CholeskyDecompositionTest {

    private static final double TOL = 1e-14;

    @Test
    public void testBasic() {
        RandomSource.setSeed(1234);
        for (int i = 0; i < 100; i++) {
            RM a = SolidRM.random(30, 30);
            RM b = a.t().dot(a);

            CholeskyDecomposition cholesky = CholeskyDecomposition.from(b);
            RM l = cholesky.getDecompositionArray();

            assertTrue(cholesky.isSymAndPositive());
            assertTrue(b.isEqual(l.dot(l.t()), TOL));
        }
    }

    @Test
    public void testNonSPD() {
        RandomSource.setSeed(1234);
        for (int i = 0; i < 100; i++) {
            RM a = SolidRM.random(30, 30);
            CholeskyDecomposition cholesky = CholeskyDecomposition.from(a);
            assertFalse(cholesky.isSymAndPositive());
        }
    }

    @Test
    public void testSystem() {
        RM a1 = SolidRM.wrap(new double[][] {
                {2, -1, 0},
                {-1, 2, -1},
                {0, -1, 2}
        });
        RM b1 = SolidRM.wrap(new double[][] {
                {1},
                {-2},
                {0}
        });
        RM x1 = SolidRM.wrap(new double[][] {
                {-0.25},
                {-1.5},
                {-0.75}
        });
        RM s1 = CholeskyDecomposition.from(a1).solve(b1);
        s1.printSummary();
        assertTrue(x1.isEqual(s1, TOL));


        RM a2 = SolidRM.wrap(new double[][] {
                {2, 3},
                {3, 9},
        });
        RM b2 = SolidRM.wrap(new double[][] {
                {6},
                {15},
        });
        RM x2 = SolidRM.wrap(new double[][] {
                {1},
                {1.33333333333333333333333333333333},
        });
        RM s2 = LUDecomposition.from(a2).solve(b2);
        s2.printSummary();
        assertTrue(x2.isEqual(s2, TOL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSystemNonSymmetric() {
        RandomSource.setSeed(1234);
        CholeskyDecomposition.from(SolidRM.random(2, 2)).solve(SolidRM.random(2, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSystemNonCompatible() {
        RandomSource.setSeed(1234);
        CholeskyDecomposition.from(SolidRM.random(2, 2)).solve(SolidRM.random(3, 1));
    }
}
