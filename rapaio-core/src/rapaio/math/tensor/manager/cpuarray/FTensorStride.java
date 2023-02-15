/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.manager.cpuarray;

import java.util.Arrays;

import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.FPointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.CPointerIterator;
import rapaio.math.tensor.iterators.ScalarChunkIterator;
import rapaio.math.tensor.iterators.SPointerIterator;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.manager.AbstractTensor;
import rapaio.math.tensor.storage.array.FStorageArray;
import rapaio.util.collection.IntArrays;

public final class FTensorStride extends AbstractTensor<Float, FStorageArray, FTensor> implements FTensor {

    private final int offset;
    private final int[] strides;
    private final FStorageArray storage;
    private final int[] strideOrder;

    public FTensorStride(TensorManager manager, Shape shape, int offset, int[] strides, FStorageArray storage) {
        super(manager, shape);
        if (!(manager instanceof CpuArraySingleTensorManager)) {
            throw new IllegalArgumentException("Illegal tensor manager type.");
        }
        this.offset = offset;
        this.strides = Arrays.copyOf(strides, strides.length);
        this.storage = storage;
        this.strideOrder = IntArrays.newSeq(0, strides.length);
        IntArrays.quickSort(strideOrder, (i, j) -> Integer.compare(strides[i], strides[j]));
    }

    public float get(int... idxs) {
        return storage.get(pointer(offset, strides, idxs));
    }

    public void set(float value, int... idxs) {
        int pos = pointer(offset, strides, idxs);
        storage.set(pos, value);
    }

    @Override
    public FStorageArray storage() {
        return storage;
    }

    @Override
    public PointerIterator pointerIterator(Order askOrder) {
        return switch (askOrder) {
            case C -> new CPointerIterator(shape, offset, strides);
            case F -> new FPointerIterator(shape, offset, strides);
            case S -> new SPointerIterator(shape, offset, strides);
        };
    }

    @Override
    public ChunkIterator chunkIterator(Order askOrder) {
        if (shape.rank() == 0) {
            return new ScalarChunkIterator(offset);
        }
        return new StrideChunkIterator(shape, offset, strides, askOrder);
    }

    @Override
    public FTensor reshape(Shape shape, Order askOrder) {
        if (this.shape.size() != shape.size()) {
            throw new IllegalArgumentException("Incompatible shape size.");
        }

        var it = new CPointerIterator(this.shape, offset, strides);

        FTensor copy = manager.floatZeros(shape, askOrder);
        var copyIt = copy.pointerIterator(Order.C);
        while (it.hasNext()) {
            copy.storage().set(copyIt.nextInt(), storage.get(it.nextInt()));
        }
        return copy;
    }

    @Override
    public FTensor t() {
        int[] reversedShape = IntArrays.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        int[] reversedStride = IntArrays.reverse(Arrays.copyOf(strides, strides.length));

        return new FTensorStride(manager, Shape.of(reversedShape), offset, reversedStride, storage);
    }
}
