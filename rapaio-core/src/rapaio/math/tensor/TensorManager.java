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

package rapaio.math.tensor;

import java.util.Random;

import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.storage.DStorage;
import rapaio.math.tensor.storage.FStorage;
import rapaio.math.tensor.storage.StorageFactory;

public interface TensorManager extends AutoCloseable {

    StorageFactory storageFactory();

    DTensor ofDoubleZeros(Shape shape, Order order);

    default DTensor ofDoubleZeros(Shape shape) {
        return ofDoubleZeros(shape, Order.defaultOrder());
    }

    DTensor ofDoubleSeq(Shape shape, Order order);

    default DTensor ofDoubleSeq(Shape shape) {
        return ofDoubleSeq(shape, Order.defaultOrder());
    }

    DTensor ofDoubleRandom(Shape shape, Random random, Order order);

    default DTensor ofDoubleRandom(Shape shape, Random random) {
        return ofDoubleRandom(shape, random, Order.defaultOrder());
    }

    DTensor ofDoubleWrap(Shape shape, double[] array, Order order);

    default DTensor ofDoubleStride(StrideLayout layout, DStorage storage) {
        return ofDoubleStride(layout.shape(), layout.offset(), layout.strides(), storage);
    }

    DTensor ofDoubleStride(Shape shape, int offset, int[] strides, DStorage storage);

    FTensor ofFloatZeros(Shape shape, Order order);

    default FTensor ofFloatZeros(Shape shape) {
        return ofFloatZeros(shape, Order.defaultOrder());
    }

    FTensor ofFloatSeq(Shape shape, Order order);

    default FTensor ofFloatSeq(Shape shape) {
        return ofFloatSeq(shape, Order.defaultOrder());
    }

    FTensor ofFloatRandom(Shape shape, Random random, Order order);

    default FTensor ofFloatRandom(Shape shape, Random random) {
        return ofFloatRandom(shape, random, Order.defaultOrder());
    }

    FTensor ofFloatWrap(Shape shape, float[] array, Order order);

    default FTensor ofFloatStride(StrideLayout layout, FStorage storage) {
        return ofFloatStride(layout.shape(), layout.offset(), layout.strides(), storage);
    }

    FTensor ofFloatStride(Shape shape, int offset, int[] strides, FStorage storage);
}
