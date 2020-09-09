package rapaio.ml.eval.metric;

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.linear.LinearRegressionModel;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/17.
 */
public class RMSETest {

    private static final double TOL = 1e-15;

    @Test
    void basicTest() {

        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = normal.sample(100).withName("x");
        VarDouble y = VarDouble.from(x, val -> val + 1).withName("y");
        VarDouble z = VarDouble.from(x, val -> val - 2).withName("z");

        RegressionScore rmse1 = RMSE.newMetric().compute(x, y);
        RegressionScore rmse2 = RMSE.newMetric().compute(x, z);

        assertEquals("RMSE: 1\n", rmse1.toSummary());
        assertEquals("RMSE: 2\n", rmse2.toSummary());
    }

    @Test
    void irisTest() {

        Frame df = Datasets.loadIrisDataset().mapVars(VRange.onlyTypes(VType.DOUBLE));

        df.printSummary();

        String[] targets = new String[]{"sepal-length", "sepal-width", "petal-length"};

        LinearRegressionModel lm = LinearRegressionModel.newModel().intercept.set(true);
        lm.fit(df, targets);

        var prediction = lm.predict(df, true);
        for (String target : targets) {
            RegressionScore rmse = RMSE.newMetric().compute(df.rvar(target), prediction.prediction(target));
            assertEquals(prediction.rss(target) / df.rowCount(), Math.pow(rmse.getValue(), 2), TOL);
        }
    }
}
