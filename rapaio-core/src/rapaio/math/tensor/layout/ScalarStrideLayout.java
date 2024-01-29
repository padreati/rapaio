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

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;

public record ScalarStrideLayout(int offset) implements StrideLayout {

    private static final Shape shape = Shape.of();

    @Override
    public Shape shape() {
        return shape;
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
    public StrideLayout squeeze(int axis) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public StrideLayout unsqueeze(int axis) {
        if (axis != 0) {
            throw new IndexOutOfBoundsException();
        }
        return StrideLayout.of(Shape.of(1), offset, new int[] {1});
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
    public String toString() {
        return STR."ScalarStride([],\{offset},[])";
    }
}
