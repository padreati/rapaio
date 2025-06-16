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
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayReduceOp;

public final class ReduceOpNanProd extends DArrayReduceOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    private static final byte initByte = 1;
    private static final int initInt = 1;
    private static final float initFloat = 1;
    private static final double initDouble = 1;

    @Override
    protected byte reduceByteVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        byte result = initByte;
        ByteVector a = Simd.broadcast(initByte);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p);
                a = a.mul(v);
                p += loop.simdLen;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.bound; i++) {
                result *= storage.getByte(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected byte reduceByteVectorStep(StrideLoopDescriptor loop, Storage storage) {
        byte result = initByte;
        ByteVector a = Simd.broadcast(initByte);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector v = storage.getByteVector(p, loop.simdIdx(), 0);
                a = a.mul(v);
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.bound; i++) {
                result *= storage.getByte(p);
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
                result *= storage.getByte(p);
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorUnit(StrideLoopDescriptor loop, Storage storage) {
        int result = initInt;
        IntVector a = Simd.broadcast(initInt);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p);
                a = a.mul(v);
                p += loop.simdLen;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.bound; i++) {
                result *= storage.getInt(p);
                p++;
            }
        }
        return result;
    }

    @Override
    protected int reduceIntVectorStep(StrideLoopDescriptor loop, Storage storage) {
        int result = initInt;
        IntVector a = Simd.broadcast(initInt);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector v = storage.getIntVector(p, loop.simdIdx(), 0);
                a = a.mul(v);
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.bound; i++) {
                result *= storage.getInt(p);
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
                result *= storage.getByte(p);
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
                VectorMask<Float> m = v.test(VectorOperators.IS_NAN);
                a = a.mul(v, m.not());
                p += loop.simdLen;
            }
            VectorMask<Float> m = a.test(VectorOperators.IS_NAN);
            result *= a.reduceLanes(VectorOperators.MUL, m.not());
            for (; i < loop.bound; i++) {
                float value = storage.getFloat(p);
                if (!Float.isNaN(value)) {
                    result *= value;
                }
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
                VectorMask<Float> m = v.test(VectorOperators.IS_NAN);
                a = a.mul(v, m.not());
                p += loop.simdLen * loop.step;
            }
            VectorMask<Float> m = a.test(VectorOperators.IS_NAN);
            result *= a.reduceLanes(VectorOperators.MUL, m.not());
            for (; i < loop.bound; i++) {
                float value = storage.getFloat(p);
                if (!Float.isNaN(value)) {
                    result *= value;
                }
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    protected float reduceFloatDefault(StrideLoopDescriptor loop, Storage storage) {
        float result = initFloat;
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                float value = storage.getFloat(p);
                if (!Float.isNaN(value)) {
                    result *= value;
                }
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
                VectorMask<Double> m = v.test(VectorOperators.IS_NAN);
                a = a.mul(v, m.not());
                p += loop.simdLen;
            }
            VectorMask<Double> m = a.test(VectorOperators.IS_NAN);
            result *= a.reduceLanes(VectorOperators.MUL, m.not());
            for (; i < loop.bound; i++) {
                float value = storage.getFloat(p);
                if (!Float.isNaN(value)) {
                    result *= value;
                }
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
                VectorMask<Double> m = v.test(VectorOperators.IS_NAN);
                a = a.mul(v, m.not());
                p += loop.simdLen * loop.step;
            }
            result *= a.reduceLanes(VectorOperators.MUL);
            for (; i < loop.bound; i++) {
                double value = storage.getDouble(p);
                if (!Double.isNaN(value)) {
                    result *= value;
                }
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
                double value = storage.getDouble(p);
                if (!Double.isNaN(value)) {
                    result *= value;
                }
                p += loop.step;
            }
        }
        return result;
    }
}
