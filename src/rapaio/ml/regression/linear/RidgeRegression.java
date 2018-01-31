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
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.Regression;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * @author VHG6KOR
 */
public class RidgeRegression extends AbstractRegression {

    private static final long serialVersionUID = -6014222985456365210L;

    public static RidgeRegression newRr(double alpha) {
        return new RidgeRegression(alpha);
    }

    private double alpha = 0;

    protected RM beta;

    /**
     * @param alpha Regularization strength; must be a positive float. Regularization improves the conditioning of the problem and reduces the variance of the estimates. Larger values specify stronger regularization
     */
    public RidgeRegression(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public Regression newInstance() {
        return new RidgeRegression(alpha);
    }

    @Override
    public String name() {
        return "RidgeRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
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

    public RV firstCoeff() {
        return beta.mapCol(0);
    }

    public RV coefficients(int targetIndex) {
        return beta.mapCol(targetIndex);
    }

    public RM allCoefficients() {
        return beta;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {
        if (targetNames().length == 0) {
            throw new IllegalArgumentException("OLS must specify at least one target variable name");
        }
        if (alpha < 0) {
            throw new IllegalArgumentException("Alpha- Regularization strength cannot be negative");
        }

        RM X = SolidRM.empty(df.rowCount() + inputNames.length, inputNames.length);
        RM Y = SolidRM.empty(df.rowCount() + inputNames.length, targetNames.length);

        double sqrt = Math.sqrt(this.alpha);
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

    @Override
    public LinearRFit fit(Frame df) {
        return (LinearRFit) super.fit(df);
    }

    @Override
    public LinearRFit fit(Frame df, boolean withResiduals) {
        return (LinearRFit) super.fit(df, withResiduals);
    }

    @Override
    protected LinearRFit coreFit(Frame df, boolean withResiduals) {
        LinearRFit rp = new LinearRFit(this, df, withResiduals);
        for (int i = 0; i < targetNames().length; i++) {
            String target = targetName(i);
            for (int j = 0; j < rp.fit(target).rowCount(); j++) {
                double fit = 0.0;
                for (int k = 0; k < inputNames().length; k++) {
                    fit += beta.get(k, i) * df.value(j, inputName(k));
                }
                rp.fit(target).setValue(j, fit);
            }
        }

        rp.buildComplete();
        return rp;
    }

    @Override
    public String summary() {
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
            RV coeff = beta.mapCol(i);

            TextTable tt = TextTable
                    .newEmpty(coeff.count() + 1, 2)
                    .withHeaderRows(1);
            tt.set(0, 0, "Name", 0);
            tt.set(0, 1, "Estimate", 0);
            for (int j = 0; j < coeff.count(); j++) {
                tt.set(j + 1, 0, inputNames[j], -1);
                tt.set(j + 1, 1, WS.formatMedium(coeff.get(j)), 1);
            }
            sb.append(tt.summary());
            sb.append("\n");
        }
        return sb.toString();
    }


}
