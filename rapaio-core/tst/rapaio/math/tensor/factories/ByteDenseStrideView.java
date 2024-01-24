/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.factories;

import java.util.Arrays;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.layout.StrideLayout;

public final class ByteDenseStrideView extends ByteDense {

    public ByteDenseStrideView(TensorEngine manager) {
        super(manager);
    }

    @Override
    public Tensor<Byte> seq(Shape shape) {
        var t = zeros(shape);
        t.apply_(Order.C, (i, p) -> (byte) i);
        return t;
    }

    @Override
    public Tensor<Byte> zeros(Shape shape) {
        int offset = 7;
        var l = StrideLayout.ofDense(shape, offset, Order.F);
        int[] strides = Arrays.copyOf(l.strides(), l.strides().length);
        for (int i = 0; i < strides.length; i++) {
            if (i == 0) {
                strides[0]++;
                continue;
            }
            strides[i] = l.dim(i - 1) * strides[i - 1] + 1;
        }
        int len = offset;
        for (int i = 0; i < l.strides().length; i++) {
            len += l.dim(i) * strides[i];
        }
        return engine.ofByte().stride(StrideLayout.of(shape, offset, strides), engine.ofByte().storage().zeros(len));
    }

    @Override
    public Tensor<Byte> random(Shape shape) {
        var t = zeros(shape);
        byte[] buff = new byte[1];
        random.nextBytes(buff);
        t.apply_(Order.C, (pos, ptr) -> buff[0]);
        return t;
    }
}