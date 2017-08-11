/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.distributions.StudentT;
import rapaio.data.*;
import rapaio.data.filter.frame.FFAddIntercept;
import rapaio.datasets.Datasets;
import rapaio.math.linear.Linear;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.sys.WS;
import rapaio.printer.Summary;

import java.io.IOException;
import java.text.DecimalFormat;

import static org.junit.Assert.assertEquals;

/**
 * Test for linear regression.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
public class LinearRegressionTest {

    @Test
    public void testOneTarget() throws IOException {

        Frame df = Datasets.loadISLAdvertising()
                .removeVars("ID", "Sales", "Newspaper");

        LinearRegression lm = new LinearRegression();
        lm.addInputFilters(FFAddIntercept.filter());
        assertEquals(
                "Regression fit summary\n" +
                        "=======================\n" +
                        "\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression\n" +
                        "\n" +
                        "> model not trained.\n" +
                        "\n", lm.getSummary());

        lm.train(df, "Radio");
        assertEquals(
                "Regression fit summary\n" +
                        "=======================\n" +
                        "\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression\n" +
                        "\n" +
                        "> input variables: \n" +
                        " 1. (Intercept) num 2. TV num                     \n" +
                        "> target variables: \n" +
                        " 1. Radio num                                       \n" +
                        "\n" +
                        "Target <<< Radio >>>\n" +
                        "\n" +
                        "> Coefficients: \n" +
                        "     Name     Estimate\n" +
                        " (Intercept) 21.870319\n" +
                        " TV           0.009478\n" +
                        "\n", lm.getSummary());

        LinearRFit lmfit = lm.fit(df, true);
        assertEquals(
                "Regression fit summary\n" +
                        "=======================\n" +
                        "\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression\n" +
                        "\n" +
                        "> input variables: \n" +
                        " 1. (Intercept) num 2. TV num                     \n" +
                        "> target variables: \n" +
                        " 1. Radio num                                       \n" +
                        "\n" +
                        "Target <<< Radio >>>\n" +
                        "\n" +
                        "> Residuals: \n" +
                        "         Min          1Q     Median         3Q       Max\n" +
                        " -22.6304611 -13.2782023 -0.3958475 13.1830607 26.947222\n" +
                        "\n" +
                        "> Coefficients: \n" +
                        "        Name  Estimate Std. error   t value  P(>|t|)    \n" +
                        " (Intercept) 21.870319   2.088102 10.473778   <2e-16 ***\n" +
                        " TV           0.009478   0.012271  0.772387 0.440806    \n" +
                        "--------\n" +
                        "Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1\n" +
                        "\n" +
                        "Residual standard error: 14.861881 on 198 degrees of freedom\n" +
                        "Multiple R-squared:  0.003004, Adjusted R-squared:  -0.0020313\n" +
                        "F-statistic: 0.597 on 1 and 198 DF,  p-value: 0.440806\n" +
                        "\n", lmfit.getSummary());

        LinearRFit lmfit2 = lm.fit(df, false);
        assertEquals(
                "Regression fit summary\n" +
                        "=======================\n" +
                        "\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression\n" +
                        "\n" +
                        "> input variables: \n" +
                        " 1. (Intercept) num 2. TV num                     \n" +
                        "> target variables: \n" +
                        " 1. Radio num                                       \n" +
                        "\n" +
                        "Target <<< Radio >>>\n" +
                        "\n" +
                        "> Coefficients: \n" +
                        "     Name     Estimate \n" +
                        " (Intercept) 21.8703186\n" +
                        " TV          0.0094781 \n",
                lmfit2.getSummary());

    }

    @Test
    public void testMultipleTargets() throws IOException {
            Frame df = Datasets.loadISLAdvertising().removeVars("ID");

            LinearRegression lm = new LinearRegression();
            lm.addInputFilters(FFAddIntercept.filter());

            lm.train(df, "Sales", "Radio");

            assertEquals("Regression fit summary\n" +
                    "=======================\n" +
                    "\n" +
                    "Model class: LinearRegression\n" +
                    "Model instance: LinearRegression\n" +
                    "\n" +
                    "> input variables: \n" +
                    " 1. (Intercept) num 2. TV num 3. Newspaper num                  \n" +
                    "> target variables: \n" +
                    " 1. Sales num 2. Radio num                                    \n" +
                    "\n" +
                    "Target <<< Sales >>>\n" +
                    "\n" +
                    "> Residuals: \n" +
                    "        Min         1Q     Median        3Q       Max\n" +
                    " -8.6230898 -1.7346082 -0.0948177 1.8925968 8.4512125\n" +
                    "\n" +
                    "> Coefficients: \n" +
                    "        Name Estimate Std. error   t value  P(>|t|)    \n" +
                    " (Intercept) 5.774948   0.525338 10.992828   <2e-16 ***\n" +
                    " TV          0.046901   0.002581 18.172707   <2e-16 ***\n" +
                    " Newspaper   0.044219   0.010174  4.346276 0.000022 ***\n" +
                    "--------\n" +
                    "Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1\n" +
                    "\n" +
                    "Residual standard error: 3.1207199 on 197 degrees of freedom\n" +
                    "Multiple R-squared:  0.6458355, Adjusted R-squared:  0.6422399\n" +
                    "F-statistic: 179.619 on 2 and 197 DF,  p-value: <2e-16\n" +
                    "\n" +
                    "Target <<< Radio >>>\n" +
                    "\n" +
                    "> Residuals: \n" +
                    "         Min          1Q     Median         3Q        Max\n" +
                    " -33.4130794 -11.7301826 -0.5438912 10.5752129 31.8607305\n" +
                    "\n" +
                    "> Coefficients: \n" +
                    "        Name  Estimate Std. error  t value    P(>|t|)    \n" +
                    " (Intercept) 15.043008   2.347560 6.407932   1.06e-09 ***\n" +
                    " TV           0.006029   0.011533 0.522724   0.601754    \n" +
                    " Newspaper    0.240052   0.045465 5.279958   3.40e-07 ***\n" +
                    "--------\n" +
                    "Signif. codes:  0 ‘***’ 0.001 ‘**’ 0.01 ‘*’ 0.05 ‘.’ 0.1 ‘ ’ 1\n" +
                    "\n" +
                    "Residual standard error: 13.9454627 on 197 degrees of freedom\n" +
                    "Multiple R-squared:  0.1266009, Adjusted R-squared:  0.1177339\n" +
                    "F-statistic: 14.278 on 2 and 197 DF,  p-value: 0.000002\n" +
                    "\n", lm.fit(df, true).getSummary());
        }
}
