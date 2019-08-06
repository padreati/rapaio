package rapaio.ml.eval.metric;

import org.junit.Test;
import rapaio.core.*;
import rapaio.core.distributions.*;
import rapaio.data.*;
import rapaio.datasets.*;
import rapaio.ml.eval.metric.*;
import rapaio.ml.regression.linear.*;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/17.
 */
public class RMSETest {

    private static final double TOL = 1e-15;

    @Test
    public void basicTest() {

        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = normal.sample(100).withName("x");
        VarDouble y = VarDouble.from(x, val -> val + 1).withName("y");
        VarDouble z = VarDouble.from(x, val -> val - 2).withName("z");

        RMSE rmse1 = RMSE.from(x, y);
        RMSE rmse2 = RMSE.from(x, z);

        assertEquals("> Root Mean Squared Error (RMSE):\n" +
                "\n" +
                "target rmse mse \n" +
                " x | y    1   1 \n" +
                "\n" +
                "Total RMSE: 1\n" +
                "Total MSE: 1\n" +
                "\n", rmse1.summary());

        assertEquals("> Root Mean Squared Error (RMSE):\n" +
                "\n" +
                "target rmse mse \n" +
                " x | z    2   4 \n" +
                "\n" +
                "Total RMSE: 2\n" +
                "Total MSE: 4\n" +
                "\n", rmse2.summary());
    }

    @Test
    public void irisTest() throws IOException, URISyntaxException {

        Frame df = Datasets.loadIrisDataset().mapVars(VRange.onlyTypes(VType.DOUBLE));

        df.printSummary();

        String[] targets = new String[]{"sepal-length", "sepal-width", "petal-length"};

        LinearRegressionModel lm = LinearRegressionModel.newLm().withIntercept(true);
        lm.fit(df, targets);

        LinearRegressionResult fit = lm.predict(df, true);
        RMSE rmse = RMSE.from(df.mapVars(fit.targetNames()), fit.predictionFrame());

        for (int i = 0; i < targets.length; i++) {
            assertEquals(fit.rss(targets[i])/df.rowCount(), rmse.mse().getDouble(i), TOL);
            assertEquals(rmse.totalRmse(), Math.sqrt(rmse.totalMse()), TOL);
        }
    }
}
