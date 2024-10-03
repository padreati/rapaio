/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarInt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupFunCount extends DefaultSingleGroupFun {

    public GroupFunCount(int normalizeLevel, List<String> varNames) {
        super("count", normalizeLevel, varNames);
    }

    @Override
    public Var buildVar(Group group, String varName) {
        return VarInt.empty(group.getNumberOfGroups()).name(varName + SEPARATOR + name);
    }

    @Override
    public void updateSingle(Var aggregate, int aggregateRow, Frame df, int varIndex, Mapping rows) {
        aggregate.setInt(aggregateRow, rows.size());
    }
}
