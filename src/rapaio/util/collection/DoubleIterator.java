package rapaio.util.collection;

import java.util.Iterator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface DoubleIterator extends Iterator<Double> {

    boolean hasNext();

    @Override
    @Deprecated
    default Double next() {
        return nextDouble();
    }

    double nextDouble();
}
