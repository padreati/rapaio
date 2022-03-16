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

import java.util.Arrays;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public final class DVectorStoreDense implements DVectorStore {

    private final static VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private final static int SPECIES_LEN = SPECIES.length();

    public final int offset;
    public final int size;
    public final double[] array;

    public DVectorStoreDense(int offset, int size, double[] array) {
        this.offset = offset;
        this.size = size;
        this.array = array;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double[] array() {
        return array;
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(SPECIES, array, offset + i);
    }

    @Override
    public DoubleVector loadVector(int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(SPECIES, array, offset + i, m);
    }

    @Override
    public VectorMask<Double> indexInRange(int i) {
        return SPECIES.indexInRange(i, size);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset + i);
    }

    @Override
    public void storeVector(DoubleVector v, int i, VectorMask<Double> m) {
        v.intoArray(array, offset + i, m);
    }

    @Override
    public double get(int i) {
        return array[offset + i];
    }

    @Override
    public void set(int i, double value) {
        array[offset + i] = value;
    }

    @Override
    public void inc(int i, double value) {
        array[offset + i] += value;
    }

    @Override
    public double[] solidArrayCopy() {
        return Arrays.copyOfRange(array, offset, offset + size);
    }

    public double sum() {
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            var vi = loadVector(i);
            aggr = aggr.add(vi);
        }
        VectorMask<Double> m = indexInRange(i);
        var vi = loadVector(i, m);
        aggr = aggr.add(vi, m);
        return aggr.reduceLanes(VectorOperators.ADD);
    }

    public void add(double x) {
        DoubleVector add = DoubleVector.broadcast(SPECIES, x);
        int i = 0;
        int bound = SPECIES.loopBound(size);
        for (; i < bound; i += SPECIES_LEN) {
            var v = loadVector(i).add(add);
            storeVector(v, i);
        }
        VectorMask<Double> m = indexInRange(i);
        var v = loadVector(i, m).add(add, m);
        storeVector(v, i, m);
    }

}
