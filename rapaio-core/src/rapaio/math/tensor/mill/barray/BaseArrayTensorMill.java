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

package rapaio.math.tensor.mill.barray;

import java.util.Arrays;
import java.util.Random;

import rapaio.math.tensor.ByteTensor;
import rapaio.math.tensor.DoubleTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.DTypes;
import rapaio.math.tensor.FloatTensor;
import rapaio.math.tensor.IntTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.mill.AbstractTensorMill;
import rapaio.util.Hardware;

public class BaseArrayTensorMill extends AbstractTensorMill {

    private final BaseArrayOfDouble ofDouble = new BaseArrayOfDouble(this);
    private final BaseArrayOfFloat ofFloat = new BaseArrayOfFloat(this);
    private final BaseArrayOfInt ofInt = new BaseArrayOfInt(this);
    private final BaseArrayOfByte ofByte = new BaseArrayOfByte(this);
    private final int cpuThreads;

    public BaseArrayTensorMill() {
        this(Hardware.CORES);
    }

    public BaseArrayTensorMill(int cpuThreads) {
        this.cpuThreads = cpuThreads;
    }

    @Override
    public int cpuThreads() {
        return cpuThreads;
    }

    @Override
    public OfType<Double, DoubleTensor> ofDouble() {
        return ofDouble;
    }

    @Override
    public OfType<Float, FloatTensor> ofFloat() {
        return ofFloat;
    }

    @Override
    public OfType<Integer, IntTensor> ofInt() {
        return ofInt;
    }

    @Override
    public OfType<Byte, ByteTensor> ofByte() {
        return ofByte;
    }

    protected static class BaseArrayOfDouble implements OfType<Double, DoubleTensor> {

        private final BaseArrayTensorMill parent;

        public BaseArrayOfDouble(BaseArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Double, DoubleTensor> dtype() {
            return DTypes.DOUBLE;
        }

        @Override
        public DoubleTensor scalar(Double value) {
            return stride(Shape.of(), 0, new int[0], new double[] {value});
        }

        @Override
        public BaseDoubleTensorStride zeros(Shape shape, Order order) {
            return new BaseDoubleTensorStride(parent, shape, 0, Order.autoFC(order), new double[shape.size()]);
        }

        @Override
        public BaseDoubleTensorStride eye(int n, Order order) {
            BaseDoubleTensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setDouble(1, i, i);
            }
            return eye;
        }

        @Override
        public DoubleTensor full(Shape shape, Double value, Order order) {
            double[] array = new double[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public BaseDoubleTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, p) -> (double) i);
        }

        @Override
        public BaseDoubleTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> random.nextDouble());
        }

        @Override
        public BaseDoubleTensorStride stride(Shape shape, int offset, int[] strides, byte[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseDoubleTensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseDoubleTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            double[] darray = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseDoubleTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            return new BaseDoubleTensorStride(parent, shape, offset, strides, array);
        }
    }

    protected static class BaseArrayOfFloat implements OfType<Float, FloatTensor> {

        private final BaseArrayTensorMill parent;

        public BaseArrayOfFloat(BaseArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Float, FloatTensor> dtype() {
            return DTypes.FLOAT;
        }

        @Override
        public FloatTensor scalar(Float value) {
            return stride(Shape.of(), 0, new int[0], new float[] {value});
        }

        @Override
        public BaseFloatTensorStride zeros(Shape shape, Order order) {
            return new BaseFloatTensorStride(parent, shape, 0, Order.autoFC(order), new float[shape.size()]);
        }

        @Override
        public BaseFloatTensorStride eye(int n, Order order) {
            BaseFloatTensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setFloat(1, i, i);
            }
            return eye;
        }

        @Override
        public FloatTensor full(Shape shape, Float value, Order order) {
            float[] array = new float[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public BaseFloatTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, p) -> (float) i);
        }

        @Override
        public BaseFloatTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> random.nextFloat());
        }

        @Override
        public BaseFloatTensorStride stride(Shape shape, int offset, int[] strides, byte[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseFloatTensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseFloatTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            return new BaseFloatTensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public BaseFloatTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            float[] darray = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (float) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }

    protected static class BaseArrayOfInt implements OfType<Integer, IntTensor> {

        private final BaseArrayTensorMill parent;

        public BaseArrayOfInt(BaseArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Integer, IntTensor> dtype() {
            return DTypes.INTEGER;
        }

        @Override
        public IntTensor scalar(Integer value) {
            return stride(Shape.of(), 0, new int[0], new int[] {value});
        }

        @Override
        public BaseIntTensorStride zeros(Shape shape, Order order) {
            return new BaseIntTensorStride(parent, shape, 0, Order.autoFC(order), new int[shape.size()]);
        }

        @Override
        public BaseIntTensorStride eye(int n, Order order) {
            BaseIntTensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setInt(1, i, i);
            }
            return eye;
        }

        @Override
        public IntTensor full(Shape shape, Integer value, Order order) {
            int[] array = new int[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public BaseIntTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, p) -> (int) i);
        }

        @Override
        public BaseIntTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> random.nextInt());
        }

        @Override
        public BaseIntTensorStride stride(Shape shape, int offset, int[] strides, byte[] array) {
            int[] darray = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseIntTensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            return new BaseIntTensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public BaseIntTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            int[] darray = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (int) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseIntTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            int[] darray = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (int) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }

    protected static class BaseArrayOfByte implements OfType<Byte, ByteTensor> {

        private final BaseArrayTensorMill parent;

        public BaseArrayOfByte(BaseArrayTensorMill parent) {
            this.parent = parent;
        }

        @Override
        public DType<Byte, ByteTensor> dtype() {
            return DTypes.BYTE;
        }

        @Override
        public ByteTensor scalar(Byte value) {
            return stride(Shape.of(), 0, new int[0], new byte[] {value});
        }

        @Override
        public BaseByteTensorStride zeros(Shape shape, Order order) {
            return new BaseByteTensorStride(parent, shape, 0, Order.autoFC(order), new byte[shape.size()]);
        }

        @Override
        public BaseByteTensorStride eye(int n, Order order) {
            BaseByteTensorStride eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.setByte((byte) 1, i, i);
            }
            return eye;
        }

        @Override
        public ByteTensor full(Shape shape, Byte value, Order order) {
            byte[] array = new byte[shape.size()];
            Arrays.fill(array, value);
            return stride(shape, Order.autoFC(order), array);
        }

        @Override
        public BaseByteTensorStride seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, p) -> (byte) i);
        }

        @Override
        public BaseByteTensorStride random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> {
                byte[] buff = new byte[1];
                random.nextBytes(buff);
                return buff[0];
            });
        }

        @Override
        public BaseByteTensorStride stride(Shape shape, int offset, int[] strides, byte[] array) {
            return new BaseByteTensorStride(parent, shape, offset, strides, array);
        }

        @Override
        public BaseByteTensorStride stride(Shape shape, int offset, int[] strides, int[] array) {
            byte[] darray = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (byte) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseByteTensorStride stride(Shape shape, int offset, int[] strides, float[] array) {
            byte[] darray = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (byte) array[i];
            }
            return stride(shape, offset, strides, darray);
        }

        @Override
        public BaseByteTensorStride stride(Shape shape, int offset, int[] strides, double[] array) {
            byte[] darray = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                darray[i] = (byte) array[i];
            }
            return stride(shape, offset, strides, darray);
        }
    }


}
