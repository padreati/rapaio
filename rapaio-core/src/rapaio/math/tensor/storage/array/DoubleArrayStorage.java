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

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import rapaio.math.tensor.storage.DoubleStorage;

public final class DoubleArrayStorage extends DoubleStorage {

    private final double[] array;

    public DoubleArrayStorage(double[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    public double getDouble(int ptr) {
        return array[ptr];
    }

    public void setDouble(int ptr, double v) {
        array[ptr] = v;
    }

    @Override
    public void incDouble(int ptr, double value) {
        array[ptr] += value;
    }

    @Override
    public void fillDouble(double value) {
        Arrays.fill(array, value);
    }

    @Override
    public DoubleVector loadDouble(VectorSpecies<Double> species, int offset) {
        return DoubleVector.fromArray(species, array, offset);
    }

    @Override
    public DoubleVector loadDouble(VectorSpecies<Double> species, int offset, VectorMask<Double> mask) {
        return DoubleVector.fromArray(species, array, offset, mask);
    }

    @Override
    public DoubleVector loadDouble(VectorSpecies<Double> species, int offset, int[] index, int indexOffset) {
        return DoubleVector.fromArray(species, array, offset, index, indexOffset);
    }

    @Override
    public DoubleVector loadDouble(VectorSpecies<Double> species, int offset, int[] index, int indexOffset, VectorMask<Double> mask) {
        return DoubleVector.fromArray(species, array, offset, index, indexOffset, mask);
    }

    @Override
    public void saveDouble(DoubleVector a, int offset) {
        a.intoArray(array, offset);
    }

    @Override
    public void saveDouble(DoubleVector a, int offset, VectorMask<Double> mask) {
        a.intoArray(array, offset, mask);
    }

    @Override
    public void saveDouble(DoubleVector a, int offset, int[] index, int indexOffset) {
        a.intoArray(array, offset, index, indexOffset);
    }

    @Override
    public void saveDouble(DoubleVector a, int offset, int[] index, int indexOffset, VectorMask<Double> mask) {
        a.intoArray(array, offset, index, indexOffset, mask);
    }
}
