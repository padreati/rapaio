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

public interface DTensor extends Tensor<Double, DTensor> {

    @Override
    default Double get(int... indexes) {
        return getDouble(indexes);
    }

    double getDouble(int... indexes);

    @Override
    default void set(Double value, int... indexes) {
        setDouble(value, indexes);
    }

    void setDouble(double value, int... indexes);

    @Override
    default Double ptrGet(int ptr) {
        return ptrGetDouble(ptr);
    }

    double ptrGetDouble(int ptr);

    @Override
    default void ptrSet(int ptr, Double value) {
        ptrSetDouble(ptr, value);
    }

    void ptrSetDouble(int ptr, double value);

    @Override
    default DTensor add_(Double value) {
        return add_(value.doubleValue());
    }

    DTensor add_(double value);

    @Override
    default DTensor sub_(Double value) {
        return sub_(value.doubleValue());
    }

    DTensor sub_(double value);

    @Override
    default DTensor mul_(Double value) {
        return mul_(value.doubleValue());
    }

    DTensor mul_(double value);

    @Override
    default DTensor div_(Double value) {
        return div_(value.doubleValue());
    }

    DTensor div_(double value);

    default Double vdot(DTensor tensor) {
        return vdotDouble(tensor);
    }

    double vdotDouble(DTensor tensor);

    default Double vdot(DTensor tensor, int start, int end) {
        return vdotDouble(tensor, start, end);
    }

    double vdotDouble(DTensor tensor, int start, int end);
}
