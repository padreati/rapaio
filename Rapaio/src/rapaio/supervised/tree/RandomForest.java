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

package rapaio.supervised.tree;

import static rapaio.core.BaseMath.*;
import rapaio.core.RandomSource;
import rapaio.core.stat.Mode;
import rapaio.data.*;
import rapaio.data.Vector;
import rapaio.explore.Workspace;
import rapaio.filters.RowFilters;
import rapaio.supervised.Classifier;
import rapaio.supervised.ClassifierModel;
import rapaio.sample.Sample;
import rapaio.supervised.stat.ConfusionMatrix;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    String[] giniImportanceNames;
    double[] giniImportanceValue;
    double[] giniImportanceCount;
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
        this.giniImportanceNames = df.getColNames();
        this.giniImportanceValue = new double[df.getColNames().length];
        this.giniImportanceCount = new double[df.getColNames().length];
        trees.clear();

        oobError = computeOob ? 0 : Double.NaN;

        if (computeOob) {
            setupOobContainer(df);
        }

        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Collection<Callable<Object>> tasks = new ArrayList<>();
        final List<Frame> bootstraps = new ArrayList<>();
        for (int i = 0; i < mtrees; i++) {
            final Tree tree = new Tree(this);
            trees.add(tree);
            final Frame bootstrap = Sample.randomBootstrap(df);
            bootstraps.add(bootstrap);
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Map<Integer, Double> weights = new HashMap<>();
                    for (int j = 0; j < bootstrap.getRowCount(); j++) {
                        weights.put(bootstrap.getRowId(j), 1.);
                    }
                    tree.learn(bootstrap, weights);
                    return null;
                }
            });

        }
        try {
            es.invokeAll(tasks);
            es.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (computeOob) {
            for (int i = 0; i < mtrees; i++) {
                addOob(df, bootstraps.get(i), trees.get(i));
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
        StringBuilder sb = new StringBuilder();
        summaryVariableImportance(sb);
        Workspace.code(sb.toString());
    }

    private void summaryVariableImportance(StringBuilder sb) {
        sb.append("Gini variable importance: \n");
        Vector[] vectors = new Vector[2];
        vectors[0] = new NominalVector("varName", giniImportanceNames.length, giniImportanceNames);
        vectors[1] = new NumericVector("meanDecrease", new double[giniImportanceNames.length]);
        Frame f = new SolidFrame("gini", giniImportanceNames.length, vectors);
        int width = 0;
        for (int i = 0; i < giniImportanceNames.length; i++) {
            String colName = giniImportanceNames[i];
            if (colName.equals(classColName)) continue;
            width = max(width, classColName.length() + 1);
            double decrease = 0;
            if (giniImportanceCount[i] != 0) {
                decrease = giniImportanceValue[i] / giniImportanceCount[i];
            }
            f.setLabel(i, 0, colName);
            f.setValue(i, 1, decrease);
        }
        f = RowFilters.sort(f, RowComparators.numericComparator(f.getCol(1), false));

        for (int i = 0; i < f.getRowCount(); i++) {
            sb.append(String.format("%" + width + "s : %10.4f\n", f.getLabel(i, 0), f.getValue(i, 1)));
        }
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

    public void learn(Frame df, Map<Integer, Double> weight) {
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
        LinkedList<Map<Integer, Double>> weights = new LinkedList<>();
        nodes.addLast(root);
        frames.addLast(df);
        weights.add(weight);
        while (!nodes.isEmpty()) {
            TreeNode cn = nodes.pollFirst();
            Frame currentFrame = frames.pollFirst();
            Map<Integer, Double> currentWeight = weights.pollFirst();
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
            cn.leftWeight = null;
            cn.rightWeight = null;
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

            double pleft = node.leftNode.totalFd;
            double pright = node.leftNode.totalFd;
            for (int i = 0; i < dict.length; i++) {
                pd[i] = (pleft * left[i] + pright * right[i]) / (pleft + pright);
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
    public double[] pd;
    public double[] fd;
    public double totalFd;
    public double giniOrig;

    public String predicted;
    public TreeNode leftNode;
    public TreeNode rightNode;
    public Frame leftFrame;
    public Frame rightFrame;
    public Map<Integer, Double> leftWeight;
    public Map<Integer, Double> rightWeight;

    public void learn(final Frame df, Map<Integer, Double> weights, int[] indexes, RandomForest rf) {
        Vector classCol = df.getCol(rf.classColName);
        int classColIndex = df.getColIndex(rf.classColName);

        // compute distribution of classes
        fd = new double[rf.dict.length];
        for (int i = 0; i < df.getRowCount(); i++) {
            if (weights.get(df.getRowId(i)) == null) {
                System.out.println("nasol");
            }
            fd[classCol.getIndex(i)] += weights.get(df.getRowId(i));
            totalFd += weights.get(df.getRowId(i));
        }
        pd = new double[fd.length];
        giniOrig = 1;
        for (int i = 0; i < pd.length; i++) {
            pd[i] = fd[i] / totalFd;
            giniOrig -= pd[i] * pd[i];
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
            if (fd[i] != 0) break;
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
                evaluateNumericCol(df, classColIndex, classCol, colIndex, col, weights);
            } else {
                evaluateNominalCol(df, classColIndex, classCol, colIndex, col, weights);
            }
        }
        if (leftNode != null && rightNode != null) {
            // build data for left and right nodes
            List<Integer> missing = new ArrayList<>();
            List<Integer> leftMap = new ArrayList<>();
            List<Integer> rightMap = new ArrayList<>();
            leftWeight = new HashMap<>();
            rightWeight = new HashMap<>();
            Vector col = df.getCol(splitCol);
            double missingWeight = 0;

            if (col.isNominal()) {
                // nominal
                for (int i = 0; i < df.getRowCount(); i++) {
                    int id = df.getRowId(i);
                    if (col.isMissing(i)) {
                        missing.add(id);
                        continue;
                    }
                    if (splitLabel.equals(col.getLabel(i))) {
                        leftMap.add(id);
                        leftWeight.put(id, weights.get(id));
                    } else {
                        rightMap.add(id);
                        rightWeight.put(id, weights.get(id));
                    }
                }
            } else {
                // numeric
                for (int i = 0; i < df.getRowCount(); i++) {
                    int id = df.getRowId(i);
                    if (col.isMissing(i)) {
                        missing.add(id);
                        continue;
                    }
                    if (col.getValue(i) <= splitValue) {
                        leftMap.add(id);
                        leftWeight.put(id, weights.get(id));
                    } else {
                        rightMap.add(df.getRowId(i));
                        rightWeight.put(id, weights.get(id));
                    }
                }
            }
            if (!missing.isEmpty()) {
                double p = leftMap.size() / (0. + leftMap.size() + rightMap.size());
                for (int id : missing) {
                    leftMap.add(id);
                    leftWeight.put(id, weights.get(id) * p);
                    rightMap.add(id);
                    rightWeight.put(id, weights.get(id) * (1. - p));
                    missingWeight += weights.get(id);
                }
            }
            if (!leftMap.isEmpty() && !rightMap.isEmpty()) {
                leftFrame = new MappedFrame(df.getSourceFrame(), new Mapping(leftMap));
                rightFrame = new MappedFrame(df.getSourceFrame(), new Mapping(rightMap));
                // sum to variable importance
                rf.giniImportanceValue[df.getColIndex(splitCol)] += metricValue * (1 - missingWeight / totalFd);
//                rf.giniImportanceValue[df.getColIndex(splitCol)] += metricValue;
                rf.giniImportanceCount[df.getColIndex(splitCol)]++;
                return;
            } else {
                System.out.println("Something went really wrong");
            }
        }
        String[] modes = new Mode(classCol, false).getModes();
        predicted = modes[RandomSource.nextInt(modes.length)];
        leaf = true;
    }

    private void evaluateNumericCol(Frame df, int classColIndex, Vector classCol, int colIndex, Vector col, Map<Integer, Double> weights) {
        double[][] p = new double[2][fd.length];
        int[] rowCounts = new int[2];
        Frame sort = RowFilters.sort(df, RowComparators.numericComparator(col, true));
        for (int i = 0; i < df.getRowCount() - 1; i++) {
            int row = sort.getCol(colIndex).isMissing(i) ? 0 : 1;
            int index = sort.getIndex(i, classColIndex);
            p[row][index] += weights.get(sort.getRowId(i));
            rowCounts[row]++;
            if (row == 0) {
                continue;
            }
            if (rowCounts[1] == 0) continue;
            if (df.getRowCount() - rowCounts[1] - rowCounts[0] == 0) continue;
            if (sort.getValue(i, colIndex) < sort.getValue(i + 1, colIndex)) {
                double metric = compute(p[0], p[1]);
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

    private void evaluateNominalCol(Frame df, int classColIndex, Vector classCol, int selColIndex, Vector selCol, Map<Integer, Double> weights) {
        if (selCol.getDictionary().length == 2) {
            return;
        }
        double[][] p = new double[selCol.getDictionary().length][classCol.getDictionary().length];
        int[] rowCounts = new int[selCol.getDictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            p[selCol.getIndex(i)][classCol.getIndex(i)] += weights.get(df.getRowId(i));
            rowCounts[selCol.getIndex(i)]++;
        }

        for (int j = 1; j < selCol.getDictionary().length; j++) {
            if (rowCounts[j] == 0) continue;
            if (df.getRowCount() - rowCounts[j] - rowCounts[0] == 0) continue;
            if (selCol.getDictionary().length == 3 && j == 2) {
                continue;
            }
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
        double totalOrig = 0;
        double totalLeft = 0;
        double totalRight = 0;
        for (int i = 1; i < fd.length; i++) {
            double left = pa[i];
            double right = fd[i] - pa[i] - missing[i];
            double orig = fd[i] - missing[i];
            totalLeft += left;
            totalRight += right;
            totalOrig += orig;
        }
        if (totalLeft == 0 || totalRight == 0) return Double.NaN;
        if (!validNumber(totalLeft) || !validNumber(totalRight)) return Double.NaN;
        double giniOrig = 1;
        double giniLeft = 1;
        double giniRight = 1;
        for (int i = 1; i < fd.length; i++) {
            double pleft = pa[i] / totalLeft;
            double pright = (fd[i] - pa[i] - missing[i]) / totalRight;
            double porig = (fd[i] - missing[i]) / totalOrig;
            giniOrig -= porig * porig;
            giniLeft -= pleft * pleft;
            giniRight -= pright * pright;
        }
        return giniOrig - totalLeft * giniLeft / totalOrig - totalRight * giniRight / totalOrig;
    }
}

