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
import java.util.stream.Collectors;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.iterators.CPointerIterator;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.DenseChunkIterator;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.FPointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.manager.AbstractTensor;
import rapaio.math.tensor.storage.array.DStorageArray;
import rapaio.util.collection.IntArrays;

public class DTensorDense extends AbstractTensor<Double, DStorageArray, DTensor> implements DTensor {

    private final DStorageArray storage;
    private final int[] rowStrides;
    private final int[] colStrides;
    private final int[] strides;
    private final Order order;

    public DTensorDense(TensorManager manager, Shape shape, DStorageArray storage, Order order) {
        super(manager, shape);
        if (!(manager instanceof CpuArraySingleTensorManager)) {
            throw new IllegalArgumentException("Illegal tensor manager type.");
        }
        this.storage = storage;
        this.order = order;

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

        this.strides = switch (order) {
            case C -> rowStrides;
            case F -> colStrides;
            case default -> throw new IllegalArgumentException("Order type is invalid.");
        };
    }

    @Override
    public double get(int... idxs) {
        return storage.get(pointer(0, strides, idxs));
    }

    @Override
    public void set(double value, int... idxs) {
        storage.set(pointer(0, strides, idxs), value);
    }

    @Override
    public DStorageArray storage() {
        return storage;
    }

    @Override
    public PointerIterator pointerIterator(Order askOrder) {
        if (askOrder == Order.S) {
            askOrder = this.order;
        }
        if (askOrder == this.order) {
            return new DensePointerIterator(shape);
        }
        return askOrder == Order.C ?
                new CPointerIterator(shape, 0, strides) :
                new FPointerIterator(shape, 0, strides);
    }

    @Override
    public ChunkIterator chunkIterator(Order askOrder) {
        if (askOrder == order || askOrder == Order.S) {
            return new DenseChunkIterator(shape.size());
        }
        return new StrideChunkIterator(shape, 0, strides, askOrder);
    }

    @Override
    public DTensorDense reshape(Shape shape, Order askOrder) {
        if (shape.size() != this.shape.size()) {
            throw new IllegalArgumentException("Incompatible shape size.");
        }
        return new DTensorDense(manager, shape, storage, askOrder);
    }

    @Override
    public DTensor t() {
        int[] reversedShape = IntArrays.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        return new DTensorDense(manager, Shape.of(reversedShape), storage,
                order == Order.C ? Order.F : Order.C);
    }

    @Override
    public DTensor copy(Order askOrder) {
        if (askOrder == order) {
            return new DTensorDense(manager, shape, storage.copy(), order);
        }
        return DTensor.super.copy(askOrder);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {rank:" + shape().rank() + ",dims:"
                + Arrays.stream(shape().dims()).mapToObj(String::valueOf).collect(Collectors.joining(",", "[", "]"))
                + ",order:" + order.name()
                + "}";
    }
}
