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

import rapaio.core.RandomSource;
import rapaio.core.tools.DTable;
import rapaio.data.Frame;
import rapaio.data.IdxVar;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.filter.var.VFRefSort;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.util.Tagged;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

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
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTreePurityFunction function) {

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
            DTable dt = DTable.empty(DTable.NUMERIC_DEFAULT_LABELS, df.levels(targetName), false);
            int misCount = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                int row = (df.isMissing(i, testName)) ? 0 : 2;
                if (df.isMissing(i, testName)) misCount++;
                dt.update(row, df.index(i, targetName), weights.value(i));
            }

//            Var sort = new VFRefSort(RowComparators.numeric(df.rvar(testName), true)).fitApply(IdxVar.seq(df.rowCount()));
            Integer[] rows = new Integer[df.rowCount()];
            double[] values = new double[df.rowCount()];
            for (int i = 0; i < df.rowCount(); i++) {
                rows[i] = i;
                values[i] = df.value(i, testName);
            }

            Arrays.sort(rows, 0, df.rowCount(), Comparator.comparingDouble(o -> values[o]));

            CTreeCandidate best = null;
            double bestScore = 0.0;

            for (int i = 0; i < df.rowCount(); i++) {
                int row = rows[i];

                if (df.isMissing(row, testName)) continue;

                dt.update(2, df.index(row, targetName), -weights.value(row));
                dt.update(1, df.index(row, targetName), +weights.value(row));

                if (i >= misCount + c.minCount() - 1 &&
                        i < df.rowCount() - c.minCount() &&
                        df.value(rows[i], testName) < df.value(rows[i+1], testName)) {

                    double currentScore = function.compute(dt);
                    if (best != null) {
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp > 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    }
                    best = new CTreeCandidate(bestScore, testName);
                    double testValue = (df.value(rows[i], testName) + df.value(rows[i+1], testName)) / 2.0;
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
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTreePurityFunction function) {

            Var test = df.rvar(testName);
            Var target = df.rvar(targetName);
            DTable dt = DTable.fromCounts(test, target, false);
            if (!(dt.hasColsWithMinimumCount(c.minCount(), 2))) {
                return null;
            }

            CTreeCandidate best = new CTreeCandidate(function.compute(dt), testName);
            best.addGroup(RowPredicate.binEqual(testName, true));
            best.addGroup(RowPredicate.binNotEqual(testName, true));
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
            Var test = df.rvar(testName);
            Var target = df.rvar(targetName);

            if (!DTable.fromCounts(test, target, false).hasColsWithMinimumCount(c.minCount(), 2)) {
                return null;
            }

            DTable dt = DTable.fromWeights(test, target, weights, false);
            double value = function.compute(dt);

            CTreeCandidate candidate = new CTreeCandidate(value, testName);
            for (int i = 1; i < test.levels().length; i++) {
                final String label = test.levels()[i];
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
            Var test = df.rvar(testName);
            Var target = df.rvar(targetName);
            DTable counts = DTable.fromCounts(test, target, false);
            if (!(counts.hasColsWithMinimumCount(c.minCount(), 2))) {
                return null;
            }

            CTreeCandidate best = null;
            double bestScore = 0.0;

            int[] termCount = new int[test.levels().length];
            test.stream().forEach(s -> termCount[s.index()]++);

            double[] rowCounts = counts.rowTotals();
            for (int i = 1; i < test.levels().length; i++) {
                if (rowCounts[i] < c.minCount())
                    continue;

                String testLabel = df.rvar(testName).levels()[i];

                DTable dt = DTable.binaryFromWeights(test, target, weights, testLabel, false);
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

    CTreeCandidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTreePurityFunction function);
}
