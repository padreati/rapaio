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

package rapaio.math.tensor.manager.cpuparallel;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jdk.incubator.concurrent.StructuredTaskScope;
import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.FTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.storage.DStorage;
import rapaio.math.tensor.storage.FStorage;
import rapaio.math.tensor.storage.StorageFactory;

public class CpuArrayParallelTensorManager implements TensorManager {

    private final StorageFactory storageFactory;

    public CpuArrayParallelTensorManager(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    @Override
    public StorageFactory storageFactory() {
        return storageFactory;
    }

    @Override
    public DTensor ofDoubleZeros(Shape shape, Order order) {
        return new DTensorStride(this, shape, 0, Order.autoFC(order), storageFactory.ofDoubleZeros(shape.size()));
    }

    @Override
    public DTensor ofDoubleSeq(Shape shape, Order order) {
        DTensor tensor = ofDoubleZeros(shape, order);
        var it = tensor.pointerIterator(Order.C);
        int seq = 0;
        while (it.hasNext()) {
            tensor.storage().set(it.nextInt(), seq++);
        }
        return tensor;
    }

    @Override
    public DTensor ofDoubleRandom(Shape shape, Random random, Order order) {
        DTensor tensor = ofDoubleZeros(shape, order);
        var it = tensor.pointerIterator(Order.C);
        while (it.hasNext()) {
            tensor.storage().set(it.nextInt(), random.nextDouble());
        }
        return tensor;
    }

    @Override
    public DTensor ofDoubleWrap(Shape shape, double[] array, Order order) {
        return new DTensorStride(this, shape, 0, Order.autoFC(order), storageFactory.ofDoubleWrap(array));
    }

    @Override
    public DTensor ofDoubleStride(Shape shape, int offset, int[] strides, DStorage storage) {
        return new DTensorStride(this, shape, offset, strides, storage);
    }

    @Override
    public FTensor ofFloatZeros(Shape shape, Order order) {
        return new FTensorStride(this, shape, 0, Order.autoFC(order), storageFactory.ofFloatZeros(shape.size()));
    }

    @Override
    public FTensor ofFloatSeq(Shape shape, Order order) {
        FTensor tensor = ofFloatZeros(shape, order);
        var it = tensor.pointerIterator(Order.C);
        int seq = 0;
        while (it.hasNext()) {
            tensor.storage().set(it.nextInt(), seq++);
        }
        return tensor;
    }

    @Override
    public FTensor ofFloatRandom(Shape shape, Random random, Order order) {
        FTensor tensor = ofFloatZeros(shape, order);
        var it = tensor.pointerIterator(Order.C);
        while (it.hasNext()) {
            tensor.storage().set(it.nextInt(), random.nextFloat());
        }
        return tensor;
    }

    @Override
    public FTensor ofFloatWrap(Shape shape, float[] array, Order order) {
        return new FTensorStride(this, shape, 0, Order.autoFC(order), storageFactory.ofFloatWrap(array));
    }

    @Override
    public FTensor ofFloatStride(Shape shape, int offset, int[] strides, FStorage storage) {
        return new FTensorStride(this, shape, offset, strides, storage);
    }

    <T> List<T> executeCallableTasks(List<Callable<T>> tasks) throws Throwable {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<Future<T>> futures = tasks.stream().map(scope::fork).toList();
            scope.join();
            scope.throwIfFailed();
            return futures.stream().map(Future::resultNow).toList();
        }
    }

    int executeRunnableTasks(List<Runnable> tasks) throws Throwable {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<Future<Object>> futures = tasks.stream().map(Executors::callable).map(scope::fork).toList();
            scope.join();
            scope.throwIfFailed();
            return futures.size();
        }
    }

    @Override
    public void close() {
    }
}
