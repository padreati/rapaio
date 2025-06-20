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

package rapaio.darray.storage.array;

import java.util.Arrays;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.Simd;
import rapaio.darray.storage.ByteStorage;

public final class ByteArrayStorage extends ByteStorage {

    private static final VectorSpecies<Byte> vsb = Simd.vsByte;

    private final byte[] array;

    public ByteArrayStorage(byte[] array) {
        this.array = array;
    }

    public ByteArrayStorage(int[] array) {
        this.array = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (byte) array[i];
        }
    }

    public ByteArrayStorage(float[] array) {
        this.array = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (byte) array[i];
        }
    }

    public ByteArrayStorage(double[] array) {
        this.array = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (byte) array[i];
        }
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean supportSimd() {
        return true;
    }

    public byte getByte(int ptr) {
        return array[ptr];
    }

    public void setByte(int ptr, byte v) {
        array[ptr] = v;
    }

    @Override
    public void incByte(int ptr, byte value) {
        array[ptr] += value;
    }

    @Override
    public void fill(byte value, int start, int len) {
        Arrays.fill(array, start, start + len, value);
    }

    @Override
    public ByteVector getByteVector(int offset) {
        return ByteVector.fromArray(vsb, array, offset);
    }

    @Override
    public ByteVector getByteVector(int offset, int[] idx, int idxOffset) {
        return ByteVector.fromArray(vsb, array, offset, idx, idxOffset);
    }

    @Override
    public void setByteVector(ByteVector value, int offset) {
        value.intoArray(array, offset);
    }

    @Override
    public void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset) {
        value.intoArray(array, offset, idx, idxOffset);
    }


    @Override
    public ByteVector getByteVector(int offset, VectorMask<Byte> m) {
        return ByteVector.fromArray(vsb, array, offset, m);
    }

    @Override
    public ByteVector getByteVector(int offset, int[] idx, int idxOffset, VectorMask<Byte> m) {
        return ByteVector.fromArray(vsb, array, offset, idx, idxOffset, m);
    }

    @Override
    public void setByteVector(ByteVector value, int offset, VectorMask<Byte> m) {
        value.intoArray(array, offset, m);
    }

    @Override
    public void setByteVector(ByteVector value, int offset, int[] idx, int idxOffset, VectorMask<Byte> m) {
        value.intoArray(array, offset, idx, idxOffset, m);
    }

    public byte[] array() {
        return array;
    }
}
