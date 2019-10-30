package rapaio.data;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import rapaio.data.format.InstantFormatter;
import rapaio.data.format.InstantParser;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Variable which contains time instants truncated to milliseconds.
 * The stored data type is a long, which is actually the number of milliseconds
 * from epoch. The exposed data type is {@link java.time.Instant}.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/29/19.
 */
public class VarInstant extends AbstractVar {

    public static VarInstant empty(int rows) {
        return new VarInstant(rows);
    }

    public static final Instant MISSING_VALUE = Instant.EPOCH;

    private static final long serialVersionUID = -3619715862394998978L;
    private static final String STRING_CLASS_NAME = "VarInstant";
    private static final long MISSING_VALUE_LONG = MISSING_VALUE.toEpochMilli();

    private LongArrayList data;
    private InstantParser parser = InstantParser.ISO;
    private InstantFormatter formatter = InstantFormatter.ISO;

    private VarInstant(int rows) {
        long[] internalArray = new long[rows];
        Arrays.fill(internalArray, MISSING_VALUE_LONG);
        data = LongArrayList.wrap(internalArray);
    }

    @Override
    protected String classNameInToString() {
        return STRING_CLASS_NAME;
    }

    @Override
    protected int elementsInToString() {
        return 14;
    }

    @Override
    public VType type() {
        return VType.INSTANT;
    }

    @Override
    public int rowCount() {
        return data.size();
    }

    @Override
    public void addRows(int rowCount) {
        int oldSize = data.size();
        data.size(oldSize + rowCount);
        for (int i = oldSize; i < oldSize + rowCount; i++) {
            data.set(i, MISSING_VALUE_LONG);
        }
    }

    @Override
    public void removeRow(int row) {
        data.removeLong(row);
    }

    @Override
    public void clearRows() {
        data.clear();
    }

    @Override
    public double getDouble(int row) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setDouble(int row, double value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addDouble(double value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int getInt(int row) {
        throw new OperationNotAvailableException();

    }

    @Override
    public void setInt(int row, int value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addInt(int value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public String getLabel(int row) {
        if (isMissing(row)) {
            return VarNominal.MISSING_VALUE;
        }
        return formatter.format(Instant.ofEpochMilli(data.getLong(row)));
    }

    @Override
    public void setLabel(int row, String value) {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            setMissing(row);
            return;
        }
        data.set(row, parser.parse(value).toEpochMilli());
    }

    @Override
    public void addLabel(String value) {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            addMissing();
        } else {
            data.add(parser.parse(value).toEpochMilli());
        }
    }

    @Override
    public List<String> levels() {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setLevels(String... dict) {
        throw new OperationNotAvailableException();
    }

    @Override
    public long getLong(int row) {
        if (isMissing(row)) {
            return VarLong.MISSING_VALUE;
        }
        return data.getLong(row);
    }

    @Override
    public void setLong(int row, long value) {
        if (VarLong.MISSING_VALUE == value) {
            setMissing(row);
        } else {
            data.set(row, value);
        }
    }

    @Override
    public void addLong(long value) {
        if (VarLong.MISSING_VALUE == value) {
            addMissing();
        } else {
            data.add(value);
        }
    }

    @Override
    public boolean isMissing(int row) {
        return MISSING_VALUE_LONG == data.getLong(row);
    }

    @Override
    public void setMissing(int row) {
        data.set(row, MISSING_VALUE_LONG);
    }

    @Override
    public void addMissing() {
        data.add(MISSING_VALUE_LONG);
    }

    @Override
    public Var newInstance(int rows) {
        return new VarInstant(rows);
    }
}
