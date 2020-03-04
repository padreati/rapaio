package rapaio.ml.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.bayes.NaiveBayes;
import rapaio.ml.classifier.bayes.nb.GaussianEstimator;
import rapaio.ml.eval.cmetric.CMetric;
import rapaio.ml.eval.split.StratifiedKFold;

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
        var nb = NaiveBayes.newModel().withEstimators(GaussianEstimator.forType(iris, VType.DOUBLE));
        var result = CEval.newInstance(iris, "class", nb)
                .withSplitStrategy(new StratifiedKFold(2, 10))
                .withMetrics(CMetric.accuracy(), CMetric.logloss())
                .withThreads(1)
                .run();

        assertNotNull(result);
        assertEquals(nb.name(), result.getModel().name());
        assertTrue(result.getFrame().deepEquals(iris));
        assertEquals("class", result.getTargetName());

        Frame raw = result.getTestScores();
        assertNotNull(raw);
        assertEquals(10 * 2, raw.rowCount());
        assertEquals(4, raw.varCount());

        assertEquals("Model:\n" +
                "NaiveBayes{prior=MLE{},estimators=[Gaussian{test=sepal-length, values=[]},Gaussian{test=sepal-width, values=[]},Gaussian{test=petal-length, values=[]},Gaussian{test=petal-width, values=[]}]}\n" +
                "CV score\n" +
                "=============\n" +
                "     metric     mean       std    \n" +
                "[0] Accuracy  0.96      0.0065376 \n" +
                "[1]  LogLoss 14.8946138 1.9126599 \n" +
                "\n", result.toContent(textWidth(100)));
        assertEquals("Model:\n" +
                "NaiveBayes{prior=MLE{},estimators=[Gaussian{test=sepal-length, values=[]},Gaussian{test=sepal-width, values=[]},Gaussian{test=petal-length, values=[]},Gaussian{test=petal-width, values=[]}]}\n" +
                "Raw scores:\n" +
                "===========\n" +
                "     round fold Accuracy   LogLoss        round fold Accuracy   LogLoss        round fold Accuracy   LogLoss   \n" +
                " [0]     0    0 0.962963  14.4392313  [7]     0    7 0.962963  15.530261  [14]     1    4 0.9555556 15.2002472 \n" +
                " [1]     0    1 0.9777778  9.2642465  [8]     0    8 0.9555556 16.000559  [15]     1    5 0.9555556 16.3006638 \n" +
                " [2]     0    2 0.9555556 16.3721544  [9]     0    9 0.9555556 16.5657826 [16]     1    6 0.962963  15.9817162 \n" +
                " [3]     0    3 0.9481481 16.3202788 [10]     1    0 0.9555556 16.3230409 [17]     1    7 0.962963  13.102989  \n" +
                " [4]     0    4 0.962963  12.4977892 [11]     1    1 0.9555556 16.7767023 [18]     1    8 0.962963  14.1861791 \n" +
                " [5]     0    5 0.9555556 16.4135835 [12]     1    2 0.9703704 14.6013046 [19]     1    9 0.962963  14.2118673 \n" +
                " [6]     0    6 0.9555556 15.6107897 [13]     1    3 0.962963  12.1928901 \n" +
                "\n" +
                "Round scores:\n" +
                "=============\n" +
                "    round Accuracy_mean Accuracy_std LogLoss_mean LogLoss_std \n" +
                "[0]     0   0.9592593    0.0075903    14.9014676   2.2160437  \n" +
                "[1]     1   0.9607407    0.0047431    14.88776     1.4282036  \n" +
                "\n" +
                "CV score\n" +
                "=============\n" +
                "     metric     mean       std    \n" +
                "[0] Accuracy  0.96      0.0065376 \n" +
                "[1]  LogLoss 14.8946138 1.9126599 \n" +
                "\n", result.toFullContent());
    }

    @Test
    void testAccuracy() {

        Frame df = Datasets.loadIrisDataset();
        var model = NaiveBayes.newModel().withEstimators(GaussianEstimator.forType(df, VType.DOUBLE));
        model.fit(df, "class");
        var r = model.predict(df, true, true);

        assertEquals("Accuracy", CMetric.accuracy().name());
        double p = CMetric.accuracy().compute(r, df.rvar("class"));
        assertEquals(0.96, p);
        double up = CMetric.accuracy(false).compute(r, df.rvar("class"));
        assertEquals(144, up);

        assertEquals(p*df.rowCount(), up);
    }
}
