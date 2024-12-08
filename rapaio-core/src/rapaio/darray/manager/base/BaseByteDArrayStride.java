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

import jdk.incubator.vector.ByteVector;
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

public final class BaseByteDArrayStride extends AbstractStrideDArray<Byte> {

    private static final DType<Byte> dt = DType.BYTE;

    public BaseByteDArrayStride(DArrayManager engine, StrideLayout layout, Storage storage) {
        super(engine, layout, storage);
    }

    @Override
    public DType<Byte> dt() {
        return dt;
    }

    @Override
    public DArray<Byte> reshape(Shape askShape, Order askOrder) {
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
        DArray<Byte> copy = manager.zeros(dt, askShape, askOrder);
        var copyIt = copy.ptrIterator(askOrder);
        while (it.hasNext()) {
            copy.ptrSetByte(copyIt.nextInt(), storage.getByte(it.nextInt()));
        }
        return copy;
    }

    @Override
    public DArray<Byte> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var result = manager.zeros(dt, Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var loop = StrideLoopDescriptor.of(layout, askOrder, dt().vs());
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                out.setByte(ptr++, storage.getByte(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    public DArray<Byte> gather_(int axis, DArray<?> index, DArray<?> input) {
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
            storage.setByte(ptrDstIt.next(), input.getByte(idx));
        }
        return this;
    }

    @Override
    public DArray<Byte> scatter_(int axis, DArray<?> index, DArray<?> input) {
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
            setByte(input.ptrGetByte(ptrSrcIt.nextInt()), idx);
        }
        return this;
    }

    @Override
    public Byte get(int... indexes) {
        return storage.getByte(layout.pointer(indexes));
    }

    @Override
    public void set(Byte value, int... indexes) {
        storage.setByte(layout.pointer(indexes), value);
    }

    @Override
    public void inc(Byte value, int... indexes) {
        storage.incByte(layout.pointer(indexes), value);
    }

    @Override
    public Byte ptrGet(int ptr) {
        return storage.getByte(ptr);
    }

    @Override
    public void ptrSet(int ptr, Byte value) {
        storage.setByte(ptr, value);
    }

    public final Iterator<Byte> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(storage::getByte).iterator();
    }

    @Override
    public BaseByteDArrayStride apply_(Order askOrder, IntIntBiFunction<Byte> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setByte(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public DArray<Byte> apply_(Function<Byte, Byte> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setByte(ptr, fun.apply(storage.getByte(ptr)));
        }
        return this;
    }

    @Override
    public DArray<Byte> unaryOp_(DArrayUnaryOp op) {
        if (op.floatingPointOnly() && !dt().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point NArrays.");
        }
        op.applyByte(loop, storage);
        return this;
    }

    @Override
    public DArray<Byte> unaryOp1d_(DArrayUnaryOp op, int axis) {
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
    public DArray<Byte> binaryOp_(DArrayBinaryOp op, DArray<?> other) {
        if (other.isScalar()) {
            return binaryOp_(op, other.getByte());
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
            storage.setByte(next, op.applyByte(storage.getByte(next), other.ptrGetByte(refIt.nextInt())));
        }
        return this;
    }

    @Override
    public <M extends Number> DArray<Byte> binaryOp_(DArrayBinaryOp op, M value) {
        byte v = value.byteValue();
        ByteVector m = ByteVector.broadcast(dt.vs(), v);
        for (int p : loop.offsets) {
            int i = 0;
            if (storage.supportVectorization()) {
                if (loop.step == 1) {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        ByteVector a = storage.getByteVector(dt.vs(), p);
                        a = op.applyByte(a, m);
                        storage.setByteVector(a, p);
                        p += loop.simdLen;
                    }
                } else {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        ByteVector a = storage.getByteVector(dt.vs(), p, loop.simdOffsets(), 0);
                        a = op.applyByte(a, m);
                        storage.setByteVector(a, p, loop.simdOffsets(), 0);
                        p += loop.simdLen * loop.step;
                    }
                }
            }
            for (; i < loop.size; i++) {
                storage.setByte(p, op.applyByte(storage.getByte(p), v));
                p += loop.step;
            }
        }
        return this;
    }

    @Override
    public DArray<Byte> fma_(Byte a, DArray<?> t) {
        if (t.isScalar()) {
            byte tVal = t.getByte();
            return add_((byte) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("NArrays does not have the same shape.");
        }
        byte aVal = a;
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = t.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setByte(next, (byte) Math.fma(t.ptrGetByte(refIt.nextInt()), aVal, storage.getByte(next)));
        }
        return this;
    }

    // REDUCE OPERATIONS

    @Override
    public Byte reduceOp(DArrayReduceOp op) {
        return op.reduceByte(loop, storage);
    }

    @Override
    public DArray<Byte> reduceOp1d(DArrayReduceOp op, int axis, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Byte> res = manager.zeros(dt, Shape.of(newDims), Order.autoFC(order));
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
                        byte value = manager.stride(dt, strideLayout, storage).reduceOp(op);
                        res.ptrSetByte(resPtr, value);
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
    public DArray<Byte> varc1d(int axis, int ddof, DArray<?> mean, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Byte> res = manager.zeros(dt, Shape.of(newDims), Order.autoFC(order));
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
                        byte m = mean.ptrGetByte(meanIt.next());
                        byte value = manager.stride(dt, strideLayout, storage).reduceOp(DArrayOp.reduceVarc(ddof, m));
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
        byte argvalue = ReduceOpMax.initByte;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dt().vs());
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.size; j++) {
                byte value = storage.getByte(p);
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
        byte argvalue = ReduceOpMin.initByte;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dt().vs());
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.size; j++) {
                byte value = storage.getByte(p);
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
                if (dt().isNaN(storage.getByte(p))) {
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
                if (storage.getByte(p) == 0) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }


    // LINEAR ALGEBRA OPERATIONS

    @Override
    public Byte inner(DArray<?> other) {
        return inner(other, 0, shape().dim(0));
    }

    @Override
    public Byte inner(DArray<?> other, int start, int end) {
        if (shape().rank() != 1 || other.shape().rank() != 1 || shape().dim(0) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        if (start >= end || start < 0 || end > other.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseByteDArrayStride dts = (BaseByteDArrayStride) other;

        int offset1 = layout.offset();
        int offset2 = dts.layout.offset();
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        byte sum = 0;
        for (int i = start; i < end; i++) {
            sum += (byte) (storage.getByte(offset1 + i * step1) * dts.storage.getByte(offset2 + i * step2));
        }
        return sum;
    }

    @Override
    public DArray<Byte> mv(DArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 1 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-vector multiplication (m = %s, v = %s).",
                            shape(), other.shape()));
        }
        var result = manager.zeros(dt, Shape.of(shape().dim(0)), askOrder);
        for (int i = 0; i < shape().dim(0); i++) {
            result.ptrSetByte(i, selsq(0, i).inner(other));
        }
        return result;
    }

    @Override
    public DArray<Byte> bmv(DArray<?> other, Order askOrder) {
        BaseByteDArrayStride a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseByteDArrayStride) a.strexp(0, 1).strexp(1, 1);
        }
        if (other.isScalar()) {
            b = b.strexp(0, 1);
        }
        if (a.rank() == 2 && b.rank() == 1 && a.dim(1) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseByteDArrayStride) a.stretch(0)).bmvInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 3 && b.rank() == 1 && a.dim(2) == b.dim(0)) {
            // batch on matrix, add batch to vector
            return a.bmvInternal(b.strexp(0, a.dim(0)), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(1)) {
            // batch on vector, add batch to matrix
            return ((BaseByteDArrayStride) a.strexp(0, b.dim(0))).bmvInternal(b, askOrder);
        }
        if (a.rank() == 3 && b.rank() == 2 && a.dim(2) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bmvInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch matrix vector multiplication (bm : %s, bv = %s)", shape(), other.shape()));
    }

    private DArray<Byte> bmvInternal(DArray<?> other, Order askOrder) {
        DArray<Byte> res = manager.zeros(dt, Shape.of(dim(0), dim(1)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).mv(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Byte> vtm(DArray<?> other, Order askOrder) {
        if (shape().rank() != 1 || other.rank() != 2 || shape().dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for vector transpose matrix multiplication (v = %s, m = %s).",
                            shape(), other.shape())
            );
        }
        var result = manager.zeros(dt, Shape.of(other.dim(1)), askOrder);
        for (int i = 0; i < other.dim(1); i++) {
            result.ptrSetByte(i, this.inner(other.selsq(1, i)));
        }
        return result;
    }

    @Override
    public DArray<?> bvtm(DArray<?> other, Order askOrder) {
        BaseByteDArrayStride a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseByteDArrayStride) a.stretch(0);
        }
        if (other.isScalar()) {
            b = b.stretch(0, 1);
        }
        if (a.rank() == 1 && b.rank() == 2 && a.dim(0) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseByteDArrayStride) a.stretch(0)).bvtmInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(0)) {
            // batch on vector, add batch to matrix
            return a.mm(b, askOrder);
        }
        if (a.rank() == 1 && b.rank() == 3 && a.dim(0) == b.dim(1)) {
            // batch on matrix, add batch to vector
            return ((BaseByteDArrayStride) a.strexp(0, b.dim(0))).bvtmInternal(b, askOrder);
        }
        if (a.rank() == 2 && b.rank() == 3 && a.dim(1) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bvtmInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch vector transpose matrix multiplication (bv : %s, bm = %s)", shape(), other.shape()));
    }

    private DArray<Byte> bvtmInternal(DArray<?> other, Order askOrder) {
        DArray<Byte> res = manager.zeros(dt, Shape.of(dim(0), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).vtm(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Byte> mm(DArray<?> other, Order askOrder) {
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

    private DArray<Byte> mmInternal(DArray<?> other, DArray<Byte> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<DArray<Byte>> rows = unbind(0, false);
        List<DArray<Byte>> cols = other.cast(dt()).unbind(1, false);

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
                        var krow = (BaseByteDArrayStride) rows.get(i);
                        for (int j = c; j < ce; j++) {
                            byte value = to.ptrGetByte(off + i * iStride + j * jStride);
                            to.ptrSetByte(off + i * iStride + j * jStride, (byte) (value + krow.inner(cols.get(j), k, end)));
                        }
                    }
                }
            }
        }
        return to;
    }

    private DArray<Byte> mmInternalParallel(DArray<?> other, DArray<Byte> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<DArray<Byte>> rows = unbind(0, false);
        List<DArray<Byte>> cols = other.cast(dt()).unbind(1, false);

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
                                var krow = (BaseByteDArrayStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    byte value = to.ptrGetByte(off + i * iStride + j * jStride);
                                    to.ptrSetByte(off + i * iStride + j * jStride, (byte) (value + krow.inner(cols.get(j), k, end)));
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
    public DArray<Byte> bmm(DArray<?> other, Order askOrder) {
        if (rank() == 2 && other.rank() == 2 && dim(1) == other.dim(0)) {
            return ((BaseByteDArrayStride) stretch(0)).bmmInternal(other.stretch(0), askOrder);
        }
        if (rank() == 3 && other.rank() == 2 && dim(2) == other.dim(0)) {
            return bmmInternal(other.strexp(0, dim(0)), askOrder);
        }
        if (rank() == 2 && other.rank() == 3 && dim(1) == other.dim(1)) {
            return ((BaseByteDArrayStride) strexp(0, other.dim(0))).bmmInternal(other, askOrder);
        }
        if (rank() == 3 && other.rank() == 3 && dim(0) == other.dim(0) && dim(2) == other.dim(1)) {
            return bmmInternal(other, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "NArrays are not valid for batch matrix-matrix multiplication (bm1: %s, bm2: %s)", shape(), other.shape()));
    }

    private DArray<Byte> bmmInternal(DArray<?> other, Order askOrder) {
        DArray<Byte> res = manager.zeros(dt, Shape.of(dim(0), dim(1), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            ((BaseByteDArrayStride) selsq(0, b)).mmInternal(other.selsq(0, b), res.selsq(0, b));
        }
        return res;
    }

    @Override
    public Byte trace() {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("This operation is available only on matrix.");
        }
        if (dim(0) != dim(1)) {
            throw new OperationNotAvailableException("This operation is available only on a square matrix.");
        }
        byte trace = 0;
        for (int i = 0; i < dim(0); i++) {
            trace += getByte(i, i);
        }
        return trace;
    }

    @Override
    public DArray<Byte> diag(int diagonal) {
        if (isScalar() && diagonal == 0) {
            return this;
        }
        if (isVector()) {
            int n = dim(0) + Math.abs(diagonal);
            DArray<Byte> m = manager.zeros(dt, Shape.of(n, n));
            for (int i = 0; i < dim(0); i++) {
                m.setByte(getByte(i), i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return m;
        }
        if (isMatrix()) {
            int d = diagonal >= 0 ? dim(1) : dim(0);
            int len = diagonal >= 0 ? d - diagonal : d + diagonal;
            if (len <= 0) {
                throw new IllegalArgumentException("Diagonal " + diagonal + " does not exists for shape " + shape() + ".");
            }
            byte[] diag = new byte[len];
            for (int i = 0; i < len; i++) {
                diag[i] = getByte(i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return manager.stride(dt, Shape.of(len), Order.defaultOrder(), diag);
        }
        throw new OperationNotAvailableException("This operation is available for tensors with shape " + shape() + ".");
    }

    @Override
    public Byte norm(
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
            return (byte) shape().size();
        }
        if (pow == 1) {
            return abs().sum();
        }
        if (pow == 2) {
            return (byte) Math.sqrt(sqr().sum());
        }
        byte sum = (byte) 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (byte) Math.pow(Math.abs(storage.getByte(p)), pow);
                p += loop.step;
            }
        }
        return (byte) Math.pow(sum, 1. / pow);
    }

    @Override
    public DArray<Byte> normalize_(
            // FREEZE
            double pow
    ) {
        return div_(norm(pow));
    }

    @Override
    public DArray<Byte> copy(Order askOrder) {
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
            for (int i = 0; i < loop.size; i++) {
                copy.setByte(last++, storage.getByte(p));
                p += loop.step;
            }
        }
    }

    @Override
    public DArray<Byte> copyTo(DArray<Byte> to) {

        Order askOrder = Layout.storageFastTandemOrder(layout, to.layout());

        if (to instanceof BaseByteDArrayStride dst) {

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
                                    BaseByteDArrayStride s = (BaseByteDArrayStride) this.narrowAll(false, ss, es);
                                    BaseByteDArrayStride d = (BaseByteDArrayStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseByteDArrayStride src, BaseByteDArrayStride dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder, dt().vs());
        var it2 = dst.ptrIterator(askOrder);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                dst.storage.setByte(it2.nextInt(), src.storage.getByte(p));
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
