/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.ml.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.eval.metric.RegressionMetric;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;
import rapaio.ml.model.simple.L2Regression;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/19.
 */
public class RegressionResultEvaluationTest {

    private final SplitStrategy splitStrategy = (df, weights, __) -> List.of(
            new Split(0, 0, df, df), new Split(0, 1, df, df));

    private static final String TARGET_NAME = "target";

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void testSmoke() {
        Frame df = SolidFrame.byVars(VarDouble.copy(1.0, 1.0, 1.0, 2.0).name(TARGET_NAME));
        var model = L2Regression.newModel();
        RegressionMetric metric = RMSE.newMetric();

        RegressionEval eval = RegressionEval.newEval()
                .df.set(df)
                .model.set(model)
                .splitStrategy.set(splitStrategy)
                .threads.set(1)
                .metrics.add(metric)
                .targetName.set(TARGET_NAME)
                .seed.set(123L);

        Var target = df.rvar(TARGET_NAME);
        double mean = target.darray_().nanMean();
        double count = target.size();

        double expectedScore = Math.sqrt(target.darray().sub_(mean).sqr_().nanSum() / count);

        RegressionEvaluationResult result = eval.run();
        assertEquals(2, result.getTrainScores().rowCount());
        assertEquals(0, result.getTrainScores().getDouble(0, "round"));
        assertEquals(0, result.getTrainScores().getDouble(0, "fold"));
        assertEquals(expectedScore, result.getTrainScores().getDouble(0, metric.getName()));
        assertEquals(0, result.getTrainScores().getDouble(1, "round"));
        assertEquals(1, result.getTrainScores().getDouble(1, "fold"));
        assertEquals(expectedScore, result.getTrainScores().getDouble(1, metric.getName()));

        assertEquals(2, result.getTestScores().rowCount());
        assertEquals(0, result.getTestScores().getDouble(0, "round"));
        assertEquals(0, result.getTestScores().getDouble(0, "fold"));
        assertEquals(expectedScore, result.getTestScores().getDouble(0, metric.getName()));
        assertEquals(0, result.getTestScores().getDouble(1, "round"));
        assertEquals(1, result.getTestScores().getDouble(1, "fold"));
        assertEquals(expectedScore, result.getTestScores().getDouble(1, metric.getName()));
    }

    /**
     * Regression: test scores used to be computed on the training fold, so they always equalled the train scores.
     * With disjoint train and test frames the two must differ, and the test score must be the metric of the
     * fitted model evaluated on the test target.
     */
    @Test
    void testScoresComeFromTheTestFold() {
        Frame train = SolidFrame.byVars(VarDouble.copy(1.0, 1.0, 1.0, 1.0).name(TARGET_NAME));
        Frame test = SolidFrame.byVars(VarDouble.copy(3.0, 3.0).name(TARGET_NAME));
        SplitStrategy disjoint = (df, weights, __) -> List.of(new Split(0, 0, train, test));
        RegressionMetric metric = RMSE.newMetric();
        RegressionEvaluationResult result = RegressionEval.newEval()
                .df.set(train)
                .model.set(L2Regression.newModel())
                .splitStrategy.set(disjoint)
                .threads.set(1)
                .metrics.add(metric)
                .targetName.set(TARGET_NAME)
                .run();
        // the model predicts the train mean (1.0); train RMSE is 0, test RMSE is |3 - 1| = 2
        assertEquals(0.0, result.getTrainScores().getDouble(0, metric.getName()), 1e-12);
        assertEquals(2.0, result.getTestScores().getDouble(0, metric.getName()), 1e-12);
        assertEquals(2.0, result.getMeanTestScore(metric.getName()), 1e-12);
        assertNotEquals(result.getMeanTrainScore(metric.getName()), result.getMeanTestScore(metric.getName()));
    }

    /**
     * The four argument {@link Split} constructor used to give training rows weight zero.
     */
    @Test
    void splitConvenienceConstructorUsesUnitWeights() {
        Frame df = SolidFrame.byVars(VarDouble.copy(1.0, 2.0, 3.0).name(TARGET_NAME));
        Split split = new Split(0, 0, df, df.mapRows(0, 1));
        assertEquals(3, split.trainWeights().size());
        assertEquals(2, split.testWeights().size());
        for (int i = 0; i < split.trainWeights().size(); i++) {
            assertEquals(1.0, split.trainWeights().getDouble(i));
        }
        for (int i = 0; i < split.testWeights().size(); i++) {
            assertEquals(1.0, split.testWeights().getDouble(i));
        }
    }

    /**
     * Instance weights configured on the evaluation must reach the model's fit method.
     */
    @Test
    void trainWeightsArePassedToFit() {
        Frame df = SolidFrame.byVars(VarDouble.copy(1.0, 2.0, 3.0, 4.0).name(TARGET_NAME));
        Var weights = VarDouble.copy(5.0, 6.0, 7.0, 8.0);
        SplitStrategy strategy = (frame, w, __) -> List.of(new Split(0, 0, frame, w, frame, w));

        L2Regression model = spy(L2Regression.newModel());
        doReturn(model).when(model).newInstance();

        RegressionEval.newEval()
                .df.set(df)
                .weights.set(weights)
                .model.set(model)
                .splitStrategy.set(strategy)
                .threads.set(1)
                .metrics.add(RMSE.newMetric())
                .targetName.set(TARGET_NAME)
                .run();

        ArgumentCaptor<Var> captor = ArgumentCaptor.forClass(Var.class);
        verify(model).fit(any(Frame.class), captor.capture(), eq(TARGET_NAME));
        assertSame(weights, captor.getValue());
    }

    /**
     * A fold that fails must surface as an exception instead of being silently dropped from the results.
     */
    @Test
    void failingFoldIsReported() {
        Frame df = SolidFrame.byVars(VarDouble.copy(1.0, 2.0).name(TARGET_NAME));
        SplitStrategy strategy = (frame, w, __) -> List.of(new Split(0, 0, frame, frame));
        L2Regression model = spy(L2Regression.newModel());
        doReturn(model).when(model).newInstance();
        doThrow(new IllegalArgumentException("boom")).when(model).fit(any(Frame.class), any(Var.class), eq(TARGET_NAME));

        RegressionEval eval = RegressionEval.newEval()
                .df.set(df)
                .model.set(model)
                .splitStrategy.set(strategy)
                .threads.set(1)
                .metrics.add(RMSE.newMetric())
                .targetName.set(TARGET_NAME);
        IllegalStateException ex = assertThrows(IllegalStateException.class, eval::run);
        assertTrue(ex.getMessage().contains("boom"));
    }
}
