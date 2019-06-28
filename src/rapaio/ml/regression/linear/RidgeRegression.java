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
package rapaio.ml.regression.linear;

import rapaio.core.stat.*;
import rapaio.data.*;
import rapaio.data.filter.frame.*;
import rapaio.math.linear.*;
import rapaio.math.linear.dense.*;
import rapaio.ml.common.*;
import rapaio.ml.regression.*;
import rapaio.printer.*;
import rapaio.printer.format.*;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author VHG6KOR
 */
public class RidgeRegression extends AbstractRegression implements DefaultPrintable {

    private static final long serialVersionUID = -6014222985456365210L;

    /**
     * Builds a new ridge regression model.
     *
     * @param lambda regularization parameter: 0 means no regularization, infinity means all coefficients shrink to 0
     * @return new ridge regression model
     */
    public static RidgeRegression newRidgeLm(double lambda) {
        return new RidgeRegression()
                .withLambda(lambda)
                .withIntercept(true)
                .withCentering(true)
                .withScaling(true);
    }

    protected boolean intercept = true;
    protected boolean centering = false;
    protected boolean scaling = false;
    protected RM beta;
    /*
    Regularization strength; must be a positive float. Regularization improves the conditioning
    of the problem and reduces the variance of the estimates.
    Larger values specify stronger regularization
     */
    private double lambda = 0.0;

    protected HashMap<String, Double> inputMean = new HashMap<>();
    protected HashMap<String, Double> inputSd = new HashMap<>();
    protected HashMap<String, Double> targetMean = new HashMap<>();

    @Override
    public Regression newInstance() {
        return newInstanceDecoration(new RidgeRegression())
                .withIntercept(hasIntercept())
                .withLambda(getLambda())
                .withCentering(hasCentering())
                .withScaling(hasScaling());
    }

    @Override
    public String name() {
        return "RidgeRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("(lambda=").append(Format.floatFlex(lambda));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VType.DOUBLE, VType.INT, VType.BINARY)
                .withTargetTypes(VType.DOUBLE)
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1_000_000)
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(false);
    }

    /**
     * @return true if the linear model adds an intercept
     */
    public boolean hasIntercept() {
        return intercept;
    }

    /**
     * Configure the model to introduce an intercept or not.
     *
     * @param intercept if true an intercept variable will be generated, false otherwise
     * @return linear model instance
     */
    public RidgeRegression withIntercept(boolean intercept) {
        this.intercept = intercept;
        return this;
    }

    public boolean hasCentering() {
        return centering;
    }

    public RidgeRegression withCentering(boolean centering) {
        this.centering = centering;
        return this;
    }

    public boolean hasScaling() {
        return scaling;
    }

    public RidgeRegression withScaling(boolean scaling) {
        this.scaling = scaling;
        return this;
    }

    public RV firstCoefficients() {
        return beta.mapCol(0);
    }

    public RV getCoefficients(int targetIndex) {
        return beta.mapCol(targetIndex);
    }

    public RM allCoefficients() {
        return beta;
    }

    public double getLambda() {
        return lambda;
    }

    public RidgeRegression withLambda(double lambda) {
        this.lambda = lambda;
        return this;
    }

    @Override
    public RidgeRegression fit(Frame df, String... targetVarNames) {
        return (RidgeRegression) super.fit(df, targetVarNames);
    }

    @Override
    public RidgeRegression fit(Frame df, Var weights, String... targetVarNames) {
        return (RidgeRegression) super.fit(df, weights, targetVarNames);
    }

    @Override
    protected FitSetup prepareFit(Frame df, Var weights, String... targetVarNames) {
        if (intercept) {
            return super.prepareFit(FIntercept.filter().apply(df), weights, targetVarNames);
        }
        return super.prepareFit(df, weights, targetVarNames);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        if (lambda < 0) {
            throw new IllegalArgumentException("lambda - regularization strength cannot be negative");
        }

        for (String inputName : inputNames) {
            if (FIntercept.INTERCEPT.equals(inputName)) {
                inputMean.put(FIntercept.INTERCEPT, 0.0);
                inputSd.put(FIntercept.INTERCEPT, 1.0);
                continue;
            }
            inputMean.put(inputName, centering ? Mean.of(df.rvar(inputName)).value() : 0);
            inputSd.put(inputName, scaling ? Variance.of(df.rvar(inputName)).sdValue() : 1);
        }
        for (String targetName : targetNames) {
            targetMean.put(targetName, centering ? Mean.of(df.rvar(targetName)).value() : 0);
        }

        String[] selNames = Arrays.copyOfRange(inputNames, intercept ? 1 : 0, inputNames.length);
        RM X = SolidRM.empty(df.rowCount() + selNames.length, selNames.length);
        RM Y = SolidRM.empty(df.rowCount() + selNames.length, targetNames.length);

        double sqrt = Math.sqrt(this.lambda);
        for (int i = 0; i < selNames.length; i++) {
            int varIndex = df.varIndex(selNames[i]);
            for (int j = 0; j < df.rowCount(); j++) {
                X.set(j, i, (df.getDouble(j, varIndex) - inputMean.get(selNames[i])) / (inputSd.get(selNames[i])));
            }
            X.set(i + df.rowCount(), i, sqrt);
        }
        for (int i = 0; i < targetNames.length; i++) {
            int varIndex = df.varIndex(targetNames[i]);
            for (int j = 0; j < df.rowCount(); j++) {
                Y.set(j, i, (df.getDouble(j, varIndex) - targetMean.get(targetNames[i])));
            }
        }

        RM rawBeta = QRDecomposition.from(X).solve(Y);
        int offset = intercept ? 1 : 0;
        beta = SolidRM.empty(rawBeta.rowCount() + offset, rawBeta.colCount());
        for (int i = 0; i < rawBeta.rowCount(); i++) {
            for (int j = 0; j < rawBeta.colCount(); j++) {
                beta.set(i + offset, j, rawBeta.get(i, j) / inputSd.get(inputNames[i + offset]));
            }
        }
        if (intercept) {
            for (int i = 0; i < beta.colCount(); i++) {
                double ym = targetMean.get(targetNames[i]);
                for (int j = 0; j < rawBeta.rowCount(); j++) {
                    ym -= beta.get(j + 1, i) * inputMean.get(inputNames[j + offset]);
                }
                beta.set(0, i, ym);
            }
        }
        return true;
    }

    @Override
    protected PredSetup preparePredict(Frame df, boolean withResiduals) {
        if (intercept) {
            return super.preparePredict(FIntercept.filter().apply(df), withResiduals);
        }
        return super.preparePredict(df, withResiduals);
    }

    @Override
    protected RidgeRPrediction corePredict(Frame df, boolean withResiduals) {
        RidgeRPrediction rp = new RidgeRPrediction(this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            for (int j = 0; j < rp.prediction(target).rowCount(); j++) {
                double fit = 0.0;
                for (int k = 0; k < inputNames().length; k++) {
                    fit += beta.get(k, i) * df.getDouble(j, inputName(k));
                }
                rp.prediction(target).setDouble(j, fit);
            }
        }

        rp.buildComplete();
        return rp;
    }

    @Override
    public RidgeRPrediction predict(Frame df) {
        return predict(df, false);
    }

    @Override
    public RidgeRPrediction predict(Frame df, boolean withResiduals) {
        return (RidgeRPrediction) super.predict(df, withResiduals);
    }
}
