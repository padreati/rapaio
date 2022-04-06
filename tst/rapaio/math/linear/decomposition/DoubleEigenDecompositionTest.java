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
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DMatrixDenseR;

public class DoubleEigenDecompositionTest {

    private static final double TOL = 1e-12;
    private static final int TIMES = 100;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void testInvalidShape() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> DMatrix.random(3, 4).evd());
        assertEquals("Only square matrices can have eigen decomposition.", ex.getMessage());
    }

    @Test
    void testSymmetric() {
        for (int i = 0; i < 1; i++) {
            DMatrix a = DMatrix.random(5, 5).scatter();
            var evd = a.evd();
            DMatrix v = evd.v();
            DMatrix d = evd.d();
            DMatrix vt = evd.v().t();

            assertTrue(a.deepEquals(v.dot(d).dot(vt), TOL));
        }
    }

    @Test
    void testNonSymmetric() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = DMatrix.random(10, 10);

            DoubleEigenDecomposition evd = a.evd();

            DMatrix v = evd.v();
            DMatrix d = evd.d();

            assertTrue(a.dot(v).deepEquals(v.dot(d), TOL));
        }
    }

    @Test
    void testRealImaginary() {
        DMatrix m = DMatrixDenseR.wrap(2, 2,
                1, -1,
                1, 1
        );
        var evd = m.evd();
        DVector real = evd.real();
        DVector imag = evd.imag();

        assertTrue(DVector.wrap(1, 1).deepEquals(real));
        assertTrue(DVector.wrap(-1, 1).deepEquals(imag));

        // diagonal contains imaginary coefficients also since we have multiple eigen values
        assertTrue(DMatrixDenseR.wrap(2, 2, 1, -1, 1, 1).deepEquals(evd.d()));
    }

    @Test
    void testPowerOfMatrix() {
        DMatrix a = DMatrix.random(3, 3);
        DMatrix ata = a.t().dot(a);

        var evd = ata.evd();

        for (double power = 1; power <= 5; power++) {

            DMatrix atap = ata.copy();
            for (int i = 1; i < power; i++) {
                atap = atap.dot(ata);
            }

            DMatrix ataevdp = evd.power(power);
            assertTrue(ataevdp.deepEquals(atap));
        }
    }
}
