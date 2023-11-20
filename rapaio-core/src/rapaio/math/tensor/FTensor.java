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

public interface FTensor extends Tensor<Float, FTensor> {

    @Override
    default Float get(int... indexes) {
        return getFloat(indexes);
    }

    float getFloat(int... indexes);

    @Override
    default void set(Float value, int... indexes) {
        setFloat(value, indexes);
    }

    void setFloat(float value, int... indexes);

    @Override
    default Float getAt(int ptr) {
        return getAtFloat(ptr);
    }

    float getAtFloat(int ptr);

    @Override
    default void setAt(int ptr, Float value) {
        setAtFloat(ptr, value);
    }

    void setAtFloat(int ptr, float value);

    @Override
    default FTensor add_(Float value) {
        return add_(value.floatValue());
    }

    FTensor add_(float value);

    @Override
    default FTensor sub_(Float value) {
        return sub_(value.floatValue());
    }

    FTensor sub_(float value);

    @Override
    default FTensor mul_(Float value) {
        return mul_(value.floatValue());
    }

    FTensor mul_(float value);

    @Override
    default FTensor div_(Float value) {
        return div_(value.floatValue());
    }

    FTensor div_(float value);

    default Float vdotValue(FTensor tensor) {
        return vdot(tensor);
    }

    float vdot(FTensor tensor);
}
