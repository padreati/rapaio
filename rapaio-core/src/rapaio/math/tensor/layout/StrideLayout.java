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

package rapaio.math.tensor.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.math.tensor.Layout;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.util.IntComparators;
import rapaio.util.collection.IntArrays;

public final class StrideLayout implements Layout {

    public static StrideLayout of(Shape shape, int offset, int[] strides) {
        return new StrideLayout(shape, offset, strides);
    }

    public static StrideLayout ofDense(Shape shape, int offset, Order order) {
        order = Order.autoFC(order);
        int[] strides = switch (order) {
            case C -> {
                int[] rowStrides = IntArrays.newFill(shape.rank(), 1);
                for (int i = rowStrides.length - 2; i >= 0; i--) {
                    rowStrides[i] = shape.dim(i + 1) * rowStrides[i + 1];
                }
                yield rowStrides;
            }
            case F -> {
                int[] colStrides = IntArrays.newFill(shape.rank(), 1);
                for (int i = 1; i < colStrides.length; i++) {
                    colStrides[i] = shape.dim(i - 1) * colStrides[i - 1];
                }
                yield colStrides;
            }
            default -> throw new IllegalArgumentException("Order type is invalid.");
        };
        return new StrideLayout(shape, offset, strides);
    }

    private static final int C_DENSE = 1;
    private static final int F_DENSE = 2;

    private final Shape shape;
    private final int offset;
    private final int[] strides;
    private int flags;

    public StrideLayout(Shape shape, int offset, int[] strides) {
        this.shape = shape;
        this.offset = offset;
        this.strides = strides;
        if (shape.rank() != strides.length) {
            throw new IllegalArgumentException("Dimensions does not have the same length as strides.");
        }
        updateFlags();
    }

    private void updateFlags() {
        if (shape.rank() < 2) {
            flags |= C_DENSE;
            flags |= F_DENSE;
            return;
        }
        if (isValidCOrder()) {
            flags |= C_DENSE;
        }
        if (isValidFOrder()) {
            flags |= F_DENSE;
        }
    }

    @Override
    public Shape shape() {
        return shape;
    }

    public int[] dims() {
        return shape.dims();
    }

    public int dim(int i) {
        return shape.dim(i);
    }

    public int offset() {
        return offset;
    }

    public int[] strides() {
        return strides;
    }

    public int stride(int i) {
        if(strides.length==0) {
            return 1;
        }
        return i >= 0 ? strides[i] : strides[i + strides.length];
    }

    @Override
    public int rank() {
        return shape.rank();
    }

    @Override
    public int size() {
        return shape.size();
    }

    @Override
    public boolean isCOrdered() {
        return (C_DENSE & flags) == C_DENSE;
    }

    @Override
    public boolean isFOrdered() {
        return (F_DENSE & flags) == F_DENSE;
    }

    @Override
    public boolean isDense() {
        return (isCOrdered() && stride(-1) == 1) || (isFOrdered() && stride(0) == 1);
    }

    @Override
    public Order storageFastOrder() {
        /*
        If the rank is 1, then we have a dense layout and any kind of order matches criteria.
        We return default order.
         */
        if (shape.rank() < 2) {
            return Order.defaultOrder();
        }
        if (isFOrdered()) {
            return Order.F;
        }
        if (isCOrdered()) {
            return Order.C;
        }
        return Order.S;
    }

    private boolean isValidFOrder() {
        for (int i = 1; i < shape.rank(); i++) {
            if (strides[i] != strides[i - 1] * shape.dim(i - 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidCOrder() {
        for (int i = shape.rank() - 2; i >= 0; i--) {
            if (strides[i] != strides[i + 1] * shape.dim(i + 1)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int pointer(int... index) {
        int pointer = offset;
        for (int i = 0; i < index.length; i++) {
            pointer += index[i] * strides[i];
        }
        return pointer;
    }

    @Override
    public int[] index(int pointer) {
        int[] storageOrder = IntArrays.newSeq(shape.rank());
        // sort indexes by strides, dims in decreasing order
        IntArrays.quickSort(storageOrder,
                IntComparators.asIntComparator(Comparator.comparingInt(this::stride).thenComparing(this::dim).reversed()));
        int[] index = new int[rank()];
        for (int j : storageOrder) {
            int p = pointer / strides[j];
            // this should not happen, if it happens than the strides are wrong from the very beginning
            if (p >= dim(j)) {
                throw new IllegalStateException("Could not compute index from pointer.");
            }
            index[j] = p;
            pointer -= p * strides[j];
        }
        return index;
    }

    public StrideLayout computeFortranLayout(Order askOrder, boolean compact) {
        int[] newDims;
        int[] newStrides;
        switch (askOrder) {
            case F -> {
                newDims = Arrays.copyOf(shape.dims(), shape.rank());
                newStrides = Arrays.copyOf(strides, shape.rank());
            }
            case C -> {
                newDims = Arrays.copyOf(shape.dims(), shape.rank());
                newStrides = Arrays.copyOf(strides, shape.rank());
                IntArrays.reverse(newDims);
                IntArrays.reverse(newStrides);
            }
            case S -> {
                int[] storageOrder = IntArrays.newSeq(shape.rank());
                IntArrays.quickSort(storageOrder,
                        IntComparators.asIntComparator(Comparator.comparingInt(this::stride).thenComparing(this::dim)));
                newDims = IntArrays.newPermutation(shape.dims(), storageOrder);
                newStrides = IntArrays.newPermutation(strides, storageOrder);
            }
            default -> throw new IllegalStateException();
        }
        if (!compact) {
            return new StrideLayout(Shape.of(newDims), offset, newStrides);
        }
        int len = compactFortranLayout(newDims, newStrides);
        return new StrideLayout(Shape.of(Arrays.copyOf(newDims, len)), offset, Arrays.copyOf(newStrides, len));
    }

    private int compactFortranLayout(int[] dims, int[] strides) {
        if (rank() < 2) {
            return rank();
        }
        int len = 1;
        for (int i = 1; i < dims.length; i++) {
            if (dims[len - 1] * strides[len - 1] == strides[i]) {
                dims[len - 1] *= dims[i];
                continue;
            }
            dims[len] = dims[i];
            strides[len] = strides[i];
            len++;
        }
        return len;
    }

    public StrideLayout computeCLayout(Order askOrder, boolean compact) {
        int[] newDims;
        int[] newStrides;
        switch (askOrder) {
            case F -> {
                newDims = Arrays.copyOf(shape.dims(), shape.rank());
                newStrides = Arrays.copyOf(strides, shape.rank());
                IntArrays.reverse(newDims);
                IntArrays.reverse(newStrides);
            }
            case C -> {
                newDims = Arrays.copyOf(shape.dims(), shape.rank());
                newStrides = Arrays.copyOf(strides, shape.rank());
            }
            case S -> {
                int[] storageOrder = IntArrays.newSeq(shape.rank());
                IntArrays.quickSort(storageOrder,
                        IntComparators.asIntComparator(Comparator.comparingInt(this::stride).thenComparing(this::dim).reversed()));
                newDims = IntArrays.newPermutation(shape.dims(), storageOrder);
                newStrides = IntArrays.newPermutation(strides, storageOrder);
            }
            default -> throw new IllegalStateException();
        }
        if (!compact) {
            return new StrideLayout(Shape.of(newDims), offset, newStrides);
        }
        int len = compactFortranLayout(newDims, newStrides);
        return new StrideLayout(Shape.of(Arrays.copyOf(newDims, len)), offset, Arrays.copyOf(newStrides, len));
    }

    private int compactCLayout(int[] dims, int[] strides) {
        if (rank() < 2) {
            return rank();
        }
        int len = 1;
        for (int i = 1; i < dims.length; i++) {
            if (strides[len - 1] == dims[i] * strides[i]) {
                dims[len - 1] *= dims[i];
                strides[len - 1] = strides[i];
                continue;
            }
            dims[len] = dims[i];
            strides[len] = strides[i];
            len++;
        }
        return len;
    }

    @Override
    public StrideLayout squeeze() {
        int count = shape.unitDimCount();
        if (count == 0) {
            return this;
        }
        int[] newDims = new int[rank() - count];
        int[] newStrides = new int[rank() - count];

        int pos = 0;
        for (int i = 0; i < rank(); i++) {
            if (dim(i) == 1) {
                continue;
            }
            newDims[pos] = dim(i);
            newStrides[pos] = strides[i];
            pos++;
        }
        return new StrideLayout(Shape.of(newDims), offset, newStrides);
    }

    @Override
    public StrideLayout squeeze(int axis) {
        if (dim(axis) != 1) {
            return this;
        }
        int[] newDims = new int[rank() - 1];
        int[] newStrides = new int[rank() - 1];
        for (int i = 0; i < rank(); i++) {
            if (i == axis) {
                continue;
            }
            newDims[i < axis ? i : i - 1] = dim(i);
            newStrides[i < axis ? i : i - 1] = strides[i];
        }
        return new StrideLayout(Shape.of(newDims), offset, newStrides);
    }

    @Override
    public StrideLayout unsqueeze(int axis) {
        if (axis < 0 || axis > rank()) {
            throw new IllegalArgumentException("Axis is out of bounds.");
        }
        int[] newDims = new int[rank() + 1];
        int[] newStrides = new int[rank() + 1];
        for (int i = 0; i < shape().rank(); i++) {
            newDims[i < axis ? i : i + 1] = dim(i);
            newStrides[i < axis ? i : i + 1] = stride(i);
        }
        newDims[axis] = 1;
        if (isCOrdered()) {
            newStrides[axis] = axis == rank() ? 1 : stride(axis);
        } else if (isFOrdered()) {
            newStrides[axis] = axis == 0 ? 1 : stride(axis);
        } else {
            newStrides[axis] = 1;
        }
        return StrideLayout.of(Shape.of(newDims), offset(), newStrides);
    }

    @Override
    public StrideLayout revert() {
        int[] reversedDims = IntArrays.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        int[] reversedStride = IntArrays.reverse(Arrays.copyOf(strides, shape.rank()));
        return StrideLayout.of(Shape.of(reversedDims), offset, reversedStride);
    }

    @Override
    public StrideLayout moveAxis(int src, int dst) {
        if (src < 0 || src >= shape.rank()) {
            throw new IllegalArgumentException("Source axis has an invalid value.");
        }
        if (dst < 0 || dst >= shape.rank()) {
            throw new IllegalArgumentException("Destination position has an invalid value.");
        }
        if (src == dst) {
            return this;
        }
        int[] askDims = IntArrays.copy(shape.dims());
        int[] askStrides = IntArrays.copy(strides);
        int tmpDim = askDims[src];
        int tmpStride = askStrides[src];
        for (int i = src; i < dst; i++) {
            askDims[i] = askDims[i + 1];
            askStrides[i] = askStrides[i + 1];
        }
        askDims[dst] = tmpDim;
        askStrides[dst] = tmpStride;
        return StrideLayout.of(Shape.of(askDims), offset, askStrides);
    }

    @Override
    public StrideLayout swapAxis(int src, int dst) {
        if (src < 0 || src >= shape.rank()) {
            throw new IllegalArgumentException("Source axis has an invalid value.");
        }
        if (dst < 0 || dst >= shape.rank()) {
            throw new IllegalArgumentException("Destination position has an invalid value.");
        }
        if (src == dst) {
            return this;
        }
        int[] askDims = IntArrays.copy(shape.dims());
        int[] askStrides = IntArrays.copy(strides);

        IntArrays.swap(askDims, src, dst);
        IntArrays.swap(askStrides, src, dst);

        return StrideLayout.of(Shape.of(askDims), offset, askStrides);
    }

    @Override
    public StrideLayout narrow(int axis, int start, int end) {
        return narrow(axis, true, start, end);
    }

    @Override
    public StrideLayout narrow(int axis, boolean keepdim, int start, int end) {
        if (axis < 0 || axis >= strides.length) {
            throw new IllegalArgumentException("Axis is out of bounds.");
        }
        if (rank() == 1) {
            return StrideLayout.of(
                    Shape.of(end - start),
                    offset + stride(axis) * start,
                    strides
            );
        }
        int[] newDims = Arrays.copyOf(shape.dims(), strides.length);
        newDims[axis] = end - start;
        int newOffset = offset + start * stride(axis);
        var result = StrideLayout.of(Shape.of(newDims), newOffset, strides);
        return keepdim ? result : result.squeeze(axis);
    }

    @Override
    public StrideLayout narrowAll(int[] starts, int[] ends) {
        return narrowAll(true, starts, ends);
    }

    @Override
    public StrideLayout narrowAll(boolean keepdim, int[] starts, int[] ends) {
        if (strides.length != starts.length) {
            throw new IllegalArgumentException("Start arrays must have dimension equal with rank.");
        }
        if (starts.length != ends.length) {
            throw new IllegalArgumentException("Starts and ends does not have the same length.");
        }
        int[] newDims = Arrays.copyOf(dims(), strides.length);
        int newOffset = offset;
        for (int i = 0; i < newDims.length; i++) {
            newDims[i] = ends[i] - starts[i];
            newOffset += starts[i] * strides[i];

        }
        return StrideLayout.of(Shape.of(newDims), newOffset, strides);
    }

    @Override
    public StrideLayout permute(int[] dims) {
        if (strides.length != dims.length) {
            throw new IllegalArgumentException("Numer of dimension is not equal with rank.");
        }
        boolean[] flags = new boolean[rank()];
        int flagCount = 0;
        for (int dim : dims) {
            if (dim < 0 || dim >= rank() - 1) {
                throw new IllegalArgumentException("Dimension value is invalid: [" +
                        IntStream.of(dims).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "]");
            }
            if (!flags[dim]) {
                flags[dim] = true;
                flagCount++;
            }
        }
        if (flagCount != rank()) {
            throw new IllegalArgumentException("Dimension values contains duplicates: [" +
                    IntStream.of(dims).mapToObj(String::valueOf).collect(Collectors.joining("")) + "]");
        }
        int[] newDims = IntArrays.newPermutation(dims(), dims);
        int[] newStrides = IntArrays.newPermutation(strides, dims);
        return StrideLayout.of(Shape.of(newDims), offset, newStrides);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StrideLayout layout = (StrideLayout) o;
        return offset == layout.offset && shape.equals(layout.shape) && Arrays.equals(strides, layout.strides);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(shape, offset);
        result = 31 * result + Arrays.hashCode(strides);
        return result;
    }

    @Override
    public String toString() {
        List<String> flagString = new ArrayList<>();
        if (isCOrdered()) {
            flagString.add("C_DENSE");
        }
        if (isFOrdered()) {
            flagString.add("F_DENSE");
        }
        return "StrideLayout{"
                + "shape=[" + IntStream.of(shape.dims()).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "],"
                + "offset=" + offset + ","
                + "strides=[" + IntStream.of(strides).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "],"
                + "flags=[" + String.join(",", flagString) + "]"
                + "}";
    }
}
