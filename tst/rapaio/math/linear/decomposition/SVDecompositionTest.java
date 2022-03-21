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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.math.linear.DMatrix;

public class SVDecompositionTest {

    private static final double TOL = 1e-14;
    private static final int ROUNDS = 100;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

//    @Test
    void testBuilder() {

        for (int round = 0; round < ROUNDS; round++) {

            int n = RandomSource.nextInt(20) + 1;
            int m = RandomSource.nextInt(20) + n;

            DMatrix a = DMatrix.random(m, n);

            SVDecomposition svd = SVDecomposition.from(a);

            DMatrix u = svd.getU();
            DMatrix s = svd.getS();
            DMatrix v = svd.getV();

            assertTrue(a.deepEquals(u.dot(s).dot(v.t()), TOL));

            double[] sv = svd.getSingularValues();
            for (int i = 0; i < n - 1; i++) {
                assertEquals(s.get(i, i), sv[i], TOL);
                assertTrue(sv[i] >= sv[i + 1]);
            }

            assertEquals(sv[0], svd.norm2(), TOL);
            assertEquals(n, svd.rank());
        }
    }

    @Test
    void testDimension() {
        assertThrows(IllegalArgumentException.class, () -> SVDecomposition.from(DMatrix.random(10, 50)));
    }

    @Test
    void conditionNumberTest() {

        // for random matrices we expect a low condition number

        for (int i = 0; i < ROUNDS; i++) {
            double c = SVDecomposition.from(DMatrix.random(10, 10)).cond();
            assertTrue(Math.log10(c) < 4);
        }

        // for ill conditioned the condition number explodes

        Normal norm = Normal.of(0, 0.000001);

        for (int i = 0; i < 100; i++) {
            DMatrix a = DMatrix.random(10, 10);

            // we create the first column as a slightly modified
            // version of the second column, thus we have linearity
            for (int j = 0; j < 10; j++) {
                a.set(j, 0, a.get(j, 1) + norm.sampleNext());
            }

            double c = SVDecomposition.from(a).cond();
            assertTrue(Math.log10(c) > 5);
        }
    }
}
