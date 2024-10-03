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

package rapaio.math.tensor.factories;

import java.util.Random;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;

public abstract class DataFactory<N extends Number> {

    final TensorManager engine;
    final TensorManager.OfType<N> ofType;
    final DType<N> dType;
    final Random random = new Random(42);


    public DataFactory(TensorManager tensorManager, TensorManager.OfType<N> ofType, DType<N> dType) {
        this.engine = tensorManager;
        this.ofType = ofType;
        this.dType = dType;
    }

    public TensorManager engine() {
        return engine;
    }

    public DType<N> dType() {
        return dType;
    }

    public abstract N value(double x);

    public abstract N inc(N x);

    public abstract N sum(N x, N y);

    public Tensor<N> scalar(N value) {
        return ofType.scalar(value);
    }

    public abstract Tensor<N> seq(Shape shape);

    public abstract Tensor<N> zeros(Shape shape);

    public abstract Tensor<N> random(Shape shape);
}
