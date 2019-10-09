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

package rapaio.data.group.function;

import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.data.group.Group;
import rapaio.data.unique.Unique;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupFunNUnique extends DefaultSingleGroupFun {

    public GroupFunNUnique(int normalizeLevel, List<String> varNames) {
        super("nunique", normalizeLevel, varNames);
    }

    @Override
    public Var buildVar(Group group, String varName) {
        return VarInt.empty(group.getGroupCount()).withName(varName + SEPARATOR + name);
    }

    @Override
    public void updateSingle(Var aggregate, int aggregateRow, Frame df, int varIndex, IntList rows) {
        int offset = 0;
        for (int row : rows) {
            if (df.isMissing(row, varIndex)) {
                offset = 1;
                break;
            }
        }
        Unique unique = Unique.of(df.rvar(varIndex).mapRows(Mapping.wrap(rows)), false);
        aggregate.setInt(aggregateRow, unique.uniqueCount() - offset);
    }
}

