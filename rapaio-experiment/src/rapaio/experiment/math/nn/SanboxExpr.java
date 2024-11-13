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

import rapaio.math.nn.Autograd;
import rapaio.math.nn.Node;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensors;

public class SanboxExpr {

    public static void main(String[] args) {

        Node a = Autograd.var(Tensors.stride(Shape.of(2, 2), 2., 1, 1.3, 5)).requiresGrad(true).name("a");
        Node f = Autograd.var(Tensors.scalar(1.)).name("f");

        Node c = a.sqr().name("c");
        Node b = a.add(c).name("b");
        Node d = b.log().name("d");
        Node e1 = c.add(d).requiresGrad(true).name("e1");
        Node e2 = e1.add(f).name("e2");
        Node g = e2.add(d).name("g");
        Node h = f.add(g).name("h");
        Node l = h.sum().name("l");

        l.setGrad(Tensors.scalar(1.));

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(l, true);
        graph.run();

        graph.printNodes();

    }
}
