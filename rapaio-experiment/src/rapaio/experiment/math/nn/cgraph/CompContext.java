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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import rapaio.experiment.math.nn.cgraph.operations.CompNode;
import rapaio.experiment.math.nn.cgraph.operations.OpAdd;
import rapaio.experiment.math.nn.cgraph.operations.OpCos;
import rapaio.experiment.math.nn.cgraph.operations.OpLog;
import rapaio.experiment.math.nn.cgraph.operations.OpMul;
import rapaio.experiment.math.nn.cgraph.operations.OpPower;
import rapaio.experiment.math.nn.cgraph.operations.OpSin;
import rapaio.experiment.math.nn.cgraph.operations.OpSub;
import rapaio.experiment.math.nn.cgraph.operations.OpVDot;
import rapaio.experiment.math.nn.cgraph.operations.OpVSum;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;

public final class CompContext {

    private final Random random = new Random();

    public void seed(long seed) {
        random.setSeed(seed);
    }

    public Random random() {
        return random;
    }

    public CompVariable newVar(String name) {
        return new CompVariable(this, name);
    }

    public CompConstant newConst(String name, Tensor<?> value) {
        return new CompConstant(this, name, value);
    }

    public CompNode add(CompNode left, CompNode right) {
        return new OpAdd(this, left, right);
    }

    public CompNode mul(CompNode left, CompNode right) {
        return new OpMul(this, left, right);
    }

    public CompNode sub(CompNode left, CompNode right) {
        return new OpSub(this, left, right);
    }

    public CompNode cos(CompNode child) {
        return new OpCos(this, child);
    }

    public CompNode sin(CompNode child) {
        return new OpSin(this, child);
    }

    public CompNode log(CompNode child) {
        return new OpLog(this, child);
    }

    public CompNode exp(CompNode child) {
        return new OpLog(this, child);
    }

    public CompNode pow(CompNode child, double pow) {
        return new OpPower(this, child, pow);
    }

    public CompNode vsum(CompNode child) {
        return new OpVSum(this, child);
    }

    public CompNode vdot(CompNode left, CompNode right) {
        return new OpVDot(this, left, right);
    }

    private final TensorManager.OfType<?> tmt;
    private final AtomicInteger idGenerator = new AtomicInteger(-1);
    private final LinkedList<CompNode> nodes = new LinkedList<>();
    private final ArrayList<CompNode> idToNode = new ArrayList<>();
    private final ConcurrentLinkedDeque<Runnable> tape = new ConcurrentLinkedDeque<>();

    public CompContext(DType<?> dtype) {
        this.tmt = TensorManager.base().ofType(dtype);
    }

    public TensorManager.OfType<?> tmt() {
        return tmt;
    }

    public int register(CompNode node) {
        int id = idGenerator.incrementAndGet();
        idToNode.add(id, node);
        return id;
    }

    public void zeroGrad() {
        tape.clear();
        nodes.forEach(node -> node.adjoint().reset());
    }

    public void forward(CompNode t) {
        List<Integer> ordered = topologicalCoverage(t);
        for (int index : ordered) {
            List<Runnable> backFunctions = idToNode.get(index).compute();
            tape.addAll(backFunctions);
        }
    }

    public void backward(CompNode t) {
        t.adjoint().assign(Tensors.ofDouble().scalar(1.));
        while (!tape.isEmpty()) {
            tape.pollLast().run();
        }
    }

    private List<Integer> topologicalCoverage(CompNode t) {
        List<Integer> list = new ArrayList<>();
        boolean[] visited = new boolean[idToNode.size()];
        recCoverage(t, list, visited);
        return list;
    }

    private void recCoverage(CompNode t, List<Integer> list, boolean[] visited) {
        if (visited[t.id()]) {
            return;
        }
        visited[t.id()] = true;
        for (CompNode child : t.children()) {
            recCoverage(child, list, visited);
        }
        list.add(t.id());
    }
}
