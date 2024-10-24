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

import rapaio.math.tensor.storage.array.ArrayStorageManager;

public abstract class StorageManager {

    public static StorageManager array() {
        return new ArrayStorageManager();
    }

    public interface OfType<N extends Number> {

        <M extends Number> Storage<N> scalar(M value);

        Storage<N> zeros(int len);

        Storage<N> from(byte... array);

        Storage<N> from(int... array);

        Storage<N> from(float... array);

        Storage<N> from(double... array);

        Storage<N> from(Storage<?> source);
    }

    public abstract <M extends Number> OfType<M> ofType(DType<M> dType);
}
