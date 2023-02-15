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

import rapaio.math.tensor.storage.array.FStorageArray;

public interface FTensor extends Tensor<Float, FStorageArray, FTensor> {

    @Override
    FStorageArray storage();

    @Override
    default Float getValue(int... idxs) {
        return get(idxs);
    }

    float get(int... idxs);

    @Override
    default void setValue(Float value, int... idxs) {
        set(value, idxs);
    }

    void set(float value, int... idxs);

    @Override
    default FTensor reshape(Shape shape) {
        return reshape(shape, Order.defaultOrder());
    }

    @Override
    FTensor reshape(Shape shape, Order askOrder);

    @Override
    default FTensor copy(Order askOrder) {
        if (askOrder == Order.S) {
            throw new IllegalArgumentException("Order argument is invalid.");
        }
        FTensor copy = manager().floatZeros(shape(), askOrder);
        var it = chunkIterator(askOrder);
        int pos = 0;
        int l = it.chunkSize();
        int s = it.chunkStride();
        while (it.hasNext()) {
            int pointer = it.nextInt();
            for (int i = 0; i < l; i++) {
                copy.storage().set(pos++, storage().get(pointer + i * s));
            }
        }
        return copy;
    }

    @Override
    FTensor t();
}
