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


    private static final NArrayManager tm = NArrayManager.base();
    private static final NArrayManager.OfType<Double> tmd = tm.ofDouble();

    public static <N extends Number> NArrayManager.OfType<N> ofType(DType<N> dtype) {
        return tm.ofType(dtype);
    }

    public static NArrayManager.OfType<Double> ofDouble() {
        return tm.ofDouble();
    }

    public static NArrayManager.OfType<Float> ofFloat() {
        return tm.ofFloat();
    }

    public static NArrayManager.OfType<Integer> ofInt() {
        return tm.ofInt();
    }

    public static NArrayManager.OfType<Byte> ofByte() {
        return tm.ofByte();
    }

    public static NArray<Double> scalar(Double value) {
        return tmd.scalar(value);
    }

    public static NArray<Double> zeros(Shape shape) {
        return tmd.zeros(shape, Order.defaultOrder());
    }

    public static NArray<Double> full(Shape shape, Double value) {
        return tmd.full(shape, value);
    }

    public static NArray<Double> full(Shape shape, Double value, Order order) {
        return tmd.full(shape, value, order);
    }

    public static NArray<Double> zeros(Shape shape, Order order) {
        return tmd.zeros(shape, order);
    }

    public static NArray<Double> eye(int n) {
        return tmd.eye(n);
    }

    public static NArray<Double> eye(int n, Order order) {
        return tmd.eye(n, order);
    }

    public static NArray<Double> seq(Shape shape) {
        return tmd.seq(shape);
    }

    public static NArray<Double> seq(Shape shape, Order order) {
        return tmd.seq(shape, order);
    }

    public static NArray<Double> random(Shape shape, Random random) {
        return tmd.random(shape, random);
    }

    public static NArray<Double> random(Shape shape, Random random, Order order) {
        return tmd.random(shape, random, order);
    }

    public static NArray<Double> stride(double... array) {
        return tmd.stride(array);
    }

    public static NArray<Double> stride(Shape shape, double... array) {
        return tmd.stride(shape, array);
    }

    public static NArray<Double> stride(Shape shape, Order order, double... array) {
        return tmd.stride(shape, order, array);
    }

    public static NArray<Double> stride(Shape shape, Order order, Storage<Double> storage) {
        return tmd.stride(shape, order, storage);
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
