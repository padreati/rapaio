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

package rapaio.narray.operator;

import rapaio.narray.Storage;
import rapaio.narray.iterators.StrideLoopDescriptor;

public abstract class NArrayReduceOp {

    public abstract boolean floatingPointOnly();

    public final byte reduceByte(StrideLoopDescriptor<Byte> loop, Storage storage) {
        if (floatingPointOnly()) {
            throw new IllegalArgumentException("This operation is available only for floating points data types.");
        }
        if (storage.supportVectorization()) {
            if (loop.step == 1) {
                return reduceByteVectorUnit(loop, storage);
            } else {
                return reduceByteVectorStep(loop, storage);
            }
        }
        return reduceByteDefault(loop, storage);
    }

    public final int reduceInt(StrideLoopDescriptor<Integer> loop, Storage storage) {
        if (floatingPointOnly()) {
            throw new IllegalArgumentException("This operation is available only for floating points data types.");
        }
        if (storage.supportVectorization()) {
            if (loop.step == 1) {
                return reduceIntVectorUnit(loop, storage);
            } else {
                return reduceIntVectorStep(loop, storage);
            }
        }
        return reduceIntDefault(loop, storage);
    }

    public final float reduceFloat(StrideLoopDescriptor<Float> loop, Storage storage) {
        if (storage.supportVectorization()) {
            if (loop.step == 1) {
                return reduceFloatVectorUnit(loop, storage);
            } else {
                return reduceFloatVectorStep(loop, storage);
            }
        }
        return reduceFloatDefault(loop, storage);
    }

    public final double reduceDouble(StrideLoopDescriptor<Double> loop, Storage storage) {
        if (storage.supportVectorization()) {
            if (loop.step == 1) {
                return reduceDoubleVectorUnit(loop, storage);
            } else {
                return reduceDoubleVectorStep(loop, storage);
            }
        }
        return reduceDoubleDefault(loop, storage);
    }


    protected abstract byte reduceByteVectorUnit(StrideLoopDescriptor<Byte> loop, Storage storage);

    protected abstract byte reduceByteVectorStep(StrideLoopDescriptor<Byte> loop, Storage storage);

    protected abstract byte reduceByteDefault(StrideLoopDescriptor<Byte> loop, Storage storage);

    protected abstract int reduceIntVectorUnit(StrideLoopDescriptor<Integer> loop, Storage storage);

    protected abstract int reduceIntVectorStep(StrideLoopDescriptor<Integer> loop, Storage storage);

    protected abstract int reduceIntDefault(StrideLoopDescriptor<Integer> loop, Storage storage);

    protected abstract float reduceFloatVectorUnit(StrideLoopDescriptor<Float> loop, Storage storage);

    protected abstract float reduceFloatVectorStep(StrideLoopDescriptor<Float> loop, Storage storage);

    protected abstract float reduceFloatDefault(StrideLoopDescriptor<Float> loop, Storage storage);

    protected abstract double reduceDoubleVectorUnit(StrideLoopDescriptor<Double> loop, Storage storage);

    protected abstract double reduceDoubleVectorStep(StrideLoopDescriptor<Double> loop, Storage storage);

    protected abstract double reduceDoubleDefault(StrideLoopDescriptor<Double> loop, Storage storage);
}

