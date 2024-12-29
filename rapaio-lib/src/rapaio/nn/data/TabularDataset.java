/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.nn.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import rapaio.darray.DArray;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.util.collection.IntArrays;

public class TabularDataset implements TensorDataset<TabularDataset> {

    private final TensorManager tm;
    private final int len;
    private final DArray<?>[] arrays;

    public TabularDataset(TensorManager tm, Tensor... tensors) {
        this(tm, Arrays.stream(tensors).map(Tensor::value).toArray(DArray[]::new));
    }

    public TabularDataset(TensorManager tm, DArray<?>... arrays) {
        this.tm = tm;
        this.arrays = arrays;
        this.len = Arrays.stream(arrays).mapToInt(array -> array.dim(0)).min().orElse(0);
        if (len == 0) {
            throw new IllegalArgumentException("Arrays must have at least one element");
        }
    }

    @Override
    public TensorManager tm() {
        return tm;
    }

    @Override
    public int len() {
        return len;
    }

    @Override
    public DArray<?>[] darrays() {
        return arrays;
    }

    @Override
    public DArray<?> darray(int index) {
        return arrays[index];
    }

    @Override
    public Tensor[] tensors() {
        return Arrays.stream(arrays).map(tm::var).toArray(Tensor[]::new);
    }

    @Override
    public Tensor tensor(int index) {
        return tm.var(arrays[index]);
    }

    @Override
    public TabularDataset[] trainTestSplit(double testPercentage) {
        int trainSize = (int) ((1 - testPercentage) * len);

        int[] index = IntArrays.newSeq(len);
        IntArrays.shuffle(index, tm.random());
        int[] trainIndex = Arrays.copyOfRange(index, 0, trainSize);
        int[] testIndex = Arrays.copyOfRange(index, trainSize, len);
        return new TabularDataset[] {
                trainIndex.length == 0 ? null :
                        new TabularDataset(tm, Arrays.stream(arrays).map(a -> a.sel(0, trainIndex)).toArray(DArray[]::new)),
                testIndex.length == 0 ? null :
                        new TabularDataset(tm, Arrays.stream(arrays).map(a -> a.sel(0, testIndex)).toArray(DArray[]::new))
        };
    }

    @Override
    public Iterator<Batch> batchIterator(int batchSize, boolean shuffle, boolean skipLast) {
        return new BatchIterator(tm, batchSize, shuffle, skipLast, tm.random(), this);
    }

    private static class BatchIndexIterator implements Iterator<int[]> {

        private final int[] index;
        private final int batchSize;
        private int[] batchIndices;
        private final int len;
        int pos = 0;

        public BatchIndexIterator(int batchSize, boolean shuffle, boolean skipLast, Random random, DArray<?>[] arrays) {
            this.index = IntArrays.newSeq(Arrays.stream(arrays).mapToInt(array -> array.dim(0)).min().orElse(0));
            if (shuffle) {
                IntArrays.shuffle(this.index, random);
            }
            this.batchSize = batchSize;
            this.len = Math.floorDiv(index.length, batchSize) + ((!skipLast && index.length % batchSize != 0) ? 1 : 0);
        }

        @Override
        public boolean hasNext() {
            return pos < len;
        }

        @Override
        public int[] next() {
            this.batchIndices = Arrays.copyOfRange(index, pos * batchSize, Math.min((pos + 1) * batchSize, index.length));
            pos++;
            return batchIndices;
        }
    }

    private static class BatchIterator implements Iterator<Batch> {

        private final TensorManager tm;
        private final BatchIndexIterator it;
        private final TabularDataset dataset;

        public BatchIterator(TensorManager tm, int batchSize, boolean shuffle, boolean skipLast, Random random, TabularDataset dataset) {
            this.tm = tm;
            this.it = new BatchIndexIterator(batchSize, shuffle, skipLast, random, dataset.darrays());
            this.dataset = dataset;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Batch next() {
            int[] indices = it.next();
            return new Batch(dataset, indices);
        }
    }
}
