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

package rapaio.math.tensor.engine;

import java.util.Random;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.StorageFactory;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.layout.StrideLayout;

public abstract class AbstractEngineOfType<N extends Number> implements TensorEngine.OfType<N> {

    protected final DType<N> dType;
    protected TensorEngine parent;
    protected StorageFactory.OfType<N> storageOfType;

    protected AbstractEngineOfType(DType<N> dType) {
        this.dType = dType;
    }

    @Override
    public void registerParent(TensorEngine parent, StorageFactory.OfType<N> storageOfType) {
        if (this.parent != null) {
            throw new IllegalArgumentException("AbstractEngineOfType has already a registered parent.");
        }
        this.parent = parent;
        this.storageOfType = storageOfType;
    }

    @Override
    public final DType<N> dtype() {
        return dType;
    }

    public StorageFactory.OfType<N> storage() {
        return storageOfType;
    }

    @Override
    public final Tensor<N> scalar(N value) {
        return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
    }

    @Override
    public final Tensor<N> zeros(Shape shape) {
        return zeros(shape, Order.defaultOrder());
    }

    @Override
    public final Tensor<N> zeros(Shape shape, Order order) {
        return stride(shape, Order.autoFC(order), storage().zeros(shape.size()));
    }


    @Override
    public final Tensor<N> eye(int n) {
        return eye(n, Order.defaultOrder());
    }

    @Override
    public final Tensor<N> eye(int n, Order order) {
        var eye = zeros(Shape.of(n, n), order);
        for (int i = 0; i < n; i++) {
            eye.set(dType.castValue(1), i, i);
        }
        return eye;
    }

    @Override
    public final Tensor<N> full(Shape shape, N value) {
        return full(shape, value, Order.defaultOrder());
    }

    @Override
    public final Tensor<N> full(Shape shape, N value, Order order) {
        var storage = storage().zeros(shape.size());
        storage.fill(value);
        return stride(shape, Order.autoFC(order), storage);
    }

    @Override
    public final Tensor<N> seq(Shape shape) {
        return seq(shape, Order.defaultOrder());
    }

    @Override
    public final Tensor<N> seq(Shape shape, Order order) {
        return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, p) -> dType.castValue(i));
    }

    @Override
    public final Tensor<N> random(Shape shape, Random random) {
        return random(shape, random, Order.defaultOrder());
    }

    @Override
    public abstract Tensor<N> random(Shape shape, Random random, Order order);


    @Override
    public final <M extends Number> Tensor<N> strideCast(Shape shape, Order order, Storage<M> storage) {
        return strideCast(StrideLayout.ofDense(shape, 0, order), storage);
    }

    @Override
    public final <M extends Number> Tensor<N> strideCast(StrideLayout layout, Storage<M> storage) {
        return stride(layout, storage().from(storage));
    }

    @Override
    public final Tensor<N> stride(Shape shape, Order order, Storage<N> storage) {
        return stride(StrideLayout.ofDense(shape, 0, order), storage);
    }

    @Override
    public abstract Tensor<N> stride(StrideLayout layout, Storage<N> storage);
}
