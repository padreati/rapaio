/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.filter.Filters;
import rapaio.data.stream.VSpot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Method which computes the best node candidate for a given numeric
 * variable. A candidate describes how a the current node can be split.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface RTreeNumericMethod extends Serializable {

    /**
     * @return name of the numeric method
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
     * @return a list of candidates in the descending order of score
     */
    Optional<RTree.Candidate> computeCandidate(RTree tree, Frame df, Var w,
                                               String testVarName, String targetVarName,
                                               RTreeTestFunction testFunction);

    /**
     * Ignore all numeric variables and produces no candidates.
     */
    RTreeNumericMethod IGNORE = new RTreeNumericMethod() {
        private static final long serialVersionUID = -5982576265221513285L;

        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public Optional<RTree.Candidate> computeCandidate(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreeTestFunction function) {
            return Optional.empty();
        }
    };

    RTreeNumericMethod BINARY = new RTreeNumericMethod() {
        @Override
        public String name() {
            return "BINARY";
        }

        @Override
        public Optional<RTree.Candidate> computeCandidate(RTree c, Frame dfOld, Var weights, String testVarName, String targetVarName, RTreeTestFunction function) {

            Frame df = Filters.refSort(dfOld, dfOld.getVar(testVarName).refComparator());
            Mapping cleanMapping = Mapping.wrap(df.getVar(testVarName).stream().complete().map(VSpot::getRow).collect(Collectors.toList()));

            Var test = df.getVar(testVarName).mapRows(cleanMapping);
            Var target = df.getVar(targetVarName).mapRows(cleanMapping);

            double[] leftWeight = new double[test.getRowCount()];
            double[] leftVar = new double[test.getRowCount()];
            double[] rightWeight = new double[test.getRowCount()];
            double[] rightVar = new double[test.getRowCount()];

            OnlineStat so = OnlineStat.empty();

            double w = 0.0;
            for (int i = 0; i < test.getRowCount(); i++) {
                so.update(target.getValue(i));
                w += weights.getValue(i);
                leftWeight[i] = w;
                leftVar[i] = so.variance();
            }
            w = 0.0;
            for (int i = test.getRowCount() - 1; i >= 0; i--) {
                w += weights.getValue(i);
                so.update(target.getValue(i));
                rightWeight[i] = w;
                rightVar[i] += so.variance();
            }

            RTree.Candidate best = null;
            double bestScore = 0.0;

            RTreeTestPayload p = new RTreeTestPayload(2);
            p.totalVar = CoreTools.variance(target).getValue();

            for (int i = c.minCount; i < test.getRowCount() - c.minCount - 1; i++) {
                if (test.getValue(i) == test.getValue(i + 1)) continue;

                p.splitVar[0] = leftVar[i];
                p.splitVar[1] = rightVar[i];
                p.splitWeight[0] = leftWeight[i];
                p.splitWeight[1] = rightWeight[i];
                double value = c.function.computeTestValue(p);
                if (value > bestScore) {
                    bestScore = value;
                    best = new RTree.Candidate(value, testVarName);

                    double testValue = test.getValue(i);
                    best.addGroup(
                            String.format("%s <= %.6f", testVarName, testValue),
                            spot -> !spot.isMissing(testVarName) && spot.getValue(testVarName) <= testValue);
                    best.addGroup(
                            String.format("%s > %.6f", testVarName, testValue),
                            spot -> !spot.isMissing(testVarName) && spot.getValue(testVarName) > testValue);
                }
            }
            return (best != null) ? Optional.of(best) : Optional.empty();
        }
    };
}
