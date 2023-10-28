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

package rapaio.math.tensor.engine.basearray;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.TensorOps;
import rapaio.math.tensor.engine.AbstractTensor;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarChunkIterator;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.operators.TensorBinaryOp;
import rapaio.math.tensor.operators.TensorUnaryOp;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public sealed class FTensorStride extends AbstractTensor<Float, FTensor>
        implements FTensor permits rapaio.math.tensor.engine.parallelarray.FTensorStride {

    protected final StrideLayout layout;
    protected final TensorEngine manager;
    protected final float[] array;

    public FTensorStride(TensorEngine manager, StrideLayout layout, float[] array) {
        this.layout = layout;
        this.manager = manager;
        this.array = array;
    }

    public FTensorStride(TensorEngine manager, Shape shape, int offset, int[] strides, float[] array) {
        this(manager, StrideLayout.of(shape, offset, strides), array);
    }

    public FTensorStride(TensorEngine manager, Shape shape, int offset, Order order, float[] array) {
        this(manager, StrideLayout.ofDense(shape, offset, order), array);
    }

    @Override
    public TensorEngine manager() {
        return manager;
    }

    public float[] array() {
        return array;
    }

    @Override
    public StrideLayout layout() {
        return layout;
    }

    @Override
    public float get(int... indexes) {
        return array[layout.pointer(indexes)];
    }

    @Override
    public void set(float value, int... indexes) {
        array[layout.pointer(indexes)] = value;
    }

    @Override
    public float ptrGet(int ptr) {
        return array[ptr];
    }

    @Override
    public void ptrSet(int ptr, float value) {
        array[ptr] = value;
    }

    protected FTensor unaryOp(TensorUnaryOp op) {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = op.apply(array[pos]);
        }
        return this;
    }

    @Override
    public FTensor abs() {
        return unaryOp(TensorOps.ABS);
    }

    @Override
    public FTensor neg() {
        return unaryOp(TensorOps.NEG);
    }

    @Override
    public FTensor log() {
        return unaryOp(TensorOps.LOG);
    }

    @Override
    public FTensor log1p() {
        return unaryOp(TensorOps.LOG1P);
    }

    @Override
    public FTensor exp() {
        return unaryOp(TensorOps.EXP);
    }

    @Override
    public FTensor expm1() {
        return unaryOp(TensorOps.EXPM1);
    }

    @Override
    public FTensor sin() {
        return unaryOp(TensorOps.SIN);
    }

    @Override
    public FTensor asin() {
        return unaryOp(TensorOps.ASIN);
    }

    @Override
    public FTensor sinh() {
        return unaryOp(TensorOps.SINH);
    }

    @Override
    public FTensor cos() {
        return unaryOp(TensorOps.COS);
    }

    @Override
    public FTensor acos() {
        return unaryOp(TensorOps.ACOS);
    }

    @Override
    public FTensor cosh() {
        return unaryOp(TensorOps.COSH);
    }

    @Override
    public FTensor tan() {
        return unaryOp(TensorOps.TAN);
    }

    @Override
    public FTensor atan() {
        return unaryOp(TensorOps.ATAN);
    }

    @Override
    public FTensor tanh() {
        return unaryOp(TensorOps.TANH);
    }

    @Override
    public FTensor binaryOp(FTensor tensor, TensorBinaryOp op) {
        if (shape().equals(tensor.shape())) {
            // shapes are compatible, apply op
            var order = layout.storageFastOrder();
            order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

            var it = pointerIterator(order);
            var refIt = tensor.pointerIterator(order);
            while (it.hasNext()) {
                int pos = it.nextInt();
                array[pos] = op.apply(array[pos], tensor.ptrGetValue(refIt.nextInt()));
            }
            return this;
        }
        throw new IllegalArgumentException("Shapes does not match.");
    }

    @Override
    public FTensor add(FTensor tensor) {
        return binaryOp(tensor, TensorOps.ADD);
    }

    @Override
    public FTensor sub(FTensor tensor) {
        return binaryOp(tensor, TensorOps.SUB);
    }

    @Override
    public FTensor mul(FTensor tensor) {
        return binaryOp(tensor, TensorOps.MUL);
    }

    @Override
    public FTensor div(FTensor tensor) {
        return binaryOp(tensor, TensorOps.DIV);
    }

    @Override
    public Iterator<Float> iterator() {
        return iterator(Order.A);
    }

    @Override
    public Iterator<Float> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(pointerIterator(askOrder), Spliterator.ORDERED), false)
                .map(i -> array[i]).iterator();
    }

    @Override
    public FTensorStride iteratorApply(Order askOrder, IntIntBiFunction<Float> apply) {
        var it = pointerIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            array[p] = apply.applyAsInt(i++, p);
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
    public FTensor reshape(Shape askShape, Order askOrder) {
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
        FTensor copy = manager.ofFloat().zeros(askShape, askOrder);
        var copyIt = copy.pointerIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSet(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public FTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return manager.ofFloat().stride(compact, array);
        }
        return flatten(askOrder);
    }

    @Override
    public FTensor flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = new float[layout.size()];
        int p = 0;
        var it = chunkIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out[p++] = array[i];
            }
        }
        return manager.ofFloat().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public FTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : manager.ofFloat().stride(layout.squeeze(), array);
    }

    @Override
    public FTensor t() {
        return manager.ofFloat().stride(layout.revert(), array);
    }

    @Override
    public FTensor moveAxis(int src, int dst) {
        return manager.ofFloat().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public FTensor swapAxis(int src, int dst) {
        return manager.ofFloat().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public FTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = manager.ofFloat().zeros(shape(), askOrder);
        var it1 = chunkIterator(askOrder);
        var it2 = copy.pointerIterator(askOrder);
        while (it1.hasNext()) {
            int pointer = it1.nextInt();
            for (int i = pointer; i < pointer + it1.loopBound(); i += it1.loopStep()) {
                copy.ptrSet(it2.nextInt(), ptrGet(i));
            }
        }
        return copy;
    }
}
