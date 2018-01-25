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

import rapaio.core.distributions.StudentT;
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.math.MTools;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.regression.RFit;
import rapaio.printer.Summary;
import rapaio.printer.format.TextTable;
import rapaio.sys.WS;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
public class LinearRFit extends RFit {

    private final RidgeRegression lm;
    private RM beta_hat;
    private RM beta_std_error;
    private RM beta_t_value;
    private RM beta_p_value;
    private String[][] beta_significance;

    
    public LinearRFit(RidgeRegression model, Frame df, boolean withResiduals) {
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
                NumVar res = residuals.get(targetName);

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
                    double pValue = new StudentT(degrees).cdf(-Math.abs(beta_t_value.get(j, i))) * 2;
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

                TextTable tt = TextTable
                        .newEmpty(coeff.count() + 1, 2)
                        .withHeaderRows(1);
                tt.set(0, 0, "Name", 0);
                tt.set(0, 1, "Estimate", 0);
                for (int j = 0; j < coeff.count(); j++) {
                    tt.set(j + 1, 0, lm.inputName(j), -1);
                    tt.set(j + 1, 1, WS.formatFlex(coeff.get(j)), -1);
                }
                sb.append(tt.summary());
            } else {
                NumVar res = residuals.get(targetName);

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

                TextTable tt = TextTable.newEmpty(coeff.count() + 1, 6).withHeaderRows(1).withHeaderCols(1);

                tt.set(0, 0, "Name", 1);
                tt.set(0, 1, "Estimate", 1);
                tt.set(0, 2, "Std. error", 1);
                tt.set(0, 3, "t value", 1);
                tt.set(0, 4, "P(>|t|)", 1);
                tt.set(0, 5, "", 1);
                for (int j = 0; j < coeff.count(); j++) {
                    tt.set(j + 1, 0, model.inputName(j), -1);
                    tt.set(j + 1, 1, WS.formatMedium(coeff.get(j)), 1);
                    tt.set(j + 1, 2, WS.formatMedium(beta_std_error.get(j, i)), 1);
                    tt.set(j + 1, 3, WS.formatMedium(beta_t_value.get(j, i)), 1);
                    tt.set(j + 1, 4, WS.formatPValue(beta_p_value.get(j, i)), 1);
                    tt.set(j + 1, 5, beta_significance[j][i], -1);
                }
                sb.append(tt.summary());
                sb.append("--------\n");
                sb.append("Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1\n\n");


                sb.append(String.format("Residual standard error: %s on %d degrees of freedom\n",
                        WS.formatFlex(Math.sqrt(var)),
                        degrees));
                sb.append(String.format("Multiple R-squared:  %s, Adjusted R-squared:  %s\n",
                        WS.formatFlex(rs), WS.formatFlex(rsa)));
                sb.append(String.format("F-statistic: %s on %d and %d DF,  p-value: %s\n",
                        WS.formatFlexShort(fvalue),
                        fdegree1,
                        degrees,
                        WS.formatPValue(fpvalue)));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
