/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.regressor.boost;

import rapaio.core.sample.Sampling;
import rapaio.data.*;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.RResult;
import rapaio.ml.regressor.Regressor;
import rapaio.ml.regressor.RunningRegressor;
import rapaio.ml.regressor.boost.gbt.BTRegressor;
import rapaio.ml.regressor.boost.gbt.GBTLossFunction;
import rapaio.ml.regressor.simple.L2Regressor;
import rapaio.ml.regressor.tree.rtree.RTree;
import rapaio.printer.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GBTRegressor extends AbstractRegressor implements RunningRegressor {

    // parameters
    int runs = 1; // number of rounds
    GBTLossFunction lossFunction = new GBTLossFunction.Huber();

    Regressor initRegressor = new L2Regressor();
    BTRegressor regressor = RTree.buildCART().withMaxDepth(4).withMinCount(10);
    double shrinkage = 1.0;
    boolean useBootstrap = false;
    double bootstrapSize = 1.0;

    // prediction
    Numeric fitLearn;
    Numeric fitValues;
    List<BTRegressor> trees;

    @Override
    public Regressor newInstance() {
        return new GBTRegressor()
                .withLossFunction(lossFunction)
                .withInitRegressor(initRegressor)
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
        sb.append("initRegressor=").append(initRegressor.fullName()).append(", ");
        sb.append("regressor=").append(regressor.fullName()).append(", ");
        sb.append("shrinkage=").append(Printer.formatDecShort.format(shrinkage)).append(", ");
        sb.append("useBootstrap=").append(useBootstrap).append(", ");
        sb.append("bootstrapSize=").append(Printer.formatDecShort.format(bootstrapSize)).append(", ");
        sb.append("runs=").append(runs);
        sb.append("}");
        return sb.toString();
    }

    public GBTRegressor withLossFunction(GBTLossFunction lossFunction) {
        this.lossFunction = lossFunction;
        return this;
    }

    public GBTRegressor withRegressor(BTRegressor regressor) {
        this.regressor = regressor;
        return this;
    }

    public GBTRegressor withInitRegressor(Regressor initRegressor) {
        this.initRegressor = initRegressor;
        return this;
    }

    public GBTRegressor withShrinkage(double shrinkage) {
        this.shrinkage = shrinkage;
        return this;
    }

    public GBTRegressor withBootstrap(boolean use) {
        this.useBootstrap = use;
        return this;
    }

    public GBTRegressor withBootstrapSize(double bootstrapSize) {
        this.bootstrapSize = bootstrapSize;
        return this;
    }

    public GBTRegressor withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        List<String> list = new VarRange(targetVarNames).parseVarNames(df);
        if (list.size() != 1) {
            throw new IllegalArgumentException("GBT accepts a single target variable");
        }

        this.targetNames = new String[]{list.get(0)};

        Var y = df.var(firstTargetName());
        Frame x = df.removeVars(new VarRange(firstTargetName()));

        initRegressor.learn(df, firstTargetName());
        RResult initPred = initRegressor.predict(df, false);
        trees = new ArrayList<>();

        fitLearn = Numeric.newFill(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            fitLearn.setValue(i, initPred.firstFit().value(i));
        }

        for (int i = 1; i <= runs; i++) {
            Numeric gradient = lossFunction.gradient(y, fitLearn).withName("target");

            Frame xm = x.bindVars(gradient);
            BTRegressor tree = regressor.newInstance();

            // bootstrap samples

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (useBootstrap) {
                bootstrapMapping = Mapping.newEmpty();
                int[] sample = Sampling.sampleWOR((int) (bootstrapSize * xmLearn.rowCount()), xmLearn.rowCount());
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

            RResult treePred = tree.predict(df, false);
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
    public void learnFurther(Frame df, Var ignored, int runs, String... targetVarNames) {

        withRuns(runs);

        List<String> list = new VarRange(targetVarNames).parseVarNames(df);
        if (list.size() != 1) {
            throw new IllegalArgumentException("GBT accepts a single target variable");
        }

        // we have learned nothing before

        if (targetNames == null) {
            learn(df, ignored, targetVarNames);
            return;
        }

        // we learned something that does not fit

        if (!targetNames[0].equals(list.get(0))) {
            throw new IllegalArgumentException("Incompatible previously fit");
        }

        Var y = df.var(firstTargetName());
        Frame x = df.removeVars(new VarRange(firstTargetName()));

        for (int i = trees.size(); i < runs; i++) {

            // build gradient

            Numeric gradient = lossFunction.gradient(y, fitLearn).withName("target");

            // build next tree and gradient learning data set

            Frame xm = x.bindVars(gradient);
            BTRegressor tree = regressor.newInstance();

            // bootstrap samples if is the case

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (useBootstrap) {
                bootstrapMapping = Mapping.newEmpty();
                int[] sample = Sampling.sampleWOR((int) (bootstrapSize * xmLearn.rowCount()), xmLearn.rowCount());
                for (int aSample : sample) {
                    bootstrapMapping.add(aSample);
                }
                xmLearn = MappedFrame.newByRow(xm, bootstrapMapping);
                xLearn = MappedFrame.newByRow(x, bootstrapMapping);
            }

            // learn regions from gradients

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

            RResult treePred = tree.predict(df, false);
            for (int j = 0; j < df.rowCount(); j++) {
                fitLearn.setValue(j, fitLearn.value(j) + shrinkage * treePred.firstFit().value(j));
            }

            // add tree to the list of trees

            trees.add(tree);
        }

        fitValues = Numeric.newEmpty();
        for (int i = 0; i < fitLearn.rowCount(); i++) {
            fitValues.addValue(fitLearn.value(i));
        }
    }

    @Override
    public RResult predict(final Frame df, final boolean withResiduals) {
        RResult pred = RResult.newEmpty(this, df, withResiduals);
        for (String targetName : targetNames) {
            pred.addTarget(targetName);
        }

        RResult initPred = initRegressor.predict(df);
        for (int i = 0; i < df.rowCount(); i++) {
            pred.firstFit().setValue(i, initPred.firstFit().value(i));
        }
        for (BTRegressor tree : trees) {
            RResult treePred = tree.predict(df);
            for (int i = 0; i < df.rowCount(); i++) {
                pred.firstFit().setValue(i, pred.firstFit().value(i) + shrinkage * treePred.firstFit().value(i));
            }
        }
        pred.buildComplete();
        return pred;
    }
}
