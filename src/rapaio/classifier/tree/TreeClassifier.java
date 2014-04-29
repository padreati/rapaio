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

import rapaio.classifier.AbstractClassifier;
import rapaio.classifier.Classifier;
import rapaio.classifier.tools.DensityTable;
import rapaio.classifier.tools.DensityVector;
import rapaio.cluster.util.Pair;
import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.Vector;
import rapaio.data.filters.BaseFilters;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.data.stream.FSpot;
import rapaio.util.SPredicate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class TreeClassifier extends AbstractClassifier {

    // parameters
    int minCount = 1;
    int maxDepth = Integer.MAX_VALUE;

    TestCounter testCounter = TestCounters.M_NOMINAL_M_NUMERIC;
    NominalMethod nominalMethod = NominalMethods.FULL;
    NumericMethod numericMethod = NumericMethods.BINARY;
    Function function = Functions.INFO_GAIN;
    Splitter splitter = Splitters.REMAINS_IGNORED;
    Predictor predictor = Predictors.STANDARD;

    // tree root node
    CTreeNode root;
    int rows;

    @Override
    public Classifier newInstance() {
        return new TreeClassifier()
                .withMinCount(minCount)
                .withMaxDepth(maxDepth)
                .withNominalMethod(nominalMethod)
                .withNumericMethod(numericMethod)
                .withFunction(function)
                .withSplitter(splitter)
                .withPredictor(predictor);
    }

    public static TreeClassifier buildID3() {
        return new TreeClassifier()
                .withTestCounter(TestCounters.ONE_NOMINAL_ONE_NUMERIC)
                .withMaxDepth(Integer.MAX_VALUE)
                .withSplitter(Splitters.REMAINS_IGNORED)
                .withNominalMethod(NominalMethods.FULL)
                .withNumericMethod(NumericMethods.IGNORE)
                .withFunction(Functions.ENTROPY)
                .withPredictor(Predictors.STANDARD);
    }

    public static TreeClassifier buildC45() {
        return new TreeClassifier()
                .withTestCounter(TestCounters.ONE_NOMINAL_ONE_NUMERIC)
                .withMaxDepth(Integer.MAX_VALUE)
                .withSplitter(Splitters.REMAINS_TO_ALL_WEIGHTED)
                .withNominalMethod(NominalMethods.FULL)
                .withNumericMethod(NumericMethods.BINARY)
                .withFunction(Functions.GAIN_RATIO)
                .withPredictor(Predictors.STANDARD);
    }

    public static TreeClassifier buildDecisionStump() {
        return new TreeClassifier()
                .withMaxDepth(2)
                .withTestCounter(TestCounters.ONE_NOMINAL_ONE_NUMERIC)
                .withSplitter(Splitters.REMAINS_TO_ALL_WEIGHTED)
                .withNominalMethod(NominalMethods.BINARY)
                .withNumericMethod(NumericMethods.BINARY)
                .withPredictor(Predictors.STANDARD);
    }

    public static TreeClassifier buildCART() {
        return new TreeClassifier()
                .withMaxDepth(Integer.MAX_VALUE)
                .withTestCounter(TestCounters.ONE_NOMINAL_ONE_NUMERIC)
                .withSplitter(Splitters.REMAINS_TO_ALL_WEIGHTED)
                .withNominalMethod(NominalMethods.BINARY)
                .withNumericMethod(NumericMethods.BINARY)
                .withPredictor(Predictors.STANDARD);
    }

    public int getMinCount() {
        return minCount;
    }

    public TreeClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public TreeClassifier withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public TestCounter getTestCounter() {
        return testCounter;
    }

    public TreeClassifier withTestCounter(TestCounter testCounter) {
        this.testCounter = testCounter;
        return this;
    }

    public NominalMethod getNominalMethod() {
        return nominalMethod;
    }

    public TreeClassifier withNominalMethod(NominalMethod methodNominal) {
        this.nominalMethod = methodNominal;
        return this;
    }

    public NumericMethod getNumericMethod() {
        return numericMethod;
    }

    public TreeClassifier withNumericMethod(NumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public Function getFunction() {
        return function;
    }

    public TreeClassifier withFunction(Function function) {
        this.function = function;
        return this;
    }

    public Splitter getSplitter() {
        return splitter;
    }

    public TreeClassifier withSplitter(Splitter splitter) {
        this.splitter = splitter;
        return this;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public TreeClassifier withPredictor(Predictor predictor) {
        this.predictor = predictor;
        return this;
    }

    @Override
    public String name() {
        return "PartitionTreeClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("PartitionTreeClassifier (");
        sb.append("colSelector=").append(colSelector.toString()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth).append(",");
        sb.append("testCounter=").append(testCounter.name()).append(",");
        sb.append("numericMethod=").append(numericMethod.name()).append(",");
        sb.append("nominalMethod=").append(nominalMethod.name()).append(",");
        sb.append("function=").append(function.name()).append(",");
        sb.append("splitter=").append(splitter.name()).append(",");
        sb.append("predictor=").append(predictor.name());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {

        targetCol = targetColName;
        dict = df.col(targetCol).getDictionary();
        rows = df.rowCount();

        testCounter.initialize(df, targetColName);

        root = new CTreeNode(this, null, "root", spot -> true);
        root.learn(df, maxDepth);
    }

    @Override
    public void predict(Frame df) {

        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        df.stream().forEach(spot -> {
            Pair<Integer, DensityVector> result = predictor.predict(spot, root);
            pred.setIndex(spot.row(), result.first);
            for (int j = 0; j < dict.length; j++) {
                dist.setValue(spot.row(), j, result.second.get(j));
            }
        });
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append("\n");

        sb.append(String.format("n=%d\n", rows));

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, n/err, pred (dist) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);
    }

    private void buildSummary(StringBuilder sb, CTreeNode node, int level) {
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        if (node.parent == null) {
            sb.append("root").append(" ");
            sb.append(node.density.sum(true)).append("/");
            sb.append(node.density.sumExcept(node.bestIndex, true)).append(" ");
            sb.append(dict[node.bestIndex]).append(" (");
            DensityVector d = node.density.solidCopy();
            d.normalize(false);
            for (int i = 1; i < dict.length; i++) {
                sb.append(String.format("%.6f", d.get(i))).append(" ");
            }
            sb.append(") ");
            if (node.leaf) sb.append("*");
            sb.append("\n");

        } else {

            sb.append(node.groupName).append("  ");

            sb.append(node.density.sum(true)).append("/");
            sb.append(node.density.sumExcept(node.bestIndex, true)).append(" ");
            sb.append(dict[node.bestIndex]).append(" (");
            DensityVector d = node.density.solidCopy();
            d.normalize(false);
            for (int i = 1; i < dict.length; i++) {
                sb.append(String.format("%.6f", d.get(i))).append(" ");
            }
            sb.append(") ");
            if (node.leaf) sb.append("*");
            sb.append("\n");
        }

        // children

        if (!node.leaf) {
            node.children.stream().forEach(child -> buildSummary(sb, child, level + 1));
        }
    }

    // components

    // TEST COUNTER

    public static interface TestCounter extends Serializable {
        String name();

        void initialize(Frame df, String targetName);

        boolean canUse(String name);

        void markUse(String name);
    }

    public static enum TestCounters implements TestCounter {
        M_NOMINAL_M_NUMERIC {

            private Map<String, Integer> counters = new HashMap<>();

            @Override
            public void initialize(Frame df, String targetName) {
                counters = new HashMap<>();
                Arrays.stream(df.colNames()).forEach(colName -> {
                    if (targetName.equals(colName))
                        return;
                    counters.put(colName, -1);
                });
            }

            @Override
            public boolean canUse(String name) {
                return counters.containsKey(name);
            }

            @Override
            public void markUse(String name) {
                if (!counters.containsKey(name))
                    throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
            }
        },
        ONE_NOMINAL_M_NUMERIC {

            private Map<String, Integer> counters = new HashMap<>();

            @Override
            public void initialize(Frame df, String targetName) {
                counters = new HashMap<>();
                Arrays.stream(df.colNames()).forEach(colName -> {
                    if (targetName.equals(colName))
                        return;
                    if (df.col(colName).type().isNominal()) {
                        counters.put(colName, 1);
                    } else {
                        counters.put(colName, -1);
                    }
                });
            }

            @Override
            public boolean canUse(String name) {
                return counters.containsKey(name) && counters.get(name) != 0;
            }

            @Override
            public void markUse(String name) {
                if (!counters.containsKey(name))
                    throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
                int count = counters.get(name);
                if (count <= 0) return;
                counters.put(name, count - 1);
            }
        },
        ONE_NOMINAL_ONE_NUMERIC {

            private Map<String, Integer> counters = new HashMap<>();

            @Override
            public void initialize(Frame df, String targetName) {
                counters = new HashMap<>();
                Arrays.stream(df.colNames()).forEach(colName -> {
                    if (targetName.equals(colName))
                        return;
                    counters.put(colName, 1);
                });
            }

            @Override
            public boolean canUse(String name) {
                return counters.containsKey(name) && counters.get(name) != 0;
            }

            @Override
            public void markUse(String name) {
                if (!counters.containsKey(name))
                    throw new IllegalArgumentException("can't mark a column for use as a test if it was not initialized");
                int count = counters.get(name);
                if (count <= 0) return;
                counters.put(name, count - 1);
            }
        }
    }

    // PURITY FUNCTION

    public static interface Function extends Serializable {
        String name();

        double compute(DensityTable dt);

        int sign();
    }

    public static enum Functions implements Function {
        ENTROPY(1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getSplitEntropy(false);
            }
        },
        INFO_GAIN(-1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getInfoGain(false);
            }
        },
        GAIN_RATIO(-1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getGainRatio();
            }
        },
        GINI_GAIN(-1) {
            @Override
            public double compute(DensityTable dt) {
                return dt.getGiniIndex();
            }
        };
        private final int sign;

        private Functions(int sign) {
            this.sign = sign;
        }

        public int sign() {
            return sign;
        }
    }


    // NOMINAL METHOD

    public static interface NominalMethod extends Serializable {
        String name();

        List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function);
    }

    public static enum NominalMethods implements NominalMethod {
        IGNORE {
            @Override
            public List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                return new ArrayList<>();
            }
        },
        FULL {
            @Override
            public List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                List<Candidate> result = new ArrayList<>();
                Vector test = df.col(testColName);
                Vector target = df.col(targetColName);

                if (new DensityTable(test, target).countWithMinimum(false, c.getMinCount()) < 2) {
                    return result;
                }

                DensityTable dt = new DensityTable(test, target, df.getWeights());
                double value = function.compute(dt);

                Candidate candidate = new Candidate(value, function.sign());
                for (int i = 1; i < test.getDictionary().length; i++) {

                    final String label = test.getDictionary()[i];
                    candidate.addGroup(
                            String.format("%s == %s", testColName, label),
                            spot -> !spot.isMissing(testColName) && spot.getLabel(testColName).equals(label));
                }

                result.add(candidate);
                return result;
            }
        },
        BINARY {
            @Override
            public List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function) {

                List<Candidate> result = new ArrayList<>();
                Candidate best = null;
                for (int i = 1; i < df.col(testColName).getDictionary().length; i++) {
                    Vector test = df.col(testColName);
                    Vector target = df.col(targetColName);
                    String testLabel = df.col(testColName).getDictionary()[i];

                    if (new DensityTable(test, target).countWithMinimum(false, c.getMinCount()) < 2) {
                        return result;
                    }

                    DensityTable dt = new DensityTable(test, target, df.getWeights(), testLabel);
                    double value = function.compute(dt);
                    Candidate candidate = new Candidate(value, function.sign());
                    if (best == null) {
                        best = candidate;
                        best.addGroup(testColName + " == " + testLabel, spot -> spot.getLabel(testColName).equals(testLabel));
                        best.addGroup(testColName + " != " + testLabel, spot -> !spot.getLabel(testColName).equals(testLabel));
                    } else {
                        int comp = best.compareTo(candidate);
                        if (comp < 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                        best = candidate;
                        best.addGroup(testColName + " == " + testLabel, spot -> spot.getLabel(testColName).equals(testLabel));
                        best.addGroup(testColName + " != " + testLabel, spot -> !spot.getLabel(testColName).equals(testLabel));
                    }
                }
                if (best != null)
                    result.add(best);
                return result;
            }
        }
    }

    // NUMERIC METHOD

    public static interface NumericMethod extends Serializable {
        String name();

        List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function);
    }

    public static enum NumericMethods implements NumericMethod {
        IGNORE {
            @Override
            public List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
                return new ArrayList<>();
            }
        },
        BINARY {
            @Override
            public List<Candidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, Function function) {
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

                Candidate best = null;

                for (int i = 0; i < df.rowCount(); i++) {
                    int row = sort.getIndex(i);

                    if (test.isMissing(row)) continue;

                    dt.update(2, target.getIndex(row), -df.getWeight(row));
                    dt.update(1, target.getIndex(row), +df.getWeight(row));

                    if (i >= misCount + c.getMinCount() &&
                            i < df.rowCount() - 1 - c.getMinCount() &&
                            test.getValue(sort.getIndex(i)) < test.getValue(sort.getIndex(i + 1))) {

                        Candidate current = new Candidate(function.compute(dt), function.sign());
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

                            final double testValue = test.getValue(sort.getIndex(i));
                            current.addGroup(
                                    String.format("%s <= %.6f", testColName, testValue),
                                    spot -> !spot.isMissing(testColName) && spot.getValue(testColName) <= testValue);
                            current.addGroup(
                                    String.format("%s > %.6f", testColName, testValue),
                                    spot -> !spot.isMissing(testColName) && spot.getValue(testColName) > testValue);
                        }
                    }
                }

                List<Candidate> result = new ArrayList<>();
                if (best != null)
                    result.add(best);
                return result;
            }
        }
    }

    public static interface Splitter extends Serializable {
        String name();

        public List<Frame> performSplit(Frame df, Candidate candidate);
    }

    public static enum Splitters implements Splitter {
        REMAINS_IGNORED {
            @Override
            public List<Frame> performSplit(Frame df, Candidate candidate) {
                List<Mapping> mappings = new ArrayList<>();
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    mappings.add(new Mapping());
                }

                df.stream().forEach(fspot -> {
                    for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                        SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                        if (predicate.test(fspot)) {
                            mappings.get(i).add(fspot.rowId());
                            break;
                        }
                    }
                });
                List<Frame> frames = new ArrayList<>();
                mappings.stream().forEach(mapping -> {
                    frames.add(new MappedFrame(df.source(), mapping));
                });
                return frames;
            }
        },
        REMAINS_TO_MAJORITY {
            @Override
            public List<Frame> performSplit(Frame df, Candidate candidate) {
                List<Mapping> mappings = new ArrayList<>();
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    mappings.add(new Mapping());
                }

                List<FSpot> missingSpots = new LinkedList<>();
                df.stream().forEach(fspot -> {
                    for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                        SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                        if (predicate.test(fspot)) {
                            mappings.get(i).add(fspot.rowId());
                            return;
                        }
                    }
                    missingSpots.add(fspot);
                });
                int majorityGroup = 0;
                int majoritySize = 0;
                for (int i = 0; i < mappings.size(); i++) {
                    if (mappings.get(i).size() > majoritySize) {
                        majorityGroup = i;
                        majoritySize = mappings.get(i).size();
                    }
                }
                final int index = majorityGroup;
                missingSpots.stream().forEach(spot -> mappings.get(index).add(spot.rowId()));
                List<Frame> frames = new ArrayList<>();
                mappings.stream().forEach(mapping -> {
                    frames.add(new MappedFrame(df.source(), mapping));
                });
                return frames;
            }
        },
        REMAINS_TO_ALL_WEIGHTED {
            @Override
            public List<Frame> performSplit(Frame df, Candidate candidate) {
                List<Mapping> mappings = new ArrayList<>();
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    mappings.add(new Mapping());
                }

                final Set<Integer> missingSpots = new HashSet<>();
                df.stream().forEach(fspot -> {
                    for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                        SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                        if (predicate.test(fspot)) {
                            mappings.get(i).add(fspot.rowId());
                            return;
                        }
                    }
                    missingSpots.add(fspot.rowId());
                });
                final double[] p = new double[mappings.size()];
                double n = 0;
                for (int i = 0; i < mappings.size(); i++) {
                    p[i] = mappings.get(i).size();
                    n += p[i];
                }
                for (int i = 0; i < p.length; i++) {
                    p[i] /= n;
                }
                mappings.stream().forEach(mapping -> missingSpots.forEach(mapping::add));
                List<Frame> frames = new ArrayList<>();
                for (int i = 0; i < mappings.size(); i++) {
                    final int index = i;
                    Mapping mapping = mappings.get(i);
                    Frame f = new MappedFrame(df.source(), mapping);
                    f.stream().forEach(spot -> {
                        if (missingSpots.contains(spot.rowId()))
                            spot.setWeight(spot.getWeight() * p[index]);
                    });
                    frames.add(f);
                }
                return frames;
            }
        },
        REMAINS_TO_RANDOM {
            @Override
            public List<Frame> performSplit(Frame df, Candidate candidate) {
                List<Mapping> mappings = new ArrayList<>();
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    mappings.add(new Mapping());
                }

                final Set<Integer> missingSpots = new HashSet<>();
                df.stream().forEach(fspot -> {
                    for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                        SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                        if (predicate.test(fspot)) {
                            mappings.get(i).add(fspot.rowId());
                            return;
                        }
                    }
                    missingSpots.add(fspot.rowId());
                });
                missingSpots.forEach(rowId -> mappings.get(RandomSource.nextInt(mappings.size())).add(rowId));
                List<Frame> frames = new ArrayList<>();
                mappings.stream().forEach(mapping -> {
                    frames.add(new MappedFrame(df.source(), mapping));
                });
                return frames;
            }
        },
        REMAINS_WITH_SURROGATES {
            @Override
            public List<Frame> performSplit(Frame df, Candidate candidate) {
                // TODO partition tree classifier - remains surrogates
                throw new NotImplementedException();
            }
        }
    }

    public static interface Predictor extends Serializable {
        String name();

        Pair<Integer, DensityVector> predict(FSpot spot, CTreeNode node);
    }

    public static enum Predictors implements Predictor {
        STANDARD {
            @Override
            public Pair<Integer, DensityVector> predict(FSpot spot, CTreeNode node) {
                if (node.counter.sum(false) == 0)
                    return new Pair<>(node.parent.bestIndex, node.parent.density);
                if (node.leaf)
                    return new Pair<>(node.bestIndex, node.density);

                for (CTreeNode child : node.children) {
                    if (child.predicate.test(spot)) {
                        return predict(spot, child);
                    }
                }

                String[] dict = node.c.getDict();
                DensityVector dv = new DensityVector(dict);
                for (CTreeNode child : node.children) {
                    DensityVector d = predict(spot, child).second;
                    for (int i = 0; i < dict.length; i++) {
                        dv.update(i, d.get(i) * child.density.sum(false));
                    }
                }
                dv.normalize(false);
                return new Pair<>(dv.findBestIndex(), dv);
            }
        }
    }

    static class Candidate implements Comparable<Candidate>, Serializable {

        private final double score;
        private final int sign;
        private List<String> groupNames = new ArrayList<>();
        private List<SPredicate<FSpot>> groupPredicates = new ArrayList<>();

        public Candidate(double score, int sign) {
            this.score = score;
            this.sign = sign;
        }

        public void addGroup(String name, SPredicate<FSpot> predicate) {
            if (groupNames.contains(name)) {
                throw new IllegalArgumentException("group name already defined");
            }
            groupNames.add(name);
            groupPredicates.add(predicate);
        }

        public List<String> getGroupNames() {
            return groupNames;
        }

        public List<SPredicate<FSpot>> getGroupPredicates() {
            return groupPredicates;
        }

        @Override
        public int compareTo(Candidate o) {
            if (o == null) return -1;
            return new Double(score).compareTo(o.score) * sign;
        }
    }

    static class CTreeNode implements Serializable {
        final TreeClassifier c;
        final CTreeNode parent;
        final String groupName;
        final SPredicate<FSpot> predicate;

        boolean leaf = true;
        List<CTreeNode> children;
        DensityVector density;
        DensityVector counter;
        int bestIndex;
        Candidate bestCandidate;
        TreeMap<Candidate, String> candidates;

        public CTreeNode(final TreeClassifier c, final CTreeNode parent,
                         final String groupName, final SPredicate<FSpot> predicate) {
            this.parent = parent;
            this.c = c;
            this.groupName = groupName;
            this.predicate = predicate;
        }

        public boolean isLeaf() {
            return leaf;
        }

        public List<CTreeNode> getChildren() {
            return children;
        }

        public void learn(Frame df, int depth) {
            density = new DensityVector(df.col(c.getTargetCol()), df.getWeights());
            counter = new DensityVector(df.col(c.getTargetCol()), new Numeric(df.rowCount(), df.rowCount(), 1));
            bestIndex = density.findBestIndex();

            if (df.rowCount() == 0) {
                return;
            }

            if (df.rowCount() <= c.getMinCount() || counter.countValues(x -> x > 0) == 1 || depth < 1) {
                return;
            }

            candidates = new TreeMap<>();
            c.getColSelector().initialize(df, null);

            // here we have to implement some form of column selector for RF, ID3 and C4.5
            for (String testCol : c.getColSelector().nextColNames()) {
                if (testCol.equals(c.getTargetCol())) continue;
                if (!c.testCounter.canUse(testCol)) continue;

                if (df.col(testCol).type().isNumeric()) {
                    c.getNumericMethod().computeCandidates(c, df, testCol, c.getTargetCol(), c.getFunction())
                            .forEach(candidate -> candidates.put(candidate, testCol));
                } else {
                    c.getNominalMethod().computeCandidates(c, df, testCol, c.getTargetCol(), c.getFunction())
                            .forEach(candidate -> candidates.put(candidate, testCol));
                }
            }

            if (candidates.isEmpty()) {
                return;
            }
            leaf = false;

            bestCandidate = candidates.firstKey();
            c.testCounter.markUse(candidates.firstEntry().getValue());

            // now that we have a best candidate, do the effective split

            if (bestCandidate.getGroupNames().isEmpty()) {
                leaf = true;
                return;
            }

            List<Frame> frames = c.getSplitter().performSplit(df, bestCandidate);
            children = new ArrayList<>(frames.size());
            for (int i = 0; i < frames.size(); i++) {
                Frame f = frames.get(i);
                CTreeNode child = new CTreeNode(c, this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
                children.add(child);
                child.learn(f, depth - 1);
            }
        }
    }
}

