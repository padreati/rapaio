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

import java.util.Arrays;
import java.util.List;

import rapaio.nn.Net;
import rapaio.nn.NetState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Sequential extends AbstractNet {

    private final Net[] nets;

    public Sequential(TensorManager tm, Net... nets) {
        super(tm);
        this.nets = nets;
    }

    @Override
    public List<Tensor> parameters() {
        return Arrays.stream(nets).flatMap(module -> module.parameters().stream()).toList();
    }

    @Override
    public NetState state() {
        NetState state = new NetState();
        for (Net net : nets) {
            state.merge(net.state());
        }
        return state;
    }

    @Override
    public void train() {
        super.train();
        for (Net net : nets) {
            net.train();
        }
    }

    @Override
    public void eval() {
        super.eval();
        for (Net net : nets) {
            net.eval();
        }
    }

    @Override
    public Tensor[] forward(Tensor... inputs) {
        if (nets == null || nets.length == 0) {
            return null;
        }
        Tensor[] outputs = null;
        for (Net net : nets) {
            if (outputs == null) {
                outputs = net.forward(inputs);
            } else {
                outputs = net.forward(outputs);
            }
        }
        return outputs;
    }

    @Override
    public Tensor forward11(Tensor x) {
        return forward(new Tensor[] {x})[0];
    }
}
