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
import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.operator.NArrayUnaryOp;

public class UnaryOpFill<N extends Number> extends NArrayUnaryOp {

    private final N fill;

    public UnaryOpFill(N fill) {
        super(false);
        this.fill = fill;
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        var a = ByteVector.broadcast(loop.vs, fill.byteValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setByteVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setByte(p, fill.byteValue());
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        var a = ByteVector.broadcast(loop.vs, fill.byteValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setByteVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setByte(p, fill.byteValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setByte(p, fill.byteValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        var a = IntVector.broadcast(loop.vs, fill.intValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setIntVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setInt(p, fill.intValue());
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        var a = IntVector.broadcast(loop.vs, fill.intValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setIntVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setInt(p, fill.intValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setInt(p, fill.intValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        var a = FloatVector.broadcast(loop.vs, fill.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setFloatVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setFloat(p, fill.floatValue());
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        var a = FloatVector.broadcast(loop.vs, fill.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setFloatVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setFloat(p, fill.floatValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setFloat(p, fill.floatValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        var a = DoubleVector.broadcast(loop.vs, fill.doubleValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setDoubleVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setDouble(p, fill.doubleValue());
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        var a = DoubleVector.broadcast(loop.vs, fill.doubleValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                s.setDoubleVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                s.setDouble(p, fill.doubleValue());
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                s.setDouble(p, fill.doubleValue());
                p += loop.step;
            }
        }
    }
}
