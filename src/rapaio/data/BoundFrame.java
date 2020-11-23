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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoundFrame extends AbstractFrame {

    /**
     * Builds a new bound frame by binding variables of multiple given frames.
     * All variable names must be unique among all the given frames.
     * The row count is the minimum of the row counts from all the given frames.
     *
     * @param dfs collection of given data frames
     * @return new frame bound frame by binding variables
     */
    public static BoundFrame byVars(Frame... dfs) {
        Map<String, Integer> indexes = new HashMap<>();
        List<Var> vars = new ArrayList<>();
        if (dfs.length == 0) {
            return new BoundFrame(0, vars, new String[]{}, indexes);
        }
        List<String> names = new ArrayList<>();
        Set<String> namesSet = new HashSet<>();

        int pos = 0;
        int rowCount = dfs[0].rowCount();
        for (Frame df : dfs) {
            rowCount = Math.min(rowCount, df.rowCount());
            for (int j = 0; j < df.varCount(); j++) {
                if (namesSet.contains(df.rvar(j).name())) {
                    throw new IllegalArgumentException("bound frame does not allow variables with the same name: " + df.rvar(j).name());
                }
                vars.add(df.rvar(j));
                names.add(df.rvar(j).name());
                namesSet.add(df.rvar(j).name());
                indexes.put(df.rvar(j).name(), pos++);
            }
        }
        return new BoundFrame(rowCount, vars, names.toArray(new String[0]), indexes);
    }

    /**
     * Builds a new bound frame by binding multiple variables.
     * All variable names must be unique among.
     * The row count is the minimum of the row counts from all the given variables.
     *
     * @param varList collection of given variables
     * @return new frame bound frame by binding variables
     */
    public static BoundFrame byVars(List<Var> varList) {
        Map<String, Integer> indexes = new HashMap<>();
        List<Var> vars = new ArrayList<>();
        if (varList.isEmpty()) {
            return new BoundFrame(0, vars, new String[]{}, indexes);
        }
        List<String> names = new ArrayList<>();
        Set<String> namesSet = new HashSet<>();

        int pos = 0;
        int rowCount = varList.get(0).rowCount();
        for (Var var : varList) {
            if (namesSet.contains(var.name())) {
                throw new IllegalArgumentException("bound frame does not allow variables with the same name: " + var.name());
            }
            rowCount = Math.min(rowCount, var.rowCount());
            vars.add(var);
            names.add(var.name());
            namesSet.add(var.name());
            indexes.put(var.name(), pos++);
        }
        return new BoundFrame(rowCount, vars, names.toArray(new String[0]), indexes);
    }

    /**
     * Builds a new bound frame by binding given variables.
     * All variable names must be unique.
     * The row count is the minimum of the row counts from all the given variables.
     *
     * @param varList given data variables
     * @return new bound frame obtained by by binding variables
     */
    public static BoundFrame byVars(Var... varList) {
        return byVars(Arrays.asList(varList));
    }

    /**
     * Builds a new bound frame by binding rows of the given data frames.
     * All data frames must have the same number of variables
     * with same names and types in the same order.
     * The rows count is the sum of all rows from all frames.
     * The order of the rows is given by the specified order of data frames.
     *
     * @param dfs given data frames to be bound
     * @return new bound frame obtained by concatenating rows
     */
    public static BoundFrame byRows(Frame... dfs) {
        Map<String, Integer> indexes = new HashMap<>();
        List<Var> vars = new ArrayList<>();
        if (dfs.length == 0) {
            return new BoundFrame(0, vars, new String[]{}, indexes);
        }

        String[] names = dfs[0].varNames();

        // check that in each frame to exist all the variables and to have the same type
        // otherwise throw an exception

        for (int i = 1; i < dfs.length; i++) {
            String[] compNames = dfs[i].varNames();
            validateCompatibility(dfs[0], dfs[i]);
        }


        // for each var name build a bounded var from all the rows from all the frames

        for (int i = 0; i < names.length; i++) {

            List<Integer> counts = new ArrayList<>();
            List<Var> boundVars = new ArrayList<>();

            for (Frame df : dfs) {
                counts.add(df.rowCount()); // avoid to take rowCount from variable, but from frame
                boundVars.add(df.rvar(names[i]));
            }

            Var boundedVar = BoundVar.from(counts, boundVars).name(names[i]);
            vars.add(boundedVar);
            indexes.put(names[i], i);
        }

        int rowCount = Arrays.stream(dfs).mapToInt(Frame::rowCount).sum();

        return new BoundFrame(rowCount, vars, names, indexes);
    }

    private static void validateCompatibility(Frame df1, Frame df2) {
        String[] varNames1 = df1.varNames();
        String[] varNames2 = df2.varNames();

        if (varNames1.length != varNames2.length) {
            throw new IllegalArgumentException("Can't bind by rows frames with different variable counts.");
        }
        for (int i = 0; i < varNames1.length; i++) {
            if (!varNames1[i].equals(varNames2[i])) {
                throw new IllegalArgumentException("Can't bind by rows frames with different variable " +
                        "names or with different order of the variables.");
            }
            if (!df1.rvar(i).type().equals(df2.rvar(i).type())) {
                // column exists but does not have the same type
                throw new IllegalArgumentException("Can't bind by rows variable of different types.");
            }
        }
    }

    private static final long serialVersionUID = -445349340356580788L;
    private final int rowCount;
    private final List<Var> vars;
    private final String[] names;
    private final Map<String, Integer> indexes;

    private BoundFrame(int rowCount, List<Var> vars, String[] names, Map<String, Integer> indexes) {
        this.rowCount = rowCount;
        this.vars = vars;
        this.names = Arrays.copyOf(names, names.length);
        this.indexes = indexes;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int varCount() {
        return vars.size();
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
        if (!indexes.containsKey(name)) {
            return -1;
        }
        return indexes.get(name);
    }

    @Override
    public Var rvar(int pos) {
        return vars.get(pos);
    }

    @Override
    public Var rvar(String name) {
        if (!indexes.containsKey(name)) {
            return null;
        }
        return vars.get(indexes.get(name));
    }

    @Override
    public VType type(String varName) {
        return vars.get(indexes.get(varName)).type();
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
        List<String> parseVarNames = range.parseVarNames(this);
        String[] selectedNamed = new String[parseVarNames.size()];
        List<Var> selectedVars = new ArrayList<>();
        Map<String, Integer> selectedIndexes = new HashMap<>();
        for (int i = 0; i < parseVarNames.size(); i++) {
            selectedNamed[i] = parseVarNames.get(i);
            selectedVars.add(rvar(parseVarNames.get(i)));
            selectedIndexes.put(parseVarNames.get(i), i);
        }
        return new BoundFrame(rowCount, selectedVars, selectedNamed, selectedIndexes);
    }

    @Override
    public Frame addRows(int rowCount) {
        throw new IllegalStateException("This operation is not available for bound frames.");
    }

    @Override
    public Frame clearRows() {
        throw new IllegalStateException("This operation is not available for bound frames.");
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
    public double getDouble(int row, int varIndex) {
        return vars.get(varIndex).getDouble(row);
    }

    @Override
    public double getDouble(int row, String varName) {
        return vars.get(varIndex(varName)).getDouble(row);
    }

    @Override
    public void setDouble(int row, int col, double value) {
        vars.get(col).setDouble(row, value);
    }

    @Override
    public void setDouble(int row, String varName, double value) {
        vars.get(varIndex(varName)).setDouble(row, value);
    }

    @Override
    public int getInt(int row, int varIndex) {
        return vars.get(varIndex).getInt(row);
    }

    @Override
    public int getInt(int row, String varName) {
        return vars.get(varIndex(varName)).getInt(row);
    }

    @Override
    public void setInt(int row, int col, int value) {
        vars.get(col).setInt(row, value);
    }

    @Override
    public void setInt(int row, String varName, int value) {
        vars.get(varIndex(varName)).setInt(row, value);
    }

    @Override
    public long getLong(int row, int varIndex) {
        return vars.get(varIndex).getLong(row);
    }

    @Override
    public long getLong(int row, String varName) {
        return vars.get(varIndex(varName)).getLong(row);
    }

    @Override
    public void setLong(int row, int col, long value) {
        vars.get(col).setLong(row, value);
    }

    @Override
    public void setLong(int row, String varName, long value) {
        vars.get(varIndex(varName)).setLong(row, value);
    }

    @Override
    public String getLabel(int row, int col) {
        return vars.get(col).getLabel(row);
    }

    @Override
    public String getLabel(int row, String varName) {
        return vars.get(varIndex(varName)).getLabel(row);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        vars.get(col).setLabel(row, value);
    }

    @Override
    public void setLabel(int row, String varName, String value) {
        vars.get(varIndex(varName)).setLabel(row, value);
    }

    @Override
    public List<String> levels(String varName) {
        return vars.get(varIndex(varName)).levels();
    }

    @Override
    public boolean isMissing(int row, int col) {
        return vars.get(col).isMissing(row);
    }

    @Override
    public boolean isMissing(int row, String varName) {
        return vars.get(varIndex(varName)).isMissing(row);
    }

    @Override
    public void setMissing(int row, int col) {
        vars.get(col).setMissing(row);
    }

    @Override
    public void setMissing(int row, String varName) {
        vars.get(varIndex(varName)).setMissing(row);
    }
}
