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

package rapaio.math.tensor.manager.cpusingle;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarChunkIterator;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.manager.AbstractTensor;
import rapaio.math.tensor.storage.DStorage;
import rapaio.util.collection.IntArrays;

public sealed class DTensorStride extends AbstractTensor<Double, DStorage, DTensor> implements DTensor permits rapaio.math.tensor.manager.cpuparallel.DTensorStride {

    protected final StrideLayout layout;
    protected final TensorManager manager;
    protected final DStorage storage;

    public DTensorStride(TensorManager manager, Shape shape, int offset, int[] strides, DStorage storage) {
        this(manager, StrideLayout.of(shape, offset, strides), storage);
    }

    public DTensorStride(TensorManager manager, StrideLayout layout, DStorage storage) {
        this.layout = layout;
        this.manager = manager;
        this.storage = storage;
    }

    public DTensorStride(TensorManager manager, Shape shape, int offset, Order order, DStorage storage) {
        this.layout = StrideLayout.ofDense(shape, offset, order);
        this.manager = manager;
        this.storage = storage;
    }

    @Override
    public TensorManager manager() {
        return manager;
    }

    @Override
    public DStorage storage() {
        return storage;
    }

    @Override
    public StrideLayout layout() {
        return layout;
    }

    @Override
    public double get(int... idxs) {
        return storage.get(layout.pointer(idxs));
    }

    @Override
    public void set(double value, int... idxs) {
        storage.set(layout.pointer(idxs), value);
    }

    @Override
    public PointerIterator pointerIterator(Order askOrder) {
        if (layout.isCOrdered() && askOrder == Order.C) {
            return new DensePointerIterator(layout.shape(), layout.offset(), layout.stride(-1));
        }
        if (layout.isFOrdered() && askOrder == Order.F) {
            return new DensePointerIterator(layout.shape(), layout.offset(), layout.stride(0));
        }
        return new StridePointerIterator(layout, askOrder);
    }

    @Override
    public ChunkIterator chunkIterator(Order askOrder) {
        if (layout.rank() == 0) {
            return new ScalarChunkIterator(layout.offset());
        }
        return new StrideChunkIterator(layout, askOrder);
    }

    @Override
    public DTensor reshape(Shape askShape, Order askOrder) {
        if (layout.shape().size() != askShape.size()) {
            throw new IllegalArgumentException("Incompatible shape size.");
        }

        Order cmpOrder = askOrder == Order.C ? Order.C : Order.F;
        var baseLayout = layout.computeFortranLayout(cmpOrder, true);
        var compact = StrideLayout.ofDense(shape(), layout.offset(), cmpOrder).computeFortranLayout(cmpOrder, true);

        if (baseLayout.equals(compact)) {
            // we can create a view over tensor
            int newOffset = layout.offset();
            int[] newStrides = new int[askShape.rank()];
            for (int i = 0; i < askShape.rank(); i++) {
                int[] ione = new int[askShape.rank()];
                ione[i] = 1;
                int pos2 = askShape.position(cmpOrder, ione);
                int[] v1 = layout.shape().index(cmpOrder, pos2);
                int pointer2 = layout.pointer(v1);
                newStrides[i] = pointer2 - newOffset;
            }
            if (askOrder == Order.C) {
                IntArrays.reverse(newStrides);
            }
        }
        var it = new StridePointerIterator(layout, askOrder);
        DTensor copy = manager.ofDoubleZeros(askShape, askOrder);
        var copyIt = copy.pointerIterator(Order.C);
        while (it.hasNext()) {
            copy.storage().set(copyIt.nextInt(), storage.get(it.nextInt()));
        }
        return copy;
    }

    @Override
    public DTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return manager.ofDoubleStride(compact, storage);
        }
        return flatten(askOrder);
    }

    @Override
    public DTensor flatten(Order askOrder) {
        if (!(askOrder == Order.C || askOrder == Order.F)) {
            throw new IllegalArgumentException("Ask order is invalid.");
        }
        var out = manager.storageFactory().ofDoubleZeros(layout.shape().size());
        int p = 0;
        var it = chunkIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out.set(p++, storage().get(i));
            }
        }

        Shape askShape = Shape.of(layout.shape().size());
        StrideLayout askLayout = StrideLayout.ofDense(askShape, layout.offset(), askOrder);
        return manager.ofDoubleStride(askLayout, out);
    }

    @Override
    public DTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : manager.ofDoubleStride(layout.squeeze(), storage);
    }

    @Override
    public DTensor t() {
        return manager.ofDoubleStride(layout.revert(), storage);
    }

    @Override
    public DTensor moveAxis(int src, int dst) {
        return manager.ofDoubleStride(layout.moveAxis(src, dst), storage());
    }

    @Override
    public DTensor swapAxis(int src, int dst) {
        return manager.ofDoubleStride(layout.swapAxis(src, dst), storage());
    }

    @Override
    public DTensor copy(Order askOrder) {
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Order argument is invalid.");
        }
        var out = manager.storageFactory().ofDoubleZeros(layout.shape().size());
        var it = chunkIterator(askOrder);
        int p = 0;
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out.set(p++, storage().get(i));
            }
        }
        return manager.ofDoubleStride(StrideLayout.ofDense(layout.shape(), layout.offset(), askOrder), out);
    }
}
