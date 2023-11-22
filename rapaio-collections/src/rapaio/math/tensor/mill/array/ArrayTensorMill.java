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

package rapaio.math.tensor.mill.array;

import java.util.Arrays;
import java.util.Random;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.mill.AbstractTensorMill;

public class ArrayTensorMill extends AbstractTensorMill {

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

    protected static class BaseArrayOfDouble implements OfType<Double, DTensor> {

        private final ArrayTensorMill parent;

        public BaseArrayOfDouble(ArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Double, DTensor> dtype() {
            return DType.DOUBLE;
        }

        @Override
        public DTensorStride zeros(Shape shape, Order order) {
            return new DTensorStride(parent, shape, 0, Order.autoFC(order), new double[shape.size()]);
        }

        @Override
        public DTensorStride eye(int n, Order order) {
            DTensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setDouble(1, i, i);
            }
            return eye;
        }

        @Override
        public DTensor full(Shape shape, Double value, Order order) {
            double[] array = new double[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public DTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(Order.C, (i, p) -> (double) i);
        }

        @Override
        public DTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(order, (i, p) -> random.nextDouble());
        }

        @Override
        public DTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public DTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            return new DTensorStride(parent, shape, offset, strides, array);
        }
    }

    protected static class BaseArrayOfFloat implements OfType<Float, FTensor> {

        private final ArrayTensorMill parent;

        public BaseArrayOfFloat(ArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Float, FTensor> dtype() {
            return DType.FLOAT;
        }

        @Override
        public FTensorStride zeros(Shape shape, Order order) {
            return new FTensorStride(parent, shape, 0, Order.autoFC(order), new float[shape.size()]);
        }

        @Override
        public FTensorStride eye(int n, Order order) {
            FTensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setFloat(1, i, i);
            }
            return eye;
        }

        @Override
        public FTensor full(Shape shape, Float value, Order order) {
            float[] array = new float[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public FTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(Order.C, (i, p) -> (float) i);
        }

        @Override
        public FTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).iteratorApply(order, (i, p) -> random.nextFloat());
        }

        @Override
        public FTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            return new FTensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public FTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }


}
