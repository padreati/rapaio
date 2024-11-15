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

package rapaio.math.narrays.manager.base;

import java.util.Random;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.math.narrays.DType;
import rapaio.math.narrays.NArray;
import rapaio.math.narrays.NArrayManager;
import rapaio.math.narrays.Order;
import rapaio.math.narrays.Shape;
import rapaio.math.narrays.Storage;
import rapaio.math.narrays.StorageManager;
import rapaio.math.narrays.layout.StrideLayout;

public class BaseNArrayManager extends NArrayManager {

    public BaseNArrayManager(int cpuThreads) {
        super(cpuThreads,
                new BaseArrayOfByte(),
                new BaseArrayOfInt(),
                new BaseArrayOfFloat(),
                new BaseArrayOfDouble(),
                StorageManager.array());
    }

    protected static class BaseArrayOfDouble extends NArrayManager.OfType<Double> {

        public BaseArrayOfDouble() {
            super(DType.DOUBLE);
        }

        @Override
        public final NArray<Double> random(Shape shape, Random random, Order order) {
            Normal normal = Normal.std();
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> normal.sampleNext(random));
        }

        @Override
        public NArray<Double> random(Shape shape, Distribution dist, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> dist.sampleNext(random));
        }

        @Override
        public NArray<Double> stride(StrideLayout layout, Storage<Double> storage) {
            return new BaseDoubleNArrayStride(parent, layout, storage);
        }
    }

    protected static class BaseArrayOfFloat extends NArrayManager.OfType<Float> {

        public BaseArrayOfFloat() {
            super(DType.FLOAT);
        }

        @Override
        public final NArray<Float> random(Shape shape, Random random, Order order) {
            Normal normal = Normal.std();
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> (float) normal.sampleNext(random));
        }

        @Override
        public NArray<Float> random(Shape shape, Distribution dist, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> (float) dist.sampleNext(random));
        }

        @Override
        public NArray<Float> stride(StrideLayout layout, Storage<Float> storage) {
            return new BaseFloatNArrayStride(parent, layout, storage);
        }
    }

    protected static class BaseArrayOfInt extends NArrayManager.OfType<Integer> {

        public BaseArrayOfInt() {
            super(DType.INTEGER);
        }

        @Override
        public final NArray<Integer> random(Shape shape, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> random.nextInt());
        }

        @Override
        public NArray<Integer> random(Shape shape, Distribution dist, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> (int) dist.sampleNext(random));
        }

        @Override
        public NArray<Integer> stride(StrideLayout layout, Storage<Integer> storage) {
            return new BaseIntNArrayStride(parent, layout, storage);
        }
    }

    protected static class BaseArrayOfByte extends NArrayManager.OfType<Byte> {

        public BaseArrayOfByte() {
            super(DType.BYTE);
        }

        @Override
        public final NArray<Byte> random(Shape shape, Random random, Order order) {
            byte[] buff = new byte[shape.size()];
            random.nextBytes(buff);
            return zeros(shape, Order.autoFC(order)).apply_(order, (i, _) -> buff[i]);
        }

        @Override
        public NArray<Byte> random(Shape shape, Distribution dist, Random random, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(order, (_, _) -> (byte) dist.sampleNext(random));
        }

        @Override
        public NArray<Byte> stride(StrideLayout layout, Storage<Byte> storage) {
            return new BaseByteNArrayStride(parent, layout, storage);
        }
    }
}
