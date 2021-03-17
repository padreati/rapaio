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

import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.DVectorBase;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.Int2DoubleFunction;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.DoubleStream;

public class DVectorDense extends DVectorBase {

    private static final long serialVersionUID = 5763094452899116225L;

    /**
     * Builds a new real dense vector of size {@param n} filled with 0.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static DVectorDense zeros(int n) {
        return new DVectorDense(n, DoubleArrays.newFill(n, 0));
    }

    /**
     * Builds a new double dense vector of size {@param n} filled with 1.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static DVectorDense ones(int n) {
        return new DVectorDense(n, DoubleArrays.newFill(n, 1));
    }

    /**
     * Builds a new real dense vector of <i>len</i> size,
     * filled with <i>fill</i> value given as parameter.
     *
     * @param n    size of the vector
     * @param fill fill value
     * @return new real dense vector
     */
    public static DVectorDense fill(int n, double fill) {
        return new DVectorDense(n, DoubleArrays.newFill(n, fill));
    }

    /**
     * Builds a new real dense vector of size equal with row count,
     * filled with values from variable. The variable can have any type,
     * the values are taken by using {@link Var#getDouble(int)} calls.
     *
     * @param v given variable
     * @return new real dense vector
     */
    public static DVectorDense from(Var v) {
        if (v instanceof VarDouble) {
            VarDouble vd = (VarDouble) v;
            double[] array = vd.elements();
            return new DVectorDense(vd.size(), array);
        }
        double[] values = new double[v.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = v.getDouble(i);
        }
        return new DVectorDense(values.length, values);
    }

    /**
     * Builds a new real dense vector which is a solid copy
     * of the given source vector.
     *
     * @param source source vector
     * @return new real dense vector which is a copy of the source vector
     */
    public static DVectorDense copy(DVector source) {
        DVectorDense v = zeros(source.size());
        if (source.isDense()) {
            System.arraycopy(source.asDense().values, 0, v.values, 0, source.size());
            return v;
        }
        for (int i = 0; i < v.values.length; i++) {
            v.values[i] = source.get(i);
        }
        return v;
    }

    /**
     * Builds a new random vector which wraps a double array.
     * It uses the same reference.
     *
     * @param values referenced array of values
     * @return new real dense vector
     */
    public static DVectorDense wrap(double... values) {
        return wrapArray(values.length, values);
    }

    /**
     * Builds a new random vector which wraps a double array.
     * It uses the same reference.
     *
     * @param values referenced array of values
     * @return new real dense vector
     */
    public static DVectorDense wrapArray(int size, double[] values) {
        Objects.requireNonNull(values);
        return new DVectorDense(size, values);
    }

    public static DVectorDense from(int len, Int2DoubleFunction fun) {
        return new DVectorDense(len, DoubleArrays.newFrom(0, len, fun));
    }

    protected DVectorDense(int len, double[] values) {
        super(len, values);
    }

    @Override
    public Type type() {
        return Type.DENSE;
    }

    @Override
    public DVector mapCopy(int... indexes) {
        double[] copy = new double[indexes.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = values[indexes[i]];
        }
        return DVectorDense.wrap(copy);
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
            return DVectorDense.wrap(copy);
        }
        DVector copy = DVectorDense.wrap(new double[size]);
        for (int i = 0; i < size(); i++) {
            copy.set(i, a * values[i] + y.get(i));
        }
        return copy;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        double s = 0;
        if (b instanceof DVectorDense) {
            DVectorDense sb = (DVectorDense) b;
            for (int i = 0; i < size; i++) {
                s = Math.fma(values[i], sb.values[i], s);
            }
            return s;
        }
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
        DVectorDense copy = DVectorDense.zeros(size);
        System.arraycopy(values, 0, copy.values, 0, size);
        return copy;
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
