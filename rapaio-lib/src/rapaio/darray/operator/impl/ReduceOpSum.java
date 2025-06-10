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

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayReduceOp;

public final class ReduceOpSum extends DArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = 0;
        for (int p : loop.offsets) {
            ByteVector a = Simd.zeroByte();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p);
                a = a.add(v);
                p += loop.simdLen;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getByte(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = 0;
        for (int p : loop.offsets) {
            ByteVector a = Simd.zeroByte();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p, loop.simdIdx(), 0);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getByte(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result += storage.getByte(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = 0;
        for (int p : loop.offsets) {
            IntVector a = Simd.zeroInt();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p);
                a = a.add(v);
                p += loop.simdLen;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getInt(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = 0;
        for (int p : loop.offsets) {
            IntVector a = Simd.zeroInt();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p, loop.simdIdx(), 0);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getInt(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result += storage.getInt(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = 0;
        for (int p : loop.offsets) {
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p);
                a = a.add(v);
                p += loop.simdLen;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getFloat(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = 0;
        for (int p : loop.offsets) {
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p, loop.simdIdx(), 0);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getFloat(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result += storage.getFloat(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = 0;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.zeroDouble();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p);
                a = a.add(v);
                p += loop.simdLen;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getDouble(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = 0;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.zeroDouble();
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p, loop.simdIdx(), 0);
                a = a.add(v);
                p += loop.simdLen * loop.step;
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < loop.bound; i++) {
                result += storage.getDouble(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result += storage.getDouble(p);
                p += loop.step;
            }
        }
        return result;
    }
}
