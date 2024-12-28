/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.darray.operator.impl;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayReduceOp;
import rapaio.data.OperationNotAvailableException;

public final class ReduceOpNanMean extends DArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor<Byte> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor<Byte> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor<Byte> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor<Integer> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor<Integer> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor<Integer> loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor<Float> loop, Storage storage) {
        float sum = 0;
        float count = 0;
        for (int p : loop.offsets) {
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p);
                var m = v.test(VectorOperators.IS_NAN);
                a = a.add(v, m.not());
                count += m.not().trueCount();
                p += loop.simdLen;
            }
            var m = a.test(VectorOperators.IS_NAN);
            sum += a.reduceLanes(VectorOperators.ADD, m.not());
            for (; i < loop.bound; i++) {
                float v = storage.getFloat(p);
                if (Float.isNaN(v)) {
                    sum += v;
                    count++;
                }
                p++;
            }
        }
        float mean = sum / count;
        sum = 0;
        FloatVector vmean = FloatVector.broadcast(Simd.vsf, mean);
        for (int p : loop.offsets) {
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getFloatVector(p);
                var m = v.test(VectorOperators.IS_NAN);
                v = v.sub(vmean, m.not());
                a = a.add(v, m.not());
                p += loop.simdLen;
            }
            var m = a.test(VectorOperators.IS_NAN);
            sum += a.reduceLanes(VectorOperators.ADD, m.not());
            for (; i < loop.bound; i++) {
                float v = storage.getFloat(p);
                if (!Float.isNaN(v)) {
                    sum += storage.getFloat(p) - mean;
                }
                p++;
            }
        }

        return mean + sum / count;
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor<Float> loop, Storage storage) {
        float sum = 0;
        float count = 0;
        for (int p : loop.offsets) {
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getFloatVector(p, loop.simdOffsets(), 0);
                var m = v.test(VectorOperators.IS_NAN);
                a = a.add(v, m.not());
                count += m.not().trueCount();
                p += loop.simdLen * loop.step;
            }
            var m = a.test(VectorOperators.IS_NAN);
            sum += a.reduceLanes(VectorOperators.ADD, m.not());
            for (; i < loop.bound; i++) {
                float v = storage.getFloat(p);
                if (!Float.isNaN(v)) {
                    sum += v;
                    count++;
                }
                p += loop.step;
            }
        }
        float mean = sum / count;
        sum = 0;
        for (int p : loop.offsets) {
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getFloatVector(p, loop.simdOffsets(), 0);
                var m = v.test(VectorOperators.IS_NAN);
                v = v.sub(mean, m.not());
                a = a.add(v, m.not());
                p += loop.simdLen * loop.step;
            }
            var m = a.test(VectorOperators.IS_NAN);
            sum += a.reduceLanes(VectorOperators.ADD, m.not());
            for (; i < loop.bound; i++) {
                float v = storage.getFloat(p);
                if (!Float.isNaN(v)) {
                    sum += storage.getFloat(p) - mean;
                }
                p += loop.step;
            }
        }
        return mean + sum / count;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor<Float> loop, Storage storage) {
        float sum = 0;
        float count = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                float v = storage.getFloat(p);
                if (!Float.isNaN(v)) {
                    sum += v;
                    count++;
                }
                p += loop.step;
            }
        }
        float mean = sum / count;
        sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                float v = storage.getFloat(p);
                if (!Float.isNaN(v)) {
                    sum += storage.getFloat(p) - mean;
                }
                p += loop.step;
            }
        }

        return mean + sum / count;
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor<Double> loop, Storage storage) {
        double sum = 0;
        double count = 0;
        for (int p : loop.offsets) {
            var a = Simd.zeroDouble();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getDoubleVector(p);
                var m = v.test(VectorOperators.IS_NAN);
                a = a.add(v, m.not());
                count += m.not().trueCount();
                p += loop.simdLen;
            }
            var m = a.test(VectorOperators.IS_NAN);
            sum += a.reduceLanes(VectorOperators.ADD, m.not());
            for (; i < loop.bound; i++) {
                double v = storage.getDouble(p);
                if (!Double.isNaN(v)) {
                    sum += v;
                    count++;
                }
                p++;
            }
        }
        double mean = sum / count;
        sum = 0;
        for (int p : loop.offsets) {
            var a = Simd.zeroDouble();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getDoubleVector(p);
                var m = v.test(VectorOperators.IS_NAN);
                v = v.sub(mean, m.not());
                a = a.add(v, m.not());
                p += loop.simdLen;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                double v = storage.getDouble(p);
                if (!Double.isNaN(v)) {
                    sum += storage.getDouble(p) - mean;
                }
                p++;
            }
        }
        return mean + sum / count;
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor<Double> loop, Storage storage) {
        double sum = 0;
        double count = 0;
        for (int p : loop.offsets) {
            var a = Simd.zeroDouble();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getDoubleVector(p, loop.simdOffsets(), 0);
                var m = v.test(VectorOperators.IS_NAN);
                a = a.add(v, m.not());
                count += m.not().trueCount();
                p += loop.simdLen * loop.step;
            }
            var m = a.test(VectorOperators.IS_NAN);
            sum += a.reduceLanes(VectorOperators.ADD, m.not());
            for (; i < loop.bound; i++) {
                double v = storage.getDouble(p);
                if (!Double.isNaN(v)) {
                    sum += v;
                    count++;
                }
                p += loop.step;
            }
        }
        double mean = sum / count;
        sum = 0;
        for (int p : loop.offsets) {
            var a = Simd.zeroDouble();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var v = storage.getDoubleVector(p, loop.simdOffsets(), 0);
                var m = v.test(VectorOperators.IS_NAN);
                v = v.sub(mean, m.not());
                a = a.add(v, m.not());
                p += loop.simdLen * loop.step;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                double v = storage.getDouble(p);
                if (!Double.isNaN(v)) {
                    sum += v - mean;
                }
                p += loop.step;
            }
        }
        return mean + sum / count;
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor<Double> loop, Storage storage) {
        double sum = 0;
        double count = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                double v = storage.getDouble(p);
                if (!Double.isNaN(v)) {
                    sum += v;
                    count++;
                }
                p += loop.step;
            }
        }
        double mean = sum / count;
        sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                double v = storage.getDouble(p);
                if (!Double.isNaN(v)) {
                    sum += v - mean;
                }
                p += loop.step;
            }
        }
        return mean + sum / count;
    }
}
