package rapaio.ml.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.bayes.NaiveBayes;
import rapaio.ml.classifier.bayes.nb.GaussianEstimator;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.printer.Printer.textWidth;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/20.
 */
public class CEvalTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void cvTest() {
        Frame iris = Datasets.loadIrisDataset();
        ClassifierModel nb = NaiveBayes.newModel().withEstimators(GaussianEstimator.forType(iris, VType.DOUBLE));
        CEval.Result result = CEval.cv(iris, "class", nb, 10, 2, Arrays.asList(CEval.Metric.accuracy(), CEval.Metric.logloss()), true, 1);

        assertNotNull(result);
        assertEquals(10, result.getFolds());
        assertEquals(2, result.getRounds());
        assertEquals(nb.name(), result.getModel().name());
        assertTrue(result.getFrame().deepEquals(iris));
        assertEquals("class", result.getTargetName());

        Frame raw = result.getRawScores();
        assertNotNull(raw);
        assertEquals(10 * 2, raw.rowCount());
        assertEquals(4, raw.varCount());

        assertEquals("Model:\n" +
                "NaiveBayes{prior=MLE{},estimators=[Gaussian{test=sepal-length, values=[]},Gaussian{test=sepal-width, values=[]},Gaussian{test=petal-length, values=[]},Gaussian{test=petal-width, values=[]}]}\n" +
                "CV score\n" +
                "=============\n" +
                "     metric    mean       std    \n" +
                "[0] Accuracy 0.9533333 0.0488463 \n" +
                "[1]  LogLoss 1.9635111 1.9293964 \n" +
                "\n", result.toContent(textWidth(100)));
        assertEquals("Model:\n" +
                "NaiveBayes{prior=MLE{},estimators=[Gaussian{test=sepal-length, values=[]},Gaussian{test=sepal-width, values=[]},Gaussian{test=petal-length, values=[]},Gaussian{test=petal-width, values=[]}]}\n" +
                "Raw scores:\n" +
                "===========\n" +
                "     round fold Accuracy   LogLoss       round fold Accuracy   LogLoss       round fold Accuracy   LogLoss  \n" +
                " [0]     0    0 0.9333333 2.1714128  [7]     0    7 0.9333333 1.0274013 [14]     1    4 1         0.7122148 \n" +
                " [1]     0    1 0.8       7.7266373  [8]     0    8 1         0.4627481 [15]     1    5 1         0.9126003 \n" +
                " [2]     0    2 1         0.0989728  [9]     0    9 1         0.5383851 [16]     1    6 0.9333333 1.3961202 \n" +
                " [3]     0    3 1         0.6918864 [10]     1    0 0.9333333 1.1826963 [17]     1    7 0.9333333 4.9907208 \n" +
                " [4]     0    4 0.9333333 4.2276744 [11]     1    1 1         0.4081769 [18]     1    8 0.9333333 2.2989367 \n" +
                " [5]     0    5 0.9333333 1.37594   [12]     1    2 0.9333333 1.3309705 [19]     1    9 0.9333333 3.016199  \n" +
                " [6]     0    6 1         0.8267066 [13]     1    3 0.9333333 3.8738227 \n" +
                "\n" +
                "Round scores:\n" +
                "=============\n" +
                "    round Accuracy_mean Accuracy_std LogLoss_mean LogLoss_std \n" +
                "[0]     0   0.9533333    0.06         1.9147765    2.2416557  \n" +
                "[1]     1   0.9533333    0.0305505    2.0122458    1.4293751  \n" +
                "\n" +
                "CV score\n" +
                "=============\n" +
                "     metric    mean       std    \n" +
                "[0] Accuracy 0.9533333 0.0488463 \n" +
                "[1]  LogLoss 1.9635111 1.9293964 \n" +
                "\n", result.toFullContent());
    }

    @Test
    void testAccuracy() {

        Frame df = Datasets.loadIrisDataset();
        var model = NaiveBayes.newModel().withEstimators(GaussianEstimator.forType(df, VType.DOUBLE));
        model.fit(df, "class");
        var r = model.predict(df, true, true);

        assertEquals("Accuracy", CEval.Metric.accuracy().name());
        double p = CEval.Metric.accuracy().compute(r, df.rvar("class"));
        assertEquals(0.96, p);
        double up = CEval.Metric.accuracy(false).compute(r, df.rvar("class"));
        assertEquals(144, up);

        assertEquals(p*df.rowCount(), up);
    }
}
