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

public final class BatchVtm extends Tensor {

    public BatchVtm(Tensor bv, Tensor bm) {
        super(bv.tm(), BatchVtm.class.getSimpleName());

        this.setValue(bv.value().bvtm(bm.value()));
        backEdge(bv, () -> {
            var g = this.grad().bvtm(bm.value().t());
            return (bv.rank() == 1) ? g.mean1d(0) : g;
        });
        backEdge(bm, () -> bv.value().t().mm(this.grad()));
    }
}
