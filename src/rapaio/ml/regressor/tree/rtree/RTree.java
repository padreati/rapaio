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
 */

package rapaio.ml.regressor.tree.rtree;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.common.VarSelector;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.RResult;
import rapaio.ml.regressor.boost.gbt.BTRegressor;
import rapaio.ml.regressor.boost.gbt.GBTLossFunction;
import rapaio.printer.Printer;
import rapaio.util.Pair;

/**
 * Implements a regression tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class RTree extends AbstractRegressor implements BTRegressor {

    int minCount = 1;
    int maxDepth = Integer.MAX_VALUE;

    RTreeNominalMethod nominalMethod = RTreeNominalMethod.BINARY;
    RTreeNumericMethod numericMethod = RTreeNumericMethod.BINARY;
    RTreeTestFunction function = RTreeTestFunction.VARIANCE_SUM;
    RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
    RTreePredictor predictor = RTreePredictor.STANDARD;
    VarSelector varSelector = new VarSelector.Standard();

    // tree root node
    private RTreeNode root;
    private int rows;

    public static RTree buildDecisionStump() {
        return new RTree()
                .withMaxDepth(2)
                .withNominalMethod(RTreeNominalMethod.BINARY)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_MAJORITY)
                ;
    }

    public static RTree buildC45() {
        return new RTree()
                .withMaxDepth(Integer.MAX_VALUE)
                .withNominalMethod(RTreeNominalMethod.FULL)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withMinCount(2)
                ;
    }

    public static RTree buildCART() {
        return new RTree()
                .withMaxDepth(Integer.MAX_VALUE)
                .withNominalMethod(RTreeNominalMethod.BINARY)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withMinCount(2);
    }

    @Override
    public void boostFit(Frame x, Var y, Var fx, GBTLossFunction lossFunction) {
        root.boostFit(x, y, fx, lossFunction);
    }

    @Override
    public BTRegressor newInstance() {
        return new RTree()
                .withMinCount(minCount)
                .withNumericMethod(numericMethod)
                .withNominalMethod(nominalMethod)
                .withMaxDepth(maxDepth)
                .withSplitter(splitter)
                .withFunction(function)
                .withVarSelector(varSelector);
    }

    private RTree() {
    }

    @Override
    public String name() {
        return "RTree";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("TreeClassifier (");
        sb.append("colSelector=").append(varSelector.toString()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth).append(",");
        sb.append("numericMethod=").append(numericMethod.name()).append(",");
        sb.append("nominalMethod=").append(nominalMethod.name()).append(",");
        sb.append("function=").append(function.name()).append(",");
        sb.append("splitter=").append(splitter.name()).append(",");
        sb.append("predictor=").append(predictor.name());
        sb.append(")");
        return sb.toString();
    }

    public RTree withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    public RTree withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public RTree withMaxDepth(int maxDepth) {
        if (maxDepth == -1) {
            maxDepth = Integer.MAX_VALUE;
        }
        this.maxDepth = maxDepth;
        return this;
    }

    public RTree withNumericMethod(RTreeNumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public RTree withNominalMethod(RTreeNominalMethod nominalMethod) {
        this.nominalMethod = nominalMethod;
        return this;
    }

    public RTree withFunction(RTreeTestFunction function) {
        this.function = function;
        return this;
    }

    public RTree withSplitter(RTreeSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);

        if (targetNames().length == 0) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }

        rows = df.rowCount();

        root = new RTreeNode(null, "root", spot -> true);
        this.varSelector.initialize(inputNames());
        root.learn(this, df, weights, maxDepth);
    }

    @Override
    public RResult predict(Frame df, boolean withResiduals) {
        RResult pred = RResult.newEmpty(this, df, withResiduals).addTarget(firstTargetName());

        df.stream().forEach(spot -> {
            Pair<Double, Double> result = predictor.predict(this, spot, root);
            pred.fit(firstTargetName()).setValue(spot.row(), result.first);
        });
        pred.buildComplete();
        return pred;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append("\n");

        sb.append(String.format("n=%d\n", rows));

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, mean (total weight) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);
    }

    private void buildSummary(StringBuilder sb, RTreeNode node, int level) {
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        sb.append(node.getGroupName()).append("  ");

        sb.append(Printer.formatDecShort.format(node.getValue()));
        sb.append(" (").append(Printer.formatDecShort.format(node.getWeight())).append(") ");
        if (node.isLeaf()) sb.append(" *");
        sb.append("\n");

//        children

        if (!node.isLeaf()) {
            node.getChildren().stream().forEach(child -> buildSummary(sb, child, level + 1));
        }
    }

}
