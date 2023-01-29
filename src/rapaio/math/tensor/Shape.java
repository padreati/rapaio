/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor;

import java.util.Arrays;
import java.util.stream.Collectors;

import rapaio.util.collection.IntArrays;

/**
 * Describes the shape of a tensor. A tensor is a multidimensional array which stores numbers of the same type.
 * The shape has a number of dimensions described by {@link #rank()} and an array of integers of length
 * equal with the number of dimensions where each integer is a positive number describing the size
 * of that dimension. The size of a dimension must be a positive number.
 * <p>
 * Some particular cases are:
 * <ul>
 *     <li>[]</li> - scalar, no dimensions
 *     <li>[n1]</li> - vector of size n1
 *     <li>[n1,n2]</li> - matrix of size n1 x n2
 * </ul>
 */
public final class Shape {

    public static Shape of(int... dims) {
        return new Shape(dims);
    }

    private final int[] dims;
    private final int[] rowMajorOffsets;
    private final int[] colMajorOffsets;

    private Shape(int[] dims) {
        for (int dimSize : dims) {
            if (dimSize <= 0) {
                throw new IllegalArgumentException(
                        "Invalid shape dimension: " + Arrays.stream(dims).mapToObj(String::valueOf).collect(
                                Collectors.joining(",", "[", "]")));
            }
        }
        this.dims = Arrays.copyOf(dims, dims.length);
        this.rowMajorOffsets = IntArrays.newFill(dims.length, 1);
        this.colMajorOffsets = IntArrays.newFill(dims.length, 1);
        for (int i = 1; i < rowMajorOffsets.length; i++) {
            for (int j = 0; j < i; j++) {
                rowMajorOffsets[j] *= dims[i];
            }
        }
        for (int i = colMajorOffsets.length - 2; i >= 0; i--) {
            for (int j = colMajorOffsets.length - 1; j > i; j--) {
                colMajorOffsets[j] *= dims[i];
            }
        }
    }

    /**
     * @return number of dimensions
     */
    public int rank() {
        return dims.length;
    }

    /**
     * @return array with dimension sizes
     */
    public int[] dims() {
        return dims;
    }

    /**
     * Size of a single dimension.
     * If the dimension is a negative number it returns rank + position
     *
     * @param pos dimension index
     * @return size of the given dimension
     */
    public int dim(int pos) {
        return pos >= 0 ? dims[pos] : dims[dims.length + pos];
    }

    /**
     * Size of the shape which is equal with the product of all dimension sizes.
     *
     * @return size of the shape
     */
    public int size() {
        int prod = 1;
        for (int dim : dims) {
            prod *= dim;
        }
        return prod;
    }

    /**
     * Computes the index for a position in a given order.
     * The order could be {@link rapaio.math.tensor.Tensor.Order#RowMajor} or {@link rapaio.math.tensor.Tensor.Order#ColMajor}.
     * For other orders an exception is thrown.
     * <p>
     * The position is the ordered number of the value for a given order.
     * <p>
     * The resulted index is computed for a given order and position.
     *
     * @param order row or column major
     * @param pos   position in a given order
     * @return the computed index for the pos-th elements in the given order
     */
    public int[] index(Tensor.Order order, int pos) {
        switch (order) {
            case RowMajor -> {
                int[] index = new int[dims.length];
                for (int i = 0; i < dims.length; i++) {
                    index[i] = pos / rowMajorOffsets[i];
                    pos = pos % rowMajorOffsets[i];
                }
                return index;
            }
            case ColMajor -> {
                int[] index = new int[dims.length];
                for (int i = dims.length - 1; i >= 0; i--) {
                    index[i] = pos / colMajorOffsets[i];
                    pos = pos % colMajorOffsets[i];
                }
                return index;
            }
            default -> throw new IllegalArgumentException("Indexing order not allowed.");
        }
    }

    /**
     * Computes the position of the element for a given index in the specified order.
     *
     * @param order desired order
     * @param idxs  int array which described the element index
     * @return position of the element with index in specified order
     */
    public int position(Tensor.Order order, int... idxs) {
        int[] offsets = switch (order) {
            case RowMajor -> rowMajorOffsets;
            case ColMajor -> colMajorOffsets;
            default -> throw new IllegalArgumentException("Position order not allowed.");
        };
        int pos = 0;
        for (int i = 0; i < idxs.length; i++) {
            pos += offsets[i] * idxs[i];
        }
        return pos;
    }

    public Shape perm(int[] perm) {
        return new Shape(IntArrays.newPermutation(dims, perm));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Shape shape = (Shape) o;
        return Arrays.equals(dims, shape.dims);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dims);
    }

    @Override
    public String toString() {
        return "Shape: " + Arrays.stream(dims).mapToObj(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }
}
