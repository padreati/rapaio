/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.ml.supervised.tree;

import rapaio.core.RandomSource;
import rapaio.core.stat.Mode;
import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.Vector;
import rapaio.filters.ColFilters;
import rapaio.filters.NominalFilters;
import rapaio.filters.RowFilters;
import rapaio.ml.supervised.AbstractClassifier;
import rapaio.ml.supervised.ClassifierResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3 extends AbstractClassifier {
    private ID3Node root;
    private MetricType metricType = new EntropyMetricType();
    private String[] dict;

    /**
     * Metric type used as criterion for splitting nodes.
     */
    public static interface MetricType {

        String getMetricTypeName();

        public double compute(Frame df, int classIndex, int splitIndex);

        int compare(double metricValue1, double metricValue2);
    }

    /**
     * Entropy metric type
     */
    public static final class EntropyMetricType implements MetricType {

        @Override
        public String getMetricTypeName() {
            return "Entropy";
        }

        @Override
        public double compute(Frame df, int classIndex, int splitIndex) {
            return new TreeMetrics().entropy(df, classIndex);
        }

        @Override
        public int compare(double metricValue1, double metricValue2) {
            if (metricValue1 == metricValue2) {
                return 0;
            }
            return (metricValue1 < metricValue2) ? 1 : -1;
        }
    }

    /**
     * InfoGain metric type
     */
    class InfoGainMetricType implements MetricType {

        @Override
        public String getMetricTypeName() {
            return "InfoGain";
        }


        @Override
        public double compute(Frame df, int classIndex, int splitIndex) {
            return new TreeMetrics().infoGain(df, classIndex, splitIndex);
        }

        @Override
        public int compare(double metricValue1, double metricValue2) {
            if (metricValue1 == metricValue2) {
                return 0;
            }
            return (metricValue1 < metricValue2) ? -1 : 1;
        }
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }

    @Override
    public void learn(Frame df, int classIndex) {
        validate(df, classIndex);
        this.dict = df.getCol(classIndex).getDictionary();
        this.root = new ID3Node(null, df, classIndex, new HashSet<Integer>(), metricType);
    }

    @Override
    public void printModelSummary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassifierResult predict(final Frame df) {
        final Vector classification = new NominalVector("classification", df.getRowCount(), dict);
        for (int i = 0; i < df.getRowCount(); i++) {
            String prediction = predict(df, i, root);
            classification.setLabel(i, prediction);
        }
        return new ClassifierResult() {
            @Override
            public Frame getTestFrame() {
                return df;
            }

            @Override
            public Vector getClassification() {
                return classification;
            }

            @Override
            public Frame getProbabilities() {
                return null;
            }
        };
    }

    private String predict(Frame df, int row, ID3Node root) {
        if (root.isLeaf()) {
            return root.getPredicted();
        }
        String label = df.getLabel(row, df.getColIndex(root.getSplitCol()));
        Map<String, ID3Node> map = root.getSplitMap();
        if (!map.containsKey(label)) {
            throw new RuntimeException("Inconsistency");
        }
        return predict(df, row, map.get(label));
    }

    private void validate(Frame df, int classIndex) {
        for (int i = 0; i < df.getColCount(); i++) {
            if (!df.getCol(i).isNominal()) {
                throw new IllegalArgumentException("ID3 can handle only isNominal attributes.");
            }
//            for (int j = 0; j < df.getRowCount(); j++) {
//                if (df.getCol(i).isMissing(j)) {
//                    throw new IllegalArgumentException("ID3 can't handle missing values");
//                }
//            }
        }
        if (df.getColCount() <= classIndex) {
            throw new IllegalArgumentException("Class getIndex is not valid");
        }
    }

}

class ID3Node {
    private final ID3Node parent;
    private final Frame df;
    private final int classIndex;
    private final ID3.MetricType metricType;
    //
    private boolean leaf = false;
    private String predicted;
    private String splitCol;
    private HashMap<String, ID3Node> splitMap;

    public ID3Node(final ID3Node parent,
                   final Frame df,
                   final int classIndex,
                   final HashSet<Integer> used,
                   final ID3.MetricType metricType) {
        this.parent = parent;
        this.df = df;
        this.classIndex = classIndex;
        this.metricType = metricType;

        learn(used);
    }

    public boolean isLeaf() {
        return leaf;
    }

    public String getPredicted() {
        return predicted;
    }

    public String getSplitCol() {
        return splitCol;
    }

    public Map<String, ID3Node> getSplitMap() {
        return splitMap;
    }

    private void learn(HashSet<Integer> used) {
        // leaf on empty set
        if (df == null || df.getRowCount() == 0) {
            if (parent == null) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            String[] modes = new Mode(parent.df.getCol(classIndex)).getModes();
            if (modes.length == 0) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            predicted = modes[0];
            if (modes.length > 1) {
                predicted = modes[RandomSource.nextInt(modes.length)];
            }
            leaf = true;
            return;
        }

        // leaf on all classes of same value
        boolean same = true;
        for (int i = 1; i < df.getRowCount(); i++) {
            if (df.getIndex(i - 1, classIndex) != df.getIndex(i, classIndex)) {
                same = false;
                break;
            }
        }
        if (same) {
            predicted = df.getLabel(0, classIndex);
            leaf = true;
            return;
        }

        // find best split

        int col = -1;
        double best = Double.NaN;

        for (int i = 0; i < df.getColCount(); i++) {
            if (i == classIndex || used.contains(i)) {
                continue;
            }
            if (col == -1) {
                best = metricType.compute(df, classIndex, i);
                col = i;
                continue;
            }
            double metricValue = metricType.compute(df, classIndex, i);
            if (metricType.compare(best, metricValue) > 0) {
                best = metricValue;
                col = i;
            }
        }


        // if none were selected then there are no columns to select

        if (col == -1) {
            if (parent == null) {
                throw new IllegalArgumentException("You must have at least one nominal attribute other than class");
            }
            String[] modes = new Mode(parent.df.getCol(classIndex)).getModes();
            if (modes.length == 0) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            predicted = modes[0];
            if (modes.length > 1) {
                predicted = modes[RandomSource.nextInt(modes.length)];
            }
            leaf = true;
            return;
        }

        // usual case, a split node

        String[] dict = df.getCol(col).getDictionary();
        Frame[] frames = NominalFilters.groupByNominal(df, col);

        splitCol = df.getColNames()[col];
        splitMap = new HashMap<>();
        HashSet<Integer> newUsed = new HashSet<>(used);
        newUsed.add(col);
        for (int i = 0; i < dict.length; i++) {
            splitMap.put(dict[i], new ID3Node(this, frames[i], classIndex, newUsed, metricType));
        }
    }
}

