package rapaio.data.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class AggregateComparator implements Comparator<Integer>, Serializable {

    private final Comparator<Integer>[] comparators;

    public AggregateComparator(Comparator<Integer>[] comparators) {
        this.comparators = comparators;
    }

    @Override
    public int compare(Integer row1, Integer row2) {
        for (Comparator<Integer> comparator : comparators) {
            int comp = comparator.compare(row1, row2);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }
}
