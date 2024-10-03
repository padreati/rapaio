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

package rapaio.experiment.math.linear.dense;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import rapaio.data.VarDouble;
import rapaio.experiment.math.linear.DVector;

public class DVectorStride extends AbstractDVectorStore {

    private final int offset;
    private final int stride;
    private final int size;
    private final int[] loopIndexes;
    private final double[] array;

    public DVectorStride(int offset, int stride, int size, double[] array) {
        super(size);
        this.offset = offset;
        this.stride = stride;
        this.size = size;
        this.array = array;

        this.loopIndexes = new int[speciesLen];
        for (int i = 0; i < loopIndexes.length; i++) {
            loopIndexes[i] = i * stride;
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
        return DoubleVector.fromArray(species, array, offset + i * stride, loopIndexes, 0);
    }

    @Override
    public DoubleVector loadVector(int i, VectorMask<Double> m) {
        return DoubleVector.fromArray(species, array, offset + i * stride, loopIndexes, 0, m);
    }

    @Override
    public void storeVector(DoubleVector v, int i) {
        v.intoArray(array, offset + i * stride, loopIndexes, 0);
    }

    @Override
    public void storeVector(DoubleVector v, int i, VectorMask<Double> m) {
        v.intoArray(array, offset + i * stride, loopIndexes, 0, m);
    }

    @Override
    public double[] solidArrayCopy() {
        double[] copy = new double[size];
        for (int i = 0; i < loopBound; i += speciesLen) {
            loadVector(i).intoArray(copy, i);
        }
        VectorMask<Double> loopMask = species.indexInRange(loopBound, size);
        loadVector(loopBound, loopMask).intoArray(copy, loopBound, loopMask);
        return copy;
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
    public DVector mapTo(DVector to, int... indexes) {
        for (int i = 0; i < indexes.length; i++) {
            to.set(i, array[offset + stride * indexes[i]]);
        }
        return to;
    }

    private double[] copyArray() {
        double[] copy = new double[size];
        int len = offset + stride * size;
        int pos = 0;
        int i = offset;
        while (i < len) {
            copy[pos++] = array[i];
            i += stride;
        }
        return copy;
    }

    @Override
    public DVector copy() {
        return new DVectorDense(0, size, copyArray());
    }

    @Override
    public DVector fill(double value) {
        int len = offset + size * stride;
        int i = offset;
        while (i < len) {
            array[i] = value;
            i += stride;
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
