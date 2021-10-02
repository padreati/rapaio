/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.util.function.SFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarString extends AbstractVar {

    public static final String MISSING_VALUE = "?";

    public static VarString empty() {
        return new VarString(0);
    }

    public static VarString empty(int rows) {
        return new VarString(rows);
    }

    public static VarString copy(String... values) {
        VarString text = new VarString(0);
        text.values = Arrays.stream(values).collect(Collectors.toList());
        return text;
    }

    public static VarString copy(List<String> values) {
        VarString text = new VarString(0);
        text.values = new ArrayList<>(values);
        return text;
    }

    public static VarString wrap(List<String> values) {
        VarString text = new VarString(0);
        text.values = values;
        return text;
    }

    public static VarString from(int rows, Supplier<String> supplier) {
        VarString text = new VarString(rows);
        for (int i = 0; i < rows; i++) {
            text.values.set(i, supplier.get());
        }
        return text;
    }

    public static VarString from(int rows, SFunction<Integer, String> function) {
        VarString text = new VarString(rows);
        for (int i = 0; i < rows; i++) {
            text.values.set(i, function.apply(i));
        }
        return text;
    }

    @Serial
    private static final long serialVersionUID = -7130782019269889796L;
    private List<String> values;

    private VarString(int rows) {
        values = new ArrayList<>(rows);
        for (int i = 0; i < rows; i++) {
            values.add(null);
        }
    }

    @Override
    public VarString name(String name) {
        return (VarString) super.name(name);
    }

    @Override
    public VarType type() {
        return VarType.STRING;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public void addRows(int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            values.add(null);
        }
    }

    @Override
    public void removeRow(int row) {
        values.remove(row);
    }

    @Override
    public void clearRows() {
        values.clear();
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
        return values.get(row);
    }

    @Override
    public void setLabel(int row, String value) {
        values.set(row, value);
    }

    @Override
    public void addLabel(String value) {
        values.add(value);
    }

    @Override
    public List<String> levels() {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setLevels(String[] dict) {
        throw new OperationNotAvailableException();
    }

    @Override
    public long getLong(int row) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setLong(int row, long value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addLong(long value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void addInstant(Instant value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void setInstant(int row, Instant value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public Instant getInstant(int row) {
        throw new OperationNotAvailableException();
    }

    @Override
    public boolean isMissing(int row) {
        return MISSING_VALUE.equals(values.get(row));
    }

    @Override
    public void setMissing(int row) {
        values.set(row, MISSING_VALUE);
    }

    @Override
    public void addMissing() {
        values.add(MISSING_VALUE);
    }

    @Override
    public VarString newInstance(int rows) {
        return VarString.empty(rows);
    }

    @Override
    public VarString copy() {
        VarString copy = new VarString(0).name(name());
        copy.values = new ArrayList<>(values);
        return copy;
    }

    @Override
    public boolean deepEquals(Var var) {
        if (var.type() != VarType.STRING) {
            return false;
        }
        if (var.size() != values.size()) {
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            String val1 = values.get(i);
            String val2 = var.getLabel(i);

            if (val1 == null && val2 != null) {
                return false;
            }
            if (val1 != null && val2 == null) {
                return false;
            }
            if ((val1 != null) && val1.compareTo(val2) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POption<?>[] options) {
        tt.textCenter(i, j, getLabel(row));
    }

    @Override
    protected String toStringClassName() {
        return "VarText";
    }

    @Override
    protected int toStringDisplayValueCount() {
        return 12;
    }
}
