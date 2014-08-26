/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data;

import rapaio.core.VarRange;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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

    private final int rows;
    private final Var[] vars;
    private final HashMap<String, Integer> colIndex;
    private final String[] names;

    // public builders

    public static SolidFrame newWrapOf(List<Var> vars) {
        List<String> names = new ArrayList<>();
        int rows = Integer.MAX_VALUE;
        for (int i = 0; i < vars.size(); i++) {
            names.add(vars.get(i).name());
            rows = Math.min(rows, vars.get(i).rowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return newWrapOf(rows, vars, names);
    }

    public static SolidFrame newWrapOf(Var... vars) {
        String[] names = new String[vars.length];
        int rows = Integer.MAX_VALUE;
        for (int i = 0; i < vars.length; i++) {
            names[i] = vars[i].name();
            rows = Math.min(rows, vars[i].rowCount());
        }
        if (rows == Integer.MAX_VALUE) rows = 0;
        return newWrapOf(rows, vars, names);
    }

    public static SolidFrame newWrapOf(int rows, List<Var> vars, List<String> names) {
        return newWrapOf(rows, vars, names.toArray(new String[names.size()]));
    }

    public static SolidFrame newWrapOf(int rows, Var[] vars, String[] names) {
        return new SolidFrame(rows, Arrays.asList(vars), names);
    }

    public static SolidFrame newWrapOf(int rows, List<Var> vars, String[] names) {
        return new SolidFrame(rows, vars, names);
    }

    // private constructor

    private SolidFrame(int rows, List<Var> vars, String[] names) {
        for (Var var : vars) {
            if (var instanceof MappedVar)
                throw new IllegalArgumentException("Not allowed mapped vectors in solid frame");
            if (var instanceof BoundVar)
                throw new IllegalArgumentException("Not allowed mapped vectors in solid frame");
        }
        this.rows = rows;
        this.vars = new Var[vars.size()];
        this.colIndex = new HashMap<>();
        this.names = new String[vars.size()];

        for (int i = 0; i < vars.size(); i++) {
            this.vars[i] = vars.get(i);
            this.colIndex.put(names[i], i);
            this.names[i] = names[i];
        }
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
        throw new NotImplementedException();
    }

    @Override
    public Frame bindVars(Frame df) {
        throw new NotImplementedException();
    }

    @Override
    public Frame mapVars(VarRange range) {
        List<String> varNames = range.parseVarNames(this);
        List<Var> vars = varNames.stream().map(this::var).collect(Collectors.toList());
        return SolidFrame.newWrapOf(rowCount(), vars, varNames);
    }

    @Override
    public Frame bindRows(Frame df) {
        throw new NotImplementedException();
    }

    @Override
    public Frame mapRows(Mapping mapping) {
        throw new NotImplementedException();
    }
}
