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

package rapaio.math.tensor;

import java.util.Arrays;

import rapaio.math.tensor.iterators.Chunk;
import rapaio.math.tensor.iterators.TensorChunkIterator;
import rapaio.math.tensor.iterators.TensorColMajorIterator;
import rapaio.math.tensor.iterators.TensorPointerIterator;
import rapaio.math.tensor.iterators.TensorRowMajorIterator;
import rapaio.math.tensor.iterators.TensorStoreMajorIterator;
import rapaio.math.tensor.storage.FStorage;
import rapaio.util.collection.IntArrays;

public final class FTensorStride extends FTensor {

    private final int offset;
    private final int[] strides;
    private final FStorage storage;
    private final int[] strideOrder;

    public FTensorStride(Shape shape, int offset, int[] strides, FStorage storage) {
        super(shape, Type.Stride);
        this.offset = offset;
        this.strides = Arrays.copyOf(strides, strides.length);
        this.storage = storage;
        this.strideOrder = IntArrays.newSeq(0, strides.length);
        IntArrays.quickSort(strideOrder, (i, j) -> Integer.compare(strides[i], strides[j]));
    }

    public float getFloat(int... idxs) {
        return storage.getFloat(pointer(offset, strides, idxs));
    }

    public void setFloat(float value, int... idxs) {
        int pos = pointer(offset, strides, idxs);
        storage.setFloat(pos, value);
    }

    @Override
    public FStorage storage() {
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
    public FTensor reshape(Shape shape, Type type) {
        if (this.shape.size() != shape.size()) {
            throw new IllegalArgumentException("Incompatible shape size.");
        }

        TensorPointerIterator it = new TensorRowMajorIterator(this.shape, offset, strides);

        FTensor copy = FTensor.zeros(shape, type);
        TensorPointerIterator copyIt = copy.pointerIterator(Order.RowMajor);
        while (it.hasNext()) {
            copy.storage().setFloat(copyIt.nextInt(), storage.getFloat(it.nextInt()));
        }
        return copy;
    }

    @Override
    public FTensor t() {
        int[] reversedShape = IntArrays.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        int[] reversedStride = IntArrays.reverse(Arrays.copyOf(strides, strides.length));

        return new FTensorStride(Shape.of(reversedShape), offset, reversedStride, storage);
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
