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

/**
 * Describes the shape of a tensor. A tensor is a multidimensional array which stores numbers of the same type.
 * The shape has a number of dimensions described by {@link #size()} and an array of integers of length
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

    private Shape(int[] dims) {
        for (int dimSize : dims) {
            if (dimSize <= 0) {
                throw new IllegalArgumentException(
                        "Invalid shape dimension: " + Arrays.stream(dims).mapToObj(String::valueOf).collect(
                                Collectors.joining(",", "[", "]")));
            }
        }
        this.dims = Arrays.copyOf(dims, dims.length);
    }

    /**
     * @return number of dimensions
     */
    public int size() {
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
     *
     * @param pos dimension index
     * @return size of the given dimension
     */
    public int dim(int pos) {
        return dims[pos];
    }

    @Override
    public String toString() {
        return "Shape: " + Arrays.stream(dims).mapToObj(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }
}
