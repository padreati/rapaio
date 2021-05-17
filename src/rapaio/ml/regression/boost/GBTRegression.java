/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.experiment.ml.regression.tree.GBTRtree;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.loss.Loss;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.simple.L2Regression;
import rapaio.ml.regression.tree.RTree;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GBTRegression extends AbstractRegressionModel<GBTRegression, RegressionResult> {

    public static GBTRegression newModel() {
        return new GBTRegression();
    }

    @Serial
    private static final long serialVersionUID = 4559540258922653130L;

    public final ValueParam<Double, GBTRegression> shrinkage = new ValueParam<>(this, 1.0,
            "shrinkage",
            "Shrinkage",
            x -> Double.isFinite(x) && x > 0 && x <= 1);

    public final ValueParam<Loss, GBTRegression> loss = new ValueParam<>(this, new L2Loss(),
            "loss",
            "Loss function",
            Objects::nonNull);

    public final ValueParam<? extends RegressionModel, GBTRegression> initModel = new ValueParam<>(this, L2Regression.newModel(),
            "initModel",
            "Initial model",
            Objects::nonNull);

    public final ValueParam<GBTRtree<? extends RegressionModel, ? extends RegressionResult>, GBTRegression> model =
            new ValueParam<>(this, RTree.newCART().maxDepth.set(2).minCount.set(10),
                    "nodeModel",
                    "Node model",
                    Objects::nonNull);

    public final ValueParam<Double, GBTRegression> eps = new ValueParam<>(this, 1e-10,
            "eps",
            "Threshold to stop growing trees if gain is not met.",
            Double::isFinite);

    private VarDouble fitValues;

    private List<GBTRtree<? extends RegressionModel, ? extends RegressionResult>> trees;

    @Override
    public GBTRegression newInstance() {
        return new GBTRegression().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "GBTRegression";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, 1_000_000,
                Arrays.asList(VarType.BINARY, VarType.INT, VarType.DOUBLE, VarType.NOMINAL), true,
                1, 1, List.of(VarType.DOUBLE), false);
    }

    public VarDouble getFitValues() {
        return fitValues;
    }

    public List<GBTRtree<? extends RegressionModel, ? extends RegressionResult>> getTrees() {
        return trees;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        trees = new ArrayList<>();

        Var y = df.rvar(firstTargetName());
        Frame x = df.removeVars(VarRange.of(firstTargetName()));

        initModel.get().fit(df, weights, firstTargetName());
        fitValues = initModel.get().predict(df, false).firstPrediction().copy();

        for (int i = 1; i <= runs.get(); i++) {

            Var gradient = loss.get().gradient(y, fitValues).name("target");

            Frame xm = x.bindVars(gradient);
            var tree = (GBTRtree<? extends RegressionModel, ? extends RegressionResult>) model.get().newInstance();

            // frame sampling

            Mapping sampleRows = rowSampler.get().nextSample(xm, weights).mapping();
            Frame xmLearn = xm.mapRows(sampleRows);

            // build regions

            tree.fit(xmLearn, "target");

            // predict residuals

            tree.boostUpdate(xmLearn, y.mapRows(sampleRows), fitValues.mapRows(sampleRows), loss.get());

            // add next prediction to the predict values
            var pred = tree.predict(df, false).firstPrediction();
            VarDouble nextFit = fitValues.copy().op().plus(pred.op().mult(shrinkage.get()));

            double initScore = loss.get().errorScore(y, fitValues);
            double nextScore = loss.get().errorScore(y, nextFit);

            if (Math.abs(initScore - nextScore) < eps.get()) {
                break;
            }

            if (initScore > nextScore) {
                fitValues = nextFit;
                // add tree in the predictors list
                trees.add(tree);
            }
            runningHook.get().accept(this, i);
        }
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals, double[] quantiles) {
        RegressionResult result = RegressionResult.build(this, df, withResiduals, quantiles);
        var prediction = result.firstPrediction();

        prediction.op().fill(0);
        prediction.op().plus(initModel.get().predict(df, false).firstPrediction());
        for (var tree : trees) {
            prediction.op().plus(tree.predict(df, false).firstPrediction().op().mult(shrinkage.get()));
        }
        result.buildComplete();
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("; fitted=").append(isFitted());
        if (isFitted()) {
            sb.append(", fitted trees:").append(trees.size());
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        sb.append("Target <<< ").append(firstTargetName()).append(" >>>\n\n");
        sb.append("> Number of fitted trees: ").append(trees.size()).append("\n");

        return sb.toString();
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary();
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toSummary();
    }
}
