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

package rapaio.nn.layer;

import java.util.List;

import rapaio.core.distributions.Uniform;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.NetworkState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

/**
 * 3D convolution layer.
 * <p>
 * Input shape {@code (N, C_in, D, H, W)}, output shape {@code (N, C_out, D_out, H_out, W_out)}. The weight has shape
 * {@code (C_out, C_in/groups, kD, kH, kW)} and the optional bias has shape {@code (C_out)}. Stride, padding and
 * dilation are the same on all three spatial dimensions.
 * <p>
 * Weight and bias are initialized from {@code U(-1/sqrt(fanIn), 1/sqrt(fanIn))} with
 * {@code fanIn = C_in/groups * kD * kH * kW}, which is the default initialization of PyTorch's {@code Conv3d}.
 */
public class Conv3D extends AbstractNetwork {

    private final int inChannels;
    private final int outChannels;

    private final int kD;
    private final int kH;
    private final int kW;
    private final int stride;
    private final int padding;
    private final int dilation;
    private final int groups;
    private final boolean bias;

    private final Tensor w;
    private final Tensor b;

    public Conv3D(TensorManager tm, int inChannels, int outChannels,
            int kD, int kH, int kW, int stride, int padding, int dilation, int groups, boolean bias) {
        super(tm);

        if (groups <= 0 || inChannels % groups != 0 || outChannels % groups != 0) {
            throw new IllegalArgumentException(String.format(
                    "Input channels (%d) and output channels (%d) must be multiples of groups (%d).",
                    inChannels, outChannels, groups));
        }

        this.inChannels = inChannels;
        this.outChannels = outChannels;

        this.kD = kD;
        this.kH = kH;
        this.kW = kW;
        this.stride = stride;
        this.padding = padding;
        this.dilation = dilation;
        this.groups = groups;
        this.bias = bias;

        double range = 1. / Math.sqrt((double) (inChannels / groups) * kD * kH * kW);

        w = tm.var().requiresGrad(true).name("weight");
        w.setValue(tm.randomArray(Shape.of(outChannels, inChannels / groups, kD, kH, kW), Uniform.of(-range, range), Order.F));
        if (bias) {
            b = tm.var().requiresGrad(true).name("bias");
            b.setValue(tm.randomArray(Shape.of(outChannels), Uniform.of(-range, range)));
        } else {
            b = null;
        }
    }

    @Override
    public List<Tensor> parameters() {
        return bias ? List.of(w, b) : List.of(w);
    }

    @Override
    public NetworkState state() {
        NetworkState state = new NetworkState();
        state.addTensors(bias ? List.of(w, b) : List.of(w));
        return state;
    }

    @Override
    public Tensor forward11(Tensor x) {
        if (x.rank() != 5 || x.dim(1) != inChannels) {
            throw new IllegalArgumentException(String.format(
                    "Input must have shape (N, %d, D, H, W), but has shape %s.", inChannels, x.shape()));
        }
        return x.conv3d(w, b, stride, padding, dilation, groups);
    }
}
