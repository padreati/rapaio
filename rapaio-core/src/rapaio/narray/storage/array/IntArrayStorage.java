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

package rapaio.narray.storage.array;

import java.util.Arrays;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.narray.storage.IntStorage;

public final class IntArrayStorage extends IntStorage {

    private final int[] array;

    public IntArrayStorage(byte[] array) {
        this.array = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = array[i];
        }
    }

    public IntArrayStorage(int[] array) {
        this.array = array;
    }

    public IntArrayStorage(float[] array) {
        this.array = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (int) array[i];
        }
    }

    public IntArrayStorage(double[] array) {
        this.array = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (int) array[i];
        }
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean supportVectorization() {
        return true;
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
    public void fill(int value, int start, int len) {
        Arrays.fill(array, start, start + len, value);
    }

    @Override
    public IntVector getIntVector(VectorSpecies<Integer> vs, int offset) {
        return IntVector.fromArray(vs, array, offset);
    }

    @Override
    public IntVector getIntVector(VectorSpecies<Integer> vs, int offset, int[] idx, int idxOffset) {
        return IntVector.fromArray(vs, array, offset, idx, idxOffset);
    }

    @Override
    public void setIntVector(IntVector value, int offset) {
        value.intoArray(array, offset);
    }

    @Override
    public void setIntVector(IntVector value, int offset, int[] idx, int idxOffset) {
        value.intoArray(array, offset, idx, idxOffset);
    }

    public int[] array() {
        return array;
    }
}
