package rapaio.util.collection;

import java.util.Iterator;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/7/19.
 */
public interface IntIterator extends Iterator<Integer> {

    boolean hasNext();

    @Override
    @Deprecated
    default Integer next() {
        return nextInt();
    }

    int nextInt();
}
