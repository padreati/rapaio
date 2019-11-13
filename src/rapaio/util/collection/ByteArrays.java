package rapaio.util.collection;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/13/19.
 */
public final class ByteArrays {

    private ByteArrays() {
    }

    /**
     * Creates a new array of byte values filled in a specified interval with a filling value.
     *
     * @param capacity  dimenstion of the array
     * @param start     starting position (inclusive) of the filling interval
     * @param end       ending position (exclusive) of the filling interval
     * @param fillValue fill value
     * @return array instance
     */
    public static byte[] newFill(int capacity, int start, int end, byte fillValue) {
        byte[] array = new byte[capacity];
        if (fillValue != 0) {
            fill(array, start, end, fillValue);
        }
        return array;
    }

    /**
     * Fill a byte array with a value in a specified interval
     *
     * @param array     array to be modified
     * @param start     starting position (inclusive) of the array
     * @param end       end position (exclusive) of the array
     * @param fillValue fill value
     * @return array instance
     */
    public static byte[] fill(byte[] array, int start, int end, byte fillValue) {
        for (int i = start; i < end; i++) {
            array[i] = fillValue;
        }
        return array;
    }

    public static boolean checkCapacity(byte[] array, int size) {
        return size < array.length;
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
    public static byte[] ensureCapacity(byte[] array, int size) {
        if (size < array.length) {
            return array;
        }
        byte[] data = new byte[Math.max(size, array.length + (array.length >> 1))];
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
    public static byte[] delete(byte[] array, int size, int pos) {
        if (size > pos) {
            System.arraycopy(array, pos + 1, array, pos, size - pos - 1);
        }
        return array;
    }

}
