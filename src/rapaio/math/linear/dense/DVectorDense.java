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

    private final int offset;
    private final int size;
    private final double[] array;

    public DVectorDense(int offset, int size, double[] array) {
        this.offset = offset;
        this.size = size;
        this.array = array;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.copyByIndex(array, offset, indexes);
            return new DVectorDense(0, copy.length, copy);
        } else {
            return new DVectorMap(offset, indexes, array);
        }
    }

    public DVectorDense copy() {
        return new DVectorDense(0, size, Arrays.copyOfRange(array, offset, offset + size));
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(SPECIES, array, offset + i);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset + i);
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
    public DVector fill(double value) {
        Arrays.fill(array, offset, offset + size, value);
        return this;
    }

    @Override
    public DVectorDense add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.addTo(array, offset, bd.array, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = array[offset + i] + b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.add(array, offset, bd.array, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] += b.get(i);
        }
        return this;
    }

    @Override
    public DVectorDense sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.subTo(array, offset, bd.array, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = array[offset + i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.sub(array, offset, bd.array, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector mul(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.multTo(array, offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.mul(array, offset, x, size);
        return this;
    }

    @Override
    public DVector mul(DVector b, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.multTo(array, offset, bd.array, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = array[offset + i] * b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            DoubleArrays.mul(array, offset, bd.array, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] *= b.get(i);
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.divTo(array, offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.div(array, offset, x, size);
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.divTo(array, offset, bd.array, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = array[offset + i] / b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.div(array, offset, bd.array, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector addMul(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
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
        if (y instanceof DVectorDense yd) {
            DoubleArrays.addMul(array, offset, a, yd.array, yd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            double s = 0;
            for (int i = 0; i < size; i++) {
                s = Math.fma(array[offset + i], bd.array[bd.offset + i], s);
            }
            return s;
        }
        double s = 0;
        for (int i = 0; i < size; i++) {
            s = Math.fma(array[offset + i], b.get(i), s);
        }
        return s;
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rowCount() != size || m.colCount() != y.size()) {
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
        if (m.rowCount() != size() || m.colCount() != size()) {
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
        if (m.rowCount() != size() || m.colCount() != size()) {
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
        if (m.rowCount() != size() || m.colCount() != y.size()) {
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
    public double pnorm(double p) {
        if (p <= 0) {
            return size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            double max = Double.NaN;
            for (int i = offset; i < size + offset; i++) {
                double value = array[i];
                if (Double.isNaN(max)) {
                    max = value;
                    continue;
                }
                max = Math.max(max, value);
            }
            return max;
        }
        double s = 0.0;
        for (int i = offset; i < offset + size; i++) {
            s += Math.pow(Math.abs(array[i]), p);
        }
        return Math.pow(s, 1.0 / p);
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
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.newFrom(array, offset, size + offset, f);
            return DVector.wrap(copy);
        }
        for (int i = offset; i < offset + size; i++) {
            array[i] = f.applyAsDouble(array[i]);
        }
        return this;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            for (int i = 0; i < size; i++) {
                copy[i] = f.apply(i, array[offset + i]);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size; i++) {
            array[offset + i] = f.apply(i, array[offset + i]);
        }
        return this;
    }

    @Override
    public DVector sortValues(DoubleComparator comp, AlgebraOption<?>... opts) {
        DoubleArrays.quickSort(array, offset, offset + size, comp);
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(array).skip(offset).limit(size);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy() || offset != 0) {
            double[] copy = Arrays.copyOfRange(array, offset, size + offset);
            return VarDouble.wrapArray(size, copy);
        }
        return VarDouble.wrapArray(size, array);
    }
}
