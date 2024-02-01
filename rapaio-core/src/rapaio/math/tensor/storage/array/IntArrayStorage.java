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

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.storage.IntStorage;

public final class IntArrayStorage extends IntStorage {

    private final int[] array;

    public IntArrayStorage(int[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    public int getInt(int ptr) {
        return array[ptr];
    }

    public void setInt(int ptr, int v) {
        array[ptr] = v;
    }

    @Override
    public void incInt(int ptr, int value) {
        array[ptr] += value;
    }

    @Override
    public void fillInt(int value, int start, int len) {
        Arrays.fill(array, start, start + len, value);
    }

    @Override
    public IntVector loadInt(VectorSpecies<Integer> species, int offset) {
        return IntVector.fromArray(species, array, offset);
    }

    @Override
    public IntVector loadInt(VectorSpecies<Integer> species, int offset, VectorMask<Integer> mask) {
        return IntVector.fromArray(species, array, offset, mask);
    }

    @Override
    public IntVector loadInt(VectorSpecies<Integer> species, int offset, int[] index, int indexOffset) {
        return IntVector.fromArray(species, array, offset, index, indexOffset);
    }

    @Override
    public IntVector loadInt(VectorSpecies<Integer> species, int offset, int[] index, int indexOffset, VectorMask<Integer> mask) {
        return IntVector.fromArray(species, array, offset, index, indexOffset, mask);
    }

    @Override
    public void saveInt(IntVector a, int offset) {
        a.intoArray(array, offset);
    }

    @Override
    public void saveInt(IntVector a, int offset, VectorMask<Integer> mask) {
        a.intoArray(array, offset, mask);
    }

    @Override
    public void saveInt(IntVector a, int offset, int[] index, int indexOffset) {
        a.intoArray(array, offset, index, indexOffset);
    }

    @Override
    public void saveInt(IntVector a, int offset, int[] index, int indexOffset, VectorMask<Integer> mask) {
        a.intoArray(array, offset, index, indexOffset, mask);
    }
}
