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
import rapaio.math.tensor.StrideAlgebra;
import rapaio.util.collection.IntArrays;

public final class StrideChunkIterator implements ChunkIterator {

    private final int innerDim;
    private final int innerStride;
    private final int chunkCount;

    private final PointerIterator it;

    public StrideChunkIterator(Shape shape, int offset, int[] strides, Order order) {

        int[] axesOrder = switch (order) {
            case C -> IntArrays.newSeq(0, shape.rank());
            case F -> {
                int[] ints = IntArrays.newSeq(0, shape.rank());
                IntArrays.reverse(ints);
                yield ints;
            }
            case S -> StrideAlgebra.computeStorageOrder(shape.dims(), strides);
        };

        int[] dims = IntArrays.newPermutation(shape.dims(), axesOrder);
        strides = IntArrays.newPermutation(strides, axesOrder);

        int len = compactOffsets(dims, strides);

        this.innerDim = dims.length > 0 ? dims[0] : 1;
        this.innerStride = strides.length > 0 ? strides[0] : 1;

        Shape outerShape = dims.length> 0 ? Shape.of(Arrays.copyOfRange(dims, 1, len)) : Shape.of();
        int[] outerStrides = strides.length>0 ? Arrays.copyOfRange(strides, 1, len) : new int[0];

        this.chunkCount = outerShape.size();

        this.it = outerShape.rank() == 0
                ? new ScalarPointerIterator(offset)
                : new FPointerIterator(outerShape, offset, outerStrides);
    }

    private int compactOffsets(int[] dims, int[] strides) {
        if (dims.length == 0) {
            return 0;
        }
        int len = 1;
        int lastDim = 0;
        for (int i = 1; i < dims.length; i++) {
            if (dims[lastDim] * strides[lastDim] == strides[i]) {
                dims[lastDim] *= dims[i];
                continue;
            }
            len++;
            lastDim++;
            dims[lastDim] = dims[i];
            strides[lastDim] = strides[i];
        }

        return len;
    }

    @Override
    public int chunkCount() {
        return chunkCount;
    }

    @Override
    public int chunkSize() {
        return innerDim;
    }

    @Override
    public int chunkStride() {
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
