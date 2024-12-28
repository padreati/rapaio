/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.data;

import java.io.Serial;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;
import rapaio.text.Formatter;
import rapaio.text.Formatters;
import rapaio.text.Parser;
import rapaio.text.Parsers;

/**
 * Variable which contains time instants truncated to milliseconds.
 * The stored data type is a long, which is actually the number of milliseconds
 * from epoch. The exposed data type is {@link java.time.Instant}.
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/29/19.
 */
public class VarInstant extends AbstractVar {

    public static VarInstant empty() {
        return new VarInstant(0);
    }

    public static VarInstant empty(int rows) {
        return new VarInstant(rows);
    }

    public static VarInstant from(int rows, Function<Integer, Instant> fun) {
        VarInstant time = VarInstant.empty(rows);
        for (int i = 0; i < rows; i++) {
            time.setInstant(i, fun.apply(i));
        }
        return time;
    }

    public static VarInstant from(long... values) {
        VarInstant time = VarInstant.empty(values.length);
        for (int i = 0; i < time.size(); i++) {
            time.setLong(i, values[i]);
        }
        return time;
    }

    @Serial
    private static final long serialVersionUID = -3619715862394998978L;

    private static final String STRING_CLASS_NAME = "VarInstant";
    public static final Instant MISSING_VALUE = null;

    private int rows;
    private Instant[] data;
    private Parser<Instant> parser = Parsers.DEFAULT_VAR_INSTANT_PARSER;
    private Formatter<Instant> formatter = Formatters.DEFAULT_VAR_INSTANT_FORMATTER;


    private VarInstant(int rows) {
        this.rows = rows;
        this.data = new Instant[rows];
    }

    public Parser<Instant> getParser() {
        return parser;
    }

    public VarInstant withParser(Parser<Instant> parser) {
        this.parser = parser;
        return this;
    }

    public Formatter<Instant> getFormatter() {
        return formatter;
    }

    public VarInstant withFormatter(Formatter<Instant> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    protected String toStringClassName() {
        return STRING_CLASS_NAME;
    }

    @Override
    protected int toStringDisplayValueCount() {
        return 8;
    }

    @Override
    public VarType type() {
        return VarType.INSTANT;
    }

    @Override
    public int size() {
        return rows;
    }

    @Override
    public void addRows(int rowCount) {
        ensureCapacity(rows + rowCount);
        rows += rowCount;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int oldCapacity = data.length;
            int newCapacity = oldCapacity > 0xFFFF ? oldCapacity << 1 : oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    @Override
    public void removeRow(int row) {
        if (rows - row > 0) {
            System.arraycopy(data, row + 1, data, row, rows - row - 1);
        }
    }

    @Override
    public void clearRows() {
        data = new Instant[0];
        rows = 0;
    }

    @Override
    public float getFloat(int row) {
        return data[row].toEpochMilli();
    }

    @Override
    public void setFloat(int row, float value) {
        data[row] = Instant.ofEpochMilli((long) value);
    }

    @Override
    public void addFloat(float value) {
        ensureCapacity(rows + 1);
        data[rows++] = Instant.ofEpochMilli((long) value);
    }

    @Override
    public double getDouble(int row) {
        return data[row].toEpochMilli();
    }

    @Override
    public void setDouble(int row, double value) {
        data[row] = Instant.ofEpochMilli((long) value);
    }

    @Override
    public void addDouble(double value) {
        ensureCapacity(rows + 1);
        data[rows++] = Instant.ofEpochMilli((long) value);
    }

    @Override
    public int getInt(int row) {
        return (int) data[row].toEpochMilli();
    }

    @Override
    public void setInt(int row, int value) {
        data[row] = Instant.ofEpochMilli(value);
    }

    @Override
    public void addInt(int value) {
        ensureCapacity(rows + 1);
        data[rows++] = Instant.ofEpochMilli(value);
    }

    @Override
    public String getLabel(int row) {
        return formatter.format(data[row]);
    }

    @Override
    public void setLabel(int row, String value) {
        setInstant(row, parser.parse(value));
    }

    @Override
    public void addLabel(String value) {
        addInstant(parser.parse(value));
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
        return data[row].toEpochMilli();
    }

    @Override
    public void setLong(int row, long value) {
        if (VarLong.MISSING_VALUE == value) {
            setMissing(row);
        } else {
            data[row] = Instant.ofEpochMilli(value);
        }
    }

    @Override
    public void addLong(long value) {
        ensureCapacity(rows + 1);
        data[rows++] = Instant.ofEpochMilli(value);
    }

    @Override
    public void addInstant(Instant value) {
        ensureCapacity(rows + 1);
        data[rows++] = value;
    }

    @Override
    public void setInstant(int row, Instant value) {
        data[row] = value;
    }

    @Override
    public Instant getInstant(int row) {
        return data[row];
    }

    @Override
    public boolean isMissing(int row) {
        return data[row] == MISSING_VALUE;
    }

    @Override
    public void setMissing(int row) {
        data[row] = MISSING_VALUE;
    }

    @Override
    public void addMissing() {
        ensureCapacity(rows + 1);
        data[rows++] = null;
    }

    @Override
    public Var newInstance(int rows) {
        return new VarInstant(rows);
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POpt<?>[] options) {
        tt.textCenter(i, j, getLabel(row));
    }
}
