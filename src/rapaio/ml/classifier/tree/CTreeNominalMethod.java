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
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.tools.DensityTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
@Deprecated
public interface CTreeNominalMethod extends Serializable {

    String name();

    CTreeNominalMethod newInstance();

    List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms);

    class Ignore implements CTreeNominalMethod {
        private static final long serialVersionUID = -4880331163848862999L;

        @Override
        public String name() {
            return "Ignore";
        }

        @Override
        public CTreeNominalMethod newInstance() {
            return new Ignore();
        }

        @Override
        public List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms) {
            return new ArrayList<>();
        }
    }

    class Full implements CTreeNominalMethod {
        private static final long serialVersionUID = 1440419101927544578L;

        @Override
        public String name() {
            return "Full";
        }

        @Override
        public CTreeNominalMethod newInstance() {
            return new Full();
        }

        @Override
        public List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms) {
            Var test = df.var(testColName);
            Var target = df.var(targetColName);

            if (!new DensityTable(test, target).hasCountWithMinimum(false, c.getMinCount(), 2)) {
                return Collections.emptyList();
            }

            List<CTreeCandidate> result = new ArrayList<>();
            DensityTable dt = new DensityTable(test, target, weights);
            double value = function.compute(dt);

            CTreeCandidate candidate = new CTreeCandidate(value, function.sign(), testColName);
            for (int i = 1; i < test.dictionary().length; i++) {
                final String label = test.dictionary()[i];
                candidate.addGroup(
                        String.format("%s == %s", testColName, label),
                        spot -> !spot.missing(testColName) && spot.label(testColName).equals(label));
            }

            result.add(candidate);
            return result;
        }
    }

    class Binary implements CTreeNominalMethod {

        private static final long serialVersionUID = -2139837342128959674L;

        @Override
        public String name() {
            return "Binary";
        }

        @Override
        public CTreeNominalMethod newInstance() {
            return new Binary();
        }

        @Override
        public List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms) {

            Var test = df.var(testColName);
            Var target = df.var(targetColName);
            if (!(new DensityTable(test, target).hasCountWithMinimum(false, c.getMinCount(), 2))) {
                return Collections.emptyList();
            }

            List<CTreeCandidate> result = new ArrayList<>();
            CTreeCandidate best = null;

            int[] termCount = new int[test.dictionary().length];
            test.stream().forEach(s -> termCount[s.index()]++);

            Iterator<Integer> indexes = terms.indexes(testColName).iterator();
            while (indexes.hasNext()) {
                int i = indexes.next();
                if (termCount[i] < c.getMinCount()) {
                    indexes.remove();
                    continue;
                }
                String testLabel = df.var(testColName).dictionary()[i];

                DensityTable dt = new DensityTable(test, target, weights, testLabel);
                double value = function.compute(dt);
                CTreeCandidate candidate = new CTreeCandidate(value, function.sign(), testColName);
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
    }
}
