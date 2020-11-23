package rapaio.ml.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.eval.metric.RMSE;
import rapaio.ml.eval.metric.RegressionMetric;
import rapaio.ml.eval.split.KFold;
import rapaio.ml.eval.split.Split;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.simple.L2Regression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/8/19.
 */
@ExtendWith(MockitoExtension.class)
public class RegressionResultEvaluationTest {

    @Mock
    private KFold splitStrategy;

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

        doReturn(List.of(
                Split.builder()
                        .trainDf(df)
                        .testDf(df)
                        .fold(0)
                        .round(0)
                        .build(),
                Split.builder()
                        .trainDf(df)
                        .testDf(df)
                        .fold(1)
                        .round(0)
                        .build())
        )
                .when(splitStrategy).generateSplits(any(), any());

        RegressionEvaluation eval = RegressionEvaluation.builder()
                .df(df)
                .model(model)
                .splitStrategy(splitStrategy)
                .threads(1)
                .metric(metric)
                .targetName(targetName)
                .build();

        Var target = df.rvar(targetName);
        double mean = target.op().nanmean();
        double count = target.rowCount();

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
