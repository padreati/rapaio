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
    default Double getValue(int... indexes) {
        return get(indexes);
    }

    double get(int... indexes);

    @Override
    default void setValue(Double value, int... indexes) {
        set(value, indexes);
    }

    void set(double value, int... indexes);

    @Override
    default Double ptrGetValue(int ptr) {
        return ptrGet(ptr);
    }

    double ptrGet(int ptr);

    @Override
    default void ptrSetValue(int ptr, Double value) {
        ptrSet(ptr, value);
    }

    void ptrSet(int ptr, double value);

    @Override
    default DTensor reshape(Shape shape) {
        return reshape(shape, Order.defaultOrder());
    }

    @Override
    DTensor reshape(Shape shape, Order askOrder);

    @Override
    DTensor ravel(Order askOrder);

    @Override
    DTensor flatten(Order askOrder);

    @Override
    DTensor squeeze();

    @Override
    DTensor moveAxis(int src, int dst);

    @Override
    DTensor swapAxis(int src, int dst);

    @Override
    DTensor t();

    @Override
    DTensor copy(Order askOrder);
}
