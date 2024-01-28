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

import java.util.Collection;
import java.util.Random;

import rapaio.math.tensor.manager.barray.BaseArrayTensorManager;

/**
 * Starting point for working with tensors in a default manner. All those methods are available also through
 * more customizable methods, working directly with tensor manager instances and types.
 * <p>
 * This class collects shortcut methods for working with default implementation of tensor manager and it uses double
 * as default type.
 */
public final class Tensors {


    private static final TensorManager tm = new BaseArrayTensorManager();
    private static final TensorManager.OfType<Double> tmd = tm.ofDouble();

    private static final StorageFactory store = tm.storage();
    private static final StorageFactory.OfType<Double> dstore = store.ofDouble();

    public static <N extends Number> TensorManager.OfType<N> ofType(DType<N> dtype) {
        return tm.ofType(dtype);
    }

    public static TensorManager.OfType<Double> ofDouble() {
        return tm.ofDouble();
    }

    public static TensorManager.OfType<Float> ofFloat() {
        return tm.ofFloat();
    }

    public static TensorManager.OfType<Integer> ofInt() {
        return tm.ofInt();
    }

    public static TensorManager.OfType<Byte> ofByte() {
        return tm.ofByte();
    }

    public static Tensor<Double> scalar(Double value) {
        return tmd.scalar(value);
    }

    public static Tensor<Double> zeros(Shape shape) {
        return tmd.zeros(shape, Order.defaultOrder());
    }

    public static Tensor<Double> full(Shape shape, Double value) {
        return tmd.full(shape, value);
    }

    public static Tensor<Double> full(Shape shape, Double value, Order order) {
        return tmd.full(shape, value, order);
    }

    public static Tensor<Double> zeros(Shape shape, Order order) {
        return tmd.zeros(shape, order);
    }

    public static Tensor<Double> eye(int n) {
        return tmd.eye(n);
    }

    public static Tensor<Double> eye(int n, Order order) {
        return tmd.eye(n, order);
    }

    public static Tensor<Double> seq(Shape shape) {
        return tmd.seq(shape);
    }

    public static Tensor<Double> seq(Shape shape, Order order) {
        return tmd.seq(shape, order);
    }

    public static Tensor<Double> random(Shape shape, Random random) {
        return tmd.random(shape, random);
    }

    public static Tensor<Double> random(Shape shape, Random random, Order order) {
        return tmd.random(shape, random, order);
    }

    public static Tensor<Double> stride(double... array) {
        return tmd.stride(array);
    }

    public static Tensor<Double> stride(Shape shape, double... array) {
        return tmd.stride(shape, array);
    }

    public static Tensor<Double> stride(Shape shape, Order order, double... array) {
        return tmd.stride(shape, order, array);
    }

    public static Tensor<Double> stride(Shape shape, Order order, Storage<Double> storage) {
        return tmd.stride(shape, order, storage);
    }

    public static <N extends Number> Tensor<N> stack(int axis, Collection<? extends Tensor<N>> tensors) {
        return tm.stack(axis, tensors);
    }

    public static <N extends Number> Tensor<N> stack(Order order, int axis, Collection<? extends Tensor<N>> tensors) {
        return tm.stack(order, axis, tensors);
    }
}
