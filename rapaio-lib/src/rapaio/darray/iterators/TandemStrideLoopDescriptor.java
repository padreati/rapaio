/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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
 * Loop descriptor for walking two arrays of the same shape in lockstep, so that element-wise binary kernels
 * can use vector instructions even when the two operands have different layouts.
 * <p>
 * Like {@link StrideLoopDescriptor}, the traversal is split into an inner loop of {@link #bound} elements and
 * an outer loop over paired starting offsets ({@link #offsets1}, {@link #offsets2}). Adjacent dimensions are
 * collapsed into a single loop only when <em>both</em> layouts are contiguous across them, which guarantees that
 * the {@code k}-th inner loop of the first array visits exactly the same logical elements as the {@code k}-th
 * inner loop of the second array. Inside the inner loop each side advances by its own stride
 * ({@link #step1}, {@link #step2}); a stride of {@code 0} denotes a broadcast dimension.
 */
public final class TandemStrideLoopDescriptor {

    /**
     * Builds a tandem descriptor for two layouts with the same shape.
     *
     * @param first    layout of the first operand
     * @param second   layout of the second operand, must have the same shape as the first
     * @param askOrder traversal order, {@link Order#C} or {@link Order#F}; other values fall back to the default order
     * @param vs       vector species used to compute the SIMD loop bound
     * @return tandem descriptor
     */
    public static TandemStrideLoopDescriptor of(StrideLayout first, StrideLayout second, Order askOrder, VectorSpecies<?> vs) {
        if (!first.shape().equals(second.shape())) {
            throw new IllegalArgumentException(
                    "Tandem iteration requires layouts with the same shape (first: %s, second: %s)."
                            .formatted(first.shape(), second.shape()));
        }
        return new TandemStrideLoopDescriptor(first, second, askOrder, vs);
    }

    /**
     * Number of elements in the inner loop.
     */
    public final int bound;
    /**
     * Inner loop stride of the first operand.
     */
    public final int step1;
    /**
     * Inner loop stride of the second operand.
     */
    public final int step2;
    /**
     * Inner loop starting pointers of the first operand, paired positionally with {@link #offsets2}.
     */
    public final int[] offsets1;
    /**
     * Inner loop starting pointers of the second operand, paired positionally with {@link #offsets1}.
     */
    public final int[] offsets2;
    /**
     * Number of lanes of the vector species.
     */
    public final int simdLen;
    /**
     * Largest multiple of {@link #simdLen} not exceeding {@link #bound}.
     */
    public final int simdBound;

    private int[] simdIdx1;
    private int[] simdIdx2;

    private TandemStrideLoopDescriptor(StrideLayout first, StrideLayout second, Order askOrder, VectorSpecies<?> vs) {
        this.simdLen = vs.length();

        int rank = first.rank();
        int[] dims = first.shape().dims();
        int[] strides1 = Arrays.copyOf(first.strides(), rank);
        int[] strides2 = Arrays.copyOf(second.strides(), rank);

        Order order = (askOrder == Order.C || askOrder == Order.F) ? askOrder : Order.defaultOrder();
        if (order == Order.C) {
            // process dimensions from the innermost one, like a Fortran ordered layout
            Ints.reverse(dims);
            Ints.reverse(strides1);
            Ints.reverse(strides2);
        }

        // drop unit dimensions (their strides are irrelevant) and merge dimensions contiguous in both layouts
        int len = 0;
        for (int i = 0; i < rank; i++) {
            if (dims[i] == 1) {
                continue;
            }
            if (len > 0 && dims[len - 1] * strides1[len - 1] == strides1[i] && dims[len - 1] * strides2[len - 1] == strides2[i]) {
                dims[len - 1] *= dims[i];
                continue;
            }
            dims[len] = dims[i];
            strides1[len] = strides1[i];
            strides2[len] = strides2[i];
            len++;
        }

        if (len == 0) {
            // scalar, or all dimensions of size one
            bound = 1;
            step1 = 1;
            step2 = 1;
            simdBound = 0;
            offsets1 = new int[] {first.offset()};
            offsets2 = new int[] {second.offset()};
            return;
        }

        bound = dims[0];
        step1 = strides1[0];
        step2 = strides2[0];
        simdBound = vs.loopBound(bound);

        int count = Ints.prod(dims, 1, len - 1);
        offsets1 = Ints.fill(count, first.offset());
        offsets2 = Ints.fill(count, second.offset());
        int inner = 1;
        for (int d = 1; d < len; d++) {
            int dim = dims[d];
            int pos = 0;
            while (pos < count) {
                int value1 = 0;
                int value2 = 0;
                for (int k = 0; k < dim; k++) {
                    for (int j = 0; j < inner; j++) {
                        offsets1[pos + j] += value1;
                        offsets2[pos + j] += value2;
                    }
                    value1 += strides1[d];
                    value2 += strides2[d];
                    pos += inner;
                }
            }
            inner *= dim;
        }
    }

    /**
     * @return gather/scatter lane offsets for the first operand, {@code {0, step1, 2*step1, ...}}
     */
    public int[] simdIdx1() {
        if (simdIdx1 == null) {
            simdIdx1 = laneOffsets(step1);
        }
        return simdIdx1;
    }

    /**
     * @return gather/scatter lane offsets for the second operand, {@code {0, step2, 2*step2, ...}}
     */
    public int[] simdIdx2() {
        if (simdIdx2 == null) {
            simdIdx2 = laneOffsets(step2);
        }
        return simdIdx2;
    }

    private int[] laneOffsets(int step) {
        int[] idx = new int[simdLen];
        for (int i = 1; i < idx.length; i++) {
            idx[i] = idx[i - 1] + step;
        }
        return idx;
    }
}
