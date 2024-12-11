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

public final class ReduceOpMax extends DArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    public static final byte initByte = Byte.MIN_VALUE;
    public static final int initInt = Integer.MIN_VALUE;
    public static final float initFloat = Float.NEGATIVE_INFINITY;
    public static final double initDouble = Double.NEGATIVE_INFINITY;

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            ByteVector a = Simd.broadcast(initByte);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p);
                a = a.max(v);
                p += loop.simdLen;
            }
            result = (byte) Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = (byte) Math.max(result, storage.getByte(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            ByteVector a = Simd.broadcast(initByte);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p, loop.simdOffsets(), 0);
                a = a.max(v);
                p += loop.simdLen * loop.step;
            }
            result = (byte) Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = (byte) Math.max(result, storage.getByte(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = (byte) Math.max(result, storage.getByte(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            IntVector a = Simd.broadcast(initInt);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p);
                a = a.max(v);
                p += loop.simdLen;
            }
            result = Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = Math.max(result, storage.getInt(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            IntVector a = Simd.broadcast(initInt);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p, loop.simdOffsets(), 0);
                a = a.max(v);
                p += loop.simdLen * loop.step;
            }
            result = Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = Math.max(result, storage.getInt(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = Math.max(result, storage.getInt(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = initFloat;
        for (int p : loop.offsets) {
            FloatVector a = Simd.broadcast(initFloat);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p);
                a = a.max(v);
                p += loop.simdLen;
            }
            result = Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = Math.max(result, storage.getFloat(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = initFloat;
        for (int p : loop.offsets) {
            FloatVector a = Simd.broadcast(initFloat);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(p, loop.simdOffsets(), 0);
                a = a.max(v);
                p += loop.simdLen * loop.step;
            }
            result = Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = Math.max(result, storage.getFloat(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = initFloat;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = Math.max(result, storage.getFloat(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = initDouble;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.broadcast(initDouble);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p);
                a = a.max(v);
                p += loop.simdLen;
            }
            result = Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = Math.max(result, storage.getDouble(p));
                p++;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = initDouble;
        for (int p : loop.offsets) {
            DoubleVector a = Simd.broadcast(initDouble);
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(p, loop.simdOffsets(), 0);
                a = a.max(v);
                p += loop.simdLen * loop.step;
            }
            result = Math.max(result, a.reduceLanes(VectorOperators.MAX));
            for (; i < loop.bound; i++) {
                result = Math.max(result, storage.getDouble(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = initDouble;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                result = Math.max(result, storage.getDouble(p));
                p += loop.step;
            }
        }
        return result;
    }
}
