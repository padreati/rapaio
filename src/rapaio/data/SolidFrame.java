/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
            this.colIndex.put(this.vars[i].name(), i);
            this.names[i] = this.vars[i].name();
        }
    }

    public static SolidFrame newWrapOf(List<Var> vars) {
        int rows = vars.stream().mapToInt(Var::rowCount).min().orElse(0);
        return newWrapOf(rows, vars);
    }

    public static SolidFrame newWrapOf(Var... vars) {
        int rows = Integer.MAX_VALUE;
        for (Var var : vars) {
            rows = Math.min(rows, var.rowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return new SolidFrame(rows, Arrays.asList(vars));
    }

    public static SolidFrame newWrapOf(int rows, Var... vars) {
        return newWrapOf(rows, Arrays.asList(vars));
    }

    public static SolidFrame newWrapOf(int rows, List<Var> vars) {
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
    public static SolidFrame newEmptyFrom(Frame src, int rowCount) {
        Var[] vars = new Var[src.varCount()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = src.var(i).type().newInstance(rowCount);
        }
        return SolidFrame.newWrapOf(vars);
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of getRowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame newMatrix(int rows, String... colNames) {
        return newMatrix(rows, Arrays.asList(colNames));
    }

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of getRowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame newMatrix(int rows, List<String> colNames) {
        List<Var> vars = new ArrayList<>();
        colNames.stream().forEach(n -> vars.add(Numeric.fill(rows, 0).withName(n)));
        return SolidFrame.newWrapOf(rows, vars);
    }

    public static Frame newMatrix(RM rm, String... varNames) {
        Frame df = newMatrix(rm.rowCount(), varNames);
        for (int i = 0; i < rm.rowCount(); i++) {
            for (int j = 0; j < rm.colCount(); j++) {
                df.setValue(i, j, rm.get(i, j));
            }
        }
        return df;
    }

    // private constructor

    public static Frame newMatrix(RM rm, List<String> varNames) {
        Frame df = newMatrix(rm.rowCount(), varNames);
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
    public int varIndex(String name) {
        if (!colIndex.containsKey(name)) {
            throw new IllegalArgumentException("Invalid column name: " + name);
        }
        return colIndex.get(name);
    }

    @Override
    public Var var(int col) {
        if (col >= 0 && col < vars.length) {
            return vars[col];
        }
        throw new IllegalArgumentException("Invalid column index: " + col);
    }

    @Override
    public Var var(String name) {
        return var(varIndex(name));
    }

    @Override
    public Frame bindVars(Var... vars) {
        return BoundFrame.newByVars(this, BoundFrame.newByVars(vars));
    }

    @Override
    public Frame bindVars(Frame df) {
        return BoundFrame.newByVars(this, df);
    }

    @Override
    public Frame mapVars(VarRange range) {
        List<String> varNames = range.parseVarNames(this);
        List<Var> vars = varNames.stream().map(this::var).collect(Collectors.toList());
        return SolidFrame.newWrapOf(rowCount(), vars);
    }

    @Override
    public Frame addRows(int rowCount) {
        varStream().forEach(var -> var.addRows(rowCount));
        this.rows += rowCount;
        return this;
    }

    @Override
    public Frame bindRows(Frame df) {
        return BoundFrame.newByRows(this, df);
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        return MappedFrame.newByRow(this, mapping);
    }
}
