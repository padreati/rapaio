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

package rapaio.experiment.math.tensor.storage;

import java.util.Arrays;
import java.util.Random;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.util.collection.FloatArrays;

public class FStorage implements Storage<Float> {

    public static FStorage wrap(float[] array) {
        return new FStorage(array);
    }

    public static FStorage zeros(int size) {
        return new FStorage(new float[size]);
    }

    public static FStorage fill(int size, float value) {
        return new FStorage(FloatArrays.newFill(size, value));
    }

    public static FStorage random(int size, Random random) {
        FStorage storage = zeros(size);
        for (int i = 0; i < size; i++) {
            storage.setFloat(i, random.nextFloat());
        }
        return storage;
    }

    public static FStorage seq(int start, int end) {
        FStorage storage = FStorage.zeros(end - start);
        for (int i = 0; i < end - start; i++) {
            storage.set(i, (float) (start + i));
        }
        return storage;
    }

    public static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
    public static final int SPECIES_LEN = SPECIES.length();
    private final float[] array;

    private FStorage(float[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public Float get(int index) {
        return getFloat(index);
    }

    public float getFloat(int pointer) {
        return array[pointer];
    }

    @Override
    public void set(int pointer, Float v) {
        setFloat(pointer, v);
    }

    public void setFloat(int pointer, float v) {
        array[pointer] = v;
    }

    @Override
    public void fill(int start, int len, Float v) {
        fillFloat(start, len, v);
    }

    public void fillFloat(int start, int len, float v) {
        Arrays.fill(array, start, start + len, v);
    }

    @Override
    public void add(int start, int len, Float v) {
        addFloat(start, len, v);
    }

    public void addFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.add(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] += v;
        }
    }

    @Override
    public void sub(int start, int len, Float v) {
        subFloat(start, len, v);
    }

    public void subFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.sub(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] -= v;
        }
    }

    @Override
    public void mul(int start, int len, Float v) {
        mulFloat(start, len, v);
    }

    public void mulFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.mul(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] *= v;
        }
    }

    @Override
    public void div(int start, int len, Float v) {
        divFloat(start, len, v);
    }

    public void divFloat(int start, int len, float v) {
        FloatVector vv = FloatVector.broadcast(SPECIES, v);
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            xv = xv.div(vv);
            xv.intoArray(array, i);
        }
        for (; i < start + len; i++) {
            array[i] /= v;
        }
    }

    @Override
    public Float min(int start, int len) {
        return minFloat(start, len);
    }

    public float minFloat(int start, int len) {
        if (len <= 0) {
            return Float.NaN;
        }
        int loopBound = SPECIES.loopBound(len) + start;
        int i = start;
        boolean vectorized = false;
        FloatVector minVector = FloatVector.broadcast(SPECIES, Float.POSITIVE_INFINITY);
        for (; i < loopBound; i += SPECIES_LEN) {
            FloatVector xv = FloatVector.fromArray(SPECIES, array, i);
            minVector = minVector.min(xv);
            vectorized = true;
        }
        float minValue = vectorized ? minVector.reduceLanes(VectorOperators.MIN) : Float.NaN;
        for (; i < start + len; i++) {
            minValue = Float.isNaN(minValue) ? array[i] : Math.min(minValue, array[i]);
        }
        return minValue;
    }

    @Override
    public int argMin(int start, int len) {
        return argMinFloat(start, len);
    }

    public int argMinFloat(int start, int len) {
        if (len <= 0) {
            return -1;
        }
        float min = array[start];
        int index = start;
        for (int i = start + 1; i < start + len; i++) {
            float value = array[i];
            if (value < min) {
                min = value;
                index = i;
            }
        }
        return index;
    }
}
