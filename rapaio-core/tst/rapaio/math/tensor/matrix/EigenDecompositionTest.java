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

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;

public class EigenDecompositionTest {

    private static final double TOL = 1e-12;
    private static final int TIMES = 100;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testAll() {
        testSuite(TensorManager.base().ofDouble());
    }

    <N extends Number> void testSuite(TensorManager.OfType<N> tmt) {
        testInvalidShape(tmt);
        testSymmetric(tmt);
        testNonSymmetric(tmt);
        testRealImaginary(tmt);
        testPowerOfMatrix(tmt);
    }

    <N extends Number> void testInvalidShape(TensorManager.OfType<N> tmt) {
        var ex = assertThrows(IllegalArgumentException.class, () -> tmt.random(Shape.of(3, 4), random).eig());
        assertEquals("Only square matrices can have eigen decomposition.", ex.getMessage());
    }

    <N extends Number> void testSymmetric(TensorManager.OfType<N> ofType) {
        Tensor<N> x = ofType.random(Shape.of(5, 5), random);
        Tensor<N> a = x.bsub(0, x.mean(0)).t().mm(x.bsub(0, x.mean(0)));
        var eig = a.eig();
        Tensor<N> v = eig.v();
        Tensor<N> d = eig.d();
        Tensor<N> vt = eig.v().t();

        assertTrue(a.deepEquals(v.mm(d).mm(vt), 1e-1));
    }

    <N extends Number> void testNonSymmetric(TensorManager.OfType<N> tmt) {
        for (int i = 0; i < TIMES; i++) {
            Tensor<N> a = tmt.random(Shape.of(10, 10), random);

            EigenDecomposition<N> eig = a.eig();

            Tensor<N> v = eig.v();
            Tensor<N> d = eig.d();

            assertTrue(a.mm(v).deepEquals(v.mm(d), TOL));
        }
    }

    <N extends Number> void testRealImaginary(TensorManager.OfType<N> tmt) {
        Tensor<N> m = tmt.stride(Shape.of(2, 2), Order.C, 1, -1,1, 1);
        var eig = m.eig();
        Tensor<N> real = eig.real();
        Tensor<N> imag = eig.imag();

        assertTrue(tmt.stride(1, 1).deepEquals(real));
        assertTrue(tmt.stride(-1, 1).deepEquals(imag));

        // diagonal contains imaginary coefficients also since we have multiple eigen values
        assertTrue(tmt.stride(Shape.of(2, 2), Order.C, 1, -1, 1, 1).deepEquals(eig.d()));
    }

    <N extends Number> void testPowerOfMatrix(TensorManager.OfType<N> tmt) {
        Tensor<N> a = tmt.random(Shape.of(3, 3), random);
        Tensor<N> ata = a.t().mm(a);

        var eig = ata.eig();

        for (double power = 1; power <= 5; power++) {

            Tensor<N> atap = ata.copy();
            for (int i = 1; i < power; i++) {
                atap = atap.mm(ata);
            }

            Tensor<N> ataevdp = eig.power(power);
            assertTrue(ataevdp.deepEquals(atap, 1e-14));
        }
    }
}
