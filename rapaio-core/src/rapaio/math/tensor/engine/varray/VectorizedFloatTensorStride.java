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

package rapaio.math.tensor.engine.varray;

import java.util.ArrayList;
import java.util.List;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.engine.barray.BaseFloatTensorStride;
import rapaio.math.tensor.layout.StrideLayout;

public final class VectorizedFloatTensorStride extends BaseFloatTensorStride implements Tensor<Float> {

    private static final VectorSpecies<Float> SPEC = FloatVector.SPECIES_PREFERRED;
    private static final int SPEC_LEN = SPEC.length();

    private final int[] loopIndexes;

    public VectorizedFloatTensorStride(TensorEngine engine, Shape shape, int offset, int[] strides, Storage<Float> storage) {
        this(engine, StrideLayout.of(shape, offset, strides), storage);
    }

    public VectorizedFloatTensorStride(TensorEngine engine, Shape shape, int offset, Order order, Storage<Float> storage) {
        this(engine, StrideLayout.ofDense(shape, offset, order), storage);
    }

    public VectorizedFloatTensorStride(TensorEngine engine, StrideLayout layout, Storage<Float> storage) {
        super(engine, layout, storage);
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
    public List<Tensor<Float>> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<Tensor<Float>> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRec(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRec(List<Tensor<Float>> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
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

    /*

    @Override
    public VectorizedFloatTensorStride apply_(Order askOrder, IntIntBiFunction<Float> apply) {
        var it = ptrIterator(askOrder);
        int i = 0;
        while (it.hasNext()) {
            int p = it.nextInt();
            storage.set(p, apply.applyAsInt(i++, p));
        }
        return this;
    }

    @Override
    public FloatTensor fill_(Float value) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                FloatVector fill = FloatVector.broadcast(SPEC, value);
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
    public FloatTensor fillNan_(Float value) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                FloatVector fill = FloatVector.broadcast(SPEC, value);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    if (loop.step == 1) {
                        FloatVector a = FloatVector.fromArray(SPEC, array, i);
                        VectorMask<Float> m = a.test(VectorOperators.IS_NAN);
                        fill.intoArray(array, i, m);
                    } else {
                        FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                        VectorMask<Float> m = a.test(VectorOperators.IS_NAN);
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
    public FloatTensor clamp_(Float min, Float max) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    FloatVector a = loop.step == 1 ?
                            FloatVector.fromArray(SPEC, array, i) :
                            FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    boolean any = false;
                    if (!dtype().isNaN(min)) {
                        VectorMask<Float> m = a.compare(VectorOperators.LT, min);
                        if (m.anyTrue()) {
                            a = a.blend(min, m);
                            any = true;
                        }
                    }
                    if (!dtype().isNaN(max)) {
                        VectorMask<Float> m = a.compare(VectorOperators.GT, max);
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

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + off;
            int i = off;
            for (; i < bound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop());
                a.intoArray(array, i);
            }
            for (; i < loop.bound + off; i++) {
                array[i] = op.applyFloat(array[i]);
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + off;
            int i = off;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                a = a.lanewise(op.vop());
                a.intoArray(array, i, loopIndexes, 0);
            }
            for (; i < loop.bound + off; i += loop.step) {
                array[i] = op.applyFloat(array[i]);
            }
        }
    }

    @Override
    protected void unaryOp(TensorUnaryOp op) {
        if (op.isFloatOnly() && !dtype().isFloat()) {
            throw new IllegalArgumentException("This operation is available only for floating point tensors.");
        }
        if (loop.step == 1) {
            unaryOpUnit(op);
        } else {
            unaryOpStep(op);
        }
    }

    protected void binaryVectorOp(TensorBinaryOp op, FloatTensor b) {
        if(b.isScalar()) {
            binaryScalarOp(op, b.getFloat());
            return;
        }
        var order = layout.storageFastOrder();
        order = order == Order.C || order == Order.F ? order : Order.defaultOrder();

        var it = ptrIterator(order);
        var refIt = b.ptrIterator(order);
        while (it.hasNext()) {
            int next = it.nextInt();
            array[next] = op.applyFloat(array[next], b.ptrGet(refIt.nextInt()));
        }
    }

    private void binaryScalarOpUnit(TensorBinaryOp op, float value) {
        for (int off : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + off;
            int i = off;
            for (; i < bound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i);
            }
            for (; i < loop.bound + off; i++) {
                array[i] = op.applyFloat(array[i], value);
            }
        }
    }

    private void binaryScalarOpStep(TensorBinaryOp op, float value) {
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                a = a.lanewise(op.vop(), value);
                a.intoArray(array, i, loopIndexes, 0);
            }
            for (; i < loop.bound + offset; i += loop.step) {
                array[i] = op.applyFloat(array[i], value);
            }
        }
    }

    protected void binaryScalarOp(TensorBinaryOp op, float value) {
        if (loop.step == 1) {
            binaryScalarOpUnit(op, value);
        } else {
            binaryScalarOpStep(op, value);
        }
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
        VectorizedFloatTensorStride dts = (VectorizedFloatTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);
        int start1 = layout.offset() + start * step1;
        int start2 = dts.layout.offset() + start * step2;
        int i = 0;
        int bound = SPEC.loopBound(end - start);
        FloatVector vsum = FloatVector.zero(SPEC);
        for (; i < bound; i += SPEC_LEN) {
            FloatVector a = loop.step == 1 ?
                    FloatVector.fromArray(SPEC, array, start1) :
                    FloatVector.fromArray(SPEC, array, start1, loopIndexes, 0);
            FloatVector b = dts.loop.step == 1 ?
                    FloatVector.fromArray(SPEC, dts.array, start2) :
                    FloatVector.fromArray(SPEC, dts.array, start2, dts.loopIndexes, 0);
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
    public FloatTensor mv(FloatTensor tensor) {
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
                sum += (float)(ptrGetFloat(it.nextInt()) * tensor.ptrGetFloat(innerIt.nextInt()));
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return engine.ofFloat().stride(layout, result);
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
        var ret = engine.ofFloat().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<FloatTensor> rows = chunk(0, false, 1);
        List<FloatTensor> cols = t.chunk(1, false, 1);

        int chunk = (int) floor(sqrt(L2_CACHE_SIZE / 2. / CORES / dtype().bytes()));
        chunk = chunk >= 8 ? chunk - chunk % 8 : chunk;

        int vectorChunk = chunk > 64 ? chunk * 4 : chunk;
        int innerChunk = chunk > 64 ? (int) ceil(sqrt(chunk / 4.)) : (int) ceil(sqrt(chunk));

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
                                var krow = (VectorizedFloatTensorStride) rows.get(i);
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
        if (loop.step == 1) {
            return computeUnitStats();
        }
        return computeStrideStats();
    }

    private Statistics<Float, FloatTensor> computeUnitStats() {
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
            int bound = SPEC.loopBound(loop.size) + offset;
            int i = offset;
            FloatVector vsum = FloatVector.zero(SPEC);
            FloatVector vnanSum = FloatVector.zero(SPEC);
            for (; i < bound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                nanSize += mask.trueCount();
                vsum = vsum.add(a);
                vnanSum = vnanSum.add(a, mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Float> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);
            for (; i < loop.bound + offset; i++) {
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
            int bound = SPEC.loopBound(loop.size) + offset;
            FloatVector vsum = FloatVector.zero(SPEC);
            FloatVector vnanSum = FloatVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                vsum = vsum.add(a.sub(mean));
                vnanSum = vnanSum.add(a.sub(mean), mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Float> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);

            for (; i < loop.bound + offset; i++) {
                sum += (float) (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum += (float) (array[i] - nanMean);
                }
            }
        }
        mean += (float) (sum / size);
        nanMean += (float) (nanSum / nanSize);

        // third pass compute variance
        float sum2 = 0;
        float sum3 = 0;
        float nanSum2 = 0;
        float nanSum3 = 0;

        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            FloatVector vsum2 = FloatVector.zero(SPEC);
            FloatVector vsum3 = FloatVector.zero(SPEC);
            FloatVector vnanSum2 = FloatVector.zero(SPEC);
            FloatVector vnanSum3 = FloatVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i);
                VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                FloatVector b = a.sub(mean);
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
                sum2 += (float) ((array[i] - mean) * (array[i] - mean));
                sum3 += (float) (array[i] - mean);

                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (float) ((array[i] - nanMean) * (array[i] - nanMean));
                    nanSum3 += (float) (array[i] - nanMean);
                }
            }
        }
        variance = (float) ((sum2 - sum3 * sum3 / size) / size);
        nanVariance = (float) ((nanSum2 - nanSum3 * nanSum3 / nanSize) / nanSize);

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
    }

    private Statistics<Float, FloatTensor> computeStrideStats() {
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
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            FloatVector vsum = FloatVector.zero(SPEC);
            FloatVector vnanSum = FloatVector.zero(SPEC);
            for (; i < bound; i += SPEC_LEN * loop.step) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                nanSize += mask.trueCount();
                vsum = vsum.add(a);
                vnanSum = vnanSum.add(a, mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Float> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);
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
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            FloatVector vsum = FloatVector.zero(SPEC);
            FloatVector vnanSum = FloatVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                vsum = vsum.add(a.sub(mean));
                vnanSum = vnanSum.add(a.sub(mean), mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Float> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);

            for (; i < loop.bound + offset; i += loop.step) {
                sum += (float) (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum += (float) (array[i] - nanMean);
                }
            }
        }
        mean += (float) (sum / size);
        nanMean += (float) (nanSum / nanSize);

        // third pass compute variance
        float sum2 = 0;
        float sum3 = 0;
        float nanSum2 = 0;
        float nanSum3 = 0;

        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            FloatVector vsum2 = FloatVector.zero(SPEC);
            FloatVector vsum3 = FloatVector.zero(SPEC);
            FloatVector vnanSum2 = FloatVector.zero(SPEC);
            FloatVector vnanSum3 = FloatVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                FloatVector b = a.sub(mean);
                vsum2 = vsum2.add(b.mul(b));
                vsum3 = vsum3.add(b);
                VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                b = a.sub(nanMean);
                vnanSum2 = vnanSum2.add(b.mul(b), mask);
                vnanSum3 = vnanSum3.add(b, mask);
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            nanSum2 += vnanSum2.reduceLanes(VectorOperators.ADD, vnanSum2.test(VectorOperators.IS_NAN).not());
            nanSum3 += vnanSum3.reduceLanes(VectorOperators.ADD, vnanSum3.test(VectorOperators.IS_NAN).not());
            for (; i < loop.bound + offset; i += loop.step) {
                sum2 += (float) ((array[i] - mean) * (array[i] - mean));
                sum3 += (float) (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (float) ((array[i] - nanMean) * (array[i] - nanMean));
                    nanSum3 += (float) (array[i] - nanMean);
                }
            }
        }
        variance = (float) ((sum2 - (sum3 * sum3) / size) / size);
        nanVariance = (float) ((nanSum2 - (nanSum3 * nanSum3) / nanSize) / nanSize);

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
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
                        FloatVector a = FloatVector.fromArray(SPEC, array, i);
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
                        FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
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
                FloatVector zeros = FloatVector.zero(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i);
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
                FloatVector zeros = FloatVector.zero(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
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

    @Override
    protected float associativeOp(TensorAssociativeOp op) {
        if (loop.step == 1) {
            return unitAssociativeOp(op);
        }
        return strideAssociativeOp(op);
    }

    private float unitAssociativeOp(TensorAssociativeOp op) {
        float aggregate = op.initialFloat();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;

            int i = offset;
            if (bound > offset) {
                FloatVector vectorAggregate = op.initialVectorFloat(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i);
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a);
                }
                aggregate = op.applyFloat(aggregate, vectorAggregate.reduceLanes(op.vop()));
            }
            for (; i < loop.bound + offset; i++) {
                aggregate = op.applyFloat(aggregate, array[i]);
            }
        }
        return aggregate;
    }

    private float strideAssociativeOp(TensorAssociativeOp op) {
        float aggregate = op.initialFloat();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;

            int i = offset;
            if (bound > offset) {
                FloatVector vsum = op.initialVectorFloat(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    vsum = vsum.lanewise(op.vop(), a);
                }
                aggregate = op.applyFloat(aggregate, vsum.reduceLanes(op.vop()));
            }
            for (; i < loop.bound + offset; i += loop.step) {
                aggregate = op.applyFloat(aggregate, array[i]);
            }
        }
        return aggregate;
    }

    @Override
    protected float nanAssociativeOp(TensorAssociativeOp op) {
        if (loop.step == 1) {
            return nanUnitAssociativeOp(op);
        }
        return nanStrideAssociativeOp(op);
    }

    private float nanUnitAssociativeOp(TensorAssociativeOp op) {
        float aggregate = op.initialFloat();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;

            int i = offset;
            if (bound > offset && dtype().isFloat()) {
                FloatVector vectorAggregate = op.initialVectorFloat(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i);
                    VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a, mask);
                }
                VectorMask<Float> mask = vectorAggregate.test(VectorOperators.IS_NAN).not();
                aggregate = op.applyFloat(aggregate, vectorAggregate.reduceLanes(op.vop(), mask));
            }
            for (; i < loop.bound + offset; i++) {
                if (!dtype().isNaN(array[i])) {
                    aggregate = op.applyFloat(aggregate, array[i]);
                }
            }
        }
        return aggregate;
    }

    private float nanStrideAssociativeOp(TensorAssociativeOp op) {
        float aggregate = op.initialFloat();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;

            int i = offset;
            if (bound > offset && dtype().isFloat()) {
                FloatVector vectorAggregate = op.initialVectorFloat(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    VectorMask<Float> mask = a.test(VectorOperators.IS_NAN).not();
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a, mask);
                }
                VectorMask<Float> mask = vectorAggregate.test(VectorOperators.IS_NAN).not();
                aggregate = op.applyFloat(aggregate, vectorAggregate.reduceLanes(op.vop(), mask));
            }
            for (; i < loop.bound + offset; i += loop.step) {
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
        VectorizedFloatTensorStride dst = (VectorizedFloatTensorStride)
                engine.ofFloat().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

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
            if (chd.step == 1) {
                int i = ptr;
                int bound = SPEC.loopBound(chd.size) + ptr;
                for (; i < bound; i += SPEC_LEN) {
                    FloatVector a = FloatVector.fromArray(SPEC, array, i);
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
    public FloatTensor copyTo(FloatTensor to, Order askOrder) {

        if (to instanceof VectorizedFloatTensorStride dst) {

            int limit = Math.floorDiv(L2_CACHE_SIZE, dtype().bytes() * 2 * engine.cpuThreads() * 8);

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
                                    VectorizedFloatTensorStride s = (VectorizedFloatTensorStride) this.narrowAll(false, ss, es);
                                    VectorizedFloatTensorStride d = (VectorizedFloatTensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(VectorizedFloatTensorStride src, VectorizedFloatTensorStride dst, Order askOrder) {
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
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    FloatVector a = (loop.step == 1) ?
                            FloatVector.fromArray(SPEC, array, i) :
                            FloatVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    a.intoArray(copy, pos);
                    pos += SPEC_LEN;
                }
            }
            for (; i < loop.bound + offset; i++) {
                copy[pos++] = array[i];
            }
        }
        return copy;
    }

     */
}