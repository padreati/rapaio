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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;

public final class Grad {

    public static Variable var(Tensor<?> value) {
        return new Variable("var", value);
    }

    public static Variable var(DType<?> dtype, String name) {
        return new Variable(name, dtype);
    }

    public static Variable var(Tensor<?> value, String name) {
        return new Variable(name, value);
    }

    public static void backward(Loss loss) {
        backward(loss.last);
    }

    public static void backward(Node node) {
        if (node.value().size() != 1) {
            throw new IllegalArgumentException(
                    "Backward cannot compute gradients on non scalar variables, variable shape: " + node.value().shape());
        }
        runBackwardGraph(node);
    }

    private static void runBackwardGraph(Node node) {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> visited = new HashSet<>();
        queue.add(node);
        visited.add(node);

        while (!queue.isEmpty()) {
            Node last = queue.poll();
            for (BackFun edge : last.backFuns()) {
                if (!visited.contains(edge.ref())) {
                    visited.add(edge.ref());
                    queue.add(edge.ref());
                }
                edge.fun().run();
            }
            last.backFuns().clear();
        }
    }
}
