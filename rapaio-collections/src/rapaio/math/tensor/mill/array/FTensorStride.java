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

package rapaio.math.tensor.mill.array;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.min;
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
import java.util.stream.StreamSupport;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorMill;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarChunkIterator;
import rapaio.math.tensor.iterators.StrideChunkDescriptor;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.mill.AbstractTensor;
import rapaio.math.tensor.mill.TensorValidation;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public final class FTensorStride extends AbstractTensor<Float, FTensor> implements FTensor {

    private static final VectorSpecies<Float> SPEC = FloatVector.SPECIES_PREFERRED;
    private static final int SPEC_LEN = SPEC.length();

    private final StrideLayout layout;
    private final ArrayTensorMill mill;
    private final float[] array;

    private final StrideChunkDescriptor chd;
    private final int[] chdIndexes;

    public FTensorStride(ArrayTensorMill mill, Shape shape, int offset, int[] strides, float[] array) {
        this(mill, StrideLayout.of(shape, offset, strides), array);
    }

    public FTensorStride(ArrayTensorMill mill, Shape shape, int offset, Order order, float[] array) {
        this(mill, StrideLayout.ofDense(shape, offset, order), array);
    }

    public FTensorStride(ArrayTensorMill mill, StrideLayout layout, float[] array) {
        this.layout = layout;
        this.mill = mill;
        this.array = array;
        this.chd = StrideChunkDescriptor.of(layout, layout.storageFastOrder());
        this.chdIndexes = chd.loopStep() == 1 ? null : chunkIndexes(chd.loopStep());
    }

    private int[] chunkIndexes(int step) {
        int[] indexes = new int[SPEC_LEN];
        for (int i = 1; i < SPEC_LEN; i++) {
            indexes[i] = indexes[i - 1] + step;
        }
        return indexes;
    }

    @Override
    public DType<Float, FTensor> dtype() {
        return DType.FLOAT;
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

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : chd.chunkOffsets()) {
            int loopBound = SPEC.loopBound(chd.loopSize()) + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop());
                a.intoArray(array, i);
            }
            for (; i < chd.loopSize() + off; i++) {
                array[i] = op.applyFloat(array[i]);
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : chd.chunkOffsets()) {
            int loopLen = chd.loopSize() * chd.loopStep() + off;
            int loopBound = SPEC.loopBound(chd.loopSize()) * chd.loopStep() + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN * chd.loopStep()) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, chdIndexes, 0);
                a = a.lanewise(op.vop());
                a.intoArray(array, i, chdIndexes, 0);
            }
            for (; i < loopLen; i += chd.loopStep()) {
                array[i] = op.applyFloat(array[i]);
            }
        }
    }

    private void unaryOp(TensorUnaryOp op) {
        if (chd.loopStep() == 1) {
            unaryOpUnit(op);
        } else {
            unaryOpStep(op);
        }
    }

    @Override
    public FTensorStride abs_() {
        unaryOp(TensorUnaryOp.ABS);
        return this;
    }

    @Override
    public FTensorStride neg_() {
        unaryOp(TensorUnaryOp.NEG);
        return this;
    }

    @Override
    public FTensorStride log_() {
        unaryOp(TensorUnaryOp.LOG);
        return this;
    }

    @Override
    public FTensorStride log1p_() {
        unaryOp(TensorUnaryOp.LOG1P);
        return this;
    }

    @Override
    public FTensorStride exp_() {
        unaryOp(TensorUnaryOp.EXP);
        return this;
    }

    @Override
    public FTensorStride expm1_() {
        unaryOp(TensorUnaryOp.EXPM1);
        return this;
    }

    @Override
    public FTensorStride sin_() {
        unaryOp(TensorUnaryOp.SIN);
        return this;
    }

    @Override
    public FTensorStride asin_() {
        unaryOp(TensorUnaryOp.ASIN);
        return this;
    }

    @Override
    public FTensorStride sinh_() {
        unaryOp(TensorUnaryOp.SINH);
        return this;
    }

    @Override
    public FTensorStride cos_() {
        unaryOp(TensorUnaryOp.COS);
        return this;
    }

    @Override
    public FTensorStride acos_() {
        unaryOp(TensorUnaryOp.ACOS);
        return this;
    }

    @Override
    public FTensorStride cosh_() {
        unaryOp(TensorUnaryOp.COSH);
        return this;
    }

    @Override
    public FTensorStride tan_() {
        unaryOp(TensorUnaryOp.TAN);
        return this;
    }

    @Override
    public FTensorStride atan_() {
        unaryOp(TensorUnaryOp.ATAN);
        return this;
    }

    @Override
    public FTensorStride tanh_() {
        unaryOp(TensorUnaryOp.TANH);
        return this;
    }

    void binaryVectorOp(TensorBinaryOp op, FTensor b) {
        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = ptrIterator(order);
        var refIt = b.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            array[next] = op.applyFloat(array[next], b.ptrGet(refIt.nextInt()));
        }
    }

    @Override
    public FTensorStride add_(FTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.ADD, tensor);
        return this;
    }

    @Override
    public FTensorStride sub_(FTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.SUB, tensor);
        return this;
    }

    @Override
    public FTensorStride mul_(FTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.MUL, tensor);
        return this;
    }

    @Override
    public FTensorStride div_(FTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.DIV, tensor);
        return this;
    }

    void binaryScalarOpUnit(TensorBinaryOp op, float value) {
        for (int off : chd.chunkOffsets()) {
            int loopBound = SPEC.loopBound(chd.loopSize()) + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i);
            }
            for (; i < chd.loopSize() + off; i++) {
                array[i] = op.applyFloat(array[i], value);
            }
        }
    }

    void binaryScalarOpStep(TensorBinaryOp op, float value) {
        for (int off : chd.chunkOffsets()) {
            int loopLen = chd.loopSize() * chd.loopStep() + off;
            int loopBound = SPEC.loopBound(chd.loopSize()) * chd.loopStep() + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN * chd.loopStep()) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, chdIndexes, 0);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i, chdIndexes, 0);
            }
            for (; i < loopLen; i += chd.loopStep()) {
                array[i] = op.applyFloat(array[i], value);
            }
        }
    }

    void binaryScalarOp(TensorBinaryOp op, float value) {
        if (chd.loopStep() == 1) {
            binaryScalarOpUnit(op, value);
        } else {
            binaryScalarOpStep(op, value);
        }
    }

    @Override
    public FTensorStride add_(float value) {
        binaryScalarOp(TensorBinaryOp.ADD, value);
        return this;
    }

    @Override
    public FTensorStride sub_(float value) {
        binaryScalarOp(TensorBinaryOp.SUB, value);
        return this;
    }

    @Override
    public FTensorStride mul_(float value) {
        binaryScalarOp(TensorBinaryOp.MUL, value);
        return this;
    }

    @Override
    public FTensorStride div_(float value) {
        binaryScalarOp(TensorBinaryOp.DIV, value);
        return this;
    }

    @Override
    public float vdotFloat(FTensor tensor) {
        return vdotFloat(tensor, 0, shape().dim(0));
    }

    @Override
    public float vdotFloat(FTensor tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        FTensorStride dts = (FTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);
        int start1 = layout.offset() + start * step1;
        int start2 = dts.layout.offset() + start * step2;
        int i = 0;
        int loopBound = SPEC.loopBound(end - start);
        FloatVector vsum = FloatVector.zero(SPEC);
        for (; i < loopBound; i += SPEC_LEN) {
            FloatVector a = chd.loopStep() == 1 ?
                    FloatVector.fromArray(SPEC, array, start1) :
                    FloatVector.fromArray(SPEC, array, start1, chdIndexes, 0);
            FloatVector b = dts.chd.loopStep() == 1 ?
                    FloatVector.fromArray(SPEC, dts.array, start2) :
                    FloatVector.fromArray(SPEC, dts.array, start2, dts.chdIndexes, 0);
            vsum = vsum.add(a.mul(b));
            start1 += SPEC_LEN * step1;
            start2 += SPEC_LEN * step2;
        }
        float sum = vsum.reduceLanes(VectorOperators.ADD);
        for (; i < end - start; i++) {
            sum += array[start1] * dts.array[start2];
            start1 += step1;
            start2 += step2;
        }
        return sum;
    }

    @Override
    public FTensor mv(FTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Operands are not valid for matrix-vector multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        float[] result = new float[shape().dim(0)];
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            float sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += ptrGetFloat(it.nextInt()) * tensor.ptrGetFloat(innerIt.nextInt());
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return mill.ofFloat().stride(layout, result);
    }

    @Override
    public FTensor mm(FTensor t, Order askOrder) {
        if (shape().rank() != 2 || t.shape().rank() != 2 || shape().dim(1) != t.shape().dim(0)) {
            throw new IllegalArgumentException("Operands are not valid for matrix-matrix multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), t.shape().toString()));
        }
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Illegal askOrder value, must be Order.C or Order.F");
        }
        int m = shape().dim(0);
        int n = shape().dim(1);
        int p = t.shape().dim(1);

        var result = new float[m * p];
        var ret = mill.ofFloat().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<FTensor> rows = chunk(0, false, 1);
        List<FTensor> cols = t.chunk(1, false, 1);

        int chunk = (int) floor(sqrt((float) L2_CACHE_SIZE / 2 / CORES / dtype().bytes()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) ceil(sqrt(chunk / 4.)) : (int) ceil(sqrt(chunk));

        int iStride = ((StrideLayout) ret.layout()).stride(0);
        int jStride = ((StrideLayout) ret.layout()).stride(1);

        List<Future<?>> futures = new ArrayList<>();
        try (ExecutorService service = Executors.newFixedThreadPool(mill.cpuThreads())) {
            for (int r = 0; r < m; r += innerChunk) {
                int rs = r;
                int re = min(m, r + innerChunk);

                futures.add(service.submit(() -> {
                    for (int c = 0; c < p; c += innerChunk) {
                        int cs = c;
                        int ce = min(p, c + innerChunk);

                        for (int k = 0; k < n; k += vectorChunk) {
                            int end = min(n, k + vectorChunk);
                            for (int i = rs; i < re; i++) {
                                var krow = (FTensorStride) rows.get(i);
                                for (int j = cs; j < ce; j++) {
                                    result[i * iStride + j * jStride] += krow.vdotFloat(cols.get(j), k, end);
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
    public Iterator<Float> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(i -> array[i]).iterator();
    }

    @Override
    public FTensorStride iteratorApply(Order askOrder, IntIntBiFunction<Float> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            array[p] = apply.applyAsInt(i++, p);
        }
        return this;
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
    public ChunkIterator chunkIterator(Order askOrder) {
        if (layout.rank() == 0) {
            return new ScalarChunkIterator(layout.offset());
        }
        return new StrideChunkIterator(layout, askOrder);
    }

    @Override
    public FTensor reshape(Shape askShape, Order askOrder) {
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
        FTensor copy = mill.ofFloat().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetFloat(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public FTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return mill.ofFloat().stride(compact, array);
        }
        return flatten(askOrder);
    }

    @Override
    public FTensor flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = new float[layout.size()];
        int p = 0;
        var it = chunkIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out[p++] = array[i];
            }
        }
        return mill.ofFloat().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public FTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : mill.ofFloat().stride(layout.squeeze(), array);
    }

    @Override
    public FTensor squeeze(int axis) {
        return layout.shape().dim(axis) != 1 ? this : mill.ofFloat().stride(layout.squeeze(axis), array);
    }

    @Override
    public FTensor unsqueeze(int axis) {
        return mill.ofFloat().stride(layout().unsqueeze(axis), array);
    }

    @Override
    public FTensor t_() {
        return mill.ofFloat().stride(layout.revert(), array);
    }

    @Override
    public FTensor t(Order askOrder) {
        return t_().copy(askOrder);
    }

    @Override
    public FTensor moveAxis(int src, int dst) {
        return mill.ofFloat().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public FTensor swapAxis(int src, int dst) {
        return mill.ofFloat().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public FTensor narrow(int axis, boolean keepdim, int start, int end) {
        return mill.ofFloat().stride(layout.narrow(axis, keepdim, start, end), array);
    }

    @Override
    public FTensor narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return mill.ofFloat().stride(layout.narrowAll(keepdim, starts, ends), array);
    }

    @Override
    public List<FTensor> split(int axis, boolean keepdim, int... indexes) {
        List<FTensor> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    @Override
    public List<FTensor> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<FTensor> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRec(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRec(List<FTensor> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
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
    public FTensor repeat(int axis, int repeat, boolean stack) {
        FTensor[] copies = new FTensor[repeat];
        Arrays.fill(copies, this);
        if (stack) {
            return mill.stack(axis, Arrays.asList(copies));
        } else {
            return mill.concat(axis, Arrays.asList(copies));
        }
    }

    @Override
    public FTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        float[] copy = new float[size()];
        FTensorStride dst = (FTensorStride) mill.ofFloat().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(float[] copy, Order askOrder) {
        var chd = StrideChunkDescriptor.of(layout, askOrder);
        var last = 0;
        for (int ptr : chd.chunkOffsets()) {
            if (chd.loopStep() == 1) {
                int i = ptr;
                int loopBound = SPEC.loopBound(chd.loopSize()) + ptr;
                for (; i < loopBound; i += SPEC_LEN) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i);
                    a.intoArray(copy, last);
                    last += SPEC_LEN;
                }
                for (; i < ptr + chd.loopSize(); i++) {
                    copy[last++] = array[i];
                }
            } else {
                for (int i = ptr; i < ptr + chd.loopLength(); i += chd.loopStep()) {
                    copy[last++] = array[i];
                }
            }
        }
    }

    private void copyTo(FTensorStride dst, Order askOrder) {

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
                                FTensorStride s = (FTensorStride) this.narrowAll(false, ss, es);
                                FTensorStride d = (FTensorStride) dst.narrowAll(false, ss, es);
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

            return;
        }

        directCopyTo(this, dst, askOrder);
    }

    private void directCopyTo(FTensorStride src, FTensorStride dst, Order askOrder) {
        var chd = StrideChunkDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int ptr : chd.chunkOffsets()) {
            for (int i = ptr; i < ptr + chd.loopLength(); i += chd.loopStep()) {
                dst.array[it2.nextInt()] = src.array[i];
            }
        }
    }
}
