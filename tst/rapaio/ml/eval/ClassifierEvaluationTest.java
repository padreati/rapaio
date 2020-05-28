package rapaio.ml.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.rule.ZeroRule;
import rapaio.ml.eval.metric.Accuracy;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.ml.eval.split.KFold;
import rapaio.ml.eval.split.Split;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/20.
 */
@ExtendWith(MockitoExtension.class)
public class ClassifierEvaluationTest {

    @Mock
    private KFold splitStrategy;

    private final String targetName = "target";

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testSmoke() {
        Frame df = SolidFrame.byVars(VarNominal.copy("a", "a", "a", "b").withName(targetName));
        ClassifierModel model = ZeroRule.newModel();
        ClassifierMetric metric = Accuracy.newMetric(true);

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

        ClassifierEvaluation eval = ClassifierEvaluation.builder()
                .df(df)
                .model(model)
                .splitStrategy(splitStrategy)
                .threads(1)
                .metric(metric)
                .targetName(targetName)
                .build();

        ClassifierEvaluationResult result = eval.run();
        assertEquals(2, result.getTrainScores().rowCount());
        assertEquals(0, result.getTrainScores().getDouble(0, "round"));
        assertEquals(0, result.getTrainScores().getDouble(0, "fold"));
        assertEquals(0.75, result.getTrainScores().getDouble(0, metric.getName()));
        assertEquals(0, result.getTrainScores().getDouble(1, "round"));
        assertEquals(1, result.getTrainScores().getDouble(1, "fold"));
        assertEquals(0.75, result.getTrainScores().getDouble(1, metric.getName()));

        assertEquals(2, result.getTestScores().rowCount());
        assertEquals(0, result.getTestScores().getDouble(0, "round"));
        assertEquals(0, result.getTestScores().getDouble(0, "fold"));
        assertEquals(0.75, result.getTestScores().getDouble(0, metric.getName()));
        assertEquals(0, result.getTestScores().getDouble(1, "round"));
        assertEquals(1, result.getTestScores().getDouble(1, "fold"));
        assertEquals(0.75, result.getTestScores().getDouble(1, metric.getName()));
    }
}
