package rapaio.filters;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.data.util.ColumnRange;

import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterRetainColumns {

    private final String rawColumnRange;

    public FilterRetainColumns(String rawColumnRange) {
        this.rawColumnRange = rawColumnRange;
    }

    public Frame filter(Frame df) {
        ColumnRange range = new ColumnRange(rawColumnRange);
        final List<Integer> indexes = range.parseColumnIndexes(df);
        Vector[] vectors = new Vector[indexes.size()];
        int posIndexes = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
                vectors[posIndexes] = df.getCol(i);
                posIndexes++;
            }
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }
}
