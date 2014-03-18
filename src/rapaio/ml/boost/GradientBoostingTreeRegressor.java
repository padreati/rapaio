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

package rapaio.ml.boost;

import rapaio.core.sample.DiscreteSampling;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.MappedVector;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;
import rapaio.ml.boost.gbt.BTRegressor;
import rapaio.ml.boost.gbt.BoostingLossFunction;
import rapaio.ml.boost.gbt.L1BoostingLossFunction;
import rapaio.ml.simple.L2ConstantRegressor;
import rapaio.ml.tree.DecisionStumpRegressor;

import java.util.ArrayList;
import java.util.List;

/**
 * Gradient Boosting Tree
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class GradientBoostingTreeRegressor extends AbstractRegressor {

    // parameters
    int rounds = 10; // number of rounds
    BoostingLossFunction lossFunction = new L1BoostingLossFunction();
    BTRegressor regressor = new DecisionStumpRegressor();
    double shrinkage = 1.;
    double bootstrap = Double.NaN;

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
    public void learn(Frame train, String targetColName) {
        this.df = train;
        if (train.isMappedFrame()) {
            this.df = Frames.solidCopy(train);
        }
        this.targetColName = targetColName;

        Vector y = df.col(targetColName);
        Frame x = BaseFilters.removeCols(df, targetColName);
        initialRegressor.learn(df, targetColName);
        trees = new ArrayList<>();

        fitLearn = new Numeric(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            fitLearn.setValue(i, initialRegressor.getFitValues().getValue(i));
        }

        for (int i = 1; i <= rounds; i++) {
            Numeric gradient = lossFunction.gradient(y, fitLearn);

            Frame xm = Frames.addCol(x, gradient, "target", x.colCount());
            BTRegressor tree = createBTRegressor();

            // bootstrap samples

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (bootstrap != 1) {
                bootstrapMapping = new Mapping();
                int[] sample = new DiscreteSampling().sampleWOR((int) (bootstrap * xmLearn.rowCount()), xmLearn.rowCount());
                for (int j = 0; j < sample.length; j++) {
                    bootstrapMapping.add(xmLearn.rowId(sample[j]));
                }
                xmLearn = new MappedFrame(xm, bootstrapMapping);
                xLearn = new MappedFrame(x, bootstrapMapping);
            }

            // build regions

            tree.learn(xmLearn, "target");

            // fit residuals

            if (bootstrapMapping == null) {
                tree.boostFit(xLearn, y, fitLearn, lossFunction);
            } else {
                tree.boostFit(
                        xLearn,
                        new MappedVector(y, bootstrapMapping),
                        new MappedVector(fitLearn, bootstrapMapping),
                        lossFunction);
            }

            // add next prediction to the fit values

            tree.predict(df);
            for (int j = 0; j < df.rowCount(); j++) {
                fitLearn.setValue(j, fitLearn.getValue(j) + shrinkage * tree.getFitValues().getValue(j));
            }

            // add tree in the predictors list

            trees.add(tree);
        }

        fitValues = new Numeric(df.rowCount());
        for (int i = 0; i < fitLearn.rowCount(); i++) {
            fitValues.addValue(fitLearn.getValue(i));
        }
    }

    public void learnFurther(int additionalRounds) {
        rounds += additionalRounds;

        Vector y = df.col(targetColName);
        Frame x = BaseFilters.removeCols(df, targetColName);

        for (int i = 0; i < additionalRounds; i++) {

            // build gradient

            Numeric gradient = lossFunction.gradient(y, fitLearn);

            // build next tree and gradient learning data set

            Frame xm = Frames.addCol(x, gradient, "target", x.colCount());
            BTRegressor tree = createBTRegressor();

            // bootstrap samples if is the case

            Frame xmLearn = xm;
            Frame xLearn = x;
            Mapping bootstrapMapping = null;
            if (bootstrap != 1) {
                bootstrapMapping = new Mapping();
                int[] sample = new DiscreteSampling().sampleWOR((int) (bootstrap * xmLearn.rowCount()), xmLearn.rowCount());
                for (int j = 0; j < sample.length; j++) {
                    bootstrapMapping.add(xmLearn.rowId(sample[j]));
                }
                xmLearn = new MappedFrame(xm, bootstrapMapping);
                xLearn = new MappedFrame(x, bootstrapMapping);
            }

            // learn regions from gradients

            tree.learn(xmLearn, "target");

            // fit residuals

            if (bootstrapMapping == null) {
                tree.boostFit(xLearn, y, fitLearn, lossFunction);
            } else {
                tree.boostFit(
                        xLearn,
                        new MappedVector(y, bootstrapMapping),
                        new MappedVector(fitLearn, bootstrapMapping),
                        lossFunction);
            }

            // add next prediction to the fit values

            tree.predict(df);
            for (int j = 0; j < df.rowCount(); j++) {
                fitLearn.setValue(j, fitLearn.getValue(j) + shrinkage * tree.getFitValues().getValue(j));
            }

            // add tree to the list of trees

            trees.add(tree);
        }

        fitValues = new Numeric(df.rowCount());
        for (int i = 0; i < fitLearn.rowCount(); i++) {
            fitValues.addValue(fitLearn.getValue(i));
        }
    }

    private BTRegressor createBTRegressor() {
        return regressor.newInstance();
    }

    @Override
    public void predict(Frame df) {
        initialRegressor.predict(df);
        fitValues = new Numeric(df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            fitValues.setValue(i, initialRegressor.getFitValues().getValue(i));
        }
        for (int m = 0; m < trees.size(); m++) {
            Regressor tree = trees.get(m);
            tree.predict(df);
            for (int i = 0; i < df.rowCount(); i++) {
                fitValues.setValue(i, fitValues.getValue(i) + shrinkage * tree.getFitValues().getValue(i));
            }
        }
    }

    @Override
    public Numeric getFitValues() {
        return fitValues;
    }

    @Override
    public Frame getAllFitValues() {
        return new SolidFrame(fitValues.rowCount(), new Vector[]{fitValues}, new String[]{targetColName});
    }
}
