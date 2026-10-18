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

import rapaio.nn.NetworkState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

/**
 * 3D max pooling layer, for inputs of shape {@code (N, C, D, H, W)} or {@code (C, D, H, W)}. It has no parameters.
 * Stride, padding and dilation are the same on all spatial dimensions.
 */
public class MaxPool3D extends AbstractNetwork {

    private final int kD;
    private final int kH;
    private final int kW;
    private final int stride;
    private final int padding;
    private final int dilation;
    private final boolean ceilMode;

    /**
     * Pooling with a cubic {@code k x k x k} window, moved by {@code k} positions, without padding.
     */
    public MaxPool3D(TensorManager tm, int k) {
        this(tm, k, k, k, k, 0, 1, false);
    }

    public MaxPool3D(TensorManager tm, int kD, int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        super(tm);
        this.kD = kD;
        this.kH = kH;
        this.kW = kW;
        this.stride = stride;
        this.padding = padding;
        this.dilation = dilation;
        this.ceilMode = ceilMode;
    }

    @Override
    public List<Tensor> parameters() {
        return List.of();
    }

    @Override
    public NetworkState state() {
        return new NetworkState();
    }

    @Override
    public Tensor forward11(Tensor x) {
        return x.maxPool3d(kD, kH, kW, stride, padding, dilation, ceilMode);
    }
}
