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

package rapaio.narray.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rapaio.data.VarDouble;
import rapaio.narray.DType;
import rapaio.narray.Layout;
import rapaio.narray.NArray;
import rapaio.narray.Order;
import rapaio.narray.Shape;
import rapaio.narray.Storage;
import rapaio.narray.NArrayManager;
import rapaio.narray.iterators.DensePointerIterator;
import rapaio.narray.iterators.PointerIterator;
import rapaio.narray.iterators.StrideLoopDescriptor;
import rapaio.narray.iterators.StridePointerIterator;
import rapaio.narray.layout.StrideLayout;
import rapaio.narray.layout.StrideWrapper;
import rapaio.narray.manager.base.BaseByteNArrayStride;
import rapaio.narray.manager.base.BaseDoubleNArrayStride;
import rapaio.narray.manager.base.BaseFloatNArrayStride;
import rapaio.narray.manager.base.BaseIntNArrayStride;
import rapaio.narray.storage.array.DoubleArrayStorage;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

public abstract sealed class AbstractStrideNArray<N extends Number> extends NArray<N>
        permits BaseDoubleNArrayStride, BaseFloatNArrayStride, BaseIntNArrayStride, BaseByteNArrayStride {

    protected final StrideLayout layout;
    protected final StrideLoopDescriptor<N> loop;

    public AbstractStrideNArray(NArrayManager manager, StrideLayout layout, Storage storage) {
        super(manager, storage);
        this.layout = layout;
        this.loop = StrideLoopDescriptor.of(layout, layout.storageFastOrder(), dtype().vectorSpecies());
    }

    @Override
    public final StrideLayout layout() {
        return layout;
    }

    @Override
    public final NArray<N> t_() {
        return manager.stride(dtype(), layout.revert(), storage);
    }

    @Override
    public final NArray<N> ravel(Order askOrder) {
        var compact = layout.computeFortranLayout(askOrder, true);
        if (compact.shape().rank() == 1) {
            return manager.stride(dtype(), compact, storage);
        }
        return flatten(askOrder);
    }

    @Override
    public final NArray<N> squeeze(int... axes) {
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
        return manager.stride(dtype(), newLayout, storage);
    }

    @Override
    public final NArray<N> stretch(int... axes) {
        var newLayout = layout.stretch(axes);
        if (newLayout == layout) {
            return this;
        }
        return manager.stride(dtype(), newLayout, storage);
    }

    @Override
    public final NArray<N> permute(int... dims) {
        return manager.stride(dtype(), layout().permute(dims), storage);
    }

    @Override
    public final NArray<N> moveAxis(int src, int dst) {
        return manager.stride(dtype(), layout.moveAxis(src, dst), storage);
    }

    @Override
    public final NArray<N> swapAxis(int src, int dst) {
        return manager.stride(dtype(), layout.swapAxis(src, dst), storage);
    }

    @Override
    public final NArray<N> narrow(int axis, boolean keepdim, int start, int end) {
        return manager.stride(dtype(), layout.narrow(axis, keepdim, start, end), storage);
    }

    @Override
    public final NArray<N> narrowAll(boolean keepdim, int[] starts, int[] ends) {
        return manager.stride(dtype(), layout.narrowAll(keepdim, starts, ends), storage);
    }

    @Override
    public final List<NArray<N>> split(int axis, boolean keepdim, int... indexes) {
        List<NArray<N>> result = new ArrayList<>(indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            result.add(narrow(axis, keepdim, indexes[i], i < indexes.length - 1 ? indexes[i + 1] : shape().dim(axis)));
        }
        return result;
    }

    @Override
    public final List<NArray<N>> splitAll(boolean keepdim, int[][] indexes) {
        if (indexes.length != rank()) {
            throw new IllegalArgumentException(
                    "Indexes length of %d is not the same as shape rank %d.".formatted(indexes.length, rank()));
        }
        List<NArray<N>> results = new ArrayList<>();
        int[] starts = new int[indexes.length];
        int[] ends = new int[indexes.length];
        splitAllRecursive(results, indexes, keepdim, starts, ends, 0);
        return results;
    }

    private void splitAllRecursive(List<NArray<N>> results, int[][] indexes, boolean keepdim, int[] starts, int[] ends, int level) {
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
    public final NArray<N> expand(int axis, int size) {
        return manager.stride(dtype(), layout.expand(axis, size), storage);
    }

    @Override
    public final NArray<N> take(Order order, int axis, int... indices) {

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
            newStrides[axis] = 1;
            int newOffset = layout().offset() + indices[0] * layout.stride(axis);
            return manager.stride(dtype(), StrideLayout.of(Shape.of(newDims), newOffset, newStrides), storage);
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
                return manager.stride(dtype(), StrideLayout.of(Shape.of(newDims), newOffset, newStrides), storage);
            }
        }

        // if we failed, we copy data into a new tensor
        List<NArray<N>> slices = new ArrayList<>();
        for (int index : indices) {
            slices.add(narrow(axis, true, index, index + 1));
        }
        return manager.concat(order, axis, slices);
    }

    @Override
    public final NArray<N> sort_(int axis, boolean asc) {
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
    public void argSort(int[] indices, boolean asc) {
        if (layout.rank() != 1) {
            throw new IllegalArgumentException("NArray must be flat (have a single dimension).");
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
        if (layout.isCOrdered() && askOrder != Order.F) {
            return new DensePointerIterator(layout.shape(), layout.offset(), layout.stride(-1));
        }
        if (layout.isFOrdered() && askOrder != Order.C) {
            return new DensePointerIterator(layout.shape(), layout.offset(), layout.stride(0));
        }
        return new StridePointerIterator(layout, askOrder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Number> NArray<M> cast(DType<M> dt, Order askOrder) {
        if (dt.equals(dtype()) && (askOrder.equals(Order.A) ||
                (layout.isCOrdered() && askOrder.equals(Order.C)) ||
                (layout.isFOrdered() && askOrder.equals(Order.F)))) {
            return (NArray<M>) this;
        }

        askOrder = askOrder == Order.A ? Order.defaultOrder() : askOrder;
        askOrder = Order.autoFC(askOrder);
        var castTensor = manager().zeros(dt, shape(), askOrder);

        Order fastOrder = Layout.storageFastTandemOrder(castTensor.layout(), layout);
        var loopDescriptor = StrideLoopDescriptor.of((StrideLayout) castTensor.layout(), fastOrder, dt.vectorSpecies());
        var iter = ptrIterator(fastOrder);
        for (int p : loopDescriptor.offsets) {
            for (int i = 0; i < loopDescriptor.size; i++) {
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
        if (this instanceof BaseDoubleNArrayStride bs) {
            if (bs.layout().offset() == 0 && bs.layout().stride(0) == 1) {
                return VarDouble.wrap(bs.asDoubleArray());
            }
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
        var loop = StrideLoopDescriptor.of(layout, askOrder, dtype().vectorSpecies());
        for (int offset : loop.offsets) {
            for (int i = 0; i < loop.size; i++) {
                int p = offset + i * loop.step;
                copy[pos++] = storage.getDouble(p);
            }
        }
        return copy;
    }

    @Override
    public double[] asDoubleArray(Order askOrder) {
        if (storage instanceof DoubleArrayStorage as && isVector() && layout.offset() == 0 && layout.stride(0) == 1) {
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