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
import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public class UnaryOpFill<N extends Number> extends TensorUnaryOp {

    private final byte fillByte;
    private final int fillInt;
    private final float fillFloat;
    private final double fillDouble;

    public UnaryOpFill(N fill) {
        fillByte = fill.byteValue();
        fillInt = fill.intValue();
        fillFloat = fill.floatValue();
        fillDouble = fill.doubleValue();
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        return fillByte;
    }

    @Override
    public int applyInt(int v) {
        return fillInt;
    }

    @Override
    public double applyDouble(double v) {
        return fillDouble;
    }

    @Override
    public float applyFloat(float v) {
        return fillFloat;
    }

    @Override
    protected void applyUnitByte(LoopDescriptor<Byte> loop, byte[] array) {
        var a = ByteVector.broadcast(loop.vs, fillByte);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillByte;
                p++;
            }
        }
    }

    @Override
    protected void applyStepByte(LoopDescriptor<Byte> loop, byte[] array) {
        var a = ByteVector.broadcast(loop.vs, fillByte);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p, loop.simdOffsets, 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillByte;
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitInt(LoopDescriptor<Integer> loop, int[] array) {
        var a = IntVector.broadcast(loop.vs, fillInt);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillInt;
                p++;
            }
        }
    }

    @Override
    protected void applyStepInt(LoopDescriptor<Integer> loop, int[] array) {
        var a = IntVector.broadcast(loop.vs, fillInt);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p, loop.simdOffsets, 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillInt;
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitFloat(LoopDescriptor<Float> loop, float[] array) {
        var a = FloatVector.broadcast(loop.vs, fillFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillFloat;
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(LoopDescriptor<Float> loop, float[] array) {
        var a = FloatVector.broadcast(loop.vs, fillFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p, loop.simdOffsets, 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillFloat;
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(LoopDescriptor<Double> loop, double[] array) {
        var a = DoubleVector.broadcast(loop.vs, fillDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillDouble;
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(LoopDescriptor<Double> loop, double[] array) {
        var a = DoubleVector.broadcast(loop.vs, fillDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                a.intoArray(array, p, loop.simdOffsets, 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = fillDouble;
                p += loop.step;
            }
        }
    }
}
