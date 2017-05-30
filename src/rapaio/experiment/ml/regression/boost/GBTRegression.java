/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.Regression;
import rapaio.experiment.ml.regression.boost.gbt.BTRegression;
import rapaio.experiment.ml.regression.boost.gbt.GBTLossFunction;
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

    // parameters
    GBTLossFunction lossFunction = new GBTLossFunction.Huber();

    Regression initRegression = L2Regression.create();
    BTRegression regressor = RTree.buildCART().withMaxDepth(4).withMinCount(10);
    double shrinkage = 1.0;

    // prediction
    NumericVar fitLearn;
    NumericVar fitValues;
    List<BTRegression> trees;

    @Override
    public Regression newInstance() {
        return new GBTRegression()
                .withLossFunction(lossFunction)
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
        sb.append("loss=").append(lossFunction.name()).append(", ");
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
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NUMERIC, VarType.ORDINAL, VarType.NOMINAL)
                .withTargetTypes(VarType.NUMERIC)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(false);
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

    public GBTRegression withSampler(RowSampler sampler) {
        return (GBTRegression) super.withSampler(sampler);
    }

    public GBTRegression withRuns(int runs) {
        return (GBTRegression) super.withRuns(runs);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        Var y = df.getVar(firstTargetName());
        Frame x = df.removeVars(VRange.of(firstTargetName()));

        initRegression.train(df, firstTargetName());
        RFit initPred = initRegression.fit(df, false);
        trees = new ArrayList<>();

        fitLearn = NumericVar.fill(df.getRowCount());
        for (int i = 0; i < df.getRowCount(); i++) {
            fitLearn.setValue(i, initPred.firstFit().getValue(i));
        }

        for (int i = 1; i <= runs(); i++) {
            NumericVar gradient = lossFunction.gradient(y, fitLearn).withName("target");

            Frame xm = x.bindVars(gradient);
            BTRegression tree = regressor.newInstance();

            // frame sampling

            Mapping samplerMapping = sampler().nextSample(xm, weights).mapping;
            Frame xmLearn = xm.mapRows(samplerMapping);
            Frame xLearn = x.mapRows(samplerMapping);

            // build regions

            tree.train(xmLearn, "target");

            // fit residuals

            tree.boostFit(
                    xLearn,
                    MappedVar.byRows(y, samplerMapping),
                    MappedVar.byRows(fitLearn, samplerMapping),
                    lossFunction);

            // add next prediction to the fit values

            RFit treePred = tree.fit(df, false);
            for (int j = 0; j < df.getRowCount(); j++) {
                fitLearn.setValue(j, fitLearn.getValue(j) + shrinkage * treePred.firstFit().getValue(j));
            }

            // add tree in the predictors list

            trees.add(tree);
        }

        fitValues = NumericVar.empty();
        for (int i = 0; i < fitLearn.getRowCount(); i++) {
            fitValues.addValue(fitLearn.getValue(i));
        }
        return true;
    }

    @Override
    protected RFit coreFit(final Frame df, final boolean withResiduals) {
        RFit pred = RFit.build(this, df, withResiduals);
        RFit initPred = initRegression.fit(df);
        for (int i = 0; i < df.getRowCount(); i++) {
            pred.firstFit().setValue(i, initPred.firstFit().getValue(i));
        }
        for (BTRegression tree : trees) {
            RFit treePred = tree.fit(df);
            for (int i = 0; i < df.getRowCount(); i++) {
                pred.firstFit().setValue(i, pred.firstFit().getValue(i) + shrinkage * treePred.firstFit().getValue(i));
            }
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String getSummary() {
        throw new IllegalArgumentException("not implemented");
    }
}
