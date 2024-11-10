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

package rapaio.math.nn.operations;

import rapaio.math.nn.Node;

public class OpBatchVtm extends BaseOpNode {

    private final Node left;
    private final Node right;

    public OpBatchVtm(Node left, Node right) {
        super(left.dtype(), "BatchVtm");
        this.left = left;
        this.right = right;
        forward();
    }

    public void forward() {
        this.setValue(left.value().bvtm(right.value()));
        backEdge(left, () -> left.addGrad(this.grad().bmm(right.value().t()).mean(0)));
        backEdge(right, () -> right.addGrad(left.value().t().bmm(this.grad()).mean(0)));
    }
}
