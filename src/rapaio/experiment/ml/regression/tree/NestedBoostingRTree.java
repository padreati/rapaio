/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.regression.tree;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.experiment.ml.regression.tree.nbrtree.NBRFunction;
import rapaio.experiment.ml.regression.tree.nbrtree.NBRTreeNode;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.loss.Loss;
import rapaio.ml.supervised.RegressionHookInfo;
import rapaio.ml.supervised.RegressionResult;
import rapaio.ml.supervised.boost.GBTRtree;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/19.
 */
public class NestedBoostingRTree
        extends GBTRtree<NestedBoostingRTree, RegressionResult, RegressionHookInfo> {

    @Serial
    private static final long serialVersionUID = 1864784340491461993L;
    private int minCount = 5;
    private int maxDepth = 3;
    private VarSelector varSelector = VarSelector.all();
    private NBRFunction nbrFunction = NBRFunction.LINEAR;
    private Loss loss = new L2Loss();
    private int basisCount = 1;
    private double learningRate = 1;
    private double diffusion = 0.05;

    private NBRTreeNode root;

    @Override
    public String name() {
        return null;
    }

    @Override
    public String fullName() {
        return null;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, Integer.MAX_VALUE,
                Arrays.asList(VarType.DOUBLE, VarType.INT, VarType.BINARY, VarType.LONG), false,
                1, 1, List.of(VarType.DOUBLE), false);
    }

    public int getMinCount() {
        return minCount;
    }

    public NestedBoostingRTree withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public NestedBoostingRTree withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public VarSelector getVarSelector() {
        return varSelector;
    }

    public NestedBoostingRTree withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    public NBRFunction getNbrFunction() {
        return nbrFunction;
    }

    public NestedBoostingRTree withNBRFunction(NBRFunction nbrFunction) {
        this.nbrFunction = nbrFunction;
        return this;
    }

    public Loss getLoss() {
        return loss;
    }

    public NestedBoostingRTree withLoss(Loss loss) {
        this.loss = loss;
        return this;
    }

    public int getBasisCount() {
        return basisCount;
    }

    public NestedBoostingRTree withBasisCount(int basisCount) {
        this.basisCount = basisCount;
        return this;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public NestedBoostingRTree withLearningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    public double getDiffusion() {
        return diffusion;
    }

    public NestedBoostingRTree withDiffusion(double diffusion) {
        this.diffusion = diffusion;
        return this;
    }

    @Override
    public NestedBoostingRTree newInstance() {
        return new NestedBoostingRTree()
                .withMaxDepth(getMaxDepth())
                .withMinCount(getMinCount())
                .withDiffusion(getDiffusion())
                .withLearningRate(getLearningRate())
                .withBasisCount(getBasisCount())
                .withNBRFunction(getNbrFunction())
                .withLoss(getLoss());
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        this.root = new NBRTreeNode(1, null);
        root.coreFit(this, df, weights);
        return true;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, double[] quantiles) {
        RegressionResult prediction = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int i = 0; i < df.rowCount(); i++) {
            double y_true = 0.0;
            NBRTreeNode node = root;
            int depth = 1;
            while (true) {
                int ddepth = depth;
                for (int j = 0; j < node.getFunctions().size(); j++) {
                    NBRFunction fun = node.getFunctions().get(j);
                    double factor = node.getFactors().get(j);
                    double funEval = fun.eval(df, i);
                    y_true += factor * learningRate * funEval;
                }
                if (node.isLeaf()) {
                    break;
                }
                if (df.getDouble(i, node.getSplitVarName()) < node.getSplitValue()) {
                    node = node.getLeftNode();
                } else {
                    node = node.getRightNode();
                }
                depth++;
            }
            prediction.firstPrediction().setDouble(i, y_true);
        }
        prediction.buildComplete();
        return prediction;
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return null;
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        nodeContent(sb, root, 0);
        return sb.toString();
    }

    private void nodeContent(StringBuilder sb, NBRTreeNode node, int level) {
        sb.append("\t".repeat(Math.max(0, level)));
        for (NBRFunction fun : node.getFunctions()) {
            sb.append("model: ").append(fun.toString()).append(";");
        }
        sb.append("\n");
        if (!node.isLeaf()) {
            nodeContent(sb, node.getLeftNode(), level + 1);
            nodeContent(sb, node.getRightNode(), level + 1);
        }
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return null;
    }

    @Override
    public void boostUpdate(Frame x, Var y, Var fx, Loss lossFunction) {

    }
}
