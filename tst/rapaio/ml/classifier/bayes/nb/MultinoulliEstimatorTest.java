package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.filter.FOneHotEncoding;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/2/20.
 */
public class MultinoulliEstimatorTest {

    @Test
    void testBuilders() {
        assertEquals("Multinoulli{tests=[x], lapaceSmoother=1}",
                MultinoulliEstimator.forName("x").name());
        assertEquals("Multinoulli{tests=[x], lapaceSmoother=1.2}",
                MultinoulliEstimator.forName(1.2, "x").name());

        assertEquals("Multinoulli{tests=[a,b], lapaceSmoother=1}",
                MultinoulliEstimator.forNames("a", "b").name());
        assertEquals("Multinoulli{tests=[a,b], lapaceSmoother=1.2}",
                MultinoulliEstimator.forNames(1.2, "a", "b").name());

        assertEquals("Multinoulli{tests=[a,b], lapaceSmoother=1}",
                MultinoulliEstimator.forRange(
                        SolidFrame.byVars(VarDouble.empty().name("a"), VarDouble.empty().name("b")),
                        VRange.onlyTypes(VType.DOUBLE)).name());
        assertEquals("Multinoulli{tests=[a,b], lapaceSmoother=1.2}",
                MultinoulliEstimator.forRange(1.2,
                        SolidFrame.byVars(VarDouble.empty().name("a"), VarDouble.empty().name("b")),
                        VRange.onlyTypes(VType.DOUBLE)).name());
    }

    @Test
    void testNewInstance() {
        var estimator = MultinoulliEstimator.forName(1.7, "b");
        var copy = estimator.newInstance();

        assertEquals(estimator.name(), copy.name());
        assertEquals(1.7, estimator.getLaplaceSmoother());
        assertEquals(estimator.getLaplaceSmoother(), copy.getLaplaceSmoother());
    }

    @Test
    void testEmptyNominal() {
        Var t = VarNominal.empty().name("t");
        Var x = VarNominal.empty().name("x");

        var estimator = MultinoulliEstimator.forName("x");
        estimator.fit(SolidFrame.byVars(t, x), VarDouble.empty(), "t");
        assertNotNull(estimator);
        assertEquals("Multinoulli{tests=[x], laplaceSmoother=1, values=[{targetLevel:?,[?:1,},]}",
                estimator.fittedName());
    }

    @Test
    void testFitPredict() {
        Var t = VarNominal.copy("a", "a", "a", "b", "b").name("t");
        Var x = VarNominal.copy("x", "x", "y", "y", "z").name("x");
        Frame df1 = SolidFrame.byVars(x, t);

        var estimator1 = MultinoulliEstimator.forName("x");
        estimator1.fit(df1, VarDouble.empty(5), "t");

        assertEquals("Multinoulli{tests=[x], laplaceSmoother=1, values=[" +
                "{targetLevel:?,[?:0.25,x:0.25,y:0.25,z:0.25,}," +
                "{targetLevel:a,[?:0.143,x:0.429,y:0.286,z:0.143,}," +
                "{targetLevel:b,[?:0.167,x:0.167,y:0.333,z:0.333,},]}", estimator1.fittedName());
        assertEquals(0.42857142857142855, estimator1.predict(df1, 0, "a"));

        Frame df2 = df1.fapply(FOneHotEncoding.on("x"));
        var estimator2 = MultinoulliEstimator.forRange(df2, VRange.byName(name -> !name.equals("t")));
        estimator2.fit(df2, VarDouble.empty(5), "t");
        assertEquals("Multinoulli{tests=[x.?,x.x,x.y,x.z], laplaceSmoother=1, values=[" +
                "{targetLevel:?,[x.?:0.25,x.x:0.25,x.y:0.25,x.z:0.25,}," +
                "{targetLevel:a,[x.?:0.143,x.x:0.429,x.y:0.286,x.z:0.143,}," +
                "{targetLevel:b,[x.?:0.167,x.x:0.167,x.y:0.333,x.z:0.333,},]}", estimator2.fittedName());
        assertEquals(0.42857142857142855, estimator2.predict(df2, 0, "a"));
    }
}
