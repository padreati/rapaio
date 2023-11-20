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

package rapaio.math.tensor.engine.basearray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.engine.AbstractTensor;
import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.DensePointerIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.iterators.ScalarChunkIterator;
import rapaio.math.tensor.iterators.StrideChunkDescriptor;
import rapaio.math.tensor.iterators.StrideChunkIterator;
import rapaio.math.tensor.iterators.StridePointerIterator;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.util.Hardware;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public final class DTensorStride extends AbstractTensor<Double, DTensor> implements DTensor {

    private static final VectorSpecies<Double> SPEC = DoubleVector.SPECIES_PREFERRED;
    private static final int SPEC_LEN = SPEC.length();

    private final StrideLayout layout;
    private final BaseArrayTensorEngine engine;
    private final double[] array;

    // precomputed things

    private final StrideChunkDescriptor chunkDesc;
    private final int[] chunkIndexes;

    public DTensorStride(BaseArrayTensorEngine engine, Shape shape, int offset, int[] strides, double[] array) {
        this(engine, StrideLayout.of(shape, offset, strides), array);
    }

    public DTensorStride(BaseArrayTensorEngine engine, Shape shape, int offset, Order order, double[] array) {
        this(engine, StrideLayout.ofDense(shape, offset, order), array);
    }

    public DTensorStride(BaseArrayTensorEngine engine, StrideLayout layout, double[] array) {
        this.layout = layout;
        this.engine = engine;
        this.array = array;

        this.chunkDesc = new StrideChunkDescriptor(layout, Order.S);
        this.chunkIndexes = chunkDesc.loopStep() == 1 ? null : chunkIndexes(chunkDesc.loopStep());
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
    public TensorEngine engine() {
        return engine;
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
    public double getAtDouble(int ptr) {
        return array[ptr];
    }

    @Override
    public void setAtDouble(int ptr, double value) {
        array[ptr] = value;
    }

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : chunkDesc.getChunkOffsets()) {
            int loopBound = SPEC.loopBound(chunkDesc.loopSize()) + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop());
                a.intoArray(array, i);
            }
            for (; i < chunkDesc.loopSize() + off; i++) {
                array[i] = op.applyDouble(array[i]);
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : chunkDesc.getChunkOffsets()) {
            int loopLen = chunkDesc.loopSize() * chunkDesc.loopStep() + off;
            int loopBound = SPEC.loopBound(chunkDesc.loopSize()) * chunkDesc.loopStep() + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN * chunkDesc.loopStep()) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i, chunkIndexes, 0);
                a = a.lanewise(op.vop());
                a.intoArray(array, i, chunkIndexes, 0);
            }
            for (; i < loopLen; i += chunkDesc.loopStep()) {
                array[i] = op.applyDouble(array[i]);
            }
        }
    }

    private void unaryOp(TensorUnaryOp op) {
        if (chunkDesc.loopStep() == 1) {
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

    private void validateSameShape(DTensor tensor) {
        if (!shape().equals(tensor.shape())) {
            throw new IllegalArgumentException("Shapes does not match.");
        }
    }

    void binaryVectorOp(TensorBinaryOp op, DTensor b) {
        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = pointerIterator(order);
        var refIt = b.pointerIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            array[next] = op.applyDouble(array[next], b.getAt(refIt.nextInt()));
        }
    }

    @Override
    public DTensorStride add_(DTensor tensor) {
        validateSameShape(tensor);
        binaryVectorOp(TensorBinaryOp.ADD, tensor);
        return this;
    }

    @Override
    public DTensorStride sub_(DTensor tensor) {
        validateSameShape(tensor);
        binaryVectorOp(TensorBinaryOp.SUB, tensor);
        return this;
    }

    @Override
    public DTensorStride mul_(DTensor tensor) {
        validateSameShape(tensor);
        binaryVectorOp(TensorBinaryOp.MUL, tensor);
        return this;
    }

    @Override
    public DTensorStride div_(DTensor tensor) {
        validateSameShape(tensor);
        binaryVectorOp(TensorBinaryOp.DIV, tensor);
        return this;
    }

    void binaryScalarOpUnit(TensorBinaryOp op, double value) {
        for (int off : chunkDesc.getChunkOffsets()) {
            int loopBound = SPEC.loopBound(chunkDesc.loopSize()) + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i);
            }
            for (; i < chunkDesc.loopSize() + off; i++) {
                array[i] = op.applyDouble(array[i], value);
            }
        }
    }

    void binaryScalarOpStep(TensorBinaryOp op, double value) {
        for (int off : chunkDesc.getChunkOffsets()) {
            int loopLen = chunkDesc.loopSize() * chunkDesc.loopStep() + off;
            int loopBound = SPEC.loopBound(chunkDesc.loopSize()) * chunkDesc.loopStep() + off;
            int i = off;
            for (; i < loopBound; i += SPEC_LEN * chunkDesc.loopStep()) {
                DoubleVector a = DoubleVector.fromArray(SPEC, array, i, chunkIndexes, 0);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i, chunkIndexes, 0);
            }
            for (; i < loopLen; i += chunkDesc.loopStep()) {
                array[i] = op.applyDouble(array[i], value);
            }
        }
    }

    void binaryScalarOp(TensorBinaryOp op, double value) {
        if (chunkDesc.loopStep() == 1) {
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
    public double vdot(DTensor tensor) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new RuntimeException("Operands are not valid for vector dot product "
                    + "(v = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        return _vdot(tensor, 0, shape().dim(0));
    }

    private double _vdot(DTensor tensor, int start, int end) {

        DTensorStride dts = (DTensorStride) tensor;

        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);
        int start1 = layout.offset() + start * step1;
        int start2 = dts.layout.offset() + start * step2;
        double sum = 0;
        for (int i = 0; i < end - start; i++) {
            sum += array[start1] * dts.array[start2];
            start1 += step1;
            start2 += step2;
        }
        return sum;
    }

    @Override
    public DTensor mv(DTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new RuntimeException("Operands are not valid for matrix-vector multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        double[] result = new double[shape().dim(0)];
        var it = pointerIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.pointerIterator(Order.C);
            double sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += getAtDouble(it.nextInt()) * tensor.getAtDouble(innerIt.nextInt());
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return engine.ofDouble().stride(layout, result);
    }

    @Override
    public DTensor mm(DTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 2 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new RuntimeException("Operands are not valid for matrix-matrix multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        double[] result = new double[shape().dim(0) * tensor.shape().dim(1)];

        List<DTensor> rows = slice(0, 1).stream().map(DTensor::squeeze).toList();
        List<DTensor> cols = tensor.slice(1, 1).stream().map(DTensor::squeeze).toList();

        int chunk = (int) Math.floor(Math.sqrt((double) Hardware.L2_CACHE_SIZE / 2 / Hardware.CORES / dtype().bytes()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;

        int rowChunk = chunk > 64 ? (int) Math.ceil(Math.sqrt(chunk / 4.)) : (int) Math.ceil(Math.sqrt(chunk));
        int colChunk = rowChunk;

        try (ExecutorService service = Executors.newFixedThreadPool(Hardware.CORES)) {
            for (int r = 0; r < rows.size(); r += rowChunk) {
                int rs = r;
                int re = Math.min(rows.size(), r + rowChunk);

                for (int c = 0; c < cols.size(); c += colChunk) {
                    int cs = c;
                    int ce = Math.min(cols.size(), c + colChunk);


                    service.submit(() -> {

                        for (int k = 0; k < shape().dim(1); k += vectorChunk) {
                            int end = Math.min(shape().dim(1), k + vectorChunk);
                            for (int i = rs; i < re; i++) {
                                var krow = (DTensorStride) rows.get(i);
                                int off = i * tensor.shape().dim(1);


                                for (int j = cs; j < ce; j++) {
                                    result[off + j] += krow._vdot(cols.get(j), k, end);
                                }

                            }
                        }
                        return null;
                    });
                }
            }
            service.shutdown();
        }

        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0), tensor.shape().dim(1)), 0, Order.C);
        return engine.ofDouble().stride(layout, result);
    }

    @Override
    public DTensor matmul(DTensor tensor) {
        if (layout.rank() == 1 && tensor.layout().rank() == 1) {
            return mv(tensor);
        }
        if (layout.rank() == 1 && tensor.layout().rank() == 2) {
            return unsqueeze(0).mm(tensor);
        }
        if (layout.rank() == 2 && tensor.layout().rank() == 1) {
            return mv(tensor);
        }
        if (layout.rank() == 2 && tensor.layout().rank() == 2) {
            return mm(tensor);
        }
        throw new IllegalArgumentException("Operation not supported.");
    }

    @Override
    public Iterator<Double> iterator() {
        return iterator(Order.A);
    }

    @Override
    public Iterator<Double> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(pointerIterator(askOrder), Spliterator.ORDERED), false)
                .map(i -> array[i]).iterator();
    }

    @Override
    public DTensorStride iteratorApply(Order askOrder, IntIntBiFunction<Double> apply) {
        var it = pointerIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            array[p] = apply.applyAsInt(i++, p);
        }
        return this;
    }

    @Override
    public PointerIterator pointerIterator(Order askOrder) {
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
        DTensor copy = engine.ofDouble().zeros(askShape, askOrder);
        var copyIt = copy.pointerIterator(Order.C);
        while (it.hasNext()) {
            copy.setAtDouble(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public DTensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return engine.ofDouble().stride(compact, array);
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
        return engine.ofDouble().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public DTensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : engine.ofDouble().stride(layout.squeeze(), array);
    }

    @Override
    public DTensor unsqueeze(int axis) {
        return engine.ofDouble().stride(layout().unsqueeze(axis), array);
    }

    @Override
    public DTensor t() {
        return engine.ofDouble().stride(layout.revert(), array);
    }

    @Override
    public DTensor moveAxis(int src, int dst) {
        return engine.ofDouble().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public DTensor swapAxis(int src, int dst) {
        return engine.ofDouble().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public DTensor truncate(int axis, int start, int end) {
        if (axis < 0 || axis >= layout.rank()) {
            throw new IllegalArgumentException("Axis is out of bounds.");
        }
        if (rank() == 1) {
            return engine.ofDouble().stride(StrideLayout.of(
                    Shape.of(end - start),
                    layout.offset() + layout.stride(0) * start,
                    layout.strides()
            ), array);
        }
        int[] newDims = Arrays.copyOf(shape().dims(), shape().rank());
        newDims[axis] = end - start;
        int newOffset = layout().offset() + start * layout.stride(axis);
        int[] newStrides = Arrays.copyOf(layout.strides(), layout.rank());

        StrideLayout copyLayout = StrideLayout.of(Shape.of(newDims), newOffset, newStrides);
        return engine.ofDouble().stride(copyLayout, array);
    }

    @Override
    public List<DTensor> split(int axis, int... indexes) {
        return IntStream
                .range(0, indexes.length)
                .mapToObj(i -> truncate(axis, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)))
                .collect(Collectors.toList());
    }

    @Override
    public DTensor repeat(int axis, int repeat, boolean stack) {
        DTensor[] copies = new DTensor[repeat];
        Arrays.fill(copies, this);
        if (stack) {
            return engine.stack(axis, Arrays.asList(copies));
        } else {
            return engine.concat(axis, Arrays.asList(copies));
        }
    }

    @Override
    public DTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        var copy = engine.ofDouble().zeros(shape(), askOrder);
        var it1 = chunkIterator(askOrder);
        var it2 = copy.pointerIterator(askOrder);
        while (it1.hasNext()) {
            int pointer = it1.nextInt();
            for (int i = pointer; i < pointer + it1.loopBound(); i += it1.loopStep()) {
                copy.setAtDouble(it2.nextInt(), getAtDouble(i));
            }
        }
        return copy;
    }
}
