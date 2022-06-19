/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.util.collection;

import java.io.Serial;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.util.IntComparator;
import rapaio.util.IntIterator;
import rapaio.util.function.Int2IntFunction;

/**
 * A class providing static methods and objects that do useful things with
 * type-specific arrays of ints.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/8/19.
 */
public final class IntArrays {

    /**
     * Creates a new array filled with given value. If the filled value is 0,
     * the a fill is avoided since the initialization is done with 0.
     *
     * @param size  size of the array
     * @param value value to fill the array with
     * @return filled array
     */
    public static int[] newFill(int size, int value) {
        int[] data = new int[size];
        if (value != 0) {
            Arrays.fill(data, value);
        }
        return data;
    }


    /**
     * Creates a new array filled with a sequence of values starting from
     * 0 (inclusive) and ending with {@param end} (exclusive).
     *
     * @param end sequence ending value (exclusive)
     * @return array with sequence values
     */
    public static int[] newSeq(int end) {
        return newSeq(0, end);
    }

    /**
     * Creates a new array filled with a sequence of values starting from
     * {@param start} (inclusive) and ending with {@param end} (exclusive).
     *
     * @param start sequence starting value (inclusive)
     * @param end   sequence ending value (exclusive)
     * @return array with sequence values
     */
    public static int[] newSeq(int start, int end) {
        int[] data = new int[end - start];
        for (int i = 0; i < end - start; i++) {
            data[i] = start + i;
        }
        return data;
    }

    /**
     * Builds a new int array with values from the given chunk transformed
     * with a function.
     *
     * @param source source array
     * @param start  starting position from source array (inclusive)
     * @param end    ending position from source array (exclusive)
     * @param fun    transforming function
     * @return transformed values array
     */
    public static int[] newFrom(int[] source, int start, int end, Int2IntFunction fun) {
        int[] data = new int[end - start];
        for (int i = start; i < end; i++) {
            data[i - start] = fun.applyAsInt(source[i]);
        }
        return data;
    }

    public static int[] newCopy(int[] array, int start, int len) {
        int[] data = new int[len];
        System.arraycopy(array, start, data, 0, len);
        return data;
    }

    public static IntStream stream(int[] array, int start, int end) {
        return Arrays.stream(array, start, end);
    }

    public static IntIterator iterator(int[] array, int start, int end) {
        return new IntIterator() {
            private int pos = start;

            @Override
            public boolean hasNext() {
                return pos < end;
            }

            @Override
            public int nextInt() {
                if (pos >= end) {
                    throw new NoSuchElementException();
                }
                return array[pos++];
            }
        };
    }

    /**
     * Adds scalar value to vector a from start (inclusive)
     * to end (exclusive).
     */
    public static void add(int[] a, int aStart, int x, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] += x;
        }
    }

    /**
     * Multiply a scalar value to vector a from start (inclusive)
     * to end (exclusive).
     */
    public static void mul(int[] a, int aStart, int x, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] *= x;
        }
    }

    /**
     * Adds values of vector b to vector a from start (inclusive)
     * to end (exclusive).
     */
    public static void add(int[] a, int aStart, int[] b, int bStart, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] += b[bStart++];
        }
    }

    /**
     * Subtracts from values of vector a the values of vector b from start (inclusive)
     * to end (exclusive). It returns the subtracted vector.
     */
    public static void sub(int[] a, int aStart, int[] b, int bStart, int len) {
        for (int i = 0; i < len; i++) {
            a[aStart++] -= b[bStart++];
        }
    }

    /**
     * Returns the multiplication of all elements starting with start (inclusive) till end (exclusive)
     */
    public static int prod(int[] a, int start, int len) {
        int prod = 1;
        for (int i = start; i < start + len; i++) {
            prod *= a[i];
        }
        return prod;
    }

    /**
     * Computes sum_{i=start}^{end} a[i]*b[i].
     *
     * @param a      first array
     * @param aStart start for the first array
     * @param b      second array
     * @param bStart start position
     * @param len    size of the operation
     * @return computed value
     */
    public static int prodsum(int[] a, int aStart, int[] b, int bStart, int len) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += a[aStart++] * b[bStart++];
        }
        return sum;
    }

    public static boolean equals(int[] a, int aStart, int[] b, int bStart, int len) {
        for (int i = 0; i < len - aStart; i++) {
            if (a[aStart + i] != b[bStart + i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] removeIndexesFromDenseSequence(int start, int end, int[] removeIndexes) {
        Set<Integer> rem = Arrays.stream(removeIndexes)
                .boxed()
                .filter(x -> x >= start)
                .filter(x -> x < end)
                .collect(Collectors.toSet());
        int[] cols = new int[end - start - rem.size()];
        int pos = 0;
        for (int i = start; i < end; i++) {
            if (rem.contains(i)) {
                continue;
            }
            cols[pos++] = i;
        }
        return cols;
    }

    /**
     * Returns true if the values from indexes are semi positive and values
     * covers a contiguous range of integer numbers, contains no duplicates and values are
     * in increasing order.
     * <p>
     * Returns the start element value
     *
     * @param indexes index array to be checked
     * @return value of the start element, if we have a contiguous interval, {@code -1} otherwise
     */
    public static boolean isDenseArray(int[] indexes) {
        int start = indexes[0];
        for (int index : indexes) {
            start = Math.min(start, index);
        }
        if (indexes[0] != start || indexes[0] < 0) {
            return false;
        }
        for (int i = 1; i < indexes.length; i++) {
            if (indexes[i] != indexes[i - 1] + 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * A static, final, empty array.
     */
    public static final int[] EMPTY_ARRAY = {};
    /**
     * A static, final, empty array to be used as default array in allocations. An
     * object distinct from {@link #EMPTY_ARRAY} makes it possible to have different
     * behaviors depending on whether the user required an empty allocation, or we are
     * just lazily delaying allocation.
     *
     * @see java.util.ArrayList
     */
    public static final int[] DEFAULT_EMPTY_ARRAY = {};

    /**
     * Forces an array to contain the given number of entries, preserving just a part of the array.
     *
     * @param array    an array.
     * @param length   the new minimum length for this array.
     * @param preserve the number of elements of the array that must be preserved in case a new allocation is necessary.
     * @return an array with {@code length} entries whose first {@code preserve}
     * entries are the same as those of {@code array}.
     */
    public static int[] forceCapacity(final int[] array, final int length, final int preserve) {
        final int[] t = new int[length];
        System.arraycopy(array, 0, t, 0, preserve);
        return t;
    }

    /**
     * Ensures that an array can contain the given number of entries.
     *
     * <p>If you cannot foresee whether this array will need again to be
     * enlarged, you should probably use {@code grow()} instead.
     *
     * @param array  an array.
     * @param length the new minimum length for this array.
     * @return {@code array}, if it contains {@code length} entries or more; otherwise,
     * an array with {@code length} entries whose first {@code array.length}
     * entries are the same as those of {@code array}.
     */
    public static int[] ensureCapacity(final int[] array, final int length) {
        return ensureCapacity(array, length, array.length);
    }

    /**
     * Ensures that an array can contain the given number of entries, preserving just a part of the array.
     *
     * @param array    an array.
     * @param length   the new minimum length for this array.
     * @param preserve the number of elements of the array that must be preserved in case a new allocation is necessary.
     * @return {@code array}, if it can contain {@code length} entries or more; otherwise,
     * an array with {@code length} entries whose first {@code preserve}
     * entries are the same as those of {@code array}.
     */
    public static int[] ensureCapacity(final int[] array, final int length, final int preserve) {
        return length > array.length ? forceCapacity(array, length, preserve) : array;
    }

    /**
     * Grows the given array to the maximum between the given length and
     * the current length increased by 50%, provided that the given
     * length is larger than the current length.
     *
     * <p>If you want complete control on the array growth, you
     * should probably use {@code ensureCapacity()} instead.
     *
     * @param array  an array.
     * @param length the new minimum length for this array.
     * @return {@code array}, if it can contain {@code length}
     * entries; otherwise, an array with
     * max({@code length},{@code array.length}/&phi;) entries whose first
     * {@code array.length} entries are the same as those of {@code array}.
     */
    public static int[] grow(final int[] array, final int length) {
        return grow(array, length, array.length);
    }

    /**
     * Grows the given array to the maximum between the given length and
     * the current length increased by 50%, provided that the given
     * length is larger than the current length, preserving just a part of the array.
     *
     * <p>If you want complete control on the array growth, you
     * should probably use {@code ensureCapacity()} instead.
     *
     * @param array    an array.
     * @param length   the new minimum length for this array.
     * @param preserve the number of elements of the array that must be preserved in case a new allocation is necessary.
     * @return {@code array}, if it can contain {@code length}
     * entries; otherwise, an array with
     * max({@code length},{@code array.length}/&phi;) entries whose first
     * {@code preserve} entries are the same as those of {@code array}.
     */
    public static int[] grow(final int[] array, final int length, final int preserve) {
        if (length > array.length) {
            final int newLength = (int) Math.max(Math.min((long) array.length + (array.length >> 1), TArrays.MAX_ARRAY_SIZE), length);
            final int[] t = new int[newLength];
            System.arraycopy(array, 0, t, 0, preserve);
            return t;
        }
        return array;
    }

    /**
     * Trims the given array to the given length.
     *
     * @param array  an array.
     * @param length the new maximum length for the array.
     * @return {@code array}, if it contains {@code length}
     * entries or less; otherwise, an array with
     * {@code length} entries whose entries are the same as
     * the first {@code length} entries of {@code array}.
     */
    public static int[] trim(final int[] array, final int length) {
        if (length >= array.length) {
            return array;
        }
        final int[] t = length == 0 ? EMPTY_ARRAY : new int[length];
        System.arraycopy(array, 0, t, 0, length);
        return t;
    }

    /**
     * Returns a copy of a portion of an array.
     *
     * @param array  an array.
     * @param offset the first element to copy.
     * @param length the number of elements to copy.
     * @return a new array containing {@code length} elements of {@code array} starting at {@code offset}.
     */
    public static int[] copy(final int[] array, final int offset, final int length) {
        ensureOffsetLength(array, offset, length);
        final int[] a =
                length == 0 ? EMPTY_ARRAY : new int[length];
        System.arraycopy(array, offset, a, 0, length);
        return a;
    }

    /**
     * Returns a copy of an array.
     *
     * @param array an array.
     * @return a copy of {@code array}.
     */
    public static int[] copy(final int[] array) {
        return array.clone();
    }

    /**
     * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array.
     *
     * <p>This method may be used whenever an array range check is needed.
     *
     * @param a    an array.
     * @param from a start index (inclusive).
     * @param to   an end index (exclusive).
     * @throws IllegalArgumentException       if {@code from} is greater than {@code to}.
     * @throws ArrayIndexOutOfBoundsException if {@code from} or {@code to} are greater than the array length or negative.
     */
    public static void ensureFromTo(final int[] a, final int from, final int to) {
        TArrays.ensureFromTo(a.length, from, to);
    }

    /**
     * Ensures that a range given by an offset and a length fits an array.
     *
     * <p>This method may be used whenever an array range check is needed.
     *
     * @param a      an array.
     * @param offset a start index.
     * @param length a length (the number of elements in the range).
     * @throws IllegalArgumentException       if {@code length} is negative.
     * @throws ArrayIndexOutOfBoundsException if {@code offset} is negative or {@code offset}+{@code length} is greater than the array length.
     */
    public static void ensureOffsetLength(final int[] a, final int offset, final int length) {
        TArrays.ensureOffsetLength(a.length, offset, length);
    }

    /**
     * Ensures that two arrays are of the same length.
     *
     * @param a an array.
     * @param b another array.
     * @throws IllegalArgumentException if the two argument arrays are not of the same length.
     */
    public static void ensureSameLength(final int[] a, final int[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Array size mismatch: " + a.length + " != " + b.length);
        }
    }

    private static final int QUICKSORT_NO_REC = 16;
    private static final int PARALLEL_QUICKSORT_NO_FORK = 8192;
    private static final int QUICKSORT_MEDIAN_OF_9 = 128;
    private static final int MERGESORT_NO_REC = 16;

    /**
     * Swaps two elements of an anrray.
     *
     * @param x an array.
     * @param a a position in {@code x}.
     * @param b another position in {@code x}.
     */
    public static void swap(final int[] x, final int a, final int b) {
        final int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }

    /**
     * Swaps two sequences of elements of an array.
     *
     * @param x an array.
     * @param a a position in {@code x}.
     * @param b another position in {@code x}.
     * @param n the number of elements to exchange starting at {@code a} and {@code b}.
     */
    public static void swap(final int[] x, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(x, a, b);
        }
    }

    private static int med3(final int[] x, final int a, final int b, final int c, IntComparator comp) {
        final int ab = comp.compare(x[a], x[b]);
        final int ac = comp.compare(x[a], x[c]);
        final int bc = comp.compare(x[b], x[c]);
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    private static void selectionSort(final int[] a, final int from, final int to, final IntComparator comp) {
        for (int i = from; i < to - 1; i++) {
            int m = i;
            for (int j = i + 1; j < to; j++) {
                if (comp.compare(a[j], a[m]) < 0) {
                    m = j;
                }
            }
            if (m != i) {
                final int u = a[i];
                a[i] = a[m];
                a[m] = u;
            }
        }
    }

    private static void insertionSort(final int[] a, final int from, final int to, final IntComparator comp) {
        for (int i = from; ++i < to; ) {
            int t = a[i];
            int j = i;
            for (int u = a[j - 1]; comp.compare(t, u) < 0; u = a[--j - 1]) {
                a[j] = u;
                if (from == j - 1) {
                    --j;
                    break;
                }
            }
            a[j] = t;
        }
    }

    /**
     * Sorts the specified range of elements according to the order induced by the specified
     * comparator using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param x    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void quickSort(final int[] x, final int from, final int to, final IntComparator comp) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            selectionSort(x, from, to, comp);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudo-median of 9
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s, comp);
            m = med3(x, m - s, m, m + s, comp);
            n = med3(x, n - 2 * s, n - s, n, comp);
        }
        m = med3(x, l, m, n, comp); // Mid-size, med of 3
        final int v = x[m];
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = comp.compare(x[b], v)) <= 0) {
                if (comparison == 0) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = comp.compare(x[c], v)) >= 0) {
                if (comparison == 0) {
                    swap(x, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(x, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(x, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort(x, from, from + s, comp);
        }
        if ((s = d - c) > 1) {
            quickSort(x, to - s, to, comp);
        }
    }

    /**
     * Sorts an array according to the order induced by the specified
     * comparator using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param x    the array to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void quickSort(final int[] x, final IntComparator comp) {
        quickSort(x, 0, x.length, comp);
    }

    protected static class ForkJoinQuickSortComp extends RecursiveAction {
        @Serial
        private static final long serialVersionUID = -8205272777060082445L;
        private final int from;
        private final int to;
        private final int[] x;
        private final IntComparator comp;

        public ForkJoinQuickSortComp(final int[] x, final int from, final int to, final IntComparator comp) {
            this.from = from;
            this.to = to;
            this.x = x;
            this.comp = comp;
        }

        @Override
        protected void compute() {
            final int[] x = this.x;
            final int len = to - from;
            if (len < PARALLEL_QUICKSORT_NO_FORK) {
                quickSort(x, from, to, comp);
                return;
            }
            // Choose a partition element, v
            int m = from + len / 2;
            int l = from;
            int n = to - 1;
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s, comp);
            m = med3(x, m - s, m, m + s, comp);
            n = med3(x, n - 2 * s, n - s, n, comp);
            m = med3(x, l, m, n, comp);
            final int v = x[m];
            // Establish Invariant: v* (<v)* (>v)* v*
            int a = from, b = a, c = to - 1, d = c;
            while (true) {
                int comparison;
                while (b <= c && (comparison = comp.compare(x[b], v)) <= 0) {
                    if (comparison == 0) {
                        swap(x, a++, b);
                    }
                    b++;
                }
                while (c >= b && (comparison = comp.compare(x[c], v)) >= 0) {
                    if (comparison == 0) {
                        swap(x, c, d--);
                    }
                    c--;
                }
                if (b > c) {
                    break;
                }
                swap(x, b++, c--);
            }
            // Swap partition elements back to middle
            int t;
            s = Math.min(a - from, b - a);
            swap(x, from, b - s, s);
            s = Math.min(d - c, to - d - 1);
            swap(x, b, to - s, s);
            // Recursively sort non-partition-elements
            s = b - a;
            t = d - c;
            if (s > 1 && t > 1) {
                invokeAll(new ForkJoinQuickSortComp(x, from, from + s, comp), new ForkJoinQuickSortComp(x, to - t, to, comp));
            } else if (s > 1) {
                invokeAll(new ForkJoinQuickSortComp(x, from, from + s, comp));
            } else {
                invokeAll(new ForkJoinQuickSortComp(x, to - t, to, comp));
            }
        }
    }

    /**
     * Sorts the specified range of elements according to the order induced by the specified
     * comparator using a parallel quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param x    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void parallelQuickSort(final int[] x, final int from, final int to, final IntComparator comp) {
        if (to - from < PARALLEL_QUICKSORT_NO_FORK) {
            quickSort(x, from, to, comp);
        } else {
            final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            pool.invoke(new ForkJoinQuickSortComp(x, from, to, comp));
            pool.shutdown();
        }
    }

    /**
     * Sorts an array according to the order induced by the specified
     * comparator using a parallel quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param x    the array to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void parallelQuickSort(final int[] x, final IntComparator comp) {
        parallelQuickSort(x, 0, x.length, comp);
    }

    private static int med3(final int[] x, final int a, final int b, final int c) {
        final int ab = (Integer.compare((x[a]), (x[b])));
        final int ac = (Integer.compare((x[a]), (x[c])));
        final int bc = (Integer.compare((x[b]), (x[c])));
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    private static void selectionSort(final int[] a, final int from, final int to) {
        for (int i = from; i < to - 1; i++) {
            int m = i;
            for (int j = i + 1; j < to; j++) {
                if (((a[j]) < (a[m]))) {
                    m = j;
                }
            }
            if (m != i) {
                final int u = a[i];
                a[i] = a[m];
                a[m] = u;
            }
        }
    }

    private static void insertionSort(final int[] a, final int from, final int to) {
        for (int i = from; ++i < to; ) {
            int t = a[i];
            int j = i;
            for (int u = a[j - 1]; ((t) < (u)); u = a[--j - 1]) {
                a[j] = u;
                if (from == j - 1) {
                    --j;
                    break;
                }
            }
            a[j] = t;
        }
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param x    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */

    public static void quickSort(final int[] x, final int from, final int to) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            selectionSort(x, from, to);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseud-omedian of 9
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s);
            m = med3(x, m - s, m, m + s);
            n = med3(x, n - 2 * s, n - s, n);
        }
        m = med3(x, l, m, n); // Mid-size, med of 3
        final int v = x[m];
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = (Integer.compare((x[b]), (v)))) <= 0) {
                if (comparison == 0) {
                    swap(x, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = (Integer.compare((x[c]), (v)))) >= 0) {
                if (comparison == 0) {
                    swap(x, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(x, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(x, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort(x, from, from + s);
        }
        if ((s = d - c) > 1) {
            quickSort(x, to - s, to);
        }
    }

    /**
     * Sorts an array according to the natural ascending order using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param x the array to be sorted.
     */
    public static void quickSort(final int[] x) {
        quickSort(x, 0, x.length);
    }

    protected static class ForkJoinQuickSort extends RecursiveAction {
        @Serial
        private static final long serialVersionUID = -5796422686450520496L;
        private final int from;
        private final int to;
        private final int[] x;

        public ForkJoinQuickSort(final int[] x, final int from, final int to) {
            this.from = from;
            this.to = to;
            this.x = x;
        }

        @Override

        protected void compute() {
            final int[] x = this.x;
            final int len = to - from;
            if (len < PARALLEL_QUICKSORT_NO_FORK) {
                quickSort(x, from, to);
                return;
            }
            // Choose a partition element, v
            int m = from + len / 2;
            int l = from;
            int n = to - 1;
            int s = len / 8;
            l = med3(x, l, l + s, l + 2 * s);
            m = med3(x, m - s, m, m + s);
            n = med3(x, n - 2 * s, n - s, n);
            m = med3(x, l, m, n);
            final int v = x[m];
            // Establish Invariant: v* (<v)* (>v)* v*
            int a = from, b = a, c = to - 1, d = c;
            while (true) {
                int comparison;
                while (b <= c && (comparison = (Integer.compare((x[b]), (v)))) <= 0) {
                    if (comparison == 0) {
                        swap(x, a++, b);
                    }
                    b++;
                }
                while (c >= b && (comparison = (Integer.compare((x[c]), (v)))) >= 0) {
                    if (comparison == 0) {
                        swap(x, c, d--);
                    }
                    c--;
                }
                if (b > c) {
                    break;
                }
                swap(x, b++, c--);
            }
            // Swap partition elements back to middle
            int t;
            s = Math.min(a - from, b - a);
            swap(x, from, b - s, s);
            s = Math.min(d - c, to - d - 1);
            swap(x, b, to - s, s);
            // Recursively sort non-partition-elements
            s = b - a;
            t = d - c;
            if (s > 1 && t > 1) {
                invokeAll(new ForkJoinQuickSort(x, from, from + s), new ForkJoinQuickSort(x, to - t, to));
            } else if (s > 1) {
                invokeAll(new ForkJoinQuickSort(x, from, from + s));
            } else {
                invokeAll(new ForkJoinQuickSort(x, to - t, to));
            }
        }
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using a parallel quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param x    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */
    public static void parallelQuickSort(final int[] x, final int from, final int to) {
        if (to - from < PARALLEL_QUICKSORT_NO_FORK) {
            quickSort(x, from, to);
        } else {
            final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            pool.invoke(new ForkJoinQuickSort(x, from, to));
            pool.shutdown();
        }
    }

    /**
     * Sorts an array according to the natural ascending order using a parallel quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param x the array to be sorted.
     */
    public static void parallelQuickSort(final int[] x) {
        parallelQuickSort(x, 0, x.length);
    }

    private static int med3Indirect(final int[] perm, final int[] x, final int a, final int b, final int c) {
        final int aa = x[perm[a]];
        final int bb = x[perm[b]];
        final int cc = x[perm[c]];
        final int ab = (Integer.compare((aa), (bb)));
        final int ac = (Integer.compare((aa), (cc)));
        final int bc = (Integer.compare((bb), (cc)));
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    private static void insertionSortIndirect(final int[] perm, final int[] a, final int from, final int to) {
        for (int i = from; ++i < to; ) {
            int t = perm[i];
            int j = i;
            for (int u = perm[j - 1]; ((a[t]) < (a[u])); u = perm[--j - 1]) {
                perm[j] = u;
                if (from == j - 1) {
                    --j;
                    break;
                }
            }
            perm[j] = t;
        }
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using indirect quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must
     * be exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
     * {@code x[perm[i]] &le; x[perm[i + 1]]}.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param perm a permutation array indexing {@code x}.
     * @param x    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */

    public static void quickSortIndirect(final int[] perm, final int[] x, final int from, final int to) {
        final int len = to - from;
        // Selection sort on smallest arrays
        if (len < QUICKSORT_NO_REC) {
            insertionSortIndirect(perm, x, from, to);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudo-median of 9
            int s = len / 8;
            l = med3Indirect(perm, x, l, l + s, l + 2 * s);
            m = med3Indirect(perm, x, m - s, m, m + s);
            n = med3Indirect(perm, x, n - 2 * s, n - s, n);
        }
        m = med3Indirect(perm, x, l, m, n); // Mid-size, med of 3
        final int v = x[perm[m]];
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison;
            while (b <= c && (comparison = (Integer.compare((x[perm[b]]), (v)))) <= 0) {
                if (comparison == 0) {
                    IntArrays.swap(perm, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = (Integer.compare((x[perm[c]]), (v)))) >= 0) {
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
            quickSortIndirect(perm, x, from, from + s);
        }
        if ((s = d - c) > 1) {
            quickSortIndirect(perm, x, to - s, to);
        }
    }

    /**
     * Sorts an array according to the natural ascending order using indirect quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must
     * be exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
     * {@code x[perm[i]] &le; x[perm[i + 1]]}.
     *
     * <p>Note that this implementation does not allocate any object, contrarily to the implementation
     * used to sort primitive types in {@link java.util.Arrays}, which switches to mergesort on large inputs.
     *
     * @param perm a permutation array indexing {@code x}.
     * @param x    the array to be sorted.
     */
    public static void quickSortIndirect(final int[] perm, final int[] x) {
        quickSortIndirect(perm, x, 0, x.length);
    }

    protected static class ForkJoinQuickSortIndirect extends RecursiveAction {
        @Serial
        private static final long serialVersionUID = -1214362895430371120L;
        private final int from;
        private final int to;
        private final int[] perm;
        private final int[] x;

        public ForkJoinQuickSortIndirect(final int[] perm, final int[] x, final int from, final int to) {
            this.from = from;
            this.to = to;
            this.x = x;
            this.perm = perm;
        }

        @Override

        protected void compute() {
            final int[] x = this.x;
            final int len = to - from;
            if (len < PARALLEL_QUICKSORT_NO_FORK) {
                quickSortIndirect(perm, x, from, to);
                return;
            }
            // Choose a partition element, v
            int m = from + len / 2;
            int l = from;
            int n = to - 1;
            int s = len / 8;
            l = med3Indirect(perm, x, l, l + s, l + 2 * s);
            m = med3Indirect(perm, x, m - s, m, m + s);
            n = med3Indirect(perm, x, n - 2 * s, n - s, n);
            m = med3Indirect(perm, x, l, m, n);
            final int v = x[perm[m]];
            // Establish Invariant: v* (<v)* (>v)* v*
            int a = from, b = a, c = to - 1, d = c;
            while (true) {
                int comparison;
                while (b <= c && (comparison = (Integer.compare((x[perm[b]]), (v)))) <= 0) {
                    if (comparison == 0) {
                        IntArrays.swap(perm, a++, b);
                    }
                    b++;
                }
                while (c >= b && (comparison = (Integer.compare((x[perm[c]]), (v)))) >= 0) {
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
            int t;
            s = Math.min(a - from, b - a);
            IntArrays.swap(perm, from, b - s, s);
            s = Math.min(d - c, to - d - 1);
            IntArrays.swap(perm, b, to - s, s);
            // Recursively sort non-partition-elements
            s = b - a;
            t = d - c;
            if (s > 1 && t > 1) {
                invokeAll(new ForkJoinQuickSortIndirect(perm, x, from, from + s), new ForkJoinQuickSortIndirect(perm, x, to - t, to));
            } else if (s > 1) {
                invokeAll(new ForkJoinQuickSortIndirect(perm, x, from, from + s));
            } else {
                invokeAll(new ForkJoinQuickSortIndirect(perm, x, to - t, to));
            }
        }
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using a parallel indirect quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must
     * be exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
     * {@code x[perm[i]] &le; x[perm[i + 1]]}.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param perm a permutation array indexing {@code x}.
     * @param x    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */
    public static void parallelQuickSortIndirect(final int[] perm, final int[] x, final int from, final int to) {
        if (to - from < PARALLEL_QUICKSORT_NO_FORK) {
            quickSortIndirect(perm, x, from, to);
        } else {
            final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            pool.invoke(new ForkJoinQuickSortIndirect(perm, x, from, to));
            pool.shutdown();
        }
    }

    /**
     * Sorts an array according to the natural ascending order using a parallel indirect quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implement an <em>indirect</em> sort. The elements of {@code perm} (which must
     * be exactly the numbers in the interval {@code [0..perm.length)}) will be permuted so that
     * {@code x[perm[i]] &le; x[perm[i + 1]]}.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param perm a permutation array indexing {@code x}.
     * @param x    the array to be sorted.
     */
    public static void parallelQuickSortIndirect(final int[] perm, final int[] x) {
        parallelQuickSortIndirect(perm, x, 0, x.length);
    }

    /**
     * Stabilizes a permutation.
     *
     * <p>This method can be used to stabilize the permutation generated by an indirect sorting, assuming that
     * initially the permutation array was in ascending order (e.g., the identity, as usually happens). This method
     * scans the permutation, and for each non-singleton block of elements with the same associated values in {@code x},
     * permutes them in ascending order. The resulting permutation corresponds to a stable sort.
     *
     * <p>Usually combining an unstable indirect sort and this method is more efficient than using a stable sort,
     * as most stable sort algorithms require a support array.
     *
     * <p>More precisely, assuming that {@code x[perm[i]] &le; x[perm[i + 1]]}, after
     * stabilization we will also have that {@code x[perm[i]] = x[perm[i + 1]]} implies
     * {@code perm[i] &le; perm[i + 1]}.
     *
     * @param perm a permutation array indexing {@code x} so that it is sorted.
     * @param x    the sorted array to be stabilized.
     * @param from the index of the first element (inclusive) to be stabilized.
     * @param to   the index of the last element (exclusive) to be stabilized.
     */
    public static void stabilize(final int[] perm, final int[] x, final int from, final int to) {
        int curr = from;
        for (int i = from + 1; i < to; i++) {
            if (x[perm[i]] != x[perm[curr]]) {
                if (i - curr > 1) {
                    IntArrays.parallelQuickSort(perm, curr, i);
                }
                curr = i;
            }
        }
        if (to - curr > 1) {
            IntArrays.parallelQuickSort(perm, curr, to);
        }
    }

    /**
     * Stabilizes a permutation.
     *
     * <p>This method can be used to stabilize the permutation generated by an indirect sorting, assuming that
     * initially the permutation array was in ascending order (e.g., the identity, as usually happens). This method
     * scans the permutation, and for each non-singleton block of elements with the same associated values in {@code x},
     * permutes them in ascending order. The resulting permutation corresponds to a stable sort.
     *
     * <p>Usually combining an unstable indirect sort and this method is more efficient than using a stable sort,
     * as most stable sort algorithms require a support array.
     *
     * <p>More precisely, assuming that {@code x[perm[i]] &le; x[perm[i + 1]]}, after
     * stabilization we will also have that {@code x[perm[i]] = x[perm[i + 1]]} implies
     * {@code perm[i] &le; perm[i + 1]}.
     *
     * @param perm a permutation array indexing {@code x} so that it is sorted.
     * @param x    the sorted array to be stabilized.
     */
    public static void stabilize(final int[] perm, final int[] x) {
        stabilize(perm, x, 0, perm.length);
    }

    private static int med3(final int[] x, final int[] y, final int a, final int b, final int c) {
        int t;
        final int ab = (t = (Integer.compare((x[a]), (x[b])))) == 0 ? (Integer.compare((y[a]), (y[b]))) : t;
        final int ac = (t = (Integer.compare((x[a]), (x[c])))) == 0 ? (Integer.compare((y[a]), (y[c]))) : t;
        final int bc = (t = (Integer.compare((x[b]), (x[c])))) == 0 ? (Integer.compare((y[b]), (y[c]))) : t;
        return (ab < 0 ?
                (bc < 0 ? b : ac < 0 ? c : a) :
                (bc > 0 ? b : ac > 0 ? c : a));
    }

    private static void swap(final int[] x, final int[] y, final int a, final int b) {
        final int t = x[a];
        final int u = y[a];
        x[a] = x[b];
        y[a] = y[b];
        x[b] = t;
        y[b] = u;
    }

    private static void swap(final int[] x, final int[] y, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            swap(x, y, a, b);
        }
    }

    private static void selectionSort(final int[] a, final int[] b, final int from, final int to) {
        for (int i = from; i < to - 1; i++) {
            int m = i, u;
            for (int j = i + 1; j < to; j++) {
                if ((u = (Integer.compare((a[j]), (a[m])))) < 0 || u == 0 && ((b[j]) < (b[m]))) {
                    m = j;
                }
            }
            if (m != i) {
                int t = a[i];
                a[i] = a[m];
                a[m] = t;
                t = b[i];
                b[i] = b[m];
                b[m] = t;
            }
        }
    }

    /**
     * Sorts the specified range of elements of two arrays according to the natural lexicographical
     * ascending order using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of
     * elements in the same position in the two provided arrays will be considered a single key, and
     * permuted accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or <code>x[i]
     * == x[i + 1]</code> and {@code y[i] &le; y[i + 1]}.
     *
     * @param x    the first array to be sorted.
     * @param y    the second array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */

    public static void quickSort(final int[] x, final int[] y, final int from, final int to) {
        final int len = to - from;
        if (len < QUICKSORT_NO_REC) {
            selectionSort(x, y, from, to);
            return;
        }
        // Choose a partition element, v
        int m = from + len / 2;
        int l = from;
        int n = to - 1;
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudo-median of 9
            int s = len / 8;
            l = med3(x, y, l, l + s, l + 2 * s);
            m = med3(x, y, m - s, m, m + s);
            n = med3(x, y, n - 2 * s, n - s, n);
        }
        m = med3(x, y, l, m, n); // Mid-size, med of 3
        final int v = x[m], w = y[m];
        // Establish Invariant: v* (<v)* (>v)* v*
        int a = from, b = a, c = to - 1, d = c;
        while (true) {
            int comparison, t;
            while (b <= c && (comparison = (t = (Integer.compare((x[b]), (v)))) == 0 ? (Integer.compare((y[b]), (w))) : t) <= 0) {
                if (comparison == 0) {
                    swap(x, y, a++, b);
                }
                b++;
            }
            while (c >= b && (comparison = (t = (Integer.compare((x[c]), (v)))) == 0 ? (Integer.compare((y[c]), (w))) : t) >= 0) {
                if (comparison == 0) {
                    swap(x, y, c, d--);
                }
                c--;
            }
            if (b > c) {
                break;
            }
            swap(x, y, b++, c--);
        }
        // Swap partition elements back to middle
        int s;
        s = Math.min(a - from, b - a);
        swap(x, y, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swap(x, y, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1) {
            quickSort(x, y, from, from + s);
        }
        if ((s = d - c) > 1) {
            quickSort(x, y, to - s, to);
        }
    }

    /**
     * Sorts two arrays according to the natural lexicographical ascending order using quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of
     * elements in the same position in the two provided arrays will be considered a single key, and
     * permuted accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or <code>x[i]
     * == x[i + 1]</code> and {@code y[i] &le; y[i + 1]}.
     *
     * @param x the first array to be sorted.
     * @param y the second array to be sorted.
     */
    public static void quickSort(final int[] x, final int[] y) {
        ensureSameLength(x, y);
        quickSort(x, y, 0, x.length);
    }

    protected static class ForkJoinQuickSort2 extends RecursiveAction {
        @Serial
        private static final long serialVersionUID = -8103612594173799536L;
        private final int from;
        private final int to;
        private final int[] x, y;

        public ForkJoinQuickSort2(final int[] x, final int[] y, final int from, final int to) {
            this.from = from;
            this.to = to;
            this.x = x;
            this.y = y;
        }

        @Override

        protected void compute() {
            final int[] x = this.x;
            final int[] y = this.y;
            final int len = to - from;
            if (len < PARALLEL_QUICKSORT_NO_FORK) {
                quickSort(x, y, from, to);
                return;
            }
            // Choose a partition element, v
            int m = from + len / 2;
            int l = from;
            int n = to - 1;
            int s = len / 8;
            l = med3(x, y, l, l + s, l + 2 * s);
            m = med3(x, y, m - s, m, m + s);
            n = med3(x, y, n - 2 * s, n - s, n);
            m = med3(x, y, l, m, n);
            final int v = x[m], w = y[m];
            // Establish Invariant: v* (<v)* (>v)* v*
            int a = from, b = a, c = to - 1, d = c;
            while (true) {
                int comparison, t;
                while (b <= c && (comparison = (t = (Integer.compare((x[b]), (v)))) == 0 ? (Integer.compare((y[b]), (w))) : t) <= 0) {
                    if (comparison == 0) {
                        swap(x, y, a++, b);
                    }
                    b++;
                }
                while (c >= b && (comparison = (t = (Integer.compare((x[c]), (v)))) == 0 ? (Integer.compare((y[c]), (w))) : t) >= 0) {
                    if (comparison == 0) {
                        swap(x, y, c, d--);
                    }
                    c--;
                }
                if (b > c) {
                    break;
                }
                swap(x, y, b++, c--);
            }
            // Swap partition elements back to middle
            int t;
            s = Math.min(a - from, b - a);
            swap(x, y, from, b - s, s);
            s = Math.min(d - c, to - d - 1);
            swap(x, y, b, to - s, s);
            s = b - a;
            t = d - c;
            // Recursively sort non-partition-elements
            if (s > 1 && t > 1) {
                invokeAll(new ForkJoinQuickSort2(x, y, from, from + s), new ForkJoinQuickSort2(x, y, to - t, to));
            } else if (s > 1) {
                invokeAll(new ForkJoinQuickSort2(x, y, from, from + s));
            } else {
                invokeAll(new ForkJoinQuickSort2(x, y, to - t, to));
            }
        }
    }

    /**
     * Sorts the specified range of elements of two arrays according to the natural lexicographical
     * ascending order using a parallel quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of
     * elements in the same position in the two provided arrays will be considered a single key, and
     * permuted accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or <code>x[i]
     * == x[i + 1]</code> and {@code y[i] &le; y[i + 1]}.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param x    the first array to be sorted.
     * @param y    the second array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */
    public static void parallelQuickSort(final int[] x, final int[] y, final int from, final int to) {
        if (to - from < PARALLEL_QUICKSORT_NO_FORK) {
            quickSort(x, y, from, to);
        }
        final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        pool.invoke(new ForkJoinQuickSort2(x, y, from, to));
        pool.shutdown();
    }

    /**
     * Sorts two arrays according to the natural lexicographical
     * ascending order using a parallel quicksort.
     *
     * <p>The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M. Douglas
     * McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software: Practice and Experience</i>, 23(11), pages
     * 1249&minus;1265, 1993.
     *
     * <p>This method implements a <em>lexicographical</em> sorting of the arguments. Pairs of
     * elements in the same position in the two provided arrays will be considered a single key, and
     * permuted accordingly. In the end, either {@code x[i] &lt; x[i + 1]} or <code>x[i]
     * == x[i + 1]</code> and {@code y[i] &le; y[i + 1]}.
     *
     * <p>This implementation uses a {@link ForkJoinPool} executor service with
     * {@link Runtime#availableProcessors()} parallel threads.
     *
     * @param x the first array to be sorted.
     * @param y the second array to be sorted.
     */
    public static void parallelQuickSort(final int[] x, final int[] y) {
        ensureSameLength(x, y);
        parallelQuickSort(x, y, 0, x.length);
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using mergesort, using a given pre-filled support array.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
     * of the sort. Moreover, no support arrays will be allocated.
     *
     * @param a    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     * @param supp a support array containing at least {@code to} elements, and whose entries are identical to those
     *             of {@code a} in the specified range.
     */

    public static void mergeSort(final int[] a, final int from, final int to, final int[] supp) {
        int len = to - from;
        // Insertion sort on smallest arrays
        if (len < MERGESORT_NO_REC) {
            insertionSort(a, from, to);
            return;
        }
        // Recursively sort halves of a into supp
        final int mid = (from + to) >>> 1;
        mergeSort(supp, from, mid, a);
        mergeSort(supp, mid, to, a);
        // If list is already sorted, just copy from supp to a.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (((supp[mid - 1]) <= (supp[mid]))) {
            System.arraycopy(supp, from, a, from, len);
            return;
        }
        // Merge sorted halves (now in supp) into a
        for (int i = from, p = from, q = mid; i < to; i++) {
            if (q >= to || p < mid && ((supp[p]) <= (supp[q]))) {
                a[i] = supp[p++];
            } else {
                a[i] = supp[q++];
            }
        }
    }

    /**
     * Sorts the specified range of elements according to the natural ascending order using mergesort.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
     * of the sort. An array as large as {@code a} will be allocated by this method.
     *
     * @param a    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     */
    public static void mergeSort(final int[] a, final int from, final int to) {
        mergeSort(a, from, to, a.clone());
    }

    /**
     * Sorts an array according to the natural ascending order using mergesort.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
     * of the sort. An array as large as {@code a} will be allocated by this method.
     *
     * @param a the array to be sorted.
     */
    public static void mergeSort(final int[] a) {
        mergeSort(a, 0, a.length);
    }

    /**
     * Sorts the specified range of elements according to the order induced by the specified
     * comparator using mergesort, using a given pre-filled support array.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
     * of the sort. Moreover, no support arrays will be allocated.
     *
     * @param a    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     * @param comp the comparator to determine the sorting order.
     * @param supp a support array containing at least {@code to} elements, and whose entries are identical to those
     *             of {@code a} in the specified range.
     */
    public static void mergeSort(final int[] a, final int from, final int to, IntComparator comp, final int[] supp) {
        int len = to - from;
        // Insertion sort on smallest arrays
        if (len < MERGESORT_NO_REC) {
            insertionSort(a, from, to, comp);
            return;
        }
        // Recursively sort halves of a into supp
        final int mid = (from + to) >>> 1;
        mergeSort(supp, from, mid, comp, a);
        mergeSort(supp, mid, to, comp, a);
        // If list is already sorted, just copy from supp to a.  This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (comp.compare(supp[mid - 1], supp[mid]) <= 0) {
            System.arraycopy(supp, from, a, from, len);
            return;
        }
        // Merge sorted halves (now in supp) into a
        for (int i = from, p = from, q = mid; i < to; i++) {
            if (q >= to || p < mid && comp.compare(supp[p], supp[q]) <= 0) {
                a[i] = supp[p++];
            } else {
                a[i] = supp[q++];
            }
        }
    }

    /**
     * Sorts the specified range of elements according to the order induced by the specified
     * comparator using mergesort.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
     * of the sort. An array as large as {@code a} will be allocated by this method.
     *
     * @param a    the array to be sorted.
     * @param from the index of the first element (inclusive) to be sorted.
     * @param to   the index of the last element (exclusive) to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void mergeSort(final int[] a, final int from, final int to, IntComparator comp) {
        mergeSort(a, from, to, comp, a.clone());
    }

    /**
     * Sorts an array according to the order induced by the specified
     * comparator using mergesort.
     *
     * <p>This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result
     * of the sort.  An array as large as {@code a} will be allocated by this method.
     *
     * @param a    the array to be sorted.
     * @param comp the comparator to determine the sorting order.
     */
    public static void mergeSort(final int[] a, IntComparator comp) {
        mergeSort(a, 0, a.length, comp);
    }

    /**
     * Searches a range of the specified array for the specified value using
     * the binary search algorithm. The range must be sorted prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a    the array to be searched.
     * @param from the index of the first element (inclusive) to be searched.
     * @param to   the index of the last element (exclusive) to be searched.
     * @param key  the value to be searched for.
     * @return index of the search key, if it is contained in the array;
     * otherwise, {@code (-(<i>insertion point</i>) - 1)}.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the array: the index of the first
     * element greater than the key, or the length of the array, if all
     * elements in the array are less than the specified key.  Note
     * that this guarantees that the return value will be &ge; 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */

    public static int binarySearch(final int[] a, int from, int to, final int key) {
        int midVal;
        to--;
        while (from <= to) {
            final int mid = (from + to) >>> 1;
            midVal = a[mid];
            if (midVal < key) {
                from = mid + 1;
            } else if (midVal > key) {
                to = mid - 1;
            } else {
                return mid;
            }
        }
        return -(from + 1);
    }

    /**
     * Searches an array for the specified value using
     * the binary search algorithm. The range must be sorted prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a   the array to be searched.
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the array;
     * otherwise, {@code (-(<i>insertion point</i>) - 1)}.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the array: the index of the first
     * element greater than the key, or the length of the array, if all
     * elements in the array are less than the specified key.  Note
     * that this guarantees that the return value will be &ge; 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    public static int binarySearch(final int[] a, final int key) {
        return binarySearch(a, 0, a.length, key);
    }

    /**
     * Searches a range of the specified array for the specified value using
     * the binary search algorithm and a specified comparator. The range must be sorted following the comparator prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a    the array to be searched.
     * @param from the index of the first element (inclusive) to be searched.
     * @param to   the index of the last element (exclusive) to be searched.
     * @param key  the value to be searched for.
     * @param c    a comparator.
     * @return index of the search key, if it is contained in the array;
     * otherwise, {@code (-(<i>insertion point</i>) - 1)}.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the array: the index of the first
     * element greater than the key, or the length of the array, if all
     * elements in the array are less than the specified key.  Note
     * that this guarantees that the return value will be &ge; 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    public static int binarySearch(final int[] a, int from, int to, final int key, final IntComparator c) {
        int midVal;
        to--;
        while (from <= to) {
            final int mid = (from + to) >>> 1;
            midVal = a[mid];
            final int cmp = c.compare(midVal, key);
            if (cmp < 0) {
                from = mid + 1;
            } else if (cmp > 0) {
                to = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(from + 1);
    }

    /**
     * Searches an array for the specified value using
     * the binary search algorithm and a specified comparator. The range must be sorted following the comparator prior to making this call.
     * If it is not sorted, the results are undefined. If the range contains multiple elements with
     * the specified value, there is no guarantee which one will be found.
     *
     * @param a   the array to be searched.
     * @param key the value to be searched for.
     * @param c   a comparator.
     * @return index of the search key, if it is contained in the array;
     * otherwise, {@code (-(<i>insertion point</i>) - 1)}.  The <i>insertion
     * point</i> is defined as the the point at which the value would
     * be inserted into the array: the index of the first
     * element greater than the key, or the length of the array, if all
     * elements in the array are less than the specified key.  Note
     * that this guarantees that the return value will be &ge; 0 if
     * and only if the key is found.
     * @see java.util.Arrays
     */
    public static int binarySearch(final int[] a, final int key, final IntComparator c) {
        return binarySearch(a, 0, a.length, key, c);
    }

    /**
     * Shuffles the specified array fragment using the specified pseudorandom number generator.
     *
     * @param a      the array to be shuffled.
     * @param from   the index of the first element (inclusive) to be shuffled.
     * @param to     the index of the last element (exclusive) to be shuffled.
     * @param random a pseudorandom number generator.
     * @return {@code a}.
     */
    public static int[] shuffle(final int[] a, final int from, final int to, final Random random) {
        for (int i = to - from; i-- != 0; ) {
            final int p = random.nextInt(i + 1);
            final int t = a[from + i];
            a[from + i] = a[from + p];
            a[from + p] = t;
        }
        return a;
    }

    /**
     * Shuffles the specified array using the specified pseudorandom number generator.
     *
     * @param a      the array to be shuffled.
     * @param random a pseudorandom number generator.
     * @return {@code a}.
     */
    public static int[] shuffle(final int[] a, final Random random) {
        for (int i = a.length; i-- != 0; ) {
            final int p = random.nextInt(i + 1);
            final int t = a[i];
            a[i] = a[p];
            a[p] = t;
        }
        return a;
    }

    /**
     * Reverses the order of the elements in the specified array.
     *
     * @param a the array to be reversed.
     * @return {@code a}.
     */
    public static int[] reverse(final int[] a) {
        final int length = a.length;
        for (int i = length / 2; i-- != 0; ) {
            final int t = a[length - i - 1];
            a[length - i - 1] = a[i];
            a[i] = t;
        }
        return a;
    }

    /**
     * Reverses the order of the elements in the specified array fragment.
     *
     * @param a    the array to be reversed.
     * @param from the index of the first element (inclusive) to be reversed.
     * @param to   the index of the last element (exclusive) to be reversed.
     * @return {@code a}.
     */
    public static int[] reverse(final int[] a, final int from, final int to) {
        final int length = to - from;
        for (int i = length / 2; i-- != 0; ) {
            final int t = a[from + length - i - 1];
            a[from + length - i - 1] = a[from + i];
            a[from + i] = t;
        }
        return a;
    }
}
