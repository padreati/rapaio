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

import rapaio.core.CoreTools;
import rapaio.core.stat.OnlineStat;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Method which computes the best node candidate for a given nominal
 * variable. A candidate describes how a the current node can be split.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface RTreeNominalMethod extends Serializable {

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
    Optional<RTree.Candidate> computeCandidate(RTree tree,
                                               Frame df, Var w,
                                               String testVarName, String targetVarName,
                                               RTreeTestFunction testFunction);

    /**
     * Ignore nominal variables
     */
    RTreeNominalMethod IGNORE = new RTreeNominalMethod() {

        private static final long serialVersionUID = 7275580448899976553L;

        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public Optional<RTree.Candidate> computeCandidate(
                RTree tree, Frame df, Var w,
                String testVarName, String targetVarName, RTreeTestFunction testFunction) {
            return Optional.empty();
        }
    };

    /**
     * Builds one node for each label of the test variable, if at least
     * two of them have enough instances, empty list otherwise.
     */
    RTreeNominalMethod FULL = new RTreeNominalMethod() {

        private static final long serialVersionUID = 2733570883914611103L;

        @Override
        public String name() {
            return "FULL";
        }

        @Override
        public Optional<RTree.Candidate> computeCandidate(RTree tree, Frame dfOld, Var weightsOld, String testVarName, String targetVarName, RTreeTestFunction testFunction) {

            List<RTree.Candidate> result = new ArrayList<>();
            Mapping cleanMapping = dfOld.stream().filter(s -> !s.isMissing(testVarName)).collectMapping();
            Frame df = dfOld.mapRows(cleanMapping);
            Var testVar = df.var(testVarName);
            Var targetVar = df.var(targetVarName);
            Var weights = weightsOld.mapRows(cleanMapping);

            DVector dvWeights = DVector.fromWeights(false, testVar, weights);
            DVector dvCount = DVector.fromCount(false, testVar);

            // check to see if we have enough instances in at least 2 child nodes
            if (dvCount.countValues(x -> x >= tree.minCount) <= 1)
                return Optional.empty();

            // make the payload
            RTreeTestPayload p = new RTreeTestPayload(testVar.levels().length - 1);
            p.totalVar = CoreTools.variance(targetVar).value();

            for (int i = 1; i < testVar.levels().length; i++) {
                p.splitWeight[i - 1] = dvWeights.get(i - 1);
                String label = testVar.levels()[i];
                p.splitVar[i - 1] = CoreTools.variance(df.stream().filter(s -> s.getLabel(testVarName).equals(label)).toMappedFrame().var(targetVarName)).value();
            }
            double value = tree.function.computeTestValue(p);
            RTree.Candidate candidate = new RTree.Candidate(value, testVarName);
            for (int i = 1; i < testVar.levels().length; i++) {
                String label = testVar.levels()[i];
                candidate.addGroup(testVarName + " == " + label, spot -> spot.getLabel(testVarName).equals(label));
            }
            return Optional.of(candidate);
        }
    };

    /**
     * Builds one candidate for each label of the test nominal
     * variable against all other labels, in the case when
     * for selected labels there are instances
     */
    RTreeNominalMethod BINARY = new RTreeNominalMethod() {

        private static final long serialVersionUID = -4703727362952157041L;

        @Override
        public String name() {
            return "BINARY";
        }

        @Override
        public Optional<RTree.Candidate> computeCandidate(RTree tree, Frame dfOld, Var weightsOld, String testVarName, String targetVarName, RTreeTestFunction testFunction) {

            Mapping cleanMapping = dfOld.stream().filter(s -> !s.isMissing(testVarName)).collectMapping();
            Frame df = dfOld.mapRows(cleanMapping);
            Var testVar = df.var(testVarName);
            Var targetVar = df.var(targetVarName);
            Var weights = weightsOld.mapRows(cleanMapping);

            DVector dvWeights = DVector.fromWeights(false, testVar, weights);
            DVector dvCount = DVector.fromCount(false, testVar);

            // compute online statistics for all level slices
            OnlineStat[] os = new OnlineStat[testVar.levels().length - 1];
            for (int i = 0; i < testVar.levels().length - 1; i++) {
                os[i] = OnlineStat.empty();
            }
            for (int i = 0; i < testVar.rowCount(); i++) {
                int index = testVar.index(i);
                if (index == 0)
                    continue;
                os[index - 1].update(targetVar.value(i));
            }

            double totalVar = CoreTools.variance(targetVar).value();

            RTree.Candidate best = null;
            double bestScore = Double.NaN;

            for (int i = 1; i < testVar.levels().length; i++) {
                String testLabel = testVar.levels()[i];

                // check to see if we have enough values

                if (dvCount.get(i) < tree.minCount || df.rowCount() - dvCount.get(i) < tree.minCount)
                    continue;

                OnlineStat osSelect = os[i - 1];
                OnlineStat osOther = OnlineStat.empty();

                for (int j = 1; j < testVar.levels().length; j++) {
                    if (i == j)
                        continue;
                    osOther.update(os[j - 1]);
                }

                RTreeTestPayload p = new RTreeTestPayload(2);
                p.totalVar = totalVar;

                // payload for current node

                p.splitWeight[0] = dvWeights.get(i);
                p.splitVar[0] = osSelect.variance();

                // payload for the others

                p.splitWeight[1] = dvWeights.sum() - dvWeights.get(i);
                p.splitVar[1] = osOther.variance();

                double value = tree.function.computeTestValue(p);
                if (Double.isNaN(bestScore) || value > bestScore) {
                    bestScore = value;
                    best = new RTree.Candidate(value, testVarName);
                    best.addGroup(testVarName + " == " + testLabel,
                            spot -> !spot.isMissing(testVarName) && spot.getLabel(testVarName).equals(testLabel));
                    best.addGroup(testVarName + " != " + testLabel,
                            spot -> !spot.isMissing(testVarName) && !spot.getLabel(testVarName).equals(testLabel));
                }
            }
            return (best == null) ? Optional.empty() : Optional.of(best);
        }
    };
}