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

package rapaio.math.narray.storage.wrapper;

import rapaio.data.Var;
import rapaio.math.narray.DType;
import rapaio.math.narray.Storage;

public class VarFloatStorage extends Storage<Float> {

    private final Var vd;

    public VarFloatStorage(Var vd) {
        this.vd = vd;
    }

    @Override
    public DType<Float> dType() {
        return DType.FLOAT;
    }

    @Override
    public int size() {
        return vd.size();
    }

    @Override
    public Float get(int ptr) {
        return vd.getFloat(ptr);
    }

    @Override
    public void set(int ptr, Float value) {
        vd.setFloat(ptr, value);
    }

    @Override
    public void inc(int ptr, Float value) {
        vd.setDouble(ptr, vd.getFloat(ptr) + value);
    }

    @Override
    public void fill(Float value, int start, int len) {
        fillFloat(value, start, len);
    }

    @Override
    public byte getByte(int ptr) {
        return (byte) getFloat(ptr);
    }

    @Override
    public void setByte(int ptr, byte value) {
        setFloat(ptr, value);
    }

    @Override
    public void incByte(int ptr, byte value) {
        incFloat(ptr, value);
    }

    @Override
    public void fillByte(byte value, int start, int len) {
        fillFloat(value, start, len);
    }

    @Override
    public int getInt(int ptr) {
        return (int) getFloat(ptr);
    }

    @Override
    public void setInt(int ptr, int value) {
        setFloat(ptr, value);
    }

    @Override
    public void incInt(int ptr, int value) {
        incFloat(ptr, value);
    }

    @Override
    public void fillInt(int value, int start, int len) {
        fillFloat(value, start, len);
    }

    @Override
    public double getDouble(int ptr) {
        return (float) getFloat(ptr);
    }

    @Override
    public void setDouble(int ptr, double value) {
        setFloat(ptr, (float) value);
    }

    @Override
    public void incDouble(int ptr, double value) {
        incFloat(ptr, (float) value);
    }

    @Override
    public void fillDouble(double value, int start, int len) {
        fillFloat((float) value, start, len);
    }

    @Override
    public float getFloat(int ptr) {
        return vd.getFloat(ptr);
    }

    @Override
    public void setFloat(int ptr, float value) {
        vd.setFloat(ptr, value);
    }

    @Override
    public void incFloat(int ptr, float value) {
        vd.setFloat(ptr, vd.getFloat(ptr) + value);
    }

    @Override
    public void fillFloat(float value, int start, int len) {
        for (int i = start; i < start + len; i++) {
            vd.setFloat(i, value);
        }
    }
}
