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

package rapaio.nn.tensors;

import rapaio.darray.Order;
import rapaio.nn.Tensor;

public final class BatchVtmNode extends Tensor {

    public BatchVtmNode(Tensor bv, Tensor bm) {
        super(bv.tm(), BatchVtmNode.class.getSimpleName());

        this.setValue(bv.value().reorder(Order.C).bvtm(bm.value().reorder(Order.F)));
        // a rank 1 vector is a batch of one row: the output (and so the gradient) already has shape (1, n)
        backEdge(bv, () -> {
            var g = this.grad().bvtm(bm.value().t());
            return (bv.rank() == 1) ? g.reshape(bv.shape()) : g;
        });
        backEdge(bm, () -> {
            var v = bv.rank() == 1 ? bv.value().stretch(0) : bv.value();
            return v.t().reorder(Order.C).mm(this.grad().reorder(Order.F));
        });
    }
}
