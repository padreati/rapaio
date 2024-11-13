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

public class OpLogSoftmax extends BaseOpNode {

    private final int axis;
    private final Node x;

    public OpLogSoftmax(Node x, int axis) {
        super(x.dtype(), "logsoftmax");
        this.x = x;
        this.axis = axis;
        forward();
    }

    public void forward() {
        this.setValue(x.value().logsoftmax(axis));
        backEdge(x, () -> {
            var s = x.value().softmax(axis);
            return this.grad().sub(s.mul(this.grad()).sum(axis).strexp(axis, x.value().dim(axis)));
        });
    }
}
