package rapaio.data;

import rapaio.data.format.InstantFormatter;
import rapaio.data.format.InstantParser;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.collection.LongArrays;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Variable which contains time instants truncated to milliseconds.
 * The stored data type is a long, which is actually the number of milliseconds
 * from epoch. The exposed data type is {@link java.time.Instant}.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/29/19.
 */
public class VarTime extends AbstractVar {

    public static VarTime empty(int rows) {
        return new VarTime(rows);
    }

    public static VarTime from(int rows, Function<Integer, Long> fun) {
        VarTime time = VarTime.empty(rows);
        for (int i = 0; i < rows; i++) {
            time.setLong(i, fun.apply(i));
        }
        return time;
    }

    public static final Instant MISSING_VALUE = Instant.EPOCH;

    private static final long serialVersionUID = -3619715862394998978L;
    private static final String STRING_CLASS_NAME = "VarInstant";
    private static final long MISSING_VALUE_LONG = MISSING_VALUE.toEpochMilli();

    private int rows;
    private long[] data;
    private InstantParser parser = InstantParser.ISO;
    private InstantFormatter formatter = InstantFormatter.ISO;

    private VarTime(int rows) {
        this.rows = rows;
        this.data = new long[rows];
        Arrays.fill(data, MISSING_VALUE_LONG);
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
        return VType.TIME;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        if (!LongArrays.checkCapacity(data, rows + rowCount)) {
            data = LongArrays.ensureCapacity(data, rows + rowCount);
        }
        LongArrays.fill(data, rows, rows + rowCount, MISSING_VALUE_LONG);
        rows += rowCount;
    }

    @Override
    public void removeRow(int row) {
        LongArrays.delete(data, rows, row);
    }

    @Override
    public void clearRows() {
        data = new long[0];
        rows = 0;
    }

    @Override
    public double getDouble(int row) {
        return data[row];
    }

    @Override
    public void setDouble(int row, double value) {
        data[row] = (long) value;
    }

    @Override
    public void addDouble(double value) {
        if (!LongArrays.checkCapacity(data, rows + 1)) {
            data = LongArrays.ensureCapacity(data, rows + 1);
        }
        data[rows++] = (long) value;
    }

    @Override
    public int getInt(int row) {
        return (int) data[row];
    }

    @Override
    public void setInt(int row, int value) {
        data[row] = value;
    }

    @Override
    public void addInt(int value) {
        if (!LongArrays.checkCapacity(data, rows + 1)) {
            data = LongArrays.ensureCapacity(data, rows + 1);
        }
        data[rows++] = value;
    }

    @Override
    public String getLabel(int row) {
        if (isMissing(row)) {
            return VarNominal.MISSING_VALUE;
        }
        return formatter.format(Instant.ofEpochMilli(data[row]));
    }

    @Override
    public void setLabel(int row, String value) {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            setMissing(row);
            return;
        }
        setLong(row, parser.parse(value).toEpochMilli());
    }

    @Override
    public void addLabel(String value) {
        if (VarNominal.MISSING_VALUE.equals(value)) {
            addMissing();
        } else {
            addLong(parser.parse(value).toEpochMilli());
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
        return data[row];
    }

    @Override
    public void setLong(int row, long value) {
        if (VarLong.MISSING_VALUE == value) {
            setMissing(row);
        } else {
            data[row] = value;
        }
    }

    @Override
    public void addLong(long value) {
        if (!LongArrays.checkCapacity(data, rows + 1)) {
            data = LongArrays.ensureCapacity(data, rows + 1);
        }
        data[rows++] = value;
    }

    @Override
    public boolean isMissing(int row) {
        return MISSING_VALUE_LONG == data[row];
    }

    @Override
    public void setMissing(int row) {
        data[row] = MISSING_VALUE_LONG;
    }

    @Override
    public void addMissing() {
        if (!LongArrays.checkCapacity(data, rows + 1)) {
            data = LongArrays.ensureCapacity(data, rows + 1);
        }
        data[rows++] = MISSING_VALUE_LONG;
    }

    @Override
    public Var newInstance(int rows) {
        return new VarTime(rows);
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>[] options) {
        tt.textCenter(i, j, getLabel(row));
    }
}
