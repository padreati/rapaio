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

package rapaio.math.tensor.matrix;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;

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
        testSuite(Tensors.ofDouble());
    }

    <N extends Number> void testSuite(TensorManager.OfType<N> tmt) {
        testBasic(tmt);
        testHouseholderProperties(tmt);
        testLMS(tmt);
        testIncompatible(tmt);
        testSingular(tmt);
        testInv(tmt);
    }

    <N extends Number> void testBasic(TensorManager.OfType<N> tmt) {
        for (int round = 0; round < 100; round++) {
            // generate a random matrix
            int off = random.nextInt(n);

            Tensor<N> a = tmt.random(Shape.of(n + off, n), random);
            QRDecomposition<N> qr = a.qr();

            Tensor<N> q = qr.q();
            Tensor<N> r = qr.r();

            // test various properties of the decomposition
            Tensor<N> I = tmt.eye(n);
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
    <N extends Number> void testHouseholderProperties(TensorManager.OfType<N> tmt) {

        for (int round = 0; round < 100; round++) {
            // generate a random matrix
            Tensor<N> a = tmt.random(Shape.of(n, n), random);
            QRDecomposition<N> qr = a.qr();

            Tensor<N> h = qr.h();
            Tensor<N> p = tmt.eye(10).sub(h.mul(tmt.dtype().castValue(2)).mm(h.t()));

            // p is hermitian
            assertTrue(p.deepEquals(p.t(), TOL));
        }
    }

    /**
     * Tests least mean squares solutions using qr decomposition
     */
    <N extends Number> void testLMS(TensorManager.OfType<N> tmt) {

        Normal normal = Normal.std();
        Uniform unif = Uniform.of(0, 100);

        for (int round = 0; round < 5; round++) {

            // we define a linear process
            // y = 3 + 2 * x1 - 2 * x2 + e; e ~ normal(0,1)
            // and take some samples

            int rows = 8_000;
            Tensor<N> a = tmt.zeros(Shape.of(rows, 3));
            Tensor<N> b = tmt.zeros(Shape.of(rows, 1));

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

            Tensor<N> x = a.qr().solve(b);

            double c0 = x.getDouble(0, 0);
            double c1 = x.getDouble(1, 0);
            double c2 = x.getDouble(2, 0);

            assertTrue(c0 >= 2.9 && c0 <= 3.1);
            assertTrue(c1 >= 1.9 && c1 <= 2.1);
            assertTrue(c2 >= -2.1 && c2 <= -1.9);
        }
    }

    <N extends Number> void testIncompatible(TensorManager.OfType<N> tmt) {
        assertThrows(IllegalArgumentException.class,
                () -> tmt.random(Shape.of(10, 10), random).qr().solve(tmt.random(Shape.of(12, 1), random)));
    }

    <N extends Number> void testSingular(TensorManager.OfType<N> ofType) {
        assertThrows(RuntimeException.class, () -> ofType.full(Shape.of(10, 10), ofType.dtype().castValue(2))
                .qr().solve(ofType.random(Shape.of(10, 1), random)));
    }

    <N extends Number> void testInv(TensorManager.OfType<N> tmt) {
        Tensor<N> m = tmt.random(Shape.of(4, 4), random);
        Tensor<N> inv = m.qr().inv();
        assertTrue(inv.deepEquals(m.qr().solve(tmt.eye(4))));

        Tensor<N> v = tmt.stride(1, 2, 3, 4);
        Tensor<N> x = m.qr().solve(v);

        assertTrue(v.deepEquals(m.mv(x), TOL));
    }
}
