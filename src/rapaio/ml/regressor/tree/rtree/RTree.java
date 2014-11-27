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

package rapaio.ml.regressor.tree.rtree;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.RPrediction;
import rapaio.ml.regressor.Regressor;
import rapaio.printer.Printer;
import rapaio.util.Pair;

import java.util.List;

/**
 * Implements a regression tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class RTree extends AbstractRegressor {

    int minCount = 1;
    int maxDepth = Integer.MAX_VALUE;

    RTreeTestCounter testCounter = RTreeTestCounter.M_NOMINAL_M_NUMERIC;
    RTreeNominalMethod nominalMethod = RTreeNominalMethod.IGNORE;
    RTreeNumericMethod numericMethod = RTreeNumericMethod.BINARY;
    RTreeTestFunction function = RTreeTestFunction.VARIANCE_SUM;
    RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
    RTreePredictor predictor = RTreePredictor.STANDARD;

    // tree root node
    private RTreeNode root;
    private int rows;


    @Override
    public Regressor newInstance() {
        return null;
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
        sb.append("testCounter=").append(testCounter.name()).append(",");
        sb.append("numericMethod=").append(numericMethod.name()).append(",");
        sb.append("nominalMethod=").append(nominalMethod.name()).append(",");
        sb.append("function=").append(function.name()).append(",");
        sb.append("splitter=").append(splitter.name()).append(",");
        sb.append("predictor=").append(predictor.name());
        sb.append(")");
        return sb.toString();
    }

    public int getMinCount() {
        return minCount;
    }

    public RTree withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public RTreeNumericMethod getNumericMethod() {
        return numericMethod;
    }

    public RTree withNumericMethod(RTreeNumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public RTreeNominalMethod getNominalMethod() {
        return nominalMethod;
    }

    public RTree withNominalMethod(RTreeNominalMethod nominalMethod) {
        this.nominalMethod = nominalMethod;
        return this;
    }

    public RTreeTestFunction getFunction() {
        return function;
    }

    public RTree withFunction(RTreeTestFunction function) {
        this.function = function;
        return this;
    }

    public RTreeSplitter getSplitter() {
        return splitter;
    }

    public RTree withSplitter(RTreeSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        List<String> targetVarList = new VarRange(targetVarNames).parseVarNames(df);
        if (targetVarList.isEmpty()) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetVarList.size() > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }
        this.targetNames = targetVarList.toArray(new String[targetVarList.size()]);

        rows = df.rowCount();

        testCounter.initialize(df, firstTargetVar());

        root = new RTreeNode(null, "root", spot -> true);
        root.learn(this, df, weights, maxDepth);
    }

    @Override
    public RPrediction predict(Frame df, boolean withResiduals) {
        RPrediction pred = RPrediction.newEmpty(df.rowCount(), withResiduals, targetNames);

        df.stream().forEach(spot -> {
            Pair<Double, Double> result = predictor.predict(this, spot, root);
            pred.fit(firstTargetVar()).setValue(spot.row(), result.first);
        });
        pred.buildResiduals(df);
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
