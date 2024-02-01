/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.storage;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Storage;

public abstract class DoubleStorage implements Storage<Double> {

    @Override
    public final DType<Double> dType() {
        return DType.DOUBLE;
    }

    @Override
    public final Double get(int ptr) {
        return getDouble(ptr);
    }

    @Override
    public final void set(int ptr, Double v) {
        setDouble(ptr, v);
    }

    @Override
    public final void inc(int ptr, Double value) {
        incDouble(ptr, value);
    }

    @Override
    public final void fill(Double value, int start, int len) {
        fillDouble(value, start, len);
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
    public final void fillByte(byte value, int start, int len) {
        fillDouble(value, start, len);
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
    public final void fillInt(int value, int start, int len) {
        fillDouble(value, start, len);
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
    public final void fillFloat(float value, int start, int len) {
        fillDouble(value, start, len);
    }


    public abstract double getDouble(int ptr);

    public abstract void setDouble(int ptr, double v);

    public abstract void incDouble(int ptr, double value);

    public abstract void fillDouble(double value, int start, int len);

}
