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

import java.util.List;

import rapaio.math.nn.Node;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.operator.Broadcast;

public class OpSub extends BaseOpNode {

    private final Node left;
    private final Node right;

    public OpSub(Node left, Node right) {
        super(left.dtype(), "sub");
        this.left = left;
        this.right = right;
        forward();
    }

    private void forward() {
        if(!Broadcast.elementWise(List.of(left.value().shape(), right.value().shape())).valid()) {
            throw new IllegalArgumentException("Nodes not valid for elementwise broadcast");
        }
        this.setValue(left.value().sub(right.value()));
        backEdge(left, () -> {
            Tensor<?> selfGrad = this.grad().reduceMean(left.value().shape());
            if (!left.value().shape().equals(selfGrad.shape())) {
                throw new IllegalStateException("Mismatched shape, current: " + this.grad().shape() + ", input: " + left.value().shape());
            }
            left.addGrad(selfGrad);
        });
        backEdge(right, () -> {
            Tensor<?> selfGrad = this.grad().reduceMean(right.value().shape());
            if (!right.value().shape().equals(selfGrad.shape())) {
                throw new IllegalStateException("Mismatched shape, current: " + this.grad().shape() + ", input: " + right.value().shape());
            }
            right.subGrad(selfGrad);
        });
    }
}
