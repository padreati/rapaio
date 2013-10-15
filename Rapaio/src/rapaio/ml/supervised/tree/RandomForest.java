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

import static rapaio.core.BaseMath.*;
import rapaio.core.RandomSource;
import rapaio.core.stat.Mode;
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
    int mcols2;
    final boolean computeOob;
    private final List<Tree> trees = new ArrayList<>();
    String classColName;
    String[] dict;
    boolean debug = false;
    double oobError = 0;
    int[][] oobFreq;

    public RandomForest(int mtrees) {
        this(mtrees, 0, true);
    }

    public RandomForest(int mtrees, int mcols) {
        this(mtrees, mcols, true);
    }

    public RandomForest(int mtrees, int mcols, boolean computeOob) {
        this.mtrees = mtrees;
        this.mcols = mcols;
        this.computeOob = computeOob;
    }

    public double getOobError() {
        return oobError;
    }

    @Override
    public void learn(final Frame df, final String classColName) {
        mcols2 = mcols;
        if (mcols2 > df.getColCount() - 1) {
            mcols2 = df.getColCount() - 1;
        }
        if (mcols2 < 1) {
            mcols2 = (int) log2(df.getColCount()) + 1;
        }

        for (int i = 0; i < df.getRowCount(); i++) {
            if (df.getCol(classColName).isMissing(i)) {
                throw new IllegalArgumentException("Not allowed missing classes");
            }
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
            Frame bootstrap = Sample.randomBootstrap(df);
            List<Double> weights = new ArrayList<>();
            for (int j = 0; j < bootstrap.getRowCount(); j++) {
                weights.add(1.);
            }
            tree.learn(bootstrap, weights);
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
            indexes[0] = 1;
            for (int j = 1; j < dict.length; j++) {
                if (oobFreq[i][j] == 0) continue;
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
                if (next != df.getSourceFrame().getCol(classColName).getIndex(i)) {
                    total += 1.;
                }
            }
        }
        return total / count;
    }

    @Override
    public ClassifierModel predict(final Frame df) {
        final Vector predict = new NominalVector(classColName, df.getRowCount(), dict);
        List<Vector> vectors = new ArrayList<>();
        for (int i = 0; i < dict.length; i++) {
            vectors.add(new NumericVector(dict[i], new double[df.getRowCount()]));
        }
        final Frame prob = new SolidFrame("prob", df.getRowCount(), vectors);

        for (int m = 0; m < mtrees; m++) {
            Tree tree = trees.get(m);
            ClassifierModel model = tree.predict(df);
            for (int i = 0; i < df.getRowCount(); i++) {
                for (int j = 0; j < model.getProbabilities().getColCount(); j++) {
                    prob.setValue(i, j, prob.getValue(i, j) + model.getProbabilities().getValue(i, j));
                }
            }
        }

        // from freq to prob

        for (int i = 0; i < prob.getRowCount(); i++) {
            double max = 0;
            int col = 0;
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

    public void learn(Frame df, List<Double> weight) {
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
        LinkedList<List<Double>> weights = new LinkedList<>();
        nodes.addLast(root);
        frames.addLast(df);
        weights.add(weight);
        while (!nodes.isEmpty()) {
            TreeNode cn = nodes.pollFirst();
            Frame currentFrame = frames.pollFirst();
            List<Double> currentWeight = weights.pollFirst();
            cn.learn(currentFrame, currentWeight, indexes, rf);
            if (cn.leaf) {
                continue;
            }
            nodes.addLast(cn.leftNode);
            nodes.addLast(cn.rightNode);
            frames.addLast(cn.leftFrame);
            frames.addLast(cn.rightFrame);
            weights.add(cn.leftWeight);
            weights.add(cn.rightWeight);
            cn.leftFrame = null;
            cn.rightFrame = null;
        }
    }

    public ClassifierModel predict(final Frame df) {
        final Vector classification = new NominalVector("classification", df.getRowCount(), dict);
        List<Vector> dvectors = new ArrayList<>();
        for (int i = 0; i < dict.length; i++) {
            dvectors.add(new NumericVector(dict[i], new double[df.getRowCount()]));
        }
        final Frame d = new SolidFrame("distribution", df.getRowCount(), dvectors);
        for (int i = 0; i < df.getRowCount(); i++) {
            double[] distribution = predict(df, i, root);
            for (int j = 0; j < distribution.length; j++) {
                d.setValue(i, j, distribution[j]);
            }
            int[] indexes = new int[distribution.length];
            int len = 1;
            indexes[0] = 1;
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
            classification.setLabel(i, dict[indexes[RandomSource.nextInt(len)]]);
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
                return d;
            }
        };
    }

    private double[] predict(Frame df, int row, TreeNode node) {
        if (node.leaf) {
            return node.pd;
        }
        int col = df.getColIndex(node.splitCol);
        if (df.getCol(col).isMissing(row)) {
            double[] left = predict(df, row, node.leftNode);
            double[] right = predict(df, row, node.rightNode);
            double[] pd = new double[dict.length];

//            double pleft = node.leftNode.size / df.getRowCount();
//            double pright = node.rightNode.size / df.getRowCount();
            double pleft = node.leftNode.weight;
            double pright = node.rightNode.weight;
            for (int i = 0; i < dict.length; i++) {
//                pd[i] = max(left[i],right[i]);
//                pd[i] = left[i] + right[i];
//                pd[i] = left[i] + right[i];
//                pd[i] = pow(left[i], E) + pow(right[i], E);
                pd[i] = pleft * left[i] + pright * right[i];
            }
            return pd;
        }
        if (df.getCol(col).isNumeric()) {
            double value = df.getValue(row, col);
            return predict(df, row, value <= node.splitValue ? node.leftNode : node.rightNode);
        } else {
            String label = df.getLabel(row, col);
            return predict(df, row, node.splitLabel.equals(label) ? node.leftNode : node.rightNode);
        }
    }
}

class TreeNode {
    public boolean leaf = false;
    public String splitCol;
    public String splitLabel;
    public double splitValue;
    public double metricValue = Double.NaN;
    public double weight;
    public List<Double> weights;
    public double[] pd;
    public double[] fd;
    public double totalFd;

    public String predicted;
    public TreeNode leftNode;
    public TreeNode rightNode;
    public Frame leftFrame;
    public Frame rightFrame;
    public List<Double> leftWeight;
    public List<Double> rightWeight;

    public void learn(final Frame df, List<Double> currentWeight, int[] indexes, RandomForest rf) {
        this.weight = 0;
        for (double w : currentWeight) {
            weight += w;
        }
        weight /= (1. * df.getRowCount());
        weights = currentWeight;

        Vector classCol = df.getCol(rf.classColName);
        int classColIndex = df.getColIndex(rf.classColName);

        // compute distribution of classes
        fd = new double[rf.dict.length];
        for (int i = 0; i < df.getRowCount(); i++) {
            fd[classCol.getIndex(i)] += weights.get(i);
            totalFd += weights.get(i);
        }
        pd = new double[fd.length];
        for (int i = 0; i < pd.length; i++) {
            pd[i] = fd[i] / totalFd;
        }

        if (df.getRowCount() == 1) {
            predicted = classCol.getLabel(0);
            leaf = true;
            return;
        }

        // leaf on all classes of same value
        for (int i = 1; i < fd.length; i++) {
            if (fd[i] == df.getRowCount()) {
                predicted = classCol.getLabel(0);
                leaf = true;
                return;
            }
            if (fd[i] != 0) {
                break;
            }
        }

        // find best split

        int count = 0;
        int len = indexes.length - 1;

        while (count < rf.mcols2) {

            int next = RandomSource.nextInt(len + 1);
            int colIndex = indexes[next];
            indexes[next] = indexes[len];
            indexes[len] = colIndex;
            len--;
            count++;

            Vector col = df.getCol(colIndex);
            if (col.isNumeric()) {
                evaluateNumericCol(df, classColIndex, classCol, colIndex, col);
            } else {
                evaluateNominalCol(df, classColIndex, classCol, colIndex, col);
            }
        }
        if (leftNode != null && rightNode != null) {
            List<Integer> leftMap = new ArrayList<>();
            List<Integer> rightMap = new ArrayList<>();
            leftWeight = new ArrayList<>();
            rightWeight = new ArrayList<>();
            Vector col = df.getCol(splitCol);

            if (col.isNominal()) {
                // nominal
                double leftCount = 0;
                double rightCount = 0;
                for (int i = 0; i < df.getRowCount(); i++) {
                    if (col.isMissing(i)) {
                        leftCount++;
                        rightCount++;
                        continue;
                    }
                    if (splitLabel.equals(col.getLabel(i))) {
                        leftCount++;
                    } else {
                        rightCount++;
                    }
                }
                double pleft = leftCount / (1. * df.getRowCount());
                double pright = rightCount / (1. * df.getRowCount());
                for (int i = 0; i < df.getRowCount(); i++) {
                    if (col.isMissing(i)) {
                        leftMap.add(col.getRowId(i));
                        leftWeight.add(currentWeight.get(i) * pleft);
                        rightMap.add(col.getRowId(i));
                        rightWeight.add(currentWeight.get(i) * pright);
                        continue;
                    }
                    if (splitLabel.equals(col.getLabel(i))) {
                        leftMap.add(col.getRowId(i));
                        leftWeight.add(currentWeight.get(i));
                    } else {
                        rightMap.add(classCol.getRowId(i));
                        rightWeight.add(currentWeight.get(i));
                    }
                }
            } else {
                // numeric
                double leftCount = 0;
                double rightCount = 0;
                for (int i = 0; i < df.getRowCount(); i++) {
                    if (col.isMissing(i)) {
                        leftCount++;
                        rightCount++;
                        continue;
                    }
                    if (col.getValue(i) <= splitValue) {
                        leftCount++;
                    } else {
                        rightCount++;
                    }
                }
                double pleft = leftCount / (1. * df.getRowCount());
                double pright = rightCount / (1. * df.getRowCount());
                for (int i = 0; i < df.getRowCount(); i++) {
                    if (col.isMissing(i)) {
                        leftMap.add(col.getRowId(i));
                        leftWeight.add(currentWeight.get(i) * pleft);
                        rightMap.add(col.getRowId(i));
                        rightWeight.add(currentWeight.get(i) * pright);
                        continue;
                    }
                    if (col.getValue(i) <= splitValue) {
                        leftMap.add(col.getRowId(i));
                        leftWeight.add(currentWeight.get(i));
                    } else {
                        rightMap.add(col.getRowId(i));
                        rightWeight.add(currentWeight.get(i));
                    }
                }
            }
            if (!leftMap.isEmpty() && !rightMap.isEmpty()) {
                leftFrame = new MappedFrame(df.getSourceFrame(), new Mapping(leftMap));
                rightFrame = new MappedFrame(df.getSourceFrame(), new Mapping(rightMap));
                return;
            } else {
                System.out.println("Something went really wrong");
            }
        }
        String[] modes = new Mode(classCol, false).getModes();
        predicted = modes[RandomSource.nextInt(modes.length)];
        leaf = true;
    }

    private void evaluateNumericCol(Frame df, int classColIndex, Vector classCol, int colIndex, Vector col) {
        double[][] pleft = new double[2][fd.length];
        Frame sort = RowFilters.sort(df, RowComparators.numericComparator(col, true));
        for (int i = 0; i < df.getRowCount() - 1; i++) {
            int row = sort.getCol(colIndex).isMissing(i) ? 0 : 1;
            int index = sort.getIndex(i, classColIndex);
            pleft[row][index] += weights.get(i);

            if (row == 0) {
                continue;
            }
            if (sort.getValue(i, colIndex) < sort.getValue(i + 1, colIndex)) {
                double metric = compute(pleft[0], pleft[1]);
                if (!validNumber(metric)) continue;

                if ((metricValue != metricValue) || metric > metricValue) {
                    metricValue = metric;
                    splitCol = df.getColNames()[colIndex];
                    splitLabel = "";
                    splitValue = sort.getCol(colIndex).getValue(i);
                    leftNode = new TreeNode();
                    rightNode = new TreeNode();
                }
            }
        }

    }

    private void evaluateNominalCol(Frame df, int classColIndex, Vector classCol, int selColIndex, Vector selCol) {
        double[][] p = new double[selCol.getDictionary().length][classCol.getDictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            p[selCol.getIndex(i)][classCol.getIndex(i)] += weights.get(i);
        }

        for (int j = 1; j < selCol.getDictionary().length; j++) {
            double metric = compute(p[0], p[j]);
            if (!validNumber(metric)) continue;
            if ((metricValue != metricValue) || metric - metricValue > 0) {
                metricValue = metric;
                splitCol = df.getColNames()[selColIndex];
                splitLabel = selCol.getDictionary()[j];
                splitValue = Double.NaN;
                leftNode = new TreeNode();
                rightNode = new TreeNode();
            }
        }
    }

    private double compute(double[] missing, double[] pa) {
        return computeGini(missing, pa);
    }

    private double computeGini(double[] missing, double[] pa) {
        double totalLeft = 0;
        double totalRight = 0;
        double upLeft = 0;
        double upRight = 0;
        for (int i = 1; i < fd.length; i++) {
            double left = pa[i];
            upLeft += left * left;
            totalLeft += left;
            double right = fd[i] - pa[i] - missing[i];
            upRight += right * right;
            totalRight += right;
        }
        if (totalLeft == 0 || totalRight == 0) return Double.NaN;
        if (!validNumber(totalLeft) || !validNumber(totalRight)) return Double.NaN;
        return upLeft / totalLeft + upRight / totalRight;
    }
}

