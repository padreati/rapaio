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
import rapaio.math.linear.dense.storage.DVectorStoreMap;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;

public class DVectorMap extends AbstractStoreDVector {

    private final DVectorStoreMap store;

    public DVectorMap(int offset, int[] indexes, double[] array) {
        this.store = new DVectorStoreMap(offset, indexes, array);
    }

    @Override
    public DVectorStore store() {
        return store;
    }

    @Override
    public int size() {
        return store.indexes.length;
    }

    @Override
    public DVector map(int[] sel, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[sel.length];
            for (int i = 0; i < sel.length; i++) {
                copy[i] = store.array[store.indexes[sel[i]]];
            }
            return new DVectorDense(0, sel.length, copy);
        }
        int[] copyIndexes = new int[sel.length];
        for (int i = 0; i < sel.length; i++) {
            copyIndexes[i] = store.indexes[sel[i]];
        }
        return new DVectorMap(store.offset, copyIndexes, store.array);
    }

    @Override
    public DVector add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            super.add(x, opts);
        }
        store.add(x);
        return this;
    }

    @Override
    public DVector copy() {
        double[] copy = DoubleArrays.copyByIndex(store.array, store.offset, store.indexes);
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.of(store.indexes).map(i -> i + store.offset).mapToDouble(i -> store.array[i]);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        double[] copy = DoubleArrays.copyByIndex(store.array, store.offset, store.indexes);
        return VarDouble.wrap(copy);
    }
}
