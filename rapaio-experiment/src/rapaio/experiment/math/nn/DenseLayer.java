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

import java.util.List;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;

public class DenseLayer extends Module {

    private final int inFeatures;
    private final int outFeatures;
    private final boolean bias;

    private final Parameter w;
    private final Parameter b;

    public DenseLayer(int inFeatures, int outFeatures, boolean bias) {
        this(Tensors.ofDouble(), inFeatures, outFeatures, bias);
    }

    public DenseLayer(TensorManager.OfType<?> tmt, int inFeatures, int outFeatures, boolean bias) {
        super(tmt);

        this.inFeatures = inFeatures;
        this.outFeatures = outFeatures;
        this.bias = bias;

        this.w = new Parameter(tmt.dtype(), "weights");
        this.b = new Parameter(tmt.dtype(), "bias");

        reset();
    }

    private void reset() {
        double range = Math.sqrt(inFeatures);
        w.value(tmt.random(Shape.of(inFeatures, outFeatures), random).mul_(range * 2).sub_(range));
        b.value(tmt.random(Shape.of(outFeatures), random).mul_(range * 2).sub_(range));
    }

    @Override
    public List<Parameter> parameters() {
        return List.of(w, b);
    }

    @Override
    public Tensor<?> forward(Tensor<?> input) {
        Node x = c.newVariable(input);
        var vdot = c.batchVDot(x, w);
        Node result = c.batchAdd(vdot, b);
        return result.value();
    }
}
