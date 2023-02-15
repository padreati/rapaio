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

package rapaio.math.tensor.manager.cpuarray;

import java.util.Random;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.storage.array.DStorageArray;
import rapaio.math.tensor.storage.array.FStorageArray;

public class CpuArraySingleTensorManager implements TensorManager {

    @Override
    public DTensor doubleZeros(Shape shape, Order order) {
        return switch (order) {
            case F, C -> new DTensorDense(this, shape, DStorageArray.zeros(shape.size()), order);
            default -> throw new IllegalArgumentException("Illegal order type.");
        };
    }

    @Override
    public DTensor doubleSeq(Shape shape, Order order) {
        if (order==Order.S) {
            throw new IllegalArgumentException("Illegal order type.");
        }
        DTensor tensor = new DTensorDense(this, shape, DStorageArray.zeros(shape.size()), order);
        var it = tensor.pointerIterator(Order.C);
        int seq = 0;
        while(it.hasNext()) {
            tensor.storage().set(it.nextInt(), seq++);
        }
        return tensor;
    }

    @Override
    public DTensor doubleRandom(Shape shape, Random random, Order order) {
        return switch (order) {
            case F, C -> new DTensorDense(this, shape, DStorageArray.random(shape.size(), random), order);
            default -> throw new IllegalArgumentException("Illegal order type.");
        };
    }

    @Override
    public DTensor wrap(Shape shape, double[] array, Order order) {
        return switch (order) {
            case F, C -> new DTensorDense(this, shape, DStorageArray.wrap(array), order);
            default -> throw new IllegalArgumentException("Illegal order type.");
        };
    }

    @Override
    public FTensor floatZeros(Shape shape, Order order) {
        return switch (order) {
            case F, C -> new FTensorDense(this, shape, FStorageArray.zeros(shape.size()), order);
            default -> throw new IllegalArgumentException("Illegal order type.");
        };
    }

    @Override
    public FTensor floatSeq(Shape shape, Order order) {
        if (order==Order.S) {
            throw new IllegalArgumentException("Illegal order type.");
        }
        FTensor tensor = new FTensorDense(this, shape, FStorageArray.zeros(shape.size()), order);
        var it = tensor.pointerIterator(Order.C);
        int seq = 0;
        while(it.hasNext()) {
            tensor.storage().set(it.nextInt(), seq++);
        }
        return tensor;
    }

    @Override
    public FTensor floatRandom(Shape shape, Random random, Order order) {
        return switch (order) {
            case F, C -> new FTensorDense(this, shape, FStorageArray.random(shape.size(), random), order);
            default -> throw new IllegalArgumentException("Illegal order type.");
        };
    }

    @Override
    public FTensor wrap(Shape shape, float[] array, Order order) {
        return switch (order) {
            case F, C -> new FTensorDense(this, shape, FStorageArray.wrap(array), order);
            default -> throw new IllegalArgumentException("Illegal order type.");
        };
    }

}
