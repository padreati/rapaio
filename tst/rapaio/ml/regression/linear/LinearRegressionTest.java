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
    public void testHappy() throws IOException {

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
}
