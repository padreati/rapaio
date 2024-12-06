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
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.DType;
import rapaio.darray.Storage;

public abstract class IntStorage extends Storage {

    @Override
    public final DType<Integer> dtype() {
        return DType.INTEGER;
    }

    @Override
    public final byte getByte(int ptr) {
        return (byte) getInt(ptr);
    }

    @Override
    public final void setByte(int ptr, byte value) {
        setInt(ptr, value);
    }

    @Override
    public final void incByte(int ptr, byte value) {
        incInt(ptr, value);
    }

    @Override
    public final void fill(byte value, int start, int len) {
        fill((int) value, start, len);
    }

    @Override
    public final float getFloat(int ptr) {
        return getInt(ptr);
    }

    @Override
    public final void setFloat(int ptr, float value) {
        setInt(ptr, (int) value);
    }

    @Override
    public final void incFloat(int ptr, float value) {
        incInt(ptr, (int) value);
    }

    @Override
    public final void fill(float value, int start, int len) {
        fill((int) value, start, len);
    }


    @Override
    public final double getDouble(int ptr) {
        return getInt(ptr);
    }

    @Override
    public final void setDouble(int ptr, double value) {
        setInt(ptr, (int) value);
    }

    @Override
    public final void incDouble(int ptr, double value) {
        incInt(ptr, (int) value);
    }

    @Override
    public final void fill(double value, int start, int len) {
        fill((int) value, start, len);
    }


    @Override
    public final ByteVector getByteVector(VectorSpecies<Byte> vs, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final ByteVector getByteVector(VectorSpecies<Byte> vs, int offset, int[] idx, int idxOffset) {
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
    public final FloatVector getFloatVector(VectorSpecies<Float> vs, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final FloatVector getFloatVector(VectorSpecies<Float> vs, int offset, int[] idx, int idxOffset) {
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

    @Override
    public final DoubleVector getDoubleVector(VectorSpecies<Double> vs, int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final DoubleVector getDoubleVector(VectorSpecies<Double> vs, int offset, int[] idx, int idxOffset) {
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
}
