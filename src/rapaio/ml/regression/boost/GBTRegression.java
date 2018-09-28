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

import rapaio.data.Frame;
import rapaio.data.MappedVar;
import rapaio.data.Mapping;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VType;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RPrediction;
import rapaio.ml.regression.Regression;
import rapaio.ml.regression.boost.gbt.GBTRegressionLoss;
import rapaio.ml.regression.boost.gbt.GBTRegressionLossL1;
import rapaio.ml.regression.loss.L2RegressionLoss;
import rapaio.ml.regression.loss.RegressionLoss;
import rapaio.ml.regression.simple.L2Regression;
import rapaio.ml.regression.tree.RTree;
import rapaio.printer.Printable;

import java.util.ArrayList;
import java.util.List;

import static rapaio.sys.WS.formatFlex;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class GBTRegression extends AbstractRegression implements Printable {

    private static final long serialVersionUID = 4559540258922653130L;

    private GBTRegressionLoss lossFunction = new GBTRegressionLossL1();
    private RegressionLoss regressionLoss = new L2RegressionLoss();
    private Regression initRegression = L2Regression.create();
    private RTree regressor = RTree.newCART()
            .withMaxDepth(4)
            .withMinCount(10);
    private double shrinkage = 1.0;

    // prediction
    VarDouble fitValues;
    List<RTree> trees;

    @Override
    public Regression newInstance() {
        return new GBTRegression()
                .withInitRegressor(initRegression)
                .withRegressor(regressor)
                .withShrinkage(shrinkage)
                .withSampler(sampler())
                .withRuns(runs());
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
        sb.append("initRegression=").append(initRegression.fullName()).append(", ");
        sb.append("regression=").append(regressor.fullName()).append(", ");
        sb.append("shrinkage=").append(formatFlex(shrinkage)).append(", ");
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
                .withInputTypes(VType.BOOLEAN, VType.INT, VType.DOUBLE, VType.NOMINAL)
                .withTargetTypes(VType.DOUBLE)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(false);
    }

    public GBTRegression withLossFunction(RegressionLoss lossFunction) {
        this.regressionLoss = lossFunction;
        return this;
    }

    public GBTRegression withRegressor(RTree regressor) {
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

    public GBTRegression withSampler(RowSampler sampler) {
        return (GBTRegression) super.withSampler(sampler);
    }

    public GBTRegression withRuns(int runs) {
        return (GBTRegression) super.withRuns(runs);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        trees = new ArrayList<>();

        Var y = df.rvar(firstTargetName());
        Frame x = df.removeVars(VRange.of(firstTargetName()));

        initRegression.fit(df, weights, firstTargetName());
        fitValues = initRegression.predict(df, false).firstFit().solidCopy();

        for (int i = 1; i <= runs(); i++) {
            Var gradient = lossFunction.gradient(y, fitValues).withName("target");

            Frame xm = x.bindVars(gradient);
            RTree tree = regressor.newInstance();

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

            RPrediction treePred = tree.predict(df, false);
            for (int j = 0; j < df.rowCount(); j++) {
                fitValues.setDouble(j, fitValues.getDouble(j) + shrinkage * treePred.firstFit().getDouble(j));
            }

            // add tree in the predictors list

            trees.add(tree);

            if(runningHook()!=null)
                runningHook().accept(this, i);
        }
        return true;
    }

    @Override
    protected RPrediction corePredict(final Frame df, final boolean withResiduals) {
        RPrediction pred = RPrediction.build(this, df, withResiduals);
        RPrediction initPred = initRegression.predict(df, false);
        for (int i = 0; i < df.rowCount(); i++) {
            pred.firstFit().setDouble(i, initPred.firstFit().getDouble(i));
        }
        for (RTree tree : trees) {
            RPrediction treePred = tree.predict(df, false);
            for (int i = 0; i < df.rowCount(); i++) {
                pred.firstFit().setDouble(i, pred.firstFit().getDouble(i) + shrinkage * treePred.firstFit().getDouble(i));
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
