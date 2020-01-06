package rapaio.ml.regression.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.math.linear.RV;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public class RidgeRegressionModelTest {

    private static final double TOL = 1e-12;

    private Frame df;

    @BeforeEach
    void setUp() throws IOException {
        df = Datasets.loadISLAdvertising().removeVars("ID");
    }

    @Test
    void testNoRegularization() throws IOException {

        // test the results for ridge are the same as those for linear regression when lambda equals 0

        // when we have intercept, in ridge we have to center, scaling does not matter

        LinearRegressionModel lm1 = LinearRegressionModel.newLm().withIntercept(true).fit(df, "Sales");
        RidgeRegressionModel rlm1 = RidgeRegressionModel.newRidgeLm(0).withIntercept(true).withCentering(true).withScaling(true)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm1.firstCoefficients().valueStream().toArray(),
                rlm1.firstCoefficients().valueStream().toArray(),
                TOL);

        LinearRegressionModel lm2 = LinearRegressionModel.newLm().withIntercept(true).fit(df, "Sales");
        RidgeRegressionModel rlm2 = RidgeRegressionModel.newRidgeLm(0).withIntercept(true).withCentering(true).withScaling(false)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm2.firstCoefficients().valueStream().toArray(),
                rlm2.firstCoefficients().valueStream().toArray(),
                TOL);

        // when we do not have intercept, then we do not center

        LinearRegressionModel lm3 = LinearRegressionModel.newLm().withIntercept(false).fit(df, "Sales");
        RidgeRegressionModel rlm3 = RidgeRegressionModel.newRidgeLm(0).withIntercept(false).withCentering(false).withScaling(false)
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
            RidgeRegressionModel rr = RidgeRegressionModel.newRidgeLm(lambdas[i]);
            double[] beta_hat = rr.fit(df, "Sales").firstCoefficients().valueStream().toArray();
            assertArrayEquals(coeff[i], beta_hat, 1e9);
        }
    }

    @Test
    void testProperties() {
        RidgeRegressionModel rlm = RidgeRegressionModel.newRidgeLm(0);

        assertTrue(rlm.hasIntercept());
        assertTrue(rlm.hasCentering());
        assertTrue(rlm.hasScaling());
        assertEquals(0, rlm.getLambda(), TOL);

        rlm = rlm.withIntercept(false).withCentering(false).withScaling(false).withLambda(1);

        assertFalse(rlm.hasIntercept());
        assertFalse(rlm.hasCentering());
        assertFalse(rlm.hasScaling());
        assertEquals(1, rlm.getLambda(), TOL);
    }

    @Test
    void testCoefficients() {
        RidgeRegressionModel rlm = RidgeRegressionModel.newRidgeLm(10).fit(df, "Sales");

        assertArrayEquals(rlm.firstCoefficients().valueStream().toArray(), rlm.getCoefficients(0).valueStream().toArray(), TOL);
        assertEquals(1, rlm.allCoefficients().colCount());
        assertEquals(4, rlm.allCoefficients().rowCount());
        assertArrayEquals(rlm.firstCoefficients().valueStream().toArray(), rlm.allCoefficients().mapCol(0).valueStream().toArray(), TOL);
    }

    @Test
    void testPredictionWithIntercept() {
        RidgeRegressionModel model = RidgeRegressionModel.newRidgeLm(10).fit(df, "Sales");
        var result = model.predict(df);
        RV beta_hat = result.getBetaHat().mapCol(0);
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
        RidgeRegressionModel model = RidgeRegressionModel.newRidgeLm(10).withIntercept(false).fit(df, "Sales");
        var result = model.predict(df);
        RV beta_hat = result.getBetaHat().mapCol(0);
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
        RidgeRegressionModel model1 = RidgeRegressionModel.newRidgeLm(1.2);
        RidgeRegressionModel model2 = RidgeRegressionModel.newRidgeLm(Math.PI).withIntercept(false).withCentering(false).withScaling(false);

        assertEquals("RidgeRegression", model1.name());
        assertEquals("RidgeRegression", model2.name());

        assertEquals("RidgeRegression(lambda=1.2,intercept=true,center=true,scaling=true)", model1.fullName());
        assertEquals("RidgeRegression(lambda=3.1415927,intercept=false,center=false,scaling=false)", model2.fullName());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RidgeRegression\n" +
                "Model instance: RidgeRegression(lambda=1.2,intercept=true,center=true,scaling=true)\n" +
                "> model not trained.\n" +
                "\n", model1.toContent());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RidgeRegression\n" +
                "Model instance: RidgeRegression(lambda=3.1415927,intercept=false,center=false,scaling=false)\n" +
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
                "  Name    Estimate \n" +
                "TV        0.053793 \n" +
                "Radio     0.222213 \n" +
                "Newspaper 0.016822 \n" +
                "\n", model2.fit(df, "Sales").toSummary());
    }
}
