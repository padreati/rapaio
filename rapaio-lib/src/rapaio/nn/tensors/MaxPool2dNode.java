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

import rapaio.darray.DArray;
import rapaio.nn.Tensor;
import rapaio.util.Pair;

/**
 * 2D max pooling of an input of shape {@code (N, C, H, W)} or {@code (C, H, W)}, with output shape {@code (N, C, H_out, W_out)} or {@code (C, H_out, W_out)}.
 * <p>
 * Each output is the maximum of its window, and the gradient of an output flows back to the input which was the
 * maximum; see {@link AbstractMaxPoolNode}.
 */
public final class MaxPool2dNode extends AbstractMaxPoolNode {

    public MaxPool2dNode(Tensor x, int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        super(x, 2, MaxPool2dNode.class.getSimpleName(), pool(x, kH, kW, stride, padding, dilation, ceilMode));
    }

    private static Pair<? extends DArray<?>, DArray<Integer>> pool(
            Tensor x, int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        validateRank("maxPool2d", x, 2);
        validateWindow("maxPool2d", stride, padding, dilation, kH, kW);
        return x.value().maxPool2d(kH, kW, stride, padding, dilation, ceilMode);
    }
}
