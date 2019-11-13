package rapaio.util.collection;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/11/19.
 */
public interface DoubleComparator extends Comparator<Double>, Serializable {

    @Override
    default int compare(Double o1, Double o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        return compareDouble(o1, o2);
    }

    int compareDouble(double o1, double o2);

    DoubleComparator ASC_COMPARATOR = Double::compare;

    DoubleComparator DESC_COMPARATOR = (x, y) -> -Double.compare(x, y);
}
