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

package rapaio.ml.supervised.boost;

import static rapaio.sys.With.copy;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.loss.Loss;
import rapaio.ml.supervised.RegressionHookInfo;
import rapaio.ml.supervised.RegressionModel;
import rapaio.ml.supervised.RegressionResult;
import rapaio.ml.supervised.simple.L2Regression;
import rapaio.ml.supervised.tree.RTree;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GBTRegression extends RegressionModel<GBTRegression, RegressionResult, RegressionHookInfo> {

    public static GBTRegression newModel() {
        return new GBTRegression();
    }

    @Serial
    private static final long serialVersionUID = 4559540258922653130L;

    /**
     * Shrinkage regularization coefficient
     */
    public final ValueParam<Double, GBTRegression> shrinkage = new ValueParam<>(this, 1.0,
            "shrinkage", x -> Double.isFinite(x) && x > 0 && x <= 1);

    /**
     * Loss function used
     */
    public final ValueParam<Loss, GBTRegression> loss = new ValueParam<>(this, new L2Loss(), "loss", Objects::nonNull);

    /**
     * First starting model
     */
    public final ValueParam<RegressionModel<?, ?, ?>, GBTRegression> initModel = new ValueParam<>(this, L2Regression.newModel(),
            "initModel", Objects::nonNull);

    /**
     * Tree weak lerner model
     */
    public final ValueParam<GBTRtree<? extends RegressionModel<?, ?, ?>, ? extends RegressionResult, RegressionHookInfo>, GBTRegression>
            model = new ValueParam<>(this, RTree.newCART().maxDepth.set(2).minCount.set(10), "nodeModel", Objects::nonNull);

    /**
     * Convergence threshold used to stop tree growing if the progress on loss function is less than specified
     */
    public final ValueParam<Double, GBTRegression> eps = new ValueParam<>(this, 1e-10, "eps", Double::isFinite);

    private VarDouble fitValues;

    private List<GBTRtree<? extends RegressionModel<?, ?, ?>, ? extends RegressionResult, RegressionHookInfo>> trees;

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

    public List<GBTRtree<? extends RegressionModel<?, ?, ?>, ? extends RegressionResult, RegressionHookInfo>> getTrees() {
        return trees;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean coreFit(Frame df, Var weights) {

        trees = new ArrayList<>();

        Var y = df.rvar(firstTargetName());
        Frame x = df.removeVars(VarRange.of(firstTargetName()));

        initModel.get().fit(df, weights, firstTargetName());
        fitValues = initModel.get().predict(df, false).firstPrediction().copy();

        for (int i = 1; i <= runs.get(); i++) {

            Var gradient = loss.get().gradient(y, fitValues).name("target");

            Frame xm = x.bindVars(gradient);
            var tree = (GBTRtree<? extends RegressionModel<?, ?, ?>, ? extends RegressionResult, RegressionHookInfo>) model.get()
                    .newInstance();

            // frame sampling

            Mapping sampleRows = rowSampler.get().nextSample(xm, weights).mapping();
            Frame xmLearn = xm.mapRows(sampleRows);

            // build regions

            tree.fit(xmLearn, "target");

            // predict residuals

            tree.boostUpdate(xmLearn, y.mapRows(sampleRows), fitValues.mapRows(sampleRows), loss.get());

            // add next prediction to the predict values
            var pred = tree.predict(df, false).firstPrediction();
            VarDouble nextFit = fitValues.dv(copy()).add(pred.dv(copy()).mul(shrinkage.get())).dVar();

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
            runningHook.get().accept(new RegressionHookInfo(this, i));
        }
        return true;
    }

    @Override
    protected RegressionResult corePredict(final Frame df, final boolean withResiduals, double[] quantiles) {
        RegressionResult result = RegressionResult.build(this, df, withResiduals, quantiles);
        DVector prediction = result.firstPrediction().dv();

        prediction.apply(v -> 0);
        prediction.add(initModel.get().predict(df, false).firstPrediction().dv());
        for (var tree : trees) {
            prediction.add(tree.predict(df, false).firstPrediction().dv(copy()).mul(shrinkage.get()));
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
