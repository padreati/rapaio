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
import rapaio.filters.RowFilters;
import rapaio.ml.supervised.Classifier;
import rapaio.ml.supervised.ClassifierModel;

import java.util.*;
import java.util.concurrent.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForest implements Classifier {
    private final int mtrees;
    private final int mcols;
    private final List<Tree> trees = new ArrayList<>();
    private String classColName;
    private String[] dict;
    private boolean debug = false;

    public RandomForest(int mtrees, int mcols) {
        this.mtrees = mtrees;
        this.mcols = mcols;
    }

    @Override
    public void learn(final Frame df, final String classColName) {
        this.classColName = classColName;
        this.dict = df.getCol(classColName).getDictionary();
        trees.clear();
        if (debug)
            System.out.println("learning rf.. ");
        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        LinkedList<Callable<Object>> tasks = new LinkedList<>();
        for (int i = 0; i < mtrees; i++) {
            final Tree tree = new Tree(max(mcols, df.getColCount() - 1), debug);
            trees.add(tree);

            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Frame bootstrap = RowFilters.bootstrap(df);
                    tree.learn(bootstrap, classColName);
                    return null;
                }
            });
        }
        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
        }
        pool.shutdown();
        if (debug)
            System.out.println();
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

class Tree implements Classifier {
    private final int mcols;
    private final boolean debug;
    private TreeNode root;
    private String[] dict;

    Tree(int mcols, boolean debug) {
        this.mcols = mcols;
        this.debug = debug;
    }

    @Override
    public void learn(Frame df, String classColName) {
        int[] indexes = new int[df.getColCount() - 1];
        int pos = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            if (i != df.getColIndex(classColName)) {
                indexes[pos++] = i;
            }
        }

        this.dict = df.getCol(classColName).getDictionary();
        this.root = new TreeNode();

        LinkedList<TreeNode> nodes = new LinkedList<>();
        LinkedList<Frame> frames = new LinkedList<>();

        nodes.addLast(root);
        frames.addLast(df);

        int len = 0;

        while (!nodes.isEmpty()) {
            len++;
            TreeNode currentNode = nodes.pollFirst();
            Frame currentFrame = frames.pollFirst();

            currentNode.learn(currentFrame, classColName, mcols, indexes);
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
        if (debug) {
            System.out.println("learned rf tree size: " + len);
        }

    }

    @Override
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
    public int[] distribution;

    public String predicted;

    public TreeNode leftNode;
    public TreeNode rightNode;
    public Frame leftFrame;
    public Frame rightFrame;

    public void learn(final Frame df, String classColName, int mcols, int[] indexes) {

        // compute distribution of classes

        int[] pall = new int[df.getCol(classColName).getDictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            pall[df.getIndex(i, df.getColIndex(classColName))]++;
        }

        if (df.getRowCount() == 1) {
            predicted = df.getLabel(0, df.getColIndex(classColName));
            leaf = true;
            distribution = pall;
            return;
        }

        // leaf on all classes of same value
        for (int i = 1; i < pall.length; i++) {
            if (pall[i] == df.getRowCount()) {
                predicted = df.getLabel(0, df.getColIndex(classColName));
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
        Vector classCol = df.getCol(classColName);
        int classColIndex = df.getColIndex(classColName);
        while (count < mcols) {

            if (count == mcols) break;

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

            for (int i = 0; i < df.getRowCount(); i++) {
                if (splitValue != splitValue) {
                    // nominal
                    if (splitLabel == df.getLabel(i, df.getColIndex(splitCol))) {
                        leftMap.add(classCol.getRowId(i));
                    } else {
                        rightMap.add(classCol.getRowId(i));
                    }
                } else {
                    // numeric
                }

            }
            leftFrame = new MappedFrame(df.getSourceFrame(), new Mapping(leftMap));
            rightFrame = new MappedFrame(df.getSourceFrame(), new Mapping(rightMap));
        } else {
            String[] modes = new Mode(df.getCol(classColName)).getModes();
            predicted = modes[RandomSource.nextInt(modes.length)];
            leaf = true;
            distribution = pall;
        }
    }

    private void evaluateNumericCol(Frame df, int classColIndex, Vector classCol, int colIndex, Vector col, int[] pall) {

        int[] pleft = new int[pall.length];

    }

    private void evaluateNominalCol(Frame df, int classColIndex, Vector classCol, int selColIndex, Vector selCol, int[] pall) {
        int[][] p = new int[selCol.getDictionary().length][classCol.getDictionary().length];

        for (int i = 0; i < df.getRowCount(); i++) {
            p[df.getIndex(i, selColIndex)][df.getIndex(i, classColIndex)]++;
        }

        for (int j = 1; j < selCol.getDictionary().length; j++) {

            int totalLeft = 0;
            int totalRight = 0;
            int upLeft = 0;
            int upRight = 0;
            for (int i = 1; i < pall.length; i++) {
                upLeft += p[j][i] * p[j][i];
                totalLeft += p[j][i];
                upRight += (pall[i] - p[j][i]) * (pall[i] - p[j][i]);
                totalRight += pall[i] - p[j][i];
            }

            if (totalLeft == 0 || totalRight == 0) continue;

            double metric = upLeft / (1. * totalLeft) + upRight / (1. * totalRight);

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
}

