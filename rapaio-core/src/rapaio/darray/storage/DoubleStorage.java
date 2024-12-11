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

package rapaio.darray.storage;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import rapaio.darray.DType;
import rapaio.darray.Storage;

public abstract class DoubleStorage extends Storage {

    @Override
    public final DType<Double> dtype() {
        return DType.DOUBLE;
    }

    public final byte getByte(int ptr) {
        return (byte) getDouble(ptr);
    }

    @Override
    public final void setByte(int ptr, byte value) {
        setDouble(ptr, value);
    }

    @Override
    public final void incByte(int ptr, byte value) {
        incDouble(ptr, value);
    }

    @Override
    public final void fill(byte value, int start, int len) {
        fill((double) value, start, len);
    }


    @Override
    public final int getInt(int ptr) {
        return (int) getDouble(ptr);
    }

    @Override
    public final void setInt(int ptr, int value) {
        setDouble(ptr, value);
    }

    @Override
    public final void incInt(int ptr, int value) {
        incDouble(ptr, value);
    }

    @Override
    public final void fill(int value, int start, int len) {
        fill((double) value, start, len);
    }


    @Override
    public final float getFloat(int ptr) {
        return (float) getDouble(ptr);
    }

    @Override
    public final void setFloat(int ptr, float value) {
        setDouble(ptr, value);
    }

    @Override
    public final void incFloat(int ptr, float value) {
        incDouble(ptr, value);
    }

    @Override
    public final void fill(float value, int start, int len) {
        fill((double) value, start, len);
    }


    @Override
    public final ByteVector getByteVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ByteVector getByteVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setByteVector(ByteVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final IntVector getIntVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final IntVector getIntVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setIntVector(IntVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setIntVector(IntVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final FloatVector getFloatVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final FloatVector getFloatVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setFloatVector(FloatVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setFloatVector(FloatVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }
}
