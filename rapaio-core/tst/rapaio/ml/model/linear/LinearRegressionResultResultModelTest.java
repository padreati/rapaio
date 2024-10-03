/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.linear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;

/**
 * Test for linear regression.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
public class LinearRegressionResultResultModelTest {

    private static final double TOL = 1e-20;

    @Test
    void testOneTarget() {

        Frame df = Datasets.loadISLAdvertising()
                .removeVars(VarRange.of("ID", "Sales", "Newspaper"));

        LinearRegressionModel lm = LinearRegressionModel.newModel().intercept.set(true);
        assertEquals("""
                Regression predict summary
                =======================
                Model class: LinearRegression
                Model instance: LinearRegression{}
                > model not trained.

                """, lm.toSummary());
        assertEquals("LinearRegression{}, not fitted.",
                lm.toString());
        lm.fit(df, "Radio");
        assertEquals("LinearRegression{}, fitted on: 2 IVs [(Intercept),TV], 1 DVs [Radio].",
                lm.toString());
        assertEquals(
                """
                        Regression predict summary
                        =======================
                        Model class: LinearRegression
                        Model instance: LinearRegression{}
                        > model is trained.
                        > input variables:\s
                        1. (Intercept) dbl\s
                        2. TV          dbl\s
                        > target variables:\s
                        1. Radio dbl\s

                        Target <<< Radio >>>

                        > Coefficients:\s
                           Name      Estimate \s
                        (Intercept) 21.8703186\s
                        TV           0.0094781\s

                        """, lm.toSummary());

        var lmfit = lm.predict(df, true);
        assertEquals(
                """
                        Regression predict summary
                        =======================
                        Model class: LinearRegression
                        Model instance: LinearRegression{}
                        > model is trained.
                        > input variables:\s
                        1. (Intercept) dbl\s
                        2. TV          dbl\s
                        > target variables:\s
                        1. Radio dbl\s

                        Target <<< Radio >>>

                        > Residuals:\s
                                Min          1Q     Median         3Q       Max\s
                        -22.6304611 -13.2782023 -0.3958475 13.1830607 26.947222\s

                        > Coefficients:\s
                               Name   Estimate Std. error    t value   P(>|t|)    \s
                        (Intercept) 21.8703186 2.0881021  10.4737782    <2e-16 ***\s
                        TV           0.0094781 0.0122712   0.7723873 0.4408061    \s
                        --------
                        Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1

                        Residual standard error: 14.861881 on 198 degrees of freedom
                        Multiple R-squared:  0.003004, Adjusted R-squared:  -0.0020313
                        F-statistic: 0.597 on 1 and 198 DF,  p-value: 0.4408061

                        """, lmfit.toSummary());


        assertEquals(2, lmfit.getBetaHat().dim(0));
        assertEquals(1, lmfit.getBetaHat().dim(1));

        assertEquals(2, lmfit.getBetaStdError().dim(0));
        assertEquals(1, lmfit.getBetaStdError().dim(1));

        assertEquals(2, lmfit.getBetaTValue().dim(0));
        assertEquals(1, lmfit.getBetaTValue().dim(1));

        assertEquals(2, lmfit.getBetaPValue().dim(0));
        assertEquals(1, lmfit.getBetaPValue().dim(1));

        assertEquals(2, lmfit.getBetaSignificance().length);

        var lmfit2 = lm.predict(df, false);
        assertEquals(
                """
                        Regression predict summary
                        =======================
                        Model class: LinearRegression
                        Model instance: LinearRegression{}
                        > model is trained.
                        > input variables:\s
                        1. (Intercept) dbl\s
                        2. TV          dbl\s
                        > target variables:\s
                        1. Radio dbl\s

                        Target <<< Radio >>>

                        > Coefficients:\s
                           Name      Estimate \s
                        (Intercept) 21.8703186\s
                        TV           0.0094781\s
                        """,
                lmfit2.toSummary());

    }

    @Test
    void testMultipleTargets() throws IOException {
        Frame df = Datasets.loadISLAdvertising().removeVars(VarRange.of("ID"));

        LinearRegressionModel lm = LinearRegressionModel.newModel().intercept.set(true);

        lm.fit(df, "Sales", "Radio");

        assertEquals("""
                Regression predict summary
                =======================
                Model class: LinearRegression
                Model instance: LinearRegression{}
                > model is trained.
                > input variables:\s
                1. (Intercept) dbl\s
                2. TV          dbl\s
                3. Newspaper   dbl\s
                > target variables:\s
                1. Sales dbl\s
                2. Radio dbl\s

                Target <<< Sales >>>

                > Residuals:\s
                       Min         1Q     Median        3Q       Max\s
                -8.6230898 -1.7346082 -0.0948177 1.8925968 8.4512125\s

                > Coefficients:\s
                       Name  Estimate Std. error    t value   P(>|t|)    \s
                (Intercept) 5.7749480 0.5253378  10.9928280    <2e-16 ***\s
                TV          0.0469012 0.0025809  18.1727070    <2e-16 ***\s
                Newspaper   0.0442194 0.0101741   4.3462757 0.0000222 ***\s
                --------
                Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1

                Residual standard error: 3.1207199 on 197 degrees of freedom
                Multiple R-squared:  0.6458355, Adjusted R-squared:  0.6422399
                F-statistic: 179.619 on 2 and 197 DF,  p-value: <2e-16

                Target <<< Radio >>>

                > Residuals:\s
                        Min          1Q     Median         3Q        Max\s
                -33.4130794 -11.7301826 -0.5438912 10.5752129 31.8607305\s

                > Coefficients:\s
                       Name   Estimate Std. error   t value     P(>|t|)    \s
                (Intercept) 15.0430082 2.3475604  6.4079322   1.06e-09  ***\s
                TV           0.0060286 0.0115330  0.5227241   0.6017537    \s
                Newspaper    0.2400515 0.0454647  5.2799584   3.40e-07  ***\s
                --------
                Signif. codes:  0 '***' 0.001 '**' 0.01 '*' 0.05 '.' 0.1 ' ' 1

                Residual standard error: 13.9454627 on 197 degrees of freedom
                Multiple R-squared:  0.1266009, Adjusted R-squared:  0.1177339
                F-statistic: 14.278 on 2 and 197 DF,  p-value: 0.0000016

                """, lm.predict(df, true).toSummary());

        assertEquals(lm.toContent(), lm.toString());
        assertEquals(lm.toFullContent(), lm.toString());
    }

    @Test
    void testIntercept() {
        Random random = new Random(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = VarDouble.seq(0, 100, 1).name("x");
        VarDouble intercept = VarDouble.fill(x.size(), 1.0).name("I");
        VarDouble y = VarDouble.from(x, v -> v * 2 + normal.sampleNext(random)).name("y");

        Frame df1 = BoundFrame.byVars(x, y);
        Frame df2 = BoundFrame.byVars(intercept, x, y);

        LinearRegressionModel lm1 = LinearRegressionModel.newModel().intercept.set(true).fit(df1, "y");
        LinearRegressionModel lm2 = LinearRegressionModel.newModel().intercept.set(false).fit(df2, "y");

        var pred1 = lm1.predict(df1, true);
        var pred2 = lm2.predict(df2, true);

        assertTrue(pred1.firstResidual().deepEquals(pred2.firstResidual()));
    }

    @Test
    void testNewInstance() {
        LinearRegressionModel lm1 = LinearRegressionModel.newModel().intercept.set(false);
        LinearRegressionModel lm2 = lm1.newInstance();

        assertEquals(lm1.intercept.get(), lm2.intercept.get());
    }

    @Test
    void testCoefficients() {
        Random random = new Random(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = VarDouble.seq(0, 100, 1).name("x");
        VarDouble intercept = VarDouble.fill(x.size(), 1.0).name("I");
        VarDouble y1 = VarDouble.from(x, v -> v * 2 + normal.sampleNext(random)).name("y1");
        VarDouble y2 = VarDouble.from(x, v -> v * 3 - 10 + normal.sampleNext(random)).name("y2");

        Frame df = BoundFrame.byVars(x, y1, y2);

        LinearRegressionModel lm = LinearRegressionModel.newModel().intercept.set(true).fit(df, "y1,y2");

        var betas = lm.getAllCoefficients();
        var firstBetas = lm.firstCoefficients();
        var secondBetas = lm.getCoefficients(1);

        for (int i = 0; i < 2; i++) {
            assertEquals(betas.get(i, 0), firstBetas.get(i), TOL);
            assertEquals(betas.get(i, 1), secondBetas.get(i), TOL);
        }
    }
}
