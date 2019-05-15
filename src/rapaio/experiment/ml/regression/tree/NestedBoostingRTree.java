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
import rapaio.ml.common.*;
import rapaio.ml.regression.*;
import rapaio.experiment.ml.regression.loss.*;
import rapaio.experiment.ml.regression.tree.nbrtree.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/19.
 */
public class NestedBoostingRTree extends AbstractRegression {

    private static final long serialVersionUID = 1864784340491461993L;
    private int minCount = 5;
    private int maxDepth = 3;
    private VarSelector varSelector = VarSelector.all();
    private NBRFunction nbrFunction = NBRFunction.LINEAR;
    private RegressionLoss loss = new L2RegressionLoss();

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

    @Override
    public Regression newInstance() {
        return new NestedBoostingRTree();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        this.root = new NBRTreeNode(1, null);
        root.coreFit(this, df, weights);
        return true;
    }

    @Override
    protected RPrediction corePredict(Frame df, boolean withResiduals) {

        RPrediction prediction = RPrediction.build(this, df, withResiduals);
        for (int i = 0; i < df.rowCount(); i++) {
            root.corePredict(df, i, prediction);
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
        return null;
    }

    @Override
    public String fullContent() {
        return null;
    }
}
