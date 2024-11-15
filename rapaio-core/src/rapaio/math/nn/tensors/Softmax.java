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

package rapaio.math.nn.tensors;

import rapaio.math.nn.Tensor;

public class Softmax extends AbstractTensor {

    private final int axis;
    private final Tensor x;

    public Softmax(Tensor x, int axis) {
        super(x.dtype(), "softmax");
        this.axis = axis;
        this.x = x;
        forward();
    }

    private void forward() {
        this.setValue(x.value().softmax(axis));
        backEdge(x, () -> {
            var s = this.value();
            var p = s.mul(this.grad());
            var ws = s.mul(p.sum(axis).strexp(axis, p.dim(axis)));
            return p.sub_(ws);
        });
    }
}
