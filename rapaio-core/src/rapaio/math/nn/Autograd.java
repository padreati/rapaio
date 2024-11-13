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

package rapaio.math.nn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

/**
 * Central place of automatic differentiation in reverse mode.
 * <p>
 * Object which allows differentiation must implement {@link Node}.
 * <p>
 * The forward operations are performed when the computation is called using various operations
 * on {@link Node} or when new node are created with {@link #var(Tensor)} or {@link #var(DType)}.
 * <p>
 * In order to compute gradients one has to call {@link #backward(Node)}. The methods can be called on nodes
 * or on loss functions {@link Loss}. In all cases the node on which {@code backward} method is called must
 * have a computed gradient and that has to be a scalar.
 * <p>
 * To maximize the performance not all the gradients are computed. The one which are computed are for
 * the variables which has {@link Node#requiresGrad()} equals with {@code true}, all on all the objects
 * in the upper computational graph to the root node (the node on which {@code backward} method was called.
 */
public final class Autograd {

    public static Variable var(Tensor<?> value) {
        return new Variable(value);
    }

    public static Variable var(DType<?> dtype) {
        return new Variable(dtype);
    }

    public static Variable scalar(DType<?> dtype, double value) {
        return (Variable) new Variable(Tensors.ofType(dtype).scalar(value)).name("scalar");
    }

    public static ComputeGraph backward(Loss loss) {
        return backward(loss, false);
    }

    public static ComputeGraph backward(Loss loss, boolean retainGrad) {
        return backward(loss.last(), retainGrad);
    }

    public static ComputeGraph backward(Node node) {
        return backward(node, false);
    }

    public static ComputeGraph backward(Node node, boolean retainGrad) {
        if(node.grad()==null) {
            throw new IllegalArgumentException("Cannot propagate gradients if the root node has none.");
        }
        if(!node.grad().shape().equals(node.value().shape())) {
            throw new IllegalArgumentException("Gradient shape must match value's shape.");
        }
        return runBackwardGraph(node, retainGrad);
    }

    private static ComputeGraph runBackwardGraph(Node node, boolean retainGrad) {
        ComputeGraph graph = new ComputeGraph(node, retainGrad);
        graph.run();
        return graph;
    }

    public static class ComputeGraph {

        final Node root;
        final boolean retainGrad;

        List<Node> reverse;
        Set<Node> computeGrad;

        public ComputeGraph(Node root, boolean retainGrad) {
            this.root = root;
            this.retainGrad = retainGrad;
        }

        public List<Node> coveredNodes() {
            return reverse;
        }

        public void printNodes() {
            reverse.forEach(System.out::println);
        }

        public void resetGrad() {
            reverse.forEach(Node::zeroGrad);
            reverse.forEach(node -> node.backfuns().clear());
        }

        public void run() {
            buildDeps();
            for (Node node : reverse) {
                for (BackFun backFun : node.backfuns()) {
                    if (computeGrad.contains(backFun.ref())) {
                        backFun.ref().addGrad(backFun.fun().get());
                    }
                }
                if (!retainGrad) {
                    node.backfuns().clear();
                }
            }
        }

        private void buildDeps() {
            // build coverage
            Set<Node> coverage = new HashSet<>();
            coverage(coverage, root);

            // build parents
            HashMap<Node, List<Node>> parents = new HashMap<>();
            coverage.forEach(node -> parents.put(node, new ArrayList<>()));
            for (Node node : coverage) {
                for (BackFun edge : node.backfuns()) {
                    parents.get(edge.ref()).add(node);
                }
            }

            // build parent counters
            HashMap<Node, Integer> counters = new HashMap<>();
            coverage.forEach(node -> counters.put(node, parents.get(node).size()));

            // build topological sort
            reverse = new ArrayList<>();
            HashSet<Node> frontier = new HashSet<>();
            frontier.add(root);

            while (!frontier.isEmpty()) {
                Optional<Node> opNext = frontier.stream().filter(node -> counters.get(node) == 0).findFirst();
                if (opNext.isEmpty()) {
                    throw new IllegalArgumentException("Graph contains cycles.");
                }
                Node next = opNext.get();
                frontier.remove(next);
                for (BackFun bf : next.backfuns()) {
                    counters.put(bf.ref(), counters.get(bf.ref()) - 1);
                    frontier.add(bf.ref());
                }
                reverse.add(next);
            }

            // compute topological sort and compute gradient

            List<Node> sorted = reverse.reversed();
            this.computeGrad = new HashSet<>();
            for (Node node : sorted) {
                if (node.requiresGrad() || computeGrad.contains(node)) {
                    computeGrad.add(node);
                    computeGrad.addAll(parents.get(node));
                }
            }

            // help gc
            parents.clear();
            coverage.clear();
            counters.clear();
        }

        private void coverage(Set<Node> visited, Node node) {
            if (visited.contains(node)) {
                return;
            }
            visited.add(node);
            for (var edge : node.backfuns()) {
                coverage(visited, edge.ref());
            }
        }
    }
}