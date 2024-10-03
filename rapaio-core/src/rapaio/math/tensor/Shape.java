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

import java.util.Arrays;
import java.util.stream.Collectors;

import rapaio.util.collection.IntArrays;

/**
 * Describes the shape of a NDArray. A NDArray is a multidimensional array which stores numbers of the same type.
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
    private final int size;
    private int[] cStrides = null;
    private int[] fStrides = null;

    private Shape(int[] dims) {
        for (int dim : dims) {
            if (dim <= 0) {
                throw new IllegalArgumentException("Invalid shape dimensions: [" + Arrays.stream(dims)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")) + "].");
            }
        }
        /*
        This limit is imposed by the maximum length of an allocated array in Java.
        If there are other considerations, we could raise or lower this limit.
         */
        long longSize = 1;
        for (int dim : dims) {
            longSize *= dim;
            if (longSize > Integer.MAX_VALUE - 2) {
                throw new IllegalArgumentException("Shape exceeds maximum number of elements which is Integer.MAX_VALUE - 2");
            }
        }
        this.dims = dims;
        this.size = (int) longSize;
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
        if (dims.length == 0) {
            return 1;
        }
        return pos >= 0 ? dims[pos] : dims[dims.length + pos];
    }

    public int[] narrowDims(int axis) {
        int[] td = new int[dims.length - 1];
        if (axis < 0) {
            axis += dims.length;
        }
        System.arraycopy(dims, 0, td, 0, axis);
        if (td.length - axis > 0) {
            System.arraycopy(dims, axis + 1, td, axis, td.length - axis);
        }
        return td;
    }

    /**
     * Size of the shape which is equal with the product of all dimension sizes.
     *
     * @return size of the shape
     */
    public int size() {
        return size;
    }

    /**
     * Computes the index for a position in a given order.
     * The order could be {@link Order#C} or {@link Order#F}.
     * For other orders an exception is thrown.
     * <p>
     * The position is the ordered number of the value for a given order.
     * <p>
     * The resulted index is computed for a given order and position.
     *
     * @param askOrder row or column major
     * @param pos      position in a given order
     * @return the computed index for the pos-th elements in the given order
     */
    public int[] index(Order askOrder, int pos) {
        int[] strides = strides(askOrder);
        int[] index = new int[dims.length];
        askOrder = Order.autoFC(askOrder);
        if (askOrder == Order.C) {
            for (int i = 0; i < dims.length; i++) {
                index[i] = pos / strides[i];
                pos = pos % strides[i];
            }
        } else {
            for (int i = dims.length - 1; i >= 0; i--) {
                index[i] = pos / strides[i];
                pos = pos % strides[i];
            }
        }
        return index;
    }

    /**
     * Computes the position of the element for a given index in the specified order.
     *
     * @param askOrder desired order
     * @param indexes  int array which described the element index
     * @return position of the element with index in specified order
     */
    public int position(Order askOrder, int... indexes) {
        int[] strides = strides(askOrder);
        int pos = 0;
        for (int i = 0; i < indexes.length; i++) {
            pos += strides[i] * indexes[i];
        }
        return pos;
    }

    private int[] cStrides() {
        if (cStrides == null) {
            cStrides = IntArrays.newFill(dims.length, 1);
            for (int i = 1; i < cStrides.length; i++) {
                for (int j = 0; j < i; j++) {
                    cStrides[j] *= dims[i];
                }
            }
        }
        return cStrides;
    }

    private int[] fStrides() {
        if (fStrides == null) {
            fStrides = IntArrays.newFill(dims.length, 1);
            for (int i = fStrides.length - 2; i >= 0; i--) {
                for (int j = fStrides.length - 1; j > i; j--) {
                    fStrides[j] *= dims[i];
                }
            }
        }
        return fStrides;
    }

    private int[] strides(Order askOrder) {
        return switch (askOrder) {
            case F -> fStrides();
            case C -> cStrides();
            default -> throw new IllegalArgumentException("Indexing order not allowed.");
        };
    }

    public int unitDimCount() {
        int count = 0;
        for (int dim : dims) {
            if (dim == 1) {
                count++;
            }
        }
        return count;
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
        return "Shape: [" + Arrays.stream(dims).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "]";
    }
}
