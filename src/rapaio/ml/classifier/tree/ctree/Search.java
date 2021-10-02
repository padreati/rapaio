/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.classifier.tree.ctree;

import java.io.Serializable;
import java.util.List;

import rapaio.core.RandomSource;
import rapaio.core.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.experiment.ml.common.predicate.RowPredicate;
import rapaio.ml.classifier.tree.CTree;
import rapaio.util.IntComparator;
import rapaio.util.collection.IntArrays;

/**
 * Impurity test implementation
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/15.
 */
public enum Search implements Serializable {

    Ignore {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, Purity function) {
            return null;
        }
    },
    NumericRandom {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, Purity function) {

            int split;
            while (true) {
                split = RandomSource.nextInt(df.rowCount());
                if (df.isMissing(split, testName)) {
                    continue;
                }
                break;
            }
            double testValue = df.getDouble(split, testName);

            var dt = DensityTable.emptyByLabel(false, DensityTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName));
            int misCount = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testName)) {
                    misCount++;
                    continue;
                }
                dt.increment(df.getDouble(i, testName) <= testValue ? 0 : 1, df.getInt(i, targetName) - 1, w.getDouble(i));
            }

            double score = function.compute(dt);
            Candidate best = new Candidate(score, testName);
            best.addGroup(RowPredicate.numLessEqual(testName, testValue));
            best.addGroup(RowPredicate.numGreater(testName, testValue));

            return best;
        }
    },
    NumericBinary {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, Purity function) {

            int testNameIndex = df.varIndex(testName);
            int targetNameIndex = df.varIndex(targetName);
            var dt = DensityTable.emptyByLabel(false, DensityTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName));

            int[] rows = new int[df.rowCount()];
            int len = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                if (!df.isMissing(i, testNameIndex)) {
                    rows[len++] = i;
                    dt.increment(1, dt.colIndex().getIndex(df, targetName, i), weights.getDouble(i));
                }
            }
            // TODO: Revise the implication of missing records
            int misCount = df.rowCount() - len;

            double[] values = new double[df.rowCount()];
            for (int i = 0; i < df.rowCount(); i++) {
                values[i] = df.getDouble(i, testNameIndex);
            }
            IntComparator comparator = (i, j) -> Double.compare(values[i], values[j]);
            IntArrays.quickSort(rows, 0, len, comparator);

            Candidate best = null;
            double bestScore = 0.0;

            for (int i = 0; i < len; i++) {
                int row = rows[i];

                if (df.isMissing(row, testNameIndex)) continue;

                int index = df.getInt(row, targetNameIndex) - 1;
                double w = weights.getDouble(row);
                dt.increment(1, index, -w);
                dt.increment(0, index, +w);

                if (i + 1 >= c.minCount.get() &&
                        i < len - c.minCount.get() &&
                        values[rows[i]] < values[rows[i + 1]]) {

                    double currentScore = function.compute(dt);
                    if (best != null) {
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp > 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    }
                    best = new Candidate(bestScore, testName);
                    double testValue = (values[rows[i]] + values[rows[i + 1]]) / 2.0;
                    best.addGroup(RowPredicate.numLessEqual(testName, testValue));
                    best.addGroup(RowPredicate.numGreater(testName, testValue));

                    bestScore = currentScore;
                }
            }
            return best;
        }
    },
    BinaryBinary {
        @Override
        public Candidate computeCandidate(
                CTree c, Frame df, Var w, String testName, String targetName,
                Purity function) {

            Var test = df.rvar(testName);
            Var target = df.rvar(targetName);
            var dt = DensityTable.fromLevelCounts(false, test, target);
            if (!(dt.hasColsWithMinimumCount(c.minCount.get(), 2))) {
                return null;
            }

            Candidate best = new Candidate(function.compute(dt), testName);
            best.addGroup(RowPredicate.binEqual(testName, true));
            best.addGroup(RowPredicate.binEqual(testName, false));
            return best;
        }
    },
    NominalFull {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, Purity function) {
            var counts = DensityTable.fromLevelCounts(false, df, testName, targetName);
            if (!counts.hasColsWithMinimumCount(c.minCount.get(), 2)) {
                return null;
            }
            var dt = DensityTable.fromLevelWeights(false, df, testName, targetName, weights);
            double value = function.compute(dt);
            Candidate candidate = new Candidate(value, testName);
            df.levels(testName).stream().skip(1).forEach(label -> candidate.addGroup(RowPredicate.nomEqual(testName, label)));
            return candidate;
        }
    },
    NominalBinary {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, Purity function) {

            var tableCounts = DensityTable.fromLevelCounts(false, df, testName, targetName);
            if (!(tableCounts.hasColsWithMinimumCount(c.minCount.get(), 2))) {
                return null;
            }

            var tableWeights = DensityTable.fromLevelWeights(false, df, testName, targetName, weights);
            var colTotalsWeights = tableWeights.colTotals();


            double[] rowCounts = tableCounts.rowTotals();
            List<String> targetLevels = df.levels(targetName);
            targetLevels = targetLevels.subList(1, targetLevels.size());

            var dt = DensityTable.emptyByLabel(true, List.of("testLevel", "other"), targetLevels);

            Candidate best = null;
            double bestScore = 0.0;

            for (int i = 0; i < tableWeights.rowIndex().size(); i++) {
                if (rowCounts[i] < c.minCount.get()) {
                    continue;
                }
                String testLabel = tableWeights.rowIndex().getValue(i);
                for (int j = 0; j < targetLevels.size(); j++) {
                    dt.increment(0, j, tableWeights.get(i, j));
                    dt.increment(1, j, colTotalsWeights[j] - tableWeights.get(i, j));
                }

                double currentScore = function.compute(dt);
                if (best != null) {
                    int comp = Double.compare(bestScore, currentScore);
                    if (comp > 0) continue;
                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                }
                best = new Candidate(currentScore, testName);
                best.addGroup(RowPredicate.nomEqual(testName, testLabel));
                best.addGroup(RowPredicate.nomNotEqual(testName, testLabel));
            }
            return best;
        }
    };

    public abstract Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, Purity function);
}
