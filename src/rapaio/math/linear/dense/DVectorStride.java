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

import jdk.incubator.vector.DoubleVector;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.base.AbstractStorageDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;

public class DVectorStride extends AbstractStorageDVector {

    public final int offset;
    public final int stride;
    public final int size;
    public final double[] array;
    private final int[] indexes;

    public DVectorStride(int offset, int stride, int size, double[] array) {
        this.offset = offset;
        this.stride = stride;
        this.size = size;
        this.array = array;

        this.indexes = new int[SPECIES.length()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i * stride;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[indexes.length];
            for (int i = 0; i < indexes.length; i++) {
                copy[i] = get(indexes[i]);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        int[] copyIndexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            copyIndexes[i] = stride * indexes[i];
        }
        return new DVectorMap(offset, copyIndexes, array);
    }

    private double[] copyArray() {
        double[] copy = new double[size];
        for (int i = 0; i < size; i++) {
            copy[i] = array[offset + stride * i];
        }
        return copy;
    }

    @Override
    public DVector copy() {
        return new DVectorStride(0, 1, size, copyArray());
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset + i * stride, indexes, 0);
    }

    @Override
    public double get(int i) {
        return array[offset + i * stride];
    }

    @Override
    public void set(int i, double value) {
        array[offset + i * stride] = value;
    }

    @Override
    public void inc(int i, double value) {
        array[offset + i * stride] += value;
    }

    @Override
    public DVector fill(double value) {
        for (int i = offset; i < offset + size; i += stride) {
            array[i] = value;
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, size).mapToDouble(i -> array[offset + i * stride]);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        return VarDouble.wrap(copyArray());
    }
}
