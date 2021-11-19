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

import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

public class DVectorDense extends AbstractDVector {

    @Serial
    private static final long serialVersionUID = 5763094452899116225L;

    private final int offset;
    private final int size;
    private final double[] values;

    public DVectorDense(int offset, int size, double[] values) {
        this.offset = offset;
        this.size = size;
        this.values = values;
    }

    public int offset() {
        return offset;
    }

    @Override
    public int size() {
        return size;
    }

    public double[] elements() {
        return values;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.copyByIndex(values, offset, indexes);
            return new DVectorDense(0, copy.length, copy);
        } else {
            return new DVectorMap(this, indexes);
        }
    }

    public DVectorDense copy() {
        double[] copy = new double[size];
        System.arraycopy(values, offset, copy, 0, size);
        return new DVectorDense(0, size, copy);
    }

    @Override
    public double get(int i) {
        return values[offset + i];
    }

    @Override
    public void set(int i, double value) {
        values[offset + i] = value;
    }

    @Override
    public void inc(int i, double value) {
        values[offset + i] += value;
    }

    @Override
    public DVectorDense add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.addTo(values, offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.add(values, offset, x, size);
        return this;
    }

    @Override
    public DVectorDense add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.addTo(values, offset, bd.values, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[offset + i] + b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.add(values, offset, bd.values, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[offset + i] += b.get(i);
        }
        return this;
    }

    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.subTo(values, offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.sub(values, offset, x, size);
        return this;
    }

    @Override
    public DVectorDense sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.subTo(values, offset, bd.values, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[offset + i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.sub(values, offset, bd.values, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[offset + i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector mul(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.multTo(values, offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.mul(values, offset, x, size);
        return this;
    }

    @Override
    public DVector mul(DVector b, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.multTo(values, offset, bd.values, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[offset + i] * b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            DoubleArrays.mul(values, offset, bd.values, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[offset + i] *= b.get(i);
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.divTo(values, offset, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.div(values, offset, x, size);
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.divTo(values, offset, bd.values, bd.offset, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[offset + i] / b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.div(values, offset, bd.values, bd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[offset + i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector addMul(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
            if (y instanceof DVectorDense yd) {
                double[] copy = new double[size];
                DoubleArrays.addMulTo(values, offset, a, yd.values, yd.offset, copy, 0, size);
                return new DVectorDense(0, copy.length, copy);
            }
            double[] copy = new double[size];
            for (int i = 0; i < size(); i++) {
                copy[i] = values[offset + i] + a * y.get(i);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        if (y instanceof DVectorDense yd) {
            DoubleArrays.addMul(values, offset, a, yd.values, yd.offset, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[offset + i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            double s = 0;
            for (int i = 0; i < size; i++) {
                s = Math.fma(values[offset + i], bd.elements()[bd.offset + i], s);
            }
            return s;
        }
        double s = 0;
        for (int i = 0; i < size; i++) {
            s = Math.fma(values[offset + i], b.get(i), s);
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
                sum += values[offset + i] * m.get(i, j) * y.get(j);
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
                sum += values[offset + i] * m.get(i, j) * values[offset + j];
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
            sum += values[offset + i] * m.get(i) * y.get(i);
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
            sum += values[offset + i] * m.get(i, i) * values[offset + i];
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
            sum += values[offset + i] * m.get(i, i) * y.get(i);
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
            sum += values[offset + i] * m.get(i) * values[offset + i];
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
                double value = values[i];
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
            s += Math.pow(Math.abs(values[i]), p);
        }
        return Math.pow(s, 1.0 / p);
    }

    @Override
    public double sum() {
        return DoubleArrays.sum(values, offset, size);
    }

    @Override
    public double nansum() {
        return DoubleArrays.nanSum(values, offset, size);
    }

    @Override
    public DVector cumsum() {
        for (int i = offset + 1; i < offset + size; i++) {
            values[i] += values[i - 1];
        }
        return this;
    }

    @Override
    public double prod() {
        return DoubleArrays.prod(values, offset, size);
    }

    @Override
    public double nanprod() {
        return DoubleArrays.nanProd(values, offset, size);
    }

    @Override
    public DVector cumprod() {
        for (int i = offset + 1; i < offset + size; i++) {
            values[i] = values[i - 1] * values[i];
        }
        return this;
    }

    @Override
    public int nancount() {
        return DoubleArrays.nanCount(values, offset, size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(values, offset, size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanMean(values, offset, size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(values, offset, size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanVariance(values, offset, size);
    }

    @Override
    public int argmin() {
        return DoubleArrays.argmin(values, offset, size) - offset;
    }

    @Override
    public double min() {
        return DoubleArrays.min(values, offset, size);
    }

    @Override
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.newFrom(values, offset, size + offset, f);
            return DVector.wrap(copy);
        }
        for (int i = offset; i < offset + size; i++) {
            values[i] = f.applyAsDouble(values[i]);
        }
        return this;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            for (int i = 0; i < size; i++) {
                copy[i] = f.apply(i, values[offset + i]);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size; i++) {
            values[offset + i] = f.apply(i, values[offset + i]);
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).skip(offset).limit(size);
    }

    @Override
    public VarDouble asVarDouble() {
        if (offset == 0) {
            return VarDouble.wrapArray(size, values);
        } else {
            double[] copy = Arrays.copyOfRange(values, offset, size + offset);
            return VarDouble.wrapArray(size, copy);
        }
    }
}
