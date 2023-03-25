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

import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;

public final class StrideChunkIterator implements ChunkIterator {

    private final int innerDim;
    private final int innerStride;
    private final int chunkCount;

    private final PointerIterator it;

    public StrideChunkIterator(Shape shape, int offset, int[] strides, Order askOrder) {
        this(new StrideLayout(shape, offset, strides), askOrder);
    }

    public StrideChunkIterator(StrideLayout layout, Order askOrder) {

        if (layout.shape().rank() == 0) {
            innerDim = 1;
            innerStride = 1;
            chunkCount = 1;
            it = new ScalarPointerIterator(layout.offset());
            return;
        }

        var compact = layout.computeFortranLayout(askOrder, true);
        innerDim = compact.shape().dim(0);
        innerStride = compact.strides()[0];

        if (compact.shape().rank() == 1) {
            chunkCount = 1;
            it = new ScalarPointerIterator(layout.offset());
            return;
        }

        Shape outerShape = Shape.of(Arrays.copyOfRange(compact.shape().dims(), 1, compact.shape().rank()));
        int[] outerStrides = Arrays.copyOfRange(compact.strides(), 1, compact.shape().rank());

        this.chunkCount = outerShape.size();
        this.it = new StridePointerIterator(new StrideLayout(outerShape, layout.offset(), outerStrides), Order.F);
    }

    @Override
    public int chunkCount() {
        return chunkCount;
    }

    @Override
    public int loopSize() {
        return innerDim;
    }

    @Override
    public int loopStep() {
        return innerStride;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public int nextInt() {
        return it.nextInt();
    }
}
