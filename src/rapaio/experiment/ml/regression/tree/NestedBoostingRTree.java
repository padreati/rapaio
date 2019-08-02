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

package rapaio.experiment.ml.regression.tree;

import rapaio.data.*;
import rapaio.experiment.ml.regression.boost.gbt.*;
import rapaio.experiment.ml.regression.loss.*;
import rapaio.experiment.ml.regression.tree.nbrtree.*;
import rapaio.ml.common.*;
import rapaio.ml.regression.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/19.
 */
public class NestedBoostingRTree extends AbstractRegressionModel<NestedBoostingRTree, RegressionResult<NestedBoostingRTree>>
        implements GBTRtree<NestedBoostingRTree, RegressionResult<NestedBoostingRTree>> {

    private static final long serialVersionUID = 1864784340491461993L;
    private int minCount = 5;
    private int maxDepth = 3;
    private VarSelector varSelector = VarSelector.all();
    private NBRFunction nbrFunction = NBRFunction.LINEAR;
    private RegressionLoss loss = new L2RegressionLoss();
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
        return new Capabilities()
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(false)
                .withInputCount(1, Integer.MAX_VALUE)
                .withInputTypes(VType.DOUBLE, VType.INT, VType.BINARY, VType.LONG)
                .withTargetCount(1, 1)
                .withTargetTypes(VType.DOUBLE);
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

    public RegressionLoss getLoss() {
        return loss;
    }

    public NestedBoostingRTree withLoss(RegressionLoss loss) {
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
        return newInstanceDecoration(new NestedBoostingRTree())
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
    protected RegressionResult corePredict(Frame df, boolean withResiduals) {
        RegressionResult prediction = RegressionResult.build(this, df, withResiduals);
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
    public String summary() {
        return null;
    }

    @Override
    public String content() {
        StringBuilder sb = new StringBuilder();
        nodeContent(sb, root, 0);
        return sb.toString();
    }

    private void nodeContent(StringBuilder sb, NBRTreeNode node, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("\t");
        }
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
    public String fullContent() {
        return null;
    }

    @Override
    public void boostUpdate(Frame x, Var y, Var fx, GBTRegressionLoss lossFunction) {

    }
}
