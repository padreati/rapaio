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
 * 1D max pooling of an input of shape {@code (N, C, L)} or {@code (C, L)}, with output shape {@code (N, C, L_out)} or {@code (C, L_out)}.
 * <p>
 * Each output is the maximum of its window, and the gradient of an output flows back to the input which was the
 * maximum; see {@link AbstractMaxPoolNode}.
 */
public final class MaxPool1dNode extends AbstractMaxPoolNode {

    public MaxPool1dNode(Tensor x, int k, int stride, int padding, int dilation, boolean ceilMode) {
        super(x, 1, MaxPool1dNode.class.getSimpleName(), pool(x, k, stride, padding, dilation, ceilMode));
    }

    private static Pair<? extends DArray<?>, DArray<Integer>> pool(
            Tensor x, int k, int stride, int padding, int dilation, boolean ceilMode) {
        validateRank("maxPool1d", x, 1);
        validateWindow("maxPool1d", stride, padding, dilation, k);
        return x.value().maxPool1d(k, stride, padding, dilation, ceilMode);
    }
}
