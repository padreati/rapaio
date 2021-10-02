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
import rapaio.math.linear.VType;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

public class DVectorDense extends AbstractDVector {

    @Serial
    private static final long serialVersionUID = 5763094452899116225L;

    private final int size;
    private final double[] values;

    public DVectorDense(int size, double[] values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public VType type() {
        return VType.DENSE;
    }

    @Override
    public VType innerType() {
        return VType.DENSE;
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
            double[] copy = DoubleArrays.copyByIndex(values, indexes);
            return new DVectorDense(copy.length, copy);
        } else {
            return new DVectorMap(this, indexes);
        }
    }

    public DVectorDense copy() {
        double[] copy = new double[size];
        System.arraycopy(values, 0, copy, 0, size);
        return new DVectorDense(size, copy);
    }

    @Override
    public double get(int i) {
        return values[i];
    }

    @Override
    public void set(int i, double value) {
        values[i] = value;
    }

    @Override
    public void inc(int i, double value) {
        values[i] += value;
    }

    @Override
    public DVectorDense add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.addTo(values, 0, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.add(values, 0, x, size);
        return this;
    }

    @Override
    public DVectorDense add(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.addTo(values, 0, bd.values, 0, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[i] + b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.add(values, 0, bd.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] += b.get(i);
        }
        return this;
    }

    @Override
    public DVector sub(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.subTo(values, 0, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.sub(values, 0, x, size);
        return this;
    }

    @Override
    public DVectorDense sub(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.subTo(values, 0, bd.values, 0, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.sub(values, 0, bd.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector mult(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.multTo(values, 0, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.mult(values, 0, x, size);
        return this;
    }

    @Override
    public DVector mult(DVector b, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.multTo(values, 0, bd.values, 0, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        checkConformance(b);
        if (b instanceof DVectorDense bd) {
            DoubleArrays.mult(values, 0, bd.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] *= b.get(i);
        }
        return this;
    }

    @Override
    public DVector div(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            DoubleArrays.divTo(values, 0, x, copy, 0, copy.length);
            return DVector.wrap(copy);
        }
        DoubleArrays.div(values, 0, x, size);
        return this;
    }

    @Override
    public DVector div(DVector b, AlgebraOption<?>... opts) {
        checkConformance(b);
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            if (b instanceof DVectorDense bd) {
                DoubleArrays.divTo(values, 0, bd.values, 0, copy, 0, size);
            } else {
                for (int i = 0; i < size; i++) {
                    copy[i] = values[i] - b.get(i);
                }
            }
            return DVector.wrap(copy);
        }
        if (b instanceof DVectorDense bd) {
            DoubleArrays.div(values, 0, bd.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector xpay(double a, DVector y, AlgebraOption<?>... opts) {
        checkConformance(y);
        if (AlgebraOptions.from(opts).isCopy()) {
            if (y instanceof DVectorDense yd) {
                double[] copy = new double[size];
                DoubleArrays.xpayTo(values, a, yd.values, copy, 0, size);
                return new DVectorDense(copy.length, copy);
            }
            double[] copy = new double[size];
            for (int i = 0; i < size(); i++) {
                copy[i] = values[i] + a * y.get(i);
            }
            return new DVectorDense(copy.length, copy);
        }
        if (y instanceof DVectorDense yd) {
            DoubleArrays.xpay(values, a, yd.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] += a * y.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense) {
            double s = 0;
            double[] bvalues = ((DVectorDense) b).elements();
            for (int i = 0; i < size; i++) {
                s = Math.fma(values[i], bvalues[i], s);
            }
            return s;
        }
        double s = 0;
        for (int i = 0; i < size; i++) {
            s = Math.fma(values[i], b.get(i), s);
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
                sum += values[i] * m.get(i, j) * y.get(j);
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
                sum += values[i] * m.get(i, j) * values[j];
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
            sum += values[i] * m.get(i) * y.get(i);
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
            sum += values[i] * m.get(i, i) * values[i];
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
            sum += values[i] * m.get(i, i) * y.get(i);
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
            sum += values[i] * m.get(i) * values[i];
        }
        return sum;
    }

    public double pnorm(double p) {
        if (p <= 0) {
            return size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            double max = Double.NaN;
            for (double value : values) {
                if (Double.isNaN(max)) {
                    max = value;
                    continue;
                }
                max = Math.max(max, value);
            }
            return max;
        }
        double s = 0.0;
        for (int i = 0; i < size; i++) {
            s += Math.pow(Math.abs(values[i]), p);
        }
        return Math.pow(s, 1.0 / p);
    }

    @Override
    public double sum() {
        return DoubleArrays.sum(values, 0, size);
    }

    @Override
    public double nansum() {
        return DoubleArrays.nanSum(values, 0, size);
    }

    @Override
    public DVector cumsum() {
        for (int i = 1; i < size(); i++) {
            values[i] += values[i - 1];
        }
        return this;
    }

    @Override
    public double prod() {
        double prod = 1;
        for (int i = 0; i < size(); i++) {
            prod *= values[i];
        }
        return prod;
    }

    @Override
    public double nanprod() {
        double nanprod = 1;
        for (int i = 0; i < size(); i++) {
            double value = values[i];
            if (Double.isNaN(value)) {
                continue;
            }
            nanprod *= value;
        }
        return nanprod;
    }

    @Override
    public DVector cumprod() {
        for (int i = 1; i < size(); i++) {
            values[i] = values[i - 1] * values[i];
        }
        return this;
    }

    @Override
    public int nancount() {
        return DoubleArrays.nanCount(values, 0, size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(values, 0, size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanMean(values, 0, size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(values, 0, size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanVariance(values, 0, size);
    }

    @Override
    public DVector apply(Double2DoubleFunction f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = DoubleArrays.newFrom(values, 0, size, f);
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size; i++) {
            values[i] = f.applyAsDouble(values[i]);
        }
        return this;
    }

    @Override
    public DVector apply(BiFunction<Integer, Double, Double> f, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[size];
            for (int i = 0; i < size; i++) {
                copy[i] = f.apply(i, copy[i]);
            }
            return DVector.wrap(copy);
        }
        for (int i = 0; i < size; i++) {
            values[i] = f.apply(i, values[i]);
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).limit(size);
    }

    @Override
    public VarDouble asVarDouble() {
        return VarDouble.wrapArray(size, values);
    }
}
