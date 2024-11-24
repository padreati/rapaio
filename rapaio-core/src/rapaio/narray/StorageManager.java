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

package rapaio.narray;

import rapaio.narray.storage.array.ArrayStorageManager;

public abstract class StorageManager {

    public static StorageManager array() {
        return new ArrayStorageManager();
    }

    public abstract Storage scalar(DType<?> dt, byte value);

    public abstract Storage scalar(DType<?> dt, int value);

    public abstract Storage scalar(DType<?> dt, float value);

    public abstract Storage scalar(DType<?> dt, double value);

    public abstract Storage zeros(DType<?> dt, int len);

    public abstract Storage from(DType<?> dt, byte... array);

    public abstract Storage from(DType<?> dt, int... array);

    public abstract Storage from(DType<?> dt, float... array);

    public abstract Storage from(DType<?> dt, double... array);

    public abstract Storage from(DType<?> dt, Storage source);

}
