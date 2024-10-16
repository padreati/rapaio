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
import rapaio.math.tensor.operator.TensorBinaryOp;
import rapaio.math.tensor.operator.TensorOp;
import rapaio.math.tensor.operator.TensorReduceOp;
import rapaio.math.tensor.operator.TensorUnaryOp;
import rapaio.printer.Printable;
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
     * @param asc     if true, than sort ascending, descending otherwise
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

    public final void incByte(byte value, int... indexes) {
        storage.incByte(layout().pointer(indexes), value);
    }

    public final void incInt(int value, int... indexes) {
        storage.incInt(layout().pointer(indexes), value);
    }

    public final void incFloat(float value, int... indexes) {
        storage.incFloat(layout().pointer(indexes), value);
    }

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

    public final Tensor<N> fillNan_(N value) {
        return unaryOp_(TensorOp.unaryFillNan(value));
    }

    public final Tensor<N> clamp(N min, N max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), min, max));
    }

    public final Tensor<N> clamp(Order order, N min, N max) {
        return unaryOp(TensorOp.unaryClamp(dtype(), min, max), order);
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

    public final Tensor<N> negate() {
        return unaryOp(TensorOp.unaryNeg());
    }

    public final Tensor<N> negate(Order order) {
        return unaryOp(TensorOp.unaryNeg(), order);
    }

    public final Tensor<N> negate_() {
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

    //--------- BINARY OPERATIONS ----------------//

    public final <M extends Number> Tensor<N> binaryOp(TensorBinaryOp op, Tensor<M> t, Order order) {
        if (isScalar()) {
            return t.cast(dtype(), order).binaryOp_(op, get());
        }
        return copy(order).binaryOp_(op, t);
    }

    public abstract <M extends Number> Tensor<N> binaryOp_(TensorBinaryOp op, Tensor<M> value);

    public final <M extends Number> Tensor<N> binaryOp(TensorBinaryOp op, M value, Order order) {
        return copy(order).binaryOp_(op, value);
    }

    public abstract <M extends Number> Tensor<N> binaryOp_(TensorBinaryOp op, M value);

    public final <M extends Number> Tensor<N> add(Tensor<M> tensor) {
        return binaryOp(TensorOp.binaryAdd(), tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> add(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.binaryAdd(), tensor, order);
    }

    public final <M extends Number> Tensor<N> add_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.binaryAdd(), tensor);
    }

    public final <M extends Number> Tensor<N> badd(int axis, Tensor<M> tensor) {
        return badd(axis, tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> badd(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).add_(get());
        }
        return copy(order).badd_(axis, tensor);
    }

    public final <M extends Number> Tensor<N> badd_(int axis, Tensor<M> tensor) {
        return add_(tensor.strexp(axis, dim(axis)));
    }

    public final <M extends Number> Tensor<N> sub(Tensor<M> tensor) {
        return binaryOp(TensorOp.binarySub(), tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> sub(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.binarySub(), tensor, order);
    }

    public final <M extends Number> Tensor<N> sub_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.binarySub(), tensor);
    }

    public final <M extends Number> Tensor<N> bsub(int axis, Tensor<M> tensor) {
        return bsub(axis, tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> bsub(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).sub_(get());
        }
        return copy(order).bsub_(axis, tensor);
    }

    public final <M extends Number> Tensor<N> bsub_(int axis, Tensor<M> tensor) {
        return sub_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    public final <M extends Number> Tensor<N> mul(Tensor<M> tensor) {
        return binaryOp(TensorOp.binaryMul(), tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> mul(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.binaryMul(), tensor, order);
    }

    public final <M extends Number> Tensor<N> mul_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.binaryMul(), tensor);
    }

    public final <M extends Number> Tensor<N> bmul(int axis, Tensor<M> tensor) {
        return bmul(axis, tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> bmul(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).mul_(get());
        }
        return copy(order).bmul_(axis, tensor);
    }

    public final <M extends Number> Tensor<N> bmul_(int axis, Tensor<M> tensor) {
        return mul_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    public final <M extends Number> Tensor<N> div(Tensor<M> tensor) {
        return binaryOp(TensorOp.binaryDiv(), tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> div(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.binaryDiv(), tensor, order);
    }

    public final <M extends Number> Tensor<N> div_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.binaryDiv(), tensor);
    }

    public final <M extends Number> Tensor<N> bdiv(int axis, Tensor<M> tensor) {
        return bdiv(axis, tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> bdiv(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).div_(get());
        }
        return copy(order).bdiv_(axis, tensor);
    }

    public final <M extends Number> Tensor<N> bdiv_(int axis, Tensor<M> tensor) {
        return div_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    public final <M extends Number> Tensor<N> min(Tensor<M> tensor) {
        return binaryOp(TensorOp.binaryMin(), tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> min(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.binaryMin(), tensor, order);
    }

    public final <M extends Number> Tensor<N> min_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.binaryMin(), tensor);
    }

    public final <M extends Number> Tensor<N> bmin(int axis, Tensor<M> tensor) {
        return bmin(axis, tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> bmin(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).min_(get());
        }
        return copy(order).bmin_(axis, tensor);
    }

    public final <M extends Number> Tensor<N> bmin_(int axis, Tensor<M> tensor) {
        return min_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    public final <M extends Number> Tensor<N> max(Tensor<M> tensor) {
        return binaryOp(TensorOp.binaryMax(), tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> max(Tensor<M> tensor, Order order) {
        return binaryOp(TensorOp.binaryMax(), tensor, order);
    }

    public final <M extends Number> Tensor<N> max_(Tensor<M> tensor) {
        return binaryOp_(TensorOp.binaryMax(), tensor);
    }

    public final <M extends Number> Tensor<N> bmax(int axis, Tensor<M> tensor) {
        return bmax(axis, tensor, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> bmax(int axis, Tensor<M> tensor, Order order) {
        if (isScalar()) {
            return tensor.cast(dtype(), order).max_(get());
        }
        return copy(order).bmax_(axis, tensor);
    }

    public final <M extends Number> Tensor<N> bmax_(int axis, Tensor<M> tensor) {
        return max_(tensor.stretch(axis).expand(axis, dim(axis)));
    }

    public final Tensor<N> add(N value) {
        return binaryOp(TensorOp.binaryAdd(), value, Order.defaultOrder());
    }

    public final Tensor<N> add(N value, Order order) {
        return binaryOp(TensorOp.binaryAdd(), value, order);
    }

    public final Tensor<N> add_(N value) {
        return binaryOp_(TensorOp.binaryAdd(), value);
    }

    public final Tensor<N> sub(N value) {
        return binaryOp(TensorOp.binarySub(), value, Order.defaultOrder());
    }

    public final Tensor<N> sub(N value, Order order) {
        return binaryOp(TensorOp.binarySub(), value, order);
    }

    public final Tensor<N> sub_(N value) {
        return binaryOp_(TensorOp.binarySub(), value);
    }

    public final Tensor<N> mul(N value) {
        return binaryOp(TensorOp.binaryMul(), value, Order.defaultOrder());
    }

    public final Tensor<N> mul(N value, Order order) {
        return binaryOp(TensorOp.binaryMul(), value, order);
    }

    public final Tensor<N> mul_(N value) {
        return binaryOp_(TensorOp.binaryMul(), value);
    }

    public final Tensor<N> div(N value) {
        return binaryOp(TensorOp.binaryDiv(), value, Order.defaultOrder());
    }

    public final Tensor<N> div(N value, Order order) {
        return binaryOp(TensorOp.binaryDiv(), value, order);
    }

    public final Tensor<N> div_(N value) {
        return binaryOp_(TensorOp.binaryDiv(), value);
    }

    public final Tensor<N> min(N value) {
        return binaryOp(TensorOp.binaryMin(), value, Order.defaultOrder());
    }

    public final Tensor<N> min(N value, Order order) {
        return binaryOp(TensorOp.binaryMin(), value, order);
    }

    public final Tensor<N> min_(N value) {
        return binaryOp_(TensorOp.binaryMin(), value);
    }

    public final Tensor<N> max(N value) {
        return binaryOp(TensorOp.binaryMax(), value, Order.defaultOrder());
    }

    public final Tensor<N> max(N value, Order order) {
        return binaryOp(TensorOp.binaryMax(), value, order);
    }

    public final Tensor<N> max_(N value) {
        return binaryOp_(TensorOp.binaryMax(), value);
    }

    public final <M extends Number> Tensor<N> fma(N a, Tensor<M> t) {
        return fma(a, t, Order.defaultOrder());
    }

    public final <M extends Number> Tensor<N> fma(N a, Tensor<M> t, Order order) {
        return copy(order).fma_(a, t);
    }

    /**
     * Adds in place the given matrix {@code t} multiplied by {@code factor} to the tensor element wise.
     *
     * @param factor multiplication factor
     * @param t      tensor to be multiplied and added to the current one
     * @return same tensor with values changed
     */
    public abstract <M extends Number> Tensor<N> fma_(N factor, Tensor<M> t);

    //--------- REDUCE OPERATIONS ----------------//

    public abstract N reduceOp(TensorReduceOp op);

    public abstract N nanAssociativeOp(TensorReduceOp op);

    public abstract Tensor<N> associativeOpNarrow(TensorReduceOp op, Order order, int axis);

    public abstract Tensor<N> nanAssociativeOpNarrow(TensorReduceOp op, Order order, int axis);

    public final N sum() {
        return reduceOp(TensorOp.reduceAdd());
    }

    public final Tensor<N> sum(int axis) {
        return sum(Order.defaultOrder(), axis);
    }

    public final Tensor<N> sum(Order order, int axis) {
        return associativeOpNarrow(TensorOp.reduceAdd(), order, axis);
    }

    public final N nanSum() {
        return nanAssociativeOp(TensorOp.reduceAdd());
    }

    public final Tensor<N> nanSum(int axis) {
        return nanSum(Order.defaultOrder(), axis);
    }

    public final Tensor<N> nanSum(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorOp.reduceAdd(), order, axis);
    }

    public final N prod() {
        return reduceOp(TensorOp.reduceMul());
    }

    public final Tensor<N> prod(int axis) {
        return prod(Order.defaultOrder(), axis);
    }

    public final Tensor<N> prod(Order order, int axis) {
        return associativeOpNarrow(TensorOp.reduceMul(), order, axis);
    }

    public final N nanProd() {
        return nanAssociativeOp(TensorOp.reduceMul());
    }

    public final Tensor<N> nanProd(int axis) {
        return nanProd(Order.defaultOrder(), axis);
    }

    public final Tensor<N> nanProd(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorOp.reduceMul(), order, axis);
    }

    public final int argmax() {
        return argmax(Order.defaultOrder());
    }

    public abstract int argmax(Order order);

    public final N max() {
        return reduceOp(TensorOp.reduceMax());
    }

    public final Tensor<N> max(int axis) {
        return max(Order.defaultOrder(), axis);
    }

    public final Tensor<N> max(Order order, int axis) {
        return associativeOpNarrow(TensorOp.reduceMax(), order, axis);
    }

    public final N nanMax() {
        return nanAssociativeOp(TensorOp.reduceMax());
    }

    public final Tensor<N> nanMax(int axis) {
        return nanMax(Order.defaultOrder(), axis);
    }

    public final Tensor<N> nanMax(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorOp.reduceMax(), order, axis);
    }

    public final int argmin() {
        return argmin(Order.defaultOrder());
    }

    public abstract int argmin(Order order);

    public final N min() {
        return reduceOp(TensorOp.reduceMin());
    }

    public final Tensor<N> min(int axis) {
        return min(Order.defaultOrder(), axis);
    }

    public final Tensor<N> min(Order order, int axis) {
        return associativeOpNarrow(TensorOp.reduceMin(), order, axis);
    }

    public final N nanMin() {
        return nanAssociativeOp(TensorOp.reduceMin());
    }

    public final Tensor<N> nanMin(int axis) {
        return nanMin(Order.defaultOrder(), axis);
    }

    public final Tensor<N> nanMin(Order order, int axis) {
        return nanAssociativeOpNarrow(TensorOp.reduceMin(), order, axis);
    }

    //------- VECTOR MATRIX OPERATIONS ----------//

    public final Tensor<N> outer(Tensor<N> t) {
        if (!isVector() || !t.isVector()) {
            throw new IllegalArgumentException("Outer product is available only for vectors.");
        }
        return stretch(1).mm(t.stretch(0));
    }

    public abstract N vdot(Tensor<N> tensor);

    public abstract N vdot(Tensor<N> tensor, int start, int end);

    public abstract Tensor<N> mv(Tensor<N> tensor);

    public final Tensor<N> mm(Tensor<N> tensor) {
        return mm(tensor, Order.defaultOrder());
    }

    public abstract Tensor<N> mm(Tensor<N> tensor, Order askOrder);

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

    public abstract N trace();

    public abstract Tensor<N> diag();

    public final N norm() {
        return norm(dtype().castValue(2));
    }

    public abstract N norm(N p);

    public final Tensor<N> normalize(N p) {
        return copy(Order.defaultOrder()).normalize_(p);
    }

    public final Tensor<N> normalize(Order order, N p) {
        return copy(order).normalize_(p);
    }

    public abstract Tensor<N> normalize_(N p);

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
        Tensor<N> centered = bsub(0, mean);
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
        Tensor<N> scaled = bsub(0, mean(0));
        return scaled.t().mm(scaled, askOrder).bdiv_(0, std).bdiv_(1, std).div_(dtype().castValue(dim(0)));
    }


    //------- SUMMARY OPERATIONS ----------//

    protected abstract Tensor<N> alongAxisOperation(Order order, int axis, Function<Tensor<N>, N> op);

    public abstract N mean();

    public final Tensor<N> mean(int axis) {
        return mean(Order.defaultOrder(), axis);
    }

    public final Tensor<N> mean(Order order, int axis) {
        return alongAxisOperation(order, axis, Tensor::mean);
    }

    public abstract N nanMean();

    public final N std() {
        return dtype().castValue(Math.sqrt(var().doubleValue()));
    }

    public final Tensor<N> std(int axis) {
        return std(Order.defaultOrder(), axis);
    }

    public final Tensor<N> std(Order order, int axis) {
        return alongAxisOperation(order, axis, Tensor::std);
    }

    public final N stdc(int ddof) {
        return dtype().castValue(Math.sqrt(varc(ddof).doubleValue()));
    }

    public final Tensor<N> stdc(int axis, int ddof) {
        return stdc(Order.defaultOrder(), axis, ddof);
    }

    public final Tensor<N> stdc(Order order, int axis, int ddof) {
        return alongAxisOperation(order, axis, t -> t.stdc(ddof));
    }

    public final N var() {
        return varc(0);
    }

    public final Tensor<N> var(int axis) {
        return var(Order.defaultOrder(), axis);
    }

    public final Tensor<N> var(Order order, int axis) {
        return varc(order, axis, 0);
    }

    public abstract N varc(int ddof);

    public final Tensor<N> varc(int axis, int ddof) {
        return varc(Order.defaultOrder(), axis, ddof);
    }

    public final Tensor<N> varc(Order order, int axis, int ddof) {
        return alongAxisOperation(order, axis, t -> t.varc(ddof));
    }

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

    public final <M extends Number> Tensor<M> cast(DType<M> dType) {
        return cast(dType, Order.defaultOrder());
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
