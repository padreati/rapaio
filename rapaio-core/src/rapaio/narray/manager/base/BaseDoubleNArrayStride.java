/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.narray.manager.base;

import static rapaio.util.Hardware.CORES;
import static rapaio.util.Hardware.L2_CACHE_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import rapaio.data.OperationNotAvailableException;
import rapaio.narray.DType;
import rapaio.narray.Layout;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Order;
import rapaio.narray.Shape;
import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.iterators.StridePointerIterator;
import rapaio.narray.layout.StrideLayout;
import rapaio.narray.layout.StrideWrapper;
import rapaio.narray.manager.AbstractStrideNArray;
import rapaio.narray.operator.Broadcast;
import rapaio.narray.operator.NArrayBinaryOp;
import rapaio.narray.operator.NArrayOp;
import rapaio.narray.operator.NArrayReduceOp;
import rapaio.narray.operator.NArrayUnaryOp;
import rapaio.printer.Format;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public final class BaseDoubleNArrayStride extends AbstractStrideNArray<Double> {

    public BaseDoubleNArrayStride(NArrayManager engine, StrideLayout layout, Storage<Double> storage) {
        super(engine, layout, storage);
    }

    @Override
    public DType<Double> dtype() {
        return DType.DOUBLE;
    }

    @Override
    public NArray<Double> reshape(Shape askShape, Order askOrder) {
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
        NArray<Double> copy = manager.ofDouble().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(askOrder);
        while (it.hasNext()) {
            copy.ptrSetDouble(copyIt.nextInt(), storage.getDouble(it.nextInt()));
        }
        return copy;
    }

    @Override
    public NArray<Double> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var result = manager.ofDouble().zeros(Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var loop = StrideLoopDescriptor.of(layout, askOrder, dtype().vectorSpecies());
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
    public BaseDoubleNArrayStride apply_(Order askOrder, IntIntBiFunction<Double> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setDouble(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public NArray<Double> apply_(Function<Double, Double> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setDouble(ptr, fun.apply(storage.getDouble(ptr)));
        }
        return this;
    }

    @Override
    public NArray<Double> unaryOp_(NArrayUnaryOp op) {
        if (op.floatingPointOnly() && !dtype().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point NArrays.");
        }
        op.applyDouble(loop, storage);
        return this;
    }

    @Override
    public NArray<Double> binaryOp_(NArrayBinaryOp op, NArray<?> other) {
        if (other.isScalar()) {
            return binaryOp_(op, other.getDouble());
        }
        Broadcast.ElementWise broadcast = Broadcast.elementWise(List.of(this.shape(), other.shape()));
        if (!broadcast.valid()) {
            throw new IllegalArgumentException(
                    String.format("Operation could not be applied on tensors with shape: %s, %s", shape(), other.shape()));
        }
        if (!broadcast.hasShape(this)) {
            throw new IllegalArgumentException("Broadcast cannot be applied for inplace operations.");
        }
        other = broadcast.transform(other);
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = other.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setDouble(next, op.applyDouble(storage.getDouble(next), other.ptrGetDouble(refIt.nextInt())));
        }
        return this;
    }

    @Override
    public <M extends Number> NArray<Double> binaryOp_(NArrayBinaryOp op, M value) {
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
    public NArray<Double> fma_(Double a, NArray<?> t) {
        if (t.isScalar()) {
            double tVal = t.getDouble();
            return add_((double) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("NArrays does not have the same shape.");
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

    // LINEAR ALGEBRA OPERATIONS

    @Override
    public Double inner(NArray<?> other) {
        return inner(other, 0, shape().dim(0));
    }

    @Override
    public Double inner(NArray<?> other, int start, int end) {
        if (shape().rank() != 1 || other.shape().rank() != 1 || shape().dim(0) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        if (start >= end || start < 0 || end > other.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseDoubleNArrayStride dts = (BaseDoubleNArrayStride) other;

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
    public NArray<Double> mv(NArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 1 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-vector multiplication (m = %s, v = %s).",
                            shape(), other.shape()));
        }
        var result = manager.ofDouble().zeros(Shape.of(shape().dim(0)), askOrder);
        for (int i = 0; i < shape().dim(0); i++) {
            result.ptrSetDouble(i, takesq(0, i).inner(other));
        }
        return result;
    }

    @Override
    public NArray<Double> bmv(NArray<?> other, Order askOrder) {
        BaseDoubleNArrayStride a = this;
        NArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseDoubleNArrayStride) a.strexp(0, 1).strexp(1, 1);
        }
        if (other.isScalar()) {
            b = b.strexp(0, 1);
        }
        if (a.rank() == 2 && b.rank() == 1 && a.dim(1) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseDoubleNArrayStride) a.stretch(0)).bmvInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 3 && b.rank() == 1 && a.dim(2) == b.dim(0)) {
            // batch on matrix, add batch to vector
            return a.bmvInternal(b.strexp(0, a.dim(0)), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(1)) {
            // batch on vector, add batch to matrix
            return ((BaseDoubleNArrayStride) a.strexp(0, b.dim(0))).bmvInternal(b, askOrder);
        }
        if (a.rank() == 3 && b.rank() == 2 && a.dim(2) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bmvInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch matrix vector multiplication (bm : %s, bv = %s)", shape(), other.shape()));
    }

    private NArray<Double> bmvInternal(NArray<?> other, Order askOrder) {
        NArray<Double> res = manager.ofDouble().zeros(Shape.of(dim(0), dim(1)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            takesq(0, b).mv(other.takesq(0, b)).copyTo(res.takesq(0, b));
        }
        return res;
    }

    @Override
    public NArray<Double> vtm(NArray<?> other, Order askOrder) {
        if (shape().rank() != 1 || other.rank() != 2 || shape().dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for vector transpose matrix multiplication (v = %s, m = %s).",
                            shape(), other.shape())
            );
        }
        var result = manager.ofDouble().zeros(Shape.of(other.dim(1)), askOrder);
        for (int i = 0; i < other.dim(1); i++) {
            result.ptrSetDouble(i, this.inner(other.takesq(1, i)));
        }
        return result;
    }

    @Override
    public NArray<?> bvtm(NArray<?> other, Order askOrder) {
        BaseDoubleNArrayStride a = this;
        NArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseDoubleNArrayStride) a.stretch(0);
        }
        if (other.isScalar()) {
            b = b.stretch(0, 1);
        }
        if (a.rank() == 1 && b.rank() == 2 && a.dim(0) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseDoubleNArrayStride) a.stretch(0)).bvtmInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(0)) {
            // batch on vector, add batch to matrix
            return a.mm(b, askOrder);
        }
        if (a.rank() == 1 && b.rank() == 3 && a.dim(0) == b.dim(1)) {
            // batch on matrix, add batch to vector
            return ((BaseDoubleNArrayStride) a.strexp(0, b.dim(0))).bvtmInternal(b, askOrder);
        }
        if (a.rank() == 2 && b.rank() == 3 && a.dim(1) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bvtmInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch vector transpose matrix multiplication (bv : %s, bm = %s)", shape(), other.shape()));
    }

    private NArray<Double> bvtmInternal(NArray<?> other, Order askOrder) {
        NArray<Double> res = manager.ofDouble().zeros(Shape.of(dim(0), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            takesq(0, b).vtm(other.takesq(0, b)).copyTo(res.takesq(0, b));
        }
        return res;
    }

    @Override
    public NArray<Double> mm(NArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 2 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-matrix multiplication (m = %s, v = %s).", shape(), other.shape()));
        }
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Illegal askOrder value, must be Order.C or Order.F");
        }
        var ret = manager.ofDouble().zeros(Shape.of(shape().dim(0), other.shape().dim(1)), askOrder);
        return mmInternalParallel(other, ret);
    }

    private NArray<Double> mmInternal(NArray<?> other, NArray<Double> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<NArray<Double>> rows = chunk(0, false, 1);
        List<NArray<Double>> cols = other.cast(dtype()).chunk(1, false, 1);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().byteCount()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) Math.ceil(Math.sqrt(chunk / 4.)) : (int) Math.ceil(Math.sqrt(chunk));

        int off = ((StrideLayout) to.layout()).offset();
        int iStride = ((StrideLayout) to.layout()).stride(0);
        int jStride = ((StrideLayout) to.layout()).stride(1);

        for (int r = 0; r < m; r += innerChunk) {
            int re = Math.min(m, r + innerChunk);

            for (int c = 0; c < p; c += innerChunk) {
                int ce = Math.min(p, c + innerChunk);

                for (int k = 0; k < n; k += vectorChunk) {
                    int end = Math.min(n, k + vectorChunk);
                    for (int i = r; i < re; i++) {
                        var krow = (BaseDoubleNArrayStride) rows.get(i);
                        for (int j = c; j < ce; j++) {
                            double value = to.ptrGetDouble(off + i * iStride + j * jStride);
                            to.ptrSetDouble(off + i * iStride + j * jStride, (double) (value + krow.inner(cols.get(j), k, end)));
                        }
                    }
                }
            }
        }
        return to;
    }

    private NArray<Double> mmInternalParallel(NArray<?> other, NArray<Double> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<NArray<Double>> rows = chunk(0, false, 1);
        List<NArray<Double>> cols = other.cast(dtype()).chunk(1, false, 1);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().byteCount()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) Math.ceil(Math.sqrt(chunk / 4.)) : (int) Math.ceil(Math.sqrt(chunk));

        int off = ((StrideLayout) to.layout()).offset();
        int iStride = ((StrideLayout) to.layout()).stride(0);
        int jStride = ((StrideLayout) to.layout()).stride(1);

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
                                var krow = (BaseDoubleNArrayStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    double value = to.ptrGetDouble(off + i * iStride + j * jStride);
                                    to.ptrSetDouble(off + i * iStride + j * jStride, (double) (value + krow.inner(cols.get(j), k, end)));
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
        return to;
    }

    @Override
    public NArray<Double> bmm(NArray<?> other, Order askOrder) {
        if (rank() == 2 && other.rank() == 2 && dim(1) == other.dim(0)) {
            return ((BaseDoubleNArrayStride) stretch(0)).bmmInternal(other.stretch(0), askOrder);
        }
        if (rank() == 3 && other.rank() == 2 && dim(2) == other.dim(0)) {
            return bmmInternal(other.strexp(0, dim(0)), askOrder);
        }
        if (rank() == 2 && other.rank() == 3 && dim(1) == other.dim(1)) {
            return ((BaseDoubleNArrayStride) strexp(0, other.dim(0))).bmmInternal(other, askOrder);
        }
        if (rank() == 3 && other.rank() == 3 && dim(0) == other.dim(0) && dim(2) == other.dim(1)) {
            return bmmInternal(other, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch matrix-matrix multiplication (bm1: %s, bm2: %s)", shape(), other.shape()));
    }

    private NArray<Double> bmmInternal(NArray<?> other, Order askOrder) {
        NArray<Double> res = manager.ofDouble().zeros(Shape.of(dim(0), dim(1), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            ((BaseDoubleNArrayStride) takesq(0, b)).mmInternal(other.takesq(0, b), res.takesq(0, b));
        }
        return res;
    }

    @Override
    public Double trace() {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("This operation is available only on matrix.");
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
    public NArray<Double> diag(int diagonal) {
        if (isScalar() && diagonal == 0) {
            return this;
        }
        if (isVector()) {
            int n = dim(0) + Math.abs(diagonal);
            NArray<Double> m = manager.ofDouble().zeros(Shape.of(n, n));
            for (int i = 0; i < dim(0); i++) {
                m.setDouble(getDouble(i), i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return m;
        }
        if (isMatrix()) {
            int d = diagonal >= 0 ? dim(1) : dim(0);
            int len = diagonal >= 0 ? d - diagonal : d + diagonal;
            if (len <= 0) {
                throw new IllegalArgumentException("Diagonal " + diagonal + " does not exists for shape " + shape() + ".");
            }
            double[] diag = new double[len];
            for (int i = 0; i < len; i++) {
                diag[i] = getDouble(i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return manager().ofDouble().stride(Shape.of(len), diag);
        }
        throw new OperationNotAvailableException("This operation is available for tensors with shape " + shape() + ".");
    }

    @Override
    public Double norm(Double pow) {
        if (!dtype().floatingPoint()) {
            throw new OperationNotAvailableException("This operation is only available on floating point data types.");
        }
        if (pow < 0) {
            throw new IllegalArgumentException(String.format("Norm power p=%s must be greater or equal with 0.", Format.floatFlex(pow)));
        }
        if (dtype().castValue(0).equals(pow)) {
            return (double) shape().size();
        }
        if (dtype().castValue(1).equals(pow)) {
            return abs().sum();
        }
        if (dtype().castValue(2).equals(pow)) {
            return (double) Math.sqrt(sqr().sum());
        }
        double sum = (double) 0;
        var loop = StrideLoopDescriptor.of(layout, Order.S, dtype().vectorSpecies());
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
    public NArray<Double> normalize_(Double pow) {
        return div_(norm(pow));
    }

    @Override
    public NArray<Double> softmax1d_(int axis) {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        // TODO: this can be improved perhaps a lot
        sub_(amax1d(axis).strexp(axis, dim(axis))).exp_();
        div_(sum1d(axis).strexp(axis, dim(axis)));
        return this;
    }

    @Override
    public NArray<Double> logsoftmax1d_(int axis) {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        // TODO: this can be improved perhaps a lot
        var max = amax1d(axis).strexp(axis, dim(axis));
        sub_(this.sub(max).exp().sum1d(axis).log_().strexp(axis, dim(axis))).sub_(max);
        return this;
    }

    @Override
    protected NArray<Double> alongAxisOperation(Order order, int axis, Function<NArray<Double>, Double> op) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        NArray<Double> res = manager.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        int size = it.size();
        int chunk = 128;
        int tasks = (size % chunk == 0) ? size / chunk : size / chunk + 1;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        var stride = manager.ofDouble().stride(StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride}), storage);
                        res.ptrSet(resPtr, op.apply(stride));
                    });
                }
                executor.submit(() -> {
                    for (var t : taskList) {
                        t.run();
                    }
                    latch.countDown();
                });
            }
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
        double mean = (double) mean();

        double sum2 = 0;
        double sum3 = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double centered = (double) (storage.getDouble(p) - mean);
                sum2 += (double) (centered * centered);
                sum3 += centered;
                p += loop.step;
            }
        }
        return (double) ((sum2 - (sum3 * sum3) / (size - ddof)) / (size - ddof));
    }

    @Override
    public int argmax(Order order) {
        int argmax = -1;
        double argvalue = NArrayOp.reduceMax().initDouble();
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dtype().vectorSpecies());
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
        double argvalue = NArrayOp.reduceMin().initDouble();
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dtype().vectorSpecies());
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
    public Double reduceOp(NArrayReduceOp op) {
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
    public Double nanAssociativeOp(NArrayReduceOp op) {
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
    public NArray<Double> associativeOpNarrow(NArrayReduceOp op, Order order, int axis) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        NArray<Double> res = manager.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
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
    public NArray<Double> nanAssociativeOpNarrow(NArrayReduceOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        NArray<Double> res = manager.ofDouble().zeros(Shape.of(newDims), Order.autoFC(order));
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
    public NArray<Double> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = manager.ofDouble().storage().zeros(size());
        var dst = manager.ofDouble().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst);
        }
        return dst;
    }

    private void sameLayoutCopy(Storage<Double> copy, Order askOrder) {
        var loop = StrideLoopDescriptor.of(layout, askOrder, dtype().vectorSpecies());
        var last = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                copy.setDouble(last++, storage.getDouble(p));
                p += loop.step;
            }
        }
    }

    @Override
    public NArray<Double> copyTo(NArray<Double> to) {

        Order askOrder = Layout.storageFastTandemOrder(layout, to.layout());

        if (to instanceof BaseDoubleNArrayStride dst) {

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
                                    BaseDoubleNArrayStride s = (BaseDoubleNArrayStride) this.narrowAll(false, ss, es);
                                    BaseDoubleNArrayStride d = (BaseDoubleNArrayStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseDoubleNArrayStride src, BaseDoubleNArrayStride dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder, dtype().vectorSpecies());
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
        return String.format("BaseStride{%s,%s,%s,%s}\n%s", dtype().id(), Arrays.toString(layout.dims()), layout.offset(),
                Arrays.toString(layout.strides()), toContent());
    }
}