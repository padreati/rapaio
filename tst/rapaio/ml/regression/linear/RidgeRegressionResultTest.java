/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DVector;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public class RidgeRegressionResultTest {

    private static final double TOL = 1e-12;

    private Frame df;

    @BeforeEach
    void setUp() throws IOException {
        df = Datasets.loadISLAdvertising().removeVars("ID");
    }

    @Test
    void testNoRegularization() {
        // test the results for ridge are the same as those for linear regression when lambda equals 0

        // when we have intercept, in ridge we have to center, scaling does not matter

        LinearRegressionModel lm1 = LinearRegressionModel.newModel().intercept.set(true).fit(df, "Sales");
        RidgeRegressionModel rlm1 = RidgeRegressionModel.newModel(0, Centering.MEAN, Scaling.SD).intercept.set(true)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm1.firstCoefficients().valueStream().toArray(),
                rlm1.firstCoefficients().valueStream().toArray(),
                TOL);

        LinearRegressionModel lm2 = LinearRegressionModel.newModel().intercept.set(true).fit(df, "Sales");
        RidgeRegressionModel rlm2 = RidgeRegressionModel.newModel(0, Centering.MEAN, Scaling.NONE).intercept.set(true)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm2.firstCoefficients().valueStream().toArray(),
                rlm2.firstCoefficients().valueStream().toArray(),
                TOL);

        // when we do not have intercept, then we do not center

        LinearRegressionModel lm3 = LinearRegressionModel.newModel().intercept.set(false).fit(df, "Sales");
        RidgeRegressionModel rlm3 = RidgeRegressionModel.newModel(0, Centering.NONE, Scaling.NONE).intercept.set(false)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm1.firstCoefficients().valueStream().toArray(),
                rlm1.firstCoefficients().valueStream().toArray(),
                TOL);
    }

    @Test
    void ridgeCoefficientsTestedWithR() {
        double[] lambdas = new double[]{0, 0.1, 0.5, 1, 5, 10, 100, 1_000_000};
        double[][] coeff = new double[][]{
                {2.938889369, 0.045764645, 0.188530017, -0.001037493},
                {2.943640063, 0.045742314, 0.188427128, -0.001007165},
                {2.9626005301, 0.0456532066, 0.1880168738, -0.0008865193},
                {2.9862063684, 0.0455423152, 0.1875069634, -0.0007371949},
                {3.1713479188, 0.0446744159, 0.1835401090, 0.0004005099},
                {3.393903399, 0.043635454, 0.178845155, 0.001691322},
                {6.23878001, 0.03079152, 0.12438395, 0.01186062},
                {14.01983, 9.505319e-06, 4.048932e-05, 1.093405e-05}
        };

        for (int i = 0; i < lambdas.length; i++) {
            RidgeRegressionModel rr = RidgeRegressionModel.newModel(lambdas[i]);
            double[] beta_hat = rr.fit(df, "Sales").firstCoefficients().valueStream().toArray();
            assertArrayEquals(coeff[i], beta_hat, 1e9);
        }
    }

    @Test
    void testProperties() {
        RidgeRegressionModel rlm = RidgeRegressionModel.newModel(0);

        assertTrue(rlm.intercept.get());
        assertEquals(rlm.centering.get(), Centering.MEAN);
        assertEquals(rlm.scaling.get(), Scaling.SD);
        assertEquals(0, rlm.lambda.get(), TOL);

        rlm = RidgeRegressionModel.newModel(1, Centering.NONE, Scaling.NONE).intercept.set(false);

        assertFalse(rlm.intercept.get());
        assertEquals(rlm.centering.get(), Centering.NONE);
        assertEquals(rlm.scaling.get(), Scaling.NONE);
        assertEquals(1, rlm.lambda.get(), TOL);
    }

    @Test
    void testCoefficients() {
        RidgeRegressionModel rlm = RidgeRegressionModel.newModel(10).fit(df, "Sales");

        assertArrayEquals(rlm.firstCoefficients().valueStream().toArray(), rlm.getCoefficients(0).valueStream().toArray(), TOL);
        assertEquals(1, rlm.getAllCoefficients().colCount());
        assertEquals(4, rlm.getAllCoefficients().rowCount());
        assertArrayEquals(rlm.firstCoefficients().valueStream().toArray(), rlm.getAllCoefficients().mapCol(0).valueStream().toArray(), TOL);
    }

    @Test
    void testPredictionWithIntercept() {
        RidgeRegressionModel model = RidgeRegressionModel.newModel(10).fit(df, "Sales");
        var result = model.predict(df);
        DVector beta_hat = result.getBetaHat().mapCol(0);
        assertEquals(4, beta_hat.size());

        assertEquals(model.firstCoefficients().get(0), beta_hat.get(0), TOL);

        for (int i = 0; i < df.rowCount(); i++) {
            double obsPred = beta_hat.get(0);
            for (int j = 1; j < 4; j++) {
                obsPred += beta_hat.get(j) * df.getDouble(i, j - 1);
            }
            assertEquals(result.firstPrediction().getDouble(i), obsPred, TOL);
        }
    }

    @Test
    void testPredictionWithOutIntercept() {
        RidgeRegressionModel model = RidgeRegressionModel.newModel(10, Centering.MEAN, Scaling.SD).intercept.set(false).fit(df, "Sales");
        var result = model.predict(df);
        model.printSummary();
        DVector beta_hat = result.getBetaHat().mapCol(0);
        assertEquals(3, beta_hat.size());

        assertEquals(model.firstCoefficients().get(0), beta_hat.get(0), TOL);

        for (int i = 0; i < df.rowCount(); i++) {
            double obsPred = 0;
            for (int j = 0; j < 3; j++) {
                obsPred += beta_hat.get(j) * df.getDouble(i, j);
            }
            assertEquals(result.firstPrediction().getDouble(i), obsPred, TOL);
        }
    }

    @Test
    void testNames() {
        RidgeRegressionModel model1 = RidgeRegressionModel.newModel(1.2);
        RidgeRegressionModel model2 = RidgeRegressionModel.newModel(Math.PI, Centering.NONE, Scaling.NONE)
                .intercept.set(false);

        assertEquals("RidgeRegression", model1.name());
        assertEquals("RidgeRegression", model2.name());

        assertEquals("RidgeRegression{lambda=1.2}", model1.fullName());
        assertEquals("RidgeRegression{centering=NONE,intercept=false,lambda=3.1415927,scaling=NONE}", model2.fullName());

        assertEquals("RidgeRegression{lambda=1.2}, not fitted.", model1.toContent());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RidgeRegression\n" +
                "Model instance: RidgeRegression{centering=NONE,intercept=false,lambda=3.1415927,scaling=NONE}\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. TV        dbl \n" +
                "2. Radio     dbl \n" +
                "3. Newspaper dbl \n" +
                "> target variables: \n" +
                "1. Sales dbl \n" +
                "\n" +
                "Target <<< Sales >>>\n" +
                "\n" +
                "> Coefficients: \n" +
                "  Name    Estimate  \n" +
                "TV        0.0537927 \n" +
                "Radio     0.2222129 \n" +
                "Newspaper 0.0168220 \n" +
                "\n", model2.fit(df, "Sales").toSummary());
    }
}
