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

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import rapaio.data.Frame;
import rapaio.data.GroupBy;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarFloat;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarShort;
import rapaio.math.MTools;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupByAggregate implements Printable {

    public static class GroupByAggregateBuilder {
        private final GroupBy groupBy;
        private final int normalizeLevel;
        private List<GroupByFunction> funs = new ArrayList<>();
        private List<String> varNames = new ArrayList<>();

        public GroupByAggregateBuilder(GroupBy groupBy, int normalizeLevel) {
            this.groupBy = groupBy;
            this.normalizeLevel = normalizeLevel;
        }

        public GroupByAggregateBuilder funs(GroupByFunction... funs) {
            this.funs = Arrays.asList(funs);
            return this;
        }

        public GroupByAggregateBuilder vars(String... varNames) {
            this.varNames = Arrays.asList(varNames);
            return this;
        }

        public GroupByAggregate run() {
            return new GroupByAggregate(groupBy, normalizeLevel, funs, varNames);
        }
    }

    private static final String SEP = "_";

    private final GroupBy groupBy;
    private final List<GroupByFunction> funs;
    private final List<String> aggNames;
    private final int normalizeLevel;

    private Frame aggregateDf;

    public GroupByAggregate(GroupBy groupBy, int normalizeLevel, List<GroupByFunction> funs, List<String> varNames) {
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
        aggregateDf = SolidFrame.byVars(shrinkVars(allVarList));
    }

    private void computeAggregate(GroupByFunction fun, String varName, Var agg) {
        IntStream.range(0, groupBy.getGroupCount())
                .parallel()
                .forEach(groupId -> {
                    double value = fun.compute(groupBy.getFrame(), varName, groupBy.getRowsForGroupId(groupId));
                    agg.setDouble(groupId, value);
                });

        if (normalizeLevel < 0) {
            return;
        }

        int count = groupBy.getGroupCount();
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
            if(Double.isNaN(value) || Double.isInfinite(value)) {
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

    private List<Var> shrinkVars(List<Var> varList) {
        List<Var> shrinkedList = new ArrayList<>();
        for (Var var : varList) {
            shrinkedList.add(shrinkCast(var));
        }
        return shrinkedList;
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
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                varShort.addMissing();
                continue;
            }
            int val = (int) var.getDouble(i);
            varShort.setInt(i, val);
        }
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
        for (int i = 0; i < var.rowCount(); i++) {
            if (var.isMissing(i)) {
                varInt.addMissing();
                continue;
            }
            int val = (int) var.getDouble(i);
            varInt.setInt(i, val);
        }
        return varInt;
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

        TextTable tt = TextTable.newEmpty(
                aggregateDf.rowCount() + 1,
                groupBy.getGroupVarNames().size() + aggregateDf.varCount() + 1);
        tt.withHeaderRows(1);
        tt.withHeaderCols(groupBy.getGroupVarNames().size() + 1);

        // group header
        for (int i = 0; i < groupBy.getGroupVarNames().size(); i++) {
            tt.set(0, i + 1, groupBy.getGroupVarNames().get(i), -1);
        }
        // feature header
        for (int i = 0; i < aggregateDf.varCount(); i++) {
            tt.set(0, i + groupBy.getGroupVarNames().size() + 1, aggregateDf.varName(i), -1);
        }
        // row numbers
        for (int i = 0; i < aggregateDf.rowCount(); i++) {
            tt.set(i + 1, 0, String.format("[%d]", i), 0);
        }
        // populate rows
        int pos = 1;
        for (int groupId : groupBy.getSortedGroupIds()) {

            GroupBy.IndexNode node = groupBy.getGroupIndex().get(groupId);
            List<String> groupValues = node.getGroupValues();

            // write group values
            for (int i = 0; i < groupValues.size(); i++) {
                tt.set(pos, i + 1, groupValues.get(i), -1);
            }
            for (int i = 0; i < aggregateDf.varCount(); i++) {
                tt.set(pos, i + groupValues.size() + 1,
                        aggregateDf.getLabel(groupId, i), -1);
            }
            pos++;
        }
        sb.append(tt.summary()).append("\n");
        return sb.toString();
    }
}
