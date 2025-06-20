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

package rapaio.darray.layout;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.util.IntComparators;
import rapaio.util.collection.Ints;

public class ArrayStrideLayout extends AbstractStrideLayout {

    private static final int C_DENSE = 1;
    private static final int F_DENSE = 2;

    private final Shape shape;
    private final int offset;
    private final int[] strides;
    private int flags;

    public ArrayStrideLayout(Shape shape, int offset, int[] strides) {
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

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int[] strides() {
        return strides;
    }

    @Override
    public int stride(int i) {
        if (strides.length == 0) {
            return 1;
        }
        return i >= 0 ? strides[i] : strides[i + strides.length];
    }

    @Override
    public int[] narrowStrides(int axis) {
        int[] newStrides = new int[strides.length - 1];
        if (axis < 0) {
            axis += strides.length;
        }
        System.arraycopy(strides, 0, newStrides, 0, axis);
        if (newStrides.length - axis > 0) {
            System.arraycopy(strides, axis + 1, newStrides, axis, newStrides.length - axis);
        }
        return newStrides;
    }

    @Override
    public int rank() {
        return shape.rank();
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
        int[] storageOrder = Ints.seq(shape.rank());
        // sort indexes by strides, dims in decreasing order
        Ints.quickSort(storageOrder,
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

    @Override
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
                Ints.reverse(newDims);
                Ints.reverse(newStrides);
            }
            case S -> {
                int[] storageOrder = Ints.seq(shape.rank());
                Ints.quickSort(storageOrder,
                        (i, j) -> {
                            if (strides[i] == 0 && strides[j] == 0) {
                                return Integer.compare(dim(i), dim(j));
                            }
                            if (strides[i] == 0) {
                                return 1;
                            }
                            if (strides[j] == 0) {
                                return -1;
                            }
                            int cmp = Integer.compare(strides[i], strides[j]);
                            if (cmp != 0) {
                                return cmp;
                            }
                            return Integer.compare(dim(i), dim(j));
                        });
                newDims = Ints.newPermutation(shape.dims(), storageOrder);
                newStrides = Ints.newPermutation(strides, storageOrder);
            }
            default -> throw new IllegalStateException();
        }
        if (!compact) {
            return StrideLayout.of(Shape.of(newDims), offset, newStrides);
        }
        int len = compactFortranLayout(newDims, newStrides);
        return StrideLayout.of(Shape.of(Arrays.copyOf(newDims, len)), offset, Arrays.copyOf(newStrides, len));
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
        return StrideLayout.of(Shape.of(newDims), offset, newStrides);
    }

    @Override
    public StrideLayout squeeze(int... axes) {
        if (axes.length == 0) {
            return this;
        }
        if (axes.length > rank()) {
            throw new IllegalArgumentException("Matrix allows maximum two axes as parameters.");
        }
        if (Ints.containsDuplicates(axes)) {
            throw new IllegalArgumentException("Duplicates values in axis parameters.");
        }

        int len = 0;
        int[] newDims = new int[rank()];
        int[] newStrides = new int[rank()];

        for (int i = 0; i < rank(); i++) {
            boolean found = false;
            for (int axis : axes) {
                if (axis == i) {
                    found = true;
                    break;
                }
            }
            if (!found || dim(i) > 1) {
                newDims[len] = dim(i);
                newStrides[len] = strides[i];
                len++;
            }
        }
        return new ArrayStrideLayout(Shape.of(Arrays.copyOf(newDims, len)), offset, Arrays.copyOf(newStrides, len));
    }

    @Override
    public StrideLayout stretch(int... axes) {
        if (axes == null || axes.length == 0) {
            return this;
        }
        for (int axis : axes) {
            if (axis < 0 || axis > rank() + 1) {
                throw new IndexOutOfBoundsException();
            }
        }
        if (Ints.containsDuplicates(axes)) {
            throw new IllegalArgumentException("Axes contains duplicates.");
        }

        int len = rank() + axes.length;
        int[] newDims = Ints.fill(len, 1);
        int[] newStrides = Ints.fill(len, 0);

        int lastDim = 0;
        for (int i = 0; i < len; i++) {
            boolean found = false;
            for (int axis : axes) {
                if (axis == i) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newDims[i] = dim(lastDim);
                newStrides[i] = strides[lastDim];
                lastDim++;
            }
        }
        return StrideLayout.of(Shape.of(newDims), offset(), newStrides);
    }

    @Override
    public StrideLayout expand(int axis, int size) {
        if (dim(axis) != 1) {
            throw new IllegalArgumentException("Dimension " + axis + " must have size 1, but have size " + dim(axis) + ".");
        }
        if (axis < 0) {
            throw new IllegalArgumentException("Dimension of the new axis " + axis + " must be positive.");
        }
        int[] newDims = Arrays.copyOf(dims(), dims().length);
        int[] newStrides = Arrays.copyOf(strides, strides.length);
        newDims[axis] = size;
        newStrides[axis] = 0;
        return StrideLayout.of(Shape.of(newDims), offset, newStrides);
    }

    @Override
    public StrideLayout revert() {
        int[] reversedDims = Ints.reverse(Arrays.copyOf(shape.dims(), shape.rank()));
        int[] reversedStride = Ints.reverse(Arrays.copyOf(strides, shape.rank()));
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
        int[] askDims = Ints.copy(shape.dims());
        int[] askStrides = Ints.copy(strides);
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
        int[] askDims = Ints.copy(shape.dims());
        int[] askStrides = Ints.copy(strides);

        Ints.swap(askDims, src, dst);
        Ints.swap(askStrides, src, dst);

        return StrideLayout.of(Shape.of(askDims), offset, askStrides);
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
            throw new IllegalArgumentException("Number of dimension is not equal with rank.");
        }
        boolean[] flags = new boolean[rank()];
        int flagCount = 0;
        for (int dim : dims) {
            if (dim < 0 || dim >= rank()) {
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
        int[] newDims = Ints.newPermutation(dims(), dims);
        int[] newStrides = Ints.newPermutation(strides, dims);
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
        ArrayStrideLayout layout = (ArrayStrideLayout) o;
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
        return "TensorStride([" +
                IntStream.of(shape.dims()).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "]," +
                offset + ",[" + IntStream.of(strides).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "])";
    }
}
