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
                _rowCount = df.getVarCount() > 0 ? df.getRowCount() : null;
            } else {
                _rowCount = Math.min(_rowCount, df.getRowCount());
            }
            for (int j = 0; j < df.getVarCount(); j++) {
                if (_namesSet.contains(df.getVar(j).getName())) {
                    throw new IllegalArgumentException("bound frame does not allow variables with the same name");
                }
                _vars.add(df.getVar(j));
                _names.add(df.getVar(j).getName());
                _namesSet.add(df.getVar(j).getName());
                _indexes.put(df.getVar(j).getName(), pos++);
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
                _rowCount = varList[i].getRowCount();
            } else {
                _rowCount = Math.min(_rowCount, varList[i].getRowCount());
            }
            if (_namesSet.contains(varList[i].getName())) {
                throw new IllegalArgumentException("bound frame does not allow variables with the same name");
            }
            _vars.add(varList[i]);
            _names.add(varList[i].getName());
            _namesSet.add(varList[i].getName());
            _indexes.put(varList[i].getName(), pos++);
        }
        return new BoundFrame(_rowCount, _vars, _names.toArray(new String[_names.size()]), _indexes);
    }

    public static BoundFrame byRows(Frame... dfs) {
        if (dfs.length == 0) {
            return new BoundFrame(0, new ArrayList<>(), new String[]{}, new HashMap<>());
        }
        String[] _names = dfs[0].getVarNames();

        // check that in each frame to exist all the variables and to have the same type
        // otherwise throw an exception

        for (int i = 1; i < dfs.length; i++) {
            String[] compNames = dfs[i].getVarNames();
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
                counts.add(df.getRowCount()); // avoid to take rowCount from variable, but from frame
                boundVars.add(df.getVar(_names[i]));
            }

            Var boundedVar = BoundVar.from(counts, boundVars).withName(_names[i]);
            _vars.add(boundedVar);
            _indexes.put(_names[i], i);
        }

        int _rowCount = Arrays.stream(dfs).mapToInt(Frame::getRowCount).sum();

        return new BoundFrame(_rowCount, _vars, _names, _indexes);
    }

	private static void columnExistsCheck(int i, String[] _names, Frame... dfs) {
		for (String _name : _names) {
		    // throw an exception if the column does not exists
		    if (!dfs[i].getVar(_name).getType().equals(dfs[0].getVar(_name).getType())) {
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
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getVarCount() {
        return vars.size();
    }

    @Override
    public String[] getVarNames() {
        return names;
    }

    @Override
    public int getVarIndex(String name) {
        return indexes.get(name);
    }

    @Override
    public Var getVar(int pos) {
        return vars.get(pos);
    }

    @Override
    public Var getVar(String name) {
        return vars.get(indexes.get(name));
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
            _vars.add(getVar(parseVarNames.get(i)));
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
}
