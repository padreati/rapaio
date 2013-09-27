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
import rapaio.data.Vector;
import rapaio.filters.NominalFilters;
import rapaio.filters.RowFilters;
import rapaio.ml.supervised.Classifier;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierProvider;

import java.util.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForest implements Classifier {
    private final int mtrees;
    private final int mcols;
    private List<Tree> trees = new ArrayList<>();
    private String classColName;
    private String[] dict;
    private boolean debug = false;

    public RandomForest(int mtrees, int mcols) {
        this.mtrees = mtrees;
        this.mcols = mcols;
    }

    @Override
    public void learn(Frame df, String classColName) {
        this.classColName = classColName;
        this.dict = df.getCol(classColName).getDictionary();
        trees.clear();
        if (debug)
            System.out.println("learning rf.. ");
        for (int i = 0; i < mtrees; i++) {
            if (debug) {
                System.out.print(".");
                if ((i + 1) % 100 == 0) System.out.println();
            }
            Tree tree = new Tree(mcols);
            Frame bootstrap = RowFilters.bootstrap(df);
            tree.learn(bootstrap, classColName);
            trees.add(tree);
        }
        if (debug)
            System.out.println();
    }

    @Override
    public ClassifierModel predict(final Frame df) {
        final Vector predict = new NominalVector(classColName, df.getRowCount(), dict);
        Vector[] vectors = new Vector[dict.length - 1];
        for (int i = 0; i < dict.length - 1; i++) {
            vectors[i] = new NumericVector(dict[i + 1], new double[df.getRowCount()]);
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
            predict.setLabel(i, dict[col + 1]);
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

class Tree implements Classifier {
    private final int mcols;
    private TreeNode root;
    private String[] dict;

    Tree(int mcols) {
        this.mcols = mcols;
    }

    @Override
    public void learn(Frame df, String classColName) {
        this.dict = df.getCol(classColName).getDictionary();
        this.root = new TreeNode();

        LinkedList<TreeNode> nodes = new LinkedList<>();
        LinkedList<Frame> frames = new LinkedList<>();

        nodes.addLast(root);
        frames.addLast(df);

        while (!nodes.isEmpty()) {
            TreeNode currentNode = nodes.pollFirst();
            Frame currentFrame = frames.pollFirst();

            currentNode.learn(currentFrame, classColName, mcols);
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

    @Override
    public ClassifierModel predict(final Frame df) {
        final Vector classification = new NominalVector("classification", df.getRowCount(), dict);
        for (int i = 0; i < df.getRowCount(); i++) {
            String prediction = predict(df, i, root);
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

    private String predict(Frame df, int row, TreeNode root) {
        while (true) {
            if (root.leaf) {
                return root.predicted;
            }
            int col = df.getColIndex(root.splitCol);
            if (df.getCol(col).isNumeric()) {
                double value = df.getValue(row, col);
                if (root.splitValue != root.splitValue) {
                    root = (value != value) ? root.leftNode : root.rightNode;
                } else {
                    root = ((value != value) || (value < root.splitValue)) ? root.leftNode : root.rightNode;
                }
            } else {
                String label = df.getLabel(row, col);
                root = root.splitLabel.equals(label) ? root.leftNode : root.rightNode;
            }
        }
    }

    @Override
    public void summary() {
    }
}

class TreeNode {
    public boolean leaf = false;
    public String splitCol;
    public String splitLabel;
    public double splitValue;
    public double metricValue = Double.NaN;

    public String predicted;

    public TreeNode leftNode;
    public TreeNode rightNode;
    public Frame leftFrame;
    public Frame rightFrame;

    public void learn(final Frame df, String classColName, int mcols) {

        // leaf on all classes of same value
        boolean same = true;
        for (int i = 1; i < df.getRowCount(); i++) {
            if (df.getIndex(i - 1, df.getColIndex(classColName)) != df.getIndex(i, df.getColIndex(classColName))) {
                same = false;
                break;
            }
        }
        if (same) {
            predicted = df.getLabel(0, df.getColIndex(classColName));
            leaf = true;
            return;
        }

        if (df.getRowCount() == 1) {
            predicted = df.getLabel(0, df.getColIndex(classColName));
            leaf = true;
            return;
        }

        // find best split

        Vector cols = new IndexVector("", 0, df.getColCount() - 1, 1);
        cols = RowFilters.shuffle(cols);
        int count = 0;
        for (int i = 0; i < cols.getRowCount(); i++) {
            int colIndex = cols.getIndex(i);

            if (df.getColIndex(classColName) == colIndex) continue;
            if (count == mcols) break;
            count++;

            Vector col = df.getCol(colIndex);
            if (col.isNumeric()) {
                evaluateNumericCol(df, classColName, colIndex, col);
            } else {
                evaluateNominalCol(df, classColName, colIndex, col);
            }
        }
        if (!leaf && ((leftNode == null) || (rightNode == null))) {
            String[] modes = new Mode(df.getCol(classColName)).getModes();
            if (modes.length == 0) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            predicted = modes[0];
            if (modes.length > 1) {
                predicted = modes[RandomSource.nextInt(modes.length)];
            }
            leaf = true;
        }
    }

    private void evaluateNumericCol(Frame df, String classColName, int colIndex, Vector col) {
    }

    private void evaluateNominalCol(Frame df, String classColName, int selColIndex, Vector selCol) {

        Vector classCol = df.getCol(classColName);
        int classColIndex = df.getColIndex(classColName);

        int[][] p = new int[selCol.getDictionary().length][classCol.getDictionary().length];
        int[] pall = new int[classCol.getDictionary().length];

        for (int i = 0; i < df.getRowCount(); i++) {
            p[df.getIndex(i, selColIndex)][df.getIndex(i, classColIndex)]++;
            pall[df.getIndex(i, classColIndex)]++;
        }


        double countAll = 0;
        double sEntropy = 0;
        for (int i = 0; i < pall.length; i++) {
            countAll += pall[i];
        }
        for (int i = 0; i < pall.length; i++) {
            sEntropy += entropy(pall[i] / countAll);
        }

        for (int j = 0; j < selCol.getDictionary().length; j++) {

            int countCol = 0;
            int countOther = 0;
            for (int i = 0; i < pall.length; i++) {
                countCol += p[j][i];
                countOther += pall[i] - p[j][i];
            }

            if (countCol == 0 || countOther == 0) continue;

            double metric = sEntropy;
            for (int i = 0; i < pall.length; i++) {
                metric -= entropy(p[j][i] / (countCol * 1.));
                metric -= entropy((pall[i] - p[j][i]) / (1. * countOther));
            }


            if (metricValue != metricValue) {
                metricValue = metric;
                splitCol = df.getColNames()[selColIndex];
                splitLabel = selCol.getDictionary()[j];
                splitValue = Double.NaN;
                leftNode = new TreeNode();
                rightNode = new TreeNode();
                leftFrame = new MappedFrame(df, buildMapping(selCol, selCol.getDictionary()[j], true));
                rightFrame = new MappedFrame(df, buildMapping(selCol, selCol.getDictionary()[j], false));
            } else {
                if (metric > metricValue) {
                    metricValue = metric;
                    splitCol = df.getColNames()[selColIndex];
                    splitLabel = selCol.getDictionary()[j];
                    splitValue = Double.NaN;
                    leftNode = new TreeNode();
                    rightNode = new TreeNode();
                    leftFrame = new MappedFrame(df, buildMapping(selCol, selCol.getDictionary()[j], true));
                    rightFrame = new MappedFrame(df, buildMapping(selCol, selCol.getDictionary()[j], false));
                }
            }
        }
    }

    private Mapping buildMapping(Vector col, String label, boolean include) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < col.getRowCount(); i++) {
            if (col.getLabel(i).equals(label) && include) {
                mapping.add(i);
                continue;
            }
            if (!col.getLabel(i).equals(label) && !include) {
                mapping.add(i);
            }
        }
        return new Mapping(mapping);
    }

    private double entropy(double value) {
        if (value == 0) return 0;
        return -1. * value * log(value) / log(2);
    }

}

