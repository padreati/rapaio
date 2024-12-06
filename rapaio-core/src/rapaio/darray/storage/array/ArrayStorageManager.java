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

package rapaio.darray.storage.array;

import rapaio.darray.DType;
import rapaio.darray.Storage;
import rapaio.darray.StorageManager;

public class ArrayStorageManager extends StorageManager {

    @Override
    public Storage scalar(DType<?> dt, byte value) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[] {value});
            case INTEGER -> new IntArrayStorage(new int[] {value});
            case FLOAT -> new FloatArrayStorage(new float[] {value});
            case DOUBLE -> new DoubleArrayStorage(new double[] {value});
        };
    }

    @Override
    public Storage scalar(DType<?> dt, int value) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[] {(byte) value});
            case INTEGER -> new IntArrayStorage(new int[] {value});
            case FLOAT -> new FloatArrayStorage(new float[] {value});
            case DOUBLE -> new DoubleArrayStorage(new double[] {value});
        };
    }

    @Override
    public Storage scalar(DType<?> dt, float value) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[] {(byte) value});
            case INTEGER -> new IntArrayStorage(new int[] {(int) value});
            case FLOAT -> new FloatArrayStorage(new float[] {value});
            case DOUBLE -> new DoubleArrayStorage(new double[] {value});
        };
    }

    @Override
    public Storage scalar(DType<?> dt, double value) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[] {(byte) value});
            case INTEGER -> new IntArrayStorage(new int[] {(int) value});
            case FLOAT -> new FloatArrayStorage(new float[] {(float) value});
            case DOUBLE -> new DoubleArrayStorage(new double[] {value});
        };
    }

    @Override
    public Storage zeros(DType<?> dt, int len) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(new byte[len]);
            case INTEGER -> new IntArrayStorage(new int[len]);
            case FLOAT -> new FloatArrayStorage(new float[len]);
            case DOUBLE -> new DoubleArrayStorage(new double[len]);
        };
    }

    @Override
    public Storage from(DType<?> dt, byte... array) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    public Storage from(DType<?> dt, int... array) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    public Storage from(DType<?> dt, float... array) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    public Storage from(DType<?> dt, double... array) {
        return switch (dt.id()) {
            case BYTE -> new ByteArrayStorage(array);
            case INTEGER -> new IntArrayStorage(array);
            case FLOAT -> new FloatArrayStorage(array);
            case DOUBLE -> new DoubleArrayStorage(array);
        };
    }

    @Override
    public Storage from(DType<?> dt, Storage source) {
        switch (dt.id()) {
            case BYTE -> {
                byte[] copy = new byte[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getByte(i);
                }
                return new ByteArrayStorage(copy);
            }
            case INTEGER -> {
                int[] copy = new int[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getInt(i);
                }
                return new IntArrayStorage(copy);
            }
            case FLOAT -> {
                float[] copy = new float[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getFloat(i);
                }
                return new FloatArrayStorage(copy);
            }
            case DOUBLE -> {
                double[] copy = new double[source.size()];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = source.getDouble(i);
                }
                return new DoubleArrayStorage(copy);
            }
        }
        return null;
    }
}
