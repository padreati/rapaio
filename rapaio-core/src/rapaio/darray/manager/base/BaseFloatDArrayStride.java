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

package rapaio.darray.manager.base;

import static rapaio.util.Hardware.CORES;
import static rapaio.util.Hardware.L2_CACHE_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import jdk.incubator.vector.FloatVector;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Layout;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Storage;
import rapaio.darray.iterators.IndexIterator;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.iterators.StridePointerIterator;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.manager.AbstractStrideDArray;
import rapaio.darray.operator.Broadcast;
import rapaio.darray.operator.DArrayBinaryOp;
import rapaio.darray.operator.DArrayOp;
import rapaio.darray.operator.DArrayReduceOp;
import rapaio.darray.operator.DArrayUnaryOp;
import rapaio.darray.operator.impl.ReduceOpMax;
import rapaio.darray.operator.impl.ReduceOpMin;
import rapaio.data.OperationNotAvailableException;
import rapaio.printer.Format;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public final class BaseFloatDArrayStride extends AbstractStrideDArray<Float> {

    private static final DType<Float> dt = DType.FLOAT;

    public BaseFloatDArrayStride(DArrayManager engine, StrideLayout layout, Storage storage) {
        super(engine, layout, storage);
    }

    @Override
    public DType<Float> dt() {
        return dt;
    }

    @Override
    public DArray<Float> reshape(Shape askShape, Order askOrder) {
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
            return manager.stride(dt, newLayout, storage);
        }
        var it = new StridePointerIterator(layout, askOrder);
        DArray<Float> copy = manager.zeros(dt, askShape, askOrder);
        var copyIt = copy.ptrIterator(askOrder);
        while (it.hasNext()) {
            copy.ptrSetFloat(copyIt.nextInt(), storage.getFloat(it.nextInt()));
        }
        return copy;
    }

    @Override
    public DArray<Float> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var result = manager.zeros(dt, Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var loop = StrideLoopDescriptor.of(layout, askOrder, dt().vs());
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                out.setFloat(ptr++, storage.getFloat(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    public DArray<Float> gather_(int axis, DArray<?> index, DArray<?> input) {
        if (index.shape() != this.shape()) {
            throw new IllegalArgumentException("Index must have the same shape as destination.");
        }
        if (index.rank() != input.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as input.");
        }
        var ptrDstIt = ptrIterator(Order.C);
        var ptrIdxIt = index.ptrIterator(Order.C);
        var indexIt = new IndexIterator(shape(), Order.C);
        int[] idx = new int[rank()];
        while(indexIt.hasNext()) {
            int[] indexNext = indexIt.next();
            System.arraycopy(indexNext, 0, idx, 0, idx.length);
            idx[axis] = index.ptrGetInt(ptrIdxIt.nextInt());
            storage.setFloat(ptrDstIt.next(), input.getFloat(idx));
        }
        return this;
    }

    @Override
    public DArray<Float> scatter_(int axis, DArray<?> index, DArray<?> input) {
        if(index.rank()!=input.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as input.");
        }
        if(index.rank()!=this.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as self tensor.");
        }
        var ptrSrcIt = input.ptrIterator(Order.C);
        var ptrIdxIt = index.ptrIterator(Order.C);
        var indexIt = new IndexIterator(index.shape(), Order.C);
        int[] idx = new int[rank()];
        while(indexIt.hasNext()) {
            int[] indexNext = indexIt.next();
            System.arraycopy(indexNext, 0, idx, 0, idx.length);
            idx[axis] = index.ptrGetInt(ptrIdxIt.nextInt());
            setFloat(input.ptrGetFloat(ptrSrcIt.nextInt()), idx);
        }
        return this;
    }

    @Override
    public Float get(int... indexes) {
        return storage.getFloat(layout.pointer(indexes));
    }

    @Override
    public void set(Float value, int... indexes) {
        storage.setFloat(layout.pointer(indexes), value);
    }

    @Override
    public void inc(Float value, int... indexes) {
        storage.incFloat(layout.pointer(indexes), value);
    }

    @Override
    public Float ptrGet(int ptr) {
        return storage.getFloat(ptr);
    }

    @Override
    public void ptrSet(int ptr, Float value) {
        storage.setFloat(ptr, value);
    }

    public final Iterator<Float> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(storage::getFloat).iterator();
    }

    @Override
    public BaseFloatDArrayStride apply_(Order askOrder, IntIntBiFunction<Float> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setFloat(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public DArray<Float> apply_(Function<Float, Float> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setFloat(ptr, fun.apply(storage.getFloat(ptr)));
        }
        return this;
    }

    @Override
    public DArray<Float> unaryOp_(DArrayUnaryOp op) {
        if (op.floatingPointOnly() && !dt().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point NArrays.");
        }
        op.applyFloat(loop, storage);
        return this;
    }

    @Override
    public DArray<Float> unaryOp1d_(DArrayUnaryOp op, int axis) {
        int ax = axis < 0 ? axis + shape().rank() : axis;

        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);


        int chunk = 64;
        int tasks = Math.ceilDiv(dim(ax), chunk);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    taskList.add(() -> {
                        manager.stride(dt, StrideLayout.of(new int[] {selDim}, ptr, new int[] {selStride}), storage).unaryOp_(op);
                    });
                }
                executor.submit(() -> {
                    for (var t : taskList) {
                        t.run();
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    @Override
    public DArray<Float> binaryOp_(DArrayBinaryOp op, DArray<?> other) {
        if (other.isScalar()) {
            return binaryOp_(op, other.getFloat());
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
            storage.setFloat(next, op.applyFloat(storage.getFloat(next), other.ptrGetFloat(refIt.nextInt())));
        }
        return this;
    }

    @Override
    public <M extends Number> DArray<Float> binaryOp_(DArrayBinaryOp op, M value) {
        float v = value.floatValue();
        FloatVector m = FloatVector.broadcast(dt.vs(), v);
        for (int p : loop.offsets) {
            int i = 0;
            if (storage.supportSimd()) {
                if (loop.step == 1) {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        FloatVector a = storage.getFloatVector(p);
                        a = op.applyFloat(a, m);
                        storage.setFloatVector(a, p);
                        p += loop.simdLen;
                    }
                } else {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        FloatVector a = storage.getFloatVector(p, loop.simdOffsets(), 0);
                        a = op.applyFloat(a, m);
                        storage.setFloatVector(a, p, loop.simdOffsets(), 0);
                        p += loop.simdLen * loop.step;
                    }
                }
            }
            for (; i < loop.bound; i++) {
                storage.setFloat(p, op.applyFloat(storage.getFloat(p), v));
                p += loop.step;
            }
        }
        return this;
    }

    @Override
    public DArray<Float> fma_(Float a, DArray<?> t) {
        if (t.isScalar()) {
            float tVal = t.getFloat();
            return add_((float) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("NArrays does not have the same shape.");
        }
        float aVal = a;
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = t.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setFloat(next, (float) Math.fma(t.ptrGetFloat(refIt.nextInt()), aVal, storage.getFloat(next)));
        }
        return this;
    }

    // REDUCE OPERATIONS

    @Override
    public Float reduceOp(DArrayReduceOp op) {
        return op.reduceFloat(loop, storage);
    }

    @Override
    public DArray<Float> reduceOp1d(DArrayReduceOp op, int axis, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Float> res = manager.zeros(dt, Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        float value = manager.stride(dt, strideLayout, storage).reduceOp(op);
                        res.ptrSetFloat(resPtr, value);
                    });
                }
                executor.submit(() -> {
                    for (var t : taskList) {
                        t.run();
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    @Override
    public DArray<Float> varc1d(int axis, int ddof, DArray<?> mean, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Float> res = manager.zeros(dt, Shape.of(newDims), Order.autoFC(order));
        if (!res.shape().equals(mean.shape())) {
            throw new IllegalArgumentException("Mean array must have the same shape as the result array.");
        }

        var resIt = res.ptrIterator(Order.C);
        var meanIt = mean.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        float m = mean.ptrGetFloat(meanIt.next());
                        float value = manager.stride(dt, strideLayout, storage).reduceOp(DArrayOp.reduceVarc(ddof, m));
                        res.ptrSet(resPtr, value);
                    });
                }
                executor.submit(() -> {
                    for (var t : taskList) {
                        t.run();
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    @Override
    public int argmax(Order order) {
        int argmax = -1;
        float argvalue = ReduceOpMax.initFloat;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dt().vs());
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.bound; j++) {
                float value = storage.getFloat(p);
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
    public DArray<Integer> argmax1d(int axis, boolean keepDim, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = keepDim ? Arrays.copyOf(layout.dims(), layout.rank()) : layout.shape().narrowDims(axis);
        int[] newStrides = keepDim ? Arrays.copyOf(layout.strides(), layout.rank()) : layout.narrowStrides(axis);
        if(keepDim) {
            newDims[axis] = 1;
            newStrides[axis] = 0;
        }

        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Integer> res = manager.zeros(DType.INTEGER, Shape.of(newDims), Order.autoFC(order));

        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        int value = manager.stride(dt, strideLayout, storage).argmax();
                        res.ptrSetInt(resPtr, value);
                    });
                }
                executor.submit(() -> {
                    for (var t : taskList) {
                        t.run();
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    @Override
    public DArray<Integer> argmin1d(int axis, boolean keepDim, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = keepDim ? Arrays.copyOf(layout.dims(), layout.rank()) : layout.shape().narrowDims(axis);
        int[] newStrides = keepDim ? Arrays.copyOf(layout.strides(), layout.rank()) : layout.narrowStrides(axis);
        if(keepDim) {
            newDims[axis] = 1;
            newStrides[axis] = 0;
        }

        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Integer> res = manager.zeros(DType.INTEGER, Shape.of(newDims), Order.autoFC(order));

        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        int value = manager.stride(dt, strideLayout, storage).argmin();
                        res.ptrSetInt(resPtr, value);
                    });
                }
                executor.submit(() -> {
                    for (var t : taskList) {
                        t.run();
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    @Override
    public int argmin(Order order) {
        int argmin = -1;
        float argvalue = ReduceOpMin.initFloat;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dt().vs());
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.bound; j++) {
                float value = storage.getFloat(p);
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
            for (int i = 0; i < loop.bound; i++) {
                if (dt().isNaN(storage.getFloat(p))) {
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
            for (int i = 0; i < loop.bound; i++) {
                if (storage.getFloat(p) == 0) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }


    // LINEAR ALGEBRA OPERATIONS

    @Override
    public Float inner(DArray<?> other) {
        return inner(other, 0, shape().dim(0));
    }

    @Override
    public Float inner(DArray<?> other, int start, int end) {
        if (shape().rank() != 1 || other.shape().rank() != 1 || shape().dim(0) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        if (start >= end || start < 0 || end > other.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseFloatDArrayStride dts = (BaseFloatDArrayStride) other;

        int offset1 = layout.offset();
        int offset2 = dts.layout.offset();
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        float sum = 0;
        for (int i = start; i < end; i++) {
            sum += (float) (storage.getFloat(offset1 + i * step1) * dts.storage.getFloat(offset2 + i * step2));
        }
        return sum;
    }

    @Override
    public DArray<Float> mv(DArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 1 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-vector multiplication (m = %s, v = %s).",
                            shape(), other.shape()));
        }
        var result = manager.zeros(dt, Shape.of(shape().dim(0)), askOrder);
        for (int i = 0; i < shape().dim(0); i++) {
            result.ptrSetFloat(i, selsq(0, i).inner(other));
        }
        return result;
    }

    @Override
    public DArray<Float> bmv(DArray<?> other, Order askOrder) {
        BaseFloatDArrayStride a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseFloatDArrayStride) a.strexp(0, 1).strexp(1, 1);
        }
        if (other.isScalar()) {
            b = b.strexp(0, 1);
        }
        if (a.rank() == 2 && b.rank() == 1 && a.dim(1) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseFloatDArrayStride) a.stretch(0)).bmvInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 3 && b.rank() == 1 && a.dim(2) == b.dim(0)) {
            // batch on matrix, add batch to vector
            return a.bmvInternal(b.strexp(0, a.dim(0)), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(1)) {
            // batch on vector, add batch to matrix
            return ((BaseFloatDArrayStride) a.strexp(0, b.dim(0))).bmvInternal(b, askOrder);
        }
        if (a.rank() == 3 && b.rank() == 2 && a.dim(2) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bmvInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch matrix vector multiplication (bm : %s, bv = %s)", shape(), other.shape()));
    }

    private DArray<Float> bmvInternal(DArray<?> other, Order askOrder) {
        DArray<Float> res = manager.zeros(dt, Shape.of(dim(0), dim(1)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).mv(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Float> vtm(DArray<?> other, Order askOrder) {
        if (shape().rank() != 1 || other.rank() != 2 || shape().dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for vector transpose matrix multiplication (v = %s, m = %s).",
                            shape(), other.shape())
            );
        }
        var result = manager.zeros(dt, Shape.of(other.dim(1)), askOrder);
        for (int i = 0; i < other.dim(1); i++) {
            result.ptrSetFloat(i, this.inner(other.selsq(1, i)));
        }
        return result;
    }

    @Override
    public DArray<?> bvtm(DArray<?> other, Order askOrder) {
        BaseFloatDArrayStride a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseFloatDArrayStride) a.stretch(0);
        }
        if (other.isScalar()) {
            b = b.stretch(0, 1);
        }
        if (a.rank() == 1 && b.rank() == 2 && a.dim(0) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseFloatDArrayStride) a.stretch(0)).bvtmInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(0)) {
            // batch on vector, add batch to matrix
            return a.mm(b, askOrder);
        }
        if (a.rank() == 1 && b.rank() == 3 && a.dim(0) == b.dim(1)) {
            // batch on matrix, add batch to vector
            return ((BaseFloatDArrayStride) a.strexp(0, b.dim(0))).bvtmInternal(b, askOrder);
        }
        if (a.rank() == 2 && b.rank() == 3 && a.dim(1) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bvtmInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch vector transpose matrix multiplication (bv : %s, bm = %s)", shape(), other.shape()));
    }

    private DArray<Float> bvtmInternal(DArray<?> other, Order askOrder) {
        DArray<Float> res = manager.zeros(dt, Shape.of(dim(0), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).vtm(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Float> mm(DArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 2 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-matrix multiplication (m = %s, v = %s).", shape(), other.shape()));
        }
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Illegal askOrder value, must be Order.C or Order.F");
        }
        var ret = manager.zeros(dt, Shape.of(shape().dim(0), other.shape().dim(1)), askOrder);
        return mmInternalParallel(other, ret);
    }

    private DArray<Float> mmInternal(DArray<?> other, DArray<Float> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<DArray<Float>> rows = unbind(0, false);
        List<DArray<Float>> cols = other.cast(dt()).unbind(1, false);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dt().byteCount()));
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
                        var krow = (BaseFloatDArrayStride) rows.get(i);
                        for (int j = c; j < ce; j++) {
                            float value = to.ptrGetFloat(off + i * iStride + j * jStride);
                            to.ptrSetFloat(off + i * iStride + j * jStride, (float) (value + krow.inner(cols.get(j), k, end)));
                        }
                    }
                }
            }
        }
        return to;
    }

    private DArray<Float> mmInternalParallel(DArray<?> other, DArray<Float> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<DArray<Float>> rows = unbind(0, false);
        List<DArray<Float>> cols = other.cast(dt()).unbind(1, false);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dt().byteCount()));
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
                                var krow = (BaseFloatDArrayStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    float value = to.ptrGetFloat(off + i * iStride + j * jStride);
                                    to.ptrSetFloat(off + i * iStride + j * jStride, (float) (value + krow.inner(cols.get(j), k, end)));
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
    public DArray<Float> bmm(DArray<?> other, Order askOrder) {
        if (rank() == 2 && other.rank() == 2 && dim(1) == other.dim(0)) {
            return ((BaseFloatDArrayStride) stretch(0)).bmmInternal(other.stretch(0), askOrder);
        }
        if (rank() == 3 && other.rank() == 2 && dim(2) == other.dim(0)) {
            return bmmInternal(other.strexp(0, dim(0)), askOrder);
        }
        if (rank() == 2 && other.rank() == 3 && dim(1) == other.dim(1)) {
            return ((BaseFloatDArrayStride) strexp(0, other.dim(0))).bmmInternal(other, askOrder);
        }
        if (rank() == 3 && other.rank() == 3 && dim(0) == other.dim(0) && dim(2) == other.dim(1)) {
            return bmmInternal(other, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch matrix-matrix multiplication (bm1: %s, bm2: %s)", shape(), other.shape()));
    }

    private DArray<Float> bmmInternal(DArray<?> other, Order askOrder) {
        DArray<Float> res = manager.zeros(dt, Shape.of(dim(0), dim(1), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            ((BaseFloatDArrayStride) selsq(0, b)).mmInternal(other.selsq(0, b), res.selsq(0, b));
        }
        return res;
    }

    @Override
    public Float trace() {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("This operation is available only on matrix.");
        }
        if (dim(0) != dim(1)) {
            throw new OperationNotAvailableException("This operation is available only on a square matrix.");
        }
        float trace = 0;
        for (int i = 0; i < dim(0); i++) {
            trace += getFloat(i, i);
        }
        return trace;
    }

    @Override
    public DArray<Float> diag(int diagonal) {
        if (isScalar() && diagonal == 0) {
            return this;
        }
        if (isVector()) {
            int n = dim(0) + Math.abs(diagonal);
            DArray<Float> m = manager.zeros(dt, Shape.of(n, n));
            for (int i = 0; i < dim(0); i++) {
                m.setFloat(getFloat(i), i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return m;
        }
        if (isMatrix()) {
            int d = diagonal >= 0 ? dim(1) : dim(0);
            int len = diagonal >= 0 ? d - diagonal : d + diagonal;
            if (len <= 0) {
                throw new IllegalArgumentException("Diagonal " + diagonal + " does not exists for shape " + shape() + ".");
            }
            float[] diag = new float[len];
            for (int i = 0; i < len; i++) {
                diag[i] = getFloat(i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return manager.stride(dt, Shape.of(len), Order.defaultOrder(), diag);
        }
        throw new OperationNotAvailableException("This operation is available for tensors with shape " + shape() + ".");
    }

    @Override
    public Float norm(
            // FREEZE
            double pow
    ) {
        if (!dt().floatingPoint()) {
            throw new OperationNotAvailableException("This operation is only available on floating point data types.");
        }
        if (pow < 0) {
            throw new IllegalArgumentException(String.format("Norm power p=%s must be greater or equal with 0.", Format.floatFlex(pow)));
        }
        if (pow == 0) {
            return (float) shape().size();
        }
        if (pow == 1) {
            return abs().sum();
        }
        if (pow == 2) {
            return (float) Math.sqrt(sqr().sum());
        }
        float sum = (float) 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += (float) Math.pow(Math.abs(storage.getFloat(p)), pow);
                p += loop.step;
            }
        }
        return (float) Math.pow(sum, 1. / pow);
    }

    @Override
    public DArray<Float> normalize_(
            // FREEZE
            double pow
    ) {
        return div_(norm(pow));
    }

    @Override
    public DArray<Float> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = manager.storageManager().zeros(dt, size());
        var dst = manager.stride(dt, StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst);
        }
        return dst;
    }

    private void sameLayoutCopy(Storage copy, Order askOrder) {
        var loop = StrideLoopDescriptor.of(layout, askOrder, dt.vs());
        var last = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                copy.setFloat(last++, storage.getFloat(p));
                p += loop.step;
            }
        }
    }

    @Override
    public DArray<Float> copyTo(DArray<Float> to) {

        Order askOrder = Layout.storageFastTandemOrder(layout, to.layout());

        if (to instanceof BaseFloatDArrayStride dst) {

            int limit = Math.floorDiv(L2_CACHE_SIZE, dt().byteCount() * 2 * manager.cpuThreads() * 8);

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
                                    BaseFloatDArrayStride s = (BaseFloatDArrayStride) this.narrowAll(false, ss, es);
                                    BaseFloatDArrayStride d = (BaseFloatDArrayStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseFloatDArrayStride src, BaseFloatDArrayStride dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder, dt().vs());
        var it2 = dst.ptrIterator(askOrder);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                dst.storage.setFloat(it2.nextInt(), src.storage.getFloat(p));
                p += loop.step;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("BaseStride{%s,%s,%s,%s}\n%s", dt().id(), Arrays.toString(layout.dims()), layout.offset(),
                Arrays.toString(layout.strides()), toContent());
    }
}
