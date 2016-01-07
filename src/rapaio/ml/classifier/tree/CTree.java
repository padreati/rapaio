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
import rapaio.core.tools.DVector;
import rapaio.data.*;
import rapaio.data.filter.FFilter;
import rapaio.data.filter.VFRefSort;
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.sys.WS;
import rapaio.util.FJPool;
import rapaio.util.Pair;
import rapaio.util.Tag;
import rapaio.util.Tagged;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

/**
 * Tree classifier.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CTree extends AbstractClassifier {

    private static final long serialVersionUID = 1203926824359387358L;

    // parameter default values

    private int minCount = 1;
    private int maxDepth = -1;

    private VarSelector varSelector = VarSelector.ALL;
    private Map<String, PurityTest> customTestMap = new HashMap<>();
    private Map<VarType, PurityTest> testMap = new HashMap<>();
    private PurityFunction function = PurityFunction.InfoGain;
    private MissingHandler splitter = MissingHandler.Ignored;
    private Tag<CTreePruning> pruning = CTreePruning.NONE;
    private Frame pruningDf = null;

    // tree root node
    private Node root;

    private transient Map<Node, Map<String, Mapping>> sortingCache = new HashMap<>();

    // static builders

    public CTree() {
        testMap.put(VarType.BINARY, PurityTest.BinaryBinary);
        testMap.put(VarType.ORDINAL, PurityTest.NumericBinary);
        testMap.put(VarType.INDEX, PurityTest.NumericBinary);
        testMap.put(VarType.NUMERIC, PurityTest.NumericBinary);
        testMap.put(VarType.NOMINAL, PurityTest.NominalBinary);
        withRuns(0);
    }

    public static CTree newID3() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(MissingHandler.Ignored)
                .withTest(VarType.NOMINAL, PurityTest.NominalFull)
                .withTest(VarType.NUMERIC, PurityTest.Ignore)
                .withFunction(CTree.PurityFunction.InfoGain)
                .withPruning(CTreePruning.NONE);
    }

    public static CTree newC45() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(MissingHandler.ToAllWeighted)
                .withTest(VarType.NOMINAL, PurityTest.NominalFull)
                .withTest(VarType.NUMERIC, PurityTest.NumericBinary)
                .withFunction(PurityFunction.GainRatio);
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(MissingHandler.ToAllWeighted)
                .withFunction(PurityFunction.GainRatio)
                .withTest(VarType.NOMINAL, PurityTest.NominalBinary)
                .withTest(VarType.NUMERIC, PurityTest.NumericBinary);
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(MissingHandler.ToAllWeighted)
                .withTest(VarType.NOMINAL, PurityTest.NominalBinary)
                .withTest(VarType.NUMERIC, PurityTest.NumericBinary)
                .withTest(VarType.INDEX, PurityTest.NumericBinary)
                .withFunction(PurityFunction.GiniGain);
    }

    @Override
    public CTree newInstance() {
        CTree tree = (CTree) new CTree()
                .withMinCount(minCount)
                .withMaxDepth(maxDepth)
                .withFunction(function)
                .withMissingHandler(splitter)
                .withVarSelector(varSelector().newInstance())
                .withRunningHook(runningHook())
                .withSampler(sampler());

        tree.withRunPoolSize(runPoolSize());
        tree.withRuns(runs());
        tree.testMap.clear();
        tree.testMap.putAll(testMap);

        tree.customTestMap.clear();
        tree.customTestMap.putAll(customTestMap);
        return tree;
    }

    public Node getRoot() {
        return root;
    }

    public VarSelector varSelector() {
        return varSelector;
    }

    public CTree withMCols(int mcols) {
        this.varSelector = new VarSelector(mcols);
        return this;
    }

    public CTree withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    public int minCount() {
        return minCount;
    }

    public CTree withMinCount(int minCount) {
        if (minCount < 1) {
            throw new IllegalArgumentException("min cont must be an integer positive number");
        }
        this.minCount = minCount;
        return this;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public CTree withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public CTree withTest(VarType varType, PurityTest test) {
        this.testMap.put(varType, test);
        return this;
    }

    public CTree withTest(String varName, PurityTest test) {
        this.customTestMap.put(varName, test);
        return this;
    }

    public Map<VarType, PurityTest> testMap() {
        return testMap;
    }

    public Map<String, PurityTest> customTestMap() {
        return customTestMap;
    }

    public CTree withNoTests() {
        this.testMap.clear();
        return this;
    }

    public CTree withPruning(Tag<CTreePruning> pruning) {
        return withPruning(pruning, null);
    }

    public CTree withPruning(Tag<CTreePruning> pruning, Frame pruningDf) {
        this.pruning = pruning;
        this.pruningDf = pruningDf;
        return this;
    }

    public PurityFunction getFunction() {
        return function;
    }

    public CTree withFunction(PurityFunction function) {
        this.function = function;
        return this;
    }

    public MissingHandler getMissingHandler() {
        return splitter;
    }

    public CTree withMissingHandler(MissingHandler splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    public String name() {
        return "CTree";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("CTree {");
        sb.append("varSelector=").append(varSelector().name()).append(";");
        sb.append("minCount=").append(minCount).append(";");
        sb.append("maxDepth=").append(maxDepth).append(";");
        sb.append("tests=").append(testMap.entrySet().stream()
                .map(e -> e.getKey().name() + ":" + e.getValue().name()).collect(joining(","))
        ).append(";");
        if (!customTestMap.isEmpty())
            sb.append("customTest=").append(customTestMap.entrySet().stream()
                    .map(e -> e.getKey() + ":" + e.getValue().name()).collect(joining(","))
            ).append(";");
        sb.append("func=").append(function.name()).append(";");
        sb.append("split=").append(splitter.name()).append(";");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VarType.NOMINAL, VarType.INDEX, VarType.NUMERIC, VarType.BINARY)
                .withInputCount(1, 1_000_000)
                .withAllowMissingInputValues(true)
                .withTargetTypes(VarType.NOMINAL)
                .withTargetCount(1, 1)
                .withAllowMissingTargetValues(false);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        additionalValidation(df);

        this.varSelector.withVarNames(inputNames());

        int rows = df.rowCount();
        root = new Node(null, "root", spot -> true);
        if (runPoolSize() == 0) {
            root.learn(this, df, weights, maxDepth() < 0 ? Integer.MAX_VALUE : maxDepth());
        } else {
            FJPool.run(runPoolSize(), () -> root.learn(this, df, weights, maxDepth < 0 ? Integer.MAX_VALUE : maxDepth));
        }
        this.root.fillId(1);
        pruning.get().prune(this, (pruningDf == null) ? df : pruningDf, false);
        return true;
    }

    public void prune(Frame df) {
        prune(df, false);
    }

    public void prune(Frame df, boolean all) {
        pruning.get().prune(this, df, all);
    }

    @Override
    protected CFit coreFit(Frame df, boolean withClasses, boolean withDensities) {
        CFit prediction = CFit.build(this, df, withClasses, withDensities);
        df.stream().forEach(spot -> {
            Pair<Integer, DVector> result = fitPoint(this, spot, root);
            if (withClasses)
                prediction.firstClasses().setIndex(spot.row(), result._1);
            if (withDensities)
                for (int j = 0; j < firstTargetLevels().length; j++) {
                    prediction.firstDensity().setValue(spot.row(), j, result._2.get(j));
                }
        });
        return prediction;
    }

    protected Pair<Integer, DVector> fitPoint(CTree tree, FSpot spot, Node node) {
        if (node.getCounter().sum() == 0)
            if (node.getParent() == null) {
                throw new RuntimeException("Something bad happened at learning time");
            } else {
                return Pair.from(node.getParent().getBestIndex(), node.getParent().getDensity());
            }
        if (node.isLeaf())
            return Pair.from(node.getBestIndex(), node.getDensity());

        for (Node child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                return this.fitPoint(tree, spot, child);
            }
        }

        String[] dict = tree.firstTargetLevels();
        DVector dv = DVector.newEmpty(false, dict);
        for (Node child : node.getChildren()) {
            DVector d = this.fitPoint(tree, spot, child)._2;
            for (int i = 0; i < dict.length; i++) {
                dv.increment(i, d.get(i));
            }
        }
        dv.normalize();
        return Pair.from(dv.findBestIndex(), dv);
    }

    private void additionalValidation(Frame df) {
        df.varStream().forEach(var -> {
            if (customTestMap.containsKey(var.name()))
                return;
            if (testMap.containsKey(var.type()))
                return;
            throw new IllegalArgumentException("can't train ctree with no " +
                    "tests for given variable: " + var.name() +
                    " [" + var.type().name() + "]");
        });
    }

    public int countNodes(boolean onlyLeaves) {
        int count = 0;
        LinkedList<CTree.Node> nodes = new LinkedList<>();
        nodes.addLast(root);
        while (!nodes.isEmpty()) {
            CTree.Node node = nodes.pollFirst();
            count += onlyLeaves ? (node.isLeaf() ? 1 : 0) : 1;
            node.getChildren().forEach(nodes::addLast);
        }
        return count;
    }

    @Override
    public CTree withInputFilters(FFilter... filters) {
        return (CTree) super.withInputFilters(filters);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CTree model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName().replaceAll(";", ";\n")).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().summary()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        sb.append("\n");

        int nodeCount = 0;
        int leaveCount = 0;
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            nodeCount++;
            if (node.isLeaf())
                leaveCount++;
            node.getChildren().forEach(queue::addLast);
        }

        sb.append("total number of nodes: ").append(nodeCount).append("\n");
        sb.append("total number of leaves: ").append(leaveCount).append("\n");
        sb.append("description:\n");
        sb.append("split, n/err, classes (densities) [* if is leaf / purity if not]\n\n");

        buildSummary(sb, root, 0);

        return sb.toString();

    }

    private void buildSummary(StringBuilder sb, Node node, int level) {

        sb.append(level == 0 ? "|- " : "|");
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "   |- " : "   |");
        }
        sb.append(node.getId()).append(". ").append(node.getGroupName()).append("    ");
        sb.append(WS.formatFlexShort(node.getCounter().sum())).append("/");
        sb.append(WS.formatFlexShort(node.getCounter().sumExcept(node.getBestIndex()))).append(" ");
        sb.append(firstTargetLevels()[node.getBestIndex()]).append(" (");
        DVector d = node.getDensity().solidCopy().normalize();
        for (int i = 1; i < firstTargetLevels().length; i++) {
            sb.append(WS.formatFlexShort(d.get(i))).append(" ");
        }
        sb.append(") ");
        if (node.isLeaf()) {
            sb.append("*");
        } else {
            sb.append("[").append(WS.formatFlex(node.getBestCandidate().getScore())).append("]");
        }
        sb.append("\n");

        node.getChildren().stream().forEach(child -> buildSummary(sb, child, level + 1));
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
     */
    public interface PurityFunction extends Tagged, Serializable {

        PurityFunction InfoGain = new PurityFunction() {
            private static final long serialVersionUID = 152790997381399918L;

            @Override
            public double compute(DTable dt) {
                return dt.splitByRowInfoGain();
            }

            @Override
            public String name() {
                return "InfoGain";
            }
        };
        PurityFunction GainRatio = new PurityFunction() {
            private static final long serialVersionUID = -2478996054579932911L;

            @Override
            public double compute(DTable dt) {
                return dt.splitByRowGainRatio();
            }

            @Override
            public String name() {
                return "GainRatio";
            }
        };
        PurityFunction GiniGain = new PurityFunction() {
            private static final long serialVersionUID = 3547209320599654633L;

            @Override
            public double compute(DTable dt) {
                return dt.splitByRowGiniGain();
            }

            @Override
            public String name() {
                return "GiniGain";
            }
        };

        double compute(DTable dt);
    }

    /**
     * Impurity test implementation
     * <p>
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/15.
     */
    public interface PurityTest extends Tagged, Serializable {

        PurityTest Ignore = new PurityTest() {

            private static final long serialVersionUID = 2862814158096438654L;

            @Override
            public String name() {
                return "Ignore";
            }

            @Override
            public Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTree.PurityFunction function) {
                return null;
            }

        };
        PurityTest NumericBinary = new PurityTest() {
            private static final long serialVersionUID = -2093990830002355963L;

            @Override
            public String name() {
                return "NumericBinary";
            }

            @Override
            public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTree.PurityFunction function) {
                Var test = df.var(testName);
                Var target = df.var(targetName);

                DTable dt = DTable.newEmpty(DTable.NUMERIC_DEFAULT_LABELS, target.levels(), false);
                int misCount = 0;
                for (int i = 0; i < df.rowCount(); i++) {
                    int row = (test.missing(i)) ? 0 : 2;
                    if (test.missing(i)) misCount++;
                    dt.update(row, target.index(i), weights.value(i));
                }

                Var sort = new VFRefSort(RowComparators.numeric(test, true)).fitApply(Index.seq(df.rowCount()));

                Candidate best = null;
                double bestScore = 0.0;

                for (int i = 0; i < df.rowCount(); i++) {
                    int row = sort.index(i);

                    if (test.missing(row)) continue;

                    dt.update(2, target.index(row), -weights.value(row));
                    dt.update(1, target.index(row), +weights.value(row));

                    if (i >= misCount + c.minCount() - 1 &&
                            i < df.rowCount() - c.minCount() &&
                            test.value(sort.index(i)) < test.value(sort.index(i + 1))) {

                        double currentScore = function.compute(dt);
                        if (best != null) {
                            int comp = Double.compare(bestScore, currentScore);
                            if (comp > 0) continue;
                            if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                        }
                        best = new Candidate(bestScore, testName);
                        double testValue = (test.value(sort.index(i)) + test.value(sort.index(i + 1))) / 2.0;
                        best.addGroup(
                                String.format("%s <= %s", testName, WS.formatFlex(testValue)),
                                spot -> !spot.missing(testName) && spot.value(testName) <= testValue);
                        best.addGroup(
                                String.format("%s > %s", testName, WS.formatFlex(testValue)),
                                spot -> !spot.missing(testName) && spot.value(testName) > testValue);

                        bestScore = currentScore;
                    }
                }
                return best;
            }
        };
        PurityTest BinaryBinary = new PurityTest() {

            private static final long serialVersionUID = 1771541941375729870L;

            @Override
            public String name() {
                return "BinaryBinary";
            }

            @Override
            public Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, CTree.PurityFunction function) {

                Var test = df.var(testName);
                Var target = df.var(targetName);
                DTable dt = DTable.newFromCounts(test, target, false);
                if (!(dt.hasColsWithMinimumCount(c.minCount(), 2))) {
                    return null;
                }

                Candidate best = new Candidate(function.compute(dt), testName);
                best.addGroup(testName + " == 1", spot -> spot.binary(testName));
                best.addGroup(testName + " != 1", spot -> !spot.binary(testName));
                return best;

            }
        };
        PurityTest NominalFull = new PurityTest() {
            private static final long serialVersionUID = 2261155834044153945L;

            @Override
            public String name() {
                return "NominalFull";
            }

            @Override
            public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, CTree.PurityFunction function) {
                Var test = df.var(testName);
                Var target = df.var(targetName);

                if (!DTable.newFromCounts(test, target, false).hasColsWithMinimumCount(c.minCount(), 2)) {
                    return null;
                }

                DTable dt = DTable.newFromWeights(test, target, weights, false);
                double value = function.compute(dt);

                Candidate candidate = new Candidate(value, testName);
                for (int i = 1; i < test.levels().length; i++) {
                    final String label = test.levels()[i];
                    candidate.addGroup(
                            String.format("%s == %s", testName, label),
                            spot -> !spot.missing(testName) && spot.label(testName).equals(label));
                }
                return candidate;
            }

        };
        PurityTest NominalBinary = new PurityTest() {

            private static final long serialVersionUID = -1257733788317891040L;

            @Override
            public String name() {
                return "Nominal_Binary";
            }

            @Override
            public Candidate computeCandidate(CTree c, Frame df, Var weights, String testName, String targetName, PurityFunction function) {
                Var test = df.var(testName);
                Var target = df.var(targetName);
                DTable counts = DTable.newFromCounts(test, target, false);
                if (!(counts.hasColsWithMinimumCount(c.minCount(), 2))) {
                    return null;
                }

                Candidate best = null;
                double bestScore = 0.0;

                int[] termCount = new int[test.levels().length];
                test.stream().forEach(s -> termCount[s.index()]++);

                double[] rowCounts = counts.rowTotals();
                for (int i = 1; i < test.levels().length; i++) {
                    if (rowCounts[i] < c.minCount())
                        continue;

                    String testLabel = df.var(testName).levels()[i];

                    DTable dt = DTable.newBinaryFromWeights(test, target, weights, testLabel, false);
                    double currentScore = function.compute(dt);
                    if (best != null) {
                        int comp = Double.compare(bestScore, currentScore);
                        if (comp > 0) continue;
                        if (comp == 0 && RandomSource.nextDouble() > 0.5) continue;
                    }
                    best = new Candidate(currentScore, testName);
                    best.addGroup(testName + " == " + testLabel, spot -> spot.label(testName).equals(testLabel));
                    best.addGroup(testName + " != " + testLabel, spot -> !spot.label(testName).equals(testLabel));
                }
                return best;
            }
        };

        Candidate computeCandidate(CTree c, Frame df, Var w, String testName, String targetName, PurityFunction function);
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
     */
    public interface MissingHandler extends Tagged, Serializable {

        MissingHandler Ignored = new MissingHandler() {
            private static final long serialVersionUID = -9017265383541294518L;

            @Override
            public String name() {
                return "Ignored";
            }

            @Override
            public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, Candidate candidate) {
                List<SPredicate<FSpot>> p = candidate.getGroupPredicates();
                List<Mapping> mappings = IntStream.range(0, p.size()).boxed().map(i -> Mapping.empty()).collect(toList());

                df.stream().forEach(s -> {
                    for (int i = 0; i < p.size(); i++) {
                        if (p.get(i).test(s)) {
                            mappings.get(i).add(s.row());
                            break;
                        }
                    }
                });
                return Pair.from(
                        mappings.stream().map(df::mapRows).collect(toList()),
                        mappings.stream().map(weights::mapRows).collect(toList())
                );
            }

        };
        MissingHandler ToMajority = new MissingHandler() {
            private static final long serialVersionUID = -5858151664805703831L;

            @Override
            public String name() {
                return "ToMajority";
            }

            @Override
            public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, Candidate candidate) {
                List<SPredicate<FSpot>> p = candidate.getGroupPredicates();
                List<Mapping> mappings = IntStream.range(0, p.size()).boxed().map(i -> Mapping.empty()).collect(toList());

                List<Integer> missingSpots = new LinkedList<>();
                df.stream().forEach(s -> {
                    for (int i = 0; i < p.size(); i++) {
                        if (p.get(i).test(s)) {
                            mappings.get(i).add(s.row());
                            return;
                        }
                    }
                    missingSpots.add(s.row());
                });
                List<Integer> lens = mappings.stream().map(Mapping::size).collect(toList());
                Collections.shuffle(lens);
                int majorityGroup = 0;
                int majoritySize = 0;
                for (int i = 0; i < mappings.size(); i++) {
                    if (mappings.get(i).size() > majoritySize) {
                        majorityGroup = i;
                        majoritySize = mappings.get(i).size();
                    }
                }
                final int index = majorityGroup;

                mappings.get(index).addAll(missingSpots);

                return Pair.from(
                        mappings.stream().map(df::mapRows).collect(toList()),
                        mappings.stream().map(weights::mapRows).collect(toList())
                );
            }
        };
        MissingHandler ToAllWeighted = new MissingHandler() {
            private static final long serialVersionUID = 5936044048099571710L;

            @Override
            public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, Candidate candidate) {
                List<SPredicate<FSpot>> pred = candidate.getGroupPredicates();
                List<Mapping> mappings = IntStream.range(0, pred.size()).boxed().map(i -> Mapping.empty()).collect(toList());

                List<Integer> missingSpots = new ArrayList<>();
                df.stream().forEach(s -> {
                    for (int i = 0; i < pred.size(); i++) {
                        if (pred.get(i).test(s)) {
                            mappings.get(i).add(s.row());
                            return;
                        }
                    }
                    missingSpots.add(s.row());
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
                List<Var> weightsList = mappings.stream().map(weights::mapRows).map(Var::solidCopy).collect(toList());
                for (int i = 0; i < mappings.size(); i++) {
                    final int ii = i;
                    missingSpots.forEach(row -> {
                        mappings.get(ii).add(row);
                        weightsList.get(ii).addValue(weights.missing(row) ? p[ii] : weights.value(row) * p[ii]);
                    });
                }
                List<Frame> frames = mappings.stream().map(df::mapRows).collect(toList());
                return Pair.from(frames, weightsList);
            }

            @Override
            public String name() {
                return "ToAllWeighted";
            }
        };
        MissingHandler ToRandom = new MissingHandler() {
            private static final long serialVersionUID = -4762758695801141929L;

            @Override
            public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, Candidate candidate) {
                List<SPredicate<FSpot>> pred = candidate.getGroupPredicates();
                List<Mapping> mappings = IntStream.range(0, pred.size()).boxed().map(i -> Mapping.empty()).collect(toList());

                final Set<Integer> missingSpots = new HashSet<>();
                df.stream().forEach(s -> {
                    for (int i = 0; i < pred.size(); i++) {
                        if (pred.get(i).test(s)) {
                            mappings.get(i).add(s.row());
                            return;
                        }
                    }
                    missingSpots.add(s.row());
                });
                missingSpots.forEach(rowId -> mappings.get(RandomSource.nextInt(mappings.size())).add(rowId));
                List<Frame> frameList = mappings.stream().map(df::mapRows).collect(toList());
                List<Var> weightList = mappings.stream().map(weights::mapRows).collect(toList());
                return Pair.from(frameList, weightList);
            }

            @Override
            public String name() {
                return "ToRandom";
            }
        };

        Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, Candidate candidate);
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
     */
    public static class Candidate implements Comparable<Candidate>, Serializable {

        private static final long serialVersionUID = -1547847207988912332L;

        private final double score;
        private final String testName;
        private final List<String> groupNames = new ArrayList<>();
        private final List<SPredicate<FSpot>> groupPredicates = new ArrayList<>();

        public Candidate(double score, String testName) {
            this.score = score;
            this.testName = testName;
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

        public double getScore() {
            return score;
        }

        public String getTestName() {
            return testName;
        }

        @Override
        public int compareTo(Candidate o) {
            if (o == null) return 1;
            return -(Double.compare(score, o.score));
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
     */
    public static class Node implements Serializable {

        private static final long serialVersionUID = -5045581827808911763L;
        private final Node parent;
        private final String groupName;
        private final SPredicate<FSpot> predicate;
        private final List<Node> children = new ArrayList<>();
        private int id;
        private boolean leaf = true;
        private DVector density;
        private DVector counter;
        private int bestIndex;
        private Candidate bestCandidate;

        public Node(final Node parent, final String groupName, final SPredicate<FSpot> predicate) {
            this.parent = parent;
            this.groupName = groupName;
            this.predicate = predicate;
        }

        public Node getParent() {
            return parent;
        }

        public int getId() {
            return id;
        }

        public String getGroupName() {
            return groupName;
        }

        public Predicate<FSpot> getPredicate() {
            return predicate;
        }

        public DVector getCounter() {
            return counter;
        }

        public int getBestIndex() {
            return bestIndex;
        }

        public DVector getDensity() {
            return density;
        }

        public boolean isLeaf() {
            return leaf;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Candidate getBestCandidate() {
            return bestCandidate;
        }

        public int fillId(int index) {
            id = index;
            int next = index;
            for (Node child : getChildren()) {
                next = child.fillId(next + 1);
            }
            return next;
        }

        public void cut() {
            leaf = true;
            children.clear();
        }

        public void learn(CTree tree, Frame df, Var weights, int depth) {
            density = DVector.newFromWeights(false, df.var(tree.firstTargetName()), weights);
            counter = DVector.newFromCount(false, df.var(tree.firstTargetName()));
            bestIndex = density.findBestIndex();

            if (df.rowCount() == 0) {
                bestIndex = parent.bestIndex;
                return;
            }
            if (counter.countValues(x -> x > 0) == 1 || depth < 1 || df.rowCount() <= tree.minCount()) {
                return;
            }

            VarSelector varSel = tree.varSelector();
            String[] nextVarNames = varSel.nextAllVarNames();
            List<Candidate> candidateList = new ArrayList<>();
            Queue<String> exhaustList = new ConcurrentLinkedQueue<>();

            if (tree.runPoolSize() == 0) {
                int m = varSel.mCount();
                for (String testCol : nextVarNames) {
                    if (m <= 0) {
                        continue;
                    }
                    if (testCol.equals(tree.firstTargetName())) {
                        continue;
                    }

                    PurityTest test = null;
                    if (tree.customTestMap().containsKey(testCol)) {
                        test = tree.customTestMap().get(testCol);
                    }
                    if (tree.testMap().containsKey(df.var(testCol).type())) {
                        test = tree.testMap().get(df.var(testCol).type());
                    }
                    if (test == null) {
                        throw new IllegalArgumentException("can't train ctree with no " +
                                "tests for given variable: " + df.var(testCol).name() +
                                " [" + df.var(testCol).type().name() + "]");
                    }
                    Candidate candidate = test.computeCandidate(
                            tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction());
                    if (candidate != null) {
                        candidateList.add(candidate);
                        m--;
                    } else {
                        exhaustList.add(testCol);
                    }
                }
            } else {
                int m = varSel.mCount();
                int start = 0;

                while (m > 0 && start < nextVarNames.length) {
                    List<Candidate> next = IntStream.range(start, Math.min(nextVarNames.length, start + m))
                            .parallel()
                            .mapToObj(i -> nextVarNames[i])
                            .filter(testCol -> !testCol.equals(tree.firstTargetName()))
                            .map(testCol -> {
                                PurityTest test = null;
                                if (tree.customTestMap().containsKey(testCol)) {
                                    test = tree.customTestMap().get(testCol);
                                }
                                if (tree.testMap().containsKey(df.var(testCol).type())) {
                                    test = tree.testMap().get(df.var(testCol).type());
                                }
                                if (test == null) {
                                    throw new IllegalArgumentException("can't train ctree with no " +
                                            "tests for given variable: " + df.var(testCol).name() +
                                            " [" + df.var(testCol).type().name() + "]");
                                }
                                Candidate candidate = test.computeCandidate(
                                        tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction());
                                if (candidate == null) {
                                    exhaustList.add(testCol);
                                }
                                return candidate;
                            })
                            .filter(c -> c != null)
                            .collect(Collectors.toList());
                    candidateList.addAll(next);
                    start += m;
                    m -= next.size();
                }
            }
            Collections.sort(candidateList);
            if (candidateList.isEmpty() || candidateList.get(0).getGroupNames().isEmpty()) {
                return;
            }

            leaf = false;
            bestCandidate = candidateList.get(0);
            String testName = bestCandidate.getTestName();

            // now that we have a best candidate, do the effective split
            Pair<List<Frame>, List<Var>> frames = tree.getMissingHandler().performSplit(df, weights, bestCandidate);

            for (int i = 0; i < bestCandidate.getGroupNames().size(); i++) {
                Node child = new Node(this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
                children.add(child);
            }
            tree.varSelector.removeVarNames(exhaustList);
            for (int i = 0; i < children.size(); i++) {
                children.get(i).learn(tree, frames._1.get(i), frames._2.get(i), depth - 1);
            }
            tree.varSelector.addVarNames(exhaustList);
        }
    }
}

