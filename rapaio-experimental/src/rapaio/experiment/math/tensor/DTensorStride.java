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

import rapaio.experiment.math.tensor.iterators.Chunk;
import rapaio.experiment.math.tensor.iterators.TensorChunkIterator;
import rapaio.experiment.math.tensor.iterators.TensorColMajorIterator;
import rapaio.experiment.math.tensor.iterators.TensorPointerIterator;
import rapaio.experiment.math.tensor.iterators.TensorRowMajorIterator;
import rapaio.experiment.math.tensor.iterators.TensorStoreMajorIterator;
import rapaio.experiment.math.tensor.storage.DStorage;
import rapaio.util.collection.IntArrays;

public final class DTensorStride extends DTensor {

    private final int offset;
    private final int[] strides;
    private final DStorage storage;
    private final int[] strideOrder;

    public DTensorStride(Shape shape, int offset, int[] strides, DStorage storage) {
        super(shape, Type.Stride);
        this.offset = offset;
        this.strides = Arrays.copyOf(strides, strides.length);
        this.storage = storage;
        this.strideOrder = IntArrays.newSeq(0, strides.length);
        IntArrays.quickSort(strideOrder, (i, j) -> Integer.compare(strides[i], strides[j]));
    }

    public double getDouble(int... idxs) {
        return storage.getDouble(pointer(offset, strides, idxs));
    }

    public void setDouble(double value, int... idxs) {
        int pos = pointer(offset, strides, idxs);
        storage.setDouble(pos, value);
    }

    @Override
    public DStorage storage() {
        return storage;
    }

    @Override
    public TensorPointerIterator pointerIterator(Order order) {
        return switch (order) {
            case RowMajor -> new TensorRowMajorIterator(shape, offset, strides);
            case ColMajor -> new TensorColMajorIterator(shape, offset, strides);
            case Storage -> new TensorStoreMajorIterator(shape, offset, strides);
        };
    }

    @Override
    public TensorChunkIterator chunkIterator() {
        return null;
    }

    @Override
    public DTensor reshape(Shape shape, Type type) {
        if (this.shape.size() != shape.size()) {
            throw new IllegalArgumentException("Incompatible shape size.");
        }

        TensorPointerIterator it = new TensorRowMajorIterator(this.shape, offset, strides);

        DTensor copy = zeros(shape, type);
        TensorPointerIterator copyIt = copy.pointerIterator(Order.RowMajor);
        while (it.hasNext()) {
            copy.storage().setDouble(copyIt.nextInt(), storage.getDouble(it.nextInt()));
        }
        return copy;
    }

    @Override
    public DTensor t() {
        int[] reversedShape = IntArrays.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        int[] reversedStride = IntArrays.reverse(Arrays.copyOf(strides, strides.length));

        return new DTensorStride(Shape.of(reversedShape), offset, reversedStride, storage);
    }

    static final class ChunkIterator implements TensorChunkIterator {

        private final Shape shape;
        private final int offset;
        private final int[] strides;

        public ChunkIterator(Shape shape, int offset, int[] strides) {

            int[] storeOrder = StrideAlgebra.computeStorageOrder(shape.dims(), strides);

            this.shape = shape.perm(storeOrder);
            this.offset = offset;
            this.strides = IntArrays.newPermutation(strides, storeOrder);

            int len = 1;
            for(int i=0; i<shape.rank(); i++) {
                if(strides[i]==len) {

                }
            }
        }

        @Override
        public int chunkCount() {
            return 0;
        }

        @Override
        public int minChunkSize() {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Chunk next() {
            return null;
        }
    }
}
