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

package rapaio.math.narrays.factories;

import java.util.Arrays;

import rapaio.core.distributions.Normal;
import rapaio.math.narrays.NArray;
import rapaio.math.narrays.NArrayManager;
import rapaio.math.narrays.Order;
import rapaio.math.narrays.Shape;
import rapaio.math.narrays.layout.StrideLayout;

public final class FloatDenseStrideView extends FloatDense {

    public FloatDenseStrideView(NArrayManager manager) {
        super(manager);
    }

    @Override
    public NArray<Float> seq(Shape shape) {
        var t = zeros(shape);
        t.apply_(Order.C, (i, p) -> (float) i);
        return t;
    }

    @Override
    public NArray<Float> zeros(Shape shape) {
        int offset = 7;
        var l = StrideLayout.ofDense(shape, offset, Order.F);
        int[] strides = Arrays.copyOf(l.strides(), l.strides().length);
        for (int i = 0; i < strides.length; i++) {
            if(i==0) {
                strides[0]++;
                continue;
            }
            strides[i] = l.dim(i - 1) * strides[i - 1] + 1;
        }
        int len = offset + 1;
        for (int i = 0; i < l.strides().length; i++) {
            len += l.dim(i) * strides[i];
        }
        return engine.ofFloat().stride(StrideLayout.of(shape, offset, strides), ofType.storage().zeros(len));
    }

    @Override
    public NArray<Float> random(Shape shape) {
        var t = zeros(shape);
        Normal normal = Normal.std();
        t.apply_(Order.C, (pos, ptr) -> (float) normal.sampleNext(random));
        return t;
    }
}
