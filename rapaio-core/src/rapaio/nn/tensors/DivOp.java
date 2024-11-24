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

import java.util.List;

import rapaio.nn.Tensor;
import rapaio.narray.operator.Broadcast;

public final class DivOp extends AbstractTensor {

    private final Tensor left;
    private final Tensor right;

    public DivOp(Tensor left, Tensor right) {
        super(left.tm(), "div");
        this.left = left;
        this.right = right;
        forward();
    }

    private void forward() {
        if(!Broadcast.elementWise(List.of(left.value().shape(), right.value().shape())).valid()) {
            throw new IllegalArgumentException("Nodes are not valid for elementwise broadcast.");
        }
        this.setValue(left.value().div(right.value()));
        backEdge(left, () -> this.grad().div(right.value()).reduceSum(left.value().shape()));
        backEdge(right, () -> this.grad().mul(left.value().neg().div(right.value().sqr())).reduceSum(right.value().shape()));
    }
}