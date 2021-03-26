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

package rapaio.math.linear.base;

import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.VType;
import rapaio.math.linear.dense.DVectorDense;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class DVectorBase extends AbstractDVector {

    public static DVectorBase wrap(double... values) {
        return new DVectorBase(values.length, values);
    }

    private static final long serialVersionUID = -6444914455097469657L;

    protected final int size;
    protected final double[] values;

    public DVectorBase(int size) {
        this.size = size;
        this.values = new double[size];
    }

    public DVectorBase(int size, double[] values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public VType type() {
        return VType.BASE;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public DVector mapCopy(int... indexes) {
        double[] copy = new double[indexes.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = values[indexes[i]];
        }
        return DVectorBase.wrap(copy);
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

}
