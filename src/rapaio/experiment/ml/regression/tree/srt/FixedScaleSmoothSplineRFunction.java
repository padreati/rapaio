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

package rapaio.experiment.ml.regression.tree.srt;

import rapaio.core.SamplingTools;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.math.linear.DV;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.loss.Loss;
import rapaio.ml.regression.linear.LinearRegressionModel;
import rapaio.ml.regression.linear.LinearRegressionResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/20/19.
 */
public class FixedScaleSmoothSplineRFunction implements SmoothRFunction {

    public static FixedScaleSmoothSplineRFunction fromScales(int degree, int minSize, double trialPercentage, double[] sigmas) {
        return new FixedScaleSmoothSplineRFunction(degree, minSize, trialPercentage, sigmas);
    }

    private final int minSize;
    private final int degree;
    private final double[] sigmas;
    private final double trialPercentage;

    private String testVarName;
    private DV coeff;
    private double ref;
    private double sigma;
    private double leftMargin;
    private double rightMargin;

    private FixedScaleSmoothSplineRFunction(int degree, int minSize, double trialPercentage, double[] sigmas) {
        this.degree = degree;
        this.minSize = minSize;
        this.trialPercentage = trialPercentage;
        this.sigmas = sigmas;
    }

    public FixedScaleSmoothSplineRFunction newInstance() {
        return new FixedScaleSmoothSplineRFunction(degree, minSize, trialPercentage, sigmas);
    }

    private double sx(double x, double sigma, double c) {
        return 1.0 / (1.0 + Math.exp(sigma * (x - c)));
    }

    @Override
    public VarDouble fit(Frame df, Var weights, Var y, String testVarName) {
        this.testVarName = testVarName;

        int index = df.varIndex(testVarName);
        Var testVar = df.rvar(testVarName);

        DV bestBeta = null;
        double bestRef = Double.NaN;
        double bestSigma = Double.NaN;
        double bestError = Double.NaN;
        VarDouble bestYHat = null;

        Loss loss = new L2Loss();

        int[] rows = SamplingTools.sampleWOR(df.rowCount(), (int) Math.ceil(df.rowCount() * trialPercentage));
        Arrays.sort(rows);

        for (double sigma : sigmas) {
            for (int c : rows) {

                if (c < minSize || c + minSize >= df.rowCount()) {
                    continue;
                }
                double ref = df.getDouble(c, index);

                List<Var> features = new ArrayList<>();
                // build features corresponding to beta_{k+1}..beta_{2k}
                for (int i = 0; i <= degree; i++) {
                    int ii = i;
                    features.add(testVar.op().capply(v -> Math.pow(v, ii) * sx(v, sigma, ref)).name(testVarName + "_alpha_" + (i + 1)));
                }

                // build features corresponding to (beta_{1}-beta_{k+1}), (beta_{2}-beta_{k+2}), ...
                for (int i = 0; i <= degree; i++) {
                    int ii = i;
                    features.add(testVar.op().capply(v -> Math.pow(v, ii) * (1.0 - sx(v, sigma, ref))).name(testVarName + "_alpha_" + (degree + 2 + i)));
                }

                // fit a linear regression
                LinearRegressionModel lm = LinearRegressionModel.newModel().intercept.set(false);
//                RidgeRegression lm = RidgeRegression.newRidgeLm(100).withIntercept(false).withCentering(false).withScaling(false);

                features.add(y);
                BoundFrame bf = BoundFrame.byVars(features);

                try {
                    lm.fit(bf, y.name());
                    if (lm.firstCoefficients().norm(2) > 10_000) {
                        continue;
                    }
                } catch (RuntimeException ex) {
                    if (ex.getMessage().contains("Matrix is rank deficient.")) {
                        continue;
                    } else {
                        throw ex;
                    }
                }
                LinearRegressionResult pred = lm.predict(bf, false);
                VarDouble y_hat = pred.firstPrediction();
                double error = loss.errorScore(y, y_hat);

                if (Double.isNaN(bestError) || bestError > error) {
                    bestError = error;
                    this.coeff = pred.getBetaHat().mapCol(0);
                    this.sigma = sigma;
                    this.ref = ref;
                    bestYHat = y_hat;
                }
            }
        }
        return coeff == null ? null : bestYHat;
    }

    private double sx(Frame df, int row) {
        double value = df.getDouble(row, testVarName);
        return 1.0 / (1.0 + Math.exp(sigma * (value - ref)));
    }

    @Override
    public double predict(Frame df, int row) {
        double value = df.getDouble(row, testVarName);
        double left = 0.0;
        double right = 0.0;
        double sx = sx(df, row);

        for (int i = 0; i <= degree; i++) {
            left += coeff.get(i) * Math.pow(value, i);
            right += coeff.get(degree + 1 + i) * Math.pow(value, i);
        }
        return left * sx + right * (1 - sx);
    }


    @Override
    public double leftWeight(Frame df, int row) {
        return sx(df, row);
    }

    @Override
    public double rightWeight(Frame df, int row) {
        return 1 - sx(df, row);
    }

    @Override
    public double leftResidual(Frame df, Var y, int row) {
        return (y.getDouble(row) - predict(df, row)) * sx(df, row);
    }

    @Override
    public double rightResidual(Frame df, Var y, int row) {
        return (y.getDouble(row) - predict(df, row)) * (1 - sx(df, row));
    }
}
