/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Layout;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.IndexIterator;
import rapaio.darray.iterators.PointerIterator;
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
import rapaio.util.collection.Ints;
import rapaio.util.function.IntIntBiFunction;

public final class BaseIntStrideDArray extends AbstractStrideDArray<Integer> {

    public BaseIntStrideDArray(DArrayManager dm, StrideLayout layout, Storage storage) {
        super(dm, DType.INTEGER, layout, storage);
    }

    @Override
    public DArray<Integer> reshape(Shape askShape, Order askOrder) {
        if (layout.shape().size() != askShape.size()) {
            throw new IllegalArgumentException(String.format(
                    "Incompatible shape size for reshape operation from %s into %s.", this.shape(), askShape));
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
            return dm.stride(dt, newLayout, storage);
        }
        var it = new StridePointerIterator(layout, askOrder);
        DArray<Integer> copy = dm.zeros(dt, askShape, askOrder);
        var copyIt = copy.ptrIterator(askOrder);
        while (it.hasNext()) {
            copy.ptrSetInt(copyIt.nextInt(), storage.getInt(it.nextInt()));
        }
        return copy;
    }

    @Override
    public DArray<Integer> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var result = dm.zeros(dt, Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var loop = StrideLoopDescriptor.of(layout, askOrder, dt().vs());
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                out.setInt(ptr++, storage.getInt(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    public DArray<Integer> gather_(int axis, DArray<?> index, DArray<?> input) {
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
        while (indexIt.hasNext()) {
            int[] indexNext = indexIt.next();
            System.arraycopy(indexNext, 0, idx, 0, idx.length);
            idx[axis] = index.ptrGetInt(ptrIdxIt.nextInt());
            storage.setInt(ptrDstIt.next(), input.getInt(idx));
        }
        return this;
    }

    @Override
    public DArray<Integer> scatter_(int axis, DArray<?> index, DArray<?> input) {
        if (index.rank() != input.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as input.");
        }
        if (index.rank() != this.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as self tensor.");
        }
        var ptrSrcIt = input.ptrIterator(Order.C);
        var ptrIdxIt = index.ptrIterator(Order.C);
        var indexIt = new IndexIterator(index.shape(), Order.C);
        int[] idx = new int[rank()];
        while (indexIt.hasNext()) {
            int[] indexNext = indexIt.next();
            System.arraycopy(indexNext, 0, idx, 0, idx.length);
            idx[axis] = index.ptrGetInt(ptrIdxIt.nextInt());
            setInt(input.ptrGetInt(ptrSrcIt.nextInt()), idx);
        }
        return this;
    }

    @Override
    public Integer get(int... indices) {
        return storage.getInt(layout.pointer(indices));
    }

    @Override
    public void set(Integer value, int... indices) {
        storage.setInt(layout.pointer(indices), value);
    }

    @Override
    public void inc(Integer value, int... indices) {
        storage.incInt(layout.pointer(indices), value);
    }

    @Override
    public Integer ptrGet(int ptr) {
        return storage.getInt(ptr);
    }

    @Override
    public void ptrSet(int ptr, Integer value) {
        storage.setInt(ptr, value);
    }

    public final Iterator<Integer> iterator(Order askOrder) {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED | Spliterator.IMMUTABLE), false)
                .map(storage::getInt).iterator();
    }

    @Override
    public BaseIntStrideDArray apply_(Order askOrder, IntIntBiFunction<Integer> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setInt(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public DArray<Integer> apply_(Function<Integer, Integer> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setInt(ptr, fun.apply(storage.getInt(ptr)));
        }
        return this;
    }

    @Override
    public DArray<Integer> unary_(DArrayUnaryOp op) {
        if (op.floatingPointOnly() && !dt().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point DArrays.");
        }
        op.applyInt(loop, storage);
        return this;
    }

    @Override
    public DArray<Integer> unary1d_(DArrayUnaryOp op, int axis) {
        int ax = axis < 0 ? axis + shape().rank() : axis;

        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);


        int chunk = 64;
        int tasks = Math.ceilDiv(dim(ax), chunk);
        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    taskList.add(() -> {
                        dm.stride(dt, StrideLayout.of(new int[] {selDim}, ptr, new int[] {selStride}), storage).unary_(op);
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
    public DArray<Integer> binary_(DArrayBinaryOp op, DArray<?> other) {
        if (other.isScalar()) {
            return binary_(op, other.getInt());
        }
        Broadcast.ElementWise broadcast = Broadcast.elementWise(List.of(this.shape(), other.shape()));
        if (!broadcast.valid()) {
            throw new IllegalArgumentException(
                    String.format("Operation could not be applied on tensors with shape: %s, %s", shape(), other.shape()));
        }
        if (!broadcast.hasShape(this)) {
            throw new IllegalArgumentException(
                    String.format("Broadcast cannot be applied for in place operations. This shape %s, other shape %s", this.shape(),
                            other.shape()));
        }
        other = broadcast.transform(other);
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = other.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setInt(next, op.applyInt(storage.getInt(next), other.ptrGetInt(refIt.nextInt())));
        }
        return this;
    }

    @Override
    public <M extends Number> DArray<Integer> binary_(DArrayBinaryOp op, M value) {
        int v = value.intValue();
        IntVector m = IntVector.broadcast(dt.vs(), v);
        for (int p : loop.offsets) {
            int i = 0;
            if (storage.supportSimd()) {
                if (loop.step == 1) {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        IntVector a = storage.getIntVector(p);
                        a = op.applyInt(a, m);
                        storage.setIntVector(a, p);
                        p += loop.simdLen;
                    }
                } else {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        IntVector a = storage.getIntVector(p, loop.simdIdx(), 0);
                        a = op.applyInt(a, m);
                        storage.setIntVector(a, p, loop.simdIdx(), 0);
                        p += loop.simdLen * loop.step;
                    }
                }
            }
            for (; i < loop.bound; i++) {
                storage.setInt(p, op.applyInt(storage.getInt(p), v));
                p += loop.step;
            }
        }
        return this;
    }

    @Override
    public DArray<Integer> fma_(Integer a, DArray<?> t) {
        if (t.isScalar()) {
            int tVal = t.getInt();
            return add_((int) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("DArrays does not have the same shape.");
        }
        int aVal = a;
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = t.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setInt(next, (int) Math.fma(t.ptrGetInt(refIt.nextInt()), aVal, storage.getInt(next)));
        }
        return this;
    }

    // REDUCE OPERATIONS

    @Override
    public Integer reduce(DArrayReduceOp op) {
        return op.reduceInt(loop, storage);
    }

    @Override
    public DArray<Integer> reduce1d(DArrayReduceOp op, int axis, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Integer> res = dm.zeros(dt, Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        int value = dm.stride(dt, strideLayout, storage).reduce(op);
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
    public DArray<Integer> reduceOn(DArrayReduceOp op, Shape shape, boolean keepDim, Order order) {
        if (shape.rank() == 0) {
            if (Order.C == order && layout.isCOrdered()) {
                return this;
            }
            if (Order.F == order && layout.isFOrdered()) {
                return this;
            }
            return this.copy(order);
        }
        if (rank() < shape.rank()) {
            throw new IllegalArgumentException(String.format(
                    "Reduce shape (%s) has a higher rank than the current tensor (%s).", shape, shape()));
        }
        for (int i = 0; i < shape.rank(); i++) {
            if (shape.dim(shape.rank() - 1 - i) != dim(rank() - 1 - i)) {
                throw new IllegalArgumentException(String.format(
                        "Reduce shape (%s) is incompatible with the shape of the current tensor (%s).", shape, shape()));
            }
        }
        if (rank() == shape.rank()) {
            return dm.scalar(dt, reduce(op));
        }

        int[] firstDims = Arrays.copyOfRange(layout.dims(), 0, rank() - shape.rank());
        int[] firstStrides = Arrays.copyOfRange(layout.strides(), 0, rank() - shape.rank());
        int[] lastDims = Arrays.copyOfRange(layout.dims(), rank() - shape.rank(), rank());
        int[] lastStrides = Arrays.copyOfRange(layout.strides(), rank() - shape.rank(), rank());

        StrideLayout firstLayout = StrideLayout.of(firstDims, layout().offset(), firstStrides);
        DArray<Integer> result = dm.zeros(dt, Shape.of(firstDims), order);
        PointerIterator resIt = result.ptrIterator(Order.C);
        PointerIterator firstIt = new StridePointerIterator(firstLayout, Order.C);

        while (resIt.hasNext()) {
            int ptr = resIt.nextInt();
            int offset = firstIt.nextInt();
            int value = dm.stride(dt, StrideLayout.of(lastDims, offset, lastStrides), storage).reduce(op);
            result.ptrSet(ptr, value);
        }
        if (keepDim) {
            for (int i = 0; i < shape.rank(); i++) {
                result = result.stretch(result.rank());
            }
        }
        return result;
    }

    @Override
    public DArray<Integer> reduceTo(DArrayReduceOp op, Shape targetShape, boolean keepDim, Order order) {
        if (targetShape.rank() == 0) {
            return dm.scalar(dt, reduce(op));
        }
        Broadcast.ElementWise broadcast = Broadcast.elementWise(this.shape(), targetShape);
        if (!broadcast.valid() || !broadcast.shape().equals(this.shape())) {
            throw new IllegalArgumentException(
                    "Target shape is not broadcastable to this tensor or the broadcast change the shape of current tensor.");
        }
        if (targetShape.equals(layout.shape())) {
            if (Order.C == order && layout.isCOrdered()) {
                return this;
            }
            if (Order.F == order && layout.isFOrdered()) {
                return this;
            }
            return this.copy(order);
        }

        int firstLen = 0;
        int lastLen = 0;
        for (int i = 0; i < layout.rank(); i++) {
            if (i < layout.rank() - targetShape.rank()) {
                firstLen++;
                continue;
            }
            if (dim(i) == targetShape.dim(i - layout.rank() + targetShape.rank())) {
                lastLen++;
            } else {
                lastLen++;
                firstLen++;
            }
        }

        int[] firstDims = new int[firstLen];
        int[] firstStrides = new int[firstLen];
        int[] lastDims = new int[lastLen];
        int[] lastStrides = new int[lastLen];

        int firstP = 0;
        int lastP = 0;
        for (int i = 0; i < layout.rank(); i++) {
            if (i < layout.rank() - targetShape.rank()) {
                firstDims[firstP] = layout.dim(i);
                firstStrides[firstP] = layout.stride(i);
                firstP++;
                continue;
            }
            if (dim(i) == targetShape.dim(i - layout.rank() + targetShape.rank())) {
                lastDims[lastP] = layout.dim(i);
                lastStrides[lastP] = layout.stride(i);
                lastP++;
            } else {
                firstDims[firstP] = layout.dim(i);
                firstStrides[firstP] = layout.stride(i);
                firstP++;
                lastDims[lastP] = targetShape.dim(i - layout.rank() + targetShape.rank());
                lastStrides[lastP] = layout.stride(i);
                lastP++;
            }
        }

        DArray<Integer> result = dm.zeros(dt, Shape.of(lastDims), order);
        PointerIterator resIt = result.ptrIterator(Order.C);
        PointerIterator lastIt = StrideLayout.of(lastDims, layout().offset(), lastStrides).ptrIterator(Order.C);

        int chunk = 128;
        int tasks = Math.ceilDiv(resIt.size(), chunk);

        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (resIt.hasNext() && taskList.size() < chunk) {
                    int ptr = resIt.nextInt();
                    int offset = lastIt.nextInt();
                    taskList.add(() -> {
                        int value = dm.stride(dt, StrideLayout.of(firstDims, offset, firstStrides), storage).reduce(op);
                        result.ptrSet(ptr, value);
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

        DArray<Integer> lastResult = result;
        if (keepDim) {
            for (int i = 0; i < layout.rank() - targetShape.rank(); i++) {
                lastResult = lastResult.stretch(0);
            }
        }

        return lastResult;
    }

    @Override
    public DArray<Integer> var1d(int axis, int ddof, DArray<?> mean, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Integer> res = dm.zeros(dt, Shape.of(newDims), Order.autoFC(order));
        if (!res.shape().equals(mean.shape())) {
            throw new IllegalArgumentException(String.format(
                    "Mean array %s must have the same shape as the result array %s.", mean.shape(), res.shape()));
        }

        var resIt = res.ptrIterator(Order.C);
        var meanIt = mean.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        int m = mean.ptrGetInt(meanIt.next());
                        int value = dm.stride(dt, strideLayout, storage).reduce(DArrayOp.reduceVarc(ddof, m));
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
    public DArray<Integer> varOn(Shape shape, int ddof, DArray<?> mean, boolean keepDim, Order order) {

        if (shape.rank() == 0) {
            throw new IllegalArgumentException("Shape must not be of rank zero.");
        }
        if (rank() < shape.rank()) {
            throw new IllegalArgumentException(String.format(
                    "Reduce shape (%s) has a higher rank than the current tensor (%s).", shape, shape()));
        }
        for (int i = 0; i < shape.rank(); i++) {
            if (shape.dim(shape.rank() - 1 - i) != dim(rank() - 1 - i)) {
                throw new IllegalArgumentException(String.format(
                        "Reduce shape (%s) is incompatible with the shape of the current tensor (%s).", shape, shape()));
            }
        }
        if (rank() == shape.rank()) {
            return dm.scalar(dt, var(ddof, mean.getInt()));
        }

        int[] firstDims = Arrays.copyOfRange(layout.dims(), 0, rank() - shape.rank());
        int[] firstStrides = Arrays.copyOfRange(layout.strides(), 0, rank() - shape.rank());
        int[] lastDims = Arrays.copyOfRange(layout.dims(), rank() - shape.rank(), rank());
        int[] lastStrides = Arrays.copyOfRange(layout.strides(), rank() - shape.rank(), rank());

        StrideLayout firstLayout = StrideLayout.of(firstDims, layout().offset(), firstStrides);
        if (mean.shape().equals(firstLayout.shape())) {
            throw new IllegalArgumentException("Mean darray must have the same shape as the result array.");
        }
        DArray<Integer> result = dm.zeros(dt, Shape.of(firstDims), order);
        PointerIterator resIt = result.ptrIterator(Order.C);
        PointerIterator firstIt = new StridePointerIterator(firstLayout, Order.C);
        PointerIterator meanIt = mean.ptrIterator(Order.C);

        while (resIt.hasNext()) {
            int ptr = resIt.nextInt();
            int offset = firstIt.nextInt();
            int value =
                    dm.stride(dt, StrideLayout.of(lastDims, offset, lastStrides), storage).var(ddof, mean.ptrGetInt(meanIt.next()));
            result.ptrSet(ptr, value);
        }
        if (keepDim) {
            for (int i = 0; i < shape.rank(); i++) {
                result = result.stretch(result.rank());
            }
        }
        return result;
    }

    @Override
    public int argmax(Order order) {
        int argmax = -1;
        int argvalue = ReduceOpMax.initInt;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dt().vs());
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.bound; j++) {
                int value = storage.getInt(p);
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
        if (keepDim) {
            newDims[axis] = 1;
            newStrides[axis] = 0;
        }

        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Integer> res = dm.zeros(DType.INTEGER, Shape.of(newDims), Order.autoFC(order));

        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        int value = dm.stride(dt, strideLayout, storage).argmax();
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
        if (keepDim) {
            newDims[axis] = 1;
            newStrides[axis] = 0;
        }

        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Integer> res = dm.zeros(DType.INTEGER, Shape.of(newDims), Order.autoFC(order));

        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        int tasks = (it.size() % chunk == 0) ? it.size() / chunk : it.size() / chunk + 1;
        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(tasks);
            for (int i = 0; i < tasks; i++) {
                List<Runnable> taskList = new ArrayList<>();
                while (it.hasNext() && taskList.size() < chunk) {
                    int ptr = it.nextInt();
                    int resPtr = resIt.next();
                    taskList.add(() -> {
                        StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                        int value = dm.stride(dt, strideLayout, storage).argmin();
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
        int argvalue = ReduceOpMin.initInt;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, dt().vs());
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.bound; j++) {
                int value = storage.getInt(p);
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
                if (dt().isNaN(storage.getInt(p))) {
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
                if (storage.getInt(p) == 0) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }


    // LINEAR ALGEBRA OPERATIONS

    @Override
    public Integer inner(DArray<?> other) {
        if (dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        return inner(other, 0, shape().dim(0));
    }

    @Override
    public Integer inner(DArray<?> other, int start, int end) {
        if (rank() != 1 || other.rank() != 1) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        if (start > end || start < 0 || end > other.dim(0) || end > dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        return innerUnchecked(other, start, end);
    }

    private Integer innerUnchecked(DArray<?> other, int start, int end) {
        BaseIntStrideDArray dts = (BaseIntStrideDArray) other;

        int step1 = loop.step;
        int step2 = dts.loop.step;

        if (step1 == 1 && step2 == 1) {
            return innerUncheckedUnit(dts, start, end);
        }

        int i = 0;
        int p1 = loop.offsets[0] + start * step1;
        int p2 = dts.loop.offsets[0] + start * step2;
        int sum = 0;

        if (storage.supportSimd() && dts.storage.supportSimd()) {
            int simdBound = Simd.vsInt.loopBound(end - start);
            if (simdBound > 0) {
                IntVector vsum = Simd.zeroInt();
                for (; i < simdBound; i += loop.simdLen) {
                    IntVector v1 = step1 == 1 ?
                            storage.getIntVector(p1) :
                            storage.getIntVector(p1, loop.simdIdx(), 0);
                    IntVector v2 = step2 == 1 ?
                            dts.storage.getIntVector(p2) :
                            dts.storage.getIntVector(p2, dts.loop.simdIdx(), 0);
                    vsum = vsum.add(v1.mul(v2));
                    p1 += loop.simdLen * step1;
                    p2 += dts.loop.simdLen * step2;
                }
                sum += vsum.reduceLanes(VectorOperators.ADD);
            }
        }
        for (; i < end - start; i++) {
            sum += (int) (storage.getInt(p1) * dts.storage.getInt(p2));
            p1 += step1;
            p2 += step2;
        }
        return sum;
    }

    private int innerUncheckedUnit(BaseIntStrideDArray dts, int start, int end) {
        int i = 0;
        int p1 = loop.offsets[0] + start;
        int p2 = dts.loop.offsets[0] + start;
        int sum = 0;

        if (storage.supportSimd() && dts.storage.supportSimd()) {
            int simdBound = Simd.vsInt.loopBound(end - start);
            if (simdBound > 0) {
                IntVector vsum = Simd.zeroInt();
                for (; i < simdBound; i += loop.simdLen) {
                    IntVector v1 = storage.getIntVector(p1);
                    IntVector v2 = dts.storage.getIntVector(p2);
                    vsum = vsum.add(v1.mul(v2));
                    p1 += loop.simdLen;
                    p2 += dts.loop.simdLen;
                }
                sum += vsum.reduceLanes(VectorOperators.ADD);
            }
        }
        for (; i < end - start; i++) {
            sum += (int) (storage.getInt(p1++) * dts.storage.getInt(p2++));
        }
        return sum;
    }

    @Override
    public DArray<Integer> mv(DArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 1 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-vector multiplication (m = %s, v = %s).",
                            shape(), other.shape()));
        }
        var result = dm.zeros(dt, Shape.of(shape().dim(0)), askOrder);
        for (int i = 0; i < shape().dim(0); i++) {
            result.ptrSetInt(i, selsq(0, i).inner(other));
        }
        return result;
    }

    @Override
    public DArray<Integer> bmv(DArray<?> other, Order askOrder) {
        BaseIntStrideDArray a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseIntStrideDArray) a.strexp(0, 1).strexp(1, 1);
        }
        if (other.isScalar()) {
            b = b.strexp(0, 1);
        }
        if (a.rank() == 2 && b.rank() == 1 && a.dim(1) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseIntStrideDArray) a.stretch(0)).bmvInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 3 && b.rank() == 1 && a.dim(2) == b.dim(0)) {
            // batch on matrix, add batch to vector
            return a.bmvInternal(b.strexp(0, a.dim(0)), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(1)) {
            // batch on vector, add batch to matrix
            return ((BaseIntStrideDArray) a.strexp(0, b.dim(0))).bmvInternal(b, askOrder);
        }
        if (a.rank() == 3 && b.rank() == 2 && a.dim(2) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bmvInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "DArrays are not valid for batch matrix vector multiplication (bm : %s, bv = %s)", shape(), other.shape()));
    }

    private DArray<Integer> bmvInternal(DArray<?> other, Order askOrder) {
        DArray<Integer> res = dm.zeros(dt, Shape.of(dim(0), dim(1)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).mv(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Integer> vtm(DArray<?> other, Order askOrder) {
        if (shape().rank() != 1 || other.rank() != 2 || shape().dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for vector transpose matrix multiplication (v = %s, m = %s).",
                            shape(), other.shape())
            );
        }
        var result = dm.zeros(dt, Shape.of(other.dim(1)), askOrder);
        for (int i = 0; i < other.dim(1); i++) {
            result.ptrSetInt(i, this.inner(other.selsq(1, i)));
        }
        return result;
    }

    @Override
    public DArray<?> bvtm(DArray<?> other, Order askOrder) {
        BaseIntStrideDArray a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseIntStrideDArray) a.stretch(0);
        }
        if (other.isScalar()) {
            b = b.stretch(0, 1);
        }
        if (a.rank() == 1 && b.rank() == 2 && a.dim(0) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseIntStrideDArray) a.stretch(0)).bvtmInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(0)) {
            // batch on vector, add batch to matrix
            return a.mm(b, askOrder);
        }
        if (a.rank() == 1 && b.rank() == 3 && a.dim(0) == b.dim(1)) {
            // batch on matrix, add batch to vector
            return ((BaseIntStrideDArray) a.strexp(0, b.dim(0))).bvtmInternal(b, askOrder);
        }
        if (a.rank() == 2 && b.rank() == 3 && a.dim(1) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bvtmInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "DArrays are not valid for batch vector transpose matrix multiplication (bv : %s, bm = %s)", shape(), other.shape()));
    }

    private DArray<Integer> bvtmInternal(DArray<?> other, Order askOrder) {
        DArray<Integer> res = dm.zeros(dt, Shape.of(dim(0), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).vtm(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Integer> mm(DArray<?> other, Order askOrder) {
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Illegal askOrder value, must be Order.C or Order.F");
        }
        return mm(other, dm.zeros(dt, Shape.of(shape().dim(0), other.shape().dim(1)), askOrder));
    }

    @Override
    public DArray<Integer> mm(DArray<?> other, DArray<?> to) {
        if (shape().rank() != 2 || other.shape().rank() != 2 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-matrix multiplication (m = %s, v = %s).", shape(), other.shape()));
        }
        if (to.dt() != dt) {
            throw new IllegalArgumentException("Target array has different data type than operation result.");
        }
        return mmInternalNonParallel(other, to.cast(dt));
    }

    private DArray<Integer> mmInternalNonParallel(DArray<?> other, DArray<Integer> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<DArray<Integer>> rows = unbind(0, false);
        List<DArray<Integer>> cols = other.cast(dt()).unbind(1, false);

        int off = ((StrideLayout) to.layout()).offset();
        int iStride = ((StrideLayout) to.layout()).stride(0);
        int jStride = ((StrideLayout) to.layout()).stride(1);

        for (int r = 0; r < m; r++) {
            for (int c = 0; c < p; c++) {
                var krow = (BaseIntStrideDArray) rows.get(r);
                to.ptrIncInt(off + r * iStride + c * jStride, (int) (krow.innerUnchecked(cols.get(c), 0, n)));
            }
        }
        return to;
    }

    private DArray<Integer> mmInternalParallel(DArray<?> other, DArray<Integer> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        List<DArray<Integer>> rows = unbind(0, false);
        List<DArray<Integer>> cols = other.cast(dt()).unbind(1, false);

        int chunk = (int) Math.floor(Math.sqrt(L2_CACHE_SIZE / 2. / CORES / dt().byteCount()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) Math.ceil(Math.sqrt(chunk / 4.)) : (int) Math.ceil(Math.sqrt(chunk));

        int off = ((StrideLayout) to.layout()).offset();
        int iStride = ((StrideLayout) to.layout()).stride(0);
        int jStride = ((StrideLayout) to.layout()).stride(1);

        CountDownLatch latch = new CountDownLatch(Math.ceilDiv(m, innerChunk) * Math.ceilDiv(p, innerChunk));
        try (ExecutorService service = Executors.newFixedThreadPool(dm.cpuThreads())) {

            for (int r = 0; r < m; r += innerChunk) {
                int rs = r;
                int re = Math.min(m, r + innerChunk);

                for (int c = 0; c < p; c += innerChunk) {
                    int cs = c;
                    int ce = Math.min(p, c + innerChunk);

                    service.submit(() -> {
                        for (int k = 0; k < n; k += vectorChunk) {
                            int end = Math.min(n, k + vectorChunk);
                            for (int i = rs; i < re; i++) {
                                var krow = (BaseIntStrideDArray) rows.get(i);
                                int offset = off + i * iStride;
                                for (int j = cs; j < ce; j++) {
                                    to.ptrIncInt(offset + j * jStride, (int) (krow.innerUnchecked(cols.get(j), k, end)));
                                }
                            }
                        }
                        latch.countDown();
                    });
                }
            }

            try {
                latch.await();
                service.shutdown();
                service.shutdownNow();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return to;
    }

    @Override
    public DArray<Integer> bmm(DArray<?> other, Order askOrder) {
        if (rank() == 2 && other.rank() == 2 && dim(1) == other.dim(0)) {
            return ((BaseIntStrideDArray) stretch(0)).bmmInternal(other.stretch(0), askOrder);
        }
        if (rank() == 3 && other.rank() == 2 && dim(2) == other.dim(0)) {
            return bmmInternal(other.strexp(0, dim(0)), askOrder);
        }
        if (rank() == 2 && other.rank() == 3 && dim(1) == other.dim(1)) {
            return ((BaseIntStrideDArray) strexp(0, other.dim(0))).bmmInternal(other, askOrder);
        }
        if (rank() == 3 && other.rank() == 3 && dim(0) == other.dim(0) && dim(2) == other.dim(1)) {
            return bmmInternal(other, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "DArrays are not valid for batch matrix-matrix multiplication (bm1: %s, bm2: %s)", shape(), other.shape()));
    }

    private DArray<Integer> bmmInternal(DArray<?> other, Order askOrder) {
        DArray<Integer> res = dm.zeros(dt, Shape.of(dim(0), dim(1), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            ((BaseIntStrideDArray) selsq(0, b)).mmInternalParallel(other.selsq(0, b), res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Integer> conv1d(DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {

        // complete input shape
        if (this.rank() > 3) {
            throw new IllegalArgumentException("Input signal (this darray) must have at most 3 dimensions.");
        }
        DArray<Integer> input = this;
        while (input.rank() < 3) {
            // missing dimensions means sequence of length 1, a single channel and a batch of unit size
            input = input.stretch(0);
        }

        // complete kernel shape
        if (kernel.rank() > 3) {
            throw new IllegalArgumentException("Weight must have at most 3 dimensions.");
        }
        while (kernel.rank() < 3) {
            kernel = kernel.stretch(0);
        }

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inLen = input.dim(2);

        int outChannels = kernel.dim(0);
        int inDepth = kernel.dim(1);
        int k = kernel.dim(2);

        if (bias != null) {
            if (bias.rank() != 1) {
                throw new IllegalArgumentException("Bias must be a 1D tensor.");
            }
            if (bias.dim(0) != outChannels) {
                throw new IllegalArgumentException("Bias must have the same number of elements as output channels.");
            }
        }

        if (inChannels % groups != 0) {
            throw new IllegalArgumentException("Number of input channels must be a multiple of groups.");
        }
        if (outChannels % groups != 0) {
            throw new IllegalArgumentException("Number of output channels must be a multiple of groups.");
        }
        if (inDepth * groups != inChannels) {
            throw new IllegalArgumentException("Number of input channels divided by groups must be equal to kernel input depth.");
        }

        int outDepth = outChannels / groups;
        int outLen = Math.floorDiv(inLen + 2 * padding - (k - 1) * dilation, stride);

        DArray<Integer> output = input.dm().zeros(DType.INTEGER, Shape.of(n, outChannels, outLen));

        for (int batch = 0; batch < n; batch++) {

            DArray<Integer> inBatch = input.selsq(0, batch); // shape: (inChannels, inLen)
            DArray<Integer> outBatch = output.selsq(0, batch); // shape: (outChannels, outLen)

            for (int group = 0; group < groups; group++) {

                DArray<Integer> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth); // shape: (inDepth, inLen)
                DArray<Integer> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth); // shape: (outDepth, outLen)

                DArray<?> kernelSlice = kernel.narrow(0, group * outDepth, (group + 1) * outDepth); // shape: (outDepth, inDepth, k)

                DArray<Integer> unfold1d = inSlice.unfold1d(k, stride, padding, dilation);

                kernelSlice = kernelSlice.reshape(Shape.of(outDepth, inDepth * k));
                outSlice.add_(kernelSlice.mm(unfold1d));
            }
        }

        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getInt(oc));
            }
        }
        return output;
    }

    @Override
    public DArray<Integer> convTranspose1d(DArray<?> weights, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        DArray<Integer> input = this;

        if (input.rank() > 3) {
            throw new IllegalArgumentException("Input signal (this darray) must have at most 3 dimensions.");
        }
        while (input.rank() < 3) {
            input = input.stretch(0);
        }
        if (weights.rank() > 3) {
            throw new IllegalArgumentException("Weight must have at most 3 dimensions.");
        }
        while (weights.rank() < 3) {
            weights = weights.stretch(0);
        }

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inLen = input.dim(2);
        int outDepth = weights.dim(1);
        int inDepth = inChannels / groups;
        int outChannels = outDepth * groups;
        int kLen = weights.dim(2);

        if (bias != null) {
            if (bias.rank() != 1) {
                throw new IllegalArgumentException("Bias must be a 1D tensor.");
            }
            if (bias.dim(0) != outChannels) {
                throw new IllegalArgumentException("Bias must have the same number of elements as output channels.");
            }
        }

        if (input.dim(1) != weights.dim(0)) {
            throw new IllegalArgumentException("Input channels and weight output channels do not match.");
        }
        if (inChannels % groups != 0) {
            throw new IllegalArgumentException("Number of input channels must be a multiple of groups.");
        }
        if (outChannels % groups != 0) {
            throw new IllegalArgumentException("Number of output channels must be a multiple of groups.");
        }

        int outLen = (inLen - 1) * stride - 2 * padding + (kLen - 1) * dilation + 1 + outputPadding;

        DArray<Integer> output = input.dm().zeros(DType.INTEGER, Shape.of(n, outChannels, outLen));

        for (int batch = 0; batch < n; batch++) {
            DArray<?> inBatch = input.selsq(0, batch);
            DArray<?> outBatch = output.selsq(0, batch);

            for (int group = 0; group < groups; group++) {
                DArray<?> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, inLen)
                DArray<?> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth); // (outDepth, outLen)
                DArray<?> kernelSlice = weights.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, outDepth, kLen)

                // col = kernelFlat^T @ inSlice: (outDepth*kLen, inDepth) @ (inDepth, inLen) = (outDepth*kLen, inLen)
                DArray<?> kernelFlat = kernelSlice.reshape(Shape.of(inDepth, outDepth * kLen)); // (inDepth, outDepth*kLen)
                DArray<?> col = kernelFlat.t().mm(inSlice); // (outDepth*kLen, inLen)

                // fold col (outDepth, kLen, inLen) -> outSlice (outDepth, outLen)
                for (int oc = 0; oc < outDepth; oc++) {
                    for (int k = 0; k < kLen; k++) {
                        for (int i = 0; i < inLen; i++) {
                            int outPos = i * stride + k * dilation - padding;
                            if (outPos >= 0 && outPos < outLen) {
                                outSlice.incInt(col.getInt(oc * kLen + k, i), oc, outPos);
                            }
                        }
                    }
                }
            }
        }
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getInt(oc));
            }
        }
        return output;
    }

    @Override
    public DArray<Integer> unfold1d(int kLen, int stride, int padding, int dilation) {

        if (this.rank() != 2 && this.rank() != 3) {
            throw new IllegalArgumentException("Input must be a 2D or 3D array.");
        }

        boolean batched = this.rank() == 3;
        DArray<Integer> input = batched ? this : this.stretch(0);

        int n = input.dim(0);
        int inCh = input.dim(1);

        int outLen = Math.floorDiv(input.dim(2) + 2 * padding - (kLen - 1) * dilation, stride);

        DArray<Integer> result = this.dm().zeros(DType.INTEGER, Shape.of(n, inCh, kLen, outLen));

        for (int b = 0; b < n; b++) {
            for (int c = 0; c < inCh; c++) {
                for (int k = 0; k < kLen; k++) {
                    for (int o = 0; o < outLen; o++) {
                        int delta = o * stride + k * dilation - padding;
                        if (delta >= 0 && delta < input.dim(2)) {
                            result.setInt(input.getInt(b, c, delta), b, c, k, o);
                        }
                    }
                }
            }
        }
        result = result.reshape(Shape.of(n, inCh * kLen, outLen));
        if (!batched) {
            result = result.squeeze(0);
        }
        return result;
    }

    @Override
    public DArray<Integer> conv2d(DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        if (this.rank() > 4) {
            throw new IllegalArgumentException(String.format(
                    "Input must have at most 4 dimensions, but it has %d.", this.rank()));
        }
        DArray<Integer> in = this;
        while (in.rank() < 4) {
            in = in.stretch(0);
        }
        while (kernel.rank() < 4) {
            kernel = kernel.stretch(0);
        }

        var input = in;
        var kk = kernel;

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inH = input.dim(2);
        int inW = input.dim(3);
        int outChannels = kernel.dim(0);
        int inDepth = kernel.dim(1);
        int kH = kernel.dim(2);
        int kW = kernel.dim(3);

        if (inDepth * groups != inChannels) {
            throw new IllegalArgumentException(
                    String.format("inDepth x groups (%d x %d) must equal inChannels (%d).", inDepth, groups, inChannels));
        }
        int outDepth = outChannels / groups;
        int outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Integer> output = dm.zeros(DType.INTEGER, Shape.of(n, outChannels, outH, outW));

        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(n);
            for (int batch = 0; batch < n; batch++) {
                int b = batch;
                executor.submit(() -> {
                    DArray<Integer> inBatch = input.selsq(0, b);   // (inChannels, inH, inW)
                    DArray<Integer> outBatch = output.selsq(0, b); // (outChannels, outH, outW)

                    var inSlices = inBatch.chunk(0, true, inDepth);
                    var outSlices = outBatch.chunk(0, true, outDepth);
                    var kernelSlices = kk.chunk(0, true, outDepth);
                    for (int group = 0; group < groups; group++) {

                        // im2col: (inDepth * kH * kW, outH * outW)
                        DArray<Integer> col = inSlices.get(group).unfold2d(kH, kW, stride, padding, dilation);
                        DArray<?> kernelSlice = kernelSlices.get(group);
                        DArray<?> kFlat = kernelSlice.reshape(Shape.of(outDepth, inDepth * kH * kW), Order.C);
                        // (outDepth, outH*outW)
                        kFlat.mm(col, outSlices.get(group).reshape(Shape.of(outDepth, outH * outW)));
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException _) {
            }
        }

        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getInt(oc));
            }
        }
        return output;
    }

    @Override
    public DArray<Integer> unfold2d(int kH, int kW, int stride, int padding, int dilation) {
        boolean batched = this.rank() == 4;
        if (this.rank() != 3 && rank() != 4) {
            throw new IllegalArgumentException("Input must be a 3D or 4D array.");
        }
        DArray<Integer> input = batched ? this : this.stretch(0);

        int n = input.dim(0);
        int inC = input.dim(1);

        int inH = input.dim(2);
        int inW = input.dim(3);
        int outLenH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outLenW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Integer> result = dm.zeros(DType.INTEGER, Shape.of(n, inC, kH, kW, outLenH, outLenW));
        for (int b = 0; b < n; b++) {
            for (int c = 0; c < inC; c++) {
                for (int kh = 0; kh < kH; kh++) {
                    for (int kw = 0; kw < kW; kw++) {
                        for (int oh = 0; oh < outLenH; oh++) {
                            for (int ow = 0; ow < outLenW; ow++) {
                                int ih = oh * stride + kh * dilation - padding;
                                int iw = ow * stride + kw * dilation - padding;
                                if (ih >= 0 && ih < inH && iw >= 0 && iw < inW) {
                                    result.setInt(input.getInt(b, c, ih, iw), b, c, kh, kw, oh, ow);
                                }
                            }
                        }
                    }
                }
            }
        }
        var ret = result.reshape(Shape.of(n, inC * kH * kW, outLenH * outLenW));
        if (!batched) {
            ret = ret.squeeze(0);
        }
        return ret;
    }

    @Override
    public DArray<Integer> convTranspose2d(DArray<?> w, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        DArray<Integer> input = this;
        while (input.rank() < 4) {
            input = input.stretch(0);
        }
        while (w.rank() < 4) {
            w = w.stretch(0);
        }
        var weights = w;

        int n = input.dim(0);
        int inChannels = input.dim(1);
        int inH = input.dim(2);
        int inW = input.dim(3);
        int outDepth = weights.dim(1);
        int inDepth = inChannels / groups;
        int outChannels = outDepth * groups;
        int kH = weights.dim(2);
        int kW = weights.dim(3);

        if (input.dim(1) != weights.dim(0)) {
            throw new IllegalArgumentException("Input channels and weight output channels do not match.");
        }

        int outH = (inH - 1) * stride - 2 * padding + dilation * (kH - 1) + 1 + outputPadding;
        int outW = (inW - 1) * stride - 2 * padding + dilation * (kW - 1) + 1 + outputPadding;

        DArray<Integer> output = dm.zeros(DType.INTEGER, Shape.of(n, outChannels, outH, outW));

        try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
            CountDownLatch latch = new CountDownLatch(n);
            for (int batch = 0; batch < n; batch++) {
                DArray<?> inBatch = input.selsq(0, batch);
                DArray<?> outBatch = output.selsq(0, batch);

                executor.submit(() -> {
                    var inSlices = inBatch.chunk(0, true, inDepth);
                    var outSlices = outBatch.chunk(0, true, outDepth);
                    var kernelSlices = weights.chunk(0, true, inDepth);
                    for (int group = 0; group < groups; group++) {
                        var inSlice = inSlices.get(group);
                        var outSlice = outSlices.get(group);
                        var kernelSlice = kernelSlices.get(group);
                        for (int ih = 0; ih < inH; ih++) {
                            for (int iw = 0; iw < inW; iw++) {
                                for (int c = 0; c < inDepth; c++) {
                                    int val = inSlice.getInt(c, ih, iw);
                                    for (int kh = 0; kh < kH; kh++) {
                                        int oh = ih * stride + kh * dilation - padding;
                                        if (oh >= 0 && oh < outH) {
                                            int ow = iw * stride - padding;
                                            for (int kw = 0; kw < kW; kw++) {
                                                if (ow >= 0 && ow < outW) {
                                                    for (int oc = 0; oc < outDepth; oc++) {
                                                        outSlice.incInt((int) (val * kernelSlice.getInt(c, oc, kh, kw)), oc, oh,
                                                                ow);
                                                    }
                                                }
                                                ow += dilation;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException _) {
            }
        }

        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getInt(oc));
            }
        }
        return output;
    }

    @Override
    public DArray<Integer> conv3d(DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        DArray<Integer> input = this;
        while (input.rank() < 5) {
            input = input.stretch(0);
        }
        while (kernel.rank() < 5) {
            kernel = kernel.stretch(0);
        }

        int n = input.dim(0), inChannels = input.dim(1);
        int inD = input.dim(2), inH = input.dim(3), inW = input.dim(4);
        int outChannels = kernel.dim(0), inDepth = kernel.dim(1);
        int kD = kernel.dim(2), kH = kernel.dim(3), kW = kernel.dim(4);

        if (inDepth * groups != inChannels) {
            throw new IllegalArgumentException("inDepth * groups must equal inChannels.");
        }
        int outDepth = outChannels / groups;
        int outD = Math.floorDiv(inD + 2 * padding - dilation * (kD - 1) - 1, stride) + 1;
        int outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Integer> output = dm.zeros(DType.INTEGER, Shape.of(n, outChannels, outD, outH, outW));
        for (int batch = 0; batch < n; batch++) {
            DArray<?> inBatch = input.selsq(0, batch);
            DArray<?> outBatch = output.selsq(0, batch);
            for (int group = 0; group < groups; group++) {
                DArray<?> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth);
                DArray<?> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth);
                DArray<?> kernelSlice = kernel.narrow(0, group * outDepth, (group + 1) * outDepth);
                DArray<?> unfold = inSlice.unfold3d(kD, kH, kW, stride, padding, dilation);
                outSlice.add_(kernelSlice.reshape(Shape.of(outDepth, inDepth * kD * kH * kW)).mm(unfold)
                        .reshape(Shape.of(outDepth, outD, outH, outW)));
            }
        }
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getInt(oc));
            }
        }
        return output;
    }

    @Override
    public DArray<Integer> unfold3d(int kD, int kH, int kW, int stride, int padding, int dilation) {

        boolean batched = this.rank() == 5;
        DArray<Integer> input = this;
        if (!batched) {
            if (this.rank() != 4) {
                throw new IllegalArgumentException("Input must be a 4D or 5D array.");
            }
            input = input.stretch(0);
        }

        int n = input.dim(0);
        int inCh = input.dim(1);

        int inD = input.dim(2);
        int inH = input.dim(3);
        int inW = input.dim(4);

        int outD = Math.floorDiv(inD + 2 * padding - dilation * (kD - 1) - 1, stride) + 1;
        int outH = Math.floorDiv(inH + 2 * padding - dilation * (kH - 1) - 1, stride) + 1;
        int outW = Math.floorDiv(inW + 2 * padding - dilation * (kW - 1) - 1, stride) + 1;

        DArray<Integer> result = dm.zeros(DType.INTEGER, Shape.of(n, inCh, kD, kH, kW, outD, outH, outW));
        for (int b = 0; b < n; b++) {
            for (int c = 0; c < inCh; c++) {
                for (int kd = 0; kd < kD; kd++) {
                    for (int kh = 0; kh < kH; kh++) {
                        for (int kw = 0; kw < kW; kw++) {
                            for (int od = 0; od < outD; od++) {
                                for (int oh = 0; oh < outH; oh++) {
                                    for (int ow = 0; ow < outW; ow++) {
                                        int id = od * stride + kd * dilation - padding;
                                        int ih = oh * stride + kh * dilation - padding;
                                        int iw = ow * stride + kw * dilation - padding;
                                        if (id >= 0 && id < inD && ih >= 0 && ih < inH && iw >= 0 && iw < inW) {
                                            result.setInt(input.getInt(b, c, id, ih, iw), b, c, kd, kh, kw, od, oh, ow);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        result = result.reshape(Shape.of(n, inCh * kD * kH * kW, outD * outH * outW));
        if (!batched) {
            result = result.squeeze(0);
        }
        return result;
    }

    @Override
    public DArray<Integer> convTranspose3d(DArray<?> weights, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        DArray<Integer> input = this;
        while (input.rank() < 5) {
            input = input.stretch(0);
        }
        while (weights.rank() < 5) {
            weights = weights.stretch(0);
        }

        int n = input.dim(0), inChannels = input.dim(1);
        int inD = input.dim(2), inH = input.dim(3), inW = input.dim(4);
        int outDepth = weights.dim(1), inDepth = inChannels / groups;
        int outChannels = outDepth * groups;
        int kD = weights.dim(2), kH = weights.dim(3), kW = weights.dim(4);

        if (input.dim(1) != weights.dim(0)) {
            throw new IllegalArgumentException("Input channels and weight output channels do not match.");
        }

        int outD = (inD - 1) * stride - 2 * padding + dilation * (kD - 1) + 1 + outputPadding;
        int outH = (inH - 1) * stride - 2 * padding + dilation * (kH - 1) + 1 + outputPadding;
        int outW = (inW - 1) * stride - 2 * padding + dilation * (kW - 1) + 1 + outputPadding;

        DArray<Integer> output = dm.zeros(DType.INTEGER, Shape.of(n, outChannels, outD, outH, outW));
        for (int batch = 0; batch < n; batch++) {
            DArray<?> inBatch = input.selsq(0, batch);
            DArray<?> outBatch = output.selsq(0, batch);
            for (int group = 0; group < groups; group++) {
                DArray<?> inSlice = inBatch.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, inD, inH, inW)
                DArray<?> outSlice = outBatch.narrow(0, group * outDepth, (group + 1) * outDepth); // (outDepth, outD, outH, outW)
                DArray<?> kernelSlice = weights.narrow(0, group * inDepth, (group + 1) * inDepth); // (inDepth, outDepth, kD, kH, kW)

                // col = kernelFlat^T @ inSlice_flat: (outDepth*kD*kH*kW, inDepth) @ (inDepth, inD*inH*inW) = (outDepth*kD*kH*kW, inD*inH*inW)
                DArray<?> kernelFlat = kernelSlice.reshape(Shape.of(inDepth, outDepth * kD * kH * kW));
                DArray<?> col = kernelFlat.t().mm(inSlice.reshape(Shape.of(inDepth, inD * inH * inW)));

                // fold col -> outSlice
                for (int oc = 0; oc < outDepth; oc++) {
                    for (int kd = 0; kd < kD; kd++) {
                        for (int kh = 0; kh < kH; kh++) {
                            for (int kw = 0; kw < kW; kw++) {
                                int colRow = oc * kD * kH * kW + kd * kH * kW + kh * kW + kw;
                                for (int id = 0; id < inD; id++) {
                                    int od = id * stride + kd * dilation - padding;
                                    if (od >= 0 && od < outD) {
                                        for (int ih = 0; ih < inH; ih++) {
                                            int oh = ih * stride + kh * dilation - padding;
                                            if (oh >= 0 && oh < outH) {
                                                for (int iw = 0; iw < inW; iw++) {
                                                    int ow = iw * stride + kw * dilation - padding;
                                                    if (ow >= 0 && ow < outW) {
                                                        outSlice.incInt(col.getInt(colRow, id * inH * inW + ih * inW + iw), oc, od,
                                                                oh, ow);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (bias != null) {
            for (int oc = 0; oc < outChannels; oc++) {
                output.narrow(1, oc, oc + 1).add_(bias.getInt(oc));
            }
        }
        return output;
    }


    @Override
    public Integer trace() {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("This operation is available only on matrix.");
        }
        if (dim(0) != dim(1)) {
            throw new OperationNotAvailableException("This operation is available only on a square matrix.");
        }
        int trace = 0;
        for (int i = 0; i < dim(0); i++) {
            trace += getInt(i, i);
        }
        return trace;
    }

    @Override
    public DArray<Integer> diag(int diagonal) {
        if (isScalar() && diagonal == 0) {
            return this;
        }
        if (isVector()) {
            int n = dim(0) + Math.abs(diagonal);
            DArray<Integer> m = dm.zeros(dt, Shape.of(n, n));
            for (int i = 0; i < dim(0); i++) {
                m.setInt(getInt(i), i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return m;
        }
        if (isMatrix()) {
            int d = diagonal >= 0 ? dim(1) : dim(0);
            int len = diagonal >= 0 ? d - diagonal : d + diagonal;
            if (len <= 0) {
                throw new IllegalArgumentException("Diagonal " + diagonal + " does not exists for shape " + shape() + ".");
            }
            int[] diag = new int[len];
            for (int i = 0; i < len; i++) {
                diag[i] = getInt(i + Math.abs(Math.min(diagonal, 0)), i + Math.max(diagonal, 0));
            }
            return dm.stride(dt, Shape.of(len), Order.defaultOrder(), diag);
        }
        throw new OperationNotAvailableException("This operation is available for tensors with shape " + shape() + ".");
    }

    @Override
    public Integer norm(
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
            return (int) shape().size();
        }
        if (pow == 1) {
            return abs().sum();
        }
        if (pow == 2) {
            return (int) Math.sqrt(sqr().sum());
        }
        int sum = (int) 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += (int) Math.pow(Math.abs(storage.getInt(p)), pow);
                p += loop.step;
            }
        }
        return (int) Math.pow(sum, 1. / pow);
    }

    @Override
    public DArray<Integer> normalize_(
            // FREEZE
            double pow
    ) {
        return div_(norm(pow));
    }

    @Override
    public DArray<Integer> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = dm.storageManager().zeros(dt, size());
        var dst = dm.stride(dt, StrideLayout.ofDense(shape(), 0, askOrder), copy);

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
                copy.setInt(last++, storage.getInt(p));
                p += loop.step;
            }
        }
    }

    @Override
    public DArray<Integer> copyTo(DArray<Integer> to) {

        Order askOrder = Layout.storageFastTandemOrder(layout, to.layout());

        if (to instanceof BaseIntStrideDArray dst) {

            int limit = Math.floorDiv(L2_CACHE_SIZE, dt().byteCount() * 2 * dm.cpuThreads() * 8);

            if (layout.size() > limit) {

                int[] slices = Arrays.copyOf(layout.dims(), layout.rank());
                int size = Ints.prod(slices, 0, slices.length);
                while (size > limit) {
                    int axis = Ints.argmax(slices, 0, slices.length);
                    size = size * (slices[axis] / 2) / slices[axis];
                    slices[axis] = slices[axis] / 2;
                }

                int[] lens = new int[slices.length];
                for (int i = 0; i < lens.length; i++) {
                    lens[i] = Math.ceilDiv(layout().dim(i), slices[i]);
                }

                int[] starts = new int[slices.length];
                int[] ends = new int[slices.length];

                try (ExecutorService executor = Executors.newFixedThreadPool(dm.cpuThreads())) {
                    List<Future<?>> futures = new ArrayList<>();
                    Stack<Integer> stack = new Stack<>();
                    boolean loop = true;
                    while (!stack.isEmpty() || loop) {
                        int level = stack.size();
                        if (loop) {
                            if (level == slices.length) {
                                int[] ss = Ints.copy(starts);
                                int[] es = Ints.copy(ends);
                                futures.add(executor.submit(() -> {
                                    BaseIntStrideDArray s = (BaseIntStrideDArray) this.narrowAll(false, ss, es);
                                    BaseIntStrideDArray d = (BaseIntStrideDArray) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseIntStrideDArray src, BaseIntStrideDArray dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder, dt().vs());
        var it2 = dst.ptrIterator(askOrder);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                dst.storage.setInt(it2.nextInt(), src.storage.getInt(p));
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
