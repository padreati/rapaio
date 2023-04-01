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

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.storage.FStorage;
import rapaio.math.tensor.storage.StorageFactory;

public final class FStorageArray implements FStorage {

    private static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    private final StorageFactory factory;
    private final float[] array;

    FStorageArray(StorageFactory factory, float[] array) {
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
    public float get(int pointer) {
        return array[pointer];
    }

    @Override
    public void set(int pointer, float v) {
        array[pointer] = v;
    }

    @Override
    public FloatVector load(int offset) {
        return FloatVector.fromArray(SPECIES, array, offset);
    }

    @Override
    public FloatVector load(int offset, VectorMask<Float> mask) {
        return FloatVector.fromArray(SPECIES, array, offset, mask);
    }

    @Override
    public FloatVector load(int offset, int[] indexMap, int mapOffset) {
        return FloatVector.fromArray(SPECIES, array, offset, indexMap, mapOffset);
    }

    @Override
    public FloatVector load(int offset, int[] indexMap, int mapOffset, VectorMask<Float> mask) {
        return FloatVector.fromArray(SPECIES, array, offset, indexMap, mapOffset, mask);
    }

    @Override
    public void save(FloatVector v, int offset) {
        v.intoArray(array, offset);
    }

    @Override
    public void save(FloatVector v, int offset, VectorMask<Float> mask) {
        v.intoArray(array, offset, mask);
    }

    @Override
    public void save(FloatVector v, int offset, int[] indexMap, int mapOffset) {
        v.intoArray(array, offset, indexMap, mapOffset);
    }

    @Override
    public void save(FloatVector v, int offset, int[] indexMap, int mapOffset, VectorMask<Float> mask) {
        v.intoArray(array, offset, indexMap, mapOffset, mask);
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
    public FStorageArray copy() {
        return new FStorageArray(factory, Arrays.copyOf(array, array.length));
    }
}
