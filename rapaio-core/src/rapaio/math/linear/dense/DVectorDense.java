/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.linear.dense;

import java.io.Serial;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.util.DoubleComparator;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Double2DoubleFunction;

public final class DVectorDense extends AbstractDVectorStore {

    public static DVectorDense empty(int n) {
        return new DVectorDense(0, n, new double[n]);
    }

    public static DVectorDense fill(int n, double fill) {
        return new DVectorDense(0, n, DoubleArrays.newFill(n, fill));
    }

    public static DVectorDense wrap(double... values) {
        return new DVectorDense(0, values.length, values);
    }

    public static DVectorDense wrapAt(int offset, int size, double... values) {
        return new DVectorDense(offset, size, values);
    }

    public static DVectorDense random(int size) {
        return random(new Random(), size);
    }

    public static DVectorDense random(Random random, int size) {
        return random(random, size, Normal.std());
    }

    public static DVectorDense random(int size, Distribution distribution) {
        return random(new Random(), size, distribution);
    }

    public static DVectorDense random(Random random, int size, Distribution distribution) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = distribution.sampleNext(random);
        }
        return new DVectorDense(0, array.length, array);
    }

    public static DVectorDense copy(DVector v) {
        if (v instanceof DVectorDense vd) {
            return vd.copy();
        }
        if (v instanceof AbstractDVectorStore vs) {
            double[] copy = vs.solidArrayCopy();
            return DVectorDense.wrap(copy);
        }
        double[] copy = new double[v.size()];
        for (int i = 0; i < v.size(); i++) {
            copy[i] = v.get(i);
        }
        return DVectorDense.wrap(copy);
    }

    @Serial
    private static final long serialVersionUID = 5763094452899116225L;

    private final int size;
    private final int offset;
    private final double[] array;

    public DVectorDense(int size) {
        this(0, size, new double[size]);
    }

    public DVectorDense(int offset, int size, double[] array) {
        super(size);
        this.offset = offset;
        this.size = size;
        this.array = array;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double[] array() {
        return array;
    }

    public int offset() {
        return offset;
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(species, array, offset + i);
    }

    @Override
    public DoubleVector loadVector(int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(species, array, offset + i, m);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset + i);
    }

    @Override
    public void storeVector(DoubleVector v, int i, VectorMask<Double> m) {
        v.intoArray(array, offset + i, m);
    }

    @Override
    public double get(int i) {
        return array[offset + i];
    }

    @Override
    public void set(int i, double value) {
        array[offset + i] = value;
    }

    @Override
    public void inc(int i, double value) {
        array[offset + i] += value;
    }

    @Override
    public double[] solidArrayCopy() {
        return Arrays.copyOfRange(array, offset, offset + size);
    }

    @Override
    public DVector map(int... indexes) {
        if (indexes.length > 0 && IntArrays.isDenseArray(indexes)) {
            return new DVectorDense(offset + indexes[0], indexes.length, array);
        }
        return new DVectorMap(offset, indexes, array);
    }

    @Override
    public DVector mapTo(DVector to, int... indexes) {
        for (int i = 0; i < indexes.length; i++) {
            to.set(i, array[offset + indexes[i]]);
        }
        return to;
    }

    public DVectorDense copy() {
        return new DVectorDense(0, size, Arrays.copyOfRange(array, offset, offset + size));
    }

    @Override
    public DVector fill(double value) {
        Arrays.fill(array, offset, offset + size, value);
        return this;
    }

    @Override
    public DVectorDense denseCopy(int len) {
        double[] copy = new double[len];
        System.arraycopy(array, offset, copy, 0, Math.min(size, len));
        return new DVectorDense(0, copy.length, copy);
    }

    // those static properties acts as freeze copies of code ready to be optimized

    private static final UnaryOp uOpLog = new UnaryOp(va -> va.lanewise(VectorOperators.LOG), Math::log);
    private static final UnaryOp uOpLog1p = new UnaryOp(va -> va.lanewise(VectorOperators.LOG1P), Math::log1p);
    private static final UnaryOp uOpLog10 = new UnaryOp(va -> va.lanewise(VectorOperators.LOG10), Math::log10);
    private static final UnaryOp uOpAbs = new UnaryOp(va -> va.lanewise(VectorOperators.ABS), Math::abs);
    private static final UnaryOp uOpNeg = new UnaryOp(va -> va.lanewise(VectorOperators.NEG), x -> -x);
    private static final UnaryOp uOpCos = new UnaryOp(va -> va.lanewise(VectorOperators.COS), Math::cos);
    private static final UnaryOp uOpCosh = new UnaryOp(va -> va.lanewise(VectorOperators.COSH), Math::cosh);
    private static final UnaryOp uOpAcos = new UnaryOp(va -> va.lanewise(VectorOperators.ACOS), Math::acos);
    private static final UnaryOp uOpSin = new UnaryOp(va -> va.lanewise(VectorOperators.SIN), Math::sin);
    private static final UnaryOp uOpSinh = new UnaryOp(va -> va.lanewise(VectorOperators.SINH), Math::sinh);
    private static final UnaryOp uOpAsin = new UnaryOp(va -> va.lanewise(VectorOperators.ASIN), Math::asin);
    private static final UnaryOp uOpTan = new UnaryOp(va -> va.lanewise(VectorOperators.TAN), Math::tan);
    private static final UnaryOp uOpTanh = new UnaryOp(va -> va.lanewise(VectorOperators.TANH), Math::tanh);
    private static final UnaryOp uOpAtan = new UnaryOp(va -> va.lanewise(VectorOperators.ATAN), Math::atan);
    private static final UnaryOp uOpExp = new UnaryOp(va -> va.lanewise(VectorOperators.EXP), Math::exp);
    private static final UnaryOp uOpExpm1 = new UnaryOp(va -> va.lanewise(VectorOperators.EXPM1), Math::expm1);
    private static final UnaryOp uOpSqrt = new UnaryOp(va -> va.lanewise(VectorOperators.SQRT), Math::sqrt);
    private static final UnaryOp uOpCbrt = new UnaryOp(va -> va.lanewise(VectorOperators.CBRT), Math::cbrt);

    private static final BinaryOp bOpAdd = new BinaryOp(DoubleVector::add, Double::sum);
    private static final BinaryOp bOpSub = new BinaryOp(DoubleVector::sub, (x, y) -> x - y);
    private static final BinaryOp bOpMul = new BinaryOp(DoubleVector::mul, (x, y) -> x * y);
    private static final BinaryOp bOpDiv = new BinaryOp(DoubleVector::div, (x, y) -> x / y);

    @Override
    public DVectorDense log() {
        return uOpLog.apply(this);
    }

    @Override
    public DVector logTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpLog.apply(this, tos);
        }
        return super.logTo(to);
    }

    @Override
    public DVectorDense log1p() {
        return uOpLog1p.apply(this);
    }

    @Override
    public DVector log1pTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpLog1p.apply(this, tos);
        }
        return super.log1pTo(to);
    }

    @Override
    public DVectorDense log10() {
        return uOpLog10.apply(this);
    }

    @Override
    public DVector log10To(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpLog10.apply(this, tos);
        }
        return super.log10To(to);
    }

    @Override
    public DVectorDense abs() {
        return uOpAbs.apply(this);
    }

    @Override
    public DVector absTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpAbs.apply(this, tos);
        }
        return super.absTo(to);
    }

    @Override
    public DVectorDense neg() {
        return uOpNeg.apply(this);
    }

    @Override
    public DVector negTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpNeg.apply(this, tos);
        }
        return super.negTo(to);
    }

    @Override
    public DVectorDense cos() {
        return uOpCos.apply(this);
    }

    @Override
    public DVector cosTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpCos.apply(this, tos);
        }
        return super.cosTo(to);
    }

    @Override
    public DVectorDense cosh() {
        return uOpCosh.apply(this);
    }

    @Override
    public DVector coshTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpCosh.apply(this, tos);
        }
        return super.coshTo(to);
    }

    @Override
    public DVectorDense acos() {
        return uOpAcos.apply(this);
    }

    @Override
    public DVector acosTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpAcos.apply(this, tos);
        }
        return super.acosTo(to);
    }

    @Override
    public DVectorDense sin() {
        return uOpSin.apply(this);
    }

    @Override
    public DVector sinTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpSin.apply(this, tos);
        }
        return super.sinTo(to);
    }

    @Override
    public DVectorDense sinh() {
        return uOpSinh.apply(this);
    }

    @Override
    public DVector sinhTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpSinh.apply(this, tos);
        }
        return super.sinhTo(to);
    }

    @Override
    public DVectorDense asin() {
        return uOpAsin.apply(this);
    }

    @Override
    public DVector asinTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpAsin.apply(this, tos);
        }
        return super.asinTo(to);
    }

    @Override
    public DVectorDense tan() {
        return uOpTan.apply(this);
    }

    @Override
    public DVector tanTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpTan.apply(this, tos);
        }
        return super.tanTo(to);
    }

    @Override
    public DVectorDense tanh() {
        return uOpTanh.apply(this);
    }

    @Override
    public DVector tanhTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpTanh.apply(this, tos);
        }
        return super.tanhTo(to);
    }

    @Override
    public DVectorDense atan() {
        return uOpAtan.apply(this);
    }

    @Override
    public DVector atanTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpAtan.apply(this, tos);
        }
        return super.atanTo(to);
    }

    @Override
    public DVectorDense exp() {
        return uOpExp.apply(this);
    }

    @Override
    public DVector expTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpExp.apply(this, tos);
        }
        return super.expTo(to);
    }

    @Override
    public DVectorDense expm1() {
        return uOpExpm1.apply(this);
    }

    @Override
    public DVector expm1To(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpExpm1.apply(this, tos);
        }
        return super.expm1To(to);
    }

    @Override
    public DVectorDense sqrt() {
        return uOpSqrt.apply(this);
    }

    @Override
    public DVector sqrtTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpSqrt.apply(this, tos);
        }
        return super.sqrtTo(to);
    }

    @Override
    public DVectorDense cbrt() {
        return uOpCbrt.apply(this);
    }

    @Override
    public DVector cbrtTo(DVector to) {
        if (to instanceof DVectorDense tos) {
            return uOpCbrt.apply(this, tos);
        }
        return super.cbrtTo(to);
    }

    // binary //

    @Override
    public DVector add(double x) {
        return bOpAdd.apply(this, this, x);
    }

    @Override
    public DVector addTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            return bOpAdd.apply(this, tos, x);
        }
        return super.addTo(to, x);
    }

    @Override
    public DVector add(DVector b) {
        if (b instanceof DVectorDense bs) {
            checkConformance(b);
            return bOpAdd.apply(this, this, bs);
        }
        return super.add(b);
    }

    @Override
    public DVector addTo(DVector to, DVector b) {
        if (b instanceof DVectorDense bs) {
            if (to instanceof DVectorDense tos) {
                return bOpAdd.apply(this, tos, bs);
            }
        }
        return super.addTo(to, b);
    }

    @Override
    public DVector sub(double x) {
        return bOpSub.apply(this, this, x);
    }

    @Override
    public DVector subTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            return bOpSub.apply(this, tos, x);
        }
        return super.subTo(to, x);
    }

    @Override
    public DVector sub(DVector b) {
        if (b instanceof DVectorDense bs) {
            checkConformance(b);
            return bOpSub.apply(this, this, bs);
        }
        return super.sub(b);
    }

    @Override
    public DVector subTo(DVector to, DVector b) {
        if (b instanceof DVectorDense bs) {
            if (to instanceof DVectorDense tos) {
                return bOpSub.apply(this, tos, bs);
            }
        }
        return super.subTo(to, b);
    }

    @Override
    public DVector mul(double x) {
        return bOpMul.apply(this, this, x);
    }

    @Override
    public DVector mulTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            return bOpMul.apply(this, tos, x);
        }
        return super.mulTo(to, x);
    }

    @Override
    public DVector mul(DVector b) {
        if (b instanceof DVectorDense bs) {
            return bOpMul.apply(this, this, bs);
        }
        return super.mul(b);
    }

    @Override
    public DVector mulTo(DVector to, DVector b) {
        if (b instanceof DVectorDense bs) {
            if (to instanceof DVectorDense tos) {
                return bOpMul.apply(this, tos, bs);
            }
        }
        return super.mulTo(to, b);
    }

    @Override
    public DVector div(double x) {
        return bOpDiv.apply(this, this, x);
    }

    @Override
    public DVector divTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            return bOpDiv.apply(this, tos, x);
        }
        return super.divTo(to, x);
    }

    @Override
    public DVector div(DVector b) {
        if (b instanceof DVectorDense bs) {
            return bOpDiv.apply(this, this, bs);
        }
        return super.div(b);
    }

    @Override
    public DVector divTo(DVector to, DVector b) {
        if (b instanceof DVectorDense bs) {
            if (to instanceof DVectorDense tos) {
                return bOpDiv.apply(this, tos, bs);
            }
        }
        return super.divTo(to, b);
    }

    @Override
    public DVector fma(double a, DVector y) {
        checkConformance(y);
        if (y instanceof DVectorDense yd) {
            int bound = species.loopBound(size);
            int i = 0;
            var va = DoubleVector.broadcast(species, a);
            for (; i < bound; i += species.length()) {
                var vs = loadVector(i);
                var vy = yd.loadVector(i).fma(va, vs);
                storeVector(vy, i);
            }
            for (; i < size; i++) {
                inc(i, yd.get(i) * a);
            }
            return this;
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public DVector fmaNew(double a, DVector y) {
        checkConformance(y);
        if (y instanceof DVectorDense yd) {
            double[] copy = new double[size];
            DoubleArrays.addMulTo(array, offset, a, yd.array, yd.offset, copy, 0, size);
            return new DVectorDense(0, copy.length, copy);
        }
        double[] copy = new double[size];
        for (int i = 0; i < size(); i++) {
            copy[i] = array[offset + i] + a * y.get(i);
        }
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public DVector cut(double low, double high) {
        if (!Double.isNaN(low)) {
            cutLowTo(this, low);
        }
        if (!Double.isNaN(high)) {
            cutHighTo(this, high);
        }
        return this;
    }

    @Override
    public DVector cutTo(DVector to, double low, double high) {
        if (to instanceof DVectorDense tos) {
            if (!Double.isNaN(low)) {
                cutLowTo(tos, low);
            }
            if (!Double.isNaN(high)) {
                cutHighTo(tos, high);
            }
            return tos;
        }
        return super.cutTo(to, low, high);
    }

    private DVector cutLowTo(DVectorDense to, double low) {
        var vlow = DoubleVector.broadcast(species, low);
        for (int i = 0; i < loopBound; i += speciesLen) {
            var va = loadVector(i);
            VectorMask<Double> m = va.lt(vlow);
            va = va.blend(vlow, m);
            to.storeVector(va, i);
        }
        for (int i = loopBound; i < size; i++) {
            if (!Double.isNaN(low)) {
                to.set(i, Math.max(low, get(i)));
            }
        }
        return to;
    }

    private DVector cutHighTo(DVectorDense to, double high) {
        var vhigh = DoubleVector.broadcast(species, high);
        for (int i = 0; i < loopBound; i += speciesLen) {
            var va = loadVector(i);
            VectorMask<Double> m = vhigh.lt(va);
            va = va.blend(vhigh, m);
            to.storeVector(va, i);
        }
        for (int i = loopBound; i < size; i++) {
            if (!Double.isNaN(high)) {
                to.set(i, Math.min(high, get(i)));
            }
        }
        return to;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            int i = 0;
            DoubleVector sum = DoubleVector.zero(species);
            for (; i < loopBound; i += speciesLen) {
                var va = loadVector(i);
                var vb = bd.loadVector(i);
                sum = va.fma(vb, sum);
            }
            double ss = sum.reduceLanes(VectorOperators.ADD);
            for (; i < size; i++) {
                ss = Math.fma(array[offset + i], bd.array[bd.offset + i], ss);
            }
            return ss;
        }
        double ss = 0;
        for (int i = 0; i < size; i++) {
            ss = Math.fma(array[offset + i], b.get(i), ss);
        }
        return ss;
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rows() != size || m.cols() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix and vector are not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < y.size(); j++) {
                sum += array[offset + i] * m.get(i, j) * y.get(j);
            }
        }
        return sum;
    }

    @Override
    public double dotBilinear(DMatrix m) {
        if (m.rows() != size() || m.cols() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                sum += array[offset + i] * m.get(i, j) * array[offset + j];
            }
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DVector m, DVector y) {
        if (m.size() != size() || m.size() != y.size()) {
            throw new IllegalArgumentException("Bilinear diagonal vector is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += array[offset + i] * m.get(i) * y.get(i);
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m) {
        if (m.rows() != size() || m.cols() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += array[offset + i] * m.get(i, i) * array[offset + i];
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m, DVector y) {
        if (m.rows() != size() || m.cols() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += array[offset + i] * m.get(i, i) * y.get(i);
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DVector m) {
        if (m.size() != size() || m.size() != size()) {
            throw new IllegalArgumentException("Bilinear diagonal vector is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += array[offset + i] * m.get(i) * array[offset + i];
        }
        return sum;
    }

    @Override
    public double norm(double p) {
        if (p <= 0) {
            return size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            return max();
        }
        double sum = 0.0;
        for (int i = offset; i < offset + size; i++) {
            sum += Math.pow(Math.abs(array[i]), p);
        }
        return Math.pow(sum, 1.0 / p);
    }

    @Override
    public double sum() {
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(species);
        int bound = species.loopBound(size());
        for (; i < bound; i += speciesLen) {
            var vi = loadVector(i);
            aggr = aggr.add(vi);
        }
        VectorMask<Double> loopMask = species.indexInRange(i, size);
        var vi = loadVector(i, loopMask);
        aggr = aggr.add(vi, loopMask);
        return aggr.reduceLanes(VectorOperators.ADD);
    }

    @Override
    public DVector cumsum() {
        for (int i = offset + 1; i < offset + size; i++) {
            array[i] += array[i - 1];
        }
        return this;
    }

    @Override
    public double nanprod() {
        return DoubleArrays.nanProd(array, offset, size);
    }

    @Override
    public DVector cumprod() {
        for (int i = offset + 1; i < offset + size; i++) {
            array[i] = array[i - 1] * array[i];
        }
        return this;
    }

    @Override
    public int nancount() {
        return DoubleArrays.nanCount(array, offset, size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(array, offset, size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanMean(array, offset, size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(array, offset, size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanVariance(array, offset, size);
    }

    @Override
    public int argmin() {
        return DoubleArrays.argmin(array, offset, size) - offset;
    }

    @Override
    public double min() {
        return DoubleArrays.min(array, offset, size);
    }

    @Override
    public int argmax() {
        return DoubleArrays.argmax(array, offset, size) - offset;
    }

    @Override
    public double max() {
        return DoubleArrays.max(array, offset, size);
    }

    @Override
    public DVector apply(Double2DoubleFunction f) {
        for (int i = offset; i < offset + size; i++) {
            array[i] = f.applyAsDouble(array[i]);
        }
        return this;
    }

    @Override
    public DVector applyTo(DVector to, Double2DoubleFunction f) {
        for (int i = 0; i < size; i++) {
            to.set(i, f.applyAsDouble(array[offset + i]));
        }
        return to;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f) {
        for (int i = 0; i < size; i++) {
            array[offset + i] = f.apply(i, array[offset + i]);
        }
        return this;
    }

    @Override
    public DVector applyTo(DVector to, BiFunction<Integer, Double, Double> f) {
        for (int i = 0; i < size; i++) {
            to.set(i, f.apply(i, array[offset + i]));
        }
        return to;
    }

    @Override
    public DVector sortValues(DoubleComparator comp) {
        DoubleArrays.quickSort(array, offset, offset + size, comp);
        return this;
    }

    @Override
    public DVector sortValuesNew(DoubleComparator comp) {
        double[] copy = solidArrayCopy();
        DoubleArrays.quickSort(copy, 0, size, comp);
        return new DVectorDense(0, size, copy);
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(array).skip(offset).limit(size);
    }

    @Override
    public VarDouble dv() {
        if (offset != 0) {
            double[] copy = Arrays.copyOfRange(array, offset, size + offset);
            return VarDouble.wrapArray(size, copy);
        }
        return VarDouble.wrapArray(size, array);
    }

    private record UnaryOp(Function<DoubleVector, DoubleVector> opFun, Function<Double, Double> sFun) {

        public DVectorDense apply(DVectorDense ref) {
            return apply(ref, ref);
        }

        public DVectorDense apply(DVectorDense ref, DVectorDense to) {
            int i = 0;
            for (; i < ref.loopBound; i += speciesLen) {
                var va = ref.loadVector(i);
                va = opFun.apply(va);
                to.storeVector(va, i);
            }
            for (; i < ref.size; i++) {
                to.set(i, sFun.apply(ref.get(i)));
            }
            return to;
        }
    }

    private record BinaryOp(BiFunction<DoubleVector, DoubleVector, DoubleVector> opFun, BiFunction<Double, Double, Double> sFun) {

        public DVectorDense apply(DVectorDense ref, DVectorDense to, double x) {
            int i = 0;
            DoubleVector vx = DoubleVector.broadcast(species, x);
            for (; i < ref.loopBound; i += speciesLen) {
                var va = ref.loadVector(i);
                va = opFun.apply(va, vx);
                to.storeVector(va, i);
            }
            for (; i < ref.size(); i++) {
                to.set(i, sFun.apply(ref.get(i), x));
            }
            return to;
        }

        public DVectorDense apply(DVectorDense ref, DVectorDense to, DVectorDense b) {
            int loopBound = ref.loopBound;
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var va = ref.loadVector(i);
                var vb = b.loadVector(i);
                va = opFun.apply(va, vb);
                to.storeVector(va, i);
            }
            for (; i < ref.size(); i++) {
                to.set(i, sFun.apply(ref.get(i), b.get(i)));
            }
            return to;
        }
    }
}
