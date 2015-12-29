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

package rapaio.ml.regression.tree;

import rapaio.core.CoreTools;
import rapaio.core.stat.OnlineStat;
import rapaio.data.*;
import rapaio.data.filter.VFRefSort;
import rapaio.data.stream.VSpot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface RTreeNumericMethod extends Serializable {

    RTreeNumericMethod IGNORE = new RTreeNumericMethod() {
        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreeTestFunction function) {
            return new ArrayList<>();
        }
    };
    RTreeNumericMethod BINARY = new RTreeNumericMethod() {
        @Override
        public String name() {
            return "BINARY";
        }

        @Override
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreeTestFunction function) {

            Mapping cleanMapping = Mapping.wrap(df.var(testVarName).stream().complete().map(VSpot::row).collect(Collectors.toList()));

            Var test = df.var(testVarName).mapRows(cleanMapping);
            Var target = df.var(targetVarName).mapRows(cleanMapping);

            Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.seq(cleanMapping.size()));

            double[] leftWeight = new double[test.rowCount()];
            double[] leftVar = new double[test.rowCount()];
            double[] rightWeight = new double[test.rowCount()];
            double[] rightVar = new double[test.rowCount()];

            OnlineStat so = new OnlineStat();

            double w = 0.0;
            for (int i = 0; i < test.rowCount(); i++) {
                int row = sort.index(i);
                so.update(target.value(row));
                w += weights.value(row);
                leftWeight[row] = w;
                leftVar[row] = so.variance();
            }
            w = 0.0;
            for (int i = test.rowCount() - 1; i >= 0; i--) {
                int row = sort.index(i);
                w += weights.value(row);
                so.update(target.value(row));
                rightWeight[row] = w;
                rightVar[row] += so.variance();
            }

            RTree.RTreeCandidate best = null;
            double bestScore = 0.0;

            RTreeTestPayload p = new RTreeTestPayload(2);
            p.totalVar = CoreTools.var(target).value();

            for (int i = 0; i < test.rowCount(); i++) {
                int row = sort.index(i);

                if (test.missing(row)) continue;
                if (i < c.minCount || i > test.rowCount() - 1 - c.minCount) continue;
                if (test.value(sort.index(i)) == test.value(sort.index(i + 1))) continue;

                p.splitVar[0] = leftVar[row];
                p.splitVar[1] = rightVar[row];
                p.splitWeight[0] = leftWeight[row];
                p.splitWeight[1] = rightWeight[row];
                double value = c.function.computeTestValue(p);
                if (value > bestScore) {
                    bestScore = value;
                    best = new RTree.RTreeCandidate(value, testVarName);

                    double testValue = test.value(sort.index(i));
                    best.addGroup(
                            String.format("%s <= %.6f", testVarName, testValue),
                            spot -> !spot.missing(testVarName) && spot.value(testVarName) <= testValue);
                    best.addGroup(
                            String.format("%s > %.6f", testVarName, testValue),
                            spot -> !spot.missing(testVarName) && spot.value(testVarName) > testValue);
                }
            }
            return (best != null) ? Collections.singletonList(best) : Collections.EMPTY_LIST;
        }
    };

    String name();

    List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testVarName, String targetVarName, RTreeTestFunction function);
}
