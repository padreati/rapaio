package rapaio.data;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

    private final String name;

    public AbstractFrame(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getValue(int row, int col) {
        return getCol(col).getValue(row);
    }

    @Override
    public void setValue(int row, int col, double value) {
        getCol(col).setValue(row, value);
    }

    @Override
    public int getIndex(int row, int col) {
        return getCol(col).getIndex(row);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        getCol(col).setIndex(row, value);
    }

    @Override
    public String getLabel(int row, int col) {
        return getCol(col).getLabel(row);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        getCol(col).setLabel(row, value);
    }
}
