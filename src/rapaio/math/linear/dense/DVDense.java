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
import rapaio.math.linear.DV;
import rapaio.math.linear.base.DVBase;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.Double2DoubleFunction;
import rapaio.util.function.Int2DoubleFunction;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.DoubleStream;

public class DVDense extends DVBase {

    private static final long serialVersionUID = 5763094452899116225L;

    /**
     * Builds a new real dense vector of size {@param n} filled with 0.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static DVDense zeros(int n) {
        return new DVDense(n, DoubleArrays.newFill(n, 0));
    }

    /**
     * Builds a new double dense vector of size {@param n} filled with 1.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static DVDense ones(int n) {
        return new DVDense(n, DoubleArrays.newFill(n, 1));
    }

    /**
     * Builds a new real dense vector of <i>len</i> size,
     * filled with <i>fill</i> value given as parameter.
     *
     * @param n    size of the vector
     * @param fill fill value
     * @return new real dense vector
     */
    public static DVDense fill(int n, double fill) {
        return new DVDense(n, DoubleArrays.newFill(n, fill));
    }

    /**
     * Builds a new real dense vector of size equal with row count,
     * filled with values from variable. The variable can have any type,
     * the values are taken by using {@link Var#getDouble(int)} calls.
     *
     * @param v given variable
     * @return new real dense vector
     */
    public static DVDense from(Var v) {
        if (v instanceof VarDouble) {
            VarDouble vd = (VarDouble) v;
            double[] array = vd.elements();
            return new DVDense(vd.rowCount(), array);
        }
        double[] values = new double[v.rowCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = v.getDouble(i);
        }
        return new DVDense(values.length, values);
    }

    /**
     * Builds a new real dense vector which is a solid copy
     * of the given source vector.
     *
     * @param source source vector
     * @return new real dense vector which is a copy of the source vector
     */
    public static DVDense copy(DV source) {
        DVDense v = zeros(source.size());
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
    public static DVDense wrap(double... values) {
        return wrapArray(values.length, values);
    }

    /**
     * Builds a new random vector which wraps a double array.
     * It uses the same reference.
     *
     * @param values referenced array of values
     * @return new real dense vector
     */
    public static DVDense wrapArray(int size, double[] values) {
        Objects.requireNonNull(values);
        return new DVDense(size, values);
    }

    public static DVDense from(int len, Int2DoubleFunction fun) {
        return new DVDense(len, DoubleArrays.newFrom(0, len, fun));
    }

    protected DVDense(int len, double[] values) {
        super(len, values);
    }

    @Override
    public Type type() {
        return Type.DENSE;
    }

    @Override
    public DVBase add(double x) {
        DoubleArrays.add(values, 0, x, size);
        return this;
    }

    @Override
    public DVBase add(DV b) {
        if (b.isDense()) {
            checkConformance(b);
            DoubleArrays.add(values, 0, b.asDense().values, 0, size);
            return this;
        }
        super.add(b);
        return this;
    }

    @Override
    public DVBase sub(DV b) {
        if (b instanceof DVDense) {
            checkConformance(b);
            DoubleArrays.sub(values, 0, b.asDense().values, 0, size);
            return this;
        }
        super.sub(b);
        return this;
    }

    @Override
    public DV mult(double scalar) {
        DoubleArrays.mult(values, 0, scalar, size);
        return this;
    }

    @Override
    public DV mult(DV b) {
        checkConformance(b);
        if (b instanceof DVDense) {
            DoubleArrays.mult(values, 0, b.asDense().values, 0, size);
            return this;
        }
        super.mult(b);
        return this;
    }

    @Override
    public DV div(double scalar) {
        DoubleArrays.div(values, 0, scalar, size);
        return this;
    }

    @Override
    public DV div(DV b) {
        checkConformance(b);
        if (b instanceof DVDense) {
            DoubleArrays.div(values, 0, b.asDense().values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] /= b.get(i);
        }
        return this;
    }

    @Override
    public double dot(DV b) {
        checkConformance(b);
        double s = 0;
        if (b instanceof DVDense) {
            DVDense sb = (DVDense) b;
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

    public DV normalize(double p) {
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
    public DV apply(Double2DoubleFunction f) {
        for (int i = 0; i < size; i++) {
            values[i] = f.applyAsDouble(values[i]);
        }
        return this;
    }

    public DVDense copy() {
        DVDense copy = DVDense.zeros(size);
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
