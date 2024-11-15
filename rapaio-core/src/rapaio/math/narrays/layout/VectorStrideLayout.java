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

package rapaio.math.narrays.layout;

import java.util.Arrays;

import rapaio.math.narrays.Order;
import rapaio.math.narrays.Shape;
import rapaio.util.collection.IntArrays;

public final class VectorStrideLayout extends AbstractStrideLayout {

    private final Shape shape;
    private final int offset;
    private final int stride;

    public VectorStrideLayout(Shape shape, int offset, int[] strides) {
        if (shape.rank() != 1 || strides == null || strides.length != 1) {
            throw new IllegalArgumentException(
                    "Shape or strides invalid for one dimensional tensors (shape:" + shape + ", strides:" + Arrays.toString(strides)
                            + ").");
        }
        this.shape = shape;
        this.offset = offset;
        this.stride = strides[0];
    }

    @Override
    public Shape shape() {
        return shape;
    }

    @Override
    public int rank() {
        return 1;
    }

    @Override
    public boolean isCOrdered() {
        return true;
    }

    @Override
    public boolean isFOrdered() {
        return true;
    }

    @Override
    public boolean isDense() {
        return false;
    }

    @Override
    public Order storageFastOrder() {
        return Order.C;
    }

    @Override
    public int pointer(int... index) {
        return offset + index[0] * stride;
    }

    @Override
    public int[] index(int pointer) {
        return new int[] {(pointer - offset) / stride};
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public int[] strides() {
        return new int[] {stride};
    }

    @Override
    public int stride(int i) {
        if (i < 0) {
            i += 1;
        }
        if (i == 0) {
            return stride;
        }
        throw new IllegalArgumentException("Invalid stride index " + i);
    }

    @Override
    public StrideLayout squeeze() {
        if (dim(0) == 1) {
            return StrideLayout.of(Shape.of(), offset, new int[0]);
        }
        return this;
    }

    @Override
    public StrideLayout squeeze(int... axes) {
        if (axes.length == 0) {
            return this;
        }
        if (axes.length == 1 && axes[0] != 0) {
            throw new IllegalArgumentException("Invalid axis value: " + axes[0] + ".");
        }
        if (axes.length > 1) {
            throw new IllegalArgumentException("Vectors accepts a single axis parameter");
        }
        return squeeze();
    }

    @Override
    public StrideLayout stretch(int... axes) {
        for (int axis : axes) {
            if (axis < 0 || axis >= axes.length + 1) {
                throw new IndexOutOfBoundsException();
            }
        }
        if (IntArrays.containsDuplicates(axes)) {
            throw new IllegalArgumentException("Axes contains duplicates.");
        }

        int len = axes.length + 1;
        int[] newDims = IntArrays.newFill(len, 1);
        int[] newStrides = IntArrays.newFill(len, 0);

        for (int i = 0; i < len; i++) {
            boolean found = false;
            for (int axis : axes) {
                if (axis == i) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                newDims[i] = dim(0);
                newStrides[i] = stride;
                break;
            }
        }
        return StrideLayout.of(Shape.of(newDims), offset(), newStrides);
    }

    @Override
    public StrideLayout expand(int axis, int size) {
        if (dim(axis) != 1) {
            throw new IllegalArgumentException("Dimension " + axis + " must have size 1, but have size " + dim(axis) + ".");
        }
        if (axis != 0) {
            throw new IllegalArgumentException("Dimension of the new axis " + axis + " must be zero.");
        }
        int[] newDims = Arrays.copyOf(dims(), dims().length);
        int[] newStrides = Arrays.copyOf(strides(), strides().length);
        newDims[axis] = size;
        newStrides[axis] = 0;
        return StrideLayout.of(Shape.of(newDims), offset, newStrides);
    }

    @Override
    public StrideLayout revert() {
        return this;
    }

    @Override
    public StrideLayout moveAxis(int src, int dst) {
        if (src != 0 || dst != 0) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    @Override
    public StrideLayout swapAxis(int src, int dst) {
        if (src != 0 || dst != 0) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    @Override
    public StrideLayout narrow(int axis, boolean keepDim, int start, int end) {
        if (axis != 0) {
            throw new IllegalArgumentException("Invalid axis value: " + axis);
        }
        if (!keepDim && (end - start != 1)) {
            throw new IllegalArgumentException("Invalid value for keepDim: " + keepDim + " if the resulting tensor is not a scalar.");
        }
        return keepDim ?
                StrideLayout.of(Shape.of(end - start), offset + stride * start, new int[] {stride}) :
                StrideLayout.of(Shape.of(), offset + stride * start, new int[0]);
    }

    @Override
    public StrideLayout narrowAll(boolean keepDim, int[] starts, int[] ends) {
        if (starts == null || ends == null) {
            throw new IllegalArgumentException("Starts and ends cannot be null.");
        }
        if (starts.length != 1 || ends.length != 1) {
            throw new IllegalArgumentException("Starts and ends must have length 1.");
        }
        return narrow(0, keepDim, starts[0], ends[0]);
    }

    @Override
    public StrideLayout permute(int[] dims) {
        if (dims == null || dims.length != 1 || dims[0] != 0) {
            throw new IllegalArgumentException("Permutation indices are not valid: " + Arrays.toString(dims));
        }
        return this;
    }

    @Override
    public StrideLayout computeFortranLayout(Order askOrder, boolean compact) {
        return this;
    }

    @Override
    public int[] narrowStrides(int axis) {
        return new int[0];
    }

    @Override
    public StrideLayout attemptReshape(Shape shape, Order askOrder) {
        if (Order.S == askOrder) {
            throw new IllegalArgumentException("Requested order must be Order.C or Order.F.");
        }
        int[] newStrides = new int[shape.rank()];
        if (Order.F == askOrder) {
            newStrides[0] = stride;
            for (int i = 1; i < newStrides.length; i++) {
                newStrides[i] = newStrides[i - 1] * shape.dim(i - 1);
            }
        } else {
            newStrides[newStrides.length - 1] = stride;
            for (int i = newStrides.length - 2; i >= 0; i--) {
                newStrides[i] = newStrides[i + 1] * shape.dim(i + 1);
            }
        }
        return StrideLayout.of(shape, offset, newStrides);
    }

    @Override
    public String toString() {
        return "VectorStride([" + dim(0) + "]," + offset + ",[" + stride(0) + "])";
    }
}
