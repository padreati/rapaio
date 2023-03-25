/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.storage.array;

import java.util.Arrays;
import java.util.Random;

import rapaio.math.tensor.storage.StorageFactory;

public final class ArrayStorageFactory implements StorageFactory {

    @Override
    public DStorageArray ofDoubleWrap(double[] array) {
        return new DStorageArray(this, array);
    }

    @Override
    public DStorageArray ofDoubleZeros(int size) {
        return new DStorageArray(this, new double[size]);
    }

    @Override
    public DStorageArray ofDoubleFill(int size, double value) {
        double[] array = new double[size];
        if (value != 0) {
            Arrays.fill(array, value);
        }
        return new DStorageArray(this, array);
    }

    @Override
    public DStorageArray ofDoubleRandom(int size, Random random) {
        DStorageArray storage = ofDoubleZeros(size);
        for (int i = 0; i < size; i++) {
            storage.set(i, random.nextDouble());
        }
        return storage;
    }

    @Override
    public DStorageArray ofDoubleSeq(int start, int end) {
        DStorageArray storage = ofDoubleZeros(end - start);
        for (int i = 0; i < end - start; i++) {
            storage.setValue(i, (double) (start + i));
        }
        return storage;
    }

    @Override
    public FStorageArray ofFloatWrap(float[] array) {
        return new FStorageArray(this, array);
    }

    @Override
    public FStorageArray ofFloatZeros(int size) {
        return new FStorageArray(this, new float[size]);
    }

    @Override
    public FStorageArray ofFloatFill(int size, float value) {
        float[] array = new float[size];
        if (value != 0) {
            Arrays.fill(array, value);
        }
        return new FStorageArray(this, array);
    }

    @Override
    public FStorageArray ofFloatRandom(int size, Random random) {
        FStorageArray storage = ofFloatZeros(size);
        for (int i = 0; i < size; i++) {
            storage.set(i, random.nextFloat());
        }
        return storage;
    }

    @Override
    public FStorageArray ofFloatSeq(int start, int end) {
        FStorageArray storage = ofFloatZeros(end - start);
        for (int i = 0; i < end - start; i++) {
            storage.setValue(i, (float) (start + i));
        }
        return storage;
    }

}
