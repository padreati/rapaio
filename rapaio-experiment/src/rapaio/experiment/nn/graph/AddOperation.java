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

package rapaio.experiment.nn.graph;

import java.util.List;

import rapaio.math.tensor.Tensor;
import rapaio.util.NotImplementedException;

public class AddOperation extends Operation {

    public AddOperation(Graph graph, String name, Node a, Node b) {
        super(graph, name, List.of(a, b));
    }

    @Override
    public Tensor<?> compute(List<? extends Tensor<?>> operands) {
        if (checkAllDouble(operands)) {
            return ((Tensor<Double>) operands.get(0)).add_((Tensor<Double>) operands.get(1));
        }
        if (checkAllFloat(operands)) {
            return ((Tensor<Float>) operands.get(0)).add_((Tensor<Float>) operands.get(1));
        }
        if (checkAllInt(operands)) {
            return ((Tensor<Integer>) operands.get(0)).add_((Tensor<Integer>) operands.get(1));
        }
        throw new NotImplementedException();
    }
}
