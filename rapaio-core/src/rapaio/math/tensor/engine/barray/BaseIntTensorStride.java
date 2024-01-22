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

package rapaio.math.tensor.engine.barray;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

import static rapaio.util.Hardware.CORES;
import static rapaio.util.Hardware.L2_CACHE_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Statistics;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.engine.AbstractTensor;
import rapaio.math.tensor.engine.varray.VectorizedIntTensorStride;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.LoopIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarLoopIterator;
import rapaio.math.tensor.iterators.StrideLoopDescriptor;
import rapaio.math.tensor.iterators.StrideLoopIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.layout.StrideWrapper;
import rapaio.math.tensor.operator.TensorAssociativeOp;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public sealed class BaseIntTensorStride extends AbstractTensor<Integer> permits VectorizedIntTensorStride {

    protected final StrideLayout layout;
    protected final TensorEngine engine;
    protected final StrideLoopDescriptor loop;

    public BaseIntTensorStride(TensorEngine engine, StrideLayout layout, Storage<Integer> storage) {
        super(storage);
        this.layout = layout;
        this.engine = engine;
        this.loop = StrideLoopDescriptor.of(layout, layout.storageFastOrder());
    }

    @Override
    public DType<Integer> dtype() {
        return DType.INTEGER;
    }

    @Override
    public TensorEngine engine() {
        return engine;
    }

    @Override
    public StrideLayout layout() {
        return layout;
    }

    @Override
    public Tensor<Integer> reshape(Shape askShape, Order askOrder) {
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
        Tensor<Integer> copy = engine.ofInt().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetInt(copyIt.nextInt(), storage.getInt(it.nextInt()));
        }
        return copy;
    }

    @Override
    public Tensor<Integer> t_() {
        return engine.ofInt().stride(layout.revert(), storage);
    }

    @Override
    public Tensor<Integer> ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return engine.ofInt().stride(compact, storage);
        }
        return flatten(askOrder);
    }

    @Override
    public Tensor<Integer> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = engine.ofInt().storage().zeros(layout.size());
        int p = 0;
        var it = loopIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.bound(); i += it.step()) {
                out.setInt(p++, storage.getInt(i));
            }
        }
        return engine.ofInt().stride(StrideLayout.of(Shape.of(layout.size()), 0, new int[] {1}), out);
    }

    @Override
    public Tensor<Integer> squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : engine.ofInt().stride(layout.squeeze(), storage);
    }

    @Override
    public Tensor<Integer> squeeze(int axis) {
        return layout.shape().dim(axis) != 1 ? this : engine.ofInt().stride(layout.squeeze(axis), storage);
    }

    @Override
    public Tensor<Integer> unsqueeze(int axis) {
        return engine.ofInt().stride(layout().unsqueeze(axis), storage);
    }

    @Override
    public Tensor<Integer> moveAxis(int src, int dst) {
        return engine.ofInt().stride(layout.moveAxis(src, dst), storage);
    }

    @Override
    public Tensor<Integer> swapAxis(int src, int dst) {
        return engine.ofInt().stride(layout.swapAxis(src, dst), storage);
    }

    @Override
    public Tensor<Integer> narrow(int axis, boolean keepdim, int start, int end) {
        return engine.ofInt().stride(layout.narrow(axis, keepdim, start, end), storage);
    }

    @Override
    public Tensor<Integer> narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return engine.ofInt().stride(layout.narrowAll(keepdim, starts, ends), storage);
    }

    @Override
    public List<Tensor<Integer>> split(int axis, boolean keepdim, int... indexes) {
        List<Tensor<Integer>> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    @Override
    public List<Tensor<Integer>> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<Tensor<Integer>> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRec(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRec(List<Tensor<Integer>> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
        if (level == indexes.length) {
            return;
        }
        for (int i = 0; i < indexes[level].length; i++) {
            starts[level] = indexes[level][i];
            ends[level] = i < indexes[level].length - 1 ? indexes[level][i + 1] : shape().dim(level);
            if (level == indexes.length - 1) {
                results.add(narrowAll(keepdim, starts, ends));
            } else {
                splitAllRec(results, indexes, keepdim, starts, ends, level + 1);
            }
        }
    }

    @Override
    public Tensor<Integer> repeat(int axis, int repeat, boolean stack) {
        List<Tensor<Integer>> copies = new ArrayList<>(repeat);
        for (int i = 0; i < repeat; i++) {
            copies.add(this);
        }
        if (stack) {
            return engine.stack(axis, copies);
        } else {
            return engine.concat(axis, copies);
        }
    }

    @Override
    public Tensor<Integer> expand(int axis, int dim) {
        if (layout.dim(axis) != 1) {
            throw new IllegalArgumentException(STR."Dimension \{axis} does not have size 1.");
        }
        if (dim < 1) {
            throw new IllegalArgumentException(STR."Dimension of the new axis \{dim} must be positive.");
        }
        int[] newDims = Arrays.copyOf(layout.dims(), layout.dims().length);
        int[] newStrides = Arrays.copyOf(layout.strides(), layout.strides().length);

        newDims[axis] = dim;
        newStrides[axis] = 0;
        return engine.ofInt().stride(StrideLayout.of(Shape.of(newDims), layout.offset(), newStrides), storage);
    }

    @Override
    public Tensor<Integer> permute(int[] dims) {
        return engine.ofInt().stride(layout().permute(dims), storage);
    }

    @Override
    public Tensor<Integer> sort_(int axis, boolean asc) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        var it = new StridePointerIterator(StrideLayout.of(Shape.of(newDims), layout().offset(), newStrides), Order.C, false);
        while (it.hasNext()) {
            StrideWrapper.of(it.nextInt(), selStride, selDim, this).sort(asc);
        }
        return this;
    }

    @Override
    public Tensor<Integer> sort(Order order, int axis, boolean asc) {
        return copy(order).sort_(axis, asc);
    }

    @Override
    public void indirectSort(int[] indices, boolean asc) {
        if (layout.rank() != 1) {
            throw new IllegalArgumentException("Tensor must be flat (have a single dimension).");
        }
        for (int index : indices) {
            if (index < 0 || index >= layout.size()) {
                throw new IllegalArgumentException("Indices must be semi-positive and less than the size of the tensor.");
            }
        }
        StrideWrapper.of(layout.offset(), layout.stride(0), layout.dim(0), this).sortIndirect(indices, asc);
    }

    @Override
    public Integer get(int... indexes) {
        return storage.getInt(layout.pointer(indexes));
    }

    @Override
    public void set(Integer value, int... indexes) {
        storage.setInt(layout.pointer(indexes), value);
    }

    @Override
    public void inc(Integer value, int... indexes) {
        storage.incInt(layout.pointer(indexes), value);
    }

    @Override
    public Integer ptrGet(int ptr) {
        return storage.getInt(ptr);
    }

    @Override
    public void ptrSet(int ptr, Integer value) {
        storage.setInt(ptr, value);
    }

    @Override
    public Iterator<Integer> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(storage::getInt).iterator();
    }

    @Override
    public PointerIterator ptrIterator(Order askOrder) {
        if (layout.isCOrdered() && askOrder != Order.F) {
            return new DensePointerIterator(layout.shape(), layout.offset(), layout.stride(-1));
        }
        if (layout.isFOrdered() && askOrder != Order.C) {
            return new DensePointerIterator(layout.shape(), layout.offset(), layout.stride(0));
        }
        return new StridePointerIterator(layout, askOrder);
    }

    @Override
    public LoopIterator loopIterator(Order askOrder) {
        if (layout.rank() == 0) {
            return new ScalarLoopIterator(layout.offset());
        }
        return new StrideLoopIterator(layout, askOrder);
    }

    @Override
    public BaseIntTensorStride apply_(Order askOrder, IntIntBiFunction<Integer> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.set(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public Tensor<Integer> apply_(Function<Integer, Integer> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.set(ptr, fun.apply(storage.get(ptr)));
        }
        return this;
    }

    @Override
    public Tensor<Integer> fill_(Integer value) {
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                storage.set(p, value);
            }
        }
        return this;
    }

    @Override
    public Tensor<Integer> fillNan_(Integer value) {
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                if (dtype().isNaN(storage.getInt(p))) {
                    storage.setInt(p, value);
                }
            }
        }
        return this;
    }

    @Override
    public Tensor<Integer> clamp_(Integer min, Integer max) {
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                if (!dtype().isNaN(min) && storage.getInt(p) < min) {
                    storage.setInt(p, min);
                }
                if (!dtype().isNaN(max) && storage.getInt(p) > max) {
                    storage.setInt(p, max);
                }
            }
        }
        return this;
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = off + i * loop.step;
                storage.setInt(p, op.applyInt(storage.getInt(p)));
            }
        }
    }

    protected void unaryOp(TensorUnaryOp op) {
        if (op.isFloatOnly() && !dtype().isFloat()) {
            throw new IllegalArgumentException("This operation is available only for floating point tensors.");
        }
        unaryOpStep(op);
    }

    @Override
    public Tensor<Integer> abs_() {
        unaryOp(TensorUnaryOp.ABS);
        return this;
    }

    @Override
    public Tensor<Integer> negate_() {
        unaryOp(TensorUnaryOp.NEG);
        return this;
    }

    @Override
    public Tensor<Integer> log_() {
        unaryOp(TensorUnaryOp.LOG);
        return this;
    }

    @Override
    public Tensor<Integer> log1p_() {
        unaryOp(TensorUnaryOp.LOG1P);
        return this;
    }

    @Override
    public Tensor<Integer> exp_() {
        unaryOp(TensorUnaryOp.EXP);
        return this;
    }

    @Override
    public Tensor<Integer> expm1_() {
        unaryOp(TensorUnaryOp.EXPM1);
        return this;
    }

    @Override
    public Tensor<Integer> sin_() {
        unaryOp(TensorUnaryOp.SIN);
        return this;
    }

    @Override
    public Tensor<Integer> asin_() {
        unaryOp(TensorUnaryOp.ASIN);
        return this;
    }

    @Override
    public Tensor<Integer> sinh_() {
        unaryOp(TensorUnaryOp.SINH);
        return this;
    }

    @Override
    public Tensor<Integer> cos_() {
        unaryOp(TensorUnaryOp.COS);
        return this;
    }

    @Override
    public Tensor<Integer> acos_() {
        unaryOp(TensorUnaryOp.ACOS);
        return this;
    }

    @Override
    public Tensor<Integer> cosh_() {
        unaryOp(TensorUnaryOp.COSH);
        return this;
    }

    @Override
    public Tensor<Integer> tan_() {
        unaryOp(TensorUnaryOp.TAN);
        return this;
    }

    @Override
    public Tensor<Integer> atan_() {
        unaryOp(TensorUnaryOp.ATAN);
        return this;
    }

    @Override
    public Tensor<Integer> tanh_() {
        unaryOp(TensorUnaryOp.TANH);
        return this;
    }

    protected void binaryVectorOp(TensorBinaryOp op, Tensor<Integer> b) {
        if (b.isScalar()) {
            binaryScalarOp(op, b.getInt());
            return;
        }
        if (!shape().equals(b.shape())) {
            throw new IllegalArgumentException("Tensors does not have the same shape.");
        }
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = b.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setInt(next, op.applyInt(storage.getInt(next), b.ptrGet(refIt.nextInt())));
        }
    }

    @Override
    public Tensor<Integer> add_(Tensor<Integer> tensor) {
        binaryVectorOp(TensorBinaryOp.ADD, tensor);
        return this;
    }

    @Override
    public Tensor<Integer> sub_(Tensor<Integer> tensor) {
        binaryVectorOp(TensorBinaryOp.SUB, tensor);
        return this;
    }

    @Override
    public Tensor<Integer> mul_(Tensor<Integer> tensor) {
        binaryVectorOp(TensorBinaryOp.MUL, tensor);
        return this;
    }

    @Override
    public Tensor<Integer> div_(Tensor<Integer> tensor) {
        binaryVectorOp(TensorBinaryOp.DIV, tensor);
        return this;
    }

    void binaryScalarOpStep(TensorBinaryOp op, int value) {
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                storage.setInt(p, op.applyInt(storage.getInt(p), value));
            }
        }
    }

    protected void binaryScalarOp(TensorBinaryOp op, int value) {
        binaryScalarOpStep(op, value);
    }

    @Override
    public BaseIntTensorStride add_(Integer value) {
        binaryScalarOp(TensorBinaryOp.ADD, value);
        return this;
    }

    @Override
    public BaseIntTensorStride sub_(Integer value) {
        binaryScalarOp(TensorBinaryOp.SUB, value);
        return this;
    }

    @Override
    public BaseIntTensorStride mul_(Integer value) {
        binaryScalarOp(TensorBinaryOp.MUL, value);
        return this;
    }

    @Override
    public BaseIntTensorStride div_(Integer value) {
        binaryScalarOp(TensorBinaryOp.DIV, value);
        return this;
    }

    @Override
    public Tensor<Integer> fma_(Integer a, Tensor<Integer> t) {
        if (t.isScalar()) {
            int tVal = t.getInt(0);
            return add_((int) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("Tensors does not have the same shape.");
        }
        int aVal = a;
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = t.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setInt(next, (int) Math.fma(t.ptrGet(refIt.nextInt()), aVal, storage.getInt(next)));
        }
        return this;
    }

    @Override
    public Integer vdot(Tensor<Integer> tensor) {
        return vdot(tensor, 0, shape().dim(0));
    }

    @Override
    public Integer vdot(Tensor<Integer> tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseIntTensorStride dts = (BaseIntTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        int start1 = layout.offset() + start * step1;
        int end1 = layout.offset() + end * step1;
        int start2 = dts.layout.offset() + start * step2;

        int sum = 0;
        for (int i = start1; i < end1; i += step1) {
            sum += (int) (storage.getInt(i) * dts.storage.getInt(start2));
            start2 += step2;
        }
        return sum;
    }

    @Override
    public Tensor<Integer> mv(Tensor<Integer> tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    STR."Operands are not valid for matrix-vector multiplication \{"(m = %s, v = %s).".formatted(shape(),
                            tensor.shape())}");
        }
        var result = engine.ofInt().storage().zeros(shape().dim(0));
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            int sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += (int) (ptrGetInt(it.nextInt()) * tensor.ptrGetInt(innerIt.nextInt()));
            }
            result.setInt(i, sum);
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return engine.ofInt().stride(layout, result);
    }

    @Override
    public Tensor<Integer> mm(Tensor<Integer> t, Order askOrder) {
        if (shape().rank() != 2 || t.shape().rank() != 2 || shape().dim(1) != t.shape().dim(0)) {
            throw new IllegalArgumentException(
                    STR."Operands are not valid for matrix-matrix multiplication \{"(m = %s, v = %s).".formatted(shape(), t.shape())}");
        }
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Illegal askOrder value, must be Order.C or Order.F");
        }
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = t.shape().dim(1);

        var result = engine.ofInt().storage().zeros(m * p);
        var ret = engine.ofInt().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<Tensor<Integer>> rows = chunk(0, false, 1);
        List<Tensor<Integer>> cols = t.chunk(1, false, 1);

        int chunk = (int) floor(sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().byteCount()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) ceil(sqrt(chunk / 4.)) : (int) ceil(sqrt(chunk));

        int iStride = ((StrideLayout) ret.layout()).stride(0);
        int jStride = ((StrideLayout) ret.layout()).stride(1);

        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService service = Executors.newFixedThreadPool(engine.cpuThreads())) {
            for (int r = 0; r < m; r += innerChunk) {
                int rs = r;
                int re = Math.min(m, r + innerChunk);

                futures.add(service.submit(() -> {
                    for (int c = 0; c < p; c += innerChunk) {
                        int ce = Math.min(p, c + innerChunk);

                        for (int k = 0; k < n; k += vectorChunk) {
                            int end = Math.min(n, k + vectorChunk);
                            for (int i = rs; i < re; i++) {
                                var krow = (BaseIntTensorStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    result.incInt(i * iStride + j * jStride, krow.vdot(cols.get(j), k, end));
                                }
                            }
                        }
                    }
                    return null;
                }));
            }

            try {
                for (var future : futures) {
                    future.get();
                }
                service.shutdown();
                service.shutdownNow();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return ret;
    }

    @Override
    public Statistics<Integer> stats() {
        if (!dtype().isFloat()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }

        int size = size();
        int nanSize = 0;
        int mean;
        int nanMean;
        int variance;
        int nanVariance;

        // first pass compute raw mean
        int sum = 0;
        int nanSum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                sum += storage.getInt(p);
                if (!dtype().isNaN(storage.getInt(p))) {
                    nanSum += storage.getInt(p);
                    nanSize++;
                }
            }
        }
        mean = (int) (sum / size);
        nanMean = (int) (nanSum / nanSize);

        // second pass adjustments for mean
        sum = 0;
        nanSum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                sum += (int) (storage.getInt(p) - mean);
                if (!dtype().isNaN(storage.getInt(p))) {
                    nanSum += (int) (storage.getInt(p) - nanMean);
                }
            }
        }
        mean += (int) (sum / size);
        nanMean += (int) (nanSum / nanSize);

        // third pass compute variance
        int sum2 = 0;
        int sum3 = 0;
        int nanSum2 = 0;
        int nanSum3 = 0;

        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                sum2 += (int) ((storage.getInt(p) - mean) * (storage.getInt(p) - mean));
                sum3 += (int) (storage.getInt(p) - mean);
                if (!dtype().isNaN(storage.getInt(p))) {
                    nanSum2 += (int) ((storage.getInt(p) - nanMean) * (storage.getInt(p) - nanMean));
                    nanSum3 += (int) (storage.getInt(p) - nanMean);
                }
            }
        }
        variance = (int) ((sum2 - (sum3 * sum3) / size) / size);
        nanVariance = (int) ((nanSum2 - (nanSum3 * nanSum3) / nanSize) / nanSize);

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
    }

    @Override
    public Integer sum() {
        return associativeOp(TensorAssociativeOp.ADD);
    }

    @Override
    public Tensor<Integer> sum(Order order, int axis) {
        return associativeOpNarrow(TensorAssociativeOp.ADD, order, axis);
    }

    @Override
    public Integer nanSum() {
        return nanAssociativeOp(TensorAssociativeOp.ADD);
    }

    @Override
    public Tensor<Integer> nanSum(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorAssociativeOp.ADD, order, axis);
    }

    @Override
    public Integer prod() {
        return associativeOp(TensorAssociativeOp.MUL);
    }

    @Override
    public Tensor<Integer> prod(Order order, int axis) {
        return associativeOpNarrow(TensorAssociativeOp.MUL, order, axis);
    }

    @Override
    public Integer nanProd() {
        return nanAssociativeOp(TensorAssociativeOp.MUL);
    }

    @Override
    public Tensor<Integer> nanProd(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorAssociativeOp.MUL, order, axis);
    }

    @Override
    public Integer max() {
        return associativeOp(TensorAssociativeOp.MAX);
    }

    @Override
    public Tensor<Integer> max(Order order, int axis) {
        return associativeOpNarrow(TensorAssociativeOp.MAX, order, axis);
    }

    @Override
    public Integer nanMax() {
        return nanAssociativeOp(TensorAssociativeOp.MAX);
    }

    @Override
    public Tensor<Integer> nanMax(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorAssociativeOp.MAX, order, axis);
    }

    @Override
    public Integer min() {
        return associativeOp(TensorAssociativeOp.MIN);
    }

    @Override
    public Tensor<Integer> min(Order order, int axis) {
        return associativeOpNarrow(TensorAssociativeOp.MIN, order, axis);
    }

    @Override
    public Integer nanMin() {
        return nanAssociativeOp(TensorAssociativeOp.MIN);
    }

    @Override
    public Tensor<Integer> nanMin(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorAssociativeOp.MIN, order, axis);
    }

    @Override
    public int nanCount() {
        int count = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                if (dtype().isNaN(storage.getInt(p))) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public int zeroCount() {
        int count = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                if (storage.getInt(p) == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    protected int associativeOp(TensorAssociativeOp op) {
        int agg = op.initialInt();
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                agg = op.applyInt(agg, storage.getInt(p));
            }
        }
        return agg;
    }

    protected int nanAssociativeOp(TensorAssociativeOp op) {
        int aggregate = op.initialInt();
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                if (!dtype().isNaN(storage.getInt(p))) {
                    aggregate = op.applyInt(aggregate, storage.getInt(p));
                }
            }
        }
        return aggregate;
    }

    protected Tensor<Integer> associativeOpNarrow(TensorAssociativeOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Integer> res = engine.ofInt().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            int value = StrideWrapper.of(ptr, selStride, selDim, this).aggregate(op.initialInt(), op::applyInt);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    protected Tensor<Integer> nanAssociativeOpNarrow(TensorAssociativeOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Integer> res = engine.ofInt().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            int value = StrideWrapper.of(ptr, selStride, selDim, this).nanAggregate(DType.INTEGER, op.initialInt(), op::applyInt);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    @Override
    public Tensor<Integer> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = engine.ofInt().storage().zeros(size());
        var dst = engine.ofInt().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(Storage<Integer> copy, Order askOrder) {
        var loop = StrideLoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                copy.setInt(last++, storage.getInt(p));
            }
        }
    }

    @Override
    public Tensor<Integer> copyTo(Tensor<Integer> to, Order askOrder) {

        if (to instanceof BaseIntTensorStride dst) {

            int limit = Math.floorDiv(L2_CACHE_SIZE, dtype().byteCount() * 2 * engine.cpuThreads() * 8);

            if (layout.size() > limit) {

                int[] slices = Arrays.copyOf(layout.dims(), layout.rank());
                int size = IntArrays.prod(slices, 0, slices.length);
                while (size > limit) {
                    int axis = IntArrays.argmax(slices, 0, slices.length);
                    size = size * (slices[axis] / 2) / slices[axis];
                    slices[axis] = slices[axis] / 2;
                }

                int[] lens = new int[slices.length];
                for (int i = 0; i < lens.length; i++) {
                    lens[i] = Math.ceilDiv(layout().dim(i), slices[i]);
                }

                int[] starts = new int[slices.length];
                int[] ends = new int[slices.length];

                try (ExecutorService executor = Executors.newFixedThreadPool(engine.cpuThreads())) {
                    List<Future<?>> futures = new ArrayList<>();
                    Stack<Integer> stack = new Stack<>();
                    boolean loop = true;
                    while (!stack.isEmpty() || loop) {
                        int level = stack.size();
                        if (loop) {
                            if (level == slices.length) {
                                int[] ss = IntArrays.copy(starts);
                                int[] es = IntArrays.copy(ends);
                                futures.add(executor.submit(() -> {
                                    BaseIntTensorStride s = (BaseIntTensorStride) this.narrowAll(false, ss, es);
                                    BaseIntTensorStride d = (BaseIntTensorStride) dst.narrowAll(false, ss, es);
                                    directCopyTo(s, d, askOrder);
                                    return null;
                                }));
                                loop = false;
                            } else {
                                stack.push(0);
                                starts[level] = 0;
                                ends[level] = Math.min(slices[level], layout.dim(level));
                            }
                        } else {
                            int last = stack.pop();
                            if (last != lens[level - 1] - 1) {
                                last++;
                                stack.push(last);
                                starts[level - 1] = last * slices[level - 1];
                                ends[level - 1] = Math.min((last + 1) * slices[level - 1], layout.dim(level - 1));
                                loop = true;
                            }
                        }
                    }
                    for (var future : futures) {
                        future.get();
                    }
                    executor.shutdown();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

                return dst;
            }

            directCopyTo(this, dst, askOrder);
            return dst;
        }
        throw new IllegalArgumentException("Not implemented for this tensor type.");
    }

    private void directCopyTo(BaseIntTensorStride src, BaseIntTensorStride dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                dst.storage.setInt(it2.nextInt(), src.storage.getInt(p));
            }
        }
    }

    public int[] toArray() {
        if (shape().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        int[] copy = new int[size()];
        int pos = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                copy[pos++] = storage.getInt(p);
            }
        }
        return copy;
    }

    public int[] asArray() {
        if (shape().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        // TODO FIX
//        if (storage.size() == shape().dim(0) && layout.stride(0) == 1) {
//            return storage.;
//        }
        return toArray();
    }
}
