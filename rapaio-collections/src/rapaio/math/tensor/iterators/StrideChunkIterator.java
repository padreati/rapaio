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

public final class StrideChunkIterator implements ChunkIterator {

    private final int loopSize;
    private final int loopStep;
    private final int chunkCount;

    private final int outerOffset;
    private final Shape outerShape;
    private final int[] outerStrides;
    private final PointerIterator it;

    public StrideChunkIterator(Shape shape, int offset, int[] strides, Order askOrder) {
        this(new StrideLayout(shape, offset, strides), askOrder);
    }

    public StrideChunkIterator(StrideLayout layout, Order askOrder) {
        outerOffset = layout.offset();

        if (layout.shape().rank() == 0) {
            loopSize = 1;
            loopStep = 1;
            chunkCount = 1;
            outerShape = null;
            outerStrides = null;
            it = new ScalarPointerIterator(layout.offset());
            return;
        }

        var compact = layout.computeFortranLayout(askOrder, true);
        loopSize = compact.dim(0);
        loopStep = compact.stride(0);

        if (compact.shape().rank() == 1) {
            chunkCount = 1;
            outerShape = null;
            outerStrides = null;
            it = new ScalarPointerIterator(layout.offset());
            return;
        }

        outerShape = Shape.of(Arrays.copyOfRange(compact.shape().dims(), 1, compact.shape().rank()));
        outerStrides = Arrays.copyOfRange(compact.strides(), 1, compact.shape().rank());

        this.chunkCount = outerShape.size();
        this.it = new StridePointerIterator(new StrideLayout(outerShape, outerOffset, outerStrides), Order.F);
    }

    @Override
    public int chunkCount() {
        return chunkCount;
    }

    @Override
    public int loopSize() {
        return loopSize;
    }

    @Override
    public int loopStep() {
        return loopStep;
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
    public int[] computeChunkOffsets() {
        if (outerShape == null) {
            return new int[] {outerOffset};
        }
        int[] offsets = new int[chunkCount];
        Arrays.fill(offsets, outerOffset);
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
