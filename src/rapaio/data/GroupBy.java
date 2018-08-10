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

package rapaio.data;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import rapaio.data.groupby.*;
import rapaio.datasets.Datasets;
import rapaio.printer.format.TextTable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/18.
 */
public class GroupBy {

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
    private final Int2IntOpenHashMap rowIndex = new Int2IntOpenHashMap();
    private final Int2ObjectOpenHashMap<IndexNode> groupIndex = new Int2ObjectOpenHashMap<>();
    private final IntList sortedGroupIds = new IntArrayList();

    private IndexNode root;

    private GroupBy(Frame df, List<String> groupVarNames) {
        this.df = df;
        this.groupVarNames = groupVarNames;
        this.featureVarNames = new ArrayList<>();

        HashSet<String> groupSet = new HashSet<>(groupVarNames);
        for (String varName : df.varNames()) {
            if (groupSet.contains(varName)) {
                continue;
            }
            featureVarNames.add(varName);
        }
        root = new IndexNode(null, "", "", -1);
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

    public String printContent() {
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

    public GroupByAggregate.GroupByAggregateBuilder aggregate() {
        return aggregate(-1);
    }

    public GroupByAggregate.GroupByAggregateBuilder aggregate(int normalizeLevel) {
        return new GroupByAggregate.GroupByAggregateBuilder(this, normalizeLevel);
    }

    private void populateRows() {
        int groupId = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            IndexNode node = root;
            for (int j = 0; j < groupVarNames.size(); j++) {
                String groupName = groupVarNames.get(j);
                String groupValue = df.getLabel(i, groupName);
                IndexNode child = node.getChildNode(groupValue);
                if (child == null) {
                    if (j != groupVarNames.size() - 1) {
                        child = new IndexNode(node, groupName, groupValue, -1);
                    } else {
                        child = new IndexNode(node, groupName, groupValue, groupId);
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
        List<List<String>> groupValues = new ArrayList<>();
        for (int i = 0; i < getGroupCount(); i++) {
            groupValues.add(groupIndex.get(i).getGroupValues());
            sortedGroupIds.add(i);
        }
        sortedGroupIds.sort((i1, i2) -> {
            List<String> groupValues1 = groupValues.get(i1);
            List<String> groupValues2 = groupValues.get(i2);

            for (int i = 0; i < groupValues1.size(); i++) {
                int comp = groupValues1.get(i).compareTo(groupValues2.get(i));
                if (comp != 0) {
                    return comp;
                }
            }
            return 0;
        });
    }

    public static class IndexNode {
        private final String groupLevel;
        private final String groupValue;
        private final IndexNode parent;
        private final int groupId;
        private final List<IndexNode> children = new ArrayList<>();
        private final Object2IntOpenHashMap<String> positions = new Object2IntOpenHashMap<>();
        private final IntList rows = new IntArrayList();

        public IndexNode(IndexNode parent, String groupLevel, String groupValue, int groupId) {
            this.parent = parent;
            this.groupLevel = groupLevel;
            this.groupValue = groupValue;
            this.groupId = groupId;
        }

        public IndexNode getParent() {
            return parent;
        }

        public String getGroupLevel() {
            return groupLevel;
        }

        public String getGroupValue() {
            return groupValue;
        }

        public boolean isLeaf() {
            return groupId >= 0;
        }

        public int getGroupId() {
            return groupId;
        }

        public IntList getRows() {
            return rows;
        }

        public void addNode(IndexNode node) {
            positions.put(node.groupValue, children.size());
            children.add(node);
        }

        public IndexNode getChildNode(String groupValue) {
            for (IndexNode child : children)
                if (child.groupValue.equals(groupValue)) return child;
            return null;
        }

        public List<IndexNode> getChildren() {
            return children;
        }

        public IndexNode findLeafNode(List<String> groupValues) {
            IndexNode leaf = this;
            for (String groupValue : groupValues)
                leaf = leaf.children.get(leaf.positions.getInt(groupValue));
            return leaf;
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
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        Frame df = Datasets.loadCarMpgDataset();
//        df.printSummary();

        String[] gbVars = new String[]{"origin", "cylinders"};
        String[] vars = new String[]{"horsepower", "weight"};

        GroupBy gb = GroupBy.from(df, gbVars);
//        WS.println(gb.printContent());


        gb.aggregate().funs(count(), min(), max(), mean(), std(), skewness(), kurtosis())
                .vars(vars)
                .run()
                .printSummary();
        gb.aggregate(0).funs(count(), min(), max(), mean(), std(), skewness(), kurtosis())
                .vars(vars)
                .run()
                .printSummary();
        gb.aggregate(1).funs(count(), min(), max(), mean(), std(), skewness(), kurtosis())
                .vars(vars)
                .run()
                .printSummary();
        gb.aggregate(2).funs(count(), min(), max(), mean(), std(), skewness(), kurtosis())
                .vars(vars)
                .run()
                .printSummary();
    }
}
