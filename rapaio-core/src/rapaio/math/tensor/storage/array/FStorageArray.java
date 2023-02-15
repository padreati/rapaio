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

import java.util.Arrays;
import java.util.Random;

import rapaio.math.tensor.storage.FStorage;

public class FStorageArray implements FStorage {

    public static FStorageArray wrap(float[] array) {
        return new FStorageArray(array);
    }

    public static FStorageArray zeros(int size) {
        return new FStorageArray(new float[size]);
    }

    public static FStorageArray fill(int size, float value) {
        float[] array = new float[size];
        if (value != 0) {
            Arrays.fill(array, value);
        }
        return new FStorageArray(array);
    }

    public static FStorageArray random(int size, Random random) {
        FStorageArray storage = zeros(size);
        for (int i = 0; i < size; i++) {
            storage.set(i, random.nextFloat());
        }
        return storage;
    }

    public static FStorageArray seq(int start, int end) {
        FStorageArray storage = FStorageArray.zeros(end - start);
        for (int i = 0; i < end - start; i++) {
            storage.setValue(i, (float) (start + i));
        }
        return storage;
    }

    private final float[] array;

    private FStorageArray(float[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public float get(int pointer) {
        return array[pointer];
    }

    @Override
    public void set(int pointer, float v) {
        array[pointer] = v;
    }

    @Override
    public void swap(int left, int right) {
        float tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }

    @Override
    public void fill(int start, int len, float v) {
        Arrays.fill(array, start, start + len, v);
    }

    @Override
    public void reverse(int start, int len) {
        int end = start + len;
        while (end - start > 1) {
            swap(start, end - 1);
            start++;
            end--;
        }
    }

    @Override
    public void add(int start, int len, float v) {
        for (int i = start; i < start + len; i++) {
            array[i] += v;
        }
    }

    public void sub(int start, int len, float v) {
        for (int i = start; i < start + len; i++) {
            array[i] -= v;
        }
    }

    public void mul(int start, int len, float v) {
        for (int i = start; i < start + len; i++) {
            array[i] *= v;
        }
    }

    public void div(int start, int len, float v) {
        for (int i = start; i < start + len; i++) {
            array[i] /= v;
        }
    }

    public float min(int start, int len) {
        if (len <= 0) {
            return Float.NaN;
        }
        float minValue = array[start];
        for (int i = start + 1; i < start + len; i++) {
            minValue = Math.min(minValue, array[i]);
        }
        return minValue;
    }

    @Override
    public int argMin(int start, int len) {
        if (len <= 0) {
            return -1;
        }
        float min = array[start];
        int index = start;
        for (int i = start + 1; i < start + len; i++) {
            float value = array[i];
            if (value < min) {
                min = value;
                index = i;
            }
        }
        return index;
    }

    @Override
    public FStorageArray copy() {
        return new FStorageArray(Arrays.copyOf(array, array.length));
    }
}
