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
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface RTreeNominalMethod extends Serializable {

    RTreeNominalMethod IGNORE = new RTreeNominalMethod() {

        private static final long serialVersionUID = 7275580448899976553L;

        @Override
        public String name() {
            return "IGNORE";
        }

        @Override
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function) {
            return Collections.EMPTY_LIST;
        }
    };
    RTreeNominalMethod FULL = new RTreeNominalMethod() {

        private static final long serialVersionUID = 2733570883914611103L;

        @Override
        public String name() {
            return "FULL";
        }


        @Override
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame dfOld, Var weightsOld, String testColName, String targetColName, RTreeTestFunction function) {

            List<RTree.RTreeCandidate> result = new ArrayList<>();
            RTree.RTreeCandidate best = null;

            Mapping cleanMapping = dfOld.stream().filter(s -> !s.missing(testColName)).collectMapping();
            Frame df = dfOld.mapRows(cleanMapping);
            Var testVar = df.var(testColName);
            Var targetVar = df.var(targetColName);
            Var weights = weightsOld.mapRows(cleanMapping);

            DVector dvWeights = DVector.newFromWeights(false, testVar, weights);
            DVector dvCount = DVector.fromCount(false, testVar);

            // check to see if we have enough instances in at least 2 child nodes
            if (dvCount.countValues(x -> x >= c.minCount) <= 1)
                return Collections.EMPTY_LIST;

            // make the payload
            RTreeTestPayload p = new RTreeTestPayload(testVar.levels().length - 1);
            p.totalVar = CoreTools.var(targetVar).value();

            for (int i = 1; i < testVar.levels().length; i++) {
                p.splitWeight[i - 1] = dvWeights.get(i - 1);
                String label = testVar.levels()[i];
                p.splitVar[i - 1] = CoreTools.var(df.stream().filter(s -> s.label(testColName).equals(label)).toMappedFrame().var(targetColName)).value();
            }
            double value = c.function.computeTestValue(p);
            RTree.RTreeCandidate candidate = new RTree.RTreeCandidate(value, testColName);
            for (int i = 1; i < testVar.levels().length; i++) {
                String label = testVar.levels()[i];
                candidate.addGroup(testColName + " == " + label, spot -> spot.label(testColName).equals(label));
            }
            return Collections.singletonList(candidate);
        }
    };
    RTreeNominalMethod BINARY = new RTreeNominalMethod() {

        private static final long serialVersionUID = -4703727362952157041L;

        @Override
        public String name() {
            return "BINARY";
        }


        @Override
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame dfOld, Var weightsOld, String testColName, String targetColName, RTreeTestFunction function) {

            Mapping cleanMapping = dfOld.stream().filter(s -> !s.missing(testColName)).collectMapping();
            Frame df = dfOld.mapRows(cleanMapping);
            Var testVar = df.var(testColName);
            Var targetVar = df.var(targetColName);
            Var weights = weightsOld.mapRows(cleanMapping);

            DVector dvWeights = DVector.newFromWeights(false, testVar, weights);
            DVector dvCount = DVector.fromCount(false, testVar);

            // compute online statistics for all level slices
            OnlineStat[] os = new OnlineStat[testVar.levels().length - 1];
            for (int i = 0; i < testVar.levels().length - 1; i++) {
                os[i] = new OnlineStat();
            }
            for (int i = 0; i < testVar.rowCount(); i++) {
                int index = testVar.index(i);
                if (index == 0)
                    continue;
                os[index - 1].update(targetVar.value(i));
            }

            double totalVar = CoreTools.var(targetVar).value();

            RTree.RTreeCandidate best = null;
            double bestScore = Double.MIN_VALUE;

            for (int i = 1; i < testVar.levels().length; i++) {
                String testLabel = testVar.levels()[i];

                // check to see if we have enough values

                if (dvCount.get(i) < c.minCount || df.rowCount() - dvCount.get(i) < c.minCount)
                    continue;

                OnlineStat osSelect = os[i - 1];
                OnlineStat osOther = new OnlineStat();

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

                double value = c.function.computeTestValue(p);
                if (value > bestScore) {
                    bestScore = value;
                    best = new RTree.RTreeCandidate(value, testColName);
                    best.addGroup(testColName + " == " + testLabel,
                            spot -> !spot.missing(testColName) && spot.label(testColName).equals(testLabel));
                    best.addGroup(testColName + " != " + testLabel,
                            spot -> !spot.missing(testColName) && !spot.label(testColName).equals(testLabel));
                }
            }
            return (best == null) ? Collections.EMPTY_LIST : Collections.singletonList(best);
        }
    };

    String name();

    List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function);
}