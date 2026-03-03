/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import rapaio.darray.iterators.PointerIterator;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.manager.AbstractStrideDArray;
import rapaio.darray.matrix.CholeskyDecomposition;
import rapaio.darray.matrix.EigenDecomposition;
import rapaio.darray.matrix.LUDecomposition;
import rapaio.darray.matrix.QRDecomposition;
import rapaio.darray.matrix.SVDecomposition;
import rapaio.darray.operator.Broadcast;
import rapaio.darray.operator.DArrayBinaryOp;
import rapaio.darray.operator.DArrayOp;
import rapaio.darray.operator.DArrayReduceOp;
import rapaio.darray.operator.DArrayUnaryOp;
import rapaio.data.OperationNotAvailableException;
import rapaio.data.VarDouble;
import rapaio.printer.Printable;
import rapaio.util.function.IntIntBiFunction;

/**
 * A {@link DArray} is a multidimensional array which contains elements of the same type.
 * Elements are indexed and organized in zero, one or multiple dimensions.
 * <p>
 * DArrays with a low number of dimensions are known also under more specific names:
 * <ul>
 *     <li>scalar</li> a darray with zero dimensions which contains a single element
 *     <li>vector</li> a darray with one dimension
 *     <li>matrix</li> a darray with two dimensions
 * </ul>
 * <p>
 * The type of data elements from a {@link DArray} is marked as a generic data type and also described by {@link #dt()}.
 * <p>
 * A darray is created by a factory which implements {@link DArrayManager}. Each DArray provides a link towards the manager
 * which created it through {@link #dm()}.
 * <p>
 * The elements are logically organized like a hyper cube with a given number of dimensions {@link #rank()}. The size of each
 * dimension is described by a {@link Shape} object and the {@link Layout} describes how the details related
 * with how the elements' indexing.
 * <p>
 * The default implemented layout is a stride array layout provided by {@link StrideLayout}, but
 * other layouts could be implemented (for example for special matrices or for sparse formats).
 *
 * @param <N> Generic data type which can be Byte, Integer, Float or Double.
 */
public abstract sealed class DArray<N extends Number> implements Printable, Iterable<N> permits AbstractStrideDArray {

    protected final DArrayManager dm;
    protected final DType<N> dt;
    protected final Storage storage;

    protected DArray(DArrayManager dm, DType<N> dt, Storage storage) {
        this.dm = dm;
        this.dt = dt;
        this.storage = storage;
    }

    /**
     * {@link DArrayManager} which created this instance.
     */
    public final DArrayManager dm() {
        return dm;
    }

    /**
     * {@link DType} describes the data type of the elements contained by the darray and provides also related utilities like value
     * casting.
     *
     * @return darray data type
     */
    public final DType<N> dt() {
        return dt;
    }

    /**
     * DArray layout contains information about logical layout of data elements in storage memory.
     *
     * @return DArray layout
     */
    public abstract Layout layout();

    /**
     * Shape describes the number of dimensions and the size on each dimension of the multidimensional elements.
     *
     * @return DArray shape
     */
    public final Shape shape() {
        return layout().shape();
    }

    /**
     * Rank is the number of dimensions for the DArray.
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
        return shape().dims();
    }

    /**
     * Size of a given dimension
     *
     * @param axis the index of that dimension
     * @return size of the dimension for the given {@code axis}
     */
    public final int dim(int axis) {
        return shape().dim(axis);
    }

    /**
     * Returns the stride array for the underlying storage. If the layout is not a stride layout, an exception is thrown.
     *
     * @return an integer vector which contains the strides
     */
    public final int[] strides() {
        if (layout() instanceof StrideLayout l) {
            return l.strides();
        }
        throw new OperationNotAvailableException("Layout is not StrideLayout");
    }

    /**
     * Returns the stride value for the given dimension. If the layout is not a stride layout, an exception is thrown.
     *
     * @param axis the dimension for which to get the stride value
     * @return the stride value for the given axis
     */
    public final int stride(int axis) {
        if (layout() instanceof StrideLayout l) {
            return l.stride(axis);
        }
        throw new OperationNotAvailableException("Layout is not StrideLayout");
    }

    /**
     * Size of an DArray is the number of elements contained in DArray and is equal with
     * the product of dimension's sizes
     *
     * @return number of elements from DArray
     */
    public final int size() {
        return shape().size();
    }

    /**
     * Storage implementation which physically contains data.
     *
     * @return storage instance
     */
    public final Storage storage() {
        return storage;
    }

    /**
     * A scalar is a DArray with no dimensions.
     *
     * @return true if the rank is 0
     */
    public final boolean isScalar() {
        return rank() == 0;
    }

    /**
     * A vector is a darray with one dimension.
     *
     * @return true if the rank is 1
     */
    public final boolean isVector() {
        return rank() == 1;
    }

    /**
     * A matrix is a darray with two dimensions.
     *
     * @return true if the rank is 2
     */
    public final boolean isMatrix() {
        return rank() == 2;
    }

    /**
     * Creates a new darray with a different shape. If possible, the data will not be copied.
     * If data is copied, the result will be a dense darray with default order.
     * <p>
     * In order to reshape a darray, the source shape and destination shape must have the same size.
     * <p>
     * The order in which elements are read is {@code C} if data is stored in C order, {@code F} if data is stored
     * in F order, and default for the other cases.
     *
     * @param shape destination shape
     * @return new instance, wrapping, if possible, the data from the old darray.
     * @see DArray#reshape(Shape, Order)
     */
    public final DArray<N> reshape(Shape shape) {
        return reshape(shape, Order.A);
    }

    /**
     * Creates a new darray with a different shape. If possible, the data will not be copied.
     * In order to reshape a darray, the source shape and destination shape must have the same size.
     * <p>
     * The indexes are interpreted according to order parameter:
     * <ul>
     *     <li>Order.C</li> indexes are read in C order, last dimension is the fastest dimension
     *     <li>Order.F</li> first dimension is the fastest dimension
     *     <li>Order.A</li> if data is stored in C format, then follows C order, if data is stored in F format it follows F order, otherwise
     *     it is the default order {@link Order#defaultOrder()}.
     *     <li>Order.S</li> storage order is not allowed
     * </ul>
     * <p>
     * Notice that the asked order is not the order in which data is stored, but in which data is interpreted for reshape.
     * If a new copy is created, that will also be the order in which new DArray copy will store data
     *
     * @param shape    destination shape
     * @param askOrder destination order, if the data will be copied, otherwise the parameter is ignored.
     * @return new DArray instance, wrapping, if possible, the data from the old DArray.
     */
    public abstract DArray<N> reshape(Shape shape, Order askOrder);

    /**
     * Creates a new transposed darray. Data will be copied and stored with default order.
     *
     * @return copy of the transposed vector
     */
    public final DArray<N> t() {
        return t(Order.defaultOrder());
    }

    /**
     * Creates a new transposed darray. Data will be stored in the specified order given as parameter.
     * <p>
     * The only accepted orders are {@link Order#C} and {@link Order#F}.
     *
     * @param askOrder storage order
     * @return copy of the transposed vector
     */
    public final DArray<N> t(Order askOrder) {
        return t_().copy(askOrder);
    }

    /**
     * Transpose of a darray. A transposed darray is a darray with reverted axis, the first axis becomes the last,
     * the second axis becomes the second to last and so on.
     * <p>
     * Data storage remain the same, no new storage copy is created.
     * As such, any modification on a transposed darray will affect the original darray.
     *
     * @return a transposed view of the DArray
     */
    public abstract DArray<N> t_();

    /**
     * Collapses the darray into one dimension using the default order. The order is used for reading. In the case when a view
     * can't be created, a new darray will be created with the storage order same as reading order.
     *
     * @return an DArray with elements in given order (new copy if needed)
     */
    public final DArray<N> ravel() {
        return ravel(Order.defaultOrder());
    }

    /**
     * Collapses the darray into one dimension using the given order. The order is used for reading. In the case when a view
     * can't be created, a new darray will be created with the storage order same as reading order.
     *
     * @param askOrder order of the elements
     * @return a darray with elements in given order (new copy if needed)
     */
    public abstract DArray<N> ravel(Order askOrder);

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is the default order.
     *
     * @return a copy of the darray with elements in asked order.
     */
    public final DArray<N> flatten() {
        return flatten(Order.defaultOrder());
    }

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is given as parameter.
     *
     * @param askOrder order of the elements
     * @return a copy of the darray with elements in asked order.
     */
    public abstract DArray<N> flatten(Order askOrder);

    /**
     * Collapses the given axes if are of dimension one. This operation does not create a new copy of the data.
     * If any dimension doesn't have size one, the dimension will remain as it is.
     *
     * @return view of the same darray with the given dimensions equal with one collapsed
     */
    public abstract DArray<N> squeeze(int... axes);

    /**
     * Creates a new darray view with additional dimensions at the position specified by {@param axes}.
     * The new dimensions will have size {@code 1}.
     * Specified axes value should be between 0 (inclusive) and the number of dimensions plus the number of added axes (exclusive).
     *
     * @param axes indexes of the axes to be added
     * @return new view darray with added axes
     */
    public abstract DArray<N> stretch(int... axes);

    /**
     * Creates a new darray by repeating values along a given dimension of size 1. This operation is
     * similar with repeating values, with the difference that the resulting darray will be a view over the same data,
     * thus avoiding copying data. This is possible if the corresponding stride is set to 0 and the corresponding original
     * dimension has size 1.
     *
     * @param axis specified dimension
     * @param dim  new size of the dimension, which is equivalent with how many times the values are repeated
     * @return new view over the original DArray with repeated data along a given dimension
     */
    public abstract DArray<N> expand(int axis, int dim);

    /**
     * Combined method of a chain call for {@link #stretch(int...)} and {@link #expand(int, int)} for a single axis.
     * It creates a new dimension with repeated data along the new dimension.
     *
     * @param axis the index of the new dimension, if there is already a dimension on that position, that dimensions and all dimension
     *             to the left are shifted one position
     * @param dim  the size of the new dimension
     * @return new view with repeated data along a new dimension
     */
    public final DArray<N> strexp(int axis, int dim) {
        return stretch(axis).expand(axis, dim);
    }

    /**
     * Creates a darray view with dimensions permuted in the order specified in parameter. The
     * parameter is an integer array containing all values from closed interval {@code [0,(rank-1)]}.
     * The order in which those values are passed defined the dimension permutation.
     *
     * @param dims dimension permutation
     * @return new DArray view with permuted dimensions
     */
    public abstract DArray<N> permute(int... dims);

    /**
     * Creates a new darray view with source axis moved into the given destination position.
     * The axes between {@code src} and {@code dst} are shifted one position.
     *
     * @param src source axis
     * @param dst destination axis position
     * @return new darray view with moved axis
     */
    public abstract DArray<N> moveAxis(int src, int dst);

    /**
     * Swap two axis. All other exes remains the same. This operation creates a darray view.
     *
     * @param src source axis
     * @param dst destination axis
     * @return new view DArray with swapped axis
     */
    public abstract DArray<N> swapAxis(int src, int dst);

    /**
     * Creates a new darray view with one truncated dimension, all other dimensions remain the same.
     * The truncated dimension will remain, even if it is unitary.
     *
     * @param axis  axis to be truncated
     * @param start start index inclusive
     * @param end   end index exclusive
     * @return new darray view with truncated axis
     */
    public final DArray<N> narrow(int axis, int start, int end) {
        return narrow(axis, true, start, end);
    }

    /**
     * Creates a new darray view with one truncated axis, all other axes remain the same.
     * The truncated dimension will be removed if {@code keepDim} is false, and it has unit 1 after truncation.
     *
     * @param axis    axis to be truncated
     * @param keepDim keep dimension or not
     * @param start   start index inclusive
     * @param end     end index exclusive
     * @return new view DArray with truncated axis
     */
    public abstract DArray<N> narrow(int axis, boolean keepDim, int start, int end);

    /**
     * Creates a new darray view with possibly all truncated axes.
     *
     * @param keepDim keep dimensions even if some of have length 1, false otherwise
     * @param starts  vector of indexes where narrow interval starts
     * @param ends    vector of indexes where narrow interval ends
     * @return a view with truncated axes
     */
    public abstract DArray<N> narrowAll(boolean keepDim, int[] starts, int[] ends);

    /**
     * Splits the darray into multiple darray views along a given axis. The resulting darrays are narrowed versions of the original ones,
     * with the start index being the current index, and the end being the next index or the end of the dimension.
     *
     * @param axis    axis to split along
     * @param indexes indexes to split along, being start indexes for truncation
     * @return list of new DArrays with truncated data.
     */
    public abstract List<DArray<N>> split(int axis, boolean keepDim, int... indexes);

    /**
     * Splits the darray into multiple darray views along all axes. The resulting darrays are narrowed versions of the original ones,
     * having for each dimension the start index being the current index in that dimension, and the end index being the next index in
     * that dimension. The indices are given as an array of arrays with length equal with number of axes, and for each sub array the
     * split indexes specified.
     *
     * @param keepDim if is true, then the original dimensions will be removed if they have size 1 after truncation, keep them otherwise
     * @param indexes array of arrays of indices
     * @return list of new darrays with truncated axes
     */
    public abstract List<DArray<N>> splitAll(boolean keepDim, int[][] indexes);

    /**
     * Slices the darray along a given axis. The resulting darrays are narrowed versions of the original one with size given by step.
     * <p>
     * The last darray in list might have smaller dimension size if step does not divide dimension size exactly.
     * <p>
     * It also may return fewer chunks if the number of requested chunks is greater than the dimension of the axis.
     * The resulting darrays are views over the original one.
     *
     * @param axis axis to slice along
     * @param step step size
     * @return list of new DArrays with truncated data.
     */
    public final List<DArray<N>> chunk(int axis, boolean keepDim, int step) {
        int dim = layout().shape().dim(axis);
        int[] indexes = new int[Math.ceilDiv(dim, step)];
        indexes[0] = 0;
        for (int i = 1; i < indexes.length; i++) {
            indexes[i] = Math.min(indexes[i - 1] + step, dim);
        }
        return split(axis, keepDim, indexes);
    }

    public final List<DArray<N>> unbind(int axis, boolean keepDim) {
        return chunk(axis, keepDim, 1);
    }

    /**
     * Slices the darray along all dimensions.
     * The resulting darrays are truncated versions of the original with sizes in each dimensions given by steps.
     * The last darray might have dimensions lesser than the other if the original dimension does not divide exactly at step.
     * The resulting darrays are views over the original one.
     *
     * @param keepDim keep the original dimensions even if those have dimensions of size 1, remove them otherwise
     * @param steps   array of steps, one step for each dimension
     * @return list of darrays with truncated data
     */
    public final List<DArray<N>> chunkAll(boolean keepDim, int[] steps) {
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
     * Creates a new darray by stacking or concatenating this darray multiple times along a given axis.
     * <p>
     * The resulting darray will be stored in default order.
     *
     * @param axis   the axis which will be repeated
     * @param repeat the number of repetitions
     * @param stack  stack DArrays if true, concatenate if false
     * @return darray with repeated values along given axis
     */
    public final DArray<N> repeat(int axis, int repeat, boolean stack) {
        return repeat(Order.defaultOrder(), axis, repeat, stack);
    }

    /**
     * Creates a new darray by stacking or concatenating this darray multiple times along a given axis.
     * <p>
     * The resulting darray will be stored in the specified order.
     *
     * @param axis   the axis which will be repeated
     * @param repeat the number of repetitions
     * @param stack  stack DArrays if true, concatenate if false
     * @return darray with repeated values along given axis
     */
    public final DArray<N> repeat(Order order, int axis, int repeat, boolean stack) {
        List<DArray<N>> copies = new ArrayList<>(repeat);
        for (int i = 0; i < repeat; i++) {
            copies.add(this);
        }
        if (stack) {
            return dm.stack(dt, order, axis, copies);
        } else {
            return dm.cat(dt, order, axis, copies);
        }
    }

    /**
     * Take values along a given axis from specified indices. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting darray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the default order.
     *
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return DArray with mapped values along the given dimension
     */
    public final DArray<N> sel(int axis, int... indices) {
        return sel(Order.defaultOrder(), axis, indices);
    }

    /**
     * Take values along a given axis from specified indices. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting DArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the specified order.
     *
     * @param order   storage order if new data copy is required, ignored otherwise
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return DArray with mapped values along the given dimension
     */
    public abstract DArray<N> sel(Order order, int axis, int... indices);

    /**
     * Takes values along a given axis from the specified indices and squeeze the given axis if a single index is requested. For example,
     * one can take a single row from a matrix DArray and the resulting DArray will have a single dimension, aka the resulting
     * DArray will be a vector. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting DArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the default order.
     *
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return DArray with mapped values along the given dimension
     */
    public final DArray<N> selsq(int axis, int... indices) {
        return selsq(Order.defaultOrder(), axis, indices);
    }

    /**
     * Takes values along a given axis from the specified indices and squeeze the given axis if a single index is requested. For example,
     * one can take a single row from a matrix DArray and the resulting DArray will have a single dimension, aka the resulting
     * darray will be a vector. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting DArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the order specified by parameter.
     *
     * @param order   order specified for the new DArray, if a copy of the data is required
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return DArray with mapped values along the given dimension
     */
    public final DArray<N> selsq(Order order, int axis, int... indices) {
        return sel(order, axis, indices).squeeze(axis);
    }

    /**
     * Removes values along a given dimension from the specified indices. This operation is similar with {@link #sel(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the default order.
     *
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return DArray with removes values along the given dimension
     */
    public final DArray<N> rem(int axis, int... indices) {
        return rem(Order.defaultOrder(), axis, indices);
    }

    /**
     * Removes values along a given dimension from the specified indices. This operation is similar with {@link #sel(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the parameter specified order.
     *
     * @param order   order of the copied data, if a copy is needed
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return DArray with removed values along the given dimension
     */
    public final DArray<N> rem(Order order, int axis, int... indices) {
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
        return sel(order, axis, toKeep.stream().mapToInt(i -> i).toArray());
    }

    /**
     * Removes values along a given dimension from the specified indices and squeeze the axis dimension if a single index is remaining.
     * This operation is similar with {@link #sel(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the default order.
     *
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return squeezed DArray with removed values along the given dimension
     */
    public final DArray<N> remsq(int axis, int... indices) {
        return remsq(Order.defaultOrder(), axis, indices);
    }

    /**
     * Removes values along a given dimension from the specified indices and squeeze the axis dimension if a single index is remaining.
     * This operation is similar with {@link #sel(int, int...)},
     * with the takes indices being the ones not specified in the remove indices.
     * <p>
     * This operation will return a view if possible, otherwise a copy of the data. If a copy is needed, the order of the copy data
     * is the parameter specified order.
     *
     * @param order   order of the copied data, if a copy is needed
     * @param axis    axis along to remove values
     * @param indices indices of the values from the specified axis to be removes.
     * @return squeezed DArray with removed values along the given dimension
     */
    public final DArray<N> remsq(Order order, int axis, int... indices) {
        return rem(order, axis, indices).squeeze(axis);
    }

    /**
     * Gather values from this darray into a new one along an axis specified by {@code axis} at indexes found in {@code index}.
     * The index must have the same rank as this darray.
     * <p>
     * The result is a new darray with the same shape as the {@code index}, with the same type as this index and with default order.
     *
     * @param axis  axis along which to index
     * @param index the indices of the elements to gather
     * @return new darray with gathered values
     */
    public final DArray<N> gather(int axis, DArray<?> index) {
        return gather(axis, index, Order.defaultOrder());
    }

    /**
     * Gather values from this darray into a new one along an axis specified by {@code axis} at indexes found in {@code index}.
     * The index must have the same rank as this darray.
     * <p>
     * The result is a new darray with the same shape as the {@code index}, with the same type as this index and with specified order.
     *
     * @param axis  axis along which to index
     * @param index the indices of the elements to gather
     * @return new darray with gathered values
     */
    public final DArray<N> gather(int axis, DArray<?> index, Order askOrder) {
        return dm.zeros(dt, index.shape(), askOrder).gather_(axis, index, this);
    }

    /**
     * Gather values into this darray from {@code input} along an axis specified by {@code axis} at indexes found in {@code index}.
     * The index must have the same rank as the {@code input}, and the same shape as this darray.
     *
     * @param axis  axis along which to index
     * @param index the indices of the elements to gather
     * @return new darray with gathered values
     */
    public abstract DArray<N> gather_(int axis, DArray<?> index, DArray<?> input);

    /**
     * Writes values from this darry along an axis specified by {@code axis} at indexes found in {@code index}.
     * The values are written in {@code out}, which has to have the same shape as {@code index}. The {@code index} must have the
     * same rank as this darray.
     *
     * @param axis  axis along which to index
     * @param index the indices of the elements to scatter
     * @param out   the darray where the values will be written
     * @return the destination array
     */
    public final DArray<?> scatter(int axis, DArray<?> index, DArray<?> out) {
        return out.scatter_(axis, index, this);
    }

    /**
     * Writes values from {@code input} into this darray along an axis specified by {@code axis} at indices found in {@code index}.
     * This array, which is the destination, must have the same shape as {@code index}. The input must have the same rank as {@code index}.
     *
     * @param axis  axis along which to index
     * @param index the indices of the elements to scatter
     * @param input input darray from which the values are taken
     * @return this darray which is also the destination
     */
    public abstract DArray<?> scatter_(int axis, DArray<?> index, DArray<?> input);

    /**
     * Get value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param indices indexed position
     * @return value at indexed position
     */
    public abstract N get(int... indices);

    /**
     * Gets the value at the indexed position as a byte value.
     *
     * @param indices indexed position
     * @return value at indexed position
     */
    public final byte getByte(int... indices) {
        return storage.getByte(layout().pointer(indices));
    }

    /**
     * Gets the value at the indexed position as an int value.
     *
     * @param indices indexed position
     * @return value at indexed position
     */
    public final int getInt(int... indices) {
        return storage.getInt(layout().pointer(indices));
    }

    /**
     * Gets the value at the indexed position as a float value.
     *
     * @param indices indexed position
     * @return value at indexed position
     */
    public final float getFloat(int... indices) {
        return storage.getFloat(layout().pointer(indices));
    }

    /**
     * Gets the value at the indexed position as a double value.
     *
     * @param indices indexed position
     * @return value at indexed position
     */
    public final double getDouble(int... indices) {
        return storage.getDouble(layout().pointer(indices));
    }

    /**
     * Sets value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public abstract void set(N value, int... indices);

    /**
     * Sets byte value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void setByte(byte value, int... indices) {
        storage.setByte(layout().pointer(indices), value);
    }

    /**
     * Sets int value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void setInt(int value, int... indices) {
        storage.setInt(layout().pointer(indices), value);
    }

    /**
     * Sets float value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void setFloat(float value, int... indices) {
        storage.setFloat(layout().pointer(indices), value);
    }

    /**
     * Sets double value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void setDouble(double value, int... indices) {
        storage.setDouble(layout().pointer(indices), value);
    }

    /**
     * Increments the value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public abstract void inc(N value, int... indices);

    /**
     * Increments the byte value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void incByte(byte value, int... indices) {
        storage.incByte(layout().pointer(indices), value);
    }

    /**
     * Increments the int value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void incInt(int value, int... indices) {
        storage.incInt(layout().pointer(indices), value);
    }

    /**
     * Increments the float value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void incFloat(float value, int... indices) {
        storage.incFloat(layout().pointer(indices), value);
    }

    /**
     * Increments the double value at indexed position. An indexed position is an int array of length equals with {@code rank},
     * with a value for each dimension.
     *
     * @param value   value to be set
     * @param indices indexed position
     */
    public final void incDouble(double value, int... indices) {
        storage.incDouble(layout().pointer(indices), value);
    }

    /**
     * Gets value at pointer. A pointer is an offset position in the storage.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    public abstract N ptrGet(int ptr);

    /**
     * Gets byte value at pointer. A pointer is an offset position in the storage.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    public final byte ptrGetByte(int ptr) {
        return storage.getByte(ptr);
    }

    /**
     * Gets int value at pointer. A pointer is an offset position in the storage.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    public final int ptrGetInt(int ptr) {
        return storage.getInt(ptr);
    }

    /**
     * Gets float value at pointer. A pointer is an offset position in the storage.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    public final float ptrGetFloat(int ptr) {
        return storage.getFloat(ptr);
    }

    /**
     * Gets double value at pointer. A pointer is an offset position in the storage.
     *
     * @param ptr data pointer
     * @return element at data pointer
     */
    public final double ptrGetDouble(int ptr) {
        return storage.getDouble(ptr);
    }

    /**
     * Sets value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    public abstract void ptrSet(int ptr, N value);

    /**
     * Sets byte value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    public final void ptrSetByte(int ptr, byte value) {
        storage.setByte(ptr, value);
    }

    /**
     * Sets int value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    public final void ptrSetInt(int ptr, int value) {
        storage.setInt(ptr, value);
    }

    /**
     * Sets float value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    public final void ptrSetFloat(int ptr, float value) {
        storage.setFloat(ptr, value);
    }

    /**
     * Sets double value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be set at data pointer
     */
    public final void ptrSetDouble(int ptr, double value) {
        storage.setDouble(ptr, value);
    }

    /**
     * Increments the byte value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be added to the value at data pointer
     */
    public final void ptrIncByte(int ptr, byte value) {
        storage.incByte(ptr, value);
    }

    /**
     * Increments the int value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be added to the value at data pointer
     */
    public final void ptrIncInt(int ptr, int value) {
        storage.incInt(ptr, value);
    }

    /**
     * Increments the float value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be added to the value at data pointer
     */
    public final void ptrIncFloat(int ptr, float value) {
        storage.incFloat(ptr, value);
    }

    /**
     * Increments the double value at given pointer. A pointer is an offset position in storage.
     *
     * @param ptr   data pointer
     * @param value element value to be added to the value at data pointer
     */
    public final void ptrIncDouble(int ptr, double value) {
        storage.incDouble(ptr, value);
    }

    /**
     * Produces an iterator over the values from this darray in the storage order. The storage order is a computed order in which
     * the computed offsets are increasing, allowing better performance.
     * The iterator is immutable.
     *
     * @return value iterator
     */
    @Override
    public final Iterator<N> iterator() {
        return iterator(Order.S);
    }

    /**
     * Produces an iterator over the values from this darray in the order specified by {@code askOrder}.
     * The iterator is immutable.
     *
     * @return value iterator
     */
    public abstract Iterator<N> iterator(Order askOrder);

    /**
     * Produces a stream with values from this darray in the storage order. The storage order is a computed order in which
     * the computed offsets are increasing, allowing better performance.
     *
     * @return value stream
     */
    public final Stream<N> stream() {
        return stream(Order.defaultOrder());
    }

    /**
     * Produces a stream with values from this darray in the order specified by {@code askOrder}.
     *
     * @return value stream
     */
    public final Stream<N> stream(Order askOrder) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(askOrder), Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
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
     * Creates a new darray in the default storage order, having as values the result of
     * a function which receives as parameters two integers: order index and storage pointer value.
     * <p>
     * The order index is a zero integer increasing value determined by the order in which
     * elements are parsed. The storage pointer describes where the value will be stored in
     * storage layer.
     *
     * @param fun function which produces values
     * @return value to be stored
     */
    public final DArray<N> apply(IntIntBiFunction<N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    /**
     * Creates a new darray in the order determined by parameter, having as values the result of
     * a function which receives as parameters two integers: order index and storage pointer value.
     * <p>
     * The order index is a zero integer increasing value determined by the order in which
     * elements are parsed. The storage pointer describes where the value will be stored in
     * storage layer.
     *
     * @param fun function which produces values
     * @return value to be stored
     */
    public final DArray<N> apply(Order askOrder, IntIntBiFunction<N> fun) {
        return copy(askOrder).apply_(askOrder, fun);
    }

    /**
     * Changes values from DArray in the default order, having as values the result of
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
    public final DArray<N> apply_(IntIntBiFunction<N> fun) {
        return apply_(Order.defaultOrder(), fun);
    }

    /**
     * Changes values from this darray in the order specified by parameter, having as values the result of
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
    public abstract DArray<N> apply_(Order askOrder, IntIntBiFunction<N> fun);

    /**
     * Creates a new darray with values transformed by function {@code fun} with default order.
     *
     * @param fun function which transforms values
     * @return new darray with transformed values
     */
    public final DArray<N> apply(Function<N, N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    /**
     * Creates a new darray with values transformed by function {@code fun} with specified order.
     *
     * @param askOrder asked order
     * @param fun      transform function
     * @return new array with transformed values
     */
    public final DArray<N> apply(Order askOrder, Function<N, N> fun) {
        return copy(askOrder).apply_(fun);
    }

    /**
     * Transforms in-place the tensor's values with function {@code fun}
     *
     * @param fun transform function
     * @return same tensor instance
     */
    public abstract DArray<N> apply_(Function<N, N> fun);

    //------------------
    // UNARY OPERATIONS
    //------------------

    /**
     * Applies unary operation to this darray and returns a new darray with result stored in the default ordering.
     *
     * @param op unary operator
     * @return new darray with operation applied
     */
    public final DArray<N> unary(DArrayUnaryOp op) {
        return copy(Order.defaultOrder()).unary_(op);
    }

    /**
     * Applies unary operation to this darray and returns a new darray with result stored in the specified ordering.
     *
     * @param op    unary operator
     * @param order storage order
     * @return new darray with operation applied
     */
    public final DArray<N> unary(DArrayUnaryOp op, Order order) {
        return copy(order).unary_(op);
    }

    /**
     * Applies unary operation to this darray in place
     *
     * @param op unary operator
     * @return this darray instance
     */
    public abstract DArray<N> unary_(DArrayUnaryOp op);

    /**
     * Applies unary operation to 1d slices of darray, along the specified axis.
     * Most unary operators has no effect for operations on 1d slices, except for softmax, logsoftmax
     * and perhaps other. In general if a unary operator operates on 1d slices, it should be applied,
     * for the other operators like abs or sign, which operate element wise, the result is the same,
     * but the computation is less efficient.
     * <p>
     * The storage order is the default order.
     *
     * @param op   unary operator
     * @param axis axis along which the operation is applied
     * @return new darray with operation applied on 1d slices
     */
    public final DArray<N> unary1d(DArrayUnaryOp op, int axis) {
        return copy(Order.defaultOrder()).unary1d_(op, axis);
    }

    /**
     * Applies unary operation to 1d slices of darray, along the specified axis.
     * Most unary operators has no effect for operations on 1d slices, except for softmax, logsoftmax
     * and perhaps other. In general if a unary operator operates on 1d slices, it should be applied,
     * for the other operators like abs or sign, which operate element wise, the result is the same,
     * but the computation is less efficient.
     * <p>
     * The storage order is the specified one.
     *
     * @param op    unary operator
     * @param axis  axis along which the operator is applied
     * @param order storage order
     * @return new darray with operation applied on 1d slices
     */
    public final DArray<N> unary1d(DArrayUnaryOp op, int axis, Order order) {
        return copy(order).unary1d_(op, axis);
    }

    /**
     *
     * Applies in place the unary operation to 1d slices of darray, along the specified axis.
     * Most unary operators has no effect for operations on 1d slices, except for softmax, logsoftmax
     * and perhaps other. In general if a unary operator operates on 1d slices, it should be applied,
     * for the other operators like abs or sign, which operate element wise, the result is the same,
     * but the computation is less efficient.
     *
     * @param op   unary operator
     * @param axis axis along which the operator is applied
     * @return this darray instance
     */
    public abstract DArray<N> unary1d_(DArrayUnaryOp op, int axis);

    /**
     * Fills all elements with a given value in place.
     *
     * @param value value used to fill
     * @return this darray instance
     */
    public final DArray<N> fill_(N value) {
        return unary_(DArrayOp.unaryFill(value));
    }

    /**
     * Fills all elements with a given value in place.
     *
     * @param value value used to fill
     * @return this darray instance
     */
    public final DArray<N> fill_(int value) {
        return unary_(DArrayOp.unaryFill(value));
    }

    /**
     * Fills all elements with a given value in place.
     *
     * @param value value used to fill
     * @return this darray instance
     */
    public final DArray<N> fill_(double value) {
        return unary_(DArrayOp.unaryFill(value));
    }

    /**
     * Replaces NaN values with the specified value in place.
     *
     * @param value replacement value
     * @return this darray instance
     */
    public final DArray<N> fillNan_(N value) {
        return unary_(DArrayOp.unaryFillNan(value));
    }

    /**
     * Replaces NaN values with the specified value in place.
     *
     * @param value replacement value
     * @return this darray instance
     */
    public final DArray<N> fillNan_(int value) {
        return unary_(DArrayOp.unaryFillNan(value));
    }

    /**
     * Replaces NaN values with the specified value in place.
     *
     * @param value replacement value
     * @return this darray instance
     */
    public final DArray<N> fillNan_(double value) {
        return unary_(DArrayOp.unaryFillNan(value));
    }

    /**
     * Replaces NaN, negative infinity, and positive infinity with the specified value in place.
     *
     * @param fill replacement value for all special values
     * @return this darray instance
     */
    public final DArray<N> nanToNum_(N fill) {
        return unary_(DArrayOp.unaryNanToNum(fill, fill, fill));
    }

    /**
     * Replaces NaN, negative infinity, and positive infinity with the specified value in place.
     *
     * @param fill replacement value for all special values
     * @return this darray instance
     */
    public final DArray<N> nanToNum_(int fill) {
        return unary_(DArrayOp.unaryNanToNum(fill, fill, fill));
    }

    /**
     * Replaces NaN, negative infinity, and positive infinity with the specified value in place.
     *
     * @param fill replacement value for all special values
     * @return this darray instance
     */
    public final DArray<N> nanToNum_(double fill) {
        return unary_(DArrayOp.unaryNanToNum(fill, fill, fill));
    }

    /**
     * Replaces NaN, negative infinity, and positive infinity with specified values in place.
     *
     * @param nan    replacement value for NaN
     * @param negInf replacement value for negative infinity
     * @param posInf replacement value for positive infinity
     * @return this darray instance
     */
    public final DArray<N> nanToNum_(N nan, N negInf, N posInf) {
        return unary_(DArrayOp.unaryNanToNum(nan, negInf, posInf));
    }

    /**
     * Replaces NaN, negative infinity, and positive infinity with specified values in place.
     *
     * @param nan    replacement value for NaN
     * @param negInf replacement value for negative infinity
     * @param posInf replacement value for positive infinity
     * @return this darray instance
     */
    public final DArray<N> nanToNum_(int nan, int negInf, int posInf) {
        return unary_(DArrayOp.unaryNanToNum(nan, negInf, posInf));
    }

    /**
     * Replaces NaN, negative infinity, and positive infinity with specified values in place.
     *
     * @param nan    replacement value for NaN
     * @param negInf replacement value for negative infinity
     * @param posInf replacement value for positive infinity
     * @return this darray instance
     */
    public final DArray<N> nanToNum_(double nan, double negInf, double posInf) {
        return unary_(DArrayOp.unaryNanToNum(nan, negInf, posInf));
    }

    /**
     * Creates a comparison mask in place where elements matching the comparison are set to 1, others to 0.
     *
     * @param cmp   comparison operator
     * @param value comparison value
     * @return this darray instance
     */
    public final DArray<N> compareMask_(Compare cmp, N value) {
        return unary_(DArrayOp.unaryOpCompareMask(cmp, value));
    }

    /**
     * Creates a comparison mask in place where elements matching the comparison are set to 1, others to 0.
     *
     * @param cmp   comparison operator
     * @param value comparison value
     * @return this darray instance
     */
    public final DArray<N> compareMask_(Compare cmp, int value) {
        return unary_(DArrayOp.unaryOpCompareMask(cmp, value));
    }

    /**
     * Creates a comparison mask in place where elements matching the comparison are set to 1, others to 0.
     *
     * @param cmp   comparison operator
     * @param value comparison value
     * @return this darray instance
     */
    public final DArray<N> compareMask_(Compare cmp, double value) {
        return unary_(DArrayOp.unaryOpCompareMask(cmp, value));
    }

    /**
     * Clamps values between min and max with default order.
     *
     * @param min minimum value
     * @param max maximum value
     * @return new darray with clamped values
     */
    public final DArray<N> clamp(N min, N max) {
        return unary(DArrayOp.unaryClamp(dt(), min, max));
    }

    /**
     * Clamps values between min and max with default order.
     *
     * @param min minimum value
     * @param max maximum value
     * @return new darray with clamped values
     */
    public final DArray<N> clamp(int min, int max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)));
    }

    /**
     * Clamps values between min and max with default order.
     *
     * @param min minimum value
     * @param max maximum value
     * @return new darray with clamped values
     */
    public final DArray<N> clamp(double min, double max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)));
    }

    /**
     * Clamps values between min and max with specified order.
     *
     * @param order storage order
     * @param min   minimum value
     * @param max   maximum value
     * @return new darray with clamped values
     */
    public final DArray<N> clamp(Order order, N min, N max) {
        return unary(DArrayOp.unaryClamp(dt(), min, max), order);
    }

    /**
     * Clamps values between min and max with specified order.
     *
     * @param order storage order
     * @param min   minimum value
     * @param max   maximum value
     * @return new darray with clamped values
     */
    public final DArray<N> clamp(Order order, int min, int max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)), order);
    }

    /**
     * Clamps values between min and max with specified order.
     *
     * @param order storage order
     * @param min   minimum value
     * @param max   maximum value
     * @return new darray with clamped values
     */
    public final DArray<N> clamp(Order order, double min, double max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)), order);
    }

    /**
     * Clamps values between min and max in place.
     *
     * @param min minimum value
     * @param max maximum value
     * @return this darray instance
     */
    public final DArray<N> clamp_(N min, N max) {
        return unary_(DArrayOp.unaryClamp(dt(), min, max));
    }

    /**
     * Rounds values to nearest integer with default order.
     *
     * @return new darray with rounded values
     */
    public final DArray<N> rint() {
        return unary(DArrayOp.unaryRint());
    }

    /**
     * Rounds values to nearest integer with specified order.
     *
     * @param order storage order
     * @return new darray with rounded values
     */
    public final DArray<N> rint(Order order) {
        return unary(DArrayOp.unaryRint(), order);
    }

    /**
     * Rounds values to nearest integer in place.
     *
     * @return this darray instance
     */
    public final DArray<N> rint_() {
        return unary_(DArrayOp.unaryRint());
    }

    /**
     * Rounds values up to nearest integer bigger or equal than the value with default storage order.
     *
     * @return new darray with ceiling values
     */
    public final DArray<N> ceil() {
        return unary(DArrayOp.unaryCeil());
    }

    /**
     * Rounds values up to nearest integer bigger or equal than the value with specified storage order.
     *
     * @param order storage order
     * @return new darray with ceiling values
     */
    public final DArray<N> ceil(Order order) {
        return unary(DArrayOp.unaryCeil(), order);
    }

    /**
     * Rounds values up to nearest integer bigger  in place.
     *
     * @return this darray instance
     */
    public final DArray<N> ceil_() {
        return unary_(DArrayOp.unaryCeil());
    }

    /**
     * Rounds values down to nearest lesser integer with default order.
     *
     * @return new darray with floor values
     */
    public final DArray<N> floor() {
        return unary(DArrayOp.unaryFloor());
    }

    /**
     * Rounds values down to nearest lesser integer with specified order.
     *
     * @param order storage order
     * @return new darray with floor values
     */
    public final DArray<N> floor(Order order) {
        return unary(DArrayOp.unaryFloor(), order);
    }

    /**
     * Rounds values down to nearest lesser integer in place.
     *
     * @return this darray instance
     */
    public final DArray<N> floor_() {
        return unary_(DArrayOp.unaryFloor());
    }

    /**
     * Computes absolute values with default order.
     *
     * @return new darray with absolute values
     */
    public final DArray<N> abs() {
        return unary(DArrayOp.unaryAbs());
    }

    /**
     * Computes absolute values with specified order.
     *
     * @param order storage order
     * @return new darray with absolute values
     */
    public final DArray<N> abs(Order order) {
        return unary(DArrayOp.unaryAbs(), order);
    }

    /**
     * Computes absolute values in place.
     *
     * @return this darray instance
     */
    public final DArray<N> abs_() {
        return unary_(DArrayOp.unaryAbs());
    }

    /**
     * Negates values with default order.
     *
     * @return new darray with negated values
     */
    public final DArray<N> neg() {
        return unary(DArrayOp.unaryNeg());
    }

    /**
     * Negates values with specified order.
     *
     * @param order storage order
     * @return new darray with negated values
     */
    public final DArray<N> neg(Order order) {
        return unary(DArrayOp.unaryNeg(), order);
    }

    /**
     * Negates values in place.
     *
     * @return this darray instance
     */
    public final DArray<N> neg_() {
        return unary_(DArrayOp.unaryNeg());
    }

    /**
     * Computes natural logarithm with default order.
     *
     * @return new darray with logarithm values
     */
    public final DArray<N> log() {
        return unary(DArrayOp.unaryLog());
    }

    /**
     * Computes natural logarithm with specified order.
     *
     * @param order storage order
     * @return new darray with logarithm values
     */
    public final DArray<N> log(Order order) {
        return unary(DArrayOp.unaryLog(), order);
    }

    /**
     * Computes natural logarithm in place.
     *
     * @return this darray instance
     */
    public final DArray<N> log_() {
        return unary_(DArrayOp.unaryLog());
    }

    /**
     * Computes natural logarithm of (1 + x) with default order.
     *
     * @return new darray with log1p values
     */
    public final DArray<N> log1p() {
        return unary(DArrayOp.unaryLog1p());
    }

    /**
     * Computes natural logarithm of (1 + x) with specified order.
     *
     * @param order storage order
     * @return new darray with log1p values
     */
    public final DArray<N> log1p(Order order) {
        return unary(DArrayOp.unaryLog1p(), order);
    }

    /**
     * Computes natural logarithm of (1 + x) in place.
     *
     * @return this darray instance
     */
    public final DArray<N> log1p_() {
        return unary_(DArrayOp.unaryLog1p());
    }

    /**
     * Computes exponential with default order.
     *
     * @return new darray with exponential values
     */
    public final DArray<N> exp() {
        return unary(DArrayOp.unaryExp());
    }

    /**
     * Computes exponential with specified order.
     *
     * @param order storage order
     * @return new darray with exponential values
     */
    public final DArray<N> exp(Order order) {
        return unary(DArrayOp.unaryExp(), order);
    }

    /**
     * Computes exponential in place.
     *
     * @return this darray instance
     */
    public final DArray<N> exp_() {
        return unary_(DArrayOp.unaryExp());
    }

    /**
     * Computes exp(x) - 1 with default order.
     *
     * @return new darray with expm1 values
     */
    public final DArray<N> expm1() {
        return unary(DArrayOp.unaryExpm1());
    }

    /**
     * Computes exp(x) - 1 with specified order.
     *
     * @param order storage order
     * @return new darray with expm1 values
     */
    public final DArray<N> expm1(Order order) {
        return unary(DArrayOp.unaryExpm1(), order);
    }

    /**
     * Computes exp(x) - 1 in place.
     *
     * @return this darray instance
     */
    public final DArray<N> expm1_() {
        return unary_(DArrayOp.unaryExpm1());
    }

    /**
     * Computes sine with default order.
     *
     * @return new darray with sine values
     */
    public final DArray<N> sin() {
        return unary(DArrayOp.unarySin());
    }

    /**
     * Computes sine with specified order.
     *
     * @param order storage order
     * @return new darray with sine values
     */
    public final DArray<N> sin(Order order) {
        return unary(DArrayOp.unarySin(), order);
    }

    /**
     * Computes sine in place.
     *
     * @return this darray instance
     */
    public final DArray<N> sin_() {
        return unary_(DArrayOp.unarySin());
    }

    /**
     * Computes arcsine with default order.
     *
     * @return new darray with arcsine values
     */
    public final DArray<N> asin() {
        return unary(DArrayOp.unaryAsin());
    }

    /**
     * Computes arcsine with specified order.
     *
     * @param order storage order
     * @return new darray with arcsine values
     */
    public final DArray<N> asin(Order order) {
        return unary(DArrayOp.unaryAsin(), order);
    }

    /**
     * Computes arcsine in place.
     *
     * @return this darray instance
     */
    public final DArray<N> asin_() {
        return unary_(DArrayOp.unaryAsin());
    }

    /**
     * Computes hyperbolic sine with default order.
     *
     * @return new darray with hyperbolic sine values
     */
    public final DArray<N> sinh() {
        return unary(DArrayOp.unarySinh());
    }

    /**
     * Computes hyperbolic sine with specified order.
     *
     * @param order storage order
     * @return new darray with hyperbolic sine values
     */
    public final DArray<N> sinh(Order order) {
        return unary(DArrayOp.unarySinh(), order);
    }

    /**
     * Computes hyperbolic sine in place.
     *
     * @return this darray instance
     */
    public final DArray<N> sinh_() {
        return unary_(DArrayOp.unarySinh());
    }

    /**
     * Computes cosine with default order.
     *
     * @return new darray with cosine values
     */
    public final DArray<N> cos() {
        return unary(DArrayOp.unaryCos());
    }

    /**
     * Computes cosine with specified order.
     *
     * @param order storage order
     * @return new darray with cosine values
     */
    public final DArray<N> cos(Order order) {
        return unary(DArrayOp.unaryCos(), order);
    }

    /**
     * Computes cosine in place.
     *
     * @return this darray instance
     */
    public final DArray<N> cos_() {
        return unary_(DArrayOp.unaryCos());
    }

    /**
     * Computes arccosine with default order.
     *
     * @return new darray with arccosine values
     */
    public final DArray<N> acos() {
        return unary(DArrayOp.unaryAcos());
    }

    /**
     * Computes arccosine with specified order.
     *
     * @param order storage order
     * @return new darray with arccosine values
     */
    public final DArray<N> acos(Order order) {
        return unary(DArrayOp.unaryAcos(), order);
    }

    /**
     * Computes arccosine in place.
     *
     * @return this darray instance
     */
    public final DArray<N> acos_() {
        return unary_(DArrayOp.unaryAcos());
    }

    /**
     * Computes hyperbolic cosine with default order.
     *
     * @return new darray with hyperbolic cosine values
     */
    public final DArray<N> cosh() {
        return unary(DArrayOp.unaryCosh());
    }

    /**
     * Computes hyperbolic cosine with specified order.
     *
     * @param order storage order
     * @return new darray with hyperbolic cosine values
     */
    public final DArray<N> cosh(Order order) {
        return unary(DArrayOp.unaryCosh(), order);
    }

    /**
     * Computes hyperbolic cosine in place.
     *
     * @return this darray instance
     */
    public final DArray<N> cosh_() {
        return unary_(DArrayOp.unaryCosh());
    }

    /**
     * Computes tangent with default order.
     *
     * @return new darray with tangent values
     */
    public final DArray<N> tan() {
        return unary(DArrayOp.unaryTan());
    }

    /**
     * Computes tangent with specified order.
     *
     * @param order storage order
     * @return new darray with tangent values
     */
    public final DArray<N> tan(Order order) {
        return unary(DArrayOp.unaryTan(), order);
    }

    /**
     * Computes tangent in place.
     *
     * @return this darray instance
     */
    public final DArray<N> tan_() {
        return unary_(DArrayOp.unaryTan());
    }

    /**
     * Computes arctangent with default order.
     *
     * @return new darray with arctangent values
     */
    public final DArray<N> atan() {
        return unary(DArrayOp.unaryAtan());
    }

    /**
     * Computes arctangent with specified order.
     *
     * @param order storage order
     * @return new darray with arctangent values
     */
    public final DArray<N> atan(Order order) {
        return unary(DArrayOp.unaryAtan(), order);
    }

    /**
     * Computes arctangent in place.
     *
     * @return this darray instance
     */
    public final DArray<N> atan_() {
        return unary_(DArrayOp.unaryAtan());
    }

    /**
     * Computes hyperbolic tangent with default order.
     *
     * @return new darray with hyperbolic tangent values
     */
    public final DArray<N> tanh() {
        return unary(DArrayOp.unaryTanh());
    }

    /**
     * Computes hyperbolic tangent with specified order.
     *
     * @param order storage order
     * @return new darray with hyperbolic tangent values
     */
    public final DArray<N> tanh(Order order) {
        return unary(DArrayOp.unaryTanh(), order);
    }

    /**
     * Computes hyperbolic tangent in place.
     *
     * @return this darray instance
     */
    public final DArray<N> tanh_() {
        return unary_(DArrayOp.unaryTanh());
    }

    /**
     * Computes square with default order.
     *
     * @return new darray with squared values
     */
    public final DArray<N> sqr() {
        return unary(DArrayOp.unarySqr());
    }

    /**
     * Computes square with specified order.
     *
     * @param order storage order
     * @return new darray with squared values
     */
    public final DArray<N> sqr(Order order) {
        return unary(DArrayOp.unarySqr(), order);
    }

    /**
     * Computes square in place.
     *
     * @return this darray instance
     */
    public final DArray<N> sqr_() {
        return unary_(DArrayOp.unarySqr());
    }

    /**
     * Computes square root with default order.
     *
     * @return new darray with square root values
     */
    public final DArray<N> sqrt() {
        return unary(DArrayOp.unarySqrt());
    }

    /**
     * Computes square root with specified order.
     *
     * @param order storage order
     * @return new darray with square root values
     */
    public final DArray<N> sqrt(Order order) {
        return unary(DArrayOp.unarySqrt(), order);
    }

    /**
     * Computes square root in place.
     *
     * @return this darray instance
     */
    public final DArray<N> sqrt_() {
        return unary_(DArrayOp.unarySqrt());
    }

    /**
     * Raises values to specified power with default order.
     *
     * @param power exponent value
     * @return new darray with power values
     */
    public final DArray<N> pow(double power) {
        return unary(DArrayOp.unaryPow(power));
    }

    /**
     * Raises values to specified power with specified order.
     *
     * @param order storage order
     * @param power exponent value
     * @return new darray with power values
     */
    public final DArray<N> pow(Order order, double power) {
        return unary(DArrayOp.unaryPow(power), order);
    }

    /**
     * Raises values to specified power in place.
     *
     * @param power exponent value
     * @return this darray instance
     */
    public final DArray<N> pow_(double power) {
        return unary_(DArrayOp.unaryPow(power));
    }

    /**
     * Applies sigmoid function with default order.
     *
     * @return new darray with sigmoid values
     */
    public final DArray<N> sigmoid() {
        return unary(DArrayOp.unarySigmoid());
    }

    /**
     * Applies sigmoid function with specified order.
     *
     * @param order storage order
     * @return new darray with sigmoid values
     */
    public final DArray<N> sigmoid(Order order) {
        return unary(DArrayOp.unarySigmoid(), order);
    }

    /**
     * Applies sigmoid function in place.
     *
     * @return this darray instance
     */
    public final DArray<N> sigmoid_() {
        return unary_(DArrayOp.unarySigmoid());
    }

    /**
     * Applies softmax function with default order.
     *
     * @return new darray with softmax values
     */
    public final DArray<N> softmax() {
        return unary(DArrayOp.unarySoftmax());
    }

    /**
     * Applies softmax function with specified order.
     *
     * @param order storage order
     * @return new darray with softmax values
     */
    public final DArray<N> softmax(Order order) {
        return unary(DArrayOp.unarySoftmax(), order);
    }

    /**
     * Applies softmax function in place.
     *
     * @return this darray instance
     */
    public final DArray<N> softmax_() {
        return unary_(DArrayOp.unarySoftmax());
    }

    /**
     * Applies softmax function along specified axis with default order.
     *
     * @param axis axis along which softmax is applied
     * @return new darray with softmax values
     */
    public final DArray<N> softmax1d(int axis) {
        return softmax1d(axis, Order.defaultOrder());
    }

    /**
     * Applies softmax function along specified axis with specified order.
     *
     * @param axis     axis along which softmax is applied
     * @param askOrder storage order
     * @return new darray with softmax values
     */
    public final DArray<N> softmax1d(int axis, Order askOrder) {
        return copy(askOrder).softmax1d_(axis);
    }

    /**
     * Applies softmax function along specified axis in place.
     *
     * @param axis axis along which softmax is applied
     * @return this darray instance
     */
    public final DArray<N> softmax1d_(int axis) {
        return unary1d_(DArrayOp.unarySoftmax(), axis);
    }

    /**
     * Applies log-softmax function with default order.
     *
     * @return new darray with log-softmax values
     */
    public final DArray<N> logsoftmax() {
        return unary(DArrayOp.unaryLogSoftmax());
    }

    /**
     * Applies log-softmax function with specified order.
     *
     * @param order storage order
     * @return new darray with log-softmax values
     */
    public final DArray<N> logsoftmax(Order order) {
        return unary(DArrayOp.unaryLogSoftmax(), order);
    }

    /**
     * Applies log-softmax function in place.
     *
     * @return this darray instance
     */
    public final DArray<N> logsoftmax_() {
        return unary_(DArrayOp.unaryLogSoftmax());
    }

    /**
     * Applies log-softmax function along specified axis with default order.
     *
     * @param axis axis along which log-softmax is applied
     * @return new darray with log-softmax values
     */
    public final DArray<N> logsoftmax1d(int axis) {
        return logsoftmax1d(axis, Order.defaultOrder());
    }

    /**
     * Applies log-softmax function along specified axis with specified order.
     *
     * @param axis     axis along which log-softmax is applied
     * @param askOrder storage order
     * @return new darray with log-softmax values
     */
    public final DArray<N> logsoftmax1d(int axis, Order askOrder) {
        return copy(askOrder).logsoftmax1d_(axis);
    }

    /**
     * Applies log-softmax function along specified axis in place.
     *
     * @param axis axis along which log-softmax is applied
     * @return this darray instance
     */
    public final DArray<N> logsoftmax1d_(int axis) {
        return unary1d_(DArrayOp.unaryLogSoftmax(), axis);
    }


    //--------- BINARY OPERATIONS ----------------//

    /**
     * Applies binary operation element-wise with another darray and returns result with specified order.
     *
     * @param op    binary operator
     * @param other second operand darray
     * @param order storage order
     * @return new darray with operation result
     */
    public final DArray<N> binary(DArrayBinaryOp op, DArray<?> other, Order order) {
        // TODO: research optimization
        Broadcast.ElementWise broadcast = Broadcast.elementWise(List.of(this.shape(), other.shape()));
        if (!broadcast.valid()) {
            throw new IllegalArgumentException(
                    String.format("Operation could not be applied on darrays with shape: %s, %s", shape(), other.shape()));
        }
        DArray<N> copy = broadcast.transform(this).copy(order);
        return copy.binary_(op, broadcast.transform(other));
    }

    /**
     * Applies binary operation element-wise with another darray in place.
     *
     * @param op    binary operator
     * @param value second operand darray
     * @return this darray instance
     */
    public abstract DArray<N> binary_(DArrayBinaryOp op, DArray<?> value);

    /**
     * Applies binary operation element-wise with a scalar value and returns result with specified order.
     *
     * @param op    binary operator
     * @param value scalar value
     * @param order storage order
     * @return new darray with operation result
     */
    public final <M extends Number> DArray<N> binary(DArrayBinaryOp op, M value, Order order) {
        return copy(order).binary_(op, value);
    }

    /**
     * Applies binary operation element-wise with a scalar value in place.
     *
     * @param op    binary operator
     * @param value scalar value
     * @return this darray instance
     */
    public abstract <M extends Number> DArray<N> binary_(DArrayBinaryOp op, M value);

    /**
     * Adds another darray element-wise with default order.
     *
     * @param array second operand
     * @return new darray with sum
     */
    public final DArray<N> add(DArray<?> array) {
        return binary(DArrayOp.binaryAdd(), array, Order.defaultOrder());
    }

    /**
     * Adds another darray element-wise with specified order.
     *
     * @param array second operand
     * @param order storage order
     * @return new darray with sum
     */
    public final DArray<N> add(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryAdd(), array, order);
    }

    /**
     * Adds another darray element-wise in place.
     *
     * @param array second operand
     * @return this darray instance
     */
    public final DArray<N> add_(DArray<?> array) {
        return binary_(DArrayOp.binaryAdd(), array);
    }

    /**
     * Subtracts another darray element-wise with default order.
     *
     * @param array second operand
     * @return new darray with difference
     */
    public final DArray<N> sub(DArray<?> array) {
        return binary(DArrayOp.binarySub(), array, Order.defaultOrder());
    }

    /**
     * Subtracts another darray element-wise with specified order.
     *
     * @param array second operand
     * @param order storage order
     * @return new darray with difference
     */
    public final DArray<N> sub(DArray<?> array, Order order) {
        return binary(DArrayOp.binarySub(), array, order);
    }

    /**
     * Subtracts another darray element-wise in place.
     *
     * @param array second operand
     * @return this darray instance
     */
    public final DArray<N> sub_(DArray<?> array) {
        return binary_(DArrayOp.binarySub(), array);
    }

    /**
     * Multiplies with another darray element-wise with default order.
     *
     * @param array second operand
     * @return new darray with product
     */
    public final DArray<N> mul(DArray<?> array) {
        return binary(DArrayOp.binaryMul(), array, Order.defaultOrder());
    }

    /**
     * Multiplies with another darray element-wise with specified order.
     *
     * @param array second operand
     * @param order storage order
     * @return new darray with product
     */
    public final DArray<N> mul(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryMul(), array, order);
    }

    /**
     * Multiplies with another darray element-wise in place.
     *
     * @param array second operand
     * @return this darray instance
     */
    public final DArray<N> mul_(DArray<?> array) {
        return binary_(DArrayOp.binaryMul(), array);
    }

    /**
     * Divides by another darray element-wise with default order.
     *
     * @param array second operand
     * @return new darray with quotient
     */
    public final DArray<N> div(DArray<?> array) {
        return binary(DArrayOp.binaryDiv(), array, Order.defaultOrder());
    }

    /**
     * Divides by another darray element-wise with specified order.
     *
     * @param array second operand
     * @param order storage order
     * @return new darray with quotient
     */
    public final DArray<N> div(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryDiv(), array, order);
    }

    /**
     * Divides by another darray element-wise in place.
     *
     * @param array second operand
     * @return this darray instance
     */
    public final DArray<N> div_(DArray<?> array) {
        return binary_(DArrayOp.binaryDiv(), array);
    }

    /**
     * Computes element-wise minimum with another darray with default order.
     *
     * @param array second operand
     * @return new darray with minimum values
     */
    public final DArray<N> min(DArray<?> array) {
        return binary(DArrayOp.binaryMin(), array, Order.defaultOrder());
    }

    /**
     * Computes element-wise minimum with another darray with specified order.
     *
     * @param array second operand
     * @param order storage order
     * @return new darray with minimum values
     */
    public final DArray<N> min(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryMin(), array, order);
    }

    /**
     * Computes element-wise minimum with another darray in place.
     *
     * @param array second operand
     * @return this darray instance
     */
    public final DArray<N> min_(DArray<?> array) {
        return binary_(DArrayOp.binaryMin(), array);
    }

    /**
     * Computes element-wise maximum with another darray with default order.
     *
     * @param array second operand
     * @return new darray with maximum values
     */
    public final DArray<N> max(DArray<?> array) {
        return binary(DArrayOp.binaryMax(), array, Order.defaultOrder());
    }

    /**
     * Computes element-wise maximum with another darray with specified order.
     *
     * @param array second operand
     * @param order storage order
     * @return new darray with maximum values
     */
    public final DArray<N> max(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryMax(), array, order);
    }

    /**
     * Computes element-wise maximum with another darray in place.
     *
     * @param array second operand
     * @return this darray instance
     */
    public final DArray<N> max_(DArray<?> array) {
        return binary_(DArrayOp.binaryMax(), array);
    }

    /**
     * Adds a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with sum
     */
    public final DArray<N> add(int value) {
        return binary(DArrayOp.binaryAdd(), value, Order.defaultOrder());
    }

    /**
     * Adds a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with sum
     */
    public final DArray<N> add(double value) {
        return binary(DArrayOp.binaryAdd(), value, Order.defaultOrder());
    }

    /**
     * Adds a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with sum
     */
    public final DArray<N> add(int value, Order order) {
        return binary(DArrayOp.binaryAdd(), value, order);
    }

    /**
     * Adds a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with sum
     */
    public final DArray<N> add(double value, Order order) {
        return binary(DArrayOp.binaryAdd(), value, order);
    }

    /**
     * Adds a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> add_(int value) {
        return binary_(DArrayOp.binaryAdd(), value);
    }

    /**
     * Adds a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> add_(double value) {
        return binary_(DArrayOp.binaryAdd(), value);
    }

    /**
     * Subtracts a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with difference
     */
    public final DArray<N> sub(int value) {
        return binary(DArrayOp.binarySub(), value, Order.defaultOrder());
    }

    /**
     * Subtracts a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with difference
     */
    public final DArray<N> sub(double value) {
        return binary(DArrayOp.binarySub(), value, Order.defaultOrder());
    }

    /**
     * Subtracts a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with difference
     */
    public final DArray<N> sub(int value, Order order) {
        return binary(DArrayOp.binarySub(), value, order);
    }

    /**
     * Subtracts a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with difference
     */
    public final DArray<N> sub(double value, Order order) {
        return binary(DArrayOp.binarySub(), value, order);
    }

    /**
     * Subtracts a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> sub_(int value) {
        return binary_(DArrayOp.binarySub(), value);
    }

    /**
     * Subtracts a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> sub_(double value) {
        return binary_(DArrayOp.binarySub(), value);
    }

    /**
     * Multiplies by a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with product
     */
    public final DArray<N> mul(int value) {
        return binary(DArrayOp.binaryMul(), value, Order.defaultOrder());
    }

    /**
     * Multiplies by a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with product
     */
    public final DArray<N> mul(double value) {
        return binary(DArrayOp.binaryMul(), value, Order.defaultOrder());
    }

    /**
     * Multiplies by a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with product
     */
    public final DArray<N> mul(int value, Order order) {
        return binary(DArrayOp.binaryMul(), value, order);
    }

    /**
     * Multiplies by a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with product
     */
    public final DArray<N> mul(double value, Order order) {
        return binary(DArrayOp.binaryMul(), value, order);
    }

    /**
     * Multiplies by a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> mul_(int value) {
        return binary_(DArrayOp.binaryMul(), value);
    }

    /**
     * Multiplies by a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> mul_(double value) {
        return binary_(DArrayOp.binaryMul(), value);
    }

    /**
     * Divides by a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with quotient
     */
    public final DArray<N> div(int value) {
        return binary(DArrayOp.binaryDiv(), value, Order.defaultOrder());
    }

    /**
     * Divides by a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with quotient
     */
    public final DArray<N> div(double value) {
        return binary(DArrayOp.binaryDiv(), value, Order.defaultOrder());
    }

    /**
     * Divides by a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with quotient
     */
    public final DArray<N> div(int value, Order order) {
        return binary(DArrayOp.binaryDiv(), value, order);
    }

    /**
     * Divides by a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with quotient
     */
    public final DArray<N> div(double value, Order order) {
        return binary(DArrayOp.binaryDiv(), value, order);
    }

    /**
     * Divides by a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> div_(int value) {
        return binary_(DArrayOp.binaryDiv(), value);
    }

    /**
     * Divides by a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> div_(double value) {
        return binary_(DArrayOp.binaryDiv(), value);
    }

    /**
     * Computes element-wise minimum with a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with minimum values
     */
    public final DArray<N> min(int value) {
        return binary(DArrayOp.binaryMin(), value, Order.defaultOrder());
    }

    /**
     * Computes element-wise minimum with a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with minimum values
     */
    public final DArray<N> min(double value) {
        return binary(DArrayOp.binaryMin(), value, Order.defaultOrder());
    }

    /**
     * Computes element-wise minimum with a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with minimum values
     */
    public final DArray<N> min(int value, Order order) {
        return binary(DArrayOp.binaryMin(), value, order);
    }

    /**
     * Computes element-wise minimum with a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with minimum values
     */
    public final DArray<N> min(double value, Order order) {
        return binary(DArrayOp.binaryMin(), value, order);
    }

    /**
     * Computes element-wise minimum with a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> min_(int value) {
        return binary_(DArrayOp.binaryMin(), value);
    }

    /**
     * Computes element-wise minimum with a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> min_(double value) {
        return binary_(DArrayOp.binaryMin(), value);
    }

    /**
     * Computes element-wise maximum with a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with maximum values
     */
    public final DArray<N> max(int value) {
        return binary(DArrayOp.binaryMax(), value, Order.defaultOrder());
    }

    /**
     * Computes element-wise maximum with a scalar value with default order.
     *
     * @param value scalar value
     * @return new darray with maximum values
     */
    public final DArray<N> max(double value) {
        return binary(DArrayOp.binaryMax(), value, Order.defaultOrder());
    }

    /**
     * Computes element-wise maximum with a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with maximum values
     */
    public final DArray<N> max(int value, Order order) {
        return binary(DArrayOp.binaryMax(), value, order);
    }

    /**
     * Computes element-wise maximum with a scalar value with specified order.
     *
     * @param value scalar value
     * @param order storage order
     * @return new darray with maximum values
     */
    public final DArray<N> max(double value, Order order) {
        return binary(DArrayOp.binaryMax(), value, order);
    }

    /**
     * Computes element-wise maximum with a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> max_(int value) {
        return binary_(DArrayOp.binaryMax(), value);
    }

    /**
     * Computes element-wise maximum with a scalar value in place.
     *
     * @param value scalar value
     * @return this darray instance
     */
    public final DArray<N> max_(double value) {
        return binary_(DArrayOp.binaryMax(), value);
    }

    /**
     * Fused multiply-add: multiplies by scalar and adds darray with default order.
     *
     * @param a multiplication factor
     * @param t darray to add
     * @return new darray with result
     */
    public final DArray<N> fma(int a, DArray<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    /**
     * Fused multiply-add: multiplies by scalar and adds darray with default order.
     *
     * @param a multiplication factor
     * @param t darray to add
     * @return new darray with result
     */
    public final DArray<N> fma(double a, DArray<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    /**
     * Fused multiply-add: multiplies by scalar and adds darray with specified order.
     *
     * @param a     multiplication factor
     * @param t     darray to add
     * @param order storage order
     * @return new darray with result
     */
    public final DArray<N> fma(int a, DArray<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    /**
     * Fused multiply-add: multiplies by scalar and adds darray with specified order.
     *
     * @param a     multiplication factor
     * @param t     darray to add
     * @param order storage order
     * @return new darray with result
     */
    public final DArray<N> fma(double a, DArray<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    /**
     * Adds in place the given matrix {@code t} multiplied by {@code factor} to the DArray element wise.
     *
     * @param factor multiplication factor
     * @param t      DArray to be multiplied and added to the current one
     * @return same DArray with values changed
     */
    public abstract DArray<N> fma_(N factor, DArray<?> t);

    /**
     * Fused multiply-add in place: multiplies by scalar and adds darray.
     *
     * @param factor multiplication factor
     * @return this darray instance
     */
    public final DArray<N> fma_(int factor, DArray<?> t) {
        return fma_(dt().cast(factor), t);
    }

    /**
     * Fused multiply-add in place: multiplies by scalar and adds darray.
     *
     * @param factor multiplication factor
     * @return this darray instance
     */
    public final DArray<N> fma_(double factor, DArray<?> t) {
        return fma_(dt().cast(factor), t);
    }

    //--------- REDUCE OPERATIONS ----------------//

    /**
     * Applies reduce operation across all elements.
     *
     * @param op reduce operator
     * @return scalar result
     */
    public abstract N reduce(DArrayReduceOp op);

    /**
     * Applies reduce operation along specified axis.
     *
     * @param op    reduce operator
     * @param axis  axis to reduce along
     * @param order storage order
     * @return darray with reduced dimension
     */
    public abstract DArray<N> reduce1d(DArrayReduceOp op, int axis, Order order);


    /**
     * Reduce operations on all dimensions specified by {@code shape}, which has to be the last dimensions of the tensor.
     * The resulting tensor will have the shape given by the first dimensions which do not belong to the reducing shape.
     *
     * @param op      reduce operation which will be applied over each module of shape size
     * @param shape   reduced shape, the shape must match the last dimensions of the tensor shape
     * @param keepDim if true then the original number of dimensions will be kept with size 1, otherwise those dimensions will collaps
     * @param order   the storage order of the result
     * @return darray with reduced values
     */
    public abstract DArray<N> reduceOn(DArrayReduceOp op, Shape shape, boolean keepDim, Order order);

    /**
     * Reduce operation on first dimensions until the reduced darray arrives at the target {@code shape} specified as parameter.
     * The target shape contains the last dimensions of the current darray. As such, if the target shape is equal with the original
     * shape, no reduction will be done.
     *
     * @param op          reduction operation
     * @param targetShape target shape
     * @param order       storage order of the result
     * @return reduced tensor
     */
    public abstract DArray<N> reduceTo(DArrayReduceOp op, Shape targetShape, boolean keepDim, Order order);

    /**
     * Computes sum of all elements.
     *
     * @return sum value
     */
    public final N sum() {
        return reduce(DArrayOp.reduceSum());
    }

    /**
     * Computes sum along axis with default storage order.
     *
     * @param axis axis to sum along
     * @return new darray with sums
     */
    public final DArray<N> sum1d(int axis) {
        return reduce1d(DArrayOp.reduceSum(), axis, Order.defaultOrder());
    }

    /**
     * Computes sum along axis with specified order.
     *
     * @param axis  axis to sum along
     * @param order storage order
     * @return new darray with sums
     */
    public final DArray<N> sum1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceSum(), axis, order);
    }

    /**
     * Reduces on specified shape dimensions with default order.
     *
     * @param shape   shape to reduce on
     * @param keepDim keep dimensions if true
     * @return darray with reduced values
     */
    public final DArray<N> sumOn(Shape shape, boolean keepDim) {
        return reduceOn(DArrayOp.reduceSum(), shape, keepDim, Order.defaultOrder());
    }

    /**
     * Reduces on specified shape dimensions with specified order.
     *
     * @param shape   shape to reduce on
     * @param keepDim keep dimensions if true
     * @param order   storage order
     * @return darray with reduced values
     */
    public final DArray<N> sumOn(Shape shape, boolean keepDim, Order order) {
        return reduceOn(DArrayOp.reduceSum(), shape, keepDim, order);
    }

    /**
     * Reduces to specified target shape with default order.
     *
     * @param shape   target shape
     * @param keepDim keep dimensions if true
     * @return darray with reduced values
     */
    public final DArray<N> sumTo(Shape shape, boolean keepDim) {
        return reduceTo(DArrayOp.reduceSum(), shape, keepDim, Order.defaultOrder());
    }

    /**
     * Reduces to specified target shape with specified order.
     *
     * @param shape   target shape
     * @param keepDim keep dimensions if true
     * @param order   storage order
     * @return darray with reduced values
     */
    public final DArray<N> sumTo(Shape shape, boolean keepDim, Order order) {
        return reduceTo(DArrayOp.reduceSum(), shape, keepDim, order);
    }

    /**
     * Computes sum ignoring NaN values.
     *
     * @return sum value
     */
    public final N nanSum() {
        return reduce(DArrayOp.reduceNanSum());
    }

    /**
     * Computes sum along axis ignoring NaN values with default order.
     *
     * @param axis axis to sum along
     * @return darray with sums
     */
    public final DArray<N> nanSum1d(int axis) {
        return reduce1d(DArrayOp.reduceNanSum(), axis, Order.defaultOrder());
    }

    /**
     * Computes sum along axis ignoring NaN values with specified order.
     *
     * @param axis  axis to sum along
     * @param order storage order
     * @return darray with sums
     */
    public final DArray<N> nanSum1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanSum(), axis, order);
    }

    /**
     * Computes product of all elements.
     *
     * @return product value
     */
    public final N prod() {
        return reduce(DArrayOp.reduceProd());
    }

    /**
     * Computes product along specified axis with default order.
     *
     * @param axis axis to compute product along
     * @return darray with products
     */
    public final DArray<N> prod1d(int axis) {
        return reduce1d(DArrayOp.reduceProd(), axis, Order.defaultOrder());
    }

    /**
     * Computes product along specified axis with specified order.
     *
     * @param axis  axis to compute product along
     * @param order storage order
     * @return darray with products
     */
    public final DArray<N> prod1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceProd(), axis, order);
    }

    /**
     * Computes product ignoring NaN values.
     *
     * @return product value
     */
    public final N nanProd() {
        return reduce(DArrayOp.reduceNanProd());
    }

    /**
     * Computes product along axis ignoring NaN values with default order.
     *
     * @param axis axis to compute product along
     * @return darray with products
     */
    public final DArray<N> nanProd1d(int axis) {
        return reduce1d(DArrayOp.reduceNanProd(), axis, Order.defaultOrder());
    }

    /**
     * Computes product along axis ignoring NaN values with specified order.
     *
     * @param axis  axis to compute product along
     * @param order storage order
     * @return darray with products
     */
    public final DArray<N> nanProd1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanProd(), axis, order);
    }

    /**
     * Finds maximum value.
     *
     * @return maximum value
     */
    public final N amax() {
        return reduce(DArrayOp.reduceMax());
    }

    /**
     * Finds maximum along axis with default order.
     *
     * @param axis axis to find maximum along
     * @return darray with maximum values
     */
    public final DArray<N> amax1d(int axis) {
        return amax1d(axis, Order.defaultOrder());
    }

    /**
     * Finds maximum along axis with specified order.
     *
     * @param axis  axis to find maximum along
     * @param order storage order
     * @return darray with maximum values
     */
    public final DArray<N> amax1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceMax(), axis, order);
    }

    /**
     * Finds maximum value ignoring NaN.
     *
     * @return maximum value
     */
    public final N nanMax() {
        return reduce(DArrayOp.reduceNanMax());
    }

    /**
     * Finds maximum along axis ignoring NaN with default order.
     *
     * @param axis axis to find maximum along
     * @return darray with maximum values
     */
    public final DArray<N> nanMax1d(int axis) {
        return reduce1d(DArrayOp.reduceNanMax(), axis, Order.defaultOrder());
    }

    /**
     * Finds maximum along axis ignoring NaN with specified order.
     *
     * @param axis  axis to find maximum along
     * @param order storage order
     * @return darray with maximum values
     */
    public final DArray<N> nanMax1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanMax(), axis, order);
    }

    /**
     * Finds minimum value.
     *
     * @return minimum value
     */
    public final N amin() {
        return reduce(DArrayOp.reduceMin());
    }

    /**
     * Finds minimum along axis with default order.
     *
     * @param axis axis to find minimum along
     * @return darray with minimum values
     */
    public final DArray<N> amin1d(int axis) {
        return reduce1d(DArrayOp.reduceMin(), axis, Order.defaultOrder());
    }

    /**
     * Finds minimum along axis with specified order.
     *
     * @param axis  axis to find minimum along
     * @param order storage order
     * @return darray with minimum values
     */
    public final DArray<N> amin1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceMin(), axis, order);
    }

    /**
     * Finds minimum value ignoring NaN.
     *
     * @return minimum value
     */
    public final N nanMin() {
        return reduce(DArrayOp.reduceNanMin());
    }

    /**
     * Finds minimum along axis ignoring NaN with default order.
     *
     * @param axis axis to find minimum along
     * @return darray with minimum values
     */
    public final DArray<N> nanMin1d(int axis) {
        return reduce1d(DArrayOp.reduceNanMin(), axis, Order.defaultOrder());
    }

    /**
     * Finds minimum along axis ignoring NaN with specified order.
     *
     * @param axis  axis to find minimum along
     * @param order storage order
     * @return darray with minimum values
     */
    public final DArray<N> nanMin1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanMin(), axis, order);
    }

    /**
     * Computes mean of all elements.
     *
     * @return mean value
     */
    public final N mean() {
        return reduce(DArrayOp.reduceMean());
    }

    /**
     * Computes mean along axis with default order.
     *
     * @param axis axis to compute mean along
     * @return darray with mean values
     */
    public final DArray<N> mean1d(int axis) {
        return reduce1d(DArrayOp.reduceMean(), axis, Order.defaultOrder());
    }

    /**
     * Computes mean along axis with specified order.
     *
     * @param axis  axis to compute mean along
     * @param order storage order
     * @return darray with mean values
     */
    public final DArray<N> mean1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceMean(), axis, order);
    }

    /**
     * Computes mean on specified shape with default order. The shape must match last dimensions
     * of darray and the resulting shape will be the first dimensions of the original darray.
     *
     * @param shape   shape to reduce on
     * @param keepDim keep dimensions if true
     * @return darray with mean values
     */
    public final DArray<N> meanOn(Shape shape, boolean keepDim) {
        return reduceOn(DArrayOp.reduceMean(), shape, keepDim, Order.defaultOrder());
    }

    /**
     * Computes mean on specified shape with specified order. The shape must match last dimensions
     * of darray and the resulting shape will be the first dimensions of the original darray.
     * The storage order is the default storage order.
     *
     * @param shape   shape to reduce on
     * @param keepDim keep dimensions if true
     * @param order   storage order
     * @return darray with mean values
     */
    public final DArray<N> meanOn(Shape shape, boolean keepDim, Order order) {
        return reduceOn(DArrayOp.reduceMean(), shape, keepDim, order);
    }

    /**
     * Computes mean to a specified target shape with default order. The shape mus match the last dimensions
     * of the array and the resulting shape will be the first dimensions of the original darray.
     * The storage order is the default storage order.
     *
     * @param shape   shape to reduce to
     * @param keepDim keep dimensions if true
     * @return darray with mean values
     */
    public final DArray<N> meanTo(Shape shape, boolean keepDim) {
        return reduceTo(DArrayOp.reduceMean(), shape, keepDim, Order.defaultOrder());
    }

    /**
     * Computes mean to a specified target shape with default order. The shape mus match the last dimensions
     * of the array and the resulting shape will be the first dimensions of the original darray.
     * The storage order is the specified storage order.
     *
     * @param shape   shape to reduce to
     * @param keepDim keep dimensions if true
     * @param order   storage order
     * @return darray with mean values
     */
    public final DArray<N> meanTo(Shape shape, boolean keepDim, Order order) {
        return reduceTo(DArrayOp.reduceMean(), shape, keepDim, order);
    }

    /**
     * Computes the mean on all elements ignoring NaN values.
     *
     * @return mean value
     */
    public final N nanMean() {
        return reduce(DArrayOp.reduceNanMean());
    }

    /**
     * Computes the variance on all elements with the specified difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     *
     * @param ddof difference degree of freedom
     * @return variance value
     */
    public final N var(int ddof) {
        return reduce(DArrayOp.reduceVarc(ddof));
    }

    /**
     * Computes the variance along the given dimension with specified difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the default storage order.
     *
     * @param axis axis along which the variance will be computed
     * @param ddof difference degree of freedom
     * @return darray with variance values
     */
    public final DArray<N> var1d(int axis, int ddof) {
        return reduce1d(DArrayOp.reduceVarc(ddof), axis, Order.defaultOrder());
    }

    /**
     * Computes the variance along the given dimension with specified difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the specified storage order.
     *
     * @param axis  axis along which the variance will be computed
     * @param ddof  difference degree of freedom
     * @param order storage order
     * @return darray with variance values
     */
    public final DArray<N> var1d(int axis, int ddof, Order order) {
        return reduce1d(DArrayOp.reduceVarc(ddof), axis, order);
    }

    /**
     * Computes the standard deviation over all elements with given difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     *
     * @param ddof difference degree of freedom
     * @return computed standard deviation value
     */
    public final N std(int ddof) {
        return dt().cast(Math.sqrt(var(ddof).doubleValue()));
    }

    /**
     * Computes the standard deviation along the given axis with the specified difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the default storage order.
     *
     * @param axis axis along which the standard deviation will be computed
     * @param ddof difference degree of freedom
     * @return darray with standard deviation values
     */
    public final DArray<N> std1d(int axis, int ddof) {
        return var1d(axis, ddof).sqrt_();
    }

    /**
     * Computes the standard deviation along the given axis with the specified difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the specified storage order.
     *
     * @param axis  axis along which the standard deviation will be computed
     * @param ddof  difference degree of freedom
     * @param order storage order
     * @return darray with standard deviation values
     */
    public final DArray<N> std1d(int axis, int ddof, Order order) {
        return var1d(axis, ddof, order).sqrt_();
    }

    /**
     * Computes the variance over all elements using a precomputed mean and difference degree of freedom.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the default storage order.
     *
     * @param ddof difference degree of freedom
     * @param mean precomputed mean
     * @return variance value
     */
    public final N var(int ddof, double mean) {
        return reduce(DArrayOp.reduceVarc(ddof, mean));
    }

    /**
     * Computes the variance along the given axis with the specified difference degree of freedom and darray of precomputed means.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the default storage order.
     *
     * @param axis axis along which the variance is computed
     * @param ddof difference degree of freedom
     * @param mean precomputed mean values
     * @return darray with variance values
     */
    public final DArray<N> var1d(int axis, int ddof, DArray<?> mean) {
        return var1d(axis, ddof, mean, Order.defaultOrder());
    }

    /**
     * Computes the variance along the given axis with the specified difference degree of freedom and darray of precomputed means.
     * The degree of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     * The storage order is the specified storage order.
     *
     * @param axis  axis along which the variance is computed
     * @param ddof  difference degree of freedom
     * @param mean  precomputed mean values
     * @param order storage order
     * @return darray with variance values
     */
    public abstract DArray<N> var1d(int axis, int ddof, DArray<?> mean, Order order);

    /**
     * Compute variance on specified shape with default storage order. The specified shape must match the last dimensions
     * of the darray and the result shape will be the first dimensions of the original darray.
     * The degrees of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     *
     * @param shape   shape to compute variance on
     * @param ddof    difference degrees of freedom
     * @param keepDim keep dimensions if true
     * @param mean    precomputed mean values
     * @return darray with variance values
     */
    public final DArray<N> varOn(Shape shape, int ddof, boolean keepDim, DArray<?> mean) {
        return varOn(shape, ddof, mean, keepDim, Order.defaultOrder());
    }

    /**
     * Compute variance on specified shape with specified storage order. The specified shape must match the last dimensions
     * of the darray and the result shape will be the first dimensions of the original darray.
     * The degrees of freedom are computed as {@code N - ddof}, where {@code N} is the number of elements.
     *
     * @param shape   shape to compute variance on
     * @param ddof    difference degrees of freedom
     * @param keepDim keep dimensions if true
     * @param mean    precomputed mean values
     * @param order   storage order of the result
     * @return darray with variance values
     */
    public abstract DArray<N> varOn(Shape shape, int ddof, DArray<?> mean, boolean keepDim, Order order);

    /**
     * Computes the position of the maximum value from the darray. The position of an element is
     * relative to an ordering, and it is the position in a flat array ordered by the storage order.
     * The position is also equivalent with the one obtained using an iterator for the corresponding ordering.
     * <p>
     * This uses the default ordering for the position computation.
     *
     * @return position of the maximum value from darray
     */
    public final int argmax() {
        return argmax(Order.defaultOrder());
    }

    /**
     * Computes the position of the maximum value from the darray using the specified ordering. The position of
     * the element is relative to an ordering, and it is the position in a flat array ordered by the storage order.
     * The position is also equivalent with the one obtained using an iterator for the corresponding ordering.
     *
     * @param order specified ordering
     * @return position of the maximum value
     */
    public abstract int argmax(Order order);

    public final DArray<Integer> argmax1d(int axis, boolean keepDim) {
        return argmax1d(axis, keepDim, Order.defaultOrder());
    }

    public abstract DArray<Integer> argmax1d(int axis, boolean keepDim, Order order);

    /**
     * Computes the position of the minimum value from the darray. The position of an element is
     * relative to an ordering, and it is the position in a flat array ordered by the storage order.
     * The position is also equivalent with the one obtained using an iterator for the corresponding ordering.
     * <p>
     * This uses the default ordering for the position computation.
     *
     * @return position of the minimum value from darray
     */
    public final int argmin() {
        return argmin(Order.defaultOrder());
    }

    /**
     * Computes the position of the minimum value from the darray using the specified ordering. The position of
     * the element is relative to an ordering, and it is the position in a flat array ordered by the storage order.
     * The position is also equivalent with the one obtained using an iterator for the corresponding ordering.
     *
     * @param order specified ordering
     * @return position of the minimum value
     */
    public abstract int argmin(Order order);

    public final DArray<Integer> argmin1d(int axis, boolean keepDim) {
        return argmin1d(axis, keepDim, Order.defaultOrder());
    }

    public abstract DArray<Integer> argmin1d(int axis, boolean keepDim, Order order);


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

    //------- VECTOR MATRIX OPERATIONS ----------//

    /**
     * Computes the dot product between vectors. This operation is available only if the
     * two operands are vectors. Vectors have to have the same size.
     *
     * @param other the other vector
     * @return scalar result
     */
    public abstract N inner(DArray<?> other);

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
    public abstract N inner(DArray<?> other, int start, int end);

    /**
     * Computes the outer product between two vectors. This operation is available only if the two
     * DArrays are vectors. The result is a matrix of shape {@code (n,m)}, where {@code n} is the
     * size of the first vector and {@code m} is the size of the second vector.
     * <p>
     * This operation does not perform broadcast.
     *
     * @param other the other vector
     * @return matrix containing the outer vector
     */
    public final DArray<N> outer(DArray<?> other) {
        if (!isVector() || !other.isVector()) {
            throw new IllegalArgumentException("Outer product is available only for vectors.");
        }
        return stretch(1).mm(other.stretch(0));
    }

    /**
     * Performs matrix vector dot product. The first DArray must be a matrix and the second DArray must be a vector.
     * Also, the second dimension of the matrix must have the same size as the dimension of the vector.
     * <p>
     * The result is a vector of the size equal with the first dimension of the matrix.
     * <p>
     * This operation does not perform broadcast and the storage order is the default order
     *
     * @param other the second operand, which must be a vector.
     * @return a vector containing the result of the matrix vector dot product
     */
    public final DArray<N> mv(DArray<?> other) {
        return mv(other, Order.defaultOrder());
    }

    /**
     * Performs matrix vector dot product. The first DArray must be a matrix and the second DArray must be a vector.
     * Also, the second dimension of the matrix must have the same size as the dimension of the vector.
     * <p>
     * The result is a vector of the size equal with the first dimension of the matrix.
     * <p>
     * This operation does not perform broadcast and the storage order is specified by {@code askOrder} parameter.
     *
     * @param other the second operand, which must be a vector.
     * @return a vector containing the result of the matrix vector dot product
     */
    public abstract DArray<N> mv(DArray<?> other, Order askOrder);

    /**
     * Performs a batched matrix vector multiplication. Self DArray plays the role of matrix batch, the {@code other} DArray
     * is the vector batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self DArray is matrix {@code (n,m)} and other DArray is a vector shape {code (m)}, the result is a unit batch
     * of shape {@code (1,n)}.
     * <p>
     * If self is a batch matrix DArray of shape {@code (b,n,m)} and second is a vector shape {@code (m)}, the vectors is multiplied with
     * all the matrices in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a matrix DArray of shape {@code (n,m)} and the other is a batch of vectors with shape {@code (b,m)}, the matrix will
     * be multiplied with every vector in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self DArray is a batch of matrices with shape {@code (b,n,m)} and {code other} is a batch of vectors with shape {@code (b,m)},
     * each matrix from the batch will be multiplied with its corresponding vector from the batch and the result will have shape {@code (b,m)}.
     * <p>
     * All other configurations are invalid and an {@link IllegalArgumentException} exception will be thrown.
     * <p>
     * The storage order of the result is the default order.
     *
     * @param other the batch of vectors
     * @return the batch with results
     */
    public final DArray<N> bmv(DArray<?> other) {
        return bmv(other, Order.defaultOrder());
    }

    /**
     * Performs a batched matrix vector multiplication. Self DArray plays the role of matrix batch, the {@code other} DArray
     * is the vector batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self DArray is matrix {@code (n,m)} and other DArray is a vector shape {code (m)}, the result is a unit batch
     * of shape {@code (1,n)}.
     * <p>
     * If self is a batch matrix DArray of shape {@code (b,n,m)} and second is a vector shape {@code (m)}, the vectors is multiplied with
     * all the matrices in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a matrix DArray of shape {@code (n,m)} and the other is a batch of vectors with shape {@code (b,m)}, the matrix will
     * be multiplied with every vector in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self DArray is a batch of matrices with shape {@code (b,n,m)} and {code other} is a batch of vectors with shape {@code (b,m)},
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
    public abstract DArray<N> bmv(DArray<?> other, Order askOrder);

    /**
     * Performs the dot product between this object transposed, which must be a vector, and the other
     * DArray which must be a matrix. The size of the vector must be equal with the size of the first dimesion of the matrix.
     * <p>
     * The result is a vector with size equal with the size of the second dimension of the matrix.
     * This operation is equivalent with calling {@link #mv(DArray)}, but with transposed matrix.
     * <p>
     * This operation does not perform broadcasting and the storage order of the result is the default order.
     *
     * @param other the other DArray which must be a matrix.
     * @return the result of the vector transpose matrix dot product
     */
    public final DArray<N> vtm(DArray<?> other) {
        return vtm(other, Order.defaultOrder());
    }

    /**
     * Performs the dot product between this object transposed, which must be a vector, and the other
     * DArray which must be a matrix. The size of the vector must be equal with the size of the first dimesion of the matrix.
     * <p>
     * The result is a vector with size equal with the size of the second dimension of the matrix.
     * This operation is equivalent with calling {@link #mv(DArray)}, but with transposed matrix.
     * <p>
     * This operation does not perform broadcasting and the storage order of the result is specified by {@code askOrder} parameter.
     *
     * @param other the other DArray which must be a matrix.
     * @return the result of the vector transpose matrix dot product
     */
    public abstract DArray<N> vtm(DArray<?> other, Order askOrder);

    /**
     * Performs a batched vector transposed matrix multiplication. Self DArray plays the role of vector batch, the {@code other} DArray
     * is the matrix batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self is vector {@code (n)} and other DArray is a matrix {code (n,m)}, the result is a unit batch
     * of shape {@code (1,m)}.
     * <p>
     * If self is a batch vector DArray of shape {@code (b,n)} and second is a matrix shape {@code (n,m)}, the vector are multiplied with
     * all the same matrix and the result will have shape {@code (b,m)}.
     * <p>
     * If self is a vector DArray of shape {@code (n)} and the other is a batch of matrices with shape {@code (b,n,m)}, the vector will
     * be multiplied with every matrix in the batch and the result will have shape {@code (b,m)}.
     * <p>
     * If self DArray is a batch of vectors with shape {@code (b,n)} and {code other} is a batch of matrices with shape {@code (b,n,m)},
     * each vector from the batch will be multiplied with its corresponding matrix from the batch and the result will have shape {@code (b,m)}.
     * <p>
     * All other configurations are invalid and an {@link IllegalArgumentException} exception will be thrown.
     * <p>
     * The storage order of the result is the default order.
     *
     * @param other the batch of vectors
     * @return the batch with results
     */
    public final DArray<?> bvtm(DArray<?> other) {
        return bvtm(other, Order.defaultOrder());
    }

    /**
     * Performs a batched vector transposed matrix multiplication. Self DArray plays the role of vector batch, the {@code other} DArray
     * is the matrix batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self is vector {@code (n)} and other DArray is a matrix {code (n,m)}, the result is a unit batch
     * of shape {@code (1,m)}.
     * <p>
     * If self is a batch vector DArray of shape {@code (b,n)} and second is a matrix shape {@code (n,m)}, the vector are multiplied with
     * all the same matrix and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a vector DArray of shape {@code (n)} and the other is a batch of matrices with shape {@code (b,n,m)}, the vector will
     * be multiplied with every matrix in the batch and the result will have shape {@code (b,m)}.
     * <p>
     * If self DArray is a batch of vectors with shape {@code (b,n)} and {code other} is a batch of matrices with shape {@code (b,n,m)},
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
    public abstract DArray<?> bvtm(DArray<?> other, Order askOrder);

    /**
     * Performs matrix multiplication between two DArrays. The two DArrays must both be matrices.
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
    public final DArray<N> mm(DArray<?> other) {
        return mm(other, Order.defaultOrder());
    }

    public final DArray<N> mm(DArray<?> other, DArray<?> to) {
        return mm(other, Order.defaultOrder(), to);
    }

    /**
     * Performs matrix multiplication between two DArrays. The two DArrays must both be matrices.
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
    public abstract DArray<N> mm(DArray<?> other, Order askOrder);

    public abstract DArray<N> mm(DArray<?> other, Order askOrder, DArray<?> to);

    /**
     * Performs batch matrix-matrix multiplication. Batch index is the first parameter, if exists.
     * If self is an DArray of shape {@code (b,n,m)} and {@code other} has shape {@code (b,m,p)}
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
    public final DArray<N> bmm(DArray<?> other) {
        return bmm(other, Order.defaultOrder());
    }

    /**
     * Performs batch matrix-matrix multiplication. Batch index is the first parameter, if exists.
     * If self is an DArray of shape {@code (b,n,m)} and {@code other} has shape {@code (b,m,p)}
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
    public abstract DArray<N> bmm(DArray<?> other, Order askOrder);

    /**
     * Applies a 1D convolution over this input signal.
     * <p>
     * This DArray acts as the input and must have shape {@code (N, C_in, L)} or {@code (C_in, L)},
     * where {@code N} is the batch size, {@code C_in} is the number of input channels, and {@code L}
     * is the length of the signal. If rank is less than 3, leading dimensions of size 1 are prepended.
     * <p>
     * The {@code weights} tensor must have shape {@code (C_out, C_in/groups, kL)}, where {@code C_out}
     * is the number of output channels and {@code kL} is the kernel length.
     * <p>
     * The output length is computed as:
     * <pre>
     *   L_out = floor((L + 2 * padding - dilation * (kL - 1) - 1) / stride + 1)
     * </pre>
     * The output has shape {@code (N, C_out, L_out)}.
     *
     * @param weights     convolution kernel of shape {@code (C_out, C_in/groups, kL)}
     * @param bias        optional bias of shape {@code (C_out)}, or {@code null} if no bias
     * @param padding     zero-padding added to both sides of the input
     * @param stride      step between consecutive kernel applications
     * @param dilation    spacing between kernel elements
     * @param groups      number of blocked connections from input to output channels;
     *                    {@code C_in} and {@code C_out} must both be divisible by {@code groups}
     * @return output DArray of shape {@code (N, C_out, L_out)}
     */
    public abstract DArray<N> conv1d(DArray<?> weights, DArray<?> bias, int padding, int stride, int dilation, int groups);

    /**
     * Applies a 1D transposed convolution (also known as fractionally-strided convolution or
     * deconvolution) over this input signal. This is the gradient operation of {@link #conv1d}
     * with respect to its input, and can be used to upsample the signal.
     * <p>
     * This DArray acts as the input and must have shape {@code (N, C_in, L)} or {@code (C_in, L)},
     * where {@code N} is the batch size, {@code C_in} is the number of input channels, and {@code L}
     * is the length of the signal. If rank is less than 3, leading dimensions of size 1 are prepended.
     * <p>
     * The {@code weights} tensor must have shape {@code (C_in, C_out/groups, kL)}, where {@code C_out}
     * is the number of output channels and {@code kL} is the kernel length. Note that the first
     * dimension of the weight corresponds to the input channels of the transposed convolution
     * (i.e. the output channels of the corresponding forward convolution).
     * <p>
     * The output length is computed as:
     * <pre>
     *   L_out = (L - 1) * stride - 2 * padding + dilation * (kL - 1) + 1 + outputPadding
     * </pre>
     * The output has shape {@code (N, C_out, L_out)}.
     *
     * @param weights       transposed convolution kernel of shape {@code (C_in, C_out/groups, kL)}
     * @param bias          optional bias of shape {@code (C_out)}, or {@code null} if no bias
     * @param padding       implicit zero-padding added to both sides of the input; reduces output size
     * @param stride        step between consecutive kernel applications in the forward convolution;
     *                      controls the upsampling factor
     * @param dilation      spacing between kernel elements
     * @param groups        number of blocked connections from input to output channels;
     *                      {@code C_in} and {@code C_out} must both be divisible by {@code groups}
     * @param outputPadding additional size added to one side of the output shape; must be less than
     *                      {@code stride} and less than {@code dilation}; used to resolve ambiguity
     *                      when multiple input sizes map to the same output size
     * @return output DArray of shape {@code (N, C_out, L_out)}
     */
    public abstract DArray<N> convTranspose1d(DArray<?> weights, DArray<?> bias, int padding, int stride, int dilation, int groups, int outputPadding);

    public abstract DArray<N> unfold1d(int kLen, int padding, int stride, int dilation);

    /**
     * Applies a 2D convolution over this input signal.
     * Input shape: {@code (N, C_in, H, W)}, kernel shape: {@code (C_out, C_in/groups, kH, kW)}.
     * Output shape: {@code (N, C_out, H_out, W_out)} where
     * {@code H_out = floor((H + 2*padding - dilation*(kH-1) - 1) / stride + 1)} and similarly for W.
     *
     * @param weights kernel of shape {@code (C_out, C_in/groups, kH, kW)}
     * @param bias    optional bias of shape {@code (C_out)}, or {@code null}
     * @param padding zero-padding added to both spatial sides
     * @param stride  step between kernel applications
     * @param dilation spacing between kernel elements
     * @param groups  number of blocked connections
     * @return output DArray of shape {@code (N, C_out, H_out, W_out)}
     */
    public abstract DArray<N> conv2d(DArray<?> weights, DArray<?> bias, int padding, int stride, int dilation, int groups);

    public abstract DArray<N> unfold2d(int kH, int kW, int padding, int stride, int dilation);
    /**
     * Applies a 2D transposed convolution over this input signal.
     * Input shape: {@code (N, C_in, H, W)}, kernel shape: {@code (C_in, C_out/groups, kH, kW)}.
     * Output shape: {@code (N, C_out, H_out, W_out)} where
     * {@code H_out = (H-1)*stride - 2*padding + dilation*(kH-1) + 1 + outputPadding} and similarly for W.
     *
     * @param weights       kernel of shape {@code (C_in, C_out/groups, kH, kW)}
     * @param bias          optional bias of shape {@code (C_out)}, or {@code null}
     * @param padding       implicit zero-padding
     * @param stride        upsampling factor
     * @param dilation      spacing between kernel elements
     * @param groups        number of blocked connections
     * @param outputPadding additional size added to one side of the output
     * @return output DArray of shape {@code (N, C_out, H_out, W_out)}
     */
    public abstract DArray<N> convTranspose2d(DArray<?> weights, DArray<?> bias, int padding, int stride, int dilation, int groups, int outputPadding);

    /**
     * Applies a 3D convolution over this input signal.
     * Input shape: {@code (N, C_in, D, H, W)}, kernel shape: {@code (C_out, C_in/groups, kD, kH, kW)}.
     * Output shape: {@code (N, C_out, D_out, H_out, W_out)}.
     *
     * @param weights kernel of shape {@code (C_out, C_in/groups, kD, kH, kW)}
     * @param bias    optional bias of shape {@code (C_out)}, or {@code null}
     * @param padding zero-padding added to all spatial sides
     * @param stride  step between kernel applications
     * @param dilation spacing between kernel elements
     * @param groups  number of blocked connections
     * @return output DArray of shape {@code (N, C_out, D_out, H_out, W_out)}
     */
    public abstract DArray<N> conv3d(DArray<?> weights, DArray<?> bias, int padding, int stride, int dilation, int groups);

    public abstract DArray<N> unfold3d(int kD, int kH, int kW, int padding, int stride, int dilation);

    /**
     * Applies a 3D transposed convolution over this input signal.
     * Input shape: {@code (N, C_in, D, H, W)}, kernel shape: {@code (C_in, C_out/groups, kD, kH, kW)}.
     * Output shape: {@code (N, C_out, D_out, H_out, W_out)}.
     *
     * @param weights       kernel of shape {@code (C_in, C_out/groups, kD, kH, kW)}
     * @param bias          optional bias of shape {@code (C_out)}, or {@code null}
     * @param padding       implicit zero-padding
     * @param stride        upsampling factor
     * @param dilation      spacing between kernel elements
     * @param groups        number of blocked connections
     * @param outputPadding additional size added to one side of the output
     * @return output DArray of shape {@code (N, C_out, D_out, H_out, W_out)}
     */
    public abstract DArray<N> convTranspose3d(DArray<?> weights, DArray<?> bias, int padding, int stride, int dilation, int groups, int outputPadding);

    /**
     * Shortcut method for {@link #diag(int)} with parameter {@code 0}.
     *
     * @return matrix if input is a vector, vector if input is a matrix
     */
    public final DArray<N> diag() {
        return diag(0);
    }

    /**
     * Handles diagonal elements. The {@code diagonal} parameter indicates the diagonal. If the value is
     * {code 0}, then the main diagonal is specified. If the {code diagonal} is a positive number, then
     * the {code diagonal}-th diagonal above the main diagonal is specified. If the {code diagonal}
     * is a negative number, then the {code diagonal-th} diagonal below the main diagonal is specified.
     * <p>
     * If the input DArray is a vector, it creates a matrix with elements on the specified diagonal.
     * The resulting matrix is a square matrix with dimension size to accommodate all the elements
     * from the vector.
     * <p>
     * If the input DArray is a matrix, then the result is a vector which contains the elements from that
     * diagonal and has the size equal with the number of elements from that diagonal.
     *
     * @param diagonal number which specifies the diagonal, 0 for main one
     * @return vector or matrix, depending on input
     */
    public abstract DArray<N> diag(int diagonal);

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
        return norm(2);
    }

    public abstract N norm(double pow);

    public final DArray<N> normalize(double p) {
        return copy(Order.defaultOrder()).normalize_(p);
    }

    public final DArray<N> normalize(Order order, double p) {
        return copy(order).normalize_(p);
    }

    public abstract DArray<N> normalize_(double p);

    public final DArray<N> scatterMatrix(int ddof) {
        return scatterMatrix(Order.defaultOrder(), ddof);
    }

    public final DArray<N> scatterMatrix(Order askOrder, int ddof) {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Available only for matrices.");
        }
        if (!dt().floatingPoint()) {
            throw new OperationNotAvailableException("Available only for floating point DArrays.");
        }
        return t().mm(this, askOrder).div_(dim(0) - ddof);
    }

    public final DArray<N> cov(int ddof) {
        return cov(Order.defaultOrder(), ddof);
    }

    public final DArray<N> cov(Order askOrder, int ddof) {
        if (!isMatrix()) {
            throw new OperationNotAvailableException("Available only for matrices.");
        }
        if (!dt().floatingPoint()) {
            throw new OperationNotAvailableException("Available only for floating point DArrays.");
        }
        DArray<N> mean = mean1d(0);
        DArray<N> centered = sub(mean);
        return centered.t().mm(centered, askOrder).div_(dim(0) - ddof);
    }

    public final DArray<N> corr() {
        return corr(Order.defaultOrder());
    }

    public final DArray<N> corr(Order askOrder) {
        if (!isMatrix()) {
            throw new IllegalArgumentException("Available only for matrices.");
        }
        if (!dt().floatingPoint()) {
            throw new OperationNotAvailableException("Available only for floating point DArrays.");
        }
        DArray<N> std = std1d(0, 0);
        DArray<N> scaled = sub(mean1d(0));
        return scaled.t().mm(scaled, askOrder).div_(std).div_(std.stretch(1)).div_(dim(0));
    }

    // -- Comparison Ops -- //

    /**
     * Creates a new DArray with values sorted along the dimension given as parameters. The order is ascending or descending, given
     * as parameter.
     * <p>
     * The order of the new DArray is the default order.
     *
     * @param axis dimension along which the values will be sorted
     * @param asc  if true the values will be sorted in ascending order, otherwise in descending order
     * @return a new copy DArray with values sorted along the given dimension
     */
    public final DArray<N> sort(int axis, boolean asc) {
        return sort(Order.defaultOrder(), axis, asc);
    }

    /**
     * Creates a new DArray with values sorted along the dimension given as parameters. The order is ascending or descending, given
     * as parameter.
     * <p>
     * The order of the new DArray is the order specified as parameter.
     *
     * @param order order of the new DArray
     * @param axis  dimension along which the values will be sorted
     * @param asc   if true the values will be sorted in ascending order, otherwise in descending order
     * @return a new copy DArray with values sorted along the given dimension
     */
    public final DArray<N> sort(Order order, int axis, boolean asc) {
        return copy(order).sort_(axis, asc);
    }

    /**
     * Sort in place values along the dimension given as parameter. The order is ascending or descending, given
     * as parameter.
     *
     * @param axis dimension along which the values will be sorted
     * @param asc  if true the values will be sorted in ascending order, otherwise in descending order
     * @return same DArray instance with values sorted along the given dimension
     */
    public abstract DArray<N> sort_(int axis, boolean asc);

    /**
     * Sorts indices given as an array of parameters according to the values from flatten DArray.
     * DArray must have a single dimension with size greater than the biggest index value.
     *
     * @param indices indices which will be sorted
     * @param asc     sort ascending if true, descending otherwise
     */
    public abstract void externalSort(int[] indices, boolean asc);


    @SuppressWarnings("unchecked")
    public final <M extends Number> DArray<M> cast(DType<M> dt) {
        if (dt().equals(dt)) {
            return (DArray<M>) this;
        } else {
            return cast(dt, Order.A);
        }
    }

    public abstract <M extends Number> DArray<M> cast(DType<M> dt, Order askOrder);

    public final DArray<N> pad(int axis, int pad, int inflation) {
        return pad(axis, pad, inflation, Order.defaultOrder());
    }

    public final DArray<N> pad(int axis, int pad, int inflation, Order askOrder) {
        int[] newDims = Arrays.copyOf(dims(), rank());
        newDims[axis] = 2 * pad + (dim(axis) - 1) * inflation + 1;
        DArray<N> copy = dm.zeros(dt, Shape.of(newDims), askOrder);
        copyTo(copy.unpad(axis, pad, inflation));
        return copy;
    }

    public final DArray<N> pad(int[] pad, int[] inflation) {
        return pad(pad, inflation, Order.defaultOrder());
    }

    public final DArray<N> pad(int[] pad, int[] inflation, Order askOrder) {
        if (pad.length != inflation.length) {
            throw new IllegalArgumentException("Length of pad and length of inflation must be the same.");
        }
        if (pad.length > rank()) {
            throw new IllegalArgumentException("Length of pad and inflation must be less than or equal with darray rank.");
        }
        int[] newDims = Arrays.copyOf(dims(), rank());
        for (int i = 0; i < pad.length; i++) {
            newDims[newDims.length - i - 1] =
                    2 * pad[pad.length - i - 1] + (dim(newDims.length - i - 1) - 1) * inflation[pad.length - i - 1] + 1;
        }
        DArray<N> copy = dm.zeros(dt, Shape.of(newDims), askOrder);
        copyTo(copy.unpad(pad, inflation));
        return copy;
    }

    public final DArray<N> unpad(int axis, int pad, int inflation) {
        int[] newDims = Arrays.copyOf(dims(), rank());
        newDims[axis] = Math.ceilDiv(dim(axis) - 2 * pad - 1, inflation) + 1;
        int newOffset = ((StrideLayout) layout()).offset() + pad * stride(axis);
        int[] newStrides = Arrays.copyOf(strides(), strides().length);
        newStrides[axis] *= inflation;
        return dm.stride(dt, StrideLayout.of(newDims, newOffset, newStrides), storage);
    }

    public final DArray<N> unpad(int[] pad, int[] inflation) {
        if (pad.length != inflation.length) {
            throw new IllegalArgumentException("Length of pad and length of dilation must be the same.");
        }
        if (pad.length > rank()) {
            throw new IllegalArgumentException("Length of pad and dilation must be less than or equal with darray rank.");
        }
        DArray<N> last = this;
        for (int i = 0; i < pad.length; i++) {
            last = last.unpad(rank() - i - 1, pad[pad.length - i - 1], inflation[pad.length - i - 1]);
        }
        return last;
    }

    /**
     * Creates a copy of the original DArray with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @return new copy of the DArray
     */
    public final DArray<N> copy() {
        return copy(Order.defaultOrder());
    }

    /**
     * Creates a copy of the original DArray with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @param askOrder desired order of the copy DArray.
     * @return new copy of the DArray
     */
    public abstract DArray<N> copy(Order askOrder);

    public abstract DArray<N> copyTo(DArray<N> dst);

    public final DArray<N> reorder(Order askOrder) {
        askOrder = Order.autoFC(askOrder);
        if (layout().storageFastOrder() == askOrder) {
            return this;
        }
        return copy(askOrder);
    }

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
        if (t instanceof DArray<?> dt) {
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
