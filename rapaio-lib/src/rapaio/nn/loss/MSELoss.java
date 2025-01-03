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

package rapaio.nn.loss;

import rapaio.core.param.Param;
import rapaio.core.param.ValueParam;
import rapaio.nn.Loss;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class MSELoss extends AbstractLoss<MSELoss> {

    public final Param<Reduce, MSELoss> reduce = new ValueParam<>(this, Reduce.MEAN, "Reduce type");

    public MSELoss(TensorManager tm) {
        super(tm);
    }

    @Override
    public Loss newInstance() {
        return new MSELoss(tm);
    }

    @Override
    public Output forward(Tensor pred, Tensor y) {
        // TODO: treat extra dimension with more care
        if (pred.value().isMatrix()) {
            y.setValue(y.value().stretch(1));
        }
        Tensor last = (reduce.get().equals(Reduce.MEAN)) ? pred.sub(y).sqr().sum().div(pred.value().size()) : pred.sub(y).sqr().sum();
        last.setGrad(tm.scalarArray(1));
        return new Output(last, last.value().getDouble());
    }
}
