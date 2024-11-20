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

package rapaio.narray.layout;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.util.collection.IntArrays;

/**
 * Utility class which allows execution of some utility operations over an abstraction of indexed list of values
 * using a stride arithmetic in one dimension.
 *
 * @param <N> data type
 */
public final class StrideWrapper<N extends Number> extends AbstractList<N> {

    public static <N extends Number> StrideWrapper<N> of(int offset, int stride, int len, NArray<N> array) {
        Function<Integer, N> getter = array::ptrGet;
        BiFunction<Integer, N, N> setter = (i, value) -> {
            array.ptrSet(i, value);
            return value;
        };
        return new StrideWrapper<>(offset, stride, len, getter, setter,
                array.dtype().naturalComparator(), array.dtype().reverseComparator());
    }

    public static <N extends Number> StrideWrapper<N> of(DType<N> dType, int offset, int stride, int len,
            Function<Integer, N> getter, BiFunction<Integer, N, N> setter) {
        return new StrideWrapper<>(offset, stride, len, getter, setter, dType.naturalComparator(), dType.reverseComparator());
    }

    private final int offset;
    private final int stride;
    private final int len;
    private final Function<Integer, N> getter;
    private final BiFunction<Integer, N, N> setter;
    private final Comparator<N> ascComp;
    private final Comparator<N> descComp;

    private StrideWrapper(int offset, int stride, int len, Function<Integer, N> getter, BiFunction<Integer, N, N> setter,
            Comparator<N> ascComp, Comparator<N> descComp) {
        this.offset = offset;
        this.stride = stride;
        this.len = len;
        this.getter = getter;
        this.setter = setter;
        this.ascComp = ascComp;
        this.descComp = descComp;
    }

    @Override
    public N set(int index, N element) {
        return setter.apply(offset + index * stride, element);
    }

    @Override
    public N get(int index) {
        return getter.apply(offset + index * stride);
    }

    @Override
    public int size() {
        return len;
    }

    public void sort(boolean asc) {
        super.sort(asc ? ascComp : descComp);
    }

    public void sortIndirect(final int[] perm, boolean asc) {
        quickSortIndirect(perm, 0, perm.length, asc ? ascComp : descComp);
    }

    private static final int QUICKSORT_MEDIAN_OF_9 = 128;

    private int med3Indirect(final int[] perm, final int a, final int b, final int c, Comparator<N> comp) {
        final N aa = get(perm[a]);
        final N bb = get(perm[b]);
        final N cc = get(perm[c]);
        final int ab = comp.compare(aa, bb);
        final int ac = comp.compare(aa, cc);
        final int bc = comp.compare(bb, cc);
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
    }

    private void quickSortIndirect(final int[] perm, final int from, final int to, final Comparator<N> comp) {
        final int len = to - from;
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
            int s = len / 8;
            l = med3Indirect(perm, l, l + s, l + 2 * s, comp);
            m = med3Indirect(perm, m - s, m, m + s, comp);
            n = med3Indirect(perm, n - 2 * s, n - s, n, comp);
        }
        m = med3Indirect(perm, l, m, n, comp); // Mid-size, med of 3
        final N v = get(perm[m]);
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = comp.compare(get(perm[b]), (v))) <= 0) {
                if (comparison == 0) {
                    IntArrays.swap(perm, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = comp.compare(get(perm[c]), (v))) >= 0) {
                if (comparison == 0) {
                    IntArrays.swap(perm, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            IntArrays.swap(perm, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        IntArrays.swap(perm, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        IntArrays.swap(perm, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSortIndirect(perm, from, from + s, comp);
        }
        if ((s = d - c) > 1) {
            quickSortIndirect(perm, to - s, to, comp);
        }
    }

    public N aggregate(N startValue, BiFunction<N, N, N> fun) {
        N v = startValue;
        for (int i = 0; i < len; i++) {
            v = fun.apply(v, getter.apply(offset + i * stride));
        }
        return v;
    }

    public N nanAggregate(DType<N> dType, N startValue, BiFunction<N, N, N> fun) {
        N v = startValue;
        for (int i = 0; i < len; i++) {
            N next = getter.apply(offset + i * stride);
            if (dType.isNaN(next)) {
                continue;
            }
            v = fun.apply(v, next);
        }
        return v;
    }
}
