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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import rapaio.data.VarDouble;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.matrix.CholeskyDecomposition;
import rapaio.math.tensor.matrix.EigenDecomposition;
import rapaio.math.tensor.matrix.LUDecomposition;
import rapaio.math.tensor.matrix.QRDecomposition;
import rapaio.math.tensor.matrix.SVDecomposition;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.printer.Printable;
import rapaio.util.function.IntIntBiFunction;

/**
 * Parametrized interface for tensors. A tensor is a multidimensional array. A tensor is homogeneous in that it contains
 * elements of the same type. Elements are indexed organized in zero, one or multiple dimensions.
 * <p>
 * Tensors with a low number of dimensions are known also under specific names:
 * <ul>
 *     <li>scalar</li> a tensor with zero dimensions which contains a single element
 *     <li>vector</li> a tensor with one dimension
 *     <li>matrix</li> a tensor with two dimensions
 * </ul>
 * <p>
 * The type of data elements from a tensor is marked as a generic data type and also described by {@link #dtype()}.
 * <p>
 * A tensor is created by a factory which implements {@link TensorManager}. Each tensor provides a link towards the manager
 * which created it through {@link #manager()}.
 * <p>
 * The elements are logically organized like a hyper cube with a given number of dimensions {@link #rank()}. The size of each
 * dimension is described by a {@link Shape} object and all the details related with how the elements are indexed and where is described
 * by {@link Layout}. The implemented layout is a stride array layout provided by {@link rapaio.math.tensor.layout.StrideLayout}, but
 * other layouts could be implemented (for example for special matrices or for sparse formats).
 *
 * @param <N> Generic data type which can be Byte, Integer, Float or Double.
 */
public interface Tensor<N extends Number> extends Printable, Iterable<N> {

    /**
     * Tensor manager which created this tensor instance.
     */
    TensorManager manager();

    /**
     * {@link DType} describes the data type of the elements contained by the tensor and provides also related utilities like value
     * casting.
     *
     * @return tensor data type
     */
    DType<N> dtype();

    /**
     * Tensor layout contains the complete information about logical layout of data elements in storage memory.
     *
     * @return tensor layout
     */
    Layout layout();

    /**
     * Shape describes the number of dimensions and the size on each dimension of the multi dimensional elements.
     *
     * @return tensor shape
     */
    default Shape shape() {
        return layout().shape();
    }

    /**
     * Rank is the number of dimensions for the tensor.
     *
     * @return number of dimensions or rank
     */
    default int rank() {
        return layout().rank();
    }

    /**
     * @return array of semi-positive dimension sizes
     */
    default int[] dims() {
        return layout().shape().dims();
    }

    /**
     * Size of the dimension
     *
     * @param axis the index of that dimension
     * @return size of the dimension for the given {@code axis}
     */
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
     * A scalar is a tensor with no dimensions.
     *
     * @return true if the rank of tensor is 0
     */
    default boolean isScalar() {
        return rank() == 0;
    }

    /**
     * A vector is a tensor with one dimension.
     *
     * @return true if the rank of the tensor is 1
     */
    default boolean isVector() {
        return rank() == 1;
    }

    /**
     * A matrix is a tensor with two dimensions.
     *
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
     * <p>
     * The order in which elements are read is {@code C} if data is stored in C order, {@code F} if data is stored
     * in F order, and default for the other cases.
     *
     * @param shape destination shape
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     * @see Tensor#reshape(Shape, Order)
     */
    default Tensor<N> reshape(Shape shape) {
        return reshape(shape, Order.A);
    }

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     * <p>
     * The indexes are interpreted according with order parameter:
     * <ul>
     *     <li>Order.C</li> indexes are read in C order, last dimension is the fastest dimension
     *     <li>Order.F</li> first dimension is the fastest dimension
     *     <li>Order.A</li> if data is stored in C format, than follows C order, if data is stored in F format it follows F order, otherwise
     *     it is the default order {@link Order#defaultOrder()}.
     *     <li>Order.S</li> storage order is not allowed
     * </ul>
     * <p>
     * Notice that the asked order is not the order in which data is stored, but in which data is interpreted for reshape.
     * If a new copy is created, that will also be the order in which new tensor copy will store data
     *
     * @param shape    destination shape
     * @param askOrder destination order, if the data will be copied, otherwise the parameter is ignored.
     * @return new tensor instance, wrapping, if possible, the data from the old tensor.
     */
    Tensor<N> reshape(Shape shape, Order askOrder);

    /**
     * Creates a new transposed tensor. Data will be copied and stored with default order.
     *
     * @return copy of the transposed vector
     */
    default Tensor<N> t() {
        return t(Order.defaultOrder());
    }

    /**
     * Creates a new transposed tensor. Data will be stored in the specified order given as parameter.
     * <p>
     * The only accepted orders are C order and F order.
     *
     * @param askOrder storage order
     * @return copy of the transposed vector
     */
    default Tensor<N> t(Order askOrder) {
        return t_().copy(askOrder);
    }

    /**
     * Transpose of a tensor. A transposed tensor is a tensor which reverts axis, the first axis becomes the last,
     * the second axis becomes the second to last and so on.
     * <p>
     * Data storage remain the same, no new storage copy is created.
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
     * Collapses the given axes if are of dimension one. This operation does not create a new copy of the data.
     * If any dimension doesn't have size one, the dimension will remain as it is.
     *
     * @return view of the same tensor with the given dimensions equal with one collapsed
     */
    Tensor<N> squeeze(int... axes);

    /**
     * Creates a new tensor view with an additional dimensions at the position specified by {@param axes}.
     * Specified axes value should be between 0 (inclusive) and the number of dimensions plus the number of added axes (exclusive).
     *
     * @param axes indexes of the axes to be added
     * @return new view tensor with added axes
     */
    Tensor<N> stretch(int... axes);

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
     * Combined method of a chain call for {@link #stretch(int...)} and {@link #expand(int, int)} for a single axis.
     * It creates a new dimension with repeated data along the new dimension.
     *
     * @param axis the index of the new dimension, if there is already a dimension on that position, that dimensions and all dimension
     *             to the left are shifted one position
     * @param dim  the size of the new dimension
     * @return new view with repeated data along a new dimension
     */
    default Tensor<N> strexp(int axis, int dim) {
        return stretch(axis).expand(axis, dim);
    }

    /**
     * Creates a tensor view with dimensions permuted in the order specified in parameter. The
     * parameter is an integer array containing all values from closed interval {@code [0,(rank-1)]}.
     * The order in which those values are passed defined the dimension permutation.
     *
     * @param dims dimension permutation
     * @return new tensor view with permuted dimensions
     */
    Tensor<N> permute(int... dims);

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
     * @param axis  axis to be truncated
     * @param start start index inclusive
     * @param end   end index exclusive
     * @return new view tensor with truncated axis
     */
    default Tensor<N> narrow(int axis, int start, int end) {
        return narrow(axis, true, start, end);
    }

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

    /**
     * Takes values along a given axis from the specified indices and squeeze the given axis if a single index is requested. For example,
     * one can take a single row from a matrix tensor and the resulting tensor will have a single dimension, aka the resulting
     * tensor will be a vector. This operation will create a view when is possible, otherwise will create
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
    default Tensor<N> takesq(int axis, int... indices) {
        return takesq(Order.defaultOrder(), axis, indices);
    }

    /**
     * Takes values along a given axis from the specified indices and squeeze the given axis if a single index is requested. For example,
     * one can take a single row from a matrix tensor and the resulting tensor will have a single dimension, aka the resulting
     * tensor will be a vector. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting tensor will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the order specified by parameter.
     *
     * @param order   order specified for the new tensor, if a copy of the data is required
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return tensor with mapped values along the given dimension
     */
    default Tensor<N> takesq(Order order, int axis, int... indices) {
        return take(order, axis, indices).squeeze(axis);
    }

    /**
     * Removes values along a given dimension from the specified indices. This operation is similar with {@link #take(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the default order.
     *
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return tensor with removes values along the given dimension
     */
    default Tensor<N> remove(int axis, int... indices) {
        return remove(Order.defaultOrder(), axis, indices);
    }

    /**
     * Removes values along a given dimension from the specified indices. This operation is similar with {@link #take(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the parameter specified order.
     *
     * @param order   order of the copied data, if a copy is needed
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return tensor with removed values along the given dimension
     */
    default Tensor<N> remove(Order order, int axis, int... indices) {
        Set<Integer> toRemove = new HashSet<>();
        for (int i : indices) {
            toRemove.add(i);
        }
        List<Integer> toKeep = new ArrayList<>();
        for (int i = 0; i < dim(axis); i++) {
            if (!toRemove.contains(i)) {
                toKeep.add(i);
            }
        }
        return take(order, axis, toKeep.stream().mapToInt(i -> i).toArray());
    }

    /**
     * Removes values along a given dimension from the specified indices and squeeze the axis dimension if a single index is remaining.
     * This operation is similar with {@link #take(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the default order.
     *
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return squeezed tensor with removed values along the given dimension
     */
    default Tensor<N> removesq(int axis, int... indices) {
        return removesq(Order.defaultOrder(), axis, indices);
    }

    /**
     * Removes values along a given dimension from the specified indices and squeeze the axis dimension if a single index is remaining.
     * This operation is similar with {@link #take(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the parameter specified order.
     *
     * @param order   order of the copied data, if a copy is needed
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return squeezed tensor with removed values along the given dimension
     */
    default Tensor<N> removesq(Order order, int axis, int... indices) {
        return remove(order, axis, indices).squeeze(axis);
    }

    /**
     * Creates a new tensor with values sorted along the dimension given as parameters. The order is ascending or descending, given
     * as parameter.
     * <p>
     * The order of the new tensor is the default order.
     *
     * @param axis dimension along which the values will be sorted
     * @param asc  if true the values will be sorted in ascending order, otherwise in descending order
     * @return a new copy tensor with values sorted along the given dimension
     */
    default Tensor<N> sort(int axis, boolean asc) {
        return sort(Order.defaultOrder(), axis, asc);
    }

    /**
     * Creates a new tensor with values sorted along the dimension given as parameters. The order is ascending or descending, given
     * as parameter.
     * <p>
     * The order of the new tensor is the order specified as parameter.
     *
     * @param order order of the new tensor
     * @param axis  dimension along which the values will be sorted
     * @param asc   if true the values will be sorted in ascending order, otherwise in descending order
     * @return a new copy tensor with values sorted along the given dimension
     */
    default Tensor<N> sort(Order order, int axis, boolean asc) {
        return copy(order).sort_(axis, asc);
    }

    /**
     * Sort in place values along the dimension given as parameter. The order is ascending or descending, given
     * as parameter.
     *
     * @param axis dimension along which the values will be sorted
     * @param asc  if true the values will be sorted in ascending order, otherwise in descending order
     * @return same tensor instance with values sorted along the given dimension
     */
    Tensor<N> sort_(int axis, boolean asc);

    /**
     * Sorts indices given as an array of parameters according to the values from flatten tensor.
     * Tensor must have a single dimension with size greater than the biggest index value.
     *
     * @param indices indices which will be sorted
     * @param asc     if true, than sort ascending, descending otherwise
     */
    void argSort(int[] indices, boolean asc);

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

    default Stream<N> stream() {
        return stream(Order.defaultOrder());
    }

    Stream<N> stream(Order order);

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
     * Creates a new tensor in the default storage order, having as values the result of
     * a function which receives as parameters two integers: order index and storage pointer value.
     * <p>
     * The order index is a zero integer increasing value determined by the order in which
     * elements are parsed. The storage pointer describes where the value will be stored in
     * storage layer.
     *
     * @param fun function which produces values
     * @return value to be stored
     */
    default Tensor<N> apply(IntIntBiFunction<N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    /**
     * Creates a new tensor in the order determined by parameter, having as values the result of
     * a function which receives as parameters two integers: order index and storage pointer value.
     * <p>
     * The order index is a zero integer increasing value determined by the order in which
     * elements are parsed. The storage pointer describes where the value will be stored in
     * storage layer.
     *
     * @param fun function which produces values
     * @return value to be stored
     */
    default Tensor<N> apply(Order askOrder, IntIntBiFunction<N> fun) {
        return copy(askOrder).apply_(askOrder, fun);
    }

    /**
     * Changes values from tensor in the default order, having as values the result of
     * a function which receives as parameters two integers: order index and storage pointer value.
     * <p>
     * The order index is a zero integer increasing value determined by the order in which
     * elements are parsed. The storage pointer describes where the value will be stored in
     * storage layer.
     * <p>
     * This function acts in place, does not create new storage.
     *
     * @param fun function which produces values
     * @return value to be stored
     */
    default Tensor<N> apply_(IntIntBiFunction<N> fun) {
        return apply_(Order.defaultOrder(), fun);
    }

    /**
     * Changes values from tensor in the order specified by parameter, having as values the result of
     * a function which receives as parameters two integers: order index and storage pointer value.
     * <p>
     * The order index is a zero integer increasing value determined by the order in which
     * elements are parsed. The storage pointer describes where the value will be stored in
     * storage layer.
     * <p>
     * This function acts in place, does not create new storage.
     *
     * @param fun function which produces values
     * @return value to be stored
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

    Tensor<N> unaryOp(TensorUnaryOp op);

    Tensor<N> unaryOp(TensorUnaryOp op, Order order);

    Tensor<N> unaryOp_(TensorUnaryOp op);

    default Tensor<N> clamp(N min, N max) {
        return unaryOp(TensorOp.clamp(dtype(), min, max));
    }

    default Tensor<N> clamp(Order order, N min, N max) {
        return unaryOp(TensorOp.clamp(dtype(), min, max), order);
    }

    default Tensor<N> clamp_(N min, N max) {
        return unaryOp_(TensorOp.clamp(dtype(), min, max));
    }

    default Tensor<N> rint() {
        return unaryOp(TensorOp.rint());
    }

    default Tensor<N> rint(Order order) {
        return unaryOp(TensorOp.rint(), order);
    }

    default Tensor<N> rint_() {
        return unaryOp_(TensorOp.rint());
    }

    default Tensor<N> ceil() {
        return unaryOp(TensorOp.ceil());
    }

    default Tensor<N> ceil(Order order) {
        return unaryOp(TensorOp.ceil(), order);
    }

    default Tensor<N> ceil_() {
        return unaryOp_(TensorOp.ceil());
    }

    default Tensor<N> floor() {
        return unaryOp(TensorOp.floor());
    }

    default Tensor<N> floor(Order order) {
        return unaryOp(TensorOp.floor(), order);
    }

    default Tensor<N> floor_() {
        return unaryOp_(TensorOp.floor());
    }

    default Tensor<N> abs() {
        return unaryOp(TensorOp.abs());
    }

    default Tensor<N> abs(Order order) {
        return unaryOp(TensorOp.abs(), order);
    }

    default Tensor<N> abs_() {
        return unaryOp_(TensorOp.abs());
    }

    default Tensor<N> negate() {
        return unaryOp(TensorOp.neg());
    }

    default Tensor<N> negate(Order order) {
        return unaryOp(TensorOp.neg(), order);
    }

    default Tensor<N> negate_() {
        return unaryOp_(TensorOp.neg());
    }

    default Tensor<N> log() {
        return unaryOp(TensorOp.log());
    }

    default Tensor<N> log(Order order) {
        return unaryOp(TensorOp.log(), order);
    }

    default Tensor<N> log_() {
        return unaryOp_(TensorOp.log());
    }

    default Tensor<N> log1p() {
        return unaryOp(TensorOp.log1p());
    }

    default Tensor<N> log1p(Order order) {
        return unaryOp(TensorOp.log1p(), order);
    }

    default Tensor<N> log1p_() {
        return unaryOp_(TensorOp.log1p());
    }

    default Tensor<N> exp() {
        return unaryOp(TensorOp.exp());
    }

    default Tensor<N> exp(Order order) {
        return unaryOp(TensorOp.exp(), order);
    }

    default Tensor<N> exp_() {
        return unaryOp_(TensorOp.exp());
    }

    default Tensor<N> expm1() {
        return unaryOp(TensorOp.expm1());
    }

    default Tensor<N> expm1(Order order) {
        return unaryOp(TensorOp.expm1(), order);
    }

    default Tensor<N> expm1_() {
        return unaryOp_(TensorOp.expm1());
    }

    default Tensor<N> sin() {
        return unaryOp(TensorOp.sin());
    }

    default Tensor<N> sin(Order order) {
        return unaryOp(TensorOp.sin(), order);
    }

    default Tensor<N> sin_() {
        return unaryOp_(TensorOp.sin());
    }

    default Tensor<N> asin() {
        return unaryOp(TensorOp.asin());
    }

    default Tensor<N> asin(Order order) {
        return unaryOp(TensorOp.asin(), order);
    }

    default Tensor<N> asin_() {
        return unaryOp_(TensorOp.asin());
    }

    default Tensor<N> sinh() {
        return unaryOp(TensorOp.sinh());
    }

    default Tensor<N> sinh(Order order) {
        return unaryOp(TensorOp.sinh(), order);
    }

    default Tensor<N> sinh_() {
        return unaryOp_(TensorOp.sinh());
    }

    default Tensor<N> cos() {
        return unaryOp(TensorOp.cos());
    }

    default Tensor<N> cos(Order order) {
        return unaryOp(TensorOp.cos(), order);
    }

    default Tensor<N> cos_() {
        return unaryOp_(TensorOp.cos());
    }

    default Tensor<N> acos() {
        return unaryOp(TensorOp.acos());
    }

    default Tensor<N> acos(Order order) {
        return unaryOp(TensorOp.acos(), order);
    }

    default Tensor<N> acos_() {
        return unaryOp_(TensorOp.acos());
    }

    default Tensor<N> cosh() {
        return unaryOp(TensorOp.cosh());
    }

    default Tensor<N> cosh(Order order) {
        return unaryOp(TensorOp.cosh(), order);
    }

    default Tensor<N> cosh_() {
        return unaryOp_(TensorOp.cosh());
    }

    default Tensor<N> tan() {
        return unaryOp(TensorOp.tan());
    }

    default Tensor<N> tan(Order order) {
        return unaryOp(TensorOp.tan(), order);
    }

    default Tensor<N> tan_() {
        return unaryOp_(TensorOp.tan());
    }

    default Tensor<N> atan() {
        return unaryOp(TensorOp.atan());
    }

    default Tensor<N> atan(Order order) {
        return unaryOp(TensorOp.atan(), order);
    }

    default Tensor<N> atan_() {
        return unaryOp_(TensorOp.atan());
    }

    default Tensor<N> tanh() {
        return unaryOp(TensorOp.tanh());
    }

    default Tensor<N> tanh(Order order) {
        return unaryOp(TensorOp.tanh(), order);
    }

    default Tensor<N> tanh_() {
        return unaryOp_(TensorOp.tanh());
    }

    default Tensor<N> sqr() {
        return unaryOp(TensorOp.sqr());
    }

    default Tensor<N> sqr(Order order) {
        return unaryOp(TensorOp.sqr(), order);
    }

    default Tensor<N> sqr_() {
        return unaryOp_(TensorOp.sqr());
    }

    default Tensor<N> sqrt() {
        return unaryOp(TensorOp.sqrt());
    }

    default Tensor<N> sqrt(Order order) {
        return unaryOp(TensorOp.sqrt(), order);
    }

    default Tensor<N> sqrt_() {
        return unaryOp_(TensorOp.sqrt());
    }

    <M extends Number> Tensor<N> binaryOp(TensorBinaryOp op, Tensor<M> t, Order order);

    <M extends Number> Tensor<N> binaryOp_(TensorBinaryOp op, Tensor<M> value);

    <M extends Number> Tensor<N> binaryOp(TensorBinaryOp op, M value, Order order);

    <M extends Number> Tensor<N> binaryOp_(TensorBinaryOp op, M value);

    default <M extends Number> Tensor<N> add(Tensor<M> tensor) {
        return binaryOp(TensorOp.add(), tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> add(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.add(), tensor, order);
    }

    default <M extends Number> Tensor<N> add_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.add(), tensor);
    }

    default <M extends Number> Tensor<N> badd(int axis, Tensor<M> tensor) {
        return badd(axis, tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> badd(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).add_(get());
        }
        return copy(order).badd_(axis, tensor);
    }

    default <M extends Number> Tensor<N> badd_(int axis, Tensor<M> tensor) {
        return add_(tensor.strexp(axis, dim(axis)));
    }

    default <M extends Number> Tensor<N> sub(Tensor<M> tensor) {
        return binaryOp(TensorOp.sub(), tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> sub(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.sub(), tensor, order);
    }

    default <M extends Number> Tensor<N> sub_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.sub(), tensor);
    }

    default <M extends Number> Tensor<N> bsub(int axis, Tensor<M> tensor) {
        return bsub(axis, tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> bsub(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).sub_(get());
        }
        return copy(order).bsub_(axis, tensor);
    }

    default <M extends Number> Tensor<N> bsub_(int axis, Tensor<M> tensor) {
        return sub_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    default <M extends Number> Tensor<N> mul(Tensor<M> tensor) {
        return binaryOp(TensorOp.mul(), tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> mul(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.mul(), tensor, order);
    }

    default <M extends Number> Tensor<N> mul_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.mul(), tensor);
    }

    default <M extends Number> Tensor<N> bmul(int axis, Tensor<M> tensor) {
        return bmul(axis, tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> bmul(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).mul_(get());
        }
        return copy(order).bmul_(axis, tensor);
    }

    default <M extends Number> Tensor<N> bmul_(int axis, Tensor<M> tensor) {
        return mul_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    default <M extends Number> Tensor<N> div(Tensor<M> tensor) {
        return binaryOp(TensorOp.div(), tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> div(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.div(), tensor, order);
    }

    default <M extends Number> Tensor<N> div_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.div(), tensor);
    }

    default <M extends Number> Tensor<N> bdiv(int axis, Tensor<M> tensor) {
        return bdiv(axis, tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> bdiv(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).div_(get());
        }
        return copy(order).bdiv_(axis, tensor);
    }

    default <M extends Number> Tensor<N> bdiv_(int axis, Tensor<M> tensor) {
        return div_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    default <M extends Number> Tensor<N> min(Tensor<M> tensor) {
        return binaryOp(TensorOp.min(), tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> min(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.min(), tensor, order);
    }

    default <M extends Number> Tensor<N> min_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.min(), tensor);
    }

    default <M extends Number> Tensor<N> bmin(int axis, Tensor<M> tensor) {
        return bmin(axis, tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> bmin(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).min_(get());
        }
        return copy(order).bmin_(axis, tensor);
    }

    default <M extends Number> Tensor<N> bmin_(int axis, Tensor<M> tensor) {
        return min_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    default <M extends Number> Tensor<N> max(Tensor<M> tensor) {
        return binaryOp(TensorOp.max(), tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> max(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.max(), tensor, order);
    }

    default <M extends Number> Tensor<N> max_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.max(), tensor);
    }

    default <M extends Number> Tensor<N> bmax(int axis, Tensor<M> tensor) {
        return bmax(axis, tensor, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> bmax(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).max_(get());
        }
        return copy(order).bmax_(axis, tensor);
    }

    default <M extends Number> Tensor<N> bmax_(int axis, Tensor<M> tensor) {
        return max_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    default Tensor<N> add(N value) {
        return binaryOp(TensorOp.add(), value, Order.defaultOrder());
    }

    default Tensor<N> add(N value, Order order) {
        return binaryOp(TensorOp.add(), value, order);
    }

    default Tensor<N> add_(N value) {
        return binaryOp_(TensorOp.add(), value);
    }

    default Tensor<N> sub(N value) {
        return binaryOp(TensorOp.sub(), value, Order.defaultOrder());
    }

    default Tensor<N> sub(N value, Order order) {
        return binaryOp(TensorOp.sub(), value, order);
    }

    default Tensor<N> sub_(N value) {
        return binaryOp_(TensorOp.sub(), value);
    }

    default Tensor<N> mul(N value) {
        return binaryOp(TensorOp.mul(), value, Order.defaultOrder());
    }

    default Tensor<N> mul(N value, Order order) {
        return binaryOp(TensorOp.mul(), value, order);
    }

    default Tensor<N> mul_(N value) {
        return binaryOp_(TensorOp.mul(), value);
    }

    default Tensor<N> div(N value) {
        return binaryOp(TensorOp.div(), value, Order.defaultOrder());
    }

    default Tensor<N> div(N value, Order order) {
        return binaryOp(TensorOp.div(), value, order);
    }

    default Tensor<N> div_(N value) {
        return binaryOp_(TensorOp.div(), value);
    }

    default Tensor<N> min(N value) {
        return binaryOp(TensorOp.min(), value, Order.defaultOrder());
    }

    default Tensor<N> min(N value, Order order) {
        return binaryOp(TensorOp.min(), value, order);
    }

    default Tensor<N> min_(N value) {
        return binaryOp_(TensorOp.min(), value);
    }

    default Tensor<N> max(N value) {
        return binaryOp(TensorOp.max(), value, Order.defaultOrder());
    }

    default Tensor<N> max(N value, Order order) {
        return binaryOp(TensorOp.max(), value, order);
    }

    default Tensor<N> max_(N value) {
        return binaryOp_(TensorOp.max(), value);
    }

    default <M extends Number> Tensor<N> fma(N a, Tensor<M> t) {
        return fma(a, t, Order.defaultOrder());
    }

    default <M extends Number> Tensor<N> fma(N a, Tensor<M> t, Order order) {
        return copy(order).fma_(a, t);
    }

    /**
     * Adds in place the given matrix {@code t} multiplied by {@code factor} to the tensor element wise.
     *
     * @param factor multiplication factor
     * @param t      tensor to be multiplied and added to the current one
     * @return same tensor with values changed
     */
    <M extends Number> Tensor<N> fma_(N factor, Tensor<M> t);

    default Tensor<N> outer(Tensor<N> t) {
        if (!isVector() || !t.isVector()) {
            throw new IllegalArgumentException("Outer product is available only for vectors.");
        }
        return stretch(1).mm(t.stretch(0));
    }

    N vdot(Tensor<N> tensor);

    N vdot(Tensor<N> tensor, int start, int end);

    Tensor<N> mv(Tensor<N> tensor);

    default Tensor<N> mm(Tensor<N> tensor) {
        return mm(tensor, Order.defaultOrder());
    }

    Tensor<N> mm(Tensor<N> tensor, Order askOrder);

    default boolean isSymmetric() {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Available only for matrices.");
        }
        if (dim(0) != dim(1)) {
            return false;
        }
        for (int i = 0; i < dim(0); i++) {
            for (int j = i + 1; j < dim(1); j++) {
                if (getDouble(i, j) != getDouble(j, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    default CholeskyDecomposition<N> cholesky() {
        return cholesky(false);
    }

    default CholeskyDecomposition<N> cholesky(boolean flag) {
        return new CholeskyDecomposition<>(this, flag);
    }

    default LUDecomposition<N> lu() {
        return lu(LUDecomposition.Method.CROUT);
    }

    default LUDecomposition<N> lu(LUDecomposition.Method method) {
        return new LUDecomposition<>(this, method);
    }

    default QRDecomposition<N> qr() {
        return new QRDecomposition<>(this);
    }

    default EigenDecomposition<N> eig() {
        return new EigenDecomposition<>(this);
    }

    default SVDecomposition<N> svd() {
        return svd(true, true);
    }

    default SVDecomposition<N> svd(boolean wantu, boolean wantv) {
        return new SVDecomposition<>(this, wantu, wantv);
    }

    Tensor<N> scatter();

    N trace();

    Tensor<N> diag();

    default N norm() {
        return norm(dtype().castValue(2));
    }

    N norm(N p);

    default Tensor<N> normalize(N p) {
        return copy(Order.defaultOrder()).normalize_(p);
    }

    default Tensor<N> normalize(Order order, N p) {
        return copy(order).normalize_(p);
    }

    Tensor<N> normalize_(N p);

    N mean();

    default Tensor<N> mean(int axis) {
        return mean(Order.defaultOrder(), axis);
    }

    Tensor<N> mean(Order order, int axis);

    N nanMean();

    N std();

    default Tensor<N> std(int axis) {
        return std(Order.defaultOrder(), axis);
    }

    Tensor<N> std(Order order, int axis);

    N stdc(int ddof);

    default Tensor<N> stdc(int axis, int ddof) {
        return stdc(Order.defaultOrder(), axis, ddof);
    }

    Tensor<N> stdc(Order order, int axis, int ddof);

    default N var() {
        return varc(0);
    }

    default Tensor<N> var(int axis) {
        return var(Order.defaultOrder(), axis);
    }

    default Tensor<N> var(Order order, int axis) {
        return varc(order, axis, 0);
    }

    N varc(int ddof);

    default Tensor<N> varc(int axis, int ddof) {
        return varc(Order.defaultOrder(), axis, ddof);
    }

    Tensor<N> varc(Order order, int axis, int ddof);

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

    default int argmax() {
        return argmax(Order.defaultOrder());
    }

    int argmax(Order order);

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

    default int argmin() {
        return argmin(Order.defaultOrder());
    }

    int argmin(Order order);

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

    /**
     * Computes the number of NaN values. For integer value types this operation returns 0.
     *
     * @return number of NaN values
     */
    int nanCount();

    /**
     * Computes the number of values equal with zero.
     *
     * @return number of zero values
     */
    int zeroCount();

    default <M extends Number> Tensor<M> cast(DType<M> dType) {
        return cast(dType, Order.defaultOrder());
    }

    <M extends Number> Tensor<M> cast(DType<M> dType, Order askOrder);

    /**
     * Creates a padded copy of a tensor along the first dimension. The padded copy will be a tensor with the same shape other then the
     * first dimension which will have size {@code before + dim(0) + after}, having first and last elements padded with 0.
     * <p>
     *
     * @return resized padded copy of the original tensor
     */
    default Tensor<N> pad(int before, int after) {
        return pad(0, before, after);
    }

    /**
     * Creates a padded copy of a tensor along a given dimension. The padded copy will be a tensor with the same shape other then the
     * specified dimension which will have size {@code before + dim(axis) + after}, having first and last elements padded with 0.
     * <p>
     *
     * @return resized padded copy of the original tensor
     */
    default Tensor<N> pad(int axis, int before, int after) {
        int[] newDims = Arrays.copyOf(dims(), rank());
        newDims[axis] += before + after;
        Tensor<N> copy = manager().ofType(dtype()).zeros(Shape.of(newDims), Order.defaultOrder());
        copyTo(copy.narrow(axis, true, before, before + dim(axis)));
        return copy;
    }

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

    Tensor<N> copyTo(Tensor<N> dst);

    VarDouble dv();

    default double[] toDoubleArray() {
        return toDoubleArray(Order.defaultOrder());
    }

    double[] toDoubleArray(Order askOrder);

    default double[] asDoubleArray() {
        return asDoubleArray(Order.defaultOrder());
    }

    double[] asDoubleArray(Order askOrder);

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
            return true;
        }
        return false;
    }
}
