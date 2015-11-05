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

package rapaio.ml.regressor.boost;

import rapaio.core.SamplingTools;
import rapaio.data.*;
import rapaio.ml.regressor.AbstractRegression;
import rapaio.ml.regressor.RFit;
import rapaio.ml.regressor.Regression;
import rapaio.ml.regressor.boost.gbt.BTRegression;
import rapaio.ml.regressor.boost.gbt.GBTLossFunction;
import rapaio.ml.regressor.simple.L2Regression;
import rapaio.ml.regressor.tree.rtree.RTree;

import java.util.ArrayList;
import java.util.List;

import static rapaio.sys.WS.formatFlex;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class GBTRegression extends AbstractRegression implements Regression {

    // parameters
    int runs = 1; // number of rounds
    GBTLossFunction lossFunction = new GBTLossFunction.Huber();

    Regression initRegression = new L2Regression();
    BTRegression regressor = RTree.buildCART().withMaxDepth(4).withMinCount(10);
    double shrinkage = 1.0;
    boolean useBootstrap = false;
    double bootstrapSize = 1.0;

    // prediction
    Numeric fitLearn;
    Numeric fitValues;
    List<BTRegression> trees;

    @Override
    public Regression newInstance() {
        return new GBTRegression()
                .withLossFunction(lossFunction)
                .withInitRegressor(initRegression)
                .withRegressor(regressor)
                .withShrinkage(shrinkage)
                .withBootstrap(useBootstrap)
                .withBootstrapSize(bootstrapSize)
                .withRuns(runs);
    }

    @Override
    public String name() {
        return "GradientBoostingTreeRegressor";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("loss=").append(lossFunction.name()).append(", ");
        sb.append("initRegression=").append(initRegression.fullName()).append(", ");
        sb.append("regressor=").append(regressor.fullName()).append(", ");
        sb.append("shrinkage=").append(formatFlex(shrinkage)).append(", ");
        sb.append("useBootstrap=").append(useBootstrap).append(", ");
        sb.append("bootstrapSize=").append(formatFlex(bootstrapSize)).append(", ");
        sb.append("runs=").append(runs);
        sb.append("}");
        return sb.toString();
    }

    public GBTRegression withLossFunction(GBTLossFunction lossFunction) {
        this.lossFunction = lossFunction;
        return this;
    }

    public GBTRegression withRegressor(BTRegression regressor) {
        this.regressor = regressor;
        return this;
    }

    public GBTRegression withInitRegressor(Regression initRegression) {
        this.initRegression = initRegression;
        return this;
    }

    public GBTRegression withShrinkage(double shrinkage) {
        this.shrinkage = shrinkage;
        return this;
    }

    public GBTRegression withBootstrap(boolean use) {
        this.useBootstrap = use;
        return this;
    }

    public GBTRegression withBootstrapSize(double bootstrapSize) {
        this.bootstrapSize = bootstrapSize;
        return this;
    }

    public GBTRegression withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        prepareTraining(df, weights, targetVarNames);

        if (targetVarNames.length != 1) {
            throw new IllegalArgumentException("GBT accepts a single target variable");
        }

        Var y = df.var(firstTargetName());
        Frame x = df.removeVars(new VarRange(firstTargetName()));

        initRegression.learn(df, firstTargetName());
        RFit initPred = initRegression.fit(df, false);
        trees = new ArrayList<>();

        fitLearn = Numeric.newFill(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            fitLearn.setValue(i, initPred.firstFit().value(i));
        }

        for (int i = 1; i <= runs; i++) {
            Numeric gradient = lossFunction.gradient(y, fitLearn).withName("target");

            Frame xm = x.bindVars(gradient);
            BTRegression tree = regressor.newInstance();

            // bootstrap samples

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (useBootstrap) {
                bootstrapMapping = Mapping.newEmpty();
                int[] sample = SamplingTools.sampleWOR(xmLearn.rowCount(), (int) (bootstrapSize * xmLearn.rowCount()));
                for (int aSample : sample) {
                    bootstrapMapping.add(aSample);
                }
                xmLearn = MappedFrame.newByRow(xm, bootstrapMapping);
                xLearn = MappedFrame.newByRow(x, bootstrapMapping);
            }

            // build regions

            tree.learn(xmLearn, "target");

            // fit residuals

            if (bootstrapMapping == null) {
                tree.boostFit(xLearn, y, fitLearn, lossFunction);
            } else {
                tree.boostFit(
                        xLearn,
                        MappedVar.newByRows(y, bootstrapMapping),
                        MappedVar.newByRows(fitLearn, bootstrapMapping),
                        lossFunction);
            }

            // add next prediction to the fit values

            RFit treePred = tree.fit(df, false);
            for (int j = 0; j < df.rowCount(); j++) {
                fitLearn.setValue(j, fitLearn.value(j) + shrinkage * treePred.firstFit().value(j));
            }

            // add tree in the predictors list

            trees.add(tree);
        }

        fitValues = Numeric.newEmpty();
        for (int i = 0; i < fitLearn.rowCount(); i++) {
            fitValues.addValue(fitLearn.value(i));
        }
    }

    @Override
    public RFit fit(final Frame df, final boolean withResiduals) {
        RFit pred = RFit.newEmpty(this, df, withResiduals);
        for (String targetName : targetNames()) {
            pred.addTarget(targetName);
        }

        RFit initPred = initRegression.fit(df);
        for (int i = 0; i < df.rowCount(); i++) {
            pred.firstFit().setValue(i, initPred.firstFit().value(i));
        }
        for (BTRegression tree : trees) {
            RFit treePred = tree.fit(df);
            for (int i = 0; i < df.rowCount(); i++) {
                pred.firstFit().setValue(i, pred.firstFit().value(i) + shrinkage * treePred.firstFit().value(i));
            }
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String summary() {
        throw new IllegalArgumentException("not implemented");
    }
}
