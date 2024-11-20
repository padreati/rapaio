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

package rapaio.narray.storage;

import rapaio.narray.DType;
import rapaio.narray.Storage;

public abstract class ByteStorage extends Storage<Byte> {

    @Override
    public final DType<Byte> dType() {
        return DType.BYTE;
    }

    @Override
    public final Byte get(int ptr) {
        return getByte(ptr);
    }

    @Override
    public final void set(int ptr, Byte v) {
        setByte(ptr, v);
    }

    @Override
    public final void inc(int ptr, Byte value) {
        incByte(ptr, value);
    }

    @Override
    public void fill(Byte value, int start, int len) {
        fillByte(value, start, len);
    }

    public abstract byte getByte(int ptr);

    public abstract void setByte(int ptr, byte v);

    public abstract void incByte(int ptr, byte value);

    public abstract void fillByte(byte value, int start, int len);


    @Override
    public final int getInt(int ptr) {
        return getByte(ptr);
    }

    @Override
    public final void setInt(int ptr, int value) {
        setByte(ptr, (byte) value);
    }

    @Override
    public final void incInt(int ptr, int value) {
        incByte(ptr, (byte) value);
    }

    @Override
    public final void fillInt(int value, int start, int len) {
        fillByte((byte) value, start, len);
    }

    @Override
    public final float getFloat(int ptr) {
        return getByte(ptr);
    }

    @Override
    public final void setFloat(int ptr, float value) {
        setByte(ptr, (byte) value);
    }

    @Override
    public final void incFloat(int ptr, float value) {
        incByte(ptr, (byte) value);
    }

    @Override
    public final void fillFloat(float value, int start, int len) {
        fillByte((byte) value, start, len);
    }


    @Override
    public final double getDouble(int ptr) {
        return getByte(ptr);
    }

    @Override
    public final void setDouble(int ptr, double value) {
        setByte(ptr, (byte) value);
    }

    @Override
    public final void incDouble(int ptr, double value) {
        incByte(ptr, (byte) value);
    }

    @Override
    public final void fillDouble(double value, int start, int len) {
        fillByte((byte) value, start, len);
    }
}
