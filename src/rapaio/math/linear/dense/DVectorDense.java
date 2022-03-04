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

import rapaio.core.distributions.Distribution;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.DoubleComparator;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

public class DVectorDense extends AbstractDVector {

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

    public record Data(int offset, int size, double[] values) {

        public Data copy() {
            return new Data(offset, size, Arrays.copyOf(values, values.length));
        }

        public Data compactCopy() {
            return new Data(0, size, Arrays.copyOfRange(values, offset, size + offset));
        }
    }

    private final Data data;

    public DVectorDense(int offset, int size, double[] values) {
        this.data = new Data(offset, size, values);
    }

    public DVectorDense(Data data) {
        this.data = data;
    }

    @Override
    public int size() {
        return data.size;
    }

    public Data data() {
        return data;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.copyByIndex(data.values, data.offset, indexes);
            return new DVectorDense(0, copy.length, copy);
        } else {
            return new DVectorMap(this, indexes);
        }
    }

    public DVectorDense copy() {
        return new DVectorDense(data.compactCopy());
    }

    @Override
    public double get(int i) {
        return data.values[data.offset + i];
    }

    @Override
    public void set(int i, double value) {
        data.values[data.offset + i] = value;
    }

    @Override
    public void inc(int i, double value) {
        data.values[data.offset + i] += value;
    }

    @Override
    public DVector fill(double value) {
        Arrays.fill(data.values, data.offset, data.offset + data.size, value);
        return this;
    }

    @Override
    public DVectorDense add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            DoubleArrays.addTo(data.values, data.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.add(data.values, data.offset, x, data.size);
        return this;
    }

    @Override
    public DVectorDense add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.addTo(data.values, data.offset, bd.data.values, bd.data.offset, copy, 0, data.size);
            } else {
                for (int i = 0; i < data.size; i++) {
                    copy[i] = data.values[data.offset + i] + b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.add(data.values, data.offset, bd.data.values, bd.data.offset, data.size);
            return this;
        }
        for (int i = 0; i < data.size; i++) {
            data.values[data.offset + i] += b.get(i);
        }
        return this;
    }

    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            DoubleArrays.subTo(data.values, data.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.sub(data.values, data.offset, x, data.size);
        return this;
    }

    @Override
    public DVectorDense sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.subTo(data.values, data.offset, bd.data.values, bd.data.offset, copy, 0, data.size);
            } else {
                for (int i = 0; i < data.size; i++) {
                    copy[i] = data.values[data.offset + i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.sub(data.values, data.offset, bd.data.values, bd.data.offset, data.size);
            return this;
        }
        for (int i = 0; i < data.size; i++) {
            data.values[data.offset + i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector mul(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            DoubleArrays.multTo(data.values, data.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.mul(data.values, data.offset, x, data.size);
        return this;
    }

    @Override
    public DVector mul(DVector b, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.multTo(data.values, data.offset, bd.data.values, bd.data.offset, copy, 0, data.size);
            } else {
                for (int i = 0; i < data.size; i++) {
                    copy[i] = data.values[data.offset + i] * b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            DoubleArrays.mul(data.values, data.offset, bd.data.values, bd.data.offset, data.size);
            return this;
        }
        for (int i = 0; i < data.size; i++) {
            data.values[data.offset + i] *= b.get(i);
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            DoubleArrays.divTo(data.values, data.offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.div(data.values, data.offset, x, data.size);
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.divTo(data.values, data.offset, bd.data.values, bd.data.offset, copy, 0, data.size);
            } else {
                for (int i = 0; i < data.size; i++) {
                    copy[i] = data.values[data.offset + i] / b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.div(data.values, data.offset, bd.data.values, bd.data.offset, data.size);
            return this;
        }
        for (int i = 0; i < data.size; i++) {
            data.values[data.offset + i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector addMul(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
            if (y instanceof DVectorDense yd) {
                double[] copy = new double[data.size];
                DoubleArrays.addMulTo(data.values, data.offset, a, yd.data.values, yd.data.offset, copy, 0, data.size);
                return new DVectorDense(0, copy.length, copy);
            }
            double[] copy = new double[data.size];
            for (int i = 0; i < size(); i++) {
                copy[i] = data.values[data.offset + i] + a * y.get(i);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        if (y instanceof DVectorDense yd) {
            DoubleArrays.addMul(data.values, data.offset, a, yd.data.values, yd.data.offset, data.size);
            return this;
        }
        for (int i = 0; i < data.size; i++) {
            data.values[data.offset + i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            double s = 0;
            for (int i = 0; i < data.size; i++) {
                s = Math.fma(data.values[data.offset + i], bd.data.values[bd.data.offset + i], s);
            }
            return s;
        }
        double s = 0;
        for (int i = 0; i < data.size; i++) {
            s = Math.fma(data.values[data.offset + i], b.get(i), s);
        }
        return s;
    }

    @Override
    public double dotBilinear(DMatrix m, DVector y) {
        if (m.rowCount() != data.size || m.colCount() != y.size()) {
            throw new IllegalArgumentException("Bilinear matrix and vector are not conform for multiplication.");
        }
        double sum = 0.0;
        for (int i = 0; i < data.size; i++) {
            for (int j = 0; j < y.size(); j++) {
                sum += data.values[data.offset + i] * m.get(i, j) * y.get(j);
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
                sum += data.values[data.offset + i] * m.get(i, j) * data.values[data.offset + j];
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
            sum += data.values[data.offset + i] * m.get(i) * y.get(i);
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
            sum += data.values[data.offset + i] * m.get(i, i) * data.values[data.offset + i];
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
            sum += data.values[data.offset + i] * m.get(i, i) * y.get(i);
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
            sum += data.values[data.offset + i] * m.get(i) * data.values[data.offset + i];
        }
        return sum;
    }

    @Override
    public double pnorm(double p) {
        if (p <= 0) {
            return data.size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            double max = Double.NaN;
            for (int i = data.offset; i < data.size + data.offset; i++) {
                double value = data.values[i];
                if (Double.isNaN(max)) {
                    max = value;
                    continue;
                }
                max = Math.max(max, value);
            }
            return max;
        }
        double s = 0.0;
        for (int i = data.offset; i < data.offset + data.size; i++) {
            s += Math.pow(Math.abs(data.values[i]), p);
        }
        return Math.pow(s, 1.0 / p);
    }

    @Override
    public double sum() {
        return DoubleArrays.sum(data.values, data.offset, data.size);
    }

    @Override
    public double nansum() {
        return DoubleArrays.nanSum(data.values, data.offset, data.size);
    }

    @Override
    public DVector cumsum() {
        for (int i = data.offset + 1; i < data.offset + data.size; i++) {
            data.values[i] += data.values[i - 1];
        }
        return this;
    }

    @Override
    public double prod() {
        return DoubleArrays.prod(data.values, data.offset, data.size);
    }

    @Override
    public double nanprod() {
        return DoubleArrays.nanProd(data.values, data.offset, data.size);
    }

    @Override
    public DVector cumprod() {
        for (int i = data.offset + 1; i < data.offset + data.size; i++) {
            data.values[i] = data.values[i - 1] * data.values[i];
        }
        return this;
    }

    @Override
    public int nancount() {
        return DoubleArrays.nanCount(data.values, data.offset, data.size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(data.values, data.offset, data.size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanMean(data.values, data.offset, data.size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(data.values, data.offset, data.size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanVariance(data.values, data.offset, data.size);
    }

    @Override
    public int argmin() {
        return DoubleArrays.argmin(data.values, data.offset, data.size) - data.offset;
    }

    @Override
    public double min() {
        return DoubleArrays.min(data.values, data.offset, data.size);
    }

    @Override
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.newFrom(data.values, data.offset, data.size + data.offset, f);
            return DVector.wrap(copy);
        }
        for (int i = data.offset; i < data.offset + data.size; i++) {
            data.values[i] = f.applyAsDouble(data.values[i]);
        }
        return this;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[data.size];
            for (int i = 0; i < data.size; i++) {
                copy[i] = f.apply(i, data.values[data.offset + i]);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < data.size; i++) {
            data.values[data.offset + i] = f.apply(i, data.values[data.offset + i]);
        }
        return this;
    }

    @Override
    public DVector sortValues(DoubleComparator comp, AlgebraOption<?>... opts) {
        DoubleArrays.quickSort(data.values, data.offset, data.offset + data.size, comp);
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(data.values).skip(data.offset).limit(data.size);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy() || data.offset != 0) {
            double[] copy = Arrays.copyOfRange(data.values, data.offset, data.size + data.offset);
            return VarDouble.wrapArray(data.size, copy);
        }
        return VarDouble.wrapArray(data.size, data.values);
    }
}
