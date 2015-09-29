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

import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.common.VarSelector;
import rapaio.util.Pair;
import rapaio.util.Tag;

/**
 * Tree classifier.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CTree extends AbstractClassifier {

    private static final long serialVersionUID = 1203926824359387358L;

    // parameter default values
    int minCount = 1;
    int maxDepth = 10_000;

    VarSelector varSelector = VarSelector.ALL;
    CTreeTestCounter testCounter = new CTreeTestCounter(10_000, 10_000);
    CTreeNominalMethod nominalMethod = new CTreeNominalMethod.Full();
    CTreeNumericMethod numericMethod = new CTreeNumericMethod.Binary();
    CTreeTestFunction function = new CTreeTestFunction.InfoGain();
    Tag<CTreeSplitter> splitter = CTreeSplitter.MissingIgnored;
    CTreePredictor predictor = new CTreePredictor.Standard();

    // tree root node
    private CTreeNode root;
    private int rows;

    // static builders

    public static CTree newID3() {
        return new CTree()
                .withTestCounter(new CTreeTestCounter(1, 1))
                .withMaxDepth(10_000)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(CTreeSplitter.MissingIgnored)
                .withNominalMethod(new CTreeNominalMethod.Full())
                .withNumericMethod(new CTreeNumericMethod.Ignore())
                .withFunction(new CTreeTestFunction.Entropy())
                .withPredictor(new CTreePredictor.Standard());
    }

    public static CTree newC45() {
        return new CTree()
                .withTestCounter(new CTreeTestCounter(1, 1))
                .withMaxDepth(10_000)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(CTreeSplitter.MissingToAllWeighted)
                .withNominalMethod(new CTreeNominalMethod.Full())
                .withNumericMethod(new CTreeNumericMethod.Binary())
                .withFunction(new CTreeTestFunction.GainRatio())
                .withPredictor(new CTreePredictor.Standard());
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withTestCounter(new CTreeTestCounter(1, 1))
                .withSplitter(CTreeSplitter.MissingToAllWeighted)
                .withFunction(new CTreeTestFunction.InfoGain())
                .withNominalMethod(new CTreeNominalMethod.Binary())
                .withNumericMethod(new CTreeNumericMethod.Binary())
                .withPredictor(new CTreePredictor.Standard());
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(10_000)
                .withMinCount(1)
                .withVarSelector(VarSelector.ALL)
                .withTestCounter(new CTreeTestCounter(10_000, 10_000))
                .withSplitter(CTreeSplitter.MissingToRandom)
                .withNominalMethod(new CTreeNominalMethod.Binary())
                .withNumericMethod(new CTreeNumericMethod.Binary())
                .withFunction(new CTreeTestFunction.GiniGain())
                .withPredictor(new CTreePredictor.Standard());
    }

    @Override
    public CTree newInstance() {
        return (CTree) new CTree()
                .withMinCount(minCount)
                .withMaxDepth(maxDepth)
                .withNominalMethod(nominalMethod.newInstance())
                .withNumericMethod(numericMethod.newInstance())
                .withFunction(function.newInstance())
                .withSplitter(splitter)
                .withPredictor(predictor.newInstance())
                .withVarSelector(varSelector().newInstance())
                .withSampler(sampler());
    }

    public CTreeNode getRoot() {
        return root;
    }

    public VarSelector varSelector() {
        return varSelector;
    }

    public CTree withMCols() {
        this.varSelector = VarSelector.AUTO;
        return this;
    }

    public CTree withMCols(int mcols) {
        this.varSelector = new VarSelector(mcols);
        return this;
    }

    public CTree withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    public int getMinCount() {
        return minCount;
    }

    public CTree withMinCount(int minCount) {
        if (minCount < 1) {
            throw new IllegalArgumentException("min cont must be an integer positive number");
        }
        this.minCount = minCount;
        return this;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public CTree withMaxDepth(int maxDepth) {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("max depth must be an integer greater than 0");
        }
        this.maxDepth = maxDepth;
        return this;
    }

    public CTreeTestCounter getCTreeTestCounter() {
        return testCounter;
    }

    public CTree withTestCounter(CTreeTestCounter CTreeTestCounter) {
        this.testCounter = CTreeTestCounter;
        return this;
    }

    public CTreeNominalMethod getNominalMethod() {
        return nominalMethod;
    }

    public CTree withNominalMethod(CTreeNominalMethod methodNominal) {
        this.nominalMethod = methodNominal;
        return this;
    }

    public CTreeNumericMethod getNumericMethod() {
        return numericMethod;
    }

    public CTree withNumericMethod(CTreeNumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public CTreeTestFunction getFunction() {
        return function;
    }

    public CTree withFunction(CTreeTestFunction function) {
        this.function = function;
        return this;
    }

    public Tag<CTreeSplitter> getSplitter() {
        return splitter;
    }

    public CTree withSplitter(Tag<CTreeSplitter> splitter) {
        this.splitter = splitter;
        return this;
    }

    public CTreePredictor getPredictor() {
        return predictor;
    }

    public CTree withPredictor(CTreePredictor predictor) {
        this.predictor = predictor;
        return this;
    }

    @Override
    public String name() {
        return "TreeClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("TreeClassifier{");
        sb.append("varSelector=").append(varSelector().name()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth).append(",");
        sb.append("testCounter=").append(testCounter.name()).append(",");
        sb.append("numericMethod=").append(numericMethod.name()).append(",");
        sb.append("nominalMethod=").append(nominalMethod.name()).append(",");
        sb.append("function=").append(function.name()).append(",");
        sb.append("splitter=").append(splitter.name()).append(",");
        sb.append("predictor=").append(predictor.name());
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
    public CTree learn(Frame df, Var weights, String... targetVars) {

        prepareLearning(df, weights, targetVars);

        this.varSelector.withVarNames(inputNames());

        rows = df.rowCount();

        testCounter.initialize(df, inputNames());

        root = new CTreeNode(null, "root", spot -> true);
        root.learn(this, df, weights, maxDepth, new CTreeNominalTerms().init(df));
        return this;
    }

    @Override
    public CFit fit(Frame df, boolean withClasses, boolean withDensities) {

        CFit prediction = CFit.newEmpty(this, df, withClasses, withDensities);
        prediction.addTarget(firstTargetName(), firstDict());

        df.stream().forEach(spot -> {
            Pair<Integer, DVector> result = predictor.predict(this, spot, root);
            if (withClasses)
                prediction.firstClasses().setIndex(spot.row(), result.first);
            if (withDensities)
                for (int j = 0; j < firstDict().length; j++) {
                    prediction.firstDensity().setValue(spot.row(), j, result.second.get(j));
                }
        });
        return prediction;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CTree model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName()).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().summary()).append("\n");

        sb.append("Learned model:\n");

        if (!isLearned()) {
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
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        if (node.getParent() == null) {
            sb.append("root").append(" ");
            sb.append(node.getCounter().sum(true)).append("/");
            sb.append(node.getCounter().sumExcept(node.getBestIndex(), true)).append(" ");
            sb.append(firstDict()[node.getBestIndex()]).append(" (");
            DVector d = node.getDensity().solidCopy();
//            d.normalize(false);
            for (int i = 1; i < firstDict().length; i++) {
                sb.append(String.format("%.4f", d.get(i))).append(" ");
            }
            sb.append(") ");
            if (node.isLeaf()) sb.append("*");
            sb.append("\n");

        } else {

            sb.append(node.getGroupName()).append("  ");

            sb.append(node.getCounter().sum(true)).append("/");
            sb.append(node.getCounter().sumExcept(node.getBestIndex(), true)).append(" ");
            sb.append(firstDict()[node.getBestIndex()]).append(" (");
            DVector d = node.getDensity().solidCopy();
//            d.normalize(false);
            for (int i = 1; i < firstDict().length; i++) {
                sb.append(String.format("%.4f", d.get(i))).append(" ");
            }
            sb.append(") ");
            if (node.isLeaf()) sb.append("*");
            sb.append("\n");
        }

        // children

        if (!node.isLeaf()) {
            node.getChildren().stream().forEach(child -> buildSummary(sb, child, level + 1));
        }
    }
}
