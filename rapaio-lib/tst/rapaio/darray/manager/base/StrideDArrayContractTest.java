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

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.layout.StrideLayout;

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

    /**
     * An expanded view maps every repeat of an axis onto one storage position, so writing through it makes the writes
     * to those repeats collide. The scalar path then applies the operation once per repeat while the vectorised path
     * writes a whole vector to the single position and keeps the last lane, which made the answer depend on the length
     * of the array relative to the vector width of the data type. Every operation which writes into the array now
     * rejects such a layout instead.
     */
    /**
     * An expanded view maps every repeat of an axis onto one storage position, so writing through it makes the writes
     * to those repeats collide. The surviving value depended on the order the loop visited them in, which differs
     * between the vectorised and the scalar path: the answer therefore changed as soon as the array was long enough to
     * fill one vector, so it followed the vector length of the data type. Every operation which writes into the array
     * now rejects such a layout instead, which is also what numpy does. These lengths straddle the old boundary.
     */
    @Test
    void inPlaceOperationsRejectAnExpandedDestination() {
        checkExpandedDestinationIsRejected(DType.DOUBLE);
        checkExpandedDestinationIsRejected(DType.FLOAT);
        checkExpandedDestinationIsRejected(DType.INTEGER);
        checkExpandedDestinationIsRejected(DType.BYTE);
    }

    private <N extends Number> void checkExpandedDestinationIsRejected(DType<N> dt) {
        int lanes = dt.vs().length();
        for (int inner : new int[] {2, lanes, 4 * lanes + 3}) {
            String name = dt.id() + " inner=" + inner;
            DArray<N> expanded = dm.zeros(dt, Shape.of(3, 1)).expand(1, inner);
            DArray<N> operand = dm.full(dt, Shape.of(3, inner), 1);

            assertTrue(expanded.layout().hasAliasedElements(), name);

            assertThrows(IllegalArgumentException.class, () -> expanded.add_(operand), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.sub_(operand), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.mul_(operand), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.div_(operand), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.add_(1), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.fill_(1), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.apply_(v -> v), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.sort_(1, true), name);
            assertThrows(IllegalArgumentException.class, () -> expanded.fma_(dt.cast(2), operand), name);
            // the destination of a copy is checked too, which is the one case where the rejected layout is not
            // the receiver of the call
            assertThrows(IllegalArgumentException.class, () -> operand.copyTo(expanded), name);
            // and so are the two other destinations a caller can supply
            assertThrows(IllegalArgumentException.class,
                    () -> dm.full(dt, Shape.of(3, 3), 1).mm(dm.full(dt, Shape.of(3, inner), 1), expanded), name);
            assertThrows(IllegalArgumentException.class,
                    () -> expanded.gather_(1, dm.zeros(DType.INTEGER, Shape.of(3, inner)), operand), name);

            // nothing was written before the rejection
            assertEquals(0.0, expanded.getDouble(0, 0), name);
            assertEquals(0.0, expanded.getDouble(2, inner - 1), name);
        }

        // the message names the operation and tells the caller what to do about it
        DArray<N> expanded = dm.zeros(dt, Shape.of(3, 1)).expand(1, 8);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> expanded.add_(1));
        assertTrue(e.getMessage().startsWith("Operation binary_ cannot write into an expanded darray"), e.getMessage());
        assertTrue(e.getMessage().endsWith("Copy it first."), e.getMessage());

        // and copying first is a working remedy
        DArray<N> copy = expanded.copy();
        assertFalse(copy.layout().hasAliasedElements());
        copy.add_(1);
        assertEquals(1.0, copy.getDouble(0, 0));
        assertEquals(1.0, copy.getDouble(2, 7));
    }

    /**
     * The rejection covers an expanded axis wherever it sits, not only the innermost one. An outer expanded axis used
     * to go unnoticed, because the repeats became separate offsets visited one after another and the operation applied
     * once per repeat; now it is rejected like any other aliased layout.
     */
    @Test
    void anExpandedOuterAxisIsRejectedToo() {
        DArray<Double> t = DArrays.seq(Shape.of(4, 1, 2));
        DArray<Double> expanded = t.expand(1, 2);
        assertArrayEquals(new int[] {4, 2, 2}, expanded.shape().dims());
        assertThrows(IllegalArgumentException.class, () -> expanded.add_(1.0));
        // the source is untouched
        assertTrue(DArrays.seq(Shape.of(4, 1, 2)).deepEquals(t));

        DArray<Double> wide = DArrays.zeros(Shape.of(2, 1, 64));
        assertThrows(IllegalArgumentException.class, () -> wide.expand(1, 3).add_(1.0));
        assertEquals(0.0, wide.sum());
    }

    /**
     * The rejection must stay narrow: an axis of a single element with a zero stride addresses one position and repeats
     * nothing, so it is not aliasing. {@code stretch} produces those, and the batched matrix products and the neural
     * network gradients write through them.
     */
    @Test
    void stretchedLayoutsAreNotTreatedAsAliased() {
        DArray<Double> stretched = DArrays.seq(Shape.of(4)).stretch(0, 2);
        assertArrayEquals(new int[] {1, 4, 1}, stretched.shape().dims());
        assertFalse(stretched.layout().hasAliasedElements());
        stretched.add_(1.0);
        assertEquals(1.0, stretched.getDouble(0, 0, 0));
        assertEquals(4.0, stretched.getDouble(0, 3, 0));

        // reading through a genuinely expanded view is well defined: every repeat yields the same value
        DArray<Double> expanded = DArrays.seq(Shape.of(3, 1)).expand(1, 5);
        assertEquals(15.0, expanded.sum());
        assertEquals(2.0, expanded.getDouble(2, 4));
        // and an out-of-place operation on it allocates its own destination, so it is allowed
        assertEquals(30.0, expanded.add(expanded).sum());

        // broadcasting the OPERAND of an in-place operation is unaffected: that side is only read
        DArray<Double> target = DArrays.zeros(Shape.of(3, 5));
        target.add_(DArrays.seq(Shape.of(3, 1)));
        assertEquals(15.0, target.sum());
        assertEquals(2.0, target.getDouble(2, 0));
        assertEquals(2.0, target.getDouble(2, 4));
    }

    @Test
    void copyToRejectsASourceAndDestinationWhichOverlap() {
        for (DType<?> dt : List.of(DType.DOUBLE, DType.FLOAT, DType.INTEGER, DType.BYTE)) {
            String name = dt.id().toString();
            // the forward overlap is the one which corrupts: the copy reads positions it has already written.
            // Both directions are rejected, since the loop gives no guarantee about either
            DArray<?> base = dm.seq(dt, Shape.of(6));
            assertTrue(assertThrows(IllegalArgumentException.class,
                    () -> copyBetween(base.narrow(0, true, 0, 4), base.narrow(0, true, 2, 6)), name)
                    .getMessage().contains("share storage over overlapping ranges"), name);
            assertThrows(IllegalArgumentException.class,
                    () -> copyBetween(base.narrow(0, true, 2, 6), base.narrow(0, true, 0, 4)), name);
            // a copy onto itself addresses exactly the same range
            assertThrows(IllegalArgumentException.class, () -> copyBetween(base, base), name);

            // nothing was written before the rejection
            for (int i = 0; i < 6; i++) {
                assertEquals(i, base.getDouble(i), TOL, name);
            }

            // disjoint ranges of one storage copy normally, which is the common case of moving data within an array
            DArray<?> halves = dm.seq(dt, Shape.of(6));
            copyBetween(halves.narrow(0, true, 0, 3), halves.narrow(0, true, 3, 6));
            assertEquals(0, halves.getDouble(3), TOL, name);
            assertEquals(1, halves.getDouble(4), TOL, name);
            assertEquals(2, halves.getDouble(5), TOL, name);

            // and so does a copy between distinct storages which happen to hold the same values
            DArray<?> src = dm.seq(dt, Shape.of(2, 3));
            DArray<?> dst = dm.zeros(dt, Shape.of(2, 3));
            copyBetween(src, dst);
            assertTrue(src.deepEquals(dst), name);

            // the remedy named by the message works
            DArray<?> remedy = dm.seq(dt, Shape.of(6));
            copyBetween(remedy.narrow(0, true, 0, 4).copy(), remedy.narrow(0, true, 2, 6));
            assertEquals(0, remedy.getDouble(2), TOL, name);
            assertEquals(1, remedy.getDouble(3), TOL, name);
            assertEquals(2, remedy.getDouble(4), TOL, name);
            assertEquals(3, remedy.getDouble(5), TOL, name);
        }
    }

    /** Copies between two arrays of the same, statically unknown, dtype. */
    private static <N extends Number> void copyBetween(DArray<N> src, DArray<?> dst) {
        @SuppressWarnings("unchecked") DArray<N> typed = (DArray<N>) dst;
        src.copyTo(typed);
    }

    @Test
    void selKeepsTheRealStrideOfASingleIndexAxis() {
        // substituting a stride of 1 on the selected axis used to make a C-ordered selection report itself as
        // F-ordered, which costs reshape and copyTo their view and fast-order paths. The addressing was never
        // affected, since the axis holds one element, so the values agreed with narrow all along
        DArray<Double> x = DArrays.seq(Shape.of(3, 4));
        DArray<Double> selected = x.sel(Order.C, 0, 1);
        DArray<Double> narrowed = x.narrow(0, true, 1, 2);
        StrideLayout selLayout = (StrideLayout) selected.layout();
        StrideLayout narLayout = (StrideLayout) narrowed.layout();

        assertArrayEquals(narLayout.dims(), selLayout.dims());
        assertArrayEquals(narLayout.strides(), selLayout.strides());
        assertEquals(narLayout.offset(), selLayout.offset());
        assertEquals(narrowed.layout().isCOrdered(), selected.layout().isCOrdered());
        assertEquals(narrowed.layout().isFOrdered(), selected.layout().isFOrdered());
        assertEquals(narrowed.layout().storageFastOrder(), selected.layout().storageFastOrder());
        assertTrue(selected.layout().isCOrdered());
        assertTrue(narrowed.deepEquals(selected));

        // the values are unchanged, on both axes and on a transposed parent
        assertEquals(4.0, selected.getDouble(0, 0), TOL);
        assertEquals(7.0, selected.getDouble(0, 3), TOL);
        assertTrue(x.narrow(1, true, 2, 3).deepEquals(x.sel(Order.C, 1, 2)));
        DArray<Double> t = x.t();
        assertTrue(t.narrow(0, true, 2, 3).deepEquals(t.sel(Order.C, 0, 2)));

        // a selection of one element is still safe to write through, and writes reach the parent
        x.sel(Order.C, 0, 1).fill_(9.0);
        assertEquals(9.0, x.getDouble(1, 0), TOL);
        assertEquals(0.0, x.getDouble(0, 0), TOL);
    }

    @Test
    void viewOperationsValidateTheirArguments() {
        DArray<Double> x = DArrays.seq(Shape.of(4, 3));

        // chunk: the step is the size of a piece, so it must be at least one element
        assertTrue(assertThrows(IllegalArgumentException.class, () -> x.chunk(0, true, 0))
                .getMessage().contains("strictly positive"));
        assertThrows(IllegalArgumentException.class, () -> x.chunk(0, true, -1));
        assertThrows(IllegalArgumentException.class, () -> x.chunk(2, true, 1));
        assertThrows(IllegalArgumentException.class, () -> x.chunk(-1, true, 1));
        assertEquals(2, x.chunk(0, true, 2).size());

        assertThrows(IllegalArgumentException.class, () -> x.chunkAll(true, new int[] {0, 1}));
        assertThrows(IllegalArgumentException.class, () -> x.chunkAll(true, new int[] {2, -1}));
        assertEquals(4, x.chunkAll(true, new int[] {2, 2}).size());

        // split: an empty request names no piece at all, and used to answer with an empty list
        assertTrue(assertThrows(IllegalArgumentException.class, () -> x.split(0, true))
                .getMessage().contains("cannot be empty"));
        assertThrows(IllegalArgumentException.class, () -> x.split(0, true, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> x.split(0, true, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> x.split(0, true, 0, 4));
        assertThrows(IllegalArgumentException.class, () -> x.split(0, true, -1, 2));
        assertEquals(2, x.split(0, true, 0, 2).size());

        assertThrows(IllegalArgumentException.class, () -> x.splitAll(true, new int[][] {{}, {0}}));
        assertThrows(IllegalArgumentException.class, () -> x.splitAll(true, new int[][] {{0}, {3}}));
        assertEquals(2, x.splitAll(true, new int[][] {{0, 2}, {0}}).size());

        // every chunk of a split still tiles the source exactly once
        double total = 0;
        for (DArray<Double> chunk : x.chunk(0, true, 3)) {
            total += chunk.sum();
        }
        assertEquals(x.sum(), total, TOL);
    }

    @Test
    void axisConventionsAreValidatedInBothFamilies() {
        DArray<Double> x = DArrays.seq(Shape.of(2, 3));

        // acting along an axis accepts a negative axis counted from the end
        assertTrue(x.sum1d(1).deepEquals(x.sum1d(-1)));
        assertTrue(x.argmax1d(0, false).deepEquals(x.argmax1d(-2, false)));
        assertTrue(x.copy().sort_(1, true).deepEquals(x.copy().sort_(-1, true)));
        // squeeze resolves a negative axis itself, so it belongs to the same family
        DArray<Double> unit = DArrays.seq(Shape.of(2, 1));
        assertArrayEquals(new int[] {2}, unit.squeeze(-1).shape().dims());
        // and an out-of-range axis is rejected rather than quietly ignored, which the rank 2 layout used to do
        assertThrows(IllegalArgumentException.class, () -> unit.squeeze(-5));
        assertThrows(IllegalArgumentException.class, () -> unit.squeeze(2));

        // but not one out of range, which used to fall through to a raw arraycopy failure
        assertTrue(assertThrows(IllegalArgumentException.class, () -> x.sum1d(-3))
                .getMessage().contains("out of bounds"));
        assertThrows(IllegalArgumentException.class, () -> x.sum1d(2));
        assertThrows(IllegalArgumentException.class, () -> x.argmax1d(-3, false));
        assertThrows(IllegalArgumentException.class, () -> x.copy().sort_(-3, true));
        assertThrows(IllegalArgumentException.class, () -> x.copy().sort_(2, true));
        // a scalar has no axis to sort along
        assertThrows(IllegalArgumentException.class, () -> DArrays.scalar(1.0).sort_(0, true));

        // creating a view of a named axis requires a non-negative axis, which DArrayManagerTest pins for sel
        assertThrows(IllegalArgumentException.class, () -> x.narrow(-1, true, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> x.sel(Order.C, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> x.chunk(-1, true, 1));
        assertThrows(IllegalArgumentException.class, () -> x.split(-1, true, 0));
    }

    @Test
    void unpadCountsOnlyThePositionsItAddresses() {
        // the positions are pad + i * inflation bounded by dim - 1 - pad, a floorDiv count. With ceilDiv the view
        // claimed one element too many whenever the inflation did not divide, and that element addressed storage
        // past the end of its own axis: on a row of a matrix it silently returned the next row's first element
        DArray<Double> x = DArrays.seq(Shape.of(2, 6));
        DArray<Double> u = x.unpad(1, 0, 2);
        assertArrayEquals(new int[] {2, 3}, u.shape().dims());
        assertEquals(0.0, u.getDouble(0, 0), TOL);
        assertEquals(2.0, u.getDouble(0, 1), TOL);
        assertEquals(4.0, u.getDouble(0, 2), TOL);
        assertEquals(6.0, u.getDouble(1, 0), TOL);
        assertEquals(10.0, u.getDouble(1, 2), TOL);
        // the whole view is now readable, which it was not when it claimed a fourth column
        assertEquals(30.0, u.sum(), TOL);

        // every non-dividing combination keeps the view inside its own axis
        for (int dim = 1; dim <= 9; dim++) {
            for (int inflation = 1; inflation <= 4; inflation++) {
                for (int p = 0; 2 * p < dim; p++) {
                    DArray<Double> src = DArrays.seq(Shape.of(dim));
                    DArray<Double> v = src.unpad(0, p, inflation);
                    String name = "dim=" + dim + " pad=" + p + " inflation=" + inflation;
                    assertEquals(Math.floorDiv(dim - 2 * p - 1, inflation) + 1, v.dim(0), name);
                    for (int i = 0; i < v.dim(0); i++) {
                        int position = p + i * inflation;
                        assertTrue(position < dim, name + " position " + position);
                        assertEquals(position, v.getDouble(i), TOL, name);
                    }
                }
            }
        }

        // pad always produces a length where ceil and floor agree, which is why the round trip never caught it
        for (int inflation = 1; inflation <= 3; inflation++) {
            for (int p = 0; p <= 2; p++) {
                DArray<Double> src = DArrays.seq(Shape.of(4));
                assertTrue(src.pad(0, p, inflation).unpad(0, p, inflation).deepEquals(src),
                        "round trip pad=" + p + " inflation=" + inflation);
            }
        }
    }

    @Test
    void padAndUnpadValidateTheirArguments() {
        DArray<Double> x = DArrays.seq(Shape.of(4));

        // a negative padding used to act as a crop, and on a view its negative offset stayed inside the parent
        assertTrue(assertThrows(IllegalArgumentException.class, () -> x.pad(0, -1, 1))
                .getMessage().contains("must not be negative"));
        assertThrows(IllegalArgumentException.class, () -> x.unpad(0, -1, 1));
        DArray<Double> parent = DArrays.seq(Shape.of(10));
        assertThrows(IllegalArgumentException.class, () -> parent.narrow(0, true, 4, 8).unpad(0, -2, 1));

        // an inflation below one divides by zero or collapses the axis
        assertTrue(assertThrows(IllegalArgumentException.class, () -> x.pad(0, 1, 0))
                .getMessage().contains("strictly positive"));
        assertThrows(IllegalArgumentException.class, () -> x.pad(0, 1, -1));
        assertThrows(IllegalArgumentException.class, () -> x.unpad(0, 0, 0));

        // the axis
        assertThrows(IllegalArgumentException.class, () -> x.pad(5, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> x.pad(-1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> x.unpad(1, 0, 1));

        // a padding too large to remove leaves nothing behind
        assertTrue(assertThrows(IllegalArgumentException.class, () -> x.unpad(0, 3, 1))
                .getMessage().contains("too large to remove"));
        assertThrows(IllegalArgumentException.class, () -> x.unpad(0, 10, 1));

        // the multi-axis overloads validate per axis, and the trailing-axes behaviour is unchanged
        DArray<Double> m = DArrays.seq(Shape.of(2, 3, 4));
        assertArrayEquals(new int[] {2, 3, 6}, m.pad(new int[] {1}, new int[] {1}).shape().dims());
        assertThrows(IllegalArgumentException.class, () -> m.pad(new int[] {1, -1}, new int[] {1, 1}));
        assertThrows(IllegalArgumentException.class, () -> m.pad(new int[] {1, 1}, new int[] {1, 0}));
        assertThrows(IllegalArgumentException.class, () -> m.unpad(new int[] {0, -1}, new int[] {1, 1}));

        // and the values a valid pad produces are unchanged: zero border, source in the interior
        DArray<Double> padded = DArrays.seq(Shape.of(2, 2)).pad(new int[] {1, 1}, new int[] {1, 1});
        assertArrayEquals(new int[] {4, 4}, padded.shape().dims());
        assertEquals(0.0, padded.getDouble(0, 0), TOL);
        assertEquals(0.0, padded.getDouble(3, 3), TOL);
        assertEquals(3.0, padded.getDouble(2, 2), TOL);
        assertEquals(6.0, padded.sum(), TOL);
    }

    @Test
    void stackValidatesItsAxisAndItsInput() {
        List<DArray<Double>> two = List.of(DArrays.seq(Shape.of(2, 3)), DArrays.seq(Shape.of(2, 3)));

        // the new axis may be appended after the last one, so rank itself is a valid axis
        assertArrayEquals(new int[] {2, 2, 3}, dm.stack(DType.DOUBLE, 0, two).shape().dims());
        assertArrayEquals(new int[] {2, 3, 2}, dm.stack(DType.DOUBLE, 2, two).shape().dims());

        // but one past it is not, and used to surface as an ArrayIndexOutOfBoundsException
        assertTrue(assertThrows(IllegalArgumentException.class, () -> dm.stack(DType.DOUBLE, 3, two))
                .getMessage().contains("out of bounds for stacking"));
        assertThrows(IllegalArgumentException.class, () -> dm.stack(DType.DOUBLE, -1, two));
        // an empty input used to surface as a NoSuchElementException with no message at all
        assertTrue(assertThrows(IllegalArgumentException.class, () -> dm.stack(DType.DOUBLE, 0, List.of()))
                .getMessage().contains("At least one darray"));

        // cat already validated all three, and still does
        assertThrows(IllegalArgumentException.class, () -> dm.cat(DType.DOUBLE, 2, two));
        assertThrows(IllegalArgumentException.class, () -> dm.cat(DType.DOUBLE, -1, two));
        assertThrows(IllegalArgumentException.class, () -> dm.cat(DType.DOUBLE, 0, List.of()));

        // values are unaffected
        DArray<Double> a = DArrays.seq(Shape.of(2, 3));
        DArray<Double> stacked = dm.stack(DType.DOUBLE, 0, List.of(a, a.add(100.0)));
        assertEquals(0.0, stacked.getDouble(0, 0, 0), TOL);
        assertEquals(105.0, stacked.getDouble(1, 1, 2), TOL);
    }

    @Test
    void unfoldValidatesItsWindowArguments() {
        DArray<Double> in1 = DArrays.seq(Shape.of(1, 1, 5));
        DArray<Double> in2 = DArrays.seq(Shape.of(1, 1, 4, 4));
        DArray<Double> in3 = DArrays.seq(Shape.of(1, 1, 3, 3, 3));

        // a non-positive stride makes the window count a quotient of two negatives, so it stays positive and can
        // exceed the number of positions: unfold1d(kLen=4, stride=-1) on a length 2 input reported four windows
        assertTrue(assertThrows(IllegalArgumentException.class, () -> in1.unfold1d(3, -1, 0, 1))
                .getMessage().contains("Stride must be strictly positive"));
        assertThrows(IllegalArgumentException.class, () -> in1.unfold1d(3, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> in2.unfold2d(2, 2, -1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> in3.unfold3d(2, 2, 2, 0, 0, 1));

        // a dilation of zero made every row of the window read the same element
        assertTrue(assertThrows(IllegalArgumentException.class, () -> in1.unfold1d(3, 1, 0, 0))
                .getMessage().contains("Dilation must be strictly positive"));
        assertThrows(IllegalArgumentException.class, () -> in1.unfold1d(3, 1, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> in2.unfold2d(2, 2, 1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> in3.unfold3d(2, 2, 2, 1, 0, -1));

        // a negative padding silently cropped the input
        assertTrue(assertThrows(IllegalArgumentException.class, () -> in1.unfold1d(3, 1, -1, 1))
                .getMessage().contains("Padding must not be negative"));
        assertThrows(IllegalArgumentException.class, () -> in2.unfold2d(2, 2, 1, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> in3.unfold3d(2, 2, 2, 1, -1, 1));

        // a window of no elements was rejected by Shape, with a message about dimensions
        assertTrue(assertThrows(IllegalArgumentException.class, () -> in1.unfold1d(0, 1, 0, 1))
                .getMessage().contains("Kernel size must be strictly positive"));
        assertThrows(IllegalArgumentException.class, () -> in2.unfold2d(2, 0, 1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> in3.unfold3d(2, 2, -1, 1, 0, 1));

        // the convolutions are built on unfold, so they inherit the same validation
        DArray<Double> kernel = DArrays.seq(Shape.of(1, 1, 2));
        assertThrows(IllegalArgumentException.class, () -> in1.conv1d(kernel, null, 0, 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> in1.conv1d(kernel, null, 1, -1, 1, 1));

        // valid arguments are unaffected, including a non-dividing stride and a dilation
        assertArrayEquals(new int[] {1, 3, 3}, in1.unfold1d(3, 1, 0, 1).shape().dims());
        assertArrayEquals(new int[] {1, 2, 2}, in1.unfold1d(2, 2, 0, 1).shape().dims());
        assertArrayEquals(new int[] {1, 3, 1}, in1.unfold1d(3, 1, 0, 2).shape().dims());
        assertArrayEquals(new int[] {1, 4, 9}, in2.unfold2d(2, 2, 1, 0, 1).shape().dims());
        assertArrayEquals(new int[] {1, 8, 8}, in3.unfold3d(2, 2, 2, 1, 0, 1).shape().dims());
    }
}
