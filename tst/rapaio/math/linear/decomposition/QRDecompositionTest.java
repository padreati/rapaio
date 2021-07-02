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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.math.linear.DMatrix;

import static org.junit.jupiter.api.Assertions.*;

public class QRDecompositionTest {

    private static final double TOL = 1e-14;

    final int n = 10;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testBasic() {
        for (int round = 0; round < 100; round++) {

            // generate a random matrix

            int off = RandomSource.nextInt(n);

            DMatrix a = DMatrix.random(n + off, n);
            QRDecomposition qr = QRDecomposition.from(a);

            DMatrix q = qr.getQ();
            DMatrix r = qr.getR();

            // test various properties of the decomposition

            DMatrix I = DMatrix.identity(n);
            assertTrue(I.deepEquals(q.t().dot(q), TOL));

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i > j) {
                        assertEquals(0.0, r.get(i, j), TOL);
                    }
                }
            }

            assertTrue(a.deepEquals(q.dot(r), TOL));

            // in general a random matrix is of full rank

            assertTrue(qr.isFullRank());
        }
    }

    /**
     * Test is done using householder reflections described
     * here: https://en.wikipedia.org/wiki/Householder_transformation
     */
    @Test
    void testHouseholderProperties() {

        for (int round = 0; round < 100; round++) {

            // generate a random matrix

            DMatrix a = DMatrix.random(n, n);
            QRDecomposition qr = QRDecomposition.from(a);

            DMatrix h = qr.getH();
            DMatrix p = DMatrix.identity(10).sub(h.mult(2).dot(h.t()));

            // p is hermitian
            assertTrue(p.deepEquals(p.t(), TOL));
        }
    }

    /**
     * Tests least mean squares solutions using qr decomposition
     */
    @Test
    void testLMS() {

        Normal normal = Normal.std();
        Uniform unif = Uniform.of(0, 100);

        for (int round = 0; round < 5; round++) {

            // we define a linear process
            // y = 3 + 2 * x1 - 2 * x2 + e; e ~ normal(0,1)
            // and take some samples

            int rows = 8_000;
            DMatrix a = DMatrix.empty(rows, 3);
            DMatrix b = DMatrix.empty(rows, 1);

            for (int i = 0; i < rows; i++) {
                double x1 = unif.sampleNext();
                double x2 = unif.sampleNext();
                double e = normal.sampleNext();
                double y = 3 + 2 * x1 - 2 * x2 + e;

                a.set(i, 0, 1);
                a.set(i, 1, x1);
                a.set(i, 2, x2);

                b.set(i, 0, y);
            }

            DMatrix x = QRDecomposition.from(a).solve(b);

            double c0 = x.get(0, 0);
            double c1 = x.get(1, 0);
            double c2 = x.get(2, 0);

            assertTrue(c0 >= 2.9 && c0 <= 3.1);
            assertTrue(c1 >= 1.9 && c1 <= 2.1);
            assertTrue(c2 >= -2.1 && c2 <= -1.9);
        }
    }

    @Test
    void testIncompatible() {
        assertThrows(IllegalArgumentException.class, () -> QRDecomposition.from(DMatrix.random(10, 10)).solve(DMatrix.random(12, 1)));
    }

    @Test
    void testSingular() {
        assertThrows(RuntimeException.class, () -> QRDecomposition.from(DMatrix.fill(10, 10, 2)).solve(DMatrix.random(10, 1)));
    }
}
