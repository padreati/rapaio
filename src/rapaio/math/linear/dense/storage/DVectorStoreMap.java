/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.dense.storage;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public final class DVectorStoreMap implements DVectorStore {

    private final static VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private final static int SPECIES_LEN = SPECIES.length();

    public final int offset;
    public final int[] indexes;
    public final double[] array;

    public DVectorStoreMap(int offset, int[] indexes, double[] array) {
        this.offset = offset;
        this.indexes = indexes;
        this.array = array;
    }

    @Override
    public int size() {
        return indexes.length;
    }

    @Override
    public double[] array() {
        return array;
    }

    @Override
    public double get(int i) {
        return array[offset + indexes[i]];
    }

    @Override
    public void set(int i, double value) {
        array[offset + indexes[i]] = value;
    }

    @Override
    public void inc(int i, double value) {
        array[offset + indexes[i]] += value;
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(SPECIES, array, offset, indexes, i);
    }

    @Override
    public DoubleVector loadVector(int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(SPECIES, array, offset, indexes, i, m);
    }

    @Override
    public VectorMask<Double> indexInRange(int i) {
        return SPECIES.indexInRange(i, indexes.length);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset, indexes, i);
    }

    @Override
    public void storeVector(DoubleVector v, int i, VectorMask<Double> m) {
        v.intoArray(array, offset, indexes, i, m);
    }

    @Override
    public double[] solidArrayCopy() {
        double[] copy = new double[indexes.length];
        int bound = SPECIES.loopBound(indexes.length);
        int i = 0;
        for (; i < bound; i += SPECIES_LEN) {
            loadVector(i).intoArray(copy, i);
        }
        VectorMask<Double> m = indexInRange(i);
        loadVector(i, m).intoArray(copy, i, m);
        return copy;
    }

    public double sum() {
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, offset, indexes, i);
            aggr = aggr.add(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.ADD);
        for (; i < size(); i++) {
            result = result + array[offset + indexes[i]];
        }
        return result;
    }

    public void add(double x) {
        DoubleVector add = DoubleVector.broadcast(SPECIES, x);
        int i = 0;
        int bound = SPECIES.loopBound(indexes.length);
        for (; i < bound; i += SPECIES_LEN) {
            var v = DoubleVector.fromArray(SPECIES, array, offset, indexes, i);
            v.add(add).intoArray(array, offset + i);
        }
        for (; i < indexes.length; i++) {
            array[offset + i] += x;
        }
    }
}
