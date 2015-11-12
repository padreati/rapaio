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

import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.sys.WS;
import rapaio.util.FJPool;
import rapaio.util.Pair;
import rapaio.util.Tag;
import rapaio.util.Util;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

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
    private Map<String, Tag<CTreeTest>> customTestMap = new HashMap<>();
    private Map<VarType, Tag<CTreeTest>> testMap = new HashMap<>();
    private Tag<CTreeFunction> function = CTreeFunction.InfoGain;
    private Tag<CTreeMissingHandler> splitter = CTreeMissingHandler.Ignored;
    private Tag<CTreePruning> pruning = CTreePruning.NONE;
    private Frame pruningDf = null;

    // tree root node
    private Node root;

    // static builders

    public static CTree newID3() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.Ignored)
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Full)
                .withTest(VarType.NUMERIC, CTreeTest.Ignore)
                .withFunction(CTreeFunction.InfoGain)
                .withPruning(CTreePruning.NONE);
    }

    public static CTree newC45() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToAllWeighted)
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Full)
                .withTest(VarType.NUMERIC, CTreeTest.Numeric_Binary)
                .withFunction(CTreeFunction.GainRatio);
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToAllWeighted)
                .withFunction(CTreeFunction.GainRatio)
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Binary)
                .withTest(VarType.NUMERIC, CTreeTest.Numeric_Binary);
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToAllWeighted)
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Binary)
                .withTest(VarType.NUMERIC, CTreeTest.Numeric_Binary)
                .withFunction(CTreeFunction.GiniGain);
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

        tree.withPoolSize(poolSize());
        tree.testMap.clear();
        tree.testMap.putAll(testMap);

        tree.customTestMap.clear();
        tree.customTestMap.putAll(customTestMap);
        return tree;
    }

    public CTree() {
        testMap.put(VarType.BINARY, CTreeTest.Binary_Binary);
        testMap.put(VarType.ORDINAL, CTreeTest.Numeric_Binary);
        testMap.put(VarType.INDEX, CTreeTest.Numeric_Binary);
        testMap.put(VarType.NUMERIC, CTreeTest.Numeric_Binary);
        testMap.put(VarType.NOMINAL, CTreeTest.Nominal_Binary);
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

    public CTree withTest(VarType varType, Tag<CTreeTest> test) {
        this.testMap.put(varType, test);
        return this;
    }

    public CTree withTest(String varName, Tag<CTreeTest> test) {
        this.customTestMap.put(varName, test);
        return this;
    }

    public Map<VarType, Tag<CTreeTest>> testMap() {
        return testMap;
    }

    public Map<String, Tag<CTreeTest>> customTestMap() {
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

    public Tag<CTreeFunction> getFunction() {
        return function;
    }

    public CTree withFunction(Tag<CTreeFunction> function) {
        this.function = function;
        return this;
    }

    public Tag<CTreeMissingHandler> getSplitter() {
        return splitter;
    }

    public CTree withMissingHandler(Tag<CTreeMissingHandler> splitter) {
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
                .withAllowMissingTargetValues(false)
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        additionalValidation(df);

        this.varSelector.withVarNames(inputNames());

        int rows = df.rowCount();
        root = new Node(null, "root", spot -> true);
        if (poolSize() == 0) {
            root.learn(this, df, weights, maxDepth() < 0 ? Integer.MAX_VALUE : maxDepth(), new NominalTerms().init(df));
        } else {
            FJPool.run(poolSize(), () -> root.learn(this, df, weights, maxDepth < 0 ? Integer.MAX_VALUE : maxDepth, new NominalTerms().init(df)));
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
    protected CFit coreFit(Frame dfOld, boolean withClasses, boolean withDensities) {

        Frame df = prepareFit(dfOld);
        CFit prediction = CFit.newEmpty(this, df, withClasses, withDensities);
        prediction.addTarget(firstTargetName(), firstTargetLevels());

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
        if (node.getCounter().sum(false) == 0)
            if (node.getParent() == null) {
                throw new RuntimeException("Something bad happened at learning time");
            } else {
                return new Pair<>(node.getParent().getBestIndex(), node.getParent().getDensity());
            }
        if (node.isLeaf())
            return new Pair<>(node.getBestIndex(), node.getDensity());

        for (Node child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                return this.fitPoint(tree, spot, child);
            }
        }

        String[] dict = tree.firstTargetLevels();
        DVector dv = DVector.newEmpty(dict);
        for (Node child : node.getChildren()) {
            DVector d = this.fitPoint(tree, spot, child)._2;
            for (int i = 0; i < dict.length; i++) {
//                dv.increment(i, d.get(i) * child.getDensity().sum(false));
                dv.increment(i, d.get(i));
            }
        }
        dv.normalize(false);
        return new Pair<>(dv.findBestIndex(false), dv);
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
        sb.append(WS.formatFlexShort(node.getCounter().sum(true))).append("/");
        sb.append(WS.formatFlexShort(node.getCounter().sumExcept(node.getBestIndex(), true))).append(" ");
        sb.append(firstTargetLevels()[node.getBestIndex()]).append(" (");
        DVector d = node.getDensity().solidCopy().normalize(false);
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
            return -(new Double(score).compareTo(o.score));
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
     */
    public static class Node implements Serializable {

        private static final long serialVersionUID = -5045581827808911763L;

        private int id;
        private final Node parent;
        private final String groupName;
        private final SPredicate<FSpot> predicate;

        private boolean leaf = true;
        private final List<Node> children = new ArrayList<>();
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

        public void learn(CTree tree, Frame df, Var weights, int depth, NominalTerms terms) {
            density = DVector.newFromWeights(df.var(tree.firstTargetName()), weights);
            counter = DVector.newFromCount(df.var(tree.firstTargetName()));
            bestIndex = density.findBestIndex(false);

            if (df.rowCount() == 0) {
                bestIndex = parent.bestIndex;
                return;
            }
            if (counter.countValues(x -> x > 0, false) == 1 || depth < 1 || df.rowCount() <= tree.minCount()) {
                return;
            }

            List<Candidate> candidateList = Arrays.stream(tree.varSelector().nextVarNames())
                    .parallel()
                    .filter(testCol -> !testCol.equals(tree.firstTargetName()))
                    .map(testCol -> {
                        CTreeTest test = null;
                        if (tree.customTestMap().containsKey(testCol)) {
                            test = tree.customTestMap().get(testCol).get();
                        }
                        if (tree.testMap().containsKey(df.var(testCol).type())) {
                            test = tree.testMap().get(df.var(testCol).type()).get();
                        }
                        if (test == null) {
                            throw new IllegalArgumentException("can't train ctree with no " +
                                    "tests for given variable: " + df.var(testCol).name() +
                                    " [" + df.var(testCol).type().name() + "]");
                        }
                        List<Candidate> c = test.computeCandidates(
                                tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction().get(), terms);
                        return (c == null || c.isEmpty()) ? null : c.get(0);
                    }).filter(c -> c != null).collect(Collectors.toList());

            Collections.sort(candidateList);
            if (candidateList.isEmpty() || candidateList.get(0).getGroupNames().isEmpty()) {
                bestIndex = parent.bestIndex;
                return;
            }

            leaf = false;
            bestCandidate = candidateList.get(0);
            String testName = bestCandidate.getTestName();

            // now that we have a best candidate, do the effective split
            Pair<List<Frame>, List<Var>> frames = tree.getSplitter().get().performSplit(df, weights, bestCandidate);

            for (int i = 0; i < bestCandidate.getGroupNames().size(); i++) {
                Node child = new Node(this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
                children.add(child);
            }
            Util.rangeStream(children.size(), tree.poolSize() > 0)
                    .forEach(i -> children.get(i).learn(tree, frames._1.get(i), frames._2.get(i), depth - 1, terms.copy()));
        }
    }

    public static class NominalTerms {

        private final Map<String, Set<Integer>> indexes = new HashMap<>();

        public NominalTerms init(Frame df) {
            Arrays.stream(df.varNames())
                    .map(df::var)
                    .filter(var -> var.type().isNominal() || var.type().isBinary())
                    .forEach(var -> indexes.put(var.name(), IntStream.range(1, var.levels().length).boxed().collect(toSet())));
            return this;
        }

        public Set<Integer> indexes(String key) {
            return indexes.get(key);
        }

        public NominalTerms copy() {
            NominalTerms terms = new NominalTerms();
            this.indexes.entrySet().forEach(e -> terms.indexes.put(e.getKey(), new ConcurrentSkipListSet<>(e.getValue())));
            return terms;
        }

    }
}
