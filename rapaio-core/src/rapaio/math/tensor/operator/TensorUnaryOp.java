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

package rapaio.math.tensor.operator;

import rapaio.data.OperationNotAvailableException;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.iterators.LoopDescriptor;
import rapaio.math.tensor.storage.array.ByteArrayStorage;
import rapaio.math.tensor.storage.array.DoubleArrayStorage;
import rapaio.math.tensor.storage.array.FloatArrayStorage;
import rapaio.math.tensor.storage.array.IntArrayStorage;

public abstract class TensorUnaryOp {

    public abstract boolean floatingPointOnly();

    public abstract byte applyByte(byte v);

    public abstract int applyInt(int v);

    public abstract double applyDouble(double v);

    public abstract float applyFloat(float v);

    public final void applyByte(LoopDescriptor<Byte> loop, Storage<Byte> storage) {
        if (floatingPointOnly()) {
            throw new OperationNotAvailableException();
        }
        if (storage instanceof ByteArrayStorage as) {
            if (loop.step == 1) {
                applyUnitByte(loop, as.array());
            } else {
                applyStepByte(loop, as.array());
            }
        } else {
            applyGenericByte(loop, storage);
        }
    }

    private void applyGenericByte(LoopDescriptor<Byte> loop, Storage<Byte> storage) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                storage.setByte(p, applyByte(storage.getByte(p)));
                p += loop.step;
            }
        }
    }

    protected abstract void applyUnitByte(LoopDescriptor<Byte> loop, byte[] array);

    protected abstract void applyStepByte(LoopDescriptor<Byte> loop, byte[] array);

    public final void applyInt(LoopDescriptor<Integer> loop, Storage<Integer> storage) {
        if (floatingPointOnly()) {
            throw new OperationNotAvailableException();
        }
        if (storage instanceof IntArrayStorage as) {
            if (loop.step == 1) {
                applyUnitInt(loop, as.array());
            } else {
                applyStepInt(loop, as.array());
            }
        } else {
            applyGenericInt(loop, storage);
        }
    }

    private void applyGenericInt(LoopDescriptor<Integer> loop, Storage<Integer> storage) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                storage.setInt(p, applyInt(storage.getInt(p)));
                p += loop.step;
            }
        }
    }

    protected abstract void applyUnitInt(LoopDescriptor<Integer> loop, int[] array);

    protected abstract void applyStepInt(LoopDescriptor<Integer> loop, int[] array);

    public final void applyFloat(LoopDescriptor<Float> loop, Storage<Float> storage) {
        if (storage instanceof FloatArrayStorage as) {
            if (loop.step == 1) {
                applyUnitFloat(loop, as.array());
            } else {
                applyStepFloat(loop, as.array());
            }
        } else {
            applyGenericFloat(loop, storage);
        }
    }

    private void applyGenericFloat(LoopDescriptor<Float> loop, Storage<Float> storage) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                storage.setFloat(p, applyFloat(storage.getFloat(p)));
                p += loop.step;
            }
        }
    }

    protected abstract void applyUnitFloat(LoopDescriptor<Float> loop, float[] array);

    protected abstract void applyStepFloat(LoopDescriptor<Float> loop, float[] array);

    public final void applyDouble(LoopDescriptor<Double> loop, Storage<Double> storage) {
        if (storage instanceof DoubleArrayStorage as) {
            if (loop.step == 1) {
                applyUnitDouble(loop, as.array());
            } else {
                applyStepDouble(loop, as.array());
            }
        } else {
            applyGenericDouble(loop, storage);
        }
    }

    private void applyGenericDouble(LoopDescriptor<Double> loop, Storage<Double> storage) {
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                storage.setDouble(p, applyDouble(storage.getDouble(p)));
                p += loop.step;
            }
        }
    }

    protected abstract void applyUnitDouble(LoopDescriptor<Double> loop, double[] array);

    protected abstract void applyStepDouble(LoopDescriptor<Double> loop, double[] array);
}

