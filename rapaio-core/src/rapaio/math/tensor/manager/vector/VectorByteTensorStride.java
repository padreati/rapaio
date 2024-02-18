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

package rapaio.math.tensor.manager.vector;

import java.util.Arrays;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.manager.base.BaseByteTensorStride;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorUnaryOp;

public final class VectorByteTensorStride extends BaseByteTensorStride implements Tensor<Byte> {

    private static final VectorSpecies<Byte> VS = ByteVector.SPECIES_PREFERRED;
    private static final int VS_LEN = VS.length();

    private final int[] loopIndexes;

    public VectorByteTensorStride(TensorManager engine, StrideLayout layout, Storage<Byte> storage) {
        super(engine, layout, storage);
        this.loopIndexes = loop.step == 1 ? null : loopIndexes(loop.step);
    }

    private int[] loopIndexes(int step) {
        int[] indexes = new int[VS_LEN];
        for (int i = 1; i < VS_LEN; i++) {
            indexes[i] = indexes[i - 1] + step;
        }
        return indexes;
    }

    @Override
    public Tensor<Byte> fill_(Byte value) {
        for (int offset : loop.offsets) {
            int bound = VS.loopBound(loop.size);
            int i = 0;
            ByteVector fill = ByteVector.broadcast(VS, value);
            for (; i < bound; i += VS_LEN) {
                if (loop.step == 1) {
                    storage.saveByte(fill, offset + i * loop.step);
                } else {
                    storage.saveByte(fill, offset + i * loop.step, loopIndexes, 0);
                }
            }
            for (; i < loop.size; i++) {
                storage.setByte(offset + i * loop.step, value);
            }
        }
        return this;
    }

    @Override
    public Tensor<Byte> fillNan_(Byte value) {
        if(!dtype().floatingPoint()) {
            return this;
        }
        for (int offset : loop.offsets) {
            int bound = VS.loopBound(loop.size);
            int i = 0;
            ByteVector fill = ByteVector.broadcast(VS, value);
            for (; i < bound; i += VS_LEN) {
                int p = offset + i * loop.step;
                if (loop.step == 1) {
                    ByteVector a = storage.loadByte(VS, p);
                    VectorMask<Byte> m = a.test(VectorOperators.IS_NAN);
                    storage.saveByte(fill, p, m);
                } else {
                    ByteVector a = storage.loadByte(VS, p, loopIndexes, 0);
                    VectorMask<Byte> m = a.test(VectorOperators.IS_NAN);
                    storage.saveByte(fill, p, loopIndexes, 0, m);
                }
            }
            for (; i < loop.size; i++) {
                int p = offset + i * loop.step;
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
            int bound = VS.loopBound(loop.size);
            int i = 0;
            for (; i < bound; i += VS_LEN) {
                int p = offset + i * loop.step;
                ByteVector a = loop.step == 1 ? storage.loadByte(VS, p) : storage.loadByte(VS, p, loopIndexes, 0);
                boolean any = false;
                if (!dtype().isNaN(min)) {
                    VectorMask<Byte> m = a.compare(VectorOperators.LT, min);
                    if (m.anyTrue()) {
                        a = a.blend(min, m);
                        any = true;
                    }
                }
                if (!dtype().isNaN(max)) {
                    VectorMask<Byte> m = a.compare(VectorOperators.GT, max);
                    if (m.anyTrue()) {
                        a = a.blend(max, m);
                        any = true;
                    }
                }
                if (any) {
                    if (loop.step == 1) {
                        storage.saveByte(a, p);
                    } else {
                        storage.saveByte(a, p, loopIndexes, 0);
                    }
                }
            }
            for (; i < loop.size; i++) {
                int p = offset + i * loop.step;
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

    @Override
    protected void unaryOp(TensorUnaryOp op) {
        if (!op.vectorSupport()) {
            super.unaryOp(op);
            return;
        }
        if (op.floatingPointOnly() && !dtype().floatingPoint()) {
            throw new IllegalArgumentException("This operation is available only for floating point tensors.");
        }
        if (loop.step == 1) {
            unaryOpUnit(op);
        } else {
            unaryOpStep(op);
        }
    }

    private void unaryOpUnit(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            int bound = VS.loopBound(loop.size) + off;
            int i = off;
            for (; i < bound; i += VS_LEN) {
                ByteVector a = storage.loadByte(VS, i);
                a = op.applyByte(a);
                storage.saveByte(a, i);
            }
            for (; i < loop.size + off; i++) {
                storage.setByte(i, op.applyByte(storage.getByte(i)));
            }
        }
    }

    private void unaryOpStep(TensorUnaryOp op) {
        for (int off : loop.offsets) {
            int bound = VS.loopBound(loop.size);
            int i = 0;
            for (; i < bound; i += VS_LEN) {
                int p = off + i * loop.step;
                ByteVector a = storage.loadByte(VS, p, loopIndexes, 0);
                a = op.applyByte(a);
                storage.saveByte(a, p, loopIndexes, 0);
            }
            for (; i < loop.size; i++) {
                int p = off + i * loop.step;
                storage.setByte(p, op.applyByte(storage.getByte(p)));
            }
        }
    }

    private void binaryScalarOpUnit(TensorBinaryOp op, byte value) {
        for (int off : loop.offsets) {
            int bound = VS.loopBound(loop.size);
            int i = 0;
            ByteVector b = ByteVector.broadcast(VS, value);
            for (; i < bound; i += VS_LEN) {
                int p = off + i * loop.step;
                ByteVector a = storage.loadByte(VS, p);
                a = op.applyByte(a, b);
                storage.saveByte(a, p);
            }
            for (; i < loop.size; i++) {
                int p = off + i * loop.step;
                storage.setByte(p, op.applyByte(storage.getByte(p), value));
            }
        }
    }

    private void binaryScalarOpStep(TensorBinaryOp op, byte value) {
        for (int offset : loop.offsets) {
            int bound = VS.loopBound(loop.size);
            int i = 0;
            ByteVector b = ByteVector.broadcast(VS, value);
            for (; i < bound; i += VS_LEN) {
                int p = offset + i * loop.step;
                ByteVector a = storage.loadByte(VS, p, loopIndexes, 0);
                a = op.applyByte(a, b);
                storage.saveByte(a, p, loopIndexes, 0);
            }
            for (; i < loop.size; i++) {
                int p = offset + i * loop.step;
                storage.setByte(p, op.applyByte(storage.getByte(p), value));
            }
        }
    }

    protected void binaryScalarOp(TensorBinaryOp op, byte value) {
        if (loop.step == 1) {
            binaryScalarOpUnit(op, value);
        } else {
            binaryScalarOpStep(op, value);
        }
    }
    /*

    @Override
    public Byte vdot(ByteTensor tensor) {
        return vdot(tensor, 0, shape().dim(0));
    }

    @Override
    public Byte vdot(ByteTensor tensor, int start, int end) {
        if (shape().rank() != 1 || tensor.shape().rank() != 1 || shape().dim(0) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException(
                    "Operands are not valid for vector dot product (v = %s, v = %s)."
                            .formatted(shape().toString(), tensor.shape().toString()));
        }
        if (start >= end || start < 0 || end > tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Start and end indexes are invalid (start: %d, end: %s).".formatted(start, end));
        }
        VectorizedByteTensorStride dts = (VectorizedByteTensorStride) tensor;
        int step1 = layout.stride(0);
        int step2 = dts.layout.stride(0);
        int start1 = layout.offset() + start * step1;
        int start2 = dts.layout.offset() + start * step2;
        int i = 0;
        int bound = SPEC.loopBound(end - start);
        ByteVector vsum = ByteVector.zero(SPEC);
        for (; i < bound; i += SPEC_LEN) {
            ByteVector a = loop.step == 1 ?
                    ByteVector.fromArray(SPEC, array, start1) :
                    ByteVector.fromArray(SPEC, array, start1, loopIndexes, 0);
            ByteVector b = dts.loop.step == 1 ?
                    ByteVector.fromArray(SPEC, dts.array, start2) :
                    ByteVector.fromArray(SPEC, dts.array, start2, dts.loopIndexes, 0);
            vsum = vsum.add(a.mul(b));
            start1 += SPEC_LEN * step1;
            start2 += SPEC_LEN * step2;
        }
        byte sum = vsum.reduceLanes(VectorOperators.ADD);
        for (; i < end - start; i++) {
            sum += array[start1] * dts.array[start2];
            start1 += step1;
            start2 += step2;
        }
        return sum;
    }

    @Override
    public ByteTensor mv(ByteTensor tensor) {
        if (shape().rank() != 2 || tensor.shape().rank() != 1 || shape().dim(1) != tensor.shape().dim(0)) {
            throw new IllegalArgumentException("Operands are not valid for matrix-vector multiplication "
                    + "(m = %s, v = %s).".formatted(shape().toString(), tensor.shape().toString()));
        }
        byte[] result = new byte[shape().dim(0)];
        var it = ptrIterator(Order.C);
        for (int i = 0; i < shape().dim(0); i++) {
            var innerIt = tensor.ptrIterator(Order.C);
            byte sum = 0;
            for (int j = 0; j < shape().dim(1); j++) {
                sum += (byte)(ptrGetByte(it.nextInt()) * tensor.ptrGetByte(innerIt.nextInt()));
            }
            result[i] = sum;
        }
        StrideLayout layout = StrideLayout.ofDense(Shape.of(shape().dim(0)), 0, Order.C);
        return engine.ofByte().stride(layout, result);
    }

    @Override
    public ByteTensor mm(ByteTensor t, Order askOrder) {
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

        var result = new byte[m * p];
        var ret = engine.ofByte().stride(StrideLayout.ofDense(Shape.of(m, p), 0, askOrder), result);

        List<ByteTensor> rows = chunk(0, false, 1);
        List<ByteTensor> cols = t.chunk(1, false, 1);

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
                                var krow = (VectorizedByteTensorStride) rows.get(i);
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
    public Statistics<Byte, ByteTensor> stats() {
        if (!dtype().isFloat()) {
            throw new IllegalArgumentException("Operation available only for float tensors.");
        }
        if (loop.step == 1) {
            return computeUnitStats();
        }
        return computeStrideStats();
    }

    private Statistics<Byte, ByteTensor> computeUnitStats() {
        int size = size();
        int nanSize = 0;
        byte mean;
        byte nanMean;
        byte variance;
        byte nanVariance;

        // first pass compute raw mean
        byte sum = 0;
        byte nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            int i = offset;
            ByteVector vsum = ByteVector.zero(SPEC);
            ByteVector vnanSum = ByteVector.zero(SPEC);
            for (; i < bound; i += SPEC_LEN) {
                ByteVector a = ByteVector.fromArray(SPEC, array, i);
                VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                nanSize += mask.trueCount();
                vsum = vsum.add(a);
                vnanSum = vnanSum.add(a, mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Byte> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);
            for (; i < loop.bound + offset; i++) {
                sum += array[i];
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i];
                    nanSize++;
                }
            }
        }
        mean = (byte) (sum / size);
        nanMean = (byte) (nanSum / nanSize);

        // second pass adjustments for mean
        sum = 0;
        nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            ByteVector vsum = ByteVector.zero(SPEC);
            ByteVector vnanSum = ByteVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN) {
                ByteVector a = ByteVector.fromArray(SPEC, array, i);
                VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                vsum = vsum.add(a.sub(mean));
                vnanSum = vnanSum.add(a.sub(mean), mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Byte> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);

            for (; i < loop.bound + offset; i++) {
                sum += (byte) (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum += (byte) (array[i] - nanMean);
                }
            }
        }
        mean += (byte) (sum / size);
        nanMean += (byte) (nanSum / nanSize);

        // third pass compute variance
        byte sum2 = 0;
        byte sum3 = 0;
        byte nanSum2 = 0;
        byte nanSum3 = 0;

        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            ByteVector vsum2 = ByteVector.zero(SPEC);
            ByteVector vsum3 = ByteVector.zero(SPEC);
            ByteVector vnanSum2 = ByteVector.zero(SPEC);
            ByteVector vnanSum3 = ByteVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN) {
                ByteVector a = ByteVector.fromArray(SPEC, array, i);
                VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                ByteVector b = a.sub(mean);
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
                sum2 += (byte) ((array[i] - mean) * (array[i] - mean));
                sum3 += (byte) (array[i] - mean);

                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (byte) ((array[i] - nanMean) * (array[i] - nanMean));
                    nanSum3 += (byte) (array[i] - nanMean);
                }
            }
        }
        variance = (byte) ((sum2 - sum3 * sum3 / size) / size);
        nanVariance = (byte) ((nanSum2 - nanSum3 * nanSum3 / nanSize) / nanSize);

        return new Statistics<>(dtype(), size, nanSize, mean, nanMean, variance, nanVariance);
    }

    private Statistics<Byte, ByteTensor> computeStrideStats() {
        int size = size();
        int nanSize = 0;
        byte mean;
        byte nanMean;
        byte variance;
        byte nanVariance;

        // first pass compute raw mean
        byte sum = 0;
        byte nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            ByteVector vsum = ByteVector.zero(SPEC);
            ByteVector vnanSum = ByteVector.zero(SPEC);
            for (; i < bound; i += SPEC_LEN * loop.step) {
                ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
                VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                nanSize += mask.trueCount();
                vsum = vsum.add(a);
                vnanSum = vnanSum.add(a, mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Byte> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);
            for (; i < loop.bound + offset; i += loop.step) {
                sum += array[i];
                if (!dtype().isNaN(array[i])) {
                    nanSum += array[i];
                    nanSize++;
                }
            }
        }
        mean = (byte) (sum / size);
        nanMean = (byte) (nanSum / nanSize);

        // second pass adjustments for mean
        sum = 0;
        nanSum = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            ByteVector vsum = ByteVector.zero(SPEC);
            ByteVector vnanSum = ByteVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
                VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                vsum = vsum.add(a.sub(mean));
                vnanSum = vnanSum.add(a.sub(mean), mask);
            }
            sum += vsum.reduceLanes(VectorOperators.ADD);
            VectorMask<Byte> mask = vnanSum.test(VectorOperators.IS_NAN).not();
            nanSum += vnanSum.reduceLanes(VectorOperators.ADD, mask);

            for (; i < loop.bound + offset; i += loop.step) {
                sum += (byte) (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum += (byte) (array[i] - nanMean);
                }
            }
        }
        mean += (byte) (sum / size);
        nanMean += (byte) (nanSum / nanSize);

        // third pass compute variance
        byte sum2 = 0;
        byte sum3 = 0;
        byte nanSum2 = 0;
        byte nanSum3 = 0;

        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;
            ByteVector vsum2 = ByteVector.zero(SPEC);
            ByteVector vsum3 = ByteVector.zero(SPEC);
            ByteVector vnanSum2 = ByteVector.zero(SPEC);
            ByteVector vnanSum3 = ByteVector.zero(SPEC);
            int i = offset;
            for (; i < bound; i += SPEC_LEN * loop.step) {
                ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
                ByteVector b = a.sub(mean);
                vsum2 = vsum2.add(b.mul(b));
                vsum3 = vsum3.add(b);
                VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                b = a.sub(nanMean);
                vnanSum2 = vnanSum2.add(b.mul(b), mask);
                vnanSum3 = vnanSum3.add(b, mask);
            }
            sum2 += vsum2.reduceLanes(VectorOperators.ADD);
            sum3 += vsum3.reduceLanes(VectorOperators.ADD);
            nanSum2 += vnanSum2.reduceLanes(VectorOperators.ADD, vnanSum2.test(VectorOperators.IS_NAN).not());
            nanSum3 += vnanSum3.reduceLanes(VectorOperators.ADD, vnanSum3.test(VectorOperators.IS_NAN).not());
            for (; i < loop.bound + offset; i += loop.step) {
                sum2 += (byte) ((array[i] - mean) * (array[i] - mean));
                sum3 += (byte) (array[i] - mean);
                if (!dtype().isNaN(array[i])) {
                    nanSum2 += (byte) ((array[i] - nanMean) * (array[i] - nanMean));
                    nanSum3 += (byte) (array[i] - nanMean);
                }
            }
        }
        variance = (byte) ((sum2 - (sum3 * sum3) / size) / size);
        nanVariance = (byte) ((nanSum2 - (nanSum3 * nanSum3) / nanSize) / nanSize);

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
                        ByteVector a = ByteVector.fromArray(SPEC, array, i);
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
                        ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
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
                ByteVector zeros = ByteVector.zero(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i);
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
                ByteVector zeros = ByteVector.zero(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
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
    protected byte associativeOp(TensorAssociativeOp op) {
        if (loop.step == 1) {
            return unitAssociativeOp(op);
        }
        return strideAssociativeOp(op);
    }

    private byte unitAssociativeOp(TensorAssociativeOp op) {
        byte aggregate = op.initialByte();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;

            int i = offset;
            if (bound > offset) {
                ByteVector vectorAggregate = op.initialVectorByte(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i);
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a);
                }
                aggregate = op.applyByte(aggregate, vectorAggregate.reduceLanes(op.vop()));
            }
            for (; i < loop.bound + offset; i++) {
                aggregate = op.applyByte(aggregate, array[i]);
            }
        }
        return aggregate;
    }

    private byte strideAssociativeOp(TensorAssociativeOp op) {
        byte aggregate = op.initialByte();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;

            int i = offset;
            if (bound > offset) {
                ByteVector vsum = op.initialVectorByte(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    vsum = vsum.lanewise(op.vop(), a);
                }
                aggregate = op.applyByte(aggregate, vsum.reduceLanes(op.vop()));
            }
            for (; i < loop.bound + offset; i += loop.step) {
                aggregate = op.applyByte(aggregate, array[i]);
            }
        }
        return aggregate;
    }

    @Override
    protected byte nanAssociativeOp(TensorAssociativeOp op) {
        if (loop.step == 1) {
            return nanUnitAssociativeOp(op);
        }
        return nanStrideAssociativeOp(op);
    }

    private byte nanUnitAssociativeOp(TensorAssociativeOp op) {
        byte aggregate = op.initialByte();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) + offset;

            int i = offset;
            if (bound > offset && dtype().isFloat()) {
                ByteVector vectorAggregate = op.initialVectorByte(SPEC);
                for (; i < bound; i += SPEC_LEN) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i);
                    VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a, mask);
                }
                VectorMask<Byte> mask = vectorAggregate.test(VectorOperators.IS_NAN).not();
                aggregate = op.applyByte(aggregate, vectorAggregate.reduceLanes(op.vop(), mask));
            }
            for (; i < loop.bound + offset; i++) {
                if (!dtype().isNaN(array[i])) {
                    aggregate = op.applyByte(aggregate, array[i]);
                }
            }
        }
        return aggregate;
    }

    private byte nanStrideAssociativeOp(TensorAssociativeOp op) {
        byte aggregate = op.initialByte();
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;

            int i = offset;
            if (bound > offset && dtype().isFloat()) {
                ByteVector vectorAggregate = op.initialVectorByte(SPEC);
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
                    VectorMask<Byte> mask = a.test(VectorOperators.IS_NAN).not();
                    vectorAggregate = vectorAggregate.lanewise(op.vop(), a, mask);
                }
                VectorMask<Byte> mask = vectorAggregate.test(VectorOperators.IS_NAN).not();
                aggregate = op.applyByte(aggregate, vectorAggregate.reduceLanes(op.vop(), mask));
            }
            for (; i < loop.bound + offset; i += loop.step) {
                if (!dtype().isNaN(array[i])) {
                    aggregate = op.applyByte(aggregate, array[i]);
                }
            }
        }
        return aggregate;
    }

    @Override
    public ByteTensor copy(Order askOrder) {
        askOrder = Order.autoFC(askOrder);

        byte[] copy = new byte[size()];
        VectorizedByteTensorStride dst = (VectorizedByteTensorStride)
                engine.ofByte().stride(StrideLayout.ofDense(shape(), 0, askOrder), copy);

        if (layout.storageFastOrder() == askOrder) {
            sameLayoutCopy(copy, askOrder);
        } else {
            copyTo(dst, askOrder);
        }
        return dst;
    }

    private void sameLayoutCopy(byte[] copy, Order askOrder) {
        var chd = StrideLoopDescriptor.of(layout, askOrder);
        var last = 0;
        for (int ptr : chd.offsets) {
            if (chd.step == 1) {
                int i = ptr;
                int bound = SPEC.loopBound(chd.size) + ptr;
                for (; i < bound; i += SPEC_LEN) {
                    ByteVector a = ByteVector.fromArray(SPEC, array, i);
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
    public ByteTensor copyTo(ByteTensor to, Order askOrder) {

        if (to instanceof VectorizedByteTensorStride dst) {

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
                                    VectorizedByteTensorStride s = (VectorizedByteTensorStride) this.narrowAll(false, ss, es);
                                    VectorizedByteTensorStride d = (VectorizedByteTensorStride) dst.narrowAll(false, ss, es);
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

    private void directCopyTo(VectorizedByteTensorStride src, VectorizedByteTensorStride dst, Order askOrder) {
        var chd = StrideLoopDescriptor.of(src.layout, askOrder);
        var it2 = dst.ptrIterator(askOrder);
        for (int ptr : chd.offsets) {
            for (int i = ptr; i < ptr + chd.bound; i += chd.step) {
                dst.array[it2.nextInt()] = src.array[i];
            }
        }
    }

    @Override
    public byte[] toArray() {
        if (shape().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be transformed into array.");
        }
        byte[] copy = new byte[size()];
        int pos = 0;
        for (int offset : loop.offsets) {
            int bound = SPEC.loopBound(loop.size) * loop.step + offset;
            int i = offset;
            if (bound > offset) {
                for (; i < bound; i += SPEC_LEN * loop.step) {
                    ByteVector a = (loop.step == 1) ?
                            ByteVector.fromArray(SPEC, array, i) :
                            ByteVector.fromArray(SPEC, array, i, loopIndexes, 0);
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

    @Override
    public String toString() {
        String strDIms = Arrays.toString(layout.dims());
        String strStrides = Arrays.toString(layout.strides());
        return STR."VectorStride{\{dtype().id()},\{strDIms},\{layout.offset()},\{strStrides}}\n\{toContent()}";
    }
}
