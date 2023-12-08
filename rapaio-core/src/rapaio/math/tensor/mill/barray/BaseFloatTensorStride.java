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

package rapaio.math.tensor.mill.barray;

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

import rapaio.math.tensor.FloatTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Statistics;
import rapaio.math.tensor.TensorMill;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.LoopIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarLoopIterator;
import rapaio.math.tensor.iterators.StrideLoopDescriptor;
import rapaio.math.tensor.iterators.StrideLoopIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.mill.AbstractTensor;
import rapaio.math.tensor.mill.TensorValidation;
import rapaio.math.tensor.mill.varray.VectorizedFloatTensorStride;
import rapaio.math.tensor.operator.TensorAssociativeOp;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.util.NotImplementedException;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public sealed class BaseFloatTensorStride extends AbstractTensor<Float, FloatTensor> implements FloatTensor
        permits VectorizedFloatTensorStride {

    protected final StrideLayout layout;
    protected final TensorMill mill;
    protected final float[] array;
    protected final StrideLoopDescriptor loop;

    public BaseFloatTensorStride(TensorMill mill, Shape shape, int offset, int[] strides, float[] array) {
        this(mill, StrideLayout.of(shape, offset, strides), array);
    }

    public BaseFloatTensorStride(TensorMill mill, Shape shape, int offset, Order order, float[] array) {
        this(mill, StrideLayout.ofDense(shape, offset, order), array);
    }

    public BaseFloatTensorStride(TensorMill mill, StrideLayout layout, float[] array) {
        this.layout = layout;
        this.mill = mill;
        this.array = array;
        this.loop = StrideLoopDescriptor.of(layout, layout.storageFastOrder());
    }

    @Override
    public TensorMill mill() {
        return mill;
    }

    @Override
    public StrideLayout layout() {
        return layout;
    }

    @Override
    public FloatTensor reshape(Shape askShape, Order askOrder) {
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
        FloatTensor copy = mill.ofFloat().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetFloat(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public FloatTensor transpose() {
        return mill.ofFloat().stride(layout.revert(), array);
    }

    @Override
    public FloatTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return mill.ofFloat().stride(compact, array);
        }
        return flatten(askOrder);
    }

    @Override
    public FloatTensor flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = new float[layout.size()];
        int p = 0;
        var it = loopIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.bound(); i += it.step()) {
                out[p++] = array[i];
            }
        }
        return mill.ofFloat().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public FloatTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : mill.ofFloat().stride(layout.squeeze(), array);
    }

    @Override
    public FloatTensor squeeze(int axis) {
        return layout.shape().dim(axis) != 1 ? this : mill.ofFloat().stride(layout.squeeze(axis), array);
    }

    @Override
    public FloatTensor unsqueeze(int axis) {
        return mill.ofFloat().stride(layout().unsqueeze(axis), array);
    }

    @Override
    public FloatTensor moveAxis(int src, int dst) {
        return mill.ofFloat().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public FloatTensor swapAxis(int src, int dst) {
        return mill.ofFloat().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public FloatTensor narrow(int axis, boolean keepdim, int start, int end) {
        return mill.ofFloat().stride(layout.narrow(axis, keepdim, start, end), array);
    }

    @Override
    public FloatTensor narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return mill.ofFloat().stride(layout.narrowAll(keepdim, starts, ends), array);
    }

    @Override
    public List<FloatTensor> split(int axis, boolean keepdim, int... indexes) {
        List<FloatTensor> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    @Override
    public List<FloatTensor> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<FloatTensor> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRec(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRec(List<FloatTensor> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
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
    public FloatTensor repeat(int axis, int repeat, boolean stack) {
        FloatTensor[] copies = new FloatTensor[repeat];
        Arrays.fill(copies, this);
        if (stack) {
            return mill.stack(axis, Arrays.asList(copies));
        } else {
            return mill.concat(axis, Arrays.asList(copies));
        }
    }

    @Override
    public FloatTensor tile(int[] repeats) {
        throw new NotImplementedException();
    }

    @Override
    public FloatTensor permute(int[] dims) {
        return mill.ofFloat().stride(layout().permute(dims), array);
    }

    @Override
    public float getFloat(int... indexes) {
        return array[layout.pointer(indexes)];
    }

    @Override
    public void setFloat(float value, int... indexes) {
        array[layout.pointer(indexes)] = value;
    }

    @Override
    public float ptrGetFloat(int ptr) {
        return array[ptr];
    }

    @Override
    public void ptrSetFloat(int ptr, float value) {
        array[ptr] = value;
    }

    @Override
    public Iterator<Float> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(i -> array[i]).iterator();
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
    public BaseFloatTensorStride apply(Order askOrder, IntIntBiFunction<Float> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            array[p] = apply.applyAsInt(i++, p);
        }
        return this;
    }

    @Override
    public FloatTensor apply(Function<Float, Float> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            array[ptr] = fun.apply(array[ptr]);
        }
        return this;
    }

    @Override
    public FloatTensor fill(Float value) {
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                array[i] = value;
            }
        }
        return this;
    }

    @Override
    public FloatTensor fillNan(Float value) {
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                if (dtype().isNaN(array[i])) {
                    array[i] = value;
                }
            }
        }
        return this;
    }

    @Override
    public FloatTensor clamp(Float min, Float max) {
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                if (!dtype().isNaN(min) && array[i] < min) {
                    array[i] = min;
                }
                if (!dtype().isNaN(max) && array[i] > max) {
                    array[i] = max;
                }
            }
        }
        return this;
    }

    @Override
    public FloatTensor take(Order order, int... indexes) {
        throw new NotImplementedException();
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            for (int i = off; i < loop.bound + off; i += loop.step) {
                array[i] = op.applyFloat(array[i]);
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
    public BaseFloatTensorStride abs() {
        unaryOp(TensorUnaryOp.ABS);
        return this;
    }

    @Override
    public BaseFloatTensorStride negate() {
        unaryOp(TensorUnaryOp.NEG);
        return this;
    }

    @Override
    public BaseFloatTensorStride log() {
        unaryOp(TensorUnaryOp.LOG);
        return this;
    }

    @Override
    public BaseFloatTensorStride log1p() {
        unaryOp(TensorUnaryOp.LOG1P);
        return this;
    }

    @Override
    public BaseFloatTensorStride exp() {
        unaryOp(TensorUnaryOp.EXP);
        return this;
    }

    @Override
    public BaseFloatTensorStride expm1() {
        unaryOp(TensorUnaryOp.EXPM1);
        return this;
    }

    @Override
    public BaseFloatTensorStride sin() {
        unaryOp(TensorUnaryOp.SIN);
        return this;
    }

    @Override
    public BaseFloatTensorStride asin() {
        unaryOp(TensorUnaryOp.ASIN);
        return this;
    }

    @Override
    public BaseFloatTensorStride sinh() {
        unaryOp(TensorUnaryOp.SINH);
        return this;
    }

    @Override
    public BaseFloatTensorStride cos() {
        unaryOp(TensorUnaryOp.COS);
        return this;
    }

    @Override
    public BaseFloatTensorStride acos() {
        unaryOp(TensorUnaryOp.ACOS);
        return this;
    }

    @Override
    public BaseFloatTensorStride cosh() {
        unaryOp(TensorUnaryOp.COSH);
        return this;
    }

    @Override
    public BaseFloatTensorStride tan() {
        unaryOp(TensorUnaryOp.TAN);
        return this;
    }

    @Override
    public BaseFloatTensorStride atan() {
        unaryOp(TensorUnaryOp.ATAN);
        return this;
    }

    @Override
    public BaseFloatTensorStride tanh() {
        unaryOp(TensorUnaryOp.TANH);
        return this;
    }

    protected void binaryVectorOp(TensorBinaryOp op, FloatTensor b) {
        var order = layout.storageFastOrder();
        order = order == Order.S ? Order.defaultOrder() : order;

        var it = ptrIterator(order);
        var refIt = b.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            array[next] = op.applyFloat(array[next], b.ptrGet(refIt.nextInt()));
        }
    }

    @Override
    public BaseFloatTensorStride add(FloatTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.ADD, tensor);
        return this;
    }

    @Override
    public BaseFloatTensorStride sub(FloatTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.SUB, tensor);
        return this;
    }

    @Override
    public BaseFloatTensorStride mul(FloatTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.MUL, tensor);
        return this;
    }

    @Override
    public BaseFloatTensorStride div(FloatTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.DIV, tensor);
        return this;
    }

    void binaryScalarOpStep(TensorBinaryOp op, float value) {
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                array[i] = op.applyFloat(array[i], value);
            }
        }
    }

    protected void binaryScalarOp(TensorBinaryOp op, float value) {
        binaryScalarOpStep(op, value);
    }

    @Override
    public BaseFloatTensorStride add(Float value) {
        binaryScalarOp(TensorBinaryOp.ADD, value);
        return this;
    }

    @Override
    public BaseFloatTensorStride sub(Float value) {
        binaryScalarOp(TensorBinaryOp.SUB, value);
        return this;
    }

    @Override
    public BaseFloatTensorStride mul(Float value) {
        binaryScalarOp(TensorBinaryOp.MUL, value);
        return this;
    }

    @Override
    public BaseFloatTensorStride div(Float value) {
        binaryScalarOp(TensorBinaryOp.DIV, value);
        return this;
    }

    @Override
    public Float vdot(FloatTensor tensor) {
        return vdot(tensor, 0, shape().dim(0));
    }

    @Override
    public Float vdot(FloatTensor tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        BaseFloatTensorStride dts = (BaseFloatTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);

        int start1 = layout.offset() + start * step1;
        int end1 = layout.offset() + end * step1;
        int start2 = dts.layout.offset() + start * step2;

        float sum = 0;
        for (int i = start1; i < end1; i += step1) {
            sum += (float)(array[i] * dts.array[start2]);
            start2 += step2;
        }
        return sum;
    }

    @Override
    public FloatTensor mv(FloatTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    STR."Operands are not valid for matrix-vector multiplication \{"(m = %s, v = %s).".formatted(shape(),
                            tensor.shape())}");
        }
        float[] result = new float[shape().dim(0)];
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            float sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += (float)(ptrGetFloat(it.nextInt()) * tensor.ptrGetFloat(innerIt.nextInt()));
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return mill.ofFloat().stride(layout, result);
    }

    @Override
    public FloatTensor mm(FloatTensor t, Order askOrder) {
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

        var result = new float[m * p];
        var ret = mill.ofFloat().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<FloatTensor> rows = chunk(0, false, 1);
        List<FloatTensor> cols = t.chunk(1, false, 1);

        int chunk = (int) floor(sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().bytes()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) ceil(sqrt(chunk / 4.)) : (int) ceil(sqrt(chunk));

        int iStride = ((StrideLayout) ret.layout()).stride(0);
        int jStride = ((StrideLayout) ret.layout()).stride(1);

        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService service = Executors.newFixedThreadPool(mill.cpuThreads())) {
            for (int r = 0; r < m; r += innerChunk) {
                int rs = r;
                int re = Math.min(m, r + innerChunk);

                futures.add(service.submit(() -> {
                    for (int c = 0; c < p; c += innerChunk) {
                        int ce = Math.min(p, c + innerChunk);

                        for (int k = 0; k < n; k += vectorChunk) {
                            int end = Math.min(n, k + vectorChunk);
                            for (int i = rs; i < re; i++) {
                                var krow = (BaseFloatTensorStride) rows.get(i);
                                for (int j = c; j < ce; j++) {
                                    result[i * iStride + j * jStride] += krow.vdot(cols.get(j), k, end);
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
    public Statistics<Float, FloatTensor> stats() {
        if (!dtype().isFloat()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }

        int size = size();
        int nanSize = 0;
        float mean;
        float nanMean;
        float variance;
        float nanVariance;

        // first pass compute raw mean
        float sum = 0;
        float nanSum = 0;
        for (int offset : loop.offsets) {
            int i = offset;
            for (; i < loop.bound + offset; i += loop.step) {
                sum += array[i];
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i];
                    nanSize++;
                }
            }
        }
        mean = (float) (sum / size);
        nanMean = (float) (nanSum / nanSize);

        // second pass adjustments for mean
        sum = 0;
        nanSum = 0;
        for (int offset : loop.offsets) {
            int i = offset;
            for (; i < loop.bound + offset; i += loop.step) {
                sum += (float)(array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum += (float)(array[i] - nanMean);
                }
            }
        }
        mean += (float) (sum / size);
        nanMean += (float)(nanSum / nanSize);

        // third pass compute variance
        float sum2 = 0;
        float sum3 = 0;
        float nanSum2 = 0;
        float nanSum3 = 0;

        for (int offset : loop.offsets) {
            int i = offset;
            for (; i < loop.bound + offset; i += loop.step) {
                sum2 += (float)((array[i] - mean) * (array[i] - mean));
                sum3 += (float)(array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (float)((array[i] - nanMean) * (array[i] - nanMean));
                    nanSum3 += (float)(array[i] - nanMean);
                }
            }
        }
        variance = (float) ((sum2 - (sum3 * sum3) / size) / size);
        nanVariance = (float) ((nanSum2 - (nanSum3 * nanSum3) / nanSize) / nanSize);

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
    }

    @Override
    public Float sum() {
        return associativeOp(TensorAssociativeOp.ADD);
    }

    @Override
    public Float nanSum() {
        return nanAssociativeOp(TensorAssociativeOp.ADD);
    }

    @Override
    public Float prod() {
        return associativeOp(TensorAssociativeOp.MUL);
    }

    @Override
    public Float nanProd() {
        return nanAssociativeOp(TensorAssociativeOp.MUL);
    }

    @Override
    public Float max() {
        return associativeOp(TensorAssociativeOp.MAX);
    }

    @Override
    public Float nanMax() {
        return nanAssociativeOp(TensorAssociativeOp.MAX);
    }

    @Override
    public Float min() {
        return associativeOp(TensorAssociativeOp.MIN);
    }

    @Override
    public Float nanMin() {
        return nanAssociativeOp(TensorAssociativeOp.MIN);
    }

    @Override
    public int nanCount() {
        int count = 0;
        for (int offset : loop.offsets) {
            int i = offset;
            for (; i < loop.bound + offset; i += loop.step) {
                if (dtype().isNaN(array[i])) {
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
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                if (array[i] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    protected float associativeOp(TensorAssociativeOp op) {
        float agg = op.initialFloat();
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                agg = op.applyFloat(agg, array[i]);
            }
        }
        return agg;
    }

    protected float nanAssociativeOp(TensorAssociativeOp op) {
        float aggregate = op.initialFloat();
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i += loop.step) {
                if (!dtype().isNaN(array[i])) {
                    aggregate = op.applyFloat(aggregate, array[i]);
                }
            }
        }
        return aggregate;
    }

    @Override
    public FloatTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        float[] copy = new float[size()];
        var dst = mill.ofFloat().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(float[] copy, Order askOrder) {
        var chd = StrideLoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int ptr : chd.offsets) {
            for (int i = ptr; i < ptr + chd.bound; i += chd.step) {
                copy[last++] = array[i];
            }
        }
    }

    @Override
    public FloatTensor copyTo(FloatTensor to, Order askOrder) {

        if (to instanceof BaseFloatTensorStride dst) {

            int limit = Math.floorDiv(L2_CACHE_SIZE, dtype().bytes() * 2 * mill.cpuThreads() * 8);

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

                try (ExecutorService executor = Executors.newFixedThreadPool(mill.cpuThreads())) {
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
                                    BaseFloatTensorStride s = (BaseFloatTensorStride) this.narrowAll(false, ss, es);
                                    BaseFloatTensorStride d = (BaseFloatTensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(BaseFloatTensorStride src, BaseFloatTensorStride dst, Order askOrder) {
        var chd = StrideLoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int ptr : chd.offsets) {
            for (int i = ptr; i < ptr + chd.bound; i += chd.step) {
                dst.array[it2.nextInt()] = src.array[i];
            }
        }
    }

    @Override
    public float[] toArray() {
        if (shape().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        float[] copy = new float[size()];
        int pos = 0;
        for (int offset : loop.offsets) {
            for (int i = offset; i < loop.bound + offset; i++) {
                copy[pos++] = array[i];
            }
        }
        return copy;
    }
}
