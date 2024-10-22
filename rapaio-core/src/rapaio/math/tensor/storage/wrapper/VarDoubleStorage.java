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

package rapaio.math.tensor.storage.wrapper;

import rapaio.data.Var;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Storage;

public final class VarDoubleStorage extends Storage<Double> {

    private final Var vd;

    public VarDoubleStorage(Var vd) {
        this.vd = vd;
    }

    @Override
    public DType<Double> dType() {
        return DType.DOUBLE;
    }

    @Override
    public int size() {
        return vd.size();
    }

    @Override
    public Double get(int ptr) {
        return vd.getDouble(ptr);
    }

    @Override
    public void set(int ptr, Double value) {
        vd.setDouble(ptr, value);
    }

    @Override
    public void inc(int ptr, Double value) {
        vd.setDouble(ptr, vd.getDouble(ptr) + value);
    }

    @Override
    public void fill(Double value, int start, int len) {
        fillDouble(value, start, len);
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
    public void fillByte(byte value, int start, int len) {
        fillDouble(value, start, len);
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
    public void fillInt(int value, int start, int len) {
        fillDouble(value, start, len);
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
    public void fillFloat(float value, int start, int len) {
        fillDouble(value, start, len);
    }

    @Override
    public double getDouble(int ptr) {
        return vd.getDouble(ptr);
    }

    @Override
    public void setDouble(int ptr, double value) {
        vd.setDouble(ptr, value);
    }

    @Override
    public void incDouble(int ptr, double value) {
        vd.setDouble(ptr, vd.getDouble(ptr) + value);
    }

    @Override
    public void fillDouble(double value, int start, int len) {
        for (int i = start; i < start+len; i++) {
            vd.setDouble(i, value);
        }
    }
}
