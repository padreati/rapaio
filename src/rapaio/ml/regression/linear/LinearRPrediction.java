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

import rapaio.core.distributions.*;
import rapaio.data.*;
import rapaio.math.*;
import rapaio.math.linear.*;
import rapaio.math.linear.dense.*;
import rapaio.ml.regression.*;
import rapaio.printer.*;
import rapaio.printer.format.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public class LinearRPrediction extends RPrediction {

    protected final AbstractLinearRegression lm;
    protected RM beta_hat;
    protected RM beta_std_error;
    protected RM beta_t_value;
    protected RM beta_p_value;
    protected String[][] beta_significance;

    protected LinearRPrediction(AbstractLinearRegression model, Frame df, boolean withResiduals) {
        super(model, df, withResiduals);
        this.lm = model;
    }

    public RM getBetaHat() {
        return beta_hat;
    }

    public RM getBetaStdError() {
        return beta_std_error;
    }

    public RM getBetaTValue() {
        return beta_t_value;
    }

    public RM getBetaPValue() {
        return beta_p_value;
    }

    public String[][] getBetaSignificance() {
        return beta_significance;
    }

    @Override
    public void buildComplete() {
        super.buildComplete();

        // compute artifacts

        String[] inputs = lm.inputNames();
        String[] targets = lm.targetNames();

        beta_hat = lm.allCoefficients().solidCopy();
        beta_std_error = SolidRM.empty(inputs.length, targets.length);
        beta_t_value = SolidRM.empty(inputs.length, targets.length);
        beta_p_value = SolidRM.empty(inputs.length, targets.length);
        beta_significance = new String[inputs.length][targets.length];

        for (int i = 0; i < lm.targetNames().length; i++) {
            String targetName = lm.targetName(i);

            if (!withResiduals) {
                RV coeff = beta_hat.mapCol(i);
            } else {
                VarDouble res = residuals.get(targetName);

                int degrees = res.rowCount() - model.inputNames().length;
                double var = rss.get(targetName) / degrees;
                double rs = rsquare.get(targetName);
                RV coeff = beta_hat.mapCol(i);
                double rsa = (rs * (res.rowCount() - 1) - coeff.count() + 1) / degrees;

                int fdegree1 = model.inputNames().length - 1;
                double fvalue = (ess.get(targetName) * degrees) / (rss.get(targetName) * (fdegree1));
                double fpvalue = MTools.fdist(fvalue, fdegree1, degrees);

                RM X = SolidRM.copy(df.mapVars(model.inputNames()));
                RM m_beta_hat = QRDecomposition.from(X.t().dot(X)).solve(SolidRM.identity(X.colCount()));

                for (int j = 0; j < model.inputNames().length; j++) {
                    beta_std_error.set(j, i, Math.sqrt(m_beta_hat.get(j, j) * var));
                    beta_t_value.set(j, i, coeff.get(j) / beta_std_error.get(j, i));
                    double pValue = degrees < 1 ? Double.NaN : StudentT.of(degrees).cdf(-Math.abs(beta_t_value.get(j, i))) * 2;
                    beta_p_value.set(j, i, pValue);
                    String signif = " ";
                    if (pValue <= 0.1)
                        signif = ".";
                    if (pValue <= 0.05)
                        signif = "*";
                    if (pValue <= 0.01)
                        signif = "**";
                    if (pValue <= 0.001)
                        signif = "***";
                    beta_significance[j][i] = signif;
                }
            }
        }
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(lm.headerSummary());
        sb.append("\n");

        for (int i = 0; i < lm.targetNames().length; i++) {
            String targetName = lm.targetName(i);
            sb.append("Target <<< ").append(targetName).append(" >>>\n\n");

            if (!withResiduals) {
                sb.append("> Coefficients: \n");
                RV coeff = lm.coefficients(i);

                TextTable tt = TextTable.empty(coeff.count() + 1, 2, 1, 0);
                tt.textCenter(0, 0, "Name");
                tt.textCenter(0, 1, "Estimate");
                for (int j = 0; j < coeff.count(); j++) {
                    tt.textLeft(j + 1, 0, lm.inputName(j));
                    tt.floatFlex(j + 1, 1, coeff.get(j));
                }
                sb.append(tt.getDefaultText());
            } else {
                VarDouble res = residuals.get(targetName);

                int degrees = res.rowCount() - model.inputNames().length;
                double var = rss.get(targetName) / degrees;
                double rs = rsquare.get(targetName);
                RV coeff = lm.coefficients(i);
                double rsa = (rs * (res.rowCount() - 1) - coeff.count() + 1) / degrees;

                int fdegree1 = model.inputNames().length - 1;
                double fvalue = (ess.get(targetName) * degrees) / (rss.get(targetName) * (fdegree1));
                double fpvalue = MTools.fdist(fvalue, fdegree1, degrees);

                sb.append("> Residuals: \n");
                sb.append(Summary.getHorizontalSummary5(res));
                sb.append("\n");

                sb.append("> Coefficients: \n");

                TextTable tt = TextTable.empty(coeff.count() + 1, 6, 1, 1);

                tt.textRight(0, 0, "Name");
                tt.textRight(0, 1, "Estimate");
                tt.textRight(0, 2, "Std. error");
                tt.textRight(0, 3, "t value");
                tt.textRight(0, 4, "P(>|t|)");
                tt.textRight(0, 5, "");
                for (int j = 0; j < coeff.count(); j++) {
                    tt.textLeft(j + 1, 0, model.inputName(j));
                    tt.floatMedium(j + 1, 1, coeff.get(j));
                    tt.floatMedium(j + 1, 2, beta_std_error.get(j, i));
                    tt.floatMedium(j + 1, 3, beta_t_value.get(j, i));
                    tt.pValue(j + 1, 4, beta_p_value.get(j, i));
                    tt.textLeft(j + 1, 5, beta_significance[j][i]);
                }
                sb.append(tt.getDefaultText());
                sb.append("--------\n");
                sb.append("Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1\n\n");


                sb.append(String.format("Residual standard error: %s on %d degrees of freedom\n",
                        Format.floatFlex(Math.sqrt(var)),
                        degrees));
                sb.append(String.format("Multiple R-squared:  %s, Adjusted R-squared:  %s\n",
                        Format.floatFlex(rs), Format.floatFlex(rsa)));
                sb.append(String.format("F-statistic: %s on %d and %d DF,  p-value: %s\n",
                        Format.floatFlexShort(fvalue),
                        fdegree1,
                        degrees,
                        Format.pValue(fpvalue)));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
