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
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import rapaio.darray.DType;
import rapaio.darray.Storage;

public abstract class FloatStorage extends Storage {

    @Override
    public final DType<Float> dt() {
        return DType.FLOAT;
    }

    @Override
    public final byte getByte(int ptr) {
        return (byte) getFloat(ptr);
    }

    @Override
    public final void setByte(int ptr, byte value) {
        setFloat(ptr, value);
    }

    @Override
    public final void incByte(int ptr, byte value) {
        incFloat(ptr, value);
    }

    @Override
    public final void fill(byte value, int start, int len) {
        fill((double) value, start, len);
    }


    @Override
    public final int getInt(int ptr) {
        return (int) getFloat(ptr);
    }

    @Override
    public final void setInt(int ptr, int value) {
        setFloat(ptr, value);
    }

    @Override
    public final void incInt(int ptr, int value) {
        incFloat(ptr, value);
    }

    @Override
    public final void fill(int value, int start, int len) {
        fill((double) value, start, len);
    }


    @Override
    public final double getDouble(int ptr) {
        return getFloat(ptr);
    }

    @Override
    public final void setDouble(int ptr, double value) {
        setFloat(ptr, (float) value);
    }

    @Override
    public final void incDouble(int ptr, double value) {
        incFloat(ptr, (float) value);
    }

    @Override
    public final void fill(double value, int start, int len) {
        fill((float) value, start, len);
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
    public final ByteVector getByteVector(int offset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ByteVector getByteVector(int offset, int[] idx, int idxOffset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setByteVector(ByteVector value, int offset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset, VectorMask<Byte> m) {
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
    public final IntVector getIntVector(int offset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final IntVector getIntVector(int offset, int[] idx, int idxOffset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setIntVector(IntVector value, int offset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setIntVector(IntVector value, int offset, int[] idx, int idxOffset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DoubleVector getDoubleVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DoubleVector getDoubleVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDoubleVector(DoubleVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDoubleVector(DoubleVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DoubleVector getDoubleVector(int offset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DoubleVector getDoubleVector(int offset, int[] idx, int idxOffset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDoubleVector(DoubleVector value, int offset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDoubleVector(DoubleVector value, int offset, int[] idx, int idxOffset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }
}
