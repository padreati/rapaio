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

/**
 * 2D convolution tensor with gradient computation.
 * <p>
 * Forward: y = conv2d(x, w, b, padding, stride, dilation, groups)
 * <p>
 * Gradients:
 * <ul>
 *   <li>grad_x = convTranspose2d(grad_y, w)</li>
 *   <li>grad_w via im2col: grad_w[g] = sum_batch( grad_y[g] @ unfold2d(x[g]).T )</li>
 *   <li>grad_b = grad_y.sum(dims=[0,2,3])</li>
 * </ul>
 */
public final class Conv2dNode extends Tensor {

    public Conv2dNode(Tensor x, Tensor w, Tensor b, int stride, int padding, int dilation, int groups) {
        super(x.tm(), Conv2dNode.class.getSimpleName());

        this.setValue(x.value().conv2d(w.value(), b != null ? b.value() : null, stride, padding, dilation, groups));

        // grad w.r.t. input: transposed convolution
        int inH = x.value().dim(2);
        int inW = x.value().dim(3);
        int kH = w.value().dim(2);
        int kW = w.value().dim(3);
        int outH = this.value().dim(2);
        int outW = this.value().dim(3);
        int outputPaddingH = inH - ((outH - 1) * stride - 2 * padding + dilation * (kH - 1) + 1);
        int outputPaddingW = inW - ((outW - 1) * stride - 2 * padding + dilation * (kW - 1) + 1);
        // convTranspose2d uses a single outputPadding; use max to be safe (typically both are equal)
        int outputPadding = Math.max(outputPaddingH, outputPaddingW);
        backEdge(x, () ->
                this.grad().convTranspose2d(w.value(), null, stride, padding, dilation, groups, outputPadding)
        );

        // grad w.r.t. weight via im2col
        backEdge(w, () -> {
            int n = x.value().dim(0);
            int inDepth = w.value().dim(1);
            int outDepth = w.value().dim(0) / groups;
            DArray<?> gradW = tm.zerosArray(w.value().shape());
            // a C ordered view of the whole gradient, one row per output channel. Reshaping the freshly allocated,
            // and therefore contiguous, array always yields a view, so the narrowed blocks below write straight into
            // gradW; reshaping a narrowed block instead would fall back to a detached copy whenever the reshape
            // could not be expressed as a view, and the gradient of that group would be silently dropped
            DArray<?> gradWFlat = gradW.reshape(Shape.of(w.value().dim(0), inDepth * kH * kW), Order.C);

            var xBatches = x.value().chunk(0, false, 1);    // (C_in, H, W)
            var gyBatches = this.grad().chunk(0, false, 1); // (C_out, outH, outW)

            for (int batch = 0; batch < n; batch++) {
                var xSlices = xBatches.get(batch).chunk(0, true, inDepth);
                var gySlices = gyBatches.get(batch).chunk(0, true, outDepth);
                for (int group = 0; group < groups; group++) {
                    // unfold: (inDepth*kH*kW, outH*outW)
                    DArray<?> unfold = xSlices.get(group).unfold2d(kH, kW, stride, padding, dilation);
                    // gyFlat: (outDepth, outH*outW). The order is pinned to C, since the gradient reaching this node
                    // carries whatever layout the upstream operation left it in, and Order.A would read an F ordered
                    // one column major and pair the wrong positions with the unfolded input
                    DArray<?> gyFlat = gySlices.get(group).reshape(Shape.of(outDepth, outH * outW), Order.C);

                    var gradWn = gradWFlat.narrow(0, group * outDepth, (group + 1) * outDepth);
                    gyFlat.mm(unfold.t_(), gradWn);
                }
            }
            return gradW;
        });

        if (b != null) {
            // grad w.r.t. bias: sum over H (dim 2), W (dim 3), then batch (dim 0)
            backEdge(b, () -> this.grad().sum1d(3).sum1d(2).sum1d(0));
        }
    }
}
