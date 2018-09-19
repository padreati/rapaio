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

package rapaio.experiment.data.groupby;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.experiment.data.unique.UniqueRows;
import rapaio.datasets.Datasets;
import rapaio.printer.Printable;
import rapaio.printer.format.TextTable;
import rapaio.printer.idea.IdeaPrinter;
import rapaio.sys.WS;
import rapaio.util.Time;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/18.
 */
public class GroupBy implements Printable {

    public static GroupBy from(Frame df, String... varNames) {
        return new GroupBy(df, Arrays.asList(varNames));
    }

    public static GroupBy from(Frame df, VRange vRange) {
        List<String> varNames = vRange.parseVarNames(df);
        return new GroupBy(df, varNames);
    }

    //// Group by functions

    public static GroupByFunction count() {
        return new GroupByFunctionCount();
    }

    public static GroupByFunction min() {
        return new GroupByFunctionMin();
    }

    public static GroupByFunction max() {
        return new GroupByFunctionMax();
    }

    public static GroupByFunction sum() {
        return new GroupByFunctionSum();
    }

    public static GroupByFunction skewness() {
        return new GroupByFunctionSkewness();
    }

    public static GroupByFunction mean() {
        return new GroupByFunctionMean();
    }

    public static GroupByFunction std() {
        return new GroupByFunctionStd();
    }

    public static GroupByFunction kurtosis() {
        return new GroupByFunctionKurtosis();
    }

    public static GroupByFunction nunique() {
        return new GroupByFunctionNUnique();
    }

    private final Frame df;
    private final List<String> groupVarNames;
    private final List<String> featureVarNames;
    private final List<UniqueRows> groupByUniqueRows;
    private final Int2IntOpenHashMap rowIndex = new Int2IntOpenHashMap();
    private final Int2ObjectOpenHashMap<IndexNode> groupIndex = new Int2ObjectOpenHashMap<>();
    private final IntList sortedGroupIds = new IntArrayList();

    private IndexNode root;

    private GroupBy(Frame df, List<String> groupVarNames) {
        this.df = df;
        this.groupVarNames = groupVarNames;
        this.groupByUniqueRows = new ArrayList<>();
        for (String varName : groupVarNames) {
            Var var = df.rvar(varName);
            groupByUniqueRows.add(UniqueRows.from(var));
        }
        this.featureVarNames = new ArrayList<>();
        HashSet<String> groupSet = new HashSet<>(groupVarNames);
        for (String varName : df.varNames()) {
            if (groupSet.contains(varName)) {
                continue;
            }
            featureVarNames.add(varName);
        }
        root = new IndexNode(null, "", "", -1, -1);
        populateRows();
        sortGroupIds();
    }

    public List<String> getGroupVarNames() {
        return groupVarNames;
    }

    public List<String> getFeatureVarNames() {
        return featureVarNames;
    }

    public Int2ObjectOpenHashMap<IndexNode> getGroupIndex() {
        return groupIndex;
    }

    public Int2IntOpenHashMap getRowIndex() {
        return rowIndex;
    }

    public Frame getFrame() {
        return df;
    }

    public IntList getRowsForGroupId(int groupId) {
        return groupIndex.get(groupId).rows;
    }

    public int getGroupCount() {
        return groupIndex.size();
    }

    public IntList getSortedGroupIds() {
        return sortedGroupIds;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("group by: ");
        for (int i = 0; i < groupVarNames.size(); i++) {
            String groupVarName = groupVarNames.get(i);
            sb.append(groupVarName);
            if (i != groupVarNames.size() - 1)
                sb.append(", ");
        }
        sb.append("\n");
        sb.append("group count: ").append(groupIndex.size()).append("\n\n");


        TextTable tt = TextTable.newEmpty(df.rowCount() + 1, groupVarNames.size() + featureVarNames.size() + 2);
        tt.withHeaderRows(1);
        tt.withHeaderCols(groupVarNames.size() + 2);

        // group header
        for (int i = 0; i < groupVarNames.size(); i++) {
            tt.set(0, i + 1, groupVarNames.get(i), -1);
        }
        tt.set(0, groupVarNames.size() + 1, "row", -1);
        // feature header
        for (int i = 0; i < featureVarNames.size(); i++) {
            tt.set(0, i + groupVarNames.size() + 2, featureVarNames.get(i), -1);
        }
        // row numbers
        for (int i = 0; i < df.rowCount(); i++) {
            tt.set(i + 1, 0, String.format("[%d]", i), 0);
        }
        // populate rows
        int pos = 1;
        for (int groupId : sortedGroupIds) {

            IndexNode node = groupIndex.get(groupId);
            List<String> groupValues = node.getGroupValues();
            for (int row : node.rows) {

                // write group values
                for (int i = 0; i < groupValues.size(); i++) {
                    tt.set(pos, i + 1, groupValues.get(i), -1);
                }
                tt.set(pos, groupValues.size() + 1, String.format("%d  -> ", row), -1);
                for (int i = 0; i < featureVarNames.size(); i++) {
                    tt.set(pos, i + groupValues.size() + 2, df.getLabel(row, featureVarNames.get(i)), -1);
                }
                pos++;
            }
        }
        sb.append(tt.summary());
        return sb.toString();
    }

    public GroupByAggregate aggregate(VRange varRange, GroupByFunction... functions) {
        return aggregate(-1, varRange, functions);
    }

    public GroupByAggregate aggregate(int normalizeLevel, VRange varRange, GroupByFunction... functions) {
        List<String> varNames = varRange.parseVarNames(df);
        return new GroupByAggregate(this, normalizeLevel, varNames, Arrays.asList(functions));
    }

    private void populateRows() {
        int groupId = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            IndexNode node = root;
            for (int j = 0; j < groupVarNames.size(); j++) {
                String groupName = groupVarNames.get(j);
                String groupValue = df.getLabel(i, groupName);
                int groupUniqueId = groupByUniqueRows.get(j).getUniqueId(i);
                IndexNode child = node.getChildNode(groupUniqueId);
                if (child == null) {
                    if (j != groupVarNames.size() - 1) {
                        child = new IndexNode(node, groupName, groupValue, groupUniqueId, -1);
                    } else {
                        child = new IndexNode(node, groupName, groupValue, groupUniqueId, groupId);
                        child.addRow(i);
                        rowIndex.put(i, groupId);
                        groupIndex.put(groupId, child);
                        groupId++;
                    }
                    node.addNode(child);
                    node = child;
                } else {
                    node = child;
                    if (j == groupVarNames.size() - 1) {
                        child.addRow(i);
                        rowIndex.put(i, child.getGroupId());
                    }
                }
            }
        }
    }

    private void sortGroupIds() {
        List<int[]> groupUniqueIds = new ArrayList<>();
        for (int i = 0; i < getGroupCount(); i++) {
            groupUniqueIds.add(groupIndex.get(i).getGroupUniqueIds(new int[groupVarNames.size() + 1]));
            sortedGroupIds.add(i);
        }
        sortedGroupIds.sort((i1, i2) -> Arrays.compare(groupUniqueIds.get(i1), groupUniqueIds.get(i2)));
    }

    public static class IndexNode {
        private final String groupLevel;
        private final String groupValue;
        private final int groupUniqueId;
        private final IndexNode parent;
        private final int groupId;
        private final List<IndexNode> children = new ArrayList<>();
        private final Int2IntOpenHashMap positions = new Int2IntOpenHashMap();
        private final IntList rows = new IntArrayList();

        public IndexNode(IndexNode parent, String groupLevel, String groupValue, int groupUniqueId, int groupId) {
            this.parent = parent;
            this.groupLevel = groupLevel;
            this.groupValue = groupValue;
            this.groupUniqueId = groupUniqueId;
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
            positions.put(node.groupUniqueId, children.size());
            children.add(node);
        }

        public IndexNode getChildNode(int groupUniqueId) {
            if (positions.containsKey(groupUniqueId)) {
                return children.get(positions.get(groupUniqueId));
            }
            return null;
        }

        public void addRow(int row) {
            rows.add(row);
        }

        public List<String> getGroupValues() {
            List<String> groupValues = new ArrayList<>();
            IndexNode node = this;
            while (node.parent != null) {
                groupValues.add(0, node.groupValue);
                node = node.parent;
            }
            return groupValues;
        }

        public int[] getGroupUniqueIds(int[] buff) {
            int pos = buff.length - 1;
            IndexNode node = this;
            while (node != null) {
                buff[pos] = node.groupUniqueId;
                node = node.parent;
                pos--;
            }
            return buff;
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter());

        Frame df = Datasets.loadCarMpgDataset();
        Frame dff = df.mapRows(Mapping.wrap(SamplingTools.sampleWR(df.rowCount(), 1000 * df.rowCount())));
        dff.printSummary();

        String[] gbVars = new String[]{"origin", "cylinders"};
        String[] vars = new String[]{"weight"};

        GroupByAggregate agg = Time.showRun(() -> GroupBy.from(dff, gbVars).aggregate(VRange.of("weight"), count()));
        agg.toFrame().printLines();
        agg.toFrame(1).printLines();
        agg.toFrame(2).printLines();
    }
}
