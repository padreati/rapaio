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

import rapaio.math.tensor.iterators.StrideLoopDescriptor;
import rapaio.math.tensor.operator.TensorUnaryOp;

public final class UnaryOpRint extends TensorUnaryOp {

    @Override
    public boolean floatingPointOnly() {
        return false;
    }

    @Override
    public double applyDouble(double v) {
        return Math.rint(v);
    }

    @Override
    public float applyFloat(float v) {
        return (float) Math.rint(v);
    }

    @Override
    public int applyInt(int v) {
        return v;
    }

    @Override
    public byte applyByte(byte v) {
        return v;
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
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = (float) Math.rint(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepFloat(StrideLoopDescriptor<Float> loop, float[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = (float) Math.rint(array[p]);
                p += loop.step;
            }
        }
    }

    @Override
    protected void applyUnitDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = Math.rint(array[p]);
                p++;
            }
        }
    }

    @Override
    protected void applyStepDouble(StrideLoopDescriptor<Double> loop, double[] array) {
        for (int p : loop.offsets) {
            int i = 0;
            for (; i < loop.size; i++) {
                array[p] = Math.rint(array[p]);
                p += loop.step;
            }
        }
    }
}
