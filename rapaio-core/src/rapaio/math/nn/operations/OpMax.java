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
import rapaio.math.tensor.operator.Compare;

public class OpMax extends BaseOpNode {

    private final Node x;
    private final double threshold;

    public OpMax(Node x, double threshold) {
        super(x.dtype(), "max");
        this.threshold = threshold;
        this.x = x;
        forward();
    }

    private void forward() {
        this.setValue(x.value().max(threshold));
        backEdge(x, () -> this.value().copy().compareMask_(Compare.GT, threshold).mul_(this.grad()));
    }
}
