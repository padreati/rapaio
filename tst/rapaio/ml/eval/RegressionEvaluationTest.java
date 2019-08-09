package rapaio.ml.eval;

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
        reval = RegressionEvaluation.builder()
                .df(df)
                .metric(RMetric.RMS)
                .targetName("Sales")
                .model("rf1", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(1))
                .model("rf10", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(10))
                .model("rf100", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(100))
                .model("lm", LinearRegressionModel.newLm())
                .model("ridge(1)", RidgeRegressionModel.newRidgeLm(1))
                .model("ridge(10)", RidgeRegressionModel.newRidgeLm(10))
                .build();
    }

    @Test
    public void testCv() {
        RegressionEvaluation.CVResult cvr = reval.cv(10);
        assertEquals(10, cvr.getFolds());
        assertEquals(3, cvr.getSummaryFrame().varCount());
        assertEquals(6, cvr.getSummaryFrame().rowCount());

        for (String modelId : cvr.getRegressionModels().keySet()) {
            for (int j = 0; j < 6; j++) {
                assertFalse(Double.isNaN(cvr.getScore(modelId, j)));
            }
        }
    }

    @Test
    public void testDefault() {
        RegressionEvaluation re = RegressionEvaluation.builder()
                .df(df)
                .metric(RMetric.RMS)
                .targetName("Sales")
                .model("rf1", RForest.newRF().withSampler(RowSampler.subsampler(0.8)).withRuns(1))
                .build();
        assertTrue(re.isDebug());
    }
}
