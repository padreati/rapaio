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

import rapaio.narray.Shape;
import rapaio.nn.NetState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class BatchNorm1D extends AbstractNet {

    public static final double DEFAULT_MOMENTUM = 0.1;
    public static final double DEFAULT_EPSILON = 1e-5;

    private final int numFeatures;
    private final double epsValue;
    private final double momentum;

    private Tensor sampleMean;
    private Tensor sampleStd;

    private final Tensor gamma;
    private final Tensor beta;

    public BatchNorm1D(TensorManager tm, int numFeatures) {
        this(tm, numFeatures, DEFAULT_MOMENTUM, DEFAULT_EPSILON);
    }

    public BatchNorm1D(TensorManager tm, int numFeatures, double momentum) {
        this(tm, numFeatures, momentum, DEFAULT_EPSILON);
    }

    public BatchNorm1D(TensorManager tm, int numFeatures, double momentum, double eps) {
        super(tm);
        this.numFeatures = numFeatures;
        this.momentum = momentum;
        this.epsValue = eps;

        this.sampleMean = tm.zerosTensor(Shape.of(numFeatures));
        this.sampleStd = tm.fullTensor(Shape.of(numFeatures), 1);

        this.gamma = tm.fullTensor(Shape.of(numFeatures), 1).requiresGrad(true).name("gamma");
        this.beta = tm.fullTensor(Shape.of(numFeatures), 0).requiresGrad(true).name("beta");
    }

    @Override
    public List<Tensor> parameters() {
        return List.of(gamma, beta);
    }

    @Override
    public NetState state() {
        NetState state = new NetState();
        state.addTensors(List.of(sampleMean, sampleStd, gamma, beta));
        return state;
    }

    @Override
    public Tensor forward11(Tensor x) {
        if (x.value().rank() != 2 || x.value().dim(1) != numFeatures) {
            throw new IllegalArgumentException("Input has an invalid shape: " + x.value().shape());
        }
        if (train) {
            Tensor mean = x.mean1d(0);
            Tensor std = x.std1d(0, 0, epsValue, mean);

            if (sampleMean == null || sampleStd == null) {
                sampleMean = mean;
                sampleStd = std;
            } else {
                sampleMean = tm.var(sampleMean.value().mul(1 - momentum).add_(mean.value().mul(momentum)));
                sampleStd = tm.var(sampleStd.value().mul(1 - momentum).add_(std.value().mul(momentum)));
            }
            var s = x.sub(mean).div(std);
            return s.mul(gamma).add(beta);
        } else {
            if (sampleMean == null || sampleStd == null) {
                throw new IllegalArgumentException("BatchNorm1D requires at least one optimization step.");
            }
            var s = x.sub(sampleMean).div(sampleStd);
            return s.mul(gamma).add(beta);
        }
    }
}
