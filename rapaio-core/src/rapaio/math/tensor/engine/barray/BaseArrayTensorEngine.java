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

package rapaio.math.tensor.engine.barray;

import java.util.Random;

import rapaio.core.distributions.Normal;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.engine.AbstractEngineOfType;
import rapaio.math.tensor.engine.AbstractTensorEngine;
import rapaio.math.tensor.storage.array.ArrayStorageFactory;
import rapaio.util.Hardware;

public class BaseArrayTensorEngine extends AbstractTensorEngine {

    public BaseArrayTensorEngine() {
        this(Hardware.CORES);
    }

    public BaseArrayTensorEngine(int cpuThreads) {
        super(cpuThreads, new BaseArrayOfDouble(), new BaseArrayOfFloat(), new BaseArrayOfInt(), new BaseArrayOfByte(), new ArrayStorageFactory());
    }

    protected static class BaseArrayOfDouble extends AbstractEngineOfType<Double> {

        public BaseArrayOfDouble() {
            super(DType.DOUBLE);
        }

        @Override
        public final Tensor<Double> random(Shape shape, Random random, Order order) {
            Normal normal = Normal.std();
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> normal.sampleNext(random));
        }


        @Override
        public Tensor<Double> stride(Shape shape, int offset, int[] strides, Storage<Double> storage) {
            return new BaseDoubleTensorStride(parent, shape, offset, strides, storage);
        }
    }

    protected static class BaseArrayOfFloat extends AbstractEngineOfType<Float> {

        public BaseArrayOfFloat() {
            super(DType.FLOAT);
        }

        @Override
        public final Tensor<Float> random(Shape shape, Random random, Order order) {
            Normal normal = Normal.std();
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> (float)normal.sampleNext(random));
        }

        @Override
        public Tensor<Float> stride(Shape shape, int offset, int[] strides, Storage<Float> storage) {
            return new BaseFloatTensorStride(parent, shape, offset, strides, storage);
        }
    }

    protected static class BaseArrayOfInt extends AbstractEngineOfType<Integer> {

        public BaseArrayOfInt() {
            super(DType.INTEGER);
        }

        @Override
        public final Tensor<Integer> random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> random.nextInt());
        }

        @Override
        public Tensor<Integer> stride(Shape shape, int offset, int[] strides, Storage<Integer> storage) {
            return new BaseIntTensorStride(parent, shape, offset, strides, storage);
        }
    }

    protected static class BaseArrayOfByte extends AbstractEngineOfType<Byte> {

        public BaseArrayOfByte() {
            super(DType.BYTE);
        }

        @Override
        public final Tensor<Byte> random(Shape shape, Random random, Order order) {
            byte[] buff = new byte[1];
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, p) -> {
                random.nextBytes(buff);
                return buff[0];
            });
        }

        @Override
        public Tensor<Byte> stride(Shape shape, int offset, int[] strides, Storage<Byte> storage) {
            return new BaseByteTensorStride(parent, shape, offset, strides, storage);
        }
    }
}
