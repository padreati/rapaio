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
 * 3D convolution tensor with gradient computation.
 * <p>
 * Input shape {@code (N, C_in, D, H, W)}, weight shape {@code (C_out, C_in/groups, kD, kH, kW)},
 * optional bias shape {@code (C_out)} and output shape {@code (N, C_out, D_out, H_out, W_out)}.
 * <p>
 * Forward: y = conv3d(x, w, b, stride, padding, dilation, groups)
 * <p>
 * Gradients:
 * <ul>
 *   <li>grad_x = convTranspose3d(grad_y, w)</li>
 *   <li>grad_w via im2col: grad_w[g] = sum_batch( grad_y[g] @ unfold3d(x[g]).T )</li>
 *   <li>grad_b = grad_y.sum(dims=[0,2,3,4])</li>
 * </ul>
 */
public final class Conv3dNode extends Tensor {

    public Conv3dNode(Tensor x, Tensor w, Tensor b, int stride, int padding, int dilation, int groups) {
        super(x.tm(), Conv3dNode.class.getSimpleName());

        this.setValue(x.value().conv3d(w.value(), b != null ? b.value() : null, stride, padding, dilation, groups));

        // grad w.r.t. input: transposed convolution
        int inD = x.value().dim(2);
        int inH = x.value().dim(3);
        int inW = x.value().dim(4);
        int kD = w.value().dim(2);
        int kH = w.value().dim(3);
        int kW = w.value().dim(4);
        int outD = this.value().dim(2);
        int outH = this.value().dim(3);
        int outW = this.value().dim(4);
        int outputPaddingD = inD - ((outD - 1) * stride - 2 * padding + dilation * (kD - 1) + 1);
        int outputPaddingH = inH - ((outH - 1) * stride - 2 * padding + dilation * (kH - 1) + 1);
        int outputPaddingW = inW - ((outW - 1) * stride - 2 * padding + dilation * (kW - 1) + 1);
        // convTranspose3d takes a single outputPadding, which only appends positions at the end of each spatial
        // dimension. The positions before them do not depend on it, so the largest padding is used and every
        // dimension which came out too long is cut back to the input size
        int outputPadding = Math.max(outputPaddingD, Math.max(outputPaddingH, outputPaddingW));
        backEdge(x, () -> {
            DArray<?> gx = this.grad().convTranspose3d(w.value(), null, stride, padding, dilation, groups, outputPadding);
            if (gx.dim(2) != inD) {
                gx = gx.narrow(2, true, 0, inD);
            }
            if (gx.dim(3) != inH) {
                gx = gx.narrow(3, true, 0, inH);
            }
            if (gx.dim(4) != inW) {
                gx = gx.narrow(4, true, 0, inW);
            }
            return gx;
        });

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
            DArray<?> gradWFlat = gradW.reshape(Shape.of(w.value().dim(0), inDepth * kD * kH * kW), Order.C);

            var xBatches = x.value().chunk(0, false, 1);    // (C_in, D, H, W)
            var gyBatches = this.grad().chunk(0, false, 1); // (C_out, outD, outH, outW)

            for (int batch = 0; batch < n; batch++) {
                var xSlices = xBatches.get(batch).chunk(0, true, inDepth);
                var gySlices = gyBatches.get(batch).chunk(0, true, outDepth);
                for (int group = 0; group < groups; group++) {
                    // unfold: (inDepth*kD*kH*kW, outD*outH*outW)
                    DArray<?> unfold = xSlices.get(group).unfold3d(kD, kH, kW, stride, padding, dilation);
                    // gyFlat: (outDepth, outD*outH*outW). The order is pinned to C, since the gradient reaching this
                    // node carries whatever layout the upstream operation left it in, and Order.A would read an F
                    // ordered one column major and pair the wrong positions with the unfolded input
                    DArray<?> gyFlat = gySlices.get(group).reshape(Shape.of(outDepth, outD * outH * outW), Order.C);

                    var gradWn = gradWFlat.narrow(0, group * outDepth, (group + 1) * outDepth);
                    gyFlat.mm(unfold.t_(), gradWn);
                }
            }
            return gradW;
        });

        if (b != null) {
            // grad w.r.t. bias: sum over W (dim 4), H (dim 3), D (dim 2), then batch (dim 0)
            backEdge(b, () -> this.grad().sum1d(4).sum1d(3).sum1d(2).sum1d(0));
        }
    }
}
