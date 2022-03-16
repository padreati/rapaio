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
import jdk.incubator.vector.VectorSpecies;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.storage.DVectorStore;
import rapaio.math.linear.dense.storage.DVectorStoreStride;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;

public class DVectorStride extends AbstractStoreDVector {

    private final DVectorStoreStride store;

    public DVectorStride(int offset, int stride, int size, double[] array) {
        this.store = new DVectorStoreStride(offset, stride, size, array);
    }

    @Override
    public DVectorStore store() {
        return store;
    }

    @Override
    public int size() {
        return store.size;
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
            copyIndexes[i] = store.stride * indexes[i];
        }
        return new DVectorMap(store.offset, copyIndexes, store.array);
    }

    private double[] copyArray() {
        double[] copy = new double[store.size];
        for (int i = 0; i < store.size; i++) {
            copy[i] = store.array[store.offset + store.stride * i];
        }
        return copy;
    }

    @Override
    public DVector copy() {
        return new DVectorStride(0, 1, store.size, copyArray());
    }

    @Override
    public DVector fill(double value) {
        for (int i = store.offset; i < store.offset + store.size; i += store.stride) {
            store.array[i] = value;
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, store.size).mapToDouble(i -> store.array[store.offset + i * store.stride]);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        return VarDouble.wrap(copyArray());
    }
}
