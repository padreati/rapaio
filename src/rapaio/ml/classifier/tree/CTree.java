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

import java.util.HashMap;
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
    private int maxDepth = 0;

    private VarSelector varSelector = VarSelector.ALL;
    private Map<String, Tag<CTreeTest>> customTestMap = new HashMap<>();
    private Map<VarType, Tag<CTreeTest>> testMap = new HashMap<>();
    private Tag<CTreeFunction> function = CTreeFunction.InfoGain;
    private Tag<CTreeMissingHandler> splitter = CTreeMissingHandler.Ignored;

    // tree root node
    private CTreeNode root;

    // static builders

    public static CTree newID3() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.Ignored)
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Full)
                .withTest(VarType.NUMERIC, CTreeTest.Ignore)
                .withFunction(CTreeFunction.Entropy);
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
                .withFunction(CTreeFunction.InfoGain)
                .withTest(VarType.NOMINAL, CTreeTest.Nominal_Binary)
                .withTest(VarType.NUMERIC, CTreeTest.Numeric_Binary);
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(-1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withMissingHandler(CTreeMissingHandler.ToRandom)
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
                .withSampler(sampler());

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
    public CTree learn(Frame dfOld, Var weights, String... targetVars) {

        Frame df = prepareLearning(dfOld, weights, targetVars);
        additionalValidation(df);

        this.varSelector.withVarNames(inputNames());

        int rows = df.rowCount();
        root = new CTreeNode(null, "root", spot -> true);
        if (poolSize() == 0) {
            root.learn(this, df, weights, maxDepth() < 0 ? Integer.MAX_VALUE : maxDepth(), new CTreeNominalTerms().init(df));
        } else {
            FJPool.run(poolSize(), () -> root.learn(this, df, weights, maxDepth < 0 ? Integer.MAX_VALUE : maxDepth, new CTreeNominalTerms().init(df)));
        }
        this.root.fillId(1);
        return this;
    }

    @Override
    public CFit fit(Frame dfOld, boolean withClasses, boolean withDensities) {

        Frame df = prepareFit(dfOld);
        CFit prediction = CFit.newEmpty(this, df, withClasses, withDensities);
        prediction.addTarget(firstTargetName(), firstTargetLevels());

        df.stream().forEach(spot -> {
            Pair<Integer, DVector> result = fitPoint(this, spot, root);
            if (withClasses)
                prediction.firstClasses().setIndex(spot.row(), result.first);
            if (withDensities)
                for (int j = 0; j < firstTargetLevels().length; j++) {
                    prediction.firstDensity().setValue(spot.row(), j, result.second.get(j));
                }
        });
        return prediction;
    }

    public Pair<Integer, DVector> fitPoint(CTree tree, FSpot spot, CTreeNode node) {
        if (node.getCounter().sum(false) == 0)
            if (node.getParent() == null) {
                throw new RuntimeException("Something bad happened at learning time");
            } else {
                return new Pair<>(node.getParent().getBestIndex(), node.getParent().getDensity());
            }
        if (node.isLeaf())
            return new Pair<>(node.getBestIndex(), node.getDensity());

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                return this.fitPoint(tree, spot, child);
            }
        }

        String[] dict = tree.firstTargetLevels();
        DVector dv = DVector.newEmpty(dict);
        for (CTreeNode child : node.getChildren()) {
            DVector d = this.fitPoint(tree, spot, child).second;
            for (int i = 0; i < dict.length; i++) {
                dv.increment(i, d.get(i) * child.getDensity().sum(false));
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
            throw new IllegalArgumentException("can't learn ctree with no " +
                    "tests for given variable: " + var.name() +
                    " [" + var.type().name() + "]");
        });
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
        sb.append("description:\n");
        sb.append("split, n/err, classes (densities) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);

        return sb.toString();

    }

    private void buildSummary(StringBuilder sb, CTreeNode node, int level) {

        sb.append(level == 0 ? "|- " : "|");
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "   |- " : "   |");
        }
        sb.append(node.getId()).append(". ").append(node.getGroupName()).append("    ");
        sb.append(WS.formatFlexShort(node.getCounter().sum(true))).append("/");
        sb.append(WS.formatFlexShort(node.getCounter().sumExcept(node.getBestIndex(), true))).append(" ");
        sb.append(firstTargetLevels()[node.getBestIndex()]).append(" (");
        DVector d = node.getDensity();
        for (int i = 1; i < firstTargetLevels().length; i++) {
            sb.append(WS.formatFlexShort(d.get(i))).append(" ");
        }
        sb.append(") ");
        if (node.isLeaf()) sb.append("*");
        sb.append("\n");

        node.getChildren().stream().forEach(child -> buildSummary(sb, child, level + 1));
    }
}
