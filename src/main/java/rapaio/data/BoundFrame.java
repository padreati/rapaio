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

import java.util.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BoundFrame extends AbstractFrame {

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

    /**
     * Builds a new bound frame by binding variables of multiple given frames.
     * All variable names must be unique among all the given frames.
     * The row count is the minimum of the row counts from all the given frames.
     *
     * @param dfs given data frames
     * @return new frame bound frame by binding variables
     */
    public static BoundFrame newByVars(Frame... dfs) {
        if (dfs.length == 0) {
            return new BoundFrame(0, new ArrayList<>(), new String[]{}, new HashMap<>());
        }
        int _rowCount = 0;
        List<Var> _vars = new ArrayList<>();
        List<String> _names = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();
        Set<String> _namesSet = new HashSet<>();

        int pos = 0;
        for (int i = 0; i < dfs.length; i++) {
            if (i == 0) {
                _rowCount = dfs[i].rowCount();
            } else {
                _rowCount = Math.min(_rowCount, dfs[i].rowCount());
            }
            for (int j = 0; j < dfs[i].varCount(); j++) {
                if (_namesSet.contains(dfs[i].var(j).name())) {
                    throw new IllegalArgumentException("bound frame does not allow variables with the same name");
                }
                _vars.add(dfs[i].var(j));
                _names.add(dfs[i].var(j).name());
                _namesSet.add(dfs[i].var(j).name());
                _indexes.put(dfs[i].var(j).name(), pos++);
            }
        }
        return new BoundFrame(_rowCount, _vars, _names.toArray(new String[_names.size()]), _indexes);
    }

    public static BoundFrame newByVars(Collection<Var> varList) {
        return newByVars(varList.stream().toArray(Var[]::new));
    }

    /**
     * Builds a new bound frame by binding given variables.
     * All variable names must be unique.
     * The row count is the minimum of the row counts from all the given variables.
     *
     * @param varList given data variables
     * @return new frame bound frame by binding variables
     */
    public static BoundFrame newByVars(Var... varList) {
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

    public static BoundFrame newByRows(Frame... dfs) {
        if (dfs.length == 0) {
            return new BoundFrame(0, new ArrayList<>(), new String[]{}, new HashMap<>());
        }
        String[] _names = dfs[0].varNames();

        // check that in each frame to exist all the variables and to have the same type
        // otherwise throw an exception

        for (int i = 1; i < dfs.length; i++) {
            String[] compNames = dfs[i].varNames();
            if (compNames.length != _names.length) {
                throw new IllegalArgumentException("can't bind by rows frames with different variable count");
            }
            for (int j = 0; j < _names.length; j++) {
                if (!_names[j].equals(compNames[j])) {
                    throw new IllegalArgumentException("can't bind by rows frames with different variable " +
                            "names or with different order of the variables");
                }
            }
            for (String _name : _names) {
                // throw an exception if the column does not exists
                if (!dfs[i].var(_name).type().equals(dfs[0].var(_name).type())) {
                    // column exists but does not have the same type
                    throw new IllegalArgumentException("can't bind by rows variable of different types");
                }
            }
        }

        List<Var> _vars = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();

        // for each var name build a bounded var from all the rows from all the frames

        for (int i = 0; i < _names.length; i++) {

            List<Integer> counts = new ArrayList<>();
            List<Var> boundVars = new ArrayList<>();

            for (Frame df : dfs) {
                counts.add(df.rowCount()); // avoid to take rowCount from variable, but from frame
                boundVars.add(df.var(_names[i]));
            }

            Var boundedVar = BoundVar.newFrom(counts, boundVars).withName(_names[i]);
            _vars.add(boundedVar);
            _indexes.put(_names[i], i);
        }

        int _rowCount = Arrays.stream(dfs).mapToInt(Frame::rowCount).sum();

        return new BoundFrame(_rowCount, _vars, _names, _indexes);
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
    public int varIndex(String name) {
        return indexes.get(name);
    }

    @Override
    public Var var(int pos) {
        return vars.get(pos);
    }

    @Override
    public Var var(String name) {
        return vars.get(indexes.get(name));
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
    public Frame mapVars(VRange range) {
        List<String> parseVarNames = range.parseVarNames(this);
        String[] _names = new String[parseVarNames.size()];
        List<Var> _vars = new ArrayList<>();
        Map<String, Integer> _indexes = new HashMap<>();
        for (int i = 0; i < parseVarNames.size(); i++) {
            _names[i] = parseVarNames.get(i);
            _vars.add(var(parseVarNames.get(i)));
            _indexes.put(parseVarNames.get(i), i);
        }
        return new BoundFrame(rowCount, _vars, _names, _indexes);
    }

    @Override
    public Frame addRows(int rowCount) {
        return BoundFrame.newByRows(this, SolidFrame.emptyFrom(this, rowCount));
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
