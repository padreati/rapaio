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

import java.util.Map;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensors;

public class Main {

    public static void main(String[] args) {

        var dtype = DType.FLOAT;

        var g = new Graph();
        var tmt = Tensors.ofType(dtype);

        var x = new Placeholder(g, "x");
        var w = new Variable(g, "w", tmt.stride(Shape.of(2, 2), 1, 1, 1, -1));

        var b = new Variable(g, "b", tmt.stride(Shape.of(2), 0, 0));

        var sm = new SoftmaxOperation(g, "softmax", new AddOperation(g, "add", b, new MatVecOperation(g, "matmul", w, x)));

        var value = g.run(sm, Map.of("x", tmt.stride(Shape.of(2), Order.C, 0, 1)));
        value.printSummary();
        value.printContent();
    }
}
