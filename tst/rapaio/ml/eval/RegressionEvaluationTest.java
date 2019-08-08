package rapaio.ml.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.*;
import rapaio.data.sample.*;
import rapaio.datasets.*;
import rapaio.experiment.ml.eval.*;
import rapaio.experiment.ml.regression.ensemble.*;
import rapaio.ml.regression.linear.*;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/19.
 */
public class RegressionEvaluationTest {

    private Frame df;
    private RegressionEvaluation reval;

    @Before
    public void setUp() throws IOException {
        df = Datasets.loadISLAdvertising().removeVars("ID").copy();
        reval = RegressionEvaluation.instance()
                .withFrame(df)
                .withDebug(false)
                .withMetric(RMetric.RMS)
                .withTarget("Sales")
                .withModel("rf1", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(1))
                .withModel("rf10", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(10))
                .withModel("rf100", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(100))
                .withModel("lm", LinearRegressionModel.newLm())
                .withModel("ridge(1)", RidgeRegressionModel.newRidgeLm(1))
                .withModel("ridge(10)", RidgeRegressionModel.newRidgeLm(10));
    }

    @Test
    public void testCv() {
        RegressionEvaluation.CVResult cvr = reval.cv(10);
        assertEquals(10, cvr.getFolds());
        assertEquals(3, cvr.getSummaryFrame().varCount());
        assertEquals(6, cvr.getSummaryFrame().rowCount());

        for (String modelId : cvr.getRegressionModels().keySet()) {
            for (int j = 0; j < 6; j++) {
                assertTrue(!Double.isNaN(cvr.getScore(modelId, j)));
            }
        }
    }

    @Test
    public void testOutput() {
        reval.withDebug(true);
        RegressionEvaluation.CVResult cv = reval.cv(2);
        cv.getSummaryFrame().printContent();
    }
}
