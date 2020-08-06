package rapaio.util.collection;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/12/19.
 */
public final class LongArrayTools {

    public static boolean checkCapacity(long[] array, int pos) {
        return pos < array.length;
    }

    /**
     * Check if the array size is enough to store an element at given {@param pos}.
     * If it is enough capacity it returns the same array. If it is not enough,
     * a new array copy is created with an increasing factor of 1.5 of the
     * original size.
     *
     * @param array initial array
     * @param pos   position for insert
     * @return adjusted capacity array
     */
    public static long[] ensureCapacity(long[] array, int pos) {
        if (pos < array.length) {
            return array;
        }
        long[] data = new long[Math.max(pos + 1, array.length + (array.length >> 1))];
        System.arraycopy(array, 0, data, 0, array.length);
        return data;
    }

    public static void fill(long[] array, int start, int end, long fillValue) {
        for (int i = start; i < end; i++) {
            array[i] = fillValue;
        }
    }

    /**
     * Delete element from given position by copying subsequent elements one position ahead.
     *
     * @param array source array of elements
     * @param size  the length of the array with known values
     * @param pos   position of the element to be removed
     * @return same int array
     */
    public static long[] delete(long[] array, int size, int pos) {
        if (size - pos > 0) {
            System.arraycopy(array, pos + 1, array, pos, size - pos - 1);
        }
        return array;
    }
}
