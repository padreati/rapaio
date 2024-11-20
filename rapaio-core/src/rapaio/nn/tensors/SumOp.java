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
import rapaio.narray.NArray;
import rapaio.narray.NArrays;

public class SumOp extends AbstractTensor {

    private final Tensor child;

    public SumOp(Tensor child) {
        super(child.tm(), "sum");
        this.child = child;
        forward();
    }

    private void forward() {
        this.setValue(NArrays.ofType(child.dtype()).scalar(child.value().sum().doubleValue()));
        backEdge(child, () -> {
            NArray<?> grad = this.grad();
            // gradient is a scalar, we expand by child shape
            if (!child.value().isScalar()) {
                for (int i = 0; i < child.value().rank(); i++) {
                    grad = grad.strexp(i, child.value().dim(i));
                }
            }
            return grad;
        });
    }
}
