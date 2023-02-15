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

public interface DStorage extends Storage<Double> {

    @Override
    default Double getValue(int offset) {
        return get(offset);
    }

    double get(int offset);

    @Override
    default void setValue(int offset, Double v) {
        set(offset, v);
    }

    void set(int offset, double v);

    @Override
    default void fillValue(int start, int len, Double v) {
        fill(start, len, v);
    }

    void fill(int start, int len, double v);

    @Override
    default void addValue(int start, int len, Double v) {
        add(start, len, v);
    }

    void add(int start, int len, double v);

    @Override
    default void subValue(int start, int len, Double v) {
        sub(start, len, v);
    }

    void sub(int start, int len, double v);

    @Override
    default void mulValue(int start, int len, Double v) {
        mul(start, len, v);
    }

    void mul(int start, int len, double v);

    @Override
    default void divValue(int start, int len, Double v) {
        div(start, len, v);
    }

    void div(int start, int len, double v);

    @Override
    default Double minValue(int start, int len) {
        return min(start, len);
    }

    double min(int start, int len);

    @Override
    int argMin(int start, int len);

    @Override
    DStorage copy();
}
