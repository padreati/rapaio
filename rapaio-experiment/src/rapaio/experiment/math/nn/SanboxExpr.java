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

import rapaio.math.narrays.DType;
import rapaio.math.narrays.NArrays;
import rapaio.math.narrays.Shape;
import rapaio.math.nn.Autograd;
import rapaio.math.nn.Tensor;

public class SanboxExpr {

    public static void main(String[] args) {

        Tensor a = Autograd.var(NArrays.stride(Shape.of(2, 2), 2., 1, 1.3, 5)).requiresGrad(true).name("a");
        Tensor f = Autograd.scalar(DType.DOUBLE, 1).name("f");

        Tensor c = a.sqr().name("c");
        Tensor b = a.add(c).name("b");
        Tensor d = b.log().name("d");
        Tensor e1 = c.add(d).requiresGrad(true).name("e1");
        Tensor e2 = e1.add(f).name("e2");
        Tensor g = e2.add(d).name("g");
        Tensor h = f.add(g).name("h");
        Tensor l = h.sum().name("l");

        l.setGrad(NArrays.scalar(1.));

        Autograd.ComputeGraph graph = new Autograd.ComputeGraph(l, true);
        graph.run();

        graph.printNodes();

    }
}
