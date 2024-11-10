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
import rapaio.util.NotImplementedException;

public final class OpAxisSum extends BaseOpNode {

    private static final int DEFAULT_AXIS = -1;

    private final int dim;
    private final Node child;

    public OpAxisSum(Node child) {
        this(child, DEFAULT_AXIS);
    }

    public OpAxisSum(Node child, int dim) {
        super(child.dtype(), "axisSum");
        this.dim = dim;
        this.child = child;
        forward();
    }

    private void forward() {
        switch (child.value().rank()) {
            case 0:
                this.setValue(child.value());
                backEdge(child, () -> child.addGrad(this.grad()));
                break;
            case 1:
                this.setValue(child.value().sum(0));
                backEdge(child, () -> child.addGrad(this.grad().strexp(0, child.value().dim(0))));
                break;
            case 2:
                this.setValue(child.value().sum(1));
                backEdge(child, () -> child.addGrad(this.grad().strexp(1, child.value().dim(1))));
                break;
            default:
                throw new NotImplementedException();
        }
    }
}
