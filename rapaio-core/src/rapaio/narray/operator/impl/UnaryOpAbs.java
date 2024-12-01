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

import jdk.incubator.vector.DoubleVector;
import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.operator.NArrayUnaryOp;

public final class UnaryOpAbs extends NArrayUnaryOp {

    public UnaryOpAbs() {
        super(false);
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getByteVector(loop.vs, p);
                a = a.abs();
                s.setByteVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setByte(p, (byte) Math.abs(s.getByte(p)));
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
                a = a.abs();
                s.setByteVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setByte(p, (byte) Math.abs(s.getByte(p)));
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setByte(p, (byte) Math.abs(s.getByte(p)));
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
                a = a.abs();
                s.setIntVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setInt(p, Math.abs(s.getInt(p)));
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
                a = a.abs();
                s.setIntVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setInt(p, Math.abs(s.getInt(p)));
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setInt(p, Math.abs(s.getInt(p)));
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
                a = a.abs();
                s.setFloatVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setFloat(p, Math.abs(s.getFloat(p)));
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
                a = a.abs();
                s.setFloatVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setFloat(p, Math.abs(s.getFloat(p)));
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setFloat(p, Math.abs(s.getFloat(p)));
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
                a = a.abs();
                s.setDoubleVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setDouble(p, Math.abs(s.getDouble(p)));
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
                a = a.abs();
                s.setDoubleVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setDouble(p, Math.abs(s.getDouble(p)));
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setDouble(p, Math.abs(s.getDouble(p)));
                p += loop.step;
            }
        }
    }
}
