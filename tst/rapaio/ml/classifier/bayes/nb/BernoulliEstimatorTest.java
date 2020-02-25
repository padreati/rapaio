package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/24/20.
 */
public class BernoulliEstimatorTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    void testBuilders() {

        var estimator = BernoulliEstimator.forName("x");
        assertEquals("Binomial{test=x, laplaceSmoother=1, values=[]}", estimator.fittedName());

        estimator = BernoulliEstimator.forName("x", 1.2);
        assertEquals("Binomial{test=x, laplaceSmoother=1.2, values=[]}", estimator.fittedName());

        List<BernoulliEstimator> estimators = BernoulliEstimator.forNames("a", "b", "c");
        assertEquals("Binomial{test=a, laplaceSmoother=1, values=[]}", estimators.get(0).fittedName());
        assertEquals("Binomial{test=b, laplaceSmoother=1, values=[]}", estimators.get(1).fittedName());
        assertEquals("Binomial{test=c, laplaceSmoother=1, values=[]}", estimators.get(2).fittedName());

        estimators = BernoulliEstimator.forNames(1.2, "a", "b", "c");
        assertEquals("Binomial{test=a, laplaceSmoother=1.2, values=[]}", estimators.get(0).fittedName());
        assertEquals("Binomial{test=b, laplaceSmoother=1.2, values=[]}", estimators.get(1).fittedName());
        assertEquals("Binomial{test=c, laplaceSmoother=1.2, values=[]}", estimators.get(2).fittedName());

        estimators = BernoulliEstimator.forRange(SolidFrame.byVars(
                VarNominal.empty().withName("a"),
                VarNominal.empty().withName("b"),
                VarNominal.empty().withName("c")
        ), VRange.all());
        assertEquals("Binomial{test=a, laplaceSmoother=1, values=[]}", estimators.get(0).fittedName());
        assertEquals("Binomial{test=b, laplaceSmoother=1, values=[]}", estimators.get(1).fittedName());
        assertEquals("Binomial{test=c, laplaceSmoother=1, values=[]}", estimators.get(2).fittedName());

        estimators = BernoulliEstimator.forRange(1.2, SolidFrame.byVars(
                VarNominal.empty().withName("a"),
                VarNominal.empty().withName("b"),
                VarNominal.empty().withName("c")
        ), VRange.all());
        assertEquals("Binomial{test=a, laplaceSmoother=1.2, values=[]}", estimators.get(0).fittedName());
        assertEquals("Binomial{test=b, laplaceSmoother=1.2, values=[]}", estimators.get(1).fittedName());
        assertEquals("Binomial{test=c, laplaceSmoother=1.2, values=[]}", estimators.get(2).fittedName());
    }

    @Test
    void testNewInstance() {
        var estimator = BernoulliEstimator.forName("x", 1.2);
        var copy = estimator.newInstance();

        assertEquals(estimator.fittedName(), copy.fittedName());
        assertEquals(estimator.name(), copy.name());
    }

    @Test
    void testFit() {
        var x = VarBinary.copy(0, 0, 0, 1, 1, 1).withName("x");
        var y = VarNominal.copy("a", "a", "b", "b", "a", "a").withName("y");
        var df = SolidFrame.byVars(x, y);
        var estimator = BernoulliEstimator.forName("x");
        estimator.fit(df, VarDouble.fill(x.rowCount(), 1), "y");

        assertEquals("Binomial{test=x, laplaceSmoother=1, values=[" +
                "{targetLevel:?,[?:0.333,0:0.167,1:0.167,}," +
                "{targetLevel:a,[?:0.333,0:0.5,1:0.5,}," +
                "{targetLevel:b,[?:0.333,0:0.333,1:0.333,},]}", estimator.fittedName());

        Frame test = SolidFrame.byVars(VarBinary.copy(0, 1, 2).withName("x"));

        assertEquals(0.5, estimator.predict(test, 0, "a"), TOLERANCE);
        assertEquals(0.5, estimator.predict(test, 1, "a"), TOLERANCE);
        assertEquals(1/3., estimator.predict(test, 2, "a"), TOLERANCE);

        assertEquals(0, estimator.predict(test, 0, "x"), TOLERANCE);

        test.setMissing(2, "x");
        assertEquals(1/3., estimator.predict(test, 2, "a"), TOLERANCE);

        test = SolidFrame.byVars(VarInt.copy(0).withName("x"), y);
        assertFalse(estimator.fit(test, VarDouble.fill(1, 1), "y"));
    }
}
