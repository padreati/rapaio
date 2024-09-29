/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.operator;

import rapaio.data.OperationNotAvailableException;
import rapaio.math.tensor.iterators.LoopDescriptor;

public abstract class TensorUnaryOp {

    /**
     * @return true if vector operations are implemented, false otherwise
     */
    public abstract boolean vectorSupport();

    public abstract boolean floatingPointOnly();

    public abstract byte applyByte(byte v);

    public abstract int applyInt(int v);

    public abstract double applyDouble(double v);

    public abstract float applyFloat(float v);

    public final void apply(LoopDescriptor<Byte> loop, byte[] array) {
        if(floatingPointOnly()) {
            throw new OperationNotAvailableException();
        }
        if (loop.step == 1) {
            applyUnit(loop, array);
        } else {
            applyStep(loop, array);
        }
    }

    protected abstract void applyUnit(LoopDescriptor<Byte> loop, byte[] array);

    protected abstract void applyStep(LoopDescriptor<Byte> loop, byte[] array);

    public final void apply(LoopDescriptor<Integer> loop, int[] array) {
        if(floatingPointOnly()) {
            throw new OperationNotAvailableException();
        }
        if (loop.step == 1) {
            applyUnit(loop, array);
        } else {
            applyStep(loop, array);
        }
    }

    protected abstract void applyUnit(LoopDescriptor<Integer> loop, int[] array);

    protected abstract void applyStep(LoopDescriptor<Integer> loop, int[] array);

    public final void apply(LoopDescriptor<Float> loop, float[] array) {
        if (loop.step == 1) {
            applyUnit(loop, array);
        } else {
            applyStep(loop, array);
        }
    }

    protected abstract void applyUnit(LoopDescriptor<Float> loop, float[] array);

    protected abstract void applyStep(LoopDescriptor<Float> loop, float[] array);

    public final void apply(LoopDescriptor<Double> loop, double[] array) {
        if (loop.step == 1) {
            applyUnit(loop, array);
        } else {
            applyStep(loop, array);
        }
    }

    protected abstract void applyUnit(LoopDescriptor<Double> loop, double[] array);

    protected abstract void applyStep(LoopDescriptor<Double> loop, double[] array);
}

