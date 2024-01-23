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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.util.collection.IntArrays;

public class MatrixStrideLayout implements StrideLayout {

    private final Shape shape;
    private final int offset;
    private final int[] strides;

    public MatrixStrideLayout(Shape shape, int offset, int[] strides) {
        if (shape.rank() != 2) {
            throw new IllegalArgumentException("Shape is not of rank 2.");
        }
        if (strides == null || strides.length != 2) {
            throw new IllegalArgumentException("Strides are not of rank 2.");
        }
        this.shape = shape;
        this.offset = offset;
        this.strides = strides;
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
        if (i < 0) {
            i += 2;
        }
        return strides[i];
    }

    @Override
    public int rank() {
        return 2;
    }

    @Override
    public boolean isCOrdered() {
        return isValidCOrder();
    }

    @Override
    public boolean isFOrdered() {
        return isValidFOrder();
    }

    private boolean isValidFOrder() {
        return strides[1] == strides[0] * shape.dim(0);
    }

    private boolean isValidCOrder() {
        return strides[0] == strides[1] * shape.dim(1);
    }

    @Override
    public boolean isDense() {
        return (isValidCOrder() && strides[1] == 1) || (isValidFOrder() && strides[0] == 1);
    }

    @Override
    public Order storageFastOrder() {
        if (isFOrdered()) {
            return Order.F;
        }
        if (isCOrdered()) {
            return Order.C;
        }
        return Order.S;
    }

    @Override
    public int pointer(int... index) {
        return offset + index[0] * strides[0] + index[1] * strides[1];
    }

    @Override
    public int[] index(int pointer) {
        int pos = strides[0] > strides[1] ? 1 : 0;
        int[] index = new int[2];

        int p = pointer / strides[pos];
        index[pos] = p;
        pointer -= p * strides[pos];
        pos = pos == 0 ? 1 : 0;
        p = pointer / strides[pos];
        index[pos] = p;

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
                IntArrays.reverse(newDims);
                IntArrays.reverse(newStrides);
            }
            case S -> {
                int[] storageOrder = IntArrays.newSeq(shape.rank());
                IntArrays.quickSort(storageOrder,
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
                newDims = IntArrays.newPermutation(shape.dims(), storageOrder);
                newStrides = IntArrays.newPermutation(strides, storageOrder);
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
        if (dims[0] * strides[0] == strides[1]) {
            dims[0] *= dims[1];
            return 1;
        }
        return 2;
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
        return new TensorStrideLayout(Shape.of(newDims), offset, newStrides);
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
        return keepdim ? result : (StrideLayout) result.squeeze(axis);
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
        MatrixStrideLayout layout = (MatrixStrideLayout) o;
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
