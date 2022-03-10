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
import rapaio.math.linear.base.AbstractStorageDVector;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.collection.DoubleArrays;

public class DVectorMap extends AbstractStorageDVector {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public static final class Storage implements DVectorStorage {
        public final int offset;
        public final int[] indexes;
        public final double[] array;

        public Storage(int offset, int[] indexes, double[] array) {
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
        public DoubleVector loadVector(int i) {
            return DoubleVector.fromArray(SPECIES, array, offset, indexes, i);
        }

        @Override
        public DoubleVector loadVector(int i, VectorMask<Double> m) {
            return DoubleVector.fromArray(SPECIES, array, offset, indexes, i, m);
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

        public double sum() {
            int i = 0;
            DoubleVector aggr = DoubleVector.zero(SPECIES);
            int bound = SPECIES.loopBound(size());
            for (; i < bound; i += SPECIES.length()) {
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
            for (; i < bound; i += SPECIES.length()) {
                var v = DoubleVector.fromArray(SPECIES, array, offset, indexes, i);
                v.add(add).intoArray(array, offset + i);
            }
            for (; i < indexes.length; i++) {
                array[offset + i] += x;
            }
        }
    }

    private final Storage s;

    public DVectorMap(int offset, int[] indexes, double[] array) {
        super(new Storage(offset, indexes, array));
        this.s = (Storage) storage;
    }

    @Override
    public int size() {
        return s.indexes.length;
    }

    @Override
    public DVector map(int[] sel, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[sel.length];
            for (int i = 0; i < sel.length; i++) {
                copy[i] = s.array[s.indexes[sel[i]]];
            }
            return new DVectorDense(0, sel.length, copy);
        }
        int[] copyIndexes = new int[sel.length];
        for (int i = 0; i < sel.length; i++) {
            copyIndexes[i] = s.indexes[sel[i]];
        }
        return new DVectorMap(s.offset, copyIndexes, s.array);
    }

    @Override
    public DVector add(double x, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            super.add(x, opts);
        }
        s.add(x);
        return this;
    }

    @Override
    public DVector copy() {
        double[] copy = DoubleArrays.copyByIndex(s.array, s.offset, s.indexes);
        return new DVectorDense(0, copy.length, copy);
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.of(s.indexes).map(i -> i + s.offset).mapToDouble(i -> s.array[i]);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        double[] copy = DoubleArrays.copyByIndex(s.array, s.offset, s.indexes);
        return VarDouble.wrap(copy);
    }
}
