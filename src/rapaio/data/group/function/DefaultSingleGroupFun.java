/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/20/19.
 */
public abstract class DefaultSingleGroupFun extends DefaultGroupFun {

    protected static final String SEPARATOR = "_";

    protected final int normalizeLevel;

    public DefaultSingleGroupFun(String name, int normalizeLevel, List<String> varNames) {
        super(name, varNames);
        this.normalizeLevel = normalizeLevel;
    }

    public abstract Var buildVar(Group group, String varName);

    public abstract void updateSingle(Var aggregate, int aggregateRow, Frame df, int varIndex, Mapping rows);

    @Override
    public List<Var> compute(Group group) {
        List<Var> result = new ArrayList<>();
        VarInt ids = group.getSortedGroupIds();
        for (String varName : varNames) {
            Var aggregate = buildVar(group, varName);
            int index = group.getFrame().varIndex(varName);
            for (int i = 0; i < ids.size(); i++) {
                int groupId = ids.getInt(i);
                updateSingle(aggregate, groupId, group.getFrame(), index, group.getRowsForGroupId(groupId));
            }
            if (normalizeLevel < 0) {
                result.add(aggregate);
                continue;
            }
            if (normalizeLevel == 0) {
                result.add(VarBinary.fill(aggregate.size(), 1).name(aggregate.name() + "_N0"));
                continue;
            }
            result.add(normalize(group, aggregate));
        }
        return result;
    }

    private Var normalize(Group group, Var agg) {
        int count = group.getGroupCount();

        var groupIndex = group.getGroupIdToLastLevelIndex();

        HashMap<Integer, Group.IndexNode> reducedGroup = new HashMap<>();
        HashMap<Group.IndexNode, Double> sum = new HashMap<>();
        for (int i = 0; i < count; i++) {
            Group.IndexNode node = groupIndex.get(i);
            for (int j = 0; j < normalizeLevel; j++) {
                node = node.getParent();
            }
            reducedGroup.put(i, node);
            sum.put(node, 0d);
        }

        // accumulate at higher group

        for (int i = 0; i < group.getGroupCount(); i++) {
            double value = agg.getDouble(i);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            Group.IndexNode node = reducedGroup.get(i);
            sum.put(node, sum.get(node) + value);
        }

        // normalize

        VarDouble normalized = VarDouble.empty(count).name(agg.name() + "_N" + normalizeLevel);
        for (int i = 0; i < group.getGroupCount(); i++) {
            double value = agg.getDouble(i);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            Group.IndexNode node = reducedGroup.get(i);
            double groupSum = sum.get(node);
            if (Double.isNaN(groupSum) || Double.isInfinite(groupSum) || groupSum == 0) {
                continue;
            }
            normalized.setDouble(i, value / groupSum);
        }
        return normalized;
    }
}
