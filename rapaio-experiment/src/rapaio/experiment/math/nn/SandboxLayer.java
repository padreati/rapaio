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

import rapaio.experiment.math.nn.data.DoubleDiffTensor;
import rapaio.experiment.math.nn.gradient.GradientTape;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensors;

import java.util.List;
import java.util.Random;

public class SandboxLayer {

    public static void main(String[] args) {

//        Identity id1 = Identity.of("id1");
//        Sequential nn1 = Sequential.of("seq1", List.of(
//                id1,
//                Identity.of("id2")
//        ));
//        System.out.println(nn1);

//        Identity id1 = Identity.of("id1");
//        Identity id2 = Identity.of("id2");
//        Identity id3 = Identity.of("id3");
//
//        id2.bindTo(List.of(id1));
//        id2.bindTo(List.of(id2));
//        id3.bindTo(List.of(id2));
//
//        System.out.println(id1);


        var random = new Random(10);
        GradientTape tape = new GradientTape();

        var x = DoubleDiffTensor.of(Tensors.seq(Shape.of(2, 2)), tape);
        var y = DoubleDiffTensor.of(Tensors.random(Shape.of(2, 2), random), tape);

        var z = x.add(y, tape);

        var p = z.mul(x, tape);

        List<DiffTensor> gradients = tape.grad(p, List.of(x, y));
        for(var gradient : gradients) {
            System.out.printf("%s =\n%s", gradient.name(), gradient.asDouble().toContent());
        }

        DiffTensor d;

    }

}
