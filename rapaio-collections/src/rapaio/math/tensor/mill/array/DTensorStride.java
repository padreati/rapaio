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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.DTensor;
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

public final class DTensorStride extends AbstractTensor<Double, DTensor> implements DTensor {

    private static final VectorSpecies<Double> SPEC = DoubleVector.SPECIES_PREFERRED;
    private static final int SPEC_LEN = SPEC.length();

    private final StrideLayout layout;
    private final ArrayTensorMill mill;
    private final double[] array;

    private final StrideChunkDescriptor chd;
    private final int[] chdIndexes;

    public DTensorStride(ArrayTensorMill mill, Shape shape, int offset, int[] strides, double[] array) {
        this(mill, StrideLayout.of(shape, offset, strides), array);
    }

    public DTensorStride(ArrayTensorMill mill, Shape shape, int offset, Order order, double[] array) {
        this(mill, StrideLayout.ofDense(shape, offset, order), array);
    }

    public DTensorStride(ArrayTensorMill mill, StrideLayout layout, double[] array) {
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
    public DType<Double, DTensor> dtype() {
        return DType.DOUBLE;
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
    public double getDouble(int... indexes) {
        return array[layout.pointer(indexes)];
    }

    @Override
    public void setDouble(double value, int... indexes) {
        array[layout.pointer(indexes)] = value;
    }

    @Override
    public double ptrGetDouble(int ptr) {
        return array[ptr];
    }

    @Override
    public void ptrSetDouble(int ptr, double value) {
        array[ptr] = value;
    }

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : chd.chunkOffsets()) {
            int loopBound = SPEC.loopBound(chd.loopSize()) + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop());
                a.intoArray(array, i);
            }
            for (; i < chd.loopSize() + off; i++) {
                array[i] = op.applyDouble(array[i]);
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : chd.chunkOffsets()) {
            int loopLen = chd.loopSize() * chd.loopStep() + off;
            int loopBound = SPEC.loopBound(chd.loopSize()) * chd.loopStep() + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN * chd.loopStep()) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i, chdIndexes, 0);
                a = a.lanewise(op.vop());
                a.intoArray(array, i, chdIndexes, 0);
            }
            for (; i < loopLen; i += chd.loopStep()) {
                array[i] = op.applyDouble(array[i]);
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
    public DTensorStride abs_() {
        unaryOp(TensorUnaryOp.ABS);
        return this;
    }

    @Override
    public DTensorStride neg_() {
        unaryOp(TensorUnaryOp.NEG);
        return this;
    }

    @Override
    public DTensorStride log_() {
        unaryOp(TensorUnaryOp.LOG);
        return this;
    }

    @Override
    public DTensorStride log1p_() {
        unaryOp(TensorUnaryOp.LOG1P);
        return this;
    }

    @Override
    public DTensorStride exp_() {
        unaryOp(TensorUnaryOp.EXP);
        return this;
    }

    @Override
    public DTensorStride expm1_() {
        unaryOp(TensorUnaryOp.EXPM1);
        return this;
    }

    @Override
    public DTensorStride sin_() {
        unaryOp(TensorUnaryOp.SIN);
        return this;
    }

    @Override
    public DTensorStride asin_() {
        unaryOp(TensorUnaryOp.ASIN);
        return this;
    }

    @Override
    public DTensorStride sinh_() {
        unaryOp(TensorUnaryOp.SINH);
        return this;
    }

    @Override
    public DTensorStride cos_() {
        unaryOp(TensorUnaryOp.COS);
        return this;
    }

    @Override
    public DTensorStride acos_() {
        unaryOp(TensorUnaryOp.ACOS);
        return this;
    }

    @Override
    public DTensorStride cosh_() {
        unaryOp(TensorUnaryOp.COSH);
        return this;
    }

    @Override
    public DTensorStride tan_() {
        unaryOp(TensorUnaryOp.TAN);
        return this;
    }

    @Override
    public DTensorStride atan_() {
        unaryOp(TensorUnaryOp.ATAN);
        return this;
    }

    @Override
    public DTensorStride tanh_() {
        unaryOp(TensorUnaryOp.TANH);
        return this;
    }

    void binaryVectorOp(TensorBinaryOp op, DTensor b) {
        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = ptrIterator(order);
        var refIt = b.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            array[next] = op.applyDouble(array[next], b.ptrGet(refIt.nextInt()));
        }
    }

    @Override
    public DTensorStride add_(DTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.ADD, tensor);
        return this;
    }

    @Override
    public DTensorStride sub_(DTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.SUB, tensor);
        return this;
    }

    @Override
    public DTensorStride mul_(DTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.MUL, tensor);
        return this;
    }

    @Override
    public DTensorStride div_(DTensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.DIV, tensor);
        return this;
    }

    void binaryScalarOpUnit(TensorBinaryOp op, double value) {
        for (int off : chd.chunkOffsets()) {
            int loopBound = SPEC.loopBound(chd.loopSize()) + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i);
            }
            for (; i < chd.loopSize() + off; i++) {
                array[i] = op.applyDouble(array[i], value);
            }
        }
    }

    void binaryScalarOpStep(TensorBinaryOp op, double value) {
        for (int off : chd.chunkOffsets()) {
            int loopLen = chd.loopSize() * chd.loopStep() + off;
            int loopBound = SPEC.loopBound(chd.loopSize()) * chd.loopStep() + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN * chd.loopStep()) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i, chdIndexes, 0);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i, chdIndexes, 0);
            }
            for (; i < loopLen; i += chd.loopStep()) {
                array[i] = op.applyDouble(array[i], value);
            }
        }
    }

    void binaryScalarOp(TensorBinaryOp op, double value) {
        if (chd.loopStep() == 1) {
            binaryScalarOpUnit(op, value);
        } else {
            binaryScalarOpStep(op, value);
        }
    }

    @Override
    public DTensorStride add_(double value) {
        binaryScalarOp(TensorBinaryOp.ADD, value);
        return this;
    }

    @Override
    public DTensorStride sub_(double value) {
        binaryScalarOp(TensorBinaryOp.SUB, value);
        return this;
    }

    @Override
    public DTensorStride mul_(double value) {
        binaryScalarOp(TensorBinaryOp.MUL, value);
        return this;
    }

    @Override
    public DTensorStride div_(double value) {
        binaryScalarOp(TensorBinaryOp.DIV, value);
        return this;
    }

    @Override
    public double vdotDouble(DTensor tensor) {
        return vdotDouble(tensor, 0, shape().dim(0));
    }

    @Override
    public double vdotDouble(DTensor tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        DTensorStride dts = (DTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);
        int start1 = layout.offset() + start * step1;
        int start2 = dts.layout.offset() + start * step2;
        int i = 0;
        int loopBound = SPEC.loopBound(end - start);
        DoubleVector vsum = DoubleVector.zero(SPEC);
        for (; i < loopBound; i += SPEC_LEN) {
            DoubleVector a = chd.loopStep() == 1 ?
                    DoubleVector.fromArray(SPEC, array, start1) :
                    DoubleVector.fromArray(SPEC, array, start1, chdIndexes, 0);
            DoubleVector b = dts.chd.loopStep() == 1 ?
                    DoubleVector.fromArray(SPEC, dts.array, start2) :
                    DoubleVector.fromArray(SPEC, dts.array, start2, dts.chdIndexes, 0);
            vsum = vsum.add(a.mul(b));
            start1 += SPEC_LEN * step1;
            start2 += SPEC_LEN * step2;
        }
        double sum = vsum.reduceLanes(VectorOperators.ADD);
        for (; i < end - start; i++) {
            sum += array[start1] * dts.array[start2];
            start1 += step1;
            start2 += step2;
        }
        return sum;
    }

    @Override
    public DTensor mv(DTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Operands are not valid for matrix-vector multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        double[] result = new double[shape().dim(0)];
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            double sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += ptrGetDouble(it.nextInt()) * tensor.ptrGetDouble(innerIt.nextInt());
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return mill.ofDouble().stride(layout, result);
    }

    @Override
    public DTensor mm(DTensor t, Order askOrder) {
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

        var result = new double[m * p];
        var ret = mill.ofDouble().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<DTensor> rows = chunk(0, false, 1);
        List<DTensor> cols = t.chunk(1, false, 1);

        int chunk = (int) floor(sqrt((double) L2_CACHE_SIZE / 2 / CORES / dtype().bytes()));
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
                                var krow = (DTensorStride) rows.get(i);
                                for (int j = cs; j < ce; j++) {
                                    result[i * iStride + j * jStride] += krow.vdotDouble(cols.get(j), k, end);
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
    public Iterator<Double> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(i -> array[i]).iterator();
    }

    @Override
    public DTensorStride iteratorApply(Order askOrder, IntIntBiFunction<Double> apply) {
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
    public DTensor reshape(Shape askShape, Order askOrder) {
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
        DTensor copy = mill.ofDouble().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetDouble(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public DTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return mill.ofDouble().stride(compact, array);
        }
        return flatten(askOrder);
    }

    @Override
    public DTensor flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = new double[layout.size()];
        int p = 0;
        var it = chunkIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.loopBound(); i += it.loopStep()) {
                out[p++] = array[i];
            }
        }
        return mill.ofDouble().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public DTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : mill.ofDouble().stride(layout.squeeze(), array);
    }

    @Override
    public DTensor squeeze(int axis) {
        return layout.shape().dim(axis) != 1 ? this : mill.ofDouble().stride(layout.squeeze(axis), array);
    }

    @Override
    public DTensor unsqueeze(int axis) {
        return mill.ofDouble().stride(layout().unsqueeze(axis), array);
    }

    @Override
    public DTensor t_() {
        return mill.ofDouble().stride(layout.revert(), array);
    }

    @Override
    public DTensor t(Order askOrder) {
        return t_().copy(askOrder);
    }

    @Override
    public DTensor moveAxis(int src, int dst) {
        return mill.ofDouble().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public DTensor swapAxis(int src, int dst) {
        return mill.ofDouble().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public DTensor narrow(int axis, boolean keepdim, int start, int end) {
        return mill.ofDouble().stride(layout.narrow(axis, keepdim, start, end), array);
    }

    @Override
    public DTensor narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return mill.ofDouble().stride(layout.narrowAll(keepdim, starts, ends), array);
    }

    @Override
    public List<DTensor> split(int axis, boolean keepdim, int... indexes) {
        List<DTensor> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    @Override
    public List<DTensor> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<DTensor> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRec(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRec(List<DTensor> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
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
    public DTensor repeat(int axis, int repeat, boolean stack) {
        DTensor[] copies = new DTensor[repeat];
        Arrays.fill(copies, this);
        if (stack) {
            return mill.stack(axis, Arrays.asList(copies));
        } else {
            return mill.concat(axis, Arrays.asList(copies));
        }
    }

    @Override
    public DTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        double[] copy = new double[size()];
        DTensorStride dst = (DTensorStride) mill.ofDouble().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(double[] copy, Order askOrder) {
        var chd = StrideChunkDescriptor.of(layout, askOrder);
        var last = 0;
        for (int ptr : chd.chunkOffsets()) {
            if (chd.loopStep() == 1) {
                int i = ptr;
                int loopBound = SPEC.loopBound(chd.loopSize()) + ptr;
                for (; i < loopBound; i += SPEC_LEN) {
                    DoubleVector a = DoubleVector.fromArray(SPEC, array, i);
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

    private void copyTo(DTensorStride dst, Order askOrder) {

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
                                DTensorStride s = (DTensorStride) this.narrowAll(false, ss, es);
                                DTensorStride d = (DTensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(DTensorStride src, DTensorStride dst, Order askOrder) {
        var chd = StrideChunkDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int ptr : chd.chunkOffsets()) {
            for (int i = ptr; i < ptr + chd.loopLength(); i += chd.loopStep()) {
                dst.array[it2.nextInt()] = src.array[i];
            }
        }
    }
}