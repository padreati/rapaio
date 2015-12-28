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
import rapaio.core.RandomSource;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
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
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame dfOld, Var weights, String testColName, String targetColName, RTreeTestFunction function) {

            List<RTree.RTreeCandidate> result = new ArrayList<>();
            RTree.RTreeCandidate best = null;

            Frame df = dfOld.stream().filter(s -> s.missing(testColName)).toMappedFrame();
            Var testVar = df.var(testColName);
            Var targetVar = df.var(targetColName);

            DVector dvWeights = DVector.newFromWeights(false, testVar, weights);
            DVector dvCount = DVector.newFromCount(false, testVar);

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
                // TODO make it correct
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
        public List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame dfOld, Var weights, String testColName, String targetColName, RTreeTestFunction function) {

            List<RTree.RTreeCandidate> result = new ArrayList<>();
            RTree.RTreeCandidate best = null;

            Frame df = dfOld.stream().filter(s -> s.missing(testColName)).toMappedFrame();
            Var testVar = df.var(testColName);
            Var targetVar = df.var(targetColName);

            DVector dvWeights = DVector.newFromWeights(false, testVar, weights);
            DVector dvCount = DVector.newFromCount(false, testVar);

            double totalVar = CoreTools.var(targetVar).value();

            for (int i = 1; i < testVar.levels().length; i++) {
                String testLabel = testVar.levels()[i];

                // check to see if we have enough values

                if (dvCount.get(i - 1) < c.minCount || df.rowCount() - dvCount.get(i - 1) < c.minCount)
                    continue;

                Var in = df.stream()
                        .filter(s -> !s.missing(testColName) && s.label(testColName).equals(testLabel))
                        .toMappedFrame()
                        .var(targetColName);
                Var out = df.stream()
                        .filter(s -> !s.missing(testColName) && !s.label(testColName).equals(testLabel))
                        .toMappedFrame()
                        .var(targetColName);

                RTreeTestPayload p = new RTreeTestPayload(2);
                p.totalVar = totalVar;

                // payload for current node

                p.splitWeight[0] = dvWeights.get(i - 1);
                p.splitVar[0] = CoreTools.var(in).value();

                // payload for the others

                p.splitWeight[1] = dvWeights.sum() - dvWeights.get(i - 1);
                p.splitVar[1] = CoreTools.var(out).value();

                double value = c.function.computeTestValue(p);

                RTree.RTreeCandidate candidate = new RTree.RTreeCandidate(value, testColName);
                if (best == null) {
                    best = candidate;
                    best.addGroup(testColName + " == " + testLabel, spot -> spot.label(testColName).equals(testLabel));
                    best.addGroup(testColName + " != " + testLabel, spot -> !spot.label(testColName).equals(testLabel));
                } else {
                    int comp = best.compareTo(candidate);
                    if (comp < 0) continue;
                    if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    best = candidate;
                    best.addGroup(testColName + " == " + testLabel, spot -> spot.label(testColName).equals(testLabel));
                    best.addGroup(testColName + " != " + testLabel, spot -> !spot.label(testColName).equals(testLabel));
                }
            }
            if (best != null)
                result.add(best);
            return result;
        }
    };

    String name();

    List<RTree.RTreeCandidate> computeCandidates(RTree c, Frame df, Var weights, String testColName, String targetColName, RTreeTestFunction function);
}