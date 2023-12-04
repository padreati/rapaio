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

package rapaio.math.tensor.mill;

import rapaio.math.tensor.Tensor;

/**
 * Utility class which contains various methods used to validate conditions required before performing tensor operations.
 */
public final class TensorValidation {

    @SafeVarargs
    public static <N extends Number, T extends Tensor<N, T>> void sameShape(T t1, T t2, T... otherTensors) {
        if (!t1.shape().equals(t2.shape())) {
            throw new IllegalArgumentException("Tensors does not have the same shape.");
        }
        for (var t : otherTensors) {
            if (!t1.shape().equals(t.shape())) {
                throw new IllegalArgumentException("Tensors does not have the same shape.");
            }
        }
    }
}
