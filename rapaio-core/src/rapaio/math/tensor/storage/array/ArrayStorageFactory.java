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

package rapaio.math.tensor.storage.array;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.StorageFactory;

public class ArrayStorageFactory implements StorageFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> OfType<N> ofType(DType<N> dType) {
        return (OfType<N>) switch (dType.id()) {
            case DOUBLE -> new OfTypeDouble();
            case FLOAT -> new OfTypeFloat();
            case INTEGER -> new OfTypeInt();
            case BYTE -> new OfTypeByte();
            case null -> throw new IllegalArgumentException("DType cannot be null.");
        };
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
        public Storage<Byte> from(byte... array) {
            return new ByteArrayStorage(array);
        }

        @Override
        public Storage<Byte> from(int... array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (byte) array[i];
            }
            return new ByteArrayStorage(copy);
        }

        @Override
        public Storage<Byte> from(float... array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (byte) array[i];
            }
            return new ByteArrayStorage(copy);
        }

        @Override
        public Storage<Byte> from(double... array) {
            byte[] copy = new byte[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (byte) array[i];
            }
            return new ByteArrayStorage(copy);
        }

        @Override
        public <M extends Number> Storage<Byte> from(Storage<M> source) {
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
        public Storage<Integer> from(byte... array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new IntArrayStorage(copy);
        }

        @Override
        public Storage<Integer> from(int... array) {
            return new IntArrayStorage(array);
        }

        @Override
        public Storage<Integer> from(float... array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (int) array[i];
            }
            return new IntArrayStorage(copy);
        }

        @Override
        public Storage<Integer> from(double... array) {
            int[] copy = new int[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (int) array[i];
            }
            return new IntArrayStorage(copy);
        }

        @Override
        public <M extends Number> Storage<Integer> from(Storage<M> source) {
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
        public Storage<Float> from(byte... array) {
            float[] copy = new float[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new FloatArrayStorage(copy);
        }

        @Override
        public Storage<Float> from(int... array) {
            float[] copy = new float[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new FloatArrayStorage(copy);
        }

        @Override
        public Storage<Float> from(float... array) {
            return new FloatArrayStorage(array);
        }

        @Override
        public Storage<Float> from(double... array) {
            float[] copy = new float[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = (float) array[i];
            }
            return new FloatArrayStorage(copy);
        }

        @Override
        public <M extends Number> Storage<Float> from(Storage<M> source) {
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
        public Storage<Double> from(byte... array) {
            double[] copy = new double[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new DoubleArrayStorage(copy);
        }

        @Override
        public Storage<Double> from(int... array) {
            double[] copy = new double[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new DoubleArrayStorage(copy);
        }

        @Override
        public Storage<Double> from(float... array) {
            double[] copy = new double[array.length];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = array[i];
            }
            return new DoubleArrayStorage(copy);
        }

        @Override
        public Storage<Double> from(double... array) {
            return new DoubleArrayStorage(array);
        }

        @Override
        public <M extends Number> Storage<Double> from(Storage<M> source) {
            double[] copy = new double[source.size()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = source.getDouble(i);
            }
            return new DoubleArrayStorage(copy);
        }
    }
}
