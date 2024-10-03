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

package rapaio.math.tensor.layout;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.util.collection.IntArrays;

public record ScalarStrideLayout(int offset) implements StrideLayout {

    private static final Shape SCALAR_SHAPE = Shape.of();

    @Override
    public Shape shape() {
        return SCALAR_SHAPE;
    }

    @Override
    public int rank() {
        return 0;
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
        return true;
    }

    @Override
    public Order storageFastOrder() {
        return Order.C;
    }

    @Override
    public int pointer(int... index) {
        if (index.length != 0) {
            throw new IndexOutOfBoundsException();
        }
        return offset;
    }

    @Override
    public int[] index(int pointer) {
        if (pointer != offset) {
            throw new IndexOutOfBoundsException();
        }
        return new int[0];
    }

    @Override
    public int[] strides() {
        return new int[0];
    }

    @Override
    public int stride(int i) {
        return 0;
    }

    @Override
    public StrideLayout squeeze() {
        return this;
    }

    @Override
    public StrideLayout squeeze(int... axes) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout stretch(int... axes) {
        if (axes == null || axes.length == 0) {
            return this;
        }
        for (int axis : axes) {
            if (axis < 0 || axis >= axes.length) {
                throw new IndexOutOfBoundsException();
            }
        }
        if (IntArrays.containsDuplicates(axes)) {
            throw new IllegalArgumentException("Axes contains duplicates.");
        }
        int[] dims = IntArrays.newFill(axes.length, 1);
        int[] strides = IntArrays.newFill(axes.length, 0);
        return StrideLayout.of(Shape.of(dims), offset, strides);
    }

    @Override
    public StrideLayout expand(int axis, int size) {
        throw new IllegalArgumentException("Scalar stride cannot be expanded.");
    }

    @Override
    public StrideLayout revert() {
        return this;
    }

    @Override
    public StrideLayout moveAxis(int src, int dst) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout swapAxis(int src, int dst) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout narrow(int axis, boolean keepDim, int start, int end) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout narrowAll(boolean keepDim, int[] starts, int[] ends) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout permute(int[] dims) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout computeFortranLayout(Order askOrder, boolean compact) {
        return this;
    }

    @Override
    public int[] narrowStrides(int axis) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout attemptReshape(Shape shape, Order askOrder) {
        return StrideLayout.of(shape, offset, new int[rank()]);
    }

    @Override
    public String toString() {
        return "ScalarStride([]," + offset + ",[])";
    }
}
