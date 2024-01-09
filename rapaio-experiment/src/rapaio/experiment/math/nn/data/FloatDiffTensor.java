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

package rapaio.experiment.math.nn.data;

import rapaio.experiment.math.nn.gradient.GradientTape;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;

public final class FloatDiffTensor extends AbstractDiffTensor {

    public static FloatDiffTensor of(String name, Tensor<Float> tensor, GradientTape tape) {
        return new FloatDiffTensor(name, tensor, tape);
    }

    public static FloatDiffTensor ofAny(String name, Object object, GradientTape tape) {
        if (object instanceof Tensor<?> tensor && tensor.dtype() == DType.FLOAT) {
            return new FloatDiffTensor(name, (Tensor<Float>) tensor, tape);
        }
        throw new IllegalArgumentException();
    }

    private final Tensor<Float> tensor;

    private FloatDiffTensor(String name, Tensor<Float> tensor, GradientTape tape) {
        super(name, tape);
        this.tensor = tensor;
    }

    @Override
    public DType<?> dtype() {
        return DType.FLOAT;
    }

    @Override
    public Tensor<Float> asFloat() {
        return tensor;
    }

}
