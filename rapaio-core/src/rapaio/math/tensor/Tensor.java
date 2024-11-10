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

package rapaio.math.tensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import rapaio.data.OperationNotAvailableException;
import rapaio.data.VarDouble;
import rapaio.math.tensor.iterators.PointerIterator;
import rapaio.math.tensor.manager.AbstractStrideTensor;
import rapaio.math.tensor.matrix.CholeskyDecomposition;
import rapaio.math.tensor.matrix.EigenDecomposition;
import rapaio.math.tensor.matrix.LUDecomposition;
import rapaio.math.tensor.matrix.QRDecomposition;
import rapaio.math.tensor.matrix.SVDecomposition;
import rapaio.math.tensor.operator.Broadcast;
import rapaio.math.tensor.operator.Compare;
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorOp;
import rapaio.math.tensor.operator.TensorReduceOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.printer.Printable;
import rapaio.util.NotImplementedException;
import rapaio.util.function.IntIntBiFunction;

/**
 * Parametrized interface for tensors. A tensor is a multidimensional array which contains elements of the same type.
 * Elements are indexed organized in zero, one or multiple dimensions.
 * <p>
 * Tensors with a low number of dimensions are known also under more specific names:
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
 * dimension is described by a {@link Shape} object and the {@link Layout} describes how the details related
 * with how the elements' indexing.
 * The implemented layout is a stride array layout provided by {@link rapaio.math.tensor.layout.StrideLayout}, but
 * other layouts could be implemented (for example for special matrices or for sparse formats).
 *
 * @param <N> Generic data type which can be Byte, Integer, Float or Double.
 */
public abstract sealed class Tensor<N extends Number> implements Printable, Iterable<N> permits AbstractStrideTensor {

    protected final TensorManager manager;
    protected final Storage<N> storage;

    protected Tensor(TensorManager manager, Storage<N> storage) {
        this.manager = manager;
        this.storage = storage;
    }

    /**
     * Tensor manager which created this tensor instance.
     */
    public final TensorManager manager() {
        return manager;
    }

    /**
     * {@link DType} describes the data type of the elements contained by the tensor and provides also related utilities like value
     * casting.
     *
     * @return tensor data type
     */
    public abstract DType<N> dtype();

    /**
     * Tensor layout contains the complete information about logical layout of data elements in storage memory.
     *
     * @return tensor layout
     */
    public abstract Layout layout();

    /**
     * Shape describes the number of dimensions and the size on each dimension of the multidimensional elements.
     *
     * @return tensor shape
     */
    public final Shape shape() {
        return layout().shape();
    }

    /**
     * Rank is the number of dimensions for the tensor.
     *
     * @return number of dimensions or rank
     */
    public final int rank() {
        return layout().rank();
    }

    /**
     * @return array of semi-positive dimension sizes
     */
    public final int[] dims() {
        return layout().shape().dims();
    }

    /**
     * Size of the dimension
     *
     * @param axis the index of that dimension
     * @return size of the dimension for the given {@code axis}
     */
    public final int dim(int axis) {
        return layout().shape().dim(axis);
    }

    /**
     * Size of a tensor is the number of elements contained in tensor and is equal with
     * the product of dimension's sizes
     *
     * @return number of elements from tensor
     */
    public final int size() {
        return layout().size();
    }

    /**
     * Storage implementation which physically contains data.
     *
     * @return storage instance
     */
    public final Storage<N> storage() {
        return storage;
    }

    /**
     * A scalar is a tensor with no dimensions.
     *
     * @return true if the rank of tensor is 0
     */
    public final boolean isScalar() {
        return rank() == 0;
    }

    /**
     * A vector is a tensor with one dimension.
     *
     * @return true if the rank of the tensor is 1
     */
    public final boolean isVector() {
        return rank() == 1;
    }

    /**
     * A matrix is a tensor with two dimensions.
     *
     * @return true if the rank of the tensor is 2
     */
    public final boolean isMatrix() {
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
    public final Tensor<N> reshape(Shape shape) {
        return reshape(shape, Order.A);
    }

    /**
     * Creates a new tensor with a different shape. If possible, the data will not be copied.
     * <p>
     * In order to reshape a tensor, the source shape and destination shape must have the same size.
     * <p>
     * The indexes are interpreted according to order parameter:
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
    public abstract Tensor<N> reshape(Shape shape, Order askOrder);

    /**
     * Creates a new transposed tensor. Data will be copied and stored with default order.
     *
     * @return copy of the transposed vector
     */
    public final Tensor<N> t() {
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
    public final Tensor<N> t(Order askOrder) {
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
    public abstract Tensor<N> t_();

    /**
     * Collapses the tensor into one dimension using the default order. The order is used for reading. In the case when a view
     * can't be created, a new tensor will be created with the storage order same as reading order.
     *
     * @return a tensor with elements in given order (new copy if needed)
     */
    public final Tensor<N> ravel() {
        return ravel(Order.defaultOrder());
    }

    /**
     * Collapses the tensor into one dimension using the given order. The order is used for reading. In the case when a view
     * can't be created, a new tensor will be created with the storage order same as reading order.
     *
     * @param askOrder order of the elements
     * @return a tensor with elements in given order (new copy if needed)
     */
    public abstract Tensor<N> ravel(Order askOrder);

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is the default order.
     *
     * @return a copy of the tensor with elements in asked order.
     */
    public final Tensor<N> flatten() {
        return flatten(Order.defaultOrder());
    }

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is given as parameter.
     *
     * @param askOrder order of the elements
     * @return a copy of the tensor with elements in asked order.
     */
    public abstract Tensor<N> flatten(Order askOrder);

    /**
     * Collapses the given axes if are of dimension one. This operation does not create a new copy of the data.
     * If any dimension doesn't have size one, the dimension will remain as it is.
     *
     * @return view of the same tensor with the given dimensions equal with one collapsed
     */
    public abstract Tensor<N> squeeze(int... axes);

    /**
     * Creates a new tensor view with an additional dimensions at the position specified by {@param axes}.
     * Specified axes value should be between 0 (inclusive) and the number of dimensions plus the number of added axes (exclusive).
     *
     * @param axes indexes of the axes to be added
     * @return new view tensor with added axes
     */
    public abstract Tensor<N> stretch(int... axes);

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
    public abstract Tensor<N> expand(int axis, int dim);

    /**
     * Combined method of a chain call for {@link #stretch(int...)} and {@link #expand(int, int)} for a single axis.
     * It creates a new dimension with repeated data along the new dimension.
     *
     * @param axis the index of the new dimension, if there is already a dimension on that position, that dimensions and all dimension
     *             to the left are shifted one position
     * @param dim  the size of the new dimension
     * @return new view with repeated data along a new dimension
     */
    public final Tensor<N> strexp(int axis, int dim) {
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
    public abstract Tensor<N> permute(int... dims);

    /**
     * Creates a new tensor view with source axis moved into the given destination position.
     *
     * @param src source axis
     * @param dst destination axis position
     * @return new view tensor with moved axis
     */
    public abstract Tensor<N> moveAxis(int src, int dst);

    /**
     * Swap two axis. This does not affect the storage.
     *
     * @param src source axis
     * @param dst destination axis
     * @return new view tensor with swapped axis
     */
    public abstract Tensor<N> swapAxis(int src, int dst);

    /**
     * Creates a new tensor view with one truncated axis, all other axes remain the same.
     *
     * @param axis  axis to be truncated
     * @param start start index inclusive
     * @param end   end index exclusive
     * @return new view tensor with truncated axis
     */
    public final Tensor<N> narrow(int axis, int start, int end) {
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
    public abstract Tensor<N> narrow(int axis, boolean keepDim, int start, int end);

    /**
     * Creates a new tensor view with possibly all truncated axes.
     *
     * @param keepDim keep dimensions even if some of have length 1, false otherwise
     * @param starts  vector of indexes where narrow interval starts
     * @param ends    vector of indexes where narrow interval ends
     * @return a view with truncated axes
     */
    public abstract Tensor<N> narrowAll(boolean keepDim, int[] starts, int[] ends);

    /**
     * Splits the tensor into multiple view tensors along a given axis. The resulting tensors are narrowed versions of the original tensor,
     * with the start index being the current index, and the end being the next index or the end of the dimension.
     *
     * @param axis    axis to split along
     * @param indexes indexes to split along, being start indexes for truncation
     * @return list of new tensors with truncated data.
     */
    public abstract List<Tensor<N>> split(int axis, boolean keepDim, int... indexes);

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
    public abstract List<Tensor<N>> splitAll(boolean keepDim, int[][] indexes);

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
    public final List<Tensor<N>> chunk(int axis, boolean keepDim, int step) {
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
    public final List<Tensor<N>> chunkAll(boolean keepDim, int[] steps) {
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
    public final Tensor<N> repeat(int axis, int repeat, boolean stack) {
        return repeat(Order.defaultOrder(), axis, repeat, stack);
    }

    public final Tensor<N> repeat(Order order, int axis, int repeat, boolean stack) {
        List<Tensor<N>> copies = new ArrayList<>(repeat);
        for (int i = 0; i < repeat; i++) {
            copies.add(this);
        }
        if (stack) {
            return manager.stack(order, axis, copies);
        } else {
            return manager.concat(order, axis, copies);
        }
    }

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
    public final Tensor<N> take(int axis, int... indices) {
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
    public abstract Tensor<N> take(Order order, int axis, int... indices);

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
    public final Tensor<N> takesq(int axis, int... indices) {
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
    public final Tensor<N> takesq(Order order, int axis, int... indices) {
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
    public final Tensor<N> remove(int axis, int... indices) {
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
    public final Tensor<N> remove(Order order, int axis, int... indices) {
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
    public final Tensor<N> removesq(int axis, int... indices) {
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
    public final Tensor<N> removesq(Order order, int axis, int... indices) {
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
    public final Tensor<N> sort(int axis, boolean asc) {
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
    public final Tensor<N> sort(Order order, int axis, boolean asc) {
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
    public abstract Tensor<N> sort_(int axis, boolean asc);

    /**
     * Sorts indices given as an array of parameters according to the values from flatten tensor.
     * Tensor must have a single dimension with size greater than the biggest index value.
     *
     * @param indices indices which will be sorted
     * @param asc     sort ascending if true, descending otherwise
     */
    public abstract void argSort(int[] indices, boolean asc);

    /**
     * Get value at indexed position. An indexed position is a tuple of rank
     * dimension, with an integer value on each dimension.
     *
     * @param indexes indexed position
     * @return value at indexed position
     */
    public abstract N get(int... indexes);

    public final byte getByte(int... indexes) {
        return storage.getByte(layout().pointer(indexes));
    }

    public final int getInt(int... indexes) {
        return storage.getInt(layout().pointer(indexes));
    }

    public final float getFloat(int... indexes) {
        return storage.getFloat(layout().pointer(indexes));
    }

    public final double getDouble(int... indexes) {
        return storage.getDouble(layout().pointer(indexes));
    }

    /**
     * Sets value at indexed position.
     *
     * @param value   value to be set
     * @param indexes indexed position
     */
    public abstract void set(N value, int... indexes);

    public final void setByte(byte value, int... indexes) {
        storage.setByte(layout().pointer(indexes), value);
    }

    public final void setInt(int value, int... indexes) {
        storage.setInt(layout().pointer(indexes), value);
    }

    public final void setFloat(float value, int... indexes) {
        storage.setFloat(layout().pointer(indexes), value);
    }

    public final void setDouble(double value, int... indexes) {
        storage.setDouble(layout().pointer(indexes), value);
    }

    /**
     * Sets value at indexed position.
     *
     * @param value   value to be set
     * @param indexes indexed position
     */
    public abstract void inc(N value, int... indexes);

    public final void incDouble(double value, int... indexes) {
        storage.incDouble(layout().pointer(indexes), value);
    }

    /**
     * Get value at pointer. A pointer is an index value at the memory layout.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    public abstract N ptrGet(int ptr);

    public final byte ptrGetByte(int ptr) {
        return storage.getByte(ptr);
    }

    public final int ptrGetInt(int ptr) {
        return storage.getInt(ptr);
    }

    public final float ptrGetFloat(int ptr) {
        return storage.getFloat(ptr);
    }

    public final double ptrGetDouble(int ptr) {
        return storage.getDouble(ptr);
    }

    /**
     * Sets value at given pointer.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    public abstract void ptrSet(int ptr, N value);

    public final void ptrSetByte(int ptr, byte value) {
        storage.setByte(ptr, value);
    }

    public final void ptrSetInt(int ptr, int value) {
        storage.setInt(ptr, value);
    }

    public final void ptrSetFloat(int ptr, float value) {
        storage.setFloat(ptr, value);
    }

    public final void ptrSetDouble(int ptr, double value) {
        storage.setDouble(ptr, value);
    }

    /**
     * Produces an iterator over the values from this tensor in the
     * storage order.
     *
     * @return value iterator
     */
    public final Iterator<N> iterator() {
        return iterator(Order.S);
    }

    public final Iterator<N> iterator(Order askOrder) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(ptrIterator(askOrder), Spliterator.ORDERED), false)
                .map(storage::get).iterator();
    }

    public final Stream<N> stream() {
        return stream(Order.defaultOrder());
    }

    public final Stream<N> stream(Order order) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(order), Spliterator.ORDERED), false);
    }

    /**
     * Produces an iterator of data pointer values in the storage order.
     *
     * @return data pointer iterator
     */
    public final PointerIterator ptrIterator() {
        return ptrIterator(Order.S);
    }

    /**
     * Produces an iterator of data pointer values in the order specified
     * by parameter value.
     *
     * @param askOrder traversing order
     * @return data pointer iterator
     */
    public abstract PointerIterator ptrIterator(Order askOrder);

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
    public final Tensor<N> apply(IntIntBiFunction<N> fun) {
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
    public final Tensor<N> apply(Order askOrder, IntIntBiFunction<N> fun) {
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
    public final Tensor<N> apply_(IntIntBiFunction<N> fun) {
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
    public abstract Tensor<N> apply_(Order askOrder, IntIntBiFunction<N> fun);

    public final Tensor<N> apply(Function<N, N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    public final Tensor<N> apply(Order askOrder, Function<N, N> fun) {
        return copy(askOrder).apply_(fun);
    }

    public abstract Tensor<N> apply_(Function<N, N> fun);

    //--------- UNARY OPERATIONS ----------------//

    public final Tensor<N> unaryOp(TensorUnaryOp op) {
        return unaryOp(op, Order.defaultOrder());
    }

    public final Tensor<N> unaryOp(TensorUnaryOp op, Order order) {
        return copy(order).unaryOp_(op);
    }

    public abstract Tensor<N> unaryOp_(TensorUnaryOp op);

    public final Tensor<N> fill_(N value) {
        return unaryOp_(TensorOp.unaryFill(value));
    }

    public final Tensor<N> fill_(int value) {
        return unaryOp_(TensorOp.unaryFill(value));
    }

    public final Tensor<N> fill_(double value) {
        return unaryOp_(TensorOp.unaryFill(value));
    }

    public final Tensor<N> fillNan_(N value) {
        return unaryOp_(TensorOp.unaryFillNan(value));
    }

    public final Tensor<N> fillNan_(int value) {
        return unaryOp_(TensorOp.unaryFillNan(value));
    }

    public final Tensor<N> fillNan_(double value) {
        return unaryOp_(TensorOp.unaryFillNan(value));
    }

    public final Tensor<N> nanToNum_(N fill) {
        return unaryOp_(TensorOp.unaryNanToNum(fill, fill, fill));
    }

    public final Tensor<N> nanToNum_(int fill) {
        return unaryOp_(TensorOp.unaryNanToNum(fill, fill, fill));
    }

    public final Tensor<N> nanToNum_(double fill) {
        return unaryOp_(TensorOp.unaryNanToNum(fill, fill, fill));
    }

    public final Tensor<N> nanToNum_(N nan, N negInf, N posInf) {
        return unaryOp_(TensorOp.unaryNanToNum(nan, negInf, posInf));
    }

    public final Tensor<N> nanToNum_(int nan, int negInf, int posInf) {
        return unaryOp_(TensorOp.unaryNanToNum(nan, negInf, posInf));
    }

    public final Tensor<N> nanToNum_(double nan, double ninf, double pinf) {
        return unaryOp_(TensorOp.unaryNanToNum(nan, ninf, pinf));
    }

    public final Tensor<N> compareMask_(Compare cmp, N value) {
        return unaryOp_(TensorOp.unaryOpCompareMask(cmp, value));
    }

    public final Tensor<N> compareMask_(Compare cmp, int value) {
        return unaryOp_(TensorOp.unaryOpCompareMask(cmp, value));
    }

    public final Tensor<N> compareMask_(Compare cmp, double value) {
        return unaryOp_(TensorOp.unaryOpCompareMask(cmp, value));
    }


    public final Tensor<N> clamp(N min, N max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), min, max));
    }

    public final Tensor<N> clamp(int min, int max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), dtype().castValue(min), dtype().castValue(max)));
    }

    public final Tensor<N> clamp(double min, double max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), dtype().castValue(min), dtype().castValue(max)));
    }

    public final Tensor<N> clamp(Order order, N min, N max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), min, max), order);
    }

    public final Tensor<N> clamp(Order order, int min, int max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), dtype().castValue(min), dtype().castValue(max)), order);
    }

    public final Tensor<N> clamp(Order order, double min, double max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), dtype().castValue(min), dtype().castValue(max)), order);
    }

    public final Tensor<N> clamp_(N min, N max) {
        return unaryOp_(TensorOp.unaryClamp(dtype(), min, max));
    }

    public final Tensor<N> rint() {
        return unaryOp(TensorOp.unaryRint());
    }

    public final Tensor<N> rint(Order order) {
        return unaryOp(TensorOp.unaryRint(), order);
    }

    public final Tensor<N> rint_() {
        return unaryOp_(TensorOp.unaryRint());
    }

    public final Tensor<N> ceil() {
        return unaryOp(TensorOp.unaryCeil());
    }

    public final Tensor<N> ceil(Order order) {
        return unaryOp(TensorOp.unaryCeil(), order);
    }

    public final Tensor<N> ceil_() {
        return unaryOp_(TensorOp.unaryCeil());
    }

    public final Tensor<N> floor() {
        return unaryOp(TensorOp.unaryFloor());
    }

    public final Tensor<N> floor(Order order) {
        return unaryOp(TensorOp.unaryFloor(), order);
    }

    public final Tensor<N> floor_() {
        return unaryOp_(TensorOp.unaryFloor());
    }

    public final Tensor<N> abs() {
        return unaryOp(TensorOp.unaryAbs());
    }

    public final Tensor<N> abs(Order order) {
        return unaryOp(TensorOp.unaryAbs(), order);
    }

    public final Tensor<N> abs_() {
        return unaryOp_(TensorOp.unaryAbs());
    }

    public final Tensor<N> neg() {
        return unaryOp(TensorOp.unaryNeg());
    }

    public final Tensor<N> neg(Order order) {
        return unaryOp(TensorOp.unaryNeg(), order);
    }

    public final Tensor<N> neg_() {
        return unaryOp_(TensorOp.unaryNeg());
    }

    public final Tensor<N> log() {
        return unaryOp(TensorOp.unaryLog());
    }

    public final Tensor<N> log(Order order) {
        return unaryOp(TensorOp.unaryLog(), order);
    }

    public final Tensor<N> log_() {
        return unaryOp_(TensorOp.unaryLog());
    }

    public final Tensor<N> log1p() {
        return unaryOp(TensorOp.unaryLog1p());
    }

    public final Tensor<N> log1p(Order order) {
        return unaryOp(TensorOp.unaryLog1p(), order);
    }

    public final Tensor<N> log1p_() {
        return unaryOp_(TensorOp.unaryLog1p());
    }

    public final Tensor<N> exp() {
        return unaryOp(TensorOp.unaryExp());
    }

    public final Tensor<N> exp(Order order) {
        return unaryOp(TensorOp.unaryExp(), order);
    }

    public final Tensor<N> exp_() {
        return unaryOp_(TensorOp.unaryExp());
    }

    public final Tensor<N> expm1() {
        return unaryOp(TensorOp.unaryExpm1());
    }

    public final Tensor<N> expm1(Order order) {
        return unaryOp(TensorOp.unaryExpm1(), order);
    }

    public final Tensor<N> expm1_() {
        return unaryOp_(TensorOp.unaryExpm1());
    }

    public final Tensor<N> sin() {
        return unaryOp(TensorOp.unarySin());
    }

    public final Tensor<N> sin(Order order) {
        return unaryOp(TensorOp.unarySin(), order);
    }

    public final Tensor<N> sin_() {
        return unaryOp_(TensorOp.unarySin());
    }

    public final Tensor<N> asin() {
        return unaryOp(TensorOp.unaryAsin());
    }

    public final Tensor<N> asin(Order order) {
        return unaryOp(TensorOp.unaryAsin(), order);
    }

    public final Tensor<N> asin_() {
        return unaryOp_(TensorOp.unaryAsin());
    }

    public final Tensor<N> sinh() {
        return unaryOp(TensorOp.unarySinh());
    }

    public final Tensor<N> sinh(Order order) {
        return unaryOp(TensorOp.unarySinh(), order);
    }

    public final Tensor<N> sinh_() {
        return unaryOp_(TensorOp.unarySinh());
    }

    public final Tensor<N> cos() {
        return unaryOp(TensorOp.unaryCos());
    }

    public final Tensor<N> cos(Order order) {
        return unaryOp(TensorOp.unaryCos(), order);
    }

    public final Tensor<N> cos_() {
        return unaryOp_(TensorOp.unaryCos());
    }

    public final Tensor<N> acos() {
        return unaryOp(TensorOp.unaryAcos());
    }

    public final Tensor<N> acos(Order order) {
        return unaryOp(TensorOp.unaryAcos(), order);
    }

    public final Tensor<N> acos_() {
        return unaryOp_(TensorOp.unaryAcos());
    }

    public final Tensor<N> cosh() {
        return unaryOp(TensorOp.unaryCosh());
    }

    public final Tensor<N> cosh(Order order) {
        return unaryOp(TensorOp.unaryCosh(), order);
    }

    public final Tensor<N> cosh_() {
        return unaryOp_(TensorOp.unaryCosh());
    }

    public final Tensor<N> tan() {
        return unaryOp(TensorOp.unaryTan());
    }

    public final Tensor<N> tan(Order order) {
        return unaryOp(TensorOp.unaryTan(), order);
    }

    public final Tensor<N> tan_() {
        return unaryOp_(TensorOp.unaryTan());
    }

    public final Tensor<N> atan() {
        return unaryOp(TensorOp.unaryAtan());
    }

    public final Tensor<N> atan(Order order) {
        return unaryOp(TensorOp.unaryAtan(), order);
    }

    public final Tensor<N> atan_() {
        return unaryOp_(TensorOp.unaryAtan());
    }

    public final Tensor<N> tanh() {
        return unaryOp(TensorOp.unaryTanh());
    }

    public final Tensor<N> tanh(Order order) {
        return unaryOp(TensorOp.unaryTanh(), order);
    }

    public final Tensor<N> tanh_() {
        return unaryOp_(TensorOp.unaryTanh());
    }

    public final Tensor<N> sqr() {
        return unaryOp(TensorOp.unarySqr());
    }

    public final Tensor<N> sqr(Order order) {
        return unaryOp(TensorOp.unarySqr(), order);
    }

    public final Tensor<N> sqr_() {
        return unaryOp_(TensorOp.unarySqr());
    }

    public final Tensor<N> sqrt() {
        return unaryOp(TensorOp.unarySqrt());
    }

    public final Tensor<N> sqrt(Order order) {
        return unaryOp(TensorOp.unarySqrt(), order);
    }

    public final Tensor<N> sqrt_() {
        return unaryOp_(TensorOp.unarySqrt());
    }

    public final Tensor<N> pow(double power) {
        return unaryOp(TensorOp.unaryPow(power));
    }

    public final Tensor<N> pow(Order order, double power) {
        return unaryOp(TensorOp.unaryPow(power), order);
    }

    public final Tensor<N> pow_(double power) {
        return unaryOp_(TensorOp.unaryPow(power));
    }

    public final Tensor<N> sigmoid() {
        return unaryOp(TensorOp.unarySigmoid());
    }

    public final Tensor<N> sigmoid(Order order) {
        return unaryOp(TensorOp.unarySigmoid(), order);
    }

    public final Tensor<N> sigmoid_() {
        return unaryOp_(TensorOp.unarySigmoid());
    }

    //--------- BINARY OPERATIONS ----------------//

    public final Tensor<N> binaryOp(TensorBinaryOp op, Tensor<?> t, Order order) {
        Broadcast.ElementWise broadcast = Broadcast.elementWise(List.of(this.shape(), t.shape()));
        if (!broadcast.valid()) {
            throw new IllegalArgumentException(
                    String.format("Operation could not be applied on tensors with shape: %s, %s", shape(), t.shape()));
        }
        Tensor<N> copy = broadcast.transform(this).copy(order);
        return copy.binaryOp_(op, broadcast.transform(t));
    }

    public abstract Tensor<N> binaryOp_(TensorBinaryOp op, Tensor<?> value);

    public final <M extends Number> Tensor<N> binaryOp(TensorBinaryOp op, M value, Order order) {
        return copy(order).binaryOp_(op, value);
    }

    public abstract <M extends Number> Tensor<N> binaryOp_(TensorBinaryOp op, M value);

    public final Tensor<N> add(Tensor<?> tensor) {
        return binaryOp(TensorOp.binaryAdd(), tensor, Order.defaultOrder());
    }

    public final Tensor<N> add(Tensor<?> tensor, Order order) {
        return binaryOp(TensorOp.binaryAdd(), tensor, order);
    }

    public final Tensor<N> add_(Tensor<?> tensor) {
        return binaryOp_(TensorOp.binaryAdd(), tensor);
    }

    public final Tensor<N> sub(Tensor<?> tensor) {
        return binaryOp(TensorOp.binarySub(), tensor, Order.defaultOrder());
    }

    public final Tensor<N> sub(Tensor<?> tensor, Order order) {
        return binaryOp(TensorOp.binarySub(), tensor, order);
    }

    public final Tensor<N> sub_(Tensor<?> tensor) {
        return binaryOp_(TensorOp.binarySub(), tensor);
    }

    public final Tensor<N> mul(Tensor<?> tensor) {
        return binaryOp(TensorOp.binaryMul(), tensor, Order.defaultOrder());
    }

    public final Tensor<N> mul(Tensor<?> tensor, Order order) {
        return binaryOp(TensorOp.binaryMul(), tensor, order);
    }

    public final Tensor<N> mul_(Tensor<?> tensor) {
        return binaryOp_(TensorOp.binaryMul(), tensor);
    }

    public final Tensor<N> div(Tensor<?> tensor) {
        return binaryOp(TensorOp.binaryDiv(), tensor, Order.defaultOrder());
    }

    public final Tensor<N> div(Tensor<?> tensor, Order order) {
        return binaryOp(TensorOp.binaryDiv(), tensor, order);
    }

    public final Tensor<N> div_(Tensor<?> tensor) {
        return binaryOp_(TensorOp.binaryDiv(), tensor);
    }

    public final Tensor<N> min(Tensor<?> tensor) {
        return binaryOp(TensorOp.binaryMin(), tensor, Order.defaultOrder());
    }

    public final Tensor<N> min(Tensor<?> tensor, Order order) {
        return binaryOp(TensorOp.binaryMin(), tensor, order);
    }

    public final Tensor<N> min_(Tensor<?> tensor) {
        return binaryOp_(TensorOp.binaryMin(), tensor);
    }

    public final Tensor<N> max(Tensor<?> tensor) {
        return binaryOp(TensorOp.binaryMax(), tensor, Order.defaultOrder());
    }

    public final Tensor<N> max(Tensor<?> tensor, Order order) {
        return binaryOp(TensorOp.binaryMax(), tensor, order);
    }

    public final Tensor<N> max_(Tensor<?> tensor) {
        return binaryOp_(TensorOp.binaryMax(), tensor);
    }

    public final Tensor<N> add(N value) {
        return binaryOp(TensorOp.binaryAdd(), value, Order.defaultOrder());
    }

    public final Tensor<N> add(int value) {
        return binaryOp(TensorOp.binaryAdd(), value, Order.defaultOrder());
    }

    public final Tensor<N> add(double value) {
        return binaryOp(TensorOp.binaryAdd(), value, Order.defaultOrder());
    }

    public final Tensor<N> add(N value, Order order) {
        return binaryOp(TensorOp.binaryAdd(), value, order);
    }

    public final Tensor<N> add(int value, Order order) {
        return binaryOp(TensorOp.binaryAdd(), value, order);
    }

    public final Tensor<N> add(double value, Order order) {
        return binaryOp(TensorOp.binaryAdd(), value, order);
    }

    public final Tensor<N> add_(N value) {
        return binaryOp_(TensorOp.binaryAdd(), value);
    }

    public final Tensor<N> add_(int value) {
        return binaryOp_(TensorOp.binaryAdd(), value);
    }

    public final Tensor<N> add_(double value) {
        return binaryOp_(TensorOp.binaryAdd(), value);
    }

    public final Tensor<N> sub(N value) {
        return binaryOp(TensorOp.binarySub(), value, Order.defaultOrder());
    }

    public final Tensor<N> sub(int value) {
        return binaryOp(TensorOp.binarySub(), value, Order.defaultOrder());
    }

    public final Tensor<N> sub(double value) {
        return binaryOp(TensorOp.binarySub(), value, Order.defaultOrder());
    }

    public final Tensor<N> sub(N value, Order order) {
        return binaryOp(TensorOp.binarySub(), value, order);
    }

    public final Tensor<N> sub(int value, Order order) {
        return binaryOp(TensorOp.binarySub(), value, order);
    }

    public final Tensor<N> sub(double value, Order order) {
        return binaryOp(TensorOp.binarySub(), value, order);
    }

    public final Tensor<N> sub_(N value) {
        return binaryOp_(TensorOp.binarySub(), value);
    }

    public final Tensor<N> sub_(int value) {
        return binaryOp_(TensorOp.binarySub(), value);
    }

    public final Tensor<N> sub_(double value) {
        return binaryOp_(TensorOp.binarySub(), value);
    }

    public final Tensor<N> mul(N value) {
        return binaryOp(TensorOp.binaryMul(), value, Order.defaultOrder());
    }

    public final Tensor<N> mul(int value) {
        return binaryOp(TensorOp.binaryMul(), value, Order.defaultOrder());
    }

    public final Tensor<N> mul(double value) {
        return binaryOp(TensorOp.binaryMul(), value, Order.defaultOrder());
    }

    public final Tensor<N> mul(N value, Order order) {
        return binaryOp(TensorOp.binaryMul(), value, order);
    }

    public final Tensor<N> mul(int value, Order order) {
        return binaryOp(TensorOp.binaryMul(), value, order);
    }

    public final Tensor<N> mul(double value, Order order) {
        return binaryOp(TensorOp.binaryMul(), value, order);
    }

    public final Tensor<N> mul_(N value) {
        return binaryOp_(TensorOp.binaryMul(), value);
    }

    public final Tensor<N> mul_(int value) {
        return binaryOp_(TensorOp.binaryMul(), value);
    }

    public final Tensor<N> mul_(double value) {
        return binaryOp_(TensorOp.binaryMul(), value);
    }

    public final Tensor<N> div(N value) {
        return binaryOp(TensorOp.binaryDiv(), value, Order.defaultOrder());
    }

    public final Tensor<N> div(int value) {
        return binaryOp(TensorOp.binaryDiv(), value, Order.defaultOrder());
    }

    public final Tensor<N> div(double value) {
        return binaryOp(TensorOp.binaryDiv(), value, Order.defaultOrder());
    }

    public final Tensor<N> div(N value, Order order) {
        return binaryOp(TensorOp.binaryDiv(), value, order);
    }

    public final Tensor<N> div(int value, Order order) {
        return binaryOp(TensorOp.binaryDiv(), value, order);
    }

    public final Tensor<N> div(double value, Order order) {
        return binaryOp(TensorOp.binaryDiv(), value, order);
    }

    public final Tensor<N> div_(N value) {
        return binaryOp_(TensorOp.binaryDiv(), value);
    }

    public final Tensor<N> div_(int value) {
        return binaryOp_(TensorOp.binaryDiv(), value);
    }

    public final Tensor<N> div_(double value) {
        return binaryOp_(TensorOp.binaryDiv(), value);
    }

    public final Tensor<N> min(N value) {
        return binaryOp(TensorOp.binaryMin(), value, Order.defaultOrder());
    }

    public final Tensor<N> min(int value) {
        return binaryOp(TensorOp.binaryMin(), value, Order.defaultOrder());
    }

    public final Tensor<N> min(double value) {
        return binaryOp(TensorOp.binaryMin(), value, Order.defaultOrder());
    }

    public final Tensor<N> min(N value, Order order) {
        return binaryOp(TensorOp.binaryMin(), value, order);
    }

    public final Tensor<N> min(int value, Order order) {
        return binaryOp(TensorOp.binaryMin(), value, order);
    }

    public final Tensor<N> min(double value, Order order) {
        return binaryOp(TensorOp.binaryMin(), value, order);
    }

    public final Tensor<N> min_(N value) {
        return binaryOp_(TensorOp.binaryMin(), value);
    }

    public final Tensor<N> min_(int value) {
        return binaryOp_(TensorOp.binaryMin(), value);
    }

    public final Tensor<N> min_(double value) {
        return binaryOp_(TensorOp.binaryMin(), value);
    }

    public final Tensor<N> max(N value) {
        return binaryOp(TensorOp.binaryMax(), value, Order.defaultOrder());
    }

    public final Tensor<N> max(int value) {
        return binaryOp(TensorOp.binaryMax(), value, Order.defaultOrder());
    }

    public final Tensor<N> max(double value) {
        return binaryOp(TensorOp.binaryMax(), value, Order.defaultOrder());
    }

    public final Tensor<N> max(N value, Order order) {
        return binaryOp(TensorOp.binaryMax(), value, order);
    }

    public final Tensor<N> max(int value, Order order) {
        return binaryOp(TensorOp.binaryMax(), value, order);
    }

    public final Tensor<N> max(double value, Order order) {
        return binaryOp(TensorOp.binaryMax(), value, order);
    }

    public final Tensor<N> max_(N value) {
        return binaryOp_(TensorOp.binaryMax(), value);
    }

    public final Tensor<N> max_(int value) {
        return binaryOp_(TensorOp.binaryMax(), value);
    }

    public final Tensor<N> max_(double value) {
        return binaryOp_(TensorOp.binaryMax(), value);
    }

    public final Tensor<N> fma(N a, Tensor<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    public final Tensor<N> fma(int a, Tensor<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    public final Tensor<N> fma(double a, Tensor<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    public final Tensor<N> fma(N a, Tensor<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    public final Tensor<N> fma(int a, Tensor<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    public final Tensor<N> fma(double a, Tensor<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    /**
     * Adds in place the given matrix {@code t} multiplied by {@code factor} to the tensor element wise.
     *
     * @param factor multiplication factor
     * @param t      tensor to be multiplied and added to the current one
     * @return same tensor with values changed
     */
    public abstract Tensor<N> fma_(N factor, Tensor<?> t);

    public final Tensor<N> fma_(int factor, Tensor<?> t) {
        return fma_(dtype().castValue(factor), t);
    }

    public final Tensor<N> fma_(double factor, Tensor<?> t) {
        return fma_(dtype().castValue(factor), t);
    }

    //--------- REDUCE OPERATIONS ----------------//

    public abstract N reduceOp(TensorReduceOp op);

    public abstract N nanAssociativeOp(TensorReduceOp op);

    public abstract Tensor<N> associativeOpNarrow(TensorReduceOp op, Order order, int axis);

    public abstract Tensor<N> nanAssociativeOpNarrow(TensorReduceOp op, Order order, int axis);

    public final N sum() {
        return reduceOp(TensorOp.reduceAdd());
    }

    public final Tensor<N> sum(int axis) {
        return sum(axis, Order.defaultOrder());
    }

    public final Tensor<N> sum(int axis, Order order) {
        return associativeOpNarrow(TensorOp.reduceAdd(), order, axis);
    }

    public final N nanSum() {
        return nanAssociativeOp(TensorOp.reduceAdd());
    }

    public final Tensor<N> nanSum(int axis) {
        return nanSum(axis, Order.defaultOrder());
    }

    public final Tensor<N> nanSum(int axis, Order order) {
        return nanAssociativeOpNarrow(TensorOp.reduceAdd(), order, axis);
    }

    public final N prod() {
        return reduceOp(TensorOp.reduceMul());
    }

    public final Tensor<N> prod(int axis) {
        return prod(axis, Order.defaultOrder());
    }

    public final Tensor<N> prod(int axis, Order order) {
        return associativeOpNarrow(TensorOp.reduceMul(), order, axis);
    }

    public final N nanProd() {
        return nanAssociativeOp(TensorOp.reduceMul());
    }

    public final Tensor<N> nanProd(int axis) {
        return nanProd(axis, Order.defaultOrder());
    }

    public final Tensor<N> nanProd(int axis, Order order) {
        return nanAssociativeOpNarrow(TensorOp.reduceMul(), order, axis);
    }

    public final int argmax() {
        return argmax(Order.defaultOrder());
    }

    public abstract int argmax(Order order);

    public final N amax() {
        return reduceOp(TensorOp.reduceMax());
    }

    public final Tensor<N> amax(int axis) {
        return amax(axis, Order.defaultOrder());
    }

    public final Tensor<N> amax(int axis, Order order) {
        return associativeOpNarrow(TensorOp.reduceMax(), order, axis);
    }

    public final N nanMax() {
        return nanAssociativeOp(TensorOp.reduceMax());
    }

    public final Tensor<N> nanMax(int axis) {
        return nanMax(axis, Order.defaultOrder());
    }

    public final Tensor<N> nanMax(int axis, Order order) {
        return nanAssociativeOpNarrow(TensorOp.reduceMax(), order, axis);
    }

    public final int argmin() {
        return argmin(Order.defaultOrder());
    }

    public abstract int argmin(Order order);

    public final N amin() {
        return reduceOp(TensorOp.reduceMin());
    }

    public final Tensor<N> amin(int axis) {
        return amin(axis, Order.defaultOrder());
    }

    public final Tensor<N> amin(int axis, Order order) {
        return associativeOpNarrow(TensorOp.reduceMin(), order, axis);
    }

    public final N nanMin() {
        return nanAssociativeOp(TensorOp.reduceMin());
    }

    public final Tensor<N> nanMin(int axis) {
        return nanMin(axis, Order.defaultOrder());
    }

    public final Tensor<N> nanMin(int axis, Order order) {
        return nanAssociativeOpNarrow(TensorOp.reduceMin(), order, axis);
    }

    public final Tensor<N> reduceSum(Shape targetShape) {
        return reduceSum(targetShape, Order.defaultOrder());
    }

    public final Tensor<N> reduceSum(Shape targetShape, Order askOrder) {
        var ewb = Broadcast.elementWise(shape(), targetShape);
        if (ewb.valid() && ewb.shape().equals(shape())) {
            // first dimensions which does not exist in target dimensions are reduced
            Tensor<N> result = this;
            while (result.rank() > targetShape.rank()) {
                result = result.sum(0, askOrder);
            }
            // the other dimensions are reduced to 1 and keep, if needed
            for (int i = 0; i < targetShape.rank(); i++) {
                if ((targetShape.dim(i) != result.dim(i))) {
                    // this should not happen
                    if (targetShape.dim(i) != 1) {
                        throw new IllegalStateException("Reducing shape has a non unit reducing dimension.");
                    }
                    result = result.sum(i, askOrder).stretch(i);
                }
            }
            return result;
        }
        throw new IllegalArgumentException("Current shape " + shape() + " cannot be reduced into target shape " + targetShape);
    }

    public final Tensor<N> reduceMean(Shape targetShape) {
        return reduceMean(targetShape, Order.defaultOrder());
    }

    public final Tensor<N> reduceMean(Shape targetShape, Order askOrder) {
        var ewb = Broadcast.elementWise(shape(), targetShape);
        if (ewb.valid() && ewb.shape().equals(shape())) {
            // first dimensions which does not exist in target dimensions are reduced
            Tensor<N> result = this;
            while (result.rank() > targetShape.rank()) {
                result = result.mean(0, askOrder);
            }
            // the other dimensions are reduced to 1 and keep, if needed
            for (int i = 0; i < targetShape.rank(); i++) {
                if ((targetShape.dim(i) != result.dim(i))) {
                    // this should not happen
                    if (targetShape.dim(i) != 1) {
                        throw new IllegalStateException("Reducing shape has a non unit reducing dimension.");
                    }
                    result = result.mean(i, askOrder).stretch(i);
                }
            }
            return result;
        }
        throw new IllegalArgumentException("Current shape " + shape() + " cannot be reduced into target shape " + targetShape);
    }

    //------- VECTOR MATRIX OPERATIONS ----------//

    /**
     * Computes the dot product between vectors. This operation is available only if the
     * two operands are vectors. Vectors have to have the same size.
     *
     * @param other the other vector
     * @return scalar result
     */
    public abstract N inner(Tensor<?> other);

    /**
     * Computes the dot product between the two vectors on an index range. This operation is
     * available only if the two operands are vectors. Vectors does not have to have the same
     * size, but their size must include the selected range.
     * <p>
     * This operation does not perform broadcast.
     *
     * @param other the other vector
     * @param start start index of the range (inclusive)
     * @param end   end index of the range (exclusive)
     * @return scalar result
     */
    public abstract N inner(Tensor<?> other, int start, int end);

    /**
     * Computes the outer product between two vectors. This operation is available only if the two
     * tensors are vectors. The result is a matrix of shape {@code (n,m)}, where {@code n} is the
     * size of the first vector and {@code m} is the size of the second vector.
     * <p>
     * This operation does not perform broadcast.
     *
     * @param other the other vector
     * @return matrix containing the outer vector
     */
    public final Tensor<N> outer(Tensor<?> other) {
        if (!isVector() || !other.isVector()) {
            throw new IllegalArgumentException("Outer product is available only for vectors.");
        }
        return stretch(1).mm(other.stretch(0));
    }

    /**
     * Performs matrix vector dot product. The first tensor must be a matrix and the second tensor must be a vector.
     * Also, the second dimension of the matrix must have the same size as the dimension of the vector.
     * <p>
     * The result is a vector of the size equal with the first dimension of the matrix.
     * <p>
     * This operation does not perform broadcast and the storage order is the default order
     *
     * @param other the second operand, which must be a vector.
     * @return a vector containing the result of the matrix vector dot product
     */
    public final Tensor<N> mv(Tensor<?> other) {
        return mv(other, Order.defaultOrder());
    }

    /**
     * Performs matrix vector dot product. The first tensor must be a matrix and the second tensor must be a vector.
     * Also, the second dimension of the matrix must have the same size as the dimension of the vector.
     * <p>
     * The result is a vector of the size equal with the first dimension of the matrix.
     * <p>
     * This operation does not perform broadcast and the storage order is specified by {@code askOrder} parameter.
     *
     * @param other the second operand, which must be a vector.
     * @return a vector containing the result of the matrix vector dot product
     */
    public abstract Tensor<N> mv(Tensor<?> other, Order askOrder);

    /**
     * Performs a batched matrix vector multiplication. Self tensor plays the role of matrix batch, the {@code other} tensor
     * is the vector batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self tensor is matrix {@code (n,m)} and other tensor is a vector shape {code (m)}, the result is a unit batch
     * of shape {@code (1,n)}.
     * <p>
     * If self is a batch matrix tensor of shape {@code (b,n,m)} and second is a vector shape {@code (m)}, the vectors is multiplied with
     * all the matrices in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a matrix tensor of shape {@code (n,m)} and the other is a batch of vectors with shape {@code (b,m)}, the matrix will
     * be multiplied with every vector in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self tensor is a batch of matrices with shape {@code (b,n,m)} and {code other} is a batch of vectors with shape {@code (b,m)},
     * each matrix from the batch will be multiplied with its corresponding vector from the batch and the result will have shape {@code (b,m)}.
     * <p>
     * All other configurations are invalid and an {@link IllegalArgumentException} exception will be thrown.
     * <p>
     * The storage order of the result is the default order.
     *
     * @param other the batch of vectors
     * @return the batch with results
     */
    public final Tensor<N> bmv(Tensor<?> other) {
        return bmv(other, Order.defaultOrder());
    }

    /**
     * Performs a batched matrix vector multiplication. Self tensor plays the role of matrix batch, the {@code other} tensor
     * is the vector batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self tensor is matrix {@code (n,m)} and other tensor is a vector shape {code (m)}, the result is a unit batch
     * of shape {@code (1,n)}.
     * <p>
     * If self is a batch matrix tensor of shape {@code (b,n,m)} and second is a vector shape {@code (m)}, the vectors is multiplied with
     * all the matrices in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a matrix tensor of shape {@code (n,m)} and the other is a batch of vectors with shape {@code (b,m)}, the matrix will
     * be multiplied with every vector in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self tensor is a batch of matrices with shape {@code (b,n,m)} and {code other} is a batch of vectors with shape {@code (b,m)},
     * each matrix from the batch will be multiplied with its corresponding vector from the batch and the result will have shape {@code (b,m)}.
     * <p>
     * All other configurations are invalid and an {@link IllegalArgumentException} exception will be thrown.
     * <p>
     * The storage order of the result is specified by {@code askedOrder} parameter.
     *
     * @param other    the batch of vectors
     * @param askOrder the asked storage order of the result
     * @return the batch with results
     */
    public abstract Tensor<N> bmv(Tensor<?> other, Order askOrder);

    /**
     * Performs the dot product between this object transposed, which must be a vector, and the other
     * tensor which must be a matrix. The size of the vector must be equal with the size of the first dimesion of the matrix.
     * <p>
     * The result is a vector with size equal with the size of the second dimension of the matrix.
     * This operation is equivalent with calling {@link #mv(Tensor)}, but with transposed matrix.
     * <p>
     * This operation does not perform broadcasting and the storage order of the result is the default order.
     *
     * @param other the other tensor which must be a matrix.
     * @return the result of the vector transpose matrix dot product
     */
    public final Tensor<N> vtm(Tensor<?> other) {
        return vtm(other, Order.defaultOrder());
    }

    /**
     * Performs the dot product between this object transposed, which must be a vector, and the other
     * tensor which must be a matrix. The size of the vector must be equal with the size of the first dimesion of the matrix.
     * <p>
     * The result is a vector with size equal with the size of the second dimension of the matrix.
     * This operation is equivalent with calling {@link #mv(Tensor)}, but with transposed matrix.
     * <p>
     * This operation does not perform broadcasting and the storage order of the result is specified by {@code askOrder} parameter.
     *
     * @param other the other tensor which must be a matrix.
     * @return the result of the vector transpose matrix dot product
     */
    public abstract Tensor<N> vtm(Tensor<?> other, Order askOrder);

    /**
     * Performs a batched vector transposed matrix multiplication. Self tensor plays the role of vector batch, the {@code other} tensor
     * is the matrix batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self is vector {@code (n)} and other tensor is a matrix {code (n,m)}, the result is a unit batch
     * of shape {@code (1,m)}.
     * <p>
     * If self is a batch vector tensor of shape {@code (b,n)} and second is a matrix shape {@code (n,m)}, the vector are multiplied with
     * all the same matrix and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a vector tensor of shape {@code (n)} and the other is a batch of matrices with shape {@code (b,n,m)}, the vector will
     * be multiplied with every matrix in the batch and the result will have shape {@code (b,m)}.
     * <p>
     * If self tensor is a batch of vectors with shape {@code (b,n)} and {code other} is a batch of matrices with shape {@code (b,n,m)},
     * each vector from the batch will be multiplied with its corresponding matrix from the batch and the result will have shape {@code (b,m)}.
     * <p>
     * All other configurations are invalid and an {@link IllegalArgumentException} exception will be thrown.
     * <p>
     * The storage order of the result is the default order.
     *
     * @param other the batch of vectors
     * @return the batch with results
     */
    public final Tensor<?> bvtm(Tensor<?> other) {
        return bvtm(other, Order.defaultOrder());
    }

    /**
     * Performs a batched vector transposed matrix multiplication. Self tensor plays the role of vector batch, the {@code other} tensor
     * is the matrix batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self is vector {@code (n)} and other tensor is a matrix {code (n,m)}, the result is a unit batch
     * of shape {@code (1,m)}.
     * <p>
     * If self is a batch vector tensor of shape {@code (b,n)} and second is a matrix shape {@code (n,m)}, the vector are multiplied with
     * all the same matrix and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a vector tensor of shape {@code (n)} and the other is a batch of matrices with shape {@code (b,n,m)}, the vector will
     * be multiplied with every matrix in the batch and the result will have shape {@code (b,m)}.
     * <p>
     * If self tensor is a batch of vectors with shape {@code (b,n)} and {code other} is a batch of matrices with shape {@code (b,n,m)},
     * each vector from the batch will be multiplied with its corresponding matrix from the batch and the result will have shape {@code (b,m)}.
     * <p>
     * All other configurations are invalid and an {@link IllegalArgumentException} exception will be thrown.
     * <p>
     * The storage order of the result is specified by {@code askedOrder} parameter.
     *
     * @param other    the batch of vectors
     * @param askOrder the asked storage order of the result
     * @return the batch with results
     */
    public abstract Tensor<?> bvtm(Tensor<?> other, Order askOrder);

    /**
     * Performs matrix multiplication between two tensors. The two tensors must both be matrices.
     * <p>
     * This operation does not perform broadcast. The matrices must have compatible dimension sizes.
     * The second dimension of the first matrix must be equal with the first dimension of the first matrix.
     * The result of {@code m x n} matrix multiplied with a {@code n x p} matrix will have shape {@code n x p}.
     * <p>
     * The storage order is the default order (specified by {@link Order#defaultOrder()}
     *
     * @param other the other matrix
     * @return result of matrix multiplication.
     */
    public final Tensor<N> mm(Tensor<?> other) {
        return mm(other, Order.defaultOrder());
    }

    /**
     * Performs matrix multiplication between two tensors. The two tensors must both be matrices.
     * <p>
     * This operation does not perform broadcast. The matrices must have compatible dimension sizes.
     * The second dimension of the first matrix must be equal with the first dimension of the first matrix.
     * The result of {@code m x n} matrix multiplied with a {@code n x p} matrix will have shape {@code n x p}.
     * <p>
     * The storage order is specified by parameter {@code askOrder}.
     *
     * @param other the other matrix
     * @return result of matrix multiplication.
     */
    public abstract Tensor<N> mm(Tensor<?> other, Order askOrder);

    /**
     * Performs batch matrix-matrix multiplication. Batch index is the first parameter, if exists.
     * If self is a tensor of shape {@code (b,n,m)} and {@code other} has shape {@code (b,m,p)}
     * the result will have shape {@code (b,n,p)}. If the batch axis is missing than it will be appended
     * from the other operator, if both batch axis are missing a batch axis of size 1 added.
     * <p>
     * If self is a matrix {@code (m,n)} and {@code other} is a matrix {@code (n,p)}, the result will
     * have shape {@code (1,n,p)}.
     * <p>
     * If self is a batch of shape {@code (b,n,m)} and {@code other} is a matrix of shape {@code (n,p)},
     * each of matrices from the batch will be multiplied with the same matrix {@code other}.
     * <p>
     * If self is a matrix of shape {@code (m,n)} and {@code other} is a batch of matrices of shape {@code (b,n,p)},
     * the first matrix will be multiplied with each of the matrices from the batch.
     * <p>
     * If self is a batch matrix of shape {@code (b,m,n} and {@code other} is a batch of shape {@code (b,n,p)},
     * each matrix from the first batch will be multiplied with its correspondent matrix from the second batch and
     * the result will have shape {@code (b,n,p)}.
     * <p>
     * All other configurations are invalid.
     * <p>
     * The storage order for the result is the default order.
     *
     * @param other batch of matrices
     * @return batch of results from matrix multiplication
     */
    public final Tensor<N> bmm(Tensor<?> other) {
        return bmm(other, Order.defaultOrder());
    }

    /**
     * Performs batch matrix-matrix multiplication. Batch index is the first parameter, if exists.
     * If self is a tensor of shape {@code (b,n,m)} and {@code other} has shape {@code (b,m,p)}
     * the result will have shape {@code (b,n,p)}. If the batch axis is missing than it will be appended
     * from the other operator, if both batch axis are missing a batch axis of size 1 added.
     * <p>
     * If self is a matrix {@code (m,n)} and {@code other} is a matrix {@code (n,p)}, the result will
     * have shape {@code (1,n,p)}.
     * <p>
     * If self is a batch of shape {@code (b,n,m)} and {@code other} is a matrix of shape {@code (n,p)},
     * each of matrices from the batch will be multiplied with the same matrix {@code other}.
     * <p>
     * If self is a matrix of shape {@code (m,n)} and {@code other} is a batch of matrices of shape {@code (b,n,p)},
     * the first matrix will be multiplied with each of the matrices from the batch.
     * <p>
     * If self is a batch matrix of shape {@code (b,m,n} and {@code other} is a batch of shape {@code (b,n,p)},
     * each matrix from the first batch will be multiplied with its correspondent matrix from the second batch and
     * the result will have shape {@code (b,n,p)}.
     * <p>
     * All other configurations are invalid.
     * <p>
     * The storage order for the result is specified by parameter {@code askOrder}.
     *
     * @param other    batch of matrices
     * @param askOrder storage order of the result
     * @return batch of results from matrix multiplication
     */
    public abstract Tensor<N> bmm(Tensor<?> other, Order askOrder);

    /**
     * Adds the current tensor with the batch matrix multiplications scaled by factors.
     * The operation can be described as: {@code out = beta * self + alpha * sum_b left_b x right_b}
     *
     * @param left
     * @param right
     * @param beta
     * @param alpha
     * @return self updated tensor
     */
    public final Tensor<N> addbmm(Tensor<?> left, Tensor<?> right, N beta, N alpha) {
        throw new NotImplementedException();
    }

    /**
     * Adds the current tensor to the batch of matrix multiplications scaled by factors to self.
     * The operation can be described as: {@code self = beta * self + alpha * sum_b left_b x right_b}
     *
     * @param left
     * @param right
     * @param beta
     * @param alpha
     * @return
     */
    public final Tensor<N> addbmm_(Tensor<?> left, Tensor<?> right, N beta, N alpha) {
        throw new NotImplementedException();
    }

    /**
     * Adds the current tensor to the batch matrix multiplications scaled by factors.
     * The operation can be described as: {@code out = beta * self + alpha * left x right}
     *
     * @param left
     * @param right
     * @param beta
     * @param alpha
     * @return self updated tensor
     */
    public final Tensor<N> addmm(Tensor<?> left, Tensor<?> right, N beta, N alpha) {
        throw new NotImplementedException();
    }

    /**
     * Adds the current tensor to the matrix multiplications scaled by factors to self.
     * The operation can be described as: {@code self = beta * self + alpha * left x right}
     *
     * @param left
     * @param right
     * @param beta
     * @param alpha
     * @return
     */
    public final Tensor<N> addmm_(Tensor<?> left, Tensor<?> right, N beta, N alpha) {
        throw new NotImplementedException();
    }

    public final Tensor<?> baddbmm(Tensor<?> left, Tensor<?> right, N beta, N alpha) {
        throw new NotImplementedException();
    }

    /**
     * Shortcut method for {@link #diag(int)} with parameter {@code 0}.
     *
     * @return matrix if input is a vector, vector if input is a matrix
     */
    public final Tensor<N> diag() {
        return diag(0);
    }

    /**
     * Handles diagonal elements. The {@code diagonal} parameter indicates the diagonal. If the value is
     * {code 0}, then the main diagonal is specified. If the {code diagonal} is a positive number, then
     * the {code diagonal}-th diagonal above the main diagonal is specified. If the {code diagonal}
     * is a negative number, then the {code diagonal-th} diagonal below the main diagonal is specified.
     * <p>
     * If the input tensor is a vector, it creates a matrix with elements on the specified diagonal.
     * The resulting matrix is a square matrix with dimension size to accommodate all the elements
     * from the vector.
     * <p>
     * If the input tensor is a matrix, then the result is a vector which contains the elements from that
     * diagonal and has the size equal with the number of elements from that diagonal.
     *
     * @param diagonal number which specifies the diagonal, 0 for main one
     * @return vector or matrix, depending on input
     */
    public abstract Tensor<N> diag(int diagonal);

    public abstract N trace();

    public final boolean isSymmetric() {
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

    public final CholeskyDecomposition<N> cholesky() {
        return cholesky(false);
    }

    public final CholeskyDecomposition<N> cholesky(boolean flag) {
        return new CholeskyDecomposition<>(this, flag);
    }

    public final LUDecomposition<N> lu() {
        return lu(LUDecomposition.Method.CROUT);
    }

    public final LUDecomposition<N> lu(LUDecomposition.Method method) {
        return new LUDecomposition<>(this, method);
    }

    public final QRDecomposition<N> qr() {
        return new QRDecomposition<>(this);
    }

    public final EigenDecomposition<N> eig() {
        return new EigenDecomposition<>(this);
    }

    public final SVDecomposition<N> svd() {
        return svd(true, true);
    }

    public final SVDecomposition<N> svd(boolean wantu, boolean wantv) {
        return new SVDecomposition<>(this, wantu, wantv);
    }

    public final N norm() {
        return norm(dtype().castValue(2));
    }

    public abstract N norm(N pow);

    public final Tensor<N> normalize(N p) {
        return copy(Order.defaultOrder()).normalize_(p);
    }

    public final Tensor<N> normalize(int p) {
        return copy(Order.defaultOrder()).normalize_(p);
    }

    public final Tensor<N> normalize(double p) {
        return copy(Order.defaultOrder()).normalize_(p);
    }

    public final Tensor<N> normalize(Order order, N p) {
        return copy(order).normalize_(p);
    }

    public final Tensor<N> normalize(Order order, int p) {
        return copy(order).normalize_(p);
    }

    public final Tensor<N> normalize(Order order, double p) {
        return copy(order).normalize_(p);
    }

    public abstract Tensor<N> normalize_(N p);

    public final Tensor<N> normalize_(int p) {
        return normalize_(dtype().castValue(p));
    }

    public final Tensor<N> normalize_(double p) {
        return normalize_(dtype().castValue(p));
    }

    public final Tensor<N> scatter(int ddof) {
        return scatter(Order.defaultOrder(), ddof);
    }

    public final Tensor<N> scatter(Order askOrder, int ddof) {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Available only for matrices.");
        }
        if (!dtype().floatingPoint()) {
            throw new OperationNotAvailableException("Available only for floating point tensors.");
        }
        return t().mm(this, askOrder).div_(dtype().castValue(dim(0) - ddof));
    }

    public final Tensor<N> cov(int ddof) {
        return cov(Order.defaultOrder(), ddof);
    }

    public final Tensor<N> cov(Order askOrder, int ddof) {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("Available only for matrices.");
        }
        if (!dtype().floatingPoint()) {
            throw new OperationNotAvailableException("Available only for floating point tensors.");
        }
        Tensor<N> mean = mean(0);
        Tensor<N> centered = sub(mean);
        return centered.t().mm(centered, askOrder).div_(dtype().castValue(dim(0) - ddof));
    }

    public final Tensor<N> corr() {
        return corr(Order.defaultOrder());
    }

    public final Tensor<N> corr(Order askOrder) {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Available only for matrices.");
        }
        if (!dtype().floatingPoint()) {
            throw new OperationNotAvailableException("Available only for floating point tensors.");
        }
        Tensor<N> std = stdc(0, 0);
        Tensor<N> scaled = sub(mean(0));
        return scaled.t().mm(scaled, askOrder).div_(std).div_(std.stretch(1)).div_(dtype().castValue(dim(0)));
    }


    //------- SUMMARY OPERATIONS ----------//

    protected abstract Tensor<N> alongAxisOperation(Order order, int axis, Function<Tensor<N>, N> op);

    public abstract N mean();

    public final Tensor<N> mean(int axis) {
        return mean(axis, Order.defaultOrder());
    }

    public final Tensor<N> mean(int axis, Order order) {
        return alongAxisOperation(order, axis, Tensor::mean);
    }

    public abstract N nanMean();

    public final N std() {
        return dtype().castValue(Math.sqrt(var().doubleValue()));
    }

    public final Tensor<N> std(int axis) {
        return std(axis, Order.defaultOrder());
    }

    public final Tensor<N> std(int axis, Order order) {
        return alongAxisOperation(order, axis, Tensor::std);
    }

    public final N stdc(int ddof) {
        return dtype().castValue(Math.sqrt(varc(ddof).doubleValue()));
    }

    public final Tensor<N> stdc(int axis, int ddof) {
        return stdc(axis, ddof, Order.defaultOrder());
    }

    public final Tensor<N> stdc(int axis, int ddof, Order order) {
        return alongAxisOperation(order, axis, t -> t.stdc(ddof));
    }

    public final N var() {
        return varc(0);
    }

    public final Tensor<N> var(int axis) {
        return var(axis, Order.defaultOrder());
    }

    public final Tensor<N> var(int axis, Order order) {
        return varc(axis, 0, order);
    }

    public abstract N varc(int ddof);

    public final Tensor<N> varc(int axis, int ddof) {
        return varc(axis, ddof, Order.defaultOrder());
    }

    public final Tensor<N> varc(int axis, int ddof, Order order) {
        return alongAxisOperation(order, axis, t -> t.varc(ddof));
    }

    public final Tensor<N> softmax(int axis) {
        return softmax(axis, Order.defaultOrder());
    }

    public final Tensor<N> softmax(int axis, Order askOrder) {
        return copy(askOrder).softmax_(axis);
    }

    public abstract Tensor<N> softmax_(int axis);


    public final Tensor<N> logsoftmax(int axis) {
        return logsoftmax(axis, Order.defaultOrder());
    }

    public final Tensor<N> logsoftmax(int axis, Order askOrder) {
        return copy(askOrder).logsoftmax_(axis);
    }

    public abstract Tensor<N> logsoftmax_(int axis);


    /**
     * Computes the number of NaN values. For integer value types this operation returns 0.
     *
     * @return number of NaN values
     */
    public abstract int nanCount();

    /**
     * Computes the number of values equal with zero.
     *
     * @return number of zero values
     */
    public abstract int zeroCount();

    @SuppressWarnings("unchecked")
    public final <M extends Number> Tensor<M> cast(DType<M> dtype) {
        if ((dtype.id() == dtype().id())) {
            return (Tensor<M>) this;
        } else {
            return cast(dtype, Order.A);
        }
    }

    public abstract <M extends Number> Tensor<M> cast(DType<M> dType, Order askOrder);

    /**
     * Creates a padded copy of a tensor along the first dimension. The padded copy will be a tensor with the same shape other than the
     * first dimension which will have size {@code before + dim(0) + after}, having first and last elements padded with 0.
     * <p>
     *
     * @return resized padded copy of the original tensor
     */
    public final Tensor<N> pad(int before, int after) {
        return pad(0, before, after);
    }

    /**
     * Creates a padded copy of a tensor along a given dimension. The padded copy will be a tensor with the same shape
     * on all axis other than the specified as parameter, the later being increased to {@code before + dim(axis) + after},
     * having first and last elements padded with 0.
     * <p>
     *
     * @return resized padded copy of the original tensor
     */
    public final Tensor<N> pad(int axis, int before, int after) {
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
    public final Tensor<N> copy() {
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
    public abstract Tensor<N> copy(Order askOrder);

    public abstract Tensor<N> copyTo(Tensor<N> dst);

    public abstract VarDouble dv();

    public final double[] toDoubleArray() {
        return toDoubleArray(Order.defaultOrder());
    }

    public abstract double[] toDoubleArray(Order askOrder);

    public final double[] asDoubleArray() {
        return asDoubleArray(Order.defaultOrder());
    }

    public abstract double[] asDoubleArray(Order askOrder);

    public final boolean deepEquals(Object t) {
        return deepEquals(t, 1e-100);
    }

    public final boolean deepEquals(Object t, double tol) {
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
