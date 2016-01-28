/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Index;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.filter.var.VFRefSort;
import rapaio.sys.WS;
import rapaio.util.Tagged;

import java.io.Serializable;

/**
 * Impurity test implementation
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/15.
 */
public interface CTreePurityTest extends Tagged, Serializable {

    CTreePurityTest Ignore = new CTreePurityTest() {

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
    CTreePurityTest NumericBinary = new CTreePurityTest() {
        private static final long serialVersionUID = -2093990830002355963L;

        @Override
        public String name() {
            return "NumericBinary";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTreePurityFunction function) {
            Var test = df.var(testName);
            Var target = df.var(targetName);

            DTable dt = DTable.newEmpty(DTable.NUMERIC_DEFAULT_LABELS, target.levels(), false);
            int misCount = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                int row = (test.missing(i)) ? 0 : 2;
                if (test.missing(i)) misCount++;
                dt.update(row, target.index(i), weights.value(i));
            }

            Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.seq(df.rowCount()));

            CTreeCandidate best = null;
            double bestScore = 0.0;

            for (int i = 0; i < df.rowCount(); i++) {
                int row = sort.index(i);

                if (test.missing(row)) continue;

                dt.update(2, target.index(row), -weights.value(row));
                dt.update(1, target.index(row), +weights.value(row));

                if (i >= misCount + c.minCount() - 1 &&
                        i < df.rowCount() - c.minCount() &&
                        test.value(sort.index(i)) < test.value(sort.index(i + 1))) {

                    double currentScore = function.compute(dt);
                    if (best != null) {
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp > 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    }
                    best = new CTreeCandidate(bestScore, testName);
                    double testValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                    best.addGroup(
                            String.format("%s <= %s", testName, WS.formatFlex(testValue)),
                            spot -> !spot.missing(testName) && spot.value(testName) <= testValue);
                    best.addGroup(
                            String.format("%s > %s", testName, WS.formatFlex(testValue)),
                            spot -> !spot.missing(testName) && spot.value(testName) > testValue);

                    bestScore = currentScore;
                }
            }
            return best;
        }
    };
    CTreePurityTest BinaryBinary = new CTreePurityTest() {

        private static final long serialVersionUID = 1771541941375729870L;

        @Override
        public String name() {
            return "BinaryBinary";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTreePurityFunction function) {

            Var test = df.var(testName);
            Var target = df.var(targetName);
            DTable dt = DTable.fromCounts(test, target, false);
            if (!(dt.hasColsWithMinimumCount(c.minCount(), 2))) {
                return null;
            }

            CTreeCandidate best = new CTreeCandidate(function.compute(dt), testName);
            best.addGroup(testName + " == 1", spot -> spot.binary(testName));
            best.addGroup(testName + " != 1", spot -> !spot.binary(testName));
            return best;

        }
    };
    CTreePurityTest NominalFull = new CTreePurityTest() {
        private static final long serialVersionUID = 2261155834044153945L;

        @Override
        public String name() {
            return "NominalFull";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTreePurityFunction function) {
            Var test = df.var(testName);
            Var target = df.var(targetName);

            if (!DTable.fromCounts(test, target, false).hasColsWithMinimumCount(c.minCount(), 2)) {
                return null;
            }

            DTable dt = DTable.fromWeights(test, target, weights, false);
            double value = function.compute(dt);

            CTreeCandidate candidate = new CTreeCandidate(value, testName);
            for (int i = 1; i < test.levels().length; i++) {
                final String label = test.levels()[i];
                candidate.addGroup(
                        String.format("%s == %s", testName, label),
                        spot -> !spot.missing(testName) && spot.label(testName).equals(label));
            }
            return candidate;
        }

    };
    CTreePurityTest NominalBinary = new CTreePurityTest() {

        private static final long serialVersionUID = -1257733788317891040L;

        @Override
        public String name() {
            return "Nominal_Binary";
        }

        @Override
        public CTreeCandidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTreePurityFunction function) {
            Var test = df.var(testName);
            Var target = df.var(targetName);
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

                String testLabel = df.var(testName).levels()[i];

                DTable dt = DTable.newBinaryFromWeights(test, target, weights, testLabel, false);
                double currentScore = function.compute(dt);
                if (best != null) {
                    int comp = Double.compare(bestScore, currentScore);
                    if (comp > 0) continue;
                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                }
                best = new CTreeCandidate(currentScore, testName);
                best.addGroup(testName + " == " + testLabel, spot -> spot.label(testName).equals(testLabel));
                best.addGroup(testName + " != " + testLabel, spot -> !spot.label(testName).equals(testLabel));
            }
            return best;
        }
    };

    CTreeCandidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTreePurityFunction function);
}
