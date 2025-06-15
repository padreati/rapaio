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

package rapaio.darray.iterators;

import java.util.Arrays;

import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.Order;
import rapaio.darray.layout.StrideLayout;
import rapaio.util.collection.Ints;

/**
 * A loop stride descriptor contains the same information as a loop iterator, but in a precomputed form.
 * It is an alternative to loop iterator, when you want precomputed loop offsets.
 */
public final class StrideLoopDescriptor<N extends Number> {

    public static <N extends Number> StrideLoopDescriptor<N> of(StrideLayout layout, Order askOrder, VectorSpecies<N> vs) {
        return new StrideLoopDescriptor<>(layout, askOrder, vs);
    }

    public final int bound;
    public final int step;
    public final int[] offsets;
    public final int simdLen;
    public final int simdBound;
    private int[] simdIdx;

    private StrideLoopDescriptor(StrideLayout layout, Order askOrder, VectorSpecies<N> vs) {
        this.simdLen = vs.length();

        if (layout.rank() == 0) {
            bound = 1;
            step = 1;
            simdBound = 0;
            offsets = new int[] {layout.offset()};
            return;
        }

        if (layout.rank() == 1) {
            bound = layout.dim(0);
            step = layout.stride(0);
            simdBound = vs.loopBound(bound);
            offsets = new int[] {layout.offset()};
            return;
        }

        var compact = layout.computeFortranLayout(askOrder, true);
        bound = compact.dim(0);
        step = compact.stride(0);
        simdBound = vs.loopBound(bound);

        if (compact.rank() == 1) {
            offsets = new int[] {layout.offset()};
            return;
        }

        int[] outerDims = Arrays.copyOfRange(compact.dims(), 1, compact.rank());
        int[] outerStrides = Arrays.copyOfRange(compact.strides(), 1, compact.rank());

        int count = Ints.prod(outerDims, 0, outerDims.length);
        this.offsets = Ints.fill(count, layout.offset());

        int inner = 1;
        for (int i = 0; i < outerDims.length; i++) {
            int dim = outerDims[i];
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
    }

    public int[] simdIdx() {
        if (simdIdx == null) {
            simdIdx = new int[simdLen];
            for (int i = 0; i < simdIdx.length; i++) {
                simdIdx[i] = i * step;
            }
        }
        return simdIdx;
    }
}
