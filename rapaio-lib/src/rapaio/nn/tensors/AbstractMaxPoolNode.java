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
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.Tensor;
import rapaio.util.Pair;

/**
 * Common part of the max pooling nodes.
 * <p>
 * The forward pass is the pooling of the input, computed by the darray, which also returns for every output the
 * position of its maximum, as a flat index into the spatial plane of its channel. The backward pass sends each
 * output gradient to that position. Windows overlap when the stride is smaller than the kernel, so one input can be
 * the maximum of several outputs; its gradient is then the sum of their gradients.
 */
public abstract class AbstractMaxPoolNode extends Tensor {

    private final DArray<Integer> indices;

    /**
     * @param x           input tensor, with {@code spatialRank} trailing spatial dimensions and at least one leading
     *                    (channel) dimension before them
     * @param spatialRank number of pooled dimensions
     * @param name        node name
     * @param pooled      the pooled values and the flat indices of their maxima, as returned by the darray
     */
    protected AbstractMaxPoolNode(Tensor x, int spatialRank, String name, Pair<? extends DArray<?>, DArray<Integer>> pooled) {
        super(x.tm(), name);
        this.setValue(pooled.v1);
        this.indices = pooled.v2;

        int planeIn = 1;
        for (int i = x.rank() - spatialRank; i < x.rank(); i++) {
            planeIn *= x.dim(i);
        }
        int planeOut = 1;
        for (int i = value.rank() - spatialRank; i < value.rank(); i++) {
            planeOut *= value.dim(i);
        }
        int planes = x.size() / planeIn;
        int inPlaneSize = planeIn;
        int outPlaneSize = planeOut;

        backEdge(x, () -> {
            // one row per channel plane; C order on both sides, so a row lists the plane in the same order the flat
            // indices were computed in, whatever layout the gradient arriving here has
            DArray<?> gx = tm.zerosArray(Shape.of(planes, inPlaneSize));
            DArray<?> gy = this.grad().reshape(Shape.of(planes, outPlaneSize), Order.C);
            DArray<Integer> idx = indices.reshape(Shape.of(planes, outPlaneSize), Order.C);
            for (int p = 0; p < planes; p++) {
                for (int o = 0; o < outPlaneSize; o++) {
                    int i = idx.getInt(p, o);
                    if (i >= 0) {
                        gx.incDouble(gy.getDouble(p, o), p, i);
                    }
                }
            }
            return gx.reshape(x.shape(), Order.C);
        });
    }

    /**
     * @return for every output, the flat index of its maximum within the spatial plane of its channel
     */
    public DArray<Integer> indices() {
        return indices;
    }

    /**
     * Validates the pooling window. The padding can be at most half of the effective kernel size, so that no window
     * lies entirely in the padding, which is the same rule as in PyTorch.
     */
    protected static void validateWindow(String op, int stride, int padding, int dilation, int... kernel) {
        if (stride < 1 || dilation < 1 || padding < 0) {
            throw new IllegalArgumentException(String.format(
                    "%s requires stride >= 1, dilation >= 1 and padding >= 0, but has stride %d, dilation %d, padding %d.",
                    op, stride, dilation, padding));
        }
        for (int k : kernel) {
            if (k < 1) {
                throw new IllegalArgumentException(op + " requires a kernel size of at least 1, but has " + k + ".");
            }
            int effective = dilation * (k - 1) + 1;
            if (padding > effective / 2) {
                throw new IllegalArgumentException(String.format(
                        "%s padding %d is more than half of the effective kernel size %d.", op, padding, effective));
            }
        }
    }

    protected static void validateRank(String op, Tensor x, int spatialRank) {
        if (x.rank() != spatialRank + 1 && x.rank() != spatialRank + 2) {
            throw new IllegalArgumentException(String.format(
                    "%s requires an input of rank %d (C, ...) or %d (N, C, ...), but has shape %s.",
                    op, spatialRank + 1, spatialRank + 2, x.shape()));
        }
    }
}
