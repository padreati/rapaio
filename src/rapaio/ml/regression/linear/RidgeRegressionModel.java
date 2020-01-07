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

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.filter.FIntercept;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.ml.common.Capabilities;
import rapaio.printer.format.Format;
import rapaio.printer.format.TextTable;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author VHG6KOR
 */
public class RidgeRegressionModel extends BaseLinearRegressionModel<RidgeRegressionModel> {

    private static final long serialVersionUID = -6014222985456365210L;

    /**
     * Builds a new ridge regression model.
     *
     * @param lambda regularization parameter: 0 means no regularization, infinity means all coefficients shrink to 0
     * @return new ridge regression model
     */
    public static RidgeRegressionModel newRidgeLm(double lambda) {
        return new RidgeRegressionModel()
                .withLambda(lambda)
                .withIntercept(true)
                .withCentering(true)
                .withScaling(true);
    }

    private boolean centering = false;
    private boolean scaling = false;
    /*
    Regularization strength; must be a positive float. Regularization improves the conditioning
    of the problem and reduces the variance of the estimates.
    Larger values specify stronger regularization
     */
    private double lambda = 0.0;

    // learning artifacts

    private HashMap<String, Double> inputMean = new HashMap<>();
    private HashMap<String, Double> inputSd = new HashMap<>();
    private HashMap<String, Double> targetMean = new HashMap<>();

    @Override
    public RidgeRegressionModel newInstance() {
        return newInstanceDecoration(new RidgeRegressionModel())
                .withLambda(lambda)
                .withIntercept(intercept)
                .withCentering(centering)
                .withScaling(scaling);
    }

    @Override
    public String name() {
        return "RidgeRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("(");
        sb.append("lambda=").append(Format.floatFlex(lambda)).append(",");
        sb.append("intercept=").append(intercept).append(",");
        sb.append("center=").append(centering).append(",");
        sb.append("scaling=").append(scaling).append(")");

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
     * Configure the model to introduce an intercept or not.
     *
     * @param intercept if true an intercept variable will be generated, false otherwise
     * @return linear model instance
     */
    public RidgeRegressionModel withIntercept(boolean intercept) {
        return (RidgeRegressionModel) super.withIntercept(intercept);
    }

    public boolean hasCentering() {
        return centering;
    }

    public RidgeRegressionModel withCentering(boolean centering) {
        this.centering = centering;
        return this;
    }

    public boolean hasScaling() {
        return scaling;
    }

    public RidgeRegressionModel withScaling(boolean scaling) {
        this.scaling = scaling;
        return this;
    }

    public double getLambda() {
        return lambda;
    }

    public RidgeRegressionModel withLambda(double lambda) {
        this.lambda = lambda;
        return this;
    }

    @Override
    public RidgeRegressionModel fit(Frame df, String... targetVarNames) {
        return (RidgeRegressionModel) super.fit(df, targetVarNames);
    }

    @Override
    public RidgeRegressionModel fit(Frame df, Var weights, String... targetVarNames) {
        return (RidgeRegressionModel) super.fit(df, weights, targetVarNames);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        if (lambda < 0) {
            throw new IllegalArgumentException("lambda - regularization strength cannot be negative");
        }
        boolean hasIntercept = false;
        for (String inputName : inputNames) {
            if (FIntercept.INTERCEPT.equals(inputName)) {
                hasIntercept = true;
                inputMean.put(FIntercept.INTERCEPT, 0.0);
                inputSd.put(FIntercept.INTERCEPT, 1.0);
                continue;
            }
            inputMean.put(inputName, Mean.of(df.rvar(inputName)).value());
            inputSd.put(inputName, Variance.of(df.rvar(inputName)).sdValue());
        }
        for (String targetName : targetNames) {
            targetMean.put(targetName, centering ? Mean.of(df.rvar(targetName)).value() : 0);
        }

        String[] selNames = Arrays.copyOfRange(inputNames, hasIntercept ? 1 : 0, inputNames.length);
        DMatrix X = SolidDMatrix.empty(df.rowCount() + selNames.length, selNames.length);
        DMatrix Y = SolidDMatrix.empty(df.rowCount() + selNames.length, targetNames.length);

        double sqrt = Math.sqrt(this.lambda);
        for (int i = 0; i < selNames.length; i++) {
            int varIndex = df.varIndex(selNames[i]);
            for (int j = 0; j < df.rowCount(); j++) {
                X.set(j, i, (df.getDouble(j, varIndex) - (centering ? inputMean.get(selNames[i]) : 0))
                        / (scaling ? inputSd.get(selNames[i]) : 1));
            }
            X.set(i + df.rowCount(), i, sqrt);
        }
        for (int i = 0; i < targetNames.length; i++) {
            int varIndex = df.varIndex(targetNames[i]);
            for (int j = 0; j < df.rowCount(); j++) {
                Y.set(j, i, df.getDouble(j, varIndex));
            }
        }

        DMatrix rawBeta = QRDecomposition.from(X).solve(Y);
        int offset = hasIntercept ? 1 : 0;
        beta = SolidDMatrix.empty(rawBeta.rowCount() + offset, rawBeta.colCount());
        for (int i = 0; i < rawBeta.rowCount(); i++) {
            for (int j = 0; j < rawBeta.colCount(); j++) {
                beta.set(i + offset, j, rawBeta.get(i, j) / (scaling ? inputSd.get(inputNames[i + offset]) : 1));
            }
        }
        if (hasIntercept) {
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
    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        for (int i = 0; i < targetNames.length; i++) {
            String targetName = targetNames[i];
            sb.append("Target <<< ").append(targetName).append(" >>>\n\n");
            sb.append("> Coefficients: \n");
            DVector coeff = beta.mapCol(i);

            TextTable tt = TextTable.empty(coeff.size() + 1, 2, 1, 0);
            tt.textCenter(0, 0, "Name");
            tt.textCenter(0, 1, "Estimate");
            for (int j = 0; j < coeff.size(); j++) {
                tt.textLeft(j + 1, 0, inputNames[j]);
                tt.floatMedium(j + 1, 1, coeff.get(j));
            }
            sb.append(tt.getDynamicText());
            sb.append("\n");
        }
        return sb.toString();
    }
}
