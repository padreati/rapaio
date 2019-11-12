package rapaio.util.collection;

import java.util.Comparator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/8/19.
 */
public interface IntComparator extends Comparator<Integer> {

    IntComparator NATURAL_COMPARATOR = Integer::compare;

    IntComparator REVERSE_COMPARATOR = (i1, i2) -> -Integer.compare(i1, i2);

    @Override
    @Deprecated
    default int compare(Integer o1, Integer o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return compareInt(o1, o2);
    }

    int compareInt(int i1, int i2);
}
