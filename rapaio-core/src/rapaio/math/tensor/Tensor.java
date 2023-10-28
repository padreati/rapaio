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

import java.util.Iterator;

import rapaio.math.tensor.iterators.ChunkIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.operators.TensorBinaryOp;
import rapaio.printer.Printable;
import rapaio.util.function.IntIntBiFunction;

public interface Tensor<N extends Number, T extends Tensor<N, T>> extends Printable, Iterable<N> {

    TensorEngine manager();

    /**
     * @return shape of the tensor
     */
    Layout layout();

    default Shape shape() {
        return layout().shape();
    }

    N getValue(int... idxs);

    void setValue(N value, int... idxs);

    N ptrGetValue(int pos);

    void ptrSetValue(int pos, N value);

    default PointerIterator pointerIterator() {
        return pointerIterator(Order.S);
    }

    T abs();

    T neg();

    T log();

    T log1p();

    T exp();

    T expm1();

    T sin();

    T asin();

    T sinh();

    T cos();

    T acos();

    T cosh();

    T tan();

    T atan();

    T tanh();

    T binaryOp(T tensor, TensorBinaryOp op);

    T add(T tensor);

    T sub(T tensor);

    T mul(T tensor);

    T div(T tensor);

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

    //Tensor<N, S, T> concatenate(int axis, Tensor<N, S, T>...tensors);

    //Tensor<N, S, T> stack(int axis, Tensor<N, S, T>...tensors);

    //List<Tensor<N,S,T>> split(int...indexes);

    // repeat(rep,axis)
    // flip(axis)
    // rotate(axis1,axis2)

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
