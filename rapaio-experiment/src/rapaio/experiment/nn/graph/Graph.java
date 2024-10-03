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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rapaio.math.tensor.Tensor;

public class Graph {

    private final Map<String, Node> nodeMap = new HashMap<>();

    public void registerNode(Node node) {
        if (nodeMap.containsKey(node.name())) {
            throw new IllegalArgumentException("Graph contains already a node with the same name: " + node.name());
        }
        this.nodeMap.put(node.name(), node);
    }

    public void compile(Node op) {
        List<Node> list = postorder(op);
        for (var node : list) {
            node.compile();
        }
    }

    public Tensor<?> run(Operation op, Map<String, Tensor<?>> valueMap) {
        HashMap<String, Tensor<?>> outputs = new HashMap<>();
        List<Node> postorderNodes = postorder(op);
        for (var node : postorderNodes) {
            switch (node) {
                case Placeholder p -> outputs.put(p.name(), valueMap.get(p.name()));
                case Variable v -> outputs.put(v.name(), v.value());
                case Operation o -> {
                    var valueInputs = o.inputs().stream().map(n -> outputs.get(n.name())).toList();
                    outputs.put(o.name(), o.compute(valueInputs));
                }
                default -> throw new RuntimeException();
            }
        }
        return outputs.get(op.name());
    }

    private List<Node> postorder(Node node) {
        LinkedList<Node> nodes = new LinkedList<>();
        postorderRec(node, nodes);
        return nodes;
    }

    private void postorderRec(Node node, LinkedList<Node> nodes) {
        if (node instanceof Operation opNode) {
            for (var input : opNode.inputs()) {
                nodes.addAll(postorder(input));
            }
        }
        nodes.add(node);
    }

}
