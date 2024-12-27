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

package rapaio.nn.layer;

import java.util.List;

import rapaio.darray.Shape;
import rapaio.darray.operator.Broadcast;
import rapaio.nn.NetworkState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class LayerNorm extends AbstractNetwork {

    private final Shape shape;
    private final Tensor gamma;
    private final Tensor beta;
    private final double eps;

    public LayerNorm(TensorManager tm, Shape shape) {
        this(tm, shape, 1e-8);
    }

    public LayerNorm(TensorManager tm, Shape shape, double eps) {
        super(tm);
        this.shape = shape;
        this.eps = eps;

        this.gamma = tm.scalarTensor(1).requiresGrad(true);
        this.beta = tm.scalarTensor(0).requiresGrad(true);
    }

    @Override
    public List<Tensor> parameters() {
        return List.of(gamma, beta);
    }

    @Override
    public NetworkState state() {
        NetworkState state = new NetworkState();
        state.add(gamma);
        state.add(beta);
        return state;
    }

    @Override
    public Tensor forward11(Tensor x) {
        Broadcast.ElementWise broadcast = Broadcast.elementWise(x.value().shape(), shape);
        if (!broadcast.valid() || !broadcast.shape().equals(x.shape())) {
            throw new IllegalArgumentException(String.format(
                    "Input shape %s does not match target shape %s.", x.shape(), this.shape));
        }
        var s = x.standardizeOn(shape, 0, eps);
        return s.mul(gamma).add(beta);
    }
}
