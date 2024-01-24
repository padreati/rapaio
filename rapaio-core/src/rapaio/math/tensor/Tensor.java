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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import rapaio.data.VarDouble;
import rapaio.math.tensor.iterators.LoopIterator;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.matrix.CholeskyDecomposition;
import rapaio.math.tensor.matrix.LUDecomposition;
import rapaio.printer.Printable;
import rapaio.util.function.IntIntBiFunction;

/**
 * Generic tensor interface. A tensor is a multidimensional array.
 *
 * @param <N> Generic data type
 */
public interface Tensor<N extends Number> extends Printable, Iterable<N> {

    /**
     * @return tensor engine
     */
    TensorEngine engine();

    /**
     * @return tensor data type
     */
    DType<N> dtype();

    /**
     * @return tensor layout
     */
    Layout layout();

    /**
     * @return tensor shape
     */
    default Shape shape() {
        return layout().shape();
    }

    /**
     * @return number of dimensions or rank
     */
    default int rank() {
        return layout().rank();
    }

    default int[] dims() {
        return layout().shape().dims();
    }

    default int dim(int axis) {
        return layout().shape().dim(axis);
    }

    /**
     * Size of a tensor is the number of elements contained in tensor and is equal with
     * the product of dimension's sizes
     *
     * @return number of elements from tensor
     */
    default int size() {
        return layout().size();
    }

    /**
     * Storage implementation which physically contains data.
     *
     * @return storage instance
     */
    Storage<N> storage();

    /**
     * @return true if the rank of tensor is 0
     */
    default boolean isScalar() {
        return rank() == 0;
    }

    /**
     * @return true if the rank of the tensor is 1
     */
    default boolean isVector() {
        return rank() == 1;
    }

    /**
     * @return true if the rank of the tensor is 2
     */
    default boolean isMatrix() {
        return rank() == 2;
    }

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * If data is copied, the result will be a dense tensor of default order.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     *
     * @param shape destination shape
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    default Tensor<N> reshape(Shape shape) {
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
    Tensor<N> reshape(Shape shape, Order askOrder);

    /**
     * Creates a new transposed tensor stored with default order.
     *
     * @return copy of the transposed vector
     */
    default Tensor<N> t() {
        return t(Order.defaultOrder());
    }

    /**
     * Creates a new transposed tensor stored in the specified order.
     *
     * @param askOrder storage order
     * @return copy of the transposed vector
     */
    default Tensor<N> t(Order askOrder) {
        return t_().copy(askOrder);
    }

    /**
     * Transpose of a tensor. A transposed tensor is a tensor which reverts axis, the first axis becomes the last,
     * the second axis becomes the second to last and so on. Data storage remain the same, no new storage copy is created.
     * As such, any modification on a transposed tensor will affect the original tensor.
     *
     * @return a transposed view of the tensor
     */
    Tensor<N> t_();

    /**
     * Collapses the tensor into one dimension using the default order. The order is used for reading. In the case when a view
     * can't be created, a new tensor will be created with the storage order same as reading order.
     *
     * @return a tensor with elements in given order (new copy if needed)
     */
    default Tensor<N> ravel() {
        return ravel(Order.defaultOrder());
    }

    /**
     * Collapses the tensor into one dimension using the given order. The order is used for reading. In the case when a view
     * can't be created, a new tensor will be created with the storage order same as reading order.
     *
     * @param askOrder order of the elements
     * @return a tensor with elements in given order (new copy if needed)
     */
    Tensor<N> ravel(Order askOrder);

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is the default order.
     *
     * @return a copy of the tensor with elements in asked order.
     */
    default Tensor<N> flatten() {
        return flatten(Order.defaultOrder());
    }

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is given as parameter.
     *
     * @param askOrder order of the elements
     * @return a copy of the tensor with elements in asked order.
     */
    Tensor<N> flatten(Order askOrder);

    /**
     * Collapses all dimensions equal with one. This operation does not create a new copy of the data.
     * If no dimensions have size one, the same tensor is returned.
     *
     * @return view of the same tensor with all dimensions equal with one collapsed
     */
    Tensor<N> squeeze();

    /**
     * Collapses the given axis if equals with one. This operation does not create a new copy of the data.
     * If dimension doesn't have size one, the same tensor is returned.
     *
     * @return view of the same tensor with the given dimension equal with one collapsed
     */
    Tensor<N> squeeze(int axis);

    /**
     * Creates a new tensor view with an additional dimension at the position specified by {@param axis}.
     * Specified axis value should be between 0 (inclusive) and the number of dimensions (inclusive).
     *
     * @param axis index of the axis to be added
     * @return new view tensor with added axis
     */
    Tensor<N> unsqueeze(int axis);

    /**
     * Creates a tensor view with dimensions permuted in the order specified in parameter. The
     * parameter is an integer array containing all values from closed interval {@code [0,(rank-1)]}.
     * The order in which those values are passed defined the dimension permutation.
     *
     * @param dims dimension permutation
     * @return new tensor view with permuted dimensions
     */
    Tensor<N> permute(int[] dims);

    /**
     * Creates a new tensor view with source axis moved into the given destination position.
     *
     * @param src source axis
     * @param dst destination axis position
     * @return new view tensor with moved axis
     */
    Tensor<N> moveAxis(int src, int dst);

    /**
     * Swap two axis. This does not affect the storage.
     *
     * @param src source axis
     * @param dst destination axis
     * @return new view tensor with swapped axis
     */
    Tensor<N> swapAxis(int src, int dst);

    /**
     * Creates a new tensor view with one truncated axis, all other axes remain the same.
     *
     * @param axis    axis to be truncated
     * @param keepDim keep dimension or not
     * @param start   start index inclusive
     * @param end     end index exclusive
     * @return new view tensor with truncated axis
     */
    Tensor<N> narrow(int axis, boolean keepDim, int start, int end);

    /**
     * Creates a new tensor view with possibly all truncated axes.
     *
     * @param keepDim keep dimensions even if some of have length 1, false otherwise
     * @param starts  vector of indexes where narrow interval starts
     * @param ends    vector of indexes where narrow interval ends
     * @return a view with truncated axes
     */
    Tensor<N> narrowAll(boolean keepDim, int[] starts, int[] ends);

    /**
     * Splits the tensor into multiple view tensors along a given axis. The resulting tensors are narrowed versions of the original tensor,
     * with the start index being the current index, and the end being the next index or the end of the dimension.
     *
     * @param axis    axis to split along
     * @param indexes indexes to split along, being start indexes for truncation
     * @return list of new tensors with truncated data.
     */
    List<Tensor<N>> split(int axis, boolean keepDim, int... indexes);

    /**
     * Splits the tensor into multiple view tensors along all axes. The resulting tensors are narrowed versions of the original tensors,
     * having for each dimension the start index being the current index in that dimension, and the end index being the next index in
     * that dimension. The indices are given as an array of arrays with length equal with number of axes, and for each sub array the
     * split indexes specified.
     *
     * @param keepDim keep original dimensions even if some dimensions have size 1, false otherwise
     * @param indexes array of arrays of indices
     * @return list of new tensors with truncated axes
     */
    List<Tensor<N>> splitAll(boolean keepDim, int[][] indexes);

    /**
     * Slices the tensor along a given axis.
     * The resulting tensors are truncated versions of the original one with size given by step.
     * The last tensor in list might have lesser dimension size if step does not divide dimension size
     * The resulting tensors are views over the original one.
     *
     * @param axis axis to slice along
     * @param step step size
     * @return list of new tensors with truncated data.
     */
    default List<Tensor<N>> chunk(int axis, boolean keepDim, int step) {
        int dim = layout().shape().dim(axis);
        int[] indexes = new int[Math.ceilDiv(dim, step)];
        indexes[0] = 0;
        for (int i = 1; i < indexes.length; i++) {
            indexes[i] = Math.min(indexes[i - 1] + step, dim);
        }
        return split(axis, keepDim, indexes);
    }

    /**
     * Slices the tensor along all dimensions.
     * The resulting tensors are truncated versions of the original with sizes in each dimensions given by steps.
     * The last tensor mugh have dimensions lesser than steps if the original dimension does not divide exactly at step.
     * The resulting tensors are views over the original one.
     *
     * @param keepDim keep the original dimensions even if those have dimensions of size 1, remove them otherwise
     * @param steps   array of steps, one step for each dimension
     * @return list of tensors with truncated data
     */
    default List<Tensor<N>> chunkAll(boolean keepDim, int[] steps) {
        if (layout().rank() != steps.length) {
            throw new IllegalArgumentException("Array of steps must have the length equals with rank.");
        }
        int[][] indexes = new int[steps.length][];
        for (int i = 0; i < steps.length; i++) {
            indexes[i] = new int[Math.ceilDiv(layout().shape().dim(i), steps[i])];
            indexes[i][0] = 0;
            for (int j = 1; j < indexes[i].length; j++) {
                indexes[i][j] = Math.min(indexes[i][j - 1] + steps[i], layout().shape().dim(i));
            }
        }
        return splitAll(keepDim, indexes);
    }

    /**
     * Creates a new tensor by stacking or concatenating this tensor multiple times along a given axis.
     * <p>
     * The resulting tensor will be stored in default order.
     *
     * @param axis   the axis which will be repeated
     * @param repeat the number of repetitions
     * @param stack  stack tensors if true, concatenate if false
     * @return tensor with repeated values along given axis
     */
    default Tensor<N> repeat(int axis, int repeat, boolean stack) {
        return repeat(Order.defaultOrder(), axis, repeat, stack);
    }

    /**
     * Creates a new tensor by stacking or concatenating this tensor multiple times along a given axis.
     * <p>
     * The resulting tensor will be stored in specified order.
     *
     * @param order  storage order of the new tensor
     * @param axis   the axis which will be repeated
     * @param repeat the number of repetitions
     * @param stack  stack tensors if true, concatenate if false
     * @return tensor with repeated values along given axis
     */
    Tensor<N> repeat(Order order, int axis, int repeat, boolean stack);

    /**
     * Creates a new tensor by repeating values along a given dimension of size 1. This operation is
     * similar with repeating values, with the difference that the resulting tensor will be a view over the same data,
     * thus avoiding copying data. This is possible if the corresponding stride is set to 0 and the corresponding original
     * dimension has size 1.
     *
     * @param axis specified dimension
     * @param dim  new size of the dimension, which is equivalent with how many times the values are repeated
     * @return new view over the original tensor with repeated data along a given dimension
     */
    Tensor<N> expand(int axis, int dim);

    /**
     * Take values along a given axis from specified indices. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting tensor will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the default order.
     *
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return tensor with mapped values along the given dimension
     */
    default Tensor<N> take(int axis, int... indices) {
        return take(Order.defaultOrder(), axis, indices);
    }

    /**
     * Take values along a given axis from specified indices. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting tensor will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the specified order.
     *
     * @param order   storage order if new data copy is required, ignored otherwise
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return tensor with mapped values along the given dimension
     */
    Tensor<N> take(Order order, int axis, int... indices);

    default Tensor<N> sort(int dim, boolean asc) {
        return sort(Order.defaultOrder(), dim, asc);
    }

    default Tensor<N> sort(Order order, int axis, boolean asc) {
        return copy(order).sort_(axis, asc);
    }

    Tensor<N> sort_(int axis, boolean asc);

    /**
     * Sorts indices given as an array of parameters according to the values from flatten tensor.
     * Tensor must have a single dimension with size greater than the biggest index value.
     *
     * @param indices indices which will be sorted
     * @param asc     if true, than sort ascending, descending otherwise
     */
    void indirectSort(int[] indices, boolean asc);

    /**
     * Get value at indexed position. An indexed position is a tuple of rank
     * dimension, with an integer value on each dimension.
     *
     * @param indexes indexed position
     * @return value at indexed position
     */
    N get(int... indexes);

    byte getByte(int... indexes);

    int getInt(int... indexes);

    float getFloat(int... indexes);

    double getDouble(int... indexes);

    /**
     * Sets value at indexed position.
     *
     * @param value   value to be set
     * @param indexes indexed position
     */
    void set(N value, int... indexes);

    void setByte(byte value, int... indexes);

    void setInt(int value, int... indexes);

    void setFloat(float value, int... indexes);

    void setDouble(double value, int... indexes);

    /**
     * Sets value at indexed position.
     *
     * @param value   value to be set
     * @param indexes indexed position
     */
    void inc(N value, int... indexes);

    void incByte(byte value, int... indexes);

    void incInt(int value, int... indexes);

    void incFloat(float value, int... indexes);

    void incDouble(double value, int... indexes);

    /**
     * Get value at pointer. A pointer is an index value at the memory layout.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    N ptrGet(int ptr);

    byte ptrGetByte(int ptr);

    int ptrGetInt(int ptr);

    float ptrGetFloat(int ptr);

    double ptrGetDouble(int ptr);

    /**
     * Sets value at given pointer.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    void ptrSet(int ptr, N value);

    void ptrSetByte(int ptr, byte value);

    void ptrSetInt(int ptr, int value);

    void ptrSetFloat(int ptr, float value);

    void ptrSetDouble(int ptr, double value);

    /**
     * Produces an iterator over the values from this tensor in the
     * storage order.
     *
     * @return value iterator
     */
    default Iterator<N> iterator() {
        return iterator(Order.S);
    }

    /**
     * Produces an iterator over values from this tensor in the order
     * specified by parameter value.
     *
     * @param askOrder traversing order
     * @return value iterator
     */
    Iterator<N> iterator(Order askOrder);

    /**
     * Produces an iterator of data pointer values in the storage order.
     *
     * @return data pointer iterator
     */
    default PointerIterator ptrIterator() {
        return ptrIterator(Order.S);
    }

    /**
     * Produces an iterator of data pointer values in the order specified
     * by parameter value.
     *
     * @param askOrder traversing order
     * @return data pointer iterator
     */
    PointerIterator ptrIterator(Order askOrder);

    /**
     * Produces a loop iterator in the storage order. A loop iterator iterates
     * through a series of objects which contains information which can be used
     * in a for loop instruction. All loops have the same size and step.
     * <p>
     * This kind of iterators are useful for computational reasons. In general
     * a for loop is faster than an iterator because it avoids the artifacts
     * produced. On the other side it requires more code on the usage side,
     * which eventually can be optimized by the compiler.
     *
     * @return loop iterator in storage order
     */
    default LoopIterator loopIterator() {
        return loopIterator(Order.S);
    }

    /**
     * Produces a loop iterator in the given order. A loop iterator iterates
     * through a series of objects which contains information which can be used
     * in a for loop instruction. All loops have the same size and step.
     * <p>
     * This kind of iterators are useful for computational reasons. In general
     * a for loop is faster than an iterator because it avoids the artifacts
     * produced. On the other side it requires more code on the usage side,
     * which eventually can be optimized by the compiler.
     *
     * @return loop iterator in storage order
     */
    LoopIterator loopIterator(Order askOrder);

    default Tensor<N> apply(IntIntBiFunction<N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    default Tensor<N> apply(Order askOrder, IntIntBiFunction<N> fun) {
        return copy(askOrder).apply_(askOrder, fun);
    }

    /**
     * Applies the given function over the elements of the tensor. The function has
     * two parameters: position and data pointer. The position describes
     * the index element in the order specified by {@code askOrder}. The data pointer values do not
     * depend on order.
     *
     * @param askOrder order used to iterate over positions
     * @param fun      function which produces values
     * @return new tensor with applied values
     */
    Tensor<N> apply_(Order askOrder, IntIntBiFunction<N> fun);

    default Tensor<N> apply(Function<N, N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    default Tensor<N> apply(Order askOrder, Function<N, N> fun) {
        return copy(askOrder).apply_(fun);
    }

    Tensor<N> apply_(Function<N, N> fun);

    Tensor<N> fill_(N value);

    Tensor<N> fillNan_(N value);

    Tensor<N> clamp_(N min, N max);

    default Tensor<N> abs() {
        return abs(Order.defaultOrder());
    }

    default Tensor<N> abs(Order order) {
        return copy(order).abs_();
    }

    Tensor<N> abs_();

    default Tensor<N> negate() {
        return negate(Order.defaultOrder());
    }

    default Tensor<N> negate(Order order) {
        return copy(order).negate_();
    }

    Tensor<N> negate_();

    default Tensor<N> log() {
        return log(Order.defaultOrder());
    }

    default Tensor<N> log(Order order) {
        return copy(order).log_();
    }

    Tensor<N> log_();

    default Tensor<N> log1p() {
        return log1p(Order.defaultOrder());
    }

    default Tensor<N> log1p(Order order) {
        return copy(order).log1p_();
    }

    Tensor<N> log1p_();

    default Tensor<N> exp() {
        return exp(Order.defaultOrder());
    }

    default Tensor<N> exp(Order order) {
        return copy(order).exp_();
    }

    Tensor<N> exp_();

    default Tensor<N> expm1() {
        return expm1(Order.defaultOrder());
    }

    default Tensor<N> expm1(Order order) {
        return copy(order).expm1_();
    }

    Tensor<N> expm1_();

    default Tensor<N> sin() {
        return sin(Order.defaultOrder());
    }

    default Tensor<N> sin(Order order) {
        return copy(order).sin_();
    }

    Tensor<N> sin_();

    default Tensor<N> asin() {
        return asin(Order.defaultOrder());
    }

    default Tensor<N> asin(Order order) {
        return copy(order).asin_();
    }

    Tensor<N> asin_();

    default Tensor<N> sinh() {
        return sinh(Order.defaultOrder());
    }

    default Tensor<N> sinh(Order order) {
        return copy(order).sinh_();
    }

    Tensor<N> sinh_();

    default Tensor<N> cos() {
        return cos(Order.defaultOrder());
    }

    default Tensor<N> cos(Order order) {
        return copy(order).cos_();
    }

    Tensor<N> cos_();

    default Tensor<N> acos() {
        return acos(Order.defaultOrder());
    }

    default Tensor<N> acos(Order order) {
        return copy(order).acos_();
    }

    Tensor<N> acos_();

    default Tensor<N> cosh() {
        return cosh(Order.defaultOrder());
    }

    default Tensor<N> cosh(Order order) {
        return copy(order).cosh_();
    }

    Tensor<N> cosh_();

    default Tensor<N> tan() {
        return tan(Order.defaultOrder());
    }

    default Tensor<N> tan(Order order) {
        return copy(order).tan_();
    }

    Tensor<N> tan_();

    default Tensor<N> atan() {
        return atan(Order.defaultOrder());
    }

    default Tensor<N> atan(Order order) {
        return copy(order).atan_();
    }

    Tensor<N> atan_();

    default Tensor<N> tanh() {
        return tanh(Order.defaultOrder());
    }

    default Tensor<N> tanh(Order order) {
        return copy(order).tanh_();
    }

    Tensor<N> tanh_();

    default Tensor<N> add(Tensor<N> tensor) {
        return add(tensor, Order.defaultOrder());
    }

    default Tensor<N> add(Tensor<N> tensor, Order order) {
        if (isScalar()) {
            return tensor.copy(order).add_(get());
        }
        return copy(order).add_(tensor);
    }

    Tensor<N> add_(Tensor<N> tensor);

    default Tensor<N> sub(Tensor<N> tensor) {
        return sub(tensor, Order.defaultOrder());
    }

    default Tensor<N> sub(Tensor<N> tensor, Order order) {
        if (isScalar()) {
            return tensor.copy(order).sub_(get());
        }
        return copy(order).sub_(tensor);
    }

    Tensor<N> sub_(Tensor<N> tensor);

    default Tensor<N> mul(Tensor<N> tensor) {
        return mul(tensor, Order.defaultOrder());
    }

    default Tensor<N> mul(Tensor<N> tensor, Order order) {
        if (isScalar()) {
            return tensor.copy(order).mul_(get());
        }
        return copy(order).mul_(tensor);
    }

    Tensor<N> mul_(Tensor<N> tensor);

    default Tensor<N> div(Tensor<N> tensor) {
        return div(tensor, Order.defaultOrder());
    }

    default Tensor<N> div(Tensor<N> tensor, Order order) {
        if (isScalar()) {
            return tensor.copy(order).div_(get());
        }
        return copy(order).div_(tensor);
    }

    Tensor<N> div_(Tensor<N> tensor);

    default Tensor<N> add(N value) {
        return add(value, Order.defaultOrder());
    }

    default Tensor<N> add(N value, Order order) {
        return copy(order).add_(value);
    }

    Tensor<N> add_(N value);

    default Tensor<N> sub(N value) {
        return sub(value, Order.defaultOrder());
    }

    default Tensor<N> sub(N value, Order order) {
        return copy(order).sub_(value);
    }

    Tensor<N> sub_(N value);

    default Tensor<N> mul(N value) {
        return mul(value, Order.defaultOrder());
    }

    default Tensor<N> mul(N value, Order order) {
        return copy(order).mul_(value);
    }

    Tensor<N> mul_(N value);

    default Tensor<N> div(N value) {
        return div(value, Order.defaultOrder());
    }

    default Tensor<N> div(N value, Order order) {
        return copy(order).div_(value);
    }

    Tensor<N> div_(N value);

    default Tensor<N> fma(N a, Tensor<N> t) {
        return fma(a, t, Order.defaultOrder());
    }

    default Tensor<N> fma(N a, Tensor<N> t, Order order) {
        return copy(order).fma_(a, t);
    }

    Tensor<N> fma_(N value, Tensor<N> t);

    N vdot(Tensor<N> tensor);

    N vdot(Tensor<N> tensor, int start, int end);

    Tensor<N> mv(Tensor<N> tensor);

    default Tensor<N> mm(Tensor<N> tensor) {
        return mm(tensor, Order.defaultOrder());
    }

    Tensor<N> mm(Tensor<N> tensor, Order askOrder);

    default CholeskyDecomposition<N> chol() {
        return chol(false);
    }

    default CholeskyDecomposition<N> chol(boolean flag) {
        return new CholeskyDecomposition<>(this, flag);
    }

    default LUDecomposition<N> lu() {
        return lu(LUDecomposition.Method.CROUT);
    }

    default LUDecomposition<N> lu(LUDecomposition.Method method) {
        return new LUDecomposition<>(this, method);
    }

    Statistics<N> stats();

    N sum();

    default Tensor<N> sum(int axis) {
        return sum(Order.defaultOrder(), axis);
    }

    Tensor<N> sum(Order order, int axis);

    N nanSum();

    default Tensor<N> nanSum(int axis) {
        return nanSum(Order.defaultOrder(), axis);
    }

    Tensor<N> nanSum(Order order, int axis);

    N prod();

    default Tensor<N> prod(int axis) {
        return prod(Order.defaultOrder(), axis);
    }

    Tensor<N> prod(Order order, int axis);

    N nanProd();

    default Tensor<N> nanProd(int axis) {
        return nanProd(Order.defaultOrder(), axis);
    }

    Tensor<N> nanProd(Order order, int axis);

    N max();

    default Tensor<N> max(int axis) {
        return max(Order.defaultOrder(), axis);
    }

    Tensor<N> max(Order order, int axis);

    N nanMax();

    default Tensor<N> nanMax(int axis) {
        return nanMax(Order.defaultOrder(), axis);
    }

    Tensor<N> nanMax(Order order, int axis);

    N min();

    default Tensor<N> min(int axis) {
        return min(Order.defaultOrder(), axis);
    }

    Tensor<N> min(Order order, int axis);

    N nanMin();

    default Tensor<N> nanMin(int axis) {
        return nanMin(Order.defaultOrder(), axis);
    }

    Tensor<N> nanMin(Order order, int axis);

    int nanCount();

    int zeroCount();

    /**
     * Creates a copy of the original tensor with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @return new copy of the tensor
     */
    default Tensor<N> copy() {
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
    Tensor<N> copy(Order askOrder);

    default Tensor<N> copyTo(Tensor<N> dst) {
        return copyTo(dst, Order.S);
    }

    Tensor<N> copyTo(Tensor<N> dst, Order askOrder);

    VarDouble dv();

    default boolean deepEquals(Object t) {
        return deepEquals(t, 1e-100);
    }

    default boolean deepEquals(Object t, double tol) {
        if (t instanceof Tensor<?> dt) {
            if (!layout().shape().equals(dt.layout().shape())) {
                return false;
            }
            var it1 = iterator(Order.C);
            var it2 = dt.iterator(Order.C);

            while (it1.hasNext()) {
                Number v1 = it1.next();
                Number v2 = it2.next();

                if (v1 == null && v2 == null) {
                    continue;
                }
                if (v1 == null || v2 == null) {
                    return false;
                }
                if (Math.abs(v1.doubleValue() - v2.doubleValue()) > tol) {
                    return false;
                }
            }
        }
        return true;
    }
}