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
 * 2D max pooling layer, for inputs of shape {@code (N, C, H, W)} or {@code (C, H, W)}. It has no parameters.
 * Stride, padding and dilation are the same on both spatial dimensions.
 */
public class MaxPool2D extends AbstractNetwork {

    private final int kH;
    private final int kW;
    private final int stride;
    private final int padding;
    private final int dilation;
    private final boolean ceilMode;

    /**
     * Pooling with a square {@code k x k} window, moved by {@code k} positions, without padding.
     */
    public MaxPool2D(TensorManager tm, int k) {
        this(tm, k, k, k, 0, 1, false);
    }

    public MaxPool2D(TensorManager tm, int kH, int kW, int stride, int padding, int dilation, boolean ceilMode) {
        super(tm);
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
        return x.maxPool2d(kH, kW, stride, padding, dilation, ceilMode);
    }
}
