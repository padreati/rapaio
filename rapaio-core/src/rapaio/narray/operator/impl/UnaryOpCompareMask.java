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
import jdk.incubator.vector.VectorMask;
import rapaio.narray.Compare;
import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.operator.NArrayUnaryOp;

public class UnaryOpCompareMask<N extends Number> extends NArrayUnaryOp {

    private final Compare compare;
    private final N value;

    public UnaryOpCompareMask(Compare compare, N value) {
        super(false);
        this.compare = compare;
        this.value = value;
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector a = s.getByteVector(loop.vs, p);
                VectorMask<Byte> mask = a.compare(compare.vectorComparison(), value.byteValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setByteVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                byte v = s.getByte(p);
                s.setByte(p, compare.compareByte(v, value.byteValue()) ? (byte) 1 : (byte) 0);
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector a = s.getByteVector(loop.vs, p, loop.simdOffsets(), 0);
                VectorMask<Byte> mask = a.compare(compare.vectorComparison(), value.byteValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setByteVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                byte v = s.getByte(p);
                s.setByte(p, compare.compareByte(v, value.byteValue()) ? (byte) 1 : (byte) 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericByte(StrideLoopDescriptor<Byte> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                byte v = s.getByte(p);
                s.setByte(p, compare.compareByte(v, value.byteValue()) ? (byte) 1 : (byte) 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector a = s.getIntVector(loop.vs, p);
                VectorMask<Integer> mask = a.compare(compare.vectorComparison(), value.intValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setIntVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                int v = s.getInt(p);
                s.setInt(p, compare.compareInt(v, value.intValue()) ? 1 : 0);
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector a = s.getIntVector(loop.vs, p, loop.simdOffsets(), 0);
                VectorMask<Integer> mask = a.compare(compare.vectorComparison(), value.intValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setIntVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                int v = s.getInt(p);
                s.setInt(p, compare.compareInt(v, value.intValue()) ? 1 : 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericInt(StrideLoopDescriptor<Integer> loop, Storage s) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int v = s.getInt(p);
                s.setInt(p, compare.compareInt(v, value.intValue()) ? 1 : 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float ref = value.floatValue();
        FloatVector vref = FloatVector.broadcast(loop.vs, value.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = s.getFloatVector(loop.vs, p);
                var mask = a.compare(compare.vectorComparison(), vref);
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setFloatVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                float v = s.getFloat(p);
                s.setFloat(p, compare.compareFloat(v, ref) ? 1 : 0);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float ref = value.floatValue();
        FloatVector vref = FloatVector.broadcast(loop.vs, value.floatValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = s.getFloatVector(loop.vs, p, loop.simdOffsets(), 0);
                VectorMask<Float> mask = a.compare(compare.vectorComparison(), vref);
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setFloatVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                float v = s.getFloat(p);
                s.setFloat(p, compare.compareFloat(v, ref) ? 1 : 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericFloat(StrideLoopDescriptor<Float> loop, Storage s) {
        float ref = value.floatValue();
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                float v = s.getFloat(p);
                s.setFloat(p, compare.compareFloat(v, ref) ? 1 : 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double ref = value.floatValue();
        DoubleVector vref = DoubleVector.broadcast(loop.vs, value.doubleValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = s.getDoubleVector(loop.vs, p);
                VectorMask<Double> mask = a.compare(compare.vectorComparison(), vref);
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setDoubleVector(a, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                double v = s.getDouble(p);
                s.setDouble(p, compare.compareDouble(v, ref) ? 1 : 0);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double ref = value.floatValue();
        DoubleVector vref = DoubleVector.broadcast(loop.vs, value.doubleValue());
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = s.getDoubleVector(loop.vs, p, loop.simdOffsets(), 0);
                VectorMask<Double> mask = a.compare(compare.vectorComparison(), vref);
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                s.setDoubleVector(a, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                double v = s.getDouble(p);
                s.setDouble(p, compare.compareDouble(v, ref) ? 1 : 0);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyGenericDouble(StrideLoopDescriptor<Double> loop, Storage s) {
        double ref = value.floatValue();
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                double v = s.getDouble(p);
                s.setDouble(p, compare.compareDouble(v, ref) ? 1 : 0);
                p += loop.step;
            }
        }
    }
}
