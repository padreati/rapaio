/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.experiment.ml.classifier.tree;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import rapaio.core.tools.*;
import rapaio.data.*;
import rapaio.ml.classifier.*;
import rapaio.ml.common.*;
import rapaio.ml.common.predicate.*;
import rapaio.printer.*;
import rapaio.printer.format.*;
import rapaio.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

/**
 * Tree classifier.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CTree extends AbstractClassifier implements DefaultPrintable {

    private static final long serialVersionUID = 1203926824359387358L;
    private static final Map<VType, CTreeTest> DEFAULT_TEST_MAP;

    static {
        DEFAULT_TEST_MAP = new HashMap<>();
        DEFAULT_TEST_MAP.put(VType.BINARY, CTreeTest.BinaryBinary);
        DEFAULT_TEST_MAP.put(VType.INT, CTreeTest.NumericBinary);
        DEFAULT_TEST_MAP.put(VType.DOUBLE, CTreeTest.NumericBinary);
        DEFAULT_TEST_MAP.put(VType.NOMINAL, CTreeTest.NominalBinary);
    }

    // parameter default values

    private int minCount = 1;
    private int maxDepth = -1;
    private double minGain = -1000;

    private VarSelector varSelector = VarSelector.all();
    private SortedMap<VType, CTreeTest> testMap = new TreeMap<>(DEFAULT_TEST_MAP);
    private CTreePurityFunction function = CTreePurityFunction.InfoGain;
    private CTreeSplitter splitter = CTreeSplitter.Ignored;
    private Tag<CTreePruning> pruning = CTreePruning.NONE;
    private Frame pruningDf = null;

    // tree root node
    private CTreeNode root;

    private transient Map<CTreeNode, Map<String, Mapping>> sortingCache = new HashMap<>();

    // static builders

    public CTree() {
    }

    public static CTree newID3() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.all())
                .withSplitter(CTreeSplitter.Ignored)
                .withTest(VType.NOMINAL, CTreeTest.NominalFull)
                .withTest(VType.DOUBLE, CTreeTest.Ignore)
                .withFunction(CTreePurityFunction.InfoGain)
                .withPruning(CTreePruning.NONE);
    }

    public static CTree newC45() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.all())
                .withSplitter(CTreeSplitter.ToAllWeighted)
                .withTest(VType.NOMINAL, CTreeTest.NominalFull)
                .withTest(VType.DOUBLE, CTreeTest.NumericBinary)
                .withFunction(CTreePurityFunction.GainRatio);
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withMinCount(1)
                .withVarSelector(VarSelector.all())
                .withSplitter(CTreeSplitter.ToAllWeighted)
                .withFunction(CTreePurityFunction.GainRatio)
                .withTest(VType.NOMINAL, CTreeTest.NominalBinary)
                .withTest(VType.DOUBLE, CTreeTest.NumericBinary);
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.all())
                .withSplitter(CTreeSplitter.ToAllWeighted)
                .withTest(VType.NOMINAL, CTreeTest.NominalBinary)
                .withTest(VType.DOUBLE, CTreeTest.NumericBinary)
                .withTest(VType.INT, CTreeTest.NumericBinary)
                .withFunction(CTreePurityFunction.GiniGain);
    }

    @Override
    public CTree newInstance() {
        CTree tree = newInstanceDecoration(new CTree())
                .withMinCount(minCount)
                .withMinGain(minGain)
                .withMaxDepth(maxDepth)
                .withFunction(function)
                .withSplitter(splitter)
                .withVarSelector(varSelector().newInstance());
        tree.testMap = new TreeMap<>(testMap);
        return tree;
    }

    public CTreeNode getRoot() {
        return root;
    }

    public VarSelector varSelector() {
        return varSelector;
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

    public double minGain() {
        return minGain;
    }

    public CTree withMinGain(double minGain) {
        this.minGain = minGain;
        return this;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public CTree withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public Map<VType, CTreeTest> testMap() {
        return testMap;
    }

    public CTree withNoTests() {
        this.testMap.clear();
        return this;
    }

    public CTree withTest(VType vType, CTreeTest test) {
        this.testMap.put(vType, test);
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

    public CTreePurityFunction getFunction() {
        return function;
    }

    public CTree withFunction(CTreePurityFunction function) {
        this.function = function;
        return this;
    }

    public CTreeSplitter getSplitter() {
        return splitter;
    }

    public CTree withSplitter(CTreeSplitter splitter) {
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
        sb.append("func=").append(function.name()).append(";");
        sb.append("split=").append(splitter.name()).append(";");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VType.NOMINAL, VType.INT, VType.DOUBLE, VType.BINARY)
                .withInputCount(1, 1_000_000)
                .withAllowMissingInputValues(true)
                .withTargetTypes(VType.NOMINAL)
                .withTargetCount(1, 1)
                .withAllowMissingTargetValues(false);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        additionalValidation(df);

        this.varSelector.withVarNames(inputNames());

        int rows = df.rowCount();

        // create the root node
        AtomicInteger idGenerator = new AtomicInteger();
        idGenerator.set(0);
        root = new CTreeNode(idGenerator.get(), null, "root", RowPredicate.all(), 0);

        Int2ObjectOpenHashMap<Frame> frameCache = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Var> weightCache = new Int2ObjectOpenHashMap<>();

        Queue<CTreeNode> queue = new ConcurrentLinkedQueue<>();
        queue.add(root);
        frameCache.put(root.getId(), df);
        weightCache.put(root.getId(), weights);

        while (!queue.isEmpty()) {
            CTreeNode node = queue.poll();

            Frame nodeDf = frameCache.get(node.getId());
            Var weightsDf = weightCache.get(node.getId());

            learnNode(node, nodeDf, weightsDf);

            if (node.isLeaf()) {
                continue;
            }
            CTreeCandidate bestCandidate = node.getBestCandidate();
            String testName = bestCandidate.getTestName();

            // now that we have a best candidate, do the effective split
            Pair<List<Frame>, List<Var>> frames = splitter.performSplit(nodeDf, weightsDf,
                    bestCandidate.getGroupPredicates());

            for (RowPredicate predicate : bestCandidate.getGroupPredicates()) {
                CTreeNode child = new CTreeNode(
                        idGenerator.incrementAndGet(), node, predicate.toString(), predicate, node.getDepth() + 1);
                node.getChildren().add(child);
            }
            for (int i = 0; i < node.getChildren().size(); i++) {
                CTreeNode child = node.getChildren().get(i);
                queue.add(child);
                frameCache.put(child.getId(), frames._1.get(i));
                weightCache.put(child.getId(), frames._2.get(i));
            }
        }

        pruning.get().prune(this, (pruningDf == null) ? df : pruningDf, false);
        return true;
    }

    private void learnNode(CTreeNode node, Frame df, Var weights) {
        node.density = DVector.fromWeights(false, df.rvar(firstTargetName()), weights);
        node.counter = DVector.fromCounts(false, df.rvar(firstTargetName()));
        node.bestIndex = node.density.findBestIndex();

        if (df.rowCount() == 0) {
            node.bestIndex = node.parent.bestIndex;
            return;
        }
        if (node.counter.countValues(x -> x > 0) == 1 ||
                (maxDepth > 0 && node.depth > maxDepth) || df.rowCount() <= minCount) {
            return;
        }

        String[] nextVarNames = varSelector.nextVarNames();
        List<CTreeCandidate> candidateList = new ArrayList<>();
        Queue<String> exhaustList = new ConcurrentLinkedQueue<>();

        if (runPoolSize() == 0) {
            int m = varSelector.mCount();
            for (String testCol : nextVarNames) {
                if (m <= 0) {
                    continue;
                }
                if (testCol.equals(firstTargetName())) {
                    continue;
                }

                CTreeTest test = null;
                if (testMap.containsKey(df.type(testCol))) {
                    test = testMap.get(df.type(testCol));
                }
                if (test == null) {
                    throw new IllegalArgumentException("can't predict ctree with no " +
                            "tests for given variable: " + testCol +
                            " [" + df.type(testCol).name() + "]");
                }
                CTreeCandidate candidate = test.computeCandidate(
                        this, df, weights, testCol, firstTargetName(), function);
                if (candidate != null) {
                    candidateList.add(candidate);
                    m--;
                } else {
                    exhaustList.add(testCol);
                }
            }
        } else {
            int m = varSelector.mCount();
            int start = 0;

            while (m > 0 && start < nextVarNames.length) {
                List<CTreeCandidate> next = IntStream.range(start, Math.min(nextVarNames.length, start + m))
                        .parallel()
                        .mapToObj(i -> nextVarNames[i])
                        .filter(testCol -> !testCol.equals(firstTargetName()))
                        .map(testCol -> {
                            CTreeTest test = null;
                            if (testMap.containsKey(df.type(testCol))) {
                                test = testMap.get(df.type(testCol));
                            }
                            if (test == null) {
                                throw new IllegalArgumentException("can't predict ctree with no " +
                                        "tests for given variable: " + testCol +
                                        " [" + df.type(testCol).name() + "]");
                            }
                            CTreeCandidate candidate = test.computeCandidate(
                                    this, df, weights, testCol, firstTargetName(), function);
                            if (candidate == null) {
                                exhaustList.add(testCol);
                            }
                            return candidate;
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                candidateList.addAll(next);
                start += m;
                m -= next.size();
            }
        }
        Collections.sort(candidateList);
        if (candidateList.isEmpty() || candidateList.get(0).getGroupPredicates().isEmpty()) {
            return;
        }
        // leave as leaf if the gain is not bigger than minimum gain
        if (candidateList.get(0).getScore() <= minGain) {
            return;
        }

        node.leaf = false;
        node.bestCandidate = candidateList.get(0);
        varSelector.removeVarNames(exhaustList);
    }

    public void prune(Frame df) {
        prune(df, false);
    }

    public void prune(Frame df, boolean all) {
        pruning.get().prune(this, df, all);
    }

    /**
     * Predict node indexes with one hot encoding, one var for each node
     */
    public Frame predictNodeIndexOHE(Frame df, String varPrefix) {
        Int2IntOpenHashMap indexMap = new Int2IntOpenHashMap();
        buildIndexMap(root, indexMap);

        List<Var> varList = new ArrayList<>();
        for (int i = 0; i < indexMap.size(); i++) {
            varList.add(VarBinary.fill(df.rowCount(), 0).withName(varPrefix + i));
        }
        for (int i = 0; i < df.rowCount(); i++) {
            varList.get(indexMap.get(predictPointNodeIndex(root, df, i))).setInt(i, 1);
        }
        return SolidFrame.byVars(varList);
    }

    /**
     * Predict node indexes with one hot encoding, one var for each node
     */
    public Frame predictNodeIndex(Frame df, boolean normalized, String varPrefix) {
        Int2IntOpenHashMap indexMap = new Int2IntOpenHashMap();
        buildIndexMap(root, indexMap);

        Var index = VarDouble.empty(df.rowCount()).withName(varPrefix + "index");
        double norm = normalized ? indexMap.size() : 1.0;
        for (int i = 0; i < df.rowCount(); i++) {
            index.setDouble(i, indexMap.get(predictPointNodeIndex(root, df, i)) / norm);
        }
        return SolidFrame.byVars(index);
    }

    private int predictPointNodeIndex(CTreeNode node, Frame df, int row) {
        if (node.isLeaf()) {
            return node.getId();
        }
        double maxWeight = Double.NEGATIVE_INFINITY;
        CTreeNode maxChild = null;
        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(row, df)) {
                return this.predictPointNodeIndex(child, df, row);
            }
            if (maxWeight < child.counter.sum()) {
                maxChild = child;
                maxWeight = child.counter.sum();
            }
        }
        // if missing value
        return predictPointNodeIndex(maxChild, df, row);
    }

    private void buildIndexMap(CTreeNode node, Int2IntOpenHashMap indexMap) {
        if (node.isLeaf()) {
            indexMap.put(node.getId(), indexMap.size());
        }
        for (CTreeNode child : node.children) {
            buildIndexMap(child, indexMap);
        }
    }

    @Override
    protected ClassResult corePredict(Frame df, boolean withClasses, boolean withDensities) {
        ClassResult prediction = ClassResult.build(this, df, withClasses, withDensities);
        for (int i = 0; i < df.rowCount(); i++) {
            Pair<Integer, DVector> res = predictPoint(this, root, i, df);
            int index = res._1;
            DVector dv = res._2;
            if (withClasses)
                prediction.firstClasses().setInt(i, index);
            if (withDensities)
                for (int j = 0; j < firstTargetLevels().size(); j++) {
                    prediction.firstDensity().setDouble(i, j, dv.get(j));
                }
        }
        return prediction;
    }

    protected Pair<Integer, DVector> predictPoint(CTree tree, CTreeNode node, int row, Frame df) {
        if (node.isLeaf())
            return Pair.from(node.getBestIndex(), node.getDensity().copy().normalize());

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(row, df)) {
                return this.predictPoint(tree, child, row, df);
            }
        }

        List<String> dict = tree.firstTargetLevels();
        DVector dv = DVector.empty(false, dict);
        double w = 0.0;
        for (CTreeNode child : node.getChildren()) {
            DVector d = this.predictPoint(tree, child, row, df)._2;
            double wc = child.getDensity().sum();
            dv.plus(d, wc);
            w += wc;
        }
        for (int i = 0; i < dict.size(); i++) {
            dv.set(i, dv.get(i) / w);
        }
        return Pair.from(dv.findBestIndex(), dv);
    }

    private void additionalValidation(Frame df) {
        df.varStream().forEach(var -> {
            if (testMap.containsKey(var.type()))
                return;
            throw new IllegalArgumentException("can't predict ctree with no " +
                    "tests for given variable: " + var.name() +
                    " [" + var.type().name() + "]");
        });
    }

    public int countNodes(boolean onlyLeaves) {
        int count = 0;
        LinkedList<CTreeNode> nodes = new LinkedList<>();
        nodes.addLast(root);
        while (!nodes.isEmpty()) {
            CTreeNode node = nodes.pollFirst();
            count += onlyLeaves ? (node.isLeaf() ? 1 : 0) : 1;
            node.getChildren().forEach(nodes::addLast);
        }
        return count;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CTree model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName().replaceAll(";", ";\n")).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        sb.append("\n");

        int nodeCount = 0;
        int leaveCount = 0;
        LinkedList<CTreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            CTreeNode node = queue.pollFirst();
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

    private void buildSummary(StringBuilder sb, CTreeNode node, int level) {

        sb.append(level == 0 ? "|- " : "|");
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "   |- " : "   |");
        }
        sb.append(node.getId()).append(". ").append(node.getGroupName()).append("    ");
        sb.append(Format.floatFlexShort(node.getCounter().sum())).append("/");
        sb.append(Format.floatFlexShort(node.getCounter().sumExcept(node.getBestIndex()))).append(" ");
        sb.append(firstTargetLevels().get(node.getBestIndex())).append(" (");
        DVector d = node.getDensity().copy().normalize();
        for (int i = 1; i < firstTargetLevels().size(); i++) {
            sb.append(Format.floatFlexShort(d.get(i))).append(" ");
        }
        sb.append(") ");
        if (node.isLeaf()) {
            sb.append("*");
        } else {
            sb.append("[").append(Format.floatFlex(node.getBestCandidate().getScore())).append("]");
        }
        sb.append("\n");

        node.getChildren().forEach(child -> buildSummary(sb, child, level + 1));
    }

}

