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

import rapaio.core.param.ParamSet;
import rapaio.math.nn.Autograd;
import rapaio.math.nn.Loss;
import rapaio.math.nn.Tensor;

public abstract class AbstractLoss<L extends AbstractLoss<L>> extends ParamSet<L> implements Loss {

    protected int batch;
    protected Tensor last;

    public final void backward() {
        Autograd.backward(this);
    }

    public final double loss() {
        if (last.value().get() == null) {
            return Double.NaN;
        }
        return last.value().get().doubleValue();
    }

    public Tensor last() {
        return last;
    }
}
