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

package rapaio.darray.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Layout;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Storage;
import rapaio.darray.iterators.IndexIterator;
import rapaio.darray.iterators.PointerIterator;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.iterators.StridePointerIterator;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.layout.StrideWrapper;
import rapaio.darray.manager.base.BaseByteStrideDArray;
import rapaio.darray.manager.base.BaseDoubleStrideDArray;
import rapaio.darray.manager.base.BaseFloatStrideDArray;
import rapaio.darray.manager.base.BaseIntStrideDArray;
import rapaio.darray.storage.array.DoubleArrayStorage;
import rapaio.data.VarDouble;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

public abstract sealed class AbstractStrideDArray<N extends Number> extends DArray<N>
        permits BaseDoubleStrideDArray, BaseFloatStrideDArray, BaseIntStrideDArray, BaseByteStrideDArray {

    protected final StrideLayout layout;
    /**
     * Loop descriptor over the whole array in its storage-fast order. Computed on first use: building it
     * allocates an offsets array with one entry per collapsed outer row, which is wasted work for the many
     * short-lived views (rows, slices, transposes) that are only ever read element-wise.
     */
    private volatile StrideLoopDescriptor loop;

    public AbstractStrideDArray(DArrayManager manager, DType<N> dt, StrideLayout layout, Storage storage) {
        super(manager, dt, storage);
        this.layout = layout;
    }

    /**
     * @return loop descriptor of this array in its storage-fast order, built lazily and cached
     */
    protected final StrideLoopDescriptor loop() {
        StrideLoopDescriptor ld = loop;
        if (ld == null) {
            // benign race: concurrent callers may build the same immutable descriptor twice
            ld = StrideLoopDescriptor.of(layout, layout.storageFastOrder(), dt().vs());
            loop = ld;
        }
        return ld;
    }

    @Override
    public final StrideLayout layout() {
        return layout;
    }

    /**
     * Rejects an operation which would write into a layout that maps several logical elements onto the same storage
     * position, as {@link StrideLayout#hasAliasedElements()} defines. Such a layout comes from {@code expand}, directly
     * or through {@code strexp} and broadcasting.
     * <p>
     * Writing through one is not well defined: the writes to all the repeats of an element collide, so the result
     * depends on the order the loop visits them in, which differs between the vectorised and the scalar path and
     * therefore with the vector length of the data type. Reading stays allowed, since every repeat yields the same
     * value, and so does broadcasting the operand of an in-place operation, which is only read.
     * <p>
     * Guarding here, at the entry points, is what makes the loops below safe: none of them can be reached with a
     * destination whose inner step is zero, so they need no special case for a store which does not advance.
     *
     * @param target    array being written into, which is the destination and not necessarily {@code this}
     * @param operation name of the operation, used in the message
     * @throws IllegalArgumentException if the target layout is aliased
     */
    protected static void validateWritableTarget(DArray<?> target, String operation) {
        if (target.layout().hasAliasedElements()) {
            throw new IllegalArgumentException(String.format(
                    "Operation %s cannot write into an expanded darray with shape %s, since several of its elements "
                            + "share the same storage position. Copy it first.", operation, target.shape()));
        }
    }

    /**
     * Rejects an operation which reads from and writes into the same storage over ranges which overlap. The copy
     * loops read the source and write the destination element by element in one pass, with no intermediate buffer,
     * so an overlap makes a position which has already been overwritten the source of a later element. The result
     * then depends on the direction of the overlap: copying a range onto a later one corrupts it, copying onto an
     * earlier one happens to be correct.
     * <p>
     * The ranges compared are the bounding storage intervals of the two layouts, so a pair of strided views which
     * interleave without sharing an element is rejected as well. That is deliberate: deciding the question exactly
     * costs an index-by-index comparison, while the conservative answer only rejects an operation whose result the
     * caller should not be relying on anyway. Copying between distinct storages is never affected, nor is an
     * out-of-place operation, which allocates its own destination.
     *
     * @param source    array being read
     * @param target    array being written into
     * @param operation name of the operation, used in the message
     * @throws IllegalArgumentException if both arrays share storage and their element ranges overlap
     */
    protected static void validateNonOverlapping(DArray<?> source, DArray<?> target, String operation) {
        if (!(source instanceof AbstractStrideDArray<?> src) || !(target instanceof AbstractStrideDArray<?> dst)) {
            return;
        }
        if (src.storage != dst.storage) {
            return;
        }
        long[] srcRange = storageRange(src.layout);
        long[] dstRange = storageRange(dst.layout);
        if (srcRange[0] <= dstRange[1] && dstRange[0] <= srcRange[1]) {
            throw new IllegalArgumentException(String.format(
                    "Operation %s cannot be applied with a source and a destination which share storage over "
                            + "overlapping ranges [%d,%d] and [%d,%d], since the copy would read positions it has "
                            + "already written. Copy the source first.",
                    operation, srcRange[0], srcRange[1], dstRange[0], dstRange[1]));
        }
    }

    /**
     * The inclusive bounding interval of the storage positions a layout addresses. An axis with a positive stride
     * pushes the upper bound up, one with a negative stride pushes the lower bound down, and a zero stride moves
     * neither.
     *
     * @param layout layout to measure
     * @return the lowest and the highest storage position the layout can address
     */
    private static long[] storageRange(StrideLayout layout) {
        long min = layout.offset();
        long max = layout.offset();
        for (int i = 0; i < layout.rank(); i++) {
            long span = (long) layout.stride(i) * (layout.dim(i) - 1);
            if (span > 0) {
                max += span;
            } else {
                min += span;
            }
        }
        return new long[] {min, max};
    }

    @Override
    public final DArray<N> reshape(Shape askShape, Order askOrder) {
        if (layout.shape().size() != askShape.size()) {
            throw new IllegalArgumentException(String.format(
                    "Incompatible shape size for reshape operation from %s into %s.", this.shape(), askShape));
        }
        if (Order.A == askOrder) {
            if (layout.isCOrdered()) {
                askOrder = Order.C;
            } else if (layout.isFOrdered()) {
                askOrder = Order.F;
            } else {
                askOrder = Order.defaultOrder();
            }
        }
        if (Order.S == askOrder) {
            throw new IllegalArgumentException("Illegal order specification.");
        }
        StrideLayout newLayout = layout.attemptReshape(askShape, askOrder);
        if (newLayout != null) {
            return dm.stride(dt, newLayout, storage);
        }
        var it = new StridePointerIterator(layout, askOrder);
        DArray<N> copy = dm.zeros(dt, askShape, askOrder);
        var copyIt = copy.ptrIterator(askOrder);
        while (it.hasNext()) {
            copy.ptrSetDouble(copyIt.nextInt(), storage.getDouble(it.nextInt()));
        }
        return copy;
    }

    @Override
    public final DArray<N> t_() {
        return dm.stride(dt(), layout.revert(), storage);
    }

    /**
     * Resolves {@link Order#A} against this darray's own layout, the same way {@link #reshape} does, so that every
     * caller which accepts an arbitrary order can pass it on to the layout and loop machinery, which know only
     * {@code F}, {@code C} and {@code S}.
     */
    private Order resolveAuto(Order askOrder) {
        if (Order.A != askOrder) {
            return askOrder;
        }
        if (layout.isCOrdered()) {
            return Order.C;
        }
        if (layout.isFOrdered()) {
            return Order.F;
        }
        return Order.defaultOrder();
    }

    @Override
    public final DArray<N> flatten(Order askOrder) {
        askOrder = Order.autoFC(resolveAuto(askOrder));
        var result = dm.zeros(dt, Shape.of(layout.size()), askOrder);
        var out = result.storage();
        int ptr = 0;
        var loop = StrideLoopDescriptor.of(layout, askOrder, dt().vs());
        for (int p : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                out.setDouble(ptr++, storage.getDouble(p));
                p += loop.step;
            }
        }
        return result;
    }

    @Override
    public final DArray<N> ravel(Order askOrder) {
        askOrder = resolveAuto(askOrder);
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return dm.stride(dt(), compact, storage);
        }
        return flatten(askOrder);
    }

    @Override
    public final DArray<N> squeeze(int... axes) {
        int[] copy = Arrays.copyOf(axes, axes.length);
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] < 0) {
                copy[i] += rank();
            }
        }
        var newLayout = layout.squeeze(copy);
        if (newLayout == layout) {
            return this;
        }
        return dm.stride(dt(), newLayout, storage);
    }

    @Override
    public final DArray<N> stretch(int... axes) {
        var newLayout = layout.stretch(axes);
        if (newLayout == layout) {
            return this;
        }
        return dm.stride(dt(), newLayout, storage);
    }

    @Override
    public final DArray<N> permute(int... dims) {
        return dm.stride(dt(), layout().permute(dims), storage);
    }

    @Override
    public final DArray<N> moveAxis(int src, int dst) {
        return dm.stride(dt(), layout.moveAxis(src, dst), storage);
    }

    @Override
    public final DArray<N> swapAxis(int src, int dst) {
        return dm.stride(dt(), layout.swapAxis(src, dst), storage);
    }

    @Override
    public final DArray<N> narrow(int axis, boolean keepdim, int start, int end) {
        return dm.stride(dt(), layout.narrow(axis, keepdim, start, end), storage);
    }

    @Override
    public final DArray<N> narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return dm.stride(dt(), layout.narrowAll(keepdim, starts, ends), storage);
    }

    @Override
    public final List<DArray<N>> split(int axis, boolean keepdim, int... indexes) {
        if (axis < 0 || axis >= rank()) {
            throw new IllegalArgumentException("Axis is out of bounds: " + axis + ".");
        }
        validateSplitIndexes(indexes, axis, shape().dim(axis));
        List<DArray<N>> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    /**
     * Validates the cut points of a split along one axis. Each index is the start of a piece which runs to the next
     * index, or to the end of the axis for the last one, so the indexes must be strictly increasing and inside the
     * axis. An empty request is rejected rather than answered with an empty list: it names no piece at all, and
     * returning nothing silently drops the whole array.
     *
     * @param indexes start index of each piece
     * @param axis    axis being split, used in the message
     * @param dim     dimension of that axis
     */
    private static void validateSplitIndexes(int[] indexes, int axis, int dim) {
        if (indexes == null || indexes.length == 0) {
            throw new IllegalArgumentException("Split indexes cannot be empty for axis " + axis + ".");
        }
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] < 0 || indexes[i] >= dim) {
                throw new IllegalArgumentException(String.format(
                        "Split index %d is out of range for axis %d of dimension %d.", indexes[i], axis, dim));
            }
            if (i > 0 && indexes[i] <= indexes[i - 1]) {
                throw new IllegalArgumentException(String.format(
                        "Split indexes for axis %d must be strictly increasing, found %d after %d.",
                        axis, indexes[i], indexes[i - 1]));
            }
        }
    }

    @Override
    public final List<DArray<N>> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        for (int axis = 0; axis < indexes.length; axis++) {
            validateSplitIndexes(indexes[axis], axis, shape().dim(axis));
        }
        List<DArray<N>> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRecursive(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRecursive(List<DArray<N>> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
        if (level == indexes.length) {
            return;
        }
        for (int i = 0; i < indexes[level].length; i++) {
            starts[level] = indexes[level][i];
            ends[level] = i < indexes[level].length - 1 ? indexes[level][i + 1] : shape().dim(level);
            if (level == indexes.length - 1) {
                results.add(narrowAll(keepdim, starts, ends));
            } else {
                splitAllRecursive(results, indexes, keepdim, starts, ends, level + 1);
            }
        }
    }

    @Override
    public final DArray<N> expand(int axis, int size) {
        return dm.stride(dt(), layout.expand(axis, size), storage);
    }

    @Override
    public final DArray<N> gather_(int axis, DArray<?> index, DArray<?> input) {
        validateWritableTarget(this, "gather_");
        if (!index.shape().equals(this.shape())) {
            throw new IllegalArgumentException("Index must have the same shape as destination.");
        }
        if (index.rank() != input.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as input.");
        }
        checkGatherScatterAxis(axis, input);
        var ptrDstIt = ptrIterator(Order.C);
        var ptrIdxIt = index.ptrIterator(Order.C);
        var indexIt = new IndexIterator(shape(), Order.C);
        int[] idx = new int[rank()];
        while (indexIt.hasNext()) {
            int[] indexNext = indexIt.next();
            System.arraycopy(indexNext, 0, idx, 0, idx.length);
            idx[axis] = index.ptrGetInt(ptrIdxIt.nextInt());
            storage.setDouble(ptrDstIt.next(), input.getDouble(idx));
        }
        return this;
    }

    @Override
    public final DArray<N> scatter_(int axis, DArray<?> index, DArray<?> input) {
        validateWritableTarget(this, "scatter_");
        if (index.rank() != input.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as input.");
        }
        if (index.rank() != this.rank()) {
            throw new IllegalArgumentException("Index must have the same rank as self tensor.");
        }
        // the loop below pairs index and input elements by flat position, which matches only for equal shapes
        if (!index.shape().equals(input.shape())) {
            throw new IllegalArgumentException("Index must have the same shape as input.");
        }
        checkGatherScatterAxis(axis, this);
        var ptrSrcIt = input.ptrIterator(Order.C);
        var ptrIdxIt = index.ptrIterator(Order.C);
        var indexIt = new IndexIterator(index.shape(), Order.C);
        int[] idx = new int[rank()];
        while (indexIt.hasNext()) {
            int[] indexNext = indexIt.next();
            System.arraycopy(indexNext, 0, idx, 0, idx.length);
            idx[axis] = index.ptrGetInt(ptrIdxIt.nextInt());
            setDouble(input.ptrGetDouble(ptrSrcIt.nextInt()), idx);
        }
        return this;
    }

    private void checkGatherScatterAxis(int axis, DArray<?> target) {
        if (axis < 0 || axis >= rank()) {
            throw new IllegalArgumentException("Axis is out of bounds: " + axis + ".");
        }
        if (axis >= target.rank()) {
            throw new IllegalArgumentException("Axis is out of bounds for the indexed darray: " + axis + ".");
        }
    }

    @Override
    public final DArray<N> sel(Order order, int axis, int... indices) {

        if (axis < 0 || axis >= layout.rank()) {
            throw new IllegalArgumentException(String.format("Axis value %d is out of bounds.", axis));
        }
        if (indices == null || indices.length == 0) {
            throw new IllegalArgumentException("Indices cannot be empty.");
        }
        for (int index : indices) {
            if (index < 0 || index >= layout.dim(axis)) {
                throw new IllegalArgumentException(
                        String.format("Index values are invalid %s, must be in range [0,%d].",
                                Arrays.toString(indices),
                                layout.dim(axis) - 1));
            }
        }

        // check if we can handle only through stride layout

        // a single element
        if (indices.length == 1) {
            int[] newDims = Arrays.copyOf(layout.dims(), layout.dims().length);
            int[] newStrides = Arrays.copyOf(layout.strides(), layout.strides().length);
            newDims[axis] = 1;
            // the axis holds one element, so its stride is never used for addressing, but it is used to describe the
            // view: substituting 1 makes a C-ordered selection look F-ordered, which costs reshape and copyTo their
            // view and fast-order paths. Keeping the real stride agrees with what narrow produces for the same view
            newStrides[axis] = layout.stride(axis);
            int newOffset = layout().offset() + indices[0] * layout.stride(axis);
            return dm.stride(dt(), StrideLayout.of(Shape.of(newDims), newOffset, newStrides), storage);
        }

        // a geometric sequence of indices, even if the step is 0 (repeated elements)
        if (indices[1] - indices[0] >= 0) {
            int step = indices[1] - indices[0];
            boolean validSequence = true;
            for (int i = 2; i < indices.length; i++) {
                if (indices[i] - indices[i - 1] != step) {
                    validSequence = false;
                    break;
                }
            }
            if (validSequence) {
                int[] newDims = Arrays.copyOf(layout.dims(), layout.dims().length);
                int[] newStrides = Arrays.copyOf(layout.strides(), layout.strides().length);
                newDims[axis] = indices.length;
                newStrides[axis] = layout.stride(axis) * step;
                int newOffset = layout.offset() + indices[0] * layout.stride(axis);
                return dm.stride(dt(), StrideLayout.of(Shape.of(newDims), newOffset, newStrides), storage);
            }
        }

        // if we failed, we copy data into a new tensor
        List<DArray<N>> slices = new ArrayList<>();
        for (int index : indices) {
            slices.add(narrow(axis, true, index, index + 1));
        }
        return dm.cat(dt(), order, axis, slices);
    }

    @Override
    public final DArray<N> sort_(int axis, boolean asc) {
        // sorting acts along an axis, so it follows the reduction convention and accepts a negative axis. Before this
        // was resolved here, an in-range negative axis happened to work, since narrowDims and narrowStrides resolve
        // one themselves, while an out-of-range one fell through to a raw arraycopy failure
        axis = StrideLayout.normalizeAxis(layout, axis, "sort_");
        validateWritableTarget(this, "sort_");
        int[] newDims = layout.shape().narrowDims(axis);
        int[] newStrides = layout.narrowStrides(axis);
        int selDim = layout.dim(axis);
        int selStride = layout.stride(axis);

        var it = new StridePointerIterator(StrideLayout.of(Shape.of(newDims), layout().offset(), newStrides), Order.C, false);
        while (it.hasNext()) {
            StrideWrapper.of(it.nextInt(), selStride, selDim, this).sort(asc);
        }
        return this;
    }

    @Override
    public void externalSort(int[] indices, boolean asc) {
        if (layout.rank() != 1) {
            throw new IllegalArgumentException("DArray must be flat (have a single dimension).");
        }
        for (int index : indices) {
            if (index < 0 || index >= layout.size()) {
                throw new IllegalArgumentException("Indices must be semi-positive and less than the size of the tensor.");
            }
        }
        StrideWrapper.of(layout.offset(), layout.stride(0), layout.dim(0), this).sortIndirect(indices, asc);
    }

    @Override
    public final PointerIterator ptrIterator(Order askOrder) {
        return layout.ptrIterator(askOrder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Number> DArray<M> cast(DType<M> dt, Order askOrder) {
        if (dt.equals(dt()) && (askOrder.equals(Order.A) ||
                (layout.isCOrdered() && askOrder.equals(Order.C)) ||
                (layout.isFOrdered() && askOrder.equals(Order.F)))) {
            return (DArray<M>) this;
        }

        askOrder = askOrder == Order.A ? Order.defaultOrder() : askOrder;
        askOrder = Order.autoFC(askOrder);
        var castTensor = dm().zeros(dt, shape(), askOrder);

        Order fastOrder = Layout.storageFastTandemOrder(castTensor.layout(), layout);
        var loopDescriptor = StrideLoopDescriptor.of((StrideLayout) castTensor.layout(), fastOrder, dt.vs());
        var iter = ptrIterator(fastOrder);
        for (int p : loopDescriptor.offsets) {
            for (int i = 0; i < loopDescriptor.bound; i++) {
                castTensor.ptrSet(p, dt.cast(ptrGet(iter.nextInt())));
                p += loopDescriptor.step;
            }
        }
        return castTensor;
    }

    @Override
    public final VarDouble dv() {
        if (layout().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be converted to VarDouble.");
        }
        if (this instanceof BaseDoubleStrideDArray bs) {
            // asDoubleArray either shares the backing array, when this darray covers it exactly, or returns a fresh
            // array of exactly size() values; both are safe to wrap, so there is nothing left to copy here
            return VarDouble.wrap(bs.asDoubleArray());
        }
        double[] copy = new double[layout().size()];
        var it = iterator(Order.C);
        for (int i = 0; i < copy.length; i++) {
            copy[i] = it.next().doubleValue();
        }
        return VarDouble.wrap(copy);
    }

    @Override
    public double[] toDoubleArray(Order askOrder) {
        double[] copy = new double[size()];
        int pos = 0;
        var loop = StrideLoopDescriptor.of(layout, resolveAuto(askOrder), dt().vs());
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.bound; i++) {
                int p = offset + i * loop.step;
                copy[pos++] = storage.getDouble(p);
            }
        }
        return copy;
    }

    @Override
    public double[] asDoubleArray(Order askOrder) {
        // the backing array can be shared only when this darray covers it exactly; a view over the first elements
        // of a larger storage (the first row of a matrix, a narrowed vector) must be copied out
        if (storage instanceof DoubleArrayStorage as && isVector() && layout.offset() == 0 && layout.stride(0) == 1
                && size() == as.array().length) {
            return as.array();
        }
        return toDoubleArray(askOrder);
    }

    @Override
    public final String toContent(Printer printer, POpt<?>... options) {

        final int MAX_COL_VALUES = 21;
        boolean maxColHit = false;
        int cols = 2 + shape().dim(-1);
        if (shape().dim(-1) > MAX_COL_VALUES) {
            maxColHit = true;
            cols = 2 + MAX_COL_VALUES;
        }

        final int MAX_ROW_VALUES = 41;
        boolean maxRowHit = false;
        int rows = shape().size() / shape().dim(-1);
        if (shape().size() / shape().dim(-1) > MAX_ROW_VALUES) {
            maxRowHit = true;
            rows = MAX_ROW_VALUES;
        }

        TextTable tt = TextTable.empty(rows, cols, 0, 0);

        var p = printer.withOptions(options);
        int row = 0;
        if (maxRowHit) {
            for (; row < MAX_ROW_VALUES - 1; row++) {
                tt.textCenter(row, 0, rowStart(shape(), row));
                tt.textLeft(row, cols - 1, rowEnd(shape(), row));
                appendValues(p, tt, row, cols, maxColHit);
            }
            for (int i = 0; i < cols; i++) {
                tt.textCenter(row, i, "...");
            }
        } else {
            for (; row < rows; row++) {
                tt.textCenter(row, 0, rowStart(shape(), row));
                tt.textLeft(row, cols - 1, rowEnd(shape(), row));
                appendValues(p, tt, row, cols, maxColHit);
            }
        }

        return tt.getText(-1);
    }

    private String rowStart(Shape shape, int row) {
        int[] index = shape.index(Order.C, row * shape.dim(-1));
        StringBuilder sb = new StringBuilder();
        for (int c = shape.rank() - 1; c >= 0; c--) {
            if (index[c] == 0) {
                sb.append("[");
            } else {
                break;
            }
        }
        while (sb.length() < shape.rank()) {
            sb.insert(0, " ");
        }
        return sb.toString();
    }

    private String rowEnd(Shape shape, int row) {
        int[] index = shape.index(Order.C, (row + 1) * shape.dim(-1) - 1);
        StringBuilder sb = new StringBuilder();
        for (int c = shape.rank() - 1; c >= 0; c--) {
            if (index[c] == shape.dim(c) - 1) {
                sb.append("]");
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private void appendValues(Printer printer, TextTable tt, int row, int cols, boolean maxColHit) {
        for (int i = 0; i < cols - 2; i++) {
            double value = getDouble(shape().index(Order.C, row * shape().dim(-1) + i));
            tt.floatString(row, i + 1, printer.getOptions().getFloatFormat().format(value));
        }
        if (maxColHit) {
            tt.textCenter(row, cols - 2, "...");
        }
    }

    @Override
    public final String toFullContent(Printer printer, POpt<?>... options) {
        int cols = 2 + shape().dim(-1);
        int rows = shape().size() / shape().dim(-1);

        TextTable tt = TextTable.empty(rows, cols, 0, 0);

        int row = 0;
        for (; row < rows; row++) {
            tt.textCenter(row, 0, rowStart(shape(), row));
            tt.textLeft(row, cols - 1, rowEnd(shape(), row));
            appendValues(printer, tt, row, cols, false);
        }

        return tt.getText(-1);
    }

    @Override
    public final String toSummary(Printer printer, POpt<?>... options) {
        return toString();
    }
}
