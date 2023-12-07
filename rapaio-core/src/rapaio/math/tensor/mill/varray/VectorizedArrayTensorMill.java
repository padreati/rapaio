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

package rapaio.math.tensor.mill.varray;

import java.util.Arrays;
import java.util.Random;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.ITensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.mill.AbstractTensorMill;
import rapaio.util.Hardware;

public class VectorizedArrayTensorMill extends AbstractTensorMill {

    private final BaseArrayOfDouble ofDouble = new BaseArrayOfDouble(this);
    private final BaseArrayOfFloat ofFloat = new BaseArrayOfFloat(this);
    private final BaseArrayOfInt ofInt = new BaseArrayOfInt(this);
    private final int cpuThreads;

    public VectorizedArrayTensorMill() {
        this(Hardware.CORES);
    }

    public VectorizedArrayTensorMill(int cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    @Override
    public int cpuThreads() {
        return cpuThreads;
    }

    @Override
    public OfType<Double, DTensor> ofDouble() {
        return ofDouble;
    }

    @Override
    public OfType<Float, FTensor> ofFloat() {
        return ofFloat;
    }

    @Override
    public OfType<Integer, ITensor> ofInt() {
        return ofInt;
    }

    protected static class BaseArrayOfDouble implements OfType<Double, DTensor> {

        private final VectorizedArrayTensorMill parent;

        public BaseArrayOfDouble(VectorizedArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Double, DTensor> dtype() {
            return DType.DOUBLE;
        }

        @Override
        public VectorizedDTensorStride zeros(Shape shape, Order order) {
            return new VectorizedDTensorStride(parent, shape, 0, Order.autoFC(order), new double[shape.size()]);
        }

        @Override
        public VectorizedDTensorStride eye(int n, Order order) {
            VectorizedDTensorStride eye = zeros(Shape.of(n, n), order);
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
        public VectorizedDTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply(Order.C, (i, p) -> (double) i);
        }

        @Override
        public VectorizedDTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply(order, (i, p) -> random.nextDouble());
        }

        @Override
        public VectorizedDTensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public VectorizedDTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public VectorizedDTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            return new VectorizedDTensorStride(parent, shape, offset, strides, array);
        }
    }

    protected static class BaseArrayOfFloat implements OfType<Float, FTensor> {

        private final VectorizedArrayTensorMill parent;

        public BaseArrayOfFloat(VectorizedArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Float, FTensor> dtype() {
            return DType.FLOAT;
        }

        @Override
        public VectorizedFTensorStride zeros(Shape shape, Order order) {
            return new VectorizedFTensorStride(parent, shape, 0, Order.autoFC(order), new float[shape.size()]);
        }

        @Override
        public VectorizedFTensorStride eye(int n, Order order) {
            VectorizedFTensorStride eye = zeros(Shape.of(n, n), order);
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
        public VectorizedFTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply(Order.C, (i, p) -> (float) i);
        }

        @Override
        public VectorizedFTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply(order, (i, p) -> random.nextFloat());
        }

        @Override
        public VectorizedFTensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public VectorizedFTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            return new VectorizedFTensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public VectorizedFTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }

    protected static class BaseArrayOfInt implements OfType<Integer, ITensor> {

        private final VectorizedArrayTensorMill parent;

        public BaseArrayOfInt(VectorizedArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Integer, ITensor> dtype() {
            return DType.INTEGER;
        }

        @Override
        public VectorizedITensorStride zeros(Shape shape, Order order) {
            return new VectorizedITensorStride(parent, shape, 0, Order.autoFC(order), new int[shape.size()]);
        }

        @Override
        public VectorizedITensorStride eye(int n, Order order) {
            VectorizedITensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setInt(1, i, i);
            }
            return eye;
        }

        @Override
        public ITensor full(Shape shape, Integer value, Order order) {
            float[] array = new float[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public VectorizedITensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply(Order.C, (i, p) -> (int) i);
        }

        @Override
        public VectorizedITensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply(order, (i, p) -> random.nextInt());
        }

        @Override
        public VectorizedITensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            return new VectorizedITensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public VectorizedITensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            int[] darray = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (int) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public VectorizedITensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            int[] darray = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (int) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }


}
