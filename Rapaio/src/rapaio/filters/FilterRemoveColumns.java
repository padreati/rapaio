package rapaio.filters;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.data.util.ColumnRange;

import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterRemoveColumns {

    private final String rawColumnRange;

    public FilterRemoveColumns(String rawColumnRange) {
        this.rawColumnRange = rawColumnRange;
    }

    public Frame filter(Frame df) {
        ColumnRange range = new ColumnRange(rawColumnRange);
        final List<Integer> indexes = range.parseColumnIndexes(df);
        Vector[] vectors = new Vector[df.getColCount() - indexes.size()];
        int posIndexes = 0;
        int posFinal = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
                posIndexes++;
                continue;
            }
            vectors[posFinal] = df.getCol(i);
            posFinal++;
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }
}
