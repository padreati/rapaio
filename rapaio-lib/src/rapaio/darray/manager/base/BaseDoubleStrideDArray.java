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

import static rapaio.util.Hardware.L2_CACHE_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Layout;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.PointerIterator;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.iterators.StridePointerIterator;
import rapaio.darray.iterators.TandemStrideLoopDescriptor;
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
import rapaio.util.Pair;
import rapaio.util.collection.Ints;
import rapaio.util.function.IntIntBiFunction;

public final class BaseDoubleStrideDArray extends AbstractStrideDArray<Double> {

    public BaseDoubleStrideDArray(DArrayManager dm, StrideLayout layout, Storage storage) {
        super(dm, DType.DOUBLE, layout, storage);
    }

    @Override
    public Double get(int... indices) {
        return storage.getDouble(layout.pointer(indices));
    }

    @Override
    public void set(Double value, int... indices) {
        storage.setDouble(layout.pointer(indices), value);
    }

    @Override
    public void inc(Double value, int... indices) {
        storage.incDouble(layout.pointer(indices), value);
    }

    @Override
    public Double ptrGet(int ptr) {
        return storage.getDouble(ptr);
    }

    @Override
    public void ptrSet(int ptr, Double value) {
        storage.setDouble(ptr, value);
    }

    public final Iterator<Double> iterator(Order askOrder) {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED | Spliterator.IMMUTABLE), false)
                .map(storage::getDouble).iterator();
    }

    @Override
    public BaseDoubleStrideDArray apply_(Order askOrder, IntIntBiFunction<Double> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setDouble(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public DArray<Double> apply_(Function<Double, Double> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setDouble(ptr, fun.apply(storage.getDouble(ptr)));
        }
        return this;
    }

    @Override
    public DArray<Double> unary_(DArrayUnaryOp op) {
        if (op.floatingPointOnly() && !dt().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point DArrays.");
        }
        op.applyDouble(loop(), storage);
        return this;
    }

    @Override
    public DArray<Double> unary1d_(DArrayUnaryOp op, int axis) {
        if (axis < 0) {
            axis += shape().rank();
        }

        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        // one task per group of rows; the number of rows is the size of the iterator, not the size of the axis
        int chunk = 64;
        List<Runnable> chunks = new ArrayList<>();
        while (it.hasNext()) {
            List<Runnable> taskList = new ArrayList<>(chunk);
            while (it.hasNext() && taskList.size() < chunk) {
                int ptr = it.nextInt();
                taskList.add(() -> {
                    dm.stride(dt, StrideLayout.of(new int[] {selDim}, ptr, new int[] {selStride}), storage).unary_(op);
                });
            }
            chunks.add(() -> taskList.forEach(Runnable::run));
        }
        dm.execute(size(), chunks);
        return this;
    }

    @Override
    public DArray<Double> binary_(DArrayBinaryOp op, DArray<?> other) {
        if (other.isScalar()) {
            return binary_(op, other.getDouble());
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
        // same data type is required for vector loads; cast() returns the same instance when types already agree
        DArray<Double> o = other.cast(dt);
        Storage os = o.storage();
        Order order = Layout.storageFastTandemOrder(layout, o.layout());
        var td = TandemStrideLoopDescriptor.of(layout, (StrideLayout) o.layout(), order, Simd.vsDouble);
        boolean simd = storage.supportSimd() && os.supportSimd();

        for (int k = 0; k < td.offsets1.length; k++) {
            int p1 = td.offsets1[k];
            int p2 = td.offsets2[k];
            int i = 0;
            if (simd) {
                if (td.step1 == 1 && td.step2 == 1) {
                    // both operands contiguous: plain vector loads and stores
                    for (; i < td.simdBound; i += td.simdLen) {
                        DoubleVector a = storage.getDoubleVector(p1);
                        DoubleVector b = os.getDoubleVector(p2);
                        storage.setDoubleVector(op.applyDouble(a, b), p1);
                        p1 += td.simdLen;
                        p2 += td.simdLen;
                    }
                } else if (td.step2 == 0) {
                    // second operand is broadcast along the inner loop: load it once per inner loop
                    DoubleVector b = DoubleVector.broadcast(Simd.vsDouble, os.getDouble(p2));
                    if (td.step1 == 1) {
                        for (; i < td.simdBound; i += td.simdLen) {
                            DoubleVector a = storage.getDoubleVector(p1);
                            storage.setDoubleVector(op.applyDouble(a, b), p1);
                            p1 += td.simdLen;
                        }
                    } else {
                        for (; i < td.simdBound; i += td.simdLen) {
                            DoubleVector a = storage.getDoubleVector(p1, td.simdIdx1(), 0);
                            storage.setDoubleVector(op.applyDouble(a, b), p1, td.simdIdx1(), 0);
                            p1 += td.simdLen * td.step1;
                        }
                    }
                } else {
                    // at least one strided operand: gather/scatter on the strided side
                    for (; i < td.simdBound; i += td.simdLen) {
                        DoubleVector a = td.step1 == 1 ? storage.getDoubleVector(p1) : storage.getDoubleVector(p1, td.simdIdx1(), 0);
                        DoubleVector b = td.step2 == 1 ? os.getDoubleVector(p2) : os.getDoubleVector(p2, td.simdIdx2(), 0);
                        DoubleVector r = op.applyDouble(a, b);
                        if (td.step1 == 1) {
                            storage.setDoubleVector(r, p1);
                        } else {
                            storage.setDoubleVector(r, p1, td.simdIdx1(), 0);
                        }
                        p1 += td.simdLen * td.step1;
                        p2 += td.simdLen * td.step2;
                    }
                }
            }
            for (; i < td.bound; i++) {
                storage.setDouble(p1, op.applyDouble(storage.getDouble(p1), os.getDouble(p2)));
                p1 += td.step1;
                p2 += td.step2;
            }
        }
        return this;
    }

    @Override
    public <M extends Number> DArray<Double> binary_(DArrayBinaryOp op, M value) {
        StrideLoopDescriptor loop = loop();
        double v = value.doubleValue();
        DoubleVector m = DoubleVector.broadcast(Simd.vsDouble, v);
        for (int p : loop.offsets) {
            int i = 0;
            if (storage.supportSimd()) {
                if (loop.step == 1) {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        DoubleVector a = storage.getDoubleVector(p);
                        a = op.applyDouble(a, m);
                        storage.setDoubleVector(a, p);
                        p += loop.simdLen;
                    }
                } else {
                    for (; i < loop.simdBound; i += loop.simdLen) {
                        DoubleVector a = storage.getDoubleVector(p, loop.simdIdx(), 0);
                        a = op.applyDouble(a, m);
                        storage.setDoubleVector(a, p, loop.simdIdx(), 0);
                        p += loop.simdLen * loop.step;
                    }
                }
            }
            for (; i < loop.bound; i++) {
                storage.setDouble(p, op.applyDouble(storage.getDouble(p), v));
                p += loop.step;
            }
        }
        return this;
    }

    @Override
    public DArray<Double> fma_(Double a, DArray<?> t) {
        if (t.isScalar()) {
            double tVal = t.getDouble();
            return add_((double) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("DArrays does not have the same shape.");
        }
        double aVal = a;
        // same data type is required for vector loads; cast() returns the same instance when types already agree
        DArray<Double> o = t.cast(dt);
        Storage os = o.storage();
        Order order = Layout.storageFastTandemOrder(layout, o.layout());
        var td = TandemStrideLoopDescriptor.of(layout, (StrideLayout) o.layout(), order, Simd.vsDouble);
        boolean simd = storage.supportSimd() && os.supportSimd();
        DoubleVector av = DoubleVector.broadcast(Simd.vsDouble, aVal);

        for (int k = 0; k < td.offsets1.length; k++) {
            int p1 = td.offsets1[k];
            int p2 = td.offsets2[k];
            int i = 0;
            if (simd) {
                if (td.step1 == 1 && td.step2 == 1) {
                    for (; i < td.simdBound; i += td.simdLen) {
                        DoubleVector x = storage.getDoubleVector(p1);
                        DoubleVector tv = os.getDoubleVector(p2);
                        storage.setDoubleVector(Simd.fma(tv, av, x), p1);
                        p1 += td.simdLen;
                        p2 += td.simdLen;
                    }
                } else {
                    for (; i < td.simdBound; i += td.simdLen) {
                        DoubleVector x = td.step1 == 1 ? storage.getDoubleVector(p1) : storage.getDoubleVector(p1, td.simdIdx1(), 0);
                        DoubleVector tv = td.step2 == 1 ? os.getDoubleVector(p2) : os.getDoubleVector(p2, td.simdIdx2(), 0);
                        DoubleVector r = Simd.fma(tv, av, x);
                        if (td.step1 == 1) {
                            storage.setDoubleVector(r, p1);
                        } else {
                            storage.setDoubleVector(r, p1, td.simdIdx1(), 0);
                        }
                        p1 += td.simdLen * td.step1;
                        p2 += td.simdLen * td.step2;
                    }
                }
            }
            for (; i < td.bound; i++) {
                storage.setDouble(p1, Simd.fma(os.getDouble(p2), aVal, storage.getDouble(p1)));
                p1 += td.step1;
                p2 += td.step2;
            }
        }
        return this;
    }

    // REDUCE OPERATIONS

    @Override
    public Double reduce(DArrayReduceOp op) {
        return op.reduceDouble(loop(), storage);
    }

    @Override
    public DArray<Double> reduce1d(DArrayReduceOp op, int axis, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Double> res = dm.zeros(dt, Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        List<Runnable> chunks = new ArrayList<>();
        while (it.hasNext()) {
            List<Runnable> taskList = new ArrayList<>(chunk);
            while (it.hasNext() && taskList.size() < chunk) {
                int ptr = it.nextInt();
                int resPtr = resIt.next();
                taskList.add(() -> {
                    StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                    double value = dm.stride(dt, strideLayout, storage).reduce(op);
                    res.ptrSetDouble(resPtr, value);
                });
            }
            chunks.add(() -> taskList.forEach(Runnable::run));
        }
        dm.execute(size(), chunks);
        return res;
    }

    @Override
    public DArray<Double> reduceOn(DArrayReduceOp op, Shape shape, boolean keepDim, Order order) {
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
        DArray<Double> result = dm.zeros(dt, Shape.of(firstDims), order);
        PointerIterator resIt = result.ptrIterator(Order.C);
        PointerIterator firstIt = new StridePointerIterator(firstLayout, Order.C);

        while (resIt.hasNext()) {
            int ptr = resIt.nextInt();
            int offset = firstIt.nextInt();
            double value = dm.stride(dt, StrideLayout.of(lastDims, offset, lastStrides), storage).reduce(op);
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
    public DArray<Double> reduceTo(DArrayReduceOp op, Shape targetShape, boolean keepDim, Order order) {
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

        DArray<Double> result = dm.zeros(dt, Shape.of(lastDims), order);
        PointerIterator resIt = result.ptrIterator(Order.C);
        PointerIterator lastIt = StrideLayout.of(lastDims, layout().offset(), lastStrides).ptrIterator(Order.C);

        int chunk = 128;
        List<Runnable> chunks = new ArrayList<>();
        while (resIt.hasNext()) {
            List<Runnable> taskList = new ArrayList<>(chunk);
            while (resIt.hasNext() && taskList.size() < chunk) {
                int ptr = resIt.nextInt();
                int offset = lastIt.nextInt();
                taskList.add(() -> {
                    double value = dm.stride(dt, StrideLayout.of(firstDims, offset, firstStrides), storage).reduce(op);
                    result.ptrSet(ptr, value);
                });
            }
            chunks.add(() -> taskList.forEach(Runnable::run));
        }
        dm.execute(size(), chunks);

        DArray<Double> lastResult = result;
        if (keepDim) {
            for (int i = 0; i < layout.rank() - targetShape.rank(); i++) {
                lastResult = lastResult.stretch(0);
            }
        }

        return lastResult;
    }

    @Override
    public DArray<Double> var1d(int axis, int ddof, DArray<?> mean, Order order) {
        if (axis < 0) {
            axis += shape().rank();
        }
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        DArray<Double> res = dm.zeros(dt, Shape.of(newDims), Order.autoFC(order));
        if (!res.shape().equals(mean.shape())) {
            throw new IllegalArgumentException(String.format(
                    "Mean array %s must have the same shape as the result array %s.", mean.shape(), res.shape()));
        }

        var resIt = res.ptrIterator(Order.C);
        var meanIt = mean.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);

        int chunk = 128;
        List<Runnable> chunks = new ArrayList<>();
        while (it.hasNext()) {
            List<Runnable> taskList = new ArrayList<>(chunk);
            while (it.hasNext() && taskList.size() < chunk) {
                int ptr = it.nextInt();
                int resPtr = resIt.next();
                // iterators are advanced on the calling thread; tasks may run concurrently and must not share them
                double m = mean.ptrGetDouble(meanIt.next());
                taskList.add(() -> {
                    StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                    double value = dm.stride(dt, strideLayout, storage).reduce(DArrayOp.reduceVarc(ddof, m));
                    res.ptrSet(resPtr, value);
                });
            }
            chunks.add(() -> taskList.forEach(Runnable::run));
        }
        dm.execute(size(), chunks);
        return res;
    }

    @Override
    public DArray<Double> varOn(Shape shape, int ddof, DArray<?> mean, boolean keepDim, Order order) {

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
            return dm.scalar(dt, var(ddof, mean.getDouble()));
        }

        int[] firstDims = Arrays.copyOfRange(layout.dims(), 0, rank() - shape.rank());
        int[] firstStrides = Arrays.copyOfRange(layout.strides(), 0, rank() - shape.rank());
        int[] lastDims = Arrays.copyOfRange(layout.dims(), rank() - shape.rank(), rank());
        int[] lastStrides = Arrays.copyOfRange(layout.strides(), rank() - shape.rank(), rank());

        StrideLayout firstLayout = StrideLayout.of(firstDims, layout().offset(), firstStrides);
        if (mean.shape().equals(firstLayout.shape())) {
            throw new IllegalArgumentException("Mean darray must have the same shape as the result array.");
        }
        DArray<Double> result = dm.zeros(dt, Shape.of(firstDims), order);
        PointerIterator resIt = result.ptrIterator(Order.C);
        PointerIterator firstIt = new StridePointerIterator(firstLayout, Order.C);
        PointerIterator meanIt = mean.ptrIterator(Order.C);

        while (resIt.hasNext()) {
            int ptr = resIt.nextInt();
            int offset = firstIt.nextInt();
            double value =
                    dm.stride(dt, StrideLayout.of(lastDims, offset, lastStrides), storage).var(ddof, mean.ptrGetDouble(meanIt.next()));
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
        double argvalue = ReduceOpMax.initDouble;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, Simd.vsDouble);
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.bound; j++) {
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
        List<Runnable> chunks = new ArrayList<>();
        while (it.hasNext()) {
            List<Runnable> taskList = new ArrayList<>(chunk);
            while (it.hasNext() && taskList.size() < chunk) {
                int ptr = it.nextInt();
                int resPtr = resIt.next();
                taskList.add(() -> {
                    StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                    int value = dm.stride(dt, strideLayout, storage).argmax();
                    res.ptrSetInt(resPtr, value);
                });
            }
            chunks.add(() -> taskList.forEach(Runnable::run));
        }
        dm.execute(size(), chunks);
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
        List<Runnable> chunks = new ArrayList<>();
        while (it.hasNext()) {
            List<Runnable> taskList = new ArrayList<>(chunk);
            while (it.hasNext() && taskList.size() < chunk) {
                int ptr = it.nextInt();
                int resPtr = resIt.next();
                taskList.add(() -> {
                    StrideLayout strideLayout = StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride});
                    int value = dm.stride(dt, strideLayout, storage).argmin();
                    res.ptrSetInt(resPtr, value);
                });
            }
            chunks.add(() -> taskList.forEach(Runnable::run));
        }
        dm.execute(size(), chunks);
        return res;
    }

    @Override
    public int argmin(Order order) {
        int argmin = -1;
        double argvalue = ReduceOpMin.initDouble;
        var i = 0;
        var loop = StrideLoopDescriptor.of(layout, order, Simd.vsDouble);
        for (int p : loop.offsets) {
            for (int j = 0; j < loop.bound; j++) {
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
        StrideLoopDescriptor loop = loop();
        int count = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (dt().isNaN(storage.getDouble(p))) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }

    @Override
    public int zeroCount() {
        StrideLoopDescriptor loop = loop();
        int count = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                if (storage.getDouble(p) == 0) {
                    count++;
                }
                p += loop.step;
            }
        }
        return count;
    }


    // LINEAR ALGEBRA OPERATIONS

    @Override
    public Double inner(DArray<?> other) {
        if (dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        return inner(other, 0, shape().dim(0));
    }

    @Override
    public Double inner(DArray<?> other, int start, int end) {
        if (rank() != 1 || other.rank() != 1) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), other.shape().toString()));
        }
        if (start > end || start < 0 || end > other.dim(0) || end > dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        return innerUnchecked(other.dt().equals(dt) ? other : other.cast(dt), start, end);
    }

    private Double innerUnchecked(DArray<?> other, int start, int end) {
        StrideLoopDescriptor loop = loop();
        BaseDoubleStrideDArray dts = (BaseDoubleStrideDArray) other;
        StrideLoopDescriptor otherLoop = dts.loop();

        int step1 = loop.step;
        int step2 = otherLoop.step;

        if (step1 == 1 && step2 == 1) {
            return innerUncheckedUnit(dts, start, end);
        }

        int i = 0;
        int p1 = loop.offsets[0] + start * step1;
        int p2 = otherLoop.offsets[0] + start * step2;
        double sum = 0;

        if (storage.supportSimd() && dts.storage.supportSimd()) {
            int simdBound = Simd.vsDouble.loopBound(end - start);
            if (simdBound > 0) {
                DoubleVector vsum = Simd.zeroDouble();
                for (; i < simdBound; i += loop.simdLen) {
                    DoubleVector v1 = step1 == 1 ?
                            storage.getDoubleVector(p1) :
                            storage.getDoubleVector(p1, loop.simdIdx(), 0);
                    DoubleVector v2 = step2 == 1 ?
                            dts.storage.getDoubleVector(p2) :
                            dts.storage.getDoubleVector(p2, otherLoop.simdIdx(), 0);
                    vsum = vsum.add(v1.mul(v2));
                    p1 += loop.simdLen * step1;
                    p2 += otherLoop.simdLen * step2;
                }
                sum += vsum.reduceLanes(VectorOperators.ADD);
            }
        }
        for (; i < end - start; i++) {
            sum += (double) (storage.getDouble(p1) * dts.storage.getDouble(p2));
            p1 += step1;
            p2 += step2;
        }
        return sum;
    }

    private double innerUncheckedUnit(BaseDoubleStrideDArray dts, int start, int end) {
        StrideLoopDescriptor loop = loop();
        StrideLoopDescriptor otherLoop = dts.loop();
        int i = 0;
        int p1 = loop.offsets[0] + start;
        int p2 = otherLoop.offsets[0] + start;
        double sum = 0;

        if (storage.supportSimd() && dts.storage.supportSimd()) {
            int simdBound = Simd.vsDouble.loopBound(end - start);
            if (simdBound > 0) {
                DoubleVector vsum = Simd.zeroDouble();
                for (; i < simdBound; i += loop.simdLen) {
                    DoubleVector v1 = storage.getDoubleVector(p1);
                    DoubleVector v2 = dts.storage.getDoubleVector(p2);
                    vsum = vsum.add(v1.mul(v2));
                    p1 += loop.simdLen;
                    p2 += otherLoop.simdLen;
                }
                sum += vsum.reduceLanes(VectorOperators.ADD);
            }
        }
        for (; i < end - start; i++) {
            sum += (double) (storage.getDouble(p1++) * dts.storage.getDouble(p2++));
        }
        return sum;
    }

    @Override
    public DArray<Double> mv(DArray<?> other, Order askOrder) {
        if (shape().rank() != 2 || other.shape().rank() != 1 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-vector multiplication (m = %s, v = %s).",
                            shape(), other.shape()));
        }
        var result = dm.zeros(dt, Shape.of(shape().dim(0)), askOrder);
        mvInternal(this, other, result);
        return result;
    }

    @Override
    public DArray<Double> bmv(DArray<?> other, Order askOrder) {
        BaseDoubleStrideDArray a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseDoubleStrideDArray) a.strexp(0, 1).strexp(1, 1);
        }
        if (other.isScalar()) {
            b = b.strexp(0, 1);
        }
        if (a.rank() == 2 && b.rank() == 1 && a.dim(1) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseDoubleStrideDArray) a.stretch(0)).bmvInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 3 && b.rank() == 1 && a.dim(2) == b.dim(0)) {
            // batch on matrix, add batch to vector
            return a.bmvInternal(b.strexp(0, a.dim(0)), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(1)) {
            // batch on vector, add batch to matrix
            return ((BaseDoubleStrideDArray) a.strexp(0, b.dim(0))).bmvInternal(b, askOrder);
        }
        if (a.rank() == 3 && b.rank() == 2 && a.dim(2) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bmvInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "DArrays are not valid for batch matrix vector multiplication (bm : %s, bv = %s)", shape(), other.shape()));
    }

    private DArray<Double> bmvInternal(DArray<?> other, Order askOrder) {
        DArray<Double> res = dm.zeros(dt, Shape.of(dim(0), dim(1)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).mv(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Double> vtm(DArray<?> other, Order askOrder) {
        if (shape().rank() != 1 || other.rank() != 2 || shape().dim(0) != other.dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for vector transpose matrix multiplication (v = %s, m = %s).",
                            shape(), other.shape())
            );
        }
        var result = dm.zeros(dt, Shape.of(other.dim(1)), askOrder);
        // v^T * M == (M^T * v)^T, so the transposed view of the matrix goes through the matrix-vector kernel
        mvInternal((BaseDoubleStrideDArray) other.cast(dt).t_(), this, result);
        return result;
    }

    @Override
    public DArray<?> bvtm(DArray<?> other, Order askOrder) {
        BaseDoubleStrideDArray a = this;
        DArray<?> b = other;
        if (a.isScalar()) {
            a = (BaseDoubleStrideDArray) a.stretch(0);
        }
        if (other.isScalar()) {
            b = b.stretch(0, 1);
        }
        if (a.rank() == 1 && b.rank() == 2 && a.dim(0) == b.dim(0)) {
            // simple case, create a batch of 1 for each element
            return ((BaseDoubleStrideDArray) a.stretch(0)).bvtmInternal(b.stretch(0), askOrder);
        }
        if (a.rank() == 2 && b.rank() == 2 && a.dim(1) == b.dim(0)) {
            // batch on vector, add batch to matrix
            return a.mm(b, askOrder);
        }
        if (a.rank() == 1 && b.rank() == 3 && a.dim(0) == b.dim(1)) {
            // batch on matrix, add batch to vector
            return ((BaseDoubleStrideDArray) a.strexp(0, b.dim(0))).bvtmInternal(b, askOrder);
        }
        if (a.rank() == 2 && b.rank() == 3 && a.dim(1) == b.dim(1) && a.dim(0) == b.dim(0)) {
            // no need of batching
            return a.bvtmInternal(b, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "DArrays are not valid for batch vector transpose matrix multiplication (bv : %s, bm = %s)", shape(), other.shape()));
    }

    private DArray<Double> bvtmInternal(DArray<?> other, Order askOrder) {
        DArray<Double> res = dm.zeros(dt, Shape.of(dim(0), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            selsq(0, b).vtm(other.selsq(0, b)).copyTo(res.selsq(0, b));
        }
        return res;
    }

    /**
     * Matrix-vector product {@code y += A * x} for a rank-2 matrix and rank-1 vector. Matrix-vector products are
     * memory bound (two flops per matrix element read), so the kernel streams the matrix exactly once in whatever
     * direction is contiguous instead of packing it: row dot products when rows are contiguous, column updates of a
     * dense accumulator when columns are contiguous, and gathered dot products for arbitrary strides.
     */
    private void mvInternal(BaseDoubleStrideDArray a, DArray<?> x, DArray<Double> y) {
        int m = a.dim(0);
        int n = a.dim(1);

        DArray<Double> xc = x.cast(dt);
        Storage xs = xc.storage();
        StrideLayout xl = (StrideLayout) xc.layout();
        int xOff = xl.offset();
        int xStride = xl.stride(0);

        Storage as = a.storage;
        int aOff = a.layout.offset();
        int as0 = a.layout.stride(0);
        int as1 = a.layout.stride(1);

        StrideLayout yl = (StrideLayout) y.layout();
        int yOff = yl.offset();
        int yStride = yl.stride(0);

        var vs = Simd.vsDouble;
        int simdLen = vs.length();
        boolean simd = as.supportSimd() && xs.supportSimd();

        // a contiguous copy of x lets both paths use plain vector loads / lane broadcasts
        double[] xv = new double[n];
        int xp = xOff;
        for (int k = 0; k < n; k++) {
            xv[k] = xs.getDouble(xp);
            xp += xStride;
        }

        if (as1 != 1 && as0 == 1 && simd) {
            // columns contiguous (F order / transposed): y += x[k] * A[:, k], vectorized along the rows
            double[] acc = new double[m];
            int simdBound = vs.loopBound(m);
            for (int k = 0; k < n; k++) {
                double xk = xv[k];
                if (xk == 0) {
                    continue;
                }
                DoubleVector xkv = DoubleVector.broadcast(vs, xk);
                int ptr = aOff + k * as1;
                int i = 0;
                for (; i < simdBound; i += simdLen) {
                    DoubleVector col = as.getDoubleVector(ptr);
                    DoubleVector cur = DoubleVector.fromArray(vs, acc, i);
                    Simd.fma(col, xkv, cur).intoArray(acc, i);
                    ptr += simdLen;
                }
                for (; i < m; i++) {
                    acc[i] = Simd.fma(as.getDouble(ptr), xk, acc[i]);
                    ptr++;
                }
            }
            int yp = yOff;
            for (int i = 0; i < m; i++) {
                y.ptrIncDouble(yp, acc[i]);
                yp += yStride;
            }
            return;
        }

        // rows contiguous (C order) or arbitrary strides: one dot product per row, split across threads by row blocks
        int rowsPerTask = Math.max(1, Math.min(m, Math.ceilDiv(DArrayManager.DEFAULT_PARALLEL_THRESHOLD, Math.max(1, n))));
        List<Runnable> tasks = new ArrayList<>(Math.ceilDiv(m, rowsPerTask));
        for (int r0 = 0; r0 < m; r0 += rowsPerTask) {
            int rs = r0;
            int re = Math.min(m, r0 + rowsPerTask);
            tasks.add(() -> {
                int[] gatherIdx = null;
                if (simd && as1 != 1) {
                    gatherIdx = new int[simdLen];
                    for (int l = 1; l < simdLen; l++) {
                        gatherIdx[l] = gatherIdx[l - 1] + as1;
                    }
                }
                for (int i = rs; i < re; i++) {
                    int ptr = aOff + i * as0;
                    double sum;
                    if (!simd) {
                        sum = rowDotScalar(as, ptr, as1, xv, 0, n, (double) 0);
                    } else if (as1 == 1) {
                        sum = rowDotUnit(as, ptr, xv, n);
                    } else {
                        sum = rowDotGather(as, ptr, as1, gatherIdx, xv, n);
                    }
                    y.ptrIncDouble(yOff + i * yStride, sum);
                }
            });
        }
        dm.execute((long) m * n, tasks);
    }

    // The row kernels are kept as small separate methods on purpose: the Vector API is only fast when every call
    // in the chain is inlined and intrinsified, which the JIT declines to do inside large method bodies.

    private static double rowDotScalar(Storage as, int ptr, int step, double[] xv, int from, int n, double init) {
        double sum = init;
        for (int k = from; k < n; k++) {
            sum = Simd.fma(as.getDouble(ptr), xv[k], sum);
            ptr += step;
        }
        return sum;
    }

    private static double rowDotUnit(Storage as, int ptr, double[] xv, int n) {
        var vs = Simd.vsDouble;
        int simdLen = vs.length();
        int simdBound = vs.loopBound(n);
        int twoBound = simdBound - (simdBound % (2 * simdLen));
        // two independent accumulators hide the fma latency
        DoubleVector acc0 = DoubleVector.zero(vs);
        DoubleVector acc1 = DoubleVector.zero(vs);
        int k = 0;
        for (; k < twoBound; k += 2 * simdLen) {
            acc0 = Simd.fma(as.getDoubleVector(ptr), DoubleVector.fromArray(vs, xv, k), acc0);
            acc1 = Simd.fma(as.getDoubleVector(ptr + simdLen), DoubleVector.fromArray(vs, xv, k + simdLen), acc1);
            ptr += 2 * simdLen;
        }
        for (; k < simdBound; k += simdLen) {
            acc0 = Simd.fma(as.getDoubleVector(ptr), DoubleVector.fromArray(vs, xv, k), acc0);
            ptr += simdLen;
        }
        double sum = acc0.add(acc1).reduceLanes(VectorOperators.ADD);
        return rowDotScalar(as, ptr, 1, xv, k, n, sum);
    }

    private static double rowDotGather(Storage as, int ptr, int step, int[] gatherIdx, double[] xv, int n) {
        var vs = Simd.vsDouble;
        int simdLen = vs.length();
        int simdBound = vs.loopBound(n);
        DoubleVector acc = DoubleVector.zero(vs);
        int k = 0;
        for (; k < simdBound; k += simdLen) {
            acc = Simd.fma(as.getDoubleVector(ptr, gatherIdx, 0), DoubleVector.fromArray(vs, xv, k), acc);
            ptr += simdLen * step;
        }
        double sum = acc.reduceLanes(VectorOperators.ADD);
        return rowDotScalar(as, ptr, step, xv, k, n, sum);
    }

    @Override
    public DArray<Double> mm(DArray<?> other, Order askOrder) {
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Illegal askOrder value, must be Order.C or Order.F");
        }
        return mm(other, dm.zeros(dt, Shape.of(shape().dim(0), other.shape().dim(1)), askOrder));
    }

    @Override
    public DArray<Double> mm(DArray<?> other, DArray<?> to) {
        if (shape().rank() != 2 || other.shape().rank() != 2 || shape().dim(1) != other.shape().dim(0)) {
            throw new IllegalArgumentException(
                    String.format("Operands are not valid for matrix-matrix multiplication (m = %s, v = %s).", shape(), other.shape()));
        }
        if (to.dt() != dt) {
            throw new IllegalArgumentException("Target array has different data type than operation result.");
        }
        return mmBlocked(other, to.cast(dt));
    }

    // Blocked matrix product C += A * B.
    //
    // C is split in tiles of MM_MC rows by MM_NC columns; each tile loops over the inner dimension in blocks of MM_KC.
    // Inside a tile both operands are packed into contiguous row-major buffers, which removes all layout concerns
    // (transposed operands, F order, narrow views, other data types) from the hot loop and keeps the working set in
    // cache: one A panel (MM_MC x MM_KC) and one B panel (MM_KC x MM_NC). The hot loop is a register-tiled micro kernel
    // computing 4 rows x 2 vectors of C at a time with broadcast(A) * vector(B) fused multiply-adds, so B is streamed
    // with contiguous vector loads and every load feeds four multiply-adds. Tiles of C are independent and are the
    // unit of parallel work.

    /**
     * Rows of A packed per tile.
     */
    private static final int MM_MC = 128;
    /**
     * Inner dimension block size.
     */
    private static final int MM_KC = 256;
    /**
     * Columns of B packed per tile; a multiple of two vector lengths for every supported species.
     */
    private static final int MM_NC = 256;
    /**
     * Rows of C computed together by the micro kernel.
     */
    private static final int MM_MR = 4;

    private DArray<Double> mmBlocked(DArray<?> other, DArray<Double> to) {
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = other.shape().dim(1);

        DArray<Double> b = other.cast(dt);
        Storage bs = b.storage();
        StrideLayout bl = (StrideLayout) b.layout();
        int bOff = bl.offset();
        int bs0 = bl.stride(0);
        int bs1 = bl.stride(1);

        int aOff = layout.offset();
        int as0 = layout.stride(0);
        int as1 = layout.stride(1);

        StrideLayout cl = (StrideLayout) to.layout();
        int cOff = cl.offset();
        int cs0 = cl.stride(0);
        int cs1 = cl.stride(1);

        int simdLen = Simd.vsDouble.length();
        int vec2 = 2 * simdLen;

        // shrink the row block until there are enough tiles to keep every thread busy (small matrices)
        int mc = MM_MC;
        while (mc > 4 * MM_MR && (long) Math.ceilDiv(m, mc) * Math.ceilDiv(p, MM_NC) < 2L * dm.cpuThreads()) {
            mc /= 2;
        }

        List<Runnable> tasks = new ArrayList<>(Math.ceilDiv(m, mc) * Math.ceilDiv(p, MM_NC));
        for (int i0 = 0; i0 < m; i0 += mc) {
            int im = Math.min(mc, m - i0);
            for (int j0 = 0; j0 < p; j0 += MM_NC) {
                int jn = Math.min(MM_NC, p - j0);
                // packed width: a multiple of two vectors, zero padded, so the micro kernel never needs a column tail
                int jnPad = Math.ceilDiv(jn, vec2) * vec2;
                int ti = i0;
                int tj = j0;
                tasks.add(() -> {
                    double[] ap = new double[im * MM_KC];
                    double[] bp = new double[MM_KC * jnPad];
                    double[] cp = new double[im * jnPad];
                    for (int k0 = 0; k0 < n; k0 += MM_KC) {
                        int kn = Math.min(MM_KC, n - k0);
                        // pack A[ti.., k0..] row-major (im x kn)
                        for (int i = 0; i < im; i++) {
                            int ptr = aOff + (ti + i) * as0 + k0 * as1;
                            int base = i * kn;
                            for (int k = 0; k < kn; k++) {
                                ap[base + k] = storage.getDouble(ptr);
                                ptr += as1;
                            }
                        }
                        // pack B[k0.., tj..] row-major (kn x jnPad), zero padded columns
                        for (int k = 0; k < kn; k++) {
                            int ptr = bOff + (k0 + k) * bs0 + tj * bs1;
                            int base = k * jnPad;
                            for (int j = 0; j < jn; j++) {
                                bp[base + j] = bs.getDouble(ptr);
                                ptr += bs1;
                            }
                            for (int j = jn; j < jnPad; j++) {
                                bp[base + j] = 0;
                            }
                        }
                        mmMicroKernel(ap, bp, cp, im, kn, jnPad, simdLen, k0 == 0);
                    }
                    // write the tile back into C, honouring its strides
                    for (int i = 0; i < im; i++) {
                        int ptr = cOff + (ti + i) * cs0 + tj * cs1;
                        int base = i * jnPad;
                        for (int j = 0; j < jn; j++) {
                            to.ptrIncDouble(ptr, cp[base + j]);
                            ptr += cs1;
                        }
                    }
                });
            }
        }
        dm.execute((long) m * n * p, tasks);
        return to;
    }

    /**
     * Computes {@code cp (+)= ap * bp} for packed row-major panels: ap is (im x kn), bp is (kn x jnPad) and
     * cp is (im x jnPad), with jnPad a multiple of two vector lengths. When {@code init} is true the tile is
     * overwritten (first k block), otherwise accumulated.
     */
    private static void mmMicroKernel(double[] ap, double[] bp, double[] cp, int im, int kn, int jnPad, int simdLen, boolean init) {
        int i = 0;
        for (; i + MM_MR <= im; i += MM_MR) {
            mmTile4x2(ap, bp, cp, i, kn, jnPad, simdLen, init);
        }
        for (; i < im; i++) {
            mmTile1x2(ap, bp, cp, i, kn, jnPad, simdLen, init);
        }
    }

    // The tile kernels are separate small methods on purpose: the Vector API is only fast when every call in the
    // chain is inlined and intrinsified, which the JIT declines to do inside large method bodies.

    /**
     * 4 rows x 2 vectors register tile of {@code cp (+)= ap * bp} for packed panels, starting at row {@code i}.
     */
    private static void mmTile4x2(double[] ap, double[] bp, double[] cp, int i, int kn, int jnPad, int simdLen, boolean init) {
        var vs = Simd.vsDouble;
        int vec2 = 2 * simdLen;
        {
            int a0 = i * kn;
            int a1 = a0 + kn;
            int a2 = a1 + kn;
            int a3 = a2 + kn;
            int c0 = i * jnPad;
            int c1 = c0 + jnPad;
            int c2 = c1 + jnPad;
            int c3 = c2 + jnPad;
            for (int j = 0; j < jnPad; j += vec2) {
                DoubleVector acc00, acc01, acc10, acc11, acc20, acc21, acc30, acc31;
                if (init) {
                    acc00 = DoubleVector.zero(vs);
                    acc01 = acc00;
                    acc10 = acc00;
                    acc11 = acc00;
                    acc20 = acc00;
                    acc21 = acc00;
                    acc30 = acc00;
                    acc31 = acc00;
                } else {
                    acc00 = DoubleVector.fromArray(vs, cp, c0 + j);
                    acc01 = DoubleVector.fromArray(vs, cp, c0 + j + simdLen);
                    acc10 = DoubleVector.fromArray(vs, cp, c1 + j);
                    acc11 = DoubleVector.fromArray(vs, cp, c1 + j + simdLen);
                    acc20 = DoubleVector.fromArray(vs, cp, c2 + j);
                    acc21 = DoubleVector.fromArray(vs, cp, c2 + j + simdLen);
                    acc30 = DoubleVector.fromArray(vs, cp, c3 + j);
                    acc31 = DoubleVector.fromArray(vs, cp, c3 + j + simdLen);
                }
                int bIdx = j;
                for (int k = 0; k < kn; k++) {
                    DoubleVector b0 = DoubleVector.fromArray(vs, bp, bIdx);
                    DoubleVector b1 = DoubleVector.fromArray(vs, bp, bIdx + simdLen);
                    DoubleVector av0 = DoubleVector.broadcast(vs, ap[a0 + k]);
                    DoubleVector av1 = DoubleVector.broadcast(vs, ap[a1 + k]);
                    DoubleVector av2 = DoubleVector.broadcast(vs, ap[a2 + k]);
                    DoubleVector av3 = DoubleVector.broadcast(vs, ap[a3 + k]);
                    acc00 = Simd.fma(av0, b0, acc00);
                    acc01 = Simd.fma(av0, b1, acc01);
                    acc10 = Simd.fma(av1, b0, acc10);
                    acc11 = Simd.fma(av1, b1, acc11);
                    acc20 = Simd.fma(av2, b0, acc20);
                    acc21 = Simd.fma(av2, b1, acc21);
                    acc30 = Simd.fma(av3, b0, acc30);
                    acc31 = Simd.fma(av3, b1, acc31);
                    bIdx += jnPad;
                }
                acc00.intoArray(cp, c0 + j);
                acc01.intoArray(cp, c0 + j + simdLen);
                acc10.intoArray(cp, c1 + j);
                acc11.intoArray(cp, c1 + j + simdLen);
                acc20.intoArray(cp, c2 + j);
                acc21.intoArray(cp, c2 + j + simdLen);
                acc30.intoArray(cp, c3 + j);
                acc31.intoArray(cp, c3 + j + simdLen);
            }
        }
    }

    /**
     * Single row x 2 vectors tile, used for the rows left over by the 4-row tile.
     */
    private static void mmTile1x2(double[] ap, double[] bp, double[] cp, int i, int kn, int jnPad, int simdLen, boolean init) {
        var vs = Simd.vsDouble;
        int vec2 = 2 * simdLen;
        {
            int a0 = i * kn;
            int c0 = i * jnPad;
            for (int j = 0; j < jnPad; j += vec2) {
                DoubleVector acc0;
                DoubleVector acc1;
                if (init) {
                    acc0 = DoubleVector.zero(vs);
                    acc1 = acc0;
                } else {
                    acc0 = DoubleVector.fromArray(vs, cp, c0 + j);
                    acc1 = DoubleVector.fromArray(vs, cp, c0 + j + simdLen);
                }
                int bIdx = j;
                for (int k = 0; k < kn; k++) {
                    DoubleVector av0 = DoubleVector.broadcast(vs, ap[a0 + k]);
                    acc0 = Simd.fma(av0, DoubleVector.fromArray(vs, bp, bIdx), acc0);
                    acc1 = Simd.fma(av0, DoubleVector.fromArray(vs, bp, bIdx + simdLen), acc1);
                    bIdx += jnPad;
                }
                acc0.intoArray(cp, c0 + j);
                acc1.intoArray(cp, c0 + j + simdLen);
            }
        }
    }

    @Override
    public DArray<Double> bmm(DArray<?> other, Order askOrder) {
        if (rank() == 2 && other.rank() == 2 && dim(1) == other.dim(0)) {
            return ((BaseDoubleStrideDArray) stretch(0)).bmmInternal(other.stretch(0), askOrder);
        }
        if (rank() == 3 && other.rank() == 2 && dim(2) == other.dim(0)) {
            return bmmInternal(other.strexp(0, dim(0)), askOrder);
        }
        if (rank() == 2 && other.rank() == 3 && dim(1) == other.dim(1)) {
            return ((BaseDoubleStrideDArray) strexp(0, other.dim(0))).bmmInternal(other, askOrder);
        }
        if (rank() == 3 && other.rank() == 3 && dim(0) == other.dim(0) && dim(2) == other.dim(1)) {
            return bmmInternal(other, askOrder);
        }
        throw new IllegalArgumentException(String.format(
                "DArrays are not valid for batch matrix-matrix multiplication (bm1: %s, bm2: %s)", shape(), other.shape()));
    }

    private DArray<Double> bmmInternal(DArray<?> other, Order askOrder) {
        DArray<Double> res = dm.zeros(dt, Shape.of(dim(0), dim(1), other.dim(2)), askOrder);
        for (int b = 0; b < dim(0); b++) {
            ((BaseDoubleStrideDArray) selsq(0, b)).mmBlocked(other.selsq(0, b), res.selsq(0, b));
        }
        return res;
    }

    @Override
    public DArray<Double> conv1d(DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        return BaseDoubleStrideDArrayConvolutions.conv1d(this, kernel, bias, stride, padding, dilation, groups);
    }

    @Override
    public DArray<Double> convTranspose1d(DArray<?> weights, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        return BaseDoubleStrideDArrayConvolutions.convTranspose1d(this, weights, bias, stride, padding, dilation, groups, outputPadding);
    }

    @Override
    public DArray<Double> unfold1d(int kLen, int stride, int padding, int dilation) {
        return BaseDoubleStrideDArrayConvolutions.unfold1d(this, kLen, stride, padding, dilation);
    }

    @Override
    public DArray<Double> conv2d(DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        return BaseDoubleStrideDArrayConvolutions.conv2d(this, kernel, bias, stride, padding, dilation, groups);
    }

    @Override
    public DArray<Double> unfold2d(int kH, int kW, int stride, int padding, int dilation) {
        return BaseDoubleStrideDArrayConvolutions.unfold2d(this, kH, kW, stride, padding, dilation);
    }

    @Override
    public DArray<Double> convTranspose2d(DArray<?> w, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        return BaseDoubleStrideDArrayConvolutions.convTranspose2d(this, w, bias, stride, padding, dilation, groups, outputPadding);
    }

    @Override
    public DArray<Double> conv3d(DArray<?> kernel, DArray<?> bias, int stride, int padding, int dilation, int groups) {
        return BaseDoubleStrideDArrayConvolutions.conv3d(this, kernel, bias, stride, padding, dilation, groups);
    }

    @Override
    public DArray<Double> unfold3d(int kD, int kH, int kW, int stride, int padding, int dilation) {
        return BaseDoubleStrideDArrayConvolutions.unfold3d(this, kD, kH, kW, stride, padding, dilation);
    }

    @Override
    public DArray<Double> convTranspose3d(DArray<?> weights, DArray<?> bias, int stride, int padding, int dilation, int groups,
            int outputPadding) {
        return BaseDoubleStrideDArrayConvolutions.convTranspose3d(this, weights, bias, stride, padding, dilation, groups, outputPadding);
    }

    @Override
    public Pair<DArray<Double>, DArray<Integer>> maxPool1d(int kW, int stride, int padding, int dilation, boolean ceilMode) {
        return BaseDoubleStrideDArrayConvolutions.maxPool1d(this, kW, stride, padding, dilation, ceilMode);
    }

    @Override
    public DArray<Double> maxUnpool1d(DArray<Double> input, DArray<Integer> indices, int kSize, int stride, int padding, int outputSize) {
        return BaseDoubleStrideDArrayConvolutions.maxUnpool1d(input, indices, kSize, stride, padding, outputSize);
    }

    @Override
    public Pair<DArray<Double>, DArray<Integer>> maxPool2d(int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        return BaseDoubleStrideDArrayConvolutions.maxPool2d(this, kH, kW, stride, padding, dilation, ceilMode);
    }

    @Override
    public DArray<Double> maxUnpool2d(DArray<Double> input, DArray<Integer> indices, int kH, int kW, int stride, int padding,
            int outputSizeH,
            int outputSizeW) {
        return BaseDoubleStrideDArrayConvolutions.maxUnpool2d(input, indices, kH, kW, stride, padding, outputSizeH, outputSizeW);
    }

    @Override
    public Pair<DArray<Double>, DArray<Integer>> maxPool3d(int kD, int kH, int kW, int stride, int padding, int dilation,
            boolean ceilMode) {
        return BaseDoubleStrideDArrayConvolutions.maxPool3d(this, kD, kH, kW, stride, padding, dilation, ceilMode);
    }

    @Override
    public DArray<Double> maxUnpool3d(DArray<Double> input, DArray<Integer> indices, int kD, int kH, int kW, int stride, int padding,
            int outputSizeD, int outputSizeH, int outputSizeW) {
        return BaseDoubleStrideDArrayConvolutions.maxUnpool3d(input, indices, kD, kH, kW, stride, padding, outputSizeD, outputSizeH,
                outputSizeW);
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
    public DArray<Double> diag(int diagonal) {
        if (isScalar() && diagonal == 0) {
            return this;
        }
        if (isVector()) {
            int n = dim(0) + Math.abs(diagonal);
            DArray<Double> m = dm.zeros(dt, Shape.of(n, n));
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
            return dm.stride(dt, Shape.of(len), Order.defaultOrder(), diag);
        }
        throw new OperationNotAvailableException("This operation is available for tensors with shape " + shape() + ".");
    }

    @Override
    public Double norm(
            // FREEZE
            double pow
            // UNFREEZE
    ) {
        if (!dt().floatingPoint()) {
            throw new OperationNotAvailableException("This operation is only available on floating point data types.");
        }
        if (pow < 0) {
            throw new IllegalArgumentException(String.format("Norm power p=%s must be greater or equal with 0.", Format.floatFlex(pow)));
        }
        if (pow == 0) {
            return (double) shape().size();
        }
        if (pow == 1) {
            return abs().sum();
        }
        if (pow == 2) {
            return (double) Math.sqrt(sqr().sum());
        }
        double sum = (double) 0;
        StrideLoopDescriptor loop = loop();
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += (double) Math.pow(Math.abs(storage.getDouble(p)), pow);
                p += loop.step;
            }
        }
        return (double) Math.pow(sum, 1. / pow);
    }

    @Override
    public DArray<Double> normalize_(
            // FREEZE
            double pow
            // UNFREEZE
    ) {
        return div_(norm(pow));
    }

    @Override
    public DArray<Double> copy(Order askOrder) {
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
        var loop = StrideLoopDescriptor.of(layout, askOrder, Simd.vsDouble);
        var last = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                copy.setDouble(last++, storage.getDouble(p));
                p += loop.step;
            }
        }
    }

    @Override
    public DArray<Double> copyTo(DArray<Double> to) {

        Order askOrder = Layout.storageFastTandemOrder(layout, to.layout());

        if (to instanceof BaseDoubleStrideDArray dst) {

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

                // one task per cache-sized block; blocks are disjoint so they can be copied concurrently
                List<Runnable> tasks = new ArrayList<>();
                Stack<Integer> stack = new Stack<>();
                boolean loop = true;
                while (!stack.isEmpty() || loop) {
                    int level = stack.size();
                    if (loop) {
                        if (level == slices.length) {
                            int[] ss = Ints.copy(starts);
                            int[] es = Ints.copy(ends);
                            tasks.add(() -> {
                                BaseDoubleStrideDArray s = (BaseDoubleStrideDArray) this.narrowAll(false, ss, es);
                                BaseDoubleStrideDArray d = (BaseDoubleStrideDArray) dst.narrowAll(false, ss, es);
                                directCopyTo(s, d, askOrder);
                            });
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
                dm.execute(size(), tasks);
                return dst;
            }

            directCopyTo(this, dst, askOrder);
            return dst;
        }
        throw new IllegalArgumentException("Not implemented for this tensor type.");
    }

    private void directCopyTo(BaseDoubleStrideDArray src, BaseDoubleStrideDArray dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder, Simd.vsDouble);
        var it2 = dst.ptrIterator(askOrder);
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                dst.storage.setDouble(it2.nextInt(), src.storage.getDouble(p));
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