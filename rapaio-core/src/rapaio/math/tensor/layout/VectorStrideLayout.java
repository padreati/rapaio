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

import java.util.Arrays;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;

public final class VectorStrideLayout implements StrideLayout {

    private final Shape shape;
    private final int offset;
    private final int stride;

    public VectorStrideLayout(Shape shape, int offset, int[] strides) {
        if (shape.rank() != 1 || strides == null || strides.length != 1) {
            throw new IllegalArgumentException(
                    STR."Shape or strides invalid for one dimensional tensors (shape:\{shape}, strides:\{Arrays.toString(strides)}).");
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
        throw new IllegalArgumentException(STR."Invalid stride index \{i}");
    }

    @Override
    public StrideLayout squeeze() {
        if (dim(0) == 1) {
            return StrideLayout.of(Shape.of(), offset, new int[0]);
        }
        return this;
    }

    @Override
    public StrideLayout squeeze(int axis) {
        if (axis != 0) {
            throw new IllegalArgumentException(STR."Invalid axis value: \{axis}.");
        }
        return squeeze();
    }

    @Override
    public StrideLayout unsqueeze(int axis) {
        if (axis < 0 || axis > 1) {
            throw new IllegalArgumentException("Axis is out of bounds.");
        }
        int[] newDims = new int[2];
        int[] newStrides = new int[2];
        if (axis == 0) {
            newDims[0] = 1;
            newDims[1] = dim(0);
            newStrides[1] = stride;
        } else {
            newDims[0] = dim(0);
            newDims[1] = 1;
            newStrides[0] = stride;
        }
        return StrideLayout.of(Shape.of(newDims), offset(), newStrides);
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
            throw new IllegalArgumentException(STR."Invalid axis value: \{axis}");
        }
        if (!keepDim && (end - start != 1)) {
            throw new IllegalArgumentException(STR."Invalid value for keepDim: \{keepDim} if the resulting tensor is not a scalar.");
        }
        return keepDim ?
                StrideLayout.of(Shape.of(end - start), offset + stride * start, new int[] {stride}) :
                StrideLayout.of(Shape.of(), offset + stride * start, new int[0]);
    }

    @Override
    public StrideLayout narrowAll(boolean keepDim, int[] starts, int[] ends) {
        if(starts==null || ends==null) {
            throw new IllegalArgumentException("Starts and ends cannot be null.");
        }
        if(starts.length!=1|| ends.length!=1) {
            throw new IllegalArgumentException("Starts and ends must have length 1.");
        }
        return narrow(0, keepDim, starts[0], ends[0]);
    }

    @Override
    public StrideLayout permute(int[] dims) {
        if (dims == null || dims.length != 1 || dims[0] != 0) {
            throw new IllegalArgumentException(STR."Permutation indices are not valid: \{Arrays.toString(dims)}");
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
}
