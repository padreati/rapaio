package rapaio.util.collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/22/20.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TArrays {

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
}
