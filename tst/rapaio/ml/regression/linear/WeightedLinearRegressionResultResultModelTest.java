package rapaio.ml.regression.linear;

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class WeightedLinearRegressionResultResultModelTest {

    private static final double TOL = 1e-20;

    @Test
    void testCoefficients() {
        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = VarDouble.seq(0, 100, 1).withName("x");
        VarDouble intercept = VarDouble.fill(x.rowCount(), 1.0).withName("I");
        VarDouble y1 = VarDouble.from(x, v -> v * 2 + normal.sampleNext()).withName("y1");
        VarDouble y2 = VarDouble.from(x, v -> v * 3 - 10 + normal.sampleNext()).withName("y2");

        Frame df = BoundFrame.byVars(x, y1, y2);

        var result1 = WeightedLinearRegression.newModel()
                .intercept.set(true).fit(df, "y1")
                .predict(df, true);

        var result2 = LinearRegressionModel.newModel()
                .intercept.set(true).fit(df, "y1")
                .predict(df, true);

        assertTrue(result1.beta_hat.deepEquals(result2.beta_hat));

        VarDouble w = VarDouble.wrapArray(df.rowCount(), Uniform.of(0, 1).sample(df.rowCount()).elements());

        var result3 = WeightedLinearRegression.newModel()
                .intercept.set(true)
                .newInstance()
                .fit(df, w, "y1")
                .predict(df, true);

        assertFalse(result1.beta_hat.deepEquals(result3.beta_hat));

        assertEquals("WeightedLinearRegression", WeightedLinearRegression.newModel().name());
    }
}
