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

package rapaio.experiment.math.nn.cgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import rapaio.experiment.math.nn.cgraph.operations.Node;
import rapaio.experiment.math.nn.cgraph.operations.OpAdd;
import rapaio.experiment.math.nn.cgraph.operations.OpCos;
import rapaio.experiment.math.nn.cgraph.operations.OpLog;
import rapaio.experiment.math.nn.cgraph.operations.OpMul;
import rapaio.experiment.math.nn.cgraph.operations.OpPower;
import rapaio.experiment.math.nn.cgraph.operations.OpSin;
import rapaio.experiment.math.nn.cgraph.operations.OpSub;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

public final class Context {

    public Variable newVar(String name) {
        return new Variable(this, name);
    }

    public Constant newConst(String name, Tensor<Double> value) {
        return new Constant(this, name, value);
    }

    public Node add(Node left, Node right) {
        return new OpAdd(this, left, right);
    }

    public Node mul(Node left, Node right) {
        return new OpMul(this, left, right);
    }

    public Node sub(Node left, Node right) {
        return new OpSub(this, left, right);
    }

    public Node cos(Node child) {
        return new OpCos(this, child);
    }

    public Node sin(Node child) {
        return new OpSin(this, child);
    }

    public Node log(Node child) {
        return new OpLog(this, child);
    }

    public Node exp(Node child) {
        return new OpLog(this, child);
    }

    public Node pow(Node child, double pow) {
        return new OpPower(this, child, pow);
    }

    private final AtomicInteger idGenerator = new AtomicInteger(-1);
    private final ConcurrentHashMap<Node, Integer> nodeToId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Node> idToNode = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, List<Integer>> parents = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<Runnable> tape = new ConcurrentLinkedDeque<>();

    public int register(Node node, List<Node> children) {
        int id = idGenerator.incrementAndGet();
        nodeToId.put(node, id);
        idToNode.put(id, node);
        for (Node child : children) {
            if (!parents.containsKey(child.id())) {
                parents.put(child.id(), new ArrayList<>());
            }
            parents.get(child.id()).add(id);
        }
        return id;
    }

    public void zeroGrad() {
        tape.clear();
        nodeToId.keySet().forEach(node -> node.adjoint().reset());
    }

    public void forward(Node t) {
        List<Integer> ordered = topologicalCoverage(t);
        for (int index : ordered) {
            List<Runnable> backFunctions = idToNode.get(index).compute();
            tape.addAll(backFunctions);
        }
    }

    public void backward(Node t) {
        t.adjoint().assign(Tensors.ofDouble().scalar(1.));
        while (!tape.isEmpty()) {
            tape.pollLast().run();
        }
    }

    private List<Integer> topologicalCoverage(Node t) {
        List<Integer> list = new ArrayList<>();
        boolean[] visited = new boolean[idToNode.size()];
        recCoverage(t, list, visited);
        return list;
    }

    private void recCoverage(Node t, List<Integer> list, boolean[] visited) {
        if (visited[t.id()]) {
            return;
        }
        visited[t.id()] = true;
        for (Node child : t.children()) {
            recCoverage(child, list, visited);
        }
        list.add(t.id());
    }
}
