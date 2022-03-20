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

package rapaio.math.linear.dense;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.util.collection.DoubleArrays;

public class DVectorMap extends AbstractStoreDVector {

    private final static VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private final static int SPECIES_LEN = SPECIES.length();

    private final int offset;
    private final int[] indexes;
    private final double[] array;
    private final VectorMask<Double> loopMask;
    private final int loopBound;

    public DVectorMap(int offset, int[] indexes, double[] array) {
        this.offset = offset;
        this.indexes = indexes;
        this.array = array;
        this.loopMask = SPECIES.indexInRange(SPECIES.loopBound(indexes.length), indexes.length);
        this.loopBound = SPECIES.loopBound(indexes.length);
    }

    @Override
    public VectorSpecies<Double> species() {
        return SPECIES;
    }

    @Override
    public int speciesLen() {
        return SPECIES_LEN;
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
    public int loopBound() {
        return loopBound;
    }

    @Override
    public VectorMask<Double> loopMask() {
        return loopMask;
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
        for (int i = 0; i < loopBound; i += SPECIES_LEN) {
            loadVector(i).intoArray(copy, i);
        }
        loadVector(loopBound, loopMask).intoArray(copy, loopBound, loopMask);
        return copy;
    }

    @Override
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

    @Override
    public DVector map(int[] sel) {
        int[] copyIndexes = new int[sel.length];
        for (int i = 0; i < sel.length; i++) {
            copyIndexes[i] = indexes[sel[i]];
        }
        return new DVectorMap(offset, copyIndexes, array);
    }

    @Override
    public DVector mapTo(int[] sel, DVector to) {
        for (int i = 0; i < sel.length; i++) {
            to.set(i, array[indexes[sel[i]]]);
        }
        return to;
    }

    @Override
    public DVector copy() {
        double[] copy = DoubleArrays.copyByIndex(array, offset, indexes);
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.of(indexes).map(i -> i + offset).mapToDouble(i -> array[i]);
    }

    @Override
    public VarDouble dv() {
        double[] copy = DoubleArrays.copyByIndex(array, offset, indexes);
        return VarDouble.wrap(copy);
    }
}
