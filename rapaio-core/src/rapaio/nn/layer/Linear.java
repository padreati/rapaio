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

import rapaio.core.distributions.Uniform;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.NetState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Linear extends AbstractNet {

    private final int inFeatures;
    private final int outFeatures;
    private final boolean bias;

    private final Tensor w;
    private final Tensor b;

    public Linear(TensorManager tm, int inFeatures, int outFeatures, boolean bias) {
        super(tm);

        this.inFeatures = inFeatures;
        this.outFeatures = outFeatures;
        this.bias = bias;

        this.w = tm.var().requiresGrad(true).name("weights");
        if (this.bias) {
            this.b = tm.var().requiresGrad(true).name("bias");
        } else {
            this.b = null;
        }
        reset();
    }

    private void reset() {
        double range = 1. / Math.sqrt(inFeatures);
        w.setValue(tm.randomArray(Shape.of(inFeatures, outFeatures), Uniform.of(-range, range), tm.random(), Order.F));
        if (bias) {
            b.setValue(tm.randomArray(Shape.of(outFeatures), Uniform.of(-range, range), tm.random(), Order.F));
        }
    }

    @Override
    public List<Tensor> parameters() {
        return (bias) ? List.of(w, b) : List.of(w);
    }

    @Override
    public NetState state() {
        NetState state = new NetState();
        state.addTensors(bias ? List.of(w, b) : List.of(w));
        return state;
    }

    @Override
    public Tensor forward11(Tensor x) {
        Tensor result = x.bvtm(w);
        if (bias) {
            result = result.add(b);
        }
        return result;
    }
}
