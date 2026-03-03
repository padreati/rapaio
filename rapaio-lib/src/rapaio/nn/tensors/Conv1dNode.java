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
import rapaio.darray.Shape;
import rapaio.nn.Tensor;

/**
 * 1D convolution tensor with gradient computation.
 * <p>
 * Forward: y = conv1d(x, w, b, padding, stride, dilation, groups)
 * <p>
 * Gradients:
 * <ul>
 *   <li>grad_x = convTranspose1d(grad_y, w)</li>
 *   <li>grad_w via conv1d of input with grad_y</li>
 *   <li>grad_b = grad_y.sum(dims=[0,2])</li>
 * </ul>
 */
public final class Conv1dNode extends Tensor {

    public Conv1dNode(Tensor x, Tensor w, Tensor b, int padding, int stride, int dilation, int groups) {
        super(x.tm(), Conv1dNode.class.getSimpleName());

        this.setValue(x.value().conv1d(w.value(), b != null ? b.value() : null, padding, stride, dilation, groups));

        // grad w.r.t. input: transposed convolution
        int lIn = x.value().dim(2);
        int k = w.value().dim(2);
        int lOut = this.value().dim(2);
        int outputPadding = lIn - ((lOut - 1) * stride - 2 * padding + dilation * (k - 1) + 1);
        backEdge(x, () ->
                this.grad().convTranspose1d(w.value(), null, padding, stride, dilation, groups, outputPadding)
        );

        // grad w.r.t. weight via im2col: grad_w[g] = sum_batch( grad_y[g] @ unfold(x[g]).T )
        backEdge(w, () -> {
            // grad_w[g] = sum_batch( grad_y[g] @ unfold(x[g]).T ) reshaped to (outDepth, inDepth, k)
            int n = x.value().dim(0);
            int inDepth = w.value().dim(1);
            int outDepth = w.value().dim(0) / groups;
            DArray<?> gradW = tm.zerosArray(w.value().shape());
            for (int batch = 0; batch < n; batch++) {
                DArray<?> xBatch = x.value().selsq(0, batch);       // (C_in, L_in)
                DArray<?> gyBatch = this.grad().selsq(0, batch);    // (C_out, L_out)

                var xSlices = xBatch.chunk(0, true, inDepth);
                var gySlices = gyBatch.chunk(0, true, outDepth);

                for (int group = 0; group < groups; group++) {
                    DArray<?> unfold = xSlices.get(group).unfold1d(k, padding, stride, dilation);          // (inDepth*k, L_out)
                    var gradWn = gradW.narrow(0, group * outDepth, (group + 1) * outDepth).reshape(Shape.of(outDepth, inDepth * k));
                    gySlices.get(group).mm(unfold.t_(), gradWn); // (outDepth, inDepth, k)
                }
            }
            return gradW;
        });


        if (b != null) {
            // grad w.r.t. bias: sum over spatial (dim 2) then batch (dim 0)
            backEdge(b, () -> this.grad().sum1d(2).sum1d(0));
        }
    }
}



