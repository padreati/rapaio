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

package rapaio.experiment.ml.regression.boost;

import rapaio.data.*;
import rapaio.experiment.ml.regression.boost.gbt.*;
import rapaio.experiment.ml.regression.tree.*;
import rapaio.ml.common.*;
import rapaio.ml.loss.*;
import rapaio.ml.regression.*;
import rapaio.ml.regression.simple.*;
import rapaio.ml.regression.tree.*;
import rapaio.printer.*;

import java.util.ArrayList;
import java.util.List;

import static rapaio.printer.format.Format.*;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class GBTRegressionModel extends AbstractRegressionModel<GBTRegressionModel, RegressionResult<GBTRegressionModel>>
        implements DefaultPrintable {

    private static final long serialVersionUID = 4559540258922653130L;

    private RegressionModel initRegressionModel = L2RegressionModel.newL2();
    private GBTRtree regressor = RTree.newCART().withMaxDepth(2).withMinCount(10);
    private GBTRegressionLoss lossFunction = new GBTRegressionLossL2();
    private RegressionLoss regressionLoss = new L2RegressionLoss();
    private double shrinkage = 1.0;

    // prediction
    VarDouble fitValues;
    List<GBTRtree> trees;

    @Override
    public GBTRegressionModel newInstance() {
        return newInstanceDecoration(new GBTRegressionModel())
                .withInitRegressor(initRegressionModel)
                .withRegressor(regressor)
                .withLossFunction(regressionLoss)
                .withShrinkage(shrinkage);
    }

    @Override
    public String name() {
        return "GradientBoostingTreeRegressor";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("loss=").append(regressionLoss.name()).append(", ");
        sb.append("initRegression=").append(initRegressionModel.fullName()).append(", ");
        sb.append("regression=").append(regressor.fullName()).append(", ");
        sb.append("shrinkage=").append(floatFlex(shrinkage)).append(", ");
        sb.append("sampler=").append(sampler()).append(", ");
        sb.append("runs=").append(runs());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1)
                .withInputTypes(VType.BINARY, VType.INT, VType.DOUBLE, VType.NOMINAL)
                .withTargetTypes(VType.DOUBLE)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(false);
    }

    public GBTRegressionModel withLossFunction(RegressionLoss lossFunction) {
        this.regressionLoss = lossFunction;
        return this;
    }

    public GBTRegressionModel withRegressor(GBTRtree regressor) {
        this.regressor = regressor;
        return this;
    }

    public GBTRegressionModel withInitRegressor(RegressionModel initRegressionModel) {
        this.initRegressionModel = initRegressionModel;
        return this;
    }

    public GBTRegressionModel withShrinkage(double shrinkage) {
        this.shrinkage = shrinkage;
        return this;
    }

    public List<GBTRtree> getTrees() {
        return trees;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        trees = new ArrayList<>();

        Var y = df.rvar(firstTargetName());
        Frame x = df.removeVars(VRange.of(firstTargetName()));

        initRegressionModel.fit(df, weights, firstTargetName());
        fitValues = initRegressionModel.predict(df, false).firstPrediction().copy();

        for (int i = 1; i <= runs(); i++) {
            Var gradient = lossFunction.gradient(y, fitValues).withName("target");

            Frame xm = x.bindVars(gradient);
            GBTRtree tree = (GBTRtree) regressor.newInstance();

            // frame sampling

            Mapping samplerMapping = sampler().nextSample(xm, weights).mapping;
            Frame xmLearn = xm.mapRows(samplerMapping);

            // build regions

            tree.fit(xmLearn, "target");

            // predict residuals

            tree.boostUpdate(
                    xmLearn,
                    MappedVar.byRows(y, samplerMapping),
                    MappedVar.byRows(fitValues, samplerMapping),
                    lossFunction);

            // add next prediction to the predict values
            RegressionResult treePred = tree.predict(df, false);
            VarDouble nextFit = VarDouble.fill(df.rowCount(), 0.0).withName(fitValues.name());
            for (int j = 0; j < df.rowCount(); j++) {
                nextFit.setDouble(j, fitValues.getDouble(j) + shrinkage * treePred.firstPrediction().getDouble(j));
            }

            double initScore = regressionLoss.computeErrorScore(y, fitValues);
            double nextScore = regressionLoss.computeErrorScore(y, nextFit);

            if (initScore >= nextScore) {
                fitValues = nextFit;
                // add tree in the predictors list
                trees.add(tree);
            }

            if (runningHook() != null)
                runningHook().accept(this, i);
        }
        return true;
    }

    @Override
    protected RegressionResult<GBTRegressionModel> corePredict(final Frame df, final boolean withResiduals) {
        RegressionResult<GBTRegressionModel> pred = RegressionResult.build(this, df, withResiduals);
        RegressionResult<GBTRegressionModel> initPred = initRegressionModel.predict(df, false);
        for (int i = 0; i < df.rowCount(); i++) {
            pred.firstPrediction().setDouble(i, initPred.firstPrediction().getDouble(i));
        }
        for (GBTRtree tree : trees) {
            RegressionResult treePred = tree.predict(df, false);
            for (int i = 0; i < df.rowCount(); i++) {
                pred.firstPrediction().setDouble(i, pred.firstPrediction().getDouble(i) + shrinkage * treePred.firstPrediction().getDouble(i));
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
