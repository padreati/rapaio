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

package rapaio.experiment.math.nn.operations;

import java.util.ArrayList;
import java.util.List;

import rapaio.experiment.math.nn.Context;
import rapaio.experiment.math.nn.Node;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

public class OpBatchVDot extends OpNode {

    private final Node left;
    private final Node right;

    public OpBatchVDot(Context c, Node left, Node right) {
        super(c, null, "batchVDot");
        this.left = left;
        this.right = right;
    }

    @Override
    public List<Node> children() {
        return List.of(left, right);
    }

    @Override
    public List<Runnable> forward() {
        List<Tensor<Double>> prods = new ArrayList<>();
        for (int i = 0; i < left.value().dim(0); i++) {
            Tensor<?> vec = left.value().takesq(0, i);
            prods.add((Tensor<Double>) vec.stretch(0).mm(right.value()).squeeze(0));
        }
        this.value(Tensors.stack(0, prods));
        return List.of();
    }
}
