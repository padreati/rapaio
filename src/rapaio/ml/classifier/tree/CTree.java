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
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.sys.WS;
import rapaio.util.FJPool;
import rapaio.util.Pair;
import rapaio.util.Tag;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private double minGain = -1000;

    private VarSelector varSelector = VarSelector.ALL;
    private Map<String, CTreeTest> customTestMap = new HashMap<>();
    private Map<VarType, CTreeTest> testMap = new HashMap<>();
    private CTreePurityFunction function = CTreePurityFunction.InfoGain;
    private CTreeSplitter splitter = CTreeSplitter.Ignored;
    private Tag<CTreePruning> pruning = CTreePruning.NONE;
    private Frame pruningDf = null;

    // tree root node
    private CTreeNode root;

    private transient Map<CTreeNode, Map<String, Mapping>> sortingCache = new HashMap<>();

    // static builders

    public CTree() {
        testMap.put(VarType.BINARY, CTreeTest.BinaryBinary);
        testMap.put(VarType.ORDINAL, CTreeTest.NumericBinary);
        testMap.put(VarType.INDEX, CTreeTest.NumericBinary);
        testMap.put(VarType.NUMERIC, CTreeTest.NumericBinary);
        testMap.put(VarType.NOMINAL, CTreeTest.NominalBinary);
        withRuns(0);
    }

    public static CTree newID3() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(CTreeSplitter.Ignored)
                .withTest(VarType.NOMINAL, CTreeTest.NominalFull)
                .withTest(VarType.NUMERIC, CTreeTest.Ignore)
                .withFunction(CTreePurityFunction.InfoGain)
                .withPruning(CTreePruning.NONE);
    }

    public static CTree newC45() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(CTreeSplitter.ToAllWeighted)
                .withTest(VarType.NOMINAL, CTreeTest.NominalFull)
                .withTest(VarType.NUMERIC, CTreeTest.NumericBinary)
                .withFunction(CTreePurityFunction.GainRatio);
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(CTreeSplitter.ToAllWeighted)
                .withFunction(CTreePurityFunction.GainRatio)
                .withTest(VarType.NOMINAL, CTreeTest.NominalBinary)
                .withTest(VarType.NUMERIC, CTreeTest.NumericBinary);
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(CTreeSplitter.ToAllWeighted)
                .withTest(VarType.NOMINAL, CTreeTest.NominalBinary)
                .withTest(VarType.NUMERIC, CTreeTest.NumericBinary)
                .withTest(VarType.INDEX, CTreeTest.NumericBinary)
                .withFunction(CTreePurityFunction.GiniGain);
    }

    @Override
    public CTree newInstance() {
        CTree tree = (CTree) new CTree()
                .withMinCount(minCount)
                .withMinGain(minGain)
                .withMaxDepth(maxDepth)
                .withFunction(function)
                .withSplitter(splitter)
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

    public CTree withTest(VarType varType, CTreeTest test) {
        this.testMap.put(varType, test);
        return this;
    }

    public CTree withTest(String varName, CTreeTest test) {
        this.customTestMap.put(varName, test);
        return this;
    }

    public Map<VarType, CTreeTest> testMap() {
        return testMap;
    }

    public Map<String, CTreeTest> customTestMap() {
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

        // create the root node
        root = new CTreeNode(null, "root", RowPredicate.all());

        // start learning the root node (this one will fire learning recursively
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
    protected CPrediction coreFit(Frame df, boolean withClasses, boolean withDensities) {
        CPrediction prediction = CPrediction.build(this, df, withClasses, withDensities);
        for (int i = 0; i < df.rowCount(); i++) {
            Pair<Integer, DVector> res = fitPoint(this, root, i, df);
            int index = res._1;
            DVector dv = res._2;
            if (withClasses)
                prediction.firstClasses().setIndex(i, index);
            if (withDensities)
                for (int j = 0; j < firstTargetLevels().size(); j++) {
                    prediction.firstDensity().setValue(i, j, dv.get(j));
                }
        }
        return prediction;
    }

    protected Pair<Integer, DVector> fitPoint(CTree tree, CTreeNode node, int row, Frame df) {
        if (node.isLeaf())
            return Pair.from(node.getBestIndex(), node.getDensity().solidCopy().normalize());

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(row, df)) {
                return this.fitPoint(tree, child, row, df);
            }
        }

        List<String> dict = tree.firstTargetLevels();
        DVector dv = DVector.empty(false, dict);
        double w = 0.0;
        for (CTreeNode child : node.getChildren()) {
            DVector d = this.fitPoint(tree, child, row, df)._2;
            double wc = child.getDensity().sum();
            dv.increment(d, wc);
            w += wc;
        }
        for (int i = 0; i < dict.size(); i++) {
            dv.set(i, dv.get(i) / w);
        }
        return Pair.from(dv.findBestIndex(), dv);
    }

    private void additionalValidation(Frame df) {
        df.varStream().forEach(var -> {
            if (customTestMap.containsKey(var.name()))
                return;
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
    public CTree withInputFilters(List<FFilter> filters) {
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
        sb.append(firstTargetLevels().get(node.getBestIndex())).append(" (");
        DVector d = node.getDensity().solidCopy().normalize();
        for (int i = 1; i < firstTargetLevels().size(); i++) {
            sb.append(WS.formatFlexShort(d.get(i))).append(" ");
        }
        sb.append(") ");
        if (node.isLeaf()) {
            sb.append("*");
        } else {
            sb.append("[").append(WS.formatFlex(node.getBestCandidate().getScore())).append("]");
        }
        sb.append("\n");

        node.getChildren().forEach(child -> buildSummary(sb, child, level + 1));
    }

}

