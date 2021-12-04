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

package rapaio.ml.model.tree.ctree;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rapaio.core.RandomSource;
import rapaio.core.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.model.tree.CTree;
import rapaio.ml.model.tree.RowPredicate;
import rapaio.util.collection.DoubleArrays;
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
            do {
                split = RandomSource.nextInt(df.rowCount());
            } while (df.isMissing(split, testName));

            double testValue = df.getDouble(split, testName);

            var dt = DensityTable.emptyByLabel(false, DensityTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName));
            int missingWeights = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testName)) {
                    missingWeights += w.getDouble(i);
                    continue;
                }
                dt.increment(df.getDouble(i, testName) <= testValue ? 0 : 1, df.getInt(i, targetName) - 1, w.getDouble(i));
            }

            double score = function.compute(dt);

            if (c.missingPenalty.get()) {
                double sum = w.dv().nansum();
                score = score * (sum - missingWeights) / sum;
            }

            Candidate best = new Candidate(score, testName);
            best.addGroup(RowPredicate.numLessEqual(testName, testValue));
            best.addGroup(RowPredicate.numGreater(testName, testValue));

            return best;
        }
    },
    NumericBinary {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, Purity function) {

            int testIndex = df.varIndex(testName);
            int targetIndex = df.varIndex(targetName);
            var dt = DensityTable.emptyByLabel(false, DensityTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName));

            int[] rows = new int[df.rowCount()];
            double missingWeight = 0;
            int len = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testIndex)) {
                    missingWeight += weights.getDouble(i);
                    continue;
                }
                rows[len++] = i;
                dt.increment(1, dt.colIndex().getIndex(df, targetName, i), weights.getDouble(i));
            }

            double[] values = df.rvar(testIndex).stream().mapToDouble().toArray();
            IntArrays.quickSort(rows, 0, len, (i, j) -> Double.compare(values[i], values[j]));

            double bestScore = Double.NaN;
            double bestTestValue = Double.NaN;

            for (int i = 0; i < len; i++) {

                int index = df.getInt(rows[i], targetIndex) - 1;

                double w = weights.getDouble(rows[i]);
                dt.increment(0, index, +w);
                dt.increment(1, index, -w);

                if (i >= c.minCount.get() && i < len - c.minCount.get() && values[rows[i]] < values[rows[i + 1]]) {
                    double currentScore = function.compute(dt);
                    if (Double.isNaN(bestScore) || bestScore < currentScore) {
                        bestScore = currentScore;
                        bestTestValue = (values[rows[i]] + values[rows[i + 1]]) / 2.0;
                    }
                }
            }

            if (Double.isNaN(bestScore)) {
                return null;
            }

            if (c.missingPenalty.get()) {
                double sum = weights.dv().nansum();
                bestScore = bestScore * (sum - missingWeight) / sum;
            }

            Candidate best = new Candidate(bestScore, testName);
            best.addGroup(RowPredicate.numLessEqual(testName, bestTestValue));
            best.addGroup(RowPredicate.numGreater(testName, bestTestValue));
            return best;
        }
    },
    Binary {
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

            double score = function.compute(dt);

            if (c.missingPenalty.get()) {
                double missingWeights = 0.0;
                for (int i = 0; i < test.size(); i++) {
                    if (test.isMissing(i)) {
                        missingWeights += w.getDouble(i);
                    }
                }
                double sum = w.dv().nansum();
                score = score * (sum - missingWeights) / sum;
            }

            Candidate best = new Candidate(score, testName);
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
            double score = function.compute(dt);
            Candidate candidate = new Candidate(score, testName);
            df.levels(testName).stream().skip(1).forEach(label -> candidate.addGroup(RowPredicate.nomEqual(testName, label)));
            return candidate;
        }
    },
    NominalBinary {
        @Override
        public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, Purity function) {

            var dtCounts = DensityTable.fromLevelCounts(false, df, testName, targetName);
            if (!(dtCounts.hasColsWithMinimumCount(c.minCount.get(), 2))) {
                return null;
            }

            var dtWeights = DensityTable.fromLevelWeights(false, df, testName, targetName, weights);

            double[] rowCounts = dtCounts.rowTotals();
            double totalRows = DoubleArrays.nanSum(rowCounts, 0, rowCounts.length);

            List<String> targetLevels = df.levels(targetName);
            targetLevels = targetLevels.subList(1, targetLevels.size());

            var dt = DensityTable.emptyByLabel(true, List.of("testLevel", "other"), targetLevels);
            double bestScore = Double.NaN;

            // prepare dt
            double[] colTotals = dtWeights.colTotals();
            for (int i = 0; i < colTotals.length; i++) {
                dt.increment(1, i, colTotals[i]);
            }

            if (targetLevels.size() == 2) {
                // binary classification, an optimization can be done
                Set<String> bestSet = null;

                // sort in descending order of the first class
                int[] levelIdx = IntArrays.newSeq(0, dtWeights.rowCount());
                IntArrays.quickSort(levelIdx, 0, levelIdx.length, (k1, k2) -> Double.compare(dtWeights.get(k2, 0), dtWeights.get(k1, 0)));

                int leftRowCounts = 0;

                Set<String> testLabels = new HashSet<>();
                for (int i : levelIdx) {

                    // update test labels

                    String testLabel = dtWeights.rowIndex().getValue(i);
                    testLabels.add(testLabel);

                    // update dt
                    for (int j = 0; j < targetLevels.size(); j++) {
                        dt.increment(0, j, dtWeights.get(i, j));
                        dt.increment(1, j, -dtWeights.get(i, j));
                    }

                    // check conditions on min count
                    leftRowCounts += rowCounts[i];
                    if (leftRowCounts < c.minCount.get() || totalRows - leftRowCounts < c.minCount.get()) {
                        continue;
                    }

                    double currentScore = function.compute(dt);
                    if (Double.isNaN(bestScore) || bestScore < currentScore) {
                        bestScore = currentScore;
                        bestSet = new HashSet<>(testLabels);
                    }
                }

                if (!Double.isNaN(bestScore)) {
                    Candidate best = new Candidate(bestScore, testName);
                    best.addGroup(RowPredicate.nomInSet(testName, bestSet));
                    best.addGroup(RowPredicate.nomNotInSet(testName, bestSet));
                    return best;
                }

            } else {

                String bestTest = null;

                // more than 2 target levels, we test each one separated from others
                // o avoid 2^tests-1 pairs

                for (int i = 0; i < dtWeights.rowIndex().size(); i++) {
                    if (rowCounts[i] < c.minCount.get()) {
                        continue;
                    }

                    for (int j = 0; j < targetLevels.size(); j++) {
                        dt.increment(0, j, +dtWeights.get(i, j));
                        dt.increment(1, j, -dtWeights.get(i, j));
                    }
                    double currentScore = function.compute(dt);
                    for (int j = 0; j < targetLevels.size(); j++) {
                        dt.increment(0, j, -dtWeights.get(i, j));
                        dt.increment(1, j, +dtWeights.get(i, j));
                    }

                    if (Double.isNaN(bestScore) || bestScore < currentScore) {
                        bestScore = currentScore;
                        bestTest = dtWeights.rowIndex().getValue(i);
                    }
                }

                if (!Double.isNaN(bestScore)) {
                    Candidate best = new Candidate(bestScore, testName);
                    best.addGroup(RowPredicate.nomEqual(testName, bestTest));
                    best.addGroup(RowPredicate.nomNotEqual(testName, bestTest));
                    return best;
                }
            }
            return null;
        }
    };

    public abstract Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, Purity function);
}
