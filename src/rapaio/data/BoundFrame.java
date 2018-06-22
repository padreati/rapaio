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

import java.util.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoundFrame extends AbstractFrame {

    /**
     * Builds a new bound frame by binding variables of multiple given frames.
     * All variable names must be unique among all the given frames.
     * The row count is the minimum of the row counts from all the given frames.
     *
     * @param dfs given data frames
     * @return new frame bound frame by binding variables
     */
    public static BoundFrame byVars(Frame... dfs) {
        if (dfs.length == 0) {
            return new BoundFrame(0, new ArrayList<>(), new String[]{}, new HashMap<>());
        }
        Integer _rowCount = null;
        List<Var> _vars = new ArrayList<>();
        List<String> _names = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();
        Set<String> _namesSet = new HashSet<>();

        int pos = 0;
        for (Frame df : dfs) {
            if (_rowCount == null) {
                _rowCount = df.varCount() > 0 ? df.rowCount() : null;
            } else {
                _rowCount = Math.min(_rowCount, df.rowCount());
            }
            for (int j = 0; j < df.varCount(); j++) {
                if (_namesSet.contains(df.rvar(j).name())) {
                    throw new IllegalArgumentException("bound frame does not allow variables with the same name");
                }
                _vars.add(df.rvar(j));
                _names.add(df.rvar(j).name());
                _namesSet.add(df.rvar(j).name());
                _indexes.put(df.rvar(j).name(), pos++);
            }
        }
        return new BoundFrame(_rowCount == null ? 0 : _rowCount, _vars, _names.toArray(new String[_names.size()]), _indexes);
    }

    public static BoundFrame byVars(Collection<Var> varList) {
        return byVars(varList.stream().toArray(Var[]::new));
    }

    /**
     * Builds a new bound frame by binding given variables.
     * All variable names must be unique.
     * The row count is the minimum of the row counts from all the given variables.
     *
     * @param varList given data variables
     * @return new frame bound frame by binding variables
     */
    public static BoundFrame byVars(Var... varList) {
        if (varList.length == 0) {
            return new BoundFrame(0, new ArrayList<>(), new String[]{}, new HashMap<>());
        }
        int _rowCount = 0;
        List<Var> _vars = new ArrayList<>();
        List<String> _names = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();
        Set<String> _namesSet = new HashSet<>();

        int pos = 0;
        for (int i = 0; i < varList.length; i++) {
            if (i == 0) {
                _rowCount = varList[i].rowCount();
            } else {
                _rowCount = Math.min(_rowCount, varList[i].rowCount());
            }
            if (_namesSet.contains(varList[i].name())) {
                throw new IllegalArgumentException("bound frame does not allow variables with the same name");
            }
            _vars.add(varList[i]);
            _names.add(varList[i].name());
            _namesSet.add(varList[i].name());
            _indexes.put(varList[i].name(), pos++);
        }
        return new BoundFrame(_rowCount, _vars, _names.toArray(new String[_names.size()]), _indexes);
    }

    public static BoundFrame byRows(Frame... dfs) {
        if (dfs.length == 0) {
            return new BoundFrame(0, new ArrayList<>(), new String[]{}, new HashMap<>());
        }
        String[] _names = dfs[0].varNames();

        // check that in each frame to exist all the variables and to have the same type
        // otherwise throw an exception

        for (int i = 1; i < dfs.length; i++) {
            String[] compNames = dfs[i].varNames();
            nameLengthComp(_names, compNames);
            nameValueComp(_names, compNames);
            columnExistsCheck(i, _names, dfs);
        }

        List<Var> _vars = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();

        // for each var name build a bounded var from all the rows from all the frames

        for (int i = 0; i < _names.length; i++) {

            List<Integer> counts = new ArrayList<>();
            List<Var> boundVars = new ArrayList<>();

            for (Frame df : dfs) {
                counts.add(df.rowCount()); // avoid to take rowCount from variable, but from frame
                boundVars.add(df.rvar(_names[i]));
            }

            Var boundedVar = BoundVar.from(counts, boundVars).withName(_names[i]);
            _vars.add(boundedVar);
            _indexes.put(_names[i], i);
        }

        int _rowCount = Arrays.stream(dfs).mapToInt(Frame::rowCount).sum();

        return new BoundFrame(_rowCount, _vars, _names, _indexes);
    }

    private static void columnExistsCheck(int i, String[] _names, Frame... dfs) {
        for (String _name : _names) {
            // throw an exception if the column does not exists
            if (!dfs[i].rvar(_name).type().equals(dfs[0].rvar(_name).type())) {
                // column exists but does not have the same type
                throw new IllegalArgumentException("can't bind by rows variable of different types");
            }
        }
    }

    private static void nameValueComp(String[] _names, String[] compNames) {
        for (int i = 0; i < _names.length; i++) {
            if (!_names[i].equals(compNames[i])) {
                throw new IllegalArgumentException("can't bind by rows frames with different variable " +
                        "names or with different order of the variables");
            }
        }
    }

    private static void nameLengthComp(String[] _names, String[] compNames) {
        if (compNames.length != _names.length) {
            throw new IllegalArgumentException("can't bind by rows frames with different variable count");
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
        this.names = names;
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
        return names[0];
    }

    @Override
    public int varIndex(String name) {
        return indexes.get(name);
    }

    @Override
    public Var rvar(int pos) {
        return vars.get(pos);
    }

    @Override
    public Var rvar(String name) {
        if (!indexes.containsKey(name)) {
            throw new IllegalArgumentException("Variable with name: " + name + " does not exists.");
        }
        return vars.get(indexes.get(name));
    }

    @Override
    public VarType type(String varName) {
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
        String[] _names = new String[parseVarNames.size()];
        List<Var> _vars = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();
        for (int i = 0; i < parseVarNames.size(); i++) {
            _names[i] = parseVarNames.get(i);
            _vars.add(rvar(parseVarNames.get(i)));
            _indexes.put(parseVarNames.get(i), i);
        }
        return new BoundFrame(rowCount, _vars, _names, _indexes);
    }

    @Override
    public Frame addRows(int rowCount) {
        return BoundFrame.byRows(this, SolidFrame.emptyFrom(this, rowCount));
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
        return vars.get(varIndex).value(row);
    }

    @Override
    public double value(int row, String varName) {
        return vars.get(varIndex(varName)).value(row);
    }

    @Override
    public void setValue(int row, int col, double value) {
        vars.get(col).setValue(row, value);
    }

    @Override
    public void setValue(int row, String varName, double value) {
        vars.get(varIndex(varName)).setValue(row, value);
    }

    @Override
    public int index(int row, int varIndex) {
        return vars.get(varIndex).index(row);
    }

    @Override
    public int index(int row, String varName) {
        return vars.get(varIndex(varName)).index(row);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        vars.get(col).setIndex(row, value);
    }

    @Override
    public void setIndex(int row, String varName, int value) {
        vars.get(varIndex(varName)).setIndex(row, value);
    }

    @Override
    public String label(int row, int col) {
        return vars.get(col).label(row);
    }

    @Override
    public String label(int row, String varName) {
        return vars.get(varIndex(varName)).label(row);
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
    public List<String> completeLevels(String varName) {
        return vars.get(varIndex(varName)).completeLevels();
    }

    @Override
    public boolean binary(int row, int col) {
        return vars.get(col).binary(row);
    }

    @Override
    public boolean binary(int row, String varName) {
        return vars.get(varIndex(varName)).binary(row);
    }

    @Override
    public void setBinary(int row, int col, boolean value) {
        vars.get(col).setBinary(row, value);
    }

    @Override
    public void setBinary(int row, String varName, boolean value) {
        vars.get(varIndex(varName)).setBinary(row, value);
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
