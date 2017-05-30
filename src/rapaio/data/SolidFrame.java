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

    public static SolidFrame byVars(List<Var> vars) {
        int rows = vars.stream().mapToInt(Var::getRowCount).min().orElse(0);
        return byVars(rows, vars);
    }

    public static SolidFrame byVars(Var... vars) {
        int rows = Integer.MAX_VALUE;
        for (Var var : vars) {
            rows = Math.min(rows, var.getRowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return new SolidFrame(rows, Arrays.asList(vars));
    }

    public static SolidFrame byVars(int rows, Var... vars) {
        return byVars(rows, Arrays.asList(vars));
    }

    public static SolidFrame byVars(int rows, List<Var> vars) {
        for (Var var : vars) {
            rows = Math.min(rows, var.getRowCount());
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
        Var[] vars = new Var[src.getVarCount()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = src.getVar(i).getType().newInstance(rowCount);
        }
        return SolidFrame.byVars(vars);
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of getRowCount
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
     * @param rows     number of getRowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame matrix(int rows, List<String> colNames) {
        List<Var> vars = new ArrayList<>();
        colNames.forEach(n -> vars.add(NumericVar.fill(rows, 0).withName(n)));
        return SolidFrame.byVars(rows, vars);
    }

    public static Frame matrix(RM rm, String... varNames) {
        Frame df = matrix(rm.getRowCount(), varNames);
        for (int i = 0; i < rm.getRowCount(); i++) {
            for (int j = 0; j < rm.getColCount(); j++) {
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

    private SolidFrame(int rows, List<Var> vars) {
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
            this.colIndex.put(this.vars[i].getName(), i);
            this.names[i] = this.vars[i].getName();
        }
    }

    // private constructor

    public static Frame matrix(RM rm, List<String> varNames) {
        Frame df = matrix(rm.getRowCount(), varNames);
        for (int i = 0; i < rm.getRowCount(); i++) {
            for (int j = 0; j < rm.getColCount(); j++) {
                df.setValue(i, j, rm.get(i, j));
            }
        }
        return df;
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getVarCount() {
        return vars.length;
    }

    @Override
    public String[] getVarNames() {
        return names;
    }

    @Override
    public int getVarIndex(String name) {
        if (!colIndex.containsKey(name)) {
            throw new IllegalArgumentException("Invalid column name: " + name);
        }
        return colIndex.get(name);
    }

    @Override
    public Var getVar(int col) {
        if (col >= 0 && col < vars.length) {
            return vars[col];
        }
        throw new IllegalArgumentException("Invalid column index: " + col);
    }

    @Override
    public Var getVar(String name) {
        return getVar(getVarIndex(name));
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
        List<Var> vars = varNames.stream().map(this::getVar).collect(Collectors.toList());
        return SolidFrame.byVars(getRowCount(), vars);
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
}
