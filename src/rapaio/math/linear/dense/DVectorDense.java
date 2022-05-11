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

package rapaio.math.linear.dense;

import java.io.Serial;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
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
        return random(size, Normal.std());
    }

    public static DVectorDense random(int size, Distribution distribution) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = distribution.sampleNext();
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

    private final int offset;
    private final int size;
    private final double[] array;
    private final VectorMask<Double> loopMask;

    public DVectorDense(int size) {
        this(0, size, new double[size]);
    }

    public DVectorDense(int offset, int size, double[] array) {
        super(size);
        this.offset = offset;
        this.size = size;
        this.array = array;
        this.loopMask = species.indexInRange(loopBound, size);
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
    public VectorMask<Double> loopMask() {
        return loopMask;
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

    @Override
    public DVector add(double x) {
        int i = 0;
        DoubleVector va = DoubleVector.broadcast(species, x);
        for (; i < loopBound; i += speciesLen) {
            var v = loadVector(i).add(va);
            storeVector(v, i);
        }
        var v = loadVector(i, loopMask).add(va, loopMask);
        storeVector(v, i, loopMask);
        return this;
    }

    @Override
    public DVector addTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            var va = DoubleVector.broadcast(species, x);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var v = loadVector(i).add(va);
                tos.storeVector(v, i);
            }
            for (; i < size; i++) {
                tos.set(i, get(i) + x);
            }
            return tos;
        }
        return super.addTo(to, x);
    }

    @Override
    public DVector add(DVector b) {
        if (b instanceof DVectorDense bs) {
            checkConformance(b);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var vb = bs.loadVector(i);
                var va = loadVector(i).add(vb);
                storeVector(va, i);
            }
            for (; i < size; i++) {
                inc(i, bs.get(i));
            }
            return this;
        }
        return super.add(b);
    }

    @Override
    public DVector addTo(DVector to, DVector b) {
        if (b instanceof AbstractDVectorStore bs) {
            if (to instanceof DVectorDense tos) {
                checkConformance(b);
                int i = 0;
                for (; i < loopBound; i += speciesLen) {
                    var vb = bs.loadVector(i);
                    var va = loadVector(i).add(vb);
                    tos.storeVector(va, i);
                }
                for (; i < size; i++) {
                    tos.set(i, get(i) + bs.get(i));
                }
                return tos;
            }
        }
        return super.addTo(to, b);
    }

    @Override
    public DVector sub(double x) {
        int i = 0;
        DoubleVector va = DoubleVector.broadcast(species, x);
        for (; i < loopBound; i += speciesLen) {
            var v = loadVector(i).sub(va);
            storeVector(v, i);
        }
        for (; i < size; i++) {
            inc(i, -x);
        }
        return this;
    }

    @Override
    public DVector subTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            var va = DoubleVector.broadcast(species, x);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var v = loadVector(i).sub(va);
                tos.storeVector(v, i);
            }
            for (; i < size; i++) {
                tos.set(i, get(i) - x);
            }
            return tos;
        }
        return super.subTo(to, x);
    }

    @Override
    public DVector sub(DVector b) {
        if (b instanceof DVectorDense bs) {
            checkConformance(b);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var vb = bs.loadVector(i);
                var va = loadVector(i).sub(vb);
                storeVector(va, i);
            }
            for (; i < size; i++) {
                inc(i, -bs.get(i));
            }
            return this;
        }
        return super.sub(b);
    }

    @Override
    public DVector subTo(DVector to, DVector b) {
        if (b instanceof AbstractDVectorStore bs) {
            if (to instanceof DVectorDense tos) {
                checkConformance(b);
                int i = 0;
                for (; i < loopBound; i += speciesLen) {
                    var vb = bs.loadVector(i);
                    var va = loadVector(i).sub(vb);
                    tos.storeVector(va, i);
                }
                for (; i < size; i++) {
                    tos.set(i, get(i) - bs.get(i));
                }
                return tos;
            }
        }
        return super.subTo(to, b);
    }

    @Override
    public DVector mul(double x) {
        int i = 0;
        DoubleVector va = DoubleVector.broadcast(species, x);
        for (; i < loopBound; i += speciesLen) {
            var v = loadVector(i).mul(va);
            storeVector(v, i);
        }
        for (; i < size; i++) {
            set(i, get(i) * x);
        }
        return this;
    }

    @Override
    public DVector mulTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            var va = DoubleVector.broadcast(species, x);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var v = loadVector(i).mul(va);
                tos.storeVector(v, i);
            }
            for (; i < size; i++) {
                tos.set(i, get(i) * x);
            }
            return tos;
        }
        return super.mulTo(to, x);
    }

    @Override
    public DVector mul(DVector b) {
        if (b instanceof DVectorDense bs) {
            checkConformance(b);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var vb = bs.loadVector(i);
                var va = loadVector(i).mul(vb);
                storeVector(va, i);
            }
            for (; i < size; i++) {
                set(i, get(i) * bs.get(i));
            }
            return this;
        }
        return super.mul(b);
    }

    @Override
    public DVector mulTo(DVector to, DVector b) {
        if (b instanceof AbstractDVectorStore bs) {
            if (to instanceof DVectorDense tos) {
                checkConformance(b);
                int i = 0;
                for (; i < loopBound; i += speciesLen) {
                    var vb = bs.loadVector(i);
                    var va = loadVector(i).mul(vb);
                    tos.storeVector(va, i);
                }
                for (; i < size; i++) {
                    tos.set(i, get(i) * bs.get(i));
                }
                return tos;
            }
        }
        return super.mulTo(to, b);
    }

    @Override
    public DVector div(double x) {
        int i = 0;
        DoubleVector va = DoubleVector.broadcast(species, x);
        for (; i < loopBound; i += speciesLen) {
            var v = loadVector(i).div(va);
            storeVector(v, i);
        }
        for (; i < size; i++) {
            set(i, get(i) / x);
        }
        return this;
    }

    @Override
    public DVector divTo(DVector to, double x) {
        if (to instanceof DVectorDense tos) {
            var va = DoubleVector.broadcast(species, x);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var v = loadVector(i).div(va);
                tos.storeVector(v, i);
            }
            for (; i < size; i++) {
                tos.set(i, get(i) / x);
            }
            return tos;
        }
        return super.divTo(to, x);
    }

    @Override
    public DVector div(DVector b) {
        if (b instanceof DVectorDense bs) {
            checkConformance(b);
            int i = 0;
            for (; i < loopBound; i += speciesLen) {
                var vb = bs.loadVector(i);
                var va = loadVector(i).div(vb);
                storeVector(va, i);
            }
            for (; i < size; i++) {
                set(i, get(i) / bs.get(i));
            }
            return this;
        }
        return super.div(b);
    }

    @Override
    public DVector divTo(DVector to, DVector b) {
        if (b instanceof AbstractDVectorStore bs) {
            if (to instanceof DVectorDense tos) {
                checkConformance(b);
                int i = 0;
                for (; i < loopBound; i += speciesLen) {
                    var vb = bs.loadVector(i);
                    var va = loadVector(i).div(vb);
                    tos.storeVector(va, i);
                }
                for (; i < size; i++) {
                    tos.set(i, get(i) / bs.get(i));
                }
                return tos;
            }
        }
        return super.divTo(to, b);
    }

    @Override
    public DVector addMul(double a, DVector y) {
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
    public DVector addMulNew(double a, DVector y) {
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
}
