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

package rapaio.narray.storage.array;

import rapaio.narray.DType;
import rapaio.narray.Storage;
import rapaio.narray.StorageManager;

public class ArrayStorageManager extends StorageManager {

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number, M extends Number> Storage<N> scalar(DType<N> dt, M value) {
        return (Storage<N>) switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[] {value.byteValue()});
            case INTEGER -> new IntArrayStorage(new int[] {value.intValue()});
            case FLOAT -> new FloatArrayStorage(new float[] {value.floatValue()});
            case DOUBLE -> new DoubleArrayStorage(new double[] {value.doubleValue()});
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> Storage<N> zeros(DType<N> dt, int len) {
        return (Storage<N>) switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[len]);
            case INTEGER -> new IntArrayStorage(new int[len]);
            case FLOAT -> new FloatArrayStorage(new float[len]);
            case DOUBLE -> new DoubleArrayStorage(new double[len]);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> Storage<N> from(DType<N> dt, byte... array) {
        return (Storage<N>) switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> Storage<N> from(DType<N> dt, int... array) {
        return (Storage<N>) switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> Storage<N> from(DType<N> dt, float... array) {
        return (Storage<N>) switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> Storage<N> from(DType<N> dt, double... array) {
        return (Storage<N>) switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <N extends Number> Storage<N> from(DType<N> dt, Storage<?> source) {
        switch (dt.id()) {
            case BYTE -> {
                byte[] copy = new byte[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getByte(i);
                }
                return (Storage<N>) new ByteArrayStorage(copy);
            }
            case INTEGER -> {
                int[] copy = new int[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getInt(i);
                }
                return (Storage<N>) new IntArrayStorage(copy);
            }
            case FLOAT -> {
                float[] copy = new float[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getFloat(i);
                }
                return (Storage<N>) new FloatArrayStorage(copy);
            }
            case DOUBLE -> {
                double[] copy = new double[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getDouble(i);
                }
                return (Storage<N>) new DoubleArrayStorage(copy);
            }
        }
        return null;
    }
}
