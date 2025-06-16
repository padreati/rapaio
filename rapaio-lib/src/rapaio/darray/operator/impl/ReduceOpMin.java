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

public final class ReduceOpMin extends DArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    public static final byte initByte = Byte.MAX_VALUE;
    public static final int initInt = Integer.MAX_VALUE;
    public static final float initFloat = Float.POSITIVE_INFINITY;
    public static final double initDouble = Double.POSITIVE_INFINITY;

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            ByteVector a = Simd.broadcast(initByte);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p);
                a = a.min(v);
                p += loop.simdLen;
            }
            result = (byte) Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = (byte) Math.min(result, storage.getByte(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            ByteVector a = Simd.broadcast(initByte);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p, loop.simdIdx(), 0);
                a = a.min(v);
                p += loop.simdLen * loop.step;
            }
            result = (byte) Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = (byte) Math.min(result, storage.getByte(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = (byte) Math.min(result, storage.getByte(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            IntVector a = Simd.broadcast(initInt);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p);
                a = a.min(v);
                p += loop.simdLen;
            }
            result = Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = Math.min(result, storage.getInt(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            IntVector a = Simd.broadcast(initInt);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p, loop.simdIdx(), 0);
                a = a.min(v);
                p += loop.simdLen * loop.step;
            }
            result = Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = Math.min(result, storage.getInt(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = Math.min(result, storage.getByte(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        float result = initFloat;
        FloatVector a = Simd.broadcast(initFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p);
                a = a.min(v);
                p += loop.simdLen;
            }
            result = Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = Math.min(result, storage.getFloat(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor loop, Storage storage) {
        float result = initFloat;
        FloatVector a = Simd.broadcast(initFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p, loop.simdIdx(), 0);
                a = a.min(v);
                p += loop.simdLen * loop.step;
            }
            result = Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = Math.min(result, storage.getFloat(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor loop, Storage storage) {
        float result = 0;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = Math.min(result, storage.getFloat(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        double result = initDouble;
        DoubleVector a = Simd.broadcast(initDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p);
                a = a.min(v);
                p += loop.simdLen;
            }
            result = Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = Math.min(result, storage.getDouble(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor loop, Storage storage) {
        double result = initDouble;
        DoubleVector a = Simd.broadcast(initDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p, loop.simdIdx(), 0);
                a = a.min(v);
                p += loop.simdLen * loop.step;
            }
            result = Math.min(result, a.reduceLanes(VectorOperators.MIN));
            for (; i < loop.bound; i++) {
                result = Math.min(result, storage.getDouble(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor loop, Storage storage) {
        double result = initDouble;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = Math.min(result, storage.getDouble(p));
                p += loop.step;
            }
        }
        return result;
    }
}
