package rapaio.util.collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/22/20.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TArrays {

    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    @SuppressWarnings("unchecked")
    public static <T> T[] copyOfRange(T[] source, int from, int to, int len, int offset) {
        T[] copy = (T[]) Array.newInstance(source.getClass().getComponentType(), len);
        System.arraycopy(source, from, copy, offset, to - from);
        return copy;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] copyValueFirst(T value, T[] source) {
        T[] copy = (T[]) Array.newInstance(source.getClass().getComponentType(), source.length + 1);
        System.arraycopy(source, 0, copy, 1, source.length);
        copy[0] = value;
        return copy;

    }

    /**
     * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array of given length.
     *
     * <p>This method may be used whenever an array range check is needed.
     *
     * @param arrayLength an array length.
     * @param from        a start index (inclusive).
     * @param to          an end index (inclusive).
     * @throws IllegalArgumentException       if {@code from} is greater than {@code to}.
     * @throws ArrayIndexOutOfBoundsException if {@code from} or {@code to} are greater than {@code arrayLength} or negative.
     */
    public static void ensureFromTo(final int arrayLength, final int from, final int to) {
        if (from < 0) throw new ArrayIndexOutOfBoundsException("Start index (" + from + ") is negative");
        if (from > to) throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
        if (to > arrayLength)
            throw new ArrayIndexOutOfBoundsException("End index (" + to + ") is greater than array length (" + arrayLength + ")");
    }

    /**
     * Ensures that a range given by an offset and a length fits an array of given length.
     *
     * <p>This method may be used whenever an array range check is needed.
     *
     * @param arrayLength an array length.
     * @param offset      a start index for the fragment
     * @param length      a length (the number of elements in the fragment).
     * @throws IllegalArgumentException       if {@code length} is negative.
     * @throws ArrayIndexOutOfBoundsException if {@code offset} is negative or {@code offset}+{@code length} is greater than {@code arrayLength}.
     */
    public static void ensureOffsetLength(final int arrayLength, final int offset, final int length) {
        if (offset < 0) throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
        if (length < 0) throw new IllegalArgumentException("Length (" + length + ") is negative");
        if (offset + length > arrayLength)
            throw new ArrayIndexOutOfBoundsException("Last index (" + (offset + length) + ") is greater than array length (" + arrayLength + ")");
    }
}
