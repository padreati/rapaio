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

package rapaio.math.tensor.engine.basearray;

import java.util.Random;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.engine.AbstractTensorEngine;

public class BaseArrayTensorEngine extends AbstractTensorEngine {

    private static final class ImplOfDouble implements OfDouble {

        private final BaseArrayTensorEngine self;

        ImplOfDouble(BaseArrayTensorEngine self) {
            this.self = self;
        }

        @Override
        public DTensor zeros(Shape shape, Order order) {
            return new DTensorStride(self, shape, 0, Order.autoFC(order), new double[shape.size()]);
        }

        @Override
        public DTensor seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(Order.C, (i, p) -> (double) i);
        }

        @Override
        public DTensor random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(order, (i, p) -> random.nextDouble());
        }

        @Override
        public DTensor wrap(Shape shape, double[] array, Order order) {
            return new DTensorStride(self, shape, 0, Order.autoFC(order), array);
        }

        @Override
        public DTensor stride(Shape shape, int offset, int[] strides, double[] array) {
            return new DTensorStride(self, shape, offset, strides, array);
        }
    }

    private static final class ImplOfFloat implements OfFloat {

        private final BaseArrayTensorEngine self;

        ImplOfFloat(BaseArrayTensorEngine self) {
            this.self = self;
        }

        @Override
        public FTensor zeros(Shape shape, Order order) {
            return new FTensorStride(self, shape, 0, Order.autoFC(order), new float[shape.size()]);
        }

        @Override
        public FTensor seq(Shape shape, Order order) {
            return zeros(shape, order).iteratorApply(Order.C, (i, _) -> (float) i);
        }

        @Override
        public FTensor random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(Order.C, (_, __) -> random.nextFloat());
        }

        @Override
        public FTensor wrap(Shape shape, float[] array, Order order) {
            return new FTensorStride(self, shape, 0, Order.autoFC(order), array);
        }

        @Override
        public FTensor stride(Shape shape, int offset, int[] strides, float[] array) {
            return new FTensorStride(self, shape, offset, strides, array);
        }
    }

    private final OfDouble _ofDouble = new ImplOfDouble(this);
    private final OfFloat _ofFloat = new ImplOfFloat(this);

    @Override
    public OfDouble ofDouble() {
        return _ofDouble;
    }

    @Override
    public OfFloat ofFloat() {
        return _ofFloat;
    }

}
