/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.nn.tensors.unary;

import rapaio.darray.Compare;
import rapaio.darray.DArray;
import rapaio.nn.Tensor;

/**
 * Natural logarithm.
 * <p>
 * With {@code eps <= 0} the node computes {@code log(x)} and its exact derivative {@code 1/x}, so a zero input gives
 * {@code -inf} forward and {@code +inf} backward, as it should.
 * <p>
 * With {@code eps > 0} the input is clamped from below: the node computes {@code log(max(x, eps))}. The derivative is
 * {@code 1/x} where the input is not clamped and {@code 0} where it is, which is the derivative of the clamped
 * function. This keeps both directions finite for inputs which underflow to zero.
 */
public class LogNode extends Tensor {

    public LogNode(Tensor child, double eps) {
        super(child.tm(), LogNode.class.getSimpleName());

        if (eps > 0) {
            DArray<?> clamped = child.value().max(eps);
            // 1 where the input is used as it is, 0 where it was replaced by eps
            DArray<?> mask = child.value().copy().compareMask_(Compare.GE, eps);
            this.setValue(clamped.log());
            backEdge(child, () -> this.grad().div(clamped).mul_(mask));
        } else {
            this.setValue(child.value().log());
            backEdge(child, () -> this.grad().div(child.value()));
        }
    }
}
