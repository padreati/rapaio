/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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
 *
 */

package rapaio.ml.classifier.tree;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import rapaio.core.RandomSource;
import rapaio.core.tools.DTable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.util.Tagged;

import java.io.Serializable;

/**
 * Impurity test implementation
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/15.
 */
public interface CTreeTest extends Tagged, Serializable {

    CTreeTest Ignore = new CTreeTest() {

        private static final long serialVersionUID = 2862814158096438654L;

        @Override
        public String name() {
            return "Ignore";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTreePurityFunction function) {
            return null;
        }
    };

    CTreeTest NumericRandom = new CTreeTest() {

        private static final long serialVersionUID = -4118895856520232216L;

        @Override
        public String name() {
            return "NumericRandom";
        }

        @Override
        public CTreeCandidate computeCandidate(
                CTree c, Frame df, Var w, String testName, String targetName,
                CTreePurityFunction function) {

            int split;
            while (true) {
                split = RandomSource.nextInt(df.rowCount());
                if (df.isMissing(split, testName)) {
                    continue;
                }
                break;
            }
            double testValue = df.value(split, testName);

            DTable dt = DTable.empty(DTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName), false);
            int misCount = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testName)) {
                    misCount++;
                    dt.update(0, df.index(i, targetName), w.value(i));
                }
                dt.update(df.value(i, testName) <= testValue ? 1 : 2, df.index(i, targetName), w.value(i));
            }

            double score = function.compute(dt);
            CTreeCandidate best = new CTreeCandidate(score, testName);
            best.addGroup(RowPredicate.numLessEqual(testName, testValue));
            best.addGroup(RowPredicate.numGreater(testName, testValue));

            return best;
        }
    };

    CTreeTest NumericBinary = new CTreeTest() {
        private static final long serialVersionUID = -2093990830002355963L;

        @Override
        public String name() {
            return "NumericBinary";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTreePurityFunction function) {

            int testNameIndex = df.varIndex(testName);
            int targetNameIndex = df.varIndex(targetName);
            DTable dt = DTable.empty(DTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName), false);

            int[] rows = new int[df.rowCount()];
            int len = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                boolean missing = df.isMissing(i, testNameIndex);
                int row = missing ? 0 : 2;
                if (!missing) {
                    rows[len++] = i;
                }
                dt.update(row, df.index(i, targetNameIndex), weights.value(i));
            }
            int misCount = df.rowCount() - len;

            double[] values = new double[df.rowCount()];
            for (int i = 0; i < df.rowCount(); i++) {
                values[i] = df.value(i, testNameIndex);
            }
            IntComparator comparator = (i, j) -> Double.compare(values[i], values[j]);
            IntArrays.quickSort(rows, comparator);

            CTreeCandidate best = null;
            double bestScore = 0.0;

            for (int i = 0; i < len; i++) {
                int row = rows[i];

                if (df.isMissing(row, testNameIndex)) continue;

                int index = df.index(row, targetNameIndex);
                double w = weights.value(row);
                dt.update(2, index, -w);
                dt.update(1, index, +w);

                if (i >= misCount + c.minCount() - 1 &&
                        i < df.rowCount() - c.minCount() &&
                        values[rows[i]] < values[rows[i + 1]]) {

                    double currentScore = function.compute(dt);
                    if (best != null) {
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp > 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    }
                    best = new CTreeCandidate(bestScore, testName);
                    double testValue = (values[rows[i]] + values[rows[i + 1]]) / 2.0;
                    best.addGroup(RowPredicate.numLessEqual(testName, testValue));
                    best.addGroup(RowPredicate.numGreater(testName, testValue));

                    bestScore = currentScore;
                }
            }
            return best;
        }
    };

    CTreeTest BinaryBinary = new CTreeTest() {

        private static final long serialVersionUID = 1771541941375729870L;

        @Override
        public String name() {
            return "BinaryBinary";
        }

        @Override
        public CTreeCandidate computeCandidate(
                CTree c, Frame df, Var w, String testName, String targetName,
                CTreePurityFunction function) {

            Var test = df.rvar(testName);
            Var target = df.rvar(targetName);
            DTable dt = DTable.fromCounts(test, target, false);
            if (!(dt.hasColsWithMinimumCount(c.minCount(), 2))) {
                return null;
            }

            CTreeCandidate best = new CTreeCandidate(function.compute(dt), testName);
            best.addGroup(RowPredicate.binEqual(testName, true));
            best.addGroup(RowPredicate.binEqual(testName, false));
            return best;

        }
    };

    CTreeTest NominalFull = new CTreeTest() {
        private static final long serialVersionUID = 2261155834044153945L;

        @Override
        public String name() {
            return "NominalFull";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTreePurityFunction function) {
            DTable counts = DTable.fromCounts(df, testName, targetName, false);
            if (!counts.hasColsWithMinimumCount(c.minCount(), 2)) {
                return null;
            }
            DTable dt = DTable.fromWeights(df, testName, targetName, weights, false);
            double value = function.compute(dt);
            CTreeCandidate candidate = new CTreeCandidate(value, testName);
            for (String label : df.completeLevels(testName)) {
                candidate.addGroup(RowPredicate.nomEqual(testName, label));
            }
            return candidate;
        }

    };

    CTreeTest NominalBinary = new CTreeTest() {

        private static final long serialVersionUID = -1257733788317891040L;

        @Override
        public String name() {
            return "NominalBinary";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTreePurityFunction function) {

            DTable counts = DTable.fromCounts(df, testName, targetName, false);
            if (!(counts.hasColsWithMinimumCount(c.minCount(), 2))) {
                return null;
            }

            CTreeCandidate best = null;
            double bestScore = 0.0;

            int[] termCount = new int[df.levels(testName).size()];
            for (int i = 0; i < df.rowCount(); i++) {
                termCount[df.index(i, testName)]++;
            }

            double[] rowCounts = counts.rowTotals();
            for (int i = 1; i < df.levels(testName).size(); i++) {
                if (rowCounts[i] < c.minCount())
                    continue;

                String testLabel = df.rvar(testName).levels().get(i);

                DTable dt = DTable.binaryFromWeights(df, testName, targetName, weights, testLabel, false);
                double currentScore = function.compute(dt);
                if (best != null) {
                    int comp = Double.compare(bestScore, currentScore);
                    if (comp > 0) continue;
                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                }
                best = new CTreeCandidate(currentScore, testName);
                best.addGroup(RowPredicate.nomEqual(testName, testLabel));
                best.addGroup(RowPredicate.nomNotEqual(testName, testLabel));
            }
            return best;
        }
    };

    CTreeCandidate computeCandidate(
            CTree c, Frame df, Var w,
            String testName, String targetName, CTreePurityFunction function);
}
