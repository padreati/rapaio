/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.classifier.tree;

import rapaio.classifier.tools.DensityTable;
import rapaio.classifier.tools.DensityVector;
import rapaio.cluster.util.Pair;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.RowComparators;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.data.filters.BaseFilters;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.data.stream.FSpot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CTree {

    // FUNCTION

    public static interface Function {
        double compute(DensityTable dt);

        int sign();

        String getFunctionName();
    }

    public static enum Functions implements Function {
        ENTROPY("Entropy", 1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getSplitEntropy(false);
            }
        },
        INFO_GAIN("InfoGain", -1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getInfoGain(false);
            }
        },
        GAIN_RATIO("GainRatio", -1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getGainRatio();
            }
        },
        GINI("Gini", -1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getGiniIndex();
            }
        };
        private final int sign;
        private final String name;

        private Functions(String name, int sign) {
            this.sign = sign;
            this.name = name;
        }

        public int sign() {
            return sign;
        }

        public String getFunctionName() {
            return name;
        }
    }


    // NOMINAL METHOD

    public static interface NominalMethod {
        String getMethodName();

        List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function);
    }

    public static enum NominalMethods implements NominalMethod {
        IGNORE("Ignore") {
            @Override
            public List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                return new ArrayList<>();
            }
        },
        FULL("Full") {
            @Override
            public List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                List<CTreeCandidate> result = new ArrayList<>();
                Vector test = df.col(testColName);
                Vector target = df.col(targetColName);

                if (new DensityTable(test, target).countWithMinimum(false, c.getMinCount()) < 2) {
                    return result;
                }

                DensityTable dt = new DensityTable(test, target, df.getWeights());
                double value = function.compute(dt);

                CTreeCandidate candidate = new CTreeCandidate(value, function.sign());
                for (int i = 1; i < test.getDictionary().length; i++) {

                    final String label = test.getDictionary()[i];
                    candidate.addGroup(
                            String.format("%s == %s", testColName, label),
                            spot -> !spot.isMissing(testColName) && spot.getLabel(testColName).equals(label));
                }

                result.add(candidate);
                return result;
            }
        };
        private final String name;

        private NominalMethods(String name) {
            this.name = name;
        }

        public String getMethodName() {
            return name;
        }
    }

    // NUMERIC METHOD

    public static interface NumericMethod {
        List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function);

        String getMethodName();
    }

    public static enum NumericMethods implements NumericMethod {
        IGNORE("Ignore") {
            @Override
            public List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                return new ArrayList<CTreeCandidate>();
            }
        },
        BINARY("Binary") {
            @Override
            public List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                Vector test = df.col(testColName);
                Vector target = df.col(targetColName);

                DensityTable dt = new DensityTable(DensityTable.NUMERIC_DEFAULT_LABELS, target.getDictionary());
                int misCount = 0;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = (test.isMissing(i)) ? 0 : 2;
                    if (test.isMissing(i)) misCount++;
                    dt.update(row, target.getIndex(i), df.getWeight(i));
                }

                Vector sort = BaseFilters.sort(Vectors.newSeq(df.rowCount()), RowComparators.numericComparator(test, true));

                CTreeCandidate best = null;

                for (int i = 0; i < df.rowCount(); i++) {
                    int row = sort.getIndex(i);

                    if (test.isMissing(row)) continue;

                    dt.update(2, target.getIndex(row), -df.getWeight(row));
                    dt.update(1, target.getIndex(row), +df.getWeight(row));

                    if (i >= misCount + c.getMinCount() &&
                            i < df.rowCount() - 1 - c.getMinCount() &&
                            test.getValue(sort.getIndex(i)) < test.getValue(sort.getIndex(i + 1))) {

                        CTreeCandidate current = new CTreeCandidate(function.compute(dt), function.sign());
                        if (best == null) {
                            best = current;

                            final double testValue = test.getValue(sort.getIndex(i));
                            current.addGroup(
                                    String.format("%s <= %.6f", testColName, testValue),
                                    spot -> !spot.isMissing(testColName) && spot.getValue(testColName) <= testValue);
                            current.addGroup(
                                    String.format("%s > %.6f", testColName, testValue),
                                    spot -> !spot.isMissing(testColName) && spot.getValue(testColName) > testValue);

                        } else {
                            int comp = best.compareTo(current);
                            if (comp < 0) continue;
                            if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                            best = current;
                        }
                    }
                }

                List<CTreeCandidate> result = new ArrayList<>();
                result.add(best);
                return result;
            }
        };
        private final String name;

        NumericMethods(String name) {
            this.name = name;
        }

        public abstract List<CTreeCandidate> computeCandidates(PartitionTreeClassifier c, Frame df, String testColName, String targetColName, Function function);

        public String getMethodName() {
            return name;
        }
    }

    public static interface Splitter {
        public String getSplitterName();

        public List<Frame> performSplit(Frame df, CTreeCandidate candidate);
    }

    public static enum Splitters implements Splitter {
        IGNORE_MISSING("IgnoreMissing") {
            @Override
            public List<Frame> performSplit(Frame df, CTreeCandidate candidate) {
                List<Mapping> mappings = new ArrayList<>();
                IntStream.range(0, candidate.getGroupPredicates().size()).forEach(i -> mappings.add(new Mapping()));

                df.stream().forEach(fspot -> {
                    for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                        Predicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                        if (predicate.test(fspot)) {
                            mappings.get(i).add(fspot.rowId());
                            break;
                        }
                    }
                });
                return mappings.stream().map(mapping -> new MappedFrame(df.source(), mapping)).collect(Collectors.toList());
            }
        };

        private final String name;

        Splitters(String name) {
            this.name = name;
        }

        @Override
        public String getSplitterName() {
            return name;
        }
    }

    public static interface Predictor {

        String getPredictorName();

        Pair<Integer, DensityVector> predict(Frame df, int row, CPartitionTreeNode node);
    }

    public static enum Predictors implements Predictor {
        STANDARD("standard") {
            @Override
            public Pair<Integer, DensityVector> predict(Frame df, int row, CPartitionTreeNode node) {
                if (node.counter.sum(false) == 0)
                    return new Pair<>(node.parent.bestIndex, node.parent.density);
                if (node.leaf)
                    return new Pair<>(node.bestIndex, node.density);

                String[] dict = node.c.getDict();
                DensityVector dv = new DensityVector(dict);
                for (CPartitionTreeNode child : node.children) {
                    DensityVector d = predict(df, row, child).second;
                    for (int i = 0; i < dict.length; i++) {
                        dv.update(i, d.get(i));
                    }
                }
                return new Pair<>(dv.findBestIndex(), dv);
            }
        };

        private final String predictorName;

        Predictors(String predictorName) {
            this.predictorName = predictorName;
        }

        public String getPredictorName() {
            return predictorName;
        }
    }
}
