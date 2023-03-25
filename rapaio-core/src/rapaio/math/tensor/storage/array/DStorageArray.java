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

import rapaio.math.tensor.storage.DStorage;
import rapaio.math.tensor.storage.StorageFactory;

public final class DStorageArray implements DStorage {

    private final StorageFactory factory;
    private final double[] array;

    DStorageArray(StorageFactory factory, double[] array) {
        this.factory = factory;
        this.array = array;

        if (!(factory instanceof ArrayStorageFactory)) {
            throw new IllegalArgumentException("Wrong type of storage factory.");
        }
    }

    @Override
    public StorageFactory storageFactory() {
        return factory;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public double get(int pointer) {
        return array[pointer];
    }

    @Override
    public void set(int pointer, double v) {
        array[pointer] = v;
    }

    @Override
    public void swap(int left, int right) {
        double tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }

    @Override
    public void fill(int start, int len, double v) {
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
    public void add(int start, int len, double v) {
        for (int i = start; i < start + len; i++) {
            array[i] += v;
        }
    }

    @Override
    public void add(int start, DStorage from, int fStart, int len) {
        for (int i = start, j = fStart; i < start + len; i++, j++) {
            array[i] += from.get(j);
        }
    }

    @Override
    public void sub(int start, int len, double v) {
        for (int i = start; i < start + len; i++) {
            array[i] -= v;
        }
    }

    @Override
    public void mul(int start, int len, double v) {
        for (int i = start; i < start + len; i++) {
            array[i] *= v;
        }
    }

    @Override
    public void div(int start, int len, double v) {
        for (int i = start; i < start + len; i++) {
            array[i] /= v;
        }
    }

    @Override
    public double min(int start, int len) {
        if (len <= 0) {
            return Double.NaN;
        }
        double minValue = array[start];
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
        double min = array[start];
        int index = start;
        for (int i = start + 1; i < start + len; i++) {
            double value = array[i];
            if (value < min) {
                min = value;
                index = i;
            }
        }
        return index;
    }

    @Override
    public DStorageArray copy() {
        return new DStorageArray(factory, Arrays.copyOf(array, array.length));
    }
}
