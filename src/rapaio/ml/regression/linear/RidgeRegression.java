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

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.Regression;
import rapaio.sys.WS;

/**
 * @author VHG6KOR
 */
public class RidgeRegression extends AbstractLinearRegression {

    private static final long serialVersionUID = -6014222985456365210L;

    /**
     * Builds a new ridge regression model.
     *
     * @param lambda regularization parameter: 0 means no regularization, infinity means all coefficients shrink to 0
     * @return new ridge regression model
     */
    public static RidgeRegression newRidgeLm(double lambda) {
        return new RidgeRegression(lambda);
    }

    /*
    Regularization strength; must be a positive float. Regularization improves the conditioning
    of the problem and reduces the variance of the estimates.
    Larger values specify stronger regularization
     */
    private double lambda;

    protected RidgeRegression(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public Regression newInstance() {
        return new RidgeRegression(lambda);
    }

    @Override
    public String name() {
        return "RidgeRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("(lambda=").append(WS.formatFlex(lambda));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public RidgeRegression withInputFilters(FFilter... filters) {
        return (RidgeRegression) super.withInputFilters(filters);
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputTypes(VarType.NUMERIC, VarType.INDEX, VarType.BINARY, VarType.ORDINAL)
                .withTargetTypes(VarType.NUMERIC)
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1_000_000)
                .withAllowMissingInputValues(false)
                .withAllowMissingTargetValues(false);
    }

    @Override
    public RidgeRegression train(Frame df, String... targetVarNames) {
        return (RidgeRegression) super.train(df, targetVarNames);
    }

    @Override
    public RidgeRegression train(Frame df, Var weights, String... targetVarNames) {
        return (RidgeRegression) super.train(df, weights, targetVarNames);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        if (lambda < 0) {
            throw new IllegalArgumentException("lambda - regularization strength cannot be negative");
        }

        RM X = SolidRM.empty(df.rowCount() + inputNames.length, inputNames.length);
        RM Y = SolidRM.empty(df.rowCount() + inputNames.length, targetNames.length);

        double sqrt = Math.sqrt(this.lambda);
        for (int i = 0; i < inputNames.length; i++) {
            int varIndex = df.varIndex(inputNames[i]);
            for (int j = 0; j < df.rowCount(); j++) {
                X.set(j, i, df.value(j, varIndex));
            }
            X.set(i + df.rowCount(), varIndex, sqrt);
        }
        for (int i = 0; i < targetNames.length; i++) {
            int varIndex = df.varIndex(targetNames[i]);
            for (int j = 0; j < df.rowCount(); j++) {
                Y.set(j, i, df.value(j, varIndex));
            }
        }

        beta = QRDecomposition.from(X).solve(Y);
        return true;
    }
}
