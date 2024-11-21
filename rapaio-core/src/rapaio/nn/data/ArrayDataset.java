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

import rapaio.narray.NArray;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.util.collection.IntArrays;

public class ArrayDataset {

    private final Random random = new Random();
    private final int len;
    private final NArray<?>[] arrays;

    public ArrayDataset(Tensor... tensors) {
        this(Arrays.stream(tensors).map(Tensor::value).toArray(NArray[]::new));
    }

    public ArrayDataset(NArray<?>... arrays) {
        this.arrays = arrays;
        this.len = Arrays.stream(arrays).mapToInt(array -> array.dim(0)).min().orElse(0);
        if (len == 0) {
            throw new IllegalArgumentException("Arrays must have at least one element");
        }
    }

    public void seed(long seed) {
        random.setSeed(seed);
    }

    public int len() {
        return len;
    }

    public NArray<?>[] arrays() {
        return arrays;
    }

    public NArray<?> array(int index) {
        return arrays[index];
    }

    public Tensor[] tensors(TensorManager tm) {
        return Arrays.stream(arrays).map(tm::var).toArray(Tensor[]::new);
    }

    public Tensor tensor(TensorManager tm, int index) {
        return tm.var(arrays[index]);
    }

    public ArrayDataset[] trainTestSplit(double pTest) {
        int trainSize = (int) ((1 - pTest) * len);

        int[] index = IntArrays.newSeq(len);
        IntArrays.shuffle(index, random);
        int[] trainIndex = Arrays.copyOfRange(index, 0, trainSize);
        int[] testIndex = Arrays.copyOfRange(index, trainSize, len);
        return new ArrayDataset[] {
                trainIndex.length == 0 ? null :
                        new ArrayDataset(Arrays.stream(arrays).map(a -> a.take(0, trainIndex)).toArray(NArray[]::new)),
                testIndex.length == 0 ? null :
                        new ArrayDataset(Arrays.stream(arrays).map(a -> a.take(0, testIndex)).toArray(NArray[]::new))
        };
    }

    public Iterator<int[]> batchIndexIterator(int batchSize) {
        return new BatchIndexIterator(batchSize, true, false, random, arrays);
    }

    public Iterator<int[]> batchIndexIterator(int batchSize, boolean shuffle, boolean skipLast) {
        return new BatchIndexIterator(batchSize, shuffle, skipLast, random, arrays);
    }

    private static class BatchIndexIterator implements Iterator<int[]> {

        private final int[] index;
        private final int batchSize;
        private int[] batchIndices;

        int len;
        int pos = 0;

        public BatchIndexIterator(int batchSize, boolean shuffle, boolean skipLast, Random random, NArray<?>[] arrays) {
            this.index = IntArrays.newSeq(Arrays.stream(arrays).mapToInt(array -> array.dim(0)).min().orElse(0));
            if (shuffle) {
                IntArrays.shuffle(this.index, random);
            }
            this.batchSize = batchSize;
            this.len = Math.floorDiv(index.length, batchSize);
            if (!skipLast && index.length % batchSize != 0) {
                this.len++;
            }
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
}
