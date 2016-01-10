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
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.sys.WS;
import rapaio.util.FJPool;
import rapaio.util.Pair;
import rapaio.util.Tag;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.util.stream.Collectors.joining;

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
    private Map<String, CTreePurityTest> customTestMap = new HashMap<>();
    private Map<VarType, CTreePurityTest> testMap = new HashMap<>();
    private CTreePurityFunction function = CTreePurityFunction.InfoGain;
    private CTreeMissingHandler splitter = CTreeMissingHandler.Ignored;
    private Tag<CTreePruning> pruning = CTreePruning.NONE;
    private Frame pruningDf = null;

    // tree root node
    private CTreeNode root;

    private transient Map<CTreeNode, Map<String, Mapping>> sortingCache = new HashMap<>();

    // static builders

    public CTree() {
        testMap.put(VarType.BINARY, CTreePurityTest.BinaryBinary);
        testMap.put(VarType.ORDINAL, CTreePurityTest.NumericBinary);
        testMap.put(VarType.INDEX, CTreePurityTest.NumericBinary);
        testMap.put(VarType.NUMERIC, CTreePurityTest.NumericBinary);
        testMap.put(VarType.NOMINAL, CTreePurityTest.NominalBinary);
        withRuns(0);
    }

    public static CTree newID3() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.Ignored)
                .withTest(VarType.NOMINAL, CTreePurityTest.NominalFull)
                .withTest(VarType.NUMERIC, CTreePurityTest.Ignore)
                .withFunction(CTreePurityFunction.InfoGain)
                .withPruning(CTreePruning.NONE);
    }

    public static CTree newC45() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToAllWeighted)
                .withTest(VarType.NOMINAL, CTreePurityTest.NominalFull)
                .withTest(VarType.NUMERIC, CTreePurityTest.NumericBinary)
                .withFunction(CTreePurityFunction.GainRatio);
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToAllWeighted)
                .withFunction(CTreePurityFunction.GainRatio)
                .withTest(VarType.NOMINAL, CTreePurityTest.NominalBinary)
                .withTest(VarType.NUMERIC, CTreePurityTest.NumericBinary);
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToAllWeighted)
                .withTest(VarType.NOMINAL, CTreePurityTest.NominalBinary)
                .withTest(VarType.NUMERIC, CTreePurityTest.NumericBinary)
                .withTest(VarType.INDEX, CTreePurityTest.NumericBinary)
                .withFunction(CTreePurityFunction.GiniGain);
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

    public CTreeNode getRoot() {
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

    public CTree withTest(VarType varType, CTreePurityTest test) {
        this.testMap.put(varType, test);
        return this;
    }

    public CTree withTest(String varName, CTreePurityTest test) {
        this.customTestMap.put(varName, test);
        return this;
    }

    public Map<VarType, CTreePurityTest> testMap() {
        return testMap;
    }

    public Map<String, CTreePurityTest> customTestMap() {
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

    public CTreePurityFunction getFunction() {
        return function;
    }

    public CTree withFunction(CTreePurityFunction function) {
        this.function = function;
        return this;
    }

    public CTreeMissingHandler getMissingHandler() {
        return splitter;
    }

    public CTree withMissingHandler(CTreeMissingHandler splitter) {
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
        root = new CTreeNode(null, "root", spot -> true);
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

    protected Pair<Integer, DVector> fitPoint(CTree tree, FSpot spot, CTreeNode node) {
        if (node.getCounter().sum() == 0)
            if (node.getParent() == null) {
                throw new RuntimeException("Something bad happened at learning time");
            } else {
                return Pair.from(node.getParent().getBestIndex(), node.getParent().getDensity());
            }
        if (node.isLeaf())
            return Pair.from(node.getBestIndex(), node.getDensity());

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                return this.fitPoint(tree, spot, child);
            }
        }

        String[] dict = tree.firstTargetLevels();
        DVector dv = DVector.newEmpty(false, dict);
        for (CTreeNode child : node.getChildren()) {
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

}

