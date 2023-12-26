/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.ensemble;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.tree.RTree;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/27/20.
 */
public class RForestTest {

    private static Frame advertising;

    @BeforeAll
    static void beforeAll() {
        advertising = Datasets.loadISLAdvertising().removeVars("ID");
    }

    @Test
    void paramsTest() {
        RForest rf = RForest.newRF(RTree.newCART().minCount.set(1));
        assertEquals("RForest", rf.name());
        assertEquals("RForest{model=RTree{splitter=Random,testMap={BINARY=NumericBinary,INT=NumericBinary,NOMINAL=NominalBinary," +
                "DOUBLE=NumericBinary,LONG=NumericBinary,STRING=Ignore}}}", rf.fullName());

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
                .runningHook.set(info -> {
                    errorTest.addDouble(RMSE.newMetric().compute(test.rvar("Sales"),
                            info.model().predict(test).firstPrediction()).value());
                    errorTrain.addDouble(RMSE.newMetric().compute(train.rvar("Sales"),
                            info.model().predict(train).firstPrediction()).value());
                });
        rf.fit(train, "Sales");
        assertEquals(N, rf.getFittedModels().size());
        assertEquals(N, errorTest.size());

        RegressionResult result = rf.predict(test, true);
        assertEquals(test.rowCount(), result.firstPrediction().size());
    }

    @Test
    void testParallelism() {
        var iris = Datasets.loadISLAdvertising();
        String target = "Sales";
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            var rf = RForest.newRF().runs.set(100).poolSize.set(-1).seed.set(123L);
            rf.fit(iris, target);
        }
    }
}
