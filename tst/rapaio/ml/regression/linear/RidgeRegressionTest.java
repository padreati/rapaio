package rapaio.ml.regression.linear;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.*;
import rapaio.datasets.*;
import rapaio.math.linear.*;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public class RidgeRegressionTest {

    private static final double TOL = 1e-12;

    private Frame df;

    @Before
    public void setUp() throws IOException {
        df = Datasets.loadISLAdvertising().removeVars("ID");
    }

    @Test
    public void testNoRegularization() throws IOException {

        // test the results for ridge are the same as those for linear regression when lambda equals 0

        // when we have intercept, in ridge we have to center, scaling does not matter

        LinearRegression lm1 = LinearRegression.newLm().withIntercept(true).fit(df, "Sales");
        RidgeRegression rlm1 = RidgeRegression.newRidgeLm(0).withIntercept(true).withCentering(true).withScaling(true)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm1.firstCoefficients().valueStream().toArray(),
                rlm1.firstCoefficients().valueStream().toArray(),
                TOL);

        LinearRegression lm2 = LinearRegression.newLm().withIntercept(true).fit(df, "Sales");
        RidgeRegression rlm2 = RidgeRegression.newRidgeLm(0).withIntercept(true).withCentering(true).withScaling(false)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm2.firstCoefficients().valueStream().toArray(),
                rlm2.firstCoefficients().valueStream().toArray(),
                TOL);

        // when we do not have intercept, then we do not center

        LinearRegression lm3 = LinearRegression.newLm().withIntercept(false).fit(df, "Sales");
        RidgeRegression rlm3 = RidgeRegression.newRidgeLm(0).withIntercept(false).withCentering(false).withScaling(false)
                .newInstance().fit(df, "Sales");

        assertArrayEquals(
                lm1.firstCoefficients().valueStream().toArray(),
                rlm1.firstCoefficients().valueStream().toArray(),
                TOL);

    }

    @Test
    public void ridgeCoefficientsTestedWithR() {

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
            RidgeRegression rr = RidgeRegression.newRidgeLm(lambdas[i]);
            double[] beta_hat = rr.fit(df, "Sales").firstCoefficients().valueStream().toArray();
            assertArrayEquals(coeff[i], beta_hat, 1e9);
        }
    }

    @Test
    public void testProperties() {
        RidgeRegression rlm = RidgeRegression.newRidgeLm(0);

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
    public void testCoefficients() {
        RidgeRegression rlm = RidgeRegression.newRidgeLm(10).fit(df, "Sales");

        assertArrayEquals(rlm.firstCoefficients().valueStream().toArray(), rlm.getCoefficients(0).valueStream().toArray(), TOL);
        assertEquals(1, rlm.allCoefficients().colCount());
        assertEquals(4, rlm.allCoefficients().rowCount());
        assertArrayEquals(rlm.firstCoefficients().valueStream().toArray(), rlm.allCoefficients().mapCol(0).valueStream().toArray(), TOL);
    }

    @Test
    public void testPredictionWithIntercept() {
        RidgeRegression model = RidgeRegression.newRidgeLm(10).fit(df, "Sales");
        RidgeRegResult result = model.predict(df);
        RV beta_hat = result.getBetaHat().mapCol(0);
        assertEquals(4, beta_hat.count());

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
    public void testPredictionWithOutIntercept() {
        RidgeRegression model = RidgeRegression.newRidgeLm(10).withIntercept(false).fit(df, "Sales");
        RidgeRegResult result = model.predict(df);
        RV beta_hat = result.getBetaHat().mapCol(0);
        assertEquals(3, beta_hat.count());

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
    public void testNames() {
        RidgeRegression model1 = RidgeRegression.newRidgeLm(1.2);
        RidgeRegression model2 = RidgeRegression.newRidgeLm(Math.PI).withIntercept(false).withCentering(false).withScaling(false);

        assertEquals("RidgeRegression", model1.name());
        assertEquals("RidgeRegression", model2.name());

        assertEquals("RidgeRegression(lambda=1.2,intercept=true,center=true,scaling=true)", model1.fullName());
        assertEquals("RidgeRegression(lambda=3.1415927,intercept=false,center=false,scaling=false)", model2.fullName());
    }
}
