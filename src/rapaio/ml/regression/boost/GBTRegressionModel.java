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

package rapaio.ml.regression.boost;

import lombok.Getter;
import rapaio.data.Frame;
import rapaio.data.MappedVar;
import rapaio.data.Mapping;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.regression.tree.GBTRtree;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.loss.Loss;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.simple.L2RegressionModel;
import rapaio.ml.regression.tree.RTree;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GBTRegressionModel extends AbstractRegressionModel<GBTRegressionModel, RegressionResult> {

    private static final long serialVersionUID = 4559540258922653130L;


    public final ValueParam<Double, GBTRegressionModel> shrinkage = new ValueParam<>(this, 1.0,
            "shrinkage",
            "Shrinkage",
            x -> Double.isFinite(x) && x > 0 && x <= 1);

    public final ValueParam<Loss, GBTRegressionModel> loss = new ValueParam<>(this, new L2Loss(),
            "loss",
            "Loss function",
            Objects::nonNull);

    public final ValueParam<? extends RegressionModel, GBTRegressionModel> initModel = new ValueParam<>(this, L2RegressionModel.newModel(),
            "initModel",
            "Initial model",
            Objects::nonNull);

    public final ValueParam<GBTRtree<? extends RegressionModel, ? extends RegressionResult>, GBTRegressionModel> model =
            new ValueParam<>(this, RTree.newCART().maxDepth.set(2).minCount.set(10),
                    "nodeModel",
                    "Node model",
                    Objects::nonNull);

    // prediction
    @Getter
    private VarDouble fitValues;

    @Getter
    private List<GBTRtree<? extends RegressionModel, ? extends RegressionResult>> trees;

    @Override
    public GBTRegressionModel newInstance() {
        return new GBTRegressionModel().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "GradientBoostingTreeRegression";
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .minInputCount(1).maxInputCount(1_000_000)
                .inputTypes(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE, VType.NOMINAL))
                .minTargetCount(1).maxTargetCount(1)
                .targetType(VType.DOUBLE)
                .allowMissingInputValues(true)
                .allowMissingTargetValues(false)
                .build();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        trees = new ArrayList<>();

        Var y = df.rvar(firstTargetName());
        Frame x = df.removeVars(VRange.of(firstTargetName()));

        initModel.get().fit(df, weights, firstTargetName());
        fitValues = initModel.get().predict(df, false).firstPrediction().copy();

        for (int i = 1; i <= runs.get(); i++) {
            Var gradient = loss.get().computeGradient(y, fitValues).withName("target");

            Frame xm = x.bindVars(gradient);
            GBTRtree tree = (GBTRtree) model.get().newInstance();

            // frame sampling

            Mapping samplerMapping = rowSampler.get().nextSample(xm, weights).mapping;
            Frame xmLearn = xm.mapRows(samplerMapping);

            // build regions

            tree.fit(xmLearn, "target");

            // predict residuals

            tree.boostUpdate(
                    xmLearn,
                    MappedVar.byRows(y, samplerMapping),
                    MappedVar.byRows(fitValues, samplerMapping),
                    loss.get());

            // add next prediction to the predict values
            RegressionResult treePred = tree.predict(df, false);
            VarDouble nextFit = VarDouble.fill(df.rowCount(), 0.0).withName(fitValues.name());
            for (int j = 0; j < df.rowCount(); j++) {
                nextFit.setDouble(j, fitValues.getDouble(j) + shrinkage.get() * treePred.firstPrediction().getDouble(j));
            }

            double initScore = loss.get().computeErrorScore(y, fitValues);
            double nextScore = loss.get().computeErrorScore(y, nextFit);

            if (initScore >= nextScore) {
                fitValues = nextFit;
                // add tree in the predictors list
                trees.add(tree);
            }
            runningHook.get().accept(this, i);
        }
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals) {
        RegressionResult pred = RegressionResult.build(this, df, withResiduals);
        RegressionResult initPred = initModel.get().predict(df, false);
        for (int i = 0; i < df.rowCount(); i++) {
            pred.firstPrediction().setDouble(i, initPred.firstPrediction().getDouble(i));
        }
        for (GBTRtree tree : trees) {
            RegressionResult treePred = tree.predict(df, false);
            for (int i = 0; i < df.rowCount(); i++) {
                pred.firstPrediction().setDouble(i, pred.firstPrediction().getDouble(i) + shrinkage.get() * treePred.firstPrediction().getDouble(i));
            }
        }
        pred.buildComplete();
        return pred;
    }

    public String toString() {
        return fullName();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        throw new IllegalArgumentException("not implemented");
    }

    @Override
    public String toContent(POption<?>... options) {
        return null;
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return null;
    }
}
