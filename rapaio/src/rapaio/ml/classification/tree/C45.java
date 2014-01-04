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
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.NominalVector;
import rapaio.ml.classification.AbstractClassifier;
import rapaio.ml.classification.Classifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class C45 extends AbstractClassifier<C45> {

    public static final int SELECTION_INFOGAIN = 0;
    public static final int SELECTION_GAINRATIO = 2;
    ;
    private boolean useGrouping = false; // Not implemented
    private double minWeight = 2; // min weight for at least 2 output groups in order for an attribute to be selected
    private double cf = 0.25; // Not implemented
    private int windowSize = 0; // defaul is min(0.2*train.getRowCount(), 2*srt(train.getRowCount());
    private int windowIncrement = 0; // default is min(windowIncrement*windowSize, 0.5*currentErrors);
    private int windowTrees = 10; // how many widnow trees to grow before selecting the best 
    private int selection = SELECTION_GAINRATIO;
    private int maxNodes = Integer.MAX_VALUE;
    // perhaps some parameters about rules should be used
    ;
    String[] dict;
    private C45Node root;
    private NominalVector prediction;
    private Frame distribution;

    public int getSelectionCriterion() {
        return selection;
    }

    public C45 setSelectionCriterion(int selection) {
        this.selection = selection;
        return this;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public C45 setMinWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public C45 setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new C45().setMinWeight(minWeight).setSelectionCriterion(selection);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String classColName) {
        dict = df.getCol(classColName).getDictionary();
        List<String> testColNames = new ArrayList<>();
        for (String colName : df.getColNames()) {
            if (colName.equals(classColName))
                continue;
            testColNames.add(classColName);
        }
        root = new C45Node(this);
        root.build(df, weights, testColNames, classColName);
    }

    @Override
    public void predict(Frame df) {
        prediction = new NominalVector(df.getRowCount(), dict);
        distribution = Frames.newMatrixFrame(df.getRowCount(), dict);

        for (int i = 0; i < df.getRowCount(); i++) {
            double[] d = root.computeDistribution(df, i);
            for (int j = 0; j < dict.length; j++) {
                distribution.setValue(i, j, d[j]);
            }
            List<Integer> cand = new ArrayList<>();
            double max = 0;
            for (int j = 0; j < dict.length; j++) {
                if (distribution.getValue(i, j) > max) {
                    max = distribution.getValue(i, j);
                    cand.clear();
                    cand.add(j);
                    continue;
                }
                if (distribution.getValue(i, j) == max) {
                    cand.add(j);
                }
            }
            prediction.setLabel(i, dict[RandomSource.nextInt(cand.size())]);
        }
    }

    @Override
    public NominalVector getPrediction() {
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

    final C45 parent;
    String predictedLabel;
    String testColName;
    double testValue; // used by numeric children to distinguish between left and right
    double totalWeight;
    boolean leaf = false;
    double[] distribution;
    Map<String, C45Node> nominalChildren;
    C45Node numericLeftChild;
    C45Node numericRightChild;

    public C45Node(C45 parent) {
        this.parent = parent;
    }

    public void build(Frame df, List<Double> weights, List<String> testColNames, String classColName) {

    }

    public double[] computeDistribution(Frame df, int row) {
        if (leaf) {
            return distribution;
        }
        if (df.getCol(testColName).isMissing(row)) {
            // agregate all subnodes
            if (df.getCol(testColName).isNominal()) {
                double total = 0;
                for (C45Node c45Node : nominalChildren.values()) {
                    total += c45Node.totalWeight;
                }
                double[] d = new double[parent.dict.length];
                for (Map.Entry<String, C45Node> entry : nominalChildren.entrySet()) {
                    double[] dd = entry.getValue().computeDistribution(df, row);
                    for (int i = 0; i < dd.length; i++) {
                        d[i] += dd[i] * entry.getValue().totalWeight / total;
                    }
                }
                return d;
            }
            if (df.getCol(testColName).isNumeric()) {
                double[] d = new double[parent.dict.length];
                double[] dd = numericLeftChild.computeDistribution(df, row);
                for (int i = 0; i < dd.length; i++) {
                    d[i] += dd[i] * numericLeftChild.totalWeight;
                }
                dd = numericRightChild.computeDistribution(df, row);
                for (int i = 0; i < dd.length; i++) {
                    d[i] += dd[i] * numericRightChild.totalWeight;
                }
                for (int i = 0; i < dd.length; i++) {
                    d[i] /= numericLeftChild.totalWeight + numericRightChild.totalWeight;
                }
                return d;
            }
            // should not be here
            return null;
        }
        if (df.getCol(testColName).isNominal()) {
            return nominalChildren.get(df.getLabel(row, testColName)).computeDistribution(df, row);
        }
        if (df.getCol(testColName).isNumeric()) {
            if (df.getValue(row, testColName) <= testValue) {
                return numericLeftChild.computeDistribution(df, row);
            } else {
                return numericRightChild.computeDistribution(df, row);
            }
        }
        // should not be here
        return null;
    }
}
