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

package rapaio.nn.layer;

import java.util.List;

import rapaio.math.narray.DType;
import rapaio.math.narray.NArrayManager;
import rapaio.math.narray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;

public class BatchNorm1D extends AbstractNet {

    private final int numFeatures;
    private final double epsValue;

    private final Tensor gamma;
    private final Tensor beta;

    public BatchNorm1D(DType<?> dtype, int numFeatures) {
        this(dtype, numFeatures, 1e-3);
    }

    public BatchNorm1D(DType<?> dtype, int numFeatures, double eps) {
        super(NArrayManager.base().ofType(dtype));
        this.numFeatures = numFeatures;
        this.epsValue = eps;

        this.gamma = Autograd.var(tmt.full(Shape.of(numFeatures), 1)).requiresGrad(true).name("gamma");
        this.beta = Autograd.var(tmt.full(Shape.of(numFeatures), 0)).requiresGrad(true).name("beta");
    }

    @Override
    public List<Tensor> parameters() {
        return List.of(gamma, beta);
    }

    @Override
    public Tensor forward11(Tensor x) {
        if (x.value().rank() != 2 || x.value().dim(1) != numFeatures) {
            throw new IllegalArgumentException("Input has an invalid shape: " + x.value().shape());
        }
        var mean = x.mean1d(0).name("batchnorm1d:mean1d");
        var std = x.std1d(0, 0, mean).name("batchnorm1d:std1d");
        var s = x.sub(mean).div(std.add(epsValue)).name("batchnorm1d:s");
        return s.mul(gamma).add(beta);
    }
}
