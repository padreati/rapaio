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

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for linear regression.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
public class LinearRegressionModelTest {

    private static final double TOL = 1e-20;

    @Test
    void testOneTarget() throws IOException {

        Frame df = Datasets.loadISLAdvertising()
                .removeVars(VRange.of("ID", "Sales", "Newspaper"));

        LinearRegressionModel lm = new LinearRegressionModel().withIntercept(true);
        assertEquals(
                "Regression predict summary\n" +
                        "=======================\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression(intercept=true)\n" +
                        "> model not trained.\n" +
                        "\n", lm.toSummary());
        assertEquals("LinearRegression(intercept=true), not fitted.",
                lm.toString());
        lm.fit(df, "Radio");
        assertEquals("LinearRegression(intercept=true), fitted on: 2 IVs [(Intercept),TV], 1 DVs [Radio].",
                lm.toString());
        assertEquals(
                "Regression predict summary\n" +
                        "=======================\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression(intercept=true)\n" +
                        "> model is trained.\n" +
                        "> input variables: \n" +
                        "1. (Intercept) dbl \n" +
                        "2. TV          dbl \n" +
                        "> target variables: \n" +
                        "1. Radio dbl \n" +
                        "\n" +
                        "Target <<< Radio >>>\n" +
                        "\n" +
                        "> Coefficients: \n" +
                        "   Name     Estimate  \n" +
                        "(Intercept) 21.870319 \n" +
                        "TV           0.009478 \n" +
                        "\n", lm.toSummary());

        var lmfit = lm.predict(df, true);
        assertEquals(
                "Regression predict summary\n" +
                        "=======================\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression(intercept=true)\n" +
                        "> model is trained.\n" +
                        "> input variables: \n" +
                        "1. (Intercept) dbl \n" +
                        "2. TV          dbl \n" +
                        "> target variables: \n" +
                        "1. Radio dbl \n" +
                        "\n" +
                        "Target <<< Radio >>>\n" +
                        "\n" +
                        "> Residuals: \n" +
                        "        Min          1Q     Median         3Q       Max \n" +
                        "-22.6304611 -13.2782023 -0.3958475 13.1830607 26.947222 \n" +
                        "\n" +
                        "> Coefficients: \n" +
                        "       Name  Estimate Std. error   t value  P(>|t|)     \n" +
                        "(Intercept) 21.870319  2.088102  10.473778   <2e-16 *** \n" +
                        "TV           0.009478  0.012271   0.772387 0.440806     \n" +
                        "--------\n" +
                        "Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1\n" +
                        "\n" +
                        "Residual standard error: 14.861881 on 198 degrees of freedom\n" +
                        "Multiple R-squared:  0.003004, Adjusted R-squared:  -0.0020313\n" +
                        "F-statistic: 0.597 on 1 and 198 DF,  p-value: 0.440806\n" +
                        "\n", lmfit.toSummary());


        assertEquals(2, lmfit.getBetaHat().rowCount());
        assertEquals(1, lmfit.getBetaHat().colCount());

        assertEquals(2, lmfit.getBetaStdError().rowCount());
        assertEquals(1, lmfit.getBetaStdError().colCount());

        assertEquals(2, lmfit.getBetaTValue().rowCount());
        assertEquals(1, lmfit.getBetaTValue().colCount());

        assertEquals(2, lmfit.getBetaPValue().rowCount());
        assertEquals(1, lmfit.getBetaPValue().colCount());

        assertEquals(2, lmfit.getBetaSignificance().length);

        var lmfit2 = lm.predict(df, false);
        assertEquals(
                "Regression predict summary\n" +
                        "=======================\n" +
                        "Model class: LinearRegression\n" +
                        "Model instance: LinearRegression(intercept=true)\n" +
                        "> model is trained.\n" +
                        "> input variables: \n" +
                        "1. (Intercept) dbl \n" +
                        "2. TV          dbl \n" +
                        "> target variables: \n" +
                        "1. Radio dbl \n" +
                        "\n" +
                        "Target <<< Radio >>>\n" +
                        "\n" +
                        "> Coefficients: \n" +
                        "   Name      Estimate  \n" +
                        "(Intercept) 21.8703186 \n" +
                        "TV           0.0094781 \n",
                lmfit2.toSummary());

    }

    @Test
    void testMultipleTargets() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VRange.of("ID"));

        LinearRegressionModel lm = new LinearRegressionModel().withIntercept(true);

        lm.fit(df, "Sales", "Radio");

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: LinearRegression\n" +
                "Model instance: LinearRegression(intercept=true)\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. (Intercept) dbl \n" +
                "2. TV          dbl \n" +
                "3. Newspaper   dbl \n" +
                "> target variables: \n" +
                "1. Sales dbl \n" +
                "2. Radio dbl \n" +
                "\n" +
                "Target <<< Sales >>>\n" +
                "\n" +
                "> Residuals: \n" +
                "       Min         1Q     Median        3Q       Max \n" +
                "-8.6230898 -1.7346082 -0.0948177 1.8925968 8.4512125 \n" +
                "\n" +
                "> Coefficients: \n" +
                "       Name Estimate Std. error   t value  P(>|t|)     \n" +
                "(Intercept) 5.774948  0.525338  10.992828   <2e-16 *** \n" +
                "TV          0.046901  0.002581  18.172707   <2e-16 *** \n" +
                "Newspaper   0.044219  0.010174   4.346276 0.000022 *** \n" +
                "--------\n" +
                "Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1\n" +
                "\n" +
                "Residual standard error: 3.1207199 on 197 degrees of freedom\n" +
                "Multiple R-squared:  0.6458355, Adjusted R-squared:  0.6422399\n" +
                "F-statistic: 179.619 on 2 and 197 DF,  p-value: <2e-16\n" +
                "\n" +
                "Target <<< Radio >>>\n" +
                "\n" +
                "> Residuals: \n" +
                "        Min          1Q     Median         3Q        Max \n" +
                "-33.4130794 -11.7301826 -0.5438912 10.5752129 31.8607305 \n" +
                "\n" +
                "> Coefficients: \n" +
                "       Name  Estimate Std. error  t value    P(>|t|)     \n" +
                "(Intercept) 15.043008  2.347560  6.407932   1.06e-09 *** \n" +
                "TV           0.006029  0.011533  0.522724   0.601754     \n" +
                "Newspaper    0.240052  0.045465  5.279958   3.40e-07 *** \n" +
                "--------\n" +
                "Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1\n" +
                "\n" +
                "Residual standard error: 13.9454627 on 197 degrees of freedom\n" +
                "Multiple R-squared:  0.1266009, Adjusted R-squared:  0.1177339\n" +
                "F-statistic: 14.278 on 2 and 197 DF,  p-value: 0.000002\n" +
                "\n", lm.predict(df, true).toSummary());

        assertEquals(lm.toContent(), lm.toSummary());
        assertEquals(lm.toFullContent(), lm.toSummary());
    }

    @Test
    void testIntercept() {
        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = VarDouble.seq(0, 100, 1).withName("x");
        VarDouble intercept = VarDouble.fill(x.rowCount(), 1.0).withName("I");
        VarDouble y = VarDouble.from(x, v -> v * 2 + normal.sampleNext()).withName("y");

        Frame df1 = BoundFrame.byVars(x, y);
        Frame df2 = BoundFrame.byVars(intercept, x, y);

        LinearRegressionModel lm1 = LinearRegressionModel.newLm().withIntercept(true).fit(df1, "y");
        LinearRegressionModel lm2 = LinearRegressionModel.newLm().withIntercept(false).fit(df2, "y");

        var pred1 = lm1.predict(df1, true);
        var pred2 = lm2.predict(df2, true);

        lm1.printContent();
        lm2.printContent();

        assertTrue(pred1.firstResidual().deepEquals(pred2.firstResidual()));
    }

    @Test
    void testNewInstance() {
        LinearRegressionModel lm1 = LinearRegressionModel.newLm().withIntercept(false);
        LinearRegressionModel lm2 = lm1.newInstance();

        assertEquals(lm1.hasIntercept(), lm2.hasIntercept());
    }

    @Test
    void testCoefficients() {
        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = VarDouble.seq(0, 100, 1).withName("x");
        VarDouble intercept = VarDouble.fill(x.rowCount(), 1.0).withName("I");
        VarDouble y1 = VarDouble.from(x, v -> v * 2 + normal.sampleNext()).withName("y1");
        VarDouble y2 = VarDouble.from(x, v -> v * 3 - 10 + normal.sampleNext()).withName("y2");

        Frame df = BoundFrame.byVars(x, y1, y2);

        LinearRegressionModel lm = LinearRegressionModel.newLm().withIntercept(true).fit(df, "y1,y2");
        var pred = lm.predict(df, true);

        RM betas = lm.allCoefficients();
        RV firstBetas = lm.firstCoefficients();
        RV secondBetas = lm.getCoefficients(1);

        for (int i = 0; i < 2; i++) {
            assertEquals(betas.get(i, 0), firstBetas.get(i), TOL);
            assertEquals(betas.get(i, 1), secondBetas.get(i), TOL);
        }
    }
}
