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

public class DVectorStride extends AbstractStorageDVector {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public static final class Storage implements DVectorStorage {
        public final int offset;
        public final int stride;
        public final int size;
        public final int[] indexes;
        public final double[] array;

        public Storage(int offset, int stride, int size, double[] array) {
            this.offset = offset;
            this.stride = stride;
            this.size = size;
            this.array = array;

            this.indexes = new int[SPECIES.length()];
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = i * stride;
            }
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
            return DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0);
        }

        @Override
        public DoubleVector loadVector(int i, VectorMask<Double> m) {
            return DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0, m);
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

        public double sum() {
            int i = 0;
            DoubleVector aggr = DoubleVector.zero(SPECIES);
            int bound = SPECIES.loopBound(size());
            for (; i < bound; i += SPECIES.length()) {
                DoubleVector xv = DoubleVector.fromArray(SPECIES, array, offset + i * stride, indexes, 0);
                aggr = aggr.add(xv);
            }
            double result = aggr.reduceLanes(VectorOperators.ADD);
            for (; i < size(); i++) {
                result = result + array[offset + i * stride];
            }
            return result;
        }
    }

    private final Storage s;

    public DVectorStride(int offset, int stride, int size, double[] array) {
        super(new Storage(offset, stride, size, array));
        this.s = (Storage) storage;
    }

    @Override
    public int size() {
        return s.size;
    }

    @Override
    public DVector map(int[] indexes, AlgebraOption<?>... opts) {
        if (AlgebraOptions.from(opts).isCopy()) {
            double[] copy = new double[indexes.length];
            for (int i = 0; i < indexes.length; i++) {
                copy[i] = get(indexes[i]);
            }
            return new DVectorDense(0, copy.length, copy);
        }
        int[] copyIndexes = new int[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            copyIndexes[i] = s.stride * indexes[i];
        }
        return new DVectorMap(s.offset, copyIndexes, s.array);
    }

    private double[] copyArray() {
        double[] copy = new double[s.size];
        for (int i = 0; i < s.size; i++) {
            copy[i] = s.array[s.offset + s.stride * i];
        }
        return copy;
    }

    @Override
    public DVector copy() {
        return new DVectorStride(0, 1, s.size, copyArray());
    }

    @Override
    public DVector fill(double value) {
        for (int i = s.offset; i < s.offset + s.size; i += s.stride) {
            s.array[i] = value;
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return IntStream.range(0, s.size).mapToDouble(i -> s.array[s.offset + i * s.stride]);
    }

    @Override
    public VarDouble dVar(AlgebraOption<?>... opts) {
        return VarDouble.wrap(copyArray());
    }
}
