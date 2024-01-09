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

package rapaio.math.tensor;

public interface Storage<N extends Number> {

    DType<N> dType();

    int size();


    N get(int ptr);

    void set(int ptr, N value);

    void inc(int ptr, N value);

    void fill(N value);


    byte getByte(int ptr);

    void setByte(int ptr, byte value);

    void incByte(int ptr, byte value);

    void fillByte(byte value);


    int getInt(int ptr);

    void setInt(int ptr, int value);

    void incInt(int ptr, int value);

    void fillInt(int value);


    float getFloat(int ptr);

    void setFloat(int ptr, float value);

    void incFloat(int ptr, float value);

    void fillFloat(float value);


    double getDouble(int ptr);

    void setDouble(int ptr, double value);

    void incDouble(int ptr, double value);

    void fillDouble(double value);
}
