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

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.core.sample.StatSampling;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;
import rapaio.ml.colselect.ColSelector;
import rapaio.ml.colselect.RandomColSelector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static rapaio.core.MathBase.log2;
import static rapaio.core.MathBase.max;
import static rapaio.workspace.Workspace.code;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomForestClassifier extends AbstractClassifier<RandomForestClassifier> {
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
    Nominal predict;
    Frame dist;
    long learnTime = 0;
    int minNodeSize = 1;
    double numericSelectionProb = 1.;

    @Override
    public RandomForestClassifier newInstance() {
        RandomForestClassifier rf = new RandomForestClassifier();
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

    public RandomForestClassifier setMtrees(int mtrees) {
        this.mtrees = mtrees;
        return this;
    }

    public int getMcols() {
        return mcols;
    }

    public RandomForestClassifier setMcols(int mcols) {
        this.mcols = mcols;
        return this;
    }

    public boolean getComputeOob() {
        return computeOob;
    }

    public RandomForestClassifier setComputeOob(boolean computeOob) {
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
    public void learn(final Frame df, List<Double> weights, final String targetColName) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.col(targetColName).isMissing(i)) {
                throw new IllegalArgumentException("Not allowed missing classes");
            }
        }

        int mcols2 = mcols;
        if (mcols2 > df.colCount() - 1) {
            mcols2 = df.colCount() - 1;
        }
        if (mcols2 < 1) {
            mcols2 = ((int) log2(df.colCount())) + 1;
        }

        this.colSelector = new RandomColSelector(df, new ColRange(targetColName), mcols2);

        this.classColName = targetColName;
        this.dict = df.col(targetColName).getDictionary();
        this.giniImportanceNames = df.colNames();
        this.giniImportanceValue = new double[df.colNames().length];
        this.giniImportanceCount = new double[df.colNames().length];
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
            tree.setColSelector(new RandomColSelector(df, new ColRange(targetColName), mcols2));
            tree.setMinNodeSize(minNodeSize);
            trees.add(tree);
            final Frame bootstrap = StatSampling.randomBootstrap(df);
            bootstraps.add(bootstrap);
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    tree.learn(bootstrap, targetColName);
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
        oobFreq = new int[df.sourceFrame().rowCount()][dict.length];
    }

    private void addOob(Frame source, Frame bootstrap, Classifier tree) {
        Frame delta = BaseFilters.delta(source, bootstrap);
        tree.predict(delta);
        Vector predict = tree.prediction();
        for (int i = 0; i < delta.rowCount(); i++) {
            int rowId = delta.rowId(i);
            oobFreq[rowId][predict.getIndex(i)]++;
        }
    }

    private double computeOob(Frame df) {
        double total = 0;
        double count = 0;
        int[] indexes = new int[dict.length];
        for (int i = 0; i < df.rowCount(); i++) {
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
                if (next != df.sourceFrame().col(classColName).getIndex(i)) {
                    total += 1.;
                }
            }
        }
        return total / count;
    }

    @Override
    public void predict(final Frame df) {

        predict = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int m = 0; m < mtrees; m++) {
            Classifier tree = trees.get(m);
            tree.predict(df);
            for (int i = 0; i < df.rowCount(); i++) {
                for (int j = 0; j < tree.distribution().colCount(); j++) {
                    dist.setValue(i, j, dist.value(i, j) + tree.distribution().value(i, j));
                }
            }
        }

        // from freq to prob
        for (int i = 0; i < dist.rowCount(); i++) {
            double total = 0;
            for (int j = 0; j < dist.colCount(); j++) {
                total += dist.value(i, j);
            }
            for (int j = 0; j < dist.colCount(); j++) {
                dist.setValue(i, j, dist.value(i, j) / total);
            }
            double max = 0;
            int col = 0;
            for (int j = 0; j < dist.colCount(); j++) {
                if (max < dist.value(i, j)) {
                    max = dist.value(i, j);
                    col = j;
                }
            }
            predict.setLabel(i, dict[col]);
        }
    }

    @Override
    public Nominal prediction() {
        return predict;
    }

    @Override
    public Frame distribution() {
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
        sb.append(String.format("\nRandomForestClassifier(mtrees=%d, mcols=%d)", mtrees, mcols));
        sb.append(String.format("\nTrain time %d millis", learnTime));
    }

    private void summaryVariableImportance(StringBuilder sb) {
        sb.append("\nGini variable importance: \n");
        Vector[] vectors = new Vector[2];
        vectors[0] = new Nominal(giniImportanceNames.length - 1, giniImportanceNames);
        vectors[1] = new Numeric(new double[giniImportanceNames.length - 1]);
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
        f = BaseFilters.sort(f, RowComparators.numericComparator(f.col(1), false));

        for (int i = 0; i < f.rowCount(); i++) {
            sb.append(String.format("%" + width + "s : %10.4f\n", f.label(i, 0), f.value(i, 1)));
        }
    }
}

