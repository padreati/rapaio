/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.darray.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;

/**
 * Input validation of the matrix decompositions: an integer dtype, a non-matrix rank and a shape the
 * algorithm does not support must be refused at construction instead of producing a truncated or partly
 * meaningless result. Also covers the aliasing of the eigenvalue arrays and the symmetric-only matrix power.
 */
public class DecompositionValidationTest {

    private static final DArrayManager dm = DArrayManager.base();
    private static final double TOL = 1e-12;
    private final Random random = new Random(42);

    private static List<Function<DArray<?>, Object>> decompositions() {
        return List.of(DArray::lu, DArray::qr, DArray::svd, DArray::eig, DArray::cholesky);
    }

    @Test
    void integerTypesAreRefused() {
        for (DType<?> dt : new DType<?>[] {DType.INTEGER, DType.BYTE}) {
            DArray<?> x = dm.eye(dt, 4);
            for (Function<DArray<?>, Object> f : decompositions()) {
                var ex = assertThrows(IllegalArgumentException.class, () -> f.apply(x));
                assertEquals("Cannot compute decomposition for integer types (dtype: " + dt.id() + ")", ex.getMessage(),
                        dt + " " + f);
            }
        }
        // a truncated multiplier used to give a wrong determinant without any error
        DArray<Integer> a = dm.stride(DType.INTEGER, Shape.of(2, 2), Order.C, 2, 1, 4, 3);
        assertThrows(IllegalArgumentException.class, a::lu);
        assertEquals(2.0, DArrays.stride(Shape.of(2, 2), Order.C, 2, 1, 4, 3).lu().det(), TOL);
    }

    @Test
    void nonMatrixRanksAreRefused() {
        for (Shape shape : new Shape[] {Shape.of(4), Shape.of(2, 2, 2)}) {
            DArray<Double> x = dm.random(DType.DOUBLE, shape, random, Order.C);
            for (Function<DArray<?>, Object> f : decompositions()) {
                assertThrows(IllegalArgumentException.class, () -> f.apply(x), shape + " " + f);
            }
        }
    }

    @Test
    void qrRefusesMoreColumnsThanRows() {
        DArray<Double> wide = dm.random(DType.DOUBLE, Shape.of(2, 3), random, Order.C);
        var ex = assertThrows(IllegalArgumentException.class, wide::qr);
        assertEquals("For QR decomposition, number of rows must be greater or equal with number of columns.", ex.getMessage());

        // a square and a tall matrix are still accepted, and q is orthogonal with q*r == a
        for (Shape shape : new Shape[] {Shape.of(3, 3), Shape.of(5, 3)}) {
            DArray<Double> a = dm.random(DType.DOUBLE, shape, random, Order.C);
            QRDecomposition<Double> qr = a.qr();
            assertTrue(a.deepEquals(qr.q().mm(qr.r()), 1e-11), shape.toString());
            assertTrue(qr.q().t().mm(qr.q()).deepEquals(dm.eye(DType.DOUBLE, shape.dim(1)), 1e-11), shape.toString());
        }
    }

    @Test
    void luSolvesOnlySquareSystems() {
        DArray<Double> tall = dm.random(DType.DOUBLE, Shape.of(3, 2), random, Order.C);
        LUDecomposition<Double> lu = tall.lu();
        // the decomposition itself is defined for a tall matrix
        assertTrue(tall.sel(0, lu.pivots()).deepEquals(lu.l().mm(lu.u()), 1e-11));

        DArray<Double> b = dm.random(DType.DOUBLE, Shape.of(3), random, Order.C);
        var ex = assertThrows(IllegalArgumentException.class, () -> lu.solve(b));
        assertEquals("A system can be solved only for squared matrices.", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class, lu::inv);
        assertEquals("A system can be solved only for squared matrices.", ex.getMessage());

        // a square system still works
        DArray<Double> a = dm.random(DType.DOUBLE, Shape.of(3, 3), random, Order.C);
        DArray<Double> x = a.lu().solve(b);
        assertTrue(a.mv(x).deepEquals(b, 1e-10));
    }

    @Test
    void eigenvalueArraysAreNotAliasedWithTheInternalState() {
        DArray<Double> a = DArrays.stride(Shape.of(2, 2), Order.C, 2, 1, 1, 2);
        EigenDecomposition<Double> eig = a.eig();

        DArray<Double> real = eig.real();
        double first = real.getDouble(0);
        real.setDouble(-99, 0);
        assertEquals(first, eig.real().getDouble(0), TOL, "real() must not expose the internal array");
        assertEquals(first, eig.d().getDouble(0, 0), TOL);

        DArray<Double> imag = eig.imag();
        imag.setDouble(-99, 0);
        assertEquals(0, eig.imag().getDouble(0), TOL, "imag() must not expose the internal array");

        // the same for a float decomposition, which always copied
        DArray<Float> af = dm.stride(DType.FLOAT, Shape.of(2, 2), Order.C, 2f, 1f, 1f, 2f);
        EigenDecomposition<Float> eigf = af.eig();
        double firstFloat = eigf.real().getDouble(0);
        eigf.real().setDouble(-99, 0);
        assertEquals(firstFloat, eigf.real().getDouble(0), 1e-6);
    }

    @Test
    void matrixPowerIsRefusedForNonSymmetricMatrices() {
        // a rotation by 90 degrees: eigenvalues +i and -i, so the square is -I and V' is not the inverse of V
        DArray<Double> rotation = DArrays.stride(Shape.of(2, 2), Order.C, 0, 1, -1, 0);
        var ex = assertThrows(IllegalArgumentException.class, () -> rotation.eig().power(2));
        assertEquals("Matrix power can be computed only for symmetric matrices.", ex.getMessage());

        // the symmetric case is unchanged and correct
        DArray<Double> a = dm.random(DType.DOUBLE, Shape.of(3, 3), random, Order.C);
        DArray<Double> ata = a.t().mm(a);
        assertTrue(ata.eig().power(2).deepEquals(ata.mm(ata), 1e-11));
    }
}
