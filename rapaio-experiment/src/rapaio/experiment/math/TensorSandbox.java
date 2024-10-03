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

package rapaio.experiment.math;

import java.io.IOException;
import java.net.URISyntaxException;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensors;

public class TensorSandbox {

    public static void main(String[] args) throws IOException, URISyntaxException {

        var tc = Tensors.seq(Shape.of(2,3), Order.C);
        var tf = Tensors.seq(Shape.of(2,3), Order.F);

        var copyc = Tensors.zeros(Shape.of(2,3), Order.C);
        var copyf = Tensors.zeros(Shape.of(2,3), Order.F);

        tc.copyTo(copyc).printString();
        tc.copyTo(copyf).printString();

        tf.copyTo(copyc).printString();
        tf.copyTo(copyf).printString();
    }
}