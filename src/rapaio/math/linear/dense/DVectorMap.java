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
import rapaio.util.collection.DoubleArrays;

public class DVectorMap extends AbstractStorageDVector {

    public final int offset;
    public final int[] indexes;
    public final double[] array;

    public DVectorMap(int offset, int[] indexes, double[] array) {
        this.offset = offset;
        this.indexes = indexes;
        this.array = array;
    }

    @Override
    public int size() {
        return indexes.length;
    }

    @Override
    public DVector map(int[] sel, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[sel.length];
            for (int i = 0; i < sel.length; i++) {
                copy[i] = array[indexes[sel[i]]];
            }
            return new DVectorDense(0, sel.length, copy);
        }
        int[] copyIndexes = new int[sel.length];
        for (int i = 0; i < sel.length; i++) {
            copyIndexes[i] = indexes[sel[i]];
        }
        return new DVectorMap(offset, copyIndexes, array);
    }

    @Override
    public DVector copy() {
        double[] copy = DoubleArrays.copyByIndex(array, offset, indexes);
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(SPECIES, array, offset, indexes, i);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset, indexes, i);
    }

    @Override
    public double get(int i) {
        return array[offset + indexes[i]];
    }

    @Override
    public void set(int i, double value) {
        array[offset + indexes[i]] = value;
    }

    @Override
    public void inc(int i, double value) {
        array[offset + indexes[i]] += value;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.of(indexes).map(i -> i + offset).mapToDouble(i -> array[i]);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        double[] copy = DoubleArrays.copyByIndex(array, offset, indexes);
        return VarDouble.wrap(copy);
    }
}
