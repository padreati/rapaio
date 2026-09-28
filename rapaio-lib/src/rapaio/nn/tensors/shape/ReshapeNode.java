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

package rapaio.nn.tensors.shape;

import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.Tensor;

public class ReshapeNode extends Tensor {

    public ReshapeNode(Tensor x, Shape shape, Order askOrder) {
        super(x.tm(), ReshapeNode.class.getSimpleName());
        // Order.A is resolved once, here, against the input. The backward below must read the gradient in the same
        // order the forward read the input, otherwise the values land on the wrong input positions. Passing the
        // gradient reshape no order at all defaults it to Order.A, which resolves a second time against the
        // gradient's own layout, and that is whatever the operation above this node happened to leave behind
        Order order = resolveAuto(x, askOrder);
        this.setValue(x.value().reshape(shape, order));
        backEdge(x, () -> this.grad.reshape(x.shape(), order));
    }

    /**
     * Resolves {@link Order#A} the same way {@code reshape} does, against the layout of the darray being reshaped.
     */
    private static Order resolveAuto(Tensor x, Order askOrder) {
        if (Order.A != askOrder) {
            return askOrder;
        }
        if (x.value().layout().isCOrdered()) {
            return Order.C;
        }
        if (x.value().layout().isFOrdered()) {
            return Order.F;
        }
        return Order.defaultOrder();
    }
}
