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

package rapaio.darray.operator;

import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;

public abstract class DArrayReduceOp {

    public abstract boolean floatingPointOnly();

    public final byte reduceByte(StrideLoopDescriptor loop, Storage storage) {
        if (floatingPointOnly()) {
            throw new IllegalArgumentException("This operation is available only for floating points data types.");
        }
        if (storage.supportSimd()) {
            if (loop.step == 1) {
                return reduceByteVectorUnit(loop, storage);
            } else {
                return reduceByteVectorStep(loop, storage);
            }
        }
        return reduceByteDefault(loop, storage);
    }

    public final int reduceInt(StrideLoopDescriptor loop, Storage storage) {
        if (floatingPointOnly()) {
            throw new IllegalArgumentException("This operation is available only for floating points data types.");
        }
        if (storage.supportSimd()) {
            if (loop.step == 1) {
                return reduceIntVectorUnit(loop, storage);
            } else {
                return reduceIntVectorStep(loop, storage);
            }
        }
        return reduceIntDefault(loop, storage);
    }

    public final float reduceFloat(StrideLoopDescriptor loop, Storage storage) {
        if (storage.supportSimd()) {
            if (loop.step == 1) {
                return reduceFloatVectorUnit(loop, storage);
            } else {
                return reduceFloatVectorStep(loop, storage);
            }
        }
        return reduceFloatDefault(loop, storage);
    }

    public final double reduceDouble(StrideLoopDescriptor loop, Storage storage) {
        if (storage.supportSimd()) {
            if (loop.step == 1) {
                return reduceDoubleVectorUnit(loop, storage);
            } else {
                return reduceDoubleVectorStep(loop, storage);
            }
        }
        return reduceDoubleDefault(loop, storage);
    }


    protected abstract byte reduceByteVectorUnit(StrideLoopDescriptor loop, Storage storage);

    protected abstract byte reduceByteVectorStep(StrideLoopDescriptor loop, Storage storage);

    protected abstract byte reduceByteDefault(StrideLoopDescriptor loop, Storage storage);

    protected abstract int reduceIntVectorUnit(StrideLoopDescriptor loop, Storage storage);

    protected abstract int reduceIntVectorStep(StrideLoopDescriptor loop, Storage storage);

    protected abstract int reduceIntDefault(StrideLoopDescriptor loop, Storage storage);

    protected abstract float reduceFloatVectorUnit(StrideLoopDescriptor loop, Storage storage);

    protected abstract float reduceFloatVectorStep(StrideLoopDescriptor loop, Storage storage);

    protected abstract float reduceFloatDefault(StrideLoopDescriptor loop, Storage storage);

    protected abstract double reduceDoubleVectorUnit(StrideLoopDescriptor loop, Storage storage);

    protected abstract double reduceDoubleVectorStep(StrideLoopDescriptor loop, Storage storage);

    protected abstract double reduceDoubleDefault(StrideLoopDescriptor loop, Storage storage);
}

