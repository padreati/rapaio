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

import java.util.Arrays;

import rapaio.math.tensor.storage.FloatStorage;

public final class FloatArrayStorage extends FloatStorage {

    private final float[] array;

    public FloatArrayStorage(float[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    public float getFloat(int ptr) {
        return array[ptr];
    }

    public void setFloat(int ptr, float v) {
        array[ptr] = v;
    }

    @Override
    public void incFloat(int ptr, float value) {
        array[ptr] += value;
    }

    @Override
    public void fillFloat(float value, int start, int len) {
        Arrays.fill(array, start, start + len, value);
    }

    public float[] array() {
        return array;
    }
}
