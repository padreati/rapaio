package rapaio.ml.regression.linear;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public class RidgeRegressionTest {

    private static final double TOL = 1e-20;

//    @Test
    public void basicTest() throws IOException, URISyntaxException {

        // test the results for ridge are the same as those for linear regression when lamba equals 0

        RidgeRegression rlm = RidgeRegression.newRidgeLm(0).withCentering(false).withScaling(false);
        LinearRegression lm = LinearRegression.newLm();

        Frame df = Datasets.loadISLAdvertising().removeVars("ID");
        df.printSummary();

        LinearRPrediction lmFit = lm.fit(df, "Sales").predict(df, true);
        lmFit.printSummary();
        LinearRPrediction ridgeFit = rlm.fit(df, "Sales").predict(df, true);
        ridgeFit.printSummary();

        for (int i = 0; i < 3; i++) {
            assertEquals(lmFit.beta_hat.get(i, 0), ridgeFit.beta_hat.get(i, 0), TOL);
        }
    }

    @Test
    public void scalingTest() throws IOException {


        Frame df = Datasets.loadISLAdvertising().removeVars("ID");
        df.printSummary();

        RidgeRegression.newRidgeLm(0).withIntercept(false).withCentering(true).withScaling(true)
                .fit(df, "Sales").predict(df, true).printSummary();
        LinearRegression.newLm().withIntercept(true).withCentering(true).withScaling(true)
                .fit(df, "Sales").predict(df, true).printSummary();
    }

    @Test
    @Deprecated
    public void ridgeCoefficients() throws IOException {

        VarDouble lambda = VarDouble.seq(0, 10, 0.5);

        Frame df = Datasets.loadISLAdvertising().removeVars("ID");

        for (int i = 0; i < lambda.rowCount(); i++) {
            RidgeRegression rr = RidgeRegression.newRidgeLm(lambda.getDouble(i));
            LinearRPrediction fit = rr.fit(df, "Sales").predict(df, true);

            fit.printSummary();
        }
    }
}
