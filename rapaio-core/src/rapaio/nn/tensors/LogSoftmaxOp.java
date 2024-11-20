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

package rapaio.nn.tensors;

import rapaio.nn.Tensor;

public class LogSoftmaxOp extends AbstractTensor {

    private final int axis;
    private final Tensor x;

    public LogSoftmaxOp(Tensor x, int axis) {
        super(x.tm(), "logsoftmax");
        this.x = x;
        this.axis = axis;
        forward();
    }

    public void forward() {
        this.setValue(x.value().logsoftmax1d(axis));
        backEdge(x, () -> {
            var sm = this.value().exp();
            var sum1d = this.grad().sum1d(axis).strexp(axis, x.dim(axis));
            return this.grad().sub(sm.mul(sum1d));
        });
    }
}
