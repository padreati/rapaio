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

package rapaio.math.tensor.manager.barray;

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
import rapaio.math.tensor.storage.array.ByteArrayStorage;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public class BaseByteTensorStride extends AbstractStrideTensor<Byte> {

    public BaseByteTensorStride(TensorManager engine, StrideLayout layout, Storage<Byte> storage) {
        super(engine, layout, storage);
    }

    @Override
    public DType<Byte> dtype() {
        return DType.BYTE;
    }

    @Override
    public Tensor<Byte> reshape(Shape askShape, Order askOrder) {
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
        Tensor<Byte> copy = engine.zeros(dtype(), askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetByte(copyIt.nextInt(), storage.getByte(it.nextInt()));
        }
        return copy;
    }

    @Override
    public Tensor<Byte> flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = engine.ofByte().storage().zeros(layout.size());
        int ptr = 0;
        var it = loopIterator(askOrder);
        while (it.hasNext()) {
            int off = it.nextInt();
            for (int i = 0, p = off; i < it.size(); i++, p += it.step()) {
                out.setByte(ptr++, storage.getByte(p));
            }
        }
        return engine.ofByte().stride(StrideLayout.of(Shape.of(layout.size()), 0, new int[] {1}), out);
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

    @Override
    public BaseByteTensorStride apply_(Order askOrder, IntIntBiFunction<Byte> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.setByte(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public Tensor<Byte> apply_(Function<Byte, Byte> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            storage.setByte(ptr, fun.apply(storage.getByte(ptr)));
        }
        return this;
    }

    @Override
    public Tensor<Byte> fill_(Byte value) {
        for (int offset : loop.offsets) {
            if (loop.step == 1) {
                storage.fillByte(value, offset, loop.size);
            } else {
                for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                    storage.setByte(p, value);
                }
            }
        }
        return this;
    }

    @Override
    public Tensor<Byte> fillNan_(Byte value) {
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (dtype().isNaN(storage.getByte(p))) {
                    storage.setByte(p, value);
                }
            }
        }
        return this;
    }

    @Override
    public Tensor<Byte> clamp_(Byte min, Byte max) {
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (!dtype().isNaN(min) && storage.getByte(p) < min) {
                    storage.setByte(p, min);
                }
                if (!dtype().isNaN(max) && storage.getByte(p) > max) {
                    storage.setByte(p, max);
                }
            }
        }
        return this;
    }

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            for (int i = off; i < loop.size + off; i++) {
                storage.setByte(i, op.applyByte(storage.getByte(i)));
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            for (int i = 0, p = off; i < loop.size; i++, p += loop.step) {
                storage.setByte(p, op.applyByte(storage.getByte(p)));
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
    protected void binaryVectorOp(TensorBinaryOp op, Tensor<Byte> b) {
        if (b.isScalar()) {
            binaryScalarOp(op, b.getByte());
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
            storage.setByte(next, op.applyByte(storage.getByte(next), b.ptrGet(refIt.nextInt())));
        }
    }

    void binaryScalarOpStep(TensorBinaryOp op, byte value) {
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                storage.setByte(p, op.applyByte(storage.getByte(p), value));
            }
        }
    }

    @Override
    protected void binaryScalarOp(TensorBinaryOp op, Byte value) {
        binaryScalarOpStep(op, value);
    }

    @Override
    public Tensor<Byte> fma_(Byte a, Tensor<Byte> t) {
        if (t.isScalar()) {
            byte tVal = t.getByte(0);
            return add_((byte) (a * tVal));
        }
        if (!shape().equals(t.shape())) {
            throw new IllegalArgumentException("Tensors does not have the same shape.");
        }
        byte aVal = a;
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = t.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            storage.setByte(next, (byte) Math.fma(t.ptrGet(refIt.nextInt()), aVal, storage.getByte(next)));
        }
        return this;
    }

    @Override
    public Byte vdot(Tensor<Byte> tensor) {
        return vdot(tensor, 0, shape().dim(0));
    }

    @Override
    public Byte vdot(Tensor<Byte> tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseByteTensorStride dts = (BaseByteTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        int start1 = layout.offset() + start * step1;
        int end1 = layout.offset() + end * step1;
        int start2 = dts.layout.offset() + start * step2;

        byte sum = 0;
        for (int i = start1; i < end1; i += step1) {
            sum += (byte) (storage.getByte(i) * dts.storage.getByte(start2));
            start2 += step2;
        }
        return sum;
    }

    @Override
    public Tensor<Byte> vpadCopy(int before, int after) {
        if (!isVector()) {
            throw new IllegalArgumentException("This operation is available only for vectors.");
        }
        Storage<Byte> newStorage = engine.storage().ofByte().zeros(before + dim(0) + after);
        var loop = loopIterator();
        while (loop.hasNext()) {
            int offset = loop.next();
            for (int i = 0; i < loop.size(); i++) {
                newStorage.setByte(before + i, ptrGetByte(offset + i * loop.step()));
            }
        }
        return engine.ofByte().stride(Shape.of(before + dim(0) + after), Order.C, newStorage);
    }

    @Override
    public Tensor<Byte> mv(Tensor<Byte> tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    STR."Operands are not valid for matrix-vector multiplication \{"(m = %s, v = %s).".formatted(shape(),
                            tensor.shape())}");
        }
        var result = engine.ofByte().storage().zeros(shape().dim(0));
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            byte sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += (byte) (ptrGetByte(it.nextInt()) * tensor.ptrGetByte(innerIt.nextInt()));
            }
            result.setByte(i, sum);
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return engine.ofByte().stride(layout, result);
    }

    @Override
    public Tensor<Byte> mm(Tensor<Byte> t, Order askOrder) {
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

        var result = engine.ofByte().storage().zeros(m * p);
        var ret = engine.ofByte().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<Tensor<Byte>> rows = chunk(0, false, 1);
        List<Tensor<Byte>> cols = t.chunk(1, false, 1);

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
                                var krow = (BaseByteTensorStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    result.incByte(i * iStride + j * jStride, krow.vdot(cols.get(j), k, end));
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
    public Tensor<Byte> scatter() {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Scatter matrix can be computed only for matrices.");
        }
        Tensor<Byte> scatter = engine.ofByte().zeros(Shape.of(dim(1), dim(1)));
        Tensor<Byte> mean = engine.ofByte().zeros(Shape.of(dim(1)));
        for (int i = 0; i < dim(1); i++) {
            mean.setByte((byte) take(1, i).mean(), i);
        }
        for (int k = 0; k < dim(0); k++) {
            Tensor<Byte> row = take(0, k).squeeze(0).sub(mean);
            for (int i = 0; i < row.size(); i++) {
                for (int j = 0; j < row.size(); j++) {
                    scatter.incByte((byte) (row.getByte(i) * row.getByte(j)), i, j);
                }
            }
        }
        return scatter;
    }

    @Override
    public Byte norm(Byte pow) {
        if (pow < 0) {
            throw new IllegalArgumentException(STR."Norm power p=\{pow} must have a value greater than 0.");
        }
        if (dtype().castValue(1).equals(pow)) {
            return abs().sum();
        }
        if (dtype().castValue(2).equals(pow)) {
            return (byte) Math.sqrt(sqr().sum());
        }

        byte sum = (byte) 0;
        var it = loopIterator();
        while (it.hasNext()) {
            int offset = it.next();
            for (int i = 0, p = offset; i < it.size(); i++, p += it.step()) {
                byte value = (byte) Math.abs(storage.getByte(p));
                sum += (byte) Math.pow(value, pow);
            }
        }
        return (byte) Math.pow(sum, 1. / pow);
    }

    @Override
    public Tensor<Byte> normalize_(Byte pow) {
        return div_(norm(pow));
    }

    @Override
    protected Tensor<Byte> alongAxisOperation(Order order, int axis, Function<Tensor<Byte>, Byte> op) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Byte> res = engine.ofByte().zeros(Shape.of(newDims), Order.autoFC(order));
        var resIt = res.ptrIterator(Order.C);
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            var stride = engine.ofByte().stride(StrideLayout.of(Shape.of(selDim), ptr, new int[] {selStride}), storage);
            res.ptrSet(resIt.next(), op.apply(stride));
        }
        return res;
    }

    @Override
    public Byte mean() {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size();
        // first pass compute raw mean
        byte sum = 0;
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += storage.getByte(off + i * loop.step);
            }
        }
        byte mean = (byte) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (byte) (storage.getByte(off + i * loop.step) - mean);
            }
        }
        return (byte) (mean + sum / size);
    }

    @Override
    public Byte nanMean() {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size() - nanCount();
        // first pass compute raw mean
        byte sum = nanSum();

        byte mean = (byte) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int off : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                byte v = storage.getByte(off + i * loop.step);
                if (dtype().isNaN(v)) {
                    continue;
                }
                sum += (byte) (v - mean);
            }
        }
        return (byte) (mean + sum / size);
    }

    @Override
    public Byte var() {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size();
        // first pass compute raw mean
        byte sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += storage.getByte(offset + i * loop.step);
            }
        }
        byte mean = (byte) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (byte) (storage.getByte(offset + i * loop.step) - mean);
            }
        }
        mean += (byte) (sum / size);
        // third pass compute variance
        byte sum2 = 0;
        byte sum3 = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                sum2 += (byte) ((storage.getByte(p) - mean) * (storage.getByte(p) - mean));
                sum3 += (byte) (storage.getByte(p) - mean);
            }
        }
        return (byte) ((sum2 - (sum3 * sum3) / size) / size);
    }

    @Override
    public Byte varc(int ddof) {
        if (!dtype().floatingPoint()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        int size = size();
        // first pass compute raw mean
        byte sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += storage.getByte(offset + i * loop.step);
            }
        }
        byte mean = (byte) (sum / size);
        // second pass adjustments for mean
        sum = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                sum += (byte) (storage.getByte(offset + i * loop.step) - mean);
            }
        }
        mean += (byte) (sum / size);
        // third pass compute variance
        byte sum2 = 0;
        byte sum3 = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                sum2 += (byte) ((storage.getByte(p) - mean) * (storage.getByte(p) - mean));
                sum3 += (byte) (storage.getByte(p) - mean);
            }
        }
        return (byte) ((sum2 - (sum3 * sum3) / (size - ddof)) / (size - ddof));
    }

    @Override
    public int argmax(Order order) {
        int argmax = -1;
        byte argvalue = TensorAssociativeOp.MAX.initByte();
        var i = 0;
        var it = loopIterator(order);
        while (it.hasNext()) {
            int offset = it.next();
            for (int j = 0; j < loop.size; j++) {
                byte value = storage.getByte(offset + j * loop.step);
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
        byte argvalue = TensorAssociativeOp.MIN.initByte();
        var i = 0;
        var it = loopIterator(order);
        while (it.hasNext()) {
            int offset = it.next();
            for (int j = 0; j < loop.size; j++) {
                byte value = storage.getByte(offset + j * loop.step);
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
                if (dtype().isNaN(storage.getByte(p))) {
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
                if (storage.getByte(p) == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected Byte associativeOp(TensorAssociativeOp op) {
        byte agg = op.initByte();
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                agg = op.aggByte(agg, storage.getByte(p));
            }
        }
        return agg;
    }

    @Override
    protected Byte nanAssociativeOp(TensorAssociativeOp op) {
        byte aggregate = op.initByte();
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                if (!dtype().isNaN(storage.getByte(p))) {
                    aggregate = op.aggByte(aggregate, storage.getByte(p));
                }
            }
        }
        return aggregate;
    }

    @Override
    protected Tensor<Byte> associativeOpNarrow(TensorAssociativeOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Byte> res = engine.ofByte().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            byte value = StrideWrapper.of(ptr, selStride, selDim, this).aggregate(op.initByte(), op::aggByte);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    @Override
    protected Tensor<Byte> nanAssociativeOpNarrow(TensorAssociativeOp op, Order order, int axis) {
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        Tensor<Byte> res = engine.ofByte().zeros(Shape.of(newDims), Order.autoFC(order));
        var it = new StridePointerIterator(StrideLayout.of(newDims, layout().offset(), newStrides), Order.C);
        var resIt = res.ptrIterator(Order.C);
        while (it.hasNext()) {
            int ptr = it.nextInt();
            byte value = StrideWrapper.of(ptr, selStride, selDim, this).nanAggregate(DType.BYTE, op.initByte(), op::aggByte);
            res.ptrSet(resIt.next(), value);
        }
        return res;
    }

    @Override
    public Tensor<Byte> copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = engine.ofByte().storage().zeros(size());
        var dst = engine.ofByte().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(Storage<Byte> copy, Order askOrder) {
        var loop = StrideLoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                copy.setByte(last++, storage.getByte(p));
            }
        }
    }

    @Override
    public Tensor<Byte> copyTo(Tensor<Byte> to, Order askOrder) {

        if (to instanceof BaseByteTensorStride dst) {

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
                                    BaseByteTensorStride s = (BaseByteTensorStride) this.narrowAll(false, ss, es);
                                    BaseByteTensorStride d = (BaseByteTensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseByteTensorStride src, BaseByteTensorStride dst, Order askOrder) {
        var loop = StrideLoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int offset : loop.offsets) {
            for (int i = 0, p = offset; i < loop.size; i++, p += loop.step) {
                dst.storage.setByte(it2.nextInt(), src.storage.getByte(p));
            }
        }
    }

    public byte[] toArray() {
        if (!isVector()) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        byte[] copy = new byte[size()];
        int pos = 0;
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                copy[pos++] = storage.getByte(p);
            }
        }
        return copy;
    }

    public byte[] asArray() {
        if (storage instanceof ByteArrayStorage as && isVector() && layout.offset() == 0 && layout.stride(0) == 1) {
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
