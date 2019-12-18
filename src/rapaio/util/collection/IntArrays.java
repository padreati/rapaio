package rapaio.util.collection;

import rapaio.util.function.IntIntFunction;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * A class providing static methods and objects that do useful things with
 * type-specific arrays of ints.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/8/19.
 */
public final class IntArrays {

    private IntArrays() {
    }

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
     * {@param start} (inclusive) and ending with {@param end} (exclusive)
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
    public static int[] newFrom(int[] source, int start, int end, IntIntFunction fun) {
        int[] data = new int[end - start];
        for (int i = start; i < end; i++) {
            data[i - start] = fun.applyAsInt(source[i]);
        }
        return data;
    }

    public static int[] newCopy(int[] array, int start, int end) {
        int[] data = new int[end - start];
        System.arraycopy(array, start, data, 0, end - start);
        return data;
    }

    public static boolean checkCapacity(int[] array, int size) {
        return size <= array.length;
    }

    /**
     * Check if the array size is enough to store an element at given {@param pos}.
     * If it is enough capacity it returns the same array. If it is not enough,
     * a new array copy is created with an increasing factor of 1.5 of the
     * original size.
     *
     * @param array initial array
     * @param size  size of the array which must be ensured
     * @return adjusted capacity array if modified, old instance if not
     */
    public static int[] ensureCapacity(int[] array, int size) {
        if (size < array.length) {
            return array;
        }
        int[] data = new int[Math.max(size, array.length + (array.length >> 1))];
        System.arraycopy(array, 0, data, 0, array.length);
        return data;
    }

    /**
     * Delete element from given position by copying subsequent elements one position ahead.
     *
     * @param array source array of elements
     * @param size  the length of the array with known values
     * @param pos   position of the element to be removed
     * @return same int array
     */
    public static int[] delete(int[] array, int size, int pos) {
        if (size - pos > 0) {
            System.arraycopy(array, pos + 1, array, pos, size - pos - 1);
        }
        return array;
    }

    private static final int QUICKSORT_NO_REC = 16;
    private static final int QUICKSORT_MEDIAN_OF_9 = 128;

    private static void selectionSort(final int[] a, final int from, final int to, final IntComparator comp) {
        for (int i = from; i < to - 1; i++) {
            int m = i;
            for (int j = i + 1; j < to; j++) {
                if (comp.compareInt(a[j], a[m]) < 0) {
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

    /**
     * Sorts the specified range of elements according to the order induced by the
     * specified comparator using quicksort.
     *
     * <p>
     * The sorting algorithm is a tuned quicksort adapted from Jon L. Bentley and M.
     * Douglas McIlroy, &ldquo;Engineering a Sort Function&rdquo;, <i>Software:
     * Practice and Experience</i>, 23(11), pages 1249&minus;1265, 1993.
     *
     * <p>
     * Note that this implementation does not allocate any object, contrarily to the
     * implementation used to sort primitive types in {@link java.util.Arrays},
     * which switches to mergesort on large inputs.
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
        if (len > QUICKSORT_MEDIAN_OF_9) { // Big arrays, pseudomedian of 9
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
            while (b <= c && (comparison = comp.compareInt(x[b], v)) <= 0) {
                if (comparison == 0)
                    swap(x, a++, b);
                b++;
            }
            while (c >= b && (comparison = comp.compareInt(x[c], v)) >= 0) {
                if (comparison == 0)
                    swap(x, c, d--);
                c--;
            }
            if (b > c)
                break;
            swap(x, b++, c--);
        }
        // Swap partition elements back to middle
        int s = Math.min(a - from, b - a);
        swapSeq(x, from, b - s, s);
        s = Math.min(d - c, to - d - 1);
        swapSeq(x, b, to - s, s);
        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
            quickSort(x, from, from + s, comp);
        if ((s = d - c) > 1)
            quickSort(x, to - s, to, comp);
    }

    private static int med3(final int[] x, final int a, final int b, final int c, IntComparator comp) {
        final int ab = comp.compareInt(x[a], x[b]);
        final int ac = comp.compareInt(x[a], x[c]);
        final int bc = comp.compareInt(x[b], x[c]);
        return (ab < 0 ? (bc < 0 ? b : ac < 0 ? c : a) : (bc > 0 ? b : ac > 0 ? c : a));
    }

    /**
     * Swaps two sequences of elements of an array.
     *
     * @param x an array.
     * @param a a position in {@code x}.
     * @param b another position in {@code x}.
     * @param n the number of elements to exchange starting at {@code a} and
     *          {@code b}.
     */
    public static void swapSeq(final int[] x, int a, int b, final int n) {
        for (int i = 0; i < n; i++, a++, b++) {
            final int t = x[a];
            x[a] = x[b];
            x[b] = t;
        }
    }

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
     * Shuffles the specified array fragment using the specified pseudorandom number generator.
     *
     * @param a      the array to be shuffled.
     * @param from   first element inclusive
     * @param to     last element exclusive
     * @param random a pseudorandom number generator.
     */
    public static void shuffle(final int[] a, final int from, final int to, final Random random) {
        for (int i = to - from; i-- != 0; ) {
            final int p = random.nextInt(i + 1);
            final int t = a[from + i];
            a[from + i] = a[from + p];
            a[from + p] = t;
        }
    }

    /**
     * Shuffles the specified array using the specified pseudorandom number generator.
     *
     * @param a      the array to be shuffled.
     * @param random a pseudorandom number generator.
     */
    public static void shuffle(final int[] a, final Random random) {
        for (int i = a.length; i-- != 0; ) {
            final int p = random.nextInt(i + 1);
            final int t = a[i];
            a[i] = a[p];
            a[p] = t;
        }
    }

    /**
     * Reverses the order of the elements in the specified array.
     *
     * @param a the array to be reversed.
     */
    public static void reverse(final int[] a) {
        final int length = a.length;
        for (int i = length / 2; i-- != 0; ) {
            final int t = a[length - i - 1];
            a[length - i - 1] = a[i];
            a[i] = t;
        }
    }

    /**
     * Reverses the order of the elements in the specified array fragment.
     *
     * @param a    the array to be reversed.
     * @param from the index of the first element (inclusive) to be reversed.
     * @param to   the index of the last element (exclusive) to be reversed.
     */
    public static void reverse(final int[] a, final int from, final int to) {
        final int length = to - from;
        for (int i = length / 2; i-- != 0; ) {
            final int t = a[from + length - i - 1];
            a[from + length - i - 1] = a[from + i];
            a[from + i] = t;
        }
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
     * Substracts from values of vector a the values of vector b from start (inclusive)
     * to end (exclusive). It returns the substracted vector.
     *
     * @param a     first vector
     * @param b     second vector
     * @param start start position (inclusive)
     * @param end   end position (exclusive)
     * @return instance of the first version
     */
    public static int[] minus(int[] a, int[] b, int start, int end) {
        for (int i = start; i < end; i++) {
            a[i] -= b[i];
        }
        return a;
    }

    /**
     * Returns the multiplication of all elements starting with start (inclusive) till end (exclusive)
     */
    public static int product(int[] a, int start, int end) {
        int prod = 1;
        for (int i = start; i < end; i++) {
            prod *= a[i];
        }
        return prod;
    }

    /**
     * Computes sum_{i=start}^{end} a[i]*b[i].
     *
     * @param a     first array
     * @param b     second array
     * @param start start position (inclusive)
     * @param end   end position (exclusive)
     * @return computed value
     */
    public static int product(int[] a, int[] b, int start, int end) {
        int sum = 0;
        for (int i = start; i < end; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }
}
