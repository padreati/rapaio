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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeNumericMethod extends Serializable {

    List<CTreeCandidate> computeCandidates(CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeFunction function);

    Tag<CTreeNumericMethod> Ignore = Tag.valueOf("Ignore",
            (CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeFunction function) -> new ArrayList<>());

    Tag<CTreeNumericMethod> Binary = Tag.valueOf("Binary",
            (CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeFunction function) -> {
                Var test = df.var(testColName);
                Var target = df.var(targetColName);

                DTable dt = DTable.newEmpty(DTable.NUMERIC_DEFAULT_LABELS, target.levels());
                int misCount = 0;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = (test.missing(i)) ? 0 : 2;
                    if (test.missing(i)) misCount++;
                    dt.update(row, target.index(i), weights.value(i));
                }

                Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.newSeq(df.rowCount()));

                CTreeCandidate best = null;

                for (int i = 0; i < df.rowCount(); i++) {
                    int row = sort.index(i);

                    if (test.missing(row)) continue;

                    dt.update(2, target.index(row), -weights.value(row));
                    dt.update(1, target.index(row), +weights.value(row));

                    if (i >= misCount + c.minCount() - 1 &&
                            i < df.rowCount() - c.minCount() &&
                            test.value(sort.index(i)) < test.value(sort.index(i + 1))) {

                        CTreeCandidate current = new CTreeCandidate(function.compute(dt), testColName);
                        if (best == null) {
                            best = current;

                            final double testValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                            current.addGroup(
                                    String.format("%s <= %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) <= testValue);
                            current.addGroup(
                                    String.format("%s > %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) > testValue);
                        } else {
                            int comp = best.compareTo(current);
                            if (comp < 0) continue;
                            if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                            best = current;

                            final double testValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                            current.addGroup(
                                    String.format("%s <= %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) <= testValue);
                            current.addGroup(
                                    String.format("%s > %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) > testValue);
                        }
                    }
                }

                List<CTreeCandidate> result = new ArrayList<>();
                if (best != null)
                    result.add(best);
                return result;
            });

    Tag<CTreeNumericMethod> SkipHalf = Tag.valueOf("SkipHalf",
            (CTree c, Frame df, Var weights, String testColName, String targetColName, CTreeFunction function) -> {

                final int skip = 2;

                Var test = df.var(testColName);
                Var target = df.var(targetColName);

                DTable dt = DTable.newEmpty(DTable.NUMERIC_DEFAULT_LABELS, target.levels());
                int misCount = 0;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = (test.missing(i)) ? 0 : 2;
                    if (test.missing(i)) misCount++;
                    dt.update(row, target.index(i), weights.value(i));
                }

                Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.newSeq(df.rowCount()));

                CTreeCandidate best = null;

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

                    if (count != 0) continue;
                    if (i >= misCount + c.minCount() - 1 &&
                            i < df.rowCount() - c.minCount() &&
                            test.value(sort.index(i)) < test.value(sort.index(i + 1))) {

                        CTreeCandidate current = new CTreeCandidate(function.compute(dt), testColName);
                        if (best == null) {
                            best = current;

                            final double testValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                            current.addGroup(
                                    String.format("%s <= %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) <= testValue);
                            current.addGroup(
                                    String.format("%s > %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) > testValue);
                        } else {
                            int comp = best.compareTo(current);
                            if (comp < 0) continue;
                            if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                            best = current;

                            final double testValue = test.value(sort.index(i));
                            current.addGroup(
                                    String.format("%s <= %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) <= testValue);
                            current.addGroup(
                                    String.format("%s > %.6f", testColName, testValue),
                                    spot -> !spot.missing(testColName) && spot.value(testColName) > testValue);
                        }
                    }
                }

                List<CTreeCandidate> result = new ArrayList<>();
                if (best != null)
                    result.add(best);
                return result;
            });
}


