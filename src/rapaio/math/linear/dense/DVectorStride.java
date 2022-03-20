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

public class DVectorStride extends AbstractStoreDVector {

    private final static VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    private final static int SPECIES_LEN = SPECIES.length();

    private final int offset;
    private final int stride;
    private final int size;
    private final int[] indexes;
    private final double[] array;
    private final VectorMask<Double> loopMask;
    private final int loopBound;

    public DVectorStride(int offset, int stride, int size, double[] array) {
        this.offset = offset;
        this.stride = stride;
        this.size = size;
        this.array = array;
        this.loopMask = SPECIES.indexInRange(SPECIES.loopBound(size), size);
        this.loopBound = SPECIES.loopBound(size);

        this.indexes = new int[SPECIES.length()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i * stride;
        }
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
        return size;
    }

    @Override
    public double[] array() {
        return array;
    }

    @Override
    public double get(int i) {
        return array[offset + i * stride];
    }

    @Override
    public void set(int i, double value) {
        array[offset + i * stride] = value;
    }

    @Override
    public void inc(int i, double value) {
        array[offset + i * stride] += value;
    }

    @Override
    public DoubleVector loadVector(int i) {
        return DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0);
    }

    @Override
    public DoubleVector loadVector(int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0, m);
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
        v.intoArray(array, offset + i * stride, indexes, 0);
    }

    @Override
    public void storeVector(DoubleVector v, int i, VectorMask<Double> m) {
        v.intoArray(array, offset + i * stride, indexes, 0, m);
    }

    @Override
    public double[] solidArrayCopy() {
        double[] copy = new double[indexes.length];
        int bound = SPECIES.loopBound(indexes.length);
        int i = 0;
        for (; i < bound; i += SPECIES_LEN) {
            loadVector(i).intoArray(copy, i);
        }
        loadVector(i, loopMask).intoArray(copy, i, loopMask);
        return copy;
    }

    public double sum() {
        int i = 0;
        DoubleVector aggr = DoubleVector.zero(SPECIES);
        int bound = SPECIES.loopBound(size());
        for (; i < bound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0);
            aggr = aggr.add(xv);
        }
        double result = aggr.reduceLanes(VectorOperators.ADD);
        for (; i < size(); i++) {
            result = result + array[offset + i * stride];
        }
        return result;
    }

    @Override
    public DVector map(int[] indexes) {
        int[] copyIndexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            copyIndexes[i] = stride * indexes[i];
        }
        return new DVectorMap(offset, copyIndexes, array);
    }

    @Override
    public DVector mapTo(int[] indexes, DVector to) {
        for (int i = 0; i < indexes.length; i++) {
            to.set(i, array[offset + stride * indexes[i]]);
        }
        return to;
    }

    private double[] copyArray() {
        double[] copy = new double[size];
        for (int i = 0; i < size; i++) {
            copy[i] = array[offset + stride * i];
        }
        return copy;
    }

    @Override
    public DVector copy() {
        return new DVectorStride(0, 1, size, copyArray());
    }

    @Override
    public DVector fill(double value) {
        for (int i = offset; i < offset + size; i += stride) {
            array[i] = value;
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, size).mapToDouble(i -> array[offset + i * stride]);
    }

    @Override
    public VarDouble dv() {
        return VarDouble.wrap(copyArray());
    }
}
