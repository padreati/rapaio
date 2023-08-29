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

package rapaio.math.tensor.engine.base;

import jdk.incubator.vector.DoubleVector;
import rapaio.math.tensor.*;
import rapaio.math.tensor.engine.AbstractTensor;
import rapaio.math.tensor.iterators.*;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.operators.TensorBinaryOp;
import rapaio.math.tensor.operators.TensorUnaryOp;
import rapaio.math.tensor.storage.DStorage;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public sealed class DTensorStride extends AbstractTensor<Double, DoubleVector, DStorage, DTensor>
        implements DTensor permits rapaio.math.tensor.engine.parallel.DTensorStride {

    protected final StrideLayout layout;
    protected final TensorEngine manager;
    protected final DStorage storage;

    public DTensorStride(TensorEngine manager, Shape shape, int offset, int[] strides, DStorage storage) {
        this(manager, StrideLayout.of(shape, offset, strides), storage);
    }

    public DTensorStride(TensorEngine manager, StrideLayout layout, DStorage storage) {
        this.layout = layout;
        this.manager = manager;
        this.storage = storage;
    }

    public DTensorStride(TensorEngine manager, Shape shape, int offset, Order order, DStorage storage) {
        this.layout = StrideLayout.ofDense(shape, offset, order);
        this.manager = manager;
        this.storage = storage;
    }

    @Override
    public TensorEngine manager() {
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
    public DTensor unaryOp(TensorUnaryOp op) {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            storage.apply(op, it.nextInt());
        }
        return this;
    }

    @Override
    public DTensor abs() {
        return unaryOp(TensorOps.ABS);
    }

    @Override
    public DTensor neg() {
        return unaryOp(TensorOps.NEG);
    }

    @Override
    public DTensor log() {
        return unaryOp(TensorOps.LOG);
    }

    @Override
    public DTensor log1p() {
        return unaryOp(TensorOps.LOG1P);
    }

    @Override
    public DTensor exp() {
        return unaryOp(TensorOps.EXP);
    }

    @Override
    public DTensor expm1() {
        return unaryOp(TensorOps.EXPM1);
    }

    @Override
    public DTensor sin() {
        return unaryOp(TensorOps.SIN);
    }

    @Override
    public DTensor asin() {
        return unaryOp(TensorOps.ASIN);
    }

    @Override
    public DTensor sinh() {
        return unaryOp(TensorOps.SINH);
    }

    @Override
    public DTensor cos() {
        return unaryOp(TensorOps.COS);
    }

    @Override
    public DTensor acos() {
        return unaryOp(TensorOps.ACOS);
    }

    @Override
    public DTensor cosh() {
        return unaryOp(TensorOps.COSH);
    }

    @Override
    public DTensor tan() {
        return unaryOp(TensorOps.TAN);
    }

    @Override
    public DTensor atan() {
        return unaryOp(TensorOps.ATAN);
    }

    @Override
    public DTensor tanh() {
        return unaryOp(TensorOps.TANH);
    }

    @Override
    public DTensor binaryOp(DTensor tensor, TensorBinaryOp op) {
        if (shape().equals(tensor.shape())) {
            // shapes are compatible, apply op
            var order = layout.storageFastOrder();
            order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

            var it = pointerIterator(order);
            var refIt = tensor.pointerIterator(order);
            while (it.hasNext()) {
                storage.apply(op, it.nextInt(), tensor.storage().get(refIt.nextInt()));
            }
            return this;
        }
        throw new IllegalArgumentException("Shapes does not match.");
    }

    @Override
    public DTensor add(DTensor tensor) {
        return binaryOp(tensor, TensorOps.ADD);
    }

    @Override
    public DTensor sub(DTensor tensor) {
        return binaryOp(tensor, TensorOps.SUB);
    }

    @Override
    public DTensor mul(DTensor tensor) {
        return binaryOp(tensor, TensorOps.MUL);
    }

    @Override
    public DTensor div(DTensor tensor) {
        return binaryOp(tensor, TensorOps.DIV);
    }

    @Override
    public Iterator<Double> iterator() {
        return iterator(Order.A);
    }

    @Override
    public Iterator<Double> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(pointerIterator(askOrder), Spliterator.ORDERED), false)
                .map(storage::get).iterator();
    }

    @Override
    public DTensor iteratorApply(Order askOrder, IntIntBiFunction<Double> apply) {
        var it = pointerIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.set(p, apply.applyAsInt(i++, p));
        }
        return this;
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
        askOrder = Order.autoFC(askOrder);
        var out = manager.storageFactory().ofDoubleZeros(layout.size());
        int p = 0;
        var it = chunkIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out.set(p++, storage.get(i));
            }
        }
        return manager.ofDoubleStride(Shape.of(layout.size()), 0, new int[]{1}, out);
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
        askOrder = Order.autoFC(askOrder);

        var copy = manager.ofDoubleZeros(shape(), askOrder);
        var it1 = chunkIterator(askOrder);
        var it2 = copy.pointerIterator(askOrder);
        while (it1.hasNext()) {
            int pointer = it1.nextInt();
            for (int i = pointer; i < pointer + it1.loopBound(); i += it1.loopStep()) {
                copy.storage().set(it2.nextInt(), storage().get(i));
            }
        }
        return copy;
    }
}
