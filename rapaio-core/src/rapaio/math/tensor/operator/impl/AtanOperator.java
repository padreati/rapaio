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
import rapaio.data.OperationNotAvailableException;
import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public final class AtanOperator extends TensorUnaryOp {

    @Override
    public boolean floatingPointOnly() {
        return true;
    }

    @Override
    public byte applyByte(byte v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int applyInt(int v) {
        throw new OperationNotAvailableException();
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.atan(v);
    }

    @Override
    public double applyDouble(double v) {
        return Math.atan(v);
    }

    @Override
    protected void applyUnit(LoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyStep(LoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyUnit(LoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyStep(LoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyUnit(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = FloatVector.fromArray(loop.vs, array, p);
                a = a.lanewise(VectorOperators.ATAN);
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = (float) Math.atan(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStep(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                var a = FloatVector.fromArray(loop.vs, array, p, loop.simdOffsets, 0);
                a = a.lanewise(VectorOperators.ATAN);
                a.intoArray(array, p, loop.simdOffsets, 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = (float) Math.atan(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnit(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = DoubleVector.fromArray(loop.vs, array, p);
                a = a.lanewise(VectorOperators.ATAN);
                a.intoArray(array, p);
                p += loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = Math.atan(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStep(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.simdBound; i += loop.simdLen) {
                DoubleVector a = DoubleVector.fromArray(loop.vs, array, p, loop.simdOffsets, 0);
                a = a.lanewise(VectorOperators.ATAN);
                a.intoArray(array, p, loop.simdOffsets, 0);
                p += loop.step * loop.simdLen;
            }
            for (; i < loop.size; i++) {
                array[p] = Math.atan(array[p]);
                p += loop.step;
            }
        }
    }
}
