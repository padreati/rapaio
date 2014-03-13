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

package rapaio.ml.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.Vector;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;
import rapaio.ml.tools.DensityTable;
import rapaio.workspace.Workspace;

import java.util.*;

import static rapaio.data.filters.BaseFilters.sort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class C45Classifier extends AbstractClassifier<C45Classifier> {

    /**
     * Features which need to be implemented:
     * - use grouping
     */
    public static final int SELECTION_INFOGAIN = 0;
    public static final int SELECTION_GAINRATIO = 1;
    double minWeight = 2;
    int selection = SELECTION_GAINRATIO;
    String[] dict;
    C45ClassifierNode root;
    Nominal prediction;
    Frame distribution;

    public int getSelection() {
        return selection;
    }

    public C45Classifier setSelection(int selection) {
        this.selection = selection;
        return this;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public C45Classifier setMinWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new C45Classifier().setMinWeight(minWeight).setSelection(selection);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        dict = df.getCol(targetColName).getDictionary();
        Set<String> testColNames = new HashSet<>();
        for (String colName : df.getColNames()) {
            if (colName.equals(targetColName))
                continue;
            testColNames.add(colName);
        }
        root = new C45ClassifierNode(this);
        root.learn(df, weights, testColNames, targetColName);
    }

    @Override
    public void predict(Frame df) {
        prediction = new Nominal(df.rowCount(), dict);
        distribution = Frames.newMatrixFrame(df.rowCount(), dict);

        for (int i = 0; i < df.rowCount(); i++) {
            double[] d = root.computeDistribution(df, i);
            for (int j = 0; j < dict.length; j++) {
                distribution.setValue(i, j, d[j]);
            }
            List<Integer> candidates = new ArrayList<>();
            double max = 0;
            for (int j = 1; j < dict.length; j++) {
                if (d[j] > max) {
                    max = d[j];
                    candidates.clear();
                    candidates.add(j);
                    continue;
                }
                if (d[j] == max) {
                    candidates.add(j);
                }
            }
            prediction.setLabel(i, dict[candidates.get(RandomSource.nextInt(candidates.size()))]);
        }
    }

    @Override
    public Nominal getPrediction() {
        return prediction;
    }

    @Override
    public Frame getDistribution() {
        return distribution;
    }

    @Override
    public void summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nC45(selection=");
        switch (selection) {
            case SELECTION_GAINRATIO:
                sb.append("gainratio");
                break;
            case SELECTION_INFOGAIN:
                sb.append("infogain");
                break;
        }
        sb.append(String.format(", minWeight=%.3f", minWeight));
        sb.append(")\n");
        summary(root, sb, 0);
        Workspace.code(sb.toString());
    }

    private void summary(C45ClassifierNode root, StringBuilder sb, int level) {
        if (root.leaf) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append("predict:{");
            for (int i = 1; i < dict.length; i++) {
                sb.append(String.format(" %s=%.2f", dict[i], root.counts[i]));
            }
            sb.append("}\n");
            return;
        }
        for (String label : root.children.keySet()) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append(root.testName);
            if (root.testValue != root.testValue) {
                sb.append(":").append(label);
            } else {
                if ("left".equals(label)) {
                    sb.append(String.format("<=%.4f", root.testValue));
                } else {
                    sb.append(String.format(">%.4f", root.testValue));
                }
            }
            sb.append("\n");
            summary(root.children.get(label), sb, level + 1);
        }
    }
}

class C45ClassifierNode {

    final C45Classifier parent;
    String testName;
    double testValue;
    double testCriteria;
    double totalWeight;
    boolean leaf = false;
    double[] counts;
    double[] distribution;
    Map<String, C45ClassifierNode> children;

    public C45ClassifierNode(C45Classifier parent) {
        this.parent = parent;
    }

    public void learn(Frame df, List<Double> weights, Set<String> testNames, String targetName) {
        totalWeight = 0;
        counts = new double[parent.dict.length];
        distribution = new double[parent.dict.length];
        for (int i = 0; i < df.rowCount(); i++) {
            counts[df.getIndex(i, targetName)] += weights.get(i);
            totalWeight += weights.get(i);
        }
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = counts[i] / totalWeight;
        }

        leaf = true;

        // if totalWeight < 2*minWeight we have a leaf
        if (totalWeight < 2 * parent.getMinWeight()) {
            return;
        }

        // if there is only one getLabel
        for (int i = 1; i < df.rowCount(); i++) {
            if (df.getIndex(0, targetName) != df.getIndex(i, targetName)) {
                leaf = false;
                break;
            }
        }
        if (leaf) {
            return;
        }

        // try to find a good split
        testCriteria = 0;
        for (String testName : testNames) {

            // for nominal columns

            if (df.getCol(testName).type().isNominal()) {
                DensityTable id = new DensityTable(df.getCol(testName), df.getCol(targetName), weights);
                int count = id.countWithMinimum(false, parent.getMinWeight());
                if (count < 2) {
                    continue;
                }
                double value = 0;
                switch (parent.selection) {
                    case C45Classifier.SELECTION_GAINRATIO:
                        value = id.getGainRatio(true);
                        break;
                    case C45Classifier.SELECTION_INFOGAIN:
                        value = id.getInfoGain();
                        break;
                }
                if (compareCriteria(value, testCriteria, parent.selection) > 0) {
                    this.testName = testName;
                    testCriteria = value;
                    testValue = Double.NaN;
                }
                continue;
            }

            // for numeric columns

            if (df.getCol(testName).type().isNumeric()) {
                DensityTable id = new DensityTable(DensityTable.NUMERIC_DEFAULT_LABELS, parent.dict);
                Vector sort = Vectors.newSeq(df.rowCount());
                sort = sort(sort, RowComparators.numericComparator(df.getCol(testName), true));

                // first fill the density table
                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(sort.getIndex(i), testName)) {
                        id.update(0, df.getIndex(sort.getIndex(i), targetName), weights.get(sort.getIndex(i)));
                    } else {
                        id.update(2, df.getIndex(sort.getIndex(i), targetName), weights.get(sort.getIndex(i)));
                    }
                }

                // process the split points
                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(sort.getIndex(i), testName)) continue;
                    id.move(2, 1, df.getIndex(sort.getIndex(i), targetName), weights.get(sort.getIndex(i)));

                    if (i < df.rowCount() - 1
                            && df.getValue(sort.getIndex(i), testName) == df.getValue(sort.getIndex(i + 1), testName)) {
                        continue;
                    }

                    if (id.countWithMinimum(false, parent.getMinWeight()) < 2) {
                        continue;
                    }

                    double value = 0;
                    switch (parent.selection) {
                        case C45Classifier.SELECTION_GAINRATIO:
                            value = id.getGainRatio(true);
                            break;
                        case C45Classifier.SELECTION_INFOGAIN:
                            value = id.getInfoGain(true);
                            break;
                    }
                    if (compareCriteria(value, testCriteria, parent.selection) > 0) {
                        this.testName = testName;
                        testValue = df.getValue(sort.getIndex(i), testName);
                        testCriteria = value;
                    }
                }
            }
        }

        // we have some split

        if (testName != null) {

            // for nominal columns

            if (df.getCol(testName).type().isNominal()) {
                int childrenCount = df.getCol(testName).getDictionary().length - 1;

                Mapping[] childMappings = new Mapping[childrenCount];
                List<Double>[] childWeights = new List[childrenCount];
                double[] childTotals = new double[childrenCount];
                double totalNonMissing = 0;

                for (int i = 0; i < childrenCount; i++) {
                    childMappings[i] = new Mapping();
                    childWeights[i] = new ArrayList<>();
                }

                // distribute non-missing
                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(i, testName)) {
                        continue;
                    }
                    int index = df.getIndex(i, testName) - 1;
                    childMappings[index].add(df.rowId(i));
                    childWeights[index].add(weights.get(i));
                    childTotals[index] += weights.get(i);
                }

                // compute non missing totals
                for (int i = 0; i < childrenCount; i++) {
                    totalNonMissing += childTotals[i];
                }

                // distribute missing
                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(i, testName)) {
                        for (int j = 0; j < childrenCount; j++) {
                            if (childTotals[i] == 0) {
                                continue;
                            }
                            childMappings[j].add(df.rowId(i));
                            childWeights[j].add(weights.get(i) * childTotals[j] / totalNonMissing);
                        }
                    }
                }

                // build children nodes
                children = new HashMap<>();
                HashSet<String> newTestColNames = new HashSet<>(testNames);
                newTestColNames.remove(testName);
                for (int i = 0; i < childrenCount; i++) {
                    String label = df.getCol(testName).getDictionary()[i + 1];
                    C45ClassifierNode node = new C45ClassifierNode(parent);
                    children.put(label, node);
                    node.learn(new MappedFrame(df.getSourceFrame(), childMappings[i]), childWeights[i], testNames, targetName);
                }
                return;
            }

            // for numeric columns

            int childrenCount = 2;

            Mapping[] childMappings = new Mapping[childrenCount];
            List<Double>[] childWeights = new List[childrenCount];
            double[] childTotals = new double[childrenCount];
            double totalNonMissing = 0;

            for (int i = 0; i < childrenCount; i++) {
                childMappings[i] = new Mapping();
                childWeights[i] = new ArrayList<>();
            }

            // distribute non-missing
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testName)) {
                    continue;
                }
                int index = df.getValue(i, testName) <= testValue ? 0 : 1;
                childMappings[index].add(df.rowId(i));
                childWeights[index].add(weights.get(i));
                childTotals[index] += weights.get(i);
            }

            // compute non missing totals
            for (int i = 0; i < childrenCount; i++) {
                totalNonMissing += childTotals[i];
            }

            // distribute missing
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testName)) {
                    for (int j = 0; j < childrenCount; j++) {
                        if (childTotals[i] == 0) {
                            continue;
                        }
                        childMappings[j].add(df.rowId(i));
                        childWeights[j].add(weights.get(i) * childTotals[j] / totalNonMissing);
                    }
                }
            }

            // build children nodes

            children = new HashMap<>();
            HashSet<String> newTestColNames = new HashSet<>(testNames);
            newTestColNames.remove(testName);

            C45ClassifierNode left = new C45ClassifierNode(parent);
            children.put("left", left);
            C45ClassifierNode right = new C45ClassifierNode(parent);
            children.put("right", right);
            left.learn(new MappedFrame(df.getSourceFrame(), childMappings[0]), childWeights[0], newTestColNames, targetName);
            right.learn(new MappedFrame(df.getSourceFrame(), childMappings[1]), childWeights[1], newTestColNames, targetName);

        } else {

            // we could not find a split, so we degenerate to default
            leaf = true;
        }
    }

    private int compareCriteria(double value, double criteria, int selection) {
        switch (selection) {
            case C45Classifier.SELECTION_GAINRATIO:
                return (value >= criteria) ? 1 : -1;
            case C45Classifier.SELECTION_INFOGAIN:
                return (value >= criteria) ? 1 : -1;
        }
        return 0;
    }

    public double[] computeDistribution(Frame df, int row) {
        if (leaf) {
            return distribution;
        }

        // if missing aggregate all child nodes

        if (df.getCol(testName).isMissing(row)) {
            double[] d = new double[parent.dict.length];
            for (Map.Entry<String, C45ClassifierNode> entry : children.entrySet()) {
                double[] dd = entry.getValue().computeDistribution(df, row);
                for (int i = 0; i < dd.length; i++) {
                    d[i] += dd[i] * entry.getValue().totalWeight / totalWeight;
                }
            }
            return d;
        }

        // we have a getValue
        if (df.getCol(testName).type().isNominal()) {
            String label = df.getLabel(row, testName);
            for (Map.Entry<String, C45ClassifierNode> entry : children.entrySet()) {
                if (entry.getKey().equals(label)) {
                    return entry.getValue().computeDistribution(df, row);
                }
            }
            throw new RuntimeException("label value not found in classification tree");
        }
        if (df.getValue(row, testName) <= testValue) {
            return children.get("left").computeDistribution(df, row);
        } else {
            return children.get("right").computeDistribution(df, row);
        }
    }
}
