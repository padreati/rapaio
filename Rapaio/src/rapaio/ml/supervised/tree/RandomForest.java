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

import static rapaio.core.BaseMath.validNumber;
import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.filters.RowFilters;
import rapaio.ml.supervised.Classifier;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.sample.Sample;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForest implements Classifier {
    final int mtrees;
    final int mcols;
    final boolean computeOob;
    private final List<Tree> trees = new ArrayList<>();
    String classColName;
    String[] dict;
    boolean debug = false;
    double oobError = 0;
    int[][] oobFreq;

    public RandomForest(int mtrees, int mcols) {
        this(mtrees, mcols, true);
    }

    public RandomForest(int mtrees, int mcols, boolean computeOob) {
        this.mtrees = mtrees;
        this.mcols = mcols;
        this.computeOob = computeOob;
    }

    @Override
    public void learn(final Frame df, final String classColName) {
        if (mcols > df.getColCount() - 1) {
            throw new IllegalArgumentException("mcols have a value greater than frame dimensions.");
        }
        this.classColName = classColName;
        this.dict = df.getCol(classColName).getDictionary();
        trees.clear();

        oobError = computeOob ? 0 : Double.NaN;

        if (computeOob) {
            setupOobContainer(df);
        }

        for (int i = 0; i < mtrees; i++) {
            Tree tree = new Tree(this);
            trees.add(tree);
            Frame bootstrap = Sample.bootstrap(df);
            tree.learn(bootstrap);
            if (computeOob) {
                addOob(df, bootstrap, tree);
            }
        }
        if (computeOob) {
            oobError = computeOob(df);
        }
        if (debug) {
            System.out.println(String.format("avg oob error: %.4f", oobError));
        }
    }

    private void setupOobContainer(Frame df) {
        oobFreq = new int[df.getSourceFrame().getRowCount()][dict.length];
    }

    private void addOob(Frame source, Frame bootstrap, Tree tree) {
        Frame delta = RowFilters.delta(source, bootstrap);
        ClassifierModel model = tree.predict(delta);
        Vector predict = model.getClassification();
        for (int i = 0; i < delta.getRowCount(); i++) {
            int rowId = delta.getRowId(i);
            oobFreq[rowId][predict.getIndex(i)]++;
        }
    }

    private double computeOob(Frame df) {
        double total = 0;
        double count = 0;
        int[] indexes = new int[dict.length];
        for (int i = 0; i < df.getRowCount(); i++) {
            int len = 1;
            indexes[0] = 0;
            for (int j = 1; j < dict.length; j++) {
                if (oobFreq[i][j] > oobFreq[i][indexes[len - 1]]) {
                    indexes[0] = j;
                    len = 1;
                    continue;
                }
                if (oobFreq[i][j] == oobFreq[i][indexes[len - 1]]) {
                    indexes[len] = j;
                    len++;
                }
            }
            int next = indexes[RandomSource.nextInt(len)];
            if (oobFreq[i][next] > 0) {
                count += 1.;
                if (next != df.getCol(classColName).getIndex(i)) {
                    total += 1.;
                }
            }
        }
        return total / count;
    }

    @Override
    public ClassifierModel predict(final Frame df) {
        final Vector predict = new NominalVector(classColName, df.getRowCount(), dict);
        Vector[] vectors = new Vector[dict.length];
        for (int i = 0; i < dict.length; i++) {
            vectors[i] = new NumericVector(dict[i], new double[df.getRowCount()]);
        }
        final Frame prob = new SolidFrame("prob", df.getRowCount(), vectors);

        for (int i = 0; i < mtrees; i++) {
            Tree tree = trees.get(i);
            ClassifierModel model = tree.predict(df);
            for (int j = 0; j < df.getRowCount(); j++) {
                String prediction = model.getClassification().getLabel(j);
                double freq = prob.getValue(j, prob.getColIndex(prediction));
                prob.setValue(j, prob.getColIndex(prediction), freq + 1);
            }
        }

        // from freq to prob

        for (int i = 0; i < prob.getRowCount(); i++) {
            double max = -1;
            int col = -1;
            for (int j = 0; j < prob.getColCount(); j++) {
                double freq = prob.getValue(i, j);
                prob.setValue(i, j, freq / (1. * mtrees));
                if (max < freq) {
                    max = freq;
                    col = j;
                }
            }
            predict.setLabel(i, dict[col]);
        }

        return new ClassifierModel() {
            @Override
            public Frame getTestFrame() {
                return df;
            }

            @Override
            public Vector getClassification() {
                return predict;
            }

            @Override
            public Frame getProbabilities() {
                return prob;
            }
        };
    }

    @Override
    public void summary() {
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}

class Tree {
    private final RandomForest rf;
    private TreeNode root;
    private String[] dict;

    Tree(RandomForest rf) {
        this.rf = rf;
    }

    public void learn(Frame df) {
        int[] indexes = new int[df.getColCount() - 1];
        int pos = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            if (i != df.getColIndex(rf.classColName)) {
                indexes[pos++] = i;
            }
        }

        this.dict = df.getCol(rf.classColName).getDictionary();
        this.root = new TreeNode();

        LinkedList<TreeNode> nodes = new LinkedList<>();
        LinkedList<Frame> frames = new LinkedList<>();
        nodes.addLast(root);
        frames.addLast(df);
        while (!nodes.isEmpty()) {
            TreeNode currentNode = nodes.pollFirst();
            Frame currentFrame = frames.pollFirst();
            currentNode.learn(currentFrame, indexes, rf);
            if (currentNode.leaf) {
                continue;
            }
            nodes.addLast(currentNode.leftNode);
            nodes.addLast(currentNode.rightNode);
            frames.addLast(currentNode.leftFrame);
            frames.addLast(currentNode.rightFrame);
            currentNode.leftFrame = null;
            currentNode.rightFrame = null;
        }
    }

    public ClassifierModel predict(final Frame df) {
        final Vector classification = new NominalVector("classification", df.getRowCount(), dict);
        for (int i = 0; i < df.getRowCount(); i++) {
            int[] distribution = predict(df, i, root);
            int[] indexes = new int[distribution.length];
            int len = 1;
            for (int j = 1; j < distribution.length; j++) {
                if (distribution[j] == distribution[indexes[0]]) {
                    indexes[len++] = j;
                    continue;
                }
                if (distribution[j] > distribution[indexes[0]]) {
                    len = 1;
                    indexes[0] = j;
                    continue;
                }
            }
            int next = 0;
            if (len > 0) {
                next = RandomSource.nextInt(len);
            }
            String prediction = dict[indexes[next]];
            classification.setLabel(i, prediction);
        }
        return new ClassifierModel() {
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

    private int[] predict(Frame df, int row, TreeNode root) {
        if (root.leaf) {
            return root.distribution;
        }
        int col = df.getColIndex(root.splitCol);
        if (df.getCol(col).isMissing(row)) {
            int[] left = predict(df, row, root.leftNode);
            int[] right = predict(df, row, root.rightNode);
            for (int i = 0; i < left.length; i++) {
                left[i] += right[i];
                return left;
            }
        }
        if (df.getCol(col).isNumeric()) {
            double value = df.getValue(row, col);
            return predict(df, row, value < root.splitValue ? root.leftNode : root.rightNode);
        } else {
            String label = df.getLabel(row, col);
            return predict(df, row, root.splitLabel.equals(label) ? root.leftNode : root.rightNode);
        }
    }
}

class TreeNode {
    public boolean leaf = false;
    public String splitCol;
    public String splitLabel;
    public double splitValue;
    public double metricValue = Double.NaN;
    public int[] distribution;

    public String predicted;

    public TreeNode leftNode;
    public TreeNode rightNode;
    public Frame leftFrame;
    public Frame rightFrame;

    public void learn(final Frame df, int[] indexes, RandomForest rf) {

        // compute distribution of classes
        int[] pall = new int[df.getCol(rf.classColName).getDictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            pall[df.getIndex(i, df.getColIndex(rf.classColName))]++;
        }

        if (df.getRowCount() == 1) {
            predicted = df.getLabel(0, df.getColIndex(rf.classColName));
            leaf = true;
            distribution = pall;
            return;
        }

        // leaf on all classes of same value
        for (int i = 1; i < pall.length; i++) {
            if (pall[i] == df.getRowCount()) {
                predicted = df.getLabel(0, df.getColIndex(rf.classColName));
                leaf = true;
                distribution = pall;
                return;
            }
            if (pall[i] != 0) {
                break;
            }
        }

        // find best split

        int count = 0;
        int indexLen = indexes.length;
        Vector classCol = df.getCol(rf.classColName);
        int classColIndex = df.getColIndex(rf.classColName);
        while (count < rf.mcols) {

            if (count == rf.mcols) break;

            int next = RandomSource.nextInt(indexLen);
            int colIndex = indexes[next];
            indexes[next] = indexes[indexLen - 1];
            indexes[indexLen - 1] = colIndex;
            indexLen--;
            count++;

            Vector col = df.getCol(colIndex);
            if (col.isNumeric()) {
                evaluateNumericCol(df, classColIndex, classCol, colIndex, col, pall);
            } else {
                evaluateNominalCol(df, classColIndex, classCol, colIndex, col, pall);
            }
        }
        if (leftNode != null && rightNode != null) {
            List<Integer> leftMap = new ArrayList<>();
            List<Integer> rightMap = new ArrayList<>();
            Vector col = df.getCol(splitCol);

            if (col.isNominal()) {
                // nominal
                for (int i = 0; i < df.getRowCount(); i++) {
                    if (splitLabel == df.getLabel(i, df.getColIndex(splitCol))) {
                        leftMap.add(classCol.getRowId(i));
                    } else {
                        rightMap.add(classCol.getRowId(i));
                    }
                }
            } else {
                // numeric
                for (int i = 0; i < df.getRowCount(); i++) {
                    if (df.getCol(splitCol).isMissing(i) || df.getCol(splitCol).getValue(i) < splitValue) {
                        leftMap.add(classCol.getRowId(i));
                    } else {
                        rightMap.add(classCol.getRowId(i));
                    }
                }
            }
            leftFrame = new MappedFrame(df.getSourceFrame(), new Mapping(leftMap));
            rightFrame = new MappedFrame(df.getSourceFrame(), new Mapping(rightMap));
        } else {
            int[] modes = new int[pall.length];
            int len = 0;
            for (int i = 1; i < pall.length; i++) {
                if (pall[i] > pall[modes[len]]) {
                    modes[0] = i;
                    len = 0;
                    continue;
                }
                if (pall[i] == pall[modes[len]]) {
                    len++;
                    modes[len] = i;
                }
            }
            predicted = df.getCol(rf.classColName).getDictionary()[modes[RandomSource.nextInt(len + 1)]];
            leaf = true;
            distribution = pall;
        }

    }

    private void evaluateNumericCol(Frame df, int classColIndex, Vector classCol, int colIndex, Vector col, int[] pall) {

        int[] pleft = new int[pall.length];
        Frame sort = RowFilters.sort(df, RowComparators.numericComparator(col, true));
        for (int i = 0; i < df.getRowCount() - 1; i++) {
            int index = sort.getCol(classColIndex).getIndex(i);
            pleft[index]++;
            if (col.isMissing(i)) continue;
            if (col.getValue(i) != col.getValue(i + 1)) {
                double metric = computeGini(pleft, pall);
                if (!validNumber(metric)) continue;

                if ((metricValue != metricValue) || metric > metricValue) {
                    metricValue = metric;
                    splitCol = df.getColNames()[colIndex];
                    splitLabel = "";
                    splitValue = (col.getValue(i) + col.getValue(i + 1)) / 2;
                    leftNode = new TreeNode();
                    rightNode = new TreeNode();
                }
            }
        }

    }

    private void evaluateNominalCol(Frame df, int classColIndex, Vector classCol, int selColIndex, Vector selCol, int[] pall) {
        int[][] p = new int[selCol.getDictionary().length][classCol.getDictionary().length];

        for (int i = 0; i < df.getRowCount(); i++) {
            p[df.getIndex(i, selColIndex)][df.getIndex(i, classColIndex)]++;
        }

        for (int j = 1; j < selCol.getDictionary().length; j++) {
            double metric = computeGini(p[j], pall);
            if (!validNumber(metric)) continue;

            if ((metricValue != metricValue) || metric > metricValue) {
                metricValue = metric;
                splitCol = df.getColNames()[selColIndex];
                splitLabel = selCol.getDictionary()[j];
                splitValue = Double.NaN;
                leftNode = new TreeNode();
                rightNode = new TreeNode();
            }
        }
    }

    private double computeGini(int[] pa, int[] pall) {
        int totalLeft = 0;
        int totalRight = 0;
        int upLeft = 0;
        int upRight = 0;
        for (int i = 1; i < pall.length; i++) {
            upLeft += pa[i] * pa[i];
            totalLeft += pa[i];
            upRight += (pall[i] - pa[i]) * (pall[i] - pa[i]);
            totalRight += pall[i] - pa[i];
        }

        if (totalLeft == 0 || totalRight == 0) return Double.NaN;
        return upLeft / (1. * totalLeft) + upRight / (1. * totalRight);
    }
}

