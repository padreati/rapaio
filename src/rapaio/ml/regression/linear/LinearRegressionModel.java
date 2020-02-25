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

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printer;
import rapaio.printer.format.TextTable;
import rapaio.printer.opt.POption;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegressionModel extends BaseLinearRegressionModel<LinearRegressionModel> {

    /**
     * Builds a linear regression model with intercept, no centering and no scaling.
     *
     * @return new instance of linear regression model
     */
    public static LinearRegressionModel newLm() {
        return new LinearRegressionModel()
                .withIntercept(true);
    }

    private static final long serialVersionUID = 8595413796946622895L;


    @Override
    public LinearRegressionModel newInstance() {
        return newInstanceDecoration(new LinearRegressionModel())
                .withIntercept(intercept);
    }

    @Override
    public String name() {
        return "LinearRegression";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("(intercept=").append(intercept).append(")");
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

    @Override
    public LinearRegressionModel withIntercept(boolean intercept) {
        return (LinearRegressionModel) super.withIntercept(intercept);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        DMatrix X = SolidDMatrix.copy(df.mapVars(inputNames()));
        DMatrix Y = SolidDMatrix.copy(df.mapVars(targetNames()));
        beta = QRDecomposition.from(X).solve(Y);
        return true;
    }

    @Override
    public LinearRegressionModel fit(Frame df, String... targetVarNames) {
        return (LinearRegressionModel) super.fit(df, targetVarNames);
    }

    @Override
    public LinearRegressionModel fit(Frame df, Var weights, String... targetVarNames) {
        return (LinearRegressionModel) super.fit(df, weights, targetVarNames);
    }

    @Override
    public String toSummary(Printer printer, POption... options) {
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
            sb.append(tt.getDynamicText(printer, options));
            sb.append("\n");
        }
        return sb.toString();
    }
}
