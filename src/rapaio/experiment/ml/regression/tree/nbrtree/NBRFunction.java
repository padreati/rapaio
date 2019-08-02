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

package rapaio.experiment.ml.regression.tree.nbrtree;

import rapaio.core.*;
import rapaio.data.*;
import rapaio.data.unique.*;
import rapaio.experiment.ml.regression.loss.*;
import rapaio.math.linear.*;
import rapaio.ml.regression.*;
import rapaio.ml.regression.linear.*;
import rapaio.ml.regression.simple.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/23/19.
 */
public interface NBRFunction {

    /**
     * Builds a new instance of the function, useful because each function
     * is a model which can be fittted on data. As such, when one wants to fit a new
     * function then it creates a new instance and fit that instance on data.
     *
     * @return new function model instance
     */
    NBRFunction newInstance();

    /**
     * Compute function value for the given observation
     *
     * @param df  observations data frame
     * @param row observation row in the given frame
     * @return function value
     */
    double eval(Frame df, int row);

    /**
     * Fit function on given data
     *
     * @param df          input data
     * @param weights     weights of the observations
     * @param y           target data (usually residuals)
     * @param testVarName test variable name
     * @return predicted values for each observations
     */
    VarDouble fit(Frame df, Var weights, Var y, String testVarName);

    NBRFunction CONSTANT = new ConstantFunction();
    NBRFunction LINEAR = new LinearFunction();
    NBRFunction QUADRATIC = new QuadraticFunction();

    static NBRFunction spline(int degree, double lambda, int minSize, double trialPercentage) {
        return new SplineFunction(degree, lambda, minSize, trialPercentage);
    }
}

class LinearFunction implements NBRFunction {

    private String testVarName;
    private double beta_0;
    private double beta_1;

    public LinearFunction newInstance() {
        return new LinearFunction();
    }

    @Override
    public double eval(Frame df, int row) {
        return beta_0 + beta_1 * df.getDouble(row, testVarName);
    }

    @Override
    public VarDouble fit(Frame df, Var weights, Var y, String testVarName) {
        LinearRegressionModel lm = LinearRegressionModel.newLm().withIntercept(true);

        Frame map = BoundFrame.byVars(df.rvar(testVarName), y);
        try {
            lm.fit(map, weights, y.name());
            LinearRegressionResult pred = lm.withIntercept(true).predict(map, false);
            this.beta_0 = pred.getBetaHat().get(0, 0);
            this.beta_1 = pred.getBetaHat().get(1, 0);
            this.testVarName = testVarName;
            return pred.firstPrediction();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Linear {beta0:%.3f, beta1:%.3f}", beta_0, beta_1);
    }
}

class QuadraticFunction implements NBRFunction {

    private String testVarName;
    private double beta_0;
    private double beta_1;
    private double beta_2;

    public QuadraticFunction newInstance() {
        return new QuadraticFunction();
    }

    @Override
    public double eval(Frame df, int row) {
        double x = df.getDouble(row, testVarName);
        return beta_0 + beta_1 * x + beta_2 * x * x;
    }

    @Override
    public VarDouble fit(Frame df, Var weights, Var y, String testVarName) {
        LinearRegressionModel lm = LinearRegressionModel.newLm().withIntercept(true);

        if (Unique.of(df.rvar(testVarName), false).uniqueCount() <= 5) {
            return null;
        }

        VarDouble square = VarDouble.from(df.rvar(testVarName), v -> v * v)
                .withName(testVarName + "^2");
        Frame map = BoundFrame.byVars(df.rvar(testVarName), square, y);
        try {
            lm.fit(map, weights, y.name());
            LinearRegressionResult pred = lm.withIntercept(true).predict(map, false);
            this.beta_0 = pred.getBetaHat().get(0, 0);
            this.beta_1 = pred.getBetaHat().get(1, 0);
            this.beta_2 = pred.getBetaHat().get(2, 0);
            this.testVarName = testVarName;
            return pred.firstPrediction();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Quadratic {beta0:%.3f, beta1:%.3f, beta2:%.3f}", beta_0, beta_1, beta_2);
    }
}

class ConstantFunction implements NBRFunction {

    private String testVarName;
    private double constant;

    public ConstantFunction newInstance() {
        return new ConstantFunction();
    }

    @Override
    public double eval(Frame df, int row) {
        return constant;
    }

    @Override
    public VarDouble fit(Frame df, Var weights, Var y, String testVarName) {
        L2RegressionModel model = L2RegressionModel.newL2();

        Frame map = BoundFrame.byVars(y);
        model.fit(map, weights, y.name());
        RegressionResult pred = model.predict(map, false);
        this.constant = model.getMeans()[0];

        return pred.firstPrediction();
    }

    @Override
    public String toString() {
        return String.format("Constant {beta0:%.3f}", constant);
    }
}

class SplineFunction implements NBRFunction {

    private final int minSize;
    private final double trialPercentage;
    private final int degree;
    private final double lambda;

    private String testVarName;
    private RV coeff;
    private double threshold;

    SplineFunction(int degree, double lambda, int minSize, double trialPercentage) {
        this.degree = degree;
        this.lambda = lambda;
        this.minSize = minSize;
        this.trialPercentage = trialPercentage;
    }

    public SplineFunction newInstance() {
        return new SplineFunction(degree, lambda, minSize, trialPercentage);
    }

    @Override
    public double eval(Frame df, int row) {
        double value = df.getDouble(row, testVarName);
        double prediction = coeff.get(0);
        for (int i = 0; i < degree; i++) {
            prediction += coeff.get(i + 1) * Math.pow(value, i + 1);
        }
        for (int i = 0; i < degree; i++) {
            prediction += coeff.get(i + 1 + degree) * Math.max(0, Math.pow(value - threshold, i + 1));
        }
        return prediction;
    }

    @Override
    public VarDouble fit(Frame df, Var weights, Var y, String testVarName) {
        this.testVarName = testVarName;

//         this is not the fastest implementation possible,
//         but it does not rely also on linear model which is more expensive
        int index = df.varIndex(testVarName);

        RV bestBeta = null;
        double bestRef = Double.NaN;
        double bestError = Double.NaN;
        VarDouble bestYHat = null;
        RegressionLoss loss = new L2RegressionLoss();

        int[] rows = SamplingTools.sampleWOR(df.rowCount(), (int) Math.ceil(df.rowCount() * trialPercentage));
//        int[] rows = new int[] {df.rowCount()/2};

        List<Var> testFeatures = new ArrayList<>();
        for (int i = 0; i < degree; i++) {
            if (i == 0) {
                testFeatures.add(df.rvar(testVarName));
            } else {
                int ii = i;
                VarDouble feature = VarDouble
                        .from(df.rowCount(), row -> Math.pow(df.getDouble(row, index), ii + 1))
                        .withName(testVarName + "_" + (ii + 1));
                testFeatures.add(feature);
            }
        }
        for (int t : rows) {

            // check for min
            int left = 0;
            int right = 0;
            double ref = df.getDouble(t, index);

            for (int i = 0; i < df.rowCount(); i++) {
                if (df.getDouble(i, index) < ref) {
                    left++;
                } else {
                    right++;
                }
            }
            if (left < minSize || right < minSize) {
                continue;
            }

            // fit a linear regression
            LinearRegressionModel rlm = LinearRegressionModel.newLm().withIntercept(true);
//            RidgeRegression rlm = RidgeRegression.newRidgeLm(lambda).withIntercept(true);

            List<Var> features = new ArrayList<>(testFeatures);
            for (int i = 0; i < degree; i++) {
                int ii = i;
                VarDouble term = VarDouble
                        .from(df.rowCount(), row -> Math.max(0, Math.pow(df.getDouble(row, index) - ref, ii + 1)))
                        .withName(testVarName + "_knot_" + (ii + 1));
                features.add(term);
            }
            features.add(y);
            BoundFrame bf = BoundFrame.byVars(features);

            try {
                rlm.fit(bf, y.name());
//                rlm.printSummary();
            } catch (RuntimeException ex) {
                if (ex.getMessage().contains("Matrix is rank deficient.")) {
                    continue;
                } else {
                    throw ex;
                }
            }
            LinearRegressionResult pred = rlm.predict(bf, false);
            VarDouble y_hat = pred.firstPrediction();
            double error = loss.computeErrorScore(y, y_hat);

            if (Double.isNaN(bestError) || bestError > error) {
                bestError = error;
                coeff = pred.getBetaHat().mapCol(0);
                threshold = ref;
                bestYHat = y_hat;
            }
        }
        return coeff == null ? null : bestYHat;
    }


    @Override
    public String toString() {
        return String.format("Constant {threshold:.3f, beta0:%.3f, beta1:%.3f, beta2:%.3f, beta3:%.3f }",
                threshold, coeff.get(0), coeff.get(1), coeff.get(2));
    }
}