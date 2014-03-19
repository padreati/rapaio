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
import rapaio.core.stat.Mode;
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.tools.DensityTable;
import rapaio.workspace.Workspace;

import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3Classifier extends AbstractClassifier<ID3Classifier> {

    public static final int SELECTION_ENTROPY = 0;
    public static final int SELECTION_INFOGAIN = 1;
    ID3ClassifierNode root;
    String[] dict;
    Nominal prediction;
    int selection = SELECTION_ENTROPY;

    public int getSelection() {
        return selection;
    }

    public ID3Classifier setSelection(int selection) {
        this.selection = selection;
        return this;
    }

    public ID3Classifier newInstance() {
        return new ID3Classifier().setSelection(selection);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        validate(df, targetColName);
        this.dict = df.col(targetColName).getDictionary();
        this.root = new ID3ClassifierNode();
        this.root.learn(null, df, weights, targetColName, new HashSet<String>(), selection);
    }

    @Override
    public void summary() {

        StringBuilder sb = new StringBuilder();
        sb.append("\nID3(selection=");
        switch (selection) {
            case SELECTION_ENTROPY:
                sb.append("entropy");
                break;
            case SELECTION_INFOGAIN:
                sb.append("infogain");
                break;
        }
        sb.append(")\n");
        summary(root, sb, 0);
        Workspace.code(sb.toString());
    }

    private void summary(ID3ClassifierNode root, StringBuilder sb, int level) {
        if (root.leaf) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append("predict:").append(root.predicted);
            sb.append("\n");
            return;
        }
        for (String label : root.splitMap.keySet()) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append(root.splitCol).append(":").append(label);
            sb.append(" (");
            switch (selection) {
                case SELECTION_ENTROPY:
                    sb.append("entropy=");
                    break;
                case SELECTION_INFOGAIN:
                    sb.append("infogain=");
                    break;
            }
            sb.append(String.format("%.6f", root.metricValue)).append(")");
            sb.append("\n");
            summary(root.splitMap.get(label), sb, level + 1);
        }
    }

    @Override
    public void predict(final Frame df) {
        prediction = new Nominal(df.rowCount(), dict);
        for (int i = 0; i < df.rowCount(); i++) {
            String label = predict(df, i, root);
            prediction.setLabel(i, label);
        }
    }

    @Override
    public Nominal prediction() {
        return prediction;
    }

    @Override
    public Frame distribution() {
        return null;
    }

    private String predict(Frame df, int row, ID3ClassifierNode root) {
        if (root.leaf) {
            return root.predicted;
        }
        String label = df.getLabel(row, df.colIndex(root.splitCol));
        Map<String, ID3ClassifierNode> map = root.splitMap;
        if (!map.containsKey(label)) {
            throw new RuntimeException("Inconsistency");
        }
        return predict(df, row, map.get(label));
    }

    private void validate(Frame df, String classColName) {
        for (int i = 0; i < df.colCount(); i++) {
            if (!df.col(i).type().isNominal()) {
                throw new IllegalArgumentException("ID3 can handle only isNominal attributes.");
            }
        }
        if (df.toStream().complete().count() == 0)
            throw new IllegalArgumentException("ID3 can't handle missing values.");
    }
}

class ID3ClassifierNode {

    boolean leaf = false;
    String predicted;
    String splitCol;
    HashMap<String, ID3ClassifierNode> splitMap;
    double metricValue;
    Frame df;

    void learn(final ID3ClassifierNode parent,
               final Frame df,
               final List<Double> weights,
               final String classColName,
               final HashSet<String> used,
               final int selection) {
        this.df = df;

        // leaf on empty set
        if (df == null || df.rowCount() == 0) {
            if (parent == null) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            String[] modes = new Mode(parent.df.col(classColName), false).getModes();
            if (modes.length == 0) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            predicted = modes[RandomSource.nextInt(modes.length)];
            leaf = true;
            return;
        }

        // leaf on all classes of same getValue
        boolean same = true;
        for (int i = 1; i < df.rowCount(); i++) {
            if (df.getIndex(i - 1, df.colIndex(classColName)) != df.getIndex(i, df.colIndex(classColName))) {
                same = false;
                break;
            }
        }
        if (same) {
            predicted = df.getLabel(0, df.colIndex(classColName));
            leaf = true;
            return;
        }

        // find best split
        String colName = null;
        metricValue = Double.NaN;

        for (String testColName : df.colNames()) {
            if (testColName.equals(classColName) || used.contains(testColName)) {
                continue;
            }
            if (colName == null) {
                metricValue = computeMetric(df, weights, classColName, testColName, selection);
                colName = testColName;
                continue;
            }
            double value = computeMetric(df, weights, classColName, testColName, selection);
            if (compareMetric(value, metricValue, selection) > 0) {
                metricValue = value;
                colName = testColName;
            }
        }

        // if none were selected then there are no columns to select
        if (colName == null) {
            String[] modes = new Mode(df.col(classColName), false).getModes();
            if (modes.length == 0) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            predicted = modes[RandomSource.nextInt(modes.length)];
            leaf = true;
            return;
        }

        // usual case, a split node
        String[] dict = df.col(colName).getDictionary();
        Mapping[] splitMappings = new Mapping[dict.length];
        List<Double>[] splitWeights = new List[dict.length];

        for (int i = 0; i < dict.length; i++) {
            splitMappings[i] = new Mapping();
            splitWeights[i] = new ArrayList<>();
        }

        for (int i = 0; i < df.rowCount(); i++) {
            int index = df.getIndex(i, df.colIndex(colName));
            splitMappings[index].add(df.rowId(i));
            splitWeights[index].add(weights.get(i));
        }
        Frame[] frames = new Frame[dict.length];
        for (int i = 1; i < dict.length; i++) {
            frames[i] = new MappedFrame(df.sourceFrame(), splitMappings[i]);
        }

        splitCol = colName;
        splitMap = new HashMap<>();
        HashSet<String> newUsed = new HashSet<>(used);
        newUsed.add(colName);
        for (int i = 1; i < dict.length; i++) {
            ID3ClassifierNode node = new ID3ClassifierNode();
            node.learn(this, frames[i], splitWeights[i], classColName, newUsed, selection);
            splitMap.put(dict[i], node);
        }

        // clear for GC
        this.df = null;
    }

    private int compareMetric(double lastMetricValue, double metricValue, final int selection) {
        switch (selection) {
            case ID3Classifier.SELECTION_ENTROPY:
                return (lastMetricValue <= metricValue) ? 1 : -1;
            case ID3Classifier.SELECTION_INFOGAIN:
                return (lastMetricValue >= metricValue) ? 1 : -1;
        }
        return 0;
    }

    private double computeMetric(Frame df, List<Double> weights, String targetName, String testName, int selection) {
        DensityTable dt;
        switch (selection) {
            case ID3Classifier.SELECTION_ENTROPY:
                dt = new DensityTable(df.col(testName), df.col(targetName), weights);
                return dt.getSplitEntropy();
            case ID3Classifier.SELECTION_INFOGAIN:
                dt = new DensityTable(df.col(testName), df.col(targetName), weights);
                return dt.getInfoGain();
        }
        return 0;
    }
}
