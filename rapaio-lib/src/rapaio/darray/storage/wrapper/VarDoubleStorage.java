/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.darray.storage.wrapper;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import rapaio.darray.DType;
import rapaio.darray.Storage;
import rapaio.data.Var;

public final class VarDoubleStorage extends Storage {

    private final Var vd;

    public VarDoubleStorage(Var vd) {
        this.vd = vd;
    }

    @Override
    public DType<Double> dt() {
        return DType.DOUBLE;
    }

    @Override
    public int size() {
        return vd.size();
    }

    @Override
    public boolean supportSimd() {
        return false;
    }

    @Override
    public byte getByte(int ptr) {
        return (byte) getDouble(ptr);
    }

    @Override
    public void setByte(int ptr, byte value) {
        setDouble(ptr, value);
    }

    @Override
    public void incByte(int ptr, byte value) {
        incDouble(ptr, value);
    }

    @Override
    public void fill(byte value, int start, int len) {
        fill((double)value, start, len);
    }

    @Override
    public int getInt(int ptr) {
        return (int) getDouble(ptr);
    }

    @Override
    public void setInt(int ptr, int value) {
        setDouble(ptr, value);
    }

    @Override
    public void incInt(int ptr, int value) {
        incDouble(ptr, value);
    }

    @Override
    public void fill(int value, int start, int len) {
        fill((double)value, start, len);
    }

    @Override
    public float getFloat(int ptr) {
        return (float) getDouble(ptr);
    }

    @Override
    public void setFloat(int ptr, float value) {
        setDouble(ptr, value);
    }

    @Override
    public void incFloat(int ptr, float value) {
        incDouble(ptr, value);
    }

    @Override
    public void fill(float value, int start, int len) {
        fill((double)value, start, len);
    }

    @Override
    public double getDouble(int ptr) {
        checkPointer(ptr);
        return vd.getDouble(ptr);
    }

    @Override
    public void setDouble(int ptr, double value) {
        checkPointer(ptr);
        vd.setDouble(ptr, value);
    }

    @Override
    public void incDouble(int ptr, double value) {
        checkPointer(ptr);
        vd.setDouble(ptr, vd.getDouble(ptr) + value);
    }

    @Override
    public void fill(double value, int start, int len) {
        checkRange(start, len);
        for (int i = start; i < start + len; i++) {
            vd.setDouble(i, value);
        }
    }

    /**
     * The wrapped variable indexes its own array without checking the row against its size, and that array is sized by
     * the variable's growth capacity rather than by its size. A pointer between the size and the capacity would
     * therefore read stale slack, or write into slack which the next value added to the variable overwrites, where an
     * array backed storage throws for the same pointer.
     *
     * @param ptr storage position
     * @throws IndexOutOfBoundsException if the position is outside this storage
     */
    private void checkPointer(int ptr) {
        if (ptr < 0 || ptr >= vd.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Storage position %d is outside the storage of size %d.", ptr, vd.size()));
        }
    }

    /**
     * Validates a range the same way {@link java.util.Arrays#fill(double[], int, int, double)} does for the array
     * backed storages, so that every storage rejects the same arguments.
     *
     * @param start first position of the range
     * @param len   length of the range
     * @throws IllegalArgumentException   if the length is negative
     * @throws IndexOutOfBoundsException if the range is not inside this storage
     */
    private void checkRange(int start, int len) {
        if (len < 0) {
            throw new IllegalArgumentException("Length must be positive, but it is " + len + ".");
        }
        if (start < 0 || start + len > vd.size()) {
            throw new IndexOutOfBoundsException(String.format(
                    "Range [%d, %d) is outside the storage of size %d.", start, start + len, vd.size()));
        }
    }

    @Override
    public ByteVector getByteVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteVector getByteVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteVector(ByteVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteVector getByteVector(int offset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteVector getByteVector(int offset, int[] idx, int idxOffset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteVector(ByteVector value, int offset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset, VectorMask<Byte> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntVector getIntVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntVector getIntVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntVector(IntVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntVector(IntVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntVector getIntVector(int offset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntVector getIntVector(int offset, int[] idx, int idxOffset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntVector(IntVector value, int offset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntVector(IntVector value, int offset, int[] idx, int idxOffset, VectorMask<Integer> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatVector getFloatVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatVector getFloatVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatVector(FloatVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatVector(FloatVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatVector getFloatVector(int offset, VectorMask<Float> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatVector getFloatVector(int offset, int[] idx, int idxOffset, VectorMask<Float> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatVector(FloatVector value, int offset, VectorMask<Float> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFloatVector(FloatVector value, int offset, int[] idx, int idxOffset, VectorMask<Float> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleVector getDoubleVector(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleVector getDoubleVector(int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleVector(DoubleVector value, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleVector(DoubleVector value, int offset, int[] idx, int idxOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleVector getDoubleVector(int offset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DoubleVector getDoubleVector(int offset, int[] idx, int idxOffset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleVector(DoubleVector value, int offset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDoubleVector(DoubleVector value, int offset, int[] idx, int idxOffset, VectorMask<Double> m) {
        throw new UnsupportedOperationException();
    }
}
