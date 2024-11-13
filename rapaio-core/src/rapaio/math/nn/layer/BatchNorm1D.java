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

import rapaio.math.nn.Autograd;
import rapaio.math.nn.Node;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;

public class BatchNorm1D extends BaseNet {

    private final int numFeatures;
    private final double epsValue;

    private final Node gamma;
    private final Node beta;

    public BatchNorm1D(DType<?> dtype, int numFeatures) {
        this(dtype, numFeatures, 1e-3);
    }

    public BatchNorm1D(DType<?> dtype, int numFeatures, double eps) {
        super(TensorManager.base().ofType(dtype));
        this.numFeatures = numFeatures;
        this.epsValue = eps;

        this.gamma = Autograd.var(tmt.full(Shape.of(numFeatures), 1)).requiresGrad(true);
        this.beta = Autograd.var(tmt.full(Shape.of(numFeatures), 0)).requiresGrad(true);
    }

    @Override
    public List<Node> parameters() {
        return List.of(gamma, beta);
    }

    @Override
    protected Node forward11(Node x) {
        if (x.value().rank() != 2 || x.value().dim(1) != numFeatures) {
            throw new IllegalArgumentException("Input has an invalid shape: " + x.value().shape());
        }
        var mean = x.axisMean(0);
        var centered = x.sub(mean);
        var std = centered.sqr().sum().add(epsValue).div(x.value().dim(0)).sqrt();
        var standardized = centered.div(std);

        var scaled = standardized.mul(gamma).add(beta);
        return scaled;
    }
}
