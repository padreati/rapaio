/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

/**
 * A bound variable is a variable which is obtained by binding observations
 * from multiple variables of the same type.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundVar extends AbstractVar {

    public static BoundVar from(List<Integer> counts, List<Var> vars) {
        return new BoundVar(counts, vars);
    }

    public static BoundVar from(List<Var> vars) {
        return new BoundVar(vars.stream().map(Var::size).collect(Collectors.toList()), vars);
    }

    public static BoundVar from(Var... vars) {
        return new BoundVar(
                Arrays.stream(vars).map(Var::size).collect(Collectors.toList()),
                Arrays.stream(vars).collect(Collectors.toList())
        );
    }

    @Serial
    private static final long serialVersionUID = 5449912906816640189L;
    private final int rowCount;
    private final VarType varType;
    private final List<Integer> counts;
    private final List<Var> vars;

    private BoundVar(List<Integer> counts, List<Var> vars) {
        if (vars.isEmpty())
            throw new IllegalArgumentException("List of vars is empty");
        if (counts.isEmpty())
            throw new IllegalArgumentException("List of counts is empty");
        if (vars.size() != counts.size())
            throw new IllegalArgumentException("List of counts is not equal with list of variables");
        if (vars.stream().map(Var::type).distinct().count() != 1)
            throw new IllegalArgumentException("It is not allowed to bind variables of different types");

        this.rowCount = counts.stream().mapToInt(i -> i).sum();
        this.varType = vars.get(0).type();
        this.counts = new ArrayList<>();
        this.vars = new ArrayList<>();

        int last = 0;
        for (int i = 0; i < counts.size(); i++) {
            if (vars.get(i) instanceof BoundVar boundVar) {
                for (int j = 0; j < boundVar.counts.size(); j++) {
                    this.counts.add(boundVar.counts.get(j) + last);
                    this.vars.add(boundVar.vars.get(j));
                }
                last += boundVar.rowCount;
            } else {
                this.counts.add(counts.get(i) + last);
                this.vars.add(vars.get(i));
                last += vars.get(i).size();
            }
        }
        this.name(vars.get(0).name());
    }

    private int findIndex(int row) {
        if (row >= rowCount || row < 0)
            throw new IllegalArgumentException("Row index is not valid: " + row);
        int pos = Collections.binarySearch(counts, row);
        return pos >= 0 ? (counts.get(pos) == row ? pos + 1 : pos) : -pos - 1;
    }

    private int localRow(int pos, int row) {
        return pos > 0 ? row - counts.get(pos - 1) : row;
    }

    @Override
    public VarType type() {
        return varType;
    }

    @Override
    public int size() {
        return rowCount;
    }

    @Override
    public Var bindRows(Var var) {
        if (var instanceof BoundVar boundVar) {
            List<Integer> newCounts = new ArrayList<>();
            List<Var> newVars = new ArrayList<>();
            int last = 0;
            for (int i = 0; i < counts.size(); i++) {
                newCounts.add(counts.get(i) - last);
                newVars.add(vars.get(i));
                last = counts.get(i);
            }
            last = 0;
            for (int i = 0; i < boundVar.counts.size(); i++) {
                newCounts.add(boundVar.counts.get(i) - last);
                newVars.add(boundVar.vars.get(i));
                last = boundVar.counts.get(i);
            }
            return BoundVar.from(newCounts, newVars);
        } else {
            return BoundVar.from(this, var);
        }
    }

    @Override
    public void addRows(int rowCount) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void removeRow(int row) {
        throw new OperationNotAvailableException();
    }

    @Override
    public void clearRows() {
        throw new OperationNotAvailableException();
    }

    @Override
    public float getFloat(int row) {
        int pos = findIndex(row);
        return vars.get(pos).getFloat(localRow(pos, row));
    }

    @Override
    public void setFloat(int row, float value) {
        int pos = findIndex(row);
        vars.get(pos).setFloat(localRow(pos, row), value);
    }

    @Override
    public void addFloat(float value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public double getDouble(int row) {
        int pos = findIndex(row);
        return vars.get(pos).getDouble(localRow(pos, row));
    }

    @Override
    public void setDouble(int row, double value) {
        int pos = findIndex(row);
        vars.get(pos).setDouble(localRow(pos, row), value);
    }

    @Override
    public void addDouble(double value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public int getInt(int row) {
        int pos = findIndex(row);
        return vars.get(pos).getInt(localRow(pos, row));
    }

    @Override
    public void setInt(int row, int value) {
        int pos = findIndex(row);
        vars.get(pos).setInt(localRow(pos, row), value);
    }

    @Override
    public void addInt(int value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public String getLabel(int row) {
        int pos = findIndex(row);
        return vars.get(pos).getLabel(localRow(pos, row));
    }

    @Override
    public void setLabel(int row, String value) {
        int pos = findIndex(row);
        vars.get(pos).setLabel(localRow(pos, row), value);
    }

    @Override
    public void addLabel(String value) {
        throw new OperationNotAvailableException();
    }

    @Override
    public List<String> levels() {
        return vars.get(0).levels();
    }

    @Override
    public void setLevels(String[] dict) {
        throw new OperationNotAvailableException();
    }

    @Override
    public long getLong(int row) {
        int pos = findIndex(row);
        return vars.get(pos).getLong(localRow(pos, row));
    }

    @Override
    public void setLong(int row, long value) {
        int pos = findIndex(row);
        vars.get(pos).setLong(localRow(pos, row), value);
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
        int pos = findIndex(row);
        vars.get(pos).setInstant(localRow(pos, row), value);
    }

    @Override
    public Instant getInstant(int row) {
        int pos = findIndex(row);
        return vars.get(pos).getInstant(localRow(pos, row));
    }

    @Override
    public boolean isMissing(int row) {
        int pos = findIndex(row);
        int localRow = localRow(pos, row);
        return vars.get(pos).isMissing(localRow);
    }

    @Override
    public void setMissing(int row) {
        int pos = findIndex(row);
        vars.get(pos).setMissing(localRow(pos, row));
    }

    @Override
    public void addMissing() {
        throw new OperationNotAvailableException();
    }

    @Override
    public Var newInstance(int rows) {
        return vars.get(0).newInstance(rows);
    }

    @Override
    protected String toStringClassName() {
        return "BoundVar(type=" + vars.get(0).type().code() + ")";
    }

    @Override
    protected int toStringDisplayValueCount() {
        if (vars.get(0) instanceof AbstractVar) {
            return ((AbstractVar) vars.get(0)).toStringDisplayValueCount();
        }
        return 10;
    }

    @Override
    protected void textTablePutValue(TextTable tt, int i, int j, int row, Printer printer, POpt<?>[] options) {
        if (vars.get(0) instanceof AbstractVar) {
            ((AbstractVar) vars.get(0)).textTablePutValue(tt, i, j, row, printer, options);
        } else {
            tt.textCenter(i, j, getLabel(row));
        }
    }
}
