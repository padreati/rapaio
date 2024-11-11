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

package rapaio.math.tensor.operator.impl;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import rapaio.math.tensor.iterators.StrideLoopDescriptor;
import rapaio.math.tensor.operator.Compare;
import rapaio.math.tensor.operator.TensorUnaryOp;

public class UnaryOpCompareMask<N extends Number> extends TensorUnaryOp {

    private final Compare compare;
    private final N value;

    public UnaryOpCompareMask(Compare compare, N value) {
        this.compare = compare;
        this.value = value;
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        return compare.compareByte(v, value.byteValue()) ? (byte) 1 : (byte) 0;
    }

    @Override
    public int applyInt(int v) {
        return compare.compareInt(v, value.intValue()) ? 1 : 0;
    }

    @Override
    public float applyFloat(float v) {
        return compare.compareFloat(v, value.floatValue()) ? 1f : 0f;
    }

    @Override
    public double applyDouble(double v) {
        return compare.compareDouble(v, value.doubleValue()) ? 1d : 0d;
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector a = ByteVector.fromArray(loop.vs, array, p);
                VectorMask<Byte> mask = a.compare(compare.vectorComparison(), value.byteValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyByte(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                ByteVector a = ByteVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                VectorMask<Byte> mask = a.compare(compare.vectorComparison(), value.byteValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyByte(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, int[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector a = IntVector.fromArray(loop.vs, array, p);
                VectorMask<Integer> mask = a.compare(compare.vectorComparison(), value.intValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyInt(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, int[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                IntVector a = IntVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                VectorMask<Integer> mask = a.compare(compare.vectorComparison(), value.intValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyInt(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = FloatVector.fromArray(loop.vs, array, p);
                VectorMask<Float> mask = a.compare(compare.vectorComparison(), value.floatValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyFloat(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector a = FloatVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                VectorMask<Float> mask = a.compare(compare.vectorComparison(), value.floatValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyFloat(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = DoubleVector.fromArray(loop.vs, array, p);
                VectorMask<Double> mask = a.compare(compare.vectorComparison(), value.doubleValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyDouble(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = DoubleVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                VectorMask<Double> mask = a.compare(compare.vectorComparison(), value.doubleValue());
                a = a.blend(1, mask);
                a = a.blend(0, mask.not());
                a.intoArray(array, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = applyDouble(array[p]);
                p += loop.step;
            }
        }
    }
}
