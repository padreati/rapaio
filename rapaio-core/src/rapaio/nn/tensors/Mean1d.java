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

public final class Mean1d extends Tensor {

    public Mean1d(Tensor x, int axis) {
        super(x.tm(), Mean1d.class.getSimpleName());

        if (x.rank() == 0) {
            throw new IllegalArgumentException("Input node must have at least one dimension.");
        }
        this.setValue(x.value().mean1d(axis));
        backEdge(x, () -> this.grad().div(x.dim(axis)).strexp(axis, x.dim(axis)));
    }
}