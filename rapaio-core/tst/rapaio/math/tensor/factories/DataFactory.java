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

package rapaio.math.tensor.factories;

import java.util.Random;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;

public abstract class DataFactory<N extends Number, T extends Tensor<N, T>> {

    final TensorEngine engine;
    final TensorEngine.OfType<N, T> ofType;
    final DType<N, T> dType;
    final Random random = new Random(42);


    public DataFactory(TensorEngine tensorEngine, TensorEngine.OfType<N, T> ofType, DType<N, T> dType) {
        this.engine = tensorEngine;
        this.ofType = ofType;
        this.dType = dType;
    }

    public TensorEngine engine() {
        return engine;
    }

    public DType<N, T> dType() {
        return dType;
    }

    public abstract N value(double x);

    public abstract N inc(N x);

    public abstract N sum(N x, N y);

    public T scalar(N value) {
        return ofType.scalar(value);
    }

    public abstract T seq(Shape shape);

    public abstract T zeros(Shape shape);

    public abstract T random(Shape shape);
}
