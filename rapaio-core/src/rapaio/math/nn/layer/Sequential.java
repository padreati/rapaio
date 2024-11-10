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

package rapaio.math.nn.layer;

import java.util.Arrays;
import java.util.List;

import rapaio.math.nn.Net;
import rapaio.math.nn.Node;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;

public class Sequential extends Net {

    private final Net[] nets;

    public Sequential(Net... nets) {
        this(Tensors.ofDouble(), nets);
    }

    public Sequential(TensorManager.OfType<?> tmt, Net... nets) {
        super(tmt);
        this.nets = nets;
    }

    @Override
    public void seed(long seed) {
        super.seed(seed);
        for (Net net : nets) {
            net.seed(seed);
        }
    }

    @Override
    public List<Node> parameters() {
        return Arrays.stream(nets).flatMap(module -> module.parameters().stream()).toList();
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
    public Node[] forward(Node... inputs) {
        if (nets == null || nets.length == 0) {
            return null;
        }
        Node[] outputs = null;
        for (Net net : nets) {
            if (outputs == null) {
                outputs = net.forward(inputs);
            } else {
                outputs = net.forward(outputs);
            }
        }
        return outputs;
    }
}
