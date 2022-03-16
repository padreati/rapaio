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
import jdk.incubator.vector.VectorSpecies;
import rapaio.util.function.Double2DoubleFunction;

public final class DMatrixStoreDense implements DMatrixStore {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    public final int offset;
    public final int innerSize;
    public final int outerSize;
    public final double[] array;

    public DMatrixStoreDense(int offset, int innerSize, int outerSize, double[] array) {
        this.offset = offset;
        this.innerSize = innerSize;
        this.outerSize = outerSize;
        this.array = array;
    }

    @Override
    public int outerSize() {
        return outerSize;
    }

    @Override
    public int innerSize() {
        return innerSize;
    }

    @Override
    public double get(int outer, int inner) {
        return array[offset + outer * innerSize + inner];
    }

    @Override
    public void set(int outer, int inner, double value) {
        array[offset + outer * innerSize + inner] = value;
    }

    @Override
    public void inc(int outer, int inner, double value) {
        array[offset + outer * innerSize + inner] += value;
    }

    public void apply(Double2DoubleFunction fun) {
        for (int i = offset; i < offset + innerSize * outerSize; i++) {
            array[i] = fun.apply(array[i]);
        }
    }

    @Override
    public double[] solidArrayCopy() {
        return Arrays.copyOfRange(array, offset, offset + innerSize * outerSize);
    }
}
