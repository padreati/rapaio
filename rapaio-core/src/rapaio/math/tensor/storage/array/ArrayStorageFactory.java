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

package rapaio.math.tensor.storage.array;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.StorageFactory;

public class ArrayStorageFactory implements StorageFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> OfType<N> ofType(DType<N> dType) {
        if (DType.DOUBLE == dType) {
            return (OfType<N>) new OfTypeDouble();
        }
        if (DType.FLOAT == dType) {
            return (OfType<N>) new OfTypeFloat();
        }
        if (DType.INTEGER == dType) {
            return (OfType<N>) new OfTypeInt();
        }
        if (DType.BYTE == dType) {
            return (OfType<N>) new OfTypeByte();
        }
        throw new IllegalArgumentException(STR."DType \{dType} unrecognized.");
    }

    private static final class OfTypeByte implements StorageFactory.OfType<Byte> {

        @Override
        public <M extends Number> Storage<Byte> scalar(M value) {
            return new ByteArrayStorage(new byte[] {value.byteValue()});
        }

        @Override
        public Storage<Byte> zeros(int len) {
            return new ByteArrayStorage(new byte[len]);
        }

        @Override
        public Storage<Byte> cast(byte... array) {
            return new ByteArrayStorage(array);
        }

        @Override
        public Storage<Byte> cast(int... array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (byte) array[i];
            }
            return new ByteArrayStorage(copy);
        }

        @Override
        public Storage<Byte> cast(float... array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (byte) array[i];
            }
            return new ByteArrayStorage(copy);
        }

        @Override
        public Storage<Byte> cast(double... array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (byte) array[i];
            }
            return new ByteArrayStorage(copy);
        }

        @Override
        public <M extends Number> Storage<Byte> cast(Storage<M> source) {
            byte[] copy = new byte[source.size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = source.getByte(i);
            }
            return new ByteArrayStorage(copy);
        }
    }

    private static final class OfTypeInt implements StorageFactory.OfType<Integer> {

        @Override
        public <M extends Number> Storage<Integer> scalar(M value) {
            return new IntArrayStorage(new int[] {value.intValue()});
        }

        @Override
        public Storage<Integer> zeros(int len) {
            return new IntArrayStorage(new int[len]);
        }

        @Override
        public Storage<Integer> cast(byte... array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new IntArrayStorage(copy);
        }

        @Override
        public Storage<Integer> cast(int... array) {
            return new IntArrayStorage(array);
        }

        @Override
        public Storage<Integer> cast(float... array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (int) array[i];
            }
            return new IntArrayStorage(copy);
        }

        @Override
        public Storage<Integer> cast(double... array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (int) array[i];
            }
            return new IntArrayStorage(copy);
        }

        @Override
        public <M extends Number> Storage<Integer> cast(Storage<M> source) {
            int[] copy = new int[source.size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = source.getInt(i);
            }
            return new IntArrayStorage(copy);
        }
    }

    private static final class OfTypeFloat implements StorageFactory.OfType<Float> {
        @Override
        public <M extends Number> Storage<Float> scalar(M value) {
            return new FloatArrayStorage(new float[] {value.floatValue()});
        }

        @Override
        public Storage<Float> zeros(int len) {
            return new FloatArrayStorage(new float[len]);
        }

        @Override
        public Storage<Float> cast(byte... array) {
            float[] copy = new float[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new FloatArrayStorage(copy);
        }

        @Override
        public Storage<Float> cast(int... array) {
            float[] copy = new float[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new FloatArrayStorage(copy);
        }

        @Override
        public Storage<Float> cast(float... array) {
            return new FloatArrayStorage(array);
        }

        @Override
        public Storage<Float> cast(double... array) {
            float[] copy = new float[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (float) array[i];
            }
            return new FloatArrayStorage(copy);
        }

        @Override
        public <M extends Number> Storage<Float> cast(Storage<M> source) {
            float[] copy = new float[source.size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = source.getFloat(i);
            }
            return new FloatArrayStorage(copy);
        }
    }

    private static final class OfTypeDouble implements StorageFactory.OfType<Double> {

        @Override
        public <M extends Number> Storage<Double> scalar(M value) {
            return new DoubleArrayStorage(new double[] {value.doubleValue()});
        }

        @Override
        public Storage<Double> zeros(int len) {
            return new DoubleArrayStorage(new double[len]);
        }

        @Override
        public Storage<Double> cast(byte... array) {
            double[] copy = new double[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new DoubleArrayStorage(copy);
        }

        @Override
        public Storage<Double> cast(int... array) {
            double[] copy = new double[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new DoubleArrayStorage(copy);
        }

        @Override
        public Storage<Double> cast(float... array) {
            double[] copy = new double[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new DoubleArrayStorage(copy);
        }

        @Override
        public Storage<Double> cast(double... array) {
            return new DoubleArrayStorage(array);
        }

        @Override
        public <M extends Number> Storage<Double> cast(Storage<M> source) {
            double[] copy = new double[source.size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = source.getDouble(i);
            }
            return new DoubleArrayStorage(copy);
        }
    }
}
