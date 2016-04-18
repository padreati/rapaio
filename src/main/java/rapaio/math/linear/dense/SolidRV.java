/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.math.linear.RV;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/3/16.
 */
public class SolidRV implements RV {

    private static final long serialVersionUID = 5763094452899116225L;

    /**
     * Builds a new real dense vector of size {@param n} filled with 0.
     *
     * @param n the size of the vector
     * @return vector instance
     */
    public static SolidRV empty(int n) {
        return new SolidRV(n);
    }

    /**
     * Builds a new real dense vector of size equal with row count,
     * filled with values from variable. The variable can have any type,
     * the values are taken by using {@link Var#value(int)} calls.
     *
     * @param v given variable
     * @return new real dense vector
     */
    public static SolidRV from(Var v) {
        SolidRV rdv = new SolidRV(v.rowCount());
        for (int i = 0; i < rdv.count(); i++) {
            rdv.values[i] = v.value(i);
        }
        return rdv;
    }

    /**
     * Builds a new real dense vector of size equal with row count,
     * filled with fill value given as parameter.
     *
     * @param len  size of the vector
     * @param fill fill value
     * @return new real dense vector
     */
    public static SolidRV fill(int len, double fill) {
        SolidRV rdv = empty(len);
        if (fill != 0) {
            Arrays.fill(rdv.values, fill);
        }
        return rdv;
    }

    public static SolidRV copyOf(RV source) {
        SolidRV v = empty(source.count());
        if(source instanceof SolidRV) {
            System.arraycopy(((SolidRV)source).values, 0, v.values, 0, source.count());
        }
        return v;
    }


    private final double[] values;

    private SolidRV(int n) {
        values = new double[n];
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
    public void increment(int i, double value) {
        values[i] += value;
    }

    @Override
    public int count() {
        return values.length;
    }

    public RV dot(double scalar) {
        for (int i = 0; i < values.length; i++) {
            values[i] *= scalar;
        }
        return this;
    }

    public double norm(double p) {
        double s = 0.0;
        for (int i = 0; i < count(); i++) {
            s += Math.pow(values[i], p);
        }
        return Math.pow(s, 1 / p);
    }

    public RV normalize(double p) {
        double norm = norm(p);
        if (norm != 0.0)
            dot(1.0 / norm);
        return this;
    }

    public SolidRV solidCopy() {
        SolidRV copy = SolidRV.empty(count());
        System.arraycopy(values, 0, copy.values, 0, count());
        return copy;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values);
    }
}
