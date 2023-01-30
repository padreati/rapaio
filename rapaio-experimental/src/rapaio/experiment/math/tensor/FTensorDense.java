/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.math.tensor;

import java.util.Arrays;
import java.util.NoSuchElementException;

import rapaio.experiment.math.tensor.iterators.Chunk;
import rapaio.experiment.math.tensor.iterators.TensorChunkIterator;
import rapaio.experiment.math.tensor.iterators.TensorColMajorIterator;
import rapaio.experiment.math.tensor.iterators.TensorPointerIterator;
import rapaio.experiment.math.tensor.iterators.TensorRowMajorIterator;
import rapaio.experiment.math.tensor.storage.FStorage;
import rapaio.util.collection.IntArrays;

public class FTensorDense extends FTensor {

    private final FStorage storage;
    private final int[] rowStrides;
    private final int[] colStrides;
    private final int[] strides;

    public FTensorDense(Shape shape, Type type, FStorage storage) {
        super(shape, type);
        this.storage = storage;

        // build offsets
        this.rowStrides = IntArrays.newFill(shape.rank(), 1);
        this.colStrides = IntArrays.newFill(shape.rank(), 1);
        for (int i = 1; i < rowStrides.length; i++) {
            for (int j = 0; j < i; j++) {
                rowStrides[j] *= shape.dim(i);
            }
        }
        for (int i = colStrides.length - 2; i >= 0; i--) {
            for (int j = colStrides.length - 1; j > i; j--) {
                colStrides[j] *= shape.dim(i);
            }
        }
        this.strides = type == Type.DenseRow ? rowStrides : colStrides;
    }

    @Override
    public float getFloat(int... idxs) {
        return storage.getFloat(pointer(0, strides, idxs));
    }

    @Override
    public void setFloat(float value, int... idxs) {
        storage.setFloat(pointer(0, strides, idxs), value);
    }

    @Override
    public FStorage storage() {
        return storage;
    }

    @Override
    public TensorPointerIterator pointerIterator(Order order) {
        if (order == Order.Storage) {
            order = type == Type.DenseRow ? Order.RowMajor : Order.ColMajor;
        }
        return order == Order.RowMajor ?
                (type == Type.DenseRow ? new FastIterator(shape) : new TensorRowMajorIterator(shape, 0, strides)) :
                (type == Type.DenseCol ? new FastIterator(shape) : new TensorColMajorIterator(shape, 0, strides));
    }

    @Override
    public TensorChunkIterator chunkIterator() {
        return new DenseStorageChunkIterator(shape.size());
    }

    @Override
    public FTensorDense reshape(Shape shape, Type type) {
        if (shape.size() != this.shape.size()) {
            throw new IllegalArgumentException("Incompatible shape size.");
        }
        return new FTensorDense(shape, this.type, storage);
    }

    @Override
    public FTensor t() {
        int[] reversedShape = IntArrays.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        return new FTensorDense(Shape.of(reversedShape),
                type == Type.DenseRow ? Type.DenseCol : Type.DenseRow, storage);
    }

    static class FastIterator implements TensorPointerIterator {

        private final int size;

        private int position = 0;

        public FastIterator(Shape shape) {
            this.size = shape.size();
        }

        @Override
        public int nextInt() {
            if (position >= size) {
                throw new NoSuchElementException();
            }
            position++;
            return position - 1;
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public int getPosition() {
            return position - 1;
        }
    }

    static final class DenseStorageChunkIterator implements TensorChunkIterator {

        private final int size;
        private int pos = 0;

        public DenseStorageChunkIterator(int size) {
            this.size = size;
        }

        @Override
        public int chunkCount() {
            return 1;
        }

        @Override
        public int minChunkSize() {
            return size;
        }

        @Override
        public boolean hasNext() {
            return pos < 1;
        }

        @Override
        public Chunk next() {
            if (pos >= 1) {
                throw new NoSuchElementException();
            }
            pos++;
            return new Chunk(0, size);
        }
    }
}
