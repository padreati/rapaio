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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.ml.regression.tree.rtree;

import rapaio.core.RandomSource;
import rapaio.core.stat.WeightedOnlineStat;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.experiment.ml.common.predicate.RowPredicate;
import rapaio.ml.regression.tree.RTree;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Computes the selection of the best candidate node for a given test and target
 * variable. The method is universal of variable types, the name of the
 * method describes with which variable representation works to implement
 * the selection of the best candidate.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/20/19.
 */
public interface RTreeTest extends Serializable {

    /**
     * @return name of the test method
     */
    String name();

    /**
     * Computes a list of candidates for the given test variable and target
     * and selects the best one.
     *
     * @param tree          tree model
     * @param df            instances from the current node
     * @param w             weights of the instances from the current node
     * @param testVarName   test variable name
     * @param targetVarName target variable name
     * @param testFunction  test function used to compute the score
     * @return the best candidate
     */
    Optional<RTreeCandidate> computeCandidate(RTree tree, Frame df, Var w, String testVarName, String targetVarName, RTreePurityFunction testFunction);

    /**
     * This test deliberately does not compute any reliable candidate which,
     * as a consequence, determines the ignoring of the variable.
     */
    RTreeTest Ignore = new RTreeTest() {
        private static final long serialVersionUID = -5982576265221513285L;

        @Override
        public String name() {
            return "Ignore";
        }

        @Override
        public Optional<RTreeCandidate> computeCandidate(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreePurityFunction function) {
            return Optional.empty();
        }
    };

    /**
     * Selects the best candidate which splits instances in two child nodes
     * based on a test defined using double value representation.
     */
    RTreeTest NumericBinary = new RTreeTest() {
        private static final long serialVersionUID = 7573765926645246027L;

        @Override
        public String name() {
            return "NumericBinary";
        }

        @Override
        public Optional<RTreeCandidate> computeCandidate(RTree c, Frame df, Var weights, String testName, String targetName, RTreePurityFunction function) {

            int testIndex = df.varIndex(testName);
            int targetIndex = df.varIndex(targetName);

            int[] rows = df.rvar(testIndex).op().sortedCompleteRows(true);

            double[] leftWeight = new double[rows.length];
            double[] leftVar = new double[rows.length];
            double[] rightWeight = new double[rows.length];
            double[] rightVar = new double[rows.length];

            WeightedOnlineStat so = WeightedOnlineStat.empty();

            so.update(df.getDouble(rows[0], targetIndex), weights.getDouble(rows[0]));
            leftWeight[0] = weights.getDouble(rows[0]);
            leftVar[0] = 0;
            for (int i = 1; i < rows.length; i++) {
                so.update(df.getDouble(rows[i], targetIndex), weights.getDouble(rows[i]));
                leftWeight[i] = weights.getDouble(rows[i]) + leftWeight[i - 1];
                leftVar[i] = so.variance();
            }

            so = WeightedOnlineStat.empty();
            so.update(df.getDouble(rows[rows.length - 1], targetIndex), weights.getDouble(rows[rows.length - 1]));
            rightWeight[rows.length - 1] = weights.getDouble(rows[rows.length - 1]);
            rightVar[rows.length - 1] = 0;
            for (int i = rows.length - 2; i >= 0; i--) {
                so.update(df.getDouble(rows[i], targetIndex), weights.getDouble(rows[i]));
                rightWeight[i] = weights.getDouble(rows[i]) + rightWeight[i + 1];
                rightVar[i] = so.variance();
            }

            RTreeCandidate best = null;
            double bestScore = -1e100;

            RTreeTestPayload p = new RTreeTestPayload(2);

            p.totalVar = rightVar[0];
            p.totalWeight = rightWeight[0];

            for (int i = c.minCount(); i < rows.length - c.minCount() - 1; i++) {
                if (df.getDouble(rows[i], testIndex) == df.getDouble(rows[i + 1], testIndex)) continue;

                p.splitVar[0] = leftVar[i];
                p.splitWeight[0] = leftWeight[i];
                p.splitVar[1] = rightVar[i + 1];
                p.splitWeight[1] = rightWeight[i + 1];

                double value = c.purityFunction().computeTestValue(p);
                if (value < bestScore) {
                    continue;
                }
                if (value == bestScore && RandomSource.nextDouble() < 0.5) {
                    continue;
                }
                bestScore = value;
                best = new RTreeCandidate(value, testName);

                double testValue = (df.getDouble(rows[i], testName) + df.getDouble(rows[i + 1], testName)) / 2.0;
                best.addGroup(RowPredicate.numLessEqual(testName, testValue));
                best.addGroup(RowPredicate.numGreater(testName, testValue));
            }
            return (best != null) ? Optional.of(best) : Optional.empty();
        }
    };

    /**
     * Builds one node for each label of the test variable, if at least
     * two of them have enough instances, empty list otherwise.
     */
    RTreeTest NominalFull = new RTreeTest() {
        private static final long serialVersionUID = 2733570883914611103L;

        @Override
        public String name() {
            return "NominalFull";
        }

        @Override
        public Optional<RTreeCandidate> computeCandidate(RTree tree, Frame df, Var w, String testName, String targetName, RTreePurityFunction testFunction) {

            int testNameIndex = df.varIndex(testName);
            int targetNameIndex = df.varIndex(targetName);

            // we ignore the missing data points, thus we need only non missing levels
            List<String> testLevels = df.levels(testName);
            int len = testLevels.size() - 1;
            WeightedOnlineStat[] onlineStats = new WeightedOnlineStat[len];

            // initialize
            for (int i = 0; i < len; i++) {
                onlineStats[i] = new WeightedOnlineStat();
            }

            // compute weighted statistics

            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testNameIndex))
                    continue;
                int index = df.getInt(i, testNameIndex);
                onlineStats[index - 1].update(df.getDouble(i, targetNameIndex), w.getDouble(i));
            }

            // check to see if we have enough instances in all child nodes
            int validCount = 0;
            for (int i = 0; i < len; i++) {
                if (onlineStats[i].count() >= tree.minCount()) {
                    validCount++;
                }
            }
            if (validCount != len) {
                return Optional.empty();
            }

            // make the payload
            WeightedOnlineStat wos = WeightedOnlineStat.of(onlineStats);
            RTreeTestPayload p = new RTreeTestPayload(len);

            p.totalWeight = wos.weightSum();
            p.totalVar = wos.variance();

            for (int i = 0; i < len; i++) {
                p.splitWeight[i] = onlineStats[i].weightSum();
                p.splitVar[i] = onlineStats[i].variance();
            }

            // compute the candidate score

            double value = tree.purityFunction().computeTestValue(p);
            RTreeCandidate candidate = new RTreeCandidate(value, testName);
            for (int i = 0; i < len; i++) {
                String label = testLevels.get(i + 1);
                candidate.addGroup(RowPredicate.nomEqual(testName, label));
            }
            return Optional.of(candidate);
        }
    };

    /**
     * Builds one candidate for each label of the test nominal
     * variable against all other labels, in the case when
     * for selected labels there are instances
     */
    RTreeTest NominalBinary = new RTreeTest() {
        private static final long serialVersionUID = -4703727362952157041L;

        @Override
        public String name() {
            return "NominalBinary";
        }

        @Override
        public Optional<RTreeCandidate> computeCandidate(RTree tree, Frame df, Var w, String testName, String targetName, RTreePurityFunction testFunction) {

            int testNameIndex = df.varIndex(testName);
            int targetIndex = df.varIndex(targetName);

            // we ignore the missing data points, thus we need only non missing levels
            List<String> testLevels = df.levels(testName);
            int len = testLevels.size() - 1;

            WeightedOnlineStat[] onlineStats = IntStream.range(0, len)
                    .mapToObj(i -> new WeightedOnlineStat())
                    .toArray(WeightedOnlineStat[]::new);

            // compute weighted statistics
            for (int i = 0; i < df.rowCount(); i++) {
                if (df.isMissing(i, testNameIndex))
                    continue;
                int index = df.getInt(i, testNameIndex);
                onlineStats[index - 1].update(df.getDouble(i, targetIndex), w.getDouble(i));
            }

            WeightedOnlineStat wos = WeightedOnlineStat.of(onlineStats);

            RTreeCandidate best = null;
            double bestScore = Double.NaN;

            // for each class compute a possible split
            for (int i = 0; i < len; i++) {

                WeightedOnlineStat wosTest = onlineStats[i];

                // check if we have enough instances
                if (wosTest.count() < tree.minCount() || wos.count() - wosTest.count() < tree.minCount()) {
                    continue;
                }

                // compute remaining stats
                WeightedOnlineStat wosRemain = WeightedOnlineStat.empty();
                for (int j = 0; j < len; j++) {
                    if (j != i)
                        wosRemain.update(onlineStats[j]);
                }

                // build payload to compute score
                RTreeTestPayload p = new RTreeTestPayload(2);
                p.totalVar = wos.variance();
                p.totalWeight = wos.weightSum();

                // payload for current node
                p.splitVar[0] = wosTest.variance();
                p.splitWeight[0] = wosTest.weightSum();

                // payload for the others
                p.splitVar[1] = wosRemain.variance();
                p.splitWeight[1] = wosRemain.weightSum();

                double value = tree.purityFunction().computeTestValue(p);

                if (Double.isNaN(bestScore) || value > bestScore) {
                    bestScore = value;
                    best = new RTreeCandidate(value, testName);
                    best.addGroup(RowPredicate.nomEqual(testName, testLevels.get(i + 1)));
                    best.addGroup(RowPredicate.nomNotEqual(testName, testLevels.get(i + 1)));
                }
            }
            return (best == null) ? Optional.empty() : Optional.of(best);
        }
    };
}
