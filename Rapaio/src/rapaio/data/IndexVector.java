package rapaio.data;

import java.util.Comparator;

/**
 * @author Aurelian Tutuianu
 */
public class IndexVector extends AbstractVector {

    private static final int missingValue = Integer.MIN_VALUE;
    private final int[] values;

    public IndexVector(String name, int size) {
        super(name);
        this.values = new int[size];
    }

    public IndexVector(String name, int size, int fill) {
        this(name, size);
        for (int i = 0; i < values.length; i++) {
            values[i] = fill;
        }
    }

    public IndexVector(String name, int from, int to, int step) {
        super(name);
        int len = (to - from) / step;
        if ((to - from) % step == 0) {
            len++;
        }
        values = new int[len];
        for (int i = 0; i < len; i++) {
            values[i] = from + i * step;
        }
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean isNominal() {
        return false;
    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public int getRowId(int row) {
        return row;
    }

    @Override
    public int getIndex(int row) {
        return values[row];
    }

    @Override
    public void setIndex(int row, int value) {
        values[row] = value;
    }

    @Override
    public double getValue(int row) {
        return getIndex(row);
    }

    @Override
    public void setValue(int row, double value) {
        setIndex(row, (int) value);
    }

    @Override
    public String getLabel(int row) {
        return "";
    }

    @Override
    public void setLabel(int row, String value) {
    }

    @Override
    public String[] dictionary() {
        return new String[0];
    }

    @Override
    public boolean isMissing(int row) {
        return getIndex(row) == missingValue;
    }

    @Override
    public void setMissing(int row) {
        setIndex(row, missingValue);
    }

    @Override
    public Comparator<Integer> getComparator(final boolean asc) {
        final int sign = asc ? 1 : -1;

        return new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                if (isMissing(row1) && isMissing(row2)) {
                    return 0;
                }
                if (isMissing(row1)) {
                    return -1 * sign;
                }
                if (isMissing(row2)) {
                    return sign;
                }
                if (getIndex(row1) == getIndex(row2)) {
                    return 0;
                }
                return sign * (getIndex(row1) < getIndex(row2) ? -1 : 1);
            }
        };
    }
}
