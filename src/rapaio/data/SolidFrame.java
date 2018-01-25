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

import rapaio.math.linear.RM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A frame which is not mapped, its values are contained in vectors.
 *
 * @author Aurelian Tutuianu
 */
public class SolidFrame extends AbstractFrame {

    public static SolidFrame byVars(List<? extends Var> vars) {
        int rows = vars.stream().mapToInt(Var::rowCount).min().orElse(0);
        return byVars(rows, vars);
    }

    public static SolidFrame byVars(Var... vars) {
        int rows = Integer.MAX_VALUE;
        for (Var var : vars) {
            rows = Math.min(rows, var.rowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return new SolidFrame(rows, Arrays.asList(vars));
    }

    public static SolidFrame byVars(int rows, Var... vars) {
        return byVars(rows, Arrays.asList(vars));
    }

    public static SolidFrame byVars(int rows, List<? extends Var> vars) {
        for (Var var : vars) {
            rows = Math.min(rows, var.rowCount());
        }
        return new SolidFrame(rows, vars);
    }

    /**
     * Builds a new frame with missing values, having the same variables
     * as in the source frame and having given row count.
     *
     * @param rowCount row count
     * @param src      source frame
     * @return new instance of solid frame built according with the source frame variables
     */
    public static SolidFrame emptyFrom(Frame src, int rowCount) {
        Var[] vars = new Var[src.varCount()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = src.rvar(i).type().newInstance(rowCount).withName(src.rvar(i).name());
        }
        return SolidFrame.byVars(vars);
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of rowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame matrix(int rows, String... colNames) {
        return matrix(rows, Arrays.asList(colNames));
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of rowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame matrix(int rows, List<String> colNames) {
        List<Var> vars = new ArrayList<>();
        colNames.forEach(n -> vars.add(NumVar.fill(rows, 0).withName(n)));
        return SolidFrame.byVars(rows, vars);
    }

    public static Frame matrix(RM rm, String... varNames) {
        Frame df = matrix(rm.rowCount(), varNames);
        for (int i = 0; i < rm.rowCount(); i++) {
            for (int j = 0; j < rm.colCount(); j++) {
                df.setValue(i, j, rm.get(i, j));
            }
        }
        return df;
    }

    private static final long serialVersionUID = 4963238370571140813L;
    private final Var[] vars;
    private final HashMap<String, Integer> colIndex;
    private final String[] names;
    private int rows;

    // public builders

    private SolidFrame(int rows, List<? extends Var> vars) {
        for (Var var : vars) {
            if (var instanceof MappedVar)
                throw new IllegalArgumentException("Not allowed mapped vectors in solid frame");
            if (var instanceof BoundVar)
                throw new IllegalArgumentException("Not allowed bounded vectors in solid frame");
        }
        this.rows = rows;
        this.vars = new Var[vars.size()];
        this.colIndex = new HashMap<>();
        this.names = new String[vars.size()];

        for (int i = 0; i < vars.size(); i++) {
            this.vars[i] = vars.get(i); //.copy();
            this.colIndex.put(this.vars[i].name(), i);
            this.names[i] = this.vars[i].name();
        }
    }

    // private constructor

    public static Frame matrix(RM rm, List<String> varNames) {
        Frame df = matrix(rm.rowCount(), varNames);
        for (int i = 0; i < rm.rowCount(); i++) {
            for (int j = 0; j < rm.colCount(); j++) {
                df.setValue(i, j, rm.get(i, j));
            }
        }
        return df;
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int varCount() {
        return vars.length;
    }

    @Override
    public String[] varNames() {
        return names;
    }

    @Override
    public String varName(int i) {
        return names[i];
    }

    @Override
    public int varIndex(String name) {
        if (!colIndex.containsKey(name)) {
            throw new IllegalArgumentException("Invalid column name: " + name);
        }
        return colIndex.get(name);
    }

    @Override
    public Var rvar(int col) {
        if (col >= 0 && col < vars.length) {
            return vars[col];
        }
        throw new IllegalArgumentException("Invalid column index: " + col);
    }

    @Override
    public Var rvar(String name) {
        return rvar(varIndex(name));
    }

    @Override
    public VarType type(String varName) {
        return rvar(varName).type();
    }

    @Override
    public Frame bindVars(Var... vars) {
        return BoundFrame.byVars(this, BoundFrame.byVars(vars));
    }

    @Override
    public Frame bindVars(Frame df) {
        return BoundFrame.byVars(this, df);
    }

    @Override
    public Frame mapVars(VRange range) {
        List<String> varNames = range.parseVarNames(this);
        List<Var> vars = varNames.stream().map(this::rvar).collect(Collectors.toList());
        return SolidFrame.byVars(rowCount(), vars);
    }

    @Override
    public Frame addRows(int rowCount) {
        varStream().forEach(var -> var.addRows(rowCount));
        this.rows += rowCount;
        return this;
    }

    @Override
    public Frame bindRows(Frame df) {
        return BoundFrame.byRows(this, df);
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        return MappedFrame.byRow(this, mapping);
    }

    @Override
    public double value(int row, int varIndex) {
        return vars[varIndex].value(row);
    }

    @Override
    public double value(int row, String varName) {
        return vars[varIndex(varName)].value(row);
    }

    @Override
    public void setValue(int row, int col, double value) {
        vars[col].setValue(row, value);
    }

    @Override
    public void setValue(int row, String varName, double value) {
        vars[varIndex(varName)].setValue(row, value);
    }

    @Override
    public int index(int row, int varIndex) {
        return vars[varIndex].index(row);
    }

    @Override
    public int index(int row, String varName) {
        return vars[varIndex(varName)].index(row);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        vars[col].setIndex(row, value);
    }

    @Override
    public void setIndex(int row, String varName, int value) {
        vars[varIndex(varName)].setIndex(row, value);
    }

    @Override
    public String label(int row, int col) {
        return vars[col].label(row);
    }

    @Override
    public String label(int row, String varName) {
        return vars[varIndex(varName)].label(row);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        vars[col].setLabel(row, value);
    }

    @Override
    public void setLabel(int row, String varName, String value) {
        vars[varIndex(varName)].setLabel(row, value);
    }

    @Override
    public String[] levels(String varName) {
        return vars[varIndex(varName)].levels();
    }

    @Override
    public String[] completeLevels(String varName) {
        return vars[varIndex(varName)].completeLevels();
    }

    @Override
    public boolean binary(int row, int col) {
        return vars[col].binary(row);
    }

    @Override
    public boolean binary(int row, String varName) {
        return vars[varIndex(varName)].binary(row);
    }

    @Override
    public void setBinary(int row, int col, boolean value) {
        vars[col].setBinary(row, value);
    }

    @Override
    public void setBinary(int row, String varName, boolean value) {
        vars[varIndex(varName)].setBinary(row, value);
    }

    @Override
    public boolean isMissing(int row, int col) {
        return vars[col].isMissing(row);
    }

    @Override
    public boolean isMissing(int row, String varName) {
        return vars[varIndex(varName)].isMissing(row);
    }

    @Override
    public void setMissing(int row, int col) {
        vars[col].setMissing(row);
    }

    @Override
    public void setMissing(int row, String varName) {
        vars[varIndex(varName)].setMissing(row);
    }
}
