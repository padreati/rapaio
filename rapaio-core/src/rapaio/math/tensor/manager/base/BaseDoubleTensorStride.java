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

package rapaio.math.tensor.manager.base;

import static rapaio.util.Hardware.CORES;
import static rapaio.util.Hardware.L2_CACHE_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.iterators.StrideLoopDescriptor;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.layout.StrideWrapper;
import rapaio.math.tensor.manager.AbstractStrideTensor;
import rapaio.math.tensor.operator.TensorAssociativeOp;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.math.tensor.storage.array.DoubleArrayStorage;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public class BaseDoubleTensorStride extends AbstractStrideTensor<Double> {

    public BaseDoubleTensorStride(TensorManager engine, StrideLayout layout, Storage<Double> storage) {
        super(engine, layout, storage);
    }

    @Override
    public DType<Double> dtype() {
        return DType.DOUBLE;
    }

    @Override
    public Tensor<Double> reshape(Shape askShape, Order askOrder) {
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
        Tensor<Double> copy = engine.zeros(dtype(), askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetDouble(copyIt.nextInt(), storage.getDouble(it.nextInt()));
        }
        return copy;
    }

    @Override
    public Tensor<Double> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var result = engine.ofDouble().zeros(Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var it = loopIterator(askOrder);
        while (it.hasNext()) {
            int off = it.nextInt();
            for (int i = 0, p = off; i < it.size(); i++, p += it.step()) {
                out.setDouble(ptr++, storage.getDouble(p));
            }
        }
        return result;
    }

    @Override
    public Double get(int... indexes) {
        return storage.getDouble(layout.pointer(indexes));
    }

    @Override
    public void set(Double value, int... indexes) {
        storage.setDouble(layout.pointer(indexes), value);
    }

    @Override
    public void inc(Double value, int... indexes) {
        storage.incDouble(layout.pointer(indexes), value);
    }

    @Override
    public Double ptrGet(int ptr) {
        return storage.getDouble(ptr);
    }

    @Override
    public void ptrSet(int ptr, Double value) {
        storage.setDouble(ptr, value);
    }

    @Override
    public BaseDoubleTensorStride apply_(Order askOrder, IntIntBiFunction<Double> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setDouble(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public Tensor<Double> apply_(Function<Double, Double> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setDouble(ptr, fun.apply(storage.getDouble(ptr)));
        }
        return this;
    }

    @Override
    public Tensor<Double> fill_(Double value) {
        for (int offset : loop.offsets) {
            if (loop.step == 1) {
                storage.fillDouble(value, offset, loop.size);
            } else {
                for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                    storage.setDouble(p, value);
                }
            }
        }
        return this;
    }

    @Override
    public Tensor<Double> fillNan_(Double value) {
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (dtype().isNaN(storage.getDouble(p))) {
                    storage.setDouble(p, value);
                }
            }
        }
        return this;
    }

    @Override
    public Tensor<Double> clamp_(Double min, Double max) {
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (!dtype().isNaN(min) && storage.getDouble(p) < min) {
                    storage.setDouble(p, min);
                }
                if (!dtype().isNaN(max) && storage.getDouble(p) > max) {
                    storage.setDouble(p, max);
                }
            }
        }
        return this;
    }

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            for (int i = off; i < loop.size + off; i++) {
                storage.setDouble(i, op.applyDouble(storage.getDouble(i)));
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            for (int i = 0, p = off; i < loop.size; i++, p += loop.step) {
                storage.setDouble(p, op.applyDouble(storage.getDouble(p)));
            }
        }
    }

    @Override
    protected void unaryOp(TensorUnaryOp op) {
        if (op.floatingPointOnly() && !dtype().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point tensors.");
        }
        if (loop.step == 1) {
            unaryOpUnit(op);
        } else {
            unaryOpStep(op);
        }
    }

    @Override
    protected void binaryVectorOp(TensorBinaryOp op, Tensor<Double> b) {
        if (b.isScalar()) {
            binaryScalarOp(op, b.getDouble());
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
            storage.setDouble(next, op.applyDouble(storage.getDouble(next), b.ptrGet(refIt.nextInt())));
        }
    }

    void binaryScalarOpStep(TensorBinaryOp op, double value) {
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                storage.setDouble(p, op.applyDouble(storage.getDouble(p), value));
            }
        }
    }

    @Override
    protected void binaryScalarOp(TensorBinaryOp op, Double value) {
        binaryScalarOpStep(op, value);
    }

    @Override
    public Tensor<Double> fma_(Double a, Tensor<Double> t) {
        if (t.isScalar()) {
            double tVal = t.getDouble(0);
            return add_((double) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("Tensors does not have the same shape.");
        }
        double aVal = a;
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = t.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setDouble(next, (double) Math.fma(t.ptrGet(refIt.nextInt()), aVal, storage.getDouble(next)));
        }
        return this;
    }

    @Override
    public Double vdot(Tensor<Double> tensor) {
        return vdot(tensor, 0, shape().dim(0));
    }

    @Override
    public Double vdot(Tensor<Double> tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseDoubleTensorStride dts = (BaseDoubleTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        int start1 = layout.offset() + start * step1;
        int end1 = layout.offset() + end * step1;
        int start2 = dts.layout.offset() + start * step2;

        double sum = 0;
        for (int i = start1; i < end1; i += step1) {
            sum += (double) (storage.getDouble(i) * dts.storage.getDouble(start2));
            start2 += step2;
        }
        return sum;
    }

    @Override
    public Tensor<Double> vpadCopy(int before, int after) {
        if (!isVector()) {
            throw new IllegalArgumentException("This operation is available only for vectors.");
        }
        Storage<Double> newStorage = engine.storage().ofDouble().zeros(before + dim(0) + after);
        var loop = loopIterator();
        while (loop.hasNext()) {
            int offset = loop.next();
            for (int i = 0; i < loop.size(); i++) {
                newStorage.setDouble(before + i, ptrGetDouble(offset + i * loop.step()));
            }
        }
        return engine.ofDouble().stride(Shape.of(before + dim(0) + after), Order.C, newStorage);
    }

    @Override
    public Tensor<Double> mv(Tensor<Double> tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    STR."Operands are not valid for matrix-vector multiplication \{"(m = %s, v = %s).".formatted(shape(),
                            tensor.shape())}");
        }
        var result = engine.ofDouble().storage().zeros(shape().dim(0));
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            double sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += (double) (ptrGetDouble(it.nextInt()) * tensor.ptrGetDouble(innerIt.nextInt()));
            }
            result.setDouble(i, sum);
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return engine.ofDouble().stride(layout, result);
    }

    @Override
    public Tensor<Double> mm(Tensor<Double> t, Order askOrder) {
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

        var result = engine.ofDouble().storage().zeros(m * p);
        var ret = engine.ofDouble().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<Tensor<Double>> rows = chunk(0, false, 1);
        List<Tensor<Double>> cols = t.chunk(1, false, 1);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().byteCount()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) Math.ceil(Math.sqrt(chunk / 4.)) : (int) Math.ceil(Math.sqrt(chunk));

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
                                var krow = (BaseDoubleTensorStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    result.incDouble(i * iStride + j * jStride, krow.vdot(cols.get(j), k, end));
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
    public Tensor<Double> scatter() {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Scatter matrix can be computed only for matrices.");
        }
        Tensor<Double> scatter = engine.ofDouble().zeros(Shape.of(dim(1), dim(1)));
        Tensor<Double> mean = engine.ofDouble().zeros(Shape.of(dim(1)));
        for (int i = 0; i < dim(1); i++) {
            mean.setDouble((double) take(1, i).mean(), i);
        }
        for (int k = 0; k < dim(0); k++) {
            Tensor<Double> row = take(0, k).squeeze(0).sub(mean);
            for (int i = 0; i < row.size(); i++) {
                for (int j = 0; j < row.size(); j++) {
                    scatter.incDouble((double) (row.getDouble(i) * row.getDouble(j)), i, j);
                }
            }
        }
        return scatter;
    }

    @Override
    public Double norm(Double pow) {
        if (pow < 0) {
            throw new IllegalArgumentException(STR."Norm power p=\{pow} must have a value greater than 0.");
        }
        if (dtype().castValue(1).equals(pow)) {
            return abs().sum();
        }
        if (dtype().castValue(2).equals(pow)) {
            return (double) Math.sqrt(sqr().sum());
        }

        double sum = (double) 0;
        var it = loopIterator();
        while (it.hasNext()) {
            int offset = it.next();
            for (int i = 0, p = offset; i < it.size(); i++, p += it.step()) {
                double value = (double) Math.abs(storage.getDouble(p));
                sum += (double) Math.pow(value, pow);
            }
        }
        return (double) Math.pow(sum, 1. / pow);
    }

    @Override
    public Tensor<Double> normalize_(Double pow) {
        return div_(norm(pow));
    }

    @Override
    protected Tensor<Double> alongAxisOperation(Order order, int axis, Function<Tensor<Double>, Double> op) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Double> res = engine.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            var stride = engine.ofDouble().stride(StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride}), storage);
            res.ptrSet(resIt.next(), op.apply(stride));
        }
        return res;
    }

    @Override
    public Double mean() {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size();
        // first pass compute raw mean
        double sum = 0;
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += storage.getDouble(off + i * loop.step);
            }
        }
        double mean = (double) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (double) (storage.getDouble(off + i * loop.step) - mean);
            }
        }
        return (double) (mean + sum / size);
    }

    @Override
    public Double nanMean() {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size() - nanCount();
        // first pass compute raw mean
        double sum = nanSum();

        double mean = (double) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double v = storage.getDouble(off + i * loop.step);
                if (dtype().isNaN(v)) {
                    continue;
                }
                sum += (double) (v - mean);
            }
        }
        return (double) (mean + sum / size);
    }

    @Override
    public Double var() {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size();
        // first pass compute raw mean
        double sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += storage.getDouble(offset + i * loop.step);
            }
        }
        double mean = (double) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (double) (storage.getDouble(offset + i * loop.step) - mean);
            }
        }
        mean += (double) (sum / size);
        // third pass compute variance
        double sum2 = 0;
        double sum3 = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                sum2 += (double) ((storage.getDouble(p) - mean) * (storage.getDouble(p) - mean));
                sum3 += (double) (storage.getDouble(p) - mean);
            }
        }
        return (double) ((sum2 - (sum3 * sum3) / size) / size);
    }

    @Override
    public Double varc(int ddof) {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size();
        // first pass compute raw mean
        double sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += storage.getDouble(offset + i * loop.step);
            }
        }
        double mean = (double) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (double) (storage.getDouble(offset + i * loop.step) - mean);
            }
        }
        mean += (double) (sum / size);
        // third pass compute variance
        double sum2 = 0;
        double sum3 = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                sum2 += (double) ((storage.getDouble(p) - mean) * (storage.getDouble(p) - mean));
                sum3 += (double) (storage.getDouble(p) - mean);
            }
        }
        return (double) ((sum2 - (sum3 * sum3) / (size - ddof)) / (size - ddof));
    }

    @Override
    public int argmax(Order order) {
        int argmax = -1;
        double argvalue = TensorAssociativeOp.MAX.initDouble();
        var i = 0;
        var it = loopIterator(order);
        while (it.hasNext()) {
            int offset = it.next();
            for (int j = 0; j < loop.size; j++) {
                double value = storage.getDouble(offset + j * loop.step);
                if (value > argvalue) {
                    argvalue = value;
                    argmax = i;
                }
                i++;
            }
        }
        return argmax;
    }

    @Override
    public int argmin(Order order) {
        int argmin = -1;
        double argvalue = TensorAssociativeOp.MIN.initDouble();
        var i = 0;
        var it = loopIterator(order);
        while (it.hasNext()) {
            int offset = it.next();
            for (int j = 0; j < loop.size; j++) {
                double value = storage.getDouble(offset + j * loop.step);
                if (value < argvalue) {
                    argvalue = value;
                    argmin = i;
                }
                i++;
            }
        }
        return argmin;
    }

    @Override
    public int nanCount() {
        int count = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (dtype().isNaN(storage.getDouble(p))) {
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
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (storage.getDouble(p) == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected Double associativeOp(TensorAssociativeOp op) {
        double agg = op.initDouble();
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                agg = op.aggDouble(agg, storage.getDouble(p));
            }
        }
        return agg;
    }

    @Override
    protected Double nanAssociativeOp(TensorAssociativeOp op) {
        double aggregate = op.initDouble();
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (!dtype().isNaN(storage.getDouble(p))) {
                    aggregate = op.aggDouble(aggregate, storage.getDouble(p));
                }
            }
        }
        return aggregate;
    }

    @Override
    protected Tensor<Double> associativeOpNarrow(TensorAssociativeOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Double> res = engine.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            double value = StrideWrapper.of(ptr, selStride, selDim, this).aggregate(op.initDouble(), op::aggDouble);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    @Override
    protected Tensor<Double> nanAssociativeOpNarrow(TensorAssociativeOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Double> res = engine.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            double value = StrideWrapper.of(ptr, selStride, selDim, this).nanAggregate(DType.DOUBLE, op.initDouble(), op::aggDouble);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    @Override
    public Tensor<Double> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = engine.ofDouble().storage().zeros(size());
        var dst = engine.ofDouble().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(Storage<Double> copy, Order askOrder) {
        var loop = StrideLoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                copy.setDouble(last++, storage.getDouble(p));
            }
        }
    }

    @Override
    public Tensor<Double> copyTo(Tensor<Double> to, Order askOrder) {

        if (to instanceof BaseDoubleTensorStride dst) {

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
                                    BaseDoubleTensorStride s = (BaseDoubleTensorStride) this.narrowAll(false, ss, es);
                                    BaseDoubleTensorStride d = (BaseDoubleTensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseDoubleTensorStride src, BaseDoubleTensorStride dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                dst.storage.setDouble(it2.nextInt(), src.storage.getDouble(p));
            }
        }
    }

    public double[] toArray() {
        if (!isVector()) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        double[] copy = new double[size()];
        int pos = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                copy[pos++] = storage.getDouble(p);
            }
        }
        return copy;
    }

    public double[] asArray() {
        if (storage instanceof DoubleArrayStorage as && isVector() && layout.offset() == 0 && layout.stride(0) == 1) {
            return as.array();
        }
        return toArray();
    }

    @Override
    public String toString() {
        String strDIms = Arrays.toString(layout.dims());
        String strStrides = Arrays.toString(layout.strides());
        return STR."BaseStride{\{dtype().id()},\{strDIms},\{layout.offset()},\{strStrides}}\n\{toContent()}";
    }
}