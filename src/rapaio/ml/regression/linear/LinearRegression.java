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

import rapaio.data.*;
import rapaio.data.filter.FFilter;
import rapaio.math.linear.Linear;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.Regression;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegression extends AbstractLinearRegression {

    public static LinearRegression newLm() {
        return new LinearRegression()
                .withIntercept(true)
                .withCentering(false)
                .withScaling(false);
    }

    private static final long serialVersionUID = 8610329390138787530L;

    @Override
    public Regression newInstance() {
        return new LinearRegression()
                .withIntercept(intercept)
                .withCentering(centering)
                .withScaling(scaling);
    }

    @Override
    public String name() {
        return "LinearRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("(");
        sb.append(")");
        return sb.toString();
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
    public LinearRegression withInputFilters(FFilter... filters) {
        return (LinearRegression) super.withInputFilters(filters);
    }

    @Override
    public LinearRegression withIntercept(boolean intercept) {
        return (LinearRegression) super.withIntercept(intercept);
    }

    @Override
    public LinearRegression withCentering(boolean centering) {
        return (LinearRegression) super.withCentering(centering);
    }

    @Override
    public LinearRegression withScaling(boolean scaling) {
        return (LinearRegression) super.withScaling(scaling);
    }

    @Override
    public LinearRegression train(Frame df, String... targetVarNames) {
        return (LinearRegression) super.train(df, targetVarNames);
    }

    @Override
    public LinearRegression train(Frame df, Var weights, String... targetVarNames) {
        return (LinearRegression) super.train(df, weights, targetVarNames);
    }

    @Override
    protected TrainSetup prepareTraining(TrainSetup trainSetup) {
        if (intercept) {
            boolean exists = false;
            Frame prepared = trainSetup.df;
            for (String varName : prepared.varNames()) {
                if (varName.equals(INTERCEPT)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                NumVar var = NumVar.fill(prepared.rowCount(), 1).withName(INTERCEPT);
                prepared = SolidFrame.byVars(var).bindVars(prepared);
                return super.prepareTraining(TrainSetup.valueOf(prepared, trainSetup.w, trainSetup.targetVars));
            }
        }
        return super.prepareTraining(trainSetup);
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        if (targetNames().length == 0) {
            throw new IllegalArgumentException("OLS must specify at least one target variable name");
        }

        RM X = SolidRM.copy(df.mapVars(inputNames()));
        RM Y = SolidRM.copy(df.mapVars(targetNames()));
        beta = QRDecomposition.from(X).solve(Y);
        return true;
    }

    @Override
    protected FitSetup prepareFit(FitSetup fitSetup) {
        if (intercept) {
            boolean exists = false;
            Frame prepared = fitSetup.df;
            for (String varName : prepared.varNames()) {
                if (varName.equals(INTERCEPT)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                NumVar var = NumVar.fill(prepared.rowCount(), 1).withName(INTERCEPT);
                prepared = SolidFrame.byVars(var).bindVars(prepared);
                return super.prepareFit(FitSetup.valueOf(prepared, fitSetup.withResiduals));
            }
        }
        return super.prepareFit(fitSetup);
    }
}
