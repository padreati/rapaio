/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.eval.metric.RegressionMetric;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.simple.L2Regression;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/19.
 */
public class RegressionResultEvaluationTest {

    private final SplitStrategy splitStrategy = (df, weights) -> List.of(
            new Split(0, 0, df, df), new Split(0, 1, df, df));

    private final String targetName = "target";

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testSmoke() {
        Frame df = SolidFrame.byVars(VarDouble.copy(1.0, 1.0, 1.0, 2.0).name(targetName));
        RegressionModel model = L2Regression.newModel();
        RegressionMetric metric = RMSE.newMetric();

        RegressionEvaluation eval = RegressionEvaluation.newEval()
                .df.set(df)
                .model.set(model)
                .splitStrategy.set(splitStrategy)
                .threads.set(1)
                .metrics.add(metric)
                .targetName.set(targetName);

        Var target = df.rvar(targetName);
        double mean = target.op().nanmean();
        double count = target.size();

        double expectedScore = Math.sqrt(target.copy()
                .op().minus(mean)
                .op().apply(x -> x * x)
                .op().nansum() / count);

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
}
