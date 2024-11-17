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

package rapaio.math.narray.operator.impl;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import rapaio.math.narray.iterators.StrideLoopDescriptor;
import rapaio.math.narray.operator.NArrayUnaryOp;

public final class UnaryOpAbs extends NArrayUnaryOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public double applyDouble(double v) {
        return Math.abs(v);
    }

    @Override
    public float applyFloat(float v) {
        return Math.abs(v);
    }

    @Override
    public int applyInt(int v) {
        return Math.abs(v);
    }

    @Override
    public byte applyByte(byte v) {
        return (byte) Math.abs(v);
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = ByteVector.fromArray(loop.vs, array, p);
                a = a.abs();
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
                var a = ByteVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                a = a.abs();
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
                var a = IntVector.fromArray(loop.vs, array, p);
                a = a.abs();
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
                var a = IntVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                a = a.abs();
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
                var a = FloatVector.fromArray(loop.vs, array, p);
                a = a.abs();
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
                var a = FloatVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                a = a.abs();
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
                a = a.abs();
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
                a = a.abs();
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
