/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A bound variable is a variable which is obtained by binding observations
 * from multiple variables of the same type.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundVar extends AbstractVar {

    public static BoundVar from(List<Integer> counts, List<Var> vars) {
        return new BoundVar(counts, vars);
    }

    public static BoundVar from(List<Var> vars) {
        return new BoundVar(vars.stream().map(Var::rowCount).collect(Collectors.toList()), vars);
    }

    // private constructor

    public static BoundVar from(Var... vars) {
        return new BoundVar(
                Arrays.stream(vars).map(Var::rowCount).collect(Collectors.toList()),
                Arrays.stream(vars).collect(Collectors.toList())
        );
    }

    private static final long serialVersionUID = 5449912906816640189L;
    private final int rowCount;
    private final VarType varType;
    private final List<Integer> counts;
    private final List<Var> vars;

    // static builders

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
            if (vars.get(i) instanceof BoundVar) {
                BoundVar boundVar = (BoundVar) vars.get(i);
                for (int j = 0; j < boundVar.counts.size(); j++) {
                    this.counts.add(boundVar.counts.get(j) + last);
                    this.vars.add(boundVar.vars.get(j));
                }
                last += boundVar.rowCount;
            } else {
                this.counts.add(counts.get(i) + last);
                this.vars.add(vars.get(i));
                last += vars.get(i).rowCount();
            }
        }

        this.withName(vars.get(0).name());
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
    public int rowCount() {
        return rowCount;
    }

    @Override
    public Var bindRows(Var var) {
        if (var instanceof BoundVar) {
            List<Integer> newCounts = new ArrayList<>();
            List<Var> newVars = new ArrayList<>();
            int last = 0;
            for (int i = 0; i < counts.size(); i++) {
                newCounts.add(counts.get(i) - last);
                newVars.add(vars.get(i));
                last = counts.get(i);
            }
            BoundVar boundVar = (BoundVar) var;
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
        bindRows(newInstance(rowCount));
    }

    @Override
    public double value(int row) {
        int pos = findIndex(row);
        return vars.get(pos).value(localRow(pos, row));
    }

    @Override
    public void setValue(int row, double value) {
        int pos = findIndex(row);
        vars.get(pos).setValue(localRow(pos, row), value);
    }

    @Override
    public void addValue(double value) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public int index(int row) {
        int pos = findIndex(row);
        return vars.get(pos).index(localRow(pos, row));
    }

    @Override
    public void setIndex(int row, int value) {
        int pos = findIndex(row);
        vars.get(pos).setIndex(localRow(pos, row), value);
    }

    @Override
    public void addIndex(int value) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public String label(int row) {
        int pos = findIndex(row);
        return vars.get(pos).label(localRow(pos, row));
    }

    @Override
    public void setLabel(int row, String value) {
        int pos = findIndex(row);
        vars.get(pos).setLabel(localRow(pos, row), value);
    }

    @Override
    public void addLabel(String value) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public String[] levels() {
        return vars.get(0).levels();
    }

    @Override
    public void setLevels(String[] dict) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public boolean binary(int row) {
        int pos = findIndex(row);
        return vars.get(pos).binary(localRow(pos, row));
    }

    @Override
    public void setBinary(int row, boolean value) {
        int pos = findIndex(row);
        vars.get(pos).setBinary(localRow(pos, row), value);
    }

    @Override
    public void addBinary(boolean value) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public long stamp(int row) {
        int pos = findIndex(row);
        return vars.get(pos).stamp(localRow(pos, row));
    }

    @Override
    public void setStamp(int row, long value) {
        int pos = findIndex(row);
        vars.get(pos).setStamp(localRow(pos, row), value);
    }

    @Override
    public void addStamp(long value) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public boolean missing(int row) {
        int pos = findIndex(row);
        int localRow = localRow(pos, row);
        return vars.get(pos).missing(localRow);
    }

    @Override
    public void setMissing(int row) {
        int pos = findIndex(row);
        vars.get(pos).setMissing(localRow(pos, row));
    }

    @Override
    public void addMissing() {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public void remove(int row) {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public void clear() {
        throw new IllegalArgumentException("This operation is not available for bound variable");
    }

    @Override
    public Var newInstance(int rows) {
        if (vars.isEmpty())
            throw new IllegalArgumentException("this operation is not available for a bounded var with no rows");
        return vars.get(0).newInstance(rows);
    }
}
