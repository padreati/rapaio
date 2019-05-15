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

package rapaio.experiment.ml.regression.tree.rtree;

import it.unimi.dsi.fastutil.ints.IntArrays;
import rapaio.core.RandomSource;
import rapaio.core.stat.WeightedOnlineStat;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.experiment.ml.regression.tree.RTree;

import java.io.Serializable;
import java.util.Optional;

/**
 * Method which computes the best node candidate for a given numeric
 * variable. A candidate describes how a the current node can be split.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface RTreeNumericTest extends Serializable {

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
                                              RTreePurityFunction testFunction);

    static RTreeNumericTest ignore() {
        return new NumericIgnore();
    }

    static RTreeNumericTest binary() {
        return new NumericBinary();
    }
}

class NumericIgnore implements RTreeNumericTest {
    private static final long serialVersionUID = -5982576265221513285L;

    @Override
    public String name() {
        return "IGNORE";
    }

    @Override
    public Optional<RTreeCandidate> computeCandidate(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreePurityFunction function) {
        return Optional.empty();
    }
}


class NumericBinary implements RTreeNumericTest {

    private static final long serialVersionUID = 7573765926645246027L;

    @Override
    public String name() {
        return "BINARY";
    }

    @Override
    public Optional<RTreeCandidate> computeCandidate(RTree c, Frame df, Var weights, String testName, String targetName, RTreePurityFunction function) {

        int testNameIndex = df.varIndex(testName);
        int targetNameIndex = df.varIndex(targetName);

        int[] rows = new int[df.rowCount()];
        int len = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            if (!df.isMissing(i, testNameIndex))
                rows[len++] = i;
        }
        if (len == 0) {
            return Optional.empty();
        }
        IntArrays.quickSort(rows, 0, len, (o1, o2) -> Double.compare(df.getDouble(o1, testNameIndex), df.getDouble(o2, testNameIndex)));

        double[] leftWeight = new double[rows.length];
        double[] leftVar = new double[rows.length];
        double[] rightWeight = new double[rows.length];
        double[] rightVar = new double[rows.length];

        WeightedOnlineStat so = WeightedOnlineStat.empty();

        so.update(df.getDouble(rows[0], targetNameIndex), weights.getDouble(rows[0]));
        for (int i = 1; i < len; i++) {
            so.update(df.getDouble(rows[i], targetNameIndex), weights.getDouble(rows[i]));
            leftWeight[i] = weights.getDouble(rows[i]) + leftWeight[i - 1];
            leftVar[i] = so.variance();
        }
        so = WeightedOnlineStat.empty();
        so.update(df.getDouble(rows[len - 1], targetNameIndex), weights.getDouble(rows[len - 1]));
        for (int i = len - 2; i >= 0; i--) {
            so.update(df.getDouble(rows[i], targetNameIndex), weights.getDouble(rows[i]));
            rightWeight[i] = weights.getDouble(rows[i]) + rightWeight[i + 1];
            rightVar[i] = so.variance();
        }

        RTreeCandidate best = null;
        double bestScore = -1e100;

        RTreeTestPayload p = new RTreeTestPayload(2);

        p.totalVar = rightVar[0];
        p.totalWeight = rightWeight[0];

        for (int i = c.minCount(); i < len - c.minCount() - 1; i++) {
            if (df.getDouble(rows[i], testNameIndex) == df.getDouble(rows[i + 1], testNameIndex)) continue;

            p.splitVar[0] = leftVar[i];
            p.splitVar[1] = rightVar[i];
            p.splitWeight[0] = leftWeight[i];
            p.splitWeight[1] = rightWeight[i];

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
}
