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

import rapaio.data.OperationNotAvailableException;
import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public class UnaryOpPow<N extends Number> extends TensorUnaryOp {
    private final double power;

    public UnaryOpPow(double power) {
        this.power = power;
    }

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
        return (float) Math.pow(v, power);
    }

    @Override
    public double applyDouble(double v) {
        return Math.pow(v, power);
    }

    @Override
    protected void applyUnitByte(LoopDescriptor<Byte> loop, byte[] array) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyStepByte(LoopDescriptor<Byte> loop, byte[] array) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyUnitInt(LoopDescriptor<Integer> loop, int[] array) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyStepInt(LoopDescriptor<Integer> loop, int[] array) {
        throw new OperationNotAvailableException();
    }

    @Override
    protected void applyUnitFloat(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = (float) Math.pow(array[p], power);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = (float) Math.pow(array[p], power);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = Math.pow(array[p], power);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = Math.pow(array[p], power);
                p += loop.step;
            }
        }
    }
}
