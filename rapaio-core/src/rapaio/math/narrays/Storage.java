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

package rapaio.math.narrays;

public abstract class Storage<N extends Number> {

    public abstract DType<N> dType();

    public abstract int size();

    public abstract N get(int ptr);

    public abstract void set(int ptr, N value);

    public abstract void inc(int ptr, N value);

    public abstract void fill(N value, int start, int len);


    public abstract byte getByte(int ptr);

    public abstract void setByte(int ptr, byte value);

    public abstract void incByte(int ptr, byte value);

    public abstract void fillByte(byte value, int start, int len);


    public abstract int getInt(int ptr);

    public abstract void setInt(int ptr, int value);

    public abstract void incInt(int ptr, int value);

    public abstract void fillInt(int value, int start, int len);


    public abstract float getFloat(int ptr);

    public abstract void setFloat(int ptr, float value);

    public abstract void incFloat(int ptr, float value);

    public abstract void fillFloat(float value, int start, int len);


    public abstract double getDouble(int ptr);

    public abstract void setDouble(int ptr, double value);

    public abstract void incDouble(int ptr, double value);

    public abstract void fillDouble(double value, int start, int len);

}
