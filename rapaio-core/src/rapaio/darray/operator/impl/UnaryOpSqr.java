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
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.operator.DArrayUnaryOp;

public final class UnaryOpSqr extends DArrayUnaryOp {

    public UnaryOpSqr() {
        super(false);
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getByteVector(loop.vs, p);
                a = a.mul(a);
                s.setByteVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                byte value = s.getByte(p);
                s.setByte(p, (byte) (value * value));
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getByteVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(a);
                s.setByteVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                byte value = s.getByte(p);
                s.setByte(p, (byte) (value * value));
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                byte value = s.getByte(p);
                s.setByte(p, (byte) (value * value));
                p += loop.step;
            }
        }
    }


    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getIntVector(loop.vs, p);
                a = a.mul(a);
                s.setIntVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                int value = s.getInt(p);
                s.setInt(p, value * value);
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getIntVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(a);
                s.setIntVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                int value = s.getInt(p);
                s.setInt(p, value * value);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int value = s.getInt(p);
                s.setInt(p, value * value);
                p++;
            }
        }
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getFloatVector(loop.vs, p);
                a = a.mul(a);
                s.setFloatVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                float value = s.getFloat(p);
                s.setFloat(p, value * value);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(a);
                s.setFloatVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                float value = s.getFloat(p);
                s.setFloat(p, value * value);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                float value = s.getFloat(p);
                s.setFloat(p, value * value);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = s.getDoubleVector(loop.vs, p);
                a = a.mul(a);
                s.setDoubleVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                double value = s.getDouble(p);
                s.setDouble(p, value * value);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = s.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                a = a.mul(a);
                s.setDoubleVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                double value = s.getDouble(p);
                s.setDouble(p, value * value);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double value = s.getDouble(p);
                s.setDouble(p, value * value);
                p += loop.step;
            }
        }
    }
}
