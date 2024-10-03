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

package rapaio.math.tensor;

public interface StorageFactory {

    interface OfType<N extends Number> {

        <M extends Number> Storage<N> scalar(M value);

        Storage<N> zeros(int len);

        Storage<N> from(byte... array);

        Storage<N> from(int... array);

        Storage<N> from(float... array);

        Storage<N> from(double... array);

        <M extends Number> Storage<N> from(Storage<M> source);
    }

    <N extends Number> StorageFactory.OfType<N> ofType(DType<N> dType);

    default StorageFactory.OfType<Byte> ofByte() {
        return ofType(DType.BYTE);
    }

    default StorageFactory.OfType<Integer> ofInt() {
        return ofType(DType.INTEGER);
    }

    default StorageFactory.OfType<Float> ofFloat() {
        return ofType(DType.FLOAT);
    }

    default StorageFactory.OfType<Double> ofDouble() {
        return ofType(DType.DOUBLE);
    }
}
