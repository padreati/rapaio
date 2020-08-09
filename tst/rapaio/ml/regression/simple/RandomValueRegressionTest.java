package rapaio.ml.regression.simple;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.core.tests.KSTestOneSample;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/9/19.
 */
public class RandomValueRegressionTest {

    private String father = "Father";
    private String son = "Son";
    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(123);
        df = Datasets.loadPearsonHeightDataset();
    }

    @Test
    void testRandomValueRegression() {
        var fit1 = RandomValueRegression.newRVR().fit(df, father).predict(df);
        var fit2 = RandomValueRegression.from(Normal.of(10, 0.1)).fit(df, father).predict(df);

        // unsignificant if test on true distribution
        assertTrue(KSTestOneSample.from(fit1.firstPrediction(), Uniform.of(0, 1)).pValue() > 0.01);
        assertTrue(KSTestOneSample.from(fit2.firstPrediction(), Normal.of(10, 0.1)).pValue() > 0.01);

        // significant if test on a different distribution
        assertTrue(KSTestOneSample.from(fit1.firstPrediction(), Normal.of(10, 0.1)).pValue() < 0.01);
        assertTrue(KSTestOneSample.from(fit2.firstPrediction(), Uniform.of(0, 1)).pValue() < 0.01);
    }

    @Test
    void testNaming() {
        RandomValueRegression model = RandomValueRegression.newRVR();
        assertEquals("RandomValueRegression", model.name());
        assertEquals("RandomValueRegression{distribution=Uniform(a=0,b=1)}", model.newInstance().fullName());

        assertEquals("Normal(mu=10, sd=20)", RandomValueRegression.from(Normal.of(10, 20)).newInstance().distribution.get().name());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RandomValueRegression\n" +
                "Model instance: RandomValueRegression{}\n" +
                "> model not trained.\n", model.toContent());

        model = model.fit(df, "Son");
        assertEquals("RandomValueRegression{}", model.toString());
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RandomValueRegression\n" +
                "Model instance: RandomValueRegression{}\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. Father dbl \n" +
                "> target variables: \n" +
                "1. Son dbl \n" +
                "Model is trained.\n", model.toContent());
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RandomValueRegression\n" +
                "Model instance: RandomValueRegression{}\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. Father dbl \n" +
                "> target variables: \n" +
                "1. Son dbl \n" +
                "Model is trained.\n", model.toFullContent());
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RandomValueRegression\n" +
                "Model instance: RandomValueRegression{}\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. Father dbl \n" +
                "> target variables: \n" +
                "1. Son dbl \n" +
                "Model is trained.\n", model.toSummary());
    }
}
