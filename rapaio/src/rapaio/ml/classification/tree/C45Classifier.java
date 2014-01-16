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

package rapaio.ml.classification.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.filters.RowFilters;
import rapaio.ml.classification.AbstractClassifier;
import rapaio.ml.classification.Classifier;
import rapaio.ml.classification.DensityTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class C45Classifier extends AbstractClassifier<C45Classifier> {

    public static final int SELECTION_INFOGAIN = 0;
    public static final int SELECTION_GAINRATIO = 1;
    ;
    private boolean useGrouping = false; // Not implemented
    private double minWeight = 2; // min weight for at least 2 output groups in order for an attribute to be selected
    private double cf = 0.25; // Not implemented
    private int windowSize = 0; // defaul is min(0.2*train.rowCount(), 2*srt(train.rowCount());
    private int windowIncrement = 0; // default is min(windowIncrement*windowSize, 0.5*currentErrors);
    private int windowTrees = 10; // how many widnow trees to grow before selecting the best 
    private int selection = SELECTION_GAINRATIO;
    private int maxNodes = Integer.MAX_VALUE;
    // perhaps some parameters about rules should be used
    ;
    String[] dict;
    private C45Node root;
    private Nominal prediction;
    private Frame distribution;

    public int getSelectionCriterion() {
        return selection;
    }

    public C45Classifier setSelectionCriterion(int selection) {
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

    public int getMaxNodes() {
        return maxNodes;
    }

    public C45Classifier setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new C45Classifier().setMinWeight(minWeight).setSelectionCriterion(selection);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String classColName) {
        dict = df.col(classColName).dictionary();
        List<String> testColNames = new ArrayList<>();
        for (String colName : df.colNames()) {
            if (colName.equals(classColName))
                continue;
            testColNames.add(classColName);
        }
        root = new C45Node(this);
        root.learn(df, weights, testColNames, classColName);
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
            List<Integer> cand = new ArrayList<>();
            double max = 0;
            for (int j = 0; j < dict.length; j++) {
                if (distribution.value(i, j) > max) {
                    max = distribution.value(i, j);
                    cand.clear();
                    cand.add(j);
                    continue;
                }
                if (distribution.value(i, j) == max) {
                    cand.add(j);
                }
            }
            prediction.setLabel(i, dict[RandomSource.nextInt(cand.size())]);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

class C45Node {

    final C45Classifier parent;
    String testColName;
    double testValue; // used by numeric children to distinguish between left and right
    double totalWeight;
    boolean leaf = false;
    double[] distribution;
    Map<String, C45Node> nominalChildren;
    C45Node numericLeftChild;
    C45Node numericRightChild;

    public C45Node(C45Classifier parent) {
        this.parent = parent;
    }

    public void learn(Frame df, List<Double> weights, List<String> testColNames, String classColName) {
        totalWeight = 0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        leaf = true;

        // if totalWeight < 2*minWeight we have a leaf
        if (totalWeight < 2 * parent.getMinWeight()) {
            distribution = new double[parent.dict.length];
            for (int i = 0; i < df.rowCount(); i++) {
                distribution[df.index(i, classColName)] += weights.get(i);
            }
            for (int i = 0; i < distribution.length; i++) {
                distribution[i] /= totalWeight;
            }
            return;
        }

        // if there is only one label
        for (int i = 1; i < df.rowCount(); i++) {
            if (df.index(0, classColName) != df.index(i, classColName)) {
                leaf = false;
                break;
            }
        }
        if (leaf) {
            distribution = new double[parent.dict.length];
            distribution[df.index(0, classColName)] += 1.;
            return;
        }

        // try to find a good split
        double max_criteria = 0;
        String selColName = null;
        double selSplitValue = Double.NaN;

        for (String testColName : testColNames) {
            // for nominal columns
            if (df.col(testColName).type().isNominal()) {
                DensityTable id = new DensityTable(df, weights, testColName, classColName);
                int count = id.countWithMinimum(false, parent.getMinWeight());
                if (count < 2) {
                    continue;
                }
                double criteria = 0;
                if (parent.SELECTION_GAINRATIO == parent.getSelectionCriterion()) {
                    criteria = id.getGainRatio(true);
                }
                if (parent.SELECTION_INFOGAIN == parent.getSelectionCriterion()) {
                    criteria = id.getInfoGain(true);
                }
                if (criteria > max_criteria) {
                    selColName = testColName;
                    max_criteria = criteria;
                }
            }

            // for numeric columns
            if (df.col(testColName).type().isNumeric()) {
                DensityTable id = new DensityTable(DensityTable.NUMERIC_DEFAULT_LABELS, parent.dict);
                Vector sort = Vectors.newSeq(df.rowCount());
                sort = RowFilters.sort(sort, RowComparators.numericComparator(df.col(testColName), true));
                // first fill the density table
                for (int i = 0; i < df.rowCount(); i++) {
                    int pos = sort.rowId(i);
                    if (df.isMissing(pos, testColName)) {
                        id.update(0, df.index(pos, classColName), weights.get(pos));
                    } else {
                        id.update(2, df.index(pos, classColName), weights.get(pos));
                    }
                }
                // process the split points
                for (int i = 0; i < df.rowCount(); i++) {
                    int pos = sort.rowId(i);
                    if (df.isMissing(pos, testColName)) continue;
                    id.move(2, 1, df.index(pos, classColName), weights.get(pos));

                    if (i < df.rowCount() - 1
                            && df.value(pos, testColName)
                            == df.value(sort.rowId(i + 1), testColName)) {
                        continue;
                    }

                    if (id.countWithMinimum(false, parent.getMinWeight()) < 2) {
                        continue;
                    }

                    double criteria = 0;
                    if (parent.SELECTION_GAINRATIO == parent.getSelectionCriterion()) {
                        criteria = id.getGainRatio(true);
                    }
                    if (parent.SELECTION_INFOGAIN == parent.getSelectionCriterion()) {
                        criteria = id.getInfoGain(true);
                    }
                    if (criteria > max_criteria) {
                        selColName = testColName;
                        selSplitValue = df.value(pos, testColName);
                        max_criteria = criteria;
                    }
                }
            }
        }

        // we have some split
        if (selColName != null) {
            if (df.col(selColName).type().isNominal()) {
                int childrenCount = df.col(selColName).dictionary().length;

                List<Integer>[] childIds = new List[childrenCount];
                List<Double>[] childWeights = new List[childrenCount];
                double[] childTotals = new double[childrenCount];
                double totalNonMissing = 0;

                for (int i = 0; i < childrenCount; i++) {
                    childIds[i] = new ArrayList<>();
                    childWeights[i] = new ArrayList<>();
                }

                // distribute non-missing
                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(i, selColName)) {
                        continue;
                    }
                    int index = df.index(i, selColName);
                    childIds[index].add(i);
                    childWeights[index].add(weights.get(i));
                    childTotals[index] += weights.get(i);
                }

                // compute non missing totals
                for (int i = 0; i < childrenCount; i++) {
                    totalNonMissing += childTotals[i];
                }

                // distribute missing
                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(i, selColName)) {
                        for (int j = 0; j < childrenCount; j++) {
                            if (childTotals[i] == 0) {
                                continue;
                            }
                            childIds[j].add(i);
                            childWeights[j].add(weights.get(i) * childTotals[j] / totalNonMissing);
                        }
                    }
                }

                // build children nodes
                nominalChildren = new HashMap<>();
                for (int i = 0; i < childrenCount; i++) {
                    String label = df.col(selColName).dictionary()[i];

                    // TODO continue implementation here
                }
            }
        }
    }

    public double[] computeDistribution(Frame df, int row) {
        if (leaf) {
            return distribution;
        }
        // if missing aggregate all child nodes
        if (df.col(testColName).isMissing(row)) {
            if (df.col(testColName).type().isNominal()) {
                double[] d = new double[parent.dict.length];
                for (Map.Entry<String, C45Node> entry : nominalChildren.entrySet()) {
                    double[] dd = entry.getValue().computeDistribution(df, row);
                    for (int i = 0; i < dd.length; i++) {
                        d[i] += dd[i] * entry.getValue().totalWeight / totalWeight;
                    }
                }
                return d;
            }
            if (df.col(testColName).type().isNumeric()) {
                double[] d = new double[parent.dict.length];
                double[] left = numericLeftChild.computeDistribution(df, row);
                double[] right = numericRightChild.computeDistribution(df, row);
                for (int i = 0; i < d.length; i++) {
                    d[i] += left[i] * numericLeftChild.totalWeight / totalWeight;
                    d[i] += right[i] * numericRightChild.totalWeight / totalWeight;
                }
                return d;
            }
            // should not be here
            return null;
        }

        // we have a value
        if (df.col(testColName).type().isNominal()) {
            String label = df.label(row, testColName);
            for (Map.Entry<String, C45Node> entry : nominalChildren.entrySet()) {
                if (entry.getKey().equals(label)) {
                    return entry.getValue().computeDistribution(df, row);
                }
            }
            // should not be here
            return null;
        }
        if (df.col(testColName).type().isNumeric()) {
            if (df.value(row, testColName) <= testValue) {
                return numericLeftChild.computeDistribution(df, row);
            } else {
                return numericRightChild.computeDistribution(df, row);
            }
        }
        // should not be here
        return null;
    }
}
