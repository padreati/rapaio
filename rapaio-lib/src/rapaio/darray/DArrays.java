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

package rapaio.darray;

import java.util.Collection;
import java.util.Random;

/**
 * Starting point for working with NArrays in a default manner. All those methods are available also through
 * more customizable methods, working directly with NArray manager instances and types.
 * <p>
 * This class collects shortcut methods for working with default implementation of NArray manager, and it uses double
 * as default type.
 */
public final class DArrays {

    private static final DType<Double> dt = DType.DOUBLE;
    private static final DArrayManager tm = DArrayManager.base();

    public static DArray<Double> scalar(double value) {
        return tm.scalar(dt, value);
    }

    public static DArray<Double> zeros(Shape shape) {
        return tm.zeros(dt, shape, Order.defaultOrder());
    }

    public static DArray<Double> full(Shape shape, Double value) {
        return tm.full(dt, shape, value);
    }

    public static DArray<Double> full(Shape shape, Double value, Order order) {
        return tm.full(dt, shape, value, order);
    }

    public static DArray<Double> zeros(Shape shape, Order order) {
        return tm.zeros(dt, shape, order);
    }

    public static DArray<Double> eye(int n) {
        return tm.eye(dt, n);
    }

    public static DArray<Double> eye(int n, Order order) {
        return tm.eye(dt, n, order);
    }

    public static DArray<Double> seq(Shape shape) {
        return tm.seq(dt, shape);
    }

    public static DArray<Double> seq(Shape shape, Order order) {
        return tm.seq(dt, shape, order);
    }

    public static DArray<Double> random(Shape shape, Random random) {
        return tm.random(dt, shape, random);
    }

    public static DArray<Double> random(Shape shape, Random random, Order order) {
        return tm.random(dt, shape, random, order);
    }

    public static DArray<Double> stride(double... array) {
        return tm.stride(dt, Shape.of(array.length), Order.defaultOrder(), array);
    }

    public static DArray<Double> stride(Shape shape, double... array) {
        return tm.stride(dt, shape, Order.defaultOrder(), array);
    }

    public static DArray<Double> stride(Shape shape, Order order, double... array) {
        return tm.stride(dt, shape, order, array);
    }

    public static DArray<Double> stride(Shape shape, Order order, Storage storage) {
        return tm.stride(dt, shape, order, storage);
    }

    public static DArray<Double> stack(int axis, Collection<? extends DArray<?>> nArrays) {
        return tm.stack(DType.DOUBLE, axis, nArrays);
    }

    public static DArray<Double> stack(Order order, int axis, Collection<? extends DArray<?>> nArrays) {
        return tm.stack(DType.DOUBLE, order, axis, nArrays);
    }

    public static DArray<Double> concat(int axis, Collection<? extends DArray<?>> nArrays) {
        return tm.cat(DType.DOUBLE, Order.defaultOrder(), axis, nArrays);
    }

    public static DArray<Double> concat(Order order, int axis, Collection<? extends DArray<?>> nArrays) {
        return tm.cat(DType.DOUBLE, order, axis, nArrays);
    }

}
