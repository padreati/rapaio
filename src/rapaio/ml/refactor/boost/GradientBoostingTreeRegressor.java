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

package rapaio.ml.refactor.boost;

import rapaio.core.sample.Sampling;
import rapaio.data.*;
import rapaio.ml.refactor.boost.gbt.BTRegressor;
import rapaio.ml.refactor.boost.gbt.BoostingLossFunction;
import rapaio.ml.refactor.boost.gbt.L1BoostingLossFunction;
import rapaio.ml.refactor.simple.L2ConstantRegressor;
import rapaio.ml.refactor.tree.DecisionStumpRegressor;
import rapaio.ml.regressor.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class GradientBoostingTreeRegressor implements Regressor {

    // parameters
    int rounds = 10; // number of rounds
    BoostingLossFunction lossFunction = new L1BoostingLossFunction();
    BTRegressor regressor = new DecisionStumpRegressor();
    double shrinkage = 1.;
    double bootstrap = 1;

    // prediction
    Regressor initialRegressor = new L2ConstantRegressor();
    List<BTRegressor> trees;
    //
    Numeric fitLearn;
    Numeric fitValues;
    Frame df;
    String targetColName;

    @Override
    public Regressor newInstance() {
        return new GradientBoostingTreeRegressor()
                .setRounds(rounds)
                .setLossFunction(lossFunction)
                .setRegressor(regressor)
                .setInitialRegressor(initialRegressor)
                .setShrinkage(shrinkage);
    }

    public int getRounds() {
        return rounds;
    }

    public GradientBoostingTreeRegressor setRounds(int rounds) {
        this.rounds = rounds;
        return this;
    }

    public BoostingLossFunction getLossFunction() {
        return lossFunction;
    }

    public GradientBoostingTreeRegressor setLossFunction(BoostingLossFunction lossFunction) {
        this.lossFunction = lossFunction;
        return this;
    }

    public BTRegressor getRegressor() {
        return regressor;
    }

    public GradientBoostingTreeRegressor setRegressor(BTRegressor regressor) {
        this.regressor = regressor;
        return this;
    }

    public Regressor getInitialRegressor() {
        return initialRegressor;
    }

    public GradientBoostingTreeRegressor setInitialRegressor(Regressor initialRegressor) {
        this.initialRegressor = initialRegressor;
        return this;
    }

    public double getShrinkage() {
        return shrinkage;
    }

    public GradientBoostingTreeRegressor setShrinkage(double shrinkage) {
        this.shrinkage = shrinkage;
        return this;
    }

    public double getBootstrap() {
        return bootstrap;
    }

    public GradientBoostingTreeRegressor setBootstrap(double bootstrap) {
        this.bootstrap = bootstrap;
        return this;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        throw new IllegalArgumentException("Does not accept weights");
    }

    @Override
    public void learn(Frame train, String targetCols) {
        this.df = train;
        this.targetColName = targetCols;

        Var y = df.var(targetCols);
        Frame x = df.removeVars(new VarRange(targetCols));

        initialRegressor.learn(df, targetCols);
        trees = new ArrayList<>();

        fitLearn = Numeric.newFill(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            fitLearn.setValue(i, initialRegressor.getFitValues().value(i));
        }

        for (int i = 1; i <= rounds; i++) {
            Numeric gradient = lossFunction.gradient(y, fitLearn).withName("target");

            Frame xm = x.bindVars(gradient);
            BTRegressor tree = createBTRegressor();

            // bootstrap samples

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (bootstrap != 1) {
                bootstrapMapping = Mapping.newEmpty();
                int[] sample = new Sampling().sampleWOR((int) (bootstrap * xmLearn.rowCount()), xmLearn.rowCount());
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

            tree.predict(df);
            for (int j = 0; j < df.rowCount(); j++) {
                fitLearn.setValue(j, fitLearn.value(j) + shrinkage * tree.getFitValues().value(j));
            }

            // add tree in the predictors list

            trees.add(tree);
        }

        fitValues = Numeric.newEmpty();
        for (int i = 0; i < fitLearn.rowCount(); i++) {
            fitValues.addValue(fitLearn.value(i));
        }
    }

    public void learnFurther(int additionalRounds) {
        rounds += additionalRounds;

        Var y = df.var(targetColName);
        Frame x = df.removeVars(new VarRange(targetColName));

        for (int i = 0; i < additionalRounds; i++) {

            // build gradient

            Numeric gradient = lossFunction.gradient(y, fitLearn).withName("target");

            // build next tree and gradient learning data set

            Frame xm = x.bindVars(gradient);
            BTRegressor tree = createBTRegressor();

            // bootstrap samples if is the case

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (bootstrap != 1) {
                bootstrapMapping = Mapping.newEmpty();
                int[] sample = new Sampling().sampleWOR((int) (bootstrap * xmLearn.rowCount()), xmLearn.rowCount());
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

            tree.predict(df);
            for (int j = 0; j < df.rowCount(); j++) {
                fitLearn.setValue(j, fitLearn.value(j) + shrinkage * tree.getFitValues().value(j));
            }

            // add tree to the list of trees

            trees.add(tree);
        }

        fitValues = Numeric.newEmpty();
        for (int i = 0; i < fitLearn.rowCount(); i++) {
            fitValues.addValue(fitLearn.value(i));
        }
    }

    private BTRegressor createBTRegressor() {
        return regressor.newInstance();
    }

    @Override
    public void predict(Frame df) {
        initialRegressor.predict(df);
        fitValues = Numeric.newFill(df.rowCount()).withName(targetColName);
        for (int i = 0; i < df.rowCount(); i++) {
            fitValues.setValue(i, initialRegressor.getFitValues().value(i));
        }
        for (BTRegressor tree1 : trees) {
            Regressor tree = tree1;
            tree.predict(df);
            for (int i = 0; i < df.rowCount(); i++) {
                fitValues.setValue(i, fitValues.value(i) + shrinkage * tree.getFitValues().value(i));
            }
        }
    }

    @Override
    public Numeric getFitValues() {
        return fitValues;
    }

    @Override
    public Frame getAllFitValues() {
        return SolidFrame.newWrapOf(fitValues.rowCount(), fitValues);
    }
}
