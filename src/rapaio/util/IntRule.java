package rapaio.util;

import java.util.function.Predicate;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/12/20.
 */
public interface IntRule extends Predicate<Integer> {

    static IntRule none() {
        return row -> false;
    }

    static IntRule all() {
        return row -> true;
    }

    static IntRule less(int end) {
        return row -> row < end;
    }

    static IntRule greater(int start) {
        return row -> row > start;
    }

    static IntRule geq(int start) {
        return row -> row >= start;
    }

    static IntRule leq(int end) {
        return row -> row <= end;
    }

    static IntRule range(int start, int end) {
        return row -> row >= start && row < end;
    }

    static IntRule from(int... array) {
        return row -> {
            for (int r : array) {
                if (row == r) {
                    return true;
                }
            }
            return false;
        };
    }
}
