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

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.engine.AbstractEngine;

public class BaseArrayEngine extends AbstractEngine {

    private final BaseArrayOfDouble ofDouble = new BaseArrayOfDouble(this);
    private final BaseArrayOfFloat ofFloat = new BaseArrayOfFloat(this);

    @Override
    public OfType<Double, DTensor> ofDouble() {
        return ofDouble;
    }

    @Override
    public OfType<Float, FTensor> ofFloat() {
        return ofFloat;
    }

    protected static class BaseArrayOfDouble extends AbstractOfDouble {

        private final BaseArrayEngine parent;

        public BaseArrayOfDouble(BaseArrayEngine parent) {
            this.parent = parent;
        }

        @Override
        public DType<Double, DTensor> dtype() {
            return DType.DOUBLE;
        }

        @Override
        public DTensor zeros(Shape shape, Order order) {
            return new DTensorStride(parent, shape, 0, Order.autoFC(order), new double[shape.size()]);
        }

        @Override
        public DTensor stride(Shape shape, int offset, int[] strides, float[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public DTensor stride(Shape shape, int offset, int[] strides, double[] array) {
            return new DTensorStride(parent, shape, offset, strides, array);
        }
    }

    protected static class BaseArrayOfFloat extends AbstractOfFloat {

        private final BaseArrayEngine parent;

        public BaseArrayOfFloat(BaseArrayEngine parent) {
            this.parent = parent;
        }

        @Override
        public DType<Float, FTensor> dtype() {
            return DType.FLOAT;
        }

        @Override
        public FTensor zeros(Shape shape, Order order) {
            return new FTensorStride(parent, shape, 0, Order.autoFC(order), new float[shape.size()]);
        }

        @Override
        public FTensor stride(Shape shape, int offset, int[] strides, float[] array) {
            return new FTensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public FTensor stride(Shape shape, int offset, int[] strides, double[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }


}
