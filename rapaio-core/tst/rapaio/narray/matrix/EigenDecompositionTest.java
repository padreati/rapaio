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

import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Order;
import rapaio.narray.Shape;

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
        testSuite(NArrayManager.base(), DType.DOUBLE);
    }

    <N extends Number> void testSuite(NArrayManager tm, DType<Double> dt) {
        testInvalidShape(tm, dt);
        testSymmetric(tm, dt);
        testNonSymmetric(tm, dt);
        testRealImaginary(tm, dt);
        testPowerOfMatrix(tm, dt);
    }

    <N extends Number> void testInvalidShape(NArrayManager tm, DType<N> dt) {
        var ex = assertThrows(IllegalArgumentException.class, () -> tm.random(dt, Shape.of(3, 4), random).eig());
        assertEquals("Only square matrices can have eigen decomposition.", ex.getMessage());
    }

    <N extends Number> void testSymmetric(NArrayManager tm, DType<N> dt) {
        NArray<N> x = tm.random(dt, Shape.of(5, 5), random);
        NArray<N> a = x.sub(x.mean1d(0)).t().mm(x.sub(x.mean1d(0)));
        var eig = a.eig();
        NArray<N> v = eig.v();
        NArray<N> d = eig.d();
        NArray<N> vt = eig.v().t();

        assertTrue(a.deepEquals(v.mm(d).mm(vt), 1e-1));
    }

    <N extends Number> void testNonSymmetric(NArrayManager tm, DType<N> dt) {
        for (int i = 0; i < TIMES; i++) {
            NArray<N> a = tm.random(dt, Shape.of(10, 10), random);

            EigenDecomposition<N> eig = a.eig();

            NArray<N> v = eig.v();
            NArray<N> d = eig.d();

            assertTrue(a.mm(v).deepEquals(v.mm(d), TOL));
        }
    }

    <N extends Number> void testRealImaginary(NArrayManager tm, DType<N> dt) {
        NArray<N> m = tm.stride(dt, Shape.of(2, 2), Order.C, 1, -1,1, 1);
        var eig = m.eig();
        NArray<N> real = eig.real();
        NArray<N> imag = eig.imag();

        assertTrue(tm.stride(dt, 1, 1).deepEquals(real));
        assertTrue(tm.stride(dt, -1, 1).deepEquals(imag));

        // diagonal contains imaginary coefficients also since we have multiple eigen values
        assertTrue(tm.stride(dt, Shape.of(2, 2), Order.C, 1, -1, 1, 1).deepEquals(eig.d()));
    }

    <N extends Number> void testPowerOfMatrix(NArrayManager tm, DType<N> dt) {
        NArray<N> a = tm.random(dt, Shape.of(3, 3), random);
        NArray<N> ata = a.t().mm(a);

        var eig = ata.eig();

        for (double power = 1; power <= 5; power++) {

            NArray<N> atap = ata.copy();
            for (int i = 1; i < power; i++) {
                atap = atap.mm(ata);
            }

            NArray<N> ataevdp = eig.power(power);
            assertTrue(ataevdp.deepEquals(atap, 1e-14));
        }
    }
}
