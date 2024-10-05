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

import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public final class UnaryOpFloor extends TensorUnaryOp {

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
    public float applyFloat(float v) {
        return (float) StrictMath.floor(v);
    }

    @Override
    public double applyDouble(double v) {
        return StrictMath.floor(v);
    }

    @Override
    protected void applyUnitByte(LoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyStepByte(LoopDescriptor<Byte> loop, byte[] array) {
    }

    @Override
    protected void applyUnitInt(LoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyStepInt(LoopDescriptor<Integer> loop, int[] array) {
    }

    @Override
    protected void applyUnitFloat(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = (float) Math.floor(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(LoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = (float) Math.floor(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = Math.floor(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(LoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = Math.floor(array[p]);
                p += loop.step;
            }
        }
    }
}
