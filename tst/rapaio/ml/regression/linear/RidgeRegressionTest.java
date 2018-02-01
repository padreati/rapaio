package rapaio.ml.regression.linear;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/1/18.
 */
public class RidgeRegressionTest {

    @Test
    public void basicTest() throws IOException, URISyntaxException {

        // test the results for ridge are the same as those for linear regression when lamba equals 0

        RidgeRegression rlm = RidgeRegression.newRidgeLm(0);
        LinearRegression lm = LinearRegression.newLm();

        Frame df = Datasets.loadISLAdvertising().removeVars("ID");
        df.printSummary();

        lm.train(df, "Sales").fit(df, true).printSummary();
        rlm.train(df, "Sales").fit(df, true).printSummary();
    }
}
