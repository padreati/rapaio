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

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.VType;
import rapaio.math.linear.base.AbstractDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;

public class DVectorStride extends AbstractDVector {

    private final int offset;
    private final int size;
    private final int stride;

    private final double[] values;

    public DVectorStride(int offset, int size, int stride, double[] values) {
        this.offset = offset;
        this.size = size;
        this.stride = stride;
        this.values = values;
    }

    @Override
    public VType type() {
        return VType.STRIDE;
    }

    @Override
    public VType innerType() {
        return VType.STRIDE;
    }

    public int offset() {
        return offset;
    }

    @Override
    public int size() {
        return size;
    }

    public int stride() {
        return stride;
    }

    private double[] copyArray() {
        double[] copy = new double[size];
        for (int i = offset; i < size; i += stride) {
            copy[i] = values[i];
        }
        return copy;
    }

    @Override
    public DVectorDense copy() {
        return DVector.wrap(copyArray());
    }

    @Override
    public double get(int i) {
        return values[offset + i * stride];
    }

    @Override
    public void set(int i, double value) {
        values[offset + i * stride] = value;
    }

    @Override
    public void inc(int i, double value) {
        values[offset + i * stride] += value;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, size).mapToDouble(i -> values[offset + i * stride]);
    }

    @Override
    public VarDouble asVarDouble() {
        return VarDouble.wrap(copyArray());
    }
}
