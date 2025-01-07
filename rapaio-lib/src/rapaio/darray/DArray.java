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
 * A NArray is a multidimensional array which contains elements of the same type.
 * Elements are indexed organized in zero, one or multiple dimensions.
 * <p>
 * NArrays with a low number of dimensions are known also under more specific names:
 * <ul>
 *     <li>scalar</li> an NArray with zero dimensions which contains a single element
 *     <li>vector</li> an NArray with one dimension
 *     <li>matrix</li> an NArray with two dimensions
 * </ul>
 * <p>
 * The type of data elements from an NArray is marked as a generic data type and also described by {@link #dt()}.
 * <p>
 * An NArray is created by a factory which implements {@link DArrayManager}. Each NArray provides a link towards the manager
 * which created it through {@link #manager()}.
 * <p>
 * The elements are logically organized like a hyper cube with a given number of dimensions {@link #rank()}. The size of each
 * dimension is described by a {@link Shape} object and the {@link Layout} describes how the details related
 * with how the elements' indexing.
 * The implemented layout is a stride array layout provided by {@link StrideLayout}, but
 * other layouts could be implemented (for example for special matrices or for sparse formats).
 *
 * @param <N> Generic data type which can be Byte, Integer, Float or Double.
 */
public abstract sealed class DArray<N extends Number> implements Printable, Iterable<N> permits AbstractStrideDArray {

    protected final DArrayManager manager;
    protected final Storage storage;

    protected DArray(DArrayManager manager, Storage storage) {
        this.manager = manager;
        this.storage = storage;
    }

    /**
     * NArray manager which created this NArray instance.
     */
    public final DArrayManager manager() {
        return manager;
    }

    /**
     * {@link DType} describes the data type of the elements contained by the NArray and provides also related utilities like value
     * casting.
     *
     * @return NArray data type
     */
    public abstract DType<N> dt();

    /**
     * NArray layout contains the complete information about logical layout of data elements in storage memory.
     *
     * @return NArray layout
     */
    public abstract Layout layout();

    /**
     * Shape describes the number of dimensions and the size on each dimension of the multidimensional elements.
     *
     * @return NArray shape
     */
    public final Shape shape() {
        return layout().shape();
    }

    /**
     * Rank is the number of dimensions for the NArray.
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
     * Size of the dimension
     *
     * @param axis the index of that dimension
     * @return size of the dimension for the given {@code axis}
     */
    public final int dim(int axis) {
        return shape().dim(axis);
    }

    /**
     * Size of an NArray is the number of elements contained in NArray and is equal with
     * the product of dimension's sizes
     *
     * @return number of elements from NArray
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
     * A scalar is an NArray with no dimensions.
     *
     * @return true if the rank of NArray is 0
     */
    public final boolean isScalar() {
        return rank() == 0;
    }

    /**
     * A vector is an NArray with one dimension.
     *
     * @return true if the rank of the NArray is 1
     */
    public final boolean isVector() {
        return rank() == 1;
    }

    /**
     * A matrix is an NArray with two dimensions.
     *
     * @return true if the rank of the NArray is 2
     */
    public final boolean isMatrix() {
        return rank() == 2;
    }

    /**
     * Creates a new NArray with a different shape. If possible, the data will not be copied.
     * If data is copied, the result will be a dense NArray of default order.
     * <p>
     * In order to reshape an NArray, the source shape and destination shape must have the same size.
     * <p>
     * The order in which elements are read is {@code C} if data is stored in C order, {@code F} if data is stored
     * in F order, and default for the other cases.
     *
     * @param shape destination shape
     * @return new NArray instance, wrapping, if possible, the data from the old NArray.
     * @see DArray#reshape(Shape, Order)
     */
    public final DArray<N> reshape(Shape shape) {
        return reshape(shape, Order.A);
    }

    /**
     * Creates a new NArray with a different shape. If possible, the data will not be copied.
     * <p>
     * In order to reshape an NArray, the source shape and destination shape must have the same size.
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
     * If a new copy is created, that will also be the order in which new NArray copy will store data
     *
     * @param shape    destination shape
     * @param askOrder destination order, if the data will be copied, otherwise the parameter is ignored.
     * @return new NArray instance, wrapping, if possible, the data from the old NArray.
     */
    public abstract DArray<N> reshape(Shape shape, Order askOrder);

    /**
     * Creates a new transposed NArray. Data will be copied and stored with default order.
     *
     * @return copy of the transposed vector
     */
    public final DArray<N> t() {
        return t(Order.defaultOrder());
    }

    /**
     * Creates a new transposed NArray. Data will be stored in the specified order given as parameter.
     * <p>
     * The only accepted orders are C order and F order.
     *
     * @param askOrder storage order
     * @return copy of the transposed vector
     */
    public final DArray<N> t(Order askOrder) {
        return t_().copy(askOrder);
    }

    /**
     * Transpose of an NArray. A transposed NArray is an NArray which reverts axis, the first axis becomes the last,
     * the second axis becomes the second to last and so on.
     * <p>
     * Data storage remain the same, no new storage copy is created.
     * As such, any modification on a transposed NArray will affect the original NArray.
     *
     * @return a transposed view of the NArray
     */
    public abstract DArray<N> t_();

    /**
     * Collapses the NArray into one dimension using the default order. The order is used for reading. In the case when a view
     * can't be created, a new NArray will be created with the storage order same as reading order.
     *
     * @return an NArray with elements in given order (new copy if needed)
     */
    public final DArray<N> ravel() {
        return ravel(Order.defaultOrder());
    }

    /**
     * Collapses the NArray into one dimension using the given order. The order is used for reading. In the case when a view
     * can't be created, a new NArray will be created with the storage order same as reading order.
     *
     * @param askOrder order of the elements
     * @return an NArray with elements in given order (new copy if needed)
     */
    public abstract DArray<N> ravel(Order askOrder);

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is the default order.
     *
     * @return a copy of the NArray with elements in asked order.
     */
    public final DArray<N> flatten() {
        return flatten(Order.defaultOrder());
    }

    /**
     * Creates a copy of the array, flattened into one dimension. The order of the elements is given as parameter.
     *
     * @param askOrder order of the elements
     * @return a copy of the NArray with elements in asked order.
     */
    public abstract DArray<N> flatten(Order askOrder);

    /**
     * Collapses the given axes if are of dimension one. This operation does not create a new copy of the data.
     * If any dimension doesn't have size one, the dimension will remain as it is.
     *
     * @return view of the same NArray with the given dimensions equal with one collapsed
     */
    public abstract DArray<N> squeeze(int... axes);

    /**
     * Creates a new NArray view with an additional dimensions at the position specified by {@param axes}.
     * Specified axes value should be between 0 (inclusive) and the number of dimensions plus the number of added axes (exclusive).
     *
     * @param axes indexes of the axes to be added
     * @return new view NArray with added axes
     */
    public abstract DArray<N> stretch(int... axes);

    /**
     * Creates a new NArray by repeating values along a given dimension of size 1. This operation is
     * similar with repeating values, with the difference that the resulting NArray will be a view over the same data,
     * thus avoiding copying data. This is possible if the corresponding stride is set to 0 and the corresponding original
     * dimension has size 1.
     *
     * @param axis specified dimension
     * @param dim  new size of the dimension, which is equivalent with how many times the values are repeated
     * @return new view over the original NArray with repeated data along a given dimension
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
     * Creates an NArray view with dimensions permuted in the order specified in parameter. The
     * parameter is an integer array containing all values from closed interval {@code [0,(rank-1)]}.
     * The order in which those values are passed defined the dimension permutation.
     *
     * @param dims dimension permutation
     * @return new NArray view with permuted dimensions
     */
    public abstract DArray<N> permute(int... dims);

    /**
     * Creates a new NArray view with source axis moved into the given destination position.
     *
     * @param src source axis
     * @param dst destination axis position
     * @return new view NArray with moved axis
     */
    public abstract DArray<N> moveAxis(int src, int dst);

    /**
     * Swap two axis. This does not affect the storage.
     *
     * @param src source axis
     * @param dst destination axis
     * @return new view NArray with swapped axis
     */
    public abstract DArray<N> swapAxis(int src, int dst);

    /**
     * Creates a new NArray view with one truncated axis, all other axes remain the same.
     *
     * @param axis  axis to be truncated
     * @param start start index inclusive
     * @param end   end index exclusive
     * @return new view NArray with truncated axis
     */
    public final DArray<N> narrow(int axis, int start, int end) {
        return narrow(axis, true, start, end);
    }

    /**
     * Creates a new NArray view with one truncated axis, all other axes remain the same.
     *
     * @param axis    axis to be truncated
     * @param keepDim keep dimension or not
     * @param start   start index inclusive
     * @param end     end index exclusive
     * @return new view NArray with truncated axis
     */
    public abstract DArray<N> narrow(int axis, boolean keepDim, int start, int end);

    /**
     * Creates a new NArray view with possibly all truncated axes.
     *
     * @param keepDim keep dimensions even if some of have length 1, false otherwise
     * @param starts  vector of indexes where narrow interval starts
     * @param ends    vector of indexes where narrow interval ends
     * @return a view with truncated axes
     */
    public abstract DArray<N> narrowAll(boolean keepDim, int[] starts, int[] ends);

    /**
     * Splits the NArray into multiple view NArrays along a given axis. The resulting NArrays are narrowed versions of the original NArray,
     * with the start index being the current index, and the end being the next index or the end of the dimension.
     *
     * @param axis    axis to split along
     * @param indexes indexes to split along, being start indexes for truncation
     * @return list of new NArrays with truncated data.
     */
    public abstract List<DArray<N>> split(int axis, boolean keepDim, int... indexes);

    /**
     * Splits the NArray into multiple view NArrays along all axes. The resulting NArrays are narrowed versions of the original NArrays,
     * having for each dimension the start index being the current index in that dimension, and the end index being the next index in
     * that dimension. The indices are given as an array of arrays with length equal with number of axes, and for each sub array the
     * split indexes specified.
     *
     * @param keepDim keep original dimensions even if some dimensions have size 1, false otherwise
     * @param indexes array of arrays of indices
     * @return list of new NArrays with truncated axes
     */
    public abstract List<DArray<N>> splitAll(boolean keepDim, int[][] indexes);

    /**
     * Slices the narray along a given axis.
     * The resulting narrays are narrowed versions of the original one with size given by step.
     * The last narray in list might have lesser dimension size if step does not divide dimension size.
     * It also may return fewer chunks if the number of requested chunks is greater than the dimension of the axis.
     * The resulting NArrays are views over the original one.
     *
     * @param axis axis to slice along
     * @param step step size
     * @return list of new NArrays with truncated data.
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
     * Slices the n-array along all dimensions.
     * The resulting n-arrays are truncated versions of the original with sizes in each dimensions given by steps.
     * The last n-array might have dimensions lesser than steps if the original dimension does not divide exactly at step.
     * The resulting NArrays are views over the original one.
     *
     * @param keepDim keep the original dimensions even if those have dimensions of size 1, remove them otherwise
     * @param steps   array of steps, one step for each dimension
     * @return list of NArrays with truncated data
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
     * Creates a new NArray by stacking or concatenating this NArray multiple times along a given axis.
     * <p>
     * The resulting NArray will be stored in default order.
     *
     * @param axis   the axis which will be repeated
     * @param repeat the number of repetitions
     * @param stack  stack NArrays if true, concatenate if false
     * @return NArray with repeated values along given axis
     */
    public final DArray<N> repeat(int axis, int repeat, boolean stack) {
        return repeat(Order.defaultOrder(), axis, repeat, stack);
    }

    public final DArray<N> repeat(Order order, int axis, int repeat, boolean stack) {
        List<DArray<N>> copies = new ArrayList<>(repeat);
        for (int i = 0; i < repeat; i++) {
            copies.add(this);
        }
        if (stack) {
            return manager.stack(dt(), order, axis, copies);
        } else {
            return manager.cat(dt(), order, axis, copies);
        }
    }

    /**
     * Take values along a given axis from specified indices. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting NArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the default order.
     *
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return NArray with mapped values along the given dimension
     */
    public final DArray<N> sel(int axis, int... indices) {
        return sel(Order.defaultOrder(), axis, indices);
    }

    /**
     * Take values along a given axis from specified indices. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting NArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the specified order.
     *
     * @param order   storage order if new data copy is required, ignored otherwise
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return NArray with mapped values along the given dimension
     */
    public abstract DArray<N> sel(Order order, int axis, int... indices);

    /**
     * Takes values along a given axis from the specified indices and squeeze the given axis if a single index is requested. For example,
     * one can take a single row from a matrix NArray and the resulting NArray will have a single dimension, aka the resulting
     * NArray will be a vector. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting NArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the default order.
     *
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return NArray with mapped values along the given dimension
     */
    public final DArray<N> selsq(int axis, int... indices) {
        return selsq(Order.defaultOrder(), axis, indices);
    }

    /**
     * Takes values along a given axis from the specified indices and squeeze the given axis if a single index is requested. For example,
     * one can take a single row from a matrix NArray and the resulting NArray will have a single dimension, aka the resulting
     * NArray will be a vector. This operation will create a view when is possible, otherwise will create
     * a new copy of data. The indices value can be repeated or specified in any order as long as there are integer values in range
     * {@code 0} inclusive and {@code dim(axis)} exclusive.
     * <p>
     * The resulting NArray will have the dimension specified by axis of size equal with the length of indices.
     * <p>
     * If a new copy is required, the storage order is the order specified by parameter.
     *
     * @param order   order specified for the new NArray, if a copy of the data is required
     * @param axis    specified axis
     * @param indices indices of the taken values along the specified axis
     * @return NArray with mapped values along the given dimension
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
     * @return NArray with removes values along the given dimension
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
     * @return NArray with removed values along the given dimension
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
     * @return squeezed NArray with removed values along the given dimension
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
     * @return squeezed NArray with removed values along the given dimension
     */
    public final DArray<N> remsq(Order order, int axis, int... indices) {
        return rem(order, axis, indices).squeeze(axis);
    }

    public final DArray<N> gather(int axis, DArray<?> index) {
        return gather(axis, index, Order.defaultOrder());
    }

    public final DArray<N> gather(int axis, DArray<?> index, Order askOrder) {
        return manager.zeros(dt(), index.shape(), askOrder).gather_(axis, index, this);
    }

    public abstract DArray<N> gather_(int axis, DArray<?> index, DArray<?> input);

    public final DArray<?> scatter(int axis, DArray<?> index, DArray<?> out) {
        return out.scatter_(axis, index, this);
    }

    public abstract DArray<?> scatter_(int axis, DArray<?> index, DArray<?> input);

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

    public final void ptrIncByte(int ptr, byte value) {
        storage.incByte(ptr, value);
    }

    public final void ptrIncInt(int ptr, int value) {
        storage.incInt(ptr, value);
    }

    public final void ptrIncFloat(int ptr, float value) {
        storage.incFloat(ptr, value);
    }

    public final void ptrIncDouble(int ptr, double value) {
        storage.incDouble(ptr, value);
    }

    /**
     * Produces an iterator over the values from this NArray in the
     * storage order.
     *
     * @return value iterator
     */
    public final Iterator<N> iterator() {
        return iterator(Order.S);
    }

    public abstract Iterator<N> iterator(Order askOrder);

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
     * Creates a new NArray in the default storage order, having as values the result of
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
     * Creates a new NArray in the order determined by parameter, having as values the result of
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
     * Changes values from NArray in the default order, having as values the result of
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
     * Changes values from NArray in the order specified by parameter, having as values the result of
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

    public final DArray<N> apply(Function<N, N> fun) {
        return apply(Order.defaultOrder(), fun);
    }

    public final DArray<N> apply(Order askOrder, Function<N, N> fun) {
        return copy(askOrder).apply_(fun);
    }

    public abstract DArray<N> apply_(Function<N, N> fun);

    //--------- UNARY OPERATIONS ----------------//

    public final DArray<N> unary(DArrayUnaryOp op) {
        return copy(Order.defaultOrder()).unary_(op);
    }

    public final DArray<N> unary(DArrayUnaryOp op, Order order) {
        return copy(order).unary_(op);
    }

    public abstract DArray<N> unary_(DArrayUnaryOp op);

    public final DArray<N> unary1d(DArrayUnaryOp op, int axis) {
        return copy(Order.defaultOrder()).unary1d_(op, axis);
    }

    public final DArray<N> unary1d(DArrayUnaryOp op, int axis, Order order) {
        return copy(order).unary1d_(op, axis);
    }

    public abstract DArray<N> unary1d_(DArrayUnaryOp op, int axis);

    public final DArray<N> fill_(N value) {
        return unary_(DArrayOp.unaryFill(value));
    }

    public final DArray<N> fill_(int value) {
        return unary_(DArrayOp.unaryFill(value));
    }

    public final DArray<N> fill_(double value) {
        return unary_(DArrayOp.unaryFill(value));
    }

    public final DArray<N> fillNan_(N value) {
        return unary_(DArrayOp.unaryFillNan(value));
    }

    public final DArray<N> fillNan_(int value) {
        return unary_(DArrayOp.unaryFillNan(value));
    }

    public final DArray<N> fillNan_(double value) {
        return unary_(DArrayOp.unaryFillNan(value));
    }

    public final DArray<N> nanToNum_(N fill) {
        return unary_(DArrayOp.unaryNanToNum(fill, fill, fill));
    }

    public final DArray<N> nanToNum_(int fill) {
        return unary_(DArrayOp.unaryNanToNum(fill, fill, fill));
    }

    public final DArray<N> nanToNum_(double fill) {
        return unary_(DArrayOp.unaryNanToNum(fill, fill, fill));
    }

    public final DArray<N> nanToNum_(N nan, N negInf, N posInf) {
        return unary_(DArrayOp.unaryNanToNum(nan, negInf, posInf));
    }

    public final DArray<N> nanToNum_(int nan, int negInf, int posInf) {
        return unary_(DArrayOp.unaryNanToNum(nan, negInf, posInf));
    }

    public final DArray<N> nanToNum_(double nan, double negInf, double posInf) {
        return unary_(DArrayOp.unaryNanToNum(nan, negInf, posInf));
    }

    public final DArray<N> compareMask_(Compare cmp, N value) {
        return unary_(DArrayOp.unaryOpCompareMask(cmp, value));
    }

    public final DArray<N> compareMask_(Compare cmp, int value) {
        return unary_(DArrayOp.unaryOpCompareMask(cmp, value));
    }

    public final DArray<N> compareMask_(Compare cmp, double value) {
        return unary_(DArrayOp.unaryOpCompareMask(cmp, value));
    }

    public final DArray<N> clamp(N min, N max) {
        return unary(DArrayOp.unaryClamp(dt(), min, max));
    }

    public final DArray<N> clamp(int min, int max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)));
    }

    public final DArray<N> clamp(double min, double max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)));
    }

    public final DArray<N> clamp(Order order, N min, N max) {
        return unary(DArrayOp.unaryClamp(dt(), min, max), order);
    }

    public final DArray<N> clamp(Order order, int min, int max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)), order);
    }

    public final DArray<N> clamp(Order order, double min, double max) {
        return unary(DArrayOp.unaryClamp(dt(), dt().cast(min), dt().cast(max)), order);
    }

    public final DArray<N> clamp_(N min, N max) {
        return unary_(DArrayOp.unaryClamp(dt(), min, max));
    }

    public final DArray<N> rint() {
        return unary(DArrayOp.unaryRint());
    }

    public final DArray<N> rint(Order order) {
        return unary(DArrayOp.unaryRint(), order);
    }

    public final DArray<N> rint_() {
        return unary_(DArrayOp.unaryRint());
    }

    public final DArray<N> ceil() {
        return unary(DArrayOp.unaryCeil());
    }

    public final DArray<N> ceil(Order order) {
        return unary(DArrayOp.unaryCeil(), order);
    }

    public final DArray<N> ceil_() {
        return unary_(DArrayOp.unaryCeil());
    }

    public final DArray<N> floor() {
        return unary(DArrayOp.unaryFloor());
    }

    public final DArray<N> floor(Order order) {
        return unary(DArrayOp.unaryFloor(), order);
    }

    public final DArray<N> floor_() {
        return unary_(DArrayOp.unaryFloor());
    }

    public final DArray<N> abs() {
        return unary(DArrayOp.unaryAbs());
    }

    public final DArray<N> abs(Order order) {
        return unary(DArrayOp.unaryAbs(), order);
    }

    public final DArray<N> abs_() {
        return unary_(DArrayOp.unaryAbs());
    }

    public final DArray<N> neg() {
        return unary(DArrayOp.unaryNeg());
    }

    public final DArray<N> neg(Order order) {
        return unary(DArrayOp.unaryNeg(), order);
    }

    public final DArray<N> neg_() {
        return unary_(DArrayOp.unaryNeg());
    }

    public final DArray<N> log() {
        return unary(DArrayOp.unaryLog());
    }

    public final DArray<N> log(Order order) {
        return unary(DArrayOp.unaryLog(), order);
    }

    public final DArray<N> log_() {
        return unary_(DArrayOp.unaryLog());
    }

    public final DArray<N> log1p() {
        return unary(DArrayOp.unaryLog1p());
    }

    public final DArray<N> log1p(Order order) {
        return unary(DArrayOp.unaryLog1p(), order);
    }

    public final DArray<N> log1p_() {
        return unary_(DArrayOp.unaryLog1p());
    }

    public final DArray<N> exp() {
        return unary(DArrayOp.unaryExp());
    }

    public final DArray<N> exp(Order order) {
        return unary(DArrayOp.unaryExp(), order);
    }

    public final DArray<N> exp_() {
        return unary_(DArrayOp.unaryExp());
    }

    public final DArray<N> expm1() {
        return unary(DArrayOp.unaryExpm1());
    }

    public final DArray<N> expm1(Order order) {
        return unary(DArrayOp.unaryExpm1(), order);
    }

    public final DArray<N> expm1_() {
        return unary_(DArrayOp.unaryExpm1());
    }

    public final DArray<N> sin() {
        return unary(DArrayOp.unarySin());
    }

    public final DArray<N> sin(Order order) {
        return unary(DArrayOp.unarySin(), order);
    }

    public final DArray<N> sin_() {
        return unary_(DArrayOp.unarySin());
    }

    public final DArray<N> asin() {
        return unary(DArrayOp.unaryAsin());
    }

    public final DArray<N> asin(Order order) {
        return unary(DArrayOp.unaryAsin(), order);
    }

    public final DArray<N> asin_() {
        return unary_(DArrayOp.unaryAsin());
    }

    public final DArray<N> sinh() {
        return unary(DArrayOp.unarySinh());
    }

    public final DArray<N> sinh(Order order) {
        return unary(DArrayOp.unarySinh(), order);
    }

    public final DArray<N> sinh_() {
        return unary_(DArrayOp.unarySinh());
    }

    public final DArray<N> cos() {
        return unary(DArrayOp.unaryCos());
    }

    public final DArray<N> cos(Order order) {
        return unary(DArrayOp.unaryCos(), order);
    }

    public final DArray<N> cos_() {
        return unary_(DArrayOp.unaryCos());
    }

    public final DArray<N> acos() {
        return unary(DArrayOp.unaryAcos());
    }

    public final DArray<N> acos(Order order) {
        return unary(DArrayOp.unaryAcos(), order);
    }

    public final DArray<N> acos_() {
        return unary_(DArrayOp.unaryAcos());
    }

    public final DArray<N> cosh() {
        return unary(DArrayOp.unaryCosh());
    }

    public final DArray<N> cosh(Order order) {
        return unary(DArrayOp.unaryCosh(), order);
    }

    public final DArray<N> cosh_() {
        return unary_(DArrayOp.unaryCosh());
    }

    public final DArray<N> tan() {
        return unary(DArrayOp.unaryTan());
    }

    public final DArray<N> tan(Order order) {
        return unary(DArrayOp.unaryTan(), order);
    }

    public final DArray<N> tan_() {
        return unary_(DArrayOp.unaryTan());
    }

    public final DArray<N> atan() {
        return unary(DArrayOp.unaryAtan());
    }

    public final DArray<N> atan(Order order) {
        return unary(DArrayOp.unaryAtan(), order);
    }

    public final DArray<N> atan_() {
        return unary_(DArrayOp.unaryAtan());
    }

    public final DArray<N> tanh() {
        return unary(DArrayOp.unaryTanh());
    }

    public final DArray<N> tanh(Order order) {
        return unary(DArrayOp.unaryTanh(), order);
    }

    public final DArray<N> tanh_() {
        return unary_(DArrayOp.unaryTanh());
    }

    public final DArray<N> sqr() {
        return unary(DArrayOp.unarySqr());
    }

    public final DArray<N> sqr(Order order) {
        return unary(DArrayOp.unarySqr(), order);
    }

    public final DArray<N> sqr_() {
        return unary_(DArrayOp.unarySqr());
    }

    public final DArray<N> sqrt() {
        return unary(DArrayOp.unarySqrt());
    }

    public final DArray<N> sqrt(Order order) {
        return unary(DArrayOp.unarySqrt(), order);
    }

    public final DArray<N> sqrt_() {
        return unary_(DArrayOp.unarySqrt());
    }

    public final DArray<N> pow(double power) {
        return unary(DArrayOp.unaryPow(power));
    }

    public final DArray<N> pow(Order order, double power) {
        return unary(DArrayOp.unaryPow(power), order);
    }

    public final DArray<N> pow_(double power) {
        return unary_(DArrayOp.unaryPow(power));
    }

    public final DArray<N> sigmoid() {
        return unary(DArrayOp.unarySigmoid());
    }

    public final DArray<N> sigmoid(Order order) {
        return unary(DArrayOp.unarySigmoid(), order);
    }

    public final DArray<N> sigmoid_() {
        return unary_(DArrayOp.unarySigmoid());
    }

    public final DArray<N> softmax() {
        return unary(DArrayOp.unarySoftmax());
    }

    public final DArray<N> softmax(Order order) {
        return unary(DArrayOp.unarySoftmax(), order);
    }

    public final DArray<N> softmax_() {
        return unary_(DArrayOp.unarySoftmax());
    }

    public final DArray<N> softmax1d(int axis) {
        return softmax1d(axis, Order.defaultOrder());
    }

    public final DArray<N> softmax1d(int axis, Order askOrder) {
        return copy(askOrder).softmax1d_(axis);
    }

    public final DArray<N> softmax1d_(int axis) {
        return unary1d_(DArrayOp.unarySoftmax(), axis);
    }

    public final DArray<N> logsoftmax() {
        return unary(DArrayOp.unaryLogSoftmax());
    }

    public final DArray<N> logsoftmax(Order order) {
        return unary(DArrayOp.unaryLogSoftmax(), order);
    }

    public final DArray<N> logsoftmax_() {
        return unary_(DArrayOp.unaryLogSoftmax());
    }

    public final DArray<N> logsoftmax1d(int axis) {
        return logsoftmax1d(axis, Order.defaultOrder());
    }

    public final DArray<N> logsoftmax1d(int axis, Order askOrder) {
        return copy(askOrder).logsoftmax1d_(axis);
    }

    public final DArray<N> logsoftmax1d_(int axis) {
        return unary1d_(DArrayOp.unaryLogSoftmax(), axis);
    }


    //--------- BINARY OPERATIONS ----------------//

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

    public abstract DArray<N> binary_(DArrayBinaryOp op, DArray<?> value);

    public final <M extends Number> DArray<N> binary(DArrayBinaryOp op, M value, Order order) {
        return copy(order).binary_(op, value);
    }

    public abstract <M extends Number> DArray<N> binary_(DArrayBinaryOp op, M value);

    public final DArray<N> add(DArray<?> array) {
        return binary(DArrayOp.binaryAdd(), array, Order.defaultOrder());
    }

    public final DArray<N> add(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryAdd(), array, order);
    }

    public final DArray<N> add_(DArray<?> array) {
        return binary_(DArrayOp.binaryAdd(), array);
    }

    public final DArray<N> sub(DArray<?> array) {
        return binary(DArrayOp.binarySub(), array, Order.defaultOrder());
    }

    public final DArray<N> sub(DArray<?> array, Order order) {
        return binary(DArrayOp.binarySub(), array, order);
    }

    public final DArray<N> sub_(DArray<?> array) {
        return binary_(DArrayOp.binarySub(), array);
    }

    public final DArray<N> mul(DArray<?> array) {
        return binary(DArrayOp.binaryMul(), array, Order.defaultOrder());
    }

    public final DArray<N> mul(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryMul(), array, order);
    }

    public final DArray<N> mul_(DArray<?> array) {
        return binary_(DArrayOp.binaryMul(), array);
    }

    public final DArray<N> div(DArray<?> array) {
        return binary(DArrayOp.binaryDiv(), array, Order.defaultOrder());
    }

    public final DArray<N> div(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryDiv(), array, order);
    }

    public final DArray<N> div_(DArray<?> array) {
        return binary_(DArrayOp.binaryDiv(), array);
    }

    public final DArray<N> min(DArray<?> array) {
        return binary(DArrayOp.binaryMin(), array, Order.defaultOrder());
    }

    public final DArray<N> min(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryMin(), array, order);
    }

    public final DArray<N> min_(DArray<?> array) {
        return binary_(DArrayOp.binaryMin(), array);
    }

    public final DArray<N> max(DArray<?> array) {
        return binary(DArrayOp.binaryMax(), array, Order.defaultOrder());
    }

    public final DArray<N> max(DArray<?> array, Order order) {
        return binary(DArrayOp.binaryMax(), array, order);
    }

    public final DArray<N> max_(DArray<?> array) {
        return binary_(DArrayOp.binaryMax(), array);
    }

    public final DArray<N> add(int value) {
        return binary(DArrayOp.binaryAdd(), value, Order.defaultOrder());
    }

    public final DArray<N> add(double value) {
        return binary(DArrayOp.binaryAdd(), value, Order.defaultOrder());
    }

    public final DArray<N> add(int value, Order order) {
        return binary(DArrayOp.binaryAdd(), value, order);
    }

    public final DArray<N> add(double value, Order order) {
        return binary(DArrayOp.binaryAdd(), value, order);
    }

    public final DArray<N> add_(int value) {
        return binary_(DArrayOp.binaryAdd(), value);
    }

    public final DArray<N> add_(double value) {
        return binary_(DArrayOp.binaryAdd(), value);
    }

    public final DArray<N> sub(int value) {
        return binary(DArrayOp.binarySub(), value, Order.defaultOrder());
    }

    public final DArray<N> sub(double value) {
        return binary(DArrayOp.binarySub(), value, Order.defaultOrder());
    }

    public final DArray<N> sub(int value, Order order) {
        return binary(DArrayOp.binarySub(), value, order);
    }

    public final DArray<N> sub(double value, Order order) {
        return binary(DArrayOp.binarySub(), value, order);
    }

    public final DArray<N> sub_(int value) {
        return binary_(DArrayOp.binarySub(), value);
    }

    public final DArray<N> sub_(double value) {
        return binary_(DArrayOp.binarySub(), value);
    }

    public final DArray<N> mul(int value) {
        return binary(DArrayOp.binaryMul(), value, Order.defaultOrder());
    }

    public final DArray<N> mul(double value) {
        return binary(DArrayOp.binaryMul(), value, Order.defaultOrder());
    }

    public final DArray<N> mul(int value, Order order) {
        return binary(DArrayOp.binaryMul(), value, order);
    }

    public final DArray<N> mul(double value, Order order) {
        return binary(DArrayOp.binaryMul(), value, order);
    }

    public final DArray<N> mul_(int value) {
        return binary_(DArrayOp.binaryMul(), value);
    }

    public final DArray<N> mul_(double value) {
        return binary_(DArrayOp.binaryMul(), value);
    }

    public final DArray<N> div(int value) {
        return binary(DArrayOp.binaryDiv(), value, Order.defaultOrder());
    }

    public final DArray<N> div(double value) {
        return binary(DArrayOp.binaryDiv(), value, Order.defaultOrder());
    }

    public final DArray<N> div(int value, Order order) {
        return binary(DArrayOp.binaryDiv(), value, order);
    }

    public final DArray<N> div(double value, Order order) {
        return binary(DArrayOp.binaryDiv(), value, order);
    }

    public final DArray<N> div_(int value) {
        return binary_(DArrayOp.binaryDiv(), value);
    }

    public final DArray<N> div_(double value) {
        return binary_(DArrayOp.binaryDiv(), value);
    }

    public final DArray<N> min(int value) {
        return binary(DArrayOp.binaryMin(), value, Order.defaultOrder());
    }

    public final DArray<N> min(double value) {
        return binary(DArrayOp.binaryMin(), value, Order.defaultOrder());
    }

    public final DArray<N> min(int value, Order order) {
        return binary(DArrayOp.binaryMin(), value, order);
    }

    public final DArray<N> min(double value, Order order) {
        return binary(DArrayOp.binaryMin(), value, order);
    }

    public final DArray<N> min_(int value) {
        return binary_(DArrayOp.binaryMin(), value);
    }

    public final DArray<N> min_(double value) {
        return binary_(DArrayOp.binaryMin(), value);
    }

    public final DArray<N> max(int value) {
        return binary(DArrayOp.binaryMax(), value, Order.defaultOrder());
    }

    public final DArray<N> max(double value) {
        return binary(DArrayOp.binaryMax(), value, Order.defaultOrder());
    }

    public final DArray<N> max(int value, Order order) {
        return binary(DArrayOp.binaryMax(), value, order);
    }

    public final DArray<N> max(double value, Order order) {
        return binary(DArrayOp.binaryMax(), value, order);
    }

    public final DArray<N> max_(int value) {
        return binary_(DArrayOp.binaryMax(), value);
    }

    public final DArray<N> max_(double value) {
        return binary_(DArrayOp.binaryMax(), value);
    }

    public final DArray<N> fma(int a, DArray<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    public final DArray<N> fma(double a, DArray<?> t) {
        return fma(a, t, Order.defaultOrder());
    }

    public final DArray<N> fma(int a, DArray<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    public final DArray<N> fma(double a, DArray<?> t, Order order) {
        return copy(order).fma_(a, t);
    }

    /**
     * Adds in place the given matrix {@code t} multiplied by {@code factor} to the NArray element wise.
     *
     * @param factor multiplication factor
     * @param t      NArray to be multiplied and added to the current one
     * @return same NArray with values changed
     */
    public abstract DArray<N> fma_(N factor, DArray<?> t);

    public final DArray<N> fma_(int factor, DArray<?> t) {
        return fma_(dt().cast(factor), t);
    }

    public final DArray<N> fma_(double factor, DArray<?> t) {
        return fma_(dt().cast(factor), t);
    }

    //--------- REDUCE OPERATIONS ----------------//

    public abstract N reduce(DArrayReduceOp op);

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

    public final N sum() {
        return reduce(DArrayOp.reduceSum());
    }

    public final DArray<N> sum1d(int axis) {
        return reduce1d(DArrayOp.reduceSum(), axis, Order.defaultOrder());
    }

    public final DArray<N> sum1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceSum(), axis, order);
    }

    public final DArray<N> sumOn(Shape shape, boolean keepDim) {
        return reduceOn(DArrayOp.reduceSum(), shape, keepDim, Order.defaultOrder());
    }

    public final DArray<N> sumOn(Shape shape, boolean keepDim, Order order) {
        return reduceOn(DArrayOp.reduceSum(), shape, keepDim, order);
    }

    public final DArray<N> sumTo(Shape shape, boolean keepDim) {
        return reduceTo(DArrayOp.reduceSum(), shape, keepDim, Order.defaultOrder());
    }

    public final DArray<N> sumTo(Shape shape, boolean keepDim, Order order) {
        return reduceTo(DArrayOp.reduceSum(), shape, keepDim, order);
    }

    public final N nanSum() {
        return reduce(DArrayOp.reduceNanSum());
    }

    public final DArray<N> nanSum1d(int axis) {
        return reduce1d(DArrayOp.reduceNanSum(), axis, Order.defaultOrder());
    }

    public final DArray<N> nanSum1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanSum(), axis, order);
    }

    public final N prod() {
        return reduce(DArrayOp.reduceProd());
    }

    public final DArray<N> prod1d(int axis) {
        return reduce1d(DArrayOp.reduceProd(), axis, Order.defaultOrder());
    }

    public final DArray<N> prod1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceProd(), axis, order);
    }

    public final N nanProd() {
        return reduce(DArrayOp.reduceNanProd());
    }

    public final DArray<N> nanProd1d(int axis) {
        return reduce1d(DArrayOp.reduceNanProd(), axis, Order.defaultOrder());
    }

    public final DArray<N> nanProd1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanProd(), axis, order);
    }

    public final N amax() {
        return reduce(DArrayOp.reduceMax());
    }

    public final DArray<N> amax1d(int axis) {
        return amax1d(axis, Order.defaultOrder());
    }

    public final DArray<N> amax1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceMax(), axis, order);
    }

    public final N nanMax() {
        return reduce(DArrayOp.reduceNanMax());
    }

    public final DArray<N> nanMax1d(int axis) {
        return reduce1d(DArrayOp.reduceNanMax(), axis, Order.defaultOrder());
    }

    public final DArray<N> nanMax1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanMax(), axis, order);
    }

    public final N amin() {
        return reduce(DArrayOp.reduceMin());
    }

    public final DArray<N> amin1d(int axis) {
        return reduce1d(DArrayOp.reduceMin(), axis, Order.defaultOrder());
    }

    public final DArray<N> amin1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceMin(), axis, order);
    }

    public final N nanMin() {
        return reduce(DArrayOp.reduceNanMin());
    }

    public final DArray<N> nanMin1d(int axis) {
        return reduce1d(DArrayOp.reduceNanMin(), axis, Order.defaultOrder());
    }

    public final DArray<N> nanMin1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceNanMin(), axis, order);
    }

    public final N mean() {
        return reduce(DArrayOp.reduceMean());
    }

    public final DArray<N> mean1d(int axis) {
        return reduce1d(DArrayOp.reduceMean(), axis, Order.defaultOrder());
    }

    public final DArray<N> mean1d(int axis, Order order) {
        return reduce1d(DArrayOp.reduceMean(), axis, order);
    }

    public final DArray<N> meanOn(Shape shape, boolean keepDim) {
        return reduceOn(DArrayOp.reduceMean(), shape, keepDim, Order.defaultOrder());
    }

    public final DArray<N> meanOn(Shape shape, boolean keepDim, Order order) {
        return reduceOn(DArrayOp.reduceMean(), shape, keepDim, order);
    }

    public final DArray<N> meanAt(Shape shape, boolean keepDim) {
        return reduceTo(DArrayOp.reduceMean(), shape, keepDim, Order.defaultOrder());
    }

    public final DArray<N> meanAt(Shape shape, boolean keepDim, Order order) {
        return reduceTo(DArrayOp.reduceMean(), shape, keepDim, order);
    }

    public final N nanMean() {
        return reduce(DArrayOp.reduceNanMean());
    }

    public final N var(int ddof) {
        return reduce(DArrayOp.reduceVarc(ddof));
    }

    public final DArray<N> var1d(int axis, int ddof) {
        return reduce1d(DArrayOp.reduceVarc(ddof), axis, Order.defaultOrder());
    }

    public final DArray<N> var1d(int axis, int ddof, Order order) {
        return reduce1d(DArrayOp.reduceVarc(ddof), axis, order);
    }

    public final N std(int ddof) {
        return dt().cast(Math.sqrt(var(ddof).doubleValue()));
    }

    public final DArray<N> std1d(int axis, int ddof) {
        return var1d(axis, ddof).sqrt_();
    }

    public final DArray<N> std1d(int axis, int ddof, Order order) {
        return var1d(axis, ddof, order).sqrt_();
    }

    public final N var(int ddof, double mean) {
        return reduce(DArrayOp.reduceVarc(ddof, mean));
    }

    public final DArray<N> var1d(int axis, int ddof, DArray<?> mean) {
        return var1d(axis, ddof, mean, Order.defaultOrder());
    }

    public abstract DArray<N> var1d(int axis, int ddof, DArray<?> mean, Order order);

    public final DArray<N> varOn(Shape shape, int ddof, boolean keepDim, DArray<?> mean) {
        return varOn(shape, ddof, mean, keepDim, Order.defaultOrder());
    }

    public abstract DArray<N> varOn(Shape shape, int ddof, DArray<?> mean, boolean keepDim, Order order);


    public final int argmax() {
        return argmax(Order.defaultOrder());
    }

    public abstract int argmax(Order order);

    public final DArray<Integer> argmax1d(int axis, boolean keepDim) {
        return argmax1d(axis, keepDim, Order.defaultOrder());
    }

    public abstract DArray<Integer> argmax1d(int axis, boolean keepDim, Order order);

    public final int argmin() {
        return argmin(Order.defaultOrder());
    }

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
     * NArrays are vectors. The result is a matrix of shape {@code (n,m)}, where {@code n} is the
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
     * Performs matrix vector dot product. The first NArray must be a matrix and the second NArray must be a vector.
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
     * Performs matrix vector dot product. The first NArray must be a matrix and the second NArray must be a vector.
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
     * Performs a batched matrix vector multiplication. Self NArray plays the role of matrix batch, the {@code other} NArray
     * is the vector batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self NArray is matrix {@code (n,m)} and other NArray is a vector shape {code (m)}, the result is a unit batch
     * of shape {@code (1,n)}.
     * <p>
     * If self is a batch matrix NArray of shape {@code (b,n,m)} and second is a vector shape {@code (m)}, the vectors is multiplied with
     * all the matrices in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a matrix NArray of shape {@code (n,m)} and the other is a batch of vectors with shape {@code (b,m)}, the matrix will
     * be multiplied with every vector in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self NArray is a batch of matrices with shape {@code (b,n,m)} and {code other} is a batch of vectors with shape {@code (b,m)},
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
     * Performs a batched matrix vector multiplication. Self NArray plays the role of matrix batch, the {@code other} NArray
     * is the vector batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self NArray is matrix {@code (n,m)} and other NArray is a vector shape {code (m)}, the result is a unit batch
     * of shape {@code (1,n)}.
     * <p>
     * If self is a batch matrix NArray of shape {@code (b,n,m)} and second is a vector shape {@code (m)}, the vectors is multiplied with
     * all the matrices in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a matrix NArray of shape {@code (n,m)} and the other is a batch of vectors with shape {@code (b,m)}, the matrix will
     * be multiplied with every vector in the batch and the result will have shape {@code (b,n)}.
     * <p>
     * If self NArray is a batch of matrices with shape {@code (b,n,m)} and {code other} is a batch of vectors with shape {@code (b,m)},
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
     * NArray which must be a matrix. The size of the vector must be equal with the size of the first dimesion of the matrix.
     * <p>
     * The result is a vector with size equal with the size of the second dimension of the matrix.
     * This operation is equivalent with calling {@link #mv(DArray)}, but with transposed matrix.
     * <p>
     * This operation does not perform broadcasting and the storage order of the result is the default order.
     *
     * @param other the other NArray which must be a matrix.
     * @return the result of the vector transpose matrix dot product
     */
    public final DArray<N> vtm(DArray<?> other) {
        return vtm(other, Order.defaultOrder());
    }

    /**
     * Performs the dot product between this object transposed, which must be a vector, and the other
     * NArray which must be a matrix. The size of the vector must be equal with the size of the first dimesion of the matrix.
     * <p>
     * The result is a vector with size equal with the size of the second dimension of the matrix.
     * This operation is equivalent with calling {@link #mv(DArray)}, but with transposed matrix.
     * <p>
     * This operation does not perform broadcasting and the storage order of the result is specified by {@code askOrder} parameter.
     *
     * @param other the other NArray which must be a matrix.
     * @return the result of the vector transpose matrix dot product
     */
    public abstract DArray<N> vtm(DArray<?> other, Order askOrder);

    /**
     * Performs a batched vector transposed matrix multiplication. Self NArray plays the role of vector batch, the {@code other} NArray
     * is the matrix batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self is vector {@code (n)} and other NArray is a matrix {code (n,m)}, the result is a unit batch
     * of shape {@code (1,m)}.
     * <p>
     * If self is a batch vector NArray of shape {@code (b,n)} and second is a matrix shape {@code (n,m)}, the vector are multiplied with
     * all the same matrix and the result will have shape {@code (b,m)}.
     * <p>
     * If self is a vector NArray of shape {@code (n)} and the other is a batch of matrices with shape {@code (b,n,m)}, the vector will
     * be multiplied with every matrix in the batch and the result will have shape {@code (b,m)}.
     * <p>
     * If self NArray is a batch of vectors with shape {@code (b,n)} and {code other} is a batch of matrices with shape {@code (b,n,m)},
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
     * Performs a batched vector transposed matrix multiplication. Self NArray plays the role of vector batch, the {@code other} NArray
     * is the matrix batch.
     * <p>
     * If both arguments are scalars the result is a unit length batch of a scalar shape {@code (1,1)}.
     * <p>
     * If self is vector {@code (n)} and other NArray is a matrix {code (n,m)}, the result is a unit batch
     * of shape {@code (1,m)}.
     * <p>
     * If self is a batch vector NArray of shape {@code (b,n)} and second is a matrix shape {@code (n,m)}, the vector are multiplied with
     * all the same matrix and the result will have shape {@code (b,n)}.
     * <p>
     * If self is a vector NArray of shape {@code (n)} and the other is a batch of matrices with shape {@code (b,n,m)}, the vector will
     * be multiplied with every matrix in the batch and the result will have shape {@code (b,m)}.
     * <p>
     * If self NArray is a batch of vectors with shape {@code (b,n)} and {code other} is a batch of matrices with shape {@code (b,n,m)},
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
     * Performs matrix multiplication between two NArrays. The two NArrays must both be matrices.
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

    /**
     * Performs matrix multiplication between two NArrays. The two NArrays must both be matrices.
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

    /**
     * Performs batch matrix-matrix multiplication. Batch index is the first parameter, if exists.
     * If self is an NArray of shape {@code (b,n,m)} and {@code other} has shape {@code (b,m,p)}
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
     * If self is an NArray of shape {@code (b,n,m)} and {@code other} has shape {@code (b,m,p)}
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
     * If the input NArray is a vector, it creates a matrix with elements on the specified diagonal.
     * The resulting matrix is a square matrix with dimension size to accommodate all the elements
     * from the vector.
     * <p>
     * If the input NArray is a matrix, then the result is a vector which contains the elements from that
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
            throw new OperationNotAvailableException("Available only for floating point NArrays.");
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
            throw new OperationNotAvailableException("Available only for floating point NArrays.");
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
            throw new OperationNotAvailableException("Available only for floating point NArrays.");
        }
        DArray<N> std = std1d(0, 0);
        DArray<N> scaled = sub(mean1d(0));
        return scaled.t().mm(scaled, askOrder).div_(std).div_(std.stretch(1)).div_(dim(0));
    }

    // -- Comparison Ops -- //

    /**
     * Creates a new NArray with values sorted along the dimension given as parameters. The order is ascending or descending, given
     * as parameter.
     * <p>
     * The order of the new NArray is the default order.
     *
     * @param axis dimension along which the values will be sorted
     * @param asc  if true the values will be sorted in ascending order, otherwise in descending order
     * @return a new copy NArray with values sorted along the given dimension
     */
    public final DArray<N> sort(int axis, boolean asc) {
        return sort(Order.defaultOrder(), axis, asc);
    }

    /**
     * Creates a new NArray with values sorted along the dimension given as parameters. The order is ascending or descending, given
     * as parameter.
     * <p>
     * The order of the new NArray is the order specified as parameter.
     *
     * @param order order of the new NArray
     * @param axis  dimension along which the values will be sorted
     * @param asc   if true the values will be sorted in ascending order, otherwise in descending order
     * @return a new copy NArray with values sorted along the given dimension
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
     * @return same NArray instance with values sorted along the given dimension
     */
    public abstract DArray<N> sort_(int axis, boolean asc);

    /**
     * Sorts indices given as an array of parameters according to the values from flatten NArray.
     * NArray must have a single dimension with size greater than the biggest index value.
     *
     * @param indices indices which will be sorted
     * @param asc     sort ascending if true, descending otherwise
     */
    public abstract void externalSort(int[] indices, boolean asc);

    //------- broadcast operations --------//


    //------- SUMMARY OPERATIONS ----------//

    @SuppressWarnings("unchecked")
    public final <M extends Number> DArray<M> cast(DType<M> dtype) {
        if ((dtype.id() == dt().id())) {
            return (DArray<M>) this;
        } else {
            return cast(dtype, Order.A);
        }
    }

    public abstract <M extends Number> DArray<M> cast(DType<M> dType, Order askOrder);

    /**
     * Creates a padded copy of an NArray along the first dimension. The padded copy will be an NArray with the same shape other than the
     * first dimension which will have size {@code before + dim(0) + after}, having first and last elements padded with 0.
     * <p>
     *
     * @return resized padded copy of the original NArray
     */
    public final DArray<N> pad(int before, int after) {
        return pad(0, before, after);
    }

    /**
     * Creates a padded copy of an NArray along a given dimension. The padded copy will be an NArray with the same shape
     * on all axis other than the specified as parameter, the later being increased to {@code before + dim(axis) + after},
     * having first and last elements padded with 0.
     * <p>
     *
     * @return resized padded copy of the original NArray
     */
    public final DArray<N> pad(int axis, int before, int after) {
        int[] newDims = Arrays.copyOf(dims(), rank());
        newDims[axis] += before + after;
        DArray<N> copy = manager().zeros(dt(), Shape.of(newDims), Order.defaultOrder());
        copyTo(copy.narrow(axis, true, before, before + dim(axis)));
        return copy;
    }

    /**
     * Creates a copy of the original NArray with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @return new copy of the NArray
     */
    public final DArray<N> copy() {
        return copy(Order.defaultOrder());
    }

    /**
     * Creates a copy of the original NArray with the given order. Only {@link Order#C} or {@link Order#F} are allowed.
     * <p>
     * The order does not determine how values are read, but how values will be stored.
     *
     * @param askOrder desired order of the copy NArray.
     * @return new copy of the NArray
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
