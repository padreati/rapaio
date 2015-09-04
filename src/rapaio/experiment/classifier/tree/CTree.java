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

package rapaio.experiment.classifier.tree;

import rapaio.sys.WS;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.common.VarSelector;
import rapaio.util.Pair;

/**
 * Tree classifier.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class CTree extends AbstractClassifier {

    private static final long serialVersionUID = 1203926824359387358L;

    // parameter default values
    int minCount = 1;
    int maxDepth = Integer.MAX_VALUE;

    VarSelector varSelector = VarSelector.ALL;
    CTreeTestCounter testCounter = new CTreeTestCounter.MNominalMNumeric();
    CTreeNominalMethod nominalMethod = new CTreeNominalMethod.Full();
    CTreeNumericMethod numericMethod = new CTreeNumericMethod.Binary();
    CTreeTestFunction function = new CTreeTestFunction.InfoGain();
    CTreeSplitter splitter = new CTreeSplitter.RemainsIgnored();
    CTreePredictor predictor = new CTreePredictor.Standard();

    // tree root node
    private CTreeNode root;
    private int rows;

    // static builders

    public static CTree newID3() {
        return new CTree()
                .withTestCounter(new CTreeTestCounter.OneNominalOneNumeric())
                .withMaxDepth(Integer.MAX_VALUE)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(new CTreeSplitter.RemainsIgnored())
                .withNominalMethod(new CTreeNominalMethod.Full())
                .withNumericMethod(new CTreeNumericMethod.Ignore())
                .withFunction(new CTreeTestFunction.Entropy())
                .withPredictor(new CTreePredictor.Standard());
    }

    public static CTree newC45() {
        return new CTree()
                .withTestCounter(new CTreeTestCounter.OneNominalOneNumeric())
                .withMaxDepth(Integer.MAX_VALUE)
                .withVarSelector(VarSelector.ALL)
                .withSplitter(new CTreeSplitter.RemainsToAllWeighted())
                .withNominalMethod(new CTreeNominalMethod.Full())
                .withNumericMethod(new CTreeNumericMethod.Binary())
                .withFunction(new CTreeTestFunction.GainRatio())
                .withPredictor(new CTreePredictor.Standard());
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .withMaxDepth(1)
                .withVarSelector(VarSelector.ALL)
                .withTestCounter(new CTreeTestCounter.OneNominalOneNumeric())
                .withSplitter(new CTreeSplitter.RemainsToAllWeighted())
                .withNominalMethod(new CTreeNominalMethod.Binary())
                .withNumericMethod(new CTreeNumericMethod.Binary())
                .withPredictor(new CTreePredictor.Standard());
    }

    public static CTree newCART() {
        return new CTree()
                .withMaxDepth(Integer.MAX_VALUE)
                .withVarSelector(VarSelector.ALL)
                .withTestCounter(new CTreeTestCounter.MNominalMNumeric())
                .withSplitter(new CTreeSplitter.RemainsToAllWeighted())
                .withNominalMethod(new CTreeNominalMethod.Binary())
                .withNumericMethod(new CTreeNumericMethod.Binary())
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
                .withSplitter(splitter.newInstance())
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

    public CTreeSplitter getSplitter() {
        return splitter;
    }

    public CTree withSplitter(CTreeSplitter splitter) {
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
    public CTree learn(Frame df, Var weights, String... targetVars) {

        prepareLearning(df, weights, targetVars);

        this.varSelector.withVarNames(inputNames());

        if (targetNames().length == 0) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }

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

        df.spotStream().forEach(spot -> {
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
    public void printSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append(String.format("n=%d\n", rows));

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, n/err, classes (densities) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);
        WS.code(sb.toString());
    }

    private void buildSummary(StringBuilder sb, CTreeNode node, int level) {
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        if (node.getParent() == null) {
            sb.append("root").append(" ");
            sb.append(node.getDensity().sum(true)).append("/");
            sb.append(node.getDensity().sumExcept(node.getBestIndex(), true)).append(" ");
            sb.append(firstDict()[node.getBestIndex()]).append(" (");
            DVector d = node.getDensity().solidCopy();
            d.normalize(false);
            for (int i = 1; i < firstDict().length; i++) {
                sb.append(String.format("%.6f", d.get(i))).append(" ");
            }
            sb.append(") ");
            if (node.isLeaf()) sb.append("*");
            sb.append("\n");

        } else {

            sb.append(node.getGroupName()).append("  ");

            sb.append(node.getDensity().sum(true)).append("/");
            sb.append(node.getDensity().sumExcept(node.getBestIndex(), true)).append(" ");
            sb.append(firstDict()[node.getBestIndex()]).append(" (");
            DVector d = node.getDensity().solidCopy();
            d.normalize(false);
            for (int i = 1; i < firstDict().length; i++) {
                sb.append(String.format("%.6f", d.get(i))).append(" ");
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
