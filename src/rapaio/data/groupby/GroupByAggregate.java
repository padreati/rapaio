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

package rapaio.data.groupby;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.*;
import rapaio.math.MTools;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupByAggregate implements Printable {

    private static final String SEP = "_";

    private final GroupBy groupBy;
    private final List<GroupByFunction> funs;
    private final List<String> aggNames;
    private final int normalizeLevel;

    private Frame aggregateDf;

    public GroupByAggregate(GroupBy groupBy, int normalizeLevel, List<String> varNames, List<GroupByFunction> funs) {
        this.groupBy = groupBy;
        this.funs = funs;
        this.normalizeLevel = normalizeLevel;
        this.aggNames = varNames;
        compute();
    }

    private void compute() {
        List<Var> allVarList = new ArrayList<>();
        for (String varName : aggNames) {
            List<Var> varList = new ArrayList<>();
            HashMap<String, Var> varMap = new HashMap<>();
            for (GroupByFunction fun : funs) {
                Var var = VarDouble.empty(groupBy.getGroupCount()).withName(varName + SEP + fun.name());
                varList.add(var);
                varMap.put(fun.name(), var);
            }
            funs.parallelStream().forEach(fun -> computeAggregate(fun, varName, varMap.get(fun.name())));
            allVarList.addAll(varList);
        }
        allVarList = allVarList.stream().map(this::shrinkCast).collect(Collectors.toList());
        aggregateDf = SolidFrame.byVars(allVarList);
    }

    private void computeAggregate(GroupByFunction fun, String varName, Var agg) {
        int count = groupBy.getGroupCount();
        for (int i = 0; i < count; i++) {
            agg.setDouble(i, fun.compute(groupBy.getFrame(), varName, groupBy.getRowsForGroupId(i)));
        }
        if (normalizeLevel < 0) {
            return;
        }

        Int2ObjectOpenHashMap<GroupBy.IndexNode> groupIndex = groupBy.getGroupIndex();

        Int2ObjectOpenHashMap<GroupBy.IndexNode> reducedGroup = new Int2ObjectOpenHashMap<>();
        Object2DoubleOpenHashMap<GroupBy.IndexNode> sum = new Object2DoubleOpenHashMap<>();
        for (int i = 0; i < count; i++) {
            GroupBy.IndexNode node = groupIndex.get(i);
            for (int j = 0; j < normalizeLevel; j++) {
                node = node.getParent();
            }
            reducedGroup.put(i, node);
            sum.put(node, 0);
        }

        // accumulate at higher group

        for (int i = 0; i < groupBy.getGroupCount(); i++) {
            double value = agg.getDouble(i);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            GroupBy.IndexNode node = reducedGroup.get(i);
            double oldSum = sum.getDouble(node);
            sum.put(node, oldSum + value);
        }

        // normalize

        for (int i = 0; i < groupBy.getGroupCount(); i++) {
            double value = agg.getDouble(i);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            GroupBy.IndexNode node = reducedGroup.get(i);
            double groupSum = sum.getDouble(node);
            if (Double.isNaN(groupSum) || Double.isInfinite(groupSum) || groupSum == 0) {
                continue;
            }
            agg.setDouble(i, value / groupSum);
        }
    }

    private Var shrinkCast(Var var) {
        Var cast = castShort(var);
        if (cast != null) return cast;
        cast = castInt(var);
        if (cast != null) return cast;
        cast = castLong(var);
        if (cast != null) return cast;
        cast = castFloat(var);
        if (cast != null) return cast;
        return var;
    }

    private Var castShort(Var var) {
        for (int i = 0; i < var.rowCount(); i++) {
            double value = var.getDouble(i);
            if (Double.isNaN(value)) continue;
            if (Double.isInfinite(value)) return null;
            double intVal = MTools.rint(value);
            if (!(intVal == value && intVal > Short.MIN_VALUE && intVal <= Short.MAX_VALUE)) {
                return null;
            }
        }
        Var varShort = VarShort.empty(var.rowCount()).withName(var.name());
        fillInt(var, varShort);
        return varShort;
    }

    private Var castInt(Var var) {
        for (int i = 0; i < var.rowCount(); i++) {
            double value = var.getDouble(i);
            if (Double.isNaN(value)) continue;
            if (Double.isInfinite(value)) return null;
            long intVal = (long) MTools.rint(value);
            if (!(intVal == value && intVal > Integer.MIN_VALUE && intVal <= Integer.MAX_VALUE)) {
                return null;
            }
        }
        Var varInt = VarInt.empty(var.rowCount()).withName(var.name());
        fillInt(var, varInt);
        return varInt;
    }

    private void fillInt(Var var, Var varInt) {
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                varInt.addMissing();
                continue;
            }
            int val = (int) var.getDouble(i);
            varInt.setInt(i, val);
        }
    }

    private Var castLong(Var var) {
        for (int i = 0; i < var.rowCount(); i++) {
            double value = var.getDouble(i);
            if (Double.isNaN(value)) continue;
            if (Double.isInfinite(value)) return null;
            long intVal = (long) MTools.rint(value);
            if (!(intVal == value && (intVal > Long.MIN_VALUE))) {
                return null;
            }
        }
        Var varLong = VarLong.empty(var.rowCount()).withName(var.name());
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                varLong.addMissing();
                continue;
            }
            long val = (long) var.getDouble(i);
            varLong.setLong(i, val);
        }
        return varLong;
    }

    private Var castFloat(Var var) {
        for (int i = 0; i < var.rowCount(); i++) {
            double value = var.getDouble(i);
            if (Double.isNaN(value)) continue;
            if (Double.isInfinite(value)) return null;
            float floatVal = (float) value;
            if (floatVal != value) {
                return null;
            }
        }
        Var varFloat = VarFloat.empty(var.rowCount()).withName(var.name());
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                varFloat.addMissing();
                continue;
            }
            varFloat.setDouble(i, var.getDouble(i));
        }
        return varFloat;
    }

    public Frame toFrame() {
        return toFrame(0);
    }

    public Frame toFrame(int unstackLevel) {
        Frame df = groupBy.getFrame();
        IntList rows = new IntArrayList();
        IntList sortedGroupIds = groupBy.getSortedGroupIds();
        Int2ObjectOpenHashMap<GroupBy.IndexNode> groupIndex = groupBy.getGroupIndex();
        for (int sortedGroupId : sortedGroupIds) {
            rows.add(groupIndex.get(sortedGroupId).getRows().getInt(0));
        }
        Frame result = df.mapRows(Mapping.wrap(rows)).mapVars(groupBy.getGroupVarNames()).solidCopy();
        result = result.bindVars(aggregateDf.mapRows(Mapping.wrap(sortedGroupIds))).solidCopy();
        if (unstackLevel <= 0) {
            return result;
        }

        List<String> groupVarNames = groupBy.getGroupVarNames();
        if (unstackLevel > groupVarNames.size()) {
            unstackLevel = groupVarNames.size();
        }

        // split group by columns into group and unstack
        List<String> groupNames = groupVarNames.subList(0, groupVarNames.size() - unstackLevel);
        List<String> unstackNames = groupVarNames.subList(groupVarNames.size() - unstackLevel, groupVarNames.size());

        // make unique groups and unstacked ids

        Object2IntOpenHashMap<String> groupIdMap = new Object2IntOpenHashMap<>();
        TreeSet<String> unstackIds = new TreeSet<>();
        IntList originalGroupRows = new IntArrayList();

        Int2IntOpenHashMap rowToGroupRow = new Int2IntOpenHashMap();
        Int2ObjectOpenHashMap<String> rowToUnstackId = new Int2ObjectOpenHashMap<>();

        for (int i = 0; i < result.rowCount(); i++) {
            StringBuilder sb = new StringBuilder();
            for (String unstackName : unstackNames) {
                sb.append(unstackName).append(SEP);
                sb.append(result.getLabel(i, unstackName)).append(SEP);
            }
            String unstackId = sb.toString();
            sb = new StringBuilder();
            for (String groupName : groupNames) {
                sb.append(groupName).append(SEP);
                sb.append(result.getLabel(i, groupName)).append(SEP);
            }
            String groupId = sb.toString();

            if (!groupIdMap.containsKey(groupId)) {
                groupIdMap.put(groupId, groupIdMap.size());
                originalGroupRows.add(i);
            }
            unstackIds.add(unstackId);
            rowToGroupRow.put(i, groupIdMap.getInt(groupId));
            rowToUnstackId.put(i, unstackId);
        }

        // build index for unstackIds
        Object2IntOpenHashMap<String> unstackIdPos = new Object2IntOpenHashMap<>();
        for (String unstackId : unstackIds) {
            unstackIdPos.put(unstackId, unstackIdPos.size());
        }

        // make unstack frame
        Frame unstackedDf = result.mapRows(Mapping.wrap(originalGroupRows));
        if (groupNames.size() > 0) {
            unstackedDf = unstackedDf.mapVars(groupNames);
        }

        // process each aggregate field
        for (String aggregateVarName : aggregateDf.varNames()) {
            // unstack each var for each prefix and append to unstacked frame
            // first they are empty
            List<Var> unstackedVars = new ArrayList<>();
            for (String unstackId : unstackIds) {
                Var newAgg = result.rvar(aggregateVarName).newInstance(unstackedDf.rowCount()).withName(unstackId + aggregateVarName);
                // fill with missing values
                for (int i = 0; i < newAgg.rowCount(); i++) {
                    newAgg.setMissing(i);
                }
                unstackedVars.add(newAgg);
            }
            Frame unstacked = SolidFrame.byVars(unstackedVars);

            // new we fill them with values
            VType aggregateType = result.rvar(aggregateVarName).type();
            int aggregateVarIndex = result.varIndex(aggregateVarName);
            for (int i = 0; i < result.rowCount(); i++) {
                int varIndex = unstackIdPos.getInt(rowToUnstackId.get(i));
                int rowIndex = rowToGroupRow.get(i);
                if (result.isMissing(i, aggregateVarIndex)) {
                    unstacked.setMissing(rowIndex, varIndex);
                }
                switch (aggregateType) {
                    case DOUBLE:
                    case FLOAT:
                        unstacked.setDouble(rowIndex, varIndex, result.getDouble(i, aggregateVarIndex));
                        break;
                    case INT:
                    case SHORT:
                        unstacked.setInt(rowIndex, varIndex, result.getInt(i, aggregateVarIndex));
                        break;
                    case BOOLEAN:
                        unstacked.setBoolean(rowIndex, varIndex, result.getBoolean(i, aggregateVarIndex));
                        break;
                    default:
                        throw new IllegalArgumentException("var type " + aggregateType.code() + " not unstacked.");
                }
            }

            // bind unstacked set of vars to result
            unstackedDf = unstackedDf.bindVars(unstacked);
        }

        if (groupNames.isEmpty()) {
            unstackedDf = unstackedDf.removeVars(result.varNames());
        }

        return unstackedDf.solidCopy();
    }


    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("group by: ");
        for (int i = 0; i < groupBy.getGroupVarNames().size(); i++) {
            String groupVarName = groupBy.getGroupVarNames().get(i);
            sb.append(groupVarName);
            if (i != groupBy.getGroupVarNames().size() - 1)
                sb.append(", ");
        }
        sb.append("\n");
        sb.append("group count: ").append(groupBy.getGroupCount()).append("\n");
        sb.append("group by functions: ");
        for (int i = 0; i < funs.size(); i++) {
            sb.append(funs.get(i).name());
            if (i != funs.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("group by aggregate variables: ");
        for (int i = 0; i < aggNames.size(); i++) {
            String varName = aggNames.get(i);
            sb.append(varName);
            if (i != aggNames.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("\n\n");
        return sb.toString();
    }

    @Override
    public String description() {
        return summary();
    }

    @Override
    public String content() {

        int headRows = 100;
        int tailRows = 100;

        StringBuilder sb = new StringBuilder();
        sb.append(summary());

        IntList sortedGroupIds = groupBy.getSortedGroupIds();
        IntList selectedGroupIds = new IntArrayList();
        boolean full = false;
        if (headRows + tailRows > aggregateDf.rowCount()) {
            selectedGroupIds.addAll(sortedGroupIds);
            full = true;
        } else {
            selectedGroupIds.addAll(sortedGroupIds.subList(0, headRows));
            selectedGroupIds.addAll(sortedGroupIds.subList(sortedGroupIds.size() - tailRows, sortedGroupIds.size()));
        }

        TextTable tt = TextTable.newEmpty(
                selectedGroupIds.size() + 1,
                groupBy.getGroupVarNames().size() + aggregateDf.varCount() + 1);
        tt.withHeaderRows(1);
        tt.withHeaderCols(groupBy.getGroupVarNames().size() + 1);

        // group header
        for (int i = 0; i < groupBy.getGroupVarNames().size(); i++) {
            tt.set(0, i + 1, groupBy.getGroupVarNames().get(i), 0);
        }
        // feature header
        for (int i = 0; i < aggregateDf.varCount(); i++) {
            tt.set(0, i + groupBy.getGroupVarNames().size() + 1, aggregateDf.varName(i), 0);
        }
        // row numbers
        if (full) {
            for (int i = 0; i < selectedGroupIds.size(); i++) {
                tt.set(i + 1, 0, String.format("[%d]", i), 0);
            }
        } else {
            for (int i = 0; i < headRows; i++) {
                tt.set(i + 1, 0, String.format("[%d]", i), 0);
            }
            tt.set(headRows + 1, 0, "...", 0);
            for (int i = 0; i < tailRows; i++) {
                tt.set(headRows + i + 1, 0, String.format("[%d]", aggregateDf.rowCount() - tailRows + i), 0);
            }
        }
        // populate rows
        int pos = 1;
        for (int groupId : selectedGroupIds) {

            GroupBy.IndexNode node = groupBy.getGroupIndex().get(groupId);
            List<String> groupValues = node.getGroupValues();

            // write group values
            for (int i = 0; i < groupValues.size(); i++) {
                tt.set(pos, i + 1, groupValues.get(i), 1);
            }
            for (int i = 0; i < aggregateDf.varCount(); i++) {
                tt.set(pos, i + groupValues.size() + 1,
                        aggregateDf.getLabel(groupId, i), 1);
            }
            pos++;
        }
        sb.append(tt.summary()).append("\n");
        return sb.toString();
    }
}
