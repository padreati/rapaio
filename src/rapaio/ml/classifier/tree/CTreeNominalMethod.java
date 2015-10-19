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
import rapaio.core.tools.DTable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.util.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeNominalMethod extends Serializable {

    List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms);

    Tag<CTreeNominalMethod> Ignore = Tag.valueOf("Ignore",
            (CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms) -> new ArrayList<>());

    Tag<CTreeNominalMethod> Full = Tag.valueOf("Full",
            (CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms) -> {
                Var test = df.var(testColName);
                Var target = df.var(targetColName);

                if (!DTable.newFromCounts(test, target).hasCountWithMinimum(false, c.minCount(), 2)) {
                    return Collections.emptyList();
                }

                List<CTreeCandidate> result = new ArrayList<>();
                DTable dt = DTable.newFromWeights(test, target, weights);
                double value = function.compute(dt);

                CTreeCandidate candidate = new CTreeCandidate(value, testColName);
                for (int i = 1; i < test.levels().length; i++) {
                    final String label = test.levels()[i];
                    candidate.addGroup(
                            String.format("%s == %s", testColName, label),
                            spot -> !spot.missing(testColName) && spot.label(testColName).equals(label));
                }

                result.add(candidate);
                return result;
            });

    Tag<CTreeNominalMethod> Binary = Tag.valueOf("Binary",
            (CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeTestFunction function, CTreeNominalTerms terms) -> {

                Var test = df.var(testColName);
                Var target = df.var(targetColName);
                if (!(DTable.newFromCounts(test, target).hasCountWithMinimum(false, c.minCount(), 2))) {
                    return Collections.emptyList();
                }

                List<CTreeCandidate> result = new ArrayList<>();
                CTreeCandidate best = null;

                int[] termCount = new int[test.levels().length];
                test.stream().forEach(s -> termCount[s.index()]++);

                Iterator<Integer> indexes = terms.indexes(testColName).iterator();
                while (indexes.hasNext()) {
                    int i = indexes.next();
                    if (termCount[i] < c.minCount()) {
                        indexes.remove();
                        continue;
                    }
                    String testLabel = df.var(testColName).levels()[i];

                    DTable dt = DTable.newBinaryFromWeights(test, target, weights, testLabel);
                    double value = function.compute(dt);
                    CTreeCandidate candidate = new CTreeCandidate(value, testColName);
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
            });
}
