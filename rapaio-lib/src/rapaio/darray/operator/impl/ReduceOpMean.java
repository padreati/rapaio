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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayReduceOp;
import rapaio.data.OperationNotAvailableException;

public final class ReduceOpMean extends DArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    private static final float initFloat = 0;
    private static final double initDouble = 0;

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor loop, Storage storage) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        float sum = initFloat;
        for (int p : loop.offsets) {
            FloatVector a = Simd.broadcast(initFloat);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p);
                a = a.add(v);
                p += loop.simdLen;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getFloat(p);
                p++;
            }
        }
        float count = loop.bound * loop.offsets.length;
        float mean = sum / count;

        sum = 0;
        FloatVector vmean = FloatVector.broadcast(Simd.vsFloat, mean);
        for (int p : loop.offsets) {
            FloatVector a = Simd.broadcast(initFloat);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p);
                v = v.sub(vmean);
                a = a.add(v);
                p += loop.simdLen;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getFloat(p) - mean;
                p++;
            }
        }

        return mean + sum / count;
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor loop, Storage storage) {
        float sum = initFloat;
        for (int p : loop.offsets) {
            FloatVector a = Simd.broadcast(initFloat);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p, loop.simdIdx(), 0);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getFloat(p);
                p += loop.step;
            }
        }
        float count = loop.bound * loop.offsets.length;
        float mean = sum / count;

        sum = 0;
        for (int p : loop.offsets) {
            FloatVector a = Simd.broadcast(initFloat);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p, loop.simdIdx(), 0);
                v = v.sub(mean);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getFloat(p) - mean;
                p += loop.step;
            }
        }

        return mean + sum / count;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor loop, Storage storage) {
        float sum = initFloat;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += storage.getFloat(p);
                p += loop.step;
            }
        }

        float count = loop.bound * loop.offsets.length;
        float mean = sum / count;

        sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += storage.getFloat(p) - mean;
                p += loop.step;
            }
        }

        return mean + sum / count;
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        double sum = initDouble;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.broadcast(initDouble);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p);
                a = a.add(v);
                p += loop.simdLen;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getDouble(p);
                p++;
            }
        }

        double count = loop.bound * loop.offsets.length;
        double mean = sum / count;

        sum = 0;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.broadcast(initDouble);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p);
                v = v.sub(mean);
                a = a.add(v);
                p += loop.simdLen;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getDouble(p) - mean;
                p++;
            }
        }
        return mean + sum / count;
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor loop, Storage storage) {
        double sum = initDouble;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.broadcast(initDouble);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p, loop.simdIdx(), 0);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getDouble(p);
                p += loop.step;
            }
        }

        double count = loop.bound * loop.offsets.length;
        double mean = sum / count;
        sum = 0;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.broadcast(initDouble);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p, loop.simdIdx(), 0);
                v = v.sub(mean);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            sum += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                sum += storage.getDouble(p) - mean;
                p += loop.step;
            }
        }

        return mean + sum / count;
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor loop, Storage storage) {
        double sum = initDouble;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += storage.getDouble(p);
                p += loop.step;
            }
        }

        double count = loop.bound * loop.offsets.length;
        double mean = sum / count;

        sum = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                sum += storage.getDouble(p) - mean;
                p += loop.step;
            }
        }

        return mean + sum / count;
    }
}
