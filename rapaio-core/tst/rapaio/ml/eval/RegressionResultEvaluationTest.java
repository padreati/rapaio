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

package rapaio.ml.eval;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        double mean = target.tensor_().nanMean();
        double count = target.size();

        double expectedScore = Math.sqrt(target.tensor().sub_(mean).sqr_().nanSum() / count);

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
