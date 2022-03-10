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
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.AbstractStorageDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.DoubleComparator;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

public class DVectorDense extends AbstractStorageDVector {

    public static DVectorDense empty(int n) {
        return new DVectorDense(0, n, new double[n]);
    }

    public static DVectorDense fill(int n, double fill) {
        return new DVectorDense(0, n, DoubleArrays.newFill(n, fill));
    }

    public static DVectorDense wrap(int offset, int size, double[] values) {
        return new DVectorDense(offset, size, values);
    }

    public static DVectorDense random(int size, Distribution distribution) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = distribution.sampleNext();
        }
        return new DVectorDense(0, array.length, array);
    }

    @Serial
    private static final long serialVersionUID = 5763094452899116225L;
    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public static final class Storage implements DVectorStorage {

        public final int offset;
        public final int size;
        public final double[] array;

        public Storage(int offset, int size, double[] array) {
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

        @Override
        public DoubleVector loadVector(int i) {
            return DoubleVector.fromArray(SPECIES, array, offset + i);
        }

        @Override
        public DoubleVector loadVector(int i, VectorMask<Double> m) {
            return DoubleVector.fromArray(SPECIES, array, offset + i, m);
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

        public double sum() {
            int i = 0;
            DoubleVector aggr = DoubleVector.zero(SPECIES);
            int bound = SPECIES.loopBound(size());
            for (; i < bound; i += SPECIES.length()) {
                DoubleVector xv = loadVector(i);
                aggr = aggr.add(xv);
            }
            double result = aggr.reduceLanes(VectorOperators.ADD);
            for (; i < size(); i++) {
                result = result + array[offset + i];
            }
            return result;
        }

        public void add(double x) {
            DoubleVector add = DoubleVector.broadcast(SPECIES, x);
            int i = 0;
            int bound = SPECIES.loopBound(size);
            for (; i < bound; i += SPECIES.length()) {
                var v = DoubleVector.fromArray(SPECIES, array, offset + i);
                v.add(add).intoArray(array, offset + i);
            }
            for (; i < size; i++) {
                array[offset + i] += x;
            }
        }

    }

    private final Storage s;

    public DVectorDense(int offset, int size, double[] array) {
        super(new Storage(offset, size, array));
        this.s = (Storage) storage;
    }

    @Override
    public int size() {
        return s.size;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.copyByIndex(s.array, s.offset, indexes);
            return new DVectorDense(0, copy.length, copy);
        } else {
            return new DVectorMap(s.offset, indexes, s.array);
        }
    }

    public DVectorDense copy() {
        return new DVectorDense(0, s.size, Arrays.copyOfRange(s.array, s.offset, s.offset + s.size));
    }

    @Override
    public DVector fill(double value) {
        Arrays.fill(s.array, s.offset, s.offset + s.size, value);
        return this;
    }

//    @Override
//    public DVector add(double x, AlgebraOption<?>... opts) {
//        if (AlgebraOptions.from(opts).isCopy()) {
//            double[] copy = new double[s.size];
//            DoubleArrays.addTo(s.array, s.offset, x, copy, 0, copy.length);
//            return DVector.wrap(copy);
//        }
//        DoubleArrays.add(s.array, s.offset, x, s.size);
//        return this;
//    }
//


    @Override
    public DVector add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            DoubleArrays.addTo(s.array, s.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        s.add(x);
        return this;
    }

    @Override
    public DVectorDense add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.addTo(s.array, s.offset, bd.s.array, bd.s.offset, copy, 0, s.size);
            } else {
                for (int i = 0; i < s.size; i++) {
                    copy[i] = s.array[s.offset + i] + b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.add(s.array, s.offset, bd.s.array, bd.s.offset, s.size);
            return this;
        }
        for (int i = 0; i < s.size; i++) {
            s.array[s.offset + i] += b.get(i);
        }
        return this;
    }


    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            DoubleArrays.subTo(s.array, s.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.sub(s.array, s.offset, x, s.size);
        return this;
    }

    @Override
    public DVectorDense sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.subTo(s.array, s.offset, bd.s.array, bd.s.offset, copy, 0, s.size);
            } else {
                for (int i = 0; i < s.size; i++) {
                    copy[i] = s.array[s.offset + i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.sub(s.array, s.offset, bd.s.array, bd.s.offset, s.size);
            return this;
        }
        for (int i = 0; i < s.size; i++) {
            s.array[s.offset + i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector mul(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            DoubleArrays.multTo(s.array, s.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.mul(s.array, s.offset, x, s.size);
        return this;
    }

    @Override
    public DVector mul(DVector b, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.multTo(s.array, s.offset, bd.s.array, bd.s.offset, copy, 0, s.size);
            } else {
                for (int i = 0; i < s.size; i++) {
                    copy[i] = s.array[s.offset + i] * b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            DoubleArrays.mul(s.array, s.offset, bd.s.array, bd.s.offset, s.size);
            return this;
        }
        for (int i = 0; i < s.size; i++) {
            s.array[s.offset + i] *= b.get(i);
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            DoubleArrays.divTo(s.array, s.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.div(s.array, s.offset, x, s.size);
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.divTo(s.array, s.offset, bd.s.array, bd.s.offset, copy, 0, s.size);
            } else {
                for (int i = 0; i < s.size; i++) {
                    copy[i] = s.array[s.offset + i] / b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.div(s.array, s.offset, bd.s.array, bd.s.offset, s.size);
            return this;
        }
        for (int i = 0; i < s.size; i++) {
            s.array[s.offset + i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector addMul(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
            if (y instanceof DVectorDense yd) {
                double[] copy = new double[s.size];
                DoubleArrays.addMulTo(s.array, s.offset, a, yd.s.array, yd.s.offset, copy, 0, s.size);
                return new DVectorDense(0, copy.length, copy);
            }
            double[] copy = new double[s.size];
            for (int i = 0; i < size(); i++) {
                copy[i] = s.array[s.offset + i] + a * y.get(i);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        if (y instanceof DVectorDense yd) {
            DoubleArrays.addMul(s.array, s.offset, a, yd.s.array, yd.s.offset, s.size);
            return this;
        }
        for (int i = 0; i < s.size; i++) {
            s.array[s.offset + i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            double ss = 0;
            for (int i = 0; i < s.size; i++) {
                ss = Math.fma(s.array[s.offset + i], bd.s.array[bd.s.offset + i], ss);
            }
            return ss;
        }
        double ss = 0;
        for (int i = 0; i < s.size; i++) {
            ss = Math.fma(s.array[s.offset + i], b.get(i), ss);
        }
        return ss;
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rowCount() != s.size || m.colCount() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix and vector are not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < s.size; i++) {
            for (int j = 0; j < y.size(); j++) {
                sum += s.array[s.offset + i] * m.get(i, j) * y.get(j);
            }
        }
        return sum;
    }

    @Override
    public double dotBilinear(DMatrix m) {
        if (m.rowCount() != size() || m.colCount() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < size(); j++) {
                sum += s.array[s.offset + i] * m.get(i, j) * s.array[s.offset + j];
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
            sum += s.array[s.offset + i] * m.get(i) * y.get(i);
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m) {
        if (m.rowCount() != size() || m.colCount() != size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += s.array[s.offset + i] * m.get(i, i) * s.array[s.offset + i];
        }
        return sum;
    }

    @Override
    public double dotBilinearDiag(DMatrix m, DVector y) {
        if (m.rowCount() != size() || m.colCount() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix is not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < size(); i++) {
            sum += s.array[s.offset + i] * m.get(i, i) * y.get(i);
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
            sum += s.array[s.offset + i] * m.get(i) * s.array[s.offset + i];
        }
        return sum;
    }

    @Override
    public double pnorm(double p) {
        if (p <= 0) {
            return s.size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            return max();
        }
        double sum = 0.0;
        for (int i = s.offset; i < s.offset + s.size; i++) {
            sum += Math.pow(Math.abs(s.array[i]), p);
        }
        return Math.pow(sum, 1.0 / p);
    }

    @Override
    public double sum() {
        return s.sum();
    }

    @Override
    public DVector cumsum() {
        for (int i = s.offset + 1; i < s.offset + s.size; i++) {
            s.array[i] += s.array[i - 1];
        }
        return this;
    }

    @Override
    public double nanprod() {
        return DoubleArrays.nanProd(s.array, s.offset, s.size);
    }

    @Override
    public DVector cumprod() {
        for (int i = s.offset + 1; i < s.offset + s.size; i++) {
            s.array[i] = s.array[i - 1] * s.array[i];
        }
        return this;
    }

    @Override
    public int nancount() {
        return DoubleArrays.nanCount(s.array, s.offset, s.size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(s.array, s.offset, s.size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanMean(s.array, s.offset, s.size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(s.array, s.offset, s.size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanVariance(s.array, s.offset, s.size);
    }

    @Override
    public int argmin() {
        return DoubleArrays.argmin(s.array, s.offset, s.size) - s.offset;
    }

    @Override
    public double min() {
        return DoubleArrays.min(s.array, s.offset, s.size);
    }

    @Override
    public int argmax() {
        return DoubleArrays.argmax(s.array, s.offset, s.size) - s.offset;
    }

    @Override
    public double max() {
        return DoubleArrays.max(s.array, s.offset, s.size);
    }

    @Override
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.newFrom(s.array, s.offset, s.size + s.offset, f);
            return DVector.wrap(copy);
        }
        for (int i = s.offset; i < s.offset + s.size; i++) {
            s.array[i] = f.applyAsDouble(s.array[i]);
        }
        return this;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[s.size];
            for (int i = 0; i < s.size; i++) {
                copy[i] = f.apply(i, s.array[s.offset + i]);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < s.size; i++) {
            s.array[s.offset + i] = f.apply(i, s.array[s.offset + i]);
        }
        return this;
    }

    @Override
    public DVector sortValues(DoubleComparator comp, AlgebraOption<?>... opts) {
        DoubleArrays.quickSort(s.array, s.offset, s.offset + s.size, comp);
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(s.array).skip(s.offset).limit(s.size);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy() || s.offset != 0) {
            double[] copy = Arrays.copyOfRange(s.array, s.offset, s.size + s.offset);
            return VarDouble.wrapArray(s.size, copy);
        }
        return VarDouble.wrapArray(s.size, s.array);
    }
}
