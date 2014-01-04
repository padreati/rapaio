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

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.filters.RowFilters;
import rapaio.ml.classification.AbstractClassifier;
import rapaio.ml.classification.Classifier;
import rapaio.ml.classification.colselect.ColSelector;
import rapaio.ml.classification.colselect.RandomColSelector;
import rapaio.sample.StatSampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static rapaio.core.BaseMath.log2;
import static rapaio.core.BaseMath.max;
import static rapaio.workspace.Workspace.code;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForest extends AbstractClassifier<RandomForest> {
    int mtrees = 10;
    int mcols = -1;
    boolean computeOob = false;
    ColSelector colSelector;
    List<Classifier> trees = new ArrayList<>();
    String classColName;
    String[] dict;
    String[] giniImportanceNames;
    double[] giniImportanceValue;
    double[] giniImportanceCount;
    double oobError = 0;
    int[][] oobFreq;
    NominalVector predict;
    Frame dist;
    long learnTime = 0;
    int minNodeSize = 1;
    double numericSelectionProb = 1.;

    @Override
    public RandomForest newInstance() {
        RandomForest rf = new RandomForest();
        rf.setMcols(getMcols());
        rf.setMtrees(getMtrees());
        rf.setComputeOob(getComputeOob());
        rf.setMinNodeSize(getMinNodeSize());
        rf.setNumericSelectionProb(getNumericSelectionProb());
        return rf;
    }

    public int getMtrees() {
        return mtrees;
    }

    public RandomForest setMtrees(int mtrees) {
        this.mtrees = mtrees;
        return this;
    }

    public int getMcols() {
        return mcols;
    }

    public RandomForest setMcols(int mcols) {
        this.mcols = mcols;
        return this;
    }

    public boolean getComputeOob() {
        return computeOob;
    }

    public RandomForest setComputeOob(boolean computeOob) {
        this.computeOob = computeOob;
        return this;
    }

    public int getMinNodeSize() {
        return minNodeSize;
    }

    public void setMinNodeSize(int minNodeSize) {
        this.minNodeSize = minNodeSize;
    }

    public double getNumericSelectionProb() {
        return numericSelectionProb;
    }

    public void setNumericSelectionProb(double numericSelectionProb) {
        this.numericSelectionProb = numericSelectionProb;
    }

    @Override
    public void learn(final Frame df, List<Double> weights, final String classColName) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < df.getRowCount(); i++) {
            if (df.getCol(classColName).isMissing(i)) {
                throw new IllegalArgumentException("Not allowed missing classes");
            }
        }

        int mcols2 = mcols;
        if (mcols2 > df.getColCount() - 1) {
            mcols2 = df.getColCount() - 1;
        }
        if (mcols2 < 1) {
            mcols2 = ((int) log2(df.getColCount())) + 1;
        }

        this.colSelector = new RandomColSelector(df, new ColRange(classColName), mcols2);

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
            final RandomTree tree = new RandomTree();
            tree.setColSelector(new RandomColSelector(df, new ColRange(classColName), mcols2));
            tree.setMinNodeSize(minNodeSize);
            trees.add(tree);
            final Frame bootstrap = StatSampling.randomBootstrap(df);
            bootstraps.add(bootstrap);
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    tree.learn(bootstrap, classColName);
                    double[] vi = tree.getVariableImportance();
                    for (int j = 0; j < vi.length; j++) {
                        giniImportanceValue[j] += vi[j];
                        giniImportanceCount[j]++;
                    }
                    return null;
                }
            });
        }
        try {
            es.invokeAll(tasks);
            es.shutdown();
        } catch (InterruptedException e) {
        }
        if (computeOob) {
            for (int i = 0; i < mtrees; i++) {
                addOob(df, bootstraps.get(i), trees.get(i));
            }
            oobError = computeOob(df);
        }
        learnTime = System.currentTimeMillis() - start;
    }

    private void setupOobContainer(Frame df) {
        oobFreq = new int[df.getSourceFrame().getRowCount()][dict.length];
    }

    private void addOob(Frame source, Frame bootstrap, Classifier tree) {
        Frame delta = RowFilters.delta(source, bootstrap);
        tree.predict(delta);
        Vector predict = tree.getPrediction();
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
    public void predict(final Frame df) {

        predict = new NominalVector(df.getRowCount(), dict);
        dist = Frames.newMatrixFrame(df.getRowCount(), dict);

        for (int m = 0; m < mtrees; m++) {
            Classifier tree = trees.get(m);
            tree.predict(df);
            for (int i = 0; i < df.getRowCount(); i++) {
                for (int j = 0; j < tree.getDistribution().getColCount(); j++) {
                    dist.setValue(i, j, dist.getValue(i, j) + tree.getDistribution().getValue(i, j));
                }
            }
        }

        // from freq to prob
        for (int i = 0; i < dist.getRowCount(); i++) {
            double total = 0;
            for (int j = 0; j < dist.getColCount(); j++) {
                total += dist.getValue(i, j);
            }
            for (int j = 0; j < dist.getColCount(); j++) {
                dist.setValue(i, j, dist.getValue(i, j) / total);
            }
            double max = 0;
            int col = 0;
            for (int j = 0; j < dist.getColCount(); j++) {
                if (max < dist.getValue(i, j)) {
                    max = dist.getValue(i, j);
                    col = j;
                }
            }
            predict.setLabel(i, dict[col]);
        }
    }

    @Override
    public NominalVector getPrediction() {
        return predict;
    }

    @Override
    public Frame getDistribution() {
        return dist;
    }


    public double getOobError() {
        return oobError;
    }

    @Override
    public void summary() {
        StringBuilder sb = new StringBuilder();
        summaryDetails(sb);
        summaryVariableImportance(sb);
        code(sb.toString());
    }

    private void summaryDetails(StringBuilder sb) {
        sb.append(String.format("\nRandomForest(mtrees=%d, mcols=%d)", mtrees, mcols));
        sb.append(String.format("\nTrain time %d millis", learnTime));
    }

    private void summaryVariableImportance(StringBuilder sb) {
        sb.append("\nGini variable importance: \n");
        Vector[] vectors = new Vector[2];
        vectors[0] = new NominalVector(giniImportanceNames.length - 1, giniImportanceNames);
        vectors[1] = new NumericVector(new double[giniImportanceNames.length - 1]);
        Frame f = new SolidFrame(giniImportanceNames.length - 1, vectors, new String[]{"varName", "meanDecrease"});
        int width = 0;
        int pos = 0;
        for (int i = 0; i < giniImportanceNames.length; i++) {
            String colName = giniImportanceNames[i];
            if (colName.equals(classColName)) continue;
            width = max(width, classColName.length() + 1);
            double decrease = 0;
            if (giniImportanceCount[i] != 0) {
                decrease = giniImportanceValue[i] / giniImportanceCount[i];
            }
            f.setLabel(pos, 0, colName);
            f.setValue(pos, 1, decrease);
            pos++;
        }
        f = RowFilters.sort(f, RowComparators.numericComparator(f.getCol(1), false));

        for (int i = 0; i < f.getRowCount(); i++) {
            sb.append(String.format("%" + width + "s : %10.4f\n", f.getLabel(i, 0), f.getValue(i, 1)));
        }
    }
}

