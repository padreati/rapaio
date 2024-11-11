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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import rapaio.math.tensor.iterators.StrideLoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public class UnaryOpFillNan<N extends Number> extends TensorUnaryOp {

    private final float fillFloat;
    private final double fillDouble;

    public UnaryOpFillNan(N fill) {
        fillFloat = fill.floatValue();
        fillDouble = fill.doubleValue();
    }

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public byte applyByte(byte v) {
        return v;
    }

    @Override
    public int applyInt(int v) {
        return v;
    }

    @Override
    public double applyDouble(double v) {
        return Double.isNaN(v) ? fillDouble : v;
    }

    @Override
    public float applyFloat(float v) {
        return Float.isNaN(v) ? fillFloat : v;
    }

    @Override
    protected void applyUnitByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyStepByte(StrideLoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyUnitInt(StrideLoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyStepInt(StrideLoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyUnitFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        var a = FloatVector.broadcast(loop.vs, fillFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = FloatVector.fromArray(loop.vs, array, p);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                b.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = Float.isNaN(array[p]) ? fillFloat : array[p];
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        var a = FloatVector.broadcast(loop.vs, fillFloat);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                FloatVector b = FloatVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                b.intoArray(array, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = Float.isNaN(array[p]) ? fillFloat : array[p];
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        var a = DoubleVector.broadcast(loop.vs, fillDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = DoubleVector.fromArray(loop.vs, array, p);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                b.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = Double.isNaN(array[p]) ? fillDouble : array[p];
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        var a = DoubleVector.broadcast(loop.vs, fillDouble);
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector b = DoubleVector.fromArray(loop.vs, array, p, loop.simdOffsets(), 0);
                b = b.blend(a, b.test(VectorOperators.IS_NAN));
                b.intoArray(array, p, loop.simdOffsets(), 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = Double.isNaN(array[p]) ? fillDouble : array[p];
                p += loop.step;
            }
        }
    }
}
