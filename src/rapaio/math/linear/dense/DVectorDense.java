/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.VType;
import rapaio.math.linear.base.DVectorBase;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;

import java.util.Arrays;
import java.util.stream.DoubleStream;

public class DVectorDense extends DVectorBase {

    private static final long serialVersionUID = 5763094452899116225L;

    public DVectorDense(int len) {
        super(len, new double[len]);
    }

    public DVectorDense(int len, double[] values) {
        super(len, values);
    }

    @Override
    public VType type() {
        return VType.DENSE;
    }

    @Override
    public DVector mapCopy(int... indexes) {
        double[] copy = new double[indexes.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = values[indexes[i]];
        }
        return new DVectorDense(copy.length, copy);
    }

    @Override
    public DVectorBase add(double x) {
        DoubleArrays.add(values, 0, x, size);
        return this;
    }

    @Override
    public DVectorBase add(DVector b) {
        if (b.isDense()) {
            checkConformance(b);
            DoubleArrays.add(values, 0, b.asDense().values, 0, size);
            return this;
        }
        super.add(b);
        return this;
    }

    @Override
    public DVectorBase sub(DVector b) {
        if (b instanceof DVectorDense) {
            checkConformance(b);
            DoubleArrays.sub(values, 0, b.asDense().values, 0, size);
            return this;
        }
        super.sub(b);
        return this;
    }

    @Override
    public DVector mult(double scalar) {
        DoubleArrays.mult(values, 0, scalar, size);
        return this;
    }

    @Override
    public DVector mult(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense) {
            DoubleArrays.mult(values, 0, b.asDense().values, 0, size);
            return this;
        }
        super.mult(b);
        return this;
    }

    @Override
    public DVector div(double scalar) {
        DoubleArrays.div(values, 0, scalar, size);
        return this;
    }

    @Override
    public DVector div(DVector b) {
        checkConformance(b);
        if (b instanceof DVectorDense) {
            DoubleArrays.div(values, 0, b.asDense().values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] /= b.get(i);
        }
        return this;
    }

    @Override
    public DVector caxpy(double a, DVector y) {
        checkConformance(y);
        if (y instanceof DVectorDense) {
            double[] copy = new double[size];
            DoubleArrays.axpyTo(a, values, y.asDense().values, copy, 0, size);
            return new DVectorDense(copy.length, copy);
        }
        DVector copy = new DVectorDense(size, new double[size]);
        for (int i = 0; i < size(); i++) {
            copy.set(i, a * values[i] + y.get(i));
        }
        return copy;
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

    public double norm(double p) {
        if (p <= 0) {
            return size;
        }
        if (p == Double.POSITIVE_INFINITY) {
            double max = Double.NaN;
            for (double value : values) {
                if (Double.isNaN(value))
                    continue;
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

    public DVector normalize(double p) {
        double norm = norm(p);
        if (norm != 0.0)
            mult(1.0 / norm);
        return this;
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
    public DVector apply(Double2DoubleFunction f) {
        for (int i = 0; i < size; i++) {
            values[i] = f.applyAsDouble(values[i]);
        }
        return this;
    }

    public DVectorDense copy() {
        return new DVectorDense(size, Arrays.copyOf(values, size));
    }

    @Override
    public DVector copy(VType type) {
        double[] copy = new double[size];
        System.arraycopy(values, 0, copy, 0, size);
        switch (type) {
            case BASE:
                return new DVectorBase(size, copy);
            case DENSE:
                return new DVectorDense(size, copy);
            default:
                throw new IllegalArgumentException("DVType." + type.name() + " cannot be used to create a copy.");
        }
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).limit(size);
    }

    @Override
    public VarDouble asVarDouble() {
        return VarDouble.wrapArray(size, values);
    }

    public double[] elements() {
        return values;
    }
}
