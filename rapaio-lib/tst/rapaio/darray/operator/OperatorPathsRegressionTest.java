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

package rapaio.darray.operator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.Test;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import rapaio.darray.Compare;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Simd;
import rapaio.darray.storage.IntStorage;
import rapaio.data.OperationNotAvailableException;
import rapaio.data.VarDouble;
import rapaio.data.VarFloat;

/**
 * Regression tests for the operator layer. Each test exercises one code path which is not reached by
 * contiguous, SIMD-backed arrays: views with several offsets, scalar tails, non-SIMD storages, and
 * the double-typed comparison reference. Every test fails on the previous implementation.
 */
public class OperatorPathsRegressionTest {

    private static final DArrayManager dm = DArrayManager.base();
    private static final double TOL = 1e-12;

    /**
     * A view of the first half of the columns of a two-row C-order matrix: its loop descriptor has two offsets,
     * one per row, and a bound large enough to use the SIMD lanes. The first row holds a single 2 among ones,
     * the second row only ones, so the product over the view is 2 and any double counting of the first row's
     * partial product is visible.
     */
    private static <N extends Number> DArray<N> twoOffsetsView(DType<N> dt, int simdLen) {
        DArray<N> m = dm.full(dt, Shape.of(2, 4 * simdLen), 1, Order.C);
        m.setDouble(2, 0, 0);
        return m.narrow(1, 0, 2 * simdLen);
    }

    @Test
    void prodOverAViewWithSeveralOffsetsMultipliesEachElementOnce() {
        assertEquals(2, twoOffsetsView(DType.DOUBLE, Simd.vsDouble.length()).prod(), TOL);
        assertEquals(2f, twoOffsetsView(DType.FLOAT, Simd.vsFloat.length()).prod(), 1e-6f);
        assertEquals(2, twoOffsetsView(DType.INTEGER, Simd.vsInt.length()).prod());
        assertEquals((byte) 2, twoOffsetsView(DType.BYTE, Simd.vsByte.length()).prod());

        assertEquals(2, twoOffsetsView(DType.DOUBLE, Simd.vsDouble.length()).nanProd(), TOL);
        assertEquals(2f, twoOffsetsView(DType.FLOAT, Simd.vsFloat.length()).nanProd(), 1e-6f);
        assertEquals(2, twoOffsetsView(DType.INTEGER, Simd.vsInt.length()).nanProd());
        assertEquals((byte) 2, twoOffsetsView(DType.BYTE, Simd.vsByte.length()).nanProd());

        // the same on the strided view of the transposed matrix (step != 1 path)
        DArray<Double> t = twoOffsetsView(DType.DOUBLE, Simd.vsDouble.length()).t();
        assertEquals(2, t.prod(), TOL);
        assertEquals(2, t.nanProd(), TOL);
    }

    @Test
    void softmaxOverAViewWithSeveralOffsetsSumsToOne() {
        for (DType<?> dt : new DType<?>[] {DType.DOUBLE, DType.FLOAT}) {
            int len = dt == DType.DOUBLE ? Simd.vsDouble.length() : Simd.vsFloat.length();
            DArray<?> view = dm.zeros(dt, Shape.of(2, 4 * len), Order.C).narrow(1, 0, 2 * len);
            DArray<?> contiguous = view.copy();
            view.softmax_();
            contiguous.softmax_();
            assertEquals(1.0, view.sum().doubleValue(), 1e-6, dt.toString());
            assertTrue(view.deepEquals(contiguous, 1e-6), dt + ": the view and its contiguous copy must agree");
            assertEquals(1.0 / (4 * len), view.getDouble(0, 0), 1e-6);

            DArray<?> strided = dm.zeros(dt, Shape.of(2, 4 * len), Order.C).narrow(1, 0, 2 * len).t();
            strided.softmax_();
            assertEquals(1.0, strided.sum().doubleValue(), 1e-6, dt + " strided");
        }
    }

    @Test
    void logsoftmaxOverAViewWithSeveralOffsetsIsALogDensity() {
        for (DType<?> dt : new DType<?>[] {DType.DOUBLE, DType.FLOAT}) {
            int len = dt == DType.DOUBLE ? Simd.vsDouble.length() : Simd.vsFloat.length();
            DArray<?> view = dm.zeros(dt, Shape.of(2, 4 * len), Order.C).narrow(1, 0, 2 * len);
            view.logsoftmax_();
            assertEquals(1.0, view.exp().sum().doubleValue(), 1e-6, dt.toString());
            assertEquals(-Math.log(4 * len), view.getDouble(0, 0), 1e-6);

            DArray<?> strided = dm.zeros(dt, Shape.of(2, 4 * len), Order.C).narrow(1, 0, 2 * len).t();
            strided.logsoftmax_();
            assertEquals(1.0, strided.exp().sum().doubleValue(), 1e-6, dt + " strided");
        }
    }

    @Test
    void logsoftmaxOnANonSimdDoubleStorageTakesTheLogarithm() {
        // VarFloat.darray_(DOUBLE) wraps the variable in a VarDoubleStorage, which does not support SIMD
        DArray<Double> generic = VarFloat.wrap(0f, 0f).darray_(DType.DOUBLE);
        assertFalse(generic.storage().supportSimd());
        generic.logsoftmax_();
        assertEquals(-Math.log(2), generic.getDouble(0), 1e-6);
        assertEquals(-Math.log(2), generic.getDouble(1), 1e-6);

        DArray<Double> simd = DArrays.stride(0, 0).logsoftmax_();
        assertTrue(generic.deepEquals(simd, 1e-6));
    }

    @Test
    void minOnANonSimdFloatStorageStartsFromInfinity() {
        DArray<Float> generic = VarDouble.wrap(1, 2, 3).darray_(DType.FLOAT);
        assertFalse(generic.storage().supportSimd());
        assertEquals(1f, generic.amin());
        assertEquals(1f, generic.nanMin());
        assertEquals(1f, VarDouble.wrap(1, Double.NaN, 3).darray_(DType.FLOAT).nanMin());
    }

    @Test
    void nanMeanFloatIncludesTheScalarTail() {
        int len = Simd.vsFloat.length();
        float[] values = new float[len + 1];
        Arrays.fill(values, 1f);
        values[len] = 5f;
        assertEquals((len + 5.0) / (len + 1), dm.stride(DType.FLOAT, values).nanMean(), 1e-6);
        values[len] = Float.NaN;
        assertEquals(1f, dm.stride(DType.FLOAT, values).nanMean(), 1e-6);
    }

    @Test
    void nanProdDoubleKeepsDoublePrecisionInTheScalarTail() {
        int len = Simd.vsDouble.length();
        double[] values = new double[len + 1];
        Arrays.fill(values, 1.0);
        values[len] = 0.1;
        assertEquals(0.1, dm.stride(DType.DOUBLE, values).nanProd());
    }

    @Test
    void compareMaskOnDoublesUsesADoubleReference() {
        assertEquals(1.0, DArrays.stride(0.1).compareMask_(Compare.EQ, 0.1).getDouble(0));
        assertEquals(0.0, DArrays.stride(0.1).compareMask_(Compare.LT, 0.1).getDouble(0));
        assertEquals(0.0, DArrays.stride(0.1).compareMask_(Compare.GT, 0.1).getDouble(0));
        assertEquals(1.0, DArrays.stride(0.1).compareMask_(Compare.LE, 0.1).getDouble(0));
        // the same through the strided and generic paths
        DArray<Double> col = DArrays.stride(Shape.of(2, 2), Order.C, 0.1, 0.5, 0.1, 0.5).narrow(1, false, 0, 1);
        assertEquals(2.0, col.compareMask_(Compare.EQ, 0.1).sum());
        // a mapped VarDouble is wrapped in a VarDoubleStorage without SIMD support: the generic path
        DArray<Double> generic = VarDouble.wrap(0.1, 0.5).mapRows(0, 1).darray_(DType.DOUBLE);
        assertFalse(generic.storage().supportSimd());
        assertEquals(1.0, generic.compareMask_(Compare.EQ, 0.1).sum());
    }

    @Test
    void varianceWithAPrecomputedMeanIsTheSampleVarianceWhateverTheShift() {
        DArray<Double> x = DArrays.stride(0, 2);
        assertEquals(2.0, x.var(1), TOL);
        assertEquals(2.0, x.var(1, 0.0), TOL);
        assertEquals(2.0, x.var(1, 1.0), TOL);
        assertEquals(2.0, x.var(1, 100.0), TOL);

        Random random = new Random(1);
        DArray<Double> y = dm.random(DType.DOUBLE, Shape.of(3, 50), random, Order.C);
        double reference = y.var(1);
        for (double shift : new double[] {-10, 0, 0.37, 1e3}) {
            assertEquals(reference, y.var(1, shift), 1e-8 * Math.max(1, shift * shift));
        }
        // the strided path
        DArray<Double> col = y.narrow(1, false, 0, 1);
        assertEquals(col.var(1), col.var(1, 0.0), 1e-10);
    }

    @Test
    void nanSumOfOppositeInfinitiesIsNanRegardlessOfTheirPosition() {
        int len = Simd.vsDouble.length();
        double[] sameLane = new double[2 * len];
        sameLane[0] = Double.POSITIVE_INFINITY;
        sameLane[len] = Double.NEGATIVE_INFINITY;
        assertTrue(Double.isNaN(dm.stride(DType.DOUBLE, sameLane).nanSum()), "infinities in the same SIMD lane");
        assertTrue(Double.isNaN(DArrays.stride(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0).nanSum()), "adjacent infinities");
        assertTrue(Double.isNaN(dm.stride(DType.DOUBLE, sameLane).nanMean()), "nanMean, same lane");

        double[] zeroTimesInf = new double[2 * len];
        Arrays.fill(zeroTimesInf, 1.0);
        zeroTimesInf[0] = 0;
        zeroTimesInf[len] = Double.POSITIVE_INFINITY;
        assertTrue(Double.isNaN(dm.stride(DType.DOUBLE, zeroTimesInf).nanProd()), "0 * Inf in the same SIMD lane");
        assertTrue(Double.isNaN(DArrays.stride(0, Double.POSITIVE_INFINITY, 1).nanProd()), "0 * Inf adjacent");

        // a real NaN is still skipped
        assertEquals(3.0, DArrays.stride(1, Double.NaN, 2).nanSum(), TOL);
        assertEquals(2.0, DArrays.stride(1, Double.NaN, 2).nanProd(), TOL);
    }

    @Test
    void intReductionsOnANonSimdStorageReadWholeInts() {
        DArray<Integer> x = dm.stride(DType.INTEGER, Shape.of(2), Order.C, new ScalarIntStorage(new int[] {300, 400}));
        assertFalse(x.storage().supportSimd());
        assertEquals(300, x.amin());
        assertEquals(300, x.nanMin());
        assertEquals(120_000, x.prod());
        assertEquals(120_000, x.nanProd());
    }

    @Test
    void intUnaryOperatorsOnANonSimdStorageRespectTheStride() {
        int[] data = {-1, -2, -3, -4, -5, -6};
        DArray<Integer> m = dm.stride(DType.INTEGER, Shape.of(2, 3), Order.C, new ScalarIntStorage(data));
        // first column: a rank-1 view with stride 3
        m.narrow(1, false, 0, 1).abs_();
        assertArrayEquals(new int[] {1, -2, -3, 4, -5, -6}, data);
        m.narrow(1, false, 1, 2).neg_();
        assertArrayEquals(new int[] {1, 2, -3, 4, 5, -6}, data);
        m.narrow(1, false, 2, 3).sqr_();
        assertArrayEquals(new int[] {1, 2, 9, 4, 5, 36}, data);
    }

    /**
     * Int storage over a plain array which declares no SIMD support, so that the {@code Default} reduce paths
     * and the {@code applyGeneric*} unary paths are exercised for integers.
     */
    /**
     * The package documentation states that a reduction which is only meaningful for floating point values throws
     * {@code OperationNotAvailableException} on an integer array. Every path used to throw
     * {@code IllegalArgumentException}, which that exception does not extend, so a caller following the documentation
     * caught nothing; the one guard which did throw the documented type was unreachable, because the manager repeated
     * the check three lines earlier.
     */
    @Test
    void floatingPointOnlyOperationsThrowTheDocumentedException() {
        for (DType<?> dt : new DType<?>[] {DType.INTEGER, DType.BYTE}) {
            DArray<?> x = dm.seq(dt, Shape.of(4));
            String name = dt.id().toString();

            // reductions, guarded by DArrayReduceOp
            assertThrows(OperationNotAvailableException.class, x::mean, name);
            assertThrows(OperationNotAvailableException.class, x::nanMean, name);
            assertThrows(OperationNotAvailableException.class, () -> x.var(0), name);
            assertThrows(OperationNotAvailableException.class, () -> x.std(0), name);
            assertThrows(OperationNotAvailableException.class, () -> x.mean1d(0), name);

            // unary operations, guarded by DArrayUnaryOp
            assertThrows(OperationNotAvailableException.class, () -> x.copy().exp_(), name);
            assertThrows(OperationNotAvailableException.class, () -> x.copy().log_(), name);
            assertThrows(OperationNotAvailableException.class, () -> x.copy().sqrt_(), name);
            assertThrows(OperationNotAvailableException.class, () -> x.copy().tanh_(), name);

            // the reductions which are defined for an integer array are unaffected
            assertEquals(6, x.sum().intValue(), name);
            assertEquals(3, x.amax().intValue(), name);
        }

        // and the same operations work on a floating point array
        DArray<Double> d = dm.seq(DType.DOUBLE, Shape.of(4));
        assertEquals(1.5, d.mean(), TOL);
        assertEquals(1.0, d.copy().exp_().getDouble(0), TOL);
    }

    /**
     * A nan-aware extremum starts from the lowest (highest) value of the type and a NaN never replaces it, so an array
     * with no non-NaN value used to return that sentinel: an infinity which is not in the data. Both the vectorised and
     * the scalar path are checked, and so is the per-axis variant, which reaches the same operator.
     */
    @Test
    void nanExtremaOfAnAllNanArrayAreNaN() {
        double n = Double.NaN;

        // long enough to reach the SIMD path, and a non-SIMD storage for the scalar path
        DArray<Double> simd = dm.full(DType.DOUBLE, Shape.of(4 * Simd.vsDouble.length()), n);
        assertTrue(simd.storage().supportSimd());
        DArray<Double> generic = VarFloat.fill(8, Float.NaN).darray_(DType.DOUBLE);
        assertFalse(generic.storage().supportSimd());

        for (DArray<Double> x : java.util.List.of(simd, generic)) {
            String name = x.storage().supportSimd() ? "simd" : "generic";
            assertTrue(Double.isNaN(x.nanMax()), name);
            assertTrue(Double.isNaN(x.nanMin()), name);
        }

        // a float array as well, which has its own set of paths
        DArray<Float> f = dm.full(DType.FLOAT, Shape.of(4 * Simd.vsFloat.length()), Float.NaN);
        assertTrue(Float.isNaN(f.nanMax()));
        assertTrue(Float.isNaN(f.nanMin()));

        // the per-axis variant: the all-NaN row yields NaN, the others keep their extremum
        DArray<Double> m = dm.stride(DType.DOUBLE, Shape.of(3, 2), Order.C, new double[] {n, n, 1, 5, n, 2});
        DArray<Double> rowMax = m.nanMax1d(1);
        assertTrue(Double.isNaN(rowMax.getDouble(0)));
        assertEquals(5.0, rowMax.getDouble(1), TOL);
        assertEquals(2.0, rowMax.getDouble(2), TOL);

        // a sentinel which is genuinely in the data is still reported, and is not confused with the empty case
        DArray<Double> withInfinity = dm.stride(DType.DOUBLE, Shape.of(3), Order.C,
                new double[] {Double.NEGATIVE_INFINITY, n, Double.NEGATIVE_INFINITY});
        assertEquals(Double.NEGATIVE_INFINITY, withInfinity.nanMax());
        DArray<Double> withPositiveInfinity = dm.stride(DType.DOUBLE, Shape.of(3), Order.C,
                new double[] {Double.POSITIVE_INFINITY, n, Double.POSITIVE_INFINITY});
        assertEquals(Double.POSITIVE_INFINITY, withPositiveInfinity.nanMin());

        // the partially-NaN case is unchanged, and nanSum of an all-NaN array stays zero as numpy has it
        assertEquals(3.0, dm.stride(DType.DOUBLE, Shape.of(3), Order.C, new double[] {n, 3, n}).nanMax(), TOL);
        assertEquals(0.0, simd.nanSum(), TOL);
    }

    private static final class ScalarIntStorage extends IntStorage {

        private final int[] array;

        ScalarIntStorage(int[] array) {
            this.array = array;
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public boolean supportSimd() {
            return false;
        }

        @Override
        public int getInt(int ptr) {
            return array[ptr];
        }

        @Override
        public void setInt(int ptr, int value) {
            array[ptr] = value;
        }

        @Override
        public void incInt(int ptr, int value) {
            array[ptr] += value;
        }

        @Override
        public void fill(int value, int start, int len) {
            Arrays.fill(array, start, start + len, value);
        }

        @Override
        public IntVector getIntVector(int offset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntVector getIntVector(int offset, int[] idx, int idxOffset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setIntVector(IntVector value, int offset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setIntVector(IntVector value, int offset, int[] idx, int idxOffset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntVector getIntVector(int offset, VectorMask<Integer> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IntVector getIntVector(int offset, int[] idx, int idxOffset, VectorMask<Integer> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setIntVector(IntVector value, int offset, VectorMask<Integer> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setIntVector(IntVector value, int offset, int[] idx, int idxOffset, VectorMask<Integer> m) {
            throw new UnsupportedOperationException();
        }
    }
}
