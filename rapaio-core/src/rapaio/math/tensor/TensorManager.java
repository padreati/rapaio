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

import java.util.Random;

import rapaio.math.tensor.manager.cpuarray.CpuArraySingleTensorManager;

public interface TensorManager {

    static TensorManager newDefault() {
        return newCpuArraySingle();
    }

    static TensorManager newCpuArraySingle() {
        return new CpuArraySingleTensorManager();
    }

    DTensor doubleZeros(Shape shape, Order order);

    default DTensor doubleZeros(Shape shape) {
        return doubleZeros(shape, Order.defaultOrder());
    }

    DTensor doubleSeq(Shape shape, Order order);

    default DTensor doubleSeq(Shape shape) {
        return doubleSeq(shape, Order.defaultOrder());
    }

    DTensor doubleRandom(Shape shape, Random random, Order order);

    default DTensor doubleRandom(Shape shape, Random random) {
        return doubleRandom(shape, random, Order.defaultOrder());
    }

    DTensor wrap(Shape shape, double[] array, Order order);

    FTensor floatZeros(Shape shape, Order order);

    default FTensor floatZeros(Shape shape) {
        return floatZeros(shape, Order.defaultOrder());
    }

    FTensor floatSeq(Shape shape, Order order);

    default FTensor floatSeq(Shape shape) {
        return floatSeq(shape, Order.defaultOrder());
    }

    FTensor floatRandom(Shape shape, Random random, Order order);

    default FTensor floatRandom(Shape shape, Random random) {
        return floatRandom(shape, random, Order.defaultOrder());
    }

    FTensor wrap(Shape shape, float[] array, Order order);

}
