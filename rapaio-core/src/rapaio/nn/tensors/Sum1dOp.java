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

public final class Sum1dOp extends Tensor {

    private final int axis;
    private final Tensor child;

    public Sum1dOp(Tensor child, int axis) {
        super(child.tm(), "sum1d");
        this.axis = axis;
        this.child = child;

        this.setValue(child.value().sum1d(axis));
        backEdge(child, () -> this.grad().strexp(axis, child.value().dim(axis)));
    }
}
