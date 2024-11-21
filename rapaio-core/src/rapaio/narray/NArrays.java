/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.narray;

import java.util.Collection;
import java.util.Random;

/**
 * Starting point for working with NArrays in a default manner. All those methods are available also through
 * more customizable methods, working directly with NArray manager instances and types.
 * <p>
 * This class collects shortcut methods for working with default implementation of NArray manager, and it uses double
 * as default type.
 */
public final class NArrays {

    private static final DType<Double> dt = DType.DOUBLE;
    private static final NArrayManager tm = NArrayManager.base();

    public static NArray<Double> scalar(Double value) {
        return tm.scalar(dt, value);
    }

    public static NArray<Double> zeros(Shape shape) {
        return tm.zeros(dt, shape, Order.defaultOrder());
    }

    public static NArray<Double> full(Shape shape, Double value) {
        return tm.full(dt, shape, value);
    }

    public static NArray<Double> full(Shape shape, Double value, Order order) {
        return tm.full(dt, shape, value, order);
    }

    public static NArray<Double> zeros(Shape shape, Order order) {
        return tm.zeros(dt, shape, order);
    }

    public static NArray<Double> eye(int n) {
        return tm.eye(dt, n);
    }

    public static NArray<Double> eye(int n, Order order) {
        return tm.eye(dt, n, order);
    }

    public static NArray<Double> seq(Shape shape) {
        return tm.seq(dt, shape);
    }

    public static NArray<Double> seq(Shape shape, Order order) {
        return tm.seq(dt, shape, order);
    }

    public static NArray<Double> random(Shape shape, Random random) {
        return tm.random(dt, shape, random);
    }

    public static NArray<Double> random(Shape shape, Random random, Order order) {
        return tm.random(dt, shape, random, order);
    }

    public static NArray<Double> stride(double... array) {
        return tm.stride(dt, Shape.of(array.length), Order.defaultOrder(), array);
    }

    public static NArray<Double> stride(Shape shape, double... array) {
        return tm.stride(dt, shape, Order.defaultOrder(), array);
    }

    public static NArray<Double> stride(Shape shape, Order order, double... array) {
        return tm.stride(dt, shape, order, array);
    }

    public static NArray<Double> stride(Shape shape, Order order, Storage<Double> storage) {
        return tm.stride(dt, shape, order, storage);
    }

    public static <N extends Number> NArray<N> stack(int axis, Collection<? extends NArray<N>> nArrays) {
        return tm.stack(axis, nArrays);
    }

    public static <N extends Number> NArray<N> stack(Order order, int axis, Collection<? extends NArray<N>> nArrays) {
        return tm.stack(order, axis, nArrays);
    }

    public static <N extends Number> NArray<N> concat(int axis, Collection<? extends NArray<N>> nArrays) {
        return tm.concat(Order.defaultOrder(), axis, nArrays);
    }

    public static <N extends Number> NArray<N> concat(Order order, int axis, Collection<? extends NArray<N>> nArrays) {
        return tm.concat(order, axis, nArrays);
    }

}
