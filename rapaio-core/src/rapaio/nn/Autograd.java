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

package rapaio.nn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import rapaio.narray.DType;
import rapaio.narray.NArray;

/**
 * Central place of automatic differentiation in reverse mode.
 * <p>
 * Object which allows differentiation must implement {@link Tensor}.
 * <p>
 * The forward operations are performed when the computation is called using various operations
 * on {@link Tensor} or when new node are created with {@link #var(NArray)} or {@link #var(DType)}.
 * <p>
 * In order to compute gradients one has to call {@link #backward(Tensor)}. The methods can be called on nodes
 * or on loss functions {@link Loss}. In all cases the node on which {@code backward} method is called must
 * have a computed gradient and that has to be a scalar.
 * <p>
 * To maximize the performance not all the gradients are computed. The one which are computed are for
 * the variables which has {@link Tensor#requiresGrad()} equals with {@code true}, all on all the objects
 * in the upper computational graph to the root node (the node on which {@code backward} method was called.
 */
public final class Autograd {

    public static ComputeGraph backward(Loss loss) {
        return backward(loss, false);
    }

    public static ComputeGraph backward(Loss loss, boolean retainGrad) {
        return backward(loss.last(), retainGrad);
    }

    public static ComputeGraph backward(Tensor tensor) {
        return backward(tensor, false);
    }

    public static ComputeGraph backward(Tensor tensor, boolean retainGrad) {
        if(tensor.grad()==null) {
            throw new IllegalArgumentException("Cannot back propagate if the root has no gradient.");
        }
        if(!tensor.grad().shape().equals(tensor.value().shape())) {
            throw new IllegalArgumentException("Gradient shape must match value's shape.");
        }
        return runBackwardGraph(tensor, retainGrad);
    }

    private static ComputeGraph runBackwardGraph(Tensor tensor, boolean retainGrad) {
        ComputeGraph graph = new ComputeGraph(tensor, retainGrad);
        graph.run();
        return graph;
    }

    public static class ComputeGraph {

        final Tensor root;
        final boolean retainGrad;

        List<Tensor> reverse;
        Set<Tensor> computeGrad;

        public ComputeGraph(Tensor root, boolean retainGrad) {
            this.root = root;
            this.retainGrad = retainGrad;
        }

        public List<Tensor> covered() {
            return reverse;
        }

        public void printTensors() {
            reverse.forEach(System.out::println);
        }

        public void resetGrad() {
            reverse.forEach(Tensor::zeroGrad);
            reverse.forEach(node -> node.backfuns().clear());
        }

        public void run() {
            buildDeps();
            for (Tensor tensor : reverse) {
                for (BackFun backFun : tensor.backfuns()) {
                    if (computeGrad.contains(backFun.ref())) {
                        backFun.ref().addGrad(backFun.fun().get());
                    }
                }
                if (!retainGrad) {
                    tensor.backfuns().clear();
                }
            }
        }

        private void buildDeps() {
            // build coverage
            Set<Tensor> coverage = new HashSet<>();
            coverage(coverage, root);

            // build parents
            HashMap<Tensor, List<Tensor>> parents = new HashMap<>();
            coverage.forEach(node -> parents.put(node, new ArrayList<>()));
            for (Tensor tensor : coverage) {
                for (BackFun edge : tensor.backfuns()) {
                    parents.get(edge.ref()).add(tensor);
                }
            }

            // build parent counters
            HashMap<Tensor, Integer> counters = new HashMap<>();
            coverage.forEach(node -> counters.put(node, parents.get(node).size()));

            // build topological sort
            reverse = new ArrayList<>();
            HashSet<Tensor> frontier = new HashSet<>();
            frontier.add(root);

            while (!frontier.isEmpty()) {
                Optional<Tensor> opNext = frontier.stream().filter(node -> counters.get(node) == 0).findFirst();
                if (opNext.isEmpty()) {
                    throw new IllegalArgumentException("Graph contains cycles.");
                }
                Tensor next = opNext.get();
                frontier.remove(next);
                for (BackFun bf : next.backfuns()) {
                    counters.put(bf.ref(), counters.get(bf.ref()) - 1);
                    frontier.add(bf.ref());
                }
                reverse.add(next);
            }

            // compute topological sort and compute gradient

            List<Tensor> sorted = reverse.reversed();
            this.computeGrad = new HashSet<>();
            for (Tensor tensor : sorted) {
                if (tensor.requiresGrad() || computeGrad.contains(tensor)) {
                    computeGrad.add(tensor);
                    computeGrad.addAll(parents.get(tensor));
                }
            }

            // help gc
            parents.clear();
            coverage.clear();
            counters.clear();
        }

        private void coverage(Set<Tensor> visited, Tensor tensor) {
            if (visited.contains(tensor)) {
                return;
            }
            visited.add(tensor);
            for (var edge : tensor.backfuns()) {
                coverage(visited, edge.ref());
            }
        }
    }
}