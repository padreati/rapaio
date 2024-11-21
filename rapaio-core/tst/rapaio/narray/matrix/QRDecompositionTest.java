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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Shape;

public class QRDecompositionTest {

    private static final double TOL = 1e-14;

    private static final int n = 10;
    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(1234);
    }

    @Test
    void testAll() {
        testSuite(NArrayManager.base(), DType.DOUBLE);
    }

    <N extends Number> void testSuite(NArrayManager tm, DType<N> dt) {
        testBasic(tm, dt);
        testHouseholderProperties(tm, dt);
        testLMS(tm, dt);
        testIncompatible(tm, dt);
        testSingular(tm, dt);
        testInv(tm, dt);
    }

    <N extends Number> void testBasic(NArrayManager tm, DType<N> dt) {
        for (int round = 0; round < 100; round++) {
            // generate a random matrix
            int off = random.nextInt(n);

            NArray<N> a = tm.random(dt, Shape.of(n + off, n), random);
            QRDecomposition<N> qr = a.qr();

            NArray<N> q = qr.q();
            NArray<N> r = qr.r();

            // test various properties of the decomposition
            NArray<N> I = tm.eye(dt, n);
            assertTrue(I.deepEquals(q.t().mm(q), TOL));

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i > j) {
                        assertEquals(0.0, r.getDouble(i, j), TOL);
                    }
                }
            }
            assertTrue(a.deepEquals(q.mm(r), TOL));
            // in general a random matrix is of full rank
            assertTrue(qr.isFullRank());
        }
    }

    /**
     * Test is done using householder reflections described <a href="https://en.wikipedia.org/wiki/Householder_transformation">here</a>.
     */
    <N extends Number> void testHouseholderProperties(NArrayManager tm, DType<N> dt) {

        for (int round = 0; round < 100; round++) {
            // generate a random matrix
            NArray<N> a = tm.random(dt, Shape.of(n, n), random);
            QRDecomposition<N> qr = a.qr();

            NArray<N> h = qr.h();
            NArray<N> p = tm.eye(dt, 10).sub(h.mul(dt.cast(2)).mm(h.t()));

            // p is hermitian
            assertTrue(p.deepEquals(p.t(), TOL));
        }
    }

    /**
     * Tests least mean squares solutions using qr decomposition
     */
    <N extends Number> void testLMS(NArrayManager tm, DType<N> dt) {

        Normal normal = Normal.std();
        Uniform unif = Uniform.of(0, 100);

        for (int round = 0; round < 5; round++) {

            // we define a linear process
            // y = 3 + 2 * x1 - 2 * x2 + e; e ~ normal(0,1)
            // and take some samples

            int rows = 8_000;
            NArray<N> a = tm.zeros(dt, Shape.of(rows, 3));
            NArray<N> b = tm.zeros(dt, Shape.of(rows, 1));

            for (int i = 0; i < rows; i++) {
                double x1 = unif.sampleNext(random);
                double x2 = unif.sampleNext(random);
                double e = normal.sampleNext(random);
                double y = 3 + 2 * x1 - 2 * x2 + e;

                a.setDouble(1, i, 0);
                a.setDouble(x1, i, 1);
                a.setDouble(x2, i, 2);

                b.setDouble(y, i, 0);
            }

            NArray<N> x = a.qr().solve(b);

            double c0 = x.getDouble(0, 0);
            double c1 = x.getDouble(1, 0);
            double c2 = x.getDouble(2, 0);

            assertTrue(c0 >= 2.9 && c0 <= 3.1);
            assertTrue(c1 >= 1.9 && c1 <= 2.1);
            assertTrue(c2 >= -2.1 && c2 <= -1.9);
        }
    }

    <N extends Number> void testIncompatible(NArrayManager tm, DType<N> dt) {
        assertThrows(IllegalArgumentException.class,
                () -> tm.random(dt, Shape.of(10, 10), random).qr().solve(tm.random(dt, Shape.of(12, 1), random)));
    }

    <N extends Number> void testSingular(NArrayManager tm, DType<N> dt) {
        assertThrows(RuntimeException.class, () -> tm.full(dt, Shape.of(10, 10), dt.cast(2))
                .qr().solve(tm.random(dt, Shape.of(10, 1), random)));
    }

    <N extends Number> void testInv(NArrayManager tm, DType<N> dt) {
        NArray<N> m = tm.random(dt, Shape.of(4, 4), random);
        NArray<N> inv = m.qr().inv();
        assertTrue(inv.deepEquals(m.qr().solve(tm.eye(dt, 4))));

        NArray<N> v = tm.stride(dt, 1, 2, 3, 4);
        NArray<N> x = m.qr().solve(v);

        assertTrue(v.deepEquals(m.mv(x), TOL));
    }
}
