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

package rapaio.math.nn.loss;

import rapaio.math.nn.Loss;
import rapaio.math.nn.Node;
import rapaio.math.tensor.Tensors;

public class NLLoss extends Loss {

    @Override
    public void forward(Node pred, Node y) {
        // TODO: treat extra dimension with more care
        if (pred.value().isMatrix() && y.value().isVector()) {
            y.setValue(y.value().stretch(1));
        }
        batch = y.value().dim(0);
        last = pred.neg().mul(y).sum();
        last.setGrad(Tensors.ofType(pred.dtype()).scalar(1));
    }
}
