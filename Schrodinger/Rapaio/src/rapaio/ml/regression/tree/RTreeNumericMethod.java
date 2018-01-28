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
import rapaio.core.stat.WeightedMean;
import rapaio.core.stat.WeightedOnlineStat;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.filter.Filters;
import rapaio.data.stream.VSpot;
import rapaio.ml.common.predicate.RowPredicate;

import java.io.Serializable;
import java.util.*;
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
    Optional<RTreeCandidate> computeCandidate(RTree tree, Frame df, Var w,
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
        public Optional<RTreeCandidate> computeCandidate(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreeTestFunction function) {
            return Optional.empty();
        }
    };

    RTreeNumericMethod BINARY = new RTreeNumericMethod() {

        private static final long serialVersionUID = 7573765926645246027L;

        @Override
        public String name() {
            return "BINARY";
        }

        @Override
        public Optional<RTreeCandidate> computeCandidate(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreeTestFunction function) {

            Integer[] rows = new Integer[df.rowCount()];
            int len = 0;
            for (int i = 0; i < df.rowCount(); i++) {
                if (!df.rvar(testVarName).isMissing(i))
                    rows[len++] = i;
            }
            if(len<=0) {
                return Optional.empty();
            }
            Arrays.sort(rows, 0, len, Comparator.comparingDouble(o -> df.value(o, testVarName)));

            double[] leftWeight = new double[rows.length];
            double[] leftVar = new double[rows.length];
            double[] rightWeight = new double[rows.length];
            double[] rightVar = new double[rows.length];

            WeightedOnlineStat so = WeightedOnlineStat.empty();

            so.update(df.value(rows[0], targetVarName), weights.value(rows[0]));
            for (int i = 1; i < len; i++) {
                so.update(df.value(rows[i], targetVarName), weights.value(rows[i]));
                leftWeight[i] = weights.value(rows[i]) + leftWeight[i - 1];
                leftVar[i] = so.variance();
            }
            so = WeightedOnlineStat.empty();
            so.update(df.value(rows[len - 1], targetVarName), weights.value(rows[len - 1]));
            for (int i = len - 2; i >= 0; i--) {
                so.update(df.value(rows[i], targetVarName), weights.value(rows[i]));
                rightWeight[i] = weights.value(rows[i]) + rightWeight[i + 1];
                rightVar[i] = so.variance();
            }

            RTreeCandidate best = null;
            double bestScore = -1000000000;

            RTreeTestPayload p = new RTreeTestPayload(2);

            p.totalVar = (rightVar.length == 0) ? Double.NaN : rightVar[0];
            p.totalWeight = (rightVar.length == 0) ? Double.NaN : rightWeight[0];

            for (int i = c.minCount; i < len - c.minCount - 1; i++) {
                if (df.value(rows[i], testVarName) == df.value(rows[i + 1], testVarName)) continue;

                p.splitVar[0] = leftVar[i];
                p.splitVar[1] = rightVar[i];
                p.splitWeight[0] = leftWeight[i];
                p.splitWeight[1] = rightWeight[i];
                double value = c.function.computeTestValue(p);
                if (value > bestScore) {
                    bestScore = value;
                    best = new RTreeCandidate(value, testVarName);

                    double testValue = df.value(rows[i], testVarName);
                    best.addGroup(RowPredicate.numLessEqual(testVarName, testValue));
                    best.addGroup(RowPredicate.numGreater(testVarName, testValue));
                }
            }
            return (best != null) ? Optional.of(best) : Optional.empty();
        }
    };
}
