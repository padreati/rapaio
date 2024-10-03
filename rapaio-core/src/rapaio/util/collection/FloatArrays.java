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

package rapaio.util.collection;

import java.util.Arrays;

public class FloatArrays {

    /**
     * Creates a new array filled with a sequence of values starting from
     * {@param start} (inclusive) and ending with {@param end} (exclusive).
     *
     * @param start sequence starting value (inclusive)
     * @param end   sequence ending value (exclusive)
     * @return array with sequence values
     */
    public static float[] newSeq(int start, int end) {
        float[] data = new float[end - start];
        for (int i = 0; i < end - start; i++) {
            data[i] = start + i;
        }
        return data;
    }

    public static float[] newFill(int size, float value) {
        float[] array = new float[size];
        Arrays.fill(array, value);
        return array;
    }
}
