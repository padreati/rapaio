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

import java.util.ArrayList;
import java.util.List;

import rapaio.nn.Network;
import rapaio.nn.NetworkState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Sequential extends AbstractNetwork {

    private final List<Network> networks;

    public Sequential(TensorManager tm, Network... networks) {
        super(tm);
        this.networks = new ArrayList<>(List.of(networks));
    }

    @Override
    public List<Tensor> parameters() {
        return networks.stream().flatMap(module -> module.parameters().stream()).toList();
    }

    public List<Network> sequence() {
        return networks;
    }

    @Override
    public NetworkState state() {
        NetworkState state = new NetworkState();
        for (Network network : networks) {
            state.merge(network.state());
        }
        return state;
    }

    @Override
    public void train() {
        super.train();
        for (Network network : networks) {
            network.train();
        }
    }

    @Override
    public void eval() {
        super.eval();
        for (Network network : networks) {
            network.eval();
        }
    }

    @Override
    public Tensor[] forward(Tensor... inputs) {
        if (networks == null || networks.isEmpty()) {
            return null;
        }
        Tensor[] outputs = null;
        for (Network network : networks) {
            if (outputs == null) {
                outputs = network.forward(inputs);
            } else {
                outputs = network.forward(outputs);
            }
        }
        return outputs;
    }

    @Override
    public Tensor forward11(Tensor x) {
        return forward(x)[0];
    }
}
