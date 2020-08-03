package rapaio.ml.regression.ensemble;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.tree.RTree;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/27/20.
 */
public class RForestTest {

    private static Frame advertising;

    @BeforeAll
    static void beforeAll() throws IOException {
        advertising = Datasets.loadISLAdvertising().removeVars("ID");
    }

    @Test
    void paramsTest() {
        RForest rf = RForest.newRF(RTree.newCART().minCount.set(1));
        assertEquals("RForest", rf.name());
        assertEquals("RForest{model=RTree{minCount=10,minScore=0,maxDepth=2147483647,maxSize=2147483647,tests=[nom:NomBin],loss=loss,split=splitter,varSelector=varSelector,runs=1,poolSize=-1,sampler=Identity},runs=1}", rf.fullName());

        assertEquals(rf.toSummary(), rf.toContent());
        assertEquals(rf.toSummary(), rf.toFullContent());
    }

    @Test
    void smokeTest() {

        Frame[] train_test = SamplingTools.randomSampleSlices(advertising, 0.8, 0.2);

        int N = 10;

        var train = train_test[0];
        var test = train_test[1];

        var errorTrain = VarDouble.empty();
        var errorTest = VarDouble.empty();
        RForest rf = RForest.newRF()
                .runs.set(N)
                .runningHook.set((m, run) -> {
                    errorTest.addDouble(RMSE.newMetric().compute(test.rvar("Sales"),
                            m.predict(test).firstPrediction()).getScore().getValue());
                    errorTrain.addDouble(RMSE.newMetric().compute(train.rvar("Sales"),
                            m.predict(train).firstPrediction()).getScore().getValue());
                });
        rf.fit(train, "Sales");
        assertEquals(N, rf.getFittedModels().size());
        assertEquals(N, errorTest.rowCount());

        RegressionResult result = rf.predict(test, true);
        assertEquals(test.rowCount(), result.firstPrediction().rowCount());
    }
}
