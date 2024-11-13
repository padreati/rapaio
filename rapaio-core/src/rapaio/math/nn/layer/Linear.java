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

package rapaio.math.nn.layer;

import java.util.List;

import rapaio.core.distributions.Uniform;
import rapaio.math.nn.Autograd;
import rapaio.math.nn.Node;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;

public class Linear extends BaseNet {

    private final int inFeatures;
    private final int outFeatures;
    private final boolean bias;

    private final Node w;
    private final Node b;

    public Linear(DType<?> dtype, int inFeatures, int outFeatures, boolean bias) {
        this(Tensors.ofType(dtype), inFeatures, outFeatures, bias);
    }

    public Linear(TensorManager.OfType<?> tmt, int inFeatures, int outFeatures, boolean bias) {
        super(tmt);

        this.inFeatures = inFeatures;
        this.outFeatures = outFeatures;
        this.bias = bias;

        this.w = Autograd.var(tmt.dtype()).requiresGrad(true).name("weights");
        if (this.bias) {
            this.b = Autograd.var(tmt.dtype()).requiresGrad(true).name("bias");
        } else {
            this.b = null;
        }
        reset();
    }

    private void reset() {
        double range = Math.sqrt(1. / inFeatures);
        w.setValue(tmt.random(Shape.of(inFeatures, outFeatures), Uniform.of(-range, range), random));
        if (bias) {
            b.setValue(tmt.random(Shape.of(outFeatures), Uniform.of(-range, range), random));
        }
    }

    @Override
    public List<Node> parameters() {
        return (bias) ? List.of(w, b) : List.of(w);
    }

    @Override
    public Node forward11(Node x) {
        var result = x.bvtm(w);
        if (bias) {
            result = result.add(b);
        }
        return result;
    }
}
