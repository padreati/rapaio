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

package rapaio.darray.manager.base;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.util.Hardware.L2_CACHE_SIZE;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;

/**
 * Behaviour of the strided darray implementation which the existing tests do not reach: reductions over every
 * axis with keepDim, a precomputed mean, non-square diagonals, an explicit multiplication target, a destination
 * of a different shape, sentinel-valued arrays, F-ordered convolution kernels and window counts which do not
 * divide the stride. Every assertion holds for all four dtypes where the dtype allows it.
 */
public class StrideDArrayContractTest {

    private static final DArrayManager dm = DArrayManager.base();
    private static final double TOL = 1e-12;
    private final Random random = new Random(42);

    @Test
    void reduceOnEveryAxisHonoursKeepDim() {
        DArray<Double> x = DArrays.seq(Shape.of(2, 3));
        assertEquals(0, x.sumOn(Shape.of(2, 3), false).rank());
        DArray<Double> kept = x.sumOn(Shape.of(2, 3), true);
        assertArrayEquals(new int[] {1, 1}, kept.shape().dims());
        assertEquals(x.sum(), kept.getDouble(0, 0), TOL);

        DArray<Double> cube = DArrays.seq(Shape.of(2, 3, 4));
        assertArrayEquals(new int[] {1, 1, 1}, cube.sumOn(Shape.of(2, 3, 4), true).shape().dims());
        // a partial reduction was already correct and must stay so
        assertArrayEquals(new int[] {2, 1, 1}, cube.sumOn(Shape.of(3, 4), true).shape().dims());
        assertArrayEquals(new int[] {2}, cube.sumOn(Shape.of(3, 4), false).shape().dims());
    }

    @Test
    void varOnAcceptsTheMeanShapeItsMessageDemands() {
        DArray<Double> x = DArrays.seq(Shape.of(3, 4));

        // the result shape itself
        DArray<Double> reduced = x.varOn(Shape.of(4), 0, false, x.meanOn(Shape.of(4), false));
        assertArrayEquals(new int[] {3}, reduced.shape().dims());
        for (int i = 0; i < 3; i++) {
            assertEquals(x.selsq(0, i).var(0), reduced.getDouble(i), 1e-10);
        }
        // and its keepDim form, which is what the nn layers pass
        DArray<Double> keptMean = x.meanOn(Shape.of(4), true);
        assertTrue(reduced.deepEquals(x.varOn(Shape.of(4), 0, false, keptMean), 1e-10));

        // a mean with the wrong number of values is refused instead of silently exhausting its iterator
        var ex = assertThrows(IllegalArgumentException.class,
                () -> x.varOn(Shape.of(4), 0, false, DArrays.zeros(Shape.of(2))));
        assertEquals("Mean darray must have the same number of values as the result array.", ex.getMessage());

        // full reduction with keepDim
        assertArrayEquals(new int[] {1, 1}, x.varOn(Shape.of(3, 4), 0, true, DArrays.scalar(x.mean())).shape().dims());
        assertEquals(x.var(0), x.varOn(Shape.of(3, 4), 0, true, DArrays.scalar(x.mean())).getDouble(0, 0), 1e-10);
    }

    @Test
    void diagOfANonSquareMatrixStopsAtTheShorterSide() {
        DArray<Double> wide = DArrays.seq(Shape.of(2, 5));
        assertArrayEquals(new double[] {0, 6}, wide.diag(0).toDoubleArray());
        assertArrayEquals(new double[] {1, 7}, wide.diag(1).toDoubleArray());
        assertArrayEquals(new double[] {3, 9}, wide.diag(3).toDoubleArray());
        assertArrayEquals(new double[] {5}, wide.diag(-1).toDoubleArray());

        DArray<Double> tall = DArrays.seq(Shape.of(5, 2));
        assertArrayEquals(new double[] {0, 3}, tall.diag(0).toDoubleArray());
        assertArrayEquals(new double[] {1}, tall.diag(1).toDoubleArray());
        assertArrayEquals(new double[] {2, 5}, tall.diag(-1).toDoubleArray());
        assertArrayEquals(new double[] {8}, tall.diag(-4).toDoubleArray());

        // the square case is unchanged
        assertArrayEquals(new double[] {0, 4, 8}, DArrays.seq(Shape.of(3, 3)).diag(0).toDoubleArray());
        assertThrows(IllegalArgumentException.class, () -> wide.diag(5));
        assertThrows(IllegalArgumentException.class, () -> wide.diag(-2));
    }

    @Test
    void matrixMultiplicationValidatesItsTarget() {
        DArray<Double> a = DArrays.seq(Shape.of(3, 4));
        DArray<Double> b = DArrays.seq(Shape.of(4, 5));
        assertThrows(IllegalArgumentException.class, () -> a.mm(b, DArrays.zeros(Shape.of(2, 5))));
        assertThrows(IllegalArgumentException.class, () -> a.mm(b, DArrays.zeros(Shape.of(4, 5))));
        assertThrows(IllegalArgumentException.class, () -> a.mm(b, DArrays.zeros(Shape.of(3, 4))));
        assertThrows(IllegalArgumentException.class, () -> a.mm(b, DArrays.zeros(Shape.of(15))));

        DArray<Double> to = DArrays.zeros(Shape.of(3, 5));
        assertTrue(a.mm(b).deepEquals(a.mm(b, to), 1e-10));
    }

    @Test
    void copyToValidatesTheDestinationShape() {
        assertThrows(IllegalArgumentException.class, () -> DArrays.zeros(Shape.of(2, 3)).copyTo(DArrays.zeros(Shape.of(3, 3))));
        assertThrows(IllegalArgumentException.class, () -> DArrays.zeros(Shape.of(2, 3)).copyTo(DArrays.zeros(Shape.of(2, 2))));
        assertThrows(IllegalArgumentException.class, () -> DArrays.zeros(Shape.of(6)).copyTo(DArrays.zeros(Shape.of(2, 3))));

        DArray<Double> src = DArrays.seq(Shape.of(2, 3));
        DArray<Double> dst = DArrays.zeros(Shape.of(2, 3));
        src.copyTo(dst);
        assertTrue(src.deepEquals(dst));

        // the blocked path, above the cache limit
        DArray<Double> big = dm.random(DType.DOUBLE, Shape.of(400, 400), random, Order.C);
        DArray<Double> bigDst = DArrays.zeros(Shape.of(400, 400));
        big.copyTo(bigDst);
        assertTrue(big.deepEquals(bigDst));
    }

    @Test
    void blockedCopyHandlesUnitDimensions() {
        // the blocked path splits the array into cache-sized blocks and narrows source and destination with keepDim
        // false, so a unit axis changes the rank of both block views. The threshold depends on the L2 cache and the
        // thread count, so the shapes are derived from it rather than hardcoded
        int limit = Math.floorDiv(L2_CACHE_SIZE, DType.DOUBLE.byteCount() * 2 * dm.cpuThreads() * 8);
        int side = (int) Math.ceil(Math.sqrt(2.0 * limit));

        for (Shape shape : new Shape[] {
                Shape.of(1, side, side),          // leading unit axis
                Shape.of(side, 1, side),          // interior unit axis
                Shape.of(side, side, 1),          // trailing unit axis
                Shape.of(1, 1, 2 * limit),        // two unit axes, so the blocks collapse to rank one
                Shape.of(side, side)              // no unit axis, the reference case
        }) {
            assertTrue(shape.size() > limit, "shape " + shape + " does not reach the blocked path");

            DArray<Double> src = DArrays.seq(shape);
            DArray<Double> dst = DArrays.zeros(shape);
            src.copyTo(dst);
            assertTrue(src.deepEquals(dst), "shape " + shape);
        }

        // a destination whose storage order differs from the source, so the blocks are not trivially aligned
        DArray<Double> src = DArrays.seq(Shape.of(1, side, side));
        DArray<Double> transposed = DArrays.zeros(Shape.of(side, side, 1)).t();
        src.copyTo(transposed);
        assertTrue(src.deepEquals(transposed));
    }

    @Test
    void argmaxAndArgminNeverReturnAnInvalidPosition() {
        // the sentinels are legal values for the integral dtypes
        assertEquals(0, dm.stride(DType.BYTE, Shape.of(2), Order.C, new byte[] {Byte.MIN_VALUE, Byte.MIN_VALUE}).argmax());
        assertEquals(0, dm.stride(DType.BYTE, Shape.of(2), Order.C, new byte[] {Byte.MAX_VALUE, Byte.MAX_VALUE}).argmin());
        assertEquals(0, dm.stride(DType.INTEGER, Shape.of(2), Order.C, new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE}).argmax());
        assertEquals(0, dm.stride(DType.INTEGER, Shape.of(2), Order.C, new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE}).argmin());

        // and the floating point equivalents
        double inf = Double.POSITIVE_INFINITY;
        assertEquals(0, DArrays.stride(-inf, -inf).argmax());
        assertEquals(0, DArrays.stride(inf, inf).argmin());
        assertEquals(0, DArrays.stride(Double.NaN, Double.NaN).argmax());

        // a real maximum is still found
        assertEquals(1, DArrays.stride(-inf, 1.0, -inf).argmax());
        assertEquals(2, DArrays.stride(3.0, 2.0, 1.0).argmin());
    }

    @Test
    void matrixVectorProductDoesNotDependOnTheStorageOrder() {
        double[] values = {Double.NaN, 1};
        DArray<Double> c = dm.stride(DType.DOUBLE, Shape.of(2, 1), Order.C, values);
        DArray<Double> f = dm.stride(DType.DOUBLE, Shape.of(2, 1), Order.F, values);
        DArray<Double> x = DArrays.stride(0.0);
        assertTrue(c.mv(x).deepEquals(f.mv(x)), "C: " + c.mv(x) + " F: " + f.mv(x));
        assertTrue(Double.isNaN(f.mv(x).getDouble(0)), "0 * NaN must be NaN, not skipped");

        // the same for an infinity, and on a width that uses the vector lanes
        int m = 64;
        double[] col = new double[m];
        col[0] = Double.POSITIVE_INFINITY;
        DArray<Double> cc = dm.stride(DType.DOUBLE, Shape.of(m, 1), Order.C, col);
        DArray<Double> ff = dm.stride(DType.DOUBLE, Shape.of(m, 1), Order.F, col);
        assertTrue(Double.isNaN(ff.mv(x).getDouble(0)));
        assertTrue(cc.mv(x).deepEquals(ff.mv(x)));

        // ordinary values are unaffected
        DArray<Double> a = dm.random(DType.DOUBLE, Shape.of(5, 4), random, Order.C);
        DArray<Double> v = dm.random(DType.DOUBLE, Shape.of(4), random, Order.C);
        assertTrue(a.mv(v).deepEquals(a.copy(Order.F).mv(v), 1e-12));
    }

    @Test
    void conv1dKeepsEveryWindowThatFitsTheInput() {
        // length 5, kernel 3, stride 2: windows at 0 and 2 both fit
        DArray<Double> in = DArrays.seq(Shape.of(1, 1, 5)).add(1.0);
        DArray<Double> kernel = DArrays.full(Shape.of(1, 1, 3), 1.0);
        DArray<Double> y = in.conv1d(kernel, null, 2, 0, 1, 1);
        assertArrayEquals(new int[] {1, 1, 2}, y.shape().dims());
        assertArrayEquals(new double[] {6, 12}, y.toDoubleArray());

        assertArrayEquals(new int[] {1, 3, 2}, in.unfold1d(3, 2, 0, 1).shape().dims());

        // and conv1d agrees with conv2d on a single row, which already used the correct formula
        DArray<Double> in2 = in.reshape(Shape.of(1, 1, 1, 5), Order.C);
        DArray<Double> kernel2 = kernel.reshape(Shape.of(1, 1, 1, 3), Order.C);
        DArray<Double> y2 = in2.conv2d(kernel2, null, 2, 0, 1, 1);
        assertEquals(y.size(), y2.size());
        assertArrayEquals(y.toDoubleArray(), y2.toDoubleArray());
    }

    @Test
    void convolutionsDoNotDependOnTheKernelStorageOrder() {
        DArray<Double> in1 = dm.random(DType.DOUBLE, Shape.of(1, 2, 7), random, Order.C);
        DArray<Double> k1 = dm.random(DType.DOUBLE, Shape.of(3, 2, 3), random, Order.F);
        assertTrue(in1.conv1d(k1, null, 1, 0, 1, 1).deepEquals(in1.conv1d(k1.copy(Order.C), null, 1, 0, 1, 1), 1e-12));
        assertTrue(in1.convTranspose1d(k1.reshape(Shape.of(2, 3, 3), Order.C).copy(Order.F), null, 1, 0, 1, 1, 0)
                .deepEquals(in1.convTranspose1d(k1.reshape(Shape.of(2, 3, 3), Order.C), null, 1, 0, 1, 1, 0), 1e-12));

        DArray<Double> in3 = dm.random(DType.DOUBLE, Shape.of(1, 2, 4, 4, 4), random, Order.C);
        DArray<Double> k3 = dm.random(DType.DOUBLE, Shape.of(3, 2, 2, 2, 2), random, Order.F);
        assertTrue(in3.conv3d(k3, null, 1, 0, 1, 1).deepEquals(in3.conv3d(k3.copy(Order.C), null, 1, 0, 1, 1), 1e-12));

        DArray<Double> kt3 = dm.random(DType.DOUBLE, Shape.of(2, 3, 2, 2, 2), random, Order.F);
        assertTrue(in3.convTranspose3d(kt3, null, 1, 0, 1, 1, 0)
                .deepEquals(in3.convTranspose3d(kt3.copy(Order.C), null, 1, 0, 1, 1, 0), 1e-12));
    }

    @Test
    void ceilModePoolingNeverProducesAWindowInsideThePaddingOnly() {
        // length 5, kernel 2, stride 2, padding 1: ceil gives 4, but the 4th window starts at 6 > 5 + 1
        DArray<Double> in = DArrays.seq(Shape.of(1, 1, 5)).add(1.0);
        var pooled = in.maxPool1d(2, 2, 1, 1, true);
        assertArrayEquals(new int[] {1, 1, 3}, pooled.v1.shape().dims());
        assertFalse(Double.isInfinite(pooled.v1.amin()), "no output may be left at the initial sentinel");
        assertTrue(pooled.v2.amin() >= 0, "every output must have a valid source index");

        DArray<Double> in2 = DArrays.seq(Shape.of(1, 1, 5, 5)).add(1.0);
        var pooled2 = in2.maxPool2d(2, 2, 2, 1, 1, true);
        assertArrayEquals(new int[] {1, 1, 3, 3}, pooled2.v1.shape().dims());
        assertTrue(pooled2.v2.amin() >= 0);

        // a case where ceil mode legitimately adds a window is unchanged
        var pooled3 = DArrays.seq(Shape.of(1, 1, 5)).add(1.0).maxPool1d(2, 2, 0, 1, true);
        assertArrayEquals(new int[] {1, 1, 3}, pooled3.v1.shape().dims());
        assertTrue(pooled3.v2.amin() >= 0);
    }

    @Test
    void convolutionsValidateTheGroupsDivisibility() {
        // 3 output channels over 2 groups: the third kernel slice would never be used
        DArray<Double> in2 = DArrays.seq(Shape.of(1, 2, 4, 4));
        DArray<Double> k2 = DArrays.seq(Shape.of(3, 1, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> in2.conv2d(k2, null, 1, 0, 1, 2));

        DArray<Double> in3 = DArrays.seq(Shape.of(1, 2, 4, 4, 4));
        DArray<Double> k3 = DArrays.seq(Shape.of(3, 1, 2, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> in3.conv3d(k3, null, 1, 0, 1, 2));

        // 3 input channels over 2 groups on the transposed convolutions
        DArray<Double> t2 = DArrays.seq(Shape.of(1, 3, 4, 4));
        DArray<Double> w2 = DArrays.seq(Shape.of(3, 2, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> t2.convTranspose2d(w2, null, 1, 0, 1, 2, 0));

        DArray<Double> t3 = DArrays.seq(Shape.of(1, 3, 4, 4, 4));
        DArray<Double> w3 = DArrays.seq(Shape.of(3, 2, 2, 2, 2));
        assertThrows(IllegalArgumentException.class, () -> t3.convTranspose3d(w3, null, 1, 0, 1, 2, 0));

        // the divisible cases still run
        DArray<Double> k2ok = DArrays.seq(Shape.of(2, 1, 2, 2));
        assertArrayEquals(new int[] {1, 2, 3, 3}, in2.conv2d(k2ok, null, 1, 0, 1, 2).shape().dims());
    }
}
