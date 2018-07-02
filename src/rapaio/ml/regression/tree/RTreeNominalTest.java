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

package rapaio.ml.regression.tree;

import rapaio.core.stat.WeightedOnlineStat;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.common.predicate.RowPredicate;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Method which computes the best node candidate for a given nominal
 * variable. A candidate describes how a the current node can be split.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface RTreeNominalTest extends Serializable {

    /**
     * @return name of the nominal method
     */
    String name();

    /**
     * Computes a lis of candidates for the given test function,
     * dataset and weights, and test variable.
     *
     * @param tree          the original decision tree model
     * @param df            instances from the current node
     * @param w             weights of the instances from the current node
     * @param testVarName   test variable name
     * @param targetVarName target variable name
     * @param testFunction  test function used to compute the score
     * @return the best candidate
     */
    Optional<RTreeCandidate> computeCandidate(RTree tree, Frame df, Var w, String testVarName, String targetVarName, RTreePurityFunction testFunction);

    /**
     * Ignore nominal variables
     */
    RTreeNominalTest IGNORE = new RTreeNominalTest() {

        private static final long serialVersionUID = 7275580448899976553L;

        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public Optional<RTreeCandidate> computeCandidate(RTree tree, Frame df, Var w, String testVarName, String targetVarName, RTreePurityFunction testFunction) {
            return Optional.empty();
        }
    };

    /**
     * Builds one node for each label of the test variable, if at least
     * two of them have enough instances, empty list otherwise.
     */
    RTreeNominalTest FULL = new RTreeNominalTest() {

        private static final long serialVersionUID = 2733570883914611103L;

        @Override
        public String name() {
            return "FULL";
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
                int index = df.index(i, testNameIndex);
                onlineStats[index - 1].update(df.value(i, targetNameIndex), w.value(i));
            }

            // check to see if we have enough instances in at least 2 child nodes
            int validCount = 0;
            for (int i = 0; i < len; i++) {
                if (onlineStats[i].count() >= tree.minCount()) {
                    validCount++;
                }
            }
            if (validCount <= 1) {
                return Optional.empty();
            }

            // make the payload
            WeightedOnlineStat wos = WeightedOnlineStat.from(onlineStats);
            RTreeTestPayload p = new RTreeTestPayload(len);

            p.totalWeight = wos.weightSum();
            p.totalVar = wos.variance();

            for (int i = 0; i < len; i++) {
                p.splitWeight[i] = onlineStats[i].weightSum();
                p.splitVar[i] = onlineStats[i].variance();
            }

            // compute the candidate score

            double value = tree.testFunction().computeTestValue(p);
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
    RTreeNominalTest BINARY = new RTreeNominalTest() {

        private static final long serialVersionUID = -4703727362952157041L;

        @Override
        public String name() {
            return "BINARY";
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
                int index = df.index(i, testNameIndex);
                onlineStats[index - 1].update(df.value(i, targetNameIndex), w.value(i));
            }

            WeightedOnlineStat wos = WeightedOnlineStat.from(onlineStats);

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

                double value = tree.testFunction().computeTestValue(p);

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