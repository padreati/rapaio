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

import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.AbstractDVector;
import rapaio.math.linear.DVector;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.function.DoubleDoubleFunction;
import rapaio.util.function.IntDoubleFunction;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.DoubleStream;

public class SolidDVector extends AbstractDVector {

    private static final long serialVersionUID = 5763094452899116225L;

    /**
     * Builds a new real dense vector of size {@param n} filled with 0.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static SolidDVector zeros(int n) {
        return new SolidDVector(n, DoubleArrays.newFill(n, 0));
    }

    /**
     * Builds a new double dense vector of size {@param n} filled with 1.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static SolidDVector ones(int n) {
        return new SolidDVector(n, DoubleArrays.newFill(n, 1));
    }

    /**
     * Builds a new real dense vector of <i>len</i> size,
     * filled with <i>fill</i> value given as parameter.
     *
     * @param n    size of the vector
     * @param fill fill value
     * @return new real dense vector
     */
    public static SolidDVector fill(int n, double fill) {
        return new SolidDVector(n, DoubleArrays.newFill(n, fill));
    }

    /**
     * Builds a new real dense vector of size equal with row count,
     * filled with values from variable. The variable can have any type,
     * the values are taken by using {@link Var#getDouble(int)} calls.
     *
     * @param v given variable
     * @return new real dense vector
     */
    public static SolidDVector from(Var v) {
        if (v instanceof VarDouble) {
            VarDouble vd = (VarDouble) v;
            double[] array = vd.elements();
            return new SolidDVector(vd.rowCount(), array);
        }
        double[] values = new double[v.rowCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = v.getDouble(i);
        }
        return new SolidDVector(values.length, values);
    }

    /**
     * Builds a new real dense vector which is a solid copy
     * of the given source vector.
     *
     * @param source source vector
     * @return new real dense vector which is a copy of the source vector
     */
    public static SolidDVector copy(DVector source) {
        SolidDVector v = zeros(source.size());
        if (source instanceof SolidDVector) {
            System.arraycopy(((SolidDVector) source).values, 0, v.values, 0, source.size());
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
    public static SolidDVector wrap(double... values) {
        return wrapArray(values.length, values);
    }

    /**
     * Builds a new random vector which wraps a double array.
     * It uses the same reference.
     *
     * @param values referenced array of values
     * @return new real dense vector
     */
    public static SolidDVector wrapArray(int size, double[] values) {
        Objects.requireNonNull(values);
        return new SolidDVector(size, values);
    }

    public static SolidDVector from(int len, IntDoubleFunction fun) {
        return new SolidDVector(len, DoubleArrays.newFrom(0, len, fun));
    }

    // internals

    private final int size;
    private final double[] values;

    private SolidDVector(int size, double[] values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public int size() {
        return size;
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
    public DVector plus(double x) {
        DoubleArrays.plus(values, x, 0, size);
        return this;
    }

    @Override
    public DVector plus(DVector b) {
        checkConformance(b);

        if (b instanceof SolidDVector) {
            SolidDVector sb = (SolidDVector) b;
            DoubleArrays.plus(values, sb.values, 0, size);
            return this;
        }

        for (int i = 0; i < size(); i++) {
            values[i] += b.get(i);
        }
        return this;
    }

    @Override
    public DVector minus(double x) {
        DoubleArrays.minus(values, x, 0, size);
        return this;
    }

    @Override
    public DVector minus(DVector b) {
        checkConformance(b);
        if (b instanceof SolidDVector) {
            SolidDVector sb = (SolidDVector) b;
            DoubleArrays.minus(values, sb.values, 0, size);
            return this;
        }

        for (int i = 0; i < size(); i++) {
            values[i] -= b.get(i);
        }
        return this;
    }

    @Override
    public DVector times(double scalar) {
        DoubleArrays.times(values, scalar, 0, size);
        return this;
    }

    @Override
    public DVector times(DVector b) {
        checkConformance(b);
        if (b instanceof SolidDVector) {
            SolidDVector sb = (SolidDVector) b;
            DoubleArrays.times(values, sb.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] *= b.get(i);
        }
        return this;
    }


    @Override
    public DVector div(double scalar) {
        DoubleArrays.div(values, scalar, 0, size);
        return this;
    }

    @Override
    public DVector div(DVector b) {
        checkConformance(b);
        if (b instanceof SolidDVector) {
            SolidDVector sb = (SolidDVector) b;
            DoubleArrays.div(values, sb.values, 0, size);
            return this;
        }
        for (int i = 0; i < size; i++) {
            values[i] /= b.get(i);
        }
        return this;
    }

    @Override
    public double dot(DVector b) {
        checkConformance(b);
        double s = 0;
        if (b instanceof SolidDVector) {
            SolidDVector sb = (SolidDVector) b;
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
            times(1.0 / norm);
        return this;
    }

    @Override
    public double sum() {
        return DoubleArrays.sum(values, 0, size);
    }

    @Override
    public double nansum() {
        return DoubleArrays.nansum(values, 0, size);
    }

    @Override
    public int nancount() {
        return DoubleArrays.nancount(values, 0, size);
    }

    @Override
    public double mean() {
        return DoubleArrays.mean(values, 0, size);
    }

    @Override
    public double nanmean() {
        return DoubleArrays.nanmean(values, 0, size);
    }

    @Override
    public double variance() {
        return DoubleArrays.variance(values, 0, size);
    }

    @Override
    public double nanvariance() {
        return DoubleArrays.nanvariance(values, 0, size);
    }

    @Override
    public DVector apply(DoubleDoubleFunction f) {
        for (int i = 0; i < size; i++) {
            values[i] = f.applyAsDouble(values[i]);
        }
        return this;
    }

    public SolidDVector copy() {
        SolidDVector copy = SolidDVector.zeros(size);
        System.arraycopy(values, 0, copy.values, 0, size);
        return copy;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).limit(size);
    }

    public String toSummary() {
        return SolidFrame.byVars(VarDouble.wrapArray(size, values)).toFullContent();
    }

    @Override
    public VarDouble asVarDouble() {
        return VarDouble.wrapArray(size, values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RV[").append(size()).append("]{");
        for (int i = 0; i < size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(values[i]);
            if (i > 10) {
                sb.append("...");
                break;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
