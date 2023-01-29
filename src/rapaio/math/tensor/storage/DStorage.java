/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor.storage;

import java.util.Arrays;
import java.util.Random;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.util.collection.DoubleArrays;

public class DStorage implements Storage<Double> {

    public static DStorage wrap(double[] array) {
        return new DStorage(array);
    }

    public static DStorage zeros(int size) {
        return new DStorage(new double[size]);
    }

    public static DStorage fill(int size, double value) {
        return new DStorage(DoubleArrays.newFill(size, value));
    }

    public static DStorage random(int size, Random random) {
        DStorage storage = zeros(size);
        for (int i = 0; i < size; i++) {
            storage.setDouble(i, random.nextDouble());
        }
        return storage;
    }

    public static DStorage seq(int start, int end) {
        DStorage storage = DStorage.zeros(end - start);
        for (int i = 0; i < end - start; i++) {
            storage.set(i, (double) (start + i));
        }
        return storage;
    }

    public static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
    public static final int SPECIES_LEN = SPECIES.length();
    private final double[] array;

    private DStorage(double[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public Double get(int index) {
        return getDouble(index);
    }

    public double getDouble(int pointer) {
        return array[pointer];
    }

    @Override
    public void set(int pointer, Double v) {
        setDouble(pointer, v);
    }

    public void setDouble(int pointer, double v) {
        array[pointer] = v;
    }

    @Override
    public void fill(int start, int len, Double v) {
        fillDouble(start, len, v);
    }

    public void fillDouble(int start, int len, double v) {
        Arrays.fill(array, start, start + len, v);
    }

    @Override
    public void add(int start, int len, Double v) {
        addDouble(start, len, v);
    }

    public void addDouble(int start, int len, double v) {
        DoubleVector vv = DoubleVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, i);
            xv = xv.add(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] += v;
        }
    }

    @Override
    public void sub(int start, int len, Double v) {
        subDouble(start, len, v);
    }

    public void subDouble(int start, int len, double v) {
        DoubleVector vv = DoubleVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, i);
            xv = xv.sub(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] -= v;
        }
    }

    @Override
    public void mul(int start, int len, Double v) {
        mulDouble(start, len, v);
    }

    public void mulDouble(int start, int len, double v) {
        DoubleVector vv = DoubleVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, i);
            xv = xv.mul(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] *= v;
        }
    }

    @Override
    public void div(int start, int len, Double v) {
        divDouble(start, len, v);
    }

    public void divDouble(int start, int len, double v) {
        DoubleVector vv = DoubleVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, i);
            xv = xv.div(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] /= v;
        }
    }

    @Override
    public Double min(int start, int len) {
        return minDouble(start, len);
    }

    public double minDouble(int start, int len) {
        if (len <= 0) {
            return Double.NaN;
        }
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        boolean vectorized = false;
        DoubleVector minVector = DoubleVector.broadcast(SPECIES, Double.POSITIVE_INFINITY);
        for (; i < loopBound; i += SPECIES_LEN) {
            DoubleVector xv = DoubleVector.fromArray(SPECIES, array, i);
            minVector = minVector.min(xv);
            vectorized = true;
        }
        double minValue = vectorized ? minVector.reduceLanes(VectorOperators.MIN) : Double.NaN;
        for (; i < start + len; i++) {
            minValue = Double.isNaN(minValue) ? array[i] : Math.min(minValue, array[i]);
        }
        return minValue;
    }

    @Override
    public int argMin(int start, int len) {
        return argMinDouble(start, len);
    }

    public int argMinDouble(int start, int len) {
        if (len <= 0) {
            return -1;
        }
        double min = array[start];
        int index = start;
        for (int i = start + 1; i < start + len; i++) {
            double value = array[i];
            if (value < min) {
                min = value;
                index = i;
            }
        }
        return index;
    }
}
