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

import rapaio.data.OperationNotAvailableException;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.layout.StrideWrapper;
import rapaio.math.tensor.manager.AbstractStrideTensor;
import rapaio.math.tensor.operator.TensorAssociativeOp;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
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
        if (Order.A == askOrder) {
            if (layout.isCOrdered()) {
                askOrder = Order.C;
            } else if (layout.isFOrdered()) {
                askOrder = Order.F;
            } else {
                askOrder = Order.defaultOrder();
            }
        }
        if (Order.S == askOrder) {
            throw new IllegalArgumentException("Illegal order specification.");
        }
        StrideLayout newLayout = layout.attemptReshape(askShape, askOrder);
        if (newLayout != null) {
            return manager.ofDouble().stride(newLayout, storage);
        }
        var it = new StridePointerIterator(layout, askOrder);
        Tensor<Double> copy = manager.ofDouble().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(askOrder);
        while (it.hasNext()) {
            copy.ptrSetDouble(copyIt.nextInt(), storage.getDouble(it.nextInt()));
        }
        return copy;
    }

    @Override
    public Tensor<Double> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var result = manager.ofDouble().zeros(Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var loop = LoopDescriptor.of(layout, askOrder);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                out.setDouble(ptr++, storage.getDouble(p));
                p += loop.step;
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
        for (int p : loop.offsets) {
            if (loop.step == 1) {
                storage.fillDouble(value, p, loop.size);
            } else {
                for (int i = 0; i < loop.size; i++) {
                    storage.setDouble(p, value);
                    p += loop.step;
                }
            }
        }
        return this;
    }

    @Override
    public Tensor<Double> fillNan_(Double value) {
        if (!dtype().floatingPoint()) {
            return this;
        }
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                if (dtype().isNaN(storage.getDouble(p))) {
                    storage.setDouble(p, value);
                }
                p += loop.step;
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
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                storage.setDouble(p, op.applyDouble(storage.getDouble(p)));
                p += loop.step;
            }
        }
    }

    @Override
    public Tensor<Double> unaryOp_(TensorUnaryOp op) {
        if (op.floatingPointOnly() && !dtype().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point tensors.");
        }
        if (loop.step == 1) {
            unaryOpUnit(op);
        } else {
            unaryOpStep(op);
        }
        return this;
    }

    @Override
    public <M extends Number> Tensor<Double> binaryOp_(TensorBinaryOp op, Tensor<M> b) {
        if (b.isScalar()) {
            return binaryOp_(op, b.getDouble());
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
            storage.setDouble(next, op.applyDouble(storage.getDouble(next), b.ptrGetDouble(refIt.nextInt())));
        }
        return this;
    }

    @Override
    public <M extends Number> Tensor<Double> binaryOp_(TensorBinaryOp op, M value) {
        double v = value.doubleValue();
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                storage.setDouble(p, op.applyDouble(storage.getDouble(p), v));
                p += loop.step;
            }
        }
        return this;
    }

    @Override
    public <M extends Number> Tensor<Double> fma_(Double a, Tensor<M> t) {
        if (t.isScalar()) {
            double tVal = t.getDouble();
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
            storage.setDouble(next, (double) Math.fma(t.ptrGetDouble(refIt.nextInt()), aVal, storage.getDouble(next)));
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

        int offset1 = layout.offset();
        int offset2 = dts.layout.offset();
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        double sum = 0;
        for (int i = start; i < end; i++) {
            sum += (double) (storage.getDouble(offset1 + i * step1) * dts.storage.getDouble(offset2 + i * step2));
        }
        return sum;
    }

    @Override
    public Tensor<Double> vpadCopy(int before, int after) {
        if (!isVector()) {
            throw new IllegalArgumentException("This operation is available only for vectors.");
        }
        Storage<Double> newStorage = manager.storage().ofDouble().zeros(before + dim(0) + after);
        var loop = LoopDescriptor.of(layout, Order.S);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                newStorage.setDouble(before + i, ptrGetDouble(p));
                p += loop.step;
            }
        }
        return manager.ofDouble().stride(Shape.of(before + dim(0) + after), Order.C, newStorage);
    }

    @Override
    public Tensor<Double> mv(Tensor<Double> tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    STR."Operands are not valid for matrix-vector multiplication \{"(m = %s, v = %s).".formatted(shape(),
                            tensor.shape())}");
        }
        var result = manager.ofDouble().storage().zeros(shape().dim(0));
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
        return manager.ofDouble().stride(layout, result);
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

        var result = manager.ofDouble().storage().zeros(m * p);
        var ret = manager.ofDouble().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<Tensor<Double>> rows = chunk(0, false, 1);
        List<Tensor<Double>> cols = t.chunk(1, false, 1);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().byteCount()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) Math.ceil(Math.sqrt(chunk / 4.)) : (int) Math.ceil(Math.sqrt(chunk));

        int iStride = ((StrideLayout) ret.layout()).stride(0);
        int jStride = ((StrideLayout) ret.layout()).stride(1);

        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService service = Executors.newFixedThreadPool(manager.cpuThreads())) {
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
        Tensor<Double> scatter = manager.ofDouble().zeros(Shape.of(dim(1), dim(1)));
        Tensor<Double> mean = mean(0);
        for (int k = 0; k < dim(0); k++) {
            Tensor<Double> row = takesq(0, k).sub(mean);
            for (int i = 0; i < row.size(); i++) {
                for (int j = 0; j < row.size(); j++) {
                    scatter.incDouble((double) (row.getDouble(i) * row.getDouble(j)), i, j);
                }
            }
        }
        return scatter;
    }

    @Override
    public Double trace() {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("This operation is available only on tensor matrix.");
        }
        if (dim(0) != dim(1)) {
            throw new OperationNotAvailableException("This operation is available only on a square matrix.");
        }
        double trace = 0;
        for (int i = 0; i < dim(0); i++) {
            trace += getDouble(i, i);
        }
        return trace;
    }

    @Override
    public Tensor<Double> diag() {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("This operation is available only on tensor matrix.");
        }
        if (dim(0) != dim(1)) {
            throw new OperationNotAvailableException("This operation is avaiable only on a square matrix.");
        }
        int n = dim(0);
        double[] diag = new double[n];
        for (int i = 0; i < n; i++) {
            diag[i] = getDouble(i, i);
        }
        return manager().ofDouble().stride(Shape.of(n), diag);
    }

    @Override
    public Double norm(Double pow) {
        if (!dtype().floatingPoint()) {
            throw new OperationNotAvailableException("This operation is only available on floating point data types.");
        }
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
        var loop = LoopDescriptor.of(layout, Order.S);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double value = (double) Math.abs(storage.getDouble(p));
                sum += (double) Math.pow(value, pow);
                p += loop.step;
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

        Tensor<Double> res = manager.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            var stride = manager.ofDouble().stride(StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride}), storage);
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
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (double) (storage.getDouble(p) - mean);
                p += loop.step;
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
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double v = storage.getDouble(p);
                p += loop.step;
                if (dtype().isNaN(v)) {
                    continue;
                }
                sum += (double) (v - mean);
            }
        }
        return (double) (mean + sum / size);
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
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum2 += (double) ((storage.getDouble(p) - mean) * (storage.getDouble(p) - mean));
                sum3 += (double) (storage.getDouble(p) - mean);
                p += loop.step;
            }
        }
        return (double) ((sum2 - (sum3 * sum3) / (size - ddof)) / (size - ddof));
    }

    @Override
    public int argmax(Order order) {
        int argmax = -1;
        double argvalue = TensorOp.max().initDouble();
        var i = 0;
        var loop = LoopDescriptor.of(layout, order);
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.size; j++) {
                double value = storage.getDouble(p);
                p += loop.step;
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
        double argvalue = TensorOp.min().initDouble();
        var i = 0;
        var loop = LoopDescriptor.of(layout, order);
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.size; j++) {
                double value = storage.getDouble(p);
                p += loop.step;
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
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                if (dtype().isNaN(storage.getDouble(p))) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }

    @Override
    public int zeroCount() {
        int count = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                if (storage.getDouble(p) == 0) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }

    @Override
    protected Double associativeOp(TensorAssociativeOp op) {
        double agg = op.initDouble();
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                agg = op.applyDouble(agg, storage.getDouble(p));
                p += loop.step;
            }
        }
        return agg;
    }

    @Override
    protected Double nanAssociativeOp(TensorAssociativeOp op) {
        double aggregate = op.initDouble();
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                if (!dtype().isNaN(storage.getDouble(p))) {
                    aggregate = op.applyDouble(aggregate, storage.getDouble(p));
                }
                p += loop.step;
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

        Tensor<Double> res = manager.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            double value = StrideWrapper.of(ptr, selStride, selDim, this).aggregate(op.initDouble(), op::applyDouble);
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

        Tensor<Double> res = manager.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            double value = StrideWrapper.of(ptr, selStride, selDim, this).nanAggregate(DType.DOUBLE, op.initDouble(), op::applyDouble);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    @Override
    public Tensor<Double> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = manager.ofDouble().storage().zeros(size());
        var dst = manager.ofDouble().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(Storage<Double> copy, Order askOrder) {
        var loop = LoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                copy.setDouble(last++, storage.getDouble(p));
                p += loop.step;
            }
        }
    }

    @Override
    public Tensor<Double> copyTo(Tensor<Double> to, Order askOrder) {

        if (to instanceof BaseDoubleTensorStride dst) {

            int limit = Math.floorDiv(L2_CACHE_SIZE, dtype().byteCount() * 2 * manager.cpuThreads() * 8);

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

                try (ExecutorService executor = Executors.newFixedThreadPool(manager.cpuThreads())) {
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
        var loop = LoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                dst.storage.setDouble(it2.nextInt(), src.storage.getDouble(p));
                p += loop.step;
            }
        }
    }

    @Override
    public String toString() {
        String strDIms = Arrays.toString(layout.dims());
        String strStrides = Arrays.toString(layout.strides());
        return STR."BaseStride{\{dtype().id()},\{strDIms},\{layout.offset()},\{strStrides}}\n\{toContent()}";
    }
}