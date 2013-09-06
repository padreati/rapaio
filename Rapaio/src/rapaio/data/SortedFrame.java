package rapaio.data;

import rapaio.data.util.AggregateComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Aurelian Tutuianu
 */
public class SortedFrame extends AbstractFrame {

    private final Frame df;
    private final ArrayList<Integer> mapping;

    public SortedFrame(Frame df, Comparator<Integer>... comparators) {
        this("", df, comparators);
    }

    public SortedFrame(String name, Frame df, Comparator<Integer>... comparators) {
        super(name);
        this.df = df;
        this.mapping = new ArrayList();
        for (int i = 0; i < df.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, new AggregateComparator(comparators));
    }

    @Override
    public int getRowCount() {
        return df.getRowCount();
    }

    @Override
    public int getColCount() {
        return df.getColCount();
    }

    @Override
    public int rowId(int row) {
        return mapping.get(row);
    }

    @Override
    public String[] getColNames() {
        return df.getColNames();
    }

    @Override
    public int getColIndex(String name) {
        return df.getColIndex(name);
    }

    @Override
    public Vector getCol(int col) {
        return new SortedVector(df.getCol(col), mapping);
    }

    @Override
    public Vector getCol(String name) {
        return new SortedVector(df.getCol(name), mapping);
    }
}
