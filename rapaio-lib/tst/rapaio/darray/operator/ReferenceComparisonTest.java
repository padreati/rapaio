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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DArrays;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.factories.ByteDenseCol;
import rapaio.darray.factories.ByteDenseRow;
import rapaio.darray.factories.ByteDenseStride;
import rapaio.darray.factories.ByteDenseStrideView;
import rapaio.darray.factories.DataFactory;
import rapaio.darray.factories.DoubleDenseCol;
import rapaio.darray.factories.DoubleDenseRow;
import rapaio.darray.factories.DoubleDenseStride;
import rapaio.darray.factories.DoubleDenseStrideView;
import rapaio.darray.factories.FloatDenseCol;
import rapaio.darray.factories.FloatDenseRow;
import rapaio.darray.factories.FloatDenseStride;
import rapaio.darray.factories.FloatDenseStrideView;
import rapaio.darray.factories.IntegerDenseCol;
import rapaio.darray.factories.IntegerDenseRow;
import rapaio.darray.factories.IntegerDenseStride;
import rapaio.darray.factories.IntegerDenseStrideView;
import rapaio.data.VarDouble;

/**
 * Compares the SIMD / strided kernels of unary and reduce operators against a naive scalar
 * reference computed with {@code get}/{@code set} in logical C order.
 * <p>
 * Shapes deliberately use dimension sizes that are not multiples of any SIMD lane count (2, 4, 8, 16, 32, 64)
 * so that both the vector body and the scalar tail loop of every kernel are exercised. Each operator is
 * evaluated on the factory layout itself, on a transposed view and on a narrowed sub-view, so unit-stride,
 * strided and non-dense code paths are all covered for all four data types.
 */
public class ReferenceComparisonTest {

    private static final Shape[] SHAPES = new Shape[] {Shape.of(13), Shape.of(7, 13), Shape.of(3, 5, 11)};

    static Stream<DataFactory<? extends Number>> dataFactorySource() {
        DArrayManager manager = DArrayManager.base();
        List<DataFactory<? extends Number>> factories = new ArrayList<>();
        factories.add(new DoubleDenseRow(manager));
        factories.add(new DoubleDenseCol(manager));
        factories.add(new DoubleDenseStride(manager));
        factories.add(new DoubleDenseStrideView(manager));
        factories.add(new FloatDenseRow(manager));
        factories.add(new FloatDenseCol(manager));
        factories.add(new FloatDenseStride(manager));
        factories.add(new FloatDenseStrideView(manager));
        factories.add(new IntegerDenseRow(manager));
        factories.add(new IntegerDenseCol(manager));
        factories.add(new IntegerDenseStride(manager));
        factories.add(new IntegerDenseStrideView(manager));
        factories.add(new ByteDenseRow(manager));
        factories.add(new ByteDenseCol(manager));
        factories.add(new ByteDenseStride(manager));
        factories.add(new ByteDenseStrideView(manager));
        return factories.stream();
    }

    /**
     * A unary operator together with its scalar reference and the input domain it is defined on.
     */
    private record UnaryCase(String name, DArrayUnaryOp op, DoubleUnaryOperator ref, double lo, double hi) {
    }

    private static List<UnaryCase> unaryCases() {
        List<UnaryCase> cases = new ArrayList<>();
        cases.add(new UnaryCase("abs", DArrayOp.unaryAbs(), Math::abs, -20, 20));
        cases.add(new UnaryCase("neg", DArrayOp.unaryNeg(), x -> -x, -20, 20));
        cases.add(new UnaryCase("sqr", DArrayOp.unarySqr(), x -> x * x, -10, 10));
        cases.add(new UnaryCase("rint", DArrayOp.unaryRint(), Math::rint, -20, 20));
        cases.add(new UnaryCase("ceil", DArrayOp.unaryCeil(), Math::ceil, -20, 20));
        cases.add(new UnaryCase("floor", DArrayOp.unaryFloor(), Math::floor, -20, 20));
        cases.add(new UnaryCase("sqrt", DArrayOp.unarySqrt(), Math::sqrt, 0.5, 3));
        cases.add(new UnaryCase("log", DArrayOp.unaryLog(), Math::log, 0.5, 3));
        cases.add(new UnaryCase("exp", DArrayOp.unaryExp(), Math::exp, -2, 2));
        cases.add(new UnaryCase("sin", DArrayOp.unarySin(), Math::sin, -2, 2));
        cases.add(new UnaryCase("cos", DArrayOp.unaryCos(), Math::cos, -2, 2));
        cases.add(new UnaryCase("tan", DArrayOp.unaryTan(), Math::tan, -1, 1));
        cases.add(new UnaryCase("asin", DArrayOp.unaryAsin(), Math::asin, -0.9, 0.9));
        cases.add(new UnaryCase("acos", DArrayOp.unaryAcos(), Math::acos, -0.9, 0.9));
        cases.add(new UnaryCase("atan", DArrayOp.unaryAtan(), Math::atan, -2, 2));
        cases.add(new UnaryCase("sinh", DArrayOp.unarySinh(), Math::sinh, -2, 2));
        cases.add(new UnaryCase("cosh", DArrayOp.unaryCosh(), Math::cosh, -2, 2));
        cases.add(new UnaryCase("tanh", DArrayOp.unaryTanh(), Math::tanh, -2, 2));
        cases.add(new UnaryCase("pow2.5", DArrayOp.unaryPow(2.5), x -> Math.pow(x, 2.5), 0.5, 3));
        cases.add(new UnaryCase("sigmoid", DArrayOp.unarySigmoid(), x -> 1.0 / (1.0 + Math.exp(-x)), -3, 3));
        return cases;
    }

    @ParameterizedTest
    @MethodSource("dataFactorySource")
    <N extends Number> void unaryOpsMatchNaiveReference(DataFactory<N> g) {
        Random random = new Random(42);
        for (Shape shape : SHAPES) {
            for (UnaryCase c : unaryCases()) {
                if (c.op().floatingPointOnly() && g.dt().isInteger()) {
                    continue;
                }
                for (int viewKind = 0; viewKind < 3; viewKind++) {
                    DArray<N> x = view(fill(g, shape, c.lo(), c.hi(), random), viewKind);
                    if (x == null) {
                        continue;
                    }
                    // reference computed on a dense copy, element by element, in double precision
                    DArray<N> expected = x.copy(Order.C);
                    expected.apply_(Order.C, (_, p) -> g.value(c.ref().applyAsDouble(expected.ptrGetDouble(p))));
                    // kernel under test, applied in place on the (possibly strided) view
                    x.unary_(c.op());
                    assertClose(expected, x, tolerance(g.dt()), c.name() + " on " + shape + " view=" + viewKind);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("dataFactorySource")
    <N extends Number> void reduceOpsMatchNaiveReference(DataFactory<N> g) {
        Random random = new Random(42);
        for (Shape shape : SHAPES) {
            for (int viewKind = 0; viewKind < 3; viewKind++) {
                DArray<N> x = view(fill(g, shape, -20, 20, random), viewKind);
                if (x == null) {
                    continue;
                }
                double[] v = values(x);
                String ctx = shape + " view=" + viewKind;

                // integer reductions accumulate in the narrow element type and wrap on overflow by design
                // (documented in DArray.sum()/prod() and package-info), so the reference is wrapped the same way
                double expectedSum = g.dt().isInteger() ? g.value(naiveSum(v)).doubleValue() : naiveSum(v);
                assertScalarClose(expectedSum, x.reduce(DArrayOp.reduceSum()).doubleValue(), g.dt(), "sum " + ctx);
                assertScalarClose(naiveMin(v), x.reduce(DArrayOp.reduceMin()).doubleValue(), g.dt(), "min " + ctx);
                assertScalarClose(naiveMax(v), x.reduce(DArrayOp.reduceMax()).doubleValue(), g.dt(), "max " + ctx);
                if (!g.dt().isInteger()) {
                    double mean = naiveSum(v) / v.length;
                    assertScalarClose(mean, x.reduce(DArrayOp.reduceMean()).doubleValue(), g.dt(), "mean " + ctx);
                    assertScalarClose(naiveVar(v, 0), x.reduce(DArrayOp.reduceVarc(0)).doubleValue(), g.dt(), "var0 " + ctx);
                    assertScalarClose(naiveVar(v, 1), x.reduce(DArrayOp.reduceVarc(1)).doubleValue(), g.dt(), "var1 " + ctx);
                }

                // product: keep magnitudes at 1 so integer types cannot overflow and floats stay exact
                DArray<N> signs = view(fill(g, shape, -1.5, 1.5, random), viewKind);
                signs.apply_(Order.C, (_, p) -> g.value(signs.ptrGetDouble(p) < 0 ? -1 : 1));
                double[] sv = values(signs);
                assertScalarClose(naiveProd(sv), signs.reduce(DArrayOp.reduceProd()).doubleValue(), g.dt(), "prod " + ctx);
            }
        }
    }

    /**
     * Regression test for the double variance tail loop reading values through {@code getFloat}.
     * Values around 1e6 cannot be represented exactly in float, so a float read in the scalar tail
     * shifts the variance by roughly 1e-2, while the correct double path agrees with the reference to 1e-9.
     * Length 13 guarantees a non-empty scalar tail for every SIMD lane count.
     */
    @Test
    void doubleVarianceTailKeepsDoublePrecision() {
        Random random = new Random(7);
        DArray<Double> x = DArrays.zeros(Shape.of(13));
        x.apply_(Order.C, (_, _) -> 1_000_000 + random.nextDouble());
        double[] v = values(x);
        assertEquals(naiveVar(v, 0), x.var(0), 1e-9);
        assertEquals(naiveVar(v, 1), x.var(1), 1e-9);
    }

    /**
     * Regression test for {@code inner} casting the other operand to the same concrete class without
     * checking its data type first.
     */
    @Test
    void innerAcceptsOperandOfDifferentType() {
        DArray<Double> a = DArrays.seq(Shape.of(13));
        DArray<Float> b = DArrayManager.base().seq(DType.FLOAT, Shape.of(13));
        DArray<Integer> c = DArrayManager.base().seq(DType.INTEGER, Shape.of(13));
        double expected = 0;
        for (int i = 0; i < 13; i++) {
            expected += (double) i * i;
        }
        assertEquals(expected, a.inner(b), 1e-12);
        assertEquals(expected, a.inner(c), 1e-12);
        assertEquals((float) expected, b.inner(a), 1e-3);
    }

    /**
     * Pins the documented contract that integer reductions accumulate in the element type and wrap on overflow.
     * If sum/prod are ever promoted to a wider accumulator this test must be updated together with the docs.
     */
    @Test
    void integerReductionsWrapInElementType() {
        DArrayManager dm = DArrayManager.base();
        assertEquals((byte) 100, dm.full(DType.BYTE, Shape.of(100), (byte) 1).sum());
        assertEquals((byte) 200, dm.full(DType.BYTE, Shape.of(200), (byte) 1).sum());
        assertEquals(Integer.MAX_VALUE + 1, dm.full(DType.INTEGER, Shape.of(2), Integer.MAX_VALUE / 2 + 1).sum());
        assertEquals(1 << 30, dm.full(DType.INTEGER, Shape.of(30), 2).prod());
        assertEquals(Integer.MIN_VALUE, dm.full(DType.INTEGER, Shape.of(31), 2).prod());
        assertEquals(0, dm.full(DType.INTEGER, Shape.of(32), 2).prod());
    }

    /**
     * A binary operator together with its scalar reference.
     */
    private record BinaryCase(String name, DArrayBinaryOp op, DoubleBinaryOperator ref) {
    }

    private static List<BinaryCase> binaryCases() {
        List<BinaryCase> cases = new ArrayList<>();
        cases.add(new BinaryCase("add", DArrayOp.binaryAdd(), Double::sum));
        cases.add(new BinaryCase("sub", DArrayOp.binarySub(), (a, b) -> a - b));
        cases.add(new BinaryCase("mul", DArrayOp.binaryMul(), (a, b) -> a * b));
        cases.add(new BinaryCase("min", DArrayOp.binaryMin(), Math::min));
        cases.add(new BinaryCase("max", DArrayOp.binaryMax(), Math::max));
        return cases;
    }

    /**
     * Array-array in-place binary operators against a naive element-wise reference. The second operand is
     * exercised as: same factory layout, a transposed view, a row broadcast along axis 0, a column broadcast along
     * the last axis, and an operand of another data type (which forces a cast). Together with the three views of
     * the first operand this covers the contiguous, gather/scatter and broadcast SIMD paths and the scalar tails.
     */
    @ParameterizedTest
    @MethodSource("dataFactorySource")
    <N extends Number> void binaryOpsMatchNaiveReference(DataFactory<N> g) {
        Random random = new Random(42);
        DType<?> otherType = g.dt().equals(DType.DOUBLE) ? DType.FLOAT : DType.DOUBLE;
        for (Shape shape : SHAPES) {
            for (BinaryCase c : binaryCases()) {
                for (int viewKind = 0; viewKind < 3; viewKind++) {
                    DArray<N> probe = view(fill(g, shape, -20, 20, random), viewKind);
                    if (probe == null) {
                        continue;
                    }
                    Shape vs = probe.shape();
                    List<DArray<?>> others = new ArrayList<>();
                    others.add(view(fill(g, shape, -20, 20, random), viewKind));
                    if (vs.rank() >= 2) {
                        others.add(fill(g, Shape.of(reversed(vs.dims())), -20, 20, random).t());
                        // rank-1 operand broadcast along all leading axes by the operator itself
                        others.add(fill(g, Shape.of(vs.dim(-1)), -20, 20, random));
                        // first axis values broadcast along all trailing axes (stride 0 inner loop)
                        DArray<N> col = fill(g, Shape.of(vs.dim(0)), -20, 20, random);
                        for (int axis = 1; axis < vs.rank(); axis++) {
                            col = col.stretch(axis).expand(axis, vs.dim(axis));
                        }
                        others.add(col);
                    }
                    others.add(g.engine().random(otherType, vs, random));

                    for (int oi = 0; oi < others.size(); oi++) {
                        DArray<?> other = others.get(oi);
                        DArray<N> x = view(fill(g, shape, -20, 20, random), viewKind);
                        DArray<N> expected = x.copy(Order.C);
                        // the operator casts the operand to the target type before applying the function
                        double[] ov = broadcastValues(other, expected.shape());
                        for (int j = 0; j < ov.length; j++) {
                            ov[j] = g.value(ov[j]).doubleValue();
                        }
                        expected.apply_(Order.C, (i, p) -> g.value(c.ref().applyAsDouble(expected.ptrGetDouble(p), ov[i])));

                        x.binary_(c.op(), other);
                        assertClose(expected, x, tolerance(g.dt()), c.name() + " on " + shape + " view=" + viewKind + " other=" + oi);
                    }
                }
            }
        }
    }

    /**
     * Storages without SIMD support (data frame variables) must go through the scalar path and agree with the
     * vectorized one.
     */
    @Test
    void binaryOpsOnNonSimdStorageMatchArrayStorage() {
        Random random = new Random(5);
        VarDouble va = VarDouble.from(37, () -> random.nextGaussian());
        VarDouble vb = VarDouble.from(37, () -> random.nextGaussian());
        DArray<Double> a = va.darray_();
        DArray<Double> b = vb.darray_();
        DArray<Double> ac = a.copy();
        DArray<Double> bc = b.copy();
        a.add_(b);
        ac.add_(bc);
        assertTrue(a.deepEquals(ac, 1e-15));
        // mixed: array storage target, non-simd operand and the other way around
        DArray<Double> a2 = va.darray_().copy();
        a2.mul_(b);
        DArray<Double> a3 = va.darray_();
        a3.mul_(bc);
        assertTrue(a2.deepEquals(a3, 1e-15));
    }

    /**
     * In-place fused multiply-add {@code x += a * t} against a naive reference, for every layout, view and
     * for operands of the same and of a different data type.
     */
    @ParameterizedTest
    @MethodSource("dataFactorySource")
    <N extends Number> void fmaMatchesNaiveReference(DataFactory<N> g) {
        Random random = new Random(42);
        DType<?> otherType = g.dt().equals(DType.DOUBLE) ? DType.FLOAT : DType.DOUBLE;
        N factor = g.value(3);
        for (Shape shape : SHAPES) {
            for (int viewKind = 0; viewKind < 3; viewKind++) {
                DArray<N> probe = view(fill(g, shape, -20, 20, random), viewKind);
                if (probe == null) {
                    continue;
                }
                Shape vs = probe.shape();
                List<DArray<?>> others = new ArrayList<>();
                others.add(view(fill(g, shape, -20, 20, random), viewKind));
                if (vs.rank() >= 2) {
                    others.add(fill(g, Shape.of(reversed(vs.dims())), -20, 20, random).t());
                }
                others.add(g.engine().random(otherType, vs, random));

                for (int oi = 0; oi < others.size(); oi++) {
                    DArray<?> other = others.get(oi);
                    DArray<N> x = view(fill(g, shape, -20, 20, random), viewKind);
                    DArray<N> expected = x.copy(Order.C);
                    double[] ov = values(other);
                    for (int j = 0; j < ov.length; j++) {
                        ov[j] = g.value(ov[j]).doubleValue();
                    }
                    double f = factor.doubleValue();
                    expected.apply_(Order.C, (i, p) -> g.value(expected.ptrGetDouble(p) + f * ov[i]));

                    x.fma_(factor, other);
                    assertClose(expected, x, tolerance(g.dt()), "fma on " + shape + " view=" + viewKind + " other=" + oi);
                }
            }
        }
    }

    @Test
    void fmaOnNonSimdStorageMatchesArrayStorage() {
        Random random = new Random(6);
        VarDouble va = VarDouble.from(37, () -> random.nextGaussian());
        VarDouble vb = VarDouble.from(37, () -> random.nextGaussian());
        DArray<Double> a = va.darray_();
        DArray<Double> ac = a.copy();
        DArray<Double> bc = vb.darray_().copy();
        a.fma_(2.5, vb.darray_());
        ac.fma_(2.5, bc);
        assertTrue(a.deepEquals(ac, 1e-15));
    }

    /**
     * Matrix product against a naive triple loop. Sizes are chosen so that every blocking boundary is hit
     * (rows not a multiple of the 4-row micro tile, columns not a multiple of two vectors, inner dimension larger
     * than one k block) and operands come as C-ordered, F-ordered (transposed) and narrowed views, plus a
     * different data type for the right operand and a strided (transposed) target.
     */
    @ParameterizedTest
    @MethodSource("dataFactorySource")
    <N extends Number> void mmMatchesNaiveReference(DataFactory<N> g) {
        Random random = new Random(42);
        int[][] sizes = new int[][] {{1, 1, 1}, {3, 5, 7}, {7, 13, 11}, {70, 300, 37}, {133, 17, 261}};
        DType<?> otherType = g.dt().equals(DType.DOUBLE) ? DType.FLOAT : DType.DOUBLE;
        for (int[] mnp : sizes) {
            int m = mnp[0], n = mnp[1], p = mnp[2];
            // small magnitudes keep integer products exact and float sums well conditioned
            DArray<N> a = fill(g, Shape.of(m, n), -3, 3, random);
            DArray<N> b = fill(g, Shape.of(n, p), -3, 3, random);
            double[][] ref = naiveMm(a, b);

            assertMm(ref, a.mm(b), g.dt(), "C x C " + m + "x" + n + "x" + p);
            assertMm(ref, a.mm(b, Order.F), g.dt(), "C x C, F target " + m + "x" + n + "x" + p);

            DArray<N> at = fill(g, Shape.of(n, m), -3, 3, random).t();
            DArray<N> bt = fill(g, Shape.of(p, n), -3, 3, random).t();
            assertMm(naiveMm(at, bt), at.mm(bt), g.dt(), "T x T " + m + "x" + n + "x" + p);
            assertMm(naiveMm(a, bt), a.mm(bt), g.dt(), "C x T " + m + "x" + n + "x" + p);

            if (n > 2 && p > 2) {
                DArray<N> an = fill(g, Shape.of(m, n + 2), -3, 3, random).narrow(1, true, 1, n + 1);
                DArray<N> bn = fill(g, Shape.of(n + 2, p + 2), -3, 3, random).narrow(0, true, 1, n + 1).narrow(1, true, 1, p + 1);
                assertMm(naiveMm(an, bn), an.mm(bn), g.dt(), "narrow x narrow " + m + "x" + n + "x" + p);
            }

            DArray<?> bo = g.engine().random(otherType, Shape.of(n, p), random);
            DArray<N> boCast = bo.cast(g.dt());
            assertMm(naiveMm(a, boCast), a.mm(bo), g.dt(), "C x other dtype " + m + "x" + n + "x" + p);

            // accumulating into a strided target: C is a transposed view
            DArray<N> target = g.zeros(Shape.of(p, m)).t();
            a.mm(b, target);
            assertMm(ref, target, g.dt(), "into transposed target " + m + "x" + n + "x" + p);
        }
    }

    @Test
    void bmmMatchesMm() {
        Random random = new Random(9);
        DArrayManager dm = DArrayManager.base(2, 0);
        DArray<Double> a = dm.random(DType.DOUBLE, Shape.of(3, 9, 13), random);
        DArray<Double> b = dm.random(DType.DOUBLE, Shape.of(3, 13, 6), random);
        DArray<Double> r = a.bmm(b);
        assertEquals(Shape.of(3, 9, 6), r.shape());
        for (int i = 0; i < 3; i++) {
            assertTrue(r.selsq(0, i).deepEquals(a.selsq(0, i).mm(b.selsq(0, i)), 1e-12));
        }
    }

    /**
     * Matrix-vector and vector-matrix products against a naive loop, for C-ordered, transposed (column contiguous)
     * and narrowed (arbitrary stride) matrices, strided vectors and a different vector data type. Sizes are odd so
     * that vector tails are exercised on both kernels (row dot products and column updates).
     */
    @ParameterizedTest
    @MethodSource("dataFactorySource")
    <N extends Number> void mvAndVtmMatchNaiveReference(DataFactory<N> g) {
        Random random = new Random(42);
        DType<?> otherType = g.dt().equals(DType.DOUBLE) ? DType.FLOAT : DType.DOUBLE;
        int[][] sizes = new int[][] {{1, 1}, {3, 7}, {13, 11}, {37, 133}, {261, 17}};
        for (int[] mn : sizes) {
            int m = mn[0], n = mn[1];
            List<DArray<N>> matrices = new ArrayList<>();
            matrices.add(fill(g, Shape.of(m, n), -3, 3, random));
            matrices.add(fill(g, Shape.of(n, m), -3, 3, random).t_());
            if (n > 2) {
                matrices.add(fill(g, Shape.of(m, n + 2), -3, 3, random).narrow(1, true, 1, n + 1));
            }
            for (int mi = 0; mi < matrices.size(); mi++) {
                DArray<N> a = matrices.get(mi);
                String ctx = m + "x" + n + " matrix=" + mi;

                DArray<N> x = fill(g, Shape.of(n), -3, 3, random);
                DArray<N> xs = fill(g, Shape.of(n, 3), -3, 3, random).selsq(1, 1);   // strided vector
                DArray<?> xo = g.engine().random(otherType, Shape.of(n), random);
                assertVector(naiveMv(a, x), a.mv(x), g.dt(), "mv " + ctx);
                assertVector(naiveMv(a, xs), a.mv(xs), g.dt(), "mv strided x " + ctx);
                assertVector(naiveMv(a, xo.cast(g.dt())), a.mv(xo), g.dt(), "mv other dtype " + ctx);

                DArray<N> v = fill(g, Shape.of(m), -3, 3, random);
                DArray<N> vs = fill(g, Shape.of(m, 3), -3, 3, random).selsq(1, 1);
                DArray<?> vo = g.engine().random(otherType, Shape.of(m), random);
                assertVector(naiveMv(a.t_(), v), v.vtm(a), g.dt(), "vtm " + ctx);
                assertVector(naiveMv(a.t_(), vs), vs.vtm(a), g.dt(), "vtm strided v " + ctx);
                assertVector(naiveMv(a.t_(), vo.cast(g.dt())), vo.cast(g.dt()).vtm(a), g.dt(), "vtm other dtype " + ctx);
            }
        }
    }

    private static double[] naiveMv(DArray<?> a, DArray<?> x) {
        int m = a.dim(0), n = a.dim(1);
        double[] y = new double[m];
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < n; k++) {
                y[i] += a.getDouble(i, k) * x.getDouble(k);
            }
        }
        return y;
    }

    private static void assertVector(double[] expected, DArray<?> actual, DType<?> dt, String message) {
        assertEquals(1, actual.rank(), message);
        assertEquals(expected.length, actual.dim(0), message);
        double tol = dt.isInteger() ? 0 : (dt.equals(DType.FLOAT) ? 1e-4 : 1e-11);
        for (int i = 0; i < expected.length; i++) {
            double scale = Math.max(1.0, Math.abs(expected[i]));
            assertEquals(expected[i], actual.getDouble(i), tol * scale, message + " at " + i);
        }
    }

    private static double[][] naiveMm(DArray<?> a, DArray<?> b) {
        int m = a.dim(0), n = a.dim(1), p = b.dim(1);
        double[][] c = new double[m][p];
        for (int i = 0; i < m; i++) {
            for (int k = 0; k < n; k++) {
                double aik = a.getDouble(i, k);
                for (int j = 0; j < p; j++) {
                    c[i][j] += aik * b.getDouble(k, j);
                }
            }
        }
        return c;
    }

    private static void assertMm(double[][] expected, DArray<?> actual, DType<?> dt, String message) {
        assertEquals(expected.length, actual.dim(0), message);
        assertEquals(expected[0].length, actual.dim(1), message);
        double tol = dt.isInteger() ? 0 : (dt.equals(DType.FLOAT) ? 1e-4 : 1e-11);
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                double e = expected[i][j];
                double scale = Math.max(1.0, Math.abs(e));
                assertEquals(e, actual.getDouble(i, j), tol * scale, message + " at (" + i + "," + j + ")");
            }
        }
    }

    // helpers

    private static int[] reversed(int[] dims) {
        int[] r = new int[dims.length];
        for (int i = 0; i < dims.length; i++) {
            r[i] = dims[dims.length - 1 - i];
        }
        return r;
    }

    /**
     * Values of {@code other} broadcast to {@code target} shape, in logical C order.
     */
    private static double[] broadcastValues(DArray<?> other, Shape target) {
        DArray<?> b = other;
        while (b.rank() < target.rank()) {
            b = b.stretch(0);
        }
        for (int axis = 0; axis < target.rank(); axis++) {
            if (b.dim(axis) != target.dim(axis)) {
                b = b.expand(axis, target.dim(axis));
            }
        }
        return values(b);
    }

    /**
     * Fills a fresh array produced by the factory (so the factory's layout is preserved) with uniform
     * values in {@code [lo, hi)}, cast to the factory's data type.
     */
    private static <N extends Number> DArray<N> fill(DataFactory<N> g, Shape shape, double lo, double hi, Random random) {
        DArray<N> x = g.zeros(shape);
        x.apply_(Order.C, (_, _) -> g.value(lo + (hi - lo) * random.nextDouble()));
        return x;
    }

    /**
     * Returns a view of the array: 0 = the array itself, 1 = transposed (rank >= 2 only), 2 = narrowed on axis 0.
     * Returns null when the view kind does not apply to the array.
     */
    private static <N extends Number> DArray<N> view(DArray<N> x, int kind) {
        return switch (kind) {
            case 0 -> x;
            case 1 -> x.rank() >= 2 ? x.t() : null;
            case 2 -> x.dim(0) > 2 ? x.narrow(0, true, 1, x.dim(0) - 1) : null;
            default -> throw new IllegalArgumentException();
        };
    }

    private static double[] values(DArray<?> x) {
        double[] v = new double[x.size()];
        Iterator<? extends Number> it = x.iterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            v[i++] = it.next().doubleValue();
        }
        assertEquals(v.length, i);
        return v;
    }

    private static double naiveSum(double[] v) {
        double s = 0;
        for (double d : v) {
            s += d;
        }
        return s;
    }

    private static double naiveProd(double[] v) {
        double s = 1;
        for (double d : v) {
            s *= d;
        }
        return s;
    }

    private static double naiveMin(double[] v) {
        double s = Double.POSITIVE_INFINITY;
        for (double d : v) {
            s = Math.min(s, d);
        }
        return s;
    }

    private static double naiveMax(double[] v) {
        double s = Double.NEGATIVE_INFINITY;
        for (double d : v) {
            s = Math.max(s, d);
        }
        return s;
    }

    /**
     * Two-pass variance computed entirely in double.
     */
    private static double naiveVar(double[] v, int ddof) {
        double mean = naiveSum(v) / v.length;
        double s = 0;
        for (double d : v) {
            s += (d - mean) * (d - mean);
        }
        return s / (v.length - ddof);
    }

    private static double tolerance(DType<?> dt) {
        if (dt.isInteger()) {
            return 0;
        }
        return dt.equals(DType.FLOAT) ? 1e-5 : 1e-12;
    }

    private static void assertScalarClose(double expected, double actual, DType<?> dt, String message) {
        // the kernels accumulate in the array's own type while the reference accumulates in double,
        // so reductions are allowed a looser, magnitude-scaled tolerance than element-wise operators
        double tol = dt.isInteger() ? 0 : (dt.equals(DType.FLOAT) ? 1e-3 : 1e-10);
        double scale = Math.max(1.0, Math.abs(expected));
        assertEquals(expected, actual, tol * scale, message);
    }

    private static void assertClose(DArray<?> expected, DArray<?> actual, double tol, String message) {
        assertEquals(expected.shape(), actual.shape(), message);
        Iterator<? extends Number> ei = expected.iterator(Order.C);
        Iterator<? extends Number> ai = actual.iterator(Order.C);
        int pos = 0;
        while (ei.hasNext()) {
            assertTrue(ai.hasNext(), message);
            double e = ei.next().doubleValue();
            double a = ai.next().doubleValue();
            double scale = Math.max(1.0, Math.abs(e));
            assertEquals(e, a, tol * scale, message + " at position " + pos);
            pos++;
        }
        assertFalse(ai.hasNext(), message);
    }
}
