/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import rapaio.core.distributions.Uniform;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.NetworkState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Conv1D extends AbstractNetwork {

    private static final double range = 0.5;

    private final int inChannels;
    private final int outChannels;

    private final int k;
    private final int stride;
    private final int padding;
    private final int dilation;
    private final int groups;
    private final boolean bias;

    private final Tensor w;
    private final Tensor b;

    public Conv1D(TensorManager tm, int inChannels, int outChannels,
            int k, int stride, int padding, int dilation, int groups, boolean bias) {
        super(tm);

        this.inChannels = inChannels;
        this.outChannels = outChannels;

        this.k = k;
        this.stride = stride;
        this.padding = padding;
        this.dilation = dilation;
        this.groups = groups;
        this.bias = bias;

        w = tm.var().requiresGrad(true).name("weight");
        w.setValue(tm.randomArray(Shape.of(outChannels, inChannels / groups, k), Uniform.of(-range, range), Order.F));
        if (bias) {
            b = tm.var().requiresGrad(true).name("bias");
            b.setValue(tm.fullArray(Shape.of(outChannels), 1));
        } else {
            b = null;
        }
    }

    @Override
    public List<Tensor> parameters() {
        return bias ? List.of(w, b) : List.of(w);
    }

    @Override
    public NetworkState state() {
        NetworkState state = new NetworkState();
        state.addTensors(bias ? List.of(w, b) : List.of(w));
        return state;
    }

    @Override
    public Tensor forward11(Tensor x) {
        return x.conv1d(w, b, stride, padding, dilation, groups);
    }
}
