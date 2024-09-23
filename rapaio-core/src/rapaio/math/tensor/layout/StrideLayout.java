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

import rapaio.math.tensor.Layout;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.util.collection.IntArrays;

public interface StrideLayout extends Layout {

    static StrideLayout of(int[] dims, int offset, int[] strides) {
        return of(Shape.of(dims), offset, strides);
    }

    static StrideLayout of(Shape shape, int offset, int[] strides) {
        if (shape.rank() != strides.length) {
            throw new IllegalArgumentException(
                    "Dimensions and strides must have same length (dim size: " + shape.rank() + ", stride size: " + strides.length + ".");
        }
        return switch (shape.rank()) {
            case 0 -> new ScalarStrideLayout(offset);
            case 1 -> new VectorStrideLayout(shape, offset, strides);
            case 2 -> new MatrixStrideLayout(shape, offset, strides);
            default -> new TensorStrideLayout(shape, offset, strides);
        };
    }

    static StrideLayout ofDense(Shape shape, int offset, Order order) {
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
        return new TensorStrideLayout(shape, offset, strides);
    }

    int offset();

    int[] strides();

    int stride(int i);

    StrideLayout squeeze();

    StrideLayout squeeze(int... axes);

    StrideLayout stretch(int... axes);

    StrideLayout expand(int axis, int size);

    StrideLayout revert();

    StrideLayout moveAxis(int src, int dst);

    StrideLayout swapAxis(int src, int dst);

    default StrideLayout narrow(int axis, int start, int end) {
        return narrow(axis, true, start, end);
    }

    StrideLayout narrow(int axis, boolean keepDim, int start, int end);

    default StrideLayout narrowAll(int[] starts, int[] ends) {
        return narrowAll(true, starts, ends);
    }

    StrideLayout narrowAll(boolean keepDim, int[] starts, int[] ends);

    StrideLayout permute(int... dims);

    StrideLayout computeFortranLayout(Order askOrder, boolean compact);

    int[] narrowStrides(int axis);

    /**
     * Attempts to computes a stride layout for reshape, if possible.
     * <p>
     * The shape should be compatible (have the same size as original), but this validation is not checked, it is left in the scope
     * of caller. This is to distinguish between cases when a reshape is invalid or requires copy.
     * <p>
     * If the attempt fails, which means a new copy of the data is required, the returned stride array is null.
     * If no copy is needed, returns a new stride array prepared for use on the new tensor.
     * <p>
     * The "askOrder" argument describes how the array should be viewed during the reshape, not how it is stored in memory.
     * If a copy is needed, this will be also the order of storage for the new copy.
     * <p>
     * If some output dimensions have length 1, the strides assigned to them are arbitrary. In the current implementation, they are the
     * stride of the next-fastest index.
     */
    StrideLayout attemptReshape(Shape shape, Order askOrder);
}
