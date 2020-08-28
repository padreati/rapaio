package rapaio.util.collection;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntIterator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * A class providing static methods and objects that do useful things with
 * type-specific arrays of ints.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/8/19.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IArrays {

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
     * Substracts from values of vector a the values of vector b from start (inclusive)
     * to end (exclusive). It returns the substracted vector.
     *
     * @return instance of the first version
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
    public static int prod(int[] a, int aStart, int[] b, int bStart, int len) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += a[aStart++] * b[bStart++];
        }
        return sum;
    }
}
