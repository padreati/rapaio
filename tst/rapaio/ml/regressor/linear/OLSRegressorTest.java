/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 */

package rapaio.ml.regressor.linear;

import org.junit.Test;
import rapaio.WS;
import rapaio.core.distributions.StudentT;
import rapaio.data.*;
import rapaio.data.filter.frame.FFAddIntercept;
import rapaio.datasets.Datasets;
import rapaio.math.linear.*;
import rapaio.ws.Summary;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Test for ols regression.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
public class OLSRegressorTest {

    @Test
    public void testHappy() throws IOException {

        Frame df = Datasets.loadISLAdvertising().mapVars("TV", "Radio", "Newspaper", "Sales");
        Summary.summary(df);

        OLSRegressor ols = new OLSRegressor();
        ols.learn(df, "Sales");
        OLSRegressorFit rr = ols.predict(df, true);

        rr.summary();
    }

    @Test
    public void testWork() throws IOException {
        Frame df = new FFAddIntercept().fitApply(Datasets.loadISLAdvertising().mapVars("TV", "Radio", "Newspaper", "Sales"));
//        Frame df = Datasets.loadISLAdvertising().mapVars("TV", "Radio", "Newspaper", "Sales");
        String[] targetNames = new String[]{"Sales"};
        String[] inputNames = new String[]{"(Intercept)", "TV", "Radio", "Newspaper"};
//        String[] inputNames = new String[]{"TV", "Radio", "Newspaper"};

        RMatrix X = LinAlg.newMatrixCopyOf(df.mapVars(inputNames));
        RMatrix Y = LinAlg.newMatrixCopyOf(df.mapVars(targetNames));

        QRDecomposition qr1 = new QRDecomposition(X);
        RMatrix beta = qr1.solve(Y);

        Var betaTerm = Nominal.newEmpty().withName("Term");
        Var betaEstimate = Numeric.newEmpty().withName("Estimate");
        Var betaStdError = Numeric.newEmpty().withName("Std. Error");
        Var betaTValue = Numeric.newEmpty().withName("t value");
        Var betaPValue = Nominal.newEmpty().withName("Pr(>|t|)");
        Var betaSignificance = Nominal.newEmpty().withName("");

        RMatrix c = CholeskyDecomposition.chol2inv(qr1.getR());

        double sigma2 = 0;
        for (int i = 0; i < X.rowCount(); i++) {
            sigma2 += Math.pow(Y.get(i, 0) - X.mapRow(i).dotProd(beta.mapCol(0)), 2);
        }
        sigma2 /= (X.rowCount() - X.colCount());

        WS.println("sigma: " + Math.sqrt(sigma2));

        RVector var = c.mult(sigma2).diag();

        for (int i = 0; i < inputNames.length; i++) {
            betaTerm.addLabel(inputNames[i]);
            betaEstimate.addValue(beta.get(i, 0));
            betaStdError.addValue(Math.sqrt(var.get(i)));
            betaTValue.addValue(betaEstimate.value(i) / betaStdError.value(i));

            StudentT t = new StudentT(X.rowCount() - X.colCount(), 0, betaStdError.value(i));
            double pValue = 1 - Math.abs(t.cdf(betaEstimate.value(i)) - t.cdf(-betaEstimate.value(i)));
            betaPValue.addLabel(pValue < 2e-16 ? "<2e-16" : new DecimalFormat("0.00").format(pValue));
            String signif = " ";
            if (pValue <= 0.1)
                signif = ".";
            if (pValue <= 0.05)
                signif = "*";
            if (pValue <= 0.01)
                signif = "**";
            if (pValue <= 0.001)
                signif = "***";
            betaSignificance.addLabel(signif);
        }
        Frame coefficients = SolidFrame.newWrapOf(inputNames.length,
                betaTerm, betaEstimate, betaStdError, betaTValue, betaPValue, betaSignificance);

        Summary.lines(coefficients);
        WS.println("---");
        WS.println("Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1");
    }
}
