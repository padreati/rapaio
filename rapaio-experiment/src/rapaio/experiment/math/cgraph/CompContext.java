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

package rapaio.experiment.math.cgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import rapaio.experiment.math.cgraph.operations.OpAdd;
import rapaio.experiment.math.cgraph.operations.OpCos;
import rapaio.experiment.math.cgraph.operations.OpLog;
import rapaio.experiment.math.cgraph.operations.OpMul;
import rapaio.experiment.math.cgraph.operations.OpPower;
import rapaio.experiment.math.cgraph.operations.OpSin;
import rapaio.experiment.math.cgraph.operations.OpSub;

public final class CompContext {

    private final Random random = new Random();

    public void seed(long seed) {
        random.setSeed(seed);
    }

    public Random random() {
        return random;
    }

    public CVar var(String name) {
        return new CVar(this, name);
    }

    public CConst cnst(String name, double value) {
        return new CConst(this, name, value);
    }

    public CNode add(CNode left, CNode right) {
        return new OpAdd(this, left, right);
    }

    public CNode mul(CNode left, CNode right) {
        return new OpMul(this, left, right);
    }

    public CNode sub(CNode left, CNode right) {
        return new OpSub(this, left, right);
    }

    public CNode cos(CNode child) {
        return new OpCos(this, child);
    }

    public CNode sin(CNode child) {
        return new OpSin(this, child);
    }

    public CNode log(CNode child) {
        return new OpLog(this, child);
    }

    public CNode exp(CNode child) {
        return new OpLog(this, child);
    }

    public CNode pow(CNode child, double pow) {
        return new OpPower(this, child, pow);
    }

    private final AtomicInteger idGenerator = new AtomicInteger(-1);
    private final ArrayList<CNode> nodes = new ArrayList<>();
    private final ConcurrentLinkedDeque<Runnable> tape = new ConcurrentLinkedDeque<>();

    public int register(CNode node) {
        int id = idGenerator.incrementAndGet();
        nodes.add(id, node);
        return id;
    }

    public void zeroGrad() {
        tape.clear();
        nodes.forEach(node -> node.grad.reset());
    }

    public void forward(CNode t) {
        List<Integer> ordered = topologicalCoverage(t);
        for (int index : ordered) {
            List<Runnable> backFunctions = nodes.get(index).forward();
            tape.addAll(backFunctions);
        }
    }

    public void backward(CNode t) {
        t.grad.set(1.);
        while (!tape.isEmpty()) {
            tape.pollLast().run();
        }
    }

    private List<Integer> topologicalCoverage(CNode t) {
        List<Integer> list = new ArrayList<>();
        boolean[] visited = new boolean[nodes.size()];
        recCoverage(t, list, visited);
        return list;
    }

    private void recCoverage(CNode t, List<Integer> list, boolean[] visited) {
        if (visited[t.id()]) {
            return;
        }
        visited[t.id()] = true;
        for (CNode child : t.children()) {
            recCoverage(child, list, visited);
        }
        list.add(t.id());
    }
}
