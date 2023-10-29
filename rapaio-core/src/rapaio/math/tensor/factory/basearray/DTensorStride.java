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

package rapaio.math.tensor.factory.basearray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorFactory;
import rapaio.math.tensor.factory.AbstractTensor;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarChunkIterator;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public sealed class DTensorStride extends AbstractTensor<Double, DTensor>
        implements DTensor permits rapaio.math.tensor.factory.parallelarray.DTensorStride {

    protected final StrideLayout layout;
    protected final TensorFactory factory;
    protected final double[] array;

    public DTensorStride(TensorFactory factory, StrideLayout layout, double[] array) {
        this.layout = layout;
        this.factory = factory;
        this.array = array;
    }

    public DTensorStride(TensorFactory factory, Shape shape, int offset, int[] strides, double[] array) {
        this(factory, StrideLayout.of(shape, offset, strides), array);
    }

    public DTensorStride(TensorFactory factory, Shape shape, int offset, Order order, double[] array) {
        this(factory, StrideLayout.ofDense(shape, offset, order), array);
    }

    @Override
    public TensorFactory factory() {
        return factory;
    }

    public double[] array() {
        return array;
    }

    @Override
    public StrideLayout layout() {
        return layout;
    }

    @Override
    public double get(int... indexes) {
        return array[layout.pointer(indexes)];
    }

    @Override
    public void set(double value, int... indexes) {
        array[layout.pointer(indexes)] = value;
    }

    @Override
    public double ptrGet(int ptr) {
        return array[ptr];
    }

    @Override
    public void ptrSet(int ptr, double value) {
        array[ptr] = value;
    }

    @Override
    public DTensor abs_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = Math.abs(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor neg_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = -array[pos];
        }
        return this;
    }

    @Override
    public DTensor log_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.log(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor log1p_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.log1p(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor exp_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.exp(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor expm1_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.expm1(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor sin_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.sin(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor asin_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.sin(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor sinh_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.sinh(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor cos_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.cos(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor acos_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.acos(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor cosh_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.cosh(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor tan_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.tan(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor atan_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.atan(array[pos]);
        }
        return this;
    }

    @Override
    public DTensor tanh_() {
        var it = pointerIterator(Order.A);
        while (it.hasNext()) {
            int pos = it.nextInt();
            array[pos] = (double) Math.tanh(array[pos]);
        }
        return this;
    }

    private void validateSameShape(DTensor tensor) {
        if (!shape().equals(tensor.shape())) {
            throw new IllegalArgumentException("Shapes does not match.");
        }
    }

    @Override
    public DTensor add_(DTensor tensor) {
        validateSameShape(tensor);

        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = pointerIterator(order);
        var refIt = tensor.pointerIterator(order);
        while (it.hasNext()) {
            array[it.nextInt()] += tensor.ptrGetValue(refIt.nextInt());
        }
        return this;
    }

    @Override
    public DTensor sub_(DTensor tensor) {
        validateSameShape(tensor);

        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = pointerIterator(order);
        var refIt = tensor.pointerIterator(order);
        while (it.hasNext()) {
            array[it.nextInt()] -= tensor.ptrGetValue(refIt.nextInt());
        }
        return this;
    }

    @Override
    public DTensor mul_(DTensor tensor) {
        validateSameShape(tensor);

        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = pointerIterator(order);
        var refIt = tensor.pointerIterator(order);
        while (it.hasNext()) {
            array[it.nextInt()] *= tensor.ptrGetValue(refIt.nextInt());
        }
        return this;
    }

    @Override
    public DTensor div_(DTensor tensor) {
        validateSameShape(tensor);

        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = pointerIterator(order);
        var refIt = tensor.pointerIterator(order);
        while (it.hasNext()) {
            array[it.nextInt()] /= tensor.ptrGetValue(refIt.nextInt());
        }
        return this;
    }

    @Override
    public DTensor add_(double value) {
        var it = pointerIterator(layout.storageFastOrder());
        while (it.hasNext()) {
            array[it.nextInt()] += value;
        }
        return this;
    }

    @Override
    public DTensor sub_(double value) {
        var it = pointerIterator(layout.storageFastOrder());
        while (it.hasNext()) {
            array[it.nextInt()] -= value;
        }
        return this;
    }

    @Override
    public DTensor mul_(double value) {
        var it = pointerIterator(layout.storageFastOrder());
        while (it.hasNext()) {
            array[it.nextInt()] *= value;
        }
        return this;
    }

    @Override
    public DTensor div_(double value) {
        var it = pointerIterator(layout.storageFastOrder());
        while (it.hasNext()) {
            array[it.nextInt()] /= value;
        }
        return this;
    }

    @Override
    public DTensor mv(DTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new RuntimeException("Operands are not valid for matrix-vector multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        double[] result = new double[shape().dim(0)];
        var it = pointerIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.pointerIterator(Order.C);
            double sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += ptrGet(it.nextInt()) * tensor.ptrGet(innerIt.nextInt());
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return factory.ofDouble().stride(layout, result);
    }

    @Override
    public DTensor mm(DTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 2 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new RuntimeException("Operands are not valid for matrix-matrix multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        double[] result = new double[shape().dim(0) * tensor.shape().dim(1)];

        List<DTensor> rows = slice(0, 1);
        List<DTensor> cols = tensor.slice(1, 1);

        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < cols.size(); j++) {
                var it1 = rows.get(i).squeeze().iterator();
                var it2 = cols.get(j).squeeze().iterator();
                double sum = 0;
                while (it1.hasNext() && it2.hasNext()) {
                    sum += it1.next() * it2.next();
                }
                result[i * cols.size() + j] = sum;
            }
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0), tensor.shape().dim(1)), 0, Order.C);
        return factory.ofDouble().stride(layout, result);
    }

    @Override
    public DTensor matmul(DTensor tensor) {
        // TODO: implement
        return null;
    }

    @Override
    public Iterator<Double> iterator() {
        return iterator(Order.A);
    }

    @Override
    public Iterator<Double> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(pointerIterator(askOrder), Spliterator.ORDERED), false)
                .map(i -> array[i]).iterator();
    }

    @Override
    public DTensorStride iteratorApply(Order askOrder, IntIntBiFunction<Double> apply) {
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
        DTensor copy = factory.ofDouble().zeros(askShape, askOrder);
        var copyIt = copy.pointerIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSet(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public DTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return factory.ofDouble().stride(compact, array);
        }
        return flatten(askOrder);
    }

    @Override
    public DTensor flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = new double[layout.size()];
        int p = 0;
        var it = chunkIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out[p++] = array[i];
            }
        }
        return factory.ofDouble().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public DTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : factory.ofDouble().stride(layout.squeeze(), array);
    }

    @Override
    public DTensor unsqueeze(int axis) {
        return factory.ofDouble().stride(layout().unsqueeze(axis), array);
    }

    @Override
    public DTensor t() {
        return factory.ofDouble().stride(layout.revert(), array);
    }

    @Override
    public DTensor moveAxis(int src, int dst) {
        return factory.ofDouble().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public DTensor swapAxis(int src, int dst) {
        return factory.ofDouble().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public DTensor truncate(int axis, int start, int end) {
        if (axis < 0 || axis >= layout.rank()) {
            throw new IllegalArgumentException("Axis is out of bounds.");
        }
        int[] newDims = Arrays.copyOf(shape().dims(), shape().rank());
        newDims[axis] = end - start;
        int newOffset = layout().offset() + start * layout.stride(axis);
        int[] newStrides = Arrays.copyOf(layout.strides(), layout.rank());

        StrideLayout copyLayout = StrideLayout.of(Shape.of(newDims), newOffset, newStrides);
        return factory.ofDouble().stride(copyLayout, array);
    }

    @Override
    public List<DTensor> split(int axis, int... indexes) {
        return IntStream
                .range(0, indexes.length)
                .mapToObj(i -> truncate(axis, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)))
                .collect(Collectors.toList());
    }

    @Override
    public DTensor repeat(int axis, int repeat, boolean stack) {
        DTensor[] copies = new DTensor[repeat];
        Arrays.fill(copies, this);
        if (stack) {
            return factory.ofDouble().stack(axis, copies);
        } else {
            return factory.ofDouble().concatenate(axis, copies);
        }
    }

    @Override
    public DTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = factory.ofDouble().zeros(shape(), askOrder);
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
