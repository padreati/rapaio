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

package rapaio.math.tensor.mill.varray;

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

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.ITensor;
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
import rapaio.math.tensor.operator.TensorAssociativeOp;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.util.NotImplementedException;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.IntIntBiFunction;

public final class ITensorStride extends AbstractTensor<Integer, ITensor> implements ITensor {

    private static final VectorSpecies<Integer> SPEC = IntVector.SPECIES_PREFERRED;
    private static final int SPEC_LEN = SPEC.length();

    private final StrideLayout layout;
    private final ArrayTensorMill mill;
    private final int[] array;

    private final StrideLoopDescriptor loop;
    private final int[] loopIndexes;

    public ITensorStride(ArrayTensorMill mill, Shape shape, int offset, int[] strides, int[] array) {
        this(mill, StrideLayout.of(shape, offset, strides), array);
    }

    public ITensorStride(ArrayTensorMill mill, Shape shape, int offset, Order order, int[] array) {
        this(mill, StrideLayout.ofDense(shape, offset, order), array);
    }

    public ITensorStride(ArrayTensorMill mill, StrideLayout layout, int[] array) {
        this.layout = layout;
        this.mill = mill;
        this.array = array;
        this.loop = StrideLoopDescriptor.of(layout, layout.storageFastOrder());
        this.loopIndexes = loop.step == 1 ? null : loopIndexes(loop.step);
    }

    private int[] loopIndexes(int step) {
        int[] indexes = new int[SPEC_LEN];
        for (int i = 1; i < SPEC_LEN; i++) {
            indexes[i] = indexes[i - 1] + step;
        }
        return indexes;
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
    public ITensor reshape(Shape askShape, Order askOrder) {
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
        ITensor copy = mill.ofInt().zeros(askShape, askOrder);
        var copyIt = copy.ptrIterator(Order.C);
        while (it.hasNext()) {
            copy.ptrSetInteger(copyIt.nextInt(), array[it.nextInt()]);
        }
        return copy;
    }

    @Override
    public ITensor transpose() {
        return mill.ofInt().stride(layout.revert(), array);
    }

    @Override
    public ITensor ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return mill.ofInt().stride(compact, array);
        }
        return flatten(askOrder);
    }

    @Override
    public ITensor flatten(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        var out = new int[layout.size()];
        int p = 0;
        var it = loopIterator(askOrder);
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = pointer; i < pointer + it.bound(); i += it.step()) {
                out[p++] = array[i];
            }
        }
        return mill.ofInt().stride(Shape.of(layout.size()), 0, new int[] {1}, out);
    }

    @Override
    public ITensor squeeze() {
        return layout.shape().unitDimCount() == 0 ? this : mill.ofInt().stride(layout.squeeze(), array);
    }

    @Override
    public ITensor squeeze(int axis) {
        return layout.shape().dim(axis) != 1 ? this : mill.ofInt().stride(layout.squeeze(axis), array);
    }

    @Override
    public ITensor unsqueeze(int axis) {
        return mill.ofInt().stride(layout().unsqueeze(axis), array);
    }

    @Override
    public ITensor moveAxis(int src, int dst) {
        return mill.ofInt().stride(layout.moveAxis(src, dst), array);
    }

    @Override
    public ITensor swapAxis(int src, int dst) {
        return mill.ofInt().stride(layout.swapAxis(src, dst), array);
    }

    @Override
    public ITensor narrow(int axis, boolean keepdim, int start, int end) {
        return mill.ofInt().stride(layout.narrow(axis, keepdim, start, end), array);
    }

    @Override
    public ITensor narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return mill.ofInt().stride(layout.narrowAll(keepdim, starts, ends), array);
    }

    @Override
    public List<ITensor> split(int axis, boolean keepdim, int... indexes) {
        List<ITensor> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    @Override
    public List<ITensor> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<ITensor> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRec(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRec(List<ITensor> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
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
    public ITensor repeat(int axis, int repeat, boolean stack) {
        ITensor[] copies = new ITensor[repeat];
        Arrays.fill(copies, this);
        if (stack) {
            return mill.stack(axis, Arrays.asList(copies));
        } else {
            return mill.concat(axis, Arrays.asList(copies));
        }
    }

    @Override
    public ITensor tile(int[] repeats) {
        throw new NotImplementedException();
    }

    @Override
    public ITensor permute(int[] dims) {
        return mill.ofInt().stride(layout().permute(dims), array);
    }

    @Override
    public int getInt(int... indexes) {
        return array[layout.pointer(indexes)];
    }

    @Override
    public void setInt(int value, int... indexes) {
        array[layout.pointer(indexes)] = value;
    }

    @Override
    public int ptrGetInteger(int ptr) {
        return array[ptr];
    }

    @Override
    public void ptrSetInteger(int ptr, int value) {
        array[ptr] = value;
    }

    @Override
    public Iterator<Integer> iterator(Order askOrder) {
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
    public ITensorStride apply(Order askOrder, IntIntBiFunction<Integer> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            array[p] = apply.applyAsInt(i++, p);
        }
        return this;
    }

    @Override
    public ITensor apply(Function<Integer, Integer> fun) {
        var ptrIter = ptrIterator(Order.S);
        while (ptrIter.hasNext()) {
            int ptr = ptrIter.nextInt();
            array[ptr] = fun.apply(array[ptr]);
        }
        return this;
    }

    @Override
    public ITensor fill(Integer value) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                IntVector fill = IntVector.broadcast(SPEC, value);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    if (loop.step == 1) {
                        fill.intoArray(array, i);
                    } else {
                        fill.intoArray(array, i, loopIndexes, 0);
                    }
                }
            }
            for (; i < loop.bound + offset; i += loop.step) {
                array[i] = value;
            }
        }
        return this;
    }

    @Override
    public ITensor fillNan(Integer value) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                IntVector fill = IntVector.broadcast(SPEC, value);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    if (loop.step == 1) {
                        IntVector a = IntVector.fromArray(SPEC, array, i);
                        VectorMask<Integer> m = a.test(VectorOperators.IS_NAN);
                        fill.intoArray(array, i, m);
                    } else {
                        IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                        VectorMask<Integer> m = a.test(VectorOperators.IS_NAN);
                        fill.intoArray(array, i, loopIndexes, 0, m);
                    }
                }
            }
            for (; i < loop.bound + offset; i += loop.step) {
                if (dtype().isNaN(array[i])) {
                    array[i] = value;
                }
            }
        }
        return this;
    }

    @Override
    public ITensor clamp(Integer min, Integer max) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    IntVector a = loop.step == 1 ?
                            IntVector.fromArray(SPEC, array, i) :
                            IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    boolean any = false;
                    if (!dtype().isNaN(min)) {
                        VectorMask<Integer> m = a.compare(VectorOperators.LT, min);
                        if (m.anyTrue()) {
                            a = a.blend(min, m);
                            any = true;
                        }
                    }
                    if (!dtype().isNaN(max)) {
                        VectorMask<Integer> m = a.compare(VectorOperators.GT, max);
                        if (m.anyTrue()) {
                            a = a.blend(max, m);
                            any = true;
                        }
                    }
                    if (any) {
                        if (loop.step == 1) {
                            a.intoArray(array, i);
                        } else {
                            a.intoArray(array, i, loopIndexes, 0);
                        }
                    }
                }
            }
            for (; i < loop.bound + offset; i += loop.step) {
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
    public ITensor take(Order order, int... indexes) {
        throw new NotImplementedException();
    }

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + off;
            int i = off;
            for (; i < bound; i += SPEC_LEN) {
                IntVector a = IntVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop());
                a.intoArray(array, i);
            }
            for (; i < loop.bound + off; i++) {
                array[i] = op.applyInt(array[i]);
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + off;
            int i = off;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                a = a.lanewise(op.vop());
                a.intoArray(array, i, loopIndexes, 0);
            }
            for (; i < loop.bound + off; i += loop.step) {
                array[i] = op.applyInt(array[i]);
            }
        }
    }

    private void unaryOp(TensorUnaryOp op) {
        if (op.isFloatOnly() && !dtype().isFloat()) {
            throw new IllegalArgumentException("This operation is available only for floating point tensors.");
        }
        if (loop.step == 1) {
            unaryOpUnit(op);
        } else {
            unaryOpStep(op);
        }
    }

    @Override
    public ITensorStride abs() {
        unaryOp(TensorUnaryOp.ABS);
        return this;
    }

    @Override
    public ITensorStride negate() {
        unaryOp(TensorUnaryOp.NEG);
        return this;
    }

    @Override
    public ITensorStride log() {
        unaryOp(TensorUnaryOp.LOG);
        return this;
    }

    @Override
    public ITensorStride log1p() {
        unaryOp(TensorUnaryOp.LOG1P);
        return this;
    }

    @Override
    public ITensorStride exp() {
        unaryOp(TensorUnaryOp.EXP);
        return this;
    }

    @Override
    public ITensorStride expm1() {
        unaryOp(TensorUnaryOp.EXPM1);
        return this;
    }

    @Override
    public ITensorStride sin() {
        unaryOp(TensorUnaryOp.SIN);
        return this;
    }

    @Override
    public ITensorStride asin() {
        unaryOp(TensorUnaryOp.ASIN);
        return this;
    }

    @Override
    public ITensorStride sinh() {
        unaryOp(TensorUnaryOp.SINH);
        return this;
    }

    @Override
    public ITensorStride cos() {
        unaryOp(TensorUnaryOp.COS);
        return this;
    }

    @Override
    public ITensorStride acos() {
        unaryOp(TensorUnaryOp.ACOS);
        return this;
    }

    @Override
    public ITensorStride cosh() {
        unaryOp(TensorUnaryOp.COSH);
        return this;
    }

    @Override
    public ITensorStride tan() {
        unaryOp(TensorUnaryOp.TAN);
        return this;
    }

    @Override
    public ITensorStride atan() {
        unaryOp(TensorUnaryOp.ATAN);
        return this;
    }

    @Override
    public ITensorStride tanh() {
        unaryOp(TensorUnaryOp.TANH);
        return this;
    }

    void binaryVectorOp(TensorBinaryOp op, ITensor b) {
        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = ptrIterator(order);
        var refIt = b.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            array[next] = op.applyInt(array[next], b.ptrGet(refIt.nextInt()));
        }
    }

    @Override
    public ITensorStride add(ITensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.ADD, tensor);
        return this;
    }

    @Override
    public ITensorStride sub(ITensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.SUB, tensor);
        return this;
    }

    @Override
    public ITensorStride mul(ITensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.MUL, tensor);
        return this;
    }

    @Override
    public ITensorStride div(ITensor tensor) {
        TensorValidation.sameShape(this, tensor);
        binaryVectorOp(TensorBinaryOp.DIV, tensor);
        return this;
    }

    void binaryScalarOpUnit(TensorBinaryOp op, int value) {
        for (int off : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + off;
            int i = off;
            for (; i < bound; i += SPEC_LEN) {
                IntVector a = IntVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i);
            }
            for (; i < loop.bound + off; i++) {
                array[i] = op.applyInt(array[i], value);
            }
        }
    }

    void binaryScalarOpStep(TensorBinaryOp op, int value) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i, loopIndexes, 0);
            }
            for (; i < loop.bound + offset; i += loop.step) {
                array[i] = op.applyInt(array[i], value);
            }
        }
    }

    void binaryScalarOp(TensorBinaryOp op, int value) {
        if (loop.step == 1) {
            binaryScalarOpUnit(op, value);
        } else {
            binaryScalarOpStep(op, value);
        }
    }

    @Override
    public ITensorStride add(Integer value) {
        binaryScalarOp(TensorBinaryOp.ADD, value);
        return this;
    }

    @Override
    public ITensorStride sub(Integer value) {
        binaryScalarOp(TensorBinaryOp.SUB, value);
        return this;
    }

    @Override
    public ITensorStride mul(Integer value) {
        binaryScalarOp(TensorBinaryOp.MUL, value);
        return this;
    }

    @Override
    public ITensorStride div(Integer value) {
        binaryScalarOp(TensorBinaryOp.DIV, value);
        return this;
    }

    @Override
    public Integer vdot(ITensor tensor) {
        return vdot(tensor, 0, shape().dim(0));
    }

    @Override
    public Integer vdot(ITensor tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        ITensorStride dts = (ITensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);
        int start1 = layout.offset() + start * step1;
        int start2 = dts.layout.offset() + start * step2;
        int i = 0;
        int bound = SPEC.loopBound(end - start);
        IntVector vsum = IntVector.zero(SPEC);
        for (; i < bound; i += SPEC_LEN) {
            IntVector a = loop.step == 1 ?
                    IntVector.fromArray(SPEC, array, start1) :
                    IntVector.fromArray(SPEC, array, start1, loopIndexes, 0);
            IntVector b = dts.loop.step == 1 ?
                    IntVector.fromArray(SPEC, dts.array, start2) :
                    IntVector.fromArray(SPEC, dts.array, start2, dts.loopIndexes, 0);
            vsum = vsum.add(a.mul(b));
            start1 += SPEC_LEN * step1;
            start2 += SPEC_LEN * step2;
        }
        int sum = vsum.reduceLanes(VectorOperators.ADD);
        for (; i < end - start; i++) {
            sum += array[start1] * dts.array[start2];
            start1 += step1;
            start2 += step2;
        }
        return sum;
    }

    @Override
    public ITensor mv(ITensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Operands are not valid for matrix-vector multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        int[] result = new int[shape().dim(0)];
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            int sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += ptrGetInteger(it.nextInt()) * tensor.ptrGetInteger(innerIt.nextInt());
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return mill.ofInt().stride(layout, result);
    }

    @Override
    public ITensor mm(ITensor t, Order askOrder) {
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

        var result = new int[m * p];
        var ret = mill.ofInt().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<ITensor> rows = chunk(0, false, 1);
        List<ITensor> cols = t.chunk(1, false, 1);

        int chunk = (int) floor(sqrt((int) L2_CACHE_SIZE / 2 / CORES / dtype().bytes()));
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
                                var krow = (ITensorStride) rows.get(i);
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
    public Statistics<Integer, ITensor> stats() {
        if(!dtype().isFloat()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        if (loop.step == 1) {
            return computeUnitStats();
        }
        return computeStrideStats();
    }

    private Statistics<Integer, ITensor> computeUnitStats() {
        int size = size();
        int nanSize = 0;
        int mean = 0;
        int nanMean = 0;
        int variance = 0;
        int nanVariance = 0;

        // first pass compute raw mean
        int sum = 0;
        int nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            int i = offset;
            IntVector vsum = IntVector.zero(SPEC);
            IntVector vnanSum = IntVector.zero(SPEC);
            for (; i < bound; i += SPEC_LEN) {
                IntVector a = IntVector.fromArray(SPEC, array, i);
                VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                nanSize += mask.trueCount();
                vsum = vsum.add(a);
                vnanSum = vnanSum.add(a, mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Integer> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);
            for (; i < loop.bound + offset; i++) {
                sum += array[i];
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i];
                    nanSize++;
                }
            }
        }
        mean = sum / size;
        nanMean = nanSum / nanSize;

        // second pass adjustments for mean
        sum = 0;
        nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            IntVector vsum = IntVector.zero(SPEC);
            IntVector vnanSum = IntVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN) {
                IntVector a = IntVector.fromArray(SPEC, array, i);
                VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                vsum = vsum.add(a.sub(mean));
                vnanSum = vnanSum.add(a.sub(mean), mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Integer> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);

            for (; i < loop.bound + offset; i++) {
                sum += array[i] - mean;
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i] - nanMean;
                }
            }
        }
        mean += sum / size;
        nanMean += nanSum / nanSize;

        // third pass compute variance
        int sum2 = 0;
        int sum3 = 0;
        int nanSum2 = 0;
        int nanSum3 = 0;

        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            IntVector vsum2 = IntVector.zero(SPEC);
            IntVector vsum3 = IntVector.zero(SPEC);
            IntVector vnanSum2 = IntVector.zero(SPEC);
            IntVector vnanSum3 = IntVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN) {
                IntVector a = IntVector.fromArray(SPEC, array, i);
                VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                IntVector b = a.sub(mean);
                vsum2 = vsum2.add(b.mul(b));
                vsum3 = vsum3.add(b);
                b = a.sub(nanMean, mask);
                vnanSum2 = vnanSum2.add(b.mul(b), mask);
                vnanSum3 = vnanSum3.add(b, mask);
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            nanSum2 += vnanSum2.reduceLanes(VectorOperators.ADD, vnanSum2.test(VectorOperators.IS_NAN).not());
            nanSum3 += vnanSum3.reduceLanes(VectorOperators.ADD, vnanSum3.test(VectorOperators.IS_NAN).not());
            for (; i < loop.bound + offset; i += loop.step) {
                sum2 += (array[i] - mean) * (array[i] - mean);
                sum3 += (array[i] - mean);

                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (array[i] - nanMean) * (array[i] - nanMean);
                    nanSum3 += (array[i] - nanMean);
                }
            }
        }
        variance = (sum2 - (int) (sum3 * sum3) / size) / size;
        nanVariance = (nanSum2 - (int) (nanSum3 * nanSum3) / nanSize) / nanSize;

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
    }

    private Statistics<Integer, ITensor> computeStrideStats() {
        int size = size();
        int nanSize = 0;
        int mean = 0;
        int nanMean = 0;
        int variance = 0;
        int nanVariance = 0;

        // first pass compute raw mean
        int sum = 0;
        int nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            IntVector vsum = IntVector.zero(SPEC);
            IntVector vnanSum = IntVector.zero(SPEC);
            for (; i < bound; i += SPEC_LEN * loop.step) {
                IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                nanSize += mask.trueCount();
                vsum = vsum.add(a);
                vnanSum = vnanSum.add(a, mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Integer> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);
            for (; i < loop.bound + offset; i += loop.step) {
                sum += array[i];
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i];
                    nanSize++;
                }
            }
        }
        mean = sum / size;
        nanMean = nanSum / nanSize;

        // second pass adjustments for mean
        sum = 0;
        nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            IntVector vsum = IntVector.zero(SPEC);
            IntVector vnanSum = IntVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                vsum = vsum.add(a.sub(mean));
                vnanSum = vnanSum.add(a.sub(mean), mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Integer> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);

            for (; i < loop.bound + offset; i += loop.step) {
                sum += array[i] - mean;
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i] - nanMean;
                }
            }
        }
        mean += sum / size;
        nanMean += nanSum / nanSize;

        // third pass compute variance
        int sum2 = 0;
        int sum3 = 0;
        int nanSum2 = 0;
        int nanSum3 = 0;

        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            IntVector vsum2 = IntVector.zero(SPEC);
            IntVector vsum3 = IntVector.zero(SPEC);
            IntVector vnanSum2 = IntVector.zero(SPEC);
            IntVector vnanSum3 = IntVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                IntVector b = a.sub(mean);
                vsum2 = vsum2.add(b.mul(b));
                vsum3 = vsum3.add(b);
                VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                b = a.sub(nanMean);
                vnanSum2 = vnanSum2.add(b.mul(b), mask);
                vnanSum3 = vnanSum3.add(b, mask);
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            nanSum2 += vnanSum2.reduceLanes(VectorOperators.ADD, vnanSum2.test(VectorOperators.IS_NAN).not());
            nanSum3 += vnanSum3.reduceLanes(VectorOperators.ADD, vnanSum3.test(VectorOperators.IS_NAN).not());
            for (; i < loop.bound + offset; i += loop.step) {
                sum2 += (array[i] - mean) * (array[i] - mean);
                sum3 += (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (array[i] - nanMean) * (array[i] - nanMean);
                    nanSum3 += (array[i] - nanMean);
                }
            }
        }
        variance = (sum2 - (int) (sum3 * sum3) / size) / size;
        nanVariance = (nanSum2 - (int) (nanSum3 * nanSum3) / nanSize) / nanSize;

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
    }

    @Override
    public Integer sum() {
        return associativeOp(TensorAssociativeOp.ADD);
    }

    @Override
    public Integer nanSum() {
        return nanAssociativeOp(TensorAssociativeOp.ADD);
    }

    @Override
    public Integer prod() {
        return associativeOp(TensorAssociativeOp.MUL);
    }

    @Override
    public Integer nanProd() {
        return nanAssociativeOp(TensorAssociativeOp.MUL);
    }

    @Override
    public Integer max() {
        return associativeOp(TensorAssociativeOp.MAX);
    }

    @Override
    public Integer nanMax() {
        return nanAssociativeOp(TensorAssociativeOp.MAX);
    }

    @Override
    public Integer min() {
        return associativeOp(TensorAssociativeOp.MIN);
    }

    @Override
    public Integer nanMin() {
        return nanAssociativeOp(TensorAssociativeOp.MIN);
    }

    @Override
    public int nanCount() {
        int count = 0;
        if (loop.step == 1) {
            for (int offset : loop.offsets) {
                int bound = SPEC.loopBound(loop.size) + offset;
                int i = offset;
                if (bound > offset && dtype().isFloat()) {
                    for (; i < bound; i += SPEC_LEN) {
                        IntVector a = IntVector.fromArray(SPEC, array, i);
                        count += a.test(VectorOperators.IS_NAN).trueCount();
                    }
                }
                for (; i < loop.bound + offset; i++) {
                    if (dtype().isNaN(array[i])) {
                        count++;
                    }
                }
            }
        } else {
            for (int offset : loop.offsets) {
                int bound = SPEC.loopBound(loop.size) * loop.step + offset;
                int i = offset;
                if (bound > offset && dtype().isFloat()) {
                    for (; i < bound; i += SPEC_LEN * loop.step) {
                        IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                        count += a.test(VectorOperators.IS_NAN).trueCount();
                    }
                }
                for (; i < loop.bound + offset; i += loop.step) {
                    if (dtype().isNaN(array[i])) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    @Override
    public int zeroCount() {
        int count = 0;
        if (loop.step == 1) {
            for (int offset : loop.offsets) {
                int bound = SPEC.loopBound(loop.size) + offset;
                int i = offset;
                IntVector zeros = IntVector.zero(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    IntVector a = IntVector.fromArray(SPEC, array, i);
                    count += a.compare(VectorOperators.EQ, zeros).trueCount();
                }
                for (; i < loop.bound + offset; i++) {
                    if (array[i] == 0) {
                        count++;
                    }
                }
            }
        } else {
            for (int offset : loop.offsets) {
                int bound = SPEC.loopBound(loop.size) * loop.step + offset;
                int i = offset;
                IntVector zeros = IntVector.zero(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    count += a.compare(VectorOperators.EQ, zeros).trueCount();
                }
                for (; i < loop.bound + offset; i += loop.step) {
                    if (array[i] == 0) {
                        count++;
                    }
                }
            }
        }
        return count;
    }


    private int associativeOp(TensorAssociativeOp op) {
        if (loop.step == 1) {
            return unitAssociativeOp(op);
        }
        return strideAssociativeOp(op);
    }

    private int unitAssociativeOp(TensorAssociativeOp op) {
        int aggregate = op.initialInt();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;

            int i = offset;
            if (bound > offset) {
                IntVector vectorAggregate = op.initialVectorInt(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    IntVector a = IntVector.fromArray(SPEC, array, i);
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a);
                }
                aggregate = op.applyInt(aggregate, vectorAggregate.reduceLanes(op.vop()));
            }
            for (; i < loop.bound + offset; i++) {
                aggregate = op.applyInt(aggregate, array[i]);
            }
        }
        return aggregate;
    }

    private int strideAssociativeOp(TensorAssociativeOp op) {
        int aggregate = op.initialInt();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;

            int i = offset;
            if (bound > offset) {
                IntVector vsum = op.initialVectorInt(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    vsum = vsum.lanewise(op.vop(), a);
                }
                aggregate = op.applyInt(aggregate, vsum.reduceLanes(op.vop()));
            }
            for (; i < loop.bound + offset; i += loop.step) {
                aggregate = op.applyInt(aggregate, array[i]);
            }
        }
        return aggregate;
    }

    private int nanAssociativeOp(TensorAssociativeOp op) {
        if (loop.step == 1) {
            return nanUnitAssociativeOp(op);
        }
        return nanStrideAssociativeOp(op);
    }

    private int nanUnitAssociativeOp(TensorAssociativeOp op) {
        int aggregate = op.initialInt();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;

            int i = offset;
            if (bound > offset && dtype().isFloat()) {
                IntVector vectorAggregate = op.initialVectorInt(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    IntVector a = IntVector.fromArray(SPEC, array, i);
                    VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a, mask);
                }
                VectorMask<Integer> mask = vectorAggregate.test(VectorOperators.IS_NAN).not();
                aggregate = op.applyInt(aggregate, vectorAggregate.reduceLanes(op.vop(), mask));
            }
            for (; i < loop.bound + offset; i++) {
                if (!dtype().isNaN(array[i])) {
                    aggregate = op.applyInt(aggregate, array[i]);
                }
            }
        }
        return aggregate;
    }

    private int nanStrideAssociativeOp(TensorAssociativeOp op) {
        int aggregate = op.initialInt();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;

            int i = offset;
            if (bound > offset && dtype().isFloat()) {
                IntVector vectorAggregate = op.initialVectorInt(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    VectorMask<Integer> mask = a.test(VectorOperators.IS_NAN).not();
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a, mask);
                }
                VectorMask<Integer> mask = vectorAggregate.test(VectorOperators.IS_NAN).not();
                aggregate = op.applyInt(aggregate, vectorAggregate.reduceLanes(op.vop(), mask));
            }
            for (; i < loop.bound + offset; i += loop.step) {
                if (!dtype().isNaN(array[i])) {
                    aggregate = op.applyInt(aggregate, array[i]);
                }
            }
        }
        return aggregate;
    }

    @Override
    public ITensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        int[] copy = new int[size()];
        ITensorStride dst = (ITensorStride) mill.ofInt().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(int[] copy, Order askOrder) {
        var chd = StrideLoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int ptr : chd.offsets) {
            if (chd.step == 1) {
                int i = ptr;
                int bound = SPEC.loopBound(chd.size) + ptr;
                for (; i < bound; i += SPEC_LEN) {
                    IntVector a = IntVector.fromArray(SPEC, array, i);
                    a.intoArray(copy, last);
                    last += SPEC_LEN;
                }
                for (; i < ptr + chd.size; i++) {
                    copy[last++] = array[i];
                }
            } else {
                for (int i = ptr; i < ptr + chd.bound; i += chd.step) {
                    copy[last++] = array[i];
                }
            }
        }
    }

    @Override
    public ITensor copyTo(ITensor to, Order askOrder) {

        if (to instanceof ITensorStride dst) {

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
                                    ITensorStride s = (ITensorStride) this.narrowAll(false, ss, es);
                                    ITensorStride d = (ITensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(ITensorStride src, ITensorStride dst, Order askOrder) {
        var chd = StrideLoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int ptr : chd.offsets) {
            for (int i = ptr; i < ptr + chd.bound; i += chd.step) {
                dst.array[it2.nextInt()] = src.array[i];
            }
        }
    }

    @Override
    public int[] toArray() {
        if (shape().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        int[] copy = new int[size()];
        int pos = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    if (loop.step == 1) {
                        IntVector a = IntVector.fromArray(SPEC, array, i);
                        a.intoArray(copy, pos);
                        pos += SPEC_LEN;
                    } else {
                        IntVector a = IntVector.fromArray(SPEC, array, i, loopIndexes, 0);
                        a.intoArray(copy, pos);
                        pos += SPEC_LEN;
                    }
                }
            }
            for (; i < loop.bound + offset; i++) {
                copy[pos++] = array[i];
            }
        }
        return copy;
    }
}
