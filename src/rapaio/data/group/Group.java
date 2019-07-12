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

package rapaio.data.group;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.*;
import rapaio.data.group.function.*;
import rapaio.data.unique.*;
import rapaio.printer.*;
import rapaio.printer.format.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * GroupBy index structure which indexes rows from a data frame using unique
 * values from one or more key variables. This data structure index is used as
 * basis for aggregation operations realized with GroupByAggregate.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/18.
 */
public class Group implements Printable {

    ///// Builders //////

    public static Group from(Frame df, String... varNames) {
        return new Group(df, Arrays.asList(varNames));
    }

    public static Group from(Frame df, VRange vRange) {
        List<String> varNames = vRange.parseVarNames(df);
        return new Group(df, varNames);
    }

    ///// Group by functions /////

    public static GroupFun count(String... varNames) {
        return count(-1, varNames);
    }

    public static GroupFun count(int normalizeLevel, String... varNames) {
        return new GroupFunCount(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun min(String... varNames) {
        return min(-1, varNames);
    }

    public static GroupFun min(int normalizeLevel, String... varNames) {
        return new GroupFunMin(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun max(String... varNames) {
        return max(-1, varNames);
    }

    public static GroupFun max(int normalizeLevel, String... varNames) {
        return new GroupFunMax(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun sum(String... varNames) {
        return sum(-1, varNames);
    }

    public static GroupFun sum(int normalizeLevel, String... varNames) {
        return new GroupFunSum(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun skewness(String... varNames) {
        return skewness(-1, varNames);
    }

    public static GroupFun skewness(int normalizeLevel, String... varNames) {
        return new GroupFunSkewness(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun mean(String... varNames) {
        return mean(-1, varNames);
    }

    public static GroupFun mean(int normalizeLevel, String... varNames) {
        return new GroupFunMean(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun std(String... varNames) {
        return std(-1, varNames);
    }

    public static GroupFun std(int normalizeLevel, String... varNames) {
        return new GroupFunStd(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun kurtosis(String... varNames) {
        return kurtosis(-1, varNames);
    }

    public static GroupFun kurtosis(int normalizeLevel, String... varNames) {
        return new GroupFunKurtosis(normalizeLevel, Arrays.asList(varNames));
    }

    public static GroupFun nunique(String... varNames) {
        return nunique(-1, varNames);
    }

    public static GroupFun nunique(int normalizeLevel, String... varNames) {
        return new GroupFunNUnique(normalizeLevel, Arrays.asList(varNames));
    }

    ///// AGGREGATE /////

    public Aggregate aggregate(GroupFun... functions) {
        return new Aggregate(this, Arrays.asList(functions));
    }


    /**
     * IMPLEMENTATION
     */

    // frame on which grouping is realized
    private final Frame df;

    // list of primary key variable names
    private final List<String> pkNamesList;

    // set of primary key variable names
    private final Set<String> pkVarNamesSet;

    // other than pk var names from source frame
    private final List<String> featureNamesList;

    // collection of unique structures for each primary key variable
    private final List<Unique> groupByUniques;

    // tree which handles the pk values hierarchy
    private IndexNode root;

    // maps rows to group ids
    private final Int2IntOpenHashMap rowToGroupId = new Int2IntOpenHashMap();

    // map group ids to indexes from the last level of the tree
    private final Int2ObjectOpenHashMap<IndexNode> groupIdToLastLevelIndex = new Int2ObjectOpenHashMap<>();

    // sorted group ids
    private final IntList sortedGroupIds = new IntArrayList();

    private Group(Frame df, List<String> groupVarNames) {
        this.df = df;
        this.pkNamesList = groupVarNames;
        this.pkVarNamesSet = new HashSet<>(pkNamesList);
        this.groupByUniques = this.pkNamesList.stream().map(varName -> Unique.of(df.rvar(varName), true)).collect(Collectors.toList());
        this.featureNamesList = new ArrayList<>();
        for (String varName : df.varNames()) {
            if (pkVarNamesSet.contains(varName)) {
                continue;
            }
            featureNamesList.add(varName);
        }
        root = new IndexNode(null, "", "", -1, -1);

        //populate rows
        int groupId = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            IndexNode node = root;
            for (int j = 0; j < pkNamesList.size(); j++) {
                String levelName = pkNamesList.get(j);
                String levelValue = df.getLabel(i, levelName);
                int levelUniqueId = groupByUniques.get(j).idByRow(i);
                IndexNode child = node.getChildNode(levelUniqueId);
                if (child == null) {
                    if (j != pkNamesList.size() - 1) {
                        child = new IndexNode(node, levelName, levelValue, levelUniqueId, -1);
                    } else {
                        child = new IndexNode(node, levelName, levelValue, levelUniqueId, groupId);
                        child.addRow(i);
                        rowToGroupId.put(i, groupId);
                        groupIdToLastLevelIndex.put(groupId, child);
                        groupId++;
                    }
                    node.addNode(child);
                    node = child;
                } else {
                    node = child;
                    if (j == pkNamesList.size() - 1) {
                        child.addRow(i);
                        rowToGroupId.put(i, child.getGroupId());
                    }
                }
            }
        }

        // sort group ids
        List<int[]> groupUniqueIds = new ArrayList<>();
        for (int i = 0; i < getGroupCount(); i++) {
            groupUniqueIds.add(groupIdToLastLevelIndex.get(i).getLevelIds(new int[pkNamesList.size() + 1]));
            sortedGroupIds.add(i);
        }
        sortedGroupIds.sort((i1, i2) -> {
            int[] gui1 = groupUniqueIds.get(i1);
            int[] gui2 = groupUniqueIds.get(i2);
            for (int i = 0; i < gui1.length; i++) {
                int comp = Integer.compare(gui1[i], gui2[i]);
                if (comp != 0) {
                    return comp;
                }
            }
            return 0;
        });
    }

    /**
     * @return list of variable names which acts as primary keys in group by
     */
    public List<String> getGroupByNameList() {
        return pkNamesList;
    }

    /**
     * @return list of variable names on which the aggregation is realized
     */
    public List<String> getFeatureNameList() {
        return featureNamesList;
    }

    public Int2ObjectOpenHashMap<IndexNode> getGroupIdToLastLevelIndex() {
        return groupIdToLastLevelIndex;
    }

    /**
     * @return source frame on which group by is realized
     */
    public Frame getFrame() {
        return df;
    }

    /**
     * @param groupId group identifier
     * @return list of rows from that group
     */
    public IntList getRowsForGroupId(int groupId) {
        return groupIdToLastLevelIndex.get(groupId).rows;
    }

    /**
     * @return count of groups
     */
    public int getGroupCount() {
        return groupIdToLastLevelIndex.size();
    }

    /**
     * @return list of sorted group ids
     */
    public IntList getSortedGroupIds() {
        return sortedGroupIds;
    }

    /**
     * Node of the prefix tree for groups
     */
    public static class IndexNode {

        // group unique id
        private final int groupId;

        // level unique id
        private final int levelId;

        // level variable name
        private final String levelName;

        // level string value
        private final String levelValue;

        // parent node
        private final IndexNode parent;

        // list of children nodes
        private final List<IndexNode> children = new ArrayList<>();

        // index from level id to children position index
        private final Int2IntOpenHashMap positions = new Int2IntOpenHashMap();

        // rows assigned to this group
        private final IntList rows = new IntArrayList();

        public IndexNode(IndexNode parent, String levelName,
                         String levelValue, int levelId, int groupId) {
            this.parent = parent;
            this.levelName = levelName;
            this.levelValue = levelValue;
            this.levelId = levelId;
            this.groupId = groupId;
        }

        public IndexNode getParent() {
            return parent;
        }

        public int getGroupId() {
            return groupId;
        }

        public IntList getRows() {
            return rows;
        }

        public void addNode(IndexNode node) {
            positions.put(node.levelId, children.size());
            children.add(node);
        }

        public IndexNode getChildNode(int levelId) {
            if (positions.containsKey(levelId)) {
                return children.get(positions.get(levelId));
            }
            return null;
        }

        public void addRow(int row) {
            rows.add(row);
        }

        public List<String> getLevelValues() {
            List<String> groupValues = new ArrayList<>();
            IndexNode node = this;
            while (node.parent != null) {
                groupValues.add(0, node.levelValue);
                node = node.parent;
            }
            return groupValues;
        }

        public int[] getLevelIds(int[] buff) {
            int pos = buff.length - 1;
            IndexNode node = this;
            while (node != null) {
                buff[pos] = node.levelId;
                node = node.parent;
                pos--;
            }
            return buff;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GroupBy{");
        sb.append("keys:[").append(String.join(",", pkNamesList)).append("], ");
        sb.append("group count:").append(groupIdToLastLevelIndex.size()).append(", ");
        sb.append("row count:").append(df.rowCount());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String content() {
        if (df.rowCount() < 40) {
            return fullContent();
        }
        StringBuilder sb = new StringBuilder();

        sb.append("group by: ").append(String.join(", ", pkNamesList)).append("\n");
        sb.append("group count: ").append(groupIdToLastLevelIndex.size()).append("\n\n");

        TextTable tt = TextTable.empty(40 + 1, pkNamesList.size() + featureNamesList.size() + 2, 1, pkNamesList.size() + 2);

        // group header
        for (int i = 0; i < pkNamesList.size(); i++) {
            tt.textLeft(0, i + 1, pkNamesList.get(i));
        }
        tt.textLeft(0, pkNamesList.size() + 1, "row");
        // feature header
        for (int i = 0; i < featureNamesList.size(); i++) {
            tt.textLeft(0, i + pkNamesList.size() + 2, featureNamesList.get(i));
        }
        // row numbers
        for (int i = 0; i < 30; i++) {
            tt.intRow(i + 1, 0, i);
        }
        tt.textCenter(31, 0, "...");
        for (int i = 31; i < 40; i++) {
            tt.intRow(i + 1, 0, df.rowCount() - 40 + i);
        }
        // populate rows
        IntList rows = new IntArrayList();
        for (int groupId : sortedGroupIds) {
            rows.addAll(groupIdToLastLevelIndex.get(groupId).rows);
        }

        for (int i = 0; i < 30; i++) {
            int r = rows.getInt(i);
            fillRowData(tt, i, r);
        }
        IndexNode _node = groupIdToLastLevelIndex.get(0);
        List<String> _groupValues = _node.getLevelValues();
        for (int j = 0; j < _groupValues.size(); j++) {
            tt.textLeft(31, j + 1, "...");
        }
        for (int j = 0; j < featureNamesList.size(); j++) {
            tt.textLeft(31, j + _groupValues.size() + 2, "...");
        }
        for (int i = 31; i < 40; i++) {
            int r = rows.getInt(df.rowCount() - 40 + i);
            fillRowData(tt, i, r);
        }
        sb.append(tt.getDefaultText());
        return sb.toString();
    }

    private void fillRowData(TextTable tt, int i, int r) {
        int groupId = rowToGroupId.get(r);
        IndexNode node = groupIdToLastLevelIndex.get(groupId);
        List<String> groupValues = node.getLevelValues();
        for (int j = 0; j < groupValues.size(); j++) {
            tt.textLeft(i + 1, j + 1, groupValues.get(j));
        }
        tt.textLeft(i + 1, groupValues.size() + 1, String.format("%d  -> ", r));
        for (int j = 0; j < featureNamesList.size(); j++) {
            tt.textType(i + 1, j + groupValues.size() + 2, df, r, featureNamesList.get(j));
        }
    }

    @Override
    public String fullContent() {
        StringBuilder sb = new StringBuilder();

        sb.append("group by: ").append(String.join(", ", pkNamesList)).append("\n");
        sb.append("group count: ").append(groupIdToLastLevelIndex.size()).append("\n\n");

        TextTable tt = TextTable.empty(df.rowCount() + 1, pkNamesList.size() + featureNamesList.size() + 2, 1, pkNamesList.size() + 2);

        // group header
        for (int i = 0; i < pkNamesList.size(); i++) {
            tt.textLeft(0, i + 1, pkNamesList.get(i));
        }
        tt.textLeft(0, pkNamesList.size() + 1, "row");
        // feature header
        for (int i = 0; i < featureNamesList.size(); i++) {
            tt.textLeft(0, i + pkNamesList.size() + 2, featureNamesList.get(i));
        }
        // row numbers
        for (int i = 0; i < df.rowCount(); i++) {
            tt.textRight(i + 1, 0, String.format("[%d]", i));
        }
        // populate rows
        int pos = 1;
        for (int groupId : sortedGroupIds) {

            IndexNode node = groupIdToLastLevelIndex.get(groupId);
            List<String> groupValues = node.getLevelValues();
            for (int row : node.rows) {

                // write group values
                for (int i = 0; i < groupValues.size(); i++) {
                    tt.textLeft(pos, i + 1, groupValues.get(i));
                }
                tt.textLeft(pos, groupValues.size() + 1, String.format("%d  -> ", row));
                for (int i = 0; i < featureNamesList.size(); i++) {
                    tt.textType(pos, i + groupValues.size() + 2, df, row, featureNamesList.get(i));
                }
                pos++;
            }
        }
        sb.append(tt.getDefaultText());
        return sb.toString();
    }

    @Override
    public String summary() {
        return content();
    }

    /**
     * GroubBy aggregate data structure is the result of applying aggregation functions
     * on features of a GroupBy data structure.
     * <p>
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
     */
    public static class Aggregate implements Printable {

        private static final String SEP = "_";

        // Group[By data structure
        private final Group group;

        // list of group functions to be applied
        private final List<GroupFun> funs;

        // resulted data frame which contains aggregations
        private Frame aggregateDf;

        public Aggregate(Group group, List<GroupFun> funs) {
            this.group = group;
            this.funs = funs;

            List<Var> allVarList = new ArrayList<>();
            for (GroupFun fun : funs) {
                allVarList.addAll(fun.compute(group));
            }
            aggregateDf = SolidFrame.byVars(allVarList);
        }

        public Frame toFrame() {
            return toFrame(0);
        }

        public Frame toFrame(int unstackLevel) {
            Frame df = group.getFrame();
            IntList rows = new IntArrayList();
            IntList sortedGroupIds = group.getSortedGroupIds();
            Int2ObjectOpenHashMap<IndexNode> groupIndex = group.getGroupIdToLastLevelIndex();
            for (int sortedGroupId : sortedGroupIds) {
                rows.add(groupIndex.get(sortedGroupId).getRows().getInt(0));
            }
            Frame result = df.mapRows(Mapping.wrap(rows)).mapVars(group.getGroupByNameList()).copy();
            result = result.bindVars(aggregateDf.mapRows(Mapping.wrap(sortedGroupIds))).copy();
            if (unstackLevel <= 0) {
                return result;
            }

            List<String> groupVarNames = group.getGroupByNameList();
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
                            unstacked.setDouble(rowIndex, varIndex, result.getDouble(i, aggregateVarIndex));
                            break;
                        case BINARY:
                        case INT:
                            unstacked.setInt(rowIndex, varIndex, result.getInt(i, aggregateVarIndex));
                            break;
                        default:
                            throw new IllegalArgumentException("var type " + aggregateType.code() + " not unstacked.");
                    }
                }

                // bind unstacked set of vars to result
                unstackedDf = unstackedDf.bindVars(unstacked);
            }

            if (groupNames.isEmpty()) {
                unstackedDf = unstackedDf.removeVars(VRange.of(result.varNames()));
            }

            return unstackedDf.copy();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Group.Aggregate{");
            sb.append("group=").append(group.toString()).append(", ");
            sb.append("funs=[").append(funs.stream().map(GroupFun::toString).collect(Collectors.joining(","))).append("]");
            sb.append("}");
            return sb.toString();
        }

        @Override
        public String summary() {
            StringBuilder sb = new StringBuilder();

            sb.append("group by: ");
            for (int i = 0; i < group.getGroupByNameList().size(); i++) {
                String groupVarName = group.getGroupByNameList().get(i);
                sb.append(groupVarName);
                if (i != group.getGroupByNameList().size() - 1)
                    sb.append(", ");
            }
            sb.append("\n");
            sb.append("group count: ").append(group.getGroupCount()).append("\n");
            sb.append("group by functions: ");
            for (int i = 0; i < funs.size(); i++) {
                sb.append(funs.get(i).toString());
                if (i != funs.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("\n\n");
            return sb.toString();
        }

        private String selectedContent(int headRows, int tailRows) {
            StringBuilder sb = new StringBuilder();
            sb.append(summary());

            IntList sortedGroupIds = group.getSortedGroupIds();
            IntList selectedGroupIds = new IntArrayList();
            boolean full = false;
            if (headRows + tailRows > aggregateDf.rowCount()) {
                selectedGroupIds.addAll(sortedGroupIds);
                full = true;
            } else {
                selectedGroupIds.addAll(sortedGroupIds.subList(0, headRows));
                selectedGroupIds.addAll(sortedGroupIds.subList(sortedGroupIds.size() - tailRows, sortedGroupIds.size()));
            }

            TextTable tt = TextTable.empty(selectedGroupIds.size() + 1,
                    group.getGroupByNameList().size() + aggregateDf.varCount() + 1, 1, group.getGroupByNameList().size() + 1);

            // group header
            for (int i = 0; i < group.getGroupByNameList().size(); i++) {
                tt.textCenter(0, i + 1, group.getGroupByNameList().get(i));
            }
            // feature header
            for (int i = 0; i < aggregateDf.varCount(); i++) {
                tt.textCenter(0, i + group.getGroupByNameList().size() + 1, aggregateDf.varName(i));
            }
            // row numbers
            if (full) {
                for (int i = 0; i < selectedGroupIds.size(); i++) {
                    tt.textRight(i + 1, 0, String.format("[%d]", i));
                }
            } else {
                for (int i = 0; i < headRows; i++) {
                    tt.textRight(i + 1, 0, String.format("[%d]", i));
                }
                if (tailRows != 0) {
                    tt.textCenter(headRows + 1, 0, "...");
                    for (int i = 0; i < tailRows; i++) {
                        tt.textCenter(headRows + i + 1, 0, String.format("[%d]", aggregateDf.rowCount() - tailRows + i));
                    }
                }
            }
            // populate rows
            int pos = 1;
            for (int groupId : selectedGroupIds) {

                IndexNode node = group.getGroupIdToLastLevelIndex().get(groupId);
                List<String> groupValues = node.getLevelValues();

                // write group values
                for (int i = 0; i < groupValues.size(); i++) {
                    tt.textRight(pos, i + 1, groupValues.get(i));
                }
                for (int i = 0; i < aggregateDf.varCount(); i++) {
                    tt.textRight(pos, i + groupValues.size() + 1, aggregateDf.getLabel(groupId, i));
                }
                pos++;
            }
            sb.append(tt.getDefaultText()).append("\n");
            return sb.toString();
        }

        @Override
        public String content() {
            return selectedContent(30, 10);
        }

        @Override
        public String fullContent() {
            return selectedContent(group.getGroupCount(), 0);
        }
    }
}
