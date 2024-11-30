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

package rapaio.narray.operator.impl;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.operator.NArrayReduceOp;

public final class ReduceOpProd extends NArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    private static final byte initByte = 1;
    private static final int initInt = 1;
    private static final float initFloat = 1;
    private static final double initDouble = 1;

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = initByte;
        ByteVector a = ByteVector.broadcast(loop.vs, initByte);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(loop.vs, p);
                a = a.mul(v);
                p += loop.simdLen;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getByte(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = initByte;
        ByteVector a = ByteVector.broadcast(loop.vs, initByte);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(v);
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getByte(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteDefault(StrideLoopDescriptor<Byte> loop, Storage storage) {
        byte result = initByte;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                result *= storage.getByte(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = initInt;
        IntVector a = IntVector.broadcast(loop.vs, initInt);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(loop.vs, p);
                a = a.mul(v);
                p += loop.simdLen;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getInt(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = initInt;
        IntVector a = IntVector.broadcast(loop.vs, initInt);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(v);
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getInt(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntDefault(StrideLoopDescriptor<Integer> loop, Storage storage) {
        int result = initInt;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                result *= storage.getByte(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorUnit(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = initFloat;
        FloatVector a = FloatVector.broadcast(loop.vs, initFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(loop.vs, p);
                a = a.mul(v);
                p += loop.simdLen;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getFloat(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatVectorStep(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = initFloat;
        FloatVector a = FloatVector.broadcast(loop.vs, initFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector v = storage.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(v);
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getFloat(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor<Float> loop, Storage storage) {
        float result = initFloat;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                result *= storage.getFloat(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorUnit(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = initDouble;
        DoubleVector a = DoubleVector.broadcast(loop.vs, initDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(loop.vs, p);
                a = a.mul(v);
                p += loop.simdLen;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getDouble(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleVectorStep(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = initDouble;
        DoubleVector a = DoubleVector.broadcast(loop.vs, initDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector v = storage.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(v);
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.size; i++) {
                result *= storage.getDouble(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected double reduceDoubleDefault(StrideLoopDescriptor<Double> loop, Storage storage) {
        double result = initDouble;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                result *= storage.getDouble(p);
                p += loop.step;
            }
        }
        return result;
    }
}
