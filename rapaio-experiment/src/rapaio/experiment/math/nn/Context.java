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

package rapaio.experiment.math.nn;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import rapaio.experiment.math.nn.operations.OpBatchAdd;
import rapaio.experiment.math.nn.operations.OpBatchVDot;
import rapaio.math.tensor.Tensor;

public abstract class Context {

    private final Set<Node> nodes = new HashSet<>();
    private final ConcurrentLinkedDeque<Runnable> tape = new ConcurrentLinkedDeque<>();
    private Set<Parameter> parameters;

    public void bind(Module module) {
        parameters = new HashSet<>(module.parameters());
        module.bind(this);
    }

    public void zeroGrad() {
        tape.clear();
        parameters.forEach(Node::resetAdjoint);
        nodes.forEach(node -> node.value(null));
        nodes.forEach(Node::resetAdjoint);
    }

    public Variable newVariable(Tensor<?> data) {
        Variable var = new Variable(this, data);
        nodes.add(var);
        return var;
    }

    public Node batchVDot(Node left, Node right) {
        Node node = new OpBatchVDot(this, left, right);
        node.forward();
        nodes.add(node);
        return node;
    }

    public Node batchAdd(Node left, Node right) {
        Node node = new OpBatchAdd(this, left, right);
        node.forward();
        nodes.add(node);
        return node;
    }
}
