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
import rapaio.data.Index;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.data.filter.VFRefSort;
import rapaio.util.Tag;

import java.io.Serializable;
import java.util.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/15.
 */
public interface CTreeTest extends Serializable {

    List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var w, String testName, String targetName, CTreeTestFunction function);

    Tag<CTreeTest> Ignore = Tag.valueOf("Ignore",
            (CTree c, Frame df, Var w, String testName, String targetName, CTreeTestFunction function) -> new ArrayList<>());

    Tag<CTreeTest> Numeric_Binary = Tag.valueOf("Numeric_Binary",
            (CTree c, Frame df, Var weights, String testName, String targetName, CTreeTestFunction function) -> {
                Var test = df.var(testName);
                Var target = df.var(targetName);

                DTable dt = DTable.newEmpty(DTable.NUMERIC_DEFAULT_LABELS, target.levels());
                int misCount = 0;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = (test.missing(i)) ? 0 : 2;
                    if (test.missing(i)) misCount++;
                    dt.update(row, target.index(i), weights.value(i));
                }

                Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.newSeq(df.rowCount()));

                double bestScore = 0.0;
                int bestIndex = 0;
                double bestTestValue = 0;

                for (int i = 0; i < df.rowCount(); i++) {
                    int row = sort.index(i);

                    if (test.missing(row)) continue;

                    dt.update(2, target.index(row), -weights.value(row));
                    dt.update(1, target.index(row), +weights.value(row));

                    if (i >= misCount + c.getMinCount() - 1 &&
                            i < df.rowCount() - c.getMinCount() &&
                            test.value(sort.index(i)) < test.value(sort.index(i + 1))) {

                        double currentScore = function.compute(dt);
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp < 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;

                        bestScore = currentScore;
                        bestTestValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                    }
                }
                CTreeCandidate best = new CTreeCandidate(bestScore, testName);
                double testValue = bestTestValue;
                best.addGroup(
                        String.format("%s <= %.6f", testName, testValue),
                        spot -> !spot.missing(testName) && spot.value(testName) <= testValue);
                best.addGroup(
                        String.format("%s > %.6f", testName, bestTestValue),
                        spot -> !spot.missing(testName) && spot.value(testName) > testValue);
                return Collections.singletonList(best);
            });

    Tag<CTreeTest> Numeric_SkipHalf = Tag.valueOf("Numeric_SkipHalf",
            (CTree c, Frame df, Var weights, String testName, String targetName, CTreeTestFunction function) -> {

                final int skip = 5;

                Var test = df.var(testName);
                Var target = df.var(targetName);

                DTable dt = DTable.newEmpty(DTable.NUMERIC_DEFAULT_LABELS, target.levels());
                int misCount = 0;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = (test.missing(i)) ? 0 : 2;
                    if (test.missing(i)) misCount++;
                    dt.update(row, target.index(i), weights.value(i));
                }

                Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.newSeq(df.rowCount()));

                double bestScore = 0.0;
                int bestIndex = 0;
                double bestTestValue = 0;

                int count = skip - 1;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = sort.index(i);
                    count++;
                    if (count == skip) {
                        count = 0;
                    }

                    if (test.missing(row)) continue;

                    dt.update(2, target.index(row), -weights.value(row));
                    dt.update(1, target.index(row), +weights.value(row));

                    if (i >= misCount + c.getMinCount() - 1 &&
                            i < df.rowCount() - c.getMinCount() &&
                            test.value(sort.index(i)) < test.value(sort.index(i + 1))) {

                        double currentScore = function.compute(dt);
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp < 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;

                        bestScore = currentScore;
                        bestTestValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                    }
                }
                CTreeCandidate best = new CTreeCandidate(bestScore, testName);
                double testValue = bestTestValue;
                best.addGroup(
                        String.format("%s <= %.6f", testName, testValue),
                        spot -> !spot.missing(testName) && spot.value(testName) <= testValue);
                best.addGroup(
                        String.format("%s > %.6f", testName, bestTestValue),
                        spot -> !spot.missing(testName) && spot.value(testName) > testValue);
                return Collections.singletonList(best);
            });

    Tag<CTreeTest> Binary_Binary = Tag.valueOf("Binary_Binary",
            (CTree c, Frame df, Var weights, String testName, String targetName, CTreeTestFunction function) -> {

                Var test = df.var(testName);
                Var target = df.var(targetName);
                DTable dt = DTable.newFromCounts(test, target);
                if (!(dt.hasCountWithMinimum(false, c.getMinCount(), 2))) {
                    return Collections.emptyList();
                }

                CTreeCandidate best = new CTreeCandidate(function.compute(dt), testName);
                best.addGroup(testName + " == 1", spot -> spot.binary(testName));
                best.addGroup(testName + " != 1", spot -> !spot.binary(testName));
                return Collections.singletonList(best);
            });

}
