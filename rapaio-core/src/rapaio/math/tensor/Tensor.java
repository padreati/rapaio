/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.printer.Printable;
import rapaio.util.function.IntIntBiFunction;

public interface Tensor<N extends Number, T extends Tensor<N, T>> extends Printable, Iterable<N> {

    TensorFactory factory();

    Layout layout();

    default Shape shape() {
        return layout().shape();
    }

    N getValue(int... indexes);

    void setValue(N value, int... indexes);

    N ptrGetValue(int pos);

    void ptrSetValue(int pos, N value);

    default PointerIterator pointerIterator() {
        return pointerIterator(Order.S);
    }

    default T abs() {
        return abs(Order.defaultOrder());
    }

    default T abs(Order order) {
        return copy(order).abs_();
    }

    T abs_();

    default T neg() {
        return neg(Order.defaultOrder());
    }

    default T neg(Order order) {
        return copy(order).neg_();
    }

    T neg_();

    default T log() {
        return log(Order.defaultOrder());
    }

    default T log(Order order) {
        return copy(order).log_();
    }

    T log_();

    default T log1p() {
        return log1p(Order.defaultOrder());
    }

    default T log1p(Order order) {
        return copy(order).log1p_();
    }

    T log1p_();

    default T exp() {
        return exp(Order.defaultOrder());
    }

    default T exp(Order order) {
        return copy(order).exp_();
    }

    T exp_();

    default T expm1() {
        return expm1(Order.defaultOrder());
    }

    default T expm1(Order order) {
        return copy(order).expm1_();
    }

    T expm1_();

    default T sin() {
        return sin(Order.defaultOrder());
    }

    default T sin(Order order) {
        return copy(order).sin_();
    }

    T sin_();

    default T asin() {
        return asin(Order.defaultOrder());
    }

    default T asin(Order order) {
        return copy(order).asin_();
    }

    T asin_();

    default T sinh() {
        return sinh(Order.defaultOrder());
    }

    default T sinh(Order order) {
        return copy(order).sinh_();
    }

    T sinh_();

    default T cos() {
        return cos(Order.defaultOrder());
    }

    default T cos(Order order) {
        return copy(order).cos_();
    }

    T cos_();

    default T acos() {
        return acos(Order.defaultOrder());
    }

    default T acos(Order order) {
        return copy(order).acos_();
    }

    T acos_();

    default T cosh() {
        return cosh(Order.defaultOrder());
    }

    default T cosh(Order order) {
        return copy(order).cosh_();
    }

    T cosh_();

    default T tan() {
        return tan(Order.defaultOrder());
    }

    default T tan(Order order) {
        return copy(order).tan_();
    }

    T tan_();

    default T atan() {
        return atan(Order.defaultOrder());
    }

    default T atan(Order order) {
        return copy(order).atan_();
    }

    T atan_();

    default T tanh() {
        return tanh(Order.defaultOrder());
    }

    default T tanh(Order order) {
        return copy(order).tanh_();
    }

    T tanh_();

    default T add(T tensor) {
        return add(tensor, Order.defaultOrder());
    }

    default T add(T tensor, Order order) {
        return copy(order).add_(tensor);
    }

    T add_(T tensor);

    default T sub(T tensor) {
        return sub(tensor, Order.defaultOrder());
    }

    default T sub(T tensor, Order order) {
        return copy(order).sub_(tensor);
    }

    T sub_(T tensor);

    default T mul(T tensor) {
        return mul(tensor, Order.defaultOrder());
    }

    default T mul(T tensor, Order order) {
        return copy(order).mul_(tensor);
    }

    T mul_(T tensor);

    default T div(T tensor) {
        return div(tensor, Order.defaultOrder());
    }

    default T div(T tensor, Order order) {
        return copy(order).div_(tensor);
    }

    T div_(T tensor);

    default T add(N value) {
        return add(value, Order.defaultOrder());
    }

    default T add(N value, Order order) {
        return copy(order).add_(value);
    }

    T add_(N value);

    default T sub(N value) {
        return sub(value, Order.defaultOrder());
    }

    default T sub(N value, Order order) {
        return copy(order).sub_(value);
    }

    T sub_(N value);

    default T mul(N value) {
        return mul(value, Order.defaultOrder());
    }

    default T mul(N value, Order order) {
        return copy(order).mul_(value);
    }

    T mul_(N value);

    default T div(N value) {
        return div(value, Order.defaultOrder());
    }

    default T div(N value, Order order) {
        return copy(order).div_(value);
    }

    T div_(N value);

    T matmul(T tensor);

    T mv(T tensor);

    T mm(T tensor);

    Iterator<N> iterator(Order askOrder);

    T iteratorApply(Order order, IntIntBiFunction<N> apply);

    PointerIterator pointerIterator(Order askOrder);

    default ChunkIterator chunkIterator() {
        return chunkIterator(Order.S);
    }

    ChunkIterator chunkIterator(Order askOrder);

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * If data is copied, the result will be a dense tensor of default order.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     *
     * @param shape destination shape
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    default T reshape(Shape shape) {
        return reshape(shape, Order.defaultOrder());
    }

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     *
     * @param shape    destination shape
     * @param askOrder destination order, if the data will be copied, otherwise the parameter is ignored.
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    T reshape(Shape shape, Order askOrder);

    /**
     * Transpose of a tensor. A transposed tensor is a tensor which reverts axis, the first axis becomes the last,
     * the second axis becomes the second to last and so on. Data storage remain the same, no new storage copy is created.
     * As such, any modification on a transposed tensor will affect the original tensor.
     *
     * @return a transposed view of the tensor
     */
    T t();

    /**
     * Collapses the tensor into one dimension in the order given as parameter. It creates a new tensor copy
     * only if needed (no stride could be created).
     *
     * @param askOrder order of the elements
     * @return a tensor with elements in given order (new copy if needed)
     */
    T ravel(Order askOrder);

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is given as parameter.
     *
     * @param askOrder order of the elements
     * @return a copy of the tensor with elements in asked order.
     */
    T flatten(Order askOrder);

    /**
     * Collapses all dimensions equal with one. This operation does not create a new copy of the data.
     * If no dimensions have size one, the same tensor is returned.
     *
     * @return view of the same tensor with all dimensions equal with one collapsed
     */
    T squeeze();

    /**
     * Creates a new tensor view with an additional dimension at the position specified by {@param axis}.
     * Specified axis value should be between 0 (inclusive) and the number of dimensions (inclusive).
     *
     * @param axis index of the axis to be added
     * @return new view tensor with added axis
     */
    T unsqueeze(int axis);

    /**
     * Creates a new tensor view with source axis moved into the given destination position.
     *
     * @param src source axis
     * @param dst destination axis position
     * @return new view tensor with moved axis
     */
    T moveAxis(int src, int dst);

    /**
     * Swap two axis. This does not affect the storage.
     *
     * @param src source axis
     * @param dst destination axis
     * @return new view tensor with swapped axis
     */
    T swapAxis(int src, int dst);

    /**
     * Creates a new tensor view with truncated axis, all other axes remain the same.
     *
     * @param axis  axis to be truncated
     * @param start start index inclusive
     * @param end   end index exclusive
     * @return new view tensor with truncated axis
     */
    T truncate(int axis, int start, int end);

    /**
     * Splits the tensor into multiple view tensors along a given axis.
     * The resulting tensors are truncated versions of the original tensor, with the start index being the current index, and the end
     * being the next index or the end of the dimension.
     *
     * @param axis    axis to split along
     * @param indexes indexes to split along, being start indexes for truncation
     * @return list of new tensors with truncated data.
     */
    List<T> split(int axis, int... indexes);

    /**
     * Slices the tensor along a given axis.
     * The resulting tensors are truncated versions of the original one with size given by step.
     * The last tensor in list might have lesser dimension size if step does not divide dimension size
     *
     * @param axis axis to slice along
     * @param step step size
     * @return list of new tensors with truncated data.
     */
    default List<T> slice(int axis, int step) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < layout().shape().dim(axis); i += step) {
            indexes.add(i);
        }
        return split(axis, indexes.stream().mapToInt(i -> i).toArray());
    }

    T repeat(int axis, int repeat, boolean stack);

    /**
     * Creates a copy of the original tensor with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @return new copy of the tensor
     */
    default T copy() {
        return copy(Order.defaultOrder());
    }

    /**
     * Creates a copy of the original tensor with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @param askOrder desired order of the copy tensor.
     * @return new copy of the tensor
     */
    T copy(Order askOrder);

    default boolean deepEquals(Object t) {
        return deepEquals(t, 1e-100);
    }

    default boolean deepEquals(Object t, double tol) {
        if (t instanceof Tensor<?, ?> dt) {
            if (!layout().shape().equals(dt.layout().shape())) {
                return false;
            }
            var it1 = iterator(Order.C);
            var it2 = dt.iterator(Order.C);
            while (it1.hasNext()) {
                double v1 = it1.next().doubleValue();
                double v2 = it2.next().doubleValue();
                if (Math.abs(v1 - v2) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
}
