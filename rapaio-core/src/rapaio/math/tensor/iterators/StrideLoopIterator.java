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

package rapaio.math.tensor.iterators;

import java.util.Arrays;

import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.layout.StrideLayout;
import rapaio.util.collection.IntArrays;

public final class StrideLoopIterator implements LoopIterator {

    public final int size;
    public final int step;
    public final int bound;
    public final int count;

    private final int outerOffset;
    private final Shape outerShape;
    private final int[] outerStrides;
    private final PointerIterator it;

    public StrideLoopIterator(Shape shape, int offset, int[] strides, Order askOrder) {
        this(new StrideLayout(shape, offset, strides), askOrder);
    }

    public StrideLoopIterator(StrideLayout layout, Order askOrder) {
        outerOffset = layout.offset();

        if (layout.shape().rank() == 0) {
            size = 1;
            step = 1;
            bound = 1;
            count = 1;
            outerShape = null;
            outerStrides = null;
            it = new ScalarPointerIterator(layout.offset());
            return;
        }

        var compact = layout.computeFortranLayout(askOrder, true);
        size = compact.dim(0);
        step = compact.stride(0);
        bound = size * step;

        if (compact.shape().rank() == 1) {
            count = 1;
            outerShape = null;
            outerStrides = null;
            it = new ScalarPointerIterator(layout.offset());
            return;
        }

        outerShape = Shape.of(Arrays.copyOfRange(compact.shape().dims(), 1, compact.shape().rank()));
        outerStrides = Arrays.copyOfRange(compact.strides(), 1, compact.shape().rank());

        this.count = outerShape.size();
        this.it = new StridePointerIterator(new StrideLayout(outerShape, outerOffset, outerStrides), Order.F);
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int bound() {
        return bound;
    }

    @Override
    public int step() {
        return step;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public int nextInt() {
        return it.nextInt();
    }

    @Override
    public int[] computeOffsets() {
        if (outerShape == null) {
            return new int[] {outerOffset};
        }
        int[] offsets = IntArrays.newFill(count, outerOffset);
        int inner = 1;
        for (int i = 0; i < outerShape.rank(); i++) {
            int dim = outerShape.dim(i);
            int stride = outerStrides[i];
            int pos = 0;
            while (pos < offsets.length) {
                int value = 0;
                for (int k = 0; k < dim; k++) {
                    for (int j = 0; j < inner; j++) {
                        offsets[pos + j] += value;
                    }
                    value += stride;
                    pos += inner;
                }
            }
            inner *= dim;
        }
        return offsets;
    }
}
